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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;

public class TestRDFParser {
    private static String testdata = "@prefix : <http://example/ns#> . :x :x _:b .";  
    
    @Test public void source_not_uri_01() {
        Graph graph = GraphFactory.createGraphMem();
        RDFParserBuilder.create().lang(Lang.TTL).source(new StringReader(testdata)).parse(graph);
        assertEquals(1, graph.size());
    }
    
    @Test public void source_not_uri_02() {
        Graph graph = GraphFactory.createGraphMem();
        InputStream input = new ByteArrayInputStream(testdata.getBytes(StandardCharsets.UTF_8));
        RDFParserBuilder.create().lang(Lang.TTL).source(input).parse(graph);
        assertEquals(1, graph.size());
    }
    
    // Source via URI
    
    // Lang via baseURI
    
    // Lang via path.
    
    // Lang via conneg
    
    // Language
    
    // HTTP options
    // Specific "Accept:"
    
    // Error handler
    
    // FactoryRDF
    
    // LabelToNode
    
    @Test public void labels_01() {
        Graph graph = GraphFactory.createGraphMem();
        InputStream input = new ByteArrayInputStream(testdata.getBytes(StandardCharsets.UTF_8));
        RDFParserBuilder.create().lang(Lang.TTL).source(input)
            .labelToNode(LabelToNode.createUseLabelAsGiven())
            .parse(graph);
        
        assertEquals(1, graph.size());
    }
    
    // Clone
    
    

}
