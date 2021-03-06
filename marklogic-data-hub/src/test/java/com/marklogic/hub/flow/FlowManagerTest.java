/*
 * Copyright (c) 2021 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marklogic.hub.flow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.hub.AbstractHubCoreTest;
import com.marklogic.hub.impl.FlowManagerImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

class FlowManagerTest extends AbstractHubCoreTest {

    @Autowired
    FlowManagerImpl fm;

    private String flowString = "{\n" +
        "  \"name\": \"test-flow\",\n" +
        "  \"description\": \"this is an example\",\n" +
        "  \"options\": {\"sourceQuery\": \"(some sourceQuery or search)\"},\n" +
        "  \"steps\": {\n" +
        "    \"1\": {\n" +
        "      \"stepDefinitionType\": \"MAPPING\",\n" +
        "      \"name\": \"person-mapping1.json\",\n" +
        "      \"options\": {\"sourceQuery\": \"null\"}\n" +
        "    }\n" +
        "  }\n" +
        "}\n";

    @BeforeEach
    void setUp() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(flowString);
        Flow flow = fm.createFlowFromJSON(node);
        fm.saveFlow(flow);
    }

    @Test
    void missingFlowsDirectory() {
        deleteProjectDir();
        List<String> names = fm.getLocalFlowNames();
        Assertions.assertEquals(0, names.size(), "When the flows directory doesn't exist (in this case, the entire " +
            "project directory is missing), an error shouldn't be thrown - should just get back an empty list");
    }

    @Test
    void getFlow() {
        Flow flow = fm.getFlow("test-flow");
        Assertions.assertNotNull(flow);
        Assertions.assertEquals("test-flow", flow.getName());
    }

    @Test
    void getFlowAsJSON() throws IOException {
        String actual = fm.getFlowAsJSON("test-flow");
        assertJsonEqual(flowString, actual, true);
    }

    @Test
    void getFlows() {
        List<Flow> flows = fm.getFlows();
        Assertions.assertEquals(flows.size(), 1);
        Assertions.assertEquals(flows.get(0).getName(), fm.getFlow("test-flow").getName());
    }

    @Test
    void getFlowNames() {
        List<String> flows = fm.getLocalFlowNames();
        Assertions.assertEquals(flows.size(), 1);
        Assertions.assertEquals(flows.get(0), "test-flow");
    }

    @Test
    void createFlow() {
        Flow flow = fm.createFlow("test-flow");
        Assertions.assertEquals("test-flow", flow.getName());
    }

    @Test
    void createFlowFromJSON() {
        fm.deleteFlow("test-flow");
        Flow flow = fm.createFlowFromJSON(flowString);
        Assertions.assertEquals("test-flow", flow.getName());
    }

    @Test
    void createFlowFromJSON1() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(flowString);
        Flow flow = fm.createFlowFromJSON(node);
        Assertions.assertEquals("test-flow", flow.getName());
    }

    @Test
    void deleteFlow() {
        fm.deleteFlow("test-flow");

        Flow flow = fm.getFlow("test-flow");
        Assertions.assertNull(flow);
    }

    @Test
    void saveFlow() throws IOException {
        fm.deleteFlow("test-flow");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(flowString);
        Flow flow = fm.createFlowFromJSON(node);
        fm.saveFlow(flow);
        Assertions.assertEquals("test-flow", fm.getFlow("test-flow").getName());

    }
}
