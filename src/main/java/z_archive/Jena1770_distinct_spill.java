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

package z_archive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.apache.jena.atlas.data.DistinctDataBag;
import org.apache.jena.atlas.data.ThresholdPolicyCount;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.*;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.sparql.system.SerializationFactoryFinder;
import org.apache.jena.sparql.util.NodeUtils;
import org.junit.Test;

public class Jena1770_distinct_spill {

    public static void main(String ...a) {
        // Don't write based on VARS.

        // Simple fix - reset VARS when different.
        // better? - extend vars - preserves write order.

        // BindingOutputStream - snoops one row for bindings.
        // BIOInput **
        // BindingOutputStream has a "vars". Remove **

        testOptionalVariables();
        System.out.println("DONE");
    }

    @Test
    public void testOptionalVariablesOriginal() {
        // Setup a situation where the second binding in a spill file binds more
        // variables than the first binding
        Binding binding1 = BindingFactory.binding(Var.alloc("1"), NodeFactory.createLiteral("A"));

        Binding binding2 = BindingFactory.binding(Var.alloc("1"), NodeFactory.createLiteral("A"),
                                                  Var.alloc("2"), NodeFactory.createLiteral("B"));

        List<Binding> undistinct = Arrays.asList(binding1, binding2, binding1);
        List<Binding> control = Iter.toList(Iter.distinct(undistinct.iterator()));
        List<Binding> distinct = new ArrayList<>();

        DistinctDataBag<Binding> db = new DistinctDataBag<>(new ThresholdPolicyCount<Binding>(2),
                                                            SerializationFactoryFinder.bindingSerializationFactory(),
                                                            new BindingComparator(new ArrayList<SortCondition>()));
        try {
            db.addAll(undistinct);
            Iterator<Binding> iter = db.iterator();
            while (iter.hasNext()) {
                distinct.add(iter.next());
            }
            Iter.close(iter);
        }
        finally {
            db.close();
        }

        assertEquals(control.size(), distinct.size());
        assertTrue(ResultSetCompare.equalsByTest(control, distinct, NodeUtils.sameRdfTerm));
    }

    public static void testOptionalVariables() {
        // Setup a situation where the second binding in a spill file binds more
        // variables than the first binding

        Binding binding1 = BindingFactory.binding(Var.alloc("1"), NodeFactory.createLiteral("A"));

        Binding binding2 = BindingFactory.binding(Var.alloc("1"), NodeFactory.createLiteral("A"),
                                                  Var.alloc("2"), NodeFactory.createLiteral("B"));

        List<Binding> undistinct = Arrays.asList(binding1, binding2, binding1);
        List<Binding> control = Iter.toList(Iter.distinct(undistinct.iterator()));
        List<Binding> distinct = new ArrayList<>();

        List<SortCondition> sortConditions = Collections.emptyList();
        // Fails
        DistinctDataBag<Binding> db = new DistinctDataBag<>(new ThresholdPolicyCount<Binding>(2),
                                                            SerializationFactoryFinder.bindingSerializationFactory(),
                                                            new BindingComparator(sortConditions));
        try {
            db.addAll(undistinct);
            Iterator<Binding> iter = db.iterator();
            while (iter.hasNext()) {
                Binding b = iter.next();
                System.out.println("Binding: "+b);
                distinct.add(b);
            }
            Iter.close(iter);
        }
        finally {
            db.close();
        }

        assertEquals(control.size(), distinct.size());
        assertTrue(ResultSetCompare.equalsByTest(control, distinct, NodeUtils.sameRdfTerm));
    }


}
