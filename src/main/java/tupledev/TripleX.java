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

package tupledev;

import java.util.Objects ;

import org.apache.jena.graph.Node ;
import tuple.Tuple ;
import tuple.Tuple3 ;

/** Dummy version of Triple */
public final class TripleX  implements Tuple<Node> // Only so alternative designs elsewhere can be experimented with 
{
    private final Node subject;
    private final Node predicate;
    private final Node object;
    
    public TripleX(Node s, Node p, Node o) {
        subject = Objects.requireNonNull(s) ;
        predicate = Objects.requireNonNull(p) ;
        object = Objects.requireNonNull(o) ;
    }

    //---- Tuple
    @Override
    public Node get(int i) {
        switch (i) {
            case 0: return getSubject() ;
            case 1: return getPredicate() ;
            case 2: return getObject() ;
            default:
                throw new IndexOutOfBoundsException("index = "+i) ; 
        }
    }

    @Override
    public int len() {
        return 3 ;
    }
    //---- Tuple

    public Node getSubject()    { return subject ; }
    public Node getPredicate()  { return predicate ; }
    public Node getObject()     { return object ; }

    // Must agree with Tuple3.
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( !super.equals(obj) )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        TripleX other = (TripleX)obj;
        if ( subject == null ) {
            if ( other.subject != null )
                return false;
        } else if ( !subject.equals(other.subject) )
            return false;
        if ( predicate == null ) {
            if ( other.predicate != null )
                return false;
        } else if ( !predicate.equals(other.predicate) )
            return false;
        if ( object == null ) {
            if ( other.object != null )
                return false;
        } else if ( !object.equals(other.object) )
            return false;
        return true;
    }
}
