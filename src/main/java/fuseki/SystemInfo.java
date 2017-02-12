package fuseki;

import java.io.IOException;
import java.util.function.Function;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.slf4j.Logger;

public class SystemInfo {
    
    public static void main(String ...args) throws IOException {
        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usedMem = totalMem - freeMem;
        Function<Long, String> f = SystemInfo::strNumMixed;
        
        System.out.printf("max=%s  total=%s  used=%s  free=%s\n", f.apply(maxMem), f.apply(totalMem), f.apply(usedMem), f.apply(freeMem));
    }
    
    public static void logDetails(Logger log) {
        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usedMem = totalMem - freeMem;
        Function<Long, String> f = SystemInfo::strNum2;
        //FmtLog.info(log, "Memory: max=%s  total=%s  used=%s  free=%s", f.apply(maxMem), f.apply(totalMem), f.apply(usedMem), f.apply(freeMem));
        FmtLog.info(log, "  Memory: max=%s", f.apply(maxMem));
        //FmtLog.info(log, "Apache Jena %s", Jena.VERSION);
        FmtLog.info(log, "  Apache Jena Fuseki %s", Fuseki.VERSION);
        FmtLog.info(log, "  OS: %s %s %s", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        FmtLog.info(log, "  Java: %s", System.getProperty("java.version"));
        //FmtLog.info(log, "Java %s", System.getProperty("java.runtime.version"));
    }
    
    public static void logDetailsVerbose(Logger log) {
        logDetails(log);
        logOne(log, "java.vendor");
        logOne(log, "java.home");
        logOne(log, "java.runtime.version");
        logOne(log, "java.runtime.name");
        //logOne(log, "java.endorsed.dirs");
        logOne(log, "user.language");
        logOne(log, "user.timezone");
        logOne(log, "user.country");
        logOne(log, "user.dir");
        //logOne(log, "file.encoding");
    }
    
    private static void logOne(Logger log, String property) {
        FmtLog.info(log, "    %s = %s", property, System.getProperty(property));
    }

    /** Create a human-friendly string for a number based on Kilo/Mega/Giga/Tera (powers of 2) */
    public static String strNumMixed(long x) {
        // https://en.wikipedia.org/wiki/Kibibyte
        if ( x < 1024 )
            return Long.toString(x);
        if ( x < 1024*1024 )
            return String.format("%.1fK", x/1024.0);
        if ( x < 1024*1024*1024 )
            return String.format("%.1fM", x/(1024.0*1024));
        if ( x < 1024L*1024*1024*1024 )
            return String.format("%.1fG", x/(1024.0*1024*1024));
        return String.format("%.1fT", x/(1024.0*1024*1024*1024));
    }
    

    /** Create a human-friendly string for a number based on Kilo/Mega/Giga/Tera (powers of 10) */
    public static String strNum10(long x) {
        if ( x < 1_000 )
            return Long.toString(x);
        if ( x < 1_000_000 )
            return String.format("%.1fK", x/1000.0);
        if ( x < 1_000_000_000 )
            return String.format("%.1fM", x/(1000.0*1000));
        if ( x < 1_000_000_000_000L )
            return String.format("%.1fG", x/(1000.0*1000*1000));
        return String.format("%.1fT", x/(1000.0*1000*1000*1000));
    }
    
    /** Create a human-friendly string for a number based on Kibi/Mebi/Gibi/Tebi (powers of 2) */
    public static String strNum2(long x) {
        // https://en.wikipedia.org/wiki/Kibibyte
        if ( x < 1024 )
            return Long.toString(x);
        if ( x < 1024*1024 )
            return String.format("%.1f KiB", x/1024.0);
        if ( x < 1024*1024*1024 )
            return String.format("%.1f MiB", x/(1024.0*1024));
        if ( x < 1024L*1024*1024*1024 )
            return String.format("%.1f GiB", x/(1024.0*1024*1024));
        return String.format("%.1fTiB", x/(1024.0*1024*1024*1024));
    }
}
