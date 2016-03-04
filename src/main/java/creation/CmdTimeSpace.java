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

package creation;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.stream.IntStream ;

import jena.cmd.ArgDecl ;
import jena.cmd.CmdException ;
import jena.cmd.CmdGeneral ;
import log_dsg.Txn ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Creator ;
import org.apache.jena.atlas.lib.IRILib ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.atlas.logging.ProgressLogger ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.apache.jena.riot.system.* ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.Quad ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

class CmdTimeSpace extends CmdGeneral {
    
    // Add caching to results
    // Add sizes to results.

    static { LogCtl.setLog4j(); }
    //static { LogCtl.setCmdLogging(); }
    static Logger LOG = LoggerFactory.getLogger("4D") ;
    
    static final ArgDecl argWarm = new ArgDecl(ArgDecl.HasValue, "warm") ;
    static final ArgDecl argTIM = new ArgDecl(ArgDecl.NoValue, "tim") ;
    static final ArgDecl argNORM = new ArgDecl(ArgDecl.NoValue, "norm") ;
    static final ArgDecl argTIM_N = new ArgDecl(ArgDecl.HasValue, "tim_n", "numTim") ;
    static final ArgDecl argNORM_N = new ArgDecl(ArgDecl.HasValue, "norm_n", "numNorm") ;
    static final ArgDecl argNum = new ArgDecl(ArgDecl.HasValue, "num", "n") ;
    
    int numWarm = 0 ;
    int numTim  = 0 ;
    int numNorm = 1 ;
    
    public static void main(String... args) {
        if ( args.length == 0 ) {
            String FN1 = "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz" ;
            String FN2 = "/home/afs/Datasets/BSBM/bsbm-250k.nt.gz" ;
            String FN3 = "/home/afs/Datasets/Chebi/chebi.nt" ;
            String FN4 = "/home/afs/Datasets/Chembl/chembl_20.0_assay.ttl.gz" ;
            String FN5 = "/home/afs/Datasets/Nature-2015-06/datasets/nq/npg-contributors-dataset.nq" ;
            //args = new String[] {FN1, FN2, FN4, FN4, FN5} ;
            //args = new String[] {"--warm=4", "--tim_n=2", "--norm_n=2","/home/afs/Datasets/BSBM/bsbm-5m.nt.gz"} ;
            args = new String[] {"/home/afs/Datasets/BSBM/bsbm-1m.nt.gz"} ;
        }
        new CmdTimeSpace(args).mainRun(); 
    }
    
    
    public CmdTimeSpace(String[] argv) {
        super(argv);
        super.add(argWarm) ;
        super.add(argTIM) ;
        super.add(argNORM) ;
        super.add(argTIM_N) ;
        super.add(argNORM_N) ;
        super.add(argNum) ;
    }

    @Override
    protected String getCommandName() {
        return null;
    }


    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs();
        
        if ( hasArg(argWarm) ) {
            numWarm = parseInt(argWarm) ;
        }
        
        if ( hasArg(argTIM) ) {
            numNorm = hasArg(argNORM) ? 1 : 0 ;
            numTim = 1 ;
        }
        
        if ( hasArg(argNORM) ) {
            numNorm = hasArg(argTIM) ? 1 : 0 ;
            numTim = 1 ;
        }
        
        if ( hasArg(argNum) ) {
            numNorm = parseInt(argNum) ;
            numTim = parseInt(argNum) ;
        }

        if ( hasArg(argTIM_N) ) {
            numTim = parseInt(argTIM_N) ;
        }
            
        if ( hasArg(argNORM_N) ) {
            numNorm = parseInt(argNORM_N) ;
        }
        
