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

package jena1815_var_type;

import static org.apache.jena.sparql.util.VarUtils.*;

import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.util.VarUtils;

public class VarFinder2
{
    public static VarFinder2 process(Op op) {
        return new VarFinder2(op);
    }

    // See also VarUtils and OpVars.
    // This class is specific to the needs of the main query engine and scoping of variables

//    public static Set<Var> optDefined(Op op) {
//        return VarUsageVisitor.apply(op).optDefines;
//    }
//
//    public static Set<Var> fixed(Op op) {
//        return VarUsageVisitor.apply(op).defines;
//    }
//
//    public static Set<Var> filter(Op op) {
//        return VarUsageVisitor.apply(op).filterMentions;
//    }
//
//    public static Set<Var> assignMention(Op op) {
//        return VarUsageVisitor.apply(op).assignMentions;
//    }

    VarUsageVisitor varUsageVisitor;

    private VarFinder2(Op op)
    { varUsageVisitor = VarUsageVisitor.apply(op); }

    public Set<Var> getFixed()      { return varUsageVisitor.defines; }
    public Set<Var> getOpt()        { return varUsageVisitor.optDefines; }
    public Set<Var> getExpr()       { return varUsageVisitor.exprMentions; }
    public Set<Var> getExprOnly()   { return varUsageVisitor.exprMentionsOnly; }
    public Set<Var> getAssign()     { return varUsageVisitor.assignment; }

    @Override
    public String toString() {
        return varUsageVisitor.toString();
    }

    public void print(PrintStream out) {
        out.printf("  Fixed:        %s\n", getFixed());
        out.printf("  Opt:          %s\n", getOpt());
        out.printf("  Expr:         %s\n", getExpr());
        out.printf("  Expr only:    %s\n", getExprOnly());
        out.printf("  Assign:       %s\n", getAssign());
    }

    public void printFlat(PrintStream out) {
        out.println(varUsageVisitor.toString());
    }

    // This is a one-level visitor that walks down the algebra tree in each op as needed.
    private static class VarUsageVisitor implements OpVisitor {

        /** Apply and return a clean VarUsageVisitor to the Op */
        static VarUsageVisitor apply(Op op) {
            VarUsageVisitor v = new VarUsageVisitor();
            op.visit(v);
            return v;
        }

        // Variables maybe in multiple places.
        // These sets will record each usage type - one variable can be in multiple sets.
        //   defines/optDefines + exprMentions/exprMentionsOnly/assignment
        // Note:
        //   defines/optDefines + exprMentionsOnly because it may be expression-mentioned before defined.
        // { BIND (?x + 1) AS ?Y) ?x ?y ?z }

        // Set by a pattern, always
        private final Set<Var> defines;      // -> patternDefines
        // Set by a patter, optional.
        private final Set<Var> optDefines;   // -> patternOptDefines

        // Used in an expression (filter or assignment) before being defined.
        // An exprMentions variable can be substituted.
        private final Set<Var> exprMentions;

        // Used in an expression (filter or assignment) before being defined, or possibly defined.
        // An exprMentionsOnly variable can not be substituted.
        private final Set<Var> exprMentionsOnly;

        // Variable only VALUES ?X {}, project and BIND( AS ?X), bound(?X)
        private final Set<Var> assignment;

        VarUsageVisitor() {
            defines = createSet();
            optDefines = createSet();
            exprMentions = createSet();
            exprMentionsOnly = createSet();
            assignment = createSet();
        }

        static private <X> Set<X> createSet() {
            // Development.
            return new LinkedHashSet<>();
            //return new HashSet<>();
        }

        // XXX Fast rebuild.
//        VarUsageVisitor(Set<Var> _defines, Set<Var> _optDefines, Set<Var> _filterMentions, Set<Var> _exprMentionsOnly,
//                        Set<Var> _assignMentions, Set<Var> _assigned) {
//            defines = _defines;
//            optDefines = _optDefines;
//            filterMentions = _filterMentions;
//            filterMentionsOnly = _filterMentions2;
//            exprMentionsOnly = _exprMentionsOnly;
//            assigned = _assigned;
//        }

        @Override
        public void visit(OpQuadPattern quadPattern) {
            addVars(defines, quadPattern.getGraphNode(), quadPattern.getBasicPattern());
        }

        @Override
        public void visit(OpBGP opBGP) {
            BasicPattern triples = opBGP.getPattern();
            addVars(defines, triples);
        }

        @Override
        public void visit(OpQuadBlock quadBlock) {
            addVars(defines, quadBlock.getPattern());
        }

        @Override
        public void visit(OpTriple opTriple) {
            addVarsFromTriple(defines, opTriple.getTriple());
        }

        @Override
        public void visit(OpQuad opQuad) {
            addVarsFromQuad(defines, opQuad.getQuad());
        }

