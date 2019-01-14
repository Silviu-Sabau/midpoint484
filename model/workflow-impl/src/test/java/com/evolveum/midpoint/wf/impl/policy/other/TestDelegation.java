/*
 * Copyright (c) 2010-2017 Evolveum
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

package com.evolveum.midpoint.wf.impl.policy.other;

import com.evolveum.midpoint.model.api.WorkflowService;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.util.PrismAsserts;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.WfContextUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.test.util.TestUtil;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.wf.impl.policy.AbstractWfTestPolicy;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.evolveum.midpoint.xml.ns._public.common.common_3.WorkItemDelegationMethodType.ADD_ASSIGNEES;
import static com.evolveum.midpoint.xml.ns._public.common.common_3.WorkItemDelegationMethodType.REPLACE_ASSIGNEES;
import static org.testng.AssertJUnit.*;

/**
 * @author mederly
 */
@ContextConfiguration(locations = {"classpath:ctx-workflow-test-main.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestDelegation extends AbstractWfTestPolicy {

	@Override
	protected PrismObject<UserType> getDefaultActor() {
		return userAdministrator;
	}

	@Autowired private WorkflowService workflowService;

	private PrismObject<UserType> userLead1, userLead3;
	private String workItemId;
	private String taskOid;

	@Override
	public void initSystem(Task initTask, OperationResult initResult) throws Exception {
		super.initSystem(initTask, initResult);

		DebugUtil.setPrettyPrintBeansAs(PrismContext.LANG_YAML);
	}

	@Test
	public void test100CreateTask() throws Exception {
		final String TEST_NAME = "test100CreateTask";
		TestUtil.displayTestTitle(this, TEST_NAME);
		login(userAdministrator);

		userLead1 = getUser(userLead1Oid);
		userLead3 = getUser(userLead3Oid);

		Task task = createTask(TEST_NAME);
		OperationResult result = task.getResult();

		assignRole(userJackOid, roleRole1aOid, task, result);				// should start approval process
		assertNotAssignedRole(userJackOid, roleRole1aOid, task, result);

		WorkItemType workItem = getWorkItem(task, result);
		workItemId = workItem.getExternalId();
		taskOid = WfContextUtil.getTask(workItem).getOid();

		display("work item", workItem);
		display("task", getObjectViaRepo(TaskType.class, taskOid));

		PrismAsserts.assertReferenceValues(ref(workItem.getAssigneeRef()), userLead1Oid);
	}

	@Test
	public void test110DelegateToUser2Unauthorized() throws Exception {
		final String TEST_NAME = "test110DelegateToUser2Unauthorized";
		TestUtil.displayTestTitle(this, TEST_NAME);
		login(userLead3);

		Task task = createTask(TEST_NAME);
		OperationResult result = task.getResult();

		try {
			workflowService.delegateWorkItem(workItemId, Collections.singletonList(ort(userLead2Oid)), ADD_ASSIGNEES, result);
			fail("delegate succeeded even if it shouldn't");
		} catch (SecurityViolationException e) {
			// ok
		}

		WorkItemType workItem = getWorkItem(task, result);
		PrismAsserts.assertReferenceValues(ref(workItem.getAssigneeRef()), userLead1Oid);
	}

	@Test
	public void test120DelegateToUser2() throws Exception {
		final String TEST_NAME = "test120DelegateToUser2";
		TestUtil.displayTestTitle(this, TEST_NAME);
		login(userLead1);

		Task task = createTask(TEST_NAME);
		OperationResult result = task.getResult();

		workflowService.delegateWorkItem(workItemId, Collections.singletonList(ort(userLead2Oid)), ADD_ASSIGNEES, result);

		result.computeStatus();
		assertSuccess(result);

		WorkItemType workItem = getWorkItem(task, result);
		display("work item", workItem);

		PrismObject<TaskType> wfTask = getObjectViaRepo(TaskType.class, taskOid);
		display("task", wfTask);

		PrismAsserts.assertReferenceValues(ref(workItem.getAssigneeRef()), userLead1Oid, userLead2Oid);
		assertRefEquals("Wrong originalAssigneeRef", ort(userLead1Oid), workItem.getOriginalAssigneeRef());

		WorkItemType workItem1 = workflowEngine.getFullWorkItem(workItem.getExternalId(), result);
		System.out.println("Work item: " + workItem1);
		List<String> assigneeOids = getAssigneeOids(workItem1);
		assertEquals("Wrong midpoint-assignee values", new HashSet<>(Arrays.asList(userLead1Oid, userLead2Oid)),
				new HashSet<>(assigneeOids));

		List<WorkItemDelegationEventType> events = WfContextUtil.getWorkItemEvents(wfTask.asObjectable().getWorkflowContext(), workItemId, WorkItemDelegationEventType.class);
		assertEquals("Wrong # of delegation events", 1, events.size());
		// TODO check content
	}

	private List<String> getAssigneeOids(WorkItemType workItem) {
		return workItem.getAssigneeRef().stream()
				.map(ObjectReferenceType::getOid)
				.collect(Collectors.toList());
	}

	@Test
	public void test130DelegateToUser3ByReplace() throws Exception {
		final String TEST_NAME = "test130DelegateToUser3ByReplace";
		TestUtil.displayTestTitle(this, TEST_NAME);
		login(userLead1);

		Task task = createTask(TEST_NAME);
		OperationResult result = task.getResult();

		workflowService.delegateWorkItem(workItemId, Collections.singletonList(ort(userLead3Oid)), REPLACE_ASSIGNEES, result);

		result.computeStatus();
		assertSuccess(result);

		WorkItemType workItem = getWorkItem(task, result);
		display("work item", workItem);

		PrismObject<TaskType> wfTask = getObjectViaRepo(TaskType.class, taskOid);
		display("task", wfTask);

		PrismAsserts.assertReferenceValues(ref(workItem.getAssigneeRef()), userLead3Oid);
		assertRefEquals("Wrong originalAssigneeRef", ort(userLead1Oid), workItem.getOriginalAssigneeRef());

		WorkItemType fullWorkItem = workflowEngine.getFullWorkItem(workItem.getExternalId(), result);
		System.out.println("Full work item: " + fullWorkItem);
		List<String> assigneeOids = getAssigneeOids(fullWorkItem);
		assertEquals("Wrong assignees", Collections.singleton(userLead3Oid), new HashSet<>(assigneeOids));

		List<WorkItemDelegationEventType> events = WfContextUtil.getWorkItemEvents(wfTask.asObjectable().getWorkflowContext(), workItemId, WorkItemDelegationEventType.class);
		assertEquals("Wrong # of delegation events", 2, events.size());
		// TODO check content
	}

	@Test
	public void test140DelegateToNoneByReplace() throws Exception {
		final String TEST_NAME = "test140DelegateToNoneByReplace";
		TestUtil.displayTestTitle(this, TEST_NAME);
		login(userLead3);

		Task task = createTask(TEST_NAME);
		OperationResult result = task.getResult();

		workflowService.delegateWorkItem(workItemId, Collections.emptyList(), REPLACE_ASSIGNEES, result);

		result.computeStatus();
		assertSuccess(result);

		WorkItemType workItem = getWorkItem(task, result);
		display("work item", workItem);

		PrismObject<TaskType> wfTask = getObjectViaRepo(TaskType.class, taskOid);
		display("task", wfTask);

		assertEquals("Wrong assigneeRef count", 0, workItem.getAssigneeRef().size());
		assertRefEquals("Wrong originalAssigneeRef", ort(userLead1Oid), workItem.getOriginalAssigneeRef());

		WorkItemType fullWorkItem = workflowEngine.getFullWorkItem(workItem.getExternalId(), result);
		System.out.println("Full work item: " + fullWorkItem);
		assertEquals("Wrong # of assignees", 0, getAssigneeOids(fullWorkItem).size());

		List<WorkItemDelegationEventType> events = WfContextUtil.getWorkItemEvents(wfTask.asObjectable().getWorkflowContext(), workItemId, WorkItemDelegationEventType.class);
		assertEquals("Wrong # of delegation events", 3, events.size());
		// TODO check content
	}
}
