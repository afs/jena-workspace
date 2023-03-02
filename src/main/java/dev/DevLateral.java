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

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.main.QueryEngineMain;
import org.apache.jena.sparql.engine.ref.QueryEngineRef;
import org.apache.jena.sparql.sse.writers.WriterOp;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.sys.JenaSystem;

public class DevLateral {

    static {
      LogCtl.setLog4j2();
      //FusekiLogging.setLogging();
      JenaSystem.init();
      RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
  }

  // UNION interferes (separate issue)
  // *** Should treat LATERAL like subquery

  public static void main(String...args) {
      String qs0 = """
              PREFIX : <http://example/>
SELECT * {
  ?s a :T .
  ## { LATERAL { SELECT * { ?s :p ?p } ORDER BY ?p LIMIT 2 } }

  ## Correctly does not optimize.
##  { SELECT * { ?s :p ?p } ORDER BY ?p LIMIT 2 }
}
              """;

      String qs1 = """
              PREFIX : <http://example/>
SELECT * {
  ?s a :T .
  # bad
  { SELECT * { ?s :p ?p } ORDER BY ?p LIMIT 2 }
  UNION
  { SELECT * { ?s :q ?q } ORDER BY ?p LIMIT 2 }
}
              """;



      DevLib.joinClassification(qs1);
      System.exit(0);

      System.out.println("== Bad");
      dwimOp(qs0);
      Graph g = RDFParser.fromString(data).lang(Lang.TTL).toGraph();
      dwimExec(g, qs0);

  }

  static String data = """
          PREFIX :        <http://example/>

          :s1 a :T .

          :s1 :p "s1-p-1" .
          :s1 :p "s1-p-2" .
          :s1 :p "s1-p-3" .

          :s1 :q "s1-q-1" .
          :s1 :q "s1-q-2" .
          :s1 :q "s1-q-3" .

          :s2 a :T .

          :s2 :p "s2-p-1" .
          :s2 :p "s2-p-2" .
          :s2 :p "s2-p-3" .

          :s2 :q "s2-q-1" .
          :s2 :q "s2-q-2" .
          :s2 :q "s2-q-3" .
                         """;


  public static void mainZ(String...args) {

      Graph g = RDFParser.fromString(data).lang(Lang.TTL).toGraph();

      // Can't flow into a LATERAL.
      // Exception?
      // Lateral vars in VarFinder?
      // Wrong
      // Problem: It's { {} LATERAL { SELECT * { ?s :p ?p } ORDER BY ?p LIMIT 2 } }
      //   Unconstrained LATERAL
      //   then UNION
      String qs1 = """
PREFIX : <http://example/>

SELECT * {
  ?s a :T .
  { LATERAL { SELECT * { ?s :p ?p } ORDER BY ?p LIMIT 2 } }
  UNION
  { LATERAL { SELECT * { ?s :q ?q } ORDER BY ?q LIMIT 2 } }
}
              """;
      // Right
      String qs2 = """
              PREFIX : <http://example/>
SELECT * {
  ?s a :T .
  LATERAL {
    { SELECT * { ?s :p ?p } ORDER BY ?p LIMIT 2 }
    UNION
    { SELECT * { ?s :q ?q } ORDER BY ?q LIMIT 2 }
  }
}
              """;

      String qs0 = """
              PREFIX : <http://example/>
SELECT * {
  ?s a :T .
  ## TDevLa{ LATERAL { SELECT * { ?s :p ?p } ORDER BY ?p LIMIT 2 } }
  { SELECT * { ?s :p ?p } ORDER BY ?p LIMIT 2 } }
}
              """;


      DevLib.joinClassification(qs1);
      System.exit(0);

      System.out.println("== Bad");
      dwimOp(qs1);
      dwimExec(g, qs1);
      System.out.println();
      System.out.println("== Good");
      dwimOp(qs2);
      dwimExec(g, qs2);
      System.exit(0);
  }

  private static void dwimExec(Graph data, String qs) {
      Query query = QueryFactory.create(qs);
      QueryEngineRef.register();
      QueryExecUtils.exec(query, DatasetGraphFactory.wrap(data));
      QueryEngineRef.unregister();
      QueryEngineMain.register();
      QueryExecUtils.exec(query, DatasetGraphFactory.wrap(data));
  }

  public static void dwimOp(String qs) {
      Query query = QueryFactory.create(qs);
      Op op = Algebra.compile(query) ;
      System.out.println(" = Algebra");
      WriterOp.output(IndentedWriter.stdout, op, query.getPrefixMapping());
      //Op op1 = Transformer.transform(new TransformReorder(), op) ;
      System.out.println(" = Optimized");
      Op op1 = Algebra.optimize(op);
      WriterOp.output(IndentedWriter.stdout, op1, query.getPrefixMapping());
      IndentedWriter.stdout.flush() ;

  }

}
