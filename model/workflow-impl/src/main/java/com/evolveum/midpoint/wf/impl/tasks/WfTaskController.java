/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.wf.impl.tasks;

import com.evolveum.midpoint.audit.api.AuditEventRecord;
import com.evolveum.midpoint.audit.api.AuditEventStage;
import com.evolveum.midpoint.audit.api.AuditService;
import com.evolveum.midpoint.common.Clock;
import com.evolveum.midpoint.model.api.ModelInteractionService;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.api.PreconditionViolationException;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.wf.api.*;
import com.evolveum.midpoint.wf.impl.WfConfiguration;
import com.evolveum.midpoint.wf.impl.engine.WorkflowInterface;
import com.evolveum.midpoint.wf.impl.processors.ChangeProcessor;
import com.evolveum.midpoint.wf.impl.processors.primary.PcpWfTask;
import com.evolveum.midpoint.wf.impl.processors.primary.PrimaryChangeProcessor;
import com.evolveum.midpoint.wf.impl.util.MiscDataUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.datatype.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages everything related to a activiti process instance, including the task that monitors that process instance.
 *
 * This class provides a facade over ugly mess of code managing activiti + task pair describing a workflow process instance.
 *
 * @author mederly
 */
@Component
public class WfTaskController {

	//region Attributes
    private static final Trace LOGGER = TraceManager.getTrace(WfTaskController.class);

    public static final long TASK_START_DELAY = 5000L;
    public static final long COMPLETION_TRIGGER_EQUALITY_THRESHOLD = 10000L;

    private static final Object DOT_CLASS = WfTaskController.class.getName() + ".";

    private Set<ProcessListener> processListeners = ConcurrentHashMap.newKeySet();
    private Set<WorkItemListener> workItemListeners = ConcurrentHashMap.newKeySet();

    @Autowired private WfTaskUtil wfTaskUtil;
    @Autowired private TaskManager taskManager;
    @Autowired private WorkflowInterface workflowInterface;
    @Autowired private AuditService auditService;
    @Autowired private MiscDataUtil miscDataUtil;
    @Autowired private WfConfiguration wfConfiguration;
    @Autowired private PrismContext prismContext;
    @Autowired private Clock clock;
    @Autowired private ModelInteractionService modelInteractionService;
    //endregion

    //region Job creation & re-creation
    /**
     * Creates a background task, just as prescribed by the task creation instruction.
     * @param instruction the wf task creation instruction
     * @param parentWfTask the wf task that will be the parent of newly created one; it may be null
	 */
    public WfTask submitWfTask(WfTaskCreationInstruction instruction, WfTask parentWfTask, WfConfigurationType wfConfigurationType,
			OperationResult result) throws SchemaException, ObjectNotFoundException {
        return submitWfTask(instruction, parentWfTask.getTask(), wfConfigurationType, null, result);
    }

    /**
     * As above, but this time we know only the parent task (not a wf task).
     * @param instruction the wf task creation instruction
     * @param parentTask the task that will be the parent of the task of newly created wf-task; it may be null
	 */
    public WfTask submitWfTask(WfTaskCreationInstruction instruction, Task parentTask, WfConfigurationType wfConfigurationType,
			String channelOverride, OperationResult result) throws SchemaException, ObjectNotFoundException {
	    LOGGER.trace("Processing start instruction:\n{}", instruction.debugDumpLazily());
        Task task = submitTask(instruction, parentTask, wfConfigurationType, channelOverride, result);
		WfTask wfTask = recreateWfTask(task, instruction.getChangeProcessor());
        if (!instruction.isNoProcess()) {
            startWorkflowProcessInstance(wfTask, instruction, result);
        }
        return wfTask;
    }

    /**
     * Re-creates a job, based on existing task information.
     *
     * @param task a task from task-processInstance pair
     * @return recreated job
     */
    public WfTask recreateWfTask(Task task) {
		return recreateWfTask(task, wfTaskUtil.getChangeProcessor(task));
	}

    private WfTask recreateWfTask(Task task, ChangeProcessor changeProcessor) {
		String processInstanceId = wfTaskUtil.getCaseOid(task);
		if (changeProcessor instanceof PrimaryChangeProcessor) {
			return new PcpWfTask(this, task, processInstanceId, changeProcessor);
		} else {
			return new WfTask(this, task, processInstanceId, changeProcessor);
		}
    }

