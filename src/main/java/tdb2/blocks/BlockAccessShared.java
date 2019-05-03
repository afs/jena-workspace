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

import org.apache.jena.dboe.base.block.Block;
import org.apache.jena.dboe.base.file.BlockAccess;

/** Single "file" in shared BlockAccess. */  
public class BlockAccessShared implements BlockAccess {
    private final BlockAccess storage;
    final long virtualZero;
    
    public BlockAccessShared(String name, BlockAccess storage, long virtualZero) {
        this.storage = storage;
        this.virtualZero = virtualZero;
    }

    @Override
    public Block allocate(int size) {
        return storage.allocate(size);
    }

    @Override
    public Block read(long id) {
        if ( id == 0 )
            return storage.read(virtualZero);
        return storage.read(id);
    }

    @Override
    public void write(Block block) {
        storage.write(block);
    }

    @Override
    public void overwrite(Block block) {
        storage.overwrite(block);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long allocBoundary() {
        return storage.allocBoundary();
    }

    @Override
    public void resetAllocBoundary(long boundary) {
        storage.resetAllocBoundary(boundary);
    }

    @Override
    public boolean valid(long id) {
        return storage.valid(id);
    }

    @Override
    public String getLabel() {
        // XXX Index
        return storage.getLabel();
    }

    @Override
    public void sync() {
        storage.sync();
    }

    @Override
    public void close() {
        // XXX Close once.
        storage.close();
    }
}