        if ( ! hasPositional() )
            throw new CmdException("No filename given") ; 
    }
    
    private int parseInt(ArgDecl x) {
        try {
        return Integer.parseInt(getValue(x)) ;
        } catch (NumberFormatException ex) {
            throw new CmdException("Argument: --"+x.getKeyName()+" : Bad integer") ; 
        }
    }
    
    @Override
    protected String getSummary() {
        return Lib.classShortName(getClass())+": [--tim] [--norm] [-n=NUM] [--tim_n=NUM] [--norm_n=NUM]" ;
    }
    
    @Override
    protected void exec() {
        getPositional().forEach((filename) -> {
            exec1(filename) ;
            System.out.println() ;
        }) ;
        
        results.forEach(report->
            System.out.printf("%-20s  Space=%7.2f MB  Time=%5.2fs\n", report.label, report.spaceUsed/(1000*1000.0), report.timeUsed/1000.0)
            );
    }

    List<ActionReport> results = new ArrayList<>() ;
    
    final static int tick = 1_000_000 ;
    final static int superTick = 10 ;
    
    private void exec1(String filename) {
        System.out.println("Data: "+filename);
        count = -1 ;
        
        IntStream.range(0, numWarm).sequential().forEach((i)->warmUp(filename)) ;
        
        IntStream.range(0, numNorm).sequential().forEach((i)->execNorm(filename)) ;
        
        IntStream.range(0, numTim) .sequential().forEach((i)->execTim(filename)) ;
    }

    // DRY
    
    Creator<DatasetGraph> creatorGeneral = () -> DatasetGraphFactory.create() ;
    Creator<DatasetGraph> creatorTxnMem = () -> DatasetGraphFactory.createTxnMem() ;
    
    private void execTim(String filename) {
        System.out.println() ;
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem() ;
        execOne("TIM/Standard", creatorGeneral, filename, new FactoryRDFStd()) ;
        System.out.println();
        execOne("TIM/Caching", creatorGeneral, filename, new FactoryRDFCaching()) ;
    }

    private void execNorm(String filename) {
        System.out.println() ;
        DatasetGraph dsg = DatasetGraphFactory.create() ;
        execOne("General/Standard", creatorTxnMem, filename, new FactoryRDFStd()) ;
        System.out.println();
        execOne("General/Caching", creatorTxnMem, filename, new FactoryRDFCaching()) ;
    }
    
    // Put warming into the DSG type code 
    
    private void warmUp(String filename) {
        if ( numNorm > 0 ) {
            DatasetGraph dsg = creatorGeneral.create() ;
            warmUp(dsg, filename);
            dsg.clear() ;
            dsg = null ;
            gc() ;
        }
        if ( numNorm > 0 ) {
            DatasetGraph dsg = creatorTxnMem.create() ;
            warmUp(dsg, filename);
            dsg.clear() ;
            dsg = null ;
            gc() ;
        }
    }
    
    long count = -1 ;
    long countTriples ;
    long countQuads ;

    private void warmUp(DatasetGraph dsg, String filename) {
        StreamRDF stream = StreamRDFLib.dataset(dsg) ;
        StreamRDFCounting stream2 = StreamRDFLib.count(stream) ;
        ProgressLogger monitor = new ProgressLogger(LOG, "tuples", tick, superTick) ;
        StreamRDF stream3 = new ProgressStreamRDF(stream2, monitor) ; 
        Runnable r =  ()->Txn.executeWrite(dsg, ()->RDFDataMgr.parse(stream3, filename)) ;
        spaceTime("Warm", ()-> monitor(monitor, r)) ;
        dsg.clear() ;
        if ( count < 0 ) {
            count = stream2.count() ;
            countTriples = stream2.countTriples() ;
            countQuads = stream2.countQuads() ;
            System.out.printf("Count=%,d  Triples=%,d  Quads=%,d\n", count, countTriples, countQuads) ;
        }
        //stream2 = null ;
        stream = null ;
        //dsg = null ;
        
        
    }
    
    private void execOne(String label, Creator<DatasetGraph> creator, String filename, FactoryRDF f) {
        System.out.flush();
        System.err.printf("**** %s: In use: %.2f\n", label, memory()/(1000*1000.0)) ;
        DatasetGraph dsg = creator.create() ;
        ActionReport report = parseOne(label, filename, dsg, f);
        dsg.clear() ;
        gc() ;
        results.add(report) ;
    }
    
    private static ActionReport parseOne(String label, String FN, DatasetGraph dsg, FactoryRDF f) {
        
        ReaderRIOT r = setup(FN, dsg, f) ;
        InputStream in = IO.openFile(FN) ;
        StreamRDF stream$ = StreamRDFLib.dataset(dsg) ;
        ProgressLogger progress = new ProgressLogger(LOG, "tuples", tick, superTick) ;
        StreamRDF stream = new ProgressStreamRDF(stream$, progress) ; 

        Runnable action = ()-> r.read(in, IRILib.filenameToIRI(FN), null, stream, null) ;
        ActionReport report = spaceTime(label, ()-> monitor(progress, action)) ;
        printReport(label, report); 
        if ( f instanceof FactoryRDFCaching ) {
            System.out.println();
            FactoryRDFCaching cf = (FactoryRDFCaching)f ;
            cf.details() ;
        }
        return report ;
    }
    
    private static ReaderRIOT setup(String FN, DatasetGraph dsg, FactoryRDF f) {
     // No ParserProfile 
        Lang lang = RDFLanguages.filenameToLang(FN) ;
        ReaderRIOT r = RDFDataMgr.createReader(lang) ;
        
        
        // UGLY
        ParserProfile pp0 = r.getParserProfile() ;
        pp0 = RiotLib.profile(lang, IRILib.filenameToIRI(FN)) ;
        
        // Redirect operations.
        ParserProfile pp = new ParserProfileBase(pp0.getPrologue(), null) {
            @Override
            public Node createURI(String uriStr, long line, long col) {
                return f.createURI(uriStr) ; 
            }
            @Override
            public Node createTypedLiteral(String lexical, RDFDatatype datatype, long line, long col) {
                return f.createTypedLiteral(lexical, datatype) ;
            }
            
            @Override
            public Triple createTriple(Node s, Node p, Node o, long line, long col) {
                return f.createTriple(s, p, o) ;
            }
            @Override
            public Quad createQuad(Node g, Node s, Node p, Node o, long line, long col) {
                return f.createQuad(g, s, p, o) ;
            }

        } ;
        pp.setHandler(ErrorHandlerFactory.errorHandlerStd);
        r.setParserProfile(pp);
        return r ;
    }

    public static void printReport(String label, ActionReport report) {
        if ( label != null ) {
            System.out.print(label) ;
            System.out.print(" : ") ;
            System.out.println() ;
        }
        //System.out.printf("  Space=%7.2f MB  Time=%5.2fs\n", report.spaceUsed/(1000*1000.0), report.timeUsed/1000.0) ;
        System.out.printf("  Space=%.2f MB\n", report.spaceUsed/(1000*1000.0)) ;
        System.out.printf("  Time=%.2fs\n",  report.timeUsed/1000.0) ;
    }
    
    public static ActionReport spaceTime(String label, Runnable action) {
        Timer t = new Timer() ;
        gc() ;
        long before = memory();
        t.startTimer();
        action.run();
        long time = t.endTimer() ;
        gc() ;
        long after = memory();
        long mem = after-before ;
        return new ActionReport(label, mem, time) ;
    }
    
    public static void monitor(ProgressLogger monitor, Runnable action) {
        if ( monitor != null ) {
            //monitor.startMessage() ;
            monitor.start() ;
        }
        action.run(); 
        if ( monitor != null ) {
            monitor.finish() ;
            monitor.finishMessage() ;
        }
    }

    public static long memory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
    
    public static void gc() {
        Runtime.getRuntime().gc() ;
    }
    
    public static class ActionReport {
        public final long spaceUsed ;
        public final long timeUsed ;
        public final String label;
        public ActionReport(String label, long spaceUsed, long timeUsed) {
            this.label = label ;
            this.spaceUsed = spaceUsed ;
            this.timeUsed = timeUsed ;
        }
    }

    public static class ParseActionReport extends ActionReport {
        public final long count ;
        public ParseActionReport(String label, long spaceUsed, long timeUsed, long count) {
            super(label, spaceUsed, timeUsed) ;
            this.count = count ;
        }
    }
}