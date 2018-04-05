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

import java.util.function.BiFunction ;
import java.util.function.Function ;
import java.util.function.Supplier ;

import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions ;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp ;
import org.apache.jena.sparql.function.FunctionBase2 ;
import org.apache.jena.sparql.function.FunctionFactory ;
import org.apache.jena.sparql.function.FunctionRegistry ;

// Support for SPARQL functions as URI calls.
//    https://www.w3.org/ns/sparql#
// Support for XSD operators as URI calls. 

public class Xop {
    private static void add(FunctionRegistry registry, String uri, Class<? > funcClass) {
        registry.put(uri, funcClass) ;
    }
    
    /// "op:"
    private static  String xfn = ARQConstants.fnPrefix;
    private static  String sfn = "https://www.w3.org/ns/sparql#"; //ARQConstants.sparqlFunctionsPrefix;
    
    private static void addOp(FunctionRegistry registry, String name, BiFunction<NodeValue, NodeValue, NodeValue> f) {
        
        FunctionBase2 fb2 = new FunctionBase2() {
            @Override
            public NodeValue exec(NodeValue v1, NodeValue v2) {
                return f.apply(v1, v2) ;
            }};
        FunctionFactory ff = (uri) -> fb2;
        registry.put(xfn+name, ff) ;
    }

    private static void addOpComp(FunctionRegistry registry, String uri, BiFunction<NodeValue, NodeValue, Boolean> f) {
        FunctionBase2 fb2 = new FunctionBase2() {
            @Override
            public NodeValue exec(NodeValue v1, NodeValue v2) {
                return NodeValue.makeBoolean(f.apply(v1, v2)) ;
            }};
        FunctionFactory ff = (xuri) -> fb2;
        registry.put(uri, ff) ;
    }
    
    private static void addSparqlOp(FunctionRegistry registry, String uri, BiFunction<NodeValue, NodeValue, NodeValue> f) {}
    private static void addSparqlOp(FunctionRegistry registry, String uri, Function<NodeValue, NodeValue> f) {}
    private static void addSparqlOp(FunctionRegistry registry, String uri, Supplier<NodeValue> f) {}

    static {
        //addSparqlOp(null, "bound", ()->NodeFunctions.isBlank(v)

        addSparqlOp(null, "isIRI", NodeFunctions::isIRI);
        addSparqlOp(null, "isURI", NodeFunctions::isIRI);
        addSparqlOp(null, "isBlank", NodeFunctions::isBlank);

        addSparqlOp(null, "isLiteral", NodeFunctions::isLiteral);
        addSparqlOp(null, "isNumeric", (nv)->NodeValue.makeBoolean(nv.isNumber()));

        addSparqlOp(null, "str", NodeFunctions::str);

        addSparqlOp(null, "lang", NodeFunctions::lang);
        addSparqlOp(null, "datatype", NodeFunctions::datatype);
        addSparqlOp(null, "IRI", nv->NodeFunctions.iri(nv,null));
        addSparqlOp(null, "URI", nv->NodeFunctions.iri(nv,null));
        //addSparqlOp(null, "BNODE", NodeFunctions::BNODE);

        addSparqlOp(null, "STRDT", NodeFunctions::strDatatype);
        addSparqlOp(null, "STRLANG", NodeFunctions::strLang);
        addSparqlOp(null, "UUID", NodeFunctions::uuid);
        addSparqlOp(null, "STRUUID", NodeFunctions::struuid);
        
                    
        // XCheck.
        /*
         * Adapters for ExprFunction
         *     NodeFunctions.strDatatype
         *   Some are XSDFuncOp
         * 
         SPARQL only:
            17.4.1.1 bound
            17.4.1.2 IF
            17.4.1.3 COALESCE
            17.4.1.4 NOT EXISTS and EXISTS
            17.4.1.5 logical-or
            17.4.1.6 logical-and
            17.4.1.7 RDFterm-equal
            17.4.1.8 sameTerm
            17.4.1.9 IN
            17.4.1.10 NOT IN
        17.4.2 Functions on RDF Terms
            17.4.2.1 isIRI
            17.4.2.2 isBlank
            17.4.2.3 isLiteral
            17.4.2.4 isNumeric
            17.4.2.5 str
            17.4.2.6 lang
            17.4.2.7 datatype
            17.4.2.8 IRI
            17.4.2.9 BNODE
            17.4.2.10 STRDT
            17.4.2.11 STRLANG
            17.4.2.12 UUID
            17.4.2.13 STRUUID
            
            17.4.5.9 tz
         */

        /*
        17.4 Function Definitions
        17.4.1 Functional Forms
            17.4.1.1 bound
            17.4.1.2 IF
            17.4.1.3 COALESCE
            17.4.1.4 NOT EXISTS and EXISTS
            17.4.1.5 logical-or
            17.4.1.6 logical-and
            17.4.1.7 RDFterm-equal
            17.4.1.8 sameTerm
            17.4.1.9 IN
            17.4.1.10 NOT IN
        17.4.2 Functions on RDF Terms
            17.4.2.1 isIRI
            17.4.2.2 isBlank
            17.4.2.3 isLiteral
            17.4.2.4 isNumeric
            17.4.2.5 str
            17.4.2.6 lang
            17.4.2.7 datatype
            17.4.2.8 IRI
            17.4.2.9 BNODE
            17.4.2.10 STRDT
            17.4.2.11 STRLANG
            17.4.2.12 UUID
            17.4.2.13 STRUUID
        17.4.3 Functions on Strings
            17.4.3.1 Strings in SPARQL Functions
                17.4.3.1.1 String arguments
                17.4.3.1.2 Argument Compatibility Rules
                17.4.3.1.3 String Literal Return Type
            17.4.3.2 STRLEN
            17.4.3.3 SUBSTR
            17.4.3.4 UCASE
            17.4.3.5 LCASE
            17.4.3.6 STRSTARTS
            17.4.3.7 STRENDS
            17.4.3.8 CONTAINS
            17.4.3.9 STRBEFORE
            17.4.3.10 STRAFTER
            17.4.3.11 ENCODE_FOR_URI
            17.4.3.12 CONCAT
            17.4.3.13 langMatches
            17.4.3.14 REGEX
            17.4.3.15 REPLACE
        17.4.4 Functions on Numerics
            17.4.4.1 abs
            17.4.4.2 round
            17.4.4.3 ceil
            17.4.4.4 floor
            17.4.4.5 RAND
        17.4.5 Functions on Dates and Times
            17.4.5.1 now
            17.4.5.2 year
            17.4.5.3 month
            17.4.5.4 day
            17.4.5.5 hours
            17.4.5.6 minutes
            17.4.5.7 seconds
            17.4.5.8 timezone
            17.4.5.9 tz
        17.4.6 Hash Functions
            17.4.6.1 MD5
            17.4.6.2 SHA1
            17.4.6.3 SHA256
            17.4.6.4 SHA384
            17.4.6.5 SHA512
        */
    }
    
