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
import java.nio.file.Path;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.FactoryRDF;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.util.Context;

// Replaces ParserProfile by
//   RDFParser + FactoryRDF 

class RDFParser {
    private final String uri;
    private final Path path;
    private final InputStream inputStream;
    private final Reader javaReader;
    private final Lang hintLang;
    private final Lang forceLang;
    private final String baseUri;
    private final boolean strict;
    private final boolean resolveURIs;
    private final FactoryRDF factory;
    private final ErrorHandler errorHandler;
    private final Context context;

    /*package*/ RDFParser(String uri, Path path, InputStream inputStream, Reader javaReader, 
                          Lang hintLang, Lang forceLang, 
                          String baseUri, boolean strict, boolean resolveURIs, 
                          FactoryRDF factory, ErrorHandler errorHandler, Context context) {
        this.uri = uri;
        this.path = path;
        this.inputStream = inputStream;
        this.javaReader = javaReader;
        this.hintLang = hintLang;
        this.forceLang = forceLang;
        this.baseUri = baseUri;
        this.strict = strict;
        this.resolveURIs = resolveURIs;
        this.factory = factory;
        this.errorHandler = errorHandler;
        this.context = context;
    }

    public void parse(StreamRDF destination) {
        if ( inputStream != null || javaReader != null ) {
            // Input of bytes or chars set directly.
            // We need to know the syntax.
            Lang lang = hintLang;
            if ( forceLang != null )
                lang = forceLang;
            ContentType ct = WebContent.determineCT(null, lang, baseUri) ;
            if ( ct == null )
                throw new RiotException("Failed to determine the content type: (URI="+baseUri+" : hint="+lang+")") ;
            ReaderRIOT reader = getReader(ct) ;
            if ( reader == null )
                throw new RiotException("No parser registered for content type: "+ct.getContentType()) ;
            if ( javaReader != null )
                reader.read(javaReader, baseUri, ct, destination, context) ;
            else
                reader.read(inputStream, baseUri, ct, destination, context) ;
            return;
        }
        
        //RDFDataMgr.process
        String urlStr = ( path != null ) ? path.toString() : uri ;
        TypedInputStream input = RDFDataMgr.open(urlStr, context);

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
            if ( ct == null )
                throw new RiotException("Failed to determine the content type: (URI="+baseUri+" : stream="+input.getContentType()+")") ;
            reader = getReader(ct) ;
            if ( reader == null )
                throw new RiotException("No parser registered for content type: "+ct.getContentType()) ;
        }
        // Parse step.
        reader.read(input, baseUri, ct, destination, context);
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