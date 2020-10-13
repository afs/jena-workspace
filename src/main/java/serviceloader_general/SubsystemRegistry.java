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

package serviceloader_general;

import java.util.List ;

/**
 * A {@code JenaSubsystemRegistry} is a set of objects implementing {@link SubsystemLifecycle}.
 */
public interface SubsystemRegistry {
    
    /** Load - perform some kinds of search for {@link SubsystemLifecycle} implementations.
     * This is called once in the initialization process.
     */
    public void load();
    
    /** Add to the collection. */
    public void add(SubsystemLifecycle module);

    /** check whether registered */
    public boolean isRegistered(SubsystemLifecycle module);

    /** Remove from the collection. */
    public void remove(SubsystemLifecycle module);

    public int size();

    public boolean isEmpty();

    /**
     * Return the registered items in a copied list.
     * The list is detached from the
     * registry and the caller can mutate it.
     * There is no specific ordering requirement. 
     */
    public List<SubsystemLifecycle> snapshot();
}