/**
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

package fmt ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.util.ExprUtils ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.junit.Assert ;
import org.junit.Test ;

public class TestFmtExprSPARQL_Assoc {
    
    @Test public void expr_fmt_1() { testExprFmt("1") ; }
    @Test public void expr_fmt_2() { testExprFmt("1+2") ; }
    @Test public void expr_fmt_3() { testExprFmt("1+2+3") ; }
    @Test public void expr_fmt_4() { testExprFmt("1+(2+3)") ; }
    @Test public void expr_fmt_5() { testExprFmt("(1+2)+3") ; }
    
    @Test public void expr_fmt_6() { testExprFmt("(1+(2+3))") ; }
    @Test public void expr_fmt_7() { testExprFmt("((1+2)+3)") ; }
    
    @Test public void expr_fmt_10() { testExprCheck("1+2", "( 1 + 2 )") ; }
    @Test public void expr_fmt_11() { testExprCheck("1+2+3", "( 1 + 2 + 3 )") ; }
    @Test public void expr_fmt_12() { testExprCheck("1+(2+3)", "( 1 + ( 2 + 3 ) )") ; }
    @Test public void expr_fmt_13() { testExprCheck("(1+2)+3", "( 1 + 2 + 3 )") ; }
    
    @Test public void expr_fmt_14() { testExprCheck("(1+(2+3))", "( 1 + ( 2 + 3 ) )") ; }
    @Test public void expr_fmt_15() { testExprCheck("((1+2)+3)", "( 1 + 2 + 3 )") ; }

    @Test public void expr_fmt_16() { testExprCheck("(1+2)*(3+4)", "( ( 1 + 2 ) * ( 3 + 4 ) )") ; }
    @Test public void expr_fmt_17() { testExprCheck("(1*2)+(3*4)", "( ( 1 * 2 ) + ( 3 * 4 ) )") ; }
    
    @Test public void expr_fmt_18() { testExprFmt("1+2-3+4-5") ; }

    @Test public void expr_fmt_21() { testExprFmt("true") ; }
    @Test public void expr_fmt_22() { testExprFmt("true && false") ; }

    @Test public void expr_fmt_23() { testExprCheck("1 && 2 && 3", "( 1 && 2 && 3 )") ; }
    @Test public void expr_fmt_24() { testExprCheck("1 || 2 || 3", "( 1 || 2 || 3 )") ; }
    
    @Test public void expr_fmt_25() { testExprCheck("1 || 2 && 3", "( 1 || ( 2 && 3 ) )") ; }
    @Test public void expr_fmt_26() { testExprCheck("1 || ( 2 && 3 )", "( 1 || ( 2 && 3 ) )") ; }
    @Test public void expr_fmt_27() { testExprCheck("( 1 || 2 ) && 3", "( ( 1 || 2 ) && 3 )") ; }
    
    @Test public void expr_fmt_28() { testExprCheck("1 && 2 || 3", "( ( 1 && 2 ) || 3 )") ; }
    @Test public void expr_fmt_29() { testExprCheck("1 && ( 2 || 3 )", "( 1 && ( 2 || 3 ) )") ; }
    @Test public void expr_fmt_30() { testExprCheck("( 1 && 2 ) || 3", "( ( 1 && 2 ) || 3 )") ; }
    
    private String testExprCheck(String testString , String expected) {
        String s =  testExprFmt(testString, expected) ;
        Assert.assertEquals(expected, s) ;
        return s ;
    }
    
    private String testExprFmt(String testString) {
        return testExprFmt(testString, null) ;
    }
    
    private String testExprFmt(String testString, String expected) {
        Expr expr = ExprUtils.parse(testString) ;
        IndentedLineBuffer out = new IndentedLineBuffer() ;
        FmtExprSPARQL_Assoc fmt = new FmtExprSPARQL_Assoc(out, FmtUtils.sCxt()) ;
        fmt.format(expr);
        String s = out.asString() ;
        Expr expr2 = ExprUtils.parse(s) ;
        Assert.assertEquals(expr, expr2);
        
        NodeValue nv1 = ExprUtils.eval(expr) ;
        NodeValue nv2 = ExprUtils.eval(expr2) ;
        Assert.assertEquals(nv1, nv2);
        
        if ( expected != null ) {
            Expr expr3 = ExprUtils.parse(expected) ;
            Assert.assertEquals(expr3, expr2);
        }
        return s ;
    }
}