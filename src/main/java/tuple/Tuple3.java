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

package tuple;

/**
 * A tuple of 3 nodes.
 */
public class Tuple3<X> implements Tuple<X> {
    protected final X x1 ; 
    protected final X x2 ;
    protected final X x3 ;
    
    public Tuple3(X x1, X x2, X x3) {
        this.x1 = x1 ;
        this.x2 = x2 ;
        this.x3 = x3 ;
    }
    
    @Override
    public X get(int i) {
        switch (i) {
            case 0: return x1 ;
            case 1: return x2 ;
            case 2: return x3 ;
        }
        throw new IndexOutOfBoundsException() ;
    }
    
    @Override public String toString() {
        return "[ "+x1+", "+x2+", "+x3+" ]" ;
    }

    @Override
    public int len() {
        return 3 ;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((x1 == null) ? 0 : x1.hashCode());
        result = prime * result + ((x2 == null) ? 0 : x2.hashCode());
        result = prime * result + ((x3 == null) ? 0 : x3.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Tuple3<?> other = (Tuple3<?>)obj;
        if ( x1 == null ) {
            if ( other.x1 != null )
                return false;
        } else if ( !x1.equals(other.x1) )
            return false;
        if ( x2 == null ) {
            if ( other.x2 != null )
                return false;
        } else if ( !x2.equals(other.x2) )
            return false;
        if ( x3 == null ) {
            if ( other.x3 != null )
                return false;
        } else if ( !x3.equals(other.x3) )
            return false;
        return true;
    }
}