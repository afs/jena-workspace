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

package query.inject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.VarUtils;

/*
 * Special
 */
public class TransformInject extends TransformCopy {

    private final Set<Var> injectVars;
    private final Set<Node> varsAsNodes;
    private final Function<Var, Node> replacement;

    public TransformInject(Set<Var> injectVars, Function<Var, Node> replacement) {
        this.injectVars = injectVars;
        this.varsAsNodes = Set.copyOf(injectVars);
        this.replacement = replacement;
    }

    @Override
    public Op transform(OpBGP opBGP) {
        if ( injectVars.isEmpty())
            return opBGP;
        BasicPattern bp = opBGP.getPattern();
        List<Triple> triples = bp.getList();
        Set<Var> bgpVars = new LinkedHashSet<>();
        VarUtils.addVarsTriples(bgpVars, triples);
        Set<Var> x = SetUtils.intersection(bgpVars, injectVars);
        if ( x.isEmpty())
            return opBGP;
        VarExprList assigns = new VarExprList();
        BindingBuilder builder = BindingFactory.builder();
        for( Var var : x ) {
            Node value = replacement.apply(var);
            if ( value != null ) {
                builder.add(var, value);
                assigns.add(var, NodeValue.makeNode(replacement.apply(var)));
            }
        }

        if ( assigns.isEmpty() )
            return opBGP;

        Binding substitutions = builder.build();
        Op opExec = Substitute.substitute(opBGP, substitutions);
        opExec = OpAssign.create(opExec, assigns);
        return opExec;
    }

    public Op transform_v0(OpBGP opBGP) {
        BasicPattern bp = opBGP.getPattern();
        List<Triple> triples = bp.getList();
        Set<Var> bindingVars = new LinkedHashSet<>();

        for( Triple t : triples ) {
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();

            if ( varsAsNodes.contains(t.getSubject()) )
                bindingVars.add(Var.alloc(t.getSubject()));
            if ( varsAsNodes.contains(t.getPredicate()) )
                bindingVars.add(Var.alloc(t.getPredicate()));
            if ( varsAsNodes.contains(t.getObject()) )
                bindingVars.add(Var.alloc(t.getObject()));
            // Replace ?s with  BIND(:subject as ?s) BGP.
            // ?s :p :o => ?s:=
        }
        if ( bindingVars.isEmpty() )
            return opBGP;
        Op op = opBGP;
        // Function to get binding.
        VarExprList assigns = new VarExprList();
        for( Var var : bindingVars ) {
            Node value = replacement.apply(var);
            // Yuk
            op = Substitute.substitute(op, var, value);
            if ( value != null )
                assigns.add(var, NodeValue.makeNode(replacement.apply(var)));
        }
        // Reorder?
        op = OpAssign.create(op, assigns);
        return op;
    }
}
