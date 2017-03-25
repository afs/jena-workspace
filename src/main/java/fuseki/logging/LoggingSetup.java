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

package fuseki.logging;

import java.io.*;
import java.net.URL;
import static fuseki.logging.PkgLib.*;

/**
 * Set logging. Configuration for logging is chosen based on following steps until one
 * succedes.
 * <ol>
 * <li>Is logging already initialized? (test the system property).
 * <li>Use file:{config} (for appropriate {config} name).
 * <li>Look on the classpath: {config} as a java resource
 * <li>Look on the classpath: ...path.../{config} as a java resource
 * <li>Use a default string.
 * </ol>
 */

public abstract class LoggingSetup {
    /** Set up logging - standalone and war packaging */

    private static boolean logLogging         = false;
    private static boolean loggingInitialized = false;
    private static boolean allowLoggingReset  = true;

    public static  void logSetup(boolean value) {
        logLogging = value;
    }
    
    /**
     * Switch off logging setting. Used by the embedded server so that the application's
     * logging setup is not overwritten.
     */
    public static synchronized void allowLoggingReset(boolean value) {
        allowLoggingReset = value;
    }

    private static String pathBase  = "org/apache/jena/";

    /**
     * Get the path used to lookup a {config} file.
     */
    public static String getPathBase() {
        return pathBase;
    }

    /**
     * Set the path used to lookup a {config} file. This is the resource path, the file is
     * "path/{config}".
     */
    public static void setPathBase(String string) {
        if ( !string.endsWith("/") )
            string = string + "/";
        pathBase = string;
    }

    private static LoggingSetup theLoggingSetup = new LoggingSetupNoOp();

    public static LoggingSetup getLoggingSetup() {
        return theLoggingSetup;
    }

    public static synchronized void setLogging() {
        if ( !allowLoggingReset )
            return;
        if ( loggingInitialized )
            return;
        loggingInitialized = true;

        // Discover the binding for logging
        if ( checkForSimple() ) {
            // No specific setup.
            logLogging("slf4j-simple logging found");
            return;
        }

        boolean hasLog4j = checkForLog4J();
        boolean hasJUL = checkForJUL();
        if ( hasLog4j && hasJUL ) {
            logAlways("Found both Log4j and JUL setup for SLF4J; using Log4j");
            // Force SLF4J
        } else if ( !hasLog4j && !hasJUL ) {
            // Do nothing - hope logging gets initialized automatically. e.g. logback.
            // In some ways this is the preferred outcome for the war file.
            //
            // The standalone server we have to make a decision and it is better
            // if it uses the predefined format.
            logLogging("Neither Log4j nor JUL setup for SLF4J");
            return;
        }

        LoggingSetup loggingSetup = null;
        if ( hasLog4j )
            loggingSetup = new LoggingSetupLog4j();
        if ( hasJUL )
            loggingSetup = new LoggingSetupJUL();

        if ( loggingSetup == null ) {
            logAlways("Failed to find a provider for slf4j");
            return;
        }

        loggingSetup.setup();
        theLoggingSetup = loggingSetup;
    }

    public void setup() {
        if ( maybeAlreadySet() ) {
            logLogging("already set");
            return;
        }
        
        logLogging("Setup: "+getDisplayName());

        String fn1 = getLoggingSetupFilename();
        String fn2 = null;

        if ( tryFileFor(fn1) )
            return;
        if ( tryFileFor(fn2) )
            return;
        if ( tryClassPathFor(getLoggingSetupFilename()) )
            return;
        if ( pathBase != null && tryClassPathFor(pathBase + getLoggingSetupFilename()) )
            return;
        defaultSetup();
    }

    /**
     * Get a string that is the default configuration and use that to configure logging
     */
    protected void defaultSetup() {
        logLogging("Use default setup");
        byte b[] = asUTF8bytes(getDefaultString());
        try (InputStream input = new ByteArrayInputStream(b)) {
            initFromInputStream(input);
        }
        catch (IOException ex) {
            exception(ex);
        }
    }
    
