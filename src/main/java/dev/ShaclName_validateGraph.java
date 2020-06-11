package dev;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShaclName_validateGraph {

    public static String R = "/home/afs/tmp/Shacl/";

    public static Logger logger = LoggerFactory.getLogger(ShaclName_validateGraph.class);

    static final String BDR = "http://purl.bdrc.io/resource/";
    static final String SH = "http://www.w3.org/ns/shacl#";
    static final String SHAPES = R+"ShapeName_01.ttl";
    static final String ONT_GRAPH = "http://purl.bdrc.io/graph/ontologySchema.ttl";
    static final String REZ_NM = "P707";
    static final String DATA = R + REZ_NM+"_nameErrs02.ttl";

    static final Property SH_CONFORMS = ResourceFactory.createProperty(SH+"conforms");
    static final Property SH_RESULT = ResourceFactory.createProperty(SH+"result");
    static final Property SH_VALUE = ResourceFactory.createProperty(SH+"value");
    static final Literal FALSE = ModelFactory.createDefaultModel().createTypedLiteral(false);
    static final ShaclValidator sv = ShaclValidator.get();

    static Model process(Shapes shapes, Graph dataGraph, Resource rez) {
        logger.info("Validating Node {} with {}", rez.getLocalName(), SHAPES);
        ValidationReport report = sv.validate(shapes, dataGraph, rez.asNode());
        return report.getModel();
    }

    // for violations in top-level report validate the sh:value node, if any
    static Model completeReport(Shapes shapes, Graph dataGraph, Model top) {
        Model complete = ModelFactory.createDefaultModel();
        complete.add(top);

        if (top.contains((Resource) null, SH_CONFORMS, FALSE)) {
            StmtIterator valItr = top.listStatements((Resource) null, SH_VALUE, (RDFNode) null);
            while (valItr.hasNext()) {
                Statement valStmt = valItr.removeNext();
                RDFNode valNode = valStmt.getObject();

                if (valNode.isResource()) {
                    Model subReport = process(shapes, dataGraph, (Resource) valNode);
                    complete.add(subReport);
                }
            }
        }

        return complete;
    }

    public static void main(String ...args) {
        Graph shapesGraph = RDFDataMgr.loadGraph(SHAPES);
        Shapes shapes = Shapes.parse(shapesGraph);
        // merge ontGraph and targetGraph
        Graph ontGraph = RDFDataMgr.loadGraph(ONT_GRAPH);
        Graph targetGraph = RDFDataMgr.loadGraph(DATA);
        Model datasetModel = ModelFactory.createModelForGraph(ontGraph);
        datasetModel.add(ModelFactory.createModelForGraph(targetGraph));
        Graph dataGraph = datasetModel.getGraph();

        Resource rez = ResourceFactory.createResource(BDR+REZ_NM);
        Model topReport = process(shapes, dataGraph, rez);

        Model finalReport = completeReport(shapes, dataGraph, topReport);
        System.out.println("--");
        RDFDataMgr.write(System.out, finalReport, Lang.TTL);
    }
}