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

import java.io.InputStream;
import java.nio.file.Path;

import org.apache.http.client.HttpClient;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ReaderRIOT;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.FactoryRDF;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

/**
 * 
 * A "parser" is a process that will generate triples.
 * Th eprocss is
 * <pre>
 *     RDFParser parser = RDFParserBuilder.create()
 *          .source("filename.ttl")
 *          .build();
 *     parser.parse(destination); 
 * </pre> 
 * 
 */
public class RDFParserBuilder {
    // RDF
    
    // -- Terminals
    // Complexity is in the souse so much of this is the control of creating the input
    // stream for the parser itself.    
    
    // Sources: inputstream, (string, path)
    // Destination: StreamRDF, (graph, dataset).

    // Short cuts.
    //public void parse(src, dest);
    //public void parse(dest);
    //public void parse(src);
    //public RDFParser build();
    
    // Overall, source is complicated so make part of RDFParserBuilder.
    // (except maybe InputStream)
    // 
    
    //?? .contentType
    
    // Source : process to do the opening stuff when parse() is called.
    
    private InputStream _source() { return null; }  
    
    
    public RDFParserBuilder source(InputStream input) { return this; }
    public RDFParserBuilder source(Path path) { return this; }

    /**
     * 
     * @param url This can be a filename
     * @return this
     */
    public RDFParserBuilder setSource(String url) { return this; }
    

    /**
     * Set the hint {@link Lang}. This is the RDF syntax used when there is no way to
     * deduce the syntax (e.g. read from a InputStream, no recognized file extension, no
     * recognized HTTP Content-Type provided).
     * 
     * @param lang
     * @return this
     */
    public RDFParserBuilder lang(Lang lang) { return this; }

    /**
     * Force the choice RDF syntax to be {@code lang}, and ignore any indications such as file extension
     * or HTTP Content-Type.
     * @see Lang
     * @param lang
     * @return this
     */
    public RDFParserBuilder forceLang(Lang lang) { return this; }
    
    /**
     * Set the HTTP "Accept" header.
     * The default is {@link WebContent#defaultRDFAcceptHeader}.
     * @param acceptHeader
     * @return this
     */
    public RDFParserBuilder httpAccept(String acceptHeader) { return this; }

    /**
     * Set an HTTP header (
     */

    public RDFParserBuilder setHttpHeader(String header, String value) { return this; }
    
    public RDFParserBuilder setHttpClient(HttpClient httpClient) { return this; }

    public RDFParserBuilder base(String base) { return this; }
    
    // ---- source-destination
    
//    // XXX ??? Make part of the "parse" step? 
//    public RDFParserBuilder output(StreamRDF stream) { return this; }
    
    public void parse(StreamRDF stream) {}

    public void parse(Graph graph) {
        parse(StreamRDFLib.graph(graph));
    }

    public void parse(DatasetGraph dataset) {
        parse(StreamRDFLib.dataset(dataset));
    }

    // ---- 
    
    public RDFParserBuilder errorHandler(ErrorHandler handler) { return this; }
    
    public RDFParserBuilder factory(FactoryRDF factory) { return this; }
    
    // Deprecate ParseProfile as a visible class.
    //public RDFParserBuilder setParserProfile(ParserProfile profile) { return this; }
    
    // Divide out the line/column makers in Parser profile.
        
    public RDFParserBuilder strict(boolean strictMode) { return this; }
    
    public RDFParserBuilder context(Context context) { return this; }
    
    // ---- Output.
    // XXX ??? parseToGraph(), parseToDataset()
    
    // XXX Wrong.  A thing you can call "parse()" on and no more.
    public ReaderRIOT buildReaderRIOT() { return null; }

    // XXX Wrong.  A thing you can call "parse()" on and no more.
    public RDFParser build() { return null; }

//    // XXX Wrong.  A thing you can call "parse()" on and no more.
//    public Future<Object> buildAsync() { return null; }

    @Override
    public RDFParserBuilder clone() { return null; }
    
//    // Async parsing.
//    public Future<Object> parse() { return null; } 
    
    interface RDFParser {
        public void parseFrom(InputStream input);
        public void parseTo(StreamRDF stream);
        public void parse(InputStream input, StreamRDF stream); 

    }
}
