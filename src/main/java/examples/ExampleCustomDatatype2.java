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
import java.util.UUID;
import java.util.function.Function;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;

/** Register a custom datatype */
public class ExampleCustomDatatype2 {

    public static void main(String ... a) {
        registerCustomDatatype(UUID.class, "java:java.util.UUID", UUID::fromString, UUID::toString);

        // Node from Java object
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-00000000AAAA");
        Node n1 = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(uuid));
        System.out.println(n1);
        System.out.println(n1.getLiteralValue());
        System.out.println(n1.getLiteralValue().getClass());
        System.out.println();

//        // Get datatype.
//        RDFDatatype dt = TypeMapper.getInstance().getTypeByName("java:java.util.UUID");
//        Node n1a = NodeFactory.createLiteral("00000000-0000-0000-0000-00000000AAAA", dt);
//        System.out.println(n1a);
//        System.out.println(n1a.getLiteralValue());
//        System.out.println(n1a.getLiteralValue().getClass());
//        System.out.println();

        // QueryBuilder
        SelectBuilder select = new SelectBuilder();
        select.addWhere("?a", "<http://example.org/p>", "?uuid");
        select.addFilter(select.getExprFactory().eq("?uuid", uuid));
        System.out.println(select.build());

//        // Parse
//        Node n2 = SSE.parseNode("'00000000-0000-0000-0000-00000000AAAA'^^<java:java.util.UUID>");
//        System.out.println(n2);
//        System.out.println(n2.getLiteralValue());
//        System.out.println(n2.getLiteralValue().getClass());
//        System.out.println();

        // Parse
        String rdfStr = "_:a <http://example.org/p1> '00000000-0000-0000-0000-00000000AAAA'^^<java:java.util.UUID> .";
        Graph graph = GraphFactory.createDefaultGraph();
        RDFDataMgr.read(graph, new StringReader(rdfStr), null, Lang.NT);

        // Get the parsed
        Node n3 = graph.find().next().getObject();
        System.out.println(n3);
        System.out.println(n3.getLiteralValue());
        System.out.println(n3.getLiteralValue().getClass());
    }

    static <X> void registerCustomDatatype(Class<X> clazz, String datatypeURI,
                                           Function<String, X> parser,
                                           Function<X, String> toLexicalForm) {
        RDFDatatype customeDatatype = customDatatype(clazz, datatypeURI, parser, toLexicalForm);
        TypeMapper.getInstance().registerDatatype(customeDatatype);
    }

    /** Custom datatype for {@code X} */
    static <X> RDFDatatype customDatatype(Class<X> clazz, String uri, Function<String, X> parser, Function<X, String> toLexicalForm) {
        return new BaseDatatype(uri) {
            @Override
            public Class<X> getJavaClass() {
                return clazz;
            }

            @Override
            public String unparse(Object value) {
                @SuppressWarnings("unchecked")
                X x = (X)value;
                return toLexicalForm.apply(x);
            }

            @Override
            public X parse(String lexicalForm) throws DatatypeFormatException {
                try {
                    return parser.apply(lexicalForm);
                } catch (Throwable th) {
                    throw new DatatypeFormatException();
                }
            }
        };
    }
}
