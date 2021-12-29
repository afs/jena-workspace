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

package jsonld11;

public class DevJSONLD11 {}

//import java.io.*;
//import java.util.Map;
//
//import com.apicatalog.jsonld.JsonLd;
//import com.apicatalog.jsonld.document.Document;
//import com.apicatalog.jsonld.document.JsonDocument;
//import com.apicatalog.jsonld.document.RdfDocument;
//import com.apicatalog.jsonld.lang.Keywords;
//import com.apicatalog.rdf.RdfDataset;
//
//import jakarta.json.*;
//import org.apache.jena.atlas.lib.StrUtils;
//import org.apache.jena.riot.*;
//import org.apache.jena.riot.system.JenaTitanium;
//import org.apache.jena.riot.system.SysJSONLD11;
//import org.apache.jena.sparql.core.DatasetGraph;
//import org.apache.jena.sparql.core.DatasetGraphFactory;
//import org.apache.jena.sparql.sse.SSE;
//import org.apache.jena.sparql.util.IsoMatcher;
//import org.apache.jena.sys.JenaSystem;
//
//public class DevJSONLD11 {
//
//    // Issues
//    // 2: No preloading contexts.
//    // Not ready yet.
//    //   * TestJsonLDReader.overrideJsonLdOptions is jsonld-java specific
//    //   * no namespaces
//    //   * treatment of TestRiotWriterGraph.writer17 is different.
//    // jena-arq/testing/RIOT/Writer/writer-rt-17.ttl
//    //   * Context caching.
//
//    // Naming:
//
//    // [x] Constants: reader, Writer.
//    // [x] Prefix handling code.
//    // [-] Reader and writer naming?
//    // [-] Lang.JSONLD10 ??
//
//    // Writer
//    // [ ] Calculate a context based on prefixes.
//    // [x] Move constant from JsonLd11Writer to RDFFormat.
//    // [ ] Writing graph != writing dataset.
//    // [x] @version 1.1
//    // [x] Alt writer to signal no titanium
//    // [x] Meaning of each RDFFormat for JSONLD 1.0
//
//    // [ ]  graph writer, and flag for "one graph"
//
//    // [ ] Reset PR.
//    // [ ] Remve <scope>provided</scope> then move registration out of SysJSONLD11.init.
//
//    // Doc
//    // riot --syntax jsonld11
//    // model.read(, "JSONLD11")
//
//    static String DATA = "/home/afs/tmp/JSONLD11";
//
//    static {
//        JenaSystem.init();
//        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
//    }
//
//    public static void main(String[] args) {
//        if ( true ) {
//            mainRead(DATA+"/D.jsonld");
//            System.out.println("DONE");
//            System.exit(0);
//        }
//        // Include where ? static { JenaSystem.init(); }
//        mainSerialize();
//    }
//
//    // Takeover
//    private static void takeover() {
//        SysJSONLD11.becomeDefaultJsonLd();
//    }
//    // ----
//
//    private static void mainRead(String filename) {
//        // Keeps prefixes!
//        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
//        RDFParser.source(filename).forceLang(Lang.JSONLD11).parse(dsg);
//        RDFDataMgr.write(System.out, dsg, Lang.TRIG);
//    }
//
//    private static void mainSerialize() {
//        RDFFormat jsonld11Fmt = RDFFormat.JSONLD11;
//
//        String dsStr = StrUtils.strjoinNL
//                ("(dataset"
//                , "(_ :s :p1 :o1)"
//                , "(_ :s :p2 57)"
////                , "(:g :s :p0 :o0)"
////                , "(:g :s :p1 :o1)"
////                , "(:g _:x :p :o)"
////                , "(:g _:x :p 123)"
////                , "(:g _:x :p 'abc'@en)"
////                , "(_:x _:x :p _:x)"
//                ,")"
//                );
//
//
//        // Write out - read in again.
//        DatasetGraph dsg1 = SSE.parseDatasetGraph(dsStr);
//        DatasetGraph dsg2 = DatasetGraphFactory.createTxnMem();
//
//        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//        RDFDataMgr.write(bout, dsg1, jsonld11Fmt);
////        System.out.writeBytes(bout.toByteArray());
////        System.out.println();
//
//        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
//        RDFDataMgr.read(dsg2, bin, Lang.JSONLD11);
//        if ( IsoMatcher.isomorphic(dsg1, dsg2) )
//            System.out.println("** Same **");
//        else
//            System.out.println("** Different **");
//        RDFDataMgr.write(System.out, dsg2, Lang.TRIG);
//    }
//
//    private static Document emptyContext = JsonDocument.of(JsonValue.EMPTY_JSON_OBJECT);
//    private static Document contextVersion11 =
//            JsonDocument.of(Json.createObjectBuilder()
//                            .add("@version", 1.1)
//                            .build());
//
//    private static void writeFmt(OutputStream output, RDFFormat fmt, DatasetGraph dsg) {
//        try {
//            RdfDataset ds = JenaTitanium.convert(dsg);
//            Document doc = RdfDocument.of(ds);
//            JsonArray array = JsonLd.fromRdf(doc).get();
//            JsonStructure writeThis = null;
//
//            // Context prefixes.
//
//            if ( array.size() == 1 ) {
//                // One item (or none).
//                // If one item, and it is not an object with @graph and @id, then short form possible.
//                if ( !dsg.listGraphNodes().hasNext() ) {}
//                // Only default graph. Write without
//                JsonValue jv = array.get(0);
//                if ( jv.getValueType() == JsonValue.ValueType.OBJECT ) {
//                    JsonObject obj = jv.asJsonObject();
//                    if ( ! obj.containsKey("@graph") ) {
//                        writeThis = Json.createObjectBuilder(obj)
//                                .add(Keywords.CONTEXT, Json.createObjectBuilder().add(Keywords.VERSION, 1.1))
//                                .build();
//                    }
//                }
//            }
//            // Nest array as object.
//            if ( writeThis == null ) {
//                // "Fast compact" but @context first.
//                writeThis = Json.createObjectBuilder()
//                        // Order influences output order.
//                        .add(Keywords.CONTEXT, Json.createObjectBuilder().add(Keywords.VERSION, 1.1))
//                        .add(Keywords.GRAPH, array)
//                        .build();
//            }
//
//
//            // Any addition processing.
////            // Compact for array to object.
////            if ( true ) {
////                JsonDocument jdoc = JsonDocument.of(array);
////                // Array to Object
////                JsonObject obj = JsonLd.compact(jdoc, emptyContext).get();
////                writeThis = obj;
//////                JsonStructure jStruct = JsonLd.flatten(jdoc).get();
//////                // Only makes sense if there is a context has something in it.
//////                JsonStructure jStruct = JsonLd.compact(jdoc, jsonContext).get();
////            }
//            if ( writeThis == null ) {
//                // Basic.
//                writeThis = array;
//            }
//
//            JsonWriter jsonWriter = startWrite(output, fmt, null);
//            jsonWriter.write(writeThis);
//            finishWrite(output, fmt, null);
//
//        } catch (Throwable e) {
//            e.printStackTrace();
//            return ;
//        }
//    }
//
//    private static Map<String,?> config() { return null; }
//
//    private static JsonWriter startWrite(OutputStream output, RDFFormat format, Writer writer) {
//        Map<String,?> config = config();
//        JsonWriterFactory factory = Json.createWriterFactory(config);
//        return (output != null ) ? factory.createWriter(output) : factory.createWriter(writer);
//    }
//
//    private static void finishWrite(OutputStream output, RDFFormat format, Writer writer) throws IOException {
//        boolean outputNL = (format!=RDFFormat.JSONLD11_FLAT) ;
//        if ( output != null ) {
//            if (outputNL) output.write('\n');
//            output.flush();
//        } else {
//            if (outputNL) writer.write("\n");
//            writer.flush();
//        }
//    }
//
//}
