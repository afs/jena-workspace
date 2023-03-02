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

package lib.bcd;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdInline;
import org.apache.jena.tdb2.store.value.DecimalNode56;

public class DevBCD {

    public static void main(String...a) {
        dwim(312);
        dwim(Integer.MAX_VALUE);
        dwim(Long.MAX_VALUE);
        dwim(0);
        //dwim(-1);
    }

    public static void dwim(long value) {
        System.out.println("Input:  "+value);
        byte[] bytes = BCD.encodeBCD(value);
        String s = bytes.length==0 ? "''" : Bytes.asHexUC(bytes);
        System.out.println("Hex:    "+s); // 1203
        long out = BCD.decodeBCD(bytes);
        System.out.println("Output: "+out);

//        System.out.println();
//      {
//      byte[] bytes = encodeLoHi(value);
//      String s = Bytes.asHexUC(bytes);
//      System.out.println(s); // 1203
//      int out = decodeLoHi(bytes);
//      System.out.println(out);
//      System.out.println(stringLoHi(bytes)); // Leading zero.
//  }
    }


    public static void mainNodec(String[] args) {
        decimal("0");
        decimal("01");
        decimal("1");
        decimal("12");
        decimal("123");
        decimal("-123");
        decimal("-12");
        decimal("-1");
    }

    /** Decimal BCD format: 8 bits scale, 48bits of signed valued. See {@link DecimalNode56}. */
    static void decimal(String lex) {
        System.out.println("---- lex: '"+lex+"'");
        Node node = NodeFactory.createLiteral(lex, XSDDatatype.XSDdecimal);

        NodeId nid = NodeIdInline.inline(node);
        System.out.println(nid);
        Node node2 = NodeIdInline.extract(nid);
        System.out.println(node2);
    }
}

