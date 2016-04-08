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

package com.evolveum.midpoint.web.page.admin.server.dto;

import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.gui.api.util.WebModelServiceUtils;
import com.evolveum.midpoint.model.api.ModelInteractionService;
import com.evolveum.midpoint.model.api.ModelPublicConstants;
import com.evolveum.midpoint.model.api.ModelService;
import com.evolveum.midpoint.model.api.TaskService;
import com.evolveum.midpoint.model.api.context.ModelContext;
import com.evolveum.midpoint.model.api.visualizer.Scene;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismProperty;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.ObjectTreeDeltas;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskCategory;
import com.evolveum.midpoint.task.api.TaskExecutionStatus;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.*;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.data.column.InlineMenuable;
import com.evolveum.midpoint.web.component.menu.cog.InlineMenuItem;
import com.evolveum.midpoint.web.component.model.operationStatus.ModelOperationStatusDto;
import com.evolveum.midpoint.web.component.prism.show.SceneDto;
import com.evolveum.midpoint.web.component.prism.show.SceneUtil;
import com.evolveum.midpoint.web.component.util.Selectable;
import com.evolveum.midpoint.web.page.admin.server.handlers.HandlerDtoFactory;
import com.evolveum.midpoint.web.page.admin.server.handlers.dto.HandlerDto;
import com.evolveum.midpoint.web.page.admin.server.handlers.dto.ResourceRelatedHandlerDto;
import com.evolveum.midpoint.web.page.admin.workflow.dto.ProcessInstanceDto;
import com.evolveum.midpoint.web.page.admin.workflow.dto.WorkItemDto;
import com.evolveum.midpoint.web.security.MidPointApplication;
import com.evolveum.midpoint.wf.api.WorkflowManager;
import com.evolveum.midpoint.wf.util.ChangesByState;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ExecuteScriptType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;
import org.apache.commons.lang.Validate;
import org.apache.wicket.Application;
import org.jetbrains.annotations.Nullable;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.evolveum.midpoint.schema.GetOperationOptions.createRetrieve;
import static com.evolveum.midpoint.schema.SelectorOptions.createCollection;

/**
 * @author lazyman
 * @author mederly
 */
public class TaskDto extends Selectable implements InlineMenuable {

    public static final String CLASS_DOT = TaskDto.class.getName() + ".";
    public static final String OPERATION_NEW = CLASS_DOT + "new";

    private static final transient Trace LOGGER = TraceManager.getTrace(TaskDto.class);
    public static final String F_MODEL_OPERATION_STATUS = "modelOperationStatus";
    public static final String F_SUBTASKS = "subtasks";
    public static final String F_NAME = "name";
    public static final String F_DESCRIPTION = "description";
    public static final String F_CATEGORY = "category";
    public static final String F_PARENT_TASK_NAME = "parentTaskName";
    public static final String F_PARENT_TASK_OID = "parentTaskOid";
	public static final String F_OWNER_NAME = "ownerName";
	public static final String F_OWNER_OID = "ownerOid";
    public static final String F_WORKFLOW_DELTAS_IN = "workflowDeltasIn";
    public static final String F_WORKFLOW_DELTA_IN = "workflowDeltaIn";
    public static final String F_WORKFLOW_DELTAS_OUT = "workflowDeltasOut";
    public static final String F_IDENTIFIER = "identifier";
    public static final String F_HANDLER_URI_LIST = "handlerUriList";
    public static final String F_TASK_OPERATION_RESULT = "taskOperationResult";
    public static final String F_PROGRESS_DESCRIPTION = "progressDescription";
    public static final String F_WORKER_THREADS = "workerThreads";
    public static final String F_OP_RESULT = "opResult";
	public static final String F_WORKFLOW_CONTEXT = "workflowContext";
	public static final String F_WORK_ITEMS = "workItems";
	public static final String F_WORKFLOW_REQUESTS = "workflowRequests";
	public static final String F_RECURRING = "recurring";
	public static final String F_BOUND = "bound";
	public static final String F_INTERVAL = "interval";
	public static final String CRON_SPECIFICATION = "cronSpecification";
	public static final String F_NOT_START_BEFORE = "notStartBefore";
	public static final String F_NOT_START_AFTER = "notStartAfter";
	public static final String F_MISFIRE_ACTION = "misfireActionType";
	public static final String F_OBJECT_REF_NAME = "objectRefName";
	public static final String F_OBJECT_TYPE = "objectType";
	public static final String F_OBJECT_QUERY = "objectQuery";
	public static final String F_OBJECT_DELTA = "objectDelta";
	public static final String F_SCRIPT = "script";
	public static final String F_EXECUTE_IN_RAW_MODE = "executeInRawMode";
	public static final String F_PROCESS_INSTANCE_ID = "processInstanceId";
	public static final String F_HANDLER_DTO = "handlerDto";

	private TaskType taskType;

	// editable properties
	private String name;
	private String description;

	private boolean bound;
	private boolean recurring;
	private Integer interval;
	private String cronSpecification;
	private Date notStartBefore;
	private Date notStartAfter;
	private MisfireActionType misfireActionType;
	private ThreadStopActionType threadStopActionType;

	private Integer workerThreads;

