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

import java.util.Iterator;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb.TDBFactory;

public class Jena1855_TrigDftGraph {

//    static Node s =  SSE.parseNode("<s>");
//    static Node p =  SSE.parseNode("<p>");
//    static Node o =  SSE.parseNode("<o>");
//    static Node g =  SSE.parseNode("<g>");
//    //static Quad q = Quad.create(g, s, p, o);
//    static Quad q = SSE.parseQuad("(:g :s :p :o)");

    // Better to emit a triple, not a quad+null.

//  Quad.tripleInQuad ::
    // Quad.defaultGraphNodeGenerated
    // Quad.defaultGraphIRI
//  WriterStreamRDFPlain     jena-arq::org/apache/jena/riot/writer              line 113
//  TestParserFactory        jena-arq[test]::org/apache/jena/riot/lang          line 167
//  StreamRDFLib             jena-arq::org/apache/jena/riot/system              line 121
//  SinkQuadOutput           jena-arq::org/apache/jena/riot/out                 line 86
//  Quad                     jena-arq::org/apache/jena/sparql/core              line 151
//  ProcNodeTableBuilder     jena-tdb::org/apache/jena/tdb/store/bulkloader2    line 148
//  LangTriG                 jena-arq::org/apache/jena/riot/lang                line 185
//  LangTriG                 jena-arq::org/apache/jena/riot/lang                line 228
//  LangTriG                 jena-arq::org/apache/jena/riot/lang                line 263
//  DatasetGraphCollection   jena-arq::org/apache/jena/sparql/core              line 106
//  BulkLoader               jena-tdb::org/apache/jena/tdb/store/bulkloader     line 228

    public static void main(String...a) {
        dwim("/home/afs/tmp/D.trig");
        dwim("/home/afs/tmp/D.nq");
    }

    public static void dwim(String dataFilename) {
        System.out.println("== Data: "+dataFilename);
        System.out.println();

        boolean ALL = false;
        // NQ: urn:x-arq:DefaultGraphNode
        // TriG: null
        {
            System.out.println("RDFDataMgr.createIteratorQuads");
            Lang lang = RDFLanguages.filenameToLang(dataFilename);
            Iterator<Quad> it = RDFDataMgr.createIteratorQuads(IO.openFile(dataFilename), lang, null);
            while (it.hasNext()) {
                Quad q = it.next();
                System.out.println(q.getGraph());
            }
            System.out.println();
        }

        // NQ: urn:x-arq:DefaultGraphNode
        // TriG: null
        {
            System.out.println("RDFDataMgr.parse");
            StreamRDF dest = new StreamRDFBase() {
                @Override
                public void quad(Quad quad) {
                    System.out.println(quad.getGraph());
                }
            };
            RDFDataMgr.parse(dest, dataFilename);
            System.out.println();
        }
    }


//        dwimAll(dataFilename);
//    }

     public static void dwimAll(String dataFilename) {
        // NQ:  rest are urn:x-arq:DefaultGraph
        // TriG:
        {
            System.out.println("RDFDataMgr.read(dataset/general)");
            Dataset ds = DatasetFactory.create();
            dwim(ds, dataFilename);
            System.out.println();
        }
        {
            System.out.println("RDFDataMgr.read(dataset/TIM)");
            Dataset ds = DatasetFactory.createTxnMem();
            dwim(ds, dataFilename);
            System.out.println();
        }
        {
            System.out.println("RDFDataMgr.read(dataset/TDB1)");
            Dataset ds = TDBFactory.createDataset();
            dwim(ds, dataFilename);
            System.out.println();
        }

    }



    private static void dwim(Dataset ds, String dataFilename) {
        RDFDataMgr.read(ds, dataFilename);

        Iterator<Quad> it = ds.asDatasetGraph().find();
        while (it.hasNext()) {
            Quad q = it.next();
            System.out.println(q.getGraph());
        }
    }
}
