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

package tools;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.lib.cache.CacheInfo;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.lang.StreamRDFCounting;
import org.apache.jena.riot.system.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.progress.*;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** tools to load data into a dataset and report space taken */
class RunMemTimeSpace extends CmdGeneral {
    // Not using bulk loading.

    // Add caching to results
    // Add sizes to results.

    static {
        LogCtl.setLog4j2();
        JenaSystem.init();
    }
    static Logger LOG = LoggerFactory.getLogger("Load");


    static final ArgDecl argCache       = new ArgDecl(ArgDecl.HasValue, "cache");
    static final ArgDecl argParse       = new ArgDecl(ArgDecl.NoValue, "parse");
    static final ArgDecl argTDB         = new ArgDecl(ArgDecl.NoValue, "tdb", "tdbmem");   // Very slow.
    static final ArgDecl argTIM         = new ArgDecl(ArgDecl.NoValue, "tim");
    static final ArgDecl argNORM        = new ArgDecl(ArgDecl.NoValue, "norm");
    static final ArgDecl argNum         = new ArgDecl(ArgDecl.HasValue, "num", "n");

    // Defaults.
    boolean              runCacheMode   = true;
    boolean              runNoCacheMode = false;
    boolean              runParseOnly   = true;
    int                  numTim         = 0;
    int                  numNorm        = 1;
    int                  numTDB         = 0;

    public static void main(String... args) {
        if ( args.length == 0 ) {
            String FN =  "/home/afs/tmp/bsbm-25.rt.gz";
            String FN0 = "/home/afs/Datasets/BSBM/bsbm-250k.nt.gz";
            String FN1 = "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz";
            String FN2 = "/home/afs/Datasets/BSBM/bsbm-5m.nt.gz";
            String FN3 = "/home/afs/Datasets/BSBM/bsbm-25m.nt.gz";

            //args = new String[] {FN1, FN2};
            //args = new String[] {"--warm=4", "--tim_n=2", "--norm_n=2", FN};
            //args = new String[] {"--norm",  "--tim", "-n=2", FN1};

            args = new String[] {"--tim", "--cache=yes", "-n=1", FN1 };
        }
        System.out.println("Args: "+Arrays.asList(args));
        new RunMemTimeSpace(args).mainRun();
    }


    public RunMemTimeSpace(String[] argv) {
        super(argv);
        super.add(argCache);
        super.add(argParse);
        super.add(argTDB);     // SLOW
        super.add(argTIM);
        super.add(argNORM);
        super.add(argNum);
    }

    @Override
    protected String getCommandName() {
        return null;
    }


    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs();

        runParseOnly = hasArg(argParse);

        if ( hasArg(argTDB) || hasArg(argTIM) || hasArg(argNORM) ) {
            numTDB = 0;
            numTim = 0;
            numNorm = 0;
        }

        if ( hasArg(argTDB) )
            numTDB = 1;

        if ( hasArg(argTIM) )
            numTim = 1;

        if ( hasArg(argNORM) )
            numNorm = 1;

        if ( hasArg(argNum) ) {
            int N = parseInt(argNum);
            numNorm = N*numNorm;
            numTim = N*numTim;
            numTDB = N*numTDB;
        }

        if ( hasArg(argCache) ) {
            boolean b = parseBoolean(argCache);
            runCacheMode = b;
            runNoCacheMode = !b;
        }

