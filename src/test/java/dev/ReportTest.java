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

package dev;

import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.AlgebraGenerator;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

public class ReportTest {

    static {
        JenaSystem.init();
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...a) {
        String qs = "SELECT ?a ?d WHERE { ?a <http://example.org/p> ?b . BIND(?b AS ?c) BIND(?c AS ?d) }";
        Query q1 = QueryFactory.create(qs);
        Op op1 = Algebra.compile(q1);
        Query q2 = OpAsQuery.asQuery(op1);
        Op op2 = Algebra.compile(q2);

        System.out.println(q1);
        System.out.println(q2);
        System.out.println("Algebra equals: "+op1.equals(op2));
    }

    @Test
    public void retainVariables() {
        String sparqlStrBeforOp = "SELECT ?a ?d WHERE { ?a <http://example.org/p> ?b . BIND(?b AS ?c) BIND(?c AS ?d) }";
        String sparqlStrAfterOp = OpAsQuery.asQuery(new AlgebraGenerator().compile(QueryFactory.create(sparqlStrBeforOp))).toString();
        System.out.println(sparqlStrAfterOp);
        assertTrue(sparqlStrAfterOp.contains("(?b AS ?c)"));
    }
}

