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

package eval;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.util.Context;

public class TransformRDFStar extends TransformCopy {
    // Before quadding.

    @Override
    public Op transform(OpBGP opBGP) {
        // Two pass.
        boolean found = false;
        for ( Triple t : opBGP.getPattern() ) {
            if ( RXT.tripleHasNodeTriple(t) ) {
                found = true;
                break;
            }
        }
        if ( !found )
            return opBGP;

        // TEMP
        Context cxt = new Context();

        OpSequence seq = OpSequence.create();;
        BasicPattern current = new BasicPattern();
        for ( Triple t : opBGP.getPattern() ) {
            if ( ! RXT.tripleHasNodeTriple(t) ) {
                current.add(t);
                continue;
            }
            if ( ! current.isEmpty() ) {
                seq.add(new OpBGP(current));
                current = new BasicPattern();
            }

            RXT.rdfStarTriple(seq::add, t, cxt);
        }
        if ( ! current.isEmpty() )
            seq.add(new OpBGP(current));
        return seq;
    }
}

