/*
  Copyright 2012-2019 MarkLogic Corporation

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

declareUpdate();

xdmp.securityAssert("http://marklogic.com/data-hub/privileges/write-entity-model", "execute");

const httpUtils = require("/data-hub/5/impl/http-utils.sjs");
const entityLib = require("/data-hub/5/impl/entity-lib.sjs");
const hubUtils = require("/data-hub/5/impl/hub-utils.sjs");

var input = fn.head(xdmp.fromJSON(input));

const name = input.name;
const description = input.description;
const namespace = input.namespace ? input.namespace : null;
const namespacePrefix = input.namespacePrefix ? input.namespacePrefix : null;

if (name == null) {
  httpUtils.throwBadRequest("The model must have an info object with a title property");
}

if (fn.docAvailable(entityLib.getModelUri(name))) {
  httpUtils.throwBadRequest(`An entity type already exists with a name of ${name}`);
}

const model = {
  info: {
    title: name
  },
  definitions: {}
};

model.definitions[name] = {
  properties: {}
};

if(namespace || namespacePrefix){
  if(!namespace){
    httpUtils.throwBadRequest(`You cannot enter a prefix without specifying a namespace URI `);
  }
  if(!namespacePrefix){
    httpUtils.throwBadRequest(`Since you entered a namespace, you must specify a prefix.`);
  }
  model.definitions[name].namespace = namespace;
  model.definitions[name].namespacePrefix = namespacePrefix;
}

if (input.description) {
  model.definitions[name].description = description;
}

try{
  entityLib.writeModel(name, model);
}
catch (e){
  httpUtils.throwBadRequest(hubUtils.getErrorMessage(e));
}

model;