    /**
     * Re-creates a child job, knowing the task and the parent job.
     *
     * @param subtask a task from task-processInstance pair
     * @param parentWfTask the parent job
     * @return recreated job
     */
    public WfTask recreateChildWfTask(Task subtask, WfTask parentWfTask) {
        return new WfTask(this, subtask, wfTaskUtil.getCaseOid(subtask), parentWfTask.getChangeProcessor());
    }

    /**
     * Re-creates a root job, based on existing task information. Does not try to find the wf process instance.
     */
    public WfTask recreateRootWfTask(Task task) {
        return new WfTask(this, task, wfTaskUtil.getChangeProcessor(task));
    }
    //endregion

    //region Working with midPoint tasks

    private Task submitTask(WfTaskCreationInstruction instruction, Task parentTask, WfConfigurationType wfConfigurationType, String channelOverride, OperationResult result) throws SchemaException {
		Task wfTask = instruction.createTask(this, parentTask, wfConfigurationType);
		if (channelOverride != null) {
			wfTask.setChannel(channelOverride);
		}
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Switching workflow root or child task to background:\n{}", wfTask.debugDump());
        }
        taskManager.switchToBackground(wfTask, result);
        return wfTask;
    }

    /**
     * Beware, in order to make the change permanent, it is necessary to call commitChanges on
     * "executesFirst". It is advisable not to modify underlying tasks between 'addDependency'
     * and 'commitChanges' because of the savePendingModifications() mechanism that is used here.
     */
    public void addDependency(WfTask executesFirst, WfTask executesSecond) {
        Validate.notNull(executesFirst.getTask());
        Validate.notNull(executesSecond.getTask());
        LOGGER.trace("Setting dependency of {} on 'task0' {}", executesSecond, executesFirst);
        executesFirst.getTask().addDependent(executesSecond.getTask().getTaskIdentifier());
    }

    public void resumeTask(WfTask wfTask, OperationResult result) throws SchemaException, ObjectNotFoundException {
        taskManager.resumeTask(wfTask.getTask(), result);
    }

    public void unpauseTask(WfTask wfTask, OperationResult result) throws SchemaException, ObjectNotFoundException {
	    try {
		    taskManager.unpauseTask(wfTask.getTask(), result);
	    } catch (PreconditionViolationException e) {
		    throw new SystemException("Task " + wfTask + " cannot be unpaused because it is no longer in WAITING state (should not occur)");
	    }
    }
    //endregion

    //region Working with workflow process instances

    private void startWorkflowProcessInstance(WfTask wfTask, WfTaskCreationInstruction<?,?> instruction, OperationResult parentResult) {
		OperationResult result = parentResult.createSubresult(DOT_CLASS + "startWorkflowProcessInstance");
        try {
			LOGGER.trace("startWorkflowProcessInstance starting; instruction = {}", instruction);
			Task task = wfTask.getTask();
			workflowInterface.startWorkflowProcessInstance(instruction, task, result);
        } catch (SchemaException|RuntimeException|ObjectNotFoundException|ObjectAlreadyExistsException e) {
            LoggingUtils.logUnexpectedException(LOGGER, "Couldn't send a request to start a process instance to workflow management system", e);
			result.recordFatalError("Couldn't send a request to start a process instance to workflow management system: " + e.getMessage(), e);
            throw new SystemException("Workflow process instance creation could not be requested", e);
        } finally {
			result.computeStatusIfUnknown();
		}
        LOGGER.trace("startWorkflowProcessInstance finished");
    }

//    // skipProcessEndNotification is a bit of hack: It is to avoid sending process end notification twice if the process ends
//	// in the same thread in which it was started (MID-4850). It could be probably solved in a more brave way e.g. by removing
//	// the whole onProcessEvent call in startWorkflowProcessInstance but that could have other consequences.
//	//
//	// We get rid of these hacks when we replace Activiti with our own implementation (4.0 or 4.1).
//    public void onProcessEvent(ProcessEvent event, boolean skipProcessEndNotification, Task task, OperationResult result)
//			throws ObjectAlreadyExistsException, ObjectNotFoundException, SchemaException {
//        WfTask wfTask = recreateWfTask(task);
//
//		LOGGER.trace("Updating instance state and activiti process instance ID in task {}", task);
//
//		if (!skipProcessEndNotification && (event instanceof ProcessFinishedEvent || !event.isRunning())) {
//            onProcessFinishedEvent(event, wfTask, result);
//        }
//    }

//    private ChangeProcessor getChangeProcessor(WfContextType wfContextType) {
//        String cpName = wfContextType.getChangeProcessor();
//        Validate.notNull(cpName, "Change processor is not defined in the workflow context");
//        return wfConfiguration.findChangeProcessor(cpName);
//    }

    //endregion

    //region Processing work item (task) events

