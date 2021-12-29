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

public class TestParserPrefixedName {
    @Test public void prefixed_01() { prefixedName("a:b", true);    }
    @Test public void prefixed_02() { prefixedName("a.b", false);   }
    @Test public void prefixed_03() { prefixedName("a:",  true);    }
    @Test public void prefixed_04() { prefixedName(":a",  true);    }
    @Test public void prefixed_05() { prefixedName("aa:", true);    }
    @Test public void prefixed_06() { prefixedName(":aa", true);    }
    @Test public void prefixed_07() { prefixedName(":", true);      }

    @Test public void prefix_01() { prefixedName("-a:b", false);    }
    @Test public void prefix_02() { prefixedName("-a:b", false);    }
    @Test public void prefix_03() { prefixedName("1a:b", false);    }
    @Test public void prefix_04() { prefixedName("1:b", false);     }
    @Test public void prefix_05() { prefixedName("a1:b", true);     }
    @Test public void prefix_06() { prefixedName("a.z:b", true);    }
    @Test public void prefix_07() { prefixedName("a-z:b", true);    }
    @Test public void prefix_08() { prefixedName("a?:b", false);    }

    @Test public void local_01() { prefixedName("a:1b", true);      }
    @Test public void local_02() { prefixedName("a.:b", false);     }
    @Test public void local_03() { prefixedName("a:b1", true);      }
    @Test public void local_04() { prefixedName("a:b:::", true);    }

    // "Post parse" -- no \-escapes
    @Test public void local_05() { prefixedName("a:b\\?", false);   }
    @Test public void local_06() { prefixedName("a:b\\?z", false);  }
    @Test public void local_07() { prefixedName("a:\\?z", false);   }
    // Post parse - characters that needed an escape in the parser.
    @Test public void local_08() { prefixedName("a:b?", true);      }
    @Test public void local_09() { prefixedName("a:b$z", true);     }
    @Test public void local_10() { prefixedName("a:*z", true);      }

    @Test public void bad_chars_01() { prefixedName("a:b ", false); }
    @Test public void bad_chars_02() { prefixedName(" a:b", false); }

    @Test public void dots_01() { prefixedName("a:xx.", false);     }
    @Test public void dots_02() { prefixedName("a:.", false);       }
    @Test public void dots_03() { prefixedName("a:.a", true);       }

    @Test public void percent_20() { prefixedName("a:%AB", true);   }
    @Test public void percent_21() { prefixedName("a:x%AB", true);  }
    @Test public void percent_22() { prefixedName("a:%ABz", true);  }
    @Test public void percent_23() { prefixedName("a%AB:z", false); }
    @Test public void percent_24() { prefixedName("a:z%B", false);  }
    @Test public void percent_25() { prefixedName("a:%Bz", false);  }
    @Test public void percent_26() { prefixedName("a:%B", false);   }

    private static void prefixedName(String string, boolean expected) {
        boolean b1 = LabelParser.checkPrefixName(string);

//        Token tok = TokenizerText.fromString(string).next();
//        if ( tok.hasType(TokenType.PREFIXED_NAME) ) {
//            String s1 = tok.getImage();
//            String s2 = tok.getImage2();
//            String pname = s1+":"+s2;
//        }

        if ( b1 != expected ) {
            if ( expected )
                fail("Did not pass: ["+string+"]");
            else
                fail("Did not fail: ["+string+"]");
        }
    }
}

