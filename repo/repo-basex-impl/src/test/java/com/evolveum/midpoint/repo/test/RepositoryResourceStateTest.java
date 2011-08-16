/*
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2011 Igor Farinic
 * Portions Copyrighted 2010 Forgerock
 */

package com.evolveum.midpoint.repo.test;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.Assert;
import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.w3c.dom.Element;

import com.evolveum.midpoint.common.diff.CalculateXmlDiff;
import com.evolveum.midpoint.common.jaxb.JAXBUtil;
import com.evolveum.midpoint.common.result.OperationResult;
import com.evolveum.midpoint.common.test.XmlAsserts;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.ObjectTypes;
import com.evolveum.midpoint.schema.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectModificationType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.PagingType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ResourceStateType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ResourceType;

/**
 * 
 * @author Igor Farinic
 */
@ContextConfiguration(locations = { "../../../../../application-context-repository.xml",
		"classpath:application-context-repository-test.xml" })
public class RepositoryResourceStateTest extends AbstractTestNGSpringContextTests {

	@Autowired(required = true)
	private RepositoryService repositoryService;

	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public RepositoryResourceStateTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@BeforeMethod
	public void setUp() {
	}

	@AfterMethod
	public void tearDown() {
	}

	private void compareObjects(ResourceStateType object, ResourceStateType retrievedObject) throws Exception {
		assertEquals(object.getOid(), retrievedObject.getOid());
		assertEquals(object.getName(), retrievedObject.getName());
		assertEquals(object.getSynchronizationState().getAny().get(0).getTextContent(), retrievedObject
				.getSynchronizationState().getAny().get(0).getTextContent());
		if (object.getExtension() != null && retrievedObject.getExtension() != null) {
			List<Element> extensionElements = object.getExtension().getAny();
			assertEquals(object.getExtension().getAny().size(), retrievedObject.getExtension().getAny()
					.size());
			int i = 0;
			for (Element element : extensionElements) {
				XmlAsserts.assertPatch(DOMUtil.serializeDOMToString(element),
						DOMUtil.serializeDOMToString(retrievedObject.getExtension().getAny().get(i)));
				i++;
			}
		} else if ((object.getExtension() != null && retrievedObject.getExtension() == null)
				|| (object.getExtension() == null && retrievedObject.getExtension() != null)) {
			Assert.fail("Extension section is null for one object but not null for other object");
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testResourceState() throws Exception {
		final String resourceStateOid = "97fa8fd2-0462-42c9-9ca0-e2d317c3d93f";
		final String resourceOid = "aae7be60-df56-11df-8608-0002a5d5c51b";
		try {
			// add resource referenced by resourcestate
			ResourceType resource = ((JAXBElement<ResourceType>) JAXBUtil.unmarshal(new File(
					"src/test/resources/aae7be60-df56-11df-8608-0002a5d5c51b.xml"))).getValue();
			repositoryService.addObject(resource, new OperationResult("test"));
			ObjectType retrievedObject = repositoryService.getObject(resourceOid,
					new PropertyReferenceListType(), new OperationResult("test"));
			assertEquals(resource.getOid(), retrievedObject.getOid());

			// add resource state object
			ResourceStateType resourceState = ((JAXBElement<ResourceStateType>) JAXBUtil.unmarshal(new File(
					"src/test/resources/resource-state.xml"))).getValue();
			repositoryService.addObject(resourceState, new OperationResult("test"));

			// get object
			retrievedObject = repositoryService.getObject(resourceStateOid,
					new PropertyReferenceListType(), new OperationResult("test"));
			compareObjects(resourceState, (ResourceStateType) retrievedObject);

			// list objects
			ObjectListType objects = repositoryService.listObjects(
					ObjectTypes.RESOURCE_STATE.getClassDefinition(), new PagingType(), new OperationResult("test"));
			assertEquals(1, objects.getObject().size());
			compareObjects(resourceState, (ResourceStateType) objects.getObject().get(0));

			// delete object
			repositoryService.deleteObject(resourceStateOid, new OperationResult("test"));
			try {
				repositoryService.getObject(resourceStateOid, new PropertyReferenceListType(), new OperationResult("test"));
				Assert.fail("Object with oid " + resourceStateOid + " was not deleted");
			} catch (ObjectNotFoundException ex) {
				//ignore
			}
		} finally {
			try {
				repositoryService.deleteObject(resourceOid, new OperationResult("test"));
			} catch (Exception e) {
				// ignore errors during cleanup
			}
			try {
				repositoryService.deleteObject(resourceStateOid, new OperationResult("test"));
			} catch (Exception e) {
				// ignore errors during cleanup
			}
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testResourceStateModification() throws Exception {
		final String resourceStateOid = "97fa8fd2-0462-42c9-9ca0-e2d317c3d93f";
		final String resourceOid = "aae7be60-df56-11df-8608-0002a5d5c51b";
		try {
			// add resource referenced by resourcestate
			ResourceType resource = ((JAXBElement<ResourceType>) JAXBUtil.unmarshal(new File(
					"src/test/resources/aae7be60-df56-11df-8608-0002a5d5c51b.xml"))).getValue();
			repositoryService.addObject(resource, new OperationResult("test"));
			ObjectType retrievedObject = repositoryService.getObject(resourceOid,
					new PropertyReferenceListType(), new OperationResult("test"));
			assertEquals(resource.getOid(), retrievedObject.getOid());

			// add resource state object
			ResourceStateType resourceState = ((JAXBElement<ResourceStateType>) JAXBUtil.unmarshal(new File(
					"src/test/resources/resource-state.xml"))).getValue();
			repositoryService.addObject(resourceState, new OperationResult("test"));

			// get object
			retrievedObject = repositoryService.getObject(resourceStateOid,
					new PropertyReferenceListType(), new OperationResult("test"));
			compareObjects(resourceState, (ResourceStateType) retrievedObject);

			// modify object
			ResourceStateType resourceStateAfterSync = ((JAXBElement<ResourceStateType>) JAXBUtil
					.unmarshal(new File("src/test/resources/resource-state-after-sync.xml"))).getValue();
			ObjectModificationType objectModificationType = CalculateXmlDiff.calculateChanges(new File(
					"src/test/resources/resource-state.xml"), new File(
					"src/test/resources/resource-state-after-sync.xml"));
			repositoryService.modifyObject(objectModificationType, new OperationResult("test"));
			retrievedObject = repositoryService.getObject(resourceStateOid,
					new PropertyReferenceListType(), new OperationResult("test"));
			compareObjects(resourceStateAfterSync,
					(ResourceStateType) retrievedObject);

			// delete object
			repositoryService.deleteObject(resourceStateOid, new OperationResult("test"));
			try {
				repositoryService.getObject(resourceStateOid, new PropertyReferenceListType(), new OperationResult("test"));
				Assert.fail("Object with oid " + resourceStateOid + " was not deleted");
			} catch (ObjectNotFoundException ex) {
				//ignore
			}
		} finally {
			try {
				repositoryService.deleteObject(resourceOid, new OperationResult("test"));
			} catch (Exception e) {
				// ignore errors during cleanup
			}
			try {
				repositoryService.deleteObject(resourceStateOid, new OperationResult("test"));
			} catch (Exception e) {
				// ignore errors during cleanup
			}
		}
	}
}
