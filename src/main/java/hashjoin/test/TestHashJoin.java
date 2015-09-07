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

package hashjoin.test;

import java.util.Iterator ;

import hashjoin.HashJoin ;
import hashjoin.JoinKey ;
import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;

public class TestHashJoin extends AbstractTestJoin {

    @Override
    public QueryIterator join(JoinKey joinKey, Table left, Table right) {
        Iterator<Binding> data = HashJoin.hashJoin(joinKey, left.iterator(null), right.iterator(null), null) ;
        QueryIterator qIter = new QueryIterPlainWrapper(data) ;
        return qIter ;
    }
}