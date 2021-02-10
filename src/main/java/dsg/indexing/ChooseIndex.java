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

package dsg.indexing;

import static dsg.indexing.ChooseIndex.IndexOrder.G;
import static dsg.indexing.ChooseIndex.IndexOrder.O;
import static dsg.indexing.ChooseIndex.IndexOrder.P;
import static dsg.indexing.ChooseIndex.IndexOrder.S;

import java.util.EnumSet;

public class ChooseIndex {
    public static void main(String ...a) {
        
    }
    
    static enum IndexOrder { G, S, P, O }; 
    
    static EnumSet<IndexOrder> setGSPO = EnumSet.of(G, S, P, O);
}
