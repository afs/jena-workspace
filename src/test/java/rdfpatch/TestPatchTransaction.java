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

package rdfpatch;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Test ;
import transdsg.DatasetGraphChangeLog ;

/** Tests directly of the replay-based transaction mechanism */ 
public class TestPatchTransaction extends BaseTest
{
    private static DatasetGraphChangeLog create(Quad...quads) {
        DatasetGraph dsg1 = DatasetGraphFactory.create() ;
        for ( Quad quad : quads )
            dsg1.add(quad) ;
        DatasetGraphChangeLog dsg = new  DatasetGraphChangeLog(dsg1) ;
        return dsg ;
    }
    
    private static Quad quad1 = SSE.parseQuad("(:g <s> <p> _:a)") ;

    @Test
    public void patch_01() {
        DatasetGraphChangeLog dsg = create() ;
        dsg.begin(ReadWrite.WRITE) ;
        try {
            dsg.commit() ;
        } finally {
            dsg.end() ;
        }
        dsg.begin(ReadWrite.READ) ;
        try {
            assertTrue(dsg.isEmpty()) ;
        } finally { dsg.end() ; }
    }

    @Test
    public void patch_02() {
        DatasetGraphChangeLog dsg = create() ;
        dsg.begin(ReadWrite.WRITE) ;
        try {
            dsg.abort() ;
        } finally {
            dsg.end() ;
        }
        dsg.begin(ReadWrite.READ) ;
        try {
            assertTrue(dsg.isEmpty()) ;
        } finally { dsg.end() ; }
    }

