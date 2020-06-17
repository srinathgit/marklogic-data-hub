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

var flowName, stepName;
let resp = {};
const flow = Artifacts.getArtifact("flow", flowName);
for (let [key, value] of Object.entries(flow.steps)) {
  if (value.name === stepName) {
    resp.sourceDatabase = value.options.sourceDatabase ? value.options.sourceDatabase : '';
    resp.targetDatabase = value.options.targetDatabase ? value.options.targetDatabase : '';
    resp.collections = value.options.collections ? value.options.collections : [];
    resp.permissions = value.options.permissions ? value.options.permissions : '';
    resp.provenanceGranularityLevel = value.options.provenanceGranularityLevel ? value.options.provenanceGranularityLevel : 'off';
    resp.batchSize = value.batchSize ? value.batchSize : 100;
    resp.options = value.options;
    if(value.processors)
      resp.processors = value.processors;
    if(value.customHook)
      resp.customHook = value.customHook;
    break;
  }
}
resp;
