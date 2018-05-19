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

import java.util.Arrays;
import java.util.List;

import org.apache.jena.riot.system.StreamRDF;

/**
 * Bulk loaders imporve the loading of data into datasets. Each bulk loader has
 * consequences in achiving its improvements, including in some cases locking out
 * all other access to the daatset while the loading is underway.
 * <p> 
 * To create a lareg datasest from 
 * 
 * 
 * 
 * 
 * To use a loader,
 * 
 * <pre>
 *   loader.startBulk();
 *   send data ... either stream() or load(files) or a mixture.    
 *   loader.finishBulk();
 * </pre>
 * 
 * Loaders
 * 
 * <h3>I/O and RAM.
 * 
 * <ul>
 * <li>basic</li>
 * <li>sequential</li>
 * <li>parallel</li>
 * </ul>
 * 
 * 
 * <h3>To use a loader,
 * 
 * <pre>
 *   loader.startBulk();
 *   send data ... 
 *        use stream()
 *        or load(files)
 *        or a mixture.    
 *   loader.finishBulk();
 * </pre>
 * 
 * Loading for parallel is not transaction-safe.
 */
public interface Loader {
    public void startBulk();
    public void finishBulk();
    public void finishException();

    /** Load files with syntax given by the file name extension,
     * or URLs, with content negotiation.
     * @param filenames
     */
    public void load(List<String> filenames);
    
    /** Load files with syntax given by the file name extension,
     * or URLs, with content negotiation.
     * @param filenames
     */
    default public void load(String ... filenames) { load(Arrays.asList(filenames)); }

    
    /** Send data to the loader by {@link StreamRDF} */
    public StreamRDF stream();
    
    public long countTriples();
    public long countQuads();
}
