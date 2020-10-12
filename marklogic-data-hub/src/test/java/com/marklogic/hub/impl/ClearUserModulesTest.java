package com.marklogic.hub.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.admin.QueryOptionsManager;
import com.marklogic.client.admin.ResourceExtensionsManager;
import com.marklogic.client.admin.ServerConfigurationManager;
import com.marklogic.client.admin.TransformExtensionsManager;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.QueryOptionsListHandle;
import com.marklogic.hub.AbstractHubCoreTest;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.util.AssertionErrors.fail;

class ClearUserModulesTest extends AbstractHubCoreTest {

    @Test
    void testClearUserModules() {
        assumeTrue(isVersionCompatibleWith520Roles());
        ModuleCounts beforeClearingModules = new ModuleCounts();
        beforeClearingModules.setModuleCounts();

        installProjectWithEachTypeOfModule();

        try {
            runAsDataHubOperator();
            new DataHubImpl(getHubConfig()).clearUserModules();
            fail("'data-hub-operator' should not be able delete user modules");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("User is not allowed to delete /config/query"));
        }

        ModuleCounts afterClearingModulesAsOperator = new ModuleCounts();
        afterClearingModulesAsOperator.setModuleCounts();

        assertEquals(beforeClearingModules.optionsCount + 1, afterClearingModulesAsOperator.optionsCount);
        assertEquals(beforeClearingModules.transformsCount + 1, afterClearingModulesAsOperator.transformsCount);
        assertEquals(beforeClearingModules.resourceExtensionCount + 1, afterClearingModulesAsOperator.resourceExtensionCount);
        assertEquals(beforeClearingModules.hubCoreModuleCount, afterClearingModulesAsOperator.hubCoreModuleCount);
        checkIfCustomModulesArePresent();

        runAsDataHubDeveloper();
        new DataHubImpl(getHubConfig()).clearUserModules();

        ModuleCounts afterClearingModulesAsDeveloper = new ModuleCounts();
        afterClearingModulesAsDeveloper.setModuleCounts();

        assertEquals(beforeClearingModules.optionsCount, afterClearingModulesAsDeveloper.optionsCount);
        assertEquals(beforeClearingModules.transformsCount, afterClearingModulesAsDeveloper.transformsCount);
        assertEquals(beforeClearingModules.resourceExtensionCount, afterClearingModulesAsDeveloper.resourceExtensionCount);
        assertEquals(beforeClearingModules.hubCoreModuleCount, afterClearingModulesAsDeveloper.hubCoreModuleCount);
        //Before test starts , additional custom modules may be present
        assertTrue(afterClearingModulesAsDeveloper.totalModulesCount <= beforeClearingModules.totalModulesCount,
            "Expected total module count to be same or less than the before count, as some custom modules may have existed from other tests; before count: "
                + beforeClearingModules.totalModulesCount + "; after count: " + afterClearingModulesAsDeveloper.totalModulesCount);
        //All custom options, transforms and extensions should have been deleted
        checkIfCustomModulesAreAbsent();

        //Only function metadata that should remain is that of ootb core.sjs
        assertEquals(2, Integer.parseInt(getHubClient().getModulesClient().newServerEval().javascript("cts.estimate(cts.collectionQuery('http://marklogic.com/entity-services/function-metadata'))").evalAs(String.class)));
    }

    private void installProjectWithEachTypeOfModule(){
        installProjectInFolder("test-projects/all-module-types");
        //installing flow-runner-test so that xml/xslt are generated by mappings
        installProjectInFolder("flow-runner-test");
        checkIfCustomModulesArePresent();
    }

    private void checkIfCustomModulesArePresent(){
        List<Collection<String>> moduleTypes = new ModuleCounts().getAllModuleTypes();
        assertTrue(moduleTypes.get(0).contains("myOptions"));
        assertTrue(moduleTypes.get(1).contains("myTransform"));
        assertTrue(moduleTypes.get(2).contains("myResourceExtension"));
    }

    private void checkIfCustomModulesAreAbsent(){
        List<Collection<String>> moduleTypes = new ModuleCounts().getAllModuleTypes();
        assertFalse(moduleTypes.get(0).contains("myOptions"));
        assertFalse(moduleTypes.get(1).contains("myTransform"));
        assertFalse(moduleTypes.get(2).contains("myResourceExtension"));
    }

    class ModuleCounts {
        int totalModulesCount;
        int hubCoreModuleCount;
        int optionsCount;
        int transformsCount;
        int resourceExtensionCount;

        private List<Collection<String>> getAllModuleTypes(){
            ServerConfigurationManager configMgr = runAsDataHubDeveloper().getFinalClient().newServerConfigManager();
            QueryOptionsManager finalOptionsManager = configMgr.newQueryOptionsManager();
            Map<String, String> optionsMap = finalOptionsManager.optionsList(new QueryOptionsListHandle()).getValuesMap();

            TransformExtensionsManager transformExtensionsManager = configMgr.newTransformExtensionsManager();
            JsonNode transformsList = transformExtensionsManager.listTransforms(new JacksonHandle()).get();
            List<String> transforms = transformsList.findValuesAsText("name");

            ResourceExtensionsManager resourceExtensionsManager = configMgr.newResourceExtensionsManager();
            JsonNode resourceExtensionsList = resourceExtensionsManager.listServices(new JacksonHandle()).get();
            List<String> resourceExtensions = resourceExtensionsList.findValuesAsText("name");

            return List.of(optionsMap.keySet(), transforms, resourceExtensions);
        }

        private void setModuleCounts(){
            List<Collection<String>> moduleTypes = getAllModuleTypes();
            optionsCount = moduleTypes.get(0).size();
            transformsCount = moduleTypes.get(1).size();
            resourceExtensionCount = moduleTypes.get(2).size();

            String query = "cts.estimate(cts.collectionQuery('hub-core-module'))";
            hubCoreModuleCount = Integer.parseInt(runAsDataHubDeveloper().getModulesClient().newServerEval().javascript(query).evalAs(String.class));

            totalModulesCount = getDocumentCount(runAsDataHubDeveloper().getModulesClient());
        }
    }
}
