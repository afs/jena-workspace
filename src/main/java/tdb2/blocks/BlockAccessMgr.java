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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.dboe.base.block.Block;
import org.apache.jena.dboe.base.file.BlockAccess;
import org.apache.jena.dboe.base.file.BlockAccessMapped;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.thrift.RiotThriftException;
import org.apache.jena.riot.thrift.TRDF;
import org.apache.jena.riot.thrift.ThriftConvert;
import org.apache.jena.riot.thrift.wire.RDF_Term;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TByteBuffer;
import org.apache.thrift.transport.TTransport;

/** A manager for BlockAccess classes for a single file. */
public class BlockAccessMgr { //implements BlockAccess { // BlockaccessWrapper
    // BlockAccessMgr

    private final BlockAccessMapped storage;
    private final int blkSize = 8*1024;

    // Temp - memory index.
    private AtomicLong entryLocation = new AtomicLong(0);
    private static class IndexEntry {
        String entry ; int rootOffset;
        IndexEntry(String entry , int rootOffset) {
            this.entry = entry;
            this.rootOffset = rootOffset;
        }
    }

    private ConcurrentMap<String, BlockAccessShared> index = new ConcurrentHashMap<>();

    public BlockAccessMgr() {
        storage = new BlockAccessMapped("all.blk", 0);
        if ( storage.isEmpty() ) {
            createIndex();
        } else {
            readIndex();
        }
        // The roots file.
        // Lock file.
    }

    private void readIndex() {
        Block block = storage.read(0);
        // The block is formatted in RDF Thrift as "string", int, "string", int, "" , 0

        try {
            TTransport transport = new TByteBuffer(block.getByteBuffer());
            TProtocol protocol = TRDF.protocol(transport);
            for(;;) {
                try {
                    RDF_Term termName = new RDF_Term() ;
                    RDF_Term termId = new RDF_Term() ;
                    termName.read(protocol) ;
                    termId.read(protocol) ;
                    Node nameNode = ThriftConvert.convert(termName) ;
                    Node idNode = ThriftConvert.convert(termId) ;
                    long x = ((Number)idNode.getLiteralValue()).longValue();
                    String name = nameNode.getLiteralLexicalForm();
                    System.out.printf("%s [%d]\n", name, x);
                    if ( x == 0 )
                        // End.
                        break;
                } catch (TException e) {
                    e.printStackTrace();
                }
            }
        } catch (TException ex) {
            throw new RiotThriftException(ex);
        }
    }

    private void createIndex() {
        Block block = storage.allocate(blkSize);
        writeIndexes();
    }

    private void writeIndexes() {
        Block block = storage.read(0);
        try {
            TTransport transport = new TByteBuffer(block.getByteBuffer());
            TProtocol protocol = TRDF.protocol(transport);
            index.forEach((n,id)->writeIndex(protocol, n, id.virtualZero));
            // End marker.
            writeIndex(protocol, "", 0);
        } catch (TException ex) {
            throw new RiotThriftException(ex);
        }
    }

    public BlockAccess create(String name) {
        return
            index.computeIfAbsent(name, (n)->{
                long x = entryLocation.incrementAndGet();
                Block blockZero = storage.allocate(blkSize);
                IndexEntry idx = new IndexEntry(name, (int)x);
                return new BlockAccessShared(name, storage, blockZero.getId());
            });
    }

    private void writeIndex(TProtocol protocol, String name, long rootId) {
        try {
            // Write indexes.
            RDF_Term termName = new RDF_Term() ;
            RDF_Term termId = new RDF_Term() ;
            ThriftConvert.toThrift(NodeFactory.createLiteral(name), termName);
            ThriftConvert.toThrift(NodeFactory.createLiteral(Long.toString(rootId), XSDDatatype.XSDinteger), termId);
            termName.write(protocol);
            termId.write(protocol);
        } catch (TException e) {
            e.printStackTrace();
        }
    }
}
