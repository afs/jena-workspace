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

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.Prologue ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.sparql.core.Quad ;

public class Jena1147 {
    
    public static void main(String ...a) {
        Prologue prologue = new Prologue() ;
        ParserProfile profile = new ParserProfileWrapper() {
            // createTriple
            // createQuad
            // createURI -- predicate
            // createBlankNode - no
            // createTypedLiteral
            // createLangLiteral
            // createStringLiteral
            
//            create(Node, Token) -- Leave
//            createBlankNode(Node, long, long)
//            createBlankNode(Node, String, long, long)
//            createLangLiteral(String, String, long, long)
//            createNodeFromToken(Node, Token, long, long)
//            createQuad(Node, Node, Node, Node, long, long)
//            createStringLiteral(String, long, long)
//            createTriple(Node, Node, Node, long, long)
//            createTypedLiteral(String, RDFDatatype, long, long)
//            createURI(String, long, long)
        } ;
        // createTriple
        // createQuad
        
    }
    
    static class ParserProfileWrapper implements ParserProfile {

        @Override
        public String resolveIRI(String uriStr, long line, long col) {
            return null;
        }

        @Override
        public IRI makeIRI(String uriStr, long line, long col) {
            return null;
        }

        @Override
        public Triple createTriple(Node subject, Node predicate, Node object, long line, long col) {
            return null;
        }

        @Override
        public Quad createQuad(Node graph, Node subject, Node predicate, Node object, long line, long col) {
            return null;
        }

        @Override
        public Node createURI(String uriStr, long line, long col) {
            return null;
        }

        @Override
        public Node createTypedLiteral(String lexical, RDFDatatype datatype, long line, long col) {
            return null;
        }

        @Override
        public Node createLangLiteral(String lexical, String langTag, long line, long col) {
            return null;
        }

        @Override
        public Node createStringLiteral(String lexical, long line, long col) {
            return null;
        }

        @Override
        public Node createBlankNode(Node scope, String label, long line, long col) {
            return null;
        }

        @Override
        public Node createBlankNode(Node scope, long line, long col) {
            return null;
        }

        @Override
        public Node createNodeFromToken(Node scope, Token token, long line, long col) {
            return null;
        }

        @Override
        public Node create(Node currentGraph, Token token) {
            return null;
        }

        @Override
        public LabelToNode getLabelToNode() {
            return null;
        }

        @Override
        public void setLabelToNode(LabelToNode labelToNode) {}

        @Override
        public ErrorHandler getHandler() {
            return null;
        }

        @Override
        public void setHandler(ErrorHandler handler) {}

        @Override
        public Prologue getPrologue() {
            return null;
        }

        @Override
        public void setPrologue(Prologue prologue) {}

        @Override
        public boolean isStrictMode() {
            return false;
        }

        @Override
        public void setStrictMode(boolean mode) {}
        
    }
}
