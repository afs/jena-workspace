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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.apicatalog.jsonld.JsonLdError;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFWriter;

public class DevJSONLD {

    public static void main(String[] args) throws JsonLdError {
        run(Lang.JSONLD10);
        System.out.println();

//        String c = """
//          {
//            "x" : {
//              "@id" : "http://example/ns#x"
//            },
//            "p" : {
//              "@id" : "http://example/p"
//            },
//            "ns" : "http://example/ns#"
//        }
//        """;
//        JsonDocument jd = JsonDocument.of(new ByteArrayInputStream(Bytes.asUTF8bytes(c)));


        // How to form the context for JSON-LD 1.1
        run(Lang.JSONLD11);
        // COMPACT
    }

    static void run(Lang lang) {
        String source = "/home/afs/tmp/JSONLD11/D.ttl";
        //String source = "/home/afs/tmp/JSONLD11/D10.jsonld10";
        Graph graph = RDFParser.source(source).lang(Lang.JSONLD10).toGraph();
        // COMPACT
        //System.out.println(PrefixMapFactory.createForOutput(graph.getPrefixMapping()).toString());
        RDFWriter.source(graph).lang(lang).output(System.out);
    }

    public static void http_Java11(String...args) {
        // https://stackoverflow.com/questions/71413050/jena-javascript-custom-functions-scriptengine-null-error

        //Graph graph = RDFParser.source("D.jsonld").lang(Lang.JSONLD11).toGraph();

        // Problems in Java11 (as used by Titanium)
        // Maybe because of HTTPS (schema.org redirects http to https, redirection seems to happen)
        try {
            HttpClient hc = HttpClient.newHttpClient();
            // Bad cert https://http2cdn.cdnsun.com
            //URI uri = new URI("https://www.bbc.co.uk/news");
            URI uri = new URI("https://schema.org/");
            HttpRequest req = HttpRequest.newBuilder(uri)
                    //.version(Version.HTTP_1_1)
                    .build();
            HttpResponse<String> res = hc.send(req, BodyHandlers.ofString());
            if ( res.statusCode() != 200 ) {
                System.out.println(res.statusCode());
            }
            else {
                String x = res.body();
                System.out.println("OK");
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        System.exit(0);
    }

    public static void main_JSONLD(String...args) {
//      SysRIOT.setDefaultJSONLD("1.1");
//      Graph graph = RDFParser.source("/home/afs/tmp/JSONLD11/D11.jsonld").toGraph();
//      String x = RDFWriter.source(graph).lang(Lang.TTL).asString();
//      System.out.print(x);
//      SysRIOT.setDefaultJSONLD("");
//      Graph graph2 = RDFParser.source("/home/afs/tmp/JSONLD11/D11.jsonld").toGraph();
    }
}
