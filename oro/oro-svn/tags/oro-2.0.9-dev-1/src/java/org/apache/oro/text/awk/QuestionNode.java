/* 
 * $Id: QuestionNode.java 54450 2004-02-13 22:02:00Z dfs $
 *
 * Copyright 2000-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oro.text.awk;


/**
 * @version @version@
 * @since 1.0
 */
final class QuestionNode extends OrNode {
  final static SyntaxNode _epsilon = new EpsilonNode();

  QuestionNode(SyntaxNode child){
    super(child, _epsilon);
  }

  boolean _nullable() { return true; }

  SyntaxNode _clone(int pos[]) {
    return new QuestionNode(_left._clone(pos));
  }
}