	// simple computed properties (optimization)
	private List<String> handlerUriList;
	private List<OperationResult> opResult;
	private OperationResult taskOperationResult;
	private ModelOperationStatusDto modelOperationStatusDto;

	private List<TaskChangesDto> changes;
	private List<ProcessInstanceDto> workflowRequests;
	private List<SceneDto> workflowDeltasIn, workflowDeltasOut;
	private SceneDto workflowDeltaIn;

	// related objects
	private TaskType parentTaskType;
	private ObjectTypes objectRefType;
	private String objectRefName;
	private ObjectReferenceType objectRef;

	private List<TaskDto> subtasks = new ArrayList<>();          // only persistent subtasks are here
	private List<TaskDto> transientSubtasks = new ArrayList<>();        // transient ones are here

	// other
	private List<InlineMenuItem> menuItems;
	private HandlerDto handlerDto;

    //region Construction
    public TaskDto(TaskType taskType, ModelService modelService, TaskService taskService, ModelInteractionService modelInteractionService,
			TaskManager taskManager, WorkflowManager workflowManager, TaskDtoProviderOptions options,
			Task opTask, OperationResult parentResult, PageBase pageBase) throws SchemaException, ObjectNotFoundException {
        Validate.notNull(taskType, "Task must not be null.");
        Validate.notNull(modelService);
        Validate.notNull(taskService);
        Validate.notNull(modelInteractionService);
        Validate.notNull(taskManager);
        Validate.notNull(parentResult);
        Validate.notNull(pageBase);

        this.taskType = taskType;
		this.name = taskType.getName() != null ? taskType.getName().getOrig() : null;
		this.description = taskType.getDescription();

		fillInScheduleAttributes(taskType);

        OperationResult thisOpResult = parentResult.createMinorSubresult(OPERATION_NEW);
        fillInHandlerUriList(taskType);
        fillInObjectRefAttributes(taskType, options, pageBase, opTask, thisOpResult);
        fillInParentTaskAttributes(taskType, taskService, options, thisOpResult);
        fillInOperationResultAttributes(taskType);
        if (options.isRetrieveModelContext()) {
            fillInModelContext(taskType, modelInteractionService, opTask, thisOpResult);
        }
		if (options.isRetrieveWorkflowContext()) {
			// TODO fill-in "cheap" wf attributes not only when this option is set
			fillInWorkflowAttributes(taskType, modelInteractionService, workflowManager, pageBase.getPrismContext(), opTask, thisOpResult);
		}
        thisOpResult.computeStatusIfUnknown();

        fillFromExtension();

        for (TaskType child : taskType.getSubtask()) {
            addChildTaskDto(new TaskDto(child, modelService, taskService, modelInteractionService, taskManager,
					workflowManager, options, opTask, parentResult, pageBase));
        }

		if (options.isCreateHandlerDto()) {
			handlerDto = HandlerDtoFactory.instance().createDtoForTask(this, pageBase, opTask, thisOpResult);
		} else {
			handlerDto = new HandlerDto(this);		// just to avoid NPEs
		}
    }

    @Override
    public List<InlineMenuItem> getMenuItems() {
        if (menuItems == null) {
            menuItems = new ArrayList<>();
        }
        return menuItems;
    }

    private void fillFromExtension() {
        PrismProperty<Integer> workerThreadsItem = getExtensionProperty(SchemaConstants.MODEL_EXTENSION_WORKER_THREADS);
        if (workerThreadsItem != null) {
            workerThreads = workerThreadsItem.getRealValue();
        }
    }

    private Long xgc2long(XMLGregorianCalendar gc) {
        return gc != null ? XmlTypeConverter.toMillis(gc) : null;
    }

    private void fillInHandlerUriList(TaskType taskType) {
        handlerUriList = new ArrayList<>();
        if (taskType.getHandlerUri() != null) {
            handlerUriList.add(taskType.getHandlerUri());
        } else {
            handlerUriList.add("-");        // todo separate presentation from model
        }
        if (taskType.getOtherHandlersUriStack() != null) {
            List<UriStackEntry> stack = taskType.getOtherHandlersUriStack().getUriStackEntry();
            for (int i = stack.size()-1; i >= 0; i--) {
                handlerUriList.add(stack.get(i).getHandlerUri());
            }
        }
    }

    private void fillInScheduleAttributes(TaskType taskType) {
		this.recurring = taskType.getRecurrence() == TaskRecurrenceType.RECURRING;
		this.bound = taskType.getBinding() == TaskBindingType.TIGHT;
		this.threadStopActionType = taskType.getThreadStopAction();
		if (taskType.getSchedule() != null) {
            interval = taskType.getSchedule().getInterval();
            cronSpecification = taskType.getSchedule().getCronLikePattern();
            if (taskType.getSchedule().getMisfireAction() == null){
                misfireActionType = MisfireActionType.EXECUTE_IMMEDIATELY;
            } else {
                misfireActionType = taskType.getSchedule().getMisfireAction();
            }
            notStartBefore = MiscUtil.asDate(taskType.getSchedule().getEarliestStartTime());
            notStartAfter = MiscUtil.asDate(taskType.getSchedule().getLatestStartTime());
        }
    }

