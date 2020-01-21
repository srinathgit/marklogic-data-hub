/**
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

const DataHubSingleton = require('/data-hub/5/datahub-singleton.sjs');
const dataHub = DataHubSingleton.instance();

const collections = ['http://marklogic.com/data-hub/step-definition'];
const databases = [dataHub.config.STAGINGDATABASE, dataHub.config.FINALDATABASE];
const permissions = [xdmp.permission(dataHub.consts.DATA_HUB_DEVELOPER_ROLE, 'update'), xdmp.permission(dataHub.consts.DATA_HUB_OPERATOR_ROLE, 'read')];
const requiredProperties = ['name'];

export function getNameProperty() {
    return 'name';
}

export function getVersionProperty() {
    return null;
}

export function getCollections() {
    return collections;
}

export function getStorageDatabases() {
    return databases;
}

export function getPermissions() {
    return permissions;
}

export function getArtifactNode(artifactName, artifactVersion) {
    const results = cts.search(cts.andQuery([cts.collectionQuery(collections[0]), cts.jsonPropertyValueQuery('name', artifactName)]));
    return fn.head(results);
}

export function getDirectory(artifactName, artifact) {
    let doc = getArtifactNode(artifactName, null);
    let dir = "/step-definitions/";
    let type;
    if(!doc) {
        dir = dir + artifact.type.toLowerCase() + "/" + artifact.name +"/"
    }
    else {
        let mutableArtifact = doc.toObject();
        if(mutableArtifact.name.startsWith("default-") || mutableArtifact.name == 'entity-services-mapping'){
            dir = dir + mutableArtifact.toLowerCase() + "/" + "/marklogic/";
        }
        else {
            dir = dir + mutableArtifact.type.toLowerCase() + "/" + mutableArtifact.name + "/";
        }
    }
    return dir;
}

export function getFileExtension() {
    return ".step.json";
}

export function validateArtifact(artifact) {
    const missingProperties = requiredProperties.filter((propName) => !artifact[propName]);
    if (missingProperties.length) {
        return new Error(`Missing the following required properties: ${JSON.stringify(missingProperties)}`);
    }
    return artifact;
}