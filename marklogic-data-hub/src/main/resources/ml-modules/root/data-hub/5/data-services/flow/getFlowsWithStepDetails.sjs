/**
 Copyright (c) 2020 MarkLogic Corporation

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
'use strict';
const DataHubSingleton = require("/data-hub/5/datahub-singleton.sjs");
const datahub = DataHubSingleton.instance();
const Artifacts = require('/data-hub/5/artifacts/core.sjs');
const ds = require("/data-hub/5/data-services/ds-utils.sjs");

const flows = Artifacts.getArtifacts("flow");

// Find all stepIds so we can retrieve them in one query
const stepIds = [];
flows.forEach(flow => {
  Object.keys(flow.steps).forEach(key => {
    if (flow.steps[key].stepId) {
      stepIds.push(flow.steps[key].stepId);
    }
  });
});


// Query for referenced steps, if any exist
const steps = stepIds.length < 1 ? [] :
  cts.search(cts.andQuery([
    cts.collectionQuery("http://marklogic.com/data-hub/steps"),
    cts.jsonPropertyValueQuery("stepId", stepIds, "case-insensitive")
  ]));


// Build a map of steps for fast access
const stepMap = {};
for (var step of steps) {
  step = step.toObject();
  stepMap[step.stepId] = step;
}


// Iterate over flows again to return the desired results
flows.map(flow => {
  const flowWithStepDetails = {name: flow.name};
  if (flow.description) {
    flowWithStepDetails.description = flow.description;
  }

  if (flow.steps) {
    flowWithStepDetails.steps = [];
    Object.keys(flow.steps).forEach(stepNumber => {
      const stepDetails = {stepNumber};
      flowWithStepDetails.steps.push(stepDetails);

      let step = flow.steps[stepNumber];
      const stepId = flow.steps[stepNumber].stepId;
      if (stepId) {
        step = stepMap[stepId];
        if (!step) {
          ds.throwServerError(`Unable to find referenced step with ID ${stepId} in flow ${flow.name}`);
        }
      }
      stepDetails.stepName = step.name;
      stepDetails.stepDefinitionType = step.stepDefinitionType;
      stepDetails.sourceFormat = step.sourceFormat;

      const jobQueries = [];
      jobQueries.push(cts.collectionQuery('Job'));
      jobQueries.push(cts.jsonPropertyValueQuery("flow",flow.name));
      jobQueries.push(cts.jsonPropertyValueQuery("stepName",step.name));

      let latestJob = fn.head(datahub.hubUtils.queryLatest(function() {
         return fn.head(fn.subsequence(
          cts.search(
            cts.andQuery(jobQueries),
            [cts.indexOrder(cts.jsonPropertyReference("timeStarted"), "descending")]
          ), 1, 1
        ));
      }, datahub.config.JOBDATABASE));
      if(latestJob) {
        latestJob = latestJob.toObject();
        stepDetails.jobId = latestJob.job.jobId;
        let stepRunResponse = latestJob.job.stepResponses[stepNumber];
        stepDetails.lastRunStatus = stepRunResponse.status;
        stepDetails.stepEndTime = stepRunResponse.stepEndTime;
      }
    });
  }
  return flowWithStepDetails;
});
