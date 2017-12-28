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
import java.util.List;

import javax.script.*;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.sse.builders.ExprBuildException;
import org.apache.jena.sparql.util.Symbol;

/**
 * Javascript implemented SPARQL custom functions for ARQ. The javascript function is
 * called with arguent which are mapped so that strings, numbers and booleans become the
 * equivalent native javascript object, and anything else becomes a {@link NV}, a
 * javascript object providing access to the RDF features such as datatype.
 * {@link NV#toString} return a string so integration working with URIs can treat URIs as
 * string which is natural in jaavscript and aligned to <a
 * href="https://github.com/rdfjs/representation-task-force/"
 * >rdfjs/representation-task-force</a>.
 * <p>
 * Function are loaded from the file named in context setting
 * {@link FunctionJavaScript#symJS}.
 * <p>
 * See the source for {@link LibNV#fromNodeValue} and {@link LibNV#toNodeValue(Object)} for details
 * of the conversion into and out of JavaScript objects. Note: there is an attempt to
 * reconstruct the datatype of the result of the function into {@code xsd:integer} and
 * {@code xsd:double}.
 */
public class FunctionJavaScript extends FunctionBase {

    public static Symbol symJS = SystemARQ.allocSymbol("javacript-lib");
    
    private final String scriptLib;
    
    // Not thread safe in general.
    private final ScriptEngine scriptEngine;
    private CompiledScript compiledScript;
    
    private final Invocable invoc;
    private final String functionName;

    private boolean initialized = false;
    
    public FunctionJavaScript(String functionName) throws ScriptException {
        this.functionName = functionName;
        this.scriptLib = ARQ.getContext().getAsString(symJS);
        ScriptEngineManager manager = new ScriptEngineManager();
        scriptEngine = manager.getEngineByName("nashorn");
        // Add function to script engine.
        invoc = (Invocable)scriptEngine;
    }
    
    @Override
    public void checkBuild(String uri, ExprList args) {
        try {
            initialized = true;
            Object x = scriptEngine.eval(scriptLib);
        }
        catch (ScriptException e) {
            throw new ExprBuildException("Failed to load Javascript", e);
        }
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        if ( ! initialized )
            checkBuild(null, null);
        
        try {
            // Convert NodeValues to types more akin to Javascript. 
            // Pass strings as string, and numbers as Number.
            Object[] a = new Object[args.size()];
            for ( int i = 0 ; i < args.size(); i++ )
                a[i] = LibNV.fromNodeValue(args.get(i));
            Object r = invoc.invokeFunction(functionName, a);
            NodeValue nv = LibNV.toNodeValue(r);
            return nv;
        }
        catch (NoSuchMethodException | ScriptException e) {
            throw new ExprEvalException("Failed to evaluate javascript function '"+functionName+"'", e);
        }
    }
}