        @Override
        public void visit(OpPath opPath) {
            addVarsFromTriplePath(defines, opPath.getTriplePath());
        }

        @Override
        public void visit(OpExt opExt) {
            opExt.effectiveOp().visit(this);
        }

        @Override
        public void visit(OpJoin opJoin) {
            mergeVars(opJoin.getLeft());
            mergeVars(opJoin.getRight());
        }

        @Override
        public void visit(OpSequence opSequence) {
            for ( Op op : opSequence.getElements() )
                mergeVars(op);
        }

        private void mergeVars(Op op) {
            VarUsageVisitor usage = VarUsageVisitor.apply(op);
            mergeVars(usage);
        }

        private void mergeVars(VarUsageVisitor usage) {
            defines.addAll(usage.defines);
            optDefines.addAll(usage.optDefines);
            exprMentions.addAll(usage.exprMentions);
            exprMentionsOnly.addAll(usage.exprMentionsOnly);
            assignment.addAll(usage.assignment);
        }

        @Override
        public void visit(OpLeftJoin opLeftJoin) {
            leftJoin(opLeftJoin.getLeft(), opLeftJoin.getRight(), opLeftJoin.getExprs());
        }

        @Override
        public void visit(OpMinus opMinus) {
            mergeMinusDiff(opMinus.getLeft(), opMinus.getRight());
        }

        @Override
        public void visit(OpDiff opDiff) {
            mergeMinusDiff(opDiff.getLeft(), opDiff.getRight());
        }

        private void mergeMinusDiff(Op left, Op right) {
            mergeVars(left);
            VarUsageVisitor usage = VarUsageVisitor.apply(right);
            // Everything in the right side is really a filter.
            combinefilterMentions(this, usage.exprMentionsOnly);

            exprMentions.addAll(usage.defines);
            exprMentions.addAll(usage.optDefines);
            exprMentions.addAll(usage.exprMentions);
            exprMentions.addAll(usage.assignment);
        }

        private static void combinefilterMentions(VarUsageVisitor usage, Set<Var> mentions) {
            for ( Var v : mentions ) {
                if ( ! usage.defines.contains(v) )
                    usage.exprMentionsOnly.add(v);
            }
        }

        @Override
        public void visit(OpConditional opLeftJoin) {
            leftJoin(opLeftJoin.getLeft(), opLeftJoin.getRight(), null);
        }

        private void leftJoin(Op left, Op right, ExprList exprs) {
            VarUsageVisitor leftUsage = VarUsageVisitor.apply(left);
            VarUsageVisitor rightUsage = VarUsageVisitor.apply(right);

            //mergeVars(leftUsage);

            defines.addAll(leftUsage.defines);
            optDefines.addAll(leftUsage.optDefines);
            exprMentions.addAll(leftUsage.exprMentions);
            exprMentionsOnly.addAll(leftUsage.exprMentionsOnly);
            assignment.addAll(leftUsage.assignment);

            optDefines.addAll(rightUsage.defines); // Asymmetric.
            optDefines.addAll(rightUsage.optDefines);
            exprMentions.addAll(rightUsage.exprMentions);
            exprMentionsOnly.addAll(rightUsage.exprMentionsOnly);
            assignment.addAll(rightUsage.assignment);

            // Remove any definites that are in the optionals
            // as, overall, they are definites
            optDefines.removeAll(leftUsage.defines);

            // And the associated filter.
            if ( exprs != null ) {
                // XXX exprMentionsOnly?
                processExpr(exprs, rightUsage.defines);
                ExprVars.varsMentioned(exprMentions, exprs);
            }
        }

        // additionalDefines - set of variables which are defined when the filter is executed.
        private void processExpr(ExprList exprs, Set<Var> defined) {
            Set<Var> vars = ExprVars.getVarsMentioned(exprs);
            // Don't exprMention and also
            exprMentions.addAll(vars);
            for ( Var v : vars ) {
                if ( ! defines.contains(v) && (defined == null || ! defined.contains(v) ) )
                    exprMentionsOnly.add(v);
            }
        }

        @Override
        public void visit(OpUnion opUnion) {
            VarUsageVisitor usage1 = VarUsageVisitor.apply(opUnion.getLeft());
            VarUsageVisitor usage2 = VarUsageVisitor.apply(opUnion.getRight());

            // Fixed both sides.
            Set<Var> fixed = SetUtils.intersection(usage1.defines, usage2.defines);
            defines.addAll(fixed);

            // Fixed one side or the other, not both.
            Set<Var> notFixed = SetUtils.symmetricDifference(usage1.defines, usage2.defines);
            optDefines.addAll(notFixed);

            optDefines.addAll(usage1.optDefines);
            optDefines.addAll(usage2.optDefines);

            exprMentions.addAll(usage1.exprMentions);
            exprMentions.addAll(usage2.exprMentions);

            exprMentionsOnly.addAll(usage1.exprMentionsOnly);
            exprMentionsOnly.addAll(usage2.exprMentionsOnly);

            assignment.addAll(usage1.assignment);
            assignment.addAll(usage2.assignment);
        }

