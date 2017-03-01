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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.http.client.HttpClient;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.FactoryRDF;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.util.Context;

/**
 * An {@link RDFParser} is a process that will generate triples; 
 * {@link RDFParserBuilder} provides the means to setup the parser.
 * <p>
 * An {@link RDFParser} has a predefined source; the target for output is given when the "parse" method is called. 
 * It can be used multiple times in which case the same source is reread. The destination can vary.
 * The application is responsible for concurrency of the destination of the parse operation.
 * 
 * The process is
 * <pre>
 *     RDFParser parser = RDFParser.create()
 *          .source("filename.ttl")
 *          .build();
 *     parser.parse(destination); 
 * </pre> 
 */

public class RDFParser {
    private final String uri;
    private final Path path;
    private final InputStream inputStream;
    private final Reader javaReader;
    private final HttpClient httpClient;
    private final Lang hintLang;
    private final Lang forceLang;
    private final String baseUri;
    private final boolean strict;
    private final boolean resolveURIs;
    private final FactoryRDF factory;
    private final ErrorHandler errorHandler;
    private final Context context;
    
    private boolean canUse = true;

    public static RDFParserBuilder create() { return  RDFParserBuilder.create(); }
    
    /*package*/ RDFParser(String uri, Path path, InputStream inputStream, Reader javaReader, 
                          HttpClient httpClient, Lang hintLang, Lang forceLang, 
                          String baseUri, boolean strict, boolean resolveURIs, 
                          FactoryRDF factory, ErrorHandler errorHandler, Context context) {
        int x = countNonNull(uri, path, inputStream, javaReader);
        if ( x >= 2 )
            throw new IllegalArgumentException("One one source allowed: At most one of uri, path, inputStream and javaReader can be set");
        
        this.uri = uri;
        this.path = path;
        this.inputStream = inputStream;
        this.javaReader = javaReader;
        this.httpClient = httpClient ;
        this.hintLang = hintLang;
        this.forceLang = forceLang;
        this.baseUri = baseUri;
        this.strict = strict;
        this.resolveURIs = resolveURIs;
        this.factory = factory;
        this.errorHandler = errorHandler;
        this.context = context;
    }

    private int countNonNull(Object...objs) {
        int x = 0;
        for ( Object obj : objs )
            if ( obj != null ) x++;
        return x;
    }

    public void parse(StreamRDF destination) {
        if ( ! canUse )
            throw new RiotException("Parser has been used once and can not be used again");
        // Consuming mode.
        canUse = (inputStream == null && javaReader == null);
        
        if ( path != null || inputStream != null || javaReader != null ) {
            // Path : baseUri is set.
            parseNotUri(destination);
            return ;
        }
        
        // FactoryRDF
        
        // Source by uri
        //RDFDataMgr.process
        // Rename URI
        String urlStr = uri ;
        try (TypedInputStream input = openTypedInputStream(urlStr)) {
            ReaderRIOT reader;
            ContentType ct;
            
            if ( forceLang != null ) {
                ReaderRIOTFactory r = RDFParserRegistry.getFactory(forceLang) ;
                if ( r == null )
                    throw new RiotException("No parser registered for language: "+forceLang);
                ct = forceLang.getContentType();
                reader = r.create(forceLang);
            } else {
                // Conneg and hint
                ct = WebContent.determineCT(input.getContentType(), hintLang, baseUri) ;
                if ( ct == null ) {
                    throw new RiotException("Failed to determine the content type: (URI="+baseUri+" : stream="+input.getContentType()+")") ;
                }
                reader = getReader(ct) ;
                if ( reader == null )
                    throw new RiotException("No parser registered for content type: "+ct.getContentType()) ;
            }
            // Parse step.
            reader.read(input, baseUri, ct, destination, context);
        }
    }

    private TypedInputStream openTypedInputStream(String urlStr) {
        // *************** HttpClient
        // StreamManager - bypass? check no map then bypass?
        //StreamManager.get().getLocationMapper().altMapping(urlStr);
        
        if ( urlStr.startsWith("http://") || urlStr.startsWith("https://") ) {
            // HttpClient cases, after mapping.
            HttpClient useThisOne = httpClient;
            // Context?
            // httpClient == null means use HttpOp default.
            if ( useThisOne == null )
                useThisOne = HttpOp.getDefaultHttpClient();
            return HttpOp.execHttpGet(urlStr, null, useThisOne, null);
        } else {
            return RDFDataMgr.open(urlStr, context);
        }

    }
    
    /** Parse when there is no URI to guide the choice of syntax */ 
    private void parseNotUri(StreamRDF destination) {
        // parse from bytes or chars, no indication of the syntax from the source.
        Lang lang = hintLang;
        if ( forceLang != null )
            lang = forceLang;
        ContentType ct = WebContent.determineCT(null, lang, baseUri) ;
        if ( ct == null )
            throw new RiotException("Failed to determine the RDF syntax") ;

        ReaderRIOT readerRiot = getReader(ct) ;
        if ( readerRiot == null )
            throw new RiotException("No parser registered for content type: "+ct.getContentType()) ;
        
        if ( javaReader != null ) {
            // try(javaRead;) in Java9
            try ( Reader r = javaReader ) {
                readerRiot.read(r, baseUri, ct, destination, context) ;
            } catch (IOException ex) { IO.exception(ex); }
            return ;
        }
        
        // InputStream
        try ( InputStream input = getInputStream(inputStream, path)){
            readerRiot.read(input, baseUri, ct, destination, context) ;
        } catch (IOException ex) { IO.exception(ex); }
        return;
    }

    private InputStream getInputStream(InputStream inputStream, Path path) throws IOException {
        // only one of thse can be set.
        return ( path != null ) ? Files.newInputStream(path) :inputStream;
    }
        
    //RDFDataMgr
    private static ReaderRIOT getReader(ContentType ct) {
        Lang lang = RDFLanguages.contentTypeToLang(ct) ;
        if ( lang == null )
            return null ;
        ReaderRIOTFactory r = RDFParserRegistry.getFactory(lang) ;
        if ( r == null )
            return null ;
        return r.create(lang) ;
    }
    

    
    //public void parseFrom(InputStream input);
    //public void parseTo(StreamRDF stream);
    //public void parse(InputStream input, StreamRDF stream); 

}