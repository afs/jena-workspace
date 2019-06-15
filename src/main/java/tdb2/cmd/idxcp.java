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

package tdb2.cmd;

import java.util.Objects;

import jena.cmd.CmdException;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.loader.base.LoaderOps;
import org.apache.jena.tdb2.loader.base.MonitorOutput;
import org.apache.jena.tdb2.loader.base.ProgressMonitor;
import org.apache.jena.tdb2.loader.base.ProgressMonitorOutput;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.sys.TDBInternal;
import tdb2.cmdline.CmdTDB;

/** Copy and index to another, new index.
 */
public class idxcp extends CmdTDB {
    public static final int DataTickPoint   = 100_000;
    public static final int DataSuperTick   = 10;
    public static final int IndexTickPoint  = 1_000_000;
    public static final int IndexSuperTick  = 10;
    
    private static final MonitorOutput output = LoaderOps.outputToLog();

    private String srcIndexName = null;
    private String dstIndexName = null;
    
    private boolean showProgress = true;
    private boolean generateStats = true;
    
    public static void main(String... args) {
        CmdTDB.init();
        new idxcp(args).mainRun();
    }

    protected idxcp(String[] argv) {
        super(argv);
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();
        if ( super.getNumPositional() == 1) {
            dstIndexName = getPositionalArg(0);
            checkIsIndex(dstIndexName);
            if ( dstIndexName.length() == 3 )
                srcIndexName = "SPO";
            else if ( dstIndexName.length() == 4 )
                srcIndexName = "GSPO";
            else 
                throw new InternalErrorException("Index arg of length: "+dstIndexName.length());
            
        } else if ( super.getNumPositional() == 2) {
            srcIndexName = getPositionalArg(0);
            checkIsIndex(srcIndexName);
            dstIndexName = getPositionalArg(1);
            checkIsIndex(dstIndexName);
            if ( dstIndexName.length() != srcIndexName.length() )
                throw new CmdException("Indexes must be the same length");
        }
        else
            throw new CmdException("Usage: 1 or 2 index names");
        
    }

    // Imperfect
    private void checkIsIndex(String idxName) {
        if ( idxName.length() == 3 ) {
            if ( ! idxName.matches("[SPO]{3}") ) 
                throw new CmdException("Not recognized as an index: "+idxName);
        }
        if ( idxName.length() == 4 ) {
            if ( ! idxName.matches("[GSPO]{4}") ) 
                throw new CmdException("Not recognized as an index: "+idxName);
        }
        
        long n = idxName.chars().distinct().count();
        if ( n != idxName.length())
            throw new CmdException("Repeated column: "+idxName);
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " [--desc DATASET | --loc DIR] index1 index2";
    }

    @Override
    protected void exec() {
        String labelX = srcIndexName+"->"+dstIndexName;
        DatasetGraph dsg = getDatasetGraph();
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
//        StoreParams storeParams = dsgtdb.getStoreParams();
//        storeParams.getTripleIndexes();
//        storeParams.getQuadIndexes();
        // Check contains srcIndex and does not contain dstIndex.
        
        // Must exist
        TupleIndex srcIdx = getIndex(srcIndexName, dsgtdb);
        if ( srcIdx == null )
            throw new CmdException("Not an existing index: "+srcIndexName);
        // Must not exist
        TupleIndex dstIdx = getIndex(dstIndexName, dsgtdb);
        if ( dstIdx != null )
            throw new CmdException("Existing index: "+dstIndexName);
        TupleIndex[] dstIndexes = { dstIdx };
        String label = srcIndexName+"->"+dstIndexName;
        
        System.err.println("Not upgraded to new TDB2");
//        TDB2StorageBuilder        
//        //TDBBuilder builder = TDBBuilder.create(dsgtdb.getLocation());
//        RecordFactory recordFactory = new RecordFactory(dstIndexName.length(),0);
//        RangeIndex index = builder.buildRangeIndex(recordFactory, dstIndexName);
//        
//        dstIdx = new TupleIndexRecord(dstIndexName.length(),
//                                      TupleMap.create(srcIndexName, dstIndexName),
//                                      dstIndexName, recordFactory,
//                                      index);
        
        ProgressMonitor monitor = ProgressMonitorOutput.create(output, label, IndexTickPoint, IndexSuperTick);
        LoaderOps.copyIndex(srcIdx.all(), dstIndexes, monitor);  
    }

    private TupleIndex getIndex(String indexName, DatasetGraphTDB dsgtdb) {
        if ( indexName.length() == 3 )
            return find(indexName, dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes());
        if ( indexName.length() == 4 )
            return find(indexName, dsgtdb.getQuadTable().getNodeTupleTable().getTupleTable().getIndexes());
        throw new InternalErrorException("Bad length: "+indexName);
    }

    private TupleIndex find(String indexName, TupleIndex[] indexes) {
        for ( TupleIndex idx : indexes ) {
            if ( Objects.equals(indexName, idx.getName()) )
                return idx;
        }
        return null;
    }    
}
