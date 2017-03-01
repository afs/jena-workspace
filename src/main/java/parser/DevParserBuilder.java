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
import org.apache.jena.riot.system.FactoryRDFStd;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;

public class DevParserBuilder {

    public static void main(String[] args) {
        StreamRDF stream = StreamRDFLib.writer(System.out);
        RDFParserBuilder.create()
            .source("/home/afs/tmp/D.ttl")
            //.source("http://sparql.org/D.ttl")
            //.httpAccept("application/junk")
            //.forceLang(Lang.TRIG)
            
            //.errorHandler(null)
            //.factory(null)
            //.labelToNode(null)
            //.setHttp*
            
            .factory(new FactoryRDFStd(LabelToNode.createUseLabelAsGiven()))
            //.labelToNode(LabelToNode.createUseLabelAsGiven())
            .parse(stream);
    }

}
