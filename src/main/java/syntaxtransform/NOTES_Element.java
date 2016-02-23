/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package syntaxtransform;

public class NOTES_Element {
    // Modify SELECT to put in pre- bindings.

    // 2 ways:
    // 1: Rewrite query AND add in results (PAramterizedQuery.parameterizeIncludeInput)
    // 2: Add wrapper to put them into results.
    
    // For blank nodes, (2) is easier because it does not rely on results coming
    // back as <_:blank nodes> though it does not work for outgoing without help.    
    
    
    
    
    // Centralize BNode handling.
    //NodeFunctions.
    
    /* Search for "_:
    FmtUtils.java - jena-arq/src/main/java/org/apache/jena/sparql/util
    JsonLDReader.java - jena-arq/src/main/java/org/apache/jena/riot/lang
    LangRDFJSON.java - jena-arq/src/main/java/org/apache/jena/riot/lang
    NodeFormatterNT.java - jena-arq/src/main/java/org/apache/jena/riot/out
    NodeFunctions.java - jena-arq/src/main/java/org/apache/jena/sparql/expr/nodevalue
    NodeToLabel.java - jena-arq/src/main/java/org/apache/jena/riot/out (3 matches)
    NodeToLabelMap.java - jena-arq/src/main/java/org/apache/jena/sparql/util
    RDFJSONWriter.java - jena-arq/src/main/java/org/apache/jena/riot/writer (2 matches)
    RiotLib.java - jena-arq/src/main/java/org/apache/jena/riot/system
    TokenizerText.java - jena-arq/src/main/java/org/apache/jena/riot/tokens
    
    "<_:"
    NodeToLabel.java - jena-arq/src/main/java/org/apache/jena/riot/out
    NodeToLabelMap.java - jena-arq/src/main/java/org/apache/jena/sparql/util
    */
    
//  ARQ.constantBNodeLabels ;
//  ARQ.outputGraphBNodeLabels
//  ARQ.inputGraphBNodeLabels
    // ARQ.enableBlankNodeResultLabels(true)
    
    // QueryTransformOps
    //   CONSTRUCT
    //   DESCRIBE
    //   All the updates.
    //   Injecting blanknodes : <_:abc> [*]
    //   BIND, VALUE
    // Full testing.
    
    // Retaining the input bindings.
    
    
    // Subquery { SELECT ?x }
    // Test with Q->A->Subst->OpAsQ->Q2
    
    // [*] NodeToLabelMapBNode.asString : bnode -> <_:label> form
    
    // Modify SELECT to put in pre- bindings.

    
}

/* markdown:


















 */

