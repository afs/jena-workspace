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

package fuseki;

import java.io.* ;
import java.net.URL ;
import java.util.Objects ;
import java.util.logging.Logger ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.fuseki.webapp.FusekiEnv;
import org.apache.jena.riot.SysRIOT ;

/**
 * Set logging. Configuration for logging is chosen based on following steps
 * until one succeeds.
 * <ol>
 * <li>Is logging already initialized? (test the system property).
 * <li>Use file:{config} (for appropriate {config} name).
 * <li>Look on the classpath:{config} as a java resource
 * <li>Look on the classpath:{PathBase}/{config} as a java resource
 * <li>Use a default string.
 * </ol>
 * Support log4jv1 and JUL (java.util.logging).
 * This version does not discover the logging provider - it is coded in
 */

public abstract class FusekiLoggingUpgrade
{
    // XXX Add/complete log4j2

    public static void main(String...a) {
        // Dev - direct to JUL.
        new FusekiLoggingJUL().loggingSetup();
        /*JUL*/Logger LOG = Logger.getLogger("Fuseki") ;
        LOG.info("Information");
    }

    // This class must not have static constants, or otherwise not "Fuseki.*"
    // or any class else where that might kick off logging.  Otherwise, the
    // setLogging is pointless (it's already set).

    private static final boolean LogLogging     = false ;
    private static boolean loggingInitialized   = false ;
    private static boolean allowLoggingReset    = true ;

    /**
     * Switch off logging setting.
     * Used by the embedded server so that the application's
     * logging setup is not overwritten.
     */
    public static synchronized void allowLoggingReset(boolean value) {
        allowLoggingReset = value ;
    }

    /** Logging provider, with default of log4j v1 */
    private static FusekiLoggingUpgrade loggingProvider = new FusekiLoggingLog4j2() ;

    /** Set up logging - standalone and war packaging */
    public static synchronized void setLogging() {
        if ( ! allowLoggingReset )
            return ;
        if ( loggingInitialized )
            return ;
        loggingInitialized = true ;
        FusekiEnv.setEnvironment() ;
        loggingProvider.loggingSetup() ;
    }

    public static synchronized void setLogging(FusekiLoggingUpgrade provider) {
        Objects.requireNonNull(provider);
        loggingProvider = provider;
    }

    protected abstract void loggingSetup() ;

    protected static void logLogging(String fmt, Object ... args) {
        if ( LogLogging ) {
            System.out.printf(fmt, args) ;
            System.out.println() ;
        }
    }

    /** Opne by classpath or return null */
    protected URL getResource(String resourceName) {
        URL url = this.getClass().getClassLoader().getResource(resourceName) ;
        if ( url == null )
            return null ;
        // Skip any thing that looks like test code.
        if ( url.toString().contains("-tests.jar") || url.toString().contains("test-classes") )
            return null ;
        return url ;
    }

    // XXX
    static class FusekiLoggingLog4j2 extends FusekiLoggingUpgrade {
        // Set logging for Log4j v2
        // 1/ Use log4j.configurationFile if defined (standard log4j2 initialization)
        // 2/ Use file:log4j.properties if exists
        // 3/ Use log4j.properties on the classpath.
        // 4/ Use built-in org/apache/jena/fuseki/log4j2.properties on the classpath.
        // 5/ Use default string

        /** Files for the log4j properties file at (2) */
        private static final String[] filesForLog4j2Properties = {
            "log4j2.properties", "log4j2.yaml", "log4j2.yml", "log4j2.json", "log4j2.jsn", "log4j2.xml",
        } ;

        /** Places for the log4j properties file at (3) */
        private static final String[] resourcesForLog4j2Properties = {
            "log4j2.properties", "log4j2.yaml", "log4j2.yml", "log4j2.json", "log4j2.jsn", "log4j2.xml",
            // Possible built in/.
            "org/apache/jena/fuseki/log4j2.properties"
        } ;

