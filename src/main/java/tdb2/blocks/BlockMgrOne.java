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

package tdb2.blocks;

import java.util.UUID;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.dboe.base.block.Block;
import org.apache.jena.dboe.base.block.BlockMgr;
import org.apache.jena.dboe.base.block.BlockMgrFactory;
import org.apache.jena.dboe.base.file.BufferChannel;
import org.apache.jena.dboe.base.file.BufferChannelFile;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeFactory;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeParams;
import org.apache.jena.dboe.transaction.Transactional;
import org.apache.jena.dboe.transaction.TransactionalFactory;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.query.TxnType;
import org.apache.jena.system.Txn;

/** A BlockMgr for a single file used for multiple BlockMgrs. */
public class BlockMgrOne  {
    // State management needs work. Multiple state records per BufferChannel.
    // In-memory structure backed up by disk, variable length records.
    //   ComponentIds become names. "SPO" etc.
    //   ([len]name, [len]state bytes)

    // Free chain management? Complicated by MVCC but can keep a note of freed blocks (and
    // copy-written blocks) then one-way linked list then persist with .bpt file.
    // 
    // Keep a list of when a block became free (the W epoch) - immediately persist the free chain as part of W-commit.  
    //    In-memory: As R terminate, release up to that epoch.
    //    Restart: last commit = new generations.
    
    // Builder style BPT create?
    
    // BPlusTreeFactory.attach needs work. 
    // Starting a tree at root != 0;
    // Multiple state in one other file.
    // Need "tree -> offset record"
    // Or fixed root-root block (different format)
    //   ComponentId - names
    

    public static void main(String...a) {
        // See BlockAccessOne
        String DIR = "OneDB";
        int blkSize = 128;
        FileOps.ensureDir(DIR);
        FileOps.clearAll(DIR);
        
        // TransBlob->Multiple bptState
        
        BlockMgr blockMgr = BlockMgrFactory.createMMapFile(DIR+"/all.blk", blkSize);
        
        // Empty - format.
        if ( blockMgr.isEmpty() ) {
            Block blk = blockMgr.allocate(blkSize);
            if ( blk.getId() != 0 )
                System.err.println("First block !=0 : ==> "+blk.getId());
            // Format block.
            blockMgr.write(blk);
            blockMgr.sync();
        } else {
            System.err.println("NOT EMPTY");
            System.exit(1);
        }
        
        ComponentId cid = ComponentId.create(UUID.randomUUID(), StrUtils.asUTF8bytes("DATA"));
        RecordFactory recordFactory = new RecordFactory(8, 0);
        
        int order = BPlusTreeParams.calcOrder(blkSize, recordFactory.recordLength());
        BPlusTreeParams params = new BPlusTreeParams(order, recordFactory);
        BufferChannel bptState = BufferChannelFile.create(DIR+"/all.bpt");
        // Same blockMgr.
        BPlusTree bpt = BPlusTreeFactory.create(cid, params, bptState, blockMgr, blockMgr) ;
        
        Transactional thing = TransactionalFactory.createTransactional(Location.create(DIR), bpt);
        
        Txn.exec(thing, TxnType.WRITE, ()->{
            for ( int i = 0 ; i < 10 ; i++ ) {
                byte[] b = new byte[recordFactory.keyLength()];
                Bytes.setLong(i, b);
                Record r = recordFactory.create(b);
                bpt.insert(r);
            }
        });
        
        }
}
