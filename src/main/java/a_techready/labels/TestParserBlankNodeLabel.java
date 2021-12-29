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

package a_techready.labels;

import static org.junit.Assert.fail;

import org.junit.Test;

public class TestParserBlankNodeLabel {
    @Test public void blankNodeLabel_01() { blankNodeLabel("_:a", true);      }

    @Test public void blankNodeLabel_02() { blankNodeLabel("_:1", true);      }

    @Test public void blankNodeLabel_03() { blankNodeLabel("a", false);       }

    @Test public void blankNodeLabel_04() { blankNodeLabel("", false);        }

    @Test public void blankNodeLabel_05() { blankNodeLabel("_:", false);      }

    @Test public void blankNodeLabel_06() { blankNodeLabel("_:xx.", false);   }

    @Test public void blankNodeLabel_07() { blankNodeLabel("_:x.x", true);    }

    @Test public void blankNodeLabel_08() { blankNodeLabel("_:x%AB", false);  }

    @Test public void blankNodeLabel_09() { blankNodeLabel("_:x:x", false);   }

    @Test public void blankNodeLabel_10() { blankNodeLabel("a:x", false);     }

    @Test public void blankNodeLabel_11() { blankNodeLabel("__:x", false);    }

    @Test public void blankNodeLabel_12() { blankNodeLabel("_:x?y", false);   }

    @Test public void blankNodeLabel_13() { blankNodeLabel("_:x.", false);    }

    @Test public void blankNodeLabel_14() { blankNodeLabel("_:xy.", false);   }

    @Test public void blankNodeLabel_15() { blankNodeLabel("_:.", false);     }

    @Test public void blankNodeLabel_16() { blankNodeLabel("_:.xy", false);   }

    @Test public void blankNodeLabel_17() { blankNodeLabel("_:x.y", true);    }

    private static void blankNodeLabel(String string, boolean expected) {
        boolean b = LabelParser.checkBlankNodeLabel(string);
        if ( b != expected ) {
            if ( expected )
                fail("Did not pass: ["+string+"]");
            else
                fail("Did not fail: ["+string+"]");
        }
    }
}

