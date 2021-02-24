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

package fmt;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.serializer.FmtExprSPARQL;
import org.apache.jena.sparql.serializer.FormatterElement;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.util.FmtUtils;

/** Output expressions in SPARQL syntax */

public class FmtExprSPARQL_Assoc {

// /* Precedence table
// *
// */
// private static int NO_PREC = -99 ;
// private static Map<String, Integer> precedence = new HashMap<>() ;
// static {
// /*
// Java:
// multiplicative * / %
// additive + -
// shift << >> >>>
// relational < > <= >= instanceof
// equality == !=
// bitwise AND &
// bitwise exclusive OR ^
// bitwise inclusive OR |
// logical AND &&
// logical OR ||
// ternary ? :
// assignment = += -= *= /= %= &= ^= |= <<= >>= >>>=
// */
//
// precedence.put("*", 1) ;
// precedence.put("/", 1) ;
//
// precedence.put("+", 2) ;
// precedence.put("-", 2) ;
//
//// precedence.put("=", 3) ;
//// precedence.put("!=", 3) ;
//
// precedence.put("||", 5) ;
// precedence.put("&&", 5) ;
// }
//
//
// private static int getPrecedence(Expr expr) {
// if ( isBinaryOp(expr) ) {
// ExprFunction2 ex2 = (ExprFunction2)expr ;
// return getPrecedence(ex2.getOpName()) ;
// }
// return NO_PREC ;
// }
//
// private static int getPrecedence(String op) {
// if ( precedence.containsKey(op) )
// return precedence.get(op) ;
// return NO_PREC ;
// }

    /** Test whether the expression is an associate operator (that we handle) */
    private static boolean isAssocOp(Expr expr) {
        if ( !expr.isFunction() )
            return false;
        if ( expr instanceof E_LogicalOr )
            return true;
        return false;
    }

// private static boolean isBinaryOp(Expr expr) {
// if ( expr.isFunction() && expr instanceof ExprFunction2 ) {
// ExprFunction2 ex2 = (ExprFunction2)expr ;
// return ex2.getOpName() != null ;
// }
// return false ;
// }
//
    /** Get the opName of a associative operator we handle specially */
    private static String getAssocOpName(Expr expr) {
        if ( !isAssocOp(expr) )
            return null;
        return expr.getFunction().getOpName();
    }

    static final int INDENT = 2;

    private FmtExprARQVisitor visitor;

    public FmtExprSPARQL_Assoc(IndentedWriter writer, SerializationContext cxt) {
        visitor = new FmtExprARQVisitor(writer, cxt);
    }

    // Top level writing of an expression.
    public void format(Expr expr) {
        expr.visit(visitor);
    }

    public static void format(IndentedWriter out, Expr expr) {
        format(out, expr, null);
    }

    public static void format(IndentedWriter out, Expr expr, SerializationContext cxt) {
        FmtExprSPARQL fmt = new FmtExprSPARQL(out, cxt);
        fmt.format(expr);
    }

    private static class FmtExprARQVisitor implements ExprVisitor {
        IndentedWriter out;
        SerializationContext context;

        public FmtExprARQVisitor(IndentedWriter writer, PrefixMapping pmap) {
            this(writer, new SerializationContext(pmap, null));
        }

        public FmtExprARQVisitor(IndentedWriter writer, SerializationContext cxt) {
            out = writer;
            context = cxt;
            if ( context == null )
                context = new SerializationContext();
        }

        @Override
        public void visit(ExprFunction0 expr) {
            if ( expr.getOpName() == null ) {
                printInFunctionForm(expr);
                return;
            }
            out.print("( ");
            out.print(expr.getOpName());
            out.print(" ");
        }

        @Override
        public void visit(ExprFunction1 expr) {
            if ( expr.getOpName() == null ) {
                printInFunctionForm(expr);
                return;
            }
            out.print("( ");
            out.print(expr.getOpName());
            out.print(" ");
            expr.getArg().visit(this);
            out.print(" )");
        }

        @Override
        public void visit(ExprFunction2 expr) {
            if ( isAssocOp(expr) ) {
                out.print("( ");
                printAssoc(expr, expr.getOpName());
                out.print(" )");
                return;
            }

            if ( expr.getOpName() == null ) {
                printInFunctionForm(expr);
                return;
            }
            out.print("( ");
            expr.getArg1().visit(this);
            out.print(" ");
            out.print(expr.getOpName());
            out.print(" ");
            expr.getArg2().visit(this);
            out.print(" )");
        }

        // ExprFunction2 vs BinaryOperator.
        // The pasrer produces left-associatve trees for "1+2+3"
        // i.e. (1+2)+3
        // This undoes that but looses explicit (1+2)+3
        // Short of an explicit "brackets" node in the Expr tree, or a flag on the
        // Expr,
        // there is the information to distinguish these two cases.

