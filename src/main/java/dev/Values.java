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

import java.util.Set;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.XSD;

/**
 * Java values to and from {@link Node Nodes}
 */
public class Values {

    // int, long, String, double, (float), ZonedDateTime, LocalDateTime, Duration

    /**
     * Return a {@link Node} for the given integer value.
     * The datatype of the literal node will be xsd:integer.
     */
    public static Node node(int intValue) {
        return node(intValue, XSDDatatype.XSDinteger);
    }

    /**
     * Return a {@link Node} for the given integer value using the datatype.
     * Throw a {@link JenaException} if not valid for this datatype.
     */
    public static Node node(int intValue, XSDDatatype xsdDatatype) {
        String lex = Integer.toString(intValue);
        // xsd4ld function?
        if ( ! xsdDatatype.equals(XSDDatatype.XSDinteger) && ! xsdDatatype.isValid(lex) )
            // Can't be invalid for xsd:integer
            throw valueException("Not valid for "+xsdStr(xsdDatatype)+ ": value = "+intValue);
        return NodeFactory.createLiteral(lex, xsdDatatype);
    }

    /**
     * Return a {@link Node} for the given long value.
     * The datatype of the literal node will be xsd:integer.
     */
    public static Node node(long longValue) {
        return node(longValue, XSDDatatype.XSDinteger);
    }

    /**
     * Return a {@link Node} for the given integer value using the datatype.
     * Throw a {@link JenaException} if not valid for this datatype.
     */
    public static Node node(long longValue, XSDDatatype xsdDatatype) {
        String lex = Long.toString(longValue);
        if ( ! xsdDatatype.equals(XSDDatatype.XSDinteger) && ! xsdDatatype.isValid(lex) )
            // Can't be invalid for xsd:integer
            throw valueException("Not valid for "+xsdStr(xsdDatatype)+ ": value = "+longValue);
        return NodeFactory.createLiteral(lex, xsdDatatype);
    }

    public static Node node(String strValue) {
        return NodeFactory.createLiteral(strValue);
    }

    public static Node node(double doubleValue) {
        return NodeFactory.createLiteral(Double.toString(doubleValue), XSDDatatype.XSDdouble);
    }

    // Discourage float.

    // --

    public static int asInt(Node node) {
        if ( ! node.isLiteral() )
            throw valueException("Not a literal");
        RDFDatatype dt = node.getLiteralDatatype();
        if ( ! isIntegerDerived(dt) )
            throw valueException("Not an XSD integer or derived type of XSD integer");
        try {
            return Integer.parseInt(node.getLiteralLexicalForm());
        } catch (NumberFormatException ex) {
            throw valueException("Not acceptable for an int: "+node.getLiteralLexicalForm());
        }
    }

    public static long asLong(Node node) {
        if ( ! node.isLiteral() )
            throw valueException("Not a literal");
        RDFDatatype dt = node.getLiteralDatatype();
        if ( ! isIntegerDerived(dt) )
            throw valueException("Not an XSD integer or derived type of XSD integer");
        try {
            return Long.parseLong(node.getLiteralLexicalForm());
        } catch (NumberFormatException ex) {
            throw valueException("Not acceptable for a long: "+node.getLiteralLexicalForm());
        }
    }

    public static String asString(Node node) {
        if ( ! node.isLiteral() )
            throw valueException("Not a literal");
        RDFDatatype dt = node.getLiteralDatatype();
        if ( XSDDatatype.XSDstring.equals(dt) )
            throw valueException("Not an XSD string");
        return node.getLiteralLexicalForm();
    }

    // --



    //--

    public static double asDouble(Node node) {
        if ( ! node.isLiteral() )
            throw valueException("Not a literal");
        RDFDatatype dt = node.getLiteralDatatype();
        if ( ! fpDatatypes.contains(dt) )
            throw valueException("Not an XSD double or XSD float");
        try {
            return Double.parseDouble(node.getLiteralLexicalForm());
        } catch (NumberFormatException ex) {
            throw valueException("Not acceptable for a double: "+node.getLiteralLexicalForm());
        }
    }

    static Set<RDFDatatype> fpDatatypes = Set.of(XSDDatatype.XSDdouble, XSDDatatype.XSDfloat);

    static Set<RDFDatatype> integerDerived = Set.of(
        XSDDatatype.XSDinteger,
        XSDDatatype.XSDlong,
        XSDDatatype.XSDint,
        XSDDatatype.XSDshort,
        XSDDatatype.XSDbyte,

        XSDDatatype.XSDunsignedInt,
        XSDDatatype.XSDunsignedLong,
        XSDDatatype.XSDunsignedShort,
        XSDDatatype.XSDunsignedByte,

        XSDDatatype.XSDnonPositiveInteger,
        XSDDatatype.XSDnonNegativeInteger,
        XSDDatatype.XSDpositiveInteger,
        XSDDatatype.XSDnegativeInteger
        );

    private static boolean isIntegerDerived(RDFDatatype dt) {
        return integerDerived.contains(dt);
    }

//    private static boolean hasDatatype(Node node, XSDDatatype xsdinteger) {
//        return false;
//    }

    private static JenaException valueException(String string) {
        return new JenaException(string);
    }

    private static int nsLength = XSD.getURI().length();
    /** Display string for XSD datatypes */
    private static String xsdStr(XSDDatatype xsdDatatype) {
        String uri = xsdDatatype.getURI();
        String s = uri.substring(nsLength);
        return "xsd:"+s;
    }
}

