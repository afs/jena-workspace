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

package archive;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.UUID;
import java.util.function.Function;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
//import org.junit.jupiter.api.Test;
import org.junit.Test;

//    public class UuidParsingintoModelOrQuery {
public class Jena1841_typemapper_uuid {

    public static void main(String ... a) {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000007");

        if ( false ) {

//            RDFDatatype dtx = new BaseDatatype("java:java.util.UUID") {
//                @Override
//                public Class<?> getJavaClass() {
//                    return UUID.class;
//                }
//                @Override
//                public String unparse(Object value) {
//                    UUID x = (UUID)value;
//                    return x.toString();
//                }
//                @Override
//                public Object parse(String lexicalForm) throws DatatypeFormatException {
//                    try {
//                        UUID.fromString(lexicalForm);
//                        return lexicalForm;
//                    } catch (Throwable th) {
//                        throw new DatatypeFormatException();
//                    }
//                }
//            };

            RDFDatatype dtx = customDatatype(UUID.class, "java:java.util.UUID", UUID::fromString, UUID::toString);
            TypeMapper.getInstance().registerDatatype(dtx);
        }

        if ( false ) {
            TypeMapper.getInstance().getSafeTypeByName("java:java.util.UUID");
            LiteralLabel ll = LiteralLabelFactory.createTypedLiteral(uuid);
        }

        if ( true ) {
            SelectBuilder select = new SelectBuilder();
            select.addWhere("?a", "<http://example.org/p>", "?uuid");
            select.addFilter(select.getExprFactory().eq("?uuid", uuid));
            System.out.println(select.build());
        }

        String modelStr = "_:a <http://example.org/p1> '00000000'^^<java:java.util.UUID> .";
        Model model = ModelFactory.createDefaultModel().read(new StringReader(modelStr), "", "N-TRIPLE");
        RDFDataMgr.write(System.out, model, Lang.NT);
        System.out.println("DONE");
    }

    /** Custom datatype for {@code X} */
    static <X> RDFDatatype customDatatype(Class<X> clazz, String uri,
                                          Function<String, X> parser,
                                          Function<X, String> toLexicalForm) {
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

//    //@Test
//    public void uuidParsingIntoSingleModel() throws Exception {
//        UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000000");
//        UUID uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000001");
//
//        String modelStr = "_:a <http://example.org/p1> \"" + uuid1 + "\"^^<java:java.util.UUID> .\n"
//            + "_:a <http://example.org/p2> \"" + uuid1 + "\"^^<java:java.util.UUID> .";
//        ModelFactory.createDefaultModel().read(new ByteArrayInputStream(modelStr.getBytes()), "", "N-TRIPLE");
//        // repeat
//        modelStr = "_:a <http://example.org/p1> \"" + uuid2 + "\"^^<java:java.util.UUID> .\n"
//            + "_:a <http://example.org/p2> \"" + uuid2 + "\"^^<java:java.util.UUID> .";
//        ModelFactory.createDefaultModel().read(new ByteArrayInputStream(modelStr.getBytes()), "", "N-TRIPLE");
//    }
//
//    //@Test
//    public void sameUuidParsingIntoSingleModel() throws Exception {
//        UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000002");
//        UUID uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000003");
//        String modelStr = "_:a <http://example.org/p1> \"" + uuid1 + "\"^^<java:java.util.UUID> .\n"
//            + "_:a <http://example.org/p2> \"" + uuid2 + "\"^^<java:java.util.UUID> .";
//        ModelFactory.createDefaultModel().read(new ByteArrayInputStream(modelStr.getBytes()), "", "N-TRIPLE");
//        // repeat
//        ModelFactory.createDefaultModel().read(new ByteArrayInputStream(modelStr.getBytes()), "", "N-TRIPLE");
//    }
//
//    //@Test
//    public void sameUuidParsingIntoMultipleModel() throws Exception {
//        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000004");
//        String modelStr = "_:a <http://example.org/p1> \"" + uuid + "\"^^<java:java.util.UUID> .";
//        ModelFactory.createDefaultModel().read(new ByteArrayInputStream(modelStr.getBytes()), "", "N-TRIPLE");
//        ModelFactory.createDefaultModel().read(new ByteArrayInputStream(modelStr.getBytes()), "", "N-TRIPLE");
//    }
//
//    //@Test
//    public void uuidUseInQuery() throws Exception {
//        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000005");
//        SelectBuilder select = new SelectBuilder();
//        select.addWhere("?a", "<http://example.org/p>", "?uuid");
//        select.addFilter(select.getExprFactory().eq("?uuid", uuid));
//        QueryExecution q = QueryExecutionFactory.create(select.build());
//    }
//
//    //@Test
//    public void uuidParsingIntoQuery() throws Exception {
//        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000006");
//        SelectBuilder select = new SelectBuilder();
//        select.addWhere("?a", "<http://example.org/p>", "?uuid");
//        select.addFilter(select.getExprFactory().eq("?uuid", uuid));
//        QueryExecution q = QueryExecutionFactory.create(select.toString());
//    }

    @Test
    public void uuidUseInQueryAndParsingIntoModel() throws Exception {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000007");
        SelectBuilder select = new SelectBuilder();
        select.addWhere("?a", "<http://example.org/p>", "?uuid");
        select.addFilter(select.getExprFactory().eq("?uuid", uuid));
        QueryExecution q = QueryExecutionFactory.create(select.build());
        String modelStr = "_:a <http://example.org/p1> \"" + uuid + "\"^^<java:java.util.UUID> .\n"
            + "_:a <http://example.org/p2> \"" + uuid + "\"^^<java:java.util.UUID> .";
        Model model = ModelFactory.createDefaultModel().read(new ByteArrayInputStream(modelStr.getBytes()), "",
            "N-TRIPLE");
    }


    @Test
    public void uuidUseInQueryAndParsingIntoModel2() throws Exception {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000007");

        if ( true ) {
            SelectBuilder select = new SelectBuilder();
            select.addWhere("?a", "<http://example.org/p>", "?uuid");
            select.addFilter(select.getExprFactory().eq("?uuid", uuid));
            //QueryExecution q = QueryExecutionFactory.create(select.build());
        } else {
            String qs = "SELECT * { ?a  <http://example.org/p>  ?uuid FILTER ( ?uuid = '00000000-0000-0000-0000-000000000007'^^<java:java.util.UUID> ) }";
            Query query = QueryFactory.create(qs);
            QueryExecution q = QueryExecutionFactory.create(query);
        }
        String modelStr = "_:a <http://example.org/p1> \"" + uuid + "\"^^<java:java.util.UUID> .\n"
            + "_:a <http://example.org/p2> \"" + uuid + "\"^^<java:java.util.UUID> .";
        Model model = ModelFactory.createDefaultModel().read(new ByteArrayInputStream(modelStr.getBytes()), "",
            "N-TRIPLE");
    }

}
