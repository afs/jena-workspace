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

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.FusekiEnv ;
import org.apache.jena.riot.SysRIOT ;
import org.apache.log4j.PropertyConfigurator ;

/** Set logging. Configuration for logging is chosen based on following steps until one succedes.
 *  <ol>
 *  <li> Is logging already initialized (test the system property).
 *  <li> Use <tt>file:{config}</tt> (for appropriate {config} filename).
 *  <li> Use <tt>file:$FUSEKI_BASE/{config}</tt> if exists
 *  <li> Look on the classpath: <tt>{config}</tt> as a java resource
 *  <li> Look on the classpath: <tt>org/apache/jena/fuseki/{config}</tt> as a java resource
 *  <li> Use a built-in default string.
 *  </ol>
 */
public abstract class FusekiLoggingUpgrade
{
    // See LoggingSetup
    
    public static void main(String...a) {
        
//        org.slf4j.Logger x = org.slf4j.LoggerFactory.getLogger("FOO") ;
//        // org.slf4j.impl.Log4jLoggerAdapater or org.slf4j.impl.JDK14LoggerAdapter
//        // Except too late!
        
        setLogging() ;
        
//        // Dev - direct to JUL.
//        new FusekiLoggingJUL().loggingSetup();
        java.util.logging.Logger LOG = java.util.logging.Logger.getLogger("Fuseki") ;
        LOG.info("Information1");
        
//        new FusekiLoggingLog4j().loggingSetup();
        org.apache.log4j.Logger LOG1 = org.apache.log4j.Logger.getLogger("Fuseki") ;
        LOG1.info("Information2");
    }
    // ---------------------------------
    
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

    private static void chooseLoggingProvider() {
        
    }
    
    /** Set up logging - standalone and war packaging */
    public static synchronized void setLogging() {
        if ( ! allowLoggingReset )
            return ;
        if ( loggingInitialized )
            return ;
        loggingInitialized = true ;
        // For FUSEKI_BASE
        FusekiEnv.setEnvironment() ;
        
        // Discover the binding for logging
        if ( checkForSimple( ) ) {
            // No specific setup.
            logLogging("Fuseki logging - slf4j-simple logging found") ;
            return ;
        }

        boolean hasLog4j = checkForLog4J() ;
        boolean hasJUL = checkForJUL() ;
        if ( hasLog4j && hasJUL ) {
            logAlways("Fuseki logging - Found both Log4j and JUL setup for SLF4J; using Log4j") ;
            return ;
        }
        if ( ! hasLog4j && ! hasJUL ) {
            // Do nothing - hope logging gets initialized automatically. e.g. logback.
            // In some ways this is the preferred outcome for the war file.
            // 
            // The standalone server we have to make a decision and it is better
            // if it uses the predefined format.  
            logLogging("Fuseki logging - Neither Log4j nor JUL setup for SLF4J") ;
            return ;
        }

        FusekiLoggingUpgrade x = null ;
        if ( hasLog4j )
            x = new FusekiLoggingLog4j() ;
        else if ( hasJUL )
            x = new FusekiLoggingJUL() ;
        x.loggingSetup() ;
    }
    
    // Need both "org.slf4j.impl.Log4jLoggerAdapater" and "org.apache.log4j.Logger"
    private static boolean checkForLog4J() {
        boolean bLog4j = checkForClass("org.apache.log4j.Logger") ;
        boolean bLog4jAdapter = checkForClass("org.slf4j.impl.Log4jLoggerAdapter") ;
        if ( bLog4j && bLog4jAdapter )
            return true ;
        if ( ! bLog4j && ! bLog4jAdapter )
            return false ;
        if ( ! bLog4j ) {
            logAlways("Classpath has the slf4j-log4j adapter but no log4j") ;
            return false ;
        }
        if ( ! bLog4jAdapter ) {
            logAlways("Classpath has log4j but no the slf4j-log4j adapter") ;
            return false ;
        }
        return false ;
    }
    
    private static boolean checkForJUL() {
        return checkForClass("org.slf4j.impl.JDK14LoggerAdapter") ;
    }

    private static boolean checkForSimple() {
        return checkForClass("org.slf4j.impl.SimpleLogger") ;
    }

    private static boolean checkForClass(String className) {
        try {
            Class.forName(className) ;
            return true ;
        }
        catch (ClassNotFoundException ex) {
            return false ;
        }
    }
 
    /** Log for the logging setup process */
    protected static void logLogging(String fmt, Object ... args) {
        if ( LogLogging ) {
            System.out.printf(fmt, args) ; 
            System.out.println() ;
        }
    }

    /** Log for the logging setup messages like warnings. */
    protected static void logAlways(String fmt, Object ... args) {
        System.out.printf(fmt, args) ; 
        System.out.println() ;
    }
    
    protected void loggingSetup() { 
        logLogging("Fuseki logging") ;
        
        if ( maybeAlreadySet() ) return ;
        
        logLogging("Fuseki logging - setup") ;
        String fn1 = getLoggingSetupFilename() ;
        String fn2 = null ;
    
        if ( FusekiEnv.FUSEKI_BASE != null ) 
            fn2 = FusekiEnv.FUSEKI_BASE.toString()+"/"+fn1 ;
        
        if ( tryFileFor(fn1) )
            return ;
        if ( tryFileFor(fn2) )
            return ;
        if ( tryClassPathFor(getLoggingSetupFilename()) )
            return ;
        if ( tryClassPathFor("org/apache/jena/fuseki/" + getLoggingSetupFilename()) )
            return ;
        defaultSetup();
    }

    /** Open by classpath or return null */
    protected URL getResource(String resourceName) {
        URL url = this.getClass().getClassLoader().getResource(resourceName) ;
        if ( url == null )
            return null ;
        // Skip any thing that looks like test code.
        if ( url.toString().contains("-tests.jar") || url.toString().contains("test-classes") )
            return null ;
        return url ;
    }
    