    private void fillInObjectRefAttributes(TaskType taskType, TaskDtoProviderOptions options, PageBase pageBase, Task opTask, OperationResult thisOpResult) {
        if (taskType.getObjectRef() != null) {
			if (taskType.getObjectRef().getType() != null) {
				this.objectRefType = ObjectTypes.getObjectTypeFromTypeQName(taskType.getObjectRef().getType());
			}
			if (options.isResolveObjectRef()) {
				this.objectRefName = getTaskObjectName(taskType, pageBase, opTask, thisOpResult);
			}
			this.objectRef = taskType.getObjectRef();
		}
    }

    public String getTaskObjectName(TaskType taskType, PageBase pageBase, Task opTask, OperationResult thisOpResult) {
		return WebModelServiceUtils.resolveReferenceName(taskType.getObjectRef(), pageBase, opTask, thisOpResult);
    }

    private void fillInParentTaskAttributes(TaskType taskType, TaskService taskService, TaskDtoProviderOptions options, OperationResult thisOpResult) {
        if (options.isGetTaskParent() && taskType.getParent() != null) {
            try {
				Collection<SelectorOptions<GetOperationOptions>> getOptions =
						options.isRetrieveSiblings() ? createCollection(TaskType.F_SUBTASK, createRetrieve()) : null;
                parentTaskType = taskService.getTaskByIdentifier(taskType.getParent(), getOptions, thisOpResult).asObjectable();
            } catch (SchemaException|ObjectNotFoundException|SecurityViolationException|ConfigurationException e) {
                LoggingUtils.logException(LOGGER, "Couldn't retrieve parent task for task {}", e, taskType.getOid());
            }
        }
    }

    private void fillInOperationResultAttributes(TaskType taskType) {
        opResult = new ArrayList<OperationResult>();
        if (taskType.getResult() != null) {
            taskOperationResult = OperationResult.createOperationResult(taskType.getResult());
            opResult.add(taskOperationResult);
            opResult.addAll(taskOperationResult.getSubresults());
        }
    }

    private void fillInModelContext(TaskType taskType, ModelInteractionService modelInteractionService, Task opTask, OperationResult result) throws ObjectNotFoundException {
        ModelContext ctx = unwrapModelContext(taskType, modelInteractionService, opTask, result);
		if (ctx != null) {
			modelOperationStatusDto = new ModelOperationStatusDto(ctx, modelInteractionService, opTask, result);
		}
    }

	private ModelContext unwrapModelContext(TaskType taskType, ModelInteractionService modelInteractionService, Task opTask, OperationResult result) throws ObjectNotFoundException {
		LensContextType lensContextType = taskType.getModelOperationContext();
		if (lensContextType != null) {
			try {
				return modelInteractionService.unwrapModelContext(lensContextType, result);
			} catch (SchemaException | CommunicationException | ConfigurationException e) {   // todo treat appropriately
				throw new SystemException("Couldn't access model operation context in task: " + e.getMessage(), e);
			}
		} else {
			return null;
		}
	}

    private void fillInWorkflowAttributes(TaskType taskType, ModelInteractionService modelInteractionService, WorkflowManager workflowManager,
			PrismContext prismContext, Task opTask,
			OperationResult thisOpResult) throws SchemaException, ObjectNotFoundException {

        workflowDeltasIn = retrieveDeltasToProcess(taskType, modelInteractionService, opTask, thisOpResult);
        workflowDeltaIn = retrieveDeltaToProcess(taskType, modelInteractionService, opTask, thisOpResult);
        workflowDeltasOut = retrieveResultingDeltas(taskType, modelInteractionService, opTask, thisOpResult);

		final TaskType rootTask;
		if (parentTaskType == null) {
			rootTask = taskType;
		} else {
			rootTask = parentTaskType;
		}

		workflowRequests = new ArrayList<>();
		for (TaskType wfSubtask : rootTask.getSubtask()) {
			final WfContextType subWfc = wfSubtask.getWorkflowContext();
			if (subWfc != null && subWfc.getProcessInstanceId() != null) {
				if (this.getOid() == null || !this.getOid().equals(wfSubtask.getOid())) {
					workflowRequests.add(new ProcessInstanceDto(wfSubtask));
				}
			}
		}

		changes = new ArrayList<>();
		ChangesByState changesByState = workflowManager.getChangesByState(rootTask, modelInteractionService, prismContext, thisOpResult);
		if (!changesByState.getApplied().isEmpty()) {
			changes.add(createTaskChangesDto("TaskDto.changesApplied", "box-solid box-success", changesByState.getApplied(), modelInteractionService, prismContext, opTask, thisOpResult));
		}
		if (!changesByState.getBeingApplied().isEmpty()) {
			changes.add(createTaskChangesDto("TaskDto.changesBeingApplied", "box-solid box-info", changesByState.getBeingApplied(), modelInteractionService, prismContext, opTask, thisOpResult));
		}
		if (!changesByState.getWaitingToBeApplied().isEmpty()) {
			changes.add(createTaskChangesDto("TaskDto.changesWaitingToBeApplied", "box-solid box-warning", changesByState.getWaitingToBeApplied(), modelInteractionService, prismContext, opTask, thisOpResult));
		}
		if (!changesByState.getWaitingToBeApproved().isEmpty()) {
			changes.add(createTaskChangesDto("TaskDto.changesWaitingToBeApproved", "box-solid box-primary", changesByState.getWaitingToBeApproved(), modelInteractionService, prismContext, opTask, thisOpResult));
		}
		if (!changesByState.getRejected().isEmpty()) {
			changes.add(createTaskChangesDto("TaskDto.changesRejected", "box-solid box-danger", changesByState.getRejected(), modelInteractionService, prismContext, opTask, thisOpResult));
		}
	}

