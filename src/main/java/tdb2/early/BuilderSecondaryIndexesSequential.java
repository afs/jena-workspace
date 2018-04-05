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

package tdb2.early;

import org.apache.jena.atlas.lib.ProgressMonitor;
import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.tdb2.store.tupletable.TupleIndex ;
import tdb2.BulkLoader;
import tdb2.tools.Tools;

public class BuilderSecondaryIndexesSequential implements BuilderSecondaryIndexes
{
    public BuilderSecondaryIndexesSequential() {} 
    
    // Create each secondary indexes, doing one at a time.
    @Override
    public void createSecondaryIndexes(TupleIndex primaryIndex, TupleIndex[] secondaryIndexes)
    {
        Timer timer = new Timer() ;
        timer.startTimer() ;
        boolean printTiming = true;
        for ( TupleIndex index : secondaryIndexes )
        {
            if ( index != null ) {
                ProgressMonitor monitor = ProgressMonitor.create(BulkLoader.LOG, index.getName(), 
                    BulkLoader.IndexTickPoint, BulkLoader.IndexSuperTick);
                monitor.startMessage();
                monitor.start();

                long time1 = timer.readTimer() ;
                Tools.copyIndex(primaryIndex.all(), new TupleIndex[]{index}, index.getMapping().getLabel(), monitor) ;
                long time2 = timer.readTimer() ;
                monitor.finish();
                monitor.finishMessage();
                if ( printTiming )
                    FmtLog.info(BulkLoader.LOG,"Time for %s indexing: %.2fs", index.getName(), (time2-time1)/1000.0) ;
            }  
        }   
    }
}
