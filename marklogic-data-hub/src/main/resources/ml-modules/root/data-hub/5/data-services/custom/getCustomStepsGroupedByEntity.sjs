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

const Artifacts = require('/data-hub/5/artifacts/core.sjs');
const entityLib = require("/data-hub/5/impl/entity-lib.sjs");

const ootbStepDefinitions = ['default-mapping', 'entity-services-mapping', 'default-matching', 'default-merging', 'default-mastering'];
const flows = Artifacts.getArtifacts("flow");
const entityNameMap = {};

fn.collection(entityLib.getModelCollection()).toArray().forEach(model => {
  model = model.toObject();
  const entityName = model.info.title;
  const entityTypeId = entityLib.getEntityTypeId(model, entityName);
  entityNameMap[entityName] = {entityType: entityName, entityTypeId, artifacts:[]};

});
entityNameMap["No Entity Type"] = {entityType: "No Entity Type", artifacts:[]};
flows.forEach(flow => {
  flow = flow.toObject();
  Object.keys(flow.steps).forEach(key => {
    let step = flow.steps[key];
    if (step.stepDefinitionName && ! ootbStepDefinitions.includes(step.stepDefinitionName)
      && (step.stepDefinitionType.toLowerCase() !== "ingestion") &&(step.options)) {
      const targetEntity = step.options.targetEntity;
      let payload = {"name" : step.name, "description" : step.description, 'selectedSource': 'query','sourceQuery' : step.options.sourceQuery, 'flow': flow.name}
      if (entityNameMap[targetEntity]) {
        entityNameMap[targetEntity].artifacts.push(payload);
      }
      else {
        entityNameMap["No Entity Type"].artifacts.push(payload);
      }
    }
  });
});

Object.keys(entityNameMap).map(entityName => entityNameMap[entityName]);