        @Override
        protected void loggingSetup() {
            logLogging("Fuseki logging") ;
            // No loggers have been created but configuration may have been set up.
            String x = System.getProperty("log4j.configurationFile", null) ;
            logLogging("log4j.configurationFile = %s", x) ;

            if ( x != null ) {
                // log4j will initialize in the usual way. This includes a value of
                // "set", which indicates that logging was set before by some other Jena code.
                if ( x.equals("set") )
                    Fuseki.serverLog.warn("Fuseki logging: Unexpected: Log4j2 was setup by some other part of Jena") ;
                return ;
            }
            logLogging("Fuseki logging - setup") ;
            // Look for a log4j2.properties in the current working directory
            // and an existing FUSEKI_BASE for easy customization.

            for (String fn1 : filesForLog4j2Properties ) {
                if ( attemptLog4j2(fn1) )
                    return ;
            }

            if ( FusekiEnv.FUSEKI_BASE != null ) {
                for (String fn1 : filesForLog4j2Properties ) {
                    String fn2 = FusekiEnv.FUSEKI_BASE.toString()+"/"+fn1;
                    if ( attemptLog4j2(fn2) )
                        return ;
                }
            }

            // Try classpath
            for ( String resourceName : resourcesForLog4j2Properties ) {
                //logLogging("Fuseki logging - classpath %s", resourceName) ;
                URL url = getResource(resourceName) ;
                if ( url != null ) {
                    // XXX Log4j2
                    logLogging("Fuseki logging - found via classpath %s", url) ;
                    System.setProperty("log4j.configuration", url.toString()) ;
                    return ;
                }
            }
            // Use builtin.
            logLogging("Fuseki logging - Fallback log4j.properties string") ;
            String dftLog4j = log4J2setupFallback() ;
            FusekiLogging.resetLogging(dftLog4j);
        }

        private static boolean attemptLog4j2(String fn) {
            if ( fn == null )
                return false ;
            try {
                File f = new File(fn) ;
                if ( f.exists() ) {
                    logLogging("Fuseki logging - found file:"+fn) ;
                    // XXX Log4j2
                    System.setProperty("log4j.configuration", "file:" + fn) ;
                    return true ;
                }
            }
            catch (Throwable th) {}
            return false ;
        }

