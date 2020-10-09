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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryExecutionBase;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.Context;

/** Query Execution builder. */
public class QueryExecutionBuilderSubstitInitial {

    private DatasetGraph dataset = null;
    private Query        query   = null;
    private Context      context = null;
    private Binding      binding = null;

    public static QueryExecutionBuilderSubstitInitial create() {
        return new QueryExecutionBuilderSubstitInitial();
    }

    private QueryExecutionBuilderSubstitInitial() {}

    public QueryExecutionBuilderSubstitInitial query(Query query) {
        this.query = query;
        return this;
    }

    public QueryExecutionBuilderSubstitInitial dataset(DatasetGraph dsg) {
        this.dataset = dsg;
        return this;
    }

    public QueryExecutionBuilderSubstitInitial context(Context context) {
        this.context = context;
        return this;
    }

    public QueryExecutionBuilderSubstitInitial initialBinding(Binding binding) {
        this.binding = binding;
        return this;
    }

    public QueryExecution build() {
        Objects.requireNonNull(query, "Query for QueryExecution");

        Map<Var, Node> substitutions = null;
        if ( binding != null )
            substitutions = bindingToMap(binding);

        Query query2 = ( substitutions == null ) ? query : QueryTransformOps.transform(query, substitutions);

        query2.setResultVars();
        Context cxt;

        if ( context == null ) {
            // Default is to take the global context, the copy it and merge in the dataset context.
            // If a context is specified by context(Context), use that as given.
            // The query context is modified to insert the current time.
            cxt = ARQ.getContext();
            cxt = Context.setupContextForDataset(cxt, dataset) ;
        } else {
            // Isolate to snapshot it and to allow it to be  modified.
            cxt = context.copy();
        }

        QueryEngineFactory f = QueryEngineRegistry.get().find(query2, dataset, cxt);
        if ( f == null ) {
            Log.warn(QueryExecutionBuilderSubstitInitial.class, "Failed to find a QueryEngineFactory");
            return null;
        }

        QueryExecution qExec = new QueryExecutionBase(query2, dataset, cxt, f);
//        if ( binding != null )
//            qExec.setInitialBinding(binding);
        return qExec;
    }

    private static Map<Var, Node> bindingToMap(Binding binding) {
        Map<Var, Node> substitutions = new HashMap<>();
        Iterator<Var> iter = binding.vars();
        while(iter.hasNext()) {
            Var v = iter.next();
            Node n = binding.get(v);
            substitutions.put(v, n);
        }
        return substitutions;
    }
}
