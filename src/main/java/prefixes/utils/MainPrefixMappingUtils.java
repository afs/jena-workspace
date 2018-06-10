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

package prefixes.utils;

public class MainPrefixMappingUtils {}
//
//import static org.apache.jena.util.PrefixMappingUtils.*;
//
//import java.io.StringReader ;
//
//import org.apache.jena.atlas.lib.StrUtils ;
//import org.apache.jena.atlas.logging.LogCtl ;
//import org.apache.jena.rdf.model.Model ;
//import org.apache.jena.rdf.model.ModelFactory ;
//import org.apache.jena.riot.Lang ;
//import org.apache.jena.riot.RDFDataMgr ;
//import org.apache.jena.shared.PrefixMapping ;
//import org.apache.jena.util.PrefixMappingUtils;
//
//public class MainPrefixMappingUtils {
//    static { LogCtl.setLog4j(); }
//
//    public static void main(String ...a) {
//        Model m = ModelFactory.createDefaultModel() ;
//        RDFDataMgr.read(m, new StringReader(data), null, Lang.TURTLE);
//        
//        Model m2 = ModelFactory.createModelForGraph(PrefixMappingUtils.graphInUsePrefixMapping(m.getGraph())) ;
//        
//        RDFDataMgr.write(System.out, m, Lang.TTL) ;
//        System.out.println("-----------------");
//        RDFDataMgr.write(System.out, m2, Lang.TTL) ;
//        System.out.println("-----------------");
//        System.exit(0) ;
//        {
//            PrefixMapping pmap = calcInUsePrefixMapping(m.getGraph()) ;
//            printPMap(pmap) ;
//        }
//        
//        System.out.println("-----------------");
//        {
//            PrefixMapping pmap = calcInUsePrefixMappingTTL(m.getGraph()) ;
//            printPMap(pmap) ;
//        }
//        
////        Set<String> inUseURIs_2 = fullMethod(m);
////        print(inUseURIs_2, m);
////        System.out.println("-----------------");
////        Set<String> inUseURIs_1 = splitMethod(m) ;
////        print(inUseURIs_1, m);
////        System.out.println("-----------------");
//        
////        System.out.println("-----------------");
////        WriterGraphRIOT wg = RDFDataMgr.createGraphWriter(Lang.TURTLE) ;
////        wg.write(System.out, m.getGraph(), PrefixMapFactory.create(pmap), null, null) ;
//        
//    }
//    
//    
//    private static void printPMap(PrefixMapping pmap) {
//        pmap.getNsPrefixMap().forEach((u,p)-> System.out.printf("  %-10s -> <%s>\n", u+":", p)) ;
//    }
//    
//    // Data.
//    static String data = StrUtils.strjoinNL
//        ("PREFIX : <http://example/>" ,
//         "PREFIX ex: <http://example/ex#>" ,
//         "PREFIX notinuse: <http://example/whatever/>" ,
//         "PREFIX indirect: <urn:foo:>" ,
//         "PREFIX indirectx: <urn:x:>" ,
//
//         "PREFIX ns: <http://host/ns>" ,
//         "PREFIX ns1: <http://host/ns1>" ,
//         "PREFIX ns2: <http://host/nspace>" ,
//         "" ,
//         ":s1 :p :x1 ." ,
//         ":s1 ex:p :x1 ." ,
//
//         "<urn:foo:bar> :p 1 . ",
//         "<urn:x:a:b> :p 2 . ",
//
//         "<urn:verybad#.> :p 1 . ",
//
//         "ns:x ns1:p 'ns1' . ",
//
//         "<http://examp/abberev> indirect:p 'foo' . "
//         ) ;
//    
//    
//    // ----- Other
//    
//
//}
