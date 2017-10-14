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

package javascript_functions;
import java.util.Arrays;
import java.util.StringJoiner;

import javax.script.ScriptException;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.SSE;

/** Javascript implemented SPARQL custom functions for ARQ */
public class DevFunctionJavaScript {
    public static void main(String[] args) throws ScriptException {
        
        String camelCaseJS = 
            StrUtils.strjoinNL
             ("function toCamelCase(str) { return str.split(' ').map(cc).join('');}"
             ,"function ucFirst(word)    { return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();}"
             ,"function lcFirst(word)    { return word.toLowerCase(); }"
             ,"function cc(word,index)   { return (index == 0) ? lcFirst(word) : ucFirst(word); }"
             );
        
        ARQ.getContext().set(FunctionJavaScript.symJS, StrUtils.strjoinNL
                                    ("var barx = '||';"
                                    ,"function bar(x,y)     { return barx+combine(x,y)+barx } "
                                    ,"function node(n)      { n.value ; return true }"
                                    ,"function combine(x,y) { return x+y }"
                                    , camelCaseJS
                                    ));

        exec("toCamelCase", "'hello  world'");
        //exec("toCamelCase", "''");
        exec("node", "'foo'^^ex:bar");
        exec("bar", "'foo'^^ex:bar", "'bar'");
    }

    public static Object exec(String fn, String... args) throws ScriptException {
        NodeValue[] nvs = new NodeValue[args.length];
        for ( int i = 0 ; i < args.length ; i++ ) {
            nvs[i] = NodeValue.makeNode(SSE.parseNode(args[i]));
        }
        return exec1(fn, nvs);
    }
    
    public static Object exec1(String fn, NodeValue... args) throws ScriptException {
        StringJoiner sj = new StringJoiner(", ");
        for ( NodeValue nv : args )
            sj.add(NodeFmtLib.str(nv.asNode()));
        System.out.printf("%s( %s )",fn,sj);
        FunctionJavaScript x = new FunctionJavaScript(fn);
        NodeValue nvr = x.exec(Arrays.asList(args));
        System.out.print(" --> ");
        if ( nvr == null  ) 
            System.out.print("null");
        else
            System.out.print(NodeFmtLib.str(nvr.asNode()));
        System.out.println();
        return nvr;
    }
}