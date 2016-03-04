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

package creation;

public class Jena1147_CacheRIOT {
    
    public static void main(String ...a) {
        String FN1 = "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz" ;
        String FN2 = "/home/afs/Datasets/BSBM/bsbm-250k.nt.gz" ;
        String FN3 = "/home/afs/Datasets/Chebi/chebi.nt" ;
        String FN4 = "/home/afs/Datasets/Chembl/chembl_20.0_assay.ttl.gz" ;
        String FN5 = "/home/afs/Datasets/Nature-2015-06/datasets/nq/npg-contributors-dataset.nq" ;

        //CmdTimeSpace.main(FN1, FN2, FN4, FN4, FN5) ;
        CmdTimeSpace.main(FN5) ;
        System.exit(0) ;
        
        // No parser profile - may be better to split policy and per run items.
        // ParsrProfile(per parse) and RDFFactory (policy) 
        // .setBase
        // .setPrologue
        // setLabelToNode
        // setErrorHandler.

        // Make caching slot specific.
        // timing tests
        //   No RDFFactory.
        //   Base RDFFactory.
        //   Caching RDFFactory (variants)
        
    }
}

