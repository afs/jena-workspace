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

import java.math.BigDecimal;

import arq.qexpr;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType;
import org.apache.jena.shared.impl.JenaParameters;
import org.apache.jena.sparql.function.FunctionCastXSD;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sys.JenaSystem;

public class DevPrecisionDecimal {

    public static class XSDPrecisionDecimalType extends XSDBaseNumericType {
    //public static class XSDPrecisionDecimalType extends XSDDatatype {
        public XSDPrecisionDecimalType(String name) {
            super("decimal") ; // Lie mode.
            super.javaClass = BigDecimal.class ;
            super.uri = XSD + "#"+name ;
        }

        @Override
        public Object parse(String lexicalForm) {
            checkWhitespace(lexicalForm);
            // NaN and INF
            switch(lexicalForm) {
                case "NaN": return Double.NaN;
                case "INF":
                case "+INF":
                    return Double.POSITIVE_INFINITY;
                case "-INF":
                    return Double.NEGATIVE_INFINITY;
            }

            return new BigDecimal(lexicalForm);
            //return super.parse(lexicalForm);
        }

        //XSDBaseNumericType
        /**
         * Check for whitespace violations.
         * Turned off by default.
         */
        @Override
        protected void checkWhitespace(String lexicalForm) {
            if (JenaParameters.enableWhitespaceCheckingOfTypedLiterals) {
                if ( ! lexicalForm.trim().equals(lexicalForm)) {
                    throw new DatatypeFormatException(lexicalForm, this, "whitespace violation");
                }
            }
        }

        /**
         * Convert a value of this datatype to lexical form.
         * Certain forms are not a simple matter of java's toString on the Number object.
         */
        @Override
        public String unparse(Object value) {
            if ( value instanceof BigDecimal )
                // Avoid exponent usage.
                //return ((BigDecimal)value).toPlainString() ;
                return ((BigDecimal)value).toString();
            if ( value instanceof Double )
                return ((Double)value).toString();
            return value.toString();
        }
    }

    public static void main(String...args) {
//        BigDecimal bd = new BigDecimal("2E3");
//        System.out.println(bd);
//        System.exit(0);

        JenaSystem.init();

        XSDPrecisionDecimalType dt = new XSDPrecisionDecimalType("precisionDecimal");
        FunctionRegistry.get().put(dt.getURI(), new FunctionCastXSD(dt)) ;

        XSDDatatype.XSDinteger.isValid("123");

        dt.isValid("1.23E1");

        try {
            //qexpr.main("xsd:decimal('1.23E1')");
            qexpr.main("xsd:precisionDecimal('1.23E1')+1");
        } catch (Exception ex ) {
            ex.printStackTrace();
        }
        System.exit(0);
    }
}
