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

package tdb2;

import java.math.BigInteger;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Ext;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.jena.tdb2.store.NodeIdType;
import org.apache.jena.tdb2.store.value.IntegerNode;

/** Node wrapper to TDB2 NodeIds */
public class DevNodeTDB {

    public static void main(String[] args) {
        //Node n = SSE.parseNode("123");
        //NodeId nid = NodeIdFactory.createPtr(x);
        NodeId nid = NodeIdFactory.createValue(NodeIdType.XSD_INTEGER, 123);
        Node n = new NodeTDB2(nid);
        System.out.println(n.getLiteralValue());
    }

    static class NodeTDB2 extends Node_Ext<NodeId>{

        private NodeId nid;

        NodeTDB2(NodeId nid) {
            super(nid);
            this.nid = nid;
        }

        @Override
        public String getURI() {
            return "uri:";
        }



        @Override
        public Object getLiteralValue() {
            if ( XSDDatatype.XSDinteger.equals(getLiteralDatatype()) ) {
                long val = IntegerNode.unpack56(nid.getPtrLocation());
                RDFDatatype dt = XSDDatatype.XSDinteger;
                //Node n = NodeFactory.createLiteral(Long.toString(val), dt);
                return BigInteger.valueOf(val);
            }
            return null;
        }

        @Override
        public String getLiteralDatatypeURI() {
            return null;
        }

        @Override
        public RDFDatatype getLiteralDatatype() {
            if ( nid.getTypeValue() == NodeIdType.XSD_INTEGER.type() )
                return XSDDatatype.XSDinteger;
            return null;
        }

//    // From NodeIdType
//    static boolean isInteger(NodeIdType type) {
//        switch(type) {
//            case XSD_INTEGER:
//            case XSD_POSITIVE_INTEGER:
//            case XSD_NEGATIVE_INTEGER:
//            case XSD_NON_NEGATIVE_INTEGER:
//            case XSD_NON_POSITIVE_INTEGER:
//            case XSD_LONG:
//            case XSD_INT:
//            case XSD_SHORT:
//            case XSD_BYTE:
//            case XSD_UNSIGNEDLONG:
//            case XSD_UNSIGNEDINT:
//            case XSD_UNSIGNEDSHORT:
//            case XSD_UNSIGNEDBYTE:
//                return true;
//            default:
//                return false;
//        }
//    }


//        isConcrete()
//        isLiteral()
//        isBlank()
//        isURI()
//        isVariable()
//        isNodeTriple()
//        isNodeGraph()
//        isExt()
//        getBlankNodeId()
//        getBlankNodeLabel()
//        getLiteral()
//        getLiteralValue()
//        getLiteralLexicalForm()
//        getLiteralLanguage()
//        getLiteralDatatypeURI()
//        getLiteralDatatype()
//        getLiteralIsXML()

    }

}
