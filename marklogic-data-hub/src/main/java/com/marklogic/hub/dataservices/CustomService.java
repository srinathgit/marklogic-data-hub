package com.marklogic.hub.dataservices;

// IMPORTANT: Do not edit. This file is generated.

import com.marklogic.client.io.Format;


import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.marker.JSONWriteHandle;

import com.marklogic.client.impl.BaseProxy;

/**
 * Provides a set of operations on the database server
 */
public interface CustomService {
    /**
     * Creates a CustomService object for executing operations on the database server.
     *
     * The DatabaseClientFactory class can create the DatabaseClient parameter. A single
     * client object can be used for any number of requests and in multiple threads.
     *
     * @param db	provides a client for communicating with the database server
     * @return	an object for executing database operations
     */
    static CustomService on(DatabaseClient db) {
      return on(db, null);
    }
    /**
     * Creates a CustomService object for executing operations on the database server.
     *
     * The DatabaseClientFactory class can create the DatabaseClient parameter. A single
     * client object can be used for any number of requests and in multiple threads.
     *
     * The service declaration uses a custom implementation of the same service instead
     * of the default implementation of the service by specifying an endpoint directory
     * in the modules database with the implementation. A service.json file with the
     * declaration can be read with FileHandle or a string serialization of the JSON
     * declaration with StringHandle.
     *
     * @param db	provides a client for communicating with the database server
     * @param serviceDeclaration	substitutes a custom implementation of the service
     * @return	an object for executing database operations
     */
    static CustomService on(DatabaseClient db, JSONWriteHandle serviceDeclaration) {
        final class CustomServiceImpl implements CustomService {
            private DatabaseClient dbClient;
            private BaseProxy baseProxy;

            private BaseProxy.DBFunctionRequest req_getCustomStepsGroupedByEntity;
            private BaseProxy.DBFunctionRequest req_getCustomStep;

            private CustomServiceImpl(DatabaseClient dbClient, JSONWriteHandle servDecl) {
                this.dbClient  = dbClient;
                this.baseProxy = new BaseProxy("/data-hub/5/data-services/custom/", servDecl);

                this.req_getCustomStepsGroupedByEntity = this.baseProxy.request(
                    "getCustomStepsGroupedByEntity.sjs", BaseProxy.ParameterValuesKind.NONE);
                this.req_getCustomStep = this.baseProxy.request(
                    "getCustomStep.sjs", BaseProxy.ParameterValuesKind.MULTIPLE_ATOMICS);
            }

            @Override
            public com.fasterxml.jackson.databind.JsonNode getCustomStepsGroupedByEntity() {
                return getCustomStepsGroupedByEntity(
                    this.req_getCustomStepsGroupedByEntity.on(this.dbClient)
                    );
            }
            private com.fasterxml.jackson.databind.JsonNode getCustomStepsGroupedByEntity(BaseProxy.DBFunctionRequest request) {
              return BaseProxy.JsonDocumentType.toJsonNode(
                request.responseSingle(false, Format.JSON)
                );
            }

            @Override
            public com.fasterxml.jackson.databind.JsonNode getCustomStep(String stepName, String flowName) {
                return getCustomStep(
                    this.req_getCustomStep.on(this.dbClient), stepName, flowName
                    );
            }
            private com.fasterxml.jackson.databind.JsonNode getCustomStep(BaseProxy.DBFunctionRequest request, String stepName, String flowName) {
              return BaseProxy.JsonDocumentType.toJsonNode(
                request
                      .withParams(
                          BaseProxy.atomicParam("stepName", false, BaseProxy.StringType.fromString(stepName)),
                          BaseProxy.atomicParam("flowName", false, BaseProxy.StringType.fromString(flowName))
                          ).responseSingle(false, Format.JSON)
                );
            }
        }

        return new CustomServiceImpl(db, serviceDeclaration);
    }

  /**
   * Invokes the getCustomStepsGroupedByEntity operation on the database server
   *
   * 
   * @return	Return an array of custom steps grouped by entity. The steps that don't have an associated entity are part of 'No Entity Type' group
   */
    com.fasterxml.jackson.databind.JsonNode getCustomStepsGroupedByEntity();

  /**
   * Invokes the getCustomStep operation on the database server
   *
   * @param stepName	provides input
   * @param flowName	provides input
   * @return	Returns an inline custom step
   */
    com.fasterxml.jackson.databind.JsonNode getCustomStep(String stepName, String flowName);

}
