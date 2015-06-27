/**
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

package syntaxtransform;

import java.util.Map ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.ParameterizedSparqlString ;
import org.apache.jena.query.Query ;
import org.apache.jena.update.UpdateRequest ;

public class TestParameterizedSparqlString2 extends AbstractTestParameterized {

    @Override
    public Query process(String queryString, Map<String, Node> substitutions) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(queryString) ;
        substitutions.forEach((vn, n)->{
            pss.setParam(vn, n);
        }) ;
        return pss.asQuery() ; 
    }

    @Override
    public UpdateRequest processUpdate(String updateString, Map<String, Node> substitutions) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(updateString) ;
        substitutions.forEach((vn, n)->{
            pss.setParam(vn, n);
        }) ;
        return pss.asUpdate() ;
    }

}

