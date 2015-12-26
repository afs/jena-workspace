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

package dsg.test;

import java.util.ArrayList ;
import java.util.List ;
import java.util.function.Supplier ;

import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

@RunWith(Parameterized.class)
public class TS_DSG2 extends AbstractTestDSG {
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;
        
        // The Supplier must make a new thing each time.
        Supplier<DatasetGraph> source1 = () -> DatasetGraphFactory.create() ;
        Supplier<DatasetGraph> source2 = () -> DatasetGraphFactory.createTxnMem() ;
        Supplier<DatasetGraph> source3 = () -> ((DatasetGraphTransaction)TDBFactory.createDatasetGraph()).get();
        
        x.add(new Object[]{"General", source1}) ;
        x.add(new Object[]{"TxnMem", source2}) ;
        x.add(new Object[]{"TDB", source3}) ;
        
        return x ; 
    }

    private Supplier<DatasetGraph> source;
    
    public TS_DSG2(String name, Supplier<DatasetGraph> source) {
        super(source) ;
    }
   
}