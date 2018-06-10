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

package tuple;

import java.util.function.Function ;

import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;

public class TupleOps {
    public static <X,Y> Tuple<Y> map(Tuple<X> tuple, Function<X,Y> func) {
        switch(tuple.len()) {
            case 0: return TupleFactory.create0() ; 
            case 1: return TupleFactory.tuple(func.apply(tuple.get(0))) ;
            case 2: return TupleFactory.tuple(func.apply(tuple.get(0)), func.apply(tuple.get(1))) ;  
            case 3: return TupleFactory.tuple(func.apply(tuple.get(0)), func.apply(tuple.get(1)), func.apply(tuple.get(2))) ;  
            case 4: return TupleFactory.tuple(func.apply(tuple.get(0)), func.apply(tuple.get(1)), func.apply(tuple.get(2)), func.apply(tuple.get(3))) ;  
        }
        @SuppressWarnings("unchecked")
        // Works because we pass it to asTuple.
        Y[] elts = (Y[])new Object[tuple.len()] ;
        for ( int i = 0 ; i < tuple.len() ; i++ )
            elts[i] = func.apply(tuple.get(i)) ;
        return TupleFactory.asTuple(elts) ; 
    }
}
