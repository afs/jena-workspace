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

package tdb2;

import static tdb2.loader.BulkLoader.LOG;

import java.util.List;
import java.util.Objects;

import jena.cmd.ArgDecl;
import jena.cmd.CmdException;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.Txn;
import tdb2.cmdline.CmdTDB;
import tdb2.cmdline.CmdTDBGraph;
import tdb2.loader.Loader;
import tdb2.loader.base.TimerX;
import tdb2.loader.parallel.LoaderParallel;
import tdb2.loader.simple.LoaderSimple;

// Replaces tdb2.tdbloader.

public class CmdBulkLoaderTDB2 extends CmdTDBGraph {
    private static final ArgDecl argNoStats = new ArgDecl(ArgDecl.NoValue, "nostats");
    private static final ArgDecl argStats = new ArgDecl(ArgDecl.HasValue,  "stats");

    private boolean showProgress = true;
    private boolean generateStats = true;
    
    public static void x_main(String... args) {
        CmdTDB.init();
        new CmdBulkLoaderTDB2(args).mainRun();
    }

    protected CmdBulkLoaderTDB2(String[] argv) {
        super(argv);
//        super.add(argNoStats, "--nostats", "Switch off statistics gathering");
//        super.add(argStats);   // Hidden argument
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " [--desc DATASET | --loc DIR] FILE ...";
    }

    @Override
    protected void exec() {
        if ( isVerbose() ) {
            System.out.println("Java maximum memory: " + Runtime.getRuntime().maxMemory());
            System.out.println(ARQ.getContext());
        }
        if ( isVerbose() )
            showProgress = true;
        if ( isQuiet() )
            showProgress = false;
        if ( super.contains(argStats) ) {
            if ( ! hasValueOfTrue(argStats) && ! hasValueOfFalse(argStats) )
                throw new CmdException("Not a boolean value: "+getValue(argStats));
            generateStats = super.hasValueOfTrue(argStats);
        }

        if ( super.contains(argNoStats))
            generateStats = false;
        
        List<String> urls = getPositional();
        if ( urls.size() == 0 )
            urls.add("-");

        if ( graphName == null ) {
            loadQuads(urls);
            return;
        }
        
        // There's a --graph.
        // Check/warn that there are no quads formats mentioned
        
        for ( String url : urls ) {
            Lang lang = RDFLanguages.filenameToLang(url);
            if ( lang != null && RDFLanguages.isQuads(lang) ) {
                throw new CmdException("Warning: Quads format given - only the default graph is loaded into the graph for --graph");
            }
        }
        
        loadTriples(graphName, urls);
    }

    private void loadTriples(String graphName, List<String> urls) {
        loader(super.getDatasetGraph(), graphName, urls, showProgress);
    }

    private void loadQuads(List<String> urls) {
        loader(super.getDatasetGraph(), null, urls, showProgress);
    }
    
    private long loader(DatasetGraph dsg, String graphName, List<String> urls, boolean showProgress) {
        Loader loader = chooseLoader(dsg, graphName);
        long elapsed = TimerX.time(()->{
                    loader.startBulk();
                    loader.load(urls);
                    loader.finishBulk();
        });
        if ( ! super.isQuiet() )
            FmtLog.info(LOG, "Time: %s seconds\n", Timer.timeStr(elapsed)); 
        return elapsed;
    }

    /** Choose the bulkloader. */ 
    private Loader chooseLoader(DatasetGraph dsg, String graphName) {
        Objects.requireNonNull(dsg);
        Node gn = null;
        if ( graphName != null )
            gn = NodeFactory.createURI(graphName);
        
        boolean empty = Txn.calculateRead(dsg, ()->dsg.isEmpty());
        if ( empty )
            // The sequential load does work on non-empty datasets, but it replays the
            // whole index to rebuild so piotential doing a lot of redundant work.
            //return new LoaderSequential(dsg, gn, null, showProgress);
            
            
            return new LoaderParallel(dsg, gn, showProgress);
        else
            return new LoaderSimple(dsg, gn, showProgress);
    }
}
