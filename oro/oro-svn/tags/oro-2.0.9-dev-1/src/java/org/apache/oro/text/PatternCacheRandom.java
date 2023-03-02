/* 
 * $Id: PatternCacheRandom.java 54450 2004-02-13 22:02:00Z dfs $
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


package org.apache.oro.text;

import java.util.*;

import org.apache.oro.text.regex.*;
import org.apache.oro.util.*;

/**
 * This class is a GenericPatternCache subclass implementing a random
 * cache replacement policy.  In other words,
 * patterns are added to the cache until it becomes full.  Once the
 * cache is full, when a new pattern is added to the cache, it replaces
 * a randomly selected pattern in the cache.
 *
 * @version @version@
 * @since 1.0
 * @see GenericPatternCache
 */
public final class PatternCacheRandom extends GenericPatternCache {

  /**
   * Creates a PatternCacheRandom instance with a given cache capacity
   * and initialized to use a given PatternCompiler instance as a pattern
   * compiler.
   * <p>
   * @param capacity  The capacity of the cache.
   * @param compiler  The PatternCompiler to use to compile patterns.
   */
  public PatternCacheRandom(int capacity, PatternCompiler compiler) {
    super(new CacheRandom(capacity), compiler);
  }

  /**
   * Same as:
   * <blockquote><pre>
   * PatternCacheRandom(GenericPatternCache.DEFAULT_CAPACITY, compiler);
   * </pre></blockquote>
   */
  public PatternCacheRandom(PatternCompiler compiler) {
    this(GenericPatternCache.DEFAULT_CAPACITY, compiler);
  }

  /**
   * Same as:
   * <blockquote><pre>
   * PatternCacheRandom(capacity, new Perl5Compiler());
   * </pre></blockquote>
   */
  public PatternCacheRandom(int capacity) {
    this(capacity, new Perl5Compiler());
  }

  /**
   * Same as:
   * <blockquote><pre>
   * PatternCacheRandom(GenericPatternCache.DEFAULT_CAPACITY);
   * </pre></blockquote>
   */
  public PatternCacheRandom() {
    this(GenericPatternCache.DEFAULT_CAPACITY);
  }

}



