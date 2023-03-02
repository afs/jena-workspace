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

package uuid.newuuid;

import java.util.UUID;

public class DevUUID {
    public static void main(String[] args) {


        UUID nilUUID = new UUID(0, 0);
        UUID maxUUID = new UUID(-1L, -1L);

        UUIDGenerator generator = FactoryUUID.threadGeneratorUUID7();
        //UUIDGenerator generator = FactoryUUID.globalGeneratorUUID7();

//        // Example of a V7 UUID from the draft standard.
//        UUID uuid1 = UUID.fromString("017F22E2-79B0-7CC3-98C4-DC0C0C07398F");

        for ( int i = 0 ; i < 1 ; i++ ) {
            UUID uuid = generator.generate();
            if ( uuid.version() != 7 )
                System.out.println("BAD: Version: "+uuid.version());
            if ( uuid.variant() != 2 )
                System.out.println("BAD: Variant: "+uuid.variant());
            System.out.println(LibUUID.str(uuid));
        }
//        System.out.println("DONE");
    }

}
