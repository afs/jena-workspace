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
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

import org.apache.http.client.HttpClient;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ReaderRIOT;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.*;
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
    // Source
    private String uri = null;
    private Path path = null;
    private InputStream inputStream;
    // StringReader - charset problems with any other kind.
    private Reader javaReader = null;
    
    // Syntax
    private Lang hintLang = null;
    private Lang forceLang = null;
    
    private String baseUri = null;
    private boolean strict = false;
    private boolean resolveURIs = true;
    
    // Construction for the StreamRDF 
    private FactoryRDF factory = null;
    private LabelToNode labelToNode = null;
    
    // Bad news.
    private ErrorHandler errorHandler = null;
    
    // Parsing process
    private Context context = null;

    // ----Source : process to do the opening stuff when parse() is called.
    private InputStream _source() { return null; }  
    
    public static RDFParserBuilder create() { return new RDFParserBuilder() ; }
    private RDFParserBuilder() {}
    
    /** 
     *  Set the source to {@link Path}. 
     *  This clears any other source setting.
     *  @param path
     *  @return this
     */
    public RDFParserBuilder source(Path path) {
        clearSource();
        this.path = path;
        return this;
    }

    /** 
     *  Set the source to a URI; this includes OS file names.
     *  File URL shoudl be of the form {@code file:///...}. 
     *  This clears any other source setting.
     *  @param uri
     *  @return this
     */
    public RDFParserBuilder source(String uri) {
        clearSource();
        this.uri = uri;
        return this;
    }

    /** 
     *  Set the source to {@link InputStream}. 
     *  This clears any other source setting.
     *  @param input
     *  @return this
     */
    public RDFParserBuilder source(InputStream input) {
        clearSource();
        this.inputStream = input;
        return this;
    }

    /** 
     *  Set the source to {@link StringReader}. 
     *  This clears any other source setting.
     *  @param reader
     *  @return this
     */
    public RDFParserBuilder source(StringReader reader) {
        clearSource();
        this.javaReader = reader;
        return this;
    }

    private void clearSource() {
        this.uri = null;
        this.inputStream = null;
        this.path = null;
        this.javaReader = null;
    }

    /**
     * Set the hint {@link Lang}. This is the RDF syntax used when there is no way to
     * deduce the syntax (e.g. read from a InputStream, no recognized file extension, no
     * recognized HTTP Content-Type provided).
     * 
     * @param lang
     * @return this
     */
    public RDFParserBuilder lang(Lang lang) { this.hintLang = lang ; return this; }

    /**
     * Force the choice RDF syntax to be {@code lang}, and ignore any indications such as file extension
     * or HTTP Content-Type.
     * @see Lang
     * @param lang
     * @return this
     */
    public RDFParserBuilder forceLang(Lang lang) { this.forceLang = lang ; return this; }
    
    /**
     * Set the HTTP "Accept" header.
     * The default is {@link WebContent#defaultRDFAcceptHeader}.
     * @param acceptHeader
     * @return this
     */
    // XXX
    public RDFParserBuilder httpAccept(String acceptHeader) { return this; }

    /**
     * Set an HTTP header (
     */
    //XXX
    public RDFParserBuilder setHttpHeader(String header, String value) { return this; }
    
    //XXX
    public RDFParserBuilder setHttpClient(HttpClient httpClient) { return this; }

    public RDFParserBuilder base(String base) { this.baseUri = base ; return this; }

    /**
     * Set the {@link ErrorHandler} to use.
     * This replaces any previous setting.
     * The default is use slf4j logger "RIOT".   
     * @param handler
     * @return this
     */
    public RDFParserBuilder errorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }
    
    // XXX Consider RiotLib.factoryRDF(LabelToNode) if factory == null and  
    
    /**
     * Set the {@link FactoryRDF} to use. {@link FactoryRDF} control how parser output is
     * turned into {@code Node} and how {@code Triple}s and {@code Quad}s are built. This
     * replaces any previous setting. 
     * <br/>
     * The default is use {@link RiotLib#factoryRDF()} which is provides {@code Node}
     * reuse. 
     * <br/>
     * The {@code FactoryRDF} also determines how blank node labels in RDF syntax are
     * mapped to {@link BlankNodeId}. Use
     * <pre>
     *    new Factory(myLabelToNode) 
     * </pre>
     * to create an {@code FactoryRDF} and set the {@code LabelToNode} step.
     * @see #labelToNode
     * @param factory
     * @return this
     */
    public RDFParserBuilder factory(FactoryRDF factory) {
        this.factory = factory;
        return this;
    }
    
    /**
     * Use the given {@link LabelToNode}, the policy for converting blank node labels in
     * RDF syntax to Jena's {@code Node} objects (usually a blank node).
     * <br/>
     * Only applies when the {@link FactoryRDF} is not set in the
     * {@code RDFParserBuilder}, otherwise the {@link FactoryRDF} controls the
     * label-to-node process.
     * <br/>
     * {@link SyntaxLabels#createLabelToNode} is the default policy.
     * <br>
     * {@link LabelToNode#createUseLabelAsGiven()} uses the label in teh RDF syntax directly. 
     * This does not produce safe RDF and should only be used for development and debugging.   
     * @see #factory
     * @param labelToNode
     * @return this
     */
    public RDFParserBuilder labelToNode(LabelToNode labelToNode) {
        this.labelToNode = labelToNode;
        return this;
    }
    

    // Deprecate ParseProfile as a visible class.
    //public RDFParserBuilder setParserProfile(ParserProfile profile) { return this; }
    
    // Divide out the line/column makers in Parser profile.
        
    //XXX
    public RDFParserBuilder strict(boolean strictMode) { return this; }
    
    public RDFParserBuilder context(Context context) { this.context = context.copy() ; return this; }
    //public Context context() { return this.context; }
    
    // ---- Terminals
    // "parse" are short cuts for {@code build().parse(...)}.
    
    /** 
     * Parse the source, sending the results to a {@link StreamRDF}.
     * Short form for {@code build().parse(stream)}.
     * @param stream
     */
    public void parse(StreamRDF stream) {
        build().parse(stream);
    }

    /**
     * Parse the source, sending the results to a {@link Graph}. The source must be for
     * triples; any quads are discarded. 
     * Short form for {@code build().parse(stream)}
     * where {@code stream} sends tripes and prfixes to the {@code Graph}.
     * 
     * @param graph
     */
    public void parse(Graph graph) {
        parse(StreamRDFLib.graph(graph));
    }

    /*
     * Parse the source, sending the results to a {@link DatasetGraph}.
     * Short form for {@code build().parse(stream)}
     * where {@code stream} sends tripes and prefixes to the {@code DatasetGraph}.
     * 
     * @param graph
     */
    public void parse(DatasetGraph dataset) {
        parse(StreamRDFLib.dataset(dataset));
    }
    // ---- Output.
    // XXX Wrong??  A thing you can call "parse()" on and no more.
    public ReaderRIOT buildReaderRIOT() { return null; }

    // XXX Wrong.  A thing you can call "parse()" on and no more.
    public RDFParser build() { 
        if ( uri == null && path == null && inputStream == null && javaReader == null )
            throw new RiotException("No source specified");
        // The builder ensures only one source is set. 
        if ( strict ) {
            
        }
        
        if ( factory == null && labelToNode != null )
            factory = RiotLib.factoryRDF(labelToNode);
        return new RDFParser(uri, path, inputStream, javaReader, 
                             hintLang, forceLang,
                             baseUri, strict, resolveURIs,
                             factory, errorHandler, context);
    }

    /**
     * Duplicate this buider with current settings.
     * Changes to setting to this builder do not affect the clone. 
     */
    @Override
    public RDFParserBuilder clone() { 
        RDFParserBuilder builder = new RDFParserBuilder();
        builder.uri = this.uri;
        builder.path = this.path;
        builder.inputStream = this.inputStream;
        builder.javaReader = this.javaReader;
        builder.hintLang = this.hintLang;
        builder.forceLang = this.forceLang;
        builder.baseUri = this.baseUri;
        builder.strict = this.strict;
        builder.resolveURIs = this.resolveURIs;
        builder.factory = this.factory;
        builder.labelToNode = this.labelToNode;
        builder.errorHandler = this.errorHandler;
        builder.context = this.context;
        return builder;
    }
}
