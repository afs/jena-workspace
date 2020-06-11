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

package uuid.jena_original;

/**
 * The nil UUID. There is only one in the system.
 */

public final
class UUID_nil extends JenaUUID
{
    private static final String nilStr = "00000000-0000-0000-0000-000000000000";
    private static UUID_nil nil = new UUID_nil();

    private static final int version = 0;

    private UUID_nil() { super(0, 0); }

    @Override
    public String toString() {
        return nilStr;
    }

    public static UUID_nil getNil() { return nil; }

    public static String   getNilString() { return nilStr; }

    @Override
    protected int getImplVersion() {
        return version;
    }
}