    @Test public void patch_add_commit() {
        DatasetGraphChangeLog dsg = create() ;
        dsg.begin(ReadWrite.WRITE);
        dsg.add(quad1) ;
        assertFalse(dsg.isEmpty()) ;
        dsg.commit() ;
        dsg.end();
        
        dsg.begin(ReadWrite.READ) ;
        try {
            assertFalse(dsg.isEmpty()) ;
            assertTrue(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }
    
    @Test public void patch_add_abort() {
        DatasetGraphChangeLog dsg = create() ;
        dsg.begin(ReadWrite.WRITE);
        dsg.add(quad1) ;
        assertFalse(dsg.isEmpty()) ;
        dsg.abort() ;
        dsg.end();

        dsg.begin(ReadWrite.READ) ;
        try {
            assertTrue(dsg.isEmpty()) ;
            assertFalse(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }
    
    @Test public void patch_add_add_commit() {
        DatasetGraphChangeLog dsg = create() ;
        dsg.begin(ReadWrite.WRITE);
        dsg.add(quad1) ;
        dsg.add(quad1) ;
        assertFalse(dsg.isEmpty()) ;
        dsg.commit() ;
        dsg.end();
        
        dsg.begin(ReadWrite.READ) ;
        try {
            assertFalse(dsg.isEmpty()) ;
            assertTrue(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }
    
    @Test public void patch_add_add_abort() {
        DatasetGraphChangeLog dsg = create() ;
        dsg.begin(ReadWrite.WRITE);
        dsg.add(quad1) ;
        dsg.add(quad1) ;
        assertFalse(dsg.isEmpty()) ;
        dsg.abort() ;
        dsg.end();

        dsg.begin(ReadWrite.READ) ;
        try {
            assertTrue(dsg.isEmpty()) ;
            assertFalse(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }
    

    @Test public void patch_add_delete_commit() {
        DatasetGraphChangeLog dsg = create() ;
        dsg.begin(ReadWrite.WRITE);
        dsg.add(quad1) ;
        dsg.delete(quad1) ;
        assertTrue(dsg.isEmpty()) ;
        dsg.commit() ;
        dsg.end();
        
        dsg.begin(ReadWrite.READ) ;
        try {
            assertTrue(dsg.isEmpty()) ;
            assertFalse(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }
    
    @Test public void patch_add_delete_abort() {
        DatasetGraphChangeLog dsg = create() ;
        dsg.begin(ReadWrite.WRITE);
        dsg.add(quad1) ;
        dsg.delete(quad1) ;
        assertTrue(dsg.isEmpty()) ;
        dsg.abort() ;
        dsg.end();

        dsg.begin(ReadWrite.READ) ;
        try {
            assertTrue(dsg.isEmpty()) ;
            assertFalse(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }

    @Test public void patch_delete_add_commit() {
        DatasetGraphChangeLog dsg = create() ;
        dsg.begin(ReadWrite.WRITE);
        dsg.delete(quad1) ;
        dsg.add(quad1) ;
        assertFalse(dsg.isEmpty()) ;
        dsg.commit() ;
        dsg.end();
        
        dsg.begin(ReadWrite.READ) ;
        try {
            assertFalse(dsg.isEmpty()) ;
            assertTrue(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }
    
    @Test public void patch_delete_add_abort() {
        DatasetGraphChangeLog dsg = create() ;
        dsg.begin(ReadWrite.WRITE);
        dsg.delete(quad1) ;
        dsg.add(quad1) ;
        assertFalse(dsg.isEmpty()) ;
        dsg.abort() ;
        dsg.end();

        dsg.begin(ReadWrite.READ) ;
        try {
            assertTrue(dsg.isEmpty()) ;
            assertFalse(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }

    @Test public void patch_add_delete_add_commit() {
        DatasetGraphChangeLog dsg = create() ;
        dsg.begin(ReadWrite.WRITE);
        dsg.add(quad1) ;
        dsg.delete(quad1) ;
        dsg.add(quad1) ;
        assertFalse(dsg.isEmpty()) ;
        dsg.commit() ;
        dsg.end();
        
        dsg.begin(ReadWrite.READ) ;
        try {
            assertFalse(dsg.isEmpty()) ;
            assertTrue(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }
    
    @Test public void patch_add_delete_add_abort() {
        DatasetGraphChangeLog dsg = create() ;
        dsg.begin(ReadWrite.WRITE);
        dsg.add(quad1) ;
        dsg.delete(quad1) ;
        dsg.add(quad1) ;
        assertFalse(dsg.isEmpty()) ;
        dsg.abort() ;
        dsg.end();

        dsg.begin(ReadWrite.READ) ;
        try {
            assertTrue(dsg.isEmpty()) ;
            assertFalse(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }
    
    
    @Test public void patch_delete_01() {
        DatasetGraphChangeLog dsg = create(quad1) ;
        assertTrue(dsg.contains(quad1)) ;
        
        dsg.begin(ReadWrite.WRITE);
        dsg.delete(quad1) ;
        assertTrue(dsg.isEmpty()) ;
        dsg.commit() ;
        dsg.end();
        
        dsg.begin(ReadWrite.READ) ;
        try {
            assertTrue(dsg.isEmpty()) ;
            assertFalse(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }
    
    @Test public void patch_delete_02() {
        DatasetGraphChangeLog dsg = create(quad1) ;
        assertTrue(dsg.contains(quad1)) ;
        
        dsg.begin(ReadWrite.WRITE);
        dsg.delete(quad1) ;
        assertTrue(dsg.isEmpty()) ;
        dsg.commit() ;
        dsg.end();
        
        dsg.begin(ReadWrite.READ) ;
        try { 
            assertTrue(dsg.isEmpty()) ;
            assertFalse(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }
        
    @Test public void patch_delete_03() {
        DatasetGraphChangeLog dsg = create() ;
        assertFalse(dsg.contains(quad1)) ;
        
        dsg.begin(ReadWrite.WRITE);
        dsg.delete(quad1) ;
        assertTrue(dsg.isEmpty()) ;
        dsg.commit() ;
        dsg.end();
        
        dsg.begin(ReadWrite.READ) ;
        try {
            assertTrue(dsg.isEmpty()) ;
            assertFalse(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }
    
    @Test public void patch_delete_04() {
        DatasetGraphChangeLog dsg = create() ;
        assertFalse(dsg.contains(quad1)) ;
        
        dsg.begin(ReadWrite.WRITE);
        dsg.delete(quad1) ;
        assertTrue(dsg.isEmpty()) ;
        dsg.commit() ;
        dsg.end();
        
        dsg.begin(ReadWrite.READ) ;
        try { 
            assertTrue(dsg.isEmpty()) ;
            assertFalse(dsg.contains(quad1)) ;
        } finally { dsg.end() ; }
    }
}
