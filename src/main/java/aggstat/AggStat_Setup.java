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

import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.expr.aggregate.AccumulatorFactory ;
import org.apache.jena.sparql.expr.aggregate.AggregateRegistry ;

public class AggStat_Setup {

    public static String BASE = ARQConstants.ARQFunctionLibraryURI ; //"urn:arq:" ;

    public static void setup() {
        AccumulatorFactory f_VarP =     (agg) -> new AggStatVarP(agg.getExpr()) ;
        AccumulatorFactory f_Var =      (agg) -> new AggStatVar(agg.getExpr()) ;
        AccumulatorFactory f_StdevP =   (agg) -> new AggStatStdDevPop(agg.getExpr()) ;
        AccumulatorFactory f_Stdev =    (agg) -> new AggStatStdDev(agg.getExpr()) ;
        
        AggregateRegistry.register(BASE+"stddevp", f_StdevP, null) ;     // Common? typo
        AggregateRegistry.register(BASE+"stddev", f_Stdev, null) ;       // Common? typo
        AggregateRegistry.register(BASE+"stdevp", f_StdevP, null) ;      // SQL spelling
        AggregateRegistry.register(BASE+"stdev", f_Stdev, null) ;        // SQL spelling
        
        AggregateRegistry.register(BASE+"variancep", f_VarP, null) ;     // Longer
        AggregateRegistry.register(BASE+"variance", f_Var, null) ;       // Longer
        AggregateRegistry.register(BASE+"varp", f_VarP, null) ;          // SQL spelling
        AggregateRegistry.register(BASE+"var", f_Var, null) ;            // SQL spelling
    }
}