        private static String log4J2setupFallback() {
            return StrUtils.strjoinNL
                // Preferred: classes/log4j.properties, from src/main/resources/log4j.properties
                // Keep these in-step.  Different usages cause different logging initalizations;
                // if the jar is rebundled, it may loose the associated log4.properties file.
                ("## Plain output to stdout",
                    "") ;
        }
    }

//    static class FusekiLoggingLog4j1 extends FusekiLoggingUpgrade {
//        // Set logging for Log4j v1
//        // 1/ Use log4j.configuration if defined (standard log4j initialization)
//        // 2/ Use file:log4j.properties if exists
//        // 3/ Use log4j.properties on the classpath.
//        // 4/ Use built-in org/apache/jena/fuseki/log4j.properties on the classpath.
//        // 5/ Use default string
//
//        /** Places for the log4j properties file at (3) */
//        private static final String[] resourcesForLog4j1Properties = {
//            "log4j.properties",
//            "org/apache/jena/fuseki/log4j.properties"
//        } ;
//
//        @Override
//        protected void loggingSetup() {
//            logLogging("Fuseki logging") ;
//            // No loggers have been created but configuration may have been set up.
//            String x = System.getProperty("log4j.configuration", null) ;
//            logLogging("log4j.configuration = %s", x) ;
//
//            if ( x != null ) {
//                // log4j will initialize in the usual way. This includes a value of
//                // "set", which indicates that logging was set before by some other Jena code.
//                if ( x.equals("set") )
//                    Fuseki.serverLog.warn("Fuseki logging: Unexpected: Log4j1 was setup by some other part of Jena") ;
//                return ;
//            }
//            logLogging("Fuseki logging - setup") ;
//            // Look for a log4j.properties in the current working directory
//            // and an existing FUSEKI_BASE for easy customization.
//            String fn1 = "log4j.properties" ;
//            String fn2 = null ;
//
//            if ( FusekiEnv.FUSEKI_BASE != null )
//                fn2 = FusekiEnv.FUSEKI_BASE.toString()+"/log4j.properties" ;
//            if ( attemptLog4j1(fn1) ) return ;
//            if ( attemptLog4j1(fn2) ) return ;
//
//            // Try classpath
//            for ( String resourceName : resourcesForLog4j1Properties ) {
//                // The log4j general initialization is done in a class static
//                // in LogManager so it can't be called again in any sensible manner.
//                // Instead, we include the same basic mechanism ...
//                logLogging("Fuseki logging - classpath %s", resourceName) ;
//                URL url = getResource(resourceName) ;
//                if ( url != null ) {
//                    System.err.println("NOT UPGRADED to log4j2");
//                    throw new RuntimeException();
////                    org.apache.log4j.PropertyConfigurator.configure(url) ;
////                    logLogging("Fuseki logging - found via classpath %s", url) ;
////                    System.setProperty("log4j.configuration", url.toString()) ;
////                    return ;
//                }
//            }
//            // Use builtin.
//            logLogging("Fuseki logging - Fallback log4j.properties string") ;
//            String dftLog4j = log4j1setupFallback() ;
//            FusekiLogging.resetLogging(dftLog4j);
//        }
//
//        private static boolean attemptLog4j1(String fn) {
//            if ( fn == null )
//                return false ;
//            try {
//                File f = new File(fn) ;
//                if ( f.exists() ) {
//                    logLogging("Fuseki logging - found file:"+fn) ;
//                    System.err.println("NOT UPGRADED to log4j2");
//                    throw new RuntimeException();
////                    org.apache.log4j.PropertyConfigurator.configure(fn) ;
////                    System.setProperty("log4j.configuration", "file:" + fn) ;
////                    return true ;
//                }
//            }
//            catch (Throwable th) {}
//            return false ;
//        }
//
//        private static String log4j1setupFallback() {
//            return StrUtils.strjoinNL
//                // Preferred: classes/log4j.properties, from src/main/resources/log4j.properties
//                // Keep these in-step.  Different usages cause different logging initalizations;
//                // if the jar is rebundled, it may loose the associated log4.properties file.
//                ("## Plain output to stdout",
//                 "log4j.appender.jena.plainstdout=org.apache.log4j.ConsoleAppender",
//                 "log4j.appender.jena.plainstdout.target=System.out",
//                 "log4j.appender.jena.plainstdout.layout=org.apache.log4j.PatternLayout",
//                 "log4j.appender.jena.plainstdout.layout.ConversionPattern=[%d{yyyy-MM-dd HH:mm:ss}] %-10c{1} %-5p %m%n",
//                 //"log4j.appender.jena.plainstdout.layout.ConversionPattern=%d{HH:mm:ss} %-10c{1} %-5p %m%n",
//
//                 "# Unadorned, for the requests log.",
//                 "log4j.appender.fuseki.plain=org.apache.log4j.ConsoleAppender",
//                 "log4j.appender.fuseki.plain.target=System.out",
//                 "log4j.appender.fuseki.plain.layout=org.apache.log4j.PatternLayout",
//                 "log4j.appender.fuseki.plain.layout.ConversionPattern=%m%n",
//
//                 "## Most things",
//                 "log4j.rootLogger=INFO, jena.plainstdout",
//                 "log4j.logger.com.hp.hpl.jena=WARN",
//                 "log4j.logger.org.apache.jena=WARN",
//
//                 "# Fuseki System logs.",
//                 "log4j.logger." + Fuseki.serverLogName     + "=INFO",
//                 "log4j.logger." + Fuseki.actionLogName     + "=INFO",
//                 "log4j.logger." + Fuseki.adminLogName      + "=INFO",
//                 "log4j.logger." + Fuseki.validationLogName + "=INFO",
//                 "log4j.logger." + Fuseki.configLogName     + "=INFO",
//
//                 "log4j.logger.org.apache.jena.tdb.loader=INFO",
//                 "log4j.logger.org.eclipse.jetty=WARN" ,
//                 "log4j.logger.org.apache.shiro=WARN",
//
//                 "# NCSA Request Access log",
//                 "log4j.additivity."+Fuseki.requestLogName   + "=false",
//                 "log4j.logger."+Fuseki.requestLogName       + "=OFF, fuseki.plain",
//
//                 "## Parser output",
//                 "log4j.additivity" + SysRIOT.riotLoggerName + "=false",
//                 "log4j.logger." + SysRIOT.riotLoggerName + "=INFO, plainstdout"
//                    ) ;
//        }
//    }

    static class FusekiLoggingJUL extends FusekiLoggingUpgrade {
        // Set logging for java.util.logging (JUL)
        // 1/ Use -Djava.util.logging.config.file if defined (standard JUL initialization)
        // 2/ Use file:logging.properties if exists
        // 3/ Use logging.properties on the classpath.
        // 4/ Use built-in org/apache/jena/fuseki/logging.properties on the classpath.
        // 5/ Use default string

        private static final String[] resourcesForJULProperties = {
            "logging.properties",
            "org/apache/jena/fuseki/logging.properties"
        } ;

