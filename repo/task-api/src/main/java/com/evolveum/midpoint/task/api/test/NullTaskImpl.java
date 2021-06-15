/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.task.api.test;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ChangeType;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.statistics.*;
import com.evolveum.midpoint.schema.util.task.ActivityPath;
import com.evolveum.midpoint.task.api.*;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

/**
 * DO NOT USE in production code. This is only for testing purposes: provides a no-op implementation of Task interface
 * to be used when task-quartz-impl is not available.
 *
 * TODO move to src/main/test tree.
 *
 * @author lazyman
 */
public class NullTaskImpl implements Task {

    public static final Task INSTANCE = new NullTaskImpl();

    @Override
    public void addDependent(String taskIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAsynchronous() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TaskExecutionStateType getExecutionState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TaskSchedulingStateType getSchedulingState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInitialExecutionAndScheduledState(TaskExecutionStateType executionState, TaskSchedulingStateType schedulingState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInitiallyWaitingForPrerequisites() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull TaskPersistenceStatus getPersistenceStatus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTransient() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPersistent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TaskRecurrenceType getRecurrence() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScheduleType getSchedule() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getScheduleInterval() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasScheduleInterval() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TaskBindingType getBinding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHandlerUri() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHandlerUri(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTaskIdentifier() {
        return null;
    }

    @Override
    public PrismObject<? extends FocusType> getOwner(OperationResult result) {
        return null;
    }

    @Override
    public void setOwner(PrismObject<? extends FocusType> owner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOwnerRef(ObjectReferenceType ownerRef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getChannel() {
        return null;
    }

    @Override
    public void setChannel(String channelUri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrismObject<UserType> getRequestee() {
        return null;
    }

    @Override
    public void setRequesteeTransient(PrismObject<UserType> user) {

    }

    @Override public LensContextType getModelOperationContext() {
        return null;
    }

    @Override public void setModelOperationContext(LensContextType modelOperationContext) {
    }

    @Override
    public TaskErrorHandlingStrategyType getErrorHandlingStrategy() {
        return null;
    }

    @Override
    public String getOid() {
        return null;
    }

    @Override
    public <T extends ObjectType> PrismObject<T> getObject(Class<T> type, OperationResult result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectReferenceType getObjectRefOrClone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setObjectRef(ObjectReferenceType objectRef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setObjectRef(String oid, QName type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getObjectOid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OperationResult getResult() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResult(OperationResult result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getLastRunStartTimestamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getLastRunFinishTimestamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getNextRunStartTime(OperationResult result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PolyStringType getName() {
        return null;
    }

    @Override
    public void setName(PolyStringType value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setName(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNameImmediate(PolyStringType value, OperationResult parentResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <IV extends PrismValue,ID extends ItemDefinition<?>> Item<IV,ID> getExtensionItemOrClone(ItemName propertyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C extends Containerable> void setExtensionContainer(PrismContainer<C> item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExtensionReference(PrismReference reference) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExtensionProperty(PrismProperty<?> property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExtensionPropertyImmediate(PrismProperty<?> property, OperationResult result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addExtensionProperty(PrismProperty<?> property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void setPropertyRealValue(ItemPath path, T value) throws SchemaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Containerable> void setExtensionContainerValue(QName containerName, T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExtensionItem(Item<?, ?> item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getProgress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StructuredTaskProgressType getStructuredProgressOrClone() {
        return null;
    }

    @Override
    public void setProgress(Long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void incrementProgressTransient() {
    }

    @Override
    public void setProgressImmediate(Long progress, OperationResult parentResult) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public PrismObject<TaskType> getUpdatedTaskObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PrismObject<TaskType> getRawTaskObjectClone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(OperationResult result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String debugDump() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String debugDump(int indent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushPendingModifications(OperationResult parentResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCategory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void makeSingle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNodeAsObserved() {
        return null;
    }

    @Override
    public OperationResultStatusType getResultStatus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ThreadStopActionType getThreadStopAction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCategory(String category) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addArchetypeInformation(String archetypeOid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addArchetypeInformationIfMissing(String archetypeOid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrismContainer<? extends ExtensionType> getExtensionOrClone() {
        return null;
    }

    @NotNull
    @Override
    public PrismContainer<? extends ExtensionType> getOrCreateExtension() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrismContainer<? extends ExtensionType> getExtensionClone() {
        return null;
    }

    @Override
    public <T> PrismProperty<T> getExtensionPropertyOrClone(ItemName propertyName) {
        return null;
    }

    @Override
    public <T> T getPropertyRealValue(ItemPath path, Class<T> expectedType) {
        return null;
    }

    @Override
    public <T> T getPropertyRealValueOrClone(ItemPath path, Class<T> expectedType) {
        return null;
    }

    @Override
    public <T> T getItemRealValueOrClone(ItemPath path, Class<T> expectedType) {
        return null;
    }

    @Override
    public ObjectReferenceType getReferenceRealValue(ItemPath path) {
        return null;
    }

    @Override
    public Collection<ObjectReferenceType> getReferenceRealValues(ItemPath path) {
        return null;
    }

    @Override
    public <T extends Containerable> T getExtensionContainerRealValueOrClone(ItemName containerName) {
        return null;
    }

    @Override
    public PrismReference getExtensionReferenceOrClone(ItemName name) {
        return null;
    }

    @Override
    public void setDescriptionImmediate(String value, OperationResult result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDescription(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteExtensionProperty(PrismProperty<?> property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setThreadStopAction(ThreadStopActionType value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void makeSingle(ScheduleType schedule) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Task createSubtask() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getParent() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public List<Task> listSubtasks(boolean persistentOnly, OperationResult parentResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends Task> listSubtasksDeeply(OperationResult result) throws SchemaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Task> listPrerequisiteTasks(OperationResult parentResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PrismObject<TaskType> getRawTaskObjectClonedIfNecessary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getDependents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Task> listDependents(OperationResult result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Task getParentTask(OperationResult result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TaskWaitingReasonType getWaitingReason() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getCompletionTimestamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addExtensionReference(PrismReference reference) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Task> listSubtasksDeeply(boolean persistentOnly, OperationResult result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PolicyRuleType getPolicyRule() {
        throw new UnsupportedOperationException();
    }


    @Override
    public Long getExpectedTotal() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setExpectedTotal(Long value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void recordStateMessage(String message) {
    }

    @Override
    public void recordProvisioningOperation(String resourceOid, String resourceName, QName objectClassName,
            ProvisioningOperation operation, boolean success, int count, long duration) {
    }

    @Override
    public void recordNotificationOperation(String transportName, boolean success, long duration) {
    }

    @Override
    public void recordMappingOperation(String objectOid, String objectName, String objectTypeName, String mappingName, long duration) {
    }

    @Override
    public void onSyncItemProcessingStart(@NotNull String processingIdentifier, @Nullable SynchronizationSituationType situationBefore) {
    }

    @Override
    public void onSynchronizationStart(@Nullable String processingIdentifier, @Nullable String shadowOid, @Nullable SynchronizationSituationType situation) {
    }

    @Override
    public void onSynchronizationExclusion(@Nullable String processingIdentifier, @NotNull SynchronizationExclusionReasonType exclusionReason) {
    }

    @Override
    public void onSynchronizationSituationChange(@Nullable String processingIdentifier, String shadowOid, @Nullable SynchronizationSituationType situation) {
    }

    @Override
    public void onSyncItemProcessingEnd(@NotNull String processingIdentifier, @NotNull QualifiedItemProcessingOutcomeType outcome) {
    }

    @Override
    public void resetSynchronizationInformation(ActivitySynchronizationStatisticsType value) {
    }

    @Override
    public void resetIterativeTaskInformation(ActivityItemProcessingStatisticsType value, boolean collectExecutions) {
    }

    @Override
    public void recordPartExecutionEnd(ActivityPath activityPath, long partStartTimestamp, long partEndTimestamp) {
    }

    @Override
    public IterationInformation.@NotNull Operation recordIterativeOperationStart(IterativeOperationStartInfo operation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordObjectActionExecuted(String objectName, String objectDisplayName, QName objectType, String objectOid, ChangeType changeType, String channel, Throwable exception) {
    }

    @Override
    public void resetActionsExecutedInformation(ActivityActionsExecutedType value) {
    }

    @Override
    public void recordObjectActionExecuted(PrismObject<? extends ObjectType> object, ChangeType changeType, Throwable exception) {
    }

    @Override
    public <T extends ObjectType> void recordObjectActionExecuted(PrismObject<T> objectOld, Class<T> objectTypeClass, String oid, ChangeType delete, String channel, Throwable o) {
    }

    @Override
    public void markObjectActionExecutedBoundary() {
    }

    @Override
    public OperationStatsType getStoredOperationStatsOrClone() {
        return null;
    }

    @Override public void modify(ItemDelta<?, ?> delta) {
    }

    public void modifyAndFlush(ItemDelta<?, ?> delta, OperationResult result) {
    }

    @Override
    public TaskExecutionConstraintsType getExecutionConstraints() {
        return null;
    }

    @Override
    public String getGroup() {
        return null;
    }

    @NotNull
    @Override
    public Collection<String> getGroups() {
        return emptySet();
    }

    @NotNull
    @Override
    public Map<String, Integer> getGroupsWithLimits() {
        return emptyMap();
    }

    @NotNull
    @Override
    public List<String> getLastFailures() {
        return Collections.emptyList();
    }

    @Override
    public WorkDistributionType getWorkManagement() {
        return null;
    }

    @Override
    public TaskActivityStateType getWorkState() {
        return null;
    }

    @Override
    public TaskActivityStateType getWorkStateOrClone() {
        return null;
    }

    @Override
    public <C extends Containerable> C getContainerableOrClone(ItemPath path, Class<C> type) {
        return null;
    }

    @Override
    public boolean doesItemExist(ItemPath path) {
        return false;
    }

    @Override
    public ActivityStateType getActivityStateOrClone(ItemPath path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TaskKindType getKind() {
        return null;
    }

    @Override
    public OperationStatsType getAggregatedLiveOperationStats() {
        return null;
    }

    @Override
    public @NotNull ObjectReferenceType getSelfReference() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public List<Task> getPathToRootTask(OperationResult result) throws SchemaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectReferenceType getOwnerRef() {
        return null;
    }

    @NotNull
    @Override
    public Collection<String> getCachingProfiles() {
        return emptySet();
    }

    @Override
    public void setExecutionConstraints(TaskExecutionConstraintsType value) {
    }

    @Override
    public TaskExecutionEnvironmentType getExecutionEnvironment() {
        return null;
    }

    @Override
    public void setExecutionEnvironment(TaskExecutionEnvironmentType value) {
    }

    @NotNull
    @Override
    public Collection<TracingRootType> getTracingRequestedFor() {
        return emptySet();
    }

    @Override
    public void addTracingRequest(TracingRootType point) {
    }

    @Override
    public void removeTracingRequests() {

    }

    @Override
    public TracingProfileType getTracingProfile() {
        return null;
    }

    @Override
    public void setTracingProfile(TracingProfileType tracingProfile) {
    }

    @Override
    public boolean hasAssignments() {
        return false;
    }

    @Override
    public String getCurrentPartId() {
        return null;
    }

    @Override
    public void applyDeltasImmediate(Collection<ItemDelta<?, ?>> itemDeltas, OperationResult result) throws ObjectAlreadyExistsException, ObjectNotFoundException, SchemaException {
    }

    @Override
    public void applyModificationsTransient(Collection<ItemDelta<?, ?>> modifications) throws SchemaException {
    }
}
