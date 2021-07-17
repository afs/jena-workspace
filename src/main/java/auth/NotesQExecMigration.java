/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package auth;


public class NotesQExecMigration {

    // ** Who encodes?

    // [ ] test - mixed - JDK basic, Jena digest
    // auth tests:
    //   fuseki-webapp:          TestAuth
    //   fuseki-main:            FusekiTestAuth
    //   jena-integration-tests: TestAuthRemote

    // [ ] Find tests of QueryEngineHTTP and put Test* of that name.

    // [ ] Find all JDK HttpClient builds and set the connect timeout.
    // [ ] Look for [QExec]
    // [ ] QueryExecHTTP.executeQueryGet etc. Params needed?


    // [ ] HTTPS testing
    // [ ] get ExFuseki_Https_3_Auth working.
    //     Trust self-signed.
    //     Other examples - has too much been deleted?

    // [ ] test digest using wikipedia example.

    // **** HTTPS
    // ** Check Jetty tests
    // Need crt file.
    // ** Rethink needed.
    // Duff certificate - host name != localhost

    // [ ] Ensure LibSec_AHC isn't used. Delete.
    // [ ] LibSec.withAuth

    // Merge
    // [ ] RDFConnectionFuseki - clone from existing.
    // [ ] G2
    // [ ] Local Adapters
    // [ ] SERVICE2
    // [ ] Fuseki.isFuseki -- then remove rdfconnection as a Fuseki dependency?
    //     Move to client side.
    // [ ] Remove AuthSetup
    // [ ] Delete *_AHC

    // [ ] Examples
    // [ ] Tests

    // [ ] Old params, new params

    // [ ] XFer TestExecutionBuilders.java, TestUpdateBuilder.java
    // [ ] Unsigned : https://stackoverflow.com/questions/52988677/allow-insecure-https-connection-for-java-jdk-11-httpclient

    // ----
    // [ ] org/seaborne/improvements
    // [ ] Adapters
    // [ ] RowSet: Reader/Writer
    // [ ] HttpOp1
    // [ ] QueryExecutionfactory, UdpateExecutionFactory.
    // [ ] Documentation
    //     SERVICE
    //     RDF Connection
    //     New page

    // [ ] javadoc - API packages

    // Tickets:
    // JENA-2124: Encoding:gzip
    // JENA-2125: Epic
    // JENA-2126: org/apache/jena/http/
    // JENA-2127: RowSet
    // JENA-2128: QueryExec, UpdateExec
    // JENA-2129: QueryExecHTTP, UpdateExecHTTP
    // JENA-2130: GSP
    // JENA-2131: RDFLink

    void examples() {
        // ** Fuseki examples. Add digest examples. add Auth registration examples.
        // ** examples
        // [ ] See RDFConnection and other examples.
        // [ ] Survey examples.
        // [ ] Modification - RegistryByServiceURL
        // [ ] See ExGSP
    }

    void merge() {

        // QueryExecutionFactory
        // [ ] QueryExec
        // [ ] Stop using QueryEngineHTTP -- QueryExecutionHTTP

        // UpdateExecutonFactory

        // RowSet reader/writer

        // [ ] Port Shacl03_FusekiValidationService example.
    }

	// [ ] test - mixed - JDK basic, Jena digest
    // auth tests:
    //   fuseki-webapp:          TestAuth
    //   fuseki-main:            FusekiTestAuth
    //   jena-integration-tests: TestAuthRemote

    // [ ] Find tests of QueryEngineHTTP and put Test* of that name.

	// [ ] Find all JDK HttpClient builds and set the connect timeout.
    // [ ] Look for [QExec]
    // [ ] QueryExecHTTP.executeQueryGet etc. Params needed?


	// [ ] HTTPS testing
	// [ ] get ExFuseki_Https_3_Auth working.
	//     Trust self-signed.
	//     Other examples - has too much been deleted?

    // [ ] test digest using wikipedia example.

    // **** HTTPS
	// ** Check Jetty tests
    // Need crt file.
    // ** Rethink needed.
    // Duff certificate - host name != localhost

    // [ ] Ensure LibSec_AHC isn't used. Delete.
    // [ ] LibSec.withAuth

    // Merge
    // [ ] RDFConnectionFuseki - clone from existing.
    // [ ] G2
    // [ ] Local Adapters
    // [ ] SERVICE2
    // [ ] Fuseki.isFuseki -- then remove rdfconnection as a Fuseki dependency?
    //     Move to client side.
    // [ ] Remove AuthSetup
    // [ ] Delete *_AHC

