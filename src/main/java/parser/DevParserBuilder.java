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

package parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.util.Context;

public class DevParserBuilder {
    // Look for and clean XXX
    
    // XXX To do
    // HttpClient
    // Accept header
    // use StreamManager? - bypass for the control? check no map then bypass?

    
    // XXX Migration
    // ReaderRIOT
    // Remove ParserProfile
    // Remove RiotLib.profile
    // Remove Prologue.
    // FactoryRDF.reset (RDFParser.parse)

    
    // RDFDataMgr : deprecate Context variants. 
    // RDFDataMgr.process ; Use builder.
    /*
    public static void parse(StreamRDF sink, String uri, String base, Lang hintLang, Context context)
    public static void parse(StreamRDF sink, InputStream in, String base, Lang hintLang, Context context)
    public static void parse(StreamRDF sink, TypedInputStream in, String base, Lang hintLang, Context context)
    public static void parse(StreamRDF sink, InputStream in, String base, Lang hintLang, Context context)
    
    public static void parse(StreamRDF sink, Reader in, String base, Lang hintLang, Context context)
    public static void parse(StreamRDF sink, StringReader in, String base, Lang hintLang, Context context)
    
    ->
    private static void process(StreamRDF destination, TypedInputStream in, String baseUri, Lang lang, Context context)
    private static void process(StreamRDF destination, Reader in, String baseUri, Lang lang, Context context )
    */

    public static void main(String[] args) {
        // sparql.org.D.nt  D.rdf  D.ttl

        List<Header> hdrs = new ArrayList<>();
        Header header = new BasicHeader("Accept", "application/ld+json");
        hdrs.add(header);
        HttpClient hc = CachingHttpClientBuilder.create().setDefaultHeaders(hdrs).build();
        
        StreamRDF stream = StreamRDFLib.writer(System.out);
        RDFParserBuilder.create()
            .source("http://localhost:3030/ds")
            //.setHttpClient(hc)
            .httpAccept("xyz/def")
            
            //.errorHandler(null)
            //.factory(null)
            //.labelToNode(null)
            //.setHttp*
            
            //.factory(new FactoryRDFStd(LabelToNode.createUseLabelAsGiven()))
            //.labelToNode(LabelToNode.createUseLabelAsGiven())
            .parse(stream);
    }
    
    public static void parse(StreamRDF sink, String uri, String base, Lang hintLang, Context context) {
        builder(base, hintLang, context)
            .source(uri)
            .parse(sink);
    }
    
    public static RDFParserBuilder builder(String base, Lang hintLang, Context context) {
        return RDFParserBuilder.create().base(base).lang(hintLang).context(context) ;
        
    }
    //private static void process(StreamRDF destination, TypedInputStream in, String baseUri, Lang lang, Context context) {

}
