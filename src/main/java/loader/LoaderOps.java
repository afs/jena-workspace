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

package loader;

import org.apache.jena.dboe.base.file.BinaryDataFile;
import org.apache.jena.dboe.index.Index;
import org.apache.jena.dboe.index.RangeIndex;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.data.TransBinaryDataFile;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetable.NodeTableTRDF;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;

public class LoaderOps {
    public static TransBinaryDataFile ntDataFile(NodeTable nt) {
        NodeTableTRDF ntt = (NodeTableTRDF)(nt.baseNodeTable());
        BinaryDataFile bdf = ntt.getData();
        TransBinaryDataFile tbdf = (TransBinaryDataFile)bdf;
        return tbdf;
    }
    
    public static BPlusTree ntBPTree(NodeTable nt) {
        NodeTableTRDF ntt = (NodeTableTRDF)(nt.baseNodeTable());
        Index idx = ntt.getIndex();
        return (BPlusTree)idx; 
    }
    
    public static BPlusTree idxBTree(TupleIndex idx) {
        TupleIndexRecord idxr = (TupleIndexRecord)idx;
        RangeIndex rIndex = idxr.getRangeIndex();
        BPlusTree bpt = (BPlusTree)rIndex;
        return bpt;
    }

}
