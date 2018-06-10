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

package tuple.mtuple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.atlas.lib.tuple.Tuple3;
import org.apache.jena.atlas.lib.tuple.TupleFactory;

class MTuple3<X> extends MTupleBase<X> {
    protected X x1 = null; 
    protected X x2 = null;
    protected X x3 = null;

    public MTuple3(X x1, X x2, X x3) {
        this.x1 = x1 ;
        this.x2 = x2 ;
        this.x3 = x3 ;
    }

    @Override
    public final X get(int i) {
        switch (i) {
            case 0: return x1 ;
            case 1: return x2 ;
            case 2: return x3 ;
        }
        throw new IndexOutOfBoundsException() ;
    }
    
    @Override
    public void set(int i, X n) {
        switch (i) {
            case 0: x1 = n ; return;
            case 1: x2 = n ; return;
            case 2: x3 = n ; return;
        }
        throw new IndexOutOfBoundsException() ;
    }
    
    @Override
    public final int len() {
        return 3 ;
    }
    
    @Override
    public <Y> MTuple<Y> map(Function<X, Y> function) {
        return new MTuple3<Y>(
            function.apply(x1),
            function.apply(x2),
            function.apply(x3));
    }

    public Tuple3<X> tuple() { 
        return TupleFactory.create3(x1, x2, x3); 
    }

    @Override
    protected X[] asArray() {
        @SuppressWarnings("unchecked")
        X[] a =  (X[])new Object[3];
        a[0] = x1;
        a[1] = x2;
        a[2] = x3;
        return a;
    }

    @Override
    public List<X> asList() {
        List<X> x = new ArrayList<>(3);
        x.set(0, x1);
        x.set(1, x2);
        x.set(2, x3);
        return x;
    } 
}