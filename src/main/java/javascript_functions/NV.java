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

package javascript_functions;

import org.apache.jena.sparql.expr.NodeValue;

public class NV implements RDFJS
    {
        private NodeValue nv;
        
        // Javascript names and RDF extras.
        public boolean isURI() { return nv.isIRI(); }
        public boolean isBlank() { return nv.isBlank(); }
        public boolean isNumber() { return nv.isNumber(); }
        public boolean isLiteral() { return nv.isLiteral(); }
        
        // -- rdfjs
        @Override
        public String getTermType() {
            if ( isURI() )
                return "NamedNode";
            if ( isBlank() )
                return "BlankNode";
            if ( isLiteral() )
                return "Literal";
            return null; 
        }
        
        @Override
        public String getValue() { 
            if ( isURI() )
                return getUri();
            if ( isBlank() )
                return getLabel();
            if ( isLiteral() )
                return getLex();
            return null; 
        }
        // -- rdfjs
        
        public String getLabel() { return nv.getDatatypeURI(); }
        public String getDT() { return nv.getDatatypeURI(); }
        public String getDatatype() { return nv.getDatatypeURI(); }
        public String getLanguage() { return nv.getLang(); }
        public String getLang() { return nv.getLang().toLowerCase(); }
        public String getLex() { return nv.getNode().getLiteralLexicalForm(); }
        public String getUri() { return nv.getNode().getURI(); }
        
        public NV(NodeValue nv) {
            this.nv = nv;
        }
//        Six data types that are primitives:
//            Boolean
//            Null
//            Undefined
//            Number
//            String
//            Symbol (new in ECMAScript 6)
//        and Object

        public NodeValue nv() {
            return nv;
        }
        
        @Override
        public String toString() { return nv.asUnquotedString(); }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((nv == null) ? 0 : nv.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            NV other = (NV)obj;
            if ( nv == null ) {
                if ( other.nv != null )
                    return false;
            } else if ( !nv.equals(other.nv) )
                return false;
            return true;
        } 
    }