    // [ ] Examples
    // [ ] Tests

    // [ ] Old params, new params

    // [ ] XFer TestExecutionBuilders.java, TestUpdateBuilder.java
    // [ ] Unsigned : https://stackoverflow.com/questions/52988677/allow-insecure-https-connection-for-java-jdk-11-httpclient

    // ----
    // [ ] org/seaborne/improvements
    // [ ] Adapters
    // [ ] RowSet: Reader/Writer
    // [ ] HttpOp1
    // [ ] QueryExecutionfactory, UdpateExecutionFactory.
    // [ ] Documentation
    //     SERVICE
    //     RDF Connection
    //     New page

    // [ ] javadoc - API packages

    // Tickets:
    // JENA-2124: Encoding:gzip
    // JENA-2125: Epic
    // JENA-2126: org/apache/jena/http/
    // JENA-2127: RowSet
    // JENA-2128: QueryExec, UpdateExec
    // JENA-2129: QueryExecHTTP, UpdateExecHTTP
    // JENA-2130: GSP
    // JENA-2131: RDFLink

    void authentication() {
        // [ ] MD5 -> add other SHA "MD5-sess"
        // [ ] Revisions for https://datatracker.ietf.org/doc/html/rfc7616
        // [ ] Better parsing the WWW-Authentication header.
    }

    void migration() {
        // Delete DatasetAccessor
        // G2 merge to G etc
        /*
        Packages:
        1:: jena-arq

        org.apache.jena.sparql.exec
            QueryExec, QueryExecBuilder, QueryExecAdpater, QueryExecDataset
            RowSet (ResultSetAdapter?)

        org.apache.jena.riot.web == org.apache.jena.http
              - HttpEnv, HttpLib, HttpOp2 (rename, moved), old HttpOp->HttpOp1 - call through to HttpOp2.

        org.apache.jena.sparql.exec.http (or org.apache.jena.riot.http)
            GSP?
            HTTP variants.

        Alt:
          org.apache.jena.queryexec (no)
          org.apache.jena.engine.http

        4:: jena-rdfconnection:
            org.apache.jena.rdflink
            Alt: org.apache.jena.rdfconnection.link
         */
        // [ ] Remove DatasetAccessor!
    }

    void misc() {

        // [ ] *Remote vs *HTTP
        // [ ] Where do builders come from? Factory?
        //     QueryExec.newLocalBuilder newRemoteBuilder?
        //     SPARQL.newQueryExec()?
        //     Local.newQueryBuilder, HTTP.newBuilder.

        // [ ] TestUsage.
        // [-] EnvTest to org.apache.jena.test (integration testing)
        // [ ] QueryExecUtils.executeQuery to work on GPI. Deprecate rest. Redo.
        // [-] Move builders into classes?? Or move all out
        // [ ] RequestLogging : See LogIt


        // 1 - make new world client work with compression (send only)
        // 5 - Consider Content-Length calculation form.

        // Sometime...
        // [ ] Remove org.apache.http from log4j2.properties files.

    }

    void documentation() {
        // [ ] Document!
        //     Global -> dataset -> [fuseki service?] -> execution -> freeze
        //     StreamRDF
        // HttpClient+HttpContext -> HttpClient
    }

    void compression() {

        // Fuseki : Off for responses. On for receiving.
        //   Could make it version dependent. HTTP/2 -> on for responses.
        // Client:
        //   Support "Content-Encoding: gzip" receipt
        //   Does not send "Accept-Encoding".
        // Not fully working in Fuseki due to Jetty custom code needed for HTTP 1.1, streaming and gzip.

        // Problem:
        //   (HTTP/1.1) streaming and gzip => "Content-Encoding: chunked, gzip"
        //   Needs special handling in Jetty (GzipHttpOutputInterceptor depends on Content-Length)
        //   Naively done, then Jetty drops the "Content-Encoding: gzip" and only sends "Content-Encoding: chunked"
        // Not handled by java.net.http.HttpClient (only chunked?)
        // HTTP/2 does not have chunked at all, and "Content-Encoding: gzip" will work on receipt.
        // And is such one time compression worth it?
        // Jetty:
        //   GzipHandler
        //   GzipHttpInputInterceptor - then no special handling (server)
        //   GzipHttpOutputInterceptor
        // What does java.net.http handle?
    }

