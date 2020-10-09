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

package test4.next.sparql.tests;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.sparql.vocabulary.TestManifestX;
import org.apache.jena.sparql.vocabulary.VocabTestQuery;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.junit.TestUtils;
import test4.next.manifest.ManifestEntry;

public class T {

    // Data Parser - no warnings.
    static RDFParserBuilder parser(String sourceURI) {
        return RDFParser.create().source(sourceURI).errorHandler(ErrorHandlerFactory.errorHandlerNoWarnings);
    }

    static void setupFailure(String msg) {
        throw new RuntimeException(msg);
    }

    static void testFailure(String msg) {
        throw new RuntimeException(msg);
    }


    // Take code from testItem. Use G?
    // get query
    // get data
    // get result

//    private Node _getAction(ManifestEntry entry) {
//        return entry.getAction();
//
////        if ( actionResource == null )
////            actionResource = testResource.getProperty(TestManifest.action).getResource() ;
////        return actionResource ;
//        return null;
//    }


    static private String _getQueryFile(ManifestEntry entry) {
        Resource r = entry.getAction();

        if ( r.hasProperty(VocabTestQuery.query) )
            return TestUtils.getLiteralOrURI(r, VocabTestQuery.query) ;

        // No query property - must be this action node

        if ( r.isAnon() )
            return "[]" ;
        return r.getURI() ;
    }

    static Query queryFromString(String qStr) {
        Query query = QueryFactory.create(qStr);
        return query;
    }

    static Query queryFromEntry(ManifestEntry entry) {
        if ( _getQueryFile(entry) == null ) {
            T.setupFailure("Query test file is null");
            return null;
        }

        Query query = QueryFactory.read(_getQueryFile(entry), null, _getQuerySyntax(entry, null));
        return query;
    }

    private static Syntax _getQuerySyntax(ManifestEntry entry, Syntax def) {
        Resource r = entry.getAction();
        if ( r.hasProperty(TestManifestX.querySyntax) ) {
            Syntax x = Syntax.make(r.getProperty(TestManifestX.querySyntax).getResource().getURI()) ;
            return x ;
        }
        Resource q = r.getPropertyResourceValue(VocabTestQuery.query);
        if ( q == null )
            q = entry.getAction();

        if ( q == null ) {
            System.err.println("No query");
            // Default on manifest?
            return Syntax.syntaxSPARQL_11;
        }

        String uri = q.getURI();
        if ( uri != null ) {
            Syntax synFileName = guessFileSyntax(uri) ;
            if ( synFileName != null )
                return synFileName ;
        }
        return def ;
    }

    // *.rq is strictly SPARQL 1.1 tests.
    protected static Syntax guessFileSyntax(String filename) {
        if ( filename.endsWith(".rq") )
            return Syntax.syntaxSPARQL_11;
        if ( filename.endsWith(".ru") )
            return Syntax.syntaxSPARQL_11;

        return Syntax.guessFileSyntax(filename);
    }

    static UpdateRequest updateFromString(String str) {
        return UpdateFactory.create(str);
    }

    static UpdateRequest updateFromEntry(ManifestEntry entry) {
        if ( _getQueryFile(entry) == null ) {
            T.setupFailure("Query test file is null");
            return null;
        }
        String fn = _getQueryFile(entry);
        Syntax syntax = guessFileSyntax(fn);

        UpdateRequest request = UpdateFactory.read(fn, syntax);
        return request;
    }

}