//	// workItem contains taskRef, assignee, candidates resolved (if possible)
//	// workItem can be freely modified (e.g. by overriding result, etc.)
//	@SuppressWarnings("unchecked")
//    public void onTaskEvent(WorkItemType workItem, WorkItemEvent workItemEvent, OperationResult result) throws SchemaException {
//
//		final TaskType shadowTaskType = WfContextUtil.getTask(workItem);
//		if (shadowTaskType == null) {
//			LOGGER.warn("No task in workItem " + workItem + ", audit and notifications couldn't be performed.");
//			return;
//		}
//		final Task shadowTask = taskManager.createTaskInstance(shadowTaskType.asPrismObject(), result);
//		final WfTask wfTask = recreateWfTask(shadowTask);
//
//		// auditing & notifications & event
//        if (workItemEvent instanceof WorkItemCreatedEvent) {
//        	// moved
////			AuditEventRecord auditEventRecord = getChangeProcessor(workItemEvent).prepareWorkItemCreatedAuditRecord(workItem,
////					wfTask, result);
////			auditService.audit(auditEventRecord, wfTask.getTask());
////            try {
////				List<ObjectReferenceType> assigneesAndDeputies = getAssigneesAndDeputies(workItem, wfTask, result);
////				for (ObjectReferenceType assigneesOrDeputy : assigneesAndDeputies) {
////					notifyWorkItemCreated(assigneesOrDeputy, workItem, wfTask, result);		// we assume originalAssigneeRef == assigneeRef in this case
////				}
////				WorkItemAllocationChangeOperationInfo operationInfo =
////						new WorkItemAllocationChangeOperationInfo(null, Collections.emptyList(), assigneesAndDeputies);
////				notifyWorkItemAllocationChangeNewActors(workItem, operationInfo, null, wfTask.getTask(), result);
////            } catch (SchemaException e) {
////                LoggingUtils.logUnexpectedException(LOGGER, "Couldn't send notification about work item create event", e);
////            }
//        } else if (workItemEvent instanceof WorkItemDeletedEvent) {
////        	// this might be cancellation because of:
////			//  (1) user completion of this task
////			//  (2) timed completion of this task
////			//  (3) user completion of another task
////			//  (4) timed completion of another task
////			//  (5) process stop/deletion
////			//
////			// Actually, when the source is (4) timed completion of another task, it is quite probable that this task
////			// would be closed for the same reason. For a user it would be misleading if we would simply view this task
////			// as 'cancelled', while, in fact, it is e.g. approved/rejected because of a timed action.
////
////			WorkItemOperationKindType operationKind = BooleanUtils.isTrue(ActivitiUtil.getVariable(workItemEvent.getVariables(),
////					CommonProcessVariableNames.VARIABLE_WORK_ITEM_WAS_COMPLETED, Boolean.class, prismContext)) ?
////					WorkItemOperationKindType.COMPLETE : WorkItemOperationKindType.CANCEL;
////			WorkItemEventCauseInformationType cause = ActivitiUtil.getVariable(workItemEvent.getVariables(),
////					CommonProcessVariableNames.VARIABLE_CAUSE, WorkItemEventCauseInformationType.class, prismContext);
////			boolean genuinelyCompleted = operationKind == WorkItemOperationKindType.COMPLETE;
////
////			MidPointPrincipal user;
////			try {
////				user = SecurityUtil.getPrincipal();
////			} catch (SecurityViolationException e) {
////				throw new SystemException("Couldn't determine current user: " + e.getMessage(), e);
////			}
////
////			ObjectReferenceType userRef = user != null ? user.toObjectReference() : workItem.getPerformerRef();	// partial fallback
////
////			if (!genuinelyCompleted) {
////				TaskType task = wfTask.getTask().getTaskPrismObject().asObjectable();
////				int foundTimedActions = 0;
////				for (TriggerType trigger : task.getTrigger()) {
////					if (!WfTimedActionTriggerHandler.HANDLER_URI.equals(trigger.getHandlerUri())) {
////						continue;
////					}
////					String workItemId = ObjectTypeUtil.getExtensionItemRealValue(trigger.getExtension(), SchemaConstants.MODEL_EXTENSION_WORK_ITEM_ID);
////					if (!workItemEvent.getTaskId().equals(workItemId)) {
////						continue;
////					}
////					Duration timeBeforeAction = ObjectTypeUtil.getExtensionItemRealValue(trigger.getExtension(), SchemaConstants.MODEL_EXTENSION_TIME_BEFORE_ACTION);
////					if (timeBeforeAction != null) {
////						continue;
////					}
////					WorkItemActionsType actions = ObjectTypeUtil.getExtensionItemRealValue(trigger.getExtension(), SchemaConstants.MODEL_EXTENSION_WORK_ITEM_ACTIONS);
////					if (actions == null || actions.getComplete() == null) {
////						continue;
////					}
////					long diff = XmlTypeConverter.toMillis(trigger.getTimestamp()) - clock.currentTimeMillis();
////					if (diff >= COMPLETION_TRIGGER_EQUALITY_THRESHOLD) {
////						continue;
////					}
////					CompleteWorkItemActionType completeAction = actions.getComplete();
////					operationKind = WorkItemOperationKindType.COMPLETE;
////					cause = new WorkItemEventCauseInformationType();
////					cause.setType(WorkItemEventCauseTypeType.TIMED_ACTION);
////					cause.setName(completeAction.getName());
////					cause.setDisplayName(completeAction.getDisplayName());
////					foundTimedActions++;
////					WorkItemResultType workItemOutput = new WorkItemResultType();
////					workItemOutput.setOutcome(completeAction.getOutcome() != null ? completeAction.getOutcome() : SchemaConstants.MODEL_APPROVAL_OUTCOME_REJECT);
////					workItem.setOutput(workItemOutput);
////				}
////				if (foundTimedActions > 1) {
////					LOGGER.warn("Multiple 'work item complete' timed actions ({}) for {}: {}", foundTimedActions,
////							ObjectTypeUtil.toShortString(task), task.getTrigger());
////				}
////			}
////
////			// We don't pass userRef (initiator) to the audit method. It does need the whole object (not only the reference),
////			// so it fetches it directly from the security enforcer (logged-in user). This could change in the future.
////			AuditEventRecord auditEventRecord = getChangeProcessor(workItemEvent)
////					.prepareWorkItemDeletedAuditRecord(workItem, cause, workItemEvent, wfTask, result);
////			auditService.audit(auditEventRecord, wfTask.getTask());
////            try {
////				List<ObjectReferenceType> assigneesAndDeputies = getAssigneesAndDeputies(workItem, wfTask, result);
////				WorkItemAllocationChangeOperationInfo operationInfo =
////						new WorkItemAllocationChangeOperationInfo(operationKind, assigneesAndDeputies, null);
////				WorkItemOperationSourceInfo sourceInfo = new WorkItemOperationSourceInfo(userRef, cause, null);
////            	if (workItem.getAssigneeRef().isEmpty()) {
////					notifyWorkItemDeleted(null, workItem, operationInfo, sourceInfo, wfTask, result);
////				} else {
////					for (ObjectReferenceType assigneeOrDeputy : assigneesAndDeputies) {
////						notifyWorkItemDeleted(assigneeOrDeputy, workItem, operationInfo, sourceInfo, wfTask, result);
////					}
////				}
////				notifyWorkItemAllocationChangeCurrentActors(workItem, operationInfo, sourceInfo, null, wfTask.getTask(), result);
////            } catch (SchemaException e) {
////                LoggingUtils.logUnexpectedException(LOGGER, "Couldn't audit work item complete event", e);
////            }
////
////			AbstractWorkItemOutputType output = workItem.getOutput();
////			if (genuinelyCompleted || output != null) {
////				WorkItemCompletionEventType event = new WorkItemCompletionEventType();
////				ActivitiUtil.fillInWorkItemEvent(event, user, workItemEvent.getTaskId(), workItemEvent.getVariables(), prismContext);
////				event.setCause(cause);
////				event.setOutput(output);
////				ObjectDeltaType additionalDelta = output instanceof WorkItemResultType && ((WorkItemResultType) output).getAdditionalDeltas() != null ?
////						((WorkItemResultType) output).getAdditionalDeltas().getFocusPrimaryDelta() : null;
////				MidpointUtil.recordEventInTask(event, additionalDelta, wfTask.getTask().getOid(), result);
////			}
////
////			MidpointUtil.removeTriggersForWorkItem(wfTask.getTask(), workItemEvent.getTaskId(), result);
//		}
//    }

	public List<ObjectReferenceType> getAssigneesAndDeputies(WorkItemType workItem, WfTask wfTask, OperationResult result)
			throws SchemaException {
    	return getAssigneesAndDeputies(workItem, wfTask.getTask(), result);
	}

	public List<ObjectReferenceType> getAssigneesAndDeputies(WorkItemType workItem, Task task, OperationResult result)
			throws SchemaException {
    	List<ObjectReferenceType> rv = new ArrayList<>();
    	rv.addAll(workItem.getAssigneeRef());
		rv.addAll(modelInteractionService.getDeputyAssignees(workItem, task, result));
		return rv;
	}
	//endregion

    //region Auditing and notifications
    public void auditProcessStart(WfTask wfTask, WfContextType wfContext, OperationResult result) {
        auditProcessStartEnd(wfTask, AuditEventStage.REQUEST, wfContext, result);
    }

    public void auditProcessEnd(WfTask wfTask, WfContextType wfContext, OperationResult result) {
        auditProcessStartEnd(wfTask, AuditEventStage.EXECUTION, wfContext, result);
    }

    private void auditProcessStartEnd(WfTask wfTask, AuditEventStage stage, WfContextType wfContext, OperationResult result) {
        AuditEventRecord auditEventRecord = wfTask.getChangeProcessor().prepareProcessInstanceAuditRecord(wfTask, stage, wfContext, result);
        auditService.audit(auditEventRecord, wfTask.getTask());
    }

    public void notifyProcessStart(Task task, OperationResult result) throws SchemaException {
        for (ProcessListener processListener : processListeners) {
            processListener.onProcessInstanceStart(task, result);
        }
    }

    public void notifyProcessEnd(WfTask wfTask, OperationResult result) throws SchemaException {
        for (ProcessListener processListener : processListeners) {
            processListener.onProcessInstanceEnd(wfTask.getTask(), result);
        }
    }

    public void notifyWorkItemCreated(ObjectReferenceType originalAssigneeRef, WorkItemType workItem,
			WfTask wfTask, OperationResult result) throws SchemaException {
        for (WorkItemListener workItemListener : workItemListeners) {
            workItemListener.onWorkItemCreation(originalAssigneeRef, workItem, wfTask.getTask(), result);
        }
    }

    public void notifyWorkItemDeleted(ObjectReferenceType assignee, WorkItemType workItem,
		    WorkItemOperationInfo operationInfo, WorkItemOperationSourceInfo sourceInfo,
		    WfTask wfTask, OperationResult result) throws SchemaException {
        for (WorkItemListener workItemListener : workItemListeners) {
            workItemListener.onWorkItemDeletion(assignee, workItem, operationInfo, sourceInfo, wfTask.getTask(), result);
        }
    }

    public void notifyWorkItemAllocationChangeCurrentActors(WorkItemType workItem,
			@NotNull WorkItemAllocationChangeOperationInfo operationInfo,
			WorkItemOperationSourceInfo sourceInfo, Duration timeBefore,
			Task wfTask, OperationResult result) throws SchemaException {
        for (WorkItemListener workItemListener : workItemListeners) {
            workItemListener.onWorkItemAllocationChangeCurrentActors(workItem, operationInfo, sourceInfo, timeBefore, wfTask, result);
        }
    }

    public void notifyWorkItemAllocationChangeNewActors(WorkItemType workItem,
			@NotNull WorkItemAllocationChangeOperationInfo operationInfo,
			@Nullable WorkItemOperationSourceInfo sourceInfo,
			Task wfTask, OperationResult result) throws SchemaException {
        for (WorkItemListener workItemListener : workItemListeners) {
            workItemListener.onWorkItemAllocationChangeNewActors(workItem, operationInfo, sourceInfo, wfTask, result);
        }
    }

    public void notifyWorkItemCustom(@Nullable ObjectReferenceType assignee, WorkItemType workItem,
			WorkItemEventCauseInformationType cause, Task wfTask,
			@NotNull WorkItemNotificationActionType notificationAction,
			OperationResult result) throws SchemaException {
        for (WorkItemListener workItemListener : workItemListeners) {
            workItemListener.onWorkItemCustomEvent(assignee, workItem, notificationAction, cause, wfTask, result);
        }
    }

    public void registerProcessListener(ProcessListener processListener) {
		LOGGER.trace("Registering process listener {}", processListener);
        processListeners.add(processListener);
    }

    public void registerWorkItemListener(WorkItemListener workItemListener) {
		LOGGER.trace("Registering work item listener {}", workItemListener);
        workItemListeners.add(workItemListener);
    }
    //endregion

    //region Getters and setters
    public WfTaskUtil getWfTaskUtil() {
        return wfTaskUtil;
    }

	public MiscDataUtil getMiscDataUtil() {
		return miscDataUtil;
	}

	public PrismContext getPrismContext() {
        return prismContext;
    }

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public WfConfiguration getWfConfiguration() {
		return wfConfiguration;
	}

	//endregion
}