        @Override
        public void visit(OpDisjunction opDisjunction) {
            opDisjunction.getElements().forEach(op->mergeVars(op));
        }

        @Override
        public void visit(OpGraph opGraph) {
            addVar(defines, opGraph.getNode());
            opGraph.getSubOp().visit(this);
        }

        @Override
        public void visit(OpFilter opFilter) {
            /// XXX recursive apply?
            opFilter.getSubOp().visit(this);
            processExpr(opFilter.getExprs(), this.defines);
        }

        @Override
        public void visit(OpAssign opAssign) {
            opAssign.getSubOp().visit(this);
            processAssignVarExprList(opAssign.getVarExprList());
        }

        @Override
        public void visit(OpExtend opExtend) {
            opExtend.getSubOp().visit(this);
            processAssignVarExprList(opExtend.getVarExprList());
        }

        private void processAssignVarExprList(VarExprList varExprList) {
            varExprList.forEachVarExpr((v,e)-> {
                assignment.add(v);
                // Find variables in the expression.
                if ( e != null ) {
                    Set<Var> vars = ExprVars.getVarsMentioned(e);
                    // What about optDefines? Not stong enough fo substitution.
                    //
                   for ( Var ev : vars ) {
                       if ( this.defines.contains(ev) )
                           this.exprMentions.add(ev);
                       else
                           this.exprMentionsOnly.add(ev);
                   }
                }
//                if ( e != null )
//                    // XXX
//                    ExprVars.nonOpVarsMentioned(defines, e);
            });
        }

        @Override
        public void visit(OpProject opProject) {
            List<Var> vars = opProject.getVars();
            VarUsageVisitor subUsage = VarUsageVisitor.apply(opProject.getSubOp());

            // Mask
            subUsage.defines.retainAll(vars);
            subUsage.optDefines.retainAll(vars);
            subUsage.exprMentions.retainAll(vars);
            subUsage.exprMentionsOnly.retainAll(vars);
            subUsage.assignment.retainAll(vars);
            // Use
            mergeVars(subUsage);
        }

        @Override
        public void visit(OpTable opTable) {
            defines.addAll(opTable.getTable().getVars());
        }

        @Override
        public void visit(OpNull opNull) {}

        @Override
        public void visit(OpPropFunc opPropFunc) {
            VarUtils.addVars(defines, opPropFunc.getSubjectArgs());
            VarUtils.addVars(defines, opPropFunc.getObjectArgs());

            mergeVars(opPropFunc.getSubOp());

            // If definite (from the property function), remove from optDefines.
            optDefines.removeAll(this.defines);
        }

        // Ops that add nothing to variable scoping.
        // Some can't appear without being inside a project anyway
        // but we process generally where possible.

        @Override
        public void visit(OpReduced opReduced)      { mergeVars(opReduced.getSubOp()); }

        @Override
        public void visit(OpDistinct opDistinct)    { mergeVars(opDistinct.getSubOp()); }

        @Override
        public void visit(OpSlice opSlice)          { mergeVars(opSlice.getSubOp()); }

        @Override
        public void visit(OpLabel opLabel)          { mergeVars(opLabel.getSubOp()); }

        @Override
        public void visit(OpList opList)            { mergeVars(opList.getSubOp()); }

        @Override
        public void visit(OpService opService)      { mergeVars(opService.getSubOp()); }

        @Override
        public void visit(OpTopN opTop)             { mergeVars(opTop.getSubOp()); }

        @Override
        public void visit(OpOrder opOrder) {
            mergeVars(opOrder.getSubOp());
            opOrder.getConditions().forEach(sc-> {
                sc.getExpression()   ;
            });
        }

        @Override
        public void visit(OpGroup opGroup) {
            // Only the group variables are visible.
            // So not the subOp, and not expressions.
            VarExprList varExprs = opGroup.getGroupVars();
            varExprs.forEachVar((v)->addVar(defines, v));
        }

        @Override
        public void visit(OpDatasetNames dsNames) {
            addVar(defines, dsNames.getGraphNode());
        }

        @Override
        public void visit(OpProcedure opProc) {
            for ( Expr expr :  opProc.getArgs() ) {
                Set<Var> vars = expr.getVarsMentioned();
                defines.addAll(vars);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Fixed:").append(defines);
            sb.append(", Opt:").append(optDefines);
            sb.append(", Expr:").append(exprMentions);
            sb.append(", ExprOnly:").append(exprMentionsOnly);
            sb.append(", Assign:").append(assignment);
            return sb.toString();
        }
    }
}
