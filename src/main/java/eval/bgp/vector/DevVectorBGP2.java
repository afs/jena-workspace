/*
Vector * Licensed to the Apache Software Foundation (ASF) under one
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

package eval.bgp.vector;

import java.util.*;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.sse.SSE;

public class DevVectorBGP2 {
    public static void main(String...a) {
        BasicPattern bgp = SSE.parseBGP("(bgp (?s :p ?o) ( ?o :q ?z) )");
        List<Var> x = new ArrayList<>();
        //Set<Var> x = new HashSet<>();
        /*VarUtils.*/addVars(x, bgp);
        System.out.println(x);

        // Solver.
        // StageGeneratorGeneric : QueryIterBlockTriples

    }

    interface Binder { void bind(Var var, Node node); }
    interface Getter { Node get(Var var); }


    public static void solver(BasicPattern bgp, Graph data) {
        List<Var> x = new ArrayList<>();
        //Set<Var> x = new HashSet<>();
        /*VarUtils.*/addVars(x, bgp);
        Node[] result = new Node[x.size()];
        for ( Triple tp : bgp ) {

        }

    }

    public static Iterator<?> solverOne(Triple pattern, Graph data, Map<Var, Integer> map, Node[] result) {
        return null;

    }

    // .hash, .equals.
    //Binding0
    //Binding1
    //Binding2
    //Binding3
    //Binding4
    //BindingN


    public static class Binding2 implements Binding {
        private final Var var1;
        private final Var var2;
        private final Node val1;
        private final Node val2;

        public Binding2(Var var1, Node val1, Var var2, Node val2) {
            this.var1 = var1;
            this.val1 = val1;
            this.var2 = var2;
            this.val2 = val2;
        }

        @Override
        public Iterator<Var> vars() {
            return Arrays.asList(var1, var2).iterator();
        }

        @Override
        public boolean contains(Var var) {
            if ( var1.equals(var) ) return true;
            if ( var2.equals(var) ) return true;
            return false;
        }

        @Override
        public Node get(Var var) {
            if ( var1.equals(var) ) return val1;
            if ( var2.equals(var) ) return val2;
            return null;
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    public static class BindingBuilder {
        private final int N;



        BindingBuilder(int N) { this.N = N ;}

        BindingBuilder add(Var var, Node value) {

            return this;
        }
    }

    public class BindingN implements Binding {

        @Override
        public Iterator<Var> vars() {
            return null;
        }

        @Override
        public boolean contains(Var var) {
            return false;
        }

        @Override
        public Node get(Var var) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        } }

    public static void addVarsTriples(Collection<Var> acc, Collection<Triple> triples) {
        for ( Triple triple : triples )
            addVarsFromTriple(acc, triple);
    }

    public static void addVars(Collection<Var> acc, BasicPattern pattern) {
        addVarsTriples(acc, pattern.getList());
    }

    public static void addVarsFromTriple(Collection<Var> acc, Triple triple) {
        addVar(acc, triple.getSubject());
        addVar(acc, triple.getPredicate());
        addVar(acc, triple.getObject());
    }




    private static void addVar(Collection<Var> acc, Node n) {
        if ( n == null )
            return;

        if ( n.isVariable() ) {
            // For non-set
            if ( ! acc.contains(n) )
                acc.add(Var.alloc(n));
        }
    }
}
