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

package transdsg;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.sparql.core.DatasetChangesCapture ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.QuadAction ;

/** Play a chnag elog */
public class DatasetGraphPlayer
{
    enum Direction { FORWARDS, BACKWARDS }
    
    /**
     * @see DatasetChangesCapture
     */
    public static void play(List<Pair<QuadAction, Quad>> actions, DatasetGraph dsg, Direction direction)
    {
        Iterator<Pair<QuadAction, Quad>> iter = 
            (Direction.BACKWARDS == direction) 
            ? new ListIteratorReverse<Pair<QuadAction, Quad>>(actions.listIterator(actions.size())) 
            : actions.listIterator() ;

        loop: 
        while ( iter.hasNext() ) {
            Pair<QuadAction, Quad> p = iter.next() ;
            QuadAction action = p.getLeft() ;
            Quad quad = p.getRight() ;
            
            boolean addOp = true ;
            switch (action) {
                case ADD :
                    break ;
                case DELETE :
                    addOp = false ; 
                    break ;
                case NO_ADD :
                case NO_DELETE :
                default :
                    continue loop ;
            }
            
            if ( direction == Direction.BACKWARDS )
                addOp = ! addOp ;
            
            if ( addOp )
                dsg.add(quad) ;
            else
                dsg.delete(quad) ;
        }
    }
}

