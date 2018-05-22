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

package tdb2.loader.dev;

class LoaderPlan {
    private final boolean mulithreadedInput;
    private final String[] loadGroup3;
    private final String[] loadGroup4;
    private final String[][] secondaryGroups3;
    private final String[][] secondaryGroups4;
    
    LoaderPlan(boolean mulithreadedInput,
                String[] loadGroup3, String[] loadGroup4,
                String[][] secondaryGroups3, String[][] secondaryGroups4) {
        this.mulithreadedInput = mulithreadedInput;
        this.loadGroup3 = loadGroup3;
        this.loadGroup4 = loadGroup4;
        this.secondaryGroups3 = secondaryGroups3;
        this.secondaryGroups4 = secondaryGroups4;
    }
    public boolean mulithreadedInput() { return mulithreadedInput; }
    public String[] primaryLoad3() { return loadGroup3; }
    public String[] primaryLoad4() { return loadGroup4; }
    public String[][] secondaryIndex3() { return secondaryGroups3; }
    public String[][] secondaryIndex4() { return secondaryGroups4; }
}