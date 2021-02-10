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

package rdf_star;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sys.JenaSystem;


public class DevRDFStar {
//    asf.yaml
//    jena2008-shaclc-path


    static {
        JenaSystem.init();
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    // Annotations.

    // "TurtleParserBase" -- "LangParseBase"
    // \% escape in prefix names?

    // LangTurtleBase - rename rules.

    /*ADD SURROGATES TO PN_CHAR_BASE
      Hi D800-DBFF
      Lo DC00-DFFF
    */

    public static void main(String...a) {
    }
}