    static {
        addOp(null, "numeric-add", XSDFuncOp::numAdd);
        addOp(null, "numeric-subtract", XSDFuncOp::numSubtract);
        addOp(null, "numeric-multiply", XSDFuncOp::numMultiply);
        addOp(null, "numeric-divide", XSDFuncOp::numDivide);
        //addOp(null, "numeric-integer-divide", XSDFuncOp::num
        //addOp(null, "numeric-mod", XSDFuncOp::num
        
        addOpComp(null, "yearMonthDuration-less-than", (x,y)->XSDFuncOp.compareDuration(x,y)<0);
    }

    // And add op:'s
//  4.2.1 op:numeric-add
//  4.2.2 op:numeric-subtract
//  4.2.3 op:numeric-multiply
//  4.2.4 op:numeric-divide
//  4.2.5 op:numeric-integer-divide
//  4.2.6 op:numeric-mod
//  4.2.7 op:numeric-unary-plus
//  4.2.8 op:numeric-unary-minus
//  4.3.1 op:numeric-equal
//  4.3.2 op:numeric-less-than
//  4.3.3 op:numeric-greater-than
    
//  7.2.1 op:boolean-equal
//  7.2.2 op:boolean-less-than
//  7.2.3 op:boolean-greater-than
    
//  8.2.1 op:yearMonthDuration-less-than
//  8.2.2 op:yearMonthDuration-greater-than
    
//  8.2.3 op:dayTimeDuration-less-than
//  8.2.4 op:dayTimeDuration-greater-than
    
//  8.2.5 op:duration-equal
    
//  8.4.1 op:add-yearMonthDurations
//  8.4.2 op:subtract-yearMonthDurations
//  8.4.3 op:multiply-yearMonthDuration
//  8.4.4 op:divide-yearMonthDuration
//  8.4.5 op:divide-yearMonthDuration-by-yearMonthDuration
    
//  8.4.6 op:add-dayTimeDurations
//  8.4.7 op:subtract-dayTimeDurations
//  8.4.8 op:multiply-dayTimeDuration
//  8.4.9 op:divide-dayTimeDuration
//  8.4.10 op:divide-dayTimeDuration-by-dayTimeDuration
    
//  9.4.1 op:dateTime-equal
//  9.4.2 op:dateTime-less-than
//  9.4.3 op:dateTime-greater-than
    
//  9.4.4 op:date-equal
//  9.4.5 op:date-less-than
//  9.4.6 op:date-greater-than
    
//  9.4.7 op:time-equal
//  9.4.8 op:time-less-than
//  9.4.9 op:time-greater-than
    
//  9.4.10 op:gYearMonth-equal
//  9.4.11 op:gYear-equal
//  9.4.12 op:gMonthDay-equal
//  9.4.13 op:gMonth-equal
//  9.4.14 op:gDay-equal
    
//  9.7.2 op:subtract-dateTimes
//  9.7.3 op:subtract-dates
//  9.7.4 op:subtract-times
//  9.7.5 op:add-yearMonthDuration-to-dateTime
//  9.7.6 op:add-dayTimeDuration-to-dateTime
//  9.7.7 op:subtract-yearMonthDuration-from-dateTime
//  9.7.8 op:subtract-dayTimeDuration-from-dateTime
//  9.7.9 op:add-yearMonthDuration-to-date
//  9.7.10 op:add-dayTimeDuration-to-date
//  9.7.11 op:subtract-yearMonthDuration-from-date
//  9.7.12 op:subtract-dayTimeDuration-from-date
//  9.7.13 op:add-dayTimeDuration-to-time
//  9.7.14 op:subtract-dayTimeDuration-from-time

}