    protected abstract String getLoggingSetupFilename() ;
    protected abstract String getSystemProperty() ;
    protected abstract String getDefaultString() ;
    
    /** Configure logging from an input stream */
    protected abstract void initFromInputStream(InputStream inputStream) throws IOException ;

    /** Get a string that is the default configuration and use that to configure logging */
    protected void defaultSetup() {
        byte b[] = StrUtils.asUTF8bytes(getDefaultString()) ;
        try ( InputStream input = new ByteArrayInputStream(b) ) {
            initFromInputStream(input) ;
        } catch (IOException ex) { IO.exception(ex); }
    }
    
    /** Has logging been setup? */ 
    protected boolean maybeAlreadySet() {
        // No loggers have been created but configuration may have been set up.
        String propName = getSystemProperty() ;
        String x = System.getProperty(propName, null) ;
        if ( x != null ) {
            logLogging("%s = %s", propName, x) ;
            // The logging system will initialize in the usual way.
            // This includes a value of "set", which indicates that logging
            // was set before by some other Jena code.
            if ( x.equals("set") )
                Fuseki.serverLog.warn("Fuseki logging: Unexpected: Logging was setup by some other part of Jena") ;
            return true ;
        }
        return false ;
    }

    protected boolean tryFileFor(String fn) {
        if ( fn == null )
            return false ;
        logLogging("Fuseki logging - file %s", fn) ;
        try {
            File f = new File(fn) ;
            if ( f.exists() ) {
                logLogging("Fuseki logging - found file:"+fn) ;
                try ( InputStream input = new BufferedInputStream(new FileInputStream(f)) ) {
                    initFromInputStream(input) ;
                }
                System.setProperty(getSystemProperty(), "file:" + fn) ;
                return true ;
            }
        }
        catch (Throwable th) {}
        return false ;
    }

    protected boolean tryClassPathFor(String resourceName) {
          logLogging("Fuseki logging - classpath %s", resourceName) ;
          URL url = getResource(resourceName) ;                      
          if ( url != null ) {
              logLogging("Fuseki logging - found via classpath %s", url) ;
              if ( url != null ) {
                  try {
                      initFromInputStream(url.openStream()) ;
                  }
                  catch (IOException e) { IO.exception(e); }
                  System.setProperty(getSystemProperty(), url.toString()) ;
                  return true ;
              }
          }
          return false ;
    }

    /** Log4j setup */
    static class FusekiLoggingLog4j extends FusekiLoggingUpgrade {

        @Override
        protected void initFromInputStream(InputStream inputStream) throws IOException {
            PropertyConfigurator.configure(inputStream);
        }

        @Override
        protected String getLoggingSetupFilename() {
            return "log4j.properties" ;
        }

        @Override
        protected String getSystemProperty() {
            return "log4j.configuration" ;
        }

        @Override
        protected String getDefaultString() {
            return StrUtils.strjoinNL
                ("## Plain output to stdout",
                 "log4j.appender.jena.plainstdout=org.apache.log4j.ConsoleAppender",
                 "log4j.appender.jena.plainstdout.target=System.out",
                 "log4j.appender.jena.plainstdout.layout=org.apache.log4j.PatternLayout",
                 "log4j.appender.jena.plainstdout.layout.ConversionPattern=[%d{yyyy-MM-dd HH:mm:ss}] %-10c{1} %-5p %m%n",

                 "# Unadorned, for the requests log.",
                 "log4j.appender.fuseki.plain=org.apache.log4j.ConsoleAppender",
                 "log4j.appender.fuseki.plain.target=System.out",
                 "log4j.appender.fuseki.plain.layout=org.apache.log4j.PatternLayout",
                 "log4j.appender.fuseki.plain.layout.ConversionPattern=%m%n",

                 "## Most things", 
                 "log4j.rootLogger=INFO, jena.plainstdout",
                 "log4j.logger.com.hp.hpl.jena=WARN",
                 "log4j.logger.org.apache.jena=WARN",

                 "# Fuseki System logs.",
                 "log4j.logger." + Fuseki.serverLogName     + "=INFO",
                 "log4j.logger." + Fuseki.actionLogName     + "=INFO",
                 "log4j.logger." + Fuseki.adminLogName      + "=INFO",
                 "log4j.logger." + Fuseki.validationLogName + "=INFO",
                 "log4j.logger." + Fuseki.configLogName     + "=INFO",

                 "log4j.logger.org.apache.jena.tdb.loader=INFO",
                 "log4j.logger.org.eclipse.jetty=WARN" ,
                 "log4j.logger.org.apache.shiro=WARN",

                 "# NCSA Request Access log",
                 "log4j.additivity."+Fuseki.requestLogName   + "=false",
                 "log4j.logger."+Fuseki.requestLogName       + "=OFF, fuseki.plain",

                 "## Parser output", 
                 "log4j.additivity" + SysRIOT.riotLoggerName + "=false",
                 "log4j.logger." + SysRIOT.riotLoggerName + "=INFO, plainstdout"
                    ) ;
        }
    }

    /** java.util.logging (JUL) setup */
    static class FusekiLoggingJUL extends FusekiLoggingUpgrade {

        @Override
        protected void initFromInputStream(InputStream inputStream) throws IOException {
            java.util.logging.LogManager.getLogManager().readConfiguration(inputStream) ;
        }

        @Override
        protected String getLoggingSetupFilename() {
            return "logging.properties" ;
        }

        @Override
        protected String getSystemProperty() {
            return "java.util.logging.config.file" ;
        }
        
        @Override
        protected String getDefaultString() {
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
    }
}
