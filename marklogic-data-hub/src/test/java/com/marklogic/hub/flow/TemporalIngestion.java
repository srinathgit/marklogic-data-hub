package com.marklogic.hub.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.appdeployer.command.Command;
import com.marklogic.appdeployer.command.temporal.DeployTemporalAxesCommand;
import com.marklogic.appdeployer.command.temporal.DeployTemporalCollectionsCommand;
import com.marklogic.appdeployer.impl.SimpleAppDeployer;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.hub.AbstractHubCoreTest;
import com.marklogic.hub.deploy.commands.DeployDatabaseFieldCommand;
import com.marklogic.hub.dhs.DhsDeployer;
import com.sun.xml.bind.v2.TODO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.List;

import static com.marklogic.client.io.DocumentMetadataHandle.Capability.*;

public class TemporalIngestion extends AbstractHubCoreTest {

    @BeforeEach
    void setUp() {
        runAsDataHubDeveloper();
        installProjectInFolder("temporal-test");
        DocumentMetadataHandle meta = new DocumentMetadataHandle();
        meta.getCollections().add("testTemporal");
        meta.getPermissions().add("data-hub-common", READ, UPDATE, EXECUTE);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode sourceNode = mapper.createObjectNode();
        sourceNode.put("content", "v1-content");
        stagingClient.newJSONDocumentManager().write("/test.json", meta, new JacksonHandle(sourceNode));
        new DhsDeployer().deployAsDeveloper(getHubConfig());
    }

    @AfterEach
    void tearDown(){
        //TODO: Remove the new fields and field range index from staging db
    }

    @Test
    void writeTemporalFile() {
        final String flowName = "temporal-test";
        runAsDataHubDeveloper();
        runFlow(new FlowInputs(flowName));
        String updateTemporalDoc = "var temporal = require('/MarkLogic/temporal.xqy');\n" +
            "var root ={'envelope':{'headers':{}, 'triples':[], 'instance':{'content':'v2-content'}, 'attachments':null}};\n" +
            "declareUpdate();\n" +
            "temporal.documentInsert('temporal/test', 'temporal/test/test.json', root);";
        /*
        This test will pass if "data-hub-developer" is assigned "temporal-admin", fails otherwise
         */
        try{
            stagingClient.newServerEval().javascript(updateTemporalDoc).eval();
        }
        catch (Exception e){
            logger.error("Document update failed: ", e);
            Assertions.fail("After the step is run, a temporal document should have been created and it's update should not fail");
        }

    }
}
