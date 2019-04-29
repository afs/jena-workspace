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

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprNotComparableException;
import org.apache.jena.sparql.expr.NodeValue;

public class Jena1583_UtilCompare {
    public static void main(String... args) {
        BigDecimal decimal1 = new BigDecimal("1.5");
        BigDecimal decimal2 = new BigDecimal("1.75");
//        System.out.println(decimal1.intValue());
//        System.out.println(decimal1.doubleValue());
        
        Literal literal1 = ResourceFactory.createTypedLiteral(decimal1);
        Literal literal2 = ResourceFactory.createTypedLiteral(decimal2);

        int x = Util2.compareNumbers(literal1.asNode(), literal2.asNode());
        System.out.println(x);
        try {
            int x1 = NodeValue.compare(NodeValue.makeNode(literal1.asNode()), NodeValue.makeNode(literal2.asNode()));
            if ( x1 == Expr.CMP_INDETERMINATE ) 
                throw new ExprNotComparableException("Indeterminate"); 
            System.out.println(x1);
        } catch (ExprNotComparableException ex) {
            System.out.println("ExprNotComparableException");
        }
    }

    // OLD CODE
    public static int compareNumbersX(Node n1, Node n2) {
        if (n1.isLiteral() && n2.isLiteral()) {
            Object v1 = n1.getLiteralValue();
            Object v2 = n2.getLiteralValue();
            if (v1 instanceof Number && v2 instanceof Number) {
                if (v1 instanceof Float || v1 instanceof Double
                        || v2 instanceof Float || v2 instanceof Double) {
                            double d1 = ((Number)v1).doubleValue();
                            double d2 = ((Number)v2).doubleValue();
                            return (d1 < d2) ? -1 : ( (d1 == d2) ? 0 : +1 );
                } else {
                    long l1 = ((Number)v1).longValue();
                    long l2 = ((Number)v2).longValue();
                    return (l1 < l2) ? -1 : ( (l1 == l2) ? 0 : +1 );
                }
            }
        }
        throw new ClassCastException("Non-numeric literal in compareNumbers");
    }
}
