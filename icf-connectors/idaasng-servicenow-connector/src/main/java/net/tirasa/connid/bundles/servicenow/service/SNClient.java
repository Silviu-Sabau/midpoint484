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
package net.tirasa.connid.bundles.servicenow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.tirasa.connid.bundles.servicenow.SNConnectorConfiguration;
import net.tirasa.connid.bundles.servicenow.dto.PagedResults;
import net.tirasa.connid.bundles.servicenow.dto.Resource;
import net.tirasa.connid.bundles.servicenow.utils.SNUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;

public class SNClient extends SNService {

    private static final Log LOG = Log.getLog(SNClient.class);

    public SNClient(final SNConnectorConfiguration config) {
        super(config);
    }

    /**
     *
     * @param type
     * @return List of Resources
     */
    public PagedResults<Resource> getResources(final ResourceTable type) {
        return doGetResources(getWebclient(type, null));
    }

    /**
     *
     * @param type
     * @param startIndex
     * @param count
     * @param backward
     * @return Paged list of Resources
     */
    public PagedResults<Resource> getResources(final ResourceTable type,
            final Integer startIndex,
            final Integer count,
            final Boolean backward) {
        Map<String, String> params = new HashMap<>();
        params.put("sysparm_offset", String.valueOf(startIndex));
        if (count != null) {
            params.put("sysparm_limit", String.valueOf(count));
        }
        if (backward) {
            params.put("rel", "prev");
        } else {
            params.put("rel", "next");
        }
        return doGetResources(getWebclient(type, params));
    }

    /**
     *
     * @param type
     * @param filterQuery to filter results
     * @return Filtered list of Resources
     */
    public PagedResults<Resource> getResources(final ResourceTable type, final String filterQuery) {
        Map<String, String> params = new HashMap<>();
        params.put("sysparm_query", filterQuery);
        return doGetResources(getWebclient(type, params));
    }

    /**
     *
     * @param type
     * @param filterQuery
     * @param startIndex
     * @param count
     * @param backward
     * @return Paged list of Resources
     */
    public PagedResults<Resource> getResources(final ResourceTable type,
            final String filterQuery,
            final Integer startIndex,
            final Integer count,
            final Boolean backward) {
        Map<String, String> params = new HashMap<>();
        params.put("sysparm_query", filterQuery);
        params.put("sysparm_offset", String.valueOf(startIndex));
        if (count != null) {
            params.put("sysparm_limit", String.valueOf(count));
        }
        if (backward) {
            params.put("rel", "prev");
        } else {
            params.put("rel", "next");
        }
        return doGetResources(getWebclient(type, params));
    }

    /**
     *
     * @param type
     * @param id
     * @return Resource with resource id
     */
    public Resource getResource(final ResourceTable type, final String id) {
        return doGetResource(getWebclient(type, null)
                .path(id));
    }

    /**
     *
     * @param type
     * @param resource
     * @return Created Resource
     */
    public Resource createResource(final ResourceTable type, final Resource resource) {
        return Resource.class.cast(doCreateResource(type, resource));
    }

    /**
     *
     * @param type
     * @param resource
     * @return Update Resource
     */
    public Resource updateResource(final ResourceTable type, final Resource resource) {
        return Resource.class.cast(doUpdateResource(type, resource));
    }

    /**
     *
     * @param type
     * @param id
     */
    public void deleteResource(final ResourceTable type, final String id) {
        WebClient webClient = getWebclient(type, null)
                .path(id);
        doDeleteResource(id, webClient);
    }

    /**
     *
     *
     * @return true if service status is OK, false otherwise
     */
    public boolean testService() {
        return SNClient.this.getResources(ResourceTable.sys_user, 0, 1, false) != null;
    }

    private PagedResults<Resource> doGetResources(final WebClient webClient) {
        PagedResults<Resource> resources = null;
        JsonNode node = doGet(webClient);
        if (node == null) {
            SNUtils.handleGeneralError("While retrieving Resources from service");
        }
        
        try {
            resources = SNUtils.MAPPER.readValue(node.toString(),
                    new TypeReference<PagedResults<Resource>>() {
            });
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to Resources");
        }

        if (resources == null) {
            SNUtils.handleGeneralError("While retrieving Resources from service");
        }

        return resources;
    }

    private Resource doGetResource(final WebClient webClient) {
        Resource resource = null;
        JsonNode node = doGet(webClient);
        if (node == null) {
            SNUtils.handleGeneralError("While retrieving Resource from service");
        }

        try {
            resource = SNUtils.MAPPER.readValue(node.toString(),
                    Resource.class);
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to Resource");
        }

        if (resource == null) {
            SNUtils.handleGeneralError("While retrieving Resource from service after create");
        }

        return resource;
    }

    private Resource doUpdateResource(final ResourceTable type, final Resource resource) {
        if (StringUtil.isBlank(resource.getSysId())) {
            SNUtils.handleGeneralError("Missing required Resource id attribute for update");
        }

        Resource updated = null;
        JsonNode node = doUpdate(resource, getWebclient(type, null)
                .path(resource.getSysId()));
        if (node == null) {
            SNUtils.handleGeneralError("While running update on service");
        }

        try {
            updated = SNUtils.MAPPER.readValue(node.toString(),
                    Resource.class);
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to Resource");
        }

        if (updated == null) {
            SNUtils.handleGeneralError("While retrieving Resource from service after update");
        }

        return updated;
    }

    private Resource doCreateResource(final ResourceTable type, final Resource resource) {
        doCreate(resource, getWebclient(type, null));
        return resource;
    }

    private void doDeleteResource(final String id, final WebClient webClient) {
        doDelete(id, webClient);
    }

}
