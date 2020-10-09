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

package query;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.NodeValue;
import query.parameterized.ParameterizedQuery;

public class DevQueryUpdate {

    public static void main(String[] args) {
        // SUMMARY
        /*
         * [1] ParameterizedQuery.nodeEnc
         * Then works with bnodeUriPlain
         *
         */
        if ( true ) {
            Node bnode = NodeFactory.createBlankNode() ;
            // Different bnode.
            String bnodeUriEnc = "<_:"+NodeFmtLib.encodeBNodeLabel(bnode.getBlankNodeLabel())+">" ;
            String bnodeUriPlain = "<_:"+bnode.getBlankNodeLabel()+">" ;


            Map<Var, Node> map3 = new LinkedHashMap<Var, Node>() ;
            map3.put(Var.alloc("b"), bnode) ;
            //test("SELECT * { }", "SELECT ("+bnUri+" AS ?b) { }", map3, true) ;

            String input = "SELECT * { }";
            String expected = "SELECT ("+bnodeUriPlain+" AS ?b) { }";
            Query qInput = QueryFactory.create(input, Syntax.syntaxARQ) ;
            Query qExpected = QueryFactory.create(expected, Syntax.syntaxARQ) ;
            Query output = ParameterizedQuery.parameterizeIncludeMapped(qInput, map3);
            System.out.println("** Expected");
            System.out.println("String::\n"+expected);
            System.out.print("Query::\n"+qExpected);
            VarExprList ve1 = qExpected.getProject();
            NodeValue n1 = (NodeValue)ve1.getExprs().get(Var.alloc("b"));
            System.out.println("Blank node: " +n1.asNode().getBlankNodeLabel());

            System.out.println();

            System.out.println("** Output");
            System.out.println(output);
            VarExprList ve2 = output.getProject();
            // Because of ParameterizedQuery.nodeEnc
            NodeValue n2 = (NodeValue)ve2.getExprs().get(Var.alloc("b"));
            if ( n2.asNode().isBlank() )
                //ParameterizedQuery.nodeEnc - no encoding.
                System.out.println("Blank node: " +n2.asNode().getBlankNodeLabel());
            else
                System.out.println("URI: <"+n2.asNode().getURI()+">");

            // IF bnodeUriPlain and ParameterizedQuery.nodeEnc - no encoding, then equals.
            System.out.println(qExpected.equals(output));

//            VarExprList ve1 = qExpected.getProject();
//            NodeValue n1 = (NodeValue)ve1.getExprs().get(Var.alloc("b"));
//            VarExprList ve2 = output.getProject();
//            NodeValue n2 = (NodeValue)ve2.getExprs().get(Var.alloc("b"));

            System.exit(0);
        }
    }
}

