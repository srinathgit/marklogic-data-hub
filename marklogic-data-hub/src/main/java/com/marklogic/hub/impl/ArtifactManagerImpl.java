/*
 * Copyright 2012-2020 MarkLogic Corporation
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
package com.marklogic.hub.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.hub.ArtifactManager;
import com.marklogic.hub.HubConfig;
import com.marklogic.hub.dataservices.ArtifactService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

@Component
public class ArtifactManagerImpl implements ArtifactManager {
    HubConfig hubConfig;


    public ArtifactManagerImpl(HubConfig hubConfig) {
        this.hubConfig = hubConfig;
    }

    protected ArrayNode allArtifactTypeInfo = null;


    public ArrayNode getArtifacts(String artifactType) {
        return (ArrayNode) getArtifactService().getList(artifactType);
    }

    public ObjectNode updateArtifact(String artifactType, String artifactName, JsonNode artifactJson) {
        ObjectNode resp = (ObjectNode) getArtifactService().setArtifact(artifactType, artifactName, artifactJson);
        writeArtifactInProject(artifactType, (ObjectNode) artifactJson);
        return resp;
    }

    public ObjectNode getArtifact(String artifactType, String artifactName) {
        return (ObjectNode) getArtifactService().getArtifact(artifactType, artifactName);
    }

    public void deleteArtifact(String artifactType, String artifactName) {
       getArtifactService().deleteArtifact(artifactType, artifactName);
        deleteArtifactInProject(artifactType, artifactName, null);
    }

    public ObjectNode validateArtifact(String artifactType, String artifactName, JsonNode artifactJson) {
        return (ObjectNode) getArtifactService().validateArtifact(artifactType, artifactName, artifactJson);
    }

    public ObjectNode getArtifactSettings(String artifactType, String artifactName) {
        return (ObjectNode) getArtifactService().getArtifactSettings(artifactType, artifactName);
    }

    public ObjectNode updateArtifactSettings(String artifactType, String artifactName, JsonNode settings) {
        return (ObjectNode) getArtifactService().setArtifactSettings(artifactType, artifactName, settings);
    }

    protected ArtifactService getArtifactService() {
        DatabaseClient dataServicesClient = hubConfig.newStagingClient(null);
        return ArtifactService.on(dataServicesClient);
    }

    protected void writeArtifactInProject(String artifactType, ObjectNode artifact) {
        Path fileLocation = buildArtifactProjectLocation(artifactType, getNameFromArtifact(artifactType, artifact), getVersionFromArtifact(artifactType, artifact));
        // create folders if needed
        if (!fileLocation.getParent().toFile().exists()) {
            fileLocation.getParent().toFile().mkdirs();
        }
        try {
            Files.write(fileLocation, artifact.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void deleteArtifactInProject(String artifactType, String artifactName, String artifactVersion) {
        Path fileLocation = buildArtifactProjectLocation(artifactType, artifactName, artifactVersion);
        try {
            Files.deleteIfExists(fileLocation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getNameFromArtifact(String artifactType, ObjectNode artifact) {
        ObjectNode artifactTypeInfo = getArtifactTypeInfo(artifactType);
        return artifact.get(artifactTypeInfo.get("nameProperty").asText()).asText();
    }

    protected String getVersionFromArtifact(String artifactType, ObjectNode artifact) {
        ObjectNode artifactTypeInfo = getArtifactTypeInfo(artifactType);
        boolean hasVersioning = artifactTypeInfo.hasNonNull("versionProperty");
        if (hasVersioning) {
            return artifact.get(artifactTypeInfo.get("versionProperty").asText()).asText();
        }
        return null;
    }

    public Path buildArtifactProjectLocation(String artifactType, String artifactName, String artifactVersion) {
        ObjectNode artifactTypeInfo = getArtifactTypeInfo(artifactType);
        Path artifactDirectory = Paths.get(hubConfig.getHubProject().getProjectDir().toString(), artifactTypeInfo.get("directory").asText());
        String artifactExtension = artifactTypeInfo.get("fileExtension").asText();
        boolean hasVersioning = artifactVersion != null;
        String fileName;
        if (hasVersioning) {
            artifactDirectory = artifactDirectory.resolve(artifactName);
            fileName = artifactName + "-" + artifactVersion + artifactExtension;
        } else {
            fileName = artifactName + artifactExtension;
        }
        return artifactDirectory.resolve(fileName);
    }

    protected ObjectNode getArtifactTypeInfo(String artifactType) {
        if (allArtifactTypeInfo == null) {
            allArtifactTypeInfo = (ArrayNode) getArtifactService().getArtifactTypesInfo();
        }
        ObjectNode artifactInfo = null;
        for (Iterator<JsonNode> it = allArtifactTypeInfo.elements(); it.hasNext(); ) {
            JsonNode jsonNode = it.next();
            if (jsonNode.get("type").asText("").equals(artifactType)) {
                artifactInfo = (ObjectNode) jsonNode;
                break;
            }
        }
        return artifactInfo;
    }
}