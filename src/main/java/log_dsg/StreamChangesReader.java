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

package log_dsg;

import java.io.InputStream ;
import java.util.List ;

import log_dsg.tio.CommsException ;
import log_dsg.tio.TokenInputStream ;
import log_dsg.tio.TokenInputStreamBase ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;

// Needs reworking: for efficiency, for less features
public class StreamChangesReader {
    private final TokenInputStream input ;
    
    public StreamChangesReader(InputStream in) {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(in) ; 
        input = new TokenInputStreamBase(null, tokenizer) ;
    }
    
    public void apply(StreamChanges sink) {
        int lineNumber = 0 ;
        while(input.hasNext()) {
            List<Token> line = input.next() ;
            
            if ( line.isEmpty() ) {}
            Token token1 = line.get(0) ;
            if ( ! token1.isWord() )
                throw new CommsException("["+token1.getLine()+"] Token1 is not a word "+token1) ;
            String code = token1.getImage() ;
            if ( code.length() != 2 )
                throw new CommsException("["+token1.getLine()+"] Code is not 2 characters "+code) ;
            
            switch (code) {
                case "QA": {
                    if ( line.size() != 4 && line.size() != 5 )
                        throw new CommsException("["+token1.getLine()+"] Quad add tuple error: length = "+line.size()) ;
                    Node s = line.get(1).asNode() ;
                    Node p = line.get(2).asNode() ;
                    Node o = line.get(3).asNode() ;
                    Node g = line.size()==4 ? null : line.get(4).asNode() ;  
                    sink.add(g, s, p, o);
                    break ;
                }
                case "QD": {
                    if ( line.size() != 4 && line.size() != 5 )
                        throw new CommsException("["+token1.getLine()+"] Quad delete tuple error: length = "+line.size()) ;
                    Node s = line.get(1).asNode() ;
                    Node p = line.get(2).asNode() ;
                    Node o = line.get(3).asNode() ;
                    Node g = line.size()==4 ? null : line.get(4).asNode() ;  
                    sink.delete(g, s, p, o);
                    break ;
                }
                case "PA": {
                    if ( line.size() != 3 && line.size() != 4 )
                        throw new CommsException("["+token1.getLine()+"] Prefix add tuple error: length = "+line.size()) ;
                    String prefix = line.get(1).asString() ;
                    String uriStr = line.get(2).asString() ;
                    Node gn = line.size()==3 ? null : line.get(3).asNode() ;  
                    sink.addPrefix(gn, prefix, uriStr);
                    break ;
                }
                case "PD": {
                    if ( line.size() != 2 && line.size() != 3 )
                        throw new CommsException("["+token1.getLine()+"] Prefix delete tuple error: length = "+line.size()) ;
                    String prefix = line.get(1).asString() ;
                    Node gn = line.size()==2 ? null : line.get(3).asNode() ;  
                    sink.deletePrefix(gn, prefix);
                    break ;
                }
                case "TB": {
                    sink.txnBegin(ReadWrite.WRITE);
                    break ;
                }
                case "TC": {
                    // Possible return
                    sink.txnCommit();
                    break ;
                }
                case "TA": {
                    // Possible return
                    sink.txnAbort();
                    break ;
                }
                default:  {
                    throw new CommsException("["+token1.getLine()+"] Code '"+code+"' not recognized") ;
                }
            }
        }
    }
}
