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

package dev;

import java.io.StringReader ;
import java.util.* ;
import java.util.function.Consumer ;
import java.util.stream.Collectors ;

import org.apache.jena.atlas.lib.SetUtils ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.lib.Trie ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.util.SplitIRI ;

public class ManagePrefixes {
    public static void main(String ...a) {
        Model m = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(m, new StringReader(data), null, Lang.TURTLE);
        
        RDFDataMgr.write(System.out, m, Lang.TTL) ;
        System.out.println("-----------------");
        printPMap(m) ;
        System.out.println("-----------------");
        
        {
            PrefixMapping pmap = calcInUsePrefixMapping(m.getGraph()) ;
            printPMap(pmap) ;
        }
        
        System.out.println("-----------------");
        {
            PrefixMapping pmap = calcInUsePrefixMappingTTL(m.getGraph()) ;
            printPMap(pmap) ;
        }
        
//        Set<String> inUseURIs_2 = fullMethod(m);
//        print(inUseURIs_2, m);
//        System.out.println("-----------------");
//        Set<String> inUseURIs_1 = splitMethod(m) ;
//        print(inUseURIs_1, m);
//        System.out.println("-----------------");
        
//        System.out.println("-----------------");
//        WriterGraphRIOT wg = RDFDataMgr.createGraphWriter(Lang.TURTLE) ;
//        wg.write(System.out, m.getGraph(), PrefixMapFactory.create(pmap), null, null) ;
        
    }
    
    private static void printPMap(PrefixMapping pmap) {
        pmap.getNsPrefixMap().forEach((u,p)-> System.out.printf("  %-10s -> <%s>\n", u+":", p)) ;
    }
    
    /**
     * Analyse the graph to see which prefixes of the graph are in use.
     * <p>
     * In the case of overlapping prefixes (where one prefix declaration is has an initial
     * URI string which imatches another prefix declaration), all are included, though
     * they may not be used when printing (that depends on the output process). In effect,
     * this process has "false positives".
     * <p>
     * This function does not calculate new prefixes.
     * @see #calcInUsePrefixMappingTTL(Graph)
     */
    public static PrefixMapping calcInUsePrefixMapping(Graph graph) {
        /* Method:
         * 
         * For each URI in the data, look it up in the trie.
         * after the last ":" for URNs, then see if that is a declared prefix.
         * 
         * Exit early if every prefix is accounted for. 
         */
        if ( graph.getPrefixMapping() == null )
            return null ;
        
        PrefixMapping prefixMapping = graph.getPrefixMapping() ;
        // Map prefix to URI.
        Map<String, String> pmap = prefixMapping.getNsPrefixMap() ;
        
        // Map URI to prefix, with partial lookup (all uri keys that partly match the URI) 
        Trie<String> trie = new Trie<>() ; 
        // Chnage this to "add(uri, uri)" to calculate the uris. 
        pmap.forEach((prefix,uri)-> trie.add(uri, prefix)) ;
        Iterator<Triple> iter = graph.find(null, null, null) ;
        // Prefixes in use.
        // (URIs if "add(uri, uri)")
        Set<String> inUse = new HashSet<>() ;
        
        // Process to apply to each node
        Consumer<Node> process = (node)->{
            if ( ! node.isURI() )
                return ;
            String uri = node.getURI() ;
            // Get all prefixes whose URIs are candidates 
            List<String> hits = trie.partialSearch(uri) ;
            if ( hits == null || hits.isEmpty() )
                return ;
            inUse.addAll(hits) ;
        } ;
        
        while(iter.hasNext()) {
            Triple triple = iter.next() ;
            process.accept(triple.getSubject()) ;
            process.accept(triple.getPredicate()) ;
            process.accept(triple.getObject()) ; 
            if ( pmap.size() == inUse.size() )
                break ;
        }
        
        if ( pmap.size() == inUse.size() )
            return prefixMapping ;

        // Build result.
        PrefixMapping pmap2 = new PrefixMappingImpl() ;
        inUse.forEach((prefix)-> pmap2.setNsPrefix(prefix, prefixMapping.getNsPrefixURI(prefix)) ) ;
        return pmap2 ;
    }

