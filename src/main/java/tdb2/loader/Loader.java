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

package tdb2.loader;

import java.util.List;

import org.apache.jena.riot.system.StreamRDF;
import tdb2.loader.base.LoaderOps;

/** Interface to bulk loading.
* To use a loader,
 * <pre>
 *   loader.startBulk();
 *   send data ... either stream() or load(files) or a mixture.    
 *   loader.finishBulk();
 * </pre>
 */  
public interface Loader {
    public void startBulk();
    public void finishBulk();
    public void finishException();

    /** Load fileswith synatx given by the file name extension,
     * or URLs, with content negotiation.
     * @param filenames
     */
    public void load(List<String> filenames);
    
    /** 
     * 
     * @see LoaderOps#progressMonitor
     */
    public StreamRDF stream();
    
    public long countTriples();
    public long countQuads();
}
