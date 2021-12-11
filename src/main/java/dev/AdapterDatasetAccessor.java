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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFDatasetConnection;

public class AdapterDatasetAccessor implements DatasetAccessor {
    private final RDFDatasetConnection conn;

    public AdapterDatasetAccessor(RDFDatasetConnection connection) {
        this.conn = connection;
    }

    @Override public Model getModel()                           { return conn.fetch(); }
    @Override public Model getModel(String graphName)           { return conn.fetch(graphName); }
    @Override public boolean containsModel(String graphName)    { return true; }
    @Override public void putModel(Model model)                 { conn.put(model); }
    @Override public void putModel(String name, Model model)    { conn.put(name, model); }
    @Override public void deleteDefault()                       { conn.delete(); }
    @Override public void deleteModel(String name)              { conn.delete(name); }
    @Override public void add(Model model)                      { conn.load(model); }
    @Override public void add(String name, Model model)         { conn.load(name, model); }
}

@Deprecated
interface DatasetAccessor
{
    // Copied from Jena.
    /** Get the default model of a Dataset */
    public Model getModel() ;

    /** Get a named model of a Dataset */
    public Model getModel(String graphUri) ;

//    /** Does the Dataset contain a default graph? */
//    public boolean containsDefault() ;

    /** Does the Dataset contain a named graph? */
    public boolean containsModel(String graphURI) ;

    /** Put (replace) the default model of a Dataset */
    public void putModel(Model data) ;

    /** Put (create/replace) a named model of a Dataset */
    public void putModel(String graphUri, Model data) ;

    /** Delete (which means clear) the default model of a Dataset */
    public void deleteDefault() ;

    /** Delete a named model of a Dataset */
    public void deleteModel(String graphUri) ;

//    /** Clear the default graph, delete all the named models */
//    public void reset() ;

    /** Add statements to the default model of a Dataset */
    public void add(Model data) ;

    /** Add statements to a named model of a Dataset */
    public void add(String graphUri, Model data) ;
}