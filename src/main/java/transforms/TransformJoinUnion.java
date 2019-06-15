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

package transforms;

import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.TransformCopy ;
import org.apache.jena.sparql.algebra.Transformer ;
import org.apache.jena.sparql.algebra.op.* ;

/** Rewrite join(A, union(B,C)) => union(join(A,B), join(A,C))
 * but only for certain A
 * tables
 * bind
 */
public class TransformJoinUnion extends TransformCopy {

    public TransformJoinUnion() {}
    
    @Override
    public Op transform(OpJoin opJoin, Op left, Op right) {
        if ( opJoin.getLeft() instanceof OpUnion ) {}
        
        if ( opJoin.getLeft() instanceof OpTable) {
            
        }

        if ( opJoin.getLeft() instanceof OpExtendAssign ) {
            Op1 opLeft = (OpExtendAssign)opJoin.getLeft() ;
            if ( opLeft.getSubOp() instanceof OpTable ) {
                OpTable table = (OpTable)opLeft.getSubOp() ;
                table.isJoinIdentity() ;
            }
        }

        
        if ( opJoin.getRight() instanceof OpUnion ) {
            OpUnion op_u = (OpUnion)opJoin.getRight() ;
            Op uLeft = OpJoin.create(opJoin.getLeft() , op_u.getLeft()) ; 
            Op uRight = OpJoin.create(opJoin.getLeft() , op_u.getRight()) ;
            return OpUnion.create(uLeft, uRight) ;
        }

        return super.transform(opJoin, left, right) ;
    }
    
    public static void main(String ...args) {
        exec("SELECT * { VALUES ?s { 1 2 } {?s1 ?p1 ?o1 } UNION { ?s2 ?p2 ?o2 } }") ;
        System.out.println("---------");
        exec("SELECT * { BIND(123 AS ?s)  {?s1 ?p1 ?o1 } UNION { ?s2 ?p2 ?o2 } }") ;
    }
    
    public static void exec(String qs) {
        Query query = QueryFactory.create(qs) ;
        Op x = Algebra.compile(query) ;
        Op z = Transformer.transform(new TransformJoinUnion(), x) ;
        System.out.println(x);
        System.out.println(z);
    }
}
