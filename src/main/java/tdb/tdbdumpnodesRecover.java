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

package tdb;

import static org.apache.jena.tdb.sys.SystemTDB.SizeOfInt;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.tdb.base.file.BufferChannel;
import org.apache.jena.tdb.base.file.BufferChannelFile;
import org.apache.jena.tdb.base.file.FileException;
import org.apache.jena.tdb.store.nodetable.Nodec;
import org.apache.jena.tdb.store.nodetable.NodecSSE;

/** Read the node binary data file and print its contents */
public class tdbdumpnodesRecover {

    public static void main(String[] args) throws Exception {
        String FN0 = "nodes.dat";
        String FN = "target.dat";
        

        Path file0 = Paths.get(FN0);
        Path file = Paths.get(FN);
        
        Files.copy(file0, file, StandardCopyOption.REPLACE_EXISTING);
        
        long size = Files.size(file);
        
        Nodec nodec = new NodecSSE();
        BufferChannel chan = BufferChannelFile.create(FN);
        ObjectIterator iter = new ObjectIterator(chan, 0, size);
        boolean resyncing = false;
        long locn = 0;
        long locn1 = 0;
        
        PrintStream out = System.out;

        if ( true ) {
            OutputStream outs = IO.openOutputFile("Recovered.txt");
            outs = new BufferedOutputStream(outs);
            PrintStream out2 = new PrintStream(outs);
            out = out2;
        }
        out.printf("File length: %,d [0x%16X]\n", size, size);
        
        for ( ; iter.hasNext() ; ) {
//            Pair<Long, ByteBuffer> p = iter.next();
//            locn1 = p.getLeft();
//            ByteBuffer bb = p.getRight();
//            Node n = nodec.decode(bb, null);
//            out.printf("[0x%16X] %s\n",locn1, FmtUtils.stringForNode(n));

            try { 
                Pair<Long, ByteBuffer> p = iter.next();
                locn1 = p.getLeft();
                ByteBuffer bb = p.getRight();
                Node n = nodec.decode(bb, null);
                if ( resyncing ) {
                    out.printf("[0x%16X] **** Resync\n",locn);
                    resyncing = false;
                }
                locn = locn1;
                out.printf("[0x%16X] %s\n",locn, FmtUtils.stringForNode(n));
            } catch (Exception ex) {
                out.flush();
                //out.printf("**** FAILED locn=[0x%16X](%d) last=[0x%16X](%d)\n",locn, locn, locn1, locn1);
                out.printf("**** FAILED locn=[0x%16X]\n", locn);
                out.println("'"+ex.getMessage()+"'");
                // Jump.
                resyncing = true;
                long locn2 = locn;
                // try to decode.
                boolean recovered = false;
                for (int i = 0 ; i < 1000 ; i++ ) {
                    locn2++;
                    chan.position(locn2);

                    try { 
                        Pair<Long, ByteBuffer> p = iter.next();
                        long locn3 = p.getLeft();
                        ByteBuffer bb = p.getRight();
                        Node n = nodec.decode(bb, null);
                        out.printf("**** RESYNC locn=[0x%16X]\n",locn3);
                        recovered = true;
                        locn=locn3;
                        break;
                    } catch (Exception ex2) {}
                }
                if ( ! recovered ) {
                    out.println("**** No recovery");
                    System.exit(0);
                }
            }
        }
    }
        
    public static ByteBuffer read(BufferChannel file, long loc,long filesize) {
        // No - it's in the underlying file storage.
        ByteBuffer lengthBuffer = ByteBuffer.allocate(SizeOfInt);

        lengthBuffer.clear();
        int x = file.read(lengthBuffer, loc);
        if ( x != 4 ) {
            String msg = "read[" + file.getLabel() + "](" + loc + ")[filesize=" + filesize + "]"
                + "[file.size()=" + file.size() + "]: Failed to read the length : got " + x + " bytes";
            lengthBuffer.clear();
            int x1 = file.read(lengthBuffer, loc);
            throw new FileException(msg);
        }
        int len = lengthBuffer.getInt(0);
        // Sanity check.
        if ( len > filesize - (loc + SizeOfInt) ) {
            String msg = "ObjectFileStorage.read[" + file.getLabel() + "](" + loc + ")[filesize=" + filesize + "][file.size()="
                + file.size() + "]: Impossibly large object : " + len + " bytes > filesize-(loc+SizeOfInt)="
                + (filesize - (loc + SizeOfInt));
            throw new FileException(msg);
        }

        ByteBuffer bb = ByteBuffer.allocate(len);
        if ( len == 0 )
            // Zero bytes.
            return bb;
        x = file.read(bb, loc + SizeOfInt);
        bb.flip();
        if ( x != len )
            throw new FileException("read: Failed to read the object (" + len + " bytes) : got " + x + " bytes");
        return bb;
    }
    
    static class ObjectIterator implements Iterator<Pair<Long, ByteBuffer>> {
        final private long start;
        final private long finish;
        private long       current;
        private BufferChannel file;

        public ObjectIterator(BufferChannel file, long start, long finish) {
            this.file = file;
            this.start = start;
            this.finish = finish;
            this.current = start;
        }

        @Override
        public boolean hasNext() {
            return (current < finish);
        }

        @Override
        public Pair<Long, ByteBuffer> next() {
            // read, but reserving the file position.
            long x = current;
            long filePosn = file.position();
            ByteBuffer bb = read(file, current, finish);
            file.position(filePosn);
            current = current + bb.limit() + 4;
            return new Pair<>(x, bb);
        }

        
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}



//while (true) {
//Iterator<Pair<Long, ByteBuffer>> iter = obj.all();
//for ( ; iter.hasNext() ; ) {
////Pair<Long, ByteBuffer> p = iter.next();
////long locn1 = p.getLeft();
////ByteBuffer bb = p.getRight();
////Node n = nodec.decode(bb, null);
////System.out.printf("[0x%16X] %s\n",locn1, FmtUtils.stringForNode(n));
////if ( locn1 < locn ) {
////    System.out.flush();
////    System.err.printf("[0x%16X] FAILED\n",locn1);
////    System.exit(0);
////}
////locn = locn1;
//
//  
//  try { 
//      Pair<Long, ByteBuffer> p = iter.next();
//      locn1 = p.getLeft();
//      ByteBuffer bb = p.getRight();
//      Node n = nodec.decode(bb, null);
//      if ( resyncing ) {
//          System.out.printf("[0x%16X] Resync\n",locn);
//          resyncing = false;
//      }
//      locn = locn1;
//      System.out.printf("[0x%16X] %s\n",locn, FmtUtils.stringForNode(n));
//  } catch (Exception ex) {
//      //      System.out.flush();
//      System.err.printf("FAILED [0x%16X] [0x%16X]\n",locn, locn1);
//      // Jump.
//      long locn2 = locn+100;
//      obj.reposition(locn2);
//      resyncing = true;
//      // try to decode.
//      for (int i = 0 ; i < 100 ; i++ ) {
//          Pair<Long, ByteBuffer> p = iter.next();
//          locn1 = p.getLeft();
//          ByteBuffer bb = p.getRight();
//          Node n = nodec.decode(bb, null);
//      }
//      
//      
//      
//  }
//}
//}

