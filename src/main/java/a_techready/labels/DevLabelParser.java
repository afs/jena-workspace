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

public class DevLabelParser {

    // PrefixName parser.

    // % in local name.
    // Via \% -- no - post parse. Variant needed.
    // Via %AB

    // Code tidy
    // Code review
    //   Policy on timing of idx++


//  static {
//      LogCtl.setLog4j2();
//      RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
//  }

  public static void main(String...a) {

      onePrefixName("a:b", true);
      onePrefixName("a:b:::", true);
      onePrefixName("a:b\\?", true);
      onePrefixName("a:b   ", false);
      onePrefixName(":b", true);
      onePrefixName("a:", true);
      onePrefixName(":", true);
      onePrefixName("a:xx.", false);
      onePrefixName("a:.", false);

      oneBlankNodeLabel("_:a", true);
      oneBlankNodeLabel("_:1", true);
      oneBlankNodeLabel("a", false);
      oneBlankNodeLabel("", false);
      oneBlankNodeLabel("_:", false);
      oneBlankNodeLabel("_:xx.", false);
      System.out.println("DONE");
  }

  private static void onePrefixName(String string, boolean good) {
      boolean b = LabelParser.checkPrefixName(string);
      if ( b != good ) {
          if ( b )
              System.out.printf("**** Prefix : Good: [%s]\n", string);
          else
              System.out.printf("**** Prefix : Bad:  [%s]\n", string);
      }
  }

  private static void oneBlankNodeLabel(String string, boolean good) {
      boolean b = LabelParser.checkBlankNodeLabel(string);
      if ( b != good ) {
          if ( b )
              System.out.printf("**** BNode : Good: [%s]\n", string);
          else
              System.out.printf("**** BNode : Bad:  [%s]\n", string);
      }
  }
}