    protected boolean tryFileFor(String fn) {
        if ( fn == null )
            return false;
        logLogging("try file %s", fn);
        try {
            File f = new File(fn);
            if ( f.exists() ) {
                logLogging("found file:" + fn);
                try (InputStream input = new BufferedInputStream(new FileInputStream(f))) {
                    initFromInputStream(input);
                }
                System.setProperty(getSystemProperty(), "file:" + fn);
                return true;
            }
        }
        catch (Throwable th) {}
        return false;
    }

    protected boolean tryClassPathFor(String resourceName) {
        logLogging("try classpath %s", resourceName);
        URL url = getResource(resourceName);
        if ( url != null ) {
            logLogging("found via classpath %s", url);
            if ( url != null ) {
                try {
                    initFromInputStream(url.openStream());
                }
                catch (IOException e) { exception(e); }
                System.setProperty(getSystemProperty(), url.toString());
                return true;
            }
        }
        return false;
    }

    /** Open by classpath or return null */
    protected URL getResource(String resourceName) {
        URL url = this.getClass().getClassLoader().getResource(resourceName);
        if ( url == null )
            return null;
        // Skip any thing that looks like test code.
        if ( url.toString().contains("-tests.jar") || url.toString().contains("test-classes") )
            return null;
        return url;
    }

    /** Has logging been setup? */
    protected boolean maybeAlreadySet() {
        // No loggers have been created but configuration may have been set up.
        String propName = getSystemProperty();
        String x = System.getProperty(propName, null);
        if ( x != null ) {
            logLogging("%s = %s", propName, x);
            return true;
        }
        return false;
    }

    // Need both "org.slf4j.impl.Log4jLoggerAdapater" and "org.apache.log4j.Logger"
    private static boolean checkForLog4J() {
        boolean bLog4j = checkForClass("org.apache.log4j.Logger");
        boolean bLog4jAdapter = checkForClass("org.slf4j.impl.Log4jLoggerAdapter");
        if ( bLog4j && bLog4jAdapter )
            return true;
        if ( !bLog4j && !bLog4jAdapter )
            return false;
        if ( !bLog4j ) {
            logAlways("Classpath has the slf4j-log4j adapter but not log4j");
            return false;
        }
        if ( !bLog4jAdapter ) {
            logAlways("Classpath has log4j but not the slf4j-log4j adapter");
            return false;
        }
        return false;
    }

    private static boolean checkForJUL() {
        return checkForClass("org.slf4j.impl.JDK14LoggerAdapter");
    }

    private static boolean checkForSimple() {
        return checkForClass("org.slf4j.impl.SimpleLogger");
    }

