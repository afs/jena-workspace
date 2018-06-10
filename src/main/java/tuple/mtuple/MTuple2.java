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

import org.apache.jena.atlas.lib.tuple.Tuple2;
import org.apache.jena.atlas.lib.tuple.TupleFactory;

class MTuple2<X> extends MTupleBase<X> {
    protected X x1 = null; 
    protected X x2 = null;

    public MTuple2(X x1, X x2) {
        this.x1 = x1 ;
        this.x2 = x2 ;
    }

    @Override
    public final X get(int i) {
        switch (i) {
            case 0: return x1 ;
            case 1: return x2 ;
        }
        throw new IndexOutOfBoundsException() ;
    }
    
    @Override
    public void set(int i, X n) {
        switch (i) {
            case 0: x1 = n ; return;
            case 1: x2 = n ; return;
        }
        throw new IndexOutOfBoundsException() ;
    }
    
    @Override
    public final int len() {
        return 2 ;
    }
    
    @Override
    public <Y> MTuple<Y> map(Function<X, Y> function) {
        return new MTuple2<Y>(
            function.apply(x1),
            function.apply(x2));
    }

    public Tuple2<X> tuple() { 
        return TupleFactory.create2(x1, x2); 
    }

    @Override
    protected X[] asArray() {
        @SuppressWarnings("unchecked")
        X[] a =  (X[])new Object[2];
        a[0] = x1;
        a[1] = x2;
        return a;
    }

    @Override
    public List<X> asList() {
        List<X> x = new ArrayList<>(2);
        x.set(0, x1);
        x.set(1, x2);
        return x;
    } 
}