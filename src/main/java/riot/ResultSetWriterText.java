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

package riot;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Objects;

import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetWriter;
import org.apache.jena.riot.resultset.ResultSetWriterFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.resultset.TextOutput;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class ResultSetWriterText implements ResultSetWriter {

    public static ResultSetWriterFactory xfactory = lang->{
        if (!Objects.equals(lang, ResultSetLang.SPARQLResultSetJSON ) )
            throw new ResultSetException("ResultSetWriter for JSON asked for a "+lang); 
        return new ResultSetWriterText(); 
    };
    
    private ResultSetWriterText() {}

    private static Symbol symPrologue = SystemARQ.allocSymbol("prologue"); 
    
    @Override
    public void write(OutputStream out, ResultSet resultSet, Context context) {
        TextOutput tFmt;
        if ( resultSet.getResourceModel() != null ) {
            PrefixMapping pmap = resultSet.getResourceModel();
            tFmt = new TextOutput(pmap) ;
        } else if ( context.isDefined(symPrologue) ) {
            Prologue prologue = context.get(symPrologue);
            tFmt = new TextOutput(prologue) ;
        } else {
            PrefixMapping pmap = new PrefixMappingImpl();
            //pmap = ARQConstants.getGlobalPrefixMap();
            tFmt = new TextOutput(pmap);
        }
        
        tFmt.format(out, resultSet) ;
    }

    @Override
    public void write(Writer out, ResultSet resultSet, Context context) {
        throw new NotImplemented();
    }

    @Override
    public void write(OutputStream out, boolean result, Context context) {
        throw new NotImplemented();
    }
    
}