    private static boolean checkForClass(String className) {
        try {
            Class.forName(className);
            return true;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }

    /** Log for the logging setup process */
    protected static void logLogging(String fmt, Object... args) {
        if ( logLogging ) {
            logAlways(fmt, args);
        }
    }

    private static PrintStream logLogger = System.err; 
    
    /** Log for the logging setup messages like warnings. */
    protected static void logAlways(String fmt, Object... args) {
        fmt = "[Logging setup] "+fmt;
        if ( ! fmt.endsWith("\n") )
            fmt = fmt+"\n";
        // Do as one call so that is does not get broken up.
        logLogger.printf(fmt, args);
    }

    protected abstract String getLoggingSetupFilename();

    protected abstract String getSystemProperty();

    protected abstract String getDefaultString();

    /** Configure logging from an input stream */
    protected abstract void initFromInputStream(InputStream inputStream) throws IOException;

    protected abstract String getDisplayName();

    public abstract void setLevel(String logger, String level);

    /** Log4j setup */
    static class LoggingSetupLog4j extends LoggingSetup {

        @Override
        protected String getDisplayName() {
            return "Log4J";
        }
        
        @Override
        protected void initFromInputStream(InputStream inputStream) throws IOException {
            org.apache.log4j.PropertyConfigurator.configure(inputStream);
        }

        @Override
        protected String getLoggingSetupFilename() {
            return "log4j.properties";
        }

        @Override
        protected String getSystemProperty() {
            return "log4j.configuration";
        }

        @Override
        protected String getDefaultString() {
            return LoggingDefaults.defaultLog4j;
        }

        private boolean log4jMsgLoggedOnce = false; 
        @Override
        public void setLevel(String logger, String levelName) {
            try {
                org.apache.log4j.Level level = org.apache.log4j.Level.ALL;
                if ( levelName.equalsIgnoreCase("info") )
                    level = org.apache.log4j.Level.INFO;
                else if ( levelName.equalsIgnoreCase("debug") )
                    level = org.apache.log4j.Level.DEBUG;
                else if ( levelName.equalsIgnoreCase("trace") )
                    level = org.apache.log4j.Level.TRACE;
                else if ( levelName.equalsIgnoreCase("warn") || levelName.equalsIgnoreCase("warning") )
                    level = org.apache.log4j.Level.WARN;
                else if ( levelName.equalsIgnoreCase("error") )
                    level = org.apache.log4j.Level.ERROR;
                else if ( levelName.equalsIgnoreCase("OFF") )
                    level = org.apache.log4j.Level.OFF;
                if ( level != null )
                    org.apache.log4j.LogManager.getLogger(logger).setLevel(level);
            }
            catch (NoClassDefFoundError ex) {
                if ( ! log4jMsgLoggedOnce ) {
                    logAlways("NoClassDefFoundError (log4j)");
                    log4jMsgLoggedOnce = true;
                }
            }
        }

    }

    /** java.util.logging (JUL) setup */
    static class LoggingSetupJUL extends LoggingSetup {
        // Double initialization of JUL seesm to loose loggers. 
        
        @Override
        protected String getDisplayName() {
            return "JUL";
        }

        @Override
        protected void initFromInputStream(InputStream inputStream) throws IOException {
            java.util.logging.LogManager.getLogManager().readConfiguration(inputStream);
        }

        @Override
        protected String getLoggingSetupFilename() {
            return "logging.properties";
        }

        @Override
        protected String getSystemProperty() {
            return "java.util.logging.config.file";
        }

        @Override
        protected String getDefaultString() {
            return LoggingDefaults.defaultJUL;
        }

        @Override
        public void setLevel(String logger, String levelName) {
            java.util.logging.Level level = java.util.logging.Level.ALL;
            if ( levelName.equalsIgnoreCase("info") )
                level = java.util.logging.Level.INFO;
            else if ( levelName.equalsIgnoreCase("trace") )
                level = java.util.logging.Level.FINER;
            else if ( levelName.equalsIgnoreCase("debug") )
                level = java.util.logging.Level.FINE;
            else if ( levelName.equalsIgnoreCase("warn") || levelName.equalsIgnoreCase("warning") )
                level = java.util.logging.Level.WARNING;
            else if ( levelName.equalsIgnoreCase("error") )
                level = java.util.logging.Level.SEVERE;
            else if ( levelName.equalsIgnoreCase("OFF") )
                level = java.util.logging.Level.OFF;
            if ( level != null )
                java.util.logging.Logger.getLogger(logger).setLevel(level);
        }
    }

    /** Setup for when we don't do anything */
    static class LoggingSetupNoOp extends LoggingSetup {
        @Override
        protected String getDisplayName() {
            return "NoOp";
        }

        @Override
        protected String getLoggingSetupFilename() {
            return null;
        }

        @Override
        protected String getSystemProperty() {
            return null;
        }

        @Override
        protected String getDefaultString() {
            return null;
        }

        @Override
        protected void initFromInputStream(InputStream inputStream) throws IOException {}

        @Override
        public void setLevel(String logger, String level) {}
    }
}
