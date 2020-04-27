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

const ds = require("/data-hub/5/data-services/ds-utils.sjs");
const Security = require("/data-hub/5/impl/security.sjs");

var saveQuery;
var userCollections = ["http://marklogic.com/data-hub/saved-query"];
var queryDocument = JSON.parse(saveQuery);

let authorized = new Security().getRolesAndAuthorities().authorities.includes("canManageSavedQuery");

if(!authorized) {
    ds.throwForbidden(xdmp.getCurrentUser() + " user doesn't have authority to save or update query");
}

if (queryDocument == null || queryDocument.savedQuery == null) {
    ds.throwBadRequest("The request is empty or malformed");
}

if (queryDocument.savedQuery.name == null || !queryDocument.savedQuery.name) {
    ds.throwBadRequest("Query name is missing");
}

if (queryDocument.savedQuery.query == null || Object.keys(queryDocument.savedQuery.query) == 0) {
    ds.throwBadRequest("Query to be saved cannot be empty");
}

if (queryDocument.savedQuery.propertiesToDisplay == null || queryDocument.savedQuery.propertiesToDisplay.length == 0) {
    ds.throwBadRequest("Entity type properties to be displayed cannot be empty");
}

let id = queryDocument.savedQuery.id;
if (cts.doc("/saved-queries/" + id + ".json")) {
    queryDocument.savedQuery.systemMetadata.lastUpdatedBy = xdmp.getCurrentUser();
    queryDocument.savedQuery.systemMetadata.lastUpdatedDateTime = fn.currentDateTime();
    xdmp.nodeReplace(cts.doc("/saved-queries/" + id + ".json"), queryDocument);
} else {
    queryDocument.savedQuery.id = sem.uuidString();
    queryDocument.savedQuery.owner = xdmp.getCurrentUser();
    queryDocument.savedQuery.systemMetadata = {
        "createdBy": xdmp.getCurrentUser(),
        "createdDateTime": fn.currentDateTime(),
        "lastUpdatedBy": xdmp.getCurrentUser(),
        "lastUpdatedDateTime": fn.currentDateTime()
    };
    insertDocument(queryDocument);
}

function insertDocument(queryDocument) {
    let docUri = "/saved-queries/" + queryDocument.savedQuery.id + ".json";
    let permissions = [xdmp.permission('data-hub-saved-query-reader', 'read'),
        xdmp.permission('data-hub-saved-query-writer', 'update'),
        xdmp.defaultPermissions()];
    xdmp.documentInsert(docUri, queryDocument, {
        permissions: permissions,
        collections: userCollections
    });
}

queryDocument;