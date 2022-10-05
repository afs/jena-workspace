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

package assembler2;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.other.RDFDataException;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.engine.binding.Binding;

class Nodes {
    public static String nodeAsString(Binding row, String varName, PrefixMap pmap) {
        Node n = row.get(varName);
        if ( isSimpleString(n) ) {
            throw new RDFDataException("Not a string literal: "+NodeFmtLib.str(n, pmap));
        }
        return n.getLiteralLexicalForm();
    }

    /**
     * A Node is a simple string if:
     * <ul>
     * <li>(RDF 1.0) No datatype and no language tag.
     * <li>(RDF 1.1) xsd:string
     * </ul>
     */
    public static boolean isSimpleString(Node n) {
        return Util.isSimpleString(n);
    }

    /**
     * A Node is a language string if it has a language tag.
     * (RDF 1.0 and RDF 1.1)
     */
    public static boolean isLangString(Node n) {
        return Util.isLangString(n);
    }

    // isInteger 9any integer type)
    // isNumeric
}