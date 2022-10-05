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

import java.io.PrintStream;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.compact.ShaclcWriter;
import org.apache.jena.shacl.compact.reader.ShaclcParseException;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.parser.ShaclParseException;
import org.apache.jena.sys.JenaSystem;

public class DevSHACLC {
    static {
        // JenaSystem.DEBUG_INIT = true;\s
        JenaSystem.init();
        //LogCtl.setLog4j2();
        //FusekiLogging.setLogging();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    // SHACLC bugs
    // Too many "."

    public static void main(String...args) {
        String x = """
BASE <http://example.com/ns>
PREFIX ex: <http://example.com/ns#>
PREFIX sh: <http://www.w3.org/ns/shacl#>

shape ex:test {
     ## p1 bad
     ## Compact: ex:p1 [2..1] .
     ## RDF:
##      sh:property  [ sh:maxCount  5 , 1 ;
##                     sh:minCount  2 ;
    ex:p1 [0..1] [2..5] .

     ## Compact: ex:p1 [1..2] .
     ## RDF:
##        sh:property  [ sh:maxCount  5 , 2 ;
##                       sh:minCount  3 , 1 ;
##                       sh:path      ex:p1a
##                     ] ;
    ex:p1a [1..2] [3..5] .

     ## p2 bad : becomes:
##       ex:p2 {
##         ex:p3 datatype=xsd:string .
##         ex:p2 nodeKind=sh:IRI | datatype=xsd:string .
##      }

##     ex:p2 IRI | {
##         ex:p2 xsd:string .
##         ex:p3 xsd:string .
##     } .

    ## OK
    ## ex:p3 BlankNode { datatype=ex:DT in=[ 1 2 ] . } | { datatype=ex:DT1 . } .
}
                """;

        Shapes shapes;
        try {
            Graph g = RDFParser.fromString(x).lang(Lang.SHACLC).toGraph();
            shapes = Shapes.parse(g);
        } catch (ShaclParseException | ShaclcParseException | RiotNotFoundException ex) {
            // Errors parsing the RDF.
            // Errors parsing SHACL Compact Syntax.
            System.err.println(ex.getMessage());
            return;
        } catch ( RiotException ex ) {
            /*ErrorHandler logged this?? */
            System.err.println(ex.getMessage());
            return;
        }

        boolean printText = false;
        boolean printCompact = true;
        boolean printRDF = true;

        boolean outputByPrev = false;
        PrintStream out = System.out;
        PrintStream err = System.err;

        if ( printText ) {
            outputByPrev = printText(out, err, shapes);
        }
        if ( printCompact) {
            if ( outputByPrev ) {
                out.println("- - - - - - - -");
                outputByPrev = false;
            }
            outputByPrev = printCompact(out, err, shapes);
        }
        if ( printRDF) {
            if ( outputByPrev ) {
                out.println("- - - - - - - -");
                outputByPrev = false;
            }
            outputByPrev = printRDF(out, err, shapes);
        }

    }

    private static boolean printText(PrintStream out, PrintStream err, Shapes shapes) {
        IndentedWriter iOut  = new IndentedWriter(out);
        ShLib.printShapes(iOut, shapes);
        iOut.ensureStartOfLine();
        iOut.flush();
        int numShapes = shapes.numShapes();
        int numRootShapes = shapes.numRootShapes();
        if ( true ) {
            System.out.println();
            System.out.println("Target shapes: ");
            shapes.getShapeMap().forEach((n,shape)->{
                if ( shape.hasTarget() )
                    System.out.println("  "+ShLib.displayStr(shape.getShapeNode()));
            });

            System.out.println("Other Shapes: ");
            shapes.getShapeMap().forEach((n,shape)->{
                if ( ! shape.hasTarget() )
                    System.out.println("  "+ShLib.displayStr(shape.getShapeNode()));
            });
        }
        return true;
    }

    private static boolean printRDF(PrintStream out, PrintStream err, Shapes shapes) {
        RDFDataMgr.write(out, shapes.getGraph(), Lang.TTL);
        return somethingWritten(shapes.getGraph());
    }

    private static boolean printCompact(PrintStream out, PrintStream err, Shapes shapes) {
        try {
            ShaclcWriter.print(out, shapes);
        } catch (ShaclException ex) {
            err.println(ex.getMessage());
        }
        return somethingWritten(shapes.getGraph());
    }

    private static boolean somethingWritten(Graph graph) {
        return ! ( graph.isEmpty() && graph.getPrefixMapping().hasNoMappings() );

    }
}