        @Override
        protected void loggingSetup() {
            logLogging("Fuseki logging") ;
            String x = System.getProperty("java.util.logging.config.file", null) ;
            logLogging("java.util.logging.config.file = %s", x) ;
            if ( x != null ) {
                // JUL will initialize in the usual way. This includes a value of
                // "set", which indicates that logging was set before by some other Jena code.
                if ( x.equals("set") )
                    Fuseki.serverLog.warn("Fuseki logging: Unexpected: ava.util.logging.config.file was setup by some other part of Jena") ;
                return ;
            }
            logLogging("Fuseki logging - setup") ;
            // Look for a logging.properties in the current working directory
            // and an existing FUSEKI_BASE for easy customization.
            String fn1 = "logging.properties" ;
            String fn2 = null ;

            if ( FusekiEnv.FUSEKI_BASE != null )
                fn2 = FusekiEnv.FUSEKI_BASE.toString()+"/logging.properties" ;
            if ( attemptJUL(fn1) ) return ;
            if ( attemptJUL(fn2) ) return ;

            // Try classpath (c.f. log4j)
            for ( String resourceName : resourcesForJULProperties ) {
                logLogging("Fuseki logging - classpath %s", resourceName) ;
                URL url = getResource(resourceName) ;
                if ( url != null ) {
                    try {
                        initJULbyInputStream(url.openStream()) ;
                    }
                    catch (IOException e) { IO.exception(e); }
                    logLogging("Fuseki logging - found via classpath %s", url) ;
                    return ;
                }
            }
            // Use builtin.
            logLogging("Fuseki logging - Fallback JUL string") ;
            String dftJUL = jul_setupFallback() ;
            // XXX LogCtl should provide this.
            // LogCtl.resetLoggingJUL(dftJUL) ;
            byte b[] = StrUtils.asUTF8bytes(dftJUL) ;
            try ( InputStream in = new ByteArrayInputStream(b) ) {
                initJULbyInputStream(in) ;
            } catch (Exception ex) {
                throw new AtlasException(ex) ;
            }
        }

        private static boolean attemptJUL(String fn) {
            if ( fn == null )
                return false ;
            try {
                File f = new File(fn) ;
                if ( f.exists() ) {
                    logLogging("Fuseki logging - found file:"+fn) ;
                    initJULbyFile(fn);
                    return true ;
                }
            }
            catch (Throwable th) {}
            return false ;
        }

        private static String jul_setupFallback() {
            return StrUtils.strjoinNL
                ("handlers=org.apache.jena.atlas.logging.java.ConsoleHandlerStream" ,
                 //Log4j : [%d{yyyy-MM-dd HH:mm:ss}] %-10c{1} %-5p %m%n",
                 "org.apache.jena.atlas.logging.java.TextFormatter.format = [%5$tF %5$tT] %2$-10s %3$-5s %6$s" ,
                 "org.apache.jena.level             = INFO" ,
                 "# Fuseki System logs.",
                 Fuseki.serverLogName     + ".level  = INFO" ,
                 Fuseki.actionLogName     + ".level  = INFO" ,
                 Fuseki.adminLogName      + ".level  = INFO" ,
                 Fuseki.validationLogName + ".level  = INFO" ,
                 Fuseki.configLogName     + ".level  = INFO" ,

                 "# NCSA Request Access log",
                 Fuseki.requestLogName+".level       = OFF",
                 Fuseki.requestLogName+".useParentHandlers = false",
                 Fuseki.requestLogName+".handlers    = org.apache.jena.atlas.logging.java.FlatHandler",

                 "# Others",
                 "org.eclipse.jetty.level            = WARNING" ,
                 "org.apache.shiro.level             = WARNING" ,

                 "## Parser output",
                 SysRIOT.riotLoggerName + ".useParentHandlers = false",
                 SysRIOT.riotLoggerName+".level      = INFO" ,
                 SysRIOT.riotLoggerName+".handlers   = org.apache.jena.atlas.logging.java.FlatHandler"
                ) ;
        }

        // XXX Merge into LogCtl
        private static void initJULbyInputStream(InputStream inputStream) throws IOException {
            try ( InputStream input2 = new BufferedInputStream(inputStream) ) {
                java.util.logging.LogManager.getLogManager().readConfiguration(input2) ;
            }
        }

        private static void initJULbyFile(String filename) throws IOException {
            try ( InputStream input = new FileInputStream(filename) ) {
                initJULbyInputStream(input) ;
            }
        }

        private static void initJULbyString(String string) throws IOException {
            byte b[] = StrUtils.asUTF8bytes(string) ;
            try ( InputStream input = new ByteArrayInputStream(b) ) {
                initJULbyInputStream(input) ;
            }
        }

    }
}