    /**
     * Analyse the graph to see which prefixes of the graph are in use.
     * <p>
     * This function attempts to process each URI in the graph as if it were to be printed
     * in Turtle. Only prefixes that lead to valid output strings are returned. This is
     * more expensive than {@link #calcInUsePrefixMapping(Graph)}.
     * <p>
     * This function does not calculate new prefixes.
     * 
     * @see #calcInUsePrefixMappingTTL(Graph)
     */
    public static PrefixMapping calcInUsePrefixMappingTTL(Graph graph) {
        /* Method:
         * 
         * For each URI, split in in the usual place, after "/" or "#" for http URIs, and
         * after the last ":" for URNs, then see if that is a declared prefix.
         * 
         * Exit early if every prefix is accounted for. 
         */
        if ( graph.getPrefixMapping() == null )
            return null ;
        PrefixMapping prefixMapping = graph.getPrefixMapping() ;
        // Map prefix -> URI.
        Map<String, String> pmap = prefixMapping.getNsPrefixMap() ;    
        
        // All URIs used as prefixes in the prefix mapping.
        Set<String> prefixURIs = new HashSet<>(pmap.values()) ;             
        
        // Prefixes used.
        Set<String> inUse = new HashSet<>() ;
        // Process to be applied to each node in the graph.
        Consumer<Node> process = (node) -> {
            if ( ! node.isURI() )
                return ;
            String uri = node.getURI() ;
            
            int idx = SplitIRI.splitpoint(uri) ;
            if ( idx < 0 )
                return ;
            String nsURI = SplitIRI.namespaceTTL(uri) ;
            String prefix = prefixMapping.getNsURIPrefix(nsURI) ;
            if ( prefix != null )
                inUse.add(prefix) ;
        } ;
        
        Iterator<Triple> iter = graph.find(null, null, null) ;
        while(iter.hasNext()) {
            Triple triple = iter.next() ;
            process.accept(triple.getSubject()) ; 
            process.accept(triple.getPredicate()) ;
            process.accept(triple.getObject()) ; 
            if ( inUse.size() == prefixURIs.size() )
                // Fast exit.
                break ;
        }
        
        if ( pmap.size() == inUse.size() )
            return prefixMapping ;

        // Build result.
        PrefixMapping pmap2 = new PrefixMappingImpl() ;
        inUse.forEach((prefix)-> pmap2.setNsPrefix(prefix, prefixMapping.getNsPrefixURI(prefix)) ) ;
        return pmap2 ;
    }
    
    // Data.
    static String data = StrUtils.strjoinNL
        ("PREFIX : <http://example/>" ,
         "PREFIX ex: <http://example/ex#>" ,
         "PREFIX notinuse: <http://example/whatever/>" ,
         "PREFIX indirect: <urn:foo:>" ,
         "PREFIX indirectx: <urn:x:>" ,

         "PREFIX ns: <http://host/ns>" ,
         "PREFIX ns1: <http://host/ns1>" ,
         "PREFIX ns2: <http://host/nspace>" ,
         "" ,
         ":s1 :p :x1 ." ,
         ":s1 ex:p :x1 ." ,

         "<urn:foo:bar> :p 1 . ",
         "<urn:x:a:b> :p 2 . ",

         "<urn:verybad#.> :p 1 . ",

         "ns:x ns1:p 'ns1' . ",

         "<http://examp/abberev> indirect:p 'foo' . "
         ) ;
    
    
    // ----- Other
    
    /** Check every URI as a possible use of a prefix */ 
    private static Set<String> fullMethod(Model m) {
        /* Method: Covers prefixes not based on "/", "#" or final ":" splitting.  
         * 
         * Build a trie to use as a partial lookup matcher.
         * For each URI in the data, look it up as a partial match in the trie
         * to get all uris in the prefix map that apply.  
         */
        
        // Map prefix to URI.
        Map<String, String> pmap = m.getNsPrefixMap() ;
        // Map URI to prefix, with partial lookup (all uri keys that partly match the URI) 
        Trie<String> trie = new Trie<>() ;                      
        
        // change to add(uri, prefix) to get prefixes.
        pmap.forEach((prefix,uri)-> trie.add(uri, uri)) ;   
        
        Iterator<Triple> iter = m.getGraph().find(null, null, null) ;
        // Prefix URIs in use.
        Set<String> inUseURIs = new HashSet<>() ;
        while(iter.hasNext()) {
            Triple triple = iter.next() ;
            processFull(trie, inUseURIs, triple.getSubject()) ; 
            processFull(trie, inUseURIs, triple.getPredicate()) ;
            processFull(trie, inUseURIs, triple.getObject()) ; 
            if ( pmap.size() == inUseURIs.size() )
                break ;
        }
        return inUseURIs ;
    }

