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

package archive;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.util.PrefixMapping2;
import org.apache.jena.sparql.util.QueryExecUtils;

public class Jena1877_extend_bnode {
    static { LogCtl.setLog4j2(); }

    public static void main(String...a) {
        String DIR ="/home/afs/tmp/jena1877/";
//        jena.sparql.main("--data="+DIR+"data.ttl",
//                         "--optimize=off",
//                         "-v",
//                         "--query="+DIR+"bnode01.rq");
//        jena.qparse.main("--print=op", "--print=opt", "--query="+DIR+"bnode01.rq");

        Query q = QueryFactory.create("SELECT ?y1 ?y2 ( BNODE(?x1) AS ?A) ( BNODE(?x2) AS ?B) {"+
                                      "VALUES (?x1 ?x2) {('1' '2') ('1' '2') }"+
                                      "}");
        Op op = Algebra.compile(q);

        Op op1 = Algebra.optimize(op);
        System.out.println(op1);


        QueryExecUtils.execute(op, DatasetGraphFactory.createTxnMem());

        ARQ.getContext().set(ARQ.optimization, false);
        QueryExecUtils.execute(op, DatasetGraphFactory.createTxnMem());
//
//
//        return apply("Combine BIND/LET", new TransformExtendCombine(), op) ;

    }


    // ApplyElementTransformVisitor does the walk subquery
    //  Some how, QueryTransformOps.transform without prefixes.

    // Query(Prologue) puts in a PrefixMapping2 is Prologue.usePrologueFrom.
    //    Instead, just use it! Copy it?
    // Because SPARQLParserBase
//    protected Query newSubQuery(Prologue progloue)
//    {
//        return new Query(getPrologue());
//    }

    // [1] Query(Prologue) to copy. UNNECESSARY BUT BETTER.
    /*
    public Query(Prologue prologue)
    {
        this() ;
        Prologue p2 = prologue.copy();
        prefixMap = p2.getPrefixMapping();
        seenBaseURI = false ;
        resolver = p2.getResolver();
    }
     */
    // [2] parser to clear subquery prologue.
    // QueryShallowCopy to not copy the prologue.

    public static void mainQueryClone(String...a) {
        String [] files = {
//            "ARQ/SubQuery/sub-select-02.rq",
//            "ARQ/Serialization/syntax-subselect-02.rq",
//            "ARQ/Serialization/syntax-subselect-01.rq",
//            "ARQ/Syntax/Syntax-SPARQL_11/syntax-subquery-01.rq"
        };

        String [] strings = {

//            "PREFIX : <http://example/> SELECT * { ?s ?p ?o }",
//            "PREFIX : <http://example/> SELECT * { SELECT ?s { ?s ?p ?o } }",
//          "PREFIX : <http://example/> SELECT * { { SELECT ?s { ?s ?p ?o } } }",

          //"PREFIX : <http://example/> SELECT * { { SELECT ?s { ?s ?p ?o } } ?x ?y ?z }",

//            "PREFIX : <http://example/> SELECT * { VALUES ?x { 1 2 } }",
//          "PREFIX : <http://example/> SELECT * { VALUES ?x { 1 2 } ?s ?p ?o }",
//            "PREFIX : <http://example/> SELECT * { } VALUES ?x { 1 2 }",


            //"PREFIX : <http://example/> SELECT * {  ?x ?y ?z { SELECT ?s { ?s ?p ?o } } }"
            "PREFIX : <http://example/> SELECT * {  ?x :p ?z { SELECT ?s { ?s :p ?o } } }"
        };

        PrefixMapping2 x;

        // Query.valueDataBlock is not copied by QueryTransformOps
        String DIR = "/home/afs/ASF/afs-jena/jena-arq/testing/";
//        for ( String fn : files ) {
//            fn = DIR+fn;
//            Query query = QueryFactory.read(fn);
//            System.out.println(query);
//            Query query2 = query.cloneQuery();
//            System.out.println(query2);
//        }

        for ( String qs : strings ) {
            Query query = QueryFactory.create(qs);
            System.out.println(query);
            System.err.println();
            Query query2 = /*query*/cloneQuery(query);
            System.out.println(query2);
        }

        System.exit(0);
    }



    public static Query cloneQuery(Query query) { return query; }

//    public static Query cloneQuery(Query query) {
//        ElementTransform eltTransform = new ElementTransformCopyBase(true);
//        ExprTransform exprTransform = new ExprTransformApplyElementTransform(eltTransform, true);
//
//        Query result = QueryTransformOps.transform(query, eltTransform, exprTransform);
//
//        if ( query.getValuesVariables() != null )
//            result.setValuesDataBlock(new ArrayList<>(query.getValuesVariables()), new ArrayList<>(query.getValuesData()));
//        return result;
//    }
}
