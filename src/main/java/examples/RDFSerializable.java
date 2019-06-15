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

package examples;

import java.io.* ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.system.SNode;
import org.apache.jena.riot.system.SQuad;
import org.apache.jena.riot.system.STriple;
import org.apache.jena.sparql.sse.SSE ;

/**
 * Java serialization of core RDF classes : Quad, Triple, Node as wrapper objects
 * {@link SQuad}, {@link STriple} and {@link SNode}
 */
public class RDFSerializable {

    // ---- Dev
    public static void main(String ...args) throws IOException, ClassNotFoundException {
        exNode() ;
        System.out.println() ;
        exTriple();
    }
    
    // ---- Examples
    public static void exTriple() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream() ; 
        ObjectOutputStream oos = new ObjectOutputStream(out) ;
        Node n = NodeFactory.createBlankNode() ;
        Triple x = Triple.create(n, SSE.parseNode(":predicate"), n) ;
        oos.writeObject("BEFORE");
        oos.writeObject(x);
        oos.writeObject("AFTER");
        
        byte b[] = out.toByteArray() ;
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b)) ;
        
        String string1 = (String)(ois.readObject()) ;
        Triple s = (Triple)(ois.readObject()) ;
        String string2 = (String)(ois.readObject()) ;
        
        System.out.println(string1) ;
        SSE.write(s);
        System.out.println() ;
        System.out.println(string2) ;
    }
    
    public static void exNode() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream() ; 
        ObjectOutputStream oos = new ObjectOutputStream(out) ;
        Node n = NodeFactory.createBlankNode() ;
        oos.writeObject(n);
        
        byte b[] = out.toByteArray() ;
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b)) ;
        Node x = (Node)(ois.readObject()) ;
        SSE.write(x) ;
        System.out.println() ;
        
        if ( n.equals(x) )
            System.out.printf("Same blank node %s\n", n.getBlankNodeLabel()) ;
        else
            System.out.printf("Different blank nodes %s %s\n", n.getBlankNodeLabel(), x.getBlankNodeLabel()) ;
    }
}