        private void printAssoc(ExprFunction2 expr, String opName) {
            // If same operator, flatten left.

            Expr left = expr.getArg1();
            String opNameLeft = getAssocOpName(left);
            if ( opNameLeft != null && opNameLeft.equals(opName) )
                printAssoc((ExprFunction2)left, opNameLeft);
            else
                writeMaybeBracketted(left);
            // Self
            out.print(" ");
            out.print(expr.getOpName());
            out.print(" ");

            // Leave right.

            Expr right = expr.getArg2();
// // Flatten right. Not so simple for "-"
// String opNameRight = getAssocOpName(right) ;
// if ( opNameRight != null && opNameRight.equals(opName) )
// print((ExprFunction2)right, opNameRight) ;
// else
// writeMaybeBracketted(right) ;

            writeMaybeBracketted(right);
            return;
        }

        private void writeMaybeBracketted(Expr expr) {
            expr.visit(this);
        }

// private void writeMaybeBracketted(Expr expr) {
// if ( expr.isConstant() || expr.isVariable() ) {
// expr.visit(this) ;
// return ;
// }
// if ( expr.isFunction() && expr.getFunction().getOpName() == null ) {
// expr.visit(this) ;
// return ;
// }
// out.print("( ") ;
// expr.visit(this) ;
// out.print(" )") ;
// // Operator.
// }

        private void writeRaw(ExprFunction2 expr) {
            expr.getArg1().visit(this);
            out.print(" ");
            out.print(expr.getOpName());
            out.print(" ");
            expr.getArg2().visit(this);
        }

        @Override
        public void visit(ExprFunction3 expr) {
            printInFunctionForm(expr);
        }

        @Override
        public void visit(ExprFunctionN func) {
            if ( func instanceof E_OneOf ) {
                E_OneOf oneOf = (E_OneOf)func;
                out.print("( ");
                oneOf.getLHS().visit(this);
                out.print(" IN ");
                printExprList(oneOf.getRHS());
                out.print(" )");
                return;
            }

            if ( func instanceof E_NotOneOf ) {
                E_NotOneOf oneOf = (E_NotOneOf)func;
                out.print("( ");
                oneOf.getLHS().visit(this);
                out.print(" NOT IN ");
                printExprList(oneOf.getRHS());
                out.print(" )");
                return;
            }
            printInFunctionForm(func);
        }

        private void printInFunctionForm(ExprFunction func) {
            out.print(func.getFunctionPrintName(context));
            printExprList(func.getArgs());
        }

        private void printExprList(Iterable<Expr> exprs) {
            out.print("(");
            boolean first = true;
            for ( Expr expr : exprs ) {
                if ( expr == null )
                    break;
                if ( !first )
                    out.print(", ");
                first = false;
                expr.visit(this);
            }
            out.print(")");
        }

        @Override
        public void visit(ExprFunctionOp funcOp) {
            String fn = funcOp.getFunctionPrintName(context);
            if ( funcOp instanceof E_NotExists )
                fn = "NOT EXISTS";
            else if ( funcOp instanceof E_Exists )
                fn = "EXISTS";
            else
                throw new ARQInternalErrorException("Unrecognized ExprFunctionOp: " + fn);

            FormatterElement fmtElt = new FormatterElement(out, context);
            out.print(fn);
            out.print(" ");
            int indent = out.getAbsoluteIndent();
            int currentCol = out.getCol();
            try {
                out.setAbsoluteIndent(currentCol);
                Element el = funcOp.getElement();
                if ( el == null )
                    el = OpAsQuery.asQuery(funcOp.getGraphPattern()).getQueryPattern();
                el.visit(fmtElt);
            }
            finally {
                out.setAbsoluteIndent(indent);
            }
        }

        @Override
        public void visit(NodeValue nv) {
            out.print(nv.asQuotedString(context));
        }

        @Override
        public void visit(ExprNone none) {
            out.print("NONE");
        }

        @Override
        public void visit(ExprTripleTerm tripleTerm) {
            Triple t = tripleTerm.getTriple();
            out.print(FmtUtils.stringForNode(tripleTerm.getNode(), context));
        }

        @Override
        public void visit(ExprVar nv) {
            String s = nv.getVarName();
            if ( Var.isBlankNodeVarName(s) ) {
                // Return to a bNode via the bNode mapping of a variable.
                Var v = Var.alloc(s);
                out.print(context.getBNodeMap().asString(v));
            } else {
                // Print in variable form or as an aggregator expression
                out.print(nv.asSparqlExpr());
            }
        }

        @Override
        public void visit(ExprAggregator eAgg) {
            out.print(eAgg.asSparqlExpr(context));
        }
    }
}
