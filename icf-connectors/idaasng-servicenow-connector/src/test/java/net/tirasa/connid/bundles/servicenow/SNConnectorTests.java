/**
 * Copyright © 2018 ConnId (connid-dev@googlegroups.com)
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
package net.tirasa.connid.bundles.servicenow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import net.tirasa.connid.bundles.servicenow.dto.PagedResults;
import net.tirasa.connid.bundles.servicenow.dto.Resource;
import net.tirasa.connid.bundles.servicenow.exception.NoSuchEntityException;
import net.tirasa.connid.bundles.servicenow.service.SNClient;
import net.tirasa.connid.bundles.servicenow.service.SNService;
import net.tirasa.connid.bundles.servicenow.utils.SNAttributes;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.SortKey;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.test.common.TestHelpers;
import org.identityconnectors.test.common.ToListResultsHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SNConnectorTests {

    private static final Log LOG = Log.getLog(SNConnectorTests.class);

    private final static Properties PROPS = new Properties();

    private static SNConnectorConfiguration CONF;

    private static SNConnector CONN;

    protected static ConnectorFacade connector;

    @BeforeAll
    public static void setUpConf() throws IOException {
        PROPS.load(SNConnectorTests.class.getResourceAsStream(
                "/net/tirasa/connid/bundles/servicenow/auth.properties"));

        Map<String, String> configurationParameters = new HashMap<>();
        for (final String name : PROPS.stringPropertyNames()) {
            configurationParameters.put(name, PROPS.getProperty(name));
        }
        CONF = SNConnectorTestsUtils.buildConfiguration(configurationParameters);

        Boolean isValid = SNConnectorTestsUtils.isConfigurationValid(CONF);
        if (isValid) {
            CONN = new SNConnector();
            CONN.init(CONF);
            try {
                CONN.test();
            } catch (Exception e) {
                LOG.error(e, "While testing connector");
            }
            CONN.schema();
        }

        connector = newFacade();

        assertNotNull(CONF);
        assertNotNull(isValid);
        assertNotNull(CONF.getBaseAddress());
        assertNotNull(CONF.getPassword());
        assertNotNull(CONF.getUsername());
    }

    private static ConnectorFacade newFacade() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration impl = TestHelpers.createTestConfiguration(SNConnector.class, CONF);
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(true);
        return factory.newInstance(impl);
    }

    private SNClient newClient() {
        return CONN.getClient();
    }

    @Test
    public void validate() {
        newFacade().validate();
    }

    @Test
    public void schema() {
        Schema schema = newFacade().schema();
        assertEquals(2, schema.getObjectClassInfo().size());

        boolean accountFound = false;
        boolean groupFound = false;
        for (ObjectClassInfo oci : schema.getObjectClassInfo()) {
            if (ObjectClass.ACCOUNT_NAME.equals(oci.getType())) {
                accountFound = true;
            } else if (ObjectClass.GROUP_NAME.equals(oci.getType())) {
                groupFound = true;
            }
        }
        assertTrue(accountFound);
        assertTrue(groupFound);
    }

    @Test
    public void search() {
        ToListResultsHandler handler = new ToListResultsHandler();

        SearchResult result = connector.search(ObjectClass.ACCOUNT,
                null,
                handler,
                new OperationOptionsBuilder().build());
        assertNotNull(result);
        assertNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());
        assertFalse(handler.getObjects().isEmpty());

        result = connector.search(ObjectClass.ACCOUNT,
                null,
                handler,
                new OperationOptionsBuilder().setPageSize(1).build());
        assertNotNull(result);
        assertNotNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());

        result = connector.search(ObjectClass.ACCOUNT,
                null,
                handler,
                new OperationOptionsBuilder().setPagedResultsOffset(2).setPageSize(1).build());
        assertNotNull(result);
        assertNotNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());
    }

    private void cleanup(
            final ConnectorFacade connector,
            final SNClient client,
            final String testUserUid,
            final String testGroupUid) {
        if (testUserUid != null) {
            connector.delete(ObjectClass.ACCOUNT, new Uid(testUserUid), new OperationOptionsBuilder().build());
            try {
                client.deleteResource(SNService.ResourceTable.sys_user, testUserUid);
                fail(); // must fail
            } catch (ConnectorException e) {
                assertNotNull(e);
            }

            try {
                client.getResource(SNService.ResourceTable.sys_user, testUserUid);
                fail(); // must fail
            } catch (NoSuchEntityException e) {
                assertNotNull(e);
            }
        }

        if (testGroupUid != null) {
            connector.delete(ObjectClass.ACCOUNT, new Uid(testGroupUid), new OperationOptionsBuilder().build());
            try {
                client.deleteResource(SNService.ResourceTable.sys_user_group, testGroupUid);
                fail(); // must fail
            } catch (ConnectorException e) {
                assertNotNull(e);
            }

            try {
                client.getResource(SNService.ResourceTable.sys_user_group, testGroupUid);
                fail(); // must fail
            } catch (NoSuchEntityException e) {
                assertNotNull(e);
            }
        }
    }

    private void cleanup(
            final SNClient client,
            final String testUserUid,
            final String testGroupUid) {
        if (testUserUid != null) {
            client.deleteResource(SNService.ResourceTable.sys_user, testUserUid);

            try {
                client.getResource(SNService.ResourceTable.sys_user, testUserUid);
                fail(); // must fail
            } catch (ConnectorException e) {
                assertNotNull(e);
            }
        }

        if (testGroupUid != null) {
            client.deleteResource(SNService.ResourceTable.sys_user_group, testUserUid);

            try {
                client.getResource(SNService.ResourceTable.sys_user_group, testUserUid);
                fail(); // must fail
            } catch (ConnectorException e) {
                assertNotNull(e);
            }
        }
    }

    @Test
    public void crud() {
        ConnectorFacade connector = newFacade();
        SNClient client = newClient();

        String testUser = null;
        String testGroup = null;
        UUID uid = UUID.randomUUID();

        try {
            Uid created = createUser(uid);
            testUser = created.getUidValue();

            Resource createdUser = readUser(testUser, client);
            assertEquals(createdUser.getSysId(), created.getUidValue());

            Uid updated = updateUser(created);

            Resource updatedUser = readUser(updated.getUidValue(), client);
            LOG.info("Updated User: {0}", updatedUser);
            assertNotNull(updatedUser.getUserPassword()); // password returned in clear text
            assertEquals(updatedUser.getEmployeeNumber(), SNConnectorTestsUtils.VALUE_EMPLOYEE_NUMBER_UPDATE);
            assertFalse(StringUtil.isBlank(updatedUser.getUserPassword()));
            assertTrue(StringUtil.isBlank(updatedUser.getCity()));
            assertEquals(updatedUser.getUserPassword(), SNConnectorTestsUtils.PASSWORD_UPDATE);
        } catch (Exception e) {
            LOG.error(e, "While running test");
            fail(e.getMessage());
        } finally {
            cleanup(connector, client, testUser, testGroup);
        }
    }

    private Uid createUser(final UUID uid) {
        Attribute password = AttributeBuilder.buildPassword(
                new GuardedString(SNConnectorTestsUtils.PASSWORD.toCharArray()));

        Set<Attribute> userAttrs = new HashSet<>();
        userAttrs.add(AttributeBuilder.build(SNAttributes.USER_ATTRIBUTE_USERNAME,
                SNConnectorTestsUtils.USERNAME + uid.toString()));
        userAttrs.add(AttributeBuilder.build(SNConnectorTestsUtils.RESOURCE_ATTRIBUTE_EMPLOYEE_NUMBER,
                SNConnectorTestsUtils.VALUE_EMPLOYEE_NUMBER));
        userAttrs.add(AttributeBuilder.build(SNConnectorTestsUtils.RESOURCE_ATTRIBUTE_LOCATION,
                SNConnectorTestsUtils.VALUE_LOCATION));
        userAttrs.add(AttributeBuilder.build(SNConnectorTestsUtils.RESOURCE_ATTRIBUTE_MANAGER,
                SNConnectorTestsUtils.VALUE_MANAGER));
        userAttrs.add(AttributeBuilder.build(SNConnectorTestsUtils.RESOURCE_ATTRIBUTE_CITY,
                SNConnectorTestsUtils.VALUE_CITY));
        userAttrs.add(password);

        Uid created = connector.create(ObjectClass.ACCOUNT, userAttrs, new OperationOptionsBuilder().build());
        assertNotNull(created);
        assertFalse(created.getUidValue().isEmpty());
        LOG.info("Created User uid: {0}", created);

        return created;
    }

    private Uid updateUser(final Uid created) {
        Attribute password = AttributeBuilder.buildPassword(
                new GuardedString((SNConnectorTestsUtils.PASSWORD_UPDATE).toCharArray()));
        // UPDATE USER PASSWORD
        Set<Attribute> userAttrs = new HashSet<>();
        userAttrs.add(password);

        // want to update another attribute
        userAttrs.add(AttributeBuilder.build(SNConnectorTestsUtils.RESOURCE_ATTRIBUTE_EMPLOYEE_NUMBER,
                SNConnectorTestsUtils.VALUE_EMPLOYEE_NUMBER_UPDATE));

        // want to clear an attribute
        userAttrs.add(AttributeBuilder.build(SNConnectorTestsUtils.RESOURCE_ATTRIBUTE_CITY,
                ""));

        Uid updated = connector.update(
                ObjectClass.ACCOUNT, created, userAttrs, new OperationOptionsBuilder().build());
        assertNotNull(updated);
        assertFalse(updated.getUidValue().isEmpty());
        LOG.info("Updated User uid: {0}", updated);

        return updated;
    }

    private Resource readUser(final String id, final SNClient client)
            throws IllegalArgumentException, IllegalAccessException {
        Resource user = client.getResource(SNService.ResourceTable.sys_user, id);
        assertNotNull(user);
        assertNotNull(user.getSysId());
        assertEquals(user.getLocation().getValue(), SNConnectorTestsUtils.VALUE_LOCATION);
        assertEquals(user.getManager().getValue(), SNConnectorTestsUtils.VALUE_MANAGER);
        assertNotNull(user.getEmployeeNumber());
        assertTrue(StringUtil.isBlank(user.getEmail()));
        LOG.info("Found User: {0}", user);

        // USER TO ATTRIBUTES
        Set<Attribute> toAttributes = user.toAttributes();
        LOG.info("User to attributes: {0}", toAttributes);
        assertTrue(hasAttribute(toAttributes, SNAttributes.RESOURCE_ATTRIBUTE_ID));
        assertTrue(hasAttribute(toAttributes, SNAttributes.USER_ATTRIBUTE_USERNAME));
        assertTrue(hasAttribute(toAttributes, SNAttributes.RESOURCE_ATTRIBUTE_NAME));
        assertTrue(hasAttribute(toAttributes, SNConnectorTestsUtils.RESOURCE_ATTRIBUTE_EMPLOYEE_NUMBER));
        assertTrue(hasAttribute(toAttributes, SNConnectorTestsUtils.RESOURCE_ATTRIBUTE_LOCATION));
        assertTrue(hasAttribute(toAttributes, SNConnectorTestsUtils.RESOURCE_ATTRIBUTE_MANAGER));
        assertTrue(hasAttribute(toAttributes, SNConnectorTestsUtils.RESOURCE_ATTRIBUTE_CITY));

        // SEARCH BY USERNAME
        final List<ConnectorObject> found = new ArrayList<>();
        connector.search(ObjectClass.ACCOUNT,
                new EqualsFilter(new Name(user.getUserName())),
                found::add,
                new OperationOptionsBuilder().setAttributesToGet(SNAttributes.USER_ATTRIBUTE_USERNAME).build());
        assertEquals(found.size(), 1);
        assertNotNull(found.get(0));
        assertNotNull(found.get(0).getName());
        LOG.info("Found User using Connector search: {0}", found.get(0));

        return user;
    }

    @Test
    public void pagedSearch() {
        final List<ConnectorObject> results = new ArrayList<>();
        final ResultsHandler handler = results::add;

        final OperationOptionsBuilder oob = new OperationOptionsBuilder();
        oob.setAttributesToGet(SNAttributes.USER_ATTRIBUTE_USERNAME);
        oob.setPageSize(1);
        oob.setSortKeys(new SortKey(SNAttributes.USER_ATTRIBUTE_USERNAME, false));

        connector.search(ObjectClass.ACCOUNT, null, handler, oob.build());

        assertEquals(1, results.size());

        results.clear();

        int totalCount = newClient().getResources(SNService.ResourceTable.sys_user).getTotalCount();
        oob.setPageSize(totalCount > 3 ? totalCount / 3 : totalCount);
        String cookie = "";
        do {
            oob.setPagedResultsCookie(cookie);
            final SearchResult searchResult = connector.search(ObjectClass.ACCOUNT, null, handler, oob.build());
            cookie = searchResult.getPagedResultsCookie();
        } while (cookie != null);
        LOG.info("Paged search results : {0}", results);

        assertTrue(results.size() > 2);
    }

    @Test
    public void serviceTest() {
        SNClient client = newClient();

        String testUser = null;
        String testGroup = null;
        UUID uid = UUID.randomUUID();

        try {
            deleteUsersServiceTest(client);

            Resource created = createUserServiceTest(uid, client);
            testUser = created.getSysId();

            readUserServiceTest(testUser, client);

            readUsersServiceTest(client);
            readGroupsServiceTest(client);

            updateUserServiceTest(testUser, client);
        } catch (Exception e) {
            LOG.error(e, "While running test");
            fail(e.getMessage());
        } finally {
            cleanup(client, testUser, testGroup);
        }
    }

    private Resource createUserServiceTest(final UUID uid, final SNClient client) {
        Resource user = new Resource();
        user.setUserName(SNConnectorTestsUtils.USERNAME + uid.toString());
        user.setUserPassword(SNConnectorTestsUtils.PASSWORD);
        user.setActive("true");
        user.setCity(SNConnectorTestsUtils.VALUE_CITY);
        user.setComments(SNConnectorTestsUtils.VALUE_COMMENT);

        Resource created = client.createResource(SNService.ResourceTable.sys_user, user);
        assertNotNull(created);
        assertNotNull(created.getSysId());
        LOG.info("Created User: {0}", created);

        return created;
    }

    private Resource updateUserServiceTest(final String userId, final SNClient client) {
        Resource user = client.getResource(SNService.ResourceTable.sys_user, userId);
        assertNotNull(user);
        assertFalse(user.getCity().isEmpty());

        String oldCity = user.getCity();

        // want to update an attribute
        user.setCity(SNConnectorTestsUtils.VALUE_CITY_UPDATE);

        // want also to reset an attribute
        user.setComments(null);

        Resource updated = client.updateResource(SNService.ResourceTable.sys_user, user);
        assertNotNull(updated);
        assertFalse(updated.getCity().equals(oldCity));
        assertEquals(updated.getCity(), SNConnectorTestsUtils.VALUE_CITY_UPDATE);
        assertNull(updated.getComments());
        LOG.info("Updated User: {0}", updated);

        // test removed attribute
        return updated;
    }

    private void readUsersServiceTest(final SNClient client)
            throws IllegalArgumentException, IllegalAccessException {
        // GET USERS
        PagedResults<Resource> paged = client.getResources(SNService.ResourceTable.sys_user, 0, 1, false);
        assertNotNull(paged);
        assertFalse(paged.getResult().isEmpty());
        assertTrue(paged.getResult().size() == 1);
        assertNotEquals(paged.getTotalCount(), 1);
        LOG.info("Paged Users: {0}", paged);

        PagedResults<Resource> paged2 = client.getResources(SNService.ResourceTable.sys_user, 1, 1, false);
        assertNotNull(paged2);
        assertFalse(paged2.getResult().isEmpty());
        assertTrue(paged2.getResult().size() == 1);
        LOG.info("Paged Users next page: {0}", paged2);
    }

    private Resource readUserServiceTest(final String id, final SNClient client)
            throws IllegalArgumentException, IllegalAccessException {
        // GET USER
        Resource user = client.getResource(SNService.ResourceTable.sys_user, id);
        assertNotNull(user);
        assertNotNull(user.getSysId());
        LOG.info("Found User: {0}", user);

        // USER TO ATTRIBUTES
        Set<Attribute> toAttributes = user.toAttributes();
        LOG.info("User to attributes: {0}", toAttributes);
        assertTrue(hasAttribute(toAttributes, SNAttributes.RESOURCE_ATTRIBUTE_ID));
        assertTrue(hasAttribute(toAttributes, SNAttributes.USER_ATTRIBUTE_USERNAME));
        assertTrue(hasAttribute(toAttributes, SNAttributes.RESOURCE_ATTRIBUTE_NAME));

        // GET USER by userName
        List<Resource> users = client.getResources(SNService.ResourceTable.sys_user,
                SNAttributes.USER_ATTRIBUTE_USERNAME + "=" + user.getUserName()).getResult();
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertNotNull(users.get(0).getSysId());
        LOG.info("Found User by {0}: {1}", SNAttributes.USER_ATTRIBUTE_USERNAME, users.get(0));

        return user;
    }

    private void deleteUsersServiceTest(final SNClient client) {
        PagedResults<Resource> users = client.getResources(SNService.ResourceTable.sys_user,
                SNAttributes.USER_ATTRIBUTE_USERNAME + "STARTSWITH" + SNConnectorTestsUtils.USERNAME, 0, 100, false);
        assertNotNull(users);
        if (!users.getResult().isEmpty()) {
            for (Resource user : users.getResult()) {
                client.deleteResource(SNService.ResourceTable.sys_user, user.getSysId());
            }
        }
    }

    private void readGroupsServiceTest(final SNClient client)
            throws IllegalArgumentException, IllegalAccessException {
        // GET GROUPS
        PagedResults<Resource> paged = client.getResources(SNService.ResourceTable.sys_user_group, 0, 1, false);
        assertNotNull(paged);
        assertFalse(paged.getResult().isEmpty());
        assertTrue(paged.getResult().size() == 1);
        assertNotEquals(paged.getTotalCount(), 1);
        LOG.info("Paged Groups: {0}", paged);

        PagedResults<Resource> paged2 = client.getResources(SNService.ResourceTable.sys_user_group, 1, 1, false);
        assertNotNull(paged2);
        assertFalse(paged2.getResult().isEmpty());
        assertTrue(paged2.getResult().size() == 1);
        LOG.info("Paged Groups next page: {0}", paged2);
    }

    private boolean hasAttribute(final Set<Attribute> attrs, final String name) {
        for (Attribute attr : attrs) {
            if (attr.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