    private static void processFull(Trie<String> trie, Set<String> prefixesInUse, Node node) {
        if ( ! node.isURI() )
            return ;
        String uri = node.getURI() ;
        // Shorten to "/" or "#" or ":"
        String pref = uri ;
        
        // Get all under the pref
        List<String> hits = trie.partialSearch(pref) ;
        if ( hits == null || hits.isEmpty() )
            return ;
        //System.out.println(pref+" => ("+hits.size()+")"+hits) ;
        prefixesInUse.addAll(hits) ;
    }
    
    // -------------------------------------------

    /** Assume that prefixes:localName are in the normal places (after/ or #).
     * i.e. node.getNameSpace() ; makes sense.
     * @param m
     */
    private static Set<String> splitMethod(Model m) {
        /* Method:
         * 
         * For each URI, split in in the usual place, after "/" or "#" for http URIs, and
         * after the last ":" for URNs, then see if that is a declared prefix.
         * 
         * Exit early if every prefix is accounted for. 
         */
        
        PrefixMapping prefixMapping = m ;
        // Map prefix -> URI.
        Map<String, String> pmap = prefixMapping.getNsPrefixMap() ;    
        
        // All URIs used as prefixes in the prefix mapping.
        Set<String> prefixURIs = new HashSet<>(pmap.values()) ;             
        
        // Prefix URIs used.
        Set<String> inUsePrefixURIs = new HashSet<>() ;
        
        Iterator<Triple> iter = m.getGraph().find(null, null, null) ;
        while(iter.hasNext()) {
            Triple triple = iter.next() ;
            processBySplit(prefixURIs, inUsePrefixURIs, triple.getSubject()) ; 
            processBySplit(prefixURIs, inUsePrefixURIs, triple.getPredicate()) ;
            processBySplit(prefixURIs, inUsePrefixURIs, triple.getObject()) ; 
            if ( inUsePrefixURIs.size() == prefixURIs.size() )
                // Fast exit.
                break ;
        }
        return inUsePrefixURIs ;
    }
        
    private static void processBySplit(Collection<String> prefixURIs, Collection<String> inUse, Node node) {
        if ( ! node.isURI() )
            return ;
        String uri = node.getURI() ;
        
        int idx = SplitIRI.splitpoint(uri) ;
        if ( idx < 0 )
            return ;
        String prefixUri = uri.substring(0,idx) ;
        String localname = uri.substring(idx) ;
        
        if ( prefixURIs.contains(prefixUri) )
            inUse.add(prefixUri) ;
//        String ns = node.getNameSpace() ;
//        if ( ns == null )
//            return ;
//        if ( prefixURIs.contains(ns) )
//            inUse.add(ns) ;
    }
    
    private static void print(Set<String> inUsePrefixURIs, PrefixMapping prefixMapping) {    
        // Convert to prefixes.
        Set<String> inUsePrefixes = urisToPrefixes(prefixMapping, inUsePrefixURIs) ;
        // ----- Analysis
        
        System.out.println("In use: "+inUsePrefixURIs) ;
        System.out.println("In use: "+inUsePrefixes) ;
        
        inUsePrefixURIs.forEach((u)->System.out.printf("    %s: -> <%s>\n", prefixMapping.getNsURIPrefix(u), u)) ;
        
        // Calc not needed to be efficient.
        Map<String, String> pmap = prefixMapping.getNsPrefixMap() ;    
        Set<String> prefixURIs = new HashSet<>(pmap.values()) ;  
        Set<String> notInUseURIs = SetUtils.difference(prefixURIs, inUsePrefixURIs) ;
        Set<String> notInUsePrefixes = SetUtils.difference(pmap.keySet(), inUsePrefixes) ;
        System.out.println("Not in use: "+notInUseURIs) ;
        System.out.println("Not in use: "+notInUsePrefixes) ;
        notInUseURIs.forEach((u)->System.out.printf("    %s: -> <%s>\n", prefixMapping.getNsURIPrefix(u), u)) ;
        
    }

    /** Find the prefixes from a set of prefix uri */ 
    private static Set<String> urisToPrefixes(PrefixMapping prefixMapping, Set<String> inUsePrefixURIs) {
        return inUsePrefixURIs.stream()
            .map(prefixMapping::getNsURIPrefix)
            .collect(Collectors.toSet()) ;
    }
    
    
}