    void GSP() {
    }

    void HttpOp2() {
    }

    void HTTP() {
        // [ ] Digest auth
        // [ ] Run some integration tests in HTTP 1.1 mode
        // [ ] HTTP/2 RFC 7540: Not Keep-Alive, Proxy-Connection, Transfer-Encoding, and Upgrade,
        //     Also - Content-Encoding chunked (pointless)
        //     "However, header field names MUST be converted to lowercase prior to their encoding in HTTP/2."
        // [ ] TestUsageHTTP : compression and no compression
        // [ ] HTTP version (Fuseki issue?)
    }

    void SERVICE() {
        // [ ] Undo BraveNewWorld.
        // [-] Do not optimize SERVICE - substitution only.
        // [ ] user:passwrd: form.
    }

    void RDFConnection() {
        // [x] Adapter : link/RDFConnectionAdapter
        // [ ] RDFConnectionFactory - de-emphasise - refer to builders
        // [?] RDFConnectionDatasetBuilder
    }

    void RDFLink() {
    }

    void RowSet() {
        // [ ] Minor ResultSetCompare - check use of resultSet forms.
        // [-] ResultSet.nextBinding - deprecate/remove from codebase. Buit thisis how RowsetOverResultSet works.

        // [ ] reader, writer (cheat with adapters for now?)
        // [x] SSE
        // [x] RowSetformatter
        // [ ] SPARQLResult
        // [ ] ResultSet as adapter : resultSetSream goes away
        //     Deprecate -- ResultSetReader.read -> ResultSet
        //     Add -- ResultSetReader.readRowSet
        //     Add -- ResultSetWriter.write(,RowSet,)
        //     Deprecate -- ResultSetWriter.write(,ResultSet,)
        // [ ] RowSetFormatter => RowSetOps
        // [ ] ResultSetReader -> RowSetReader, RowSetWriter
        // [ ] ResultsWriter.builder.prefixMap(PrefixMap) - only needed for text
    }

    void query() {
        // [ ] QueryExecutionFactory -> deprecate and refer to builder.
        // [ ] Leave/deprecate QueryEngineHttp using ApcheHttpClient for one or two releases?
        // [ ] Deprecation of QueryExecution.setTimeout (use a builder). QueryExecutionBuilder
        // [-] Old use of QueryEngineHTTP, HttpQuery (leave, inc Apache HttpClient4 - deprecate - delete)
        // [ ] Deprecate all QueryExecutionFactory.sparqlService, createServiceRequest - reference builders.
        // [ ] Deprecate of QueryExecution.setTimeout (leave for now) setIntialBindings
        //     There is a QueryExecutionBuilder for local datasets
        // [ ] javadoc of QueryExecutionFactory to refer to builders.
        // [ ] javadoc of UpdateExecutionFactory to refer to builders.
        // [ ] QueryExecutionBase -> QueryExecutionDataset (?? too late ??)
    }

    void HttpOp() {
    }

    void update() {
        // Is there an abstraction for a "ready to run" UpdateExecBuilder - only has ".execute(dsg)"
        // [ ] UpdateProc or UpdateExec
        // [-] Later

        // [ ] UpdateProcessorAdapter

        // [ ] UpdateExecutionFactory
        // [ ] UpdateProcessorBase becomes "UpdateExecDataset (keep UpdateProcessor interface)
        // [-] UpdateProcessor for resource level.
        // [x] UpdateExec for graph level inc builder.
        // [ ] It's called UpdateExecHTTP
        // [ ] UpdateExecutionHTTP - same except Dataset
        // [ ] UpdateProcessor(migrate name) -> UpdateExecution, UpdateExecutionHTTP, UpdateExecutionHTTPBuilder
        // [?] UpdateProcessorBase -> UpdateExecutionDataset
        // [?] UpdateProcessorRemoteBase -> UpdateExecutionDataset
        // [ ] UpdateProcessRemote, leave but deprecate - put UpdateExecutionHTTP along side + builder.
    }

    void tests() {
        // [ ] DevAuthHTTP as tests
        // [ ] UpdateExecBuilder
        // [x] UpdateExecHTTPBuilder
    }

    void jetty() {
        // == Jetty
        // See NotesJetty
        // [ ] Configure Jetty for HTTP/2.
        // [ ] Configure Jetty for PROXY
        // https://www.eclipse.org/jetty/documentation/jetty-10/programming-guide/index.html#pg-server-http-connector
    }
}
