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

package test4.suite.riot;

import org.junit.runner.RunWith;
import test4.next.manifest.Label;
import test4.next.manifest.Manifests;
import test4.next.runners.RunnerRIOT;

@RunWith(RunnerRIOT.class)
@Label("RIOT")
@Manifests({
    "/home/afs/ASF/afs-jena/jena-arq/testing/RIOT/Lang/manifest-all.ttl",
    "/home/afs/Jena/jena-arq/testing/ARQ/RDF-Star/Turtle-Star/manifest.ttl"
})

public class TS_RIOT_Next {
}

