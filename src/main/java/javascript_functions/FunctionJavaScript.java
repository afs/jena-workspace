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
import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import javax.script.*;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.sse.builders.ExprBuildException;
import org.apache.jena.sparql.util.Symbol;

/** Javascript implemented SPARQL custom functions for ARQ */
public class FunctionJavaScript extends FunctionBase {

    // NodeValue objects.
    // Javascript expression.
    
    public static Symbol symJS = Symbol.create("js-lib");
    
    private final String scriptLib;
    
    // Not thread safe in general.
    private final ScriptEngine scriptEngine;
    CompiledScript compiledScript;
    
    
    private final Invocable invoc;
    private final String functionName;

    private boolean initialized = false;
    
    FunctionJavaScript(String functionName) throws ScriptException {
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

    static NodeValue[] marker = new NodeValue[0];

    @Override
    public NodeValue exec(List<NodeValue> args) {
        if ( ! initialized )
            checkBuild(null, null);
        
        try {
            // Convert NodeValues to types more akin to Javascript. 
            // Pass strings as string, and numbers as Number.
            Object[] a = new Object[args.size()];
            for ( int i = 0 ; i < args.size(); i++ )
                a[i] = fromNodeValue(args.get(i));
            Object r = invoc.invokeFunction(functionName, a);
            NodeValue nv = toNodeValue(r);
            return nv;
        }
        catch (NoSuchMethodException | ScriptException e) {
            throw new ExprEvalException("Failed to evaluate javascript function '"+functionName+"'", e);
        }
    }

    //NodeValue -> Javascript
    public static Object fromNodeValue(NodeValue nv) {
        if ( nv.isString() )
            return nv.getString();
        if ( nv.isNumber() ) {
            if ( nv.isInteger())
                return nv.getInteger();
            if ( nv.isDecimal() )
                return nv.getDecimal();
            if ( nv.isDouble() )
                return nv.getDouble();
        }
        if ( nv.isBoolean() )
            return nv.getBoolean();
        return new NV(nv);
    }
    
    // Javascript -> NodeValue.
    public static NodeValue toNodeValue(Object r) {
        if ( r == null )
            return null;
        if ( r instanceof NV )
            return ((NV)r).nv();
        if ( r instanceof NodeValue )
            return (NodeValue)r;
        // May not be a String.  String.toString is very efficient!
        // https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/prog_guide/javascript.html#A1147390
        if ( r instanceof CharSequence )
            return NodeValue.makeString(((CharSequence)r).toString());
        if ( r instanceof Number )
            return number2value((Number)r);
        if ( r instanceof Boolean ) {
            return NodeValue.makeBoolean((Boolean)r); 
        }
        if ( r instanceof URI ) {
            Node n = NodeFactory.createURI(((URI)r).toString());
            return NodeValue.makeNode(n); 
        }
        throw new ExprEvalException("Can't convert '"+r+"' to a NodeValue.  r is of class "+r.getClass().getName());  
    }

    static boolean narrowDoubles = true;
    
    // Convert the types that Nashorn can return.
    private static NodeValue number2value(Number r) {
        if ( r instanceof Integer )
            return NodeValue.makeInteger((Integer)r);
        if ( r instanceof Long )
            return NodeValue.makeInteger((Long)r);
        if ( r instanceof Double ) {
            double d = (Double)r;
            if ( narrowDoubles && Double.isFinite(d)) {
                try {
                    long x = (long)d;
                    // Check for loss in conversion
                    if ( x == d )
                        return NodeValue.makeInteger(x);
                    // If is a very large integer (larger than long)?
                    if ( d == Math.floor(d) && Double.isFinite(d) ) {
                        BigInteger big = new BigInteger(Double.toString(x));
                        return NodeValue.makeInteger(big); 
                    }
                    if ( false ) {
                        // This always works but it is confusing decimal and double.
                        // Leave doubles as doubles.
                        return NodeValue.makeDecimal(d);
                    }
                } catch (NumberFormatException ex) {
                    // Shouldn't happen.
                    throw new ExprEvalException("Bad number format", ex);
                }
            }
            return NodeValue.makeDouble(d);
        }
        throw new ExprEvalException("Unknown return type for number: "+r); 
    }
}