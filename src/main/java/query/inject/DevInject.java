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

package query.inject;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionEnvBase;
import org.apache.jena.sparql.sse.SSE;

public class DevInject {

    public static void main(String[] args) {
        String qs = """
PREFIX :        <http://example/>
PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>
PREFIX sh:      <http://www.w3.org/ns/shacl#>
PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>

SELECT * { ?s :p 123 }
                """;

        String data = """
                PREFIX :        <http://example/>
                :s :p 123 .
                :s :q 456 .
                """;

        Query q = QueryFactory.create(qs);
        Op op  = Algebra.compile(q);
        Op op1 = Algebra.optimize(op);
        //System.out.print(op1);

//        //op = SSE.parseOp("(extend ((?s <subj>) (?x <other>)) (triple ?s :p :o))");
//        Op op1 = Algebra.optimize(op);
//        System.out.println(op1);
//        System.out.println("DONE");
//        System.exit(0);

        DatasetGraph dsg = RDFParser.fromString(data).lang(Lang.TRIG).toDatasetGraph();
        Binding row = SSE.parseBinding("(row (?s :s))");

        FunctionEnv env = new FunctionEnvBase(ARQ.getContext(), dsg.getDefaultGraph(), dsg);

        boolean bool = evalExists(row, op, env);
        System.out.println(bool);
        System.out.println("DONE");
    }

    private static boolean evalExists(Binding row, Op pattern, FunctionEnv env) {
        System.out.println(pattern);
        Set<Var> x = new HashSet<>();
        row.vars().forEachRemaining(x::add);
        // Can we do some of this once?
        Transform transform = new TransformInject(x, v->row.get(v));
        Op opEval = Transformer.transform(transform, pattern);
        System.out.println(opEval);

        ExecutionContext execCxt = new ExecutionContext(env.getContext(),
                                                        env.getActiveGraph(),
                                                        env.getDataset(),
                                                        QC.getFactory(env.getContext())
                                                       ) ;
        QueryIterator qIter1 = QueryIterRoot.create(execCxt);
        QueryIterator qIter = QC.execute(opEval, qIter1, execCxt);
        boolean result = qIter.hasNext();
        qIter.close();
        return result;
    }
}
