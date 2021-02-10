/**
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

package tdb;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.TransformCopy ;
import org.apache.jena.sparql.algebra.Transformer ;
import org.apache.jena.sparql.algebra.op.OpFilter ;
import org.apache.jena.sparql.algebra.optimize.TransformFilterEquality ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.* ;
import org.apache.jena.tdb.store.NodeId ;

/** This transform is like {@linkplain TransformFilterEquality} but it is 
 * value based and more aggressive.
 * It only work on BGPs+Filters.
 * It works for TDB where terms have been canonicalized (integers, decimals, date, dateTimes).
 */
public class TransformFilterSameValue extends TransformCopy {
    
    public static void main(String ...a) {
        String qs = "SELECT * { ?s ?p ?o . "
            + "FILTER(?o1 = 'foo')"
            + "FILTER(?o2 = 123)"
            + "FILTER(?o3 = 'foo'@en)"
            + "FILTER(?o4 = 123e0)"
            + "}" ;
        Query query = QueryFactory.create(qs) ;
        Op op  = Algebra.compile(query) ;
        
        Op op1 = Transformer.transform(new TransformFilterSameValue(), op) ;
        
        
//        Op op1 = Algebra.optimize(op) ;
//        System.out.println(op1) ;
    }
    
    public TransformFilterSameValue() {}

    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
        Op op = apply(opFilter.getExprs(), subOp) ;
        if ( op == null )
            return super.transform(opFilter, subOp) ;
        return op ;
    }

    private Op apply(ExprList exprs, Op subOp) {
        //return null ;
        for ( Expr e : exprs.getList() ) {
            Pair<Var, NodeValue> p = preprocess(e) ;
            System.out.println(p) ;
        }
        return null ;
    }
    
    // From TransformFilterEquality
    private static Pair<Var, NodeValue> preprocess(Expr e) {
        if (!(e instanceof E_Equals) && !(e instanceof E_SameTerm))
            return null;

        ExprFunction2 eq = (ExprFunction2) e;
        Expr left = eq.getArg1();
        Expr right = eq.getArg2();

        Var var = null;
        NodeValue constant = null;

        if (left.isVariable() && right.isConstant()) {
            var = left.asVar();
            constant = right.getConstant();
        } else if (right.isVariable() && left.isConstant()) {
            var = right.asVar();
            constant = left.getConstant();
        }

        if (var == null || constant == null)
            return null;

        Node n = constant.getNode() ;

        if ( ! n.isLiteral() )
            // URI or blank node
            return Pair.create(var, constant);
        
        // General
        
        if ( Util.isLangString(n) || Util.isSimpleString(n) ) {
            return Pair.create(var, constant);
        }
        
        // TDB.  Nodes that are inline are canonical constants.
        
        if ( NodeId.inline(constant.getNode()) != null ) {
            return Pair.create(var, constant);
        }

        // No
        return null ;
    }
}
