package com.marklogic.hub.central.controllers.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.hub.central.controllers.BaseController;
import com.marklogic.hub.central.schemas.StepSchema;
import com.marklogic.hub.dataservices.ArtifactService;
import com.marklogic.hub.dataservices.CustomService;
import com.marklogic.hub.dataservices.StepService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;

@Controller
@RequestMapping("/api/steps/custom")
public class CustomStepController extends BaseController {

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get all custom steps", response = CustomSteps.class)
    @Secured("ROLE_readCustom")
    public ResponseEntity<JsonNode> getCustomSteps() {
        return ResponseEntity.ok(newService().getCustomStepsGroupedByEntity());
    }

    @RequestMapping(value = "/{flowName}/{stepName}", method = RequestMethod.GET)
    @ApiOperation(value = "Get a step", response = StepSchema.class)
    @Secured("ROLE_readCustom")
    public ResponseEntity<JsonNode> getCustomStep(@PathVariable String stepName, @PathVariable String flowName) {
        return ResponseEntity.ok(newService().getCustomStep(stepName, flowName));
    }

    private CustomService newService() {
        return CustomService.on(getHubClient().getStagingClient());
    }

    public static class CustomStep {
        private static StepSchema stepSchema;
        private static String flowName;

        public String getFlowName() {
            return flowName;
        }
        public StepSchema getStepSchema() {
            return stepSchema;
        }

        @Override
        public int hashCode() {
            return stepSchema.hashCode();
        }

        @Override
        public String toString() {
            return stepSchema.toString();
        }
    }
    public static class CustomSteps extends ArrayList<CustomStep> {
    }
}

