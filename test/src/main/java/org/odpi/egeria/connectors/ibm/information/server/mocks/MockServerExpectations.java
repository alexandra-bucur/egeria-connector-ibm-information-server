/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.egeria.connectors.ibm.information.server.mocks;

import org.mockserver.client.MockServerClient;
import org.mockserver.client.initialize.PluginExpectationInitializer;
import org.mockserver.matchers.MatchType;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.JsonBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.Parameter.param;
import static org.odpi.egeria.connectors.ibm.information.server.mocks.MockConstants.*;

/**
 * Setup a mock server to act as an IGC REST API endpoint against which we can do some thorough testing.
 */
public class MockServerExpectations implements PluginExpectationInitializer {

    private static final Logger log = LoggerFactory.getLogger(MockServerExpectations.class);

    /**
     * Setup the expectations we will need to respond to various tests.
     * @param mockServerClient the client against which to set the expectations
     */
    @Override
    public void initializeExpectations(MockServerClient mockServerClient) {

        initializeTypeDetails(mockServerClient);
        initializeIGCClientExpectations(mockServerClient);
        initializeIAClientExpectations(mockServerClient);
        initializeExampleInstances(mockServerClient);
        initializeIGCConnectorExpectations(mockServerClient);
        initializeDataStageConnectorExpectations(mockServerClient);

        initializeSecurityTestExpectations(mockServerClient);

        setDefaultSearchResponseToNoResults(mockServerClient);

    }

    private void initializeTypeDetails(MockServerClient mockServerClient) {

        setTypesQuery(mockServerClient);
        Resource[] typeFiles = getFilesMatchingPattern("types/*.json");
        if (typeFiles != null) {
            for (Resource typeFile : typeFiles) {
                setTypeDetails(mockServerClient, typeFile.getFilename());
            }
        }

    }

    private void initializeIGCClientExpectations(MockServerClient mockServerClient) {

        setStartupQuery(mockServerClient);
        setTypesQuery(mockServerClient);
        setMultipageSearch(mockServerClient);
        setSortedSearch(mockServerClient);
        setBundlesQuery(mockServerClient);

        setCreateAssetRequest(mockServerClient);
        setUpdateAssetRequest(mockServerClient);

        String glossaryIgcType = "category";
        setExamplePartAsset(mockServerClient, glossaryIgcType, MockConstants.GLOSSARY_RID);
        setExampleAssetWithModDetails(mockServerClient, glossaryIgcType, MockConstants.GLOSSARY_RID);

        setQualifiedNameSearch(mockServerClient);
        setForeignKeyFindByPropertyValue(mockServerClient);
        setSemanticAssignmentFindByProperty(mockServerClient);

        setCreateBundle(mockServerClient);

        setVirtualAssets(mockServerClient);

        setIGCLogout(mockServerClient);

    }

    private void initializeIAClientExpectations(MockServerClient mockServerClient) {

        setProjectsQuery(mockServerClient);
        setProjectDetailQuery(mockServerClient, IA_PROJECT_NAME);
        setPublishedResultsQuery(mockServerClient, IA_PROJECT_NAME);

        setColumnAnalysisResultsQuery(mockServerClient, IA_PROJECT_NAME, IA_TABLE_NAME);
        setDataQualityResultsQuery(mockServerClient, IA_PROJECT_NAME, IA_TABLE_NAME);
        setDataQualityResultsQuery(mockServerClient, IA_PROJECT_NAME, IA_TABLE_NAME_WITH_DQ_PROBLEMS);

        setRunColumnAnalysis(mockServerClient, IA_PROJECT_NAME, IA_TABLE_NAME);
        setRunDataQualityAnalysis(mockServerClient, IA_PROJECT_NAME, IA_TABLE_NAME);
        setRunningColumnAnalysis(mockServerClient, IA_CA_SCHEDULE_ID, IA_TABLE_NAME);
        setRunningDataQualityAnalysis(mockServerClient, IA_DQ_SCHEDULE_ID, IA_TABLE_NAME);
        setCompleteColumnAnalysis(mockServerClient, IA_CA_SCHEDULE_ID, IA_TABLE_NAME);
        setCompleteDataQualityAnalysis(mockServerClient, IA_DQ_SCHEDULE_ID, IA_TABLE_NAME);
        setFormatDistribution(mockServerClient, IA_PROJECT_NAME, IA_COLUMN_NAME);
        setFrequencyDistribution(mockServerClient, IA_PROJECT_NAME, IA_COLUMN_NAME);

        setPublishResults(mockServerClient, IA_PROJECT_NAME, IA_TABLE_NAME);

        setIALogout(mockServerClient);

    }

    private void initializeExampleInstances(MockServerClient mockServerClient) {

        Resource[] instanceExamples = getFilesMatchingPattern("by_rid/**/*.json");
        if (instanceExamples != null) {
            for (Resource instanceExample : instanceExamples) {
                setDetailsByRidQuery(mockServerClient, instanceExample);
            }
        }

        Resource[] childExamples = getFilesMatchingPattern("by_child_rid/**/*.json");
        if (childExamples != null) {
            for (Resource relationshipExample : childExamples) {
                setDetailsByChildRidQuery(mockServerClient, relationshipExample);
            }
        }

        Resource[] parentExamples = getFilesMatchingPattern("by_parent_rid/**/*.json");
        if (parentExamples != null) {
            for (Resource relationshipExample : parentExamples) {
                setDetailsByParentRidQuery(mockServerClient, relationshipExample);
            }
        }

        Resource[] referenceExamples = getFilesMatchingPattern("ref_by_rid/*.json");
        if (referenceExamples != null) {
            for (Resource referenceExample : referenceExamples) {
                setReferenceByRidQuery(mockServerClient, referenceExample);
            }
        }

        Resource[] fullExamples = getFilesMatchingPattern("full_by_rid/*.json");
        if (fullExamples != null) {
            for (Resource fullExample : fullExamples) {
                setFullByRidQuery(mockServerClient, fullExample);
            }
        }

        Resource[] virtualExamples = getFilesMatchingPattern("virtual_by_rid/*.json");
        if (virtualExamples != null) {
            for (Resource virtualExample : virtualExamples) {
                setVirtualByRidQuery(mockServerClient, virtualExample);
            }
        }

    }

    private void initializeIGCConnectorExpectations(MockServerClient mockServerClient) {

        setUploadBundle(mockServerClient);

        // Setup responses for specific test cases

        // Glossary tests
        setGlossaryFindByPropertyValue(mockServerClient);
        setGlossaryRelationships(mockServerClient);

        // GlossaryCategory tests
        setGlossaryCategoryFindByPropertyValue(mockServerClient);
        setGlossaryCategoryRelationships(mockServerClient);

        // GlossaryTerm search tests
        setGlossaryTermFindByPropertyValue(mockServerClient);
        setGlossaryTermFindByProperty_displayName(mockServerClient);
        setGlossaryTermFindByProperties_ANY(mockServerClient);
        setGlossaryTermFindByProperties_ALL(mockServerClient);

        // Supertype search tests (can skip folder and schema as they return no results, so our catch-all will handle)
        setAssetFindByPropertyValue(mockServerClient);

        // Limit by classification tests (can skip many as they will return no results)
        setAllTypesFindByPropertyValue_limitToConfidentiality(mockServerClient);
        setGlossaryTermFindByClassification(mockServerClient);

        // All types search tests (can skip many as they will return no results, so our catch-all will handle)
        setAllTypesFindByPropertyValue(mockServerClient);

        // Relationship tests
        setGlossaryTermRelationships(mockServerClient);
        setDatabaseRelationships(mockServerClient);
        setConnectionRelationships(mockServerClient);
        setEndpointRelationships(mockServerClient);
        setDeployedDatabaseSchemaRelationships(mockServerClient);
        setRelationalDBSchemaTypeRelationships(mockServerClient);
        setRelationalTableRelationships(mockServerClient);
        setRelationalColumnRelationships(mockServerClient);
        setDataClassRelationships(mockServerClient);
        setConnectionFSRelationships(mockServerClient);
        setDataFileFolderRelationships(mockServerClient);
        setDataFileRelationships(mockServerClient);
        setTabularSchemaTypeRelationships(mockServerClient);
        setTabularColumnRelationships(mockServerClient);

        // Complex search tests
        setFindActorProfile(mockServerClient);
        setFindEntitiesByQualifiedName(mockServerClient);
        setFindDataClassByProperty(mockServerClient);
        setFindDataClassAssignmentByProperty(mockServerClient);
        setFindSchemaElementByAnchorGUID(mockServerClient);
        setFindGovernanceDefinitionByDomain(mockServerClient);
        setFindColumnsByPrimaryKeyName(mockServerClient);
        setFindFileByType(mockServerClient);
        setFindCategoryBySubjectAreaName(mockServerClient);
        setFindContactDetailsByProperty(mockServerClient);
        setFindSchemaTypeByNamespace(mockServerClient);

        // Event tests
        setChangeSetTest(mockServerClient);
        setTermAddEvent(mockServerClient);
        setDataFileDeleteEvent(mockServerClient);
        setIMAMShareEvent(mockServerClient);
        setIAPublishEvent(mockServerClient);

    }

    private void initializeDataStageConnectorExpectations(MockServerClient mockServerClient) {

        setJobSyncRuleQueryEmpty(mockServerClient);
        setJobSyncRuleQueryFull(mockServerClient);

        setJobSyncRuleCreate(mockServerClient);
        setJobSyncRuleUpdate(mockServerClient);

        setLineageDetectResult(mockServerClient);
        setJobChangeQuery(mockServerClient);
        setJobByRidQueries(mockServerClient);

    }

