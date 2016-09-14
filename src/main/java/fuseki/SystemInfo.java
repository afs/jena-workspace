package fuseki;

import java.io.IOException ;

public class SystemInfo {
    
    public static void main(String ...args) throws IOException {
        long maxMem = Runtime.getRuntime().maxMemory() ;
        long totalMem = Runtime.getRuntime().totalMemory() ;
        long freemem = Runtime.getRuntime().freeMemory() ;
        System.out.printf("max=%s  total=%s   free=%s\n", strNum(maxMem), strNum(totalMem), strNum(freemem)) ;
        
        //System.getProperties().forEach((k,v)->System.out.printf("%-30s %s\n", k,v)) ;
        
//        // Heap settings from _Xmx
//        
//        // Version info
//        //Jena.VERSION ;
//        
//        // getServletContext().getServerInfo() 
//        // getServletContext().getMajorVersion()
//        // getServletContext().getMinorVersion()
//        // getServletContext().getContextPath()
//        // request.getContextPath()
//        
//        System.getProperty("java.version")
//        // Other system properties
        // java.io.tmpdir
        //java.runtime.version
        // java.vm.name
//        System.getProperty("java.library.path") ;
//        System.getProperty("os.name")
//        System.getProperty("os.version")
//        System.getProperty("os.arch")
    }
    
    /** Create a human-friendly string for a number based on K/M/G */
    static String strNum(long x) {
        if ( x < 1_000 )
            return Long.toString(x) ;
        if ( x < 1_000_000 )
            return String.format("%.1fK", x/1000.0) ;
        if ( x < 1_000_000_000 )
            return String.format("%.1fM", x/(1000.0*1000)) ;
        return String.format("%.1fG", x/(1000.0*1000*1000)) ;
    }
    
}
