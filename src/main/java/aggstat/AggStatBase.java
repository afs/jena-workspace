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

package aggstat;

import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.aggregate.AccumulatorExpr ;
import org.apache.jena.sparql.function.FunctionEnv ;

/** Base for statistics aggregations */ 
abstract class AggStatBase extends AccumulatorExpr {
    // Could also be used for AVG and SUM but those came before this.
    // AVG, SUM, COUNT take DISTINCT 
    
    private static final NodeValue noValuesToAvg = NodeValue.nvZERO ; // null? NaN?

    public AggStatBase(Expr expr) {
        super(expr);
    }
    protected long   count          = 0 ;
    protected double K              = 0 ;
    // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
    // Var = (SumSq − (Sum × Sum) / n) / (n − 1)
    // These are offset by K.
    protected double sum          = 0 ;
    protected double sumSquared   = 0 ;

    @Override
    final protected NodeValue getAccValue() {
        if ( super.errorCount != 0 )
            //throw new ExprEvalException("avg: error in group") ; 
            return null ;
        if ( count <= 0 ) return noValuesToAvg ;
        double x1 = calc() ;
        return NodeValue.makeDouble(x1) ;
    }

    abstract protected double calc() ;

    /** Calculate the variance */ 
    final protected double calcVariance() {
        // (N*sum(?x*?x) - sum(?x) ) / N*(N-1) 
        return calcVariance(count, count-1) ;
    }

    /** Calculate the variance */ 
    final protected double calcVariancePop() {
        // (N*sum(?x*?x) - sum(?x) ) / N*N 
        return calcVariance(count, count) ;
    }

    // Despite being at risk from  cancellation, working in doubles negates this a bit. 
    final protected double calcVariance(long N, long N1) {
//        System.out.printf("sum = %f, sum2 = %f, N=%d\n", sum, sumSquared, count) ;
        if ( N1 < 0 )
            return Double.NaN ;
        if ( N1 == 0 )
            return 0 ;
        double x = sumSquared - (sum*sum)/N ;
        x = x / N1 ;
        return x ;
    }
    
    @Override
    protected void accumulate(NodeValue nv, Binding binding, FunctionEnv functionEnv) {
        // shifted_data_variance
        if ( nv.isNumber() ) {
            double d = nv.getDouble() ;
            count++ ;
            if ( count == 1 ) {
                K = d ;
                sum = (d-K) ; // == 0 of K set.
                sumSquared = (d-K)*(d-K) ; // == 0
                return ; 
            }
            else {
                double dk = (d-K) ;
                double dk2 = dk * dk ;
                sum = sum + dk ;
                sumSquared = sumSquared + dk2 ;
            }
        }
        else
            throw new ExprEvalException("Not a number: "+nv) ;
    }

    @Override
    protected void accumulateError(Binding binding, FunctionEnv functionEnv)
    {}
}