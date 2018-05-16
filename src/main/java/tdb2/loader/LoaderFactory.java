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

import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import tdb2.loader.parallel.LoaderParallel;
import tdb2.loader.sequential.LoaderSequential;
import tdb2.loader.simple.LoaderSimple;

/** Obtain a {@link Loader}.
 * <p>
 * To use a loader,
 * <pre>
 *   loader.startBulk();
 *   send data ... either stream() or load(files) or a mixture.    
 *   loader.finishBulk();
 * </pre>
 */  
public class LoaderFactory {
    
    /** 
     * A simple loader that streams data into a dataset as a plain "read" operation.
     * Data is read inside a transaction.
     * <p>
     * Supply a {@link MonitorOutput} for the desirabled progress and summary output messages
     * or {@code null} for no output. 
     */
    public static Loader simpleLoader(DatasetGraph dsg, MonitorOutput output) {
        Objects.requireNonNull(dsg);
        return new LoaderSimple(dsg, null, output);
    }
    
    /**
     * A simple loader loader to load a single graph in the destination {@code DatasetGraph}.
     * <p>
     * Use {@link Quad#defaultGraphIRI} to load the default graph.
     * <p>
     * No other graphs in the destination {@code DatasetGraph} are touched. If quads
     * data is read, default graph data is sent to the destination named graph but all
     * other quad data is discarded.
     * <p>
     * For other behaviours, use {@link #simpleLoader(DatasetGraph, MonitorOutput)} 
     * and wrap the {@linkStreamRDF} from {@link Loader#stream()}) with the required
     * transformation.  
     * 
     * @see #simpleLoader(DatasetGraph, MonitorOutput)
     */
    public static Loader simpleLoader(DatasetGraph dsg, Node graphName, MonitorOutput output) {
        Objects.requireNonNull(dsg);
        Objects.requireNonNull(graphName);
        return new LoaderSimple(dsg, graphName, output);
    }

    /** A loader that takes all the data into the primary indexes in one phase, then 
     * calculates/updates secondary indexes. This can make more efficient use of memory
     * so that one datastructure is being owkred on at a time. 
     * <p>
     * This loader uses one thread.
     * <p>
     * The dataset can not be used for other operations - the code will block other transactions
     * as necessary and release then when loading has finished.
     * <p> 
     * Supply a {@link MonitorOutput} for the desirabled progress and summary output messages
     * or {@code null} for no output. 
     */
    public static Loader sequentialLoader(DatasetGraph dsg, MonitorOutput output) {
        Objects.requireNonNull(dsg);
        return new LoaderSequential(dsg, null, output);
    }

    /** 
     * A sequential loader to load a single graph in the destination {@code DatasetGraph}.
     * See {@link #sequentialLoader(DatasetGraph, MonitorOutput)} for loader characteristics.
     * <p>
     * Use {@link Quad#defaultGraphIRI} to load the default graph.
     * <p>
     * No other graphs in the destination {@code DatasetGraph} are touched. If quads
     * data is read, default graph data is sent to the destination named graph but all
     * other quad data is discarded.
     * <p>
     * For other behaviours, use {@link #simpleLoader(DatasetGraph, MonitorOutput)} 
     * and wrap the {@linkStreamRDF} from {@link Loader#stream()}) with the required
     * transformation.
     * 
     * @see #sequentialLoader(DatasetGraph, MonitorOutput)
     */
     
    public static Loader sequentialLoader(DatasetGraph dsg, Node graphName, MonitorOutput output) {
        Objects.requireNonNull(dsg);
        Objects.requireNonNull(graphName);
        return new LoaderSequential(dsg, graphName, output);
    }

    /** 
     * A loader that uses multiple threads to reduce loading time,
     * and makes parallel writes to persistent storage (disk or SSD).
     * <p> 
     * This loader will use all available resources of the machine,
     * making other actions on the machine unresponsive.
     * <p>
     * * The dataset can not be used for other operations - the code will block other transactions
     * as necessary and release then when loading has finished.
     * <p>
     * Supply a {@link MonitorOutput} for the desirabled progress and summary output messages
     * or {@code null} for no output.
     */
    public static Loader parallelLoader(DatasetGraph dsg, MonitorOutput output) {
        Objects.requireNonNull(dsg);
        return new LoaderParallel(dsg, null, output);
    }
    
    /** 
     * A parallel loader to load a single graph in the destination {@code DatasetGraph}.
     * See {@link #parallelLoader(DatasetGraph, MonitorOutput)} for loader characteristics.
     * <p>
     * Use {@link Quad#defaultGraphIRI} to load the default graph.
     * <p>
     * No other graphs in the destination {@code DatasetGraph} are touched. If quads
     * data is read, default graph data is sent to the destination named graph but all
     * other quad data is discarded.
     * <p>
     * For other behaviours, use {@link #parallelLoader(DatasetGraph, MonitorOutput)} 
     * and wrap the {@link StreamRDF} from {@link Loader#stream()}) with the required
     * transformation.
     * 
     * @see #parallelLoader(DatasetGraph, MonitorOutput)
     */

    public static Loader parallelLoader(DatasetGraph dsg, Node graphName, MonitorOutput output) {
        Objects.requireNonNull(dsg);
        Objects.requireNonNull(graphName);
        return new LoaderParallel(dsg, graphName, output);
    }

    /**
     * Return a general purpose loader to load a dataset.
     * This default may chnage between versions.
     */  
    public static Loader createLoader(DatasetGraph dsg, MonitorOutput output) {
        Objects.requireNonNull(dsg);
        return createDft(dsg, null, output);
    }
    
    /**
     * Return a general purpose loader to load a single graph in the destination {@code DatasetGraph}.
     * Use {@link Quad#defaultGraphIRI} to load the default graph.
     * <p>
     * No other graphs in the destination {@code DatasetGraph} are touched. If quads
     * data is read, default graph data is sent to the destination named graph but all
     * other quad data is discarded.
     * <p>
     * This default may change between versions.
     */
    public static Loader createLoader(DatasetGraph dsg, Node graphName, MonitorOutput output) {
        Objects.requireNonNull(dsg);
        Objects.requireNonNull(graphName);
        return createDft(dsg, graphName, output);
    }
    
    private static Loader createDft(DatasetGraph dsg, Node graphName, MonitorOutput output) {
        // May be a conservatively tuned LoaderParallel so that it does not
        // swamp the machine and copes with lower spec hardware while still
        // providing a reasonable loading rate.  
        return parallelLoader(dsg, graphName, output);
    }
}