        if ( ! hasPositional() )
            throw new CmdException("No filename given");
    }

    private int parseInt(ArgDecl x) {
        try {
            return Integer.parseInt(getValue(x));
        } catch (NumberFormatException ex) {
            throw new CmdException("Argument: --"+x.getKeyName()+" : Bad integer");
        }
    }

    private boolean parseBoolean(ArgDecl x) {
        String s = getValue(x);
        String s1 = s.toLowerCase();
        switch(s1) {
            case "yes": case "on": case "true": return true;
            case "no": case "off": case "false": return false;
            default:
                throw new CmdException("Argument: --"+x.getKeyName()+" : Bad boolean");
        }
    }

    @Override
    protected String getSummary() {
        return Lib.classShortName(getClass())+": [--cache=(yes|no)] [--parse] [--tim] [--norm] [-n=NUM] [--tim_n=NUM] [--norm_n=NUM] file ...";
    }

    @Override
    protected void exec() {
        //long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        //long heapFreeSize = Runtime.getRuntime().freeMemory();
        System.out.printf("Max heap: %,d\n", heapMaxSize);

        getPositional().forEach((filename) -> {
            exec1(filename);
            System.out.println();
        });

        results.forEach(report-> {
            // If no stats enables, misses is zero.
            if ( report.stats != null && report.stats.misses > 0 )
                System.out.printf("%-20s  Space=%7.2f MB  Time=%5.2fs  Bytes/triple=%,.2f  Cache=%.1f%%\n",
                                  report.label, report.spaceUsed/(1000*1000.0), 1.0*report.spaceUsed/report.count, report.timeUsed/1000.0,
                                  report.stats.hitRate*100);
            else
                System.out.printf("%-20s  Space=%7.2f MB  Time=%5.2fs  Bytes/triple=%,.2f\n",
                                  report.label, report.spaceUsed/(1000*1000.0), report.timeUsed/1000.0, 1.0*report.spaceUsed/report.count);
            });
        // For concatenating output files.
        System.out.println();
    }

    // DRY

    Creator<DatasetGraph> creatorGeneral = () -> DatasetGraphFactory.create();
    Creator<DatasetGraph> creatorTxnMem = () -> DatasetGraphFactory.createTxnMem();
    Creator<DatasetGraph> creatorTDBMem = () -> TDBFactory.createDatasetGraph();

    List<ActionReport> results = new ArrayList<>();

    long                  count          = -1;
    long                  countTriples   = 0;
    long                  countQuads     = 0;

    final static int tick = 1_000_000;
    final static int superTick = 10;

    private void exec1(String filename) {
        System.out.println("Data: "+filename);
        count = -1;
        if ( runParseOnly ) {
            if ( runNoCacheMode ) {
                System.out.println();
                parseOnly("Parse/Standard", filename, new FactoryRDFStd());
            }
            if ( runCacheMode ) {
                System.out.println();
                parseOnly("Parse/Cache", filename, new FactoryRDFCaching());
            }
            return;
        }

        // Warming.
        //execDft(filename);

        IntStream.range(0, numTim) .sequential().forEach((i)->execTim(filename));

        IntStream.range(0, numNorm).sequential().forEach((i)->execNorm(filename));

        IntStream.range(0, numTDB) .sequential().forEach((i)->execTDB(filename));
    }

    // DRY

    private void execNorm(String filename) {
        if ( runNoCacheMode ) {
            System.out.println();
            execOne("General/NoCache", creatorGeneral, filename, new FactoryRDFStd());
        }
        if ( runCacheMode ) {
            System.out.println();
            execOne("General/Caching", creatorGeneral, filename, new FactoryRDFCaching());
        }
    }

    private void execTim(String filename) {
        if ( runNoCacheMode ) {
            System.out.println();
            execOne("TIM/NoCache", creatorTxnMem, filename, new FactoryRDFStd());
        }
        if ( runCacheMode ) {
            System.out.println();
            execOne("TIM/Caching", creatorTxnMem, filename, new FactoryRDFCaching());
        }
    }

    private void execDft(String filename) {
        if ( runNoCacheMode ) {
            System.out.println();
            execOne("General/Default", creatorGeneral, filename, RiotLib.factoryRDF());
        }
    }

    private void execTDB(String filename) {
        if ( runNoCacheMode ) {
            System.out.println();
            execOne("TDB/Standard", creatorTDBMem, filename, new FactoryRDFStd());
        }
        if ( runCacheMode ) {
            System.out.println();
            execOne("TDB/Caching", creatorTDBMem, filename, new FactoryRDFCaching());
        }
    }
    // Put warming into the DSG type code

    private void parseOnly(String label, String filename, FactoryRDF factory) {
        StreamRDF stream = StreamRDFLib.sinkNull();
        StreamRDFCounting stream2 = StreamRDFLib.count(stream);
        parseOne(label, filename, stream2, factory);
        if ( count < 0 ) {
            count = stream2.count();
            countTriples = stream2.countTriples();
            countQuads = stream2.countQuads();
            System.out.printf("Count=%,d  Triples=%,d  Quads=%,d\n", count, countTriples, countQuads);
        }
    }

    private void execOne(String label, Creator<DatasetGraph> creator, String filename, FactoryRDF f) {
        System.out.flush();
        //System.err.printf("**** %s: In use: %.2f\n", label, memory()/(1000*1000.0));
        DatasetGraph dsg = creator.create();
        ActionReport report = parseOne(label, filename, dsg, f);
        dsg.clear();
        gc();
        results.add(report);
        printReport(label, report);
    }

    private static ActionReport parseOne(String label, String FN, DatasetGraph dsg, FactoryRDF f) {
        StreamRDF stream = StreamRDFLib.dataset(dsg);
        return parseOne(label, FN, stream, f);
    }

    private static ActionReport parseOne(String label, String FN, StreamRDF dest, FactoryRDF f) {
        InputStream in = IO.openFile(FN);
        Lang lang = RDFLanguages.filenameToLang(FN);
        RDFParser r = RDFParser.create()
            .source(FN)
            .factory(f)
            .errorHandler(ErrorHandlerFactory.errorHandlerStd)
            .base(IRILib.filenameToIRI(FN))
            .build();


        MonitorOutput output = MonitorOutputs.outputTo(System.out);
        ProgressMonitor progress = ProgressMonitorOutput.create(output, label, tick, superTick);
        StreamRDF stream0 = new ProgressStreamRDF(dest, progress);
        StreamRDFCounting stream = StreamRDFLib.count(stream0);

        Runnable action = ()-> r.parse(stream);
        ActionReport report = spaceTime(label, ()-> monitor(progress, action), stream);
        if ( f instanceof FactoryRDFCaching ) {
            FactoryRDFCaching cf = (FactoryRDFCaching)f;
            report.stats = cf.stats();
        }
        return report;
    }

    public static void printReport(String label, ActionReport report) {
        if ( label != null ) {
            System.out.print(label);
            System.out.print(" : ");
            System.out.println();
        }
        //System.out.printf("  Space=%7.2f MB  Time=%5.2fs\n", report.spaceUsed/(1000*1000.0), report.timeUsed/1000.0);
        System.out.printf("  Space=%.2f MB\n", report.spaceUsed/(1000*1000.0));
        System.out.printf("  Bytes/triple=%.2f\n",  1.0*report.spaceUsed/report.count);
        System.out.printf("  Time=%.2fs\n",  report.timeUsed/1000.0);
    }

    public static ActionReport spaceTime(String label, Runnable action, StreamRDFCounting stream) {
        Timer t = new Timer();
        gc();
        long before = memory();
        t.startTimer();
        action.run();
        long time = t.endTimer();
        gc();
        long after = memory();
        long mem = after-before;
        return new ActionReport(label, mem, time, stream.count());
    }

    public static void monitor(ProgressMonitor monitor, Runnable action) {
        if ( monitor != null ) {
            monitor.startMessage(null);
            monitor.start();
        }
        action.run();
        if ( monitor != null ) {
            monitor.finish();
            monitor.finishMessage(null);
        }
    }

    public static long memory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public static void gc() {
        Runtime.getRuntime().gc();
    }

    public static class ActionReport {
        public final long spaceUsed;
        public final long timeUsed;
        public final long count;
        public final String label;
        public CacheInfo stats = null;

        public ActionReport(String label, long spaceUsed, long timeUsed, long count) {
            this.label = label;
            this.spaceUsed = spaceUsed;
            this.timeUsed = timeUsed;
            this.count = count;
        }
    }
//
//    public static class ParseActionReport extends ActionReport {
//        public ParseActionReport(int x, String label, long spaceUsed, long timeUsed, long count) {
//            super(label, spaceUsed, timeUsed, count);
//        }
//    }
}