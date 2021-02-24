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

import java.io.StringReader;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserRegistry;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.system.*;
import org.apache.jena.sparql.util.FmtUtils;

public class EricParserProfile {
    static {
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...a) {
        ParserProfile profile =
            new ParserProfileStd(RiotLib.factoryRDF(),
                                 ErrorHandlerFactory.errorHandlerStd,
                                 IRIxResolver.create("http://baseURI/").build(),
                                 PrefixMapFactory.create(),
                                 RIOT.getContext(), false, false) {
            @Override
            public Triple createTriple(Node subject, Node predicate, Node object, long line, long col) {
                Triple triple = super.createTriple(subject, predicate, object, line, col);
                System.out.printf("[line=%d] %s \n", line, FmtUtils.stringForTriple(triple));
                return triple;
            }
        };

        var parser = RDFParserRegistry.getFactory(Lang.TTL).create(Lang.TTL, profile);
        parser.read(new StringReader("PREFIX : <#> :s <p> <o> .\n<s> <p> <o> .\n"), null, null, StreamRDFLib.sinkNull(), RIOT.getContext());
    }
}