    private void initializeSecurityTestExpectations(MockServerClient mockServerClient) {
        // IGC
        mockServerClient
                .withSecure(true)
                .when(searchRequest(json("{\"types\":[\"security_test\"],\"pageSize\":1}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(
                        withResponse(getResourceFileContents("no_results.json"))
                                .withHeaders(
                                        Header.header("Set-Cookie", "InvalidCookie")
                                )
                );
        mockServerClient
                .withSecure(true)
                .when(searchRequest(json("{\"types\":[\"security_test\"],\"pageSize\":2}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(
                        withResponse(getResourceFileContents("no_results.json"))
                                .withHeaders(
                                        Header.header("Set-Cookie", "NotInWhitelist=abc123; Path=/; Secure; HttpOnly")
                                )
                );
        // Would be ideal to test with a newline below, but even Mock Server validates this and does not allow it...
        mockServerClient
                .withSecure(true)
                .when(searchRequest(json("{\"types\":[\"security_test\"],\"pageSize\":3}", MatchType.ONLY_MATCHING_FIELDS)))
                .respond(
                        withResponse(getResourceFileContents("no_results.json"))
                                .withHeaders(
                                        Header.header("Set-Cookie", "JSESSIONID=abc123\t&JSESSIONID=somehackattempt; Path=/; Secure; HttpOnly")
                                )
                );
        // IA
        mockServerClient
                .withSecure(true)
                .when(getProjectDetailsRequest("SecurityTest1"))
                .respond(
                        withResponse(getResourceFileContents("ia" + File.separator + "project_CocoPharma.xml"))
                                .withHeaders(
                                        Header.header("Set-Cookie", "InvalidCookie")
                                )
                );
        mockServerClient
                .withSecure(true)
                .when(getProjectDetailsRequest("SecurityTest2"))
                .respond(
                        withResponse(getResourceFileContents("ia" + File.separator + "project_CocoPharma.xml"))
                                .withHeaders(
                                        Header.header("Set-Cookie", "NotInWhitelist=abc123; Path=/; Secure; HttpOnly")
                                )
                );
        mockServerClient
                .withSecure(true)
                .when(getProjectDetailsRequest("SecurityTest3"))
                .respond(
                        withResponse(getResourceFileContents("ia" + File.separator + "project_CocoPharma.xml"))
                                .withHeaders(
                                        Header.header("Set-Cookie", "JSESSIONID=abc123\t&JSESSIONID=somehackattempt; Path=/; Secure; HttpOnly")
                                )
                );
    }

    private void setStartupQuery(MockServerClient mockServerClient) {
        // First time respond with an invalid session
        mockServerClient
                .withSecure(true)
                .when(searchRequest("{\"types\":[\"category\",\"term\",\"information_governance_policy\",\"information_governance_rule\"],\"pageSize\":1,\"workflowMode\":\"draft\"}"),
                        Times.exactly(1))
                .respond(withResponse(getResourceFileContents("no_results.json")).withStatusCode(403));
        mockServerClient
                .withSecure(true)
                .when(searchRequest("{\"types\":[\"category\",\"term\",\"information_governance_policy\",\"information_governance_rule\"],\"pageSize\":1,\"workflowMode\":\"draft\"}"))
                .respond(
                        withResponse(getResourceFileContents("no_results.json"))
                                .withHeaders(
                                        Header.header("Set-Cookie", "LtpaToken2=CEElCeZLmydlWRiPOMGlL+arDhgYVqXNMaoH5Cif5wTy+MQAAJR/jcV5amcyQGF1LpKbukQbXwMO+oeGTzUjRGCN0JZyomB8zn7C8R31LMJpyhIpILbJym7WoiN/pOp1mBIY7ZPbkBahKKIa8JSSYvbeJUlPvS9aNKYn4+qgYzBmQf1lIbewy/UBH0cK1kYkjHKW4dsKOzZWEdixD/LE7VDHki/rEdYyprZAgqDjNb9NDRWCxy25hmlaMQ7/PBkXu/2QJuCUqaoLautLARe9gVHfTP3Zg0/KzzaEkdV0z5GQ7OO7NapLM00ahBKnszD8G7YfuSJQKeUSWrhljlrLgxzpFAyiIGtPfXGrsYKI7nKDn8OQ/+3/2OBCb9PE6uUFHCeNceKqNYj7wBa2Q2P+6w==; Path=/; Secure; HttpOnly"),
                                        Header.header("Set-Cookie", "JSESSIONID=0000_tevtrBb61tdc2_JqGF4bPn:a746286c-69ed-4c45-bab5-21ed1a232fe2; Path=/; Secure; HttpOnly"),
                                        Header.header("Set-Cookie", "X-IBM-IISSessionId={issvr2048:isf}MfvbYdnWcOxwLPR7aD8OrSlEaSv_7dZCWXX1L-X1OuxK3-pA9aRN-xQJxZKJcL0WX4l-eqj8NQw8qxJ3K_sZ8bpPZz7mJjmaUJ_og24MotbeiyNV1GqiTM6c5M8DU3-GvZroFgJCZ_f_2h9R8FyBhCxi5ppV4oA96--v5BBVMC75Nsd-Rwe0nbGsRmJTTFEnTi26Gz5GEWmDvcXwWWrMJkZSd2TXlKM6HHkaXu5QkA_I5U5LpFfzqcOVPgHOPuh_h5a80LJ8ojBSTOrGchSIpT3LamusmsiPd6xHb_CoqDiCvxmRUdGJCGPIhqU0MNv5q62GiqqWXUgVd9SMF1LE0w..; Path=/; Secure; HttpOnly"),
                                        Header.header("Set-Cookie", "X-IBM-IISSessionToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJpc2FkbWluIiwiYXVkIjoiSUlTIiwidWlkIjoiaXNhZG1pbiIsInJvbGVzIjpbIlNvcmNlcmVyVXNlciIsIlJ1bGVzQWRtaW5pc3RyYXRvciIsIkRhdGFTdGFnZUFkbWluIiwiUnVsZXNBdXRob3IiLCJSdWxlc01hbmFnZXIiLCJNRFdBZG1pbmlzdHJhdG9yIiwiQ01BZG1pbiIsIlJ1bGVzVXNlciIsIlNvcmNlcmVyQWRtaW4iLCJHbG9zc2FyeUFkbWluIiwiU3VpdGVVc2VyIiwiU3VpdGVBZG1pbiIsIlNvcmNlcmVyRGF0YUFkbWluIiwiRGF0YVN0YWdlVXNlciJdLCJkaXNwbGF5X25hbWUiOiJBZG1pbmlzdHJhdG9yLCBJSVMiLCJleHAiOjE1ODEzOTYxNzMsImlhdCI6MTU4MTM1MDU3MywidXNlcm5hbWUiOiJpc2FkbWluIn0.hoPaX-qz9AglzsQIyBg9YLEZOGzUb9xjT_SBTD4iMIFexZ2Zon2xlwA6rcWYl0SIw3wO3PBNZmWzpJcoDYCcuADVXy1q3hJT_WRGo5ELrvT_mWcPfNeQk_AXVsZlS-1dR4OXGOZ2VPYkl9ybBxdvE8Qgo17MgYxu8KV7qqnjFCeTINjzyoV9r0Q3uG2pPBk1moEtiUvrBBtma2D2TJttBP1zhCiBbqa2xb1meEI44rG8aeDC9K7xupaWeyxqEkuu0ywZO8O7DUC6LJEn2oMb9Bp5Pw7g7X6qY4qLW3Loq-zmGr0EaQ6aq1v-SgeIZ-AhC36iivQWsdQ9rvBuvccANg; Path=/; Secure; HttpOnly")
                                )
                );
    }

    private void setTypesQuery(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(typesRequest())
                .respond(withResponse(getResourceFileContents("types.json")));
    }

    private void setMultipageSearch(MockServerClient mockServerClient) {
        String caseName = "TermFindMultipage";
        List<String> properties = new ArrayList<>();
        properties.add("created_by");
        properties.add("created_on");
        properties.add("modified_by");
        properties.add("modified_on");
        setSearchAndResponse(mockServerClient, caseName, "results_1.json",
                "{\"types\":[\"term\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":2,\"where\":{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"address\"}],\"operator\":\"and\"},\"sorts\":[{\"property\":\"_id\",\"ascending\":true}]}");
        mockServerClient
                .withSecure(true)
                .when(nextPageRequest("term", properties, "2", "2", true))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + "TermFindMultipage" + File.separator + "results_2.json")));
        // Note: there appears to be a bug in IGC that prevents the sorting criteria from being passed on to subsequent pages of results
        mockServerClient
                .withSecure(true)
                .when(nextPageRequest("term", properties, "2", "4", false))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + "TermFindMultipage" + File.separator + "results_3.json")));
    }

    private void setSortedSearch(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "TermFindSorting", "results_positive.json",
                "{\"types\":[\"term\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":2,\"where\":{\"conditions\":[{\"property\":\"short_description\",\"operator\":\"<>\",\"value\":\"\"},{\"property\":\"name\",\"operator\":\"in\",\"value\":[\"Address Line 2\"],\"negated\":false}],\"operator\":\"and\"},\"sorts\":[{\"property\":\"name\",\"ascending\":true}]}");
    }

    private void setBundlesQuery(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(bundlesRequest())
                .respond(withResponse("[\"OMRS\"]"));
    }

    private void setTypeDetails(MockServerClient mockServerClient, String typeFilename) {
        String typeName = typeFilename.substring(0, typeFilename.indexOf(".json"));
        mockServerClient
                .withSecure(true)
                .when(typesRequest(typeName)
                                .withQueryStringParameters(
                                        param("showViewProperties", "true"),
                                        param("showCreateProperties", "true"),
                                        param("showEditProperties", "true")
                                )
                )
                .respond(withResponse(getResourceFileContents("types" + File.separator + typeName + ".json")));
    }

    private void setExamplePartAsset(MockServerClient mockServerClient, String type, String rid) {
        mockServerClient
                .withSecure(true)
                .when(searchRequest("{\"types\":[\"" + type + "\"],\"properties\":[\"short_description\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"_id\",\"operator\":\"=\",\"value\":\"" + rid + "\"}],\"operator\":\"and\"}}"))
                .respond(withResponse(getResourceFileContents("rid_part_" + rid + ".json")));
    }

    private void setExampleAssetWithModDetails(MockServerClient mockServerClient, String type, String rid) {
        mockServerClient
                .withSecure(true)
                .when(searchRequest("{\"types\":[\"" + type + "\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":2,\"where\":{\"conditions\":[{\"property\":\"_id\",\"operator\":\"=\",\"value\":\"" + rid + "\"}],\"operator\":\"and\"}}"))
                .respond(withResponse(getResourceFileContents("rid_mod_" + rid + ".json")));
    }

    private void setQualifiedNameSearch(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(searchRequest(
                        json(
                                "{\"types\":[\"database_column\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"database_table_or_view.name\",\"operator\":\"=\",\"value\":\"CONTACTEMAIL\"},{\"property\":\"database_table_or_view.database_schema.name\",\"operator\":\"=\",\"value\":\"DB2INST1\"},{\"property\":\"database_table_or_view.database_schema.database.name\",\"operator\":\"=\",\"value\":\"COMPDIR\"},{\"property\":\"database_table_or_view.database_schema.database.host.name\",\"operator\":\"=\",\"value\":\"INFOSVR\"},{\"property\":\"name\",\"operator\":\"=\",\"value\":\"EMAIL\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                                MatchType.ONLY_MATCHING_FIELDS
                        )))
                .respond(withResponse(getResourceFileContents("by_rid" + File.separator + "database_column" + File.separator + DATABASE_COLUMN_RID + ".json")));
    }

    private void setForeignKeyFindByPropertyValue(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "ForeignKeyFindByPropertyValue", "results.json",
                json(
                        "{\"types\":[\"database_column\"],\"properties\":[\"defined_foreign_key_references\",\"selected_foreign_key_references\"],\"where\":{\"conditions\":[{\"property\":\"defined_foreign_key_references\",\"operator\":\"isNull\",\"negated\":true},{\"property\":\"selected_foreign_key_references\",\"operator\":\"isNull\",\"negated\":true}],\"operator\":\"or\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setSemanticAssignmentFindByProperty(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "SemanticAssignmentFindByProperty", "results.json",
                json(
                        "{\"types\":[\"term\"],\"properties\":[\"assigned_assets\"],\"where\":{\"conditions\":[{\"property\":\"assigned_assets\",\"operator\":\"isNull\",\"negated\":true}],\"operator\":\"or\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setIGCLogout(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(igcLogoutRequest())
                .respond(response().withStatusCode(200));
    }

    private void setCreateBundle(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(createBundleRequest())
                .respond(response().withStatusCode(200));
    }

    private void setVirtualAssets(MockServerClient mockServerClient) {
        String caseName = "VirtualAssets";
        String rid = MockConstants.VIRTUAL_ASSET_TABLE_RID_ENCODED;
        mockServerClient
                .withSecure(true)
                .when(assetByRidRequest(rid))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + caseName + File.separator + "initial.json")));
        mockServerClient
                .withSecure(true)
                .when(assetPropertyByRidRequest(rid, "database_columns"))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + caseName + File.separator + "next_page.json")));
    }

    private void setUploadBundle(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(upsertBundleRequest())
                .respond(response().withStatusCode(200));
    }

    private void setCreateAssetRequest(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(createAssetRequest(
                        "{\"_type\":\"term\",\"name\":\"Test Term\",\"parent_category\":\"6662c0f2.ee6a64fe.001ms73o0.ft1a1dd.er0dsi.i5q6hj16mo65b060fndnp\",\"status\":\"CANDIDATE\"}"
                ))
                .respond(
                        response()
                                .withStatusCode(201)
                                .withHeader("Location","https://infosvr:9446/ibm/iis/igc-rest/v1/assets/" + RID_FOR_CREATE_AND_UPDATE)
                );
    }

    private void setUpdateAssetRequest(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(updateAssetRequest(
                        RID_FOR_CREATE_AND_UPDATE,
                        "{\"short_description\":\"Just a test short description.\",\"assigned_to_terms\":{\"items\":[\"" + TERM_RID + "\"],\"mode\":\"add\"},\"parent_category\":\"" + CATEGORY_RID + "\"}"
                ))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + "TermUpsert" + File.separator + "update.json")));
    }

    private void setJobSyncRuleQueryEmpty(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(jobSyncRuleRequest(), Times.exactly(2))
                .respond(withResponse(getResourceFileContents("no_results.json")));
    }

    private void setJobSyncRuleQueryFull(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(jobSyncRuleRequest())
                .respond(withResponse(getResourceFileContents("job_sync_rule.json")));
    }

    private void setJobSyncRuleCreate(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(jobSyncRuleCreateRequest())
                .respond(
                        response()
                                .withStatusCode(201)
                                .withHeader("Location","https://infosvr:9446/ibm/iis/igc-rest/v1/assets/" + RID_FOR_SYNC_RULE)
                );
    }

    private void setJobSyncRuleUpdate(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(jobSyncRuleUpdateRequest(RID_FOR_SYNC_RULE))
                .respond(withResponse(getResourceFileContents("job_sync_rule_update.json")));
    }

    private void setJobChangeQuery(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(searchRequest(
                        json(
                                "{\"types\":[\"dsjob\"],\"properties\":[\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"modified_on\",\"operator\":\"<=\"}],\"operator\":\"and\"}}",
                                MatchType.ONLY_MATCHING_FIELDS
                        )))
                .respond(withResponse(getResourceFileContents("changed_jobs.json")));
    }

    private void setJobByRidQueries(MockServerClient mockServerClient) {
        Resource[] jobFiles = getFilesMatchingPattern("by_rid/dsjob/*.json");
        if (jobFiles != null) {
            for (Resource jobFile : jobFiles) {
                String filename = jobFile.getFilename();
                if (filename != null) {
                    String rid = filename.substring(0, filename.indexOf(".json"));
                    mockServerClient
                            .withSecure(true)
                            .when(searchRequest(
                                    json(
                                            "{\"types\":[\"dsjob\"],\"properties\":[\"short_description\",\"long_description\",\"references_local_or_shared_containers\",\"type\",\"reads_from_(design)\",\"writes_to_(design)\",\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"_id\",\"operator\":\"=\",\"value\":\"" + rid + "\"}],\"operator\":\"and\"}}",
                                            MatchType.ONLY_MATCHING_FIELDS
                                    )))
                            .respond(withResponse(getResourceFileContents("by_rid" + File.separator + "dsjob" + File.separator + rid + ".json")));
                }
            }
        }
    }

    private void setLineageDetectResult(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(detectLineageRequest())
                .respond(withResponse(getResourceFileContents("lineage_detect.json")).withStatusCode(202));
    }

    private void setDetailsByRidQuery(MockServerClient mockServerClient, Resource resource) {
        URL url = null;
        try {
            url = resource.getURL();
        } catch (IOException e) {
            log.error("Unable to retrieve detailed file from: {}", resource, e);
        }
        if (url != null) {
            String filename = url.getFile();
            String rid = getRidFromFilename(filename);
            String type = getTypeFromFilename(filename);
            mockServerClient
                    .withSecure(true)
                    .when(searchRequest(
                            json(
                                    "{\"types\":[\"" + type + "\"],\"where\":{\"conditions\":[{\"property\":\"_id\",\"operator\":\"=\",\"value\":\"" + rid + "\"}]}}",
                                    MatchType.ONLY_MATCHING_FIELDS
                            )))
                    .respond(withResponse(getResourceFileContents("by_rid" + File.separator + type + File.separator + rid + ".json")));
        }
    }

    private void setDetailsByChildRidQuery(MockServerClient mockServerClient, Resource resource) {
        URL url = null;
        try {
            url = resource.getURL();
        } catch (IOException e) {
            log.error("Unable to retrieve detailed file from: {}", resource, e);
        }
        if (url != null) {
            String filename = url.getFile();
            String rid = getRidFromFilename(filename);
            String type = getTypeFromFilename(filename);
            String property = null;
            switch (type) {
                case "database_schema":
                    property = "database_tables";
                    break;
            }
            if (property != null) {
                mockServerClient
                        .withSecure(true)
                        .when(searchRequest(
                                json(
                                        "{\"types\":[\"" + type + "\"],\"where\":{\"conditions\":[{\"property\":\"" + property + "\",\"operator\":\"=\",\"value\":\"" + rid + "\"}]}}",
                                        MatchType.ONLY_MATCHING_FIELDS
                                )))
                        .respond(withResponse(getResourceFileContents("by_child_rid" + File.separator + type + File.separator + rid + ".json")));
            }
        }
    }

    private void setDetailsByParentRidQuery(MockServerClient mockServerClient, Resource resource) {
        URL url = null;
        try {
            url = resource.getURL();
        } catch (IOException e) {
            log.error("Unable to retrieve detailed file from: {}", resource, e);
        }
        if (url != null) {
            String filename = url.getFile();
            String rid = getRidFromFilename(filename);
            String type = getTypeFromFilename(filename);
            String property = null;
            switch (type) {
                case "database_column":
                    property = "database_table_or_view";
                    break;
                case "data_file_field":
                    property = "data_file_record";
                    break;
                case "stage_column":
                case "ds_stage_column":
                    property = "link.job_or_container";
                    break;
                case "link":
                case "stage":
                    property = "job_or_container";
                    break;
            }
            if (property != null) {
                mockServerClient
                        .withSecure(true)
                        .when(searchRequest(
                                json(
                                        "{\"types\":[\"" + type + "\"],\"where\":{\"conditions\":[{\"property\":\"" + property + "\",\"operator\":\"=\",\"value\":\"" + rid + "\"}]}}",
                                        MatchType.ONLY_MATCHING_FIELDS
                                )))
                        .respond(withResponse(getResourceFileContents("by_parent_rid" + File.separator + type + File.separator + rid + ".json")));
            }
        }
    }

    private void setReferenceByRidQuery(MockServerClient mockServerClient, Resource resource) {
        URL url = null;
        try {
            url = resource.getURL();
        } catch (IOException e) {
            log.error("Unable to retrieve reference file from: {}", resource, e);
        }
        if (url != null) {
            String filename = url.getFile();
            String rid = getRidFromFilename(filename);
            mockServerClient
                    .withSecure(true)
                    .when(searchRequest(
                            json(
                                    "{\"types\":[\"main_object\",\"classification\",\"label\",\"user\",\"group\"],\"where\":{\"conditions\":[{\"property\":\"_id\",\"operator\":\"=\",\"value\":\"" + rid + "\"}]}}",
                                    MatchType.ONLY_MATCHING_FIELDS
                            )))
                    .respond(withResponse(getResourceFileContents("ref_by_rid" + File.separator + rid + ".json")));
        }
    }

    private void setFullByRidQuery(MockServerClient mockServerClient, Resource resource) {
        URL url = null;
        try {
            url = resource.getURL();
        } catch (IOException e) {
            log.error("Unable to retrieve full details file from: {}", resource, e);
        }
        if (url != null) {
            String filename = url.getFile();
            String rid = getRidFromFilename(filename);
            mockServerClient
                    .withSecure(true)
                    .when(assetByRidRequest(rid))
                    .respond(withResponse(getResourceFileContents("full_by_rid" + File.separator + rid + ".json")));
        }
    }

    private void setVirtualByRidQuery(MockServerClient mockServerClient, Resource resource) {
        URL url = null;
        try {
            url = resource.getURL();
        } catch (IOException e) {
            log.error("Unable to retrieve virtual asset details file from: {}", resource, e);
        }
        if (url != null) {
            String filename = url.getFile();
            String rid = getRidFromFilename(filename);
            mockServerClient
                    .withSecure(true)
                    .when(assetByRidRequest("extern%3Afr%3A" + rid))
                    .respond(withResponse(getResourceFileContents("virtual_by_rid" + File.separator + rid + ".json")));
        }
    }

    private String getRidFromFilename(String filename) {
        return filename.substring(filename.lastIndexOf("/") + 1, filename.indexOf(".json"));
    }

    private String getTypeFromFilename(String filename) {
        String path = filename.substring(filename.indexOf("/") + 1, filename.lastIndexOf("/"));
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private void setDefaultSearchResponseToNoResults(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.searchRequest())
                .respond(withResponse(getResourceFileContents("no_results.json")));
    }

    private void setGlossaryFindByPropertyValue(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "GlossaryFindByPropertyValue", "results.json",
                json(
                        "{\"types\":[\"category\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"parent_category\",\"operator\":\"isNull\",\"negated\":false},{\"property\":\"name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"a\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"a\"},{\"property\":\"long_description\",\"operator\":\"like %{0}%\",\"value\":\"a\"},{\"property\":\"language\",\"operator\":\"like %{0}%\",\"value\":\"a\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setGlossaryRelationships(MockServerClient mockServerClient) {
        String caseName = "GlossaryRelationships";
        setSearchAndResponse(mockServerClient, caseName, "categories.json",
                "{\"types\":[\"category\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"category_path\",\"operator\":\"=\",\"value\":\"" + GLOSSARY_RID + "\"}],\"operator\":\"and\"}}");
        setSearchAndResponse(mockServerClient, caseName, "terms.json",
                "{\"types\":[\"term\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"parent_category.category_path\",\"operator\":\"=\",\"value\":\"" + GLOSSARY_RID + "\"},{\"property\":\"parent_category\",\"operator\":\"=\",\"value\":\"" + GLOSSARY_RID + "\"}],\"operator\":\"or\"}}");
        setSearchAndResponse(mockServerClient, caseName, "sorted_limited_categories.json",
                "{\"types\":[\"category\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":10,\"where\":{\"conditions\":[{\"property\":\"category_path\",\"operator\":\"=\",\"value\":\"" + GLOSSARY_RID + "\"}],\"operator\":\"and\"},\"sorts\":[{\"property\":\"_id\",\"ascending\":true}]}");
        setSearchAndResponse(mockServerClient, caseName, "sorted_limited_terms.json",
                "{\"types\":[\"term\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":10,\"where\":{\"conditions\":[{\"property\":\"parent_category.category_path\",\"operator\":\"=\",\"value\":\"" + GLOSSARY_RID + "\"},{\"property\":\"parent_category\",\"operator\":\"=\",\"value\":\"" + GLOSSARY_RID + "\"}],\"operator\":\"or\"},\"sorts\":[{\"property\":\"_id\",\"ascending\":true}]}");
    }

    private void setGlossaryCategoryFindByPropertyValue(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "GlossaryCategoryFindByPropertyValue", "results.json",
                json(
                        "{\"types\":[\"category\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"parent_category\",\"operator\":\"isNull\",\"negated\":true},{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"e\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"e\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setGlossaryCategoryRelationships(MockServerClient mockServerClient) {
        String caseName = "GlossaryCategoryRelationships";
        setSearchAndResponse(mockServerClient, caseName, "parent_category.json",
                "{\"types\":[\"category\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"subcategories\",\"operator\":\"=\",\"value\":\"" + CATEGORY_RID + "\"}],\"operator\":\"and\"}}");
        setSearchAndResponse(mockServerClient, caseName, "terms.json",
                "{\"types\":[\"term\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"parent_category\",\"operator\":\"=\",\"value\":\"" + CATEGORY_RID + "\"},{\"property\":\"referencing_categories\",\"operator\":\"=\",\"value\":\"" + CATEGORY_RID + "\"}],\"operator\":\"or\"}}");
    }

    private void setGlossaryTermFindByPropertyValue(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "GlossaryTermFindByPropertyValue", "results.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"long_description\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"abbreviation\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"example\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"usage\",\"operator\":\"like %{0}%\",\"value\":\"Address\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, "GlossaryTermFindByPropertyValue", "results.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"long_description\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"abbreviation\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"example\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"usage\",\"operator\":\"like %{0}%\",\"value\":\"address\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setGlossaryTermFindByProperty_displayName(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "GlossaryTermFindByProperty_displayName", "results.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"Address\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, "GlossaryTermFindByProperty_displayName", "results.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"address\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setGlossaryTermFindByProperties_ANY(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "GlossaryTermFindByProperties_ANY", "results.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"Number\"},{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"Address\"}],\"operator\":\"or\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, "GlossaryTermFindByProperties_ANY", "results.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"number\"},{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"address\"}],\"operator\":\"or\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setGlossaryTermFindByProperties_ALL(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "GlossaryTermFindByProperties_ALL", "results.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"number\"},{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"Address\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, "GlossaryTermFindByProperties_ALL", "results.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"number\"},{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"address\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setAssetFindByPropertyValue(MockServerClient mockServerClient) {
        String caseName = "AssetFindByPropertyValue";
        setSearchAndResponse(mockServerClient, caseName, "results_data_file.json",
                json(
                        "{\"types\":[\"data_file\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"COMPDIR\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"COMPDIR\"},{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"COMPDIR\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_data_file.json",
                json(
                        "{\"types\":[\"data_file\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"compdir\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"compdir\"},{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"compdir\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_database.json",
                json(
                        "{\"types\":[\"database\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"COMPDIR\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"COMPDIR\"},{\"property\":\"dbms\",\"operator\":\"like %{0}%\",\"value\":\"COMPDIR\"},{\"property\":\"dbms_server_instance\",\"operator\":\"like %{0}%\",\"value\":\"COMPDIR\"},{\"property\":\"dbms_version\",\"operator\":\"like %{0}%\",\"value\":\"COMPDIR\"},{\"property\":\"imported_from\",\"operator\":\"like %{0}%\",\"value\":\"COMPDIR\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_database.json",
                json(
                        "{\"types\":[\"database\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"compdir\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"compdir\"},{\"property\":\"dbms\",\"operator\":\"like %{0}%\",\"value\":\"compdir\"},{\"property\":\"dbms_server_instance\",\"operator\":\"like %{0}%\",\"value\":\"compdir\"},{\"property\":\"dbms_version\",\"operator\":\"like %{0}%\",\"value\":\"compdir\"},{\"property\":\"imported_from\",\"operator\":\"like %{0}%\",\"value\":\"compdir\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setAllTypesFindByPropertyValue_limitToConfidentiality(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "AllTypesFindByPropertyValue_limitToConfidentiality", "results.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"long_description\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"abbreviation\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"example\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"usage\",\"operator\":\"like %{0}%\",\"value\":\"Address\"}],\"operator\":\"or\"},{\"conditions\":[{\"conditions\":[{\"property\":\"assigned_to_terms.parent_category.name\",\"operator\":\"=\",\"value\":\"Confidentiality\"}],\"operator\":\"and\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, "AllTypesFindByPropertyValue_limitToConfidentiality", "results.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"long_description\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"abbreviation\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"example\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"usage\",\"operator\":\"like %{0}%\",\"value\":\"address\"}],\"operator\":\"or\"},{\"conditions\":[{\"conditions\":[{\"property\":\"assigned_to_terms.parent_category.name\",\"operator\":\"=\",\"value\":\"Confidentiality\"}],\"operator\":\"and\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        // Ensure data_class produces no results (next set of expectations will otherwise produce results)
        mockServerClient
                .withSecure(true)
                .when(searchRequest(
                        json(
                                "{\"types\":[\"data_class\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"class_code\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"example\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"expression\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"provider\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"data_class_type_single\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"valid_value_strings\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"data_type_filter_elements_enum\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"validValueReferenceFile\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"java_class_name_single\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"regular_expression_single\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"script\",\"operator\":\"like %{0}%\",\"value\":\"Address\"}],\"operator\":\"or\"},{\"conditions\":[{\"conditions\":[{\"property\":\"assigned_to_terms.parent_category.name\",\"operator\":\"=\",\"value\":\"Confidentiality\"}],\"operator\":\"and\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                                MatchType.ONLY_MATCHING_FIELDS
                        )
                ))
                .respond(withResponse(getResourceFileContents("no_results.json")));
        mockServerClient
                .withSecure(true)
                .when(searchRequest(
                        json(
                                "{\"types\":[\"data_class\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"class_code\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"example\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"expression\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"provider\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"data_class_type_single\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"valid_value_strings\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"data_type_filter_elements_enum\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"validValueReferenceFile\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"java_class_name_single\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"regular_expression_single\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"script\",\"operator\":\"like %{0}%\",\"value\":\"address\"}],\"operator\":\"or\"},{\"conditions\":[{\"conditions\":[{\"property\":\"assigned_to_terms.parent_category.name\",\"operator\":\"=\",\"value\":\"Confidentiality\"}],\"operator\":\"and\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                                MatchType.ONLY_MATCHING_FIELDS
                        )
                ))
                .respond(withResponse(getResourceFileContents("no_results.json")));
    }

    private void setAllTypesFindByPropertyValue(MockServerClient mockServerClient) {
        String caseName = "AllTypesFindByPropertyValue";
        setSearchAndResponse(mockServerClient, caseName, "results_term.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"long_description\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"abbreviation\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"example\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"usage\",\"operator\":\"like %{0}%\",\"value\":\"Address\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_term.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"long_description\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"abbreviation\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"example\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"usage\",\"operator\":\"like %{0}%\",\"value\":\"address\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_data_class.json",
                json(
                        "{\"types\":[\"data_class\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"example\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"class_code\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"expression\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"provider\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"data_class_type_single\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"valid_value_strings\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"data_type_filter_elements_enum\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"validValueReferenceFile\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"java_class_name_single\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"regular_expression_single\",\"operator\":\"like %{0}%\",\"value\":\"Address\"},{\"property\":\"script\",\"operator\":\"like %{0}%\",\"value\":\"Address\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_data_class.json",
                json(
                        "{\"types\":[\"data_class\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"short_description\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"example\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"class_code\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"expression\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"provider\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"data_class_type_single\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"valid_value_strings\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"data_type_filter_elements_enum\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"validValueReferenceFile\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"java_class_name_single\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"regular_expression_single\",\"operator\":\"like %{0}%\",\"value\":\"address\"},{\"property\":\"script\",\"operator\":\"like %{0}%\",\"value\":\"address\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setGlossaryTermFindByClassification(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "GlossaryTermFindByClassification", "results.json",
                json(
                        "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"assigned_to_terms.parent_category.name\",\"operator\":\"=\",\"value\":\"Confidentiality\"},{\"conditions\":[{\"property\":\"assigned_to_terms.name\",\"operator\":\"like {0}%\",\"value\":\"3 \"}],\"operator\":\"and\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setGlossaryTermRelationships(MockServerClient mockServerClient) {
        String caseName = "GlossaryTermRelationships";
        setSearchAndResponse(mockServerClient, caseName, "assigned_assets.json",
                "{\"types\":[\"category\",\"data_class\",\"data_file\",\"data_file_field\",\"data_file_folder\",\"data_file_record\",\"database\",\"database_column\",\"database_schema\",\"database_table\",\"host\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"assigned_to_terms\",\"operator\":\"=\",\"value\":\"" + TERM_RID + "\"}],\"operator\":\"or\"}}");
        setSearchAndResponse(mockServerClient, caseName, "parent_category.json",
                "{\"types\":[\"category\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"terms\",\"operator\":\"=\",\"value\":\"" + TERM_RID + "\"}],\"operator\":\"or\"}}");
    }

    private void setDatabaseRelationships(MockServerClient mockServerClient) {
        String caseName = "DatabaseRelationships";
        setSearchAndResponse(mockServerClient, caseName, "database_schema.json",
                "{\"types\":[\"database_schema\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"database\",\"operator\":\"=\",\"value\":\"" + DATABASE_RID + "\"}],\"operator\":\"or\"}}");
        setSearchAndResponse(mockServerClient, caseName, "data_connection.json",
                "{\"types\":[\"data_connection\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"imports_database\",\"operator\":\"=\",\"value\":\"" + DATABASE_RID + "\"}],\"operator\":\"or\"}}");
    }

    private void setConnectionRelationships(MockServerClient mockServerClient) {
        String caseName = "ConnectionRelationships";
        setSearchAndResponse(mockServerClient, caseName, "database.json",
                "{\"types\":[\"database\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_connections\",\"operator\":\"=\",\"value\":\"" + DATA_CONNECTION_RID + "\"}],\"operator\":\"or\"}}");
        setSearchAndResponse(mockServerClient, caseName, "connector.json",
                "{\"types\":[\"connector\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_connections\",\"operator\":\"=\",\"value\":\"" + DATA_CONNECTION_RID + "\"}],\"operator\":\"or\"}}");
        setSearchAndResponse(mockServerClient, caseName, "host.json",
                "{\"types\":[\"connector\"],\"properties\":[\"host\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_connections\",\"operator\":\"=\",\"value\":\"" + DATA_CONNECTION_RID + "\"}],\"operator\":\"and\"}}");
    }

    private void setEndpointRelationships(MockServerClient mockServerClient) {
        String caseName = "EndpointRelationships";
        setSearchAndResponse(mockServerClient, caseName, "data_connection.json",
                "{\"types\":[\"data_connection\"],\"properties\":[\"name\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_connectors.host\",\"operator\":\"=\",\"value\":\"" + HOST_RID + "\"}],\"operator\":\"and\"}}");
        setSearchAndResponse(mockServerClient, caseName, "sorted_and_limited.json",
                "{\"types\":[\"data_connection\"],\"properties\":[\"name\"],\"pageSize\":5,\"where\":{\"conditions\":[{\"property\":\"data_connectors.host\",\"operator\":\"=\",\"value\":\"" + HOST_RID + "\"}],\"operator\":\"and\"},\"sorts\":[{\"property\":\"_id\",\"ascending\":true}]}");
    }

    private void setDeployedDatabaseSchemaRelationships(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "DeployedDatabaseSchemaRelationships", "database.json",
                "{\"types\":[\"database\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"database_schemas\",\"operator\":\"=\",\"value\":\"" + DATABASE_SCHEMA_RID + "\"}],\"operator\":\"or\"}}");
    }

    private void setRelationalDBSchemaTypeRelationships(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "RelationalDBSchemaTypeRelationships", "database_table.json",
                "{\"types\":[\"database_table\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"database_schema\",\"operator\":\"=\",\"value\":\"" + DATABASE_SCHEMA_RID + "\"}],\"operator\":\"or\"}}");
    }

    private void setRelationalTableRelationships(MockServerClient mockServerClient) {
        String caseName = "RelationalTableRelationships";
        setSearchAndResponse(mockServerClient, caseName, "database_schema.json",
                "{\"types\":[\"database_schema\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"database_tables\",\"operator\":\"=\",\"value\":\"" + DATABASE_TABLE_RID + "\"}],\"operator\":\"or\"}}");
        setSearchAndResponse(mockServerClient, caseName, "database_column.json",
                "{\"types\":[\"database_column\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"database_table_or_view\",\"operator\":\"=\",\"value\":\"" + DATABASE_TABLE_RID + "\"}],\"operator\":\"or\"}}");
    }

    private void setRelationalColumnRelationships(MockServerClient mockServerClient) {
        String caseName = "RelationalColumnRelationships";
        setSearchAndResponse(mockServerClient, caseName, "term.json",
                "{\"types\":[\"term\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"assigned_assets\",\"operator\":\"=\",\"value\":\"" + DATABASE_COLUMN_RID + "\"}],\"operator\":\"or\"}}");
        setSearchAndResponse(mockServerClient, caseName, "classification.json",
                "{\"types\":[\"classification\"],\"properties\":[\"confidencePercent\",\"data_class\",\"threshold\",\"value_frequency\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"classifies_asset\",\"operator\":\"=\",\"value\":\"" + DATABASE_COLUMN_RID + "\"}],\"operator\":\"and\"}}");
        setSearchAndResponse(mockServerClient, caseName, "database_table.json",
                "{\"types\":[\"database_table\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"database_columns\",\"operator\":\"=\",\"value\":\"" + DATABASE_COLUMN_RID + "\"}],\"operator\":\"or\"}}");
    }

    private void setDataClassRelationships(MockServerClient mockServerClient) {
        String caseName = "DataClassRelationships";
        setSearchAndResponse(mockServerClient, caseName, "classification.json",
                "{\"types\":[\"classification\"],\"properties\":[\"classifies_asset\",\"confidencePercent\",\"threshold\",\"value_frequency\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_class\",\"operator\":\"=\",\"value\":\"" + DATA_CLASS_RID + "\"}],\"operator\":\"and\"}}");
        setSearchAndResponse(mockServerClient, caseName, "data_item.json",
                "{\"types\":[\"amazon_s3_data_file_field\",\"data_file_field\",\"database_column\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\",\"selected_classification\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"selected_classification\",\"operator\":\"=\",\"value\":\"" + DATA_CLASS_RID + "\"}],\"operator\":\"and\"}}");
        setSearchAndResponse(mockServerClient, caseName, "classification_by_guid.json",
                "{\"types\":[\"classification\"],\"properties\":[\"classifies_asset\",\"confidencePercent\",\"threshold\",\"value_frequency\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_class\",\"operator\":\"=\",\"value\":\"" + DATA_CLASS_RID + "\"}],\"operator\":\"and\"},\"sorts\":[{\"property\":\"_id\",\"ascending\":true}]}");
    }

    private void setConnectionFSRelationships(MockServerClient mockServerClient) {
        String caseName = "ConnectionFSRelationships";
        setSearchAndResponse(mockServerClient, caseName, "data_file_folder.json",
                "{\"types\":[\"data_file_folder\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_connection\",\"operator\":\"=\",\"value\":\"" + DATA_CONNECTION_RID_FS + "\"}],\"operator\":\"or\"}}");
        setSearchAndResponse(mockServerClient, caseName, "connector.json",
                "{\"types\":[\"connector\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_connections\",\"operator\":\"=\",\"value\":\"" + DATA_CONNECTION_RID_FS + "\"}],\"operator\":\"or\"}}");
        setSearchAndResponse(mockServerClient, caseName, "host.json",
                "{\"types\":[\"connector\"],\"properties\":[\"host\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_connections\",\"operator\":\"=\",\"value\":\"" + DATA_CONNECTION_RID_FS + "\"}],\"operator\":\"and\"}}");
    }

    private void setDataFileFolderRelationships(MockServerClient mockServerClient) {
        String caseName = "DataFileFolderRelationships";
        setSearchAndResponse(mockServerClient, caseName, "data_file_folder.json",
                "{\"types\":[\"data_file_folder\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_file_folders\",\"operator\":\"=\",\"value\":\"" + DATA_FILE_FOLDER_RID + "\"}],\"operator\":\"and\"}}");
        setSearchAndResponse(mockServerClient, caseName, "data_file.json",
                "{\"types\":[\"data_file\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"parent_folder\",\"operator\":\"=\",\"value\":\"" + DATA_FILE_FOLDER_RID + "\"}],\"operator\":\"or\"}}");
    }

    private void setDataFileRelationships(MockServerClient mockServerClient) {
        String caseName = "DataFileRelationships";
        setSearchAndResponse(mockServerClient, caseName, "data_file_record.json",
                "{\"types\":[\"data_file_record\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_file\",\"operator\":\"=\",\"value\":\"" + DATA_FILE_RID + "\"}],\"operator\":\"or\"}}");
        setSearchAndResponse(mockServerClient, caseName, "data_file_folder.json",
                "{\"types\":[\"data_file_folder\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_files\",\"operator\":\"=\",\"value\":\"" + DATA_FILE_RID + "\"}],\"operator\":\"or\"}}");
    }

    private void setTabularSchemaTypeRelationships(MockServerClient mockServerClient) {
        String caseName = "TabularSchemaTypeRelationships";
        setSearchAndResponse(mockServerClient, caseName, "data_file.json",
                "{\"types\":[\"data_file\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_file_records\",\"operator\":\"=\",\"value\":\"" + DATA_FILE_RECORD_RID + "\"}],\"operator\":\"or\"}}");
        setSearchAndResponse(mockServerClient, caseName, "data_file_field.json",
                "{\"types\":[\"data_file_field\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_file_record\",\"operator\":\"=\",\"value\":\"" + DATA_FILE_RECORD_RID + "\"}],\"operator\":\"or\"}}");
    }

    private void setTabularColumnRelationships(MockServerClient mockServerClient) {
        String caseName = "TabularColumnRelationships";
        setSearchAndResponse(mockServerClient, caseName, "term.json",
                "{\"types\":[\"term\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"assigned_assets\",\"operator\":\"=\",\"value\":\"" + DATA_FILE_FIELD_RID + "\"}],\"operator\":\"or\"}}");
        setSearchAndResponse(mockServerClient, caseName, "data_file_record.json",
                "{\"types\":[\"data_file_record\"],\"properties\":[\"created_by\",\"created_on\",\"modified_by\",\"modified_on\"],\"pageSize\":100,\"where\":{\"conditions\":[{\"property\":\"data_file_fields\",\"operator\":\"=\",\"value\":\"" + DATA_FILE_FIELD_RID + "\"}],\"operator\":\"or\"}}");
    }

    private void setFindActorProfile(MockServerClient mockServerClient) {
        String caseName = "FindActorProfile";
        setSearchAndResponse(mockServerClient, caseName, "results_group.json",
                json(
                        "{\"types\":[\"group\"],\"where\":{\"conditions\":[{\"property\":\"principal_id\",\"operator\":\"=\",\"value\":\"cocopharma_external\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_user.json",
                json(
                        "{\"types\":[\"user\"],\"where\":{\"conditions\":[{\"property\":\"principal_id\",\"operator\":\"=\",\"value\":\"cquartile\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setFindEntitiesByQualifiedName(MockServerClient mockServerClient) {
        String caseName = "FindEntitiesByQualifiedName";
        setSearchAndResponse(mockServerClient, caseName, "results_all.json",
                "{\"types\":[\"database_column\"],\"properties\":[\"allows_null_values\",\"averageValue\",\"constantFlag\",\"created_by\",\"created_on\",\"data_type\",\"default_value\",\"defined_foreign_key\",\"defined_primary_key\",\"domainType\",\"fraction\",\"inferredDataType\",\"inferredFormat\",\"inferredLength\",\"inferredPrecision\",\"inferredScale\",\"isInferredForeignKey\",\"isInferredPrimaryKey\",\"length\",\"level\",\"long_description\",\"minimum_length\",\"modified_by\",\"modified_on\",\"name\",\"native_id\",\"nbRecordsTested\",\"nullabilityFlag\",\"numberCompleteValues\",\"numberDistinctValues\",\"numberEmptyValues\",\"numberFormats\",\"numberNullValues\",\"numberValidValues\",\"numberZeroValues\",\"occurs\",\"odbc_type\",\"position\",\"qualityScore\",\"qualityScore_bubble\",\"quality_benchmark\",\"selected_foreign_key\",\"selected_natural_key\",\"selected_primary_key\",\"short_description\",\"start_end_columns\",\"synchronized_from\",\"type\",\"unique\",\"uniqueFlag\"],\"pageSize\":100,\"sorts\":[{\"property\":\"_id\",\"ascending\":true}]}");
        setSearchAndResponse(mockServerClient, caseName, "results_all.json",
                "{\"types\":[\"database_column\"],\"properties\":[\"allows_null_values\",\"averageValue\",\"constantFlag\",\"created_by\",\"created_on\",\"data_type\",\"default_value\",\"defined_foreign_key\",\"defined_primary_key\",\"domainType\",\"fraction\",\"inferredDataType\",\"inferredFormat\",\"inferredLength\",\"inferredPrecision\",\"inferredScale\",\"isInferredForeignKey\",\"isInferredPrimaryKey\",\"length\",\"level\",\"long_description\",\"minimum_length\",\"modified_by\",\"modified_on\",\"name\",\"native_id\",\"nbRecordsTested\",\"nullabilityFlag\",\"numberCompleteValues\",\"numberDistinctValues\",\"numberEmptyValues\",\"numberFormats\",\"numberNullValues\",\"numberValidValues\",\"numberZeroValues\",\"occurs\",\"odbc_type\",\"position\",\"qualityScore\",\"qualityScore_bubble\",\"quality_benchmark\",\"selected_foreign_key\",\"selected_natural_key\",\"selected_primary_key\",\"short_description\",\"start_end_columns\",\"synchronized_from\",\"type\",\"unique\",\"uniqueFlag\"],\"pageSize\":10,\"sorts\":[{\"property\":\"_id\",\"ascending\":true}]}");
        setSearchAndResponse(mockServerClient, caseName, "results_one.json",
                json(
                        "{\"types\":[\"database_column\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"database_table_or_view.name\",\"operator\":\"=\",\"value\":\"CONTACTEMAIL\"},{\"property\":\"database_table_or_view.database_schema.name\",\"operator\":\"=\",\"value\":\"DB2INST1\"},{\"property\":\"database_table_or_view.database_schema.database.name\",\"operator\":\"=\",\"value\":\"COMPDIR\"},{\"property\":\"database_table_or_view.database_schema.database.host.name\",\"operator\":\"=\",\"value\":\"INFOSVR\"},{\"property\":\"name\",\"operator\":\"=\",\"value\":\"EMAIL\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_one.json",
                json(
                        "{\"types\":[\"database_column\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"database_table_or_view.name\",\"operator\":\"=\",\"value\":\"contactemail\"},{\"property\":\"database_table_or_view.database_schema.name\",\"operator\":\"=\",\"value\":\"db2inst1\"},{\"property\":\"database_table_or_view.database_schema.database.name\",\"operator\":\"=\",\"value\":\"compdir\"},{\"property\":\"database_table_or_view.database_schema.database.host.name\",\"operator\":\"=\",\"value\":\"infosvr\"},{\"property\":\"name\",\"operator\":\"=\",\"value\":\"email\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_one.json",
                json(
                        "{\"types\":[\"database_column\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"database_table_or_view.name\",\"operator\":\"=\",\"value\":\"CONTACTEMAIL\"},{\"property\":\"database_table_or_view.database_schema.name\",\"operator\":\"=\",\"value\":\"DB2INST1\"},{\"property\":\"database_table_or_view.database_schema.database.name\",\"operator\":\"=\",\"value\":\"COMPDIR\"},{\"property\":\"name\",\"operator\":\"=\",\"value\":\"EMAIL\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_one.json",
                json(
                        "{\"types\":[\"database_column\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"database_table_or_view.name\",\"operator\":\"=\",\"value\":\"CONTACTEMAIL\"},{\"property\":\"database_table_or_view.database_schema.name\",\"operator\":\"=\",\"value\":\"DB2INST1\"},{\"property\":\"name\",\"operator\":\"=\",\"value\":\"EMAIL\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_one.json",
                json(
                        "{\"types\":[\"database_column\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"database_table_or_view.name\",\"operator\":\"=\",\"value\":\"contactemail\"},{\"property\":\"database_table_or_view.database_schema.name\",\"operator\":\"=\",\"value\":\"db2inst1\"},{\"property\":\"database_table_or_view.database_schema.database.name\",\"operator\":\"=\",\"value\":\"compdir\"},{\"property\":\"name\",\"operator\":\"=\",\"value\":\"email\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_one.json",
                json(
                        "{\"types\":[\"database_column\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"database_table_or_view.name\",\"operator\":\"=\",\"value\":\"contactemail\"},{\"property\":\"database_table_or_view.database_schema.name\",\"operator\":\"=\",\"value\":\"db2inst1\"},{\"property\":\"name\",\"operator\":\"=\",\"value\":\"email\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_user.json",
                json(
                        "{\"types\":[\"user\"],\"where\":{\"conditions\":[{\"conditions\":[{\"conditions\":[{\"conditions\":[{\"property\":\"courtesy_title\",\"operator\":\"like {0}%\",\"value\":\"Mr. Gary Geeke\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"full_name\",\"operator\":\"like {0}%\",\"value\":\"Mr. Gary Geeke\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"courtesy_title\",\"operator\":\"like {0}%\",\"value\":\"Mr.\"},{\"property\":\"full_name\",\"operator\":\"like {0}%\",\"value\":\"Gary Geeke\"}],\"operator\":\"and\"}],\"operator\":\"or\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_user.json",
                json(
                        "{\"types\":[\"user\"],\"where\":{\"conditions\":[{\"conditions\":[{\"conditions\":[{\"conditions\":[{\"property\":\"courtesy_title\",\"operator\":\"like {0}%\",\"value\":\"mr. gary geeke\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"full_name\",\"operator\":\"like {0}%\",\"value\":\"mr. gary geeke\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"courtesy_title\",\"operator\":\"like {0}%\",\"value\":\"mr.\"},{\"property\":\"full_name\",\"operator\":\"like {0}%\",\"value\":\"gary geeke\"}],\"operator\":\"and\"}],\"operator\":\"or\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_user.json",
                json(
                        "{\"types\":[\"user\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"email_address\",\"operator\":\"like %{0}\",\"value\":\"Gary Geeke\"},{\"conditions\":[{\"conditions\":[{\"property\":\"courtesy_title\",\"operator\":\"like {0}%\",\"value\":\"Gary Geeke\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"courtesy_title\",\"operator\":\"like {0}%\",\"value\":\"Gary\"},{\"property\":\"given_name\",\"operator\":\"like {0}%\",\"value\":\"Geeke\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"given_name\",\"operator\":\"like {0}%\",\"value\":\"Gary Geeke\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"given_name\",\"operator\":\"like {0}%\",\"value\":\"Gary\"},{\"property\":\"surname\",\"operator\":\"like {0}%\",\"value\":\"Geeke\"}],\"operator\":\"and\"}],\"operator\":\"or\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_user.json",
                json(
                        "{\"types\":[\"user\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"full_name\",\"operator\":\"=\",\"value\":\"Geeke\"},{\"property\":\"job_title\",\"operator\":\"=\",\"value\":\"Geeke\"},{\"property\":\"principal_id\",\"operator\":\"=\",\"value\":\"Geeke\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_user.json",
                json(
                        "{\"types\":[\"user\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"full_name\",\"operator\":\"=\",\"value\":\"geeke\"},{\"property\":\"job_title\",\"operator\":\"=\",\"value\":\"geeke\"},{\"property\":\"principal_id\",\"operator\":\"=\",\"value\":\"geeke\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_user.json",
                "{\"types\":[\"user\"],\"properties\":[\"business_address\",\"courtesy_title\",\"created_by\",\"created_on\",\"email_address\",\"fax_number\",\"full_name\",\"given_name\",\"home_phone_number\",\"instant_message_id\",\"job_title\",\"location\",\"mobile_phone_number\",\"modified_by\",\"modified_on\",\"office_phone_number\",\"organization\",\"pager_number\",\"principal_id\",\"surname\"],\"pageSize\":100,\"sorts\":[{\"property\":\"_id\",\"ascending\":true}]}");
        setSearchAndResponse(mockServerClient, caseName, "results_user.json",
                json(
                        "{\"types\":[\"user\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"full_name\",\"operator\":\"like %{0}\",\"value\":\"Geeke\"},{\"property\":\"job_title\",\"operator\":\"like %{0}\",\"value\":\"Geeke\"},{\"property\":\"principal_id\",\"operator\":\"like %{0}\",\"value\":\"Geeke\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_user.json",
                json(
                        "{\"types\":[\"user\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"full_name\",\"operator\":\"like %{0}\",\"value\":\"geeke\"},{\"property\":\"job_title\",\"operator\":\"like %{0}\",\"value\":\"geeke\"},{\"property\":\"principal_id\",\"operator\":\"like %{0}\",\"value\":\"geeke\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_folder.json",
                json(
                        "{\"types\":[\"data_file_folder\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"parent_folder.name\",\"operator\":\"=\",\"value\":\"data\"},{\"property\":\"host.name\",\"operator\":\"=\",\"value\":\"INFOSVR\"},{\"property\":\"name\",\"operator\":\"=\",\"value\":\"files\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_folder.json",
                json(
                        "{\"types\":[\"data_file_folder\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"parent_folder.name\",\"operator\":\"=\",\"value\":\"data\"},{\"property\":\"host.name\",\"operator\":\"=\",\"value\":\"infosvr\"},{\"property\":\"name\",\"operator\":\"=\",\"value\":\"files\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_record.json",
                json(
                        "{\"types\":[\"data_file_record\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"data_file.name\",\"operator\":\"=\",\"value\":\"Employee-Dept.csv\"},{\"property\":\"data_file.path\",\"operator\":\"like %{0}\",\"value\":\"CocoPharma\"},{\"property\":\"name\",\"operator\":\"=\",\"value\":\"Employee-Dept\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_record.json",
                json(
                        "{\"types\":[\"data_file_record\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"data_file.name\",\"operator\":\"=\",\"value\":\"employee-dept.csv\"},{\"property\":\"data_file.path\",\"operator\":\"like %{0}\",\"value\":\"cocopharma\"},{\"property\":\"name\",\"operator\":\"=\",\"value\":\"employee-dept\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "database_5.json",
                "{\"types\":[\"database\"],\"properties\":[\"alias_(business_name)\",\"created_by\",\"created_on\",\"database_type\",\"dbms\",\"dbms_server_instance\",\"dbms_vendor\",\"dbms_version\",\"imported_from\",\"location\",\"long_description\",\"modified_by\",\"modified_on\",\"name\",\"native_id\",\"short_description\",\"synchronized_from\"],\"pageSize\":5,\"sorts\":[{\"property\":\"_id\",\"ascending\":true}]}");
        setSearchAndResponse(mockServerClient, caseName, "data_file_record_5.json",
                "{\"types\":[\"data_file_record\"],\"properties\":[\"FKViolationCount\",\"PKDuplicateCount\",\"alias_(business_name)\",\"created_by\",\"created_on\",\"fieldCount\",\"imported_from\",\"include_for_business_lineage\",\"long_description\",\"modified_by\",\"modified_on\",\"name\",\"native_id\",\"nbRecordTested\",\"qualityScore\",\"qualityScore_bubble\",\"quality_benchmark\",\"reviewDate\",\"rowCount\",\"short_description\",\"synchronized_from\"],\"pageSize\":5,\"sorts\":[{\"property\":\"_id\",\"ascending\":true}]}");
        setSearchAndResponse(mockServerClient, caseName, "data_connection_5.json",
                "{\"types\":[\"data_connection\"],\"properties\":[\"connection_string\",\"created_by\",\"created_on\",\"imported_from\",\"long_description\",\"modified_by\",\"modified_on\",\"name\",\"native_id\",\"short_description\"],\"pageSize\":5,\"sorts\":[{\"property\":\"_id\",\"ascending\":true}]}");
    }

    private void setFindDataClassByProperty(MockServerClient mockServerClient) {
        String caseName = "FindDataClassByProperty";
        mockServerClient
                .withSecure(true)
                .when(searchRequest(
                        json(
                                getResourceFileContents("by_case" + File.separator + caseName + File.separator + "query.json"),
                                MatchType.ONLY_MATCHING_FIELDS
                        )))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + caseName + File.separator + "results.json")));
        mockServerClient
                .withSecure(true)
                .when(searchRequest(
                        json(
                                getResourceFileContents("by_case" + File.separator + caseName + File.separator + "query_ci.json"),
                                MatchType.ONLY_MATCHING_FIELDS
                        )))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + caseName + File.separator + "results.json")));
    }

    private void setFindDataClassAssignmentByProperty(MockServerClient mockServerClient) {
        String caseName = "FindDataClassAssignmentByProperty";
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json("{\"types\":[\"classification\"],\"where\":{\"conditions\":[{\"property\":\"confidencePercent\",\"operator\":\"=\",\"value\":\"100\"},{\"property\":\"value_frequency\",\"operator\":\"=\",\"value\":\"34\"},{\"property\":\"confidencePercent\",\"operator\":\"=\",\"value\":\"100\"}],\"operator\":\"and\"}}",
                                MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "classifies_asset_b1c497ce.60641b50.001mv7qhr.03up14t.lob6gp.ktaasfbn0lqab0c6ee9rl.json",
                json(
                        "{\"types\":[\"classification\"],\"where\":{\"conditions\":[{\"property\":\"classifies_asset\",\"operator\":\"=\",\"value\":\"b1c497ce.60641b50.001mv7qhr.03up14t.lob6gp.ktaasfbn0lqab0c6ee9rl\"},{\"property\":\"data_class\",\"operator\":\"=\",\"value\":\"f4951817.e469fa50.001mtr2gq.i03lpp2.ff6ti2.b6ol04ugdbtt6u6eojunp\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "classifies_asset_b1c497ce.60641b50.001mv7qhr.03u3le8.ttmqon.c22r98a4u4ptdi8cvbmtu.json",
                json(
                        "{\"types\":[\"classification\"],\"where\":{\"conditions\":[{\"property\":\"classifies_asset\",\"operator\":\"=\",\"value\":\"b1c497ce.60641b50.001mv7qhr.03u3le8.ttmqon.c22r98a4u4ptdi8cvbmtu\"},{\"property\":\"data_class\",\"operator\":\"=\",\"value\":\"f4951817.e469fa50.001mtr2gq.nsrej8a.75d8rp.ud414oj262acep64l8dpq\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_partialMatch.json",
                json(
                        "{\"types\":[\"classification\"],\"where\":{\"conditions\":[{\"property\":\"confidencePercent\",\"operator\":\"<\",\"value\":\"100\"},{\"property\":\"value_frequency\",\"operator\":\"=\",\"value\":\"28\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "classifies_asset_b1c497ce.60641b50.001mts4re.732derp.7g9t50.2rjqcak0137pg2hevl7ne.json",
                json(
                        "{\"types\":[\"classification\"],\"where\":{\"conditions\":[{\"property\":\"classifies_asset\",\"operator\":\"=\",\"value\":\"b1c497ce.60641b50.001mts4re.732derp.7g9t50.2rjqcak0137pg2hevl7ne\"},{\"property\":\"data_class\",\"operator\":\"=\",\"value\":\"f4951817.e469fa50.001mtr2gq.nsrej8a.75d8rp.ud414oj262acep64l8dpq\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setFindSchemaElementByAnchorGUID(MockServerClient mockServerClient) {
        String caseName = "FindSchemaElementByAnchorGUID";
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"database_column\"],\"where\":{\"conditions\":[{\"conditions\":[{\"conditions\":[{\"property\":\"view.database_schema\",\"operator\":\"=\",\"value\":\"b1c497ce.c1fb060b.001mts4re.7331jdi.nnod24.3aci569ca741v10qq7pbb\"},{\"property\":\"database_table.database_schema\",\"operator\":\"=\",\"value\":\"b1c497ce.c1fb060b.001mts4re.7331jdi.nnod24.3aci569ca741v10qq7pbb\"}],\"operator\":\"or\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_table.json",
                json(
                        "{\"types\":[\"database_table\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"database_schema\",\"operator\":\"=\",\"value\":\"b1c497ce.c1fb060b.001mts4re.7331jdi.nnod24.3aci569ca741v10qq7pbb\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_fields.json",
                json(
                        "{\"types\":[\"data_file_field\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"data_file_record.data_file\",\"operator\":\"=\",\"value\":\"b1c497ce.6e76d866.001mts4ph.b7m5306.9nrsit.7gtv16ok5bkl9ulo5rq8b\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results_records.json",
                json(
                        "{\"types\":[\"data_file_record\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"data_file\",\"operator\":\"=\",\"value\":\"b1c497ce.6e76d866.001mts4ph.b7m5306.9nrsit.7gtv16ok5bkl9ulo5rq8b\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setFindGovernanceDefinitionByDomain(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "FindGovernanceDefinitionByDomain", "results.json",
                json(
                        "{\"types\":[\"information_governance_policy\"],\"where\":{\"conditions\":[{\"property\":\"parent_policy.name\",\"operator\":\"like %{0}%\",\"value\":\"Data Access\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, "FindGovernanceDefinitionByDomain", "results.json",
                json(
                        "{\"types\":[\"information_governance_policy\"],\"where\":{\"conditions\":[{\"property\":\"parent_policy.name\",\"operator\":\"like %{0}%\",\"value\":\"data access\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setFindColumnsByPrimaryKeyName(MockServerClient mockServerClient) {
        String caseName = "FindColumnsByPrimaryKeyName";
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"database_column\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"selected_primary_key\",\"operator\":\"=\",\"value\":\"true\"},{\"conditions\":[{\"property\":\"defined_primary_key.name\",\"operator\":\"like {0}%\",\"value\":\"SQL\"}],\"operator\":\"or\"}],\"operator\":\"or\"}],\"operator\":\"or\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"database_column\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"selected_primary_key\",\"operator\":\"=\",\"value\":\"true\"},{\"conditions\":[{\"property\":\"defined_primary_key.name\",\"operator\":\"like {0}%\",\"value\":\"sql\"}],\"operator\":\"or\"}],\"operator\":\"or\"}],\"operator\":\"or\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setFindFileByType(MockServerClient mockServerClient) {
        String caseName = "FindFileByType";
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"data_file\"],\"where\":{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}\",\"value\":\"CSV\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"data_file\"],\"where\":{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}\",\"value\":\"csv\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"data_file\"],\"where\":{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"CS\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"data_file\"],\"where\":{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}%\",\"value\":\"cs\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"data_file\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}\",\"value\":\"CSV\"},{\"property\":\"short_description\",\"operator\":\"like %{0}\",\"value\":\"CSV\"},{\"property\":\"name\",\"operator\":\"like %{0}\",\"value\":\"CSV\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"data_file\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"name\",\"operator\":\"like %{0}\",\"value\":\"csv\"},{\"property\":\"short_description\",\"operator\":\"like %{0}\",\"value\":\"csv\"},{\"property\":\"name\",\"operator\":\"like %{0}\",\"value\":\"csv\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setFindCategoryBySubjectAreaName(MockServerClient mockServerClient) {
        setSearchAndResponse(mockServerClient, "FindCategoryBySubjectAreaName", "results.json",
                json(
                        "{\"types\":[\"category\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"parent_category\",\"operator\":\"isNull\",\"negated\":true},{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"conditions\":[{\"conditions\":[{\"property\":\"assigned_to_terms.name\",\"operator\":\"=\",\"value\":\"SubjectArea\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"name\",\"operator\":\"like {0}%\",\"value\":\"Org\"}],\"operator\":\"and\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, "FindCategoryBySubjectAreaName", "results.json",
                json(
                        "{\"types\":[\"category\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"parent_category\",\"operator\":\"isNull\",\"negated\":true},{\"property\":\"category_path.name\",\"operator\":\"<>\",\"value\":\"Classifications\"}],\"operator\":\"and\"},{\"conditions\":[{\"conditions\":[{\"property\":\"assigned_to_terms.name\",\"operator\":\"=\",\"value\":\"SubjectArea\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"name\",\"operator\":\"like {0}%\",\"value\":\"org\"}],\"operator\":\"and\"}],\"operator\":\"and\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setFindContactDetailsByProperty(MockServerClient mockServerClient) {
        String caseName = "FindContactDetailsByProperty";
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"user\"],\"where\":{\"conditions\":[{\"property\":\"email_address\",\"operator\":\"like %{0}\",\"value\":\"w@cocopharmaceutical.com\"},{\"property\":\"email_address\",\"operator\":\"isNull\",\"negated\":true}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"user\"],\"where\":{\"conditions\":[{\"property\":\"email_address\",\"operator\":\"like %{0}\",\"value\":\"w@COCOPHARMACEUTICAL.COM\"},{\"property\":\"email_address\",\"operator\":\"isNull\",\"negated\":true}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"user\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"email_address\",\"operator\":\"like %{0}\",\"value\":\"w@cocopharmaceutical.com\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"user\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"email_address\",\"operator\":\"like %{0}\",\"value\":\"w@COCOPHARMACEUTICAL.COM\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setFindSchemaTypeByNamespace(MockServerClient mockServerClient) {
        String caseName = "FindSchemaTypeByNamespace";
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"database_schema\"],\"where\":{\"conditions\":[{\"property\":\"database.name\",\"operator\":\"like {0}%\",\"value\":\"EMPL\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"database_schema\"],\"where\":{\"conditions\":[{\"property\":\"database.name\",\"operator\":\"like {0}%\",\"value\":\"empl\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"database_schema\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"name\",\"operator\":\"like {0}%\",\"value\":\"EMPL\"},{\"property\":\"short_description\",\"operator\":\"like {0}%\",\"value\":\"EMPL\"},{\"property\":\"long_description\",\"operator\":\"like {0}%\",\"value\":\"EMPL\"},{\"property\":\"modified_by\",\"operator\":\"like {0}%\",\"value\":\"EMPL\"},{\"property\":\"database.name\",\"operator\":\"like {0}%\",\"value\":\"EMPL\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
        setSearchAndResponse(mockServerClient, caseName, "results.json",
                json(
                        "{\"types\":[\"database_schema\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"name\",\"operator\":\"like {0}%\",\"value\":\"empl\"},{\"property\":\"short_description\",\"operator\":\"like {0}%\",\"value\":\"empl\"},{\"property\":\"long_description\",\"operator\":\"like {0}%\",\"value\":\"empl\"},{\"property\":\"modified_by\",\"operator\":\"like {0}%\",\"value\":\"empl\"},{\"property\":\"database.name\",\"operator\":\"like {0}%\",\"value\":\"empl\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                ));
    }

    private void setTermAddEvent(MockServerClient mockServerClient) {

        String caseName = "TermAddEvent";

        setStubCreations(mockServerClient);
        setStubLookups(mockServerClient, caseName);

        mockServerClient
                .withSecure(true)
                .when(MockConstants.searchRequest(
                        json(
                                "{\"types\":[\"term\"],\"where\":{\"conditions\":[{\"conditions\":[{\"property\":\"_id\",\"operator\":\"=\",\"value\":\"" + TERM_RID_FOR_EVENT + "\"}],\"operator\":\"and\"},{\"conditions\":[{\"property\":\"parent_category.category_path\",\"operator\":\"=\",\"value\":\"" + GLOSSARY_RID + "\"},{\"property\":\"parent_category\",\"operator\":\"=\",\"value\":\"" + GLOSSARY_RID + "\"}],\"operator\":\"or\"}],\"operator\":\"and\"}}",
                                MatchType.ONLY_MATCHING_FIELDS
                        )))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + caseName + File.separator + "TermAnchor.json")));

    }

    private void setDataFileDeleteEvent(MockServerClient mockServerClient) {

        String caseName = "DataFileDeleteEvent";

        setDeletionDetail(mockServerClient, caseName, "data_file", "b1c497ce.6e76d866.001mts4ph.b7m9lbt.m84k14.b8dvbq9op20klkftn4ro7");
        setDeletionDetail(mockServerClient, caseName, "data_file_record", "b1c497ce.54bd3a08.001mts4ph.b7m3p9e.c1m5u4.qfcpu5sv2fmlp3ej8ffhj");
        setDeletionDetail(mockServerClient, caseName, "data_file_field", "b1c497ce.60641b50.001mts4ph.b75dalt.59n30t.apvv8opio0bm9t60s96lh");
        setDeletionDetail(mockServerClient, caseName, "data_file_field", "b1c497ce.60641b50.001mts4ph.b75a2vb.jpldmv.h2n1pd5bj6ed5naskcuh1");
        setDeletionDetail(mockServerClient, caseName, "data_file_field", "b1c497ce.60641b50.001mts4ph.b7logan.ga9gvm.j9d35qlopvumnpjjlptfl");

        setStubLookupForRid(mockServerClient, caseName, "data_file_folder", "b1c497ce.11727c74.001mts4ph.b86popc.fmed0i.m91ns5n533hivl7v9hop8");
        setStubLookupForRid(mockServerClient, caseName, "term", "6662c0f2.e1b1ec6c.00263phiu.ns4tojl.pbf2qr.91blj7n4rsg7ddf37590i");
        setStubLookupForRid(mockServerClient, caseName, "term", "6662c0f2.e1b1ec6c.00263sh0s.jh26jeo.qf1k24.ip1nbj5vhcv53hif03093");
        setStubLookupForRid(mockServerClient, caseName, "term", "6662c0f2.e1b1ec6c.00263sh0h.2dhc38v.3iblsu.ul800cuokr2uebhpbqurd");

    }

    private void setIMAMShareEvent(MockServerClient mockServerClient) {

        String caseName = "IMAMShareEvent";

        setStubLookupForRid(mockServerClient, caseName, "host_(engine)", "b1c497ce.354f5217.001mtr387.0nbvgbo.uh4485.rd8qffabbjgrsfjh2sheh");
        setStubLookupForRid(mockServerClient, caseName, "database_schema", "b1c497ce.c1fb060b.001mts4qn.7n9ghn6.59e1lg.oeu3169u6dtpesgou6cqh");
        setStubLookupForRid(mockServerClient, caseName, "data_connection", DATA_CONNECTION_RID);
        setStubLookupForRid(mockServerClient, caseName, "connector", "b1c497ce.54ec142d.001mtr38f.q8hjqk4.spumq8.k1bt587cologck6u9tf8q");
        setStubLookupForRid(mockServerClient, caseName, "database_table", "b1c497ce.54bd3a08.001mts4qn.7n9a341.3l2hic.d867phul07pgt3478ctim");

    }

    private void setIAPublishEvent(MockServerClient mockServerClient) {

        String caseName = "IAPublishEvent";



    }

    private void setDeletionDetail(MockServerClient mockServerClient, String caseName, String type, String rid) {
        setStubLookupForRid(mockServerClient, caseName, type, rid);
        setStubDeletion(mockServerClient, type, rid);
    }

    private void setStubCreations(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.createOpenIGCAssetRequest())
                .respond(withResponse(getResourceFileContents("openigc" + File.separator + "stub_term.json")));
        mockServerClient
                .withSecure(true)
                .when(MockConstants.createOpenIGCAssetRequest())
                .respond(withResponse(getResourceFileContents("openigc" + File.separator + "stub_database_column.json")));
    }

    private void setStubDeletion(MockServerClient mockServerClient, String type, String rid) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.deleteOMRSStubRequest(type, rid))
                .respond(withResponse(getResourceFileContents("openigc" + File.separator + "stub_database_column.json")));
        // Note that it doesn't really matter what we respond with as the contents are not used anyway
    }

    private void setStubLookups(MockServerClient mockServerClient, String caseName) {
        JsonBody termRequest = json(
                "{\"types\":[\"$OMRS-Stub\"],\"where\":{\"conditions\":[{\"property\":\"name\",\"operator\":\"=\",\"value\":\"term_" + TERM_RID_FOR_EVENT + "\"}],\"operator\":\"and\"}}",
                MatchType.ONLY_MATCHING_FIELDS
        );
        mockServerClient
                .withSecure(true)
                .when(searchRequest(termRequest), Times.exactly(1))
                .respond(withResponse(getResourceFileContents("no_results.json")));
        mockServerClient
                .withSecure(true)
                .when(searchRequest(termRequest))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + caseName + File.separator + "term_" + TERM_RID_FOR_EVENT + ".json")));
        JsonBody dbColRequest = json(
                "{\"types\":[\"$OMRS-Stub\"],\"where\":{\"conditions\":[{\"property\":\"name\",\"operator\":\"=\",\"value\":\"database_column_" + DATABASE_COLUMN_RID + "\"}],\"operator\":\"and\"}}",
                MatchType.ONLY_MATCHING_FIELDS
        );
        mockServerClient
                .withSecure(true)
                .when(searchRequest(dbColRequest), Times.exactly(1))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + caseName + File.separator + "database_column_" + DATABASE_COLUMN_RID + "_first.json")));
        mockServerClient
                .withSecure(true)
                .when(searchRequest(dbColRequest))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + caseName + File.separator + "database_column_" + DATABASE_COLUMN_RID + "_updated.json")));
        // For the remainder of relationships just get a stub back directly...
        setStubLookupForRid(mockServerClient, caseName, "data_file_field", "b1c497ce.60641b50.001mts4ph.b86ofl8.toj5lt.0vrgjai9knc4h9k0hpr08");
        setStubLookupForRid(mockServerClient, caseName, "category", "6662c0f2.ee6a64fe.00263pfar.1a0mm9a.lfjd3c.rmgl1cdd5fcd4bijur3g3");
        setStubLookupForRid(mockServerClient, caseName, "data_class", "f4951817.e469fa50.001mtr2gq.ng3g6if.07a45h.1pc9rf31o2aaqf0uc4v02");
    }

    private void setStubLookupForRid(MockServerClient mockServerClient, String caseName, String type, String rid) {
        mockServerClient
                .withSecure(true)
                .when(searchRequest(json(
                        "{\"types\":[\"$OMRS-Stub\"],\"where\":{\"conditions\":[{\"property\":\"name\",\"operator\":\"=\",\"value\":\"" + type + "_" + rid + "\"}],\"operator\":\"and\"}}",
                        MatchType.ONLY_MATCHING_FIELDS
                )))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + caseName + File.separator + type + "_" + rid + ".json")));
    }

    private void setChangeSetTest(MockServerClient mockServerClient) {
        String caseName = "ChangeSetTest";
        JsonBody termRequest = json(
                "{\"types\":[\"$OMRS-Stub\"],\"where\":{\"conditions\":[{\"property\":\"name\",\"operator\":\"=\",\"value\":\"term_" + TERM_RID + "\"}],\"operator\":\"and\"}}",
                MatchType.ONLY_MATCHING_FIELDS
        );
        mockServerClient
                .withSecure(true)
                .when(searchRequest(termRequest), Times.exactly(1))
                .respond(withResponse(getResourceFileContents("no_results.json")));
        mockServerClient
                .withSecure(true)
                .when(searchRequest(termRequest))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + caseName + File.separator + "term_" + TERM_RID + ".json")));
    }

    private void setProjectsQuery(MockServerClient mockServerClient) {
        // First time respond with an invalid session
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getProjectsRequest(), Times.exactly(1))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "projects.xml")).withStatusCode(403));
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getProjectsRequest())
                .respond(
                        withResponse(getResourceFileContents("ia" + File.separator + "projects.xml"))
                                .withHeaders(
                                        Header.header("Set-Cookie", "LtpaToken2=CEElCeZLmydlWRiPOMGlL+arDhgYVqXNMaoH5Cif5wTy+MQAAJR/jcV5amcyQGF1LpKbukQbXwMO+oeGTzUjRGCN0JZyomB8zn7C8R31LMJpyhIpILbJym7WoiN/pOp1mBIY7ZPbkBahKKIa8JSSYvbeJUlPvS9aNKYn4+qgYzBmQf1lIbewy/UBH0cK1kYkjHKW4dsKOzZWEdixD/LE7VDHki/rEdYyprZAgqDjNb9NDRWCxy25hmlaMQ7/PBkXu/2QJuCUqaoLautLARe9gVHfTP3Zg0/KzzaEkdV0z5GQ7OO7NapLM00ahBKnszD8G7YfuSJQKeUSWrhljlrLgxzpFAyiIGtPfXGrsYKI7nKDn8OQ/+3/2OBCb9PE6uUFHCeNceKqNYj7wBa2Q2P+6w==; Path=/; Secure; HttpOnly"),
                                        Header.header("Set-Cookie", "JSESSIONID=0000_tevtrBb61tdc2_JqGF4bPn:a746286c-69ed-4c45-bab5-21ed1a232fe2; Path=/; Secure; HttpOnly"),
                                        Header.header("Set-Cookie", "X-IBM-IISSessionId={issvr2048:isf}MfvbYdnWcOxwLPR7aD8OrSlEaSv_7dZCWXX1L-X1OuxK3-pA9aRN-xQJxZKJcL0WX4l-eqj8NQw8qxJ3K_sZ8bpPZz7mJjmaUJ_og24MotbeiyNV1GqiTM6c5M8DU3-GvZroFgJCZ_f_2h9R8FyBhCxi5ppV4oA96--v5BBVMC75Nsd-Rwe0nbGsRmJTTFEnTi26Gz5GEWmDvcXwWWrMJkZSd2TXlKM6HHkaXu5QkA_I5U5LpFfzqcOVPgHOPuh_h5a80LJ8ojBSTOrGchSIpT3LamusmsiPd6xHb_CoqDiCvxmRUdGJCGPIhqU0MNv5q62GiqqWXUgVd9SMF1LE0w..; Path=/; Secure; HttpOnly"),
                                        Header.header("Set-Cookie", "X-IBM-IISSessionToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJpc2FkbWluIiwiYXVkIjoiSUlTIiwidWlkIjoiaXNhZG1pbiIsInJvbGVzIjpbIlNvcmNlcmVyVXNlciIsIlJ1bGVzQWRtaW5pc3RyYXRvciIsIkRhdGFTdGFnZUFkbWluIiwiUnVsZXNBdXRob3IiLCJSdWxlc01hbmFnZXIiLCJNRFdBZG1pbmlzdHJhdG9yIiwiQ01BZG1pbiIsIlJ1bGVzVXNlciIsIlNvcmNlcmVyQWRtaW4iLCJHbG9zc2FyeUFkbWluIiwiU3VpdGVVc2VyIiwiU3VpdGVBZG1pbiIsIlNvcmNlcmVyRGF0YUFkbWluIiwiRGF0YVN0YWdlVXNlciJdLCJkaXNwbGF5X25hbWUiOiJBZG1pbmlzdHJhdG9yLCBJSVMiLCJleHAiOjE1ODEzOTYxNzMsImlhdCI6MTU4MTM1MDU3MywidXNlcm5hbWUiOiJpc2FkbWluIn0.hoPaX-qz9AglzsQIyBg9YLEZOGzUb9xjT_SBTD4iMIFexZ2Zon2xlwA6rcWYl0SIw3wO3PBNZmWzpJcoDYCcuADVXy1q3hJT_WRGo5ELrvT_mWcPfNeQk_AXVsZlS-1dR4OXGOZ2VPYkl9ybBxdvE8Qgo17MgYxu8KV7qqnjFCeTINjzyoV9r0Q3uG2pPBk1moEtiUvrBBtma2D2TJttBP1zhCiBbqa2xb1meEI44rG8aeDC9K7xupaWeyxqEkuu0ywZO8O7DUC6LJEn2oMb9Bp5Pw7g7X6qY4qLW3Loq-zmGr0EaQ6aq1v-SgeIZ-AhC36iivQWsdQ9rvBuvccANg; Path=/; Secure; HttpOnly")
                                )
                );
    }

    private void setProjectDetailQuery(MockServerClient mockServerClient, String projectName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getProjectDetailsRequest(projectName))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "project_" + projectName + ".xml")));
    }

    private void setPublishedResultsQuery(MockServerClient mockServerClient, String projectName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getPublishedResultsRequest(projectName))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "published_" + projectName + ".xml")));
    }

    private void setColumnAnalysisResultsQuery(MockServerClient mockServerClient, String projectName, String tableName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getColumnAnalysisResultsRequest(projectName, tableName))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "ca_" + tableName + ".xml")));
    }

    private void setDataQualityResultsQuery(MockServerClient mockServerClient, String projectName, String tableName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getDataQualityResultsRequest(projectName, tableName))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "dq_" + tableName + ".xml")));
    }

    private void setRunColumnAnalysis(MockServerClient mockServerClient, String projectName, String tableName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getExecuteTaskRequest(
                        "<?xml version='1.0' encoding='UTF-8'?><iaapi:Project xmlns:iaapi=\"http://www.ibm.com/investigate/api/iaapi\" name=\"" + projectName + "\"><Tasks><RunColumnAnalysis><Column name=\"" + tableName + ".*\"/></RunColumnAnalysis></Tasks></iaapi:Project>"
                ))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "run_ca_" + tableName + ".xml")));
    }

    private void setRunDataQualityAnalysis(MockServerClient mockServerClient, String projectName, String tableName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getExecuteTaskRequest(
                        "<?xml version='1.0' encoding='UTF-8'?><iaapi:Project xmlns:iaapi=\"http://www.ibm.com/investigate/api/iaapi\" name=\"" + projectName + "\"><Tasks><RunDataQualityAnalysis><Table name=\"" + tableName + "\"/></RunDataQualityAnalysis></Tasks></iaapi:Project>"
                ))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "run_dq_" + tableName + ".xml")));
    }

    private void setRunningColumnAnalysis(MockServerClient mockServerClient, String scheduleId, String tableName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getTaskStatusRequest(scheduleId), Times.exactly(1))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "running_ca_" + tableName + ".xml")));
    }

    private void setRunningDataQualityAnalysis(MockServerClient mockServerClient, String scheduleId, String tableName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getTaskStatusRequest(scheduleId), Times.exactly(1))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "running_dq_" + tableName + ".xml")));
    }

    private void setCompleteColumnAnalysis(MockServerClient mockServerClient, String scheduleId, String tableName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getTaskStatusRequest(scheduleId))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "complete_ca_" + tableName + ".xml")));
    }

    private void setCompleteDataQualityAnalysis(MockServerClient mockServerClient, String scheduleId, String tableName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getTaskStatusRequest(scheduleId))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "complete_dq_" + tableName + ".xml")));
    }

    private void setPublishResults(MockServerClient mockServerClient, String projectName, String tableName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getPublishResultsRequest(
                        "<?xml version='1.0' encoding='UTF-8'?><iaapi:Project xmlns:iaapi=\"http://www.ibm.com/investigate/api/iaapi\" name=\"" + projectName + "\"><Tasks><PublishResults><Table name=\"" + tableName + "\"/></PublishResults></Tasks></iaapi:Project>"
                ))
                .respond(response().withStatusCode(200));
    }

    private void setFormatDistribution(MockServerClient mockServerClient, String projectName, String columnName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getFormatDistributionRequest(projectName, columnName))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "format_" + columnName + ".xml")));
    }

    private void setFrequencyDistribution(MockServerClient mockServerClient, String projectName, String columnName) {
        mockServerClient
                .withSecure(true)
                .when(MockConstants.getFrequencyDistributionRequest(projectName, columnName))
                .respond(withResponse(getResourceFileContents("ia" + File.separator + "frequency_" + columnName + ".xml")));
    }

    private void setIALogout(MockServerClient mockServerClient) {
        mockServerClient
                .withSecure(true)
                .when(iaLogoutRequest())
                .respond(response().withStatusCode(200));
    }

    private void setSearchAndResponse(MockServerClient mockServerClient, String caseName, String filename, JsonBody jsonBody) {
        mockServerClient
                .withSecure(true)
                .when(searchRequest(jsonBody))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + caseName + File.separator + filename)));
    }

    private void setSearchAndResponse(MockServerClient mockServerClient, String caseName, String filename, String body) {
        mockServerClient
                .withSecure(true)
                .when(searchRequest(body))
                .respond(withResponse(getResourceFileContents("by_case" + File.separator + caseName + File.separator + filename)));
    }

    /**
     * Retrieve the contents of a test resource file.
     * @return String
     */
    private String getResourceFileContents(String filename) {

        ClassPathResource resource = new ClassPathResource(filename);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Unable to read resource file: {}", filename, e);
        }
        if (reader != null) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return null;

    }

    /**
     * Retrieve the set of resources that match the specified pattern.
     * @param pattern to match for retrieving resources
     * @return {@code Resource[]}
     */
    private Resource[] getFilesMatchingPattern(String pattern) {

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            return resolver.getResources(pattern);
        } catch(IOException e) {
            log.error("Unable to find any matches to pattern: {}", pattern, e);
        }
        return null;

    }

}
