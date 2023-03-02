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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Consumer;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdException;

/** A a command to decide which kind of TYDB database is involved. */
public class tdbx {
    // Does not cover --desc.
    // What about --mem

    static final String CmdQuery = "query";
    static final String CmdLoader = "loader";
    static final String CmdXloader = "xloader";
    static final String CmdDump = "dump";
    static final String CmdBackup = "backup";
    static final String CmdStats = "stats";
    static final String CmdUpdate = "update";

    // xloader is different.

    static Map<String, Consumer<String[]>> redirectTDB1 =
            Map.of("query",     (args)->tdb.tdbquery.main(args)
                  ,"update",    (args)->tdb.tdbupdate.main(args)
                  ,"loader",    (args)->tdb.tdbloader.main(args)
                  ,"compact",   (args)->notSupported("tdbcompact", "Does not apply to TDB1 databases")
                  ,"backup",    (args)->tdb.tdbbackup.main(args)
                  ,"dump",      (args)->tdb.tdbdump.main(args)
                  ,"stats",     (args)->tdb.tdbstats.main(args)
                  );

    static Map<String, Consumer<String[]>> redirectTDB2 =
            Map.of("query",     (args)->tdb2.tdbquery.main(args)
                  ,"update",    (args)->tdb2.tdbquery.main(args)
                  ,"loader",    (args)->tdb2.tdbloader.main(args)
                  ,"compact",   (args)->tdb2.tdbcompact.main(args)
                  ,"backup",    (args)->tdb2.tdbbackup.main(args)
                  ,"dump",      (args)->tdb2.tdbdump.main(args)
                  ,"stats",     (args)->tdb2.tdbstats.main(args)
                  );

    /** Commands shown in usage */
    static List<String> cmdHelp = List.of("query", "update", "loader", "backup", "compact", "stats");

    private static boolean DEBUG = false;

    public static void main(String... args) {
        if ( DEBUG ) System.out.println("args = "+Arrays.asList(args));
        //args = new String[]{"tdbquery", "--loc","/home/afs/tmp/DB", "SELECT (count(*) AS ?count) { ?s ?p ?o }"};
        //args = new String[]{"tdbquery", "--help"};
        exec(args);
    }

    private static void notSupported(String cmd, String msg) {
        error(cmd+": "+msg);
    }

    // Does not return.
    private static void error(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    private static void exec(String[] args) {
        if ( args.length == 0 ) {
            usage();
            System.exit(0);
        }

        String cmd = args[0];
        if ( cmd == null )
            // main was called from elsewhere in java.
            throw new NullPointerException("Null command");

        if ( cmd.equals("help") || cmd.startsWith("-") ) {
            switch (cmd) {
                case "-h":
                case "--h":
                case "help":
                case "-help":
                case "--help":
                    usage();
                    System.exit(0);
            }
            error("No command. Found: '"+cmd+"'");
        }

        // Take off the command name. This leaves the arguments for the
        // dispatch target command and that exposes the "--loc".
        String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);
        args = null;

        if ( DEBUG ) System.err.println("exec: "+cmd+ " :: "+Arrays.asList(cmdArgs));

        String prefix = "tdb";
        if ( cmd.startsWith(prefix) )
            cmd = cmd.substring(prefix.length());

        TDBxCmdLine cmdLine = new TDBxCmdLine(cmdArgs);
        cmdLine.mainRun();

        String location = cmdLine.getLocation();
        if ( location == null )
            error("Couldn't determine the location (\"--loc\")");

        if ( cmdLine.isVerbose() ) System.err.println("Location = "+location);
        if ( DEBUG ) System.err.println("Location = "+location);

        Consumer<String[]> execCmd = null;

        if ( ! FileOps.exists(location) ) {
            execCmd = redirectTDB2.get(cmd);
        } else if ( TDBOps.isTDB2(location) ) {
            execCmd = redirectTDB2.get(cmd);
        } else if ( TDBOps.isTDB1(location) ) {
            execCmd = redirectTDB1.get(cmd);
        } else {
            throw new CmdException("Directory "+location+" exists but is not recognized as a TDB database");
        }
        if ( execCmd == null ) {
            System.err.println("Can't dispatch command '"+cmd+"'");
            System.exit(1);
            throw new CmdException("Can't dispatch command '"+cmd+"'");
        }
        execCmd.accept(cmdArgs);
    }

    private static void usage() {
        System.err.println("tdbx CMD args ...");
        StringJoiner sj = new StringJoiner(", ");
        cmdHelp.forEach(sj::add);
        System.err.println("  where cmd is one of "+sj.toString());
    }

    /** Parsing the tdbx command line up to the first non-argument. */
    static class TDBxCmdLine extends CmdArgModule {

        protected final ArgDecl locationDecl    = new ArgDecl(ArgDecl.HasValue, "location", "loc", "tdb");
        protected final ArgDecl confDecl    = new ArgDecl(ArgDecl.HasValue, "location", "loc", "tdb");

        // Leave to the command to be called.
        //protected final ArgDecl argDeclHelp     = new ArgDecl(false, "help", "h");
        protected final ArgDecl argDeclVerbose  = new ArgDecl(false, "v", "verbose");

        private String location = null;
        private boolean verbose = false;

        protected TDBxCmdLine(String[] argv) {
            super(argv);
            super.add(locationDecl);
            add(argDeclVerbose);
            //add(argDeclDebug, "--debug", "Output information for debugging");
            //add(argDeclHelp);
        }

        @Override
        protected void handleUnrecognizedArg( String argStr ) {}

        @Override
        protected void processModulesAndArgs() {
            location = getValue(locationDecl);
            verbose = contains(argDeclVerbose);
//            if ( contains(argDeclHelp) ) {
//                usage();
//                System.exit(0);
//            }
        }

        @Override
        protected void exec() {
            // Not used.
        }

        @Override
        protected String getCommandName() { return "tdbx"; }

        public String getLocation() { return location; }

        public boolean isVerbose() { return verbose; }
    }
}