	private TaskChangesDto createTaskChangesDto(String titleKey, String boxClassOverride, ObjectTreeDeltas deltas, ModelInteractionService modelInteractionService,
			PrismContext prismContext, Task opTask, OperationResult result) throws SchemaException {
		ObjectTreeDeltasType deltasType = ObjectTreeDeltas.toObjectTreeDeltasType(deltas);
		Scene scene = SceneUtil.visualizeObjectTreeDeltas(deltasType, titleKey, prismContext, modelInteractionService, opTask, result);
		SceneDto sceneDto = new SceneDto(scene);
		sceneDto.setBoxClassOverride(boxClassOverride);
		return new TaskChangesDto(sceneDto);
	}

	private List<SceneDto> retrieveDeltasToProcess(TaskType taskType, ModelInteractionService modelInteractionService, Task opTask,
			OperationResult thisOpResult) throws SchemaException {
        WfContextType wfc = taskType.getWorkflowContext();
        if (wfc == null || !(wfc.getProcessorSpecificState() instanceof WfPrimaryChangeProcessorStateType)) {
            return null;
        }
        WfPrimaryChangeProcessorStateType pcps = (WfPrimaryChangeProcessorStateType) wfc.getProcessorSpecificState();
        return objectTreeDeltasToDeltaDtoList(pcps.getDeltasToProcess(), taskType.asPrismObject().getPrismContext(), modelInteractionService, opTask, thisOpResult);
    }

	private SceneDto retrieveDeltaToProcess(TaskType taskType, ModelInteractionService modelInteractionService, Task opTask,
			OperationResult thisOpResult) throws SchemaException {
		WfContextType wfc = taskType.getWorkflowContext();
		if (wfc == null || !(wfc.getProcessorSpecificState() instanceof WfPrimaryChangeProcessorStateType)) {
			return null;
		}
		WfPrimaryChangeProcessorStateType pcps = (WfPrimaryChangeProcessorStateType) wfc.getProcessorSpecificState();
		Scene scene = SceneUtil.visualizeObjectTreeDeltas(pcps.getDeltasToProcess(), "", taskType.asPrismObject().getPrismContext(),
				modelInteractionService, opTask, thisOpResult);
		return new SceneDto(scene);
	}

    private List<SceneDto> objectTreeDeltasToDeltaDtoList(ObjectTreeDeltasType deltas, PrismContext prismContext,
			ModelInteractionService modelInteractionService, Task opTask, OperationResult thisOpResult) throws SchemaException {
        List<SceneDto> retval = new ArrayList<>();
		if (deltas == null) {
			return retval;
		}
		Scene wrapperScene = SceneUtil.visualizeObjectTreeDeltas(deltas, "", prismContext, modelInteractionService, opTask, thisOpResult);
		for (Scene scene : wrapperScene.getPartialScenes()) {
			retval.add(new SceneDto(scene));
		}
        return retval;
    }

    private List<SceneDto> retrieveResultingDeltas(TaskType taskType, ModelInteractionService modelInteractionService, Task opTask,
			OperationResult thisOpResult) throws SchemaException {
        WfContextType wfc = taskType.getWorkflowContext();
        if (wfc == null || !(wfc.getProcessorSpecificState() instanceof WfPrimaryChangeProcessorStateType)) {
            return null;
        }
        WfPrimaryChangeProcessorStateType pcps = (WfPrimaryChangeProcessorStateType) wfc.getProcessorSpecificState();
        return objectTreeDeltasToDeltaDtoList(pcps.getResultingDeltas(), taskType.asPrismObject().getPrismContext(), modelInteractionService, opTask,
				thisOpResult);
    }

    //endregion

	//region Getters and setters for read-write properties

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRecurring() {
		return recurring;
	}

	public void setRecurring(boolean recurring) {
		this.recurring = recurring;
	}

	public boolean isBound() {
		return bound;
	}

	public void setBound(boolean bound) {
		this.bound = bound;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public String getCronSpecification() {
		return cronSpecification;
	}

	public void setCronSpecification(String cronSpecification) {
		this.cronSpecification = cronSpecification;
	}

	public Date getNotStartBefore() {
		return notStartBefore;
	}

	public void setNotStartBefore(Date notStartBefore) {
		this.notStartBefore = notStartBefore;
	}

	public Date getNotStartAfter() {
		return notStartAfter;
	}

	public void setNotStartAfter(Date notStartAfter) {
		this.notStartAfter = notStartAfter;
	}

	public MisfireActionType getMisfireActionType() {
		return misfireActionType;
	}

	public void setMisfireActionType(MisfireActionType misfireActionType) {
		this.misfireActionType = misfireActionType;
	}

	public ThreadStopActionType getThreadStopActionType() {
		return threadStopActionType;
	}

	public void setThreadStopActionType(ThreadStopActionType threadStopActionType) {
		this.threadStopActionType = threadStopActionType;
	}

	public String getObjectRefName() {
		return objectRefName;
	}

	public ObjectTypes getObjectRefType() {
		return objectRefType;
	}

	public ObjectReferenceType getObjectRef() {
		return objectRef;
	}

	// should contain the name
	public void setObjectRef(@Nullable ObjectReferenceType objectRef) {
		this.objectRef = objectRef;
		if (objectRef != null) {
			this.objectRefName = PolyString.getOrig(objectRef.getTargetName());
			this.objectRefType = ObjectTypes.getObjectTypeFromTypeQName(objectRef.getType());
		} else {
			this.objectRefName = null;
			this.objectRefType = null;
		}
	}

	public Integer getWorkerThreads() { return workerThreads; }

	public void setWorkerThreads(Integer workerThreads) {
		this.workerThreads = workerThreads;
	}

	//endregion

    //region Getters for read-only properties
    public String getCategory() {
        return taskType.getCategory();
    }
    
    public List<String> getHandlerUriList() {
        return handlerUriList;
    }

	public Long getCurrentRuntime() {
        if (isRunNotFinished()) {
            if (isAliveClusterwide()) {
                return System.currentTimeMillis() - getLastRunStartTimestampLong();
            }
        }
        return null;
    }

    public TaskDtoExecutionStatus getExecution() {
        return TaskDtoExecutionStatus.fromTaskExecutionStatus(taskType.getExecutionStatus(), taskType.getNodeAsObserved() != null);
    }

    public String getExecutingAt() {
        return taskType.getNodeAsObserved();
    }

    public Long getProgress() {
        return taskType.getProgress();
    }

    public Long getExpectedTotal() {
        return taskType.getExpectedTotal();
    }

    public String getProgressDescription() {
        return getProgressDescription(taskType.getProgress());
    }

    public String getProgressDescription(Long currentProgress) {
        if (currentProgress == null && taskType.getExpectedTotal() == null) {
            return "";      // the task handler probably does not report progress at all
        } else {
            StringBuilder sb = new StringBuilder();
            if (currentProgress != null){
                sb.append(currentProgress);
            } else {
                sb.append("0");
            }
            if (taskType.getExpectedTotal() != null) {
                sb.append("/").append(taskType.getExpectedTotal());
            }
            return sb.toString();
        }
    }

    public List<OperationResult> getResult() {
		return opResult;
	}

    public Long getLastRunStartTimestampLong() {
		return xgc2long(taskType.getLastRunStartTimestamp());
	}

	public Long getLastRunFinishTimestampLong() {
		return xgc2long(taskType.getLastRunFinishTimestamp());
	}

    public String getOid() {
        return taskType.getOid();
    }
    
    public String getIdentifier() {
        return taskType.getTaskIdentifier();
    }

    public Long getNextRunStartTimeLong() {
        return xgc2long(taskType.getNextRunStartTimestamp());
    }

    public Long getScheduledToStartAgain() {
        long current = System.currentTimeMillis();

        if (getExecution() == TaskDtoExecutionStatus.RUNNING) {
            if (!recurring) {
                return null;
            } else if (bound) {
                return -1L;             // runs continually; todo provide some information also in this case
            }
        }

		Long nextRunStartTimeLong = getNextRunStartTimeLong();
        if (nextRunStartTimeLong == null || nextRunStartTimeLong == 0) {
            return null;
        }

        if (nextRunStartTimeLong > current + 1000) {
            return nextRunStartTimeLong - System.currentTimeMillis();
        } else if (nextRunStartTimeLong < current - 60000) {
            return -2L;             // already passed
        } else {
            return 0L;              // now
        }
    }

    public OperationResultStatus getStatus() {
        return taskOperationResult != null ? taskOperationResult.getStatus() : null;
    }

    private boolean isRunNotFinished() {
		final Long lastRunStartTimestampLong = getLastRunStartTimestampLong();
		final Long lastRunFinishTimestampLong = getLastRunFinishTimestampLong();
		return lastRunStartTimestampLong != null &&
                (lastRunFinishTimestampLong == null || lastRunStartTimestampLong > lastRunFinishTimestampLong);
    }

    private boolean isAliveClusterwide() {
        return getExecutingAt() != null;
    }

	public TaskExecutionStatus getRawExecutionStatus() {
		return TaskExecutionStatus.fromTaskType(taskType.getExecutionStatus());
	}

	public List<OperationResult> getOpResult() {
		return opResult;
	}

    public Long getCompletionTimestamp() {
        return xgc2long(taskType.getCompletionTimestamp());
    }

    public ModelOperationStatusDto getModelOperationStatus() {
        return modelOperationStatusDto;
    }

	public TaskChangesDto getChangesForIndex(int index) {
		int realIndex = index-1;
		return realIndex < changes.size() ? changes.get(realIndex) : null;
	}

	public void addChildTaskDto(TaskDto taskDto) {
        if (taskDto.getOid() != null) {
            subtasks.add(taskDto);
        } else {
            transientSubtasks.add(taskDto);
        }
    }

    public List<TaskDto> getSubtasks() {
        return subtasks;
    }

    public List<TaskDto> getTransientSubtasks() {
        return transientSubtasks;
    }

    public String getWorkflowProcessInstanceId() {
        return taskType.getWorkflowContext() != null ? taskType.getWorkflowContext().getProcessInstanceId() : null;
    }

    public boolean isWorkflowProcessInstanceFinished() {
        return taskType.getWorkflowContext() != null ?
				taskType.getWorkflowContext().getEndTimestamp() != null : false;
    }

    public List<SceneDto> getWorkflowDeltasIn() {
        return workflowDeltasIn;
    }

	public SceneDto getWorkflowDeltaIn() {
        return workflowDeltaIn;
    }

    public List<SceneDto> getWorkflowDeltasOut() {
        return workflowDeltasOut;
    }

    public String getParentTaskName() {
        return parentTaskType != null ? WebComponentUtil.getName(parentTaskType.asPrismObject()) : null;
    }

    public String getParentTaskOid() {
        return parentTaskType != null ? parentTaskType.getOid() : null;
    }

    public OperationResult getTaskOperationResult() {
        return taskOperationResult;
    }

	public PrismProperty getExtensionProperty(QName propertyName) {
		return taskType.asPrismObject().findProperty(new ItemPath(TaskType.F_EXTENSION, propertyName));
	}

	public <T> T getExtensionPropertyRealValue(QName propertyName, Class<T> clazz) {
		PrismProperty<T> property = taskType.asPrismObject().findProperty(new ItemPath(TaskType.F_EXTENSION, propertyName));
		return property != null ? property.getRealValue() : null;
	}

	public Long getStalledSince() {
		return xgc2long(taskType.getStalledSince());
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public WfContextType getWorkflowContext() {
		return taskType.getWorkflowContext();
	}

	public List<WorkItemDto> getWorkItems() {
		List<WorkItemDto> rv = new ArrayList<>();
		if (taskType.getWorkflowContext() != null) {
			for (WorkItemType workItemType : taskType.getWorkflowContext().getWorkItem()) {
				rv.add(new WorkItemDto(workItemType));
			}
		}
		return rv;
	}

	public List<ProcessInstanceDto> getWorkflowRequests() {
		return workflowRequests;
	}

	public String getObjectType() {
		QName type = getExtensionPropertyRealValue(SchemaConstants.MODEL_EXTENSION_OBJECT_TYPE, QName.class);
		return type != null ? type.getLocalPart() : null;
	}

	public String getObjectQuery() {
		QueryType queryType = getExtensionPropertyRealValue(SchemaConstants.MODEL_EXTENSION_OBJECT_QUERY, QueryType.class);
		PrismContext prismContext = ((MidPointApplication) Application.get()).getPrismContext();
		try {
			return prismContext.serializeAnyData(queryType, SchemaConstants.MODEL_EXTENSION_OBJECT_QUERY, PrismContext.LANG_XML);
		} catch (SchemaException e) {
			throw new SystemException("Couldn't serialize query: " + e.getMessage(), e);
		}
	}

	public String getObjectDelta() {
		ObjectDeltaType objectDeltaType = getExtensionPropertyRealValue(SchemaConstants.MODEL_EXTENSION_OBJECT_DELTA, ObjectDeltaType.class);
		PrismContext prismContext = ((MidPointApplication) Application.get()).getPrismContext();
		try {
			return prismContext.serializeAnyData(objectDeltaType, SchemaConstants.MODEL_EXTENSION_OBJECT_DELTA, PrismContext.LANG_XML);
		} catch (SchemaException e) {
			throw new SystemException("Couldn't serialize delta: " + e.getMessage(), e);
		}
	}

	public String getProcessInstanceId() {
		WfContextType wfc = getWorkflowContext();
		return wfc != null ? wfc.getProcessInstanceId() : null;
	}

	public Boolean isExecuteInRawMode() {
		return getExtensionPropertyRealValue(SchemaConstants.MODEL_EXTENSION_OPTION_RAW, Boolean.class);
	}

	public String getRequestedBy() {
		WfContextType wfc = getWorkflowContext();
		return wfc != null ? WebComponentUtil.getName(wfc.getRequesterRef()) : null;
	}

	public Date getRequestedOn() {
		WfContextType wfc = getWorkflowContext();
		return wfc != null ? XmlTypeConverter.toDate(wfc.getStartTimestamp()) : null;
	}

	public Boolean getWorkflowOutcome() {
		WfContextType wfc = getWorkflowContext();
		return wfc != null ? wfc.isApproved() : null;
	}

	public String getOwnerOid() {
		return taskType.getOwnerRef() != null ? taskType.getOwnerRef().getOid() : null;
	}

	public String getOwnerName() {
		return WebComponentUtil.getName(taskType.getOwnerRef());
	}

	public HandlerDto getHandlerDto() {
		return handlerDto;
	}

	//endregion

    public static List<String> getOids(List<TaskDto> taskDtoList) {
        List<String> retval = new ArrayList<>();
        for (TaskDto taskDto : taskDtoList) {
            retval.add(taskDto.getOid());
        }
        return retval;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		TaskDto taskDto = (TaskDto) o;

		if (bound != taskDto.bound)
			return false;
		if (recurring != taskDto.recurring)
			return false;
		if (taskType != null ? !taskType.equals(taskDto.taskType) : taskDto.taskType != null)
			return false;
		if (name != null ? !name.equals(taskDto.name) : taskDto.name != null)
			return false;
		if (description != null ? !description.equals(taskDto.description) : taskDto.description != null)
			return false;
		if (interval != null ? !interval.equals(taskDto.interval) : taskDto.interval != null)
			return false;
		if (cronSpecification != null ? !cronSpecification.equals(taskDto.cronSpecification) : taskDto.cronSpecification != null)
			return false;
		if (notStartBefore != null ? !notStartBefore.equals(taskDto.notStartBefore) : taskDto.notStartBefore != null)
			return false;
		if (notStartAfter != null ? !notStartAfter.equals(taskDto.notStartAfter) : taskDto.notStartAfter != null)
			return false;
		if (misfireActionType != taskDto.misfireActionType)
			return false;
		if (threadStopActionType != taskDto.threadStopActionType)
			return false;
		if (workerThreads != null ? !workerThreads.equals(taskDto.workerThreads) : taskDto.workerThreads != null)
			return false;
		if (handlerUriList != null ? !handlerUriList.equals(taskDto.handlerUriList) : taskDto.handlerUriList != null)
			return false;
		if (opResult != null ? !opResult.equals(taskDto.opResult) : taskDto.opResult != null)
			return false;
		if (taskOperationResult != null ? !taskOperationResult.equals(taskDto.taskOperationResult) : taskDto.taskOperationResult != null)
			return false;
		if (modelOperationStatusDto != null ?
				!modelOperationStatusDto.equals(taskDto.modelOperationStatusDto) :
				taskDto.modelOperationStatusDto != null)
			return false;
		if (changes != null ? !changes.equals(taskDto.changes) : taskDto.changes != null)
			return false;
		if (workflowRequests != null ? !workflowRequests.equals(taskDto.workflowRequests) : taskDto.workflowRequests != null)
			return false;
		if (workflowDeltasIn != null ? !workflowDeltasIn.equals(taskDto.workflowDeltasIn) : taskDto.workflowDeltasIn != null)
			return false;
		if (workflowDeltasOut != null ? !workflowDeltasOut.equals(taskDto.workflowDeltasOut) : taskDto.workflowDeltasOut != null)
			return false;
		if (workflowDeltaIn != null ? !workflowDeltaIn.equals(taskDto.workflowDeltaIn) : taskDto.workflowDeltaIn != null)
			return false;
		if (parentTaskType != null ? !parentTaskType.equals(taskDto.parentTaskType) : taskDto.parentTaskType != null)
			return false;
		if (objectRefType != taskDto.objectRefType)
			return false;
		if (objectRefName != null ? !objectRefName.equals(taskDto.objectRefName) : taskDto.objectRefName != null)
			return false;
		if (objectRef != null ? !objectRef.equals(taskDto.objectRef) : taskDto.objectRef != null)
			return false;
		if (subtasks != null ? !subtasks.equals(taskDto.subtasks) : taskDto.subtasks != null)
			return false;
		if (transientSubtasks != null ? !transientSubtasks.equals(taskDto.transientSubtasks) : taskDto.transientSubtasks != null)
			return false;
		if (menuItems != null ? !menuItems.equals(taskDto.menuItems) : taskDto.menuItems != null)
			return false;
		return handlerDto != null ? handlerDto.equals(taskDto.handlerDto) : taskDto.handlerDto == null;

	}

	@Override
	public int hashCode() {
		int result = taskType != null ? taskType.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (bound ? 1 : 0);
		result = 31 * result + (recurring ? 1 : 0);
		result = 31 * result + (interval != null ? interval.hashCode() : 0);
		result = 31 * result + (cronSpecification != null ? cronSpecification.hashCode() : 0);
		result = 31 * result + (notStartBefore != null ? notStartBefore.hashCode() : 0);
		result = 31 * result + (notStartAfter != null ? notStartAfter.hashCode() : 0);
		result = 31 * result + (misfireActionType != null ? misfireActionType.hashCode() : 0);
		result = 31 * result + (threadStopActionType != null ? threadStopActionType.hashCode() : 0);
		result = 31 * result + (workerThreads != null ? workerThreads.hashCode() : 0);
		result = 31 * result + (handlerUriList != null ? handlerUriList.hashCode() : 0);
		result = 31 * result + (opResult != null ? opResult.hashCode() : 0);
		result = 31 * result + (taskOperationResult != null ? taskOperationResult.hashCode() : 0);
		result = 31 * result + (modelOperationStatusDto != null ? modelOperationStatusDto.hashCode() : 0);
		result = 31 * result + (changes != null ? changes.hashCode() : 0);
		result = 31 * result + (workflowRequests != null ? workflowRequests.hashCode() : 0);
		result = 31 * result + (workflowDeltasIn != null ? workflowDeltasIn.hashCode() : 0);
		result = 31 * result + (workflowDeltasOut != null ? workflowDeltasOut.hashCode() : 0);
		result = 31 * result + (workflowDeltaIn != null ? workflowDeltaIn.hashCode() : 0);
		result = 31 * result + (parentTaskType != null ? parentTaskType.hashCode() : 0);
		result = 31 * result + (objectRefType != null ? objectRefType.hashCode() : 0);
		result = 31 * result + (objectRefName != null ? objectRefName.hashCode() : 0);
		result = 31 * result + (objectRef != null ? objectRef.hashCode() : 0);
		result = 31 * result + (subtasks != null ? subtasks.hashCode() : 0);
		result = 31 * result + (transientSubtasks != null ? transientSubtasks.hashCode() : 0);
		result = 31 * result + (menuItems != null ? menuItems.hashCode() : 0);
		result = 31 * result + (handlerDto != null ? handlerDto.hashCode() : 0);
		return result;
	}

	@Override
    public String toString() {
        return "TaskDto{" +
                "taskType=" + taskType +
                '}';
    }

	public boolean isRunnableOrRunning() {
		TaskDtoExecutionStatus exec = getExecution();
		return exec == TaskDtoExecutionStatus.RUNNABLE || exec == TaskDtoExecutionStatus.RUNNING;
	}

	public boolean isRunnable() {
		return getExecution() == TaskDtoExecutionStatus.RUNNABLE;
	}

	public boolean isRunning() {
		return getExecution() == TaskDtoExecutionStatus.RUNNING;
	}

	public boolean isClosed() {
		return getExecution() == TaskDtoExecutionStatus.CLOSED;
	}

	public boolean isWaiting() {
		return getExecution() == TaskDtoExecutionStatus.WAITING;
	}

	public boolean isSuspended() {
		return getExecution() == TaskDtoExecutionStatus.SUSPENDED;
	}

	public boolean isReconciliation() {
		return TaskCategory.RECONCILIATION.equals(getCategory());
	}

	public boolean isImportAccounts() {
		return TaskCategory.IMPORTING_ACCOUNTS.equals(getCategory());
	}

	public boolean isRecomputation() {
		return TaskCategory.RECOMPUTATION.equals(getCategory());
	}

	public boolean isExecuteChanges() {
		return TaskCategory.EXECUTE_CHANGES.equals(getCategory());
	}

	public boolean isWorkflowCategory() {
		return TaskCategory.WORKFLOW.equals(getCategory());
	}

	public boolean isWorkflowChild() {
		return isWorkflowCategory() && getWorkflowContext() != null && getWorkflowContext().getProcessInstanceId() != null;
	}

	public boolean isWorkflowParent() {
		return isWorkflowCategory() && getParentTaskOid() == null;
	}

	public boolean isWorkflow() {
		return isWorkflowChild() || isWorkflowParent();		// "task0" is not among these
	}

	public boolean isLiveSync() {
		return TaskCategory.LIVE_SYNCHRONIZATION.equals(getCategory());
	}

	public boolean isShadowIntegrityCheck() {
		return getHandlerUriList().contains(ModelPublicConstants.SHADOW_INTEGRITY_CHECK_TASK_HANDLER_URI);
	}

	public boolean isFocusValidityScanner() {
		return getHandlerUriList().contains(ModelPublicConstants.FOCUS_VALIDITY_SCANNER_TASK_HANDLER_URI);
	}

	public boolean isTriggerScanner() {
		return getHandlerUriList().contains(ModelPublicConstants.TRIGGER_SCANNER_TASK_HANDLER_URI);
	}

	public boolean isDelete() {
		return getHandlerUriList().contains(ModelPublicConstants.DELETE_TASK_HANDLER_URI);
	}

	public boolean isBulkAction() {
		return TaskCategory.BULK_ACTIONS.equals(getCategory());
	}

	public boolean isReportCreate() {
		return TaskCategory.REPORT.equals(getCategory());
	}

	public boolean configuresWorkerThreads() {
		return isReconciliation() || isImportAccounts() || isRecomputation() || isExecuteChanges() || isShadowIntegrityCheck() || isFocusValidityScanner() || isTriggerScanner();
	}

	public boolean configuresWorkToDo() {
		return isLiveSync() || isReconciliation() || isImportAccounts() || isRecomputation() || isExecuteChanges() || isBulkAction() || isDelete() || isShadowIntegrityCheck();
	}

	public boolean configuresResourceCoordinates() {
		return isLiveSync() || isReconciliation() || isImportAccounts();
	}

	public boolean configuresObjectType() {
		return isRecomputation() || isExecuteChanges() || isDelete();
	}

	public boolean configuresObjectQuery() {
		return isRecomputation() || isExecuteChanges() || isDelete() || isShadowIntegrityCheck();
	}

	public boolean configuresObjectDelta() {
		return isExecuteChanges();
	}

	public boolean configuresScript() {
		return isBulkAction();
	}

	public boolean configuresDryRun() {
		return isLiveSync() || isReconciliation() || isImportAccounts() || isShadowIntegrityCheck();
	}

	public boolean configuresExecuteInRawMode() {
		return isExecuteChanges();
	}

	// quite a hack (TODO think about this)
	public boolean isDryRun() {
		if (handlerDto instanceof ResourceRelatedHandlerDto) {
			return ((ResourceRelatedHandlerDto) handlerDto).isDryRun();
		} else {
			return false;
		}
	}
}
