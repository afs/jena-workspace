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

package expr;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.jena.datatypes.xsd.XSDDateTime ;
import org.apache.jena.graph.Node;

/**
 * A small random collection of utility functions used by the rule systems.
 */
public class Util2 {

    /**
     * Check whether a Node is an Instant (DateTime) value
     */
    public static boolean isInstant(Node n) {
        if (n.isLiteral()) {
            Object o = n.getLiteralValue();
            return (o instanceof XSDDateTime);
        } else {
            return false;
        }
    }

    /**
     * Compare two numeric nodes.
     * @param n1 the first numeric valued literal node
     * @param n2 the second numeric valued literal node
     * @return -1 if n1 is less than n2, 0 if n1 equals n2 and +1 if n1 greater than n2
     * @throws ClassCastException if either node is not numeric
     */
    public static int compareNumbers(Node n1, Node n2) {
        if (n1.isLiteral() && n2.isLiteral()) {
            Object v1 = n1.getLiteralValue();
            Object v2 = n2.getLiteralValue();
            if (v1 instanceof Number && v2 instanceof Number) {
                Number num1 = (Number)v1;
                Number num2 = (Number)v2;
                return compareNumbers(num1, num2);
            }
        }
        throw new ClassCastException("Non-numeric literal in compareNumbers");
    }

    /*package*/ static int compareNumbers(Number num1, Number num2) {
        // Comparing java.lang.Number.
        //
        // Check whether the two numbers are of the same primitive kind (both long
        // or both double valued) and, if so, compare. Do the same for BigDecimal
        // and BigInteger.
        //
        // If all that fails, convert to BigDecimal and compare.

        // Wrapped primitives, with integer values.
        if ( valueIsLong(num1) && valueIsLong(num2) ) {
            long z1 = num1.longValue();
            long z2 = num2.longValue();
            return Long.compare(z1, z2);
        }
        // Wrapped primitives, with floating point values.
        if ( valueIsDouble(num1) && valueIsDouble(num2) ) {
            double d1 = num1.doubleValue();
            double d2 = num2.doubleValue();
            return Double.compare(d1, d2);
        }
        // Both BigDecimal
        if ( num1 instanceof BigDecimal && num2 instanceof BigDecimal ) {
            BigDecimal dec1 = (BigDecimal)num1;
            BigDecimal dec2 = (BigDecimal)num2;
            return dec1.compareTo(dec2); 
        }
        // Both BigInteger
        if ( num1 instanceof BigInteger && num2 instanceof BigInteger ) {
            BigInteger int1 = (BigInteger)num1;
            BigInteger int2 = (BigInteger)num2;
            return int1.compareTo(int2); 
        }

        // Mixed. Includes comparing BigInteger and BigDecimal and comparing
        // BigInteger or BigDecimal with a wrapped primitive.
        BigDecimal dec1 = convertToBigDecimal(num1);
        BigDecimal dec2 = convertToBigDecimal(num2);
        return dec1.compareTo(dec2);
    }
    
    private static BigDecimal convertToBigDecimal(Number num) {
        if ( num instanceof BigDecimal )
            return (BigDecimal)num ;
        if ( valueIsLong(num) )
            return new BigDecimal(num.longValue()) ;
        if ( num instanceof BigInteger )
            return new BigDecimal((BigInteger)num) ;
        // double and float.
        return new BigDecimal(num.doubleValue()) ;
    }

    private static boolean valueIsLong(Number v) {
        if ( v instanceof Long ) return true;
        if ( v instanceof Integer ) return true;
        if ( v instanceof Short ) return true;
        if ( v instanceof Byte ) return true;
        return false;
    }

    private static boolean valueIsDouble(Number v) {
        if ( v instanceof Double ) return true;
        if ( v instanceof Float ) return true;
        return false;
    }

    /**
     * Compare two time Instant nodes.
     * @param n1 the first time instant (XSDDateTime) valued literal node
     * @param n2 the second time instant (XSDDateTime) valued literal node
     * @return -1 if n1 is less than n2, 0 if n1 equals n2 and +1 if n1 greater than n2
     * @throws ClassCastException if either not is not numeric
     */
    public static int compareInstants(Node n1, Node n2) {
        if (n1.isLiteral() && n2.isLiteral()) {
            Object v1 = n1.getLiteralValue();
            Object v2 = n2.getLiteralValue();
            if (v1 instanceof XSDDateTime && v2 instanceof XSDDateTime) {
                XSDDateTime a = (XSDDateTime) v1;
                XSDDateTime b = (XSDDateTime) v2;
                return a.compare(b);
            }
        }
        throw new ClassCastException("Non-numeric literal in compareNumbers");
    }

    /**
     * General order comparator for typed literal nodes, works for all numbers and
     * for date times.
     */
    public static int compareTypedLiterals(Node n1, Node n2) {
        if (n1.isLiteral() && n2.isLiteral()) {
            Object v1 = n1.getLiteralValue();
            Object v2 = n2.getLiteralValue();
            if (v1 instanceof Number && v2 instanceof Number) {
                return compareNumbers((Number)v1, (Number)v2);
            }
            if (v1 instanceof XSDDateTime && v2 instanceof XSDDateTime) {
                XSDDateTime a = (XSDDateTime) v1;
                XSDDateTime b = (XSDDateTime) v2;
                return a.compare(b);
            }
        }
        throw new ClassCastException("Compare typed literals can only compare numbers and datetimes");
    }
}
