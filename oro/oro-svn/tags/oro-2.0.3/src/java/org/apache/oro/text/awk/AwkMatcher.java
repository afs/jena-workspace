package org.apache.oro.text.awk;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation", "Jakarta-Oro" 
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache" 
 *    or "Jakarta-Oro", nor may "Apache" or "Jakarta-Oro" appear in their 
 *    name, without prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon software originally written 
 * by Daniel F. Savarese. We appreciate his contributions.
 */

/*
 * $Id: AwkMatcher.java 54340 2001-05-20 23:55:26Z dfs $
 */
import java.io.*;

import org.apache.oro.text.regex.*;

/**
 * The AwkMatcher class is used to match regular expressions
 * (conforming to the Awk regular expression syntax) generated by
 * AwkCompiler.  AwkMatcher only supports 8-bit ASCII.  Any attempt
 * to match Unicode values greater than 255 will result in undefined
 * behavior.  AwkMatcher finds true leftmost-longest matches, so
 * you must take care with how you formulate your regular expression
 * to avoid matching more than you really want.
 * <p>
 * It is important for you to remember that AwkMatcher does not save
 * parenthesized sub-group information.  Therefore the number of groups
 * saved in a MatchResult produced by AwkMatcher will always be 1.
 *
 * @author <a href="mailto:oro-dev@jakarta.apache.org">Daniel F. Savarese</a>
 * @version @version@
 * @since 1.0
 * @see org.apache.oro.text.regex.PatternMatcher
 * @see AwkCompiler
 */
public final class AwkMatcher implements PatternMatcher {
  private int __lastMatchedBufferOffset;
  private AwkMatchResult __lastMatchResult = null;
  private AwkStreamInput __scratchBuffer, __streamSearchBuffer;
  private AwkPattern __awkPattern;
  private int __offsets[] = new int[2];

  public AwkMatcher() {
    __scratchBuffer = new AwkStreamInput();
    __scratchBuffer._endOfStreamReached = true;
  }

  /**
   * Determines if a prefix of a string (represented as a char[])
   * matches a given pattern, starting from a given offset into the string.
   * If a prefix of the string matches the pattern, a MatchResult instance
   * representing the match is made accesible via
   * {@link #getMatch()}.
   * <p>
   * This method is useful for certain common token identification tasks
   * that are made more difficult without this functionality.
   * <p>
   * @param input  The char[] to test for a prefix match.
   * @param pattern  The Pattern to be matched.
   * @param offset The offset at which to start searching for the prefix.
   * @return True if input matches pattern, false otherwise.
   */
  // I reimplemented this method in terms of streammatchesPrefix
  // to reduce the code size.  This is not very elegant and
  // reduces performance by a small degree.
  public boolean matchesPrefix(char[] input, Pattern pattern, int offset){
    int result = -1;

    __awkPattern = (AwkPattern)pattern;

    __scratchBuffer._buffer       = input;
    __scratchBuffer._bufferSize   = input.length;
    __scratchBuffer._bufferOffset = 0;
    __scratchBuffer._endOfStreamReached = true;
    __streamSearchBuffer = __scratchBuffer;
    __offsets[0] = offset;
    try {
      result = __streamMatchPrefix();
    } catch(IOException e){
      // Don't do anything because we're not doing any I/O
      result = -1;
    }

    if(result < 0) {
      __lastMatchResult = null;
      return false;
    }

    __lastMatchResult =
      new AwkMatchResult(new String(input, 0, result), offset);

    return true;
  }


  /**
   * Determines if a prefix of a string (represented as a char[])
   * matches a given pattern.
   * If a prefix of the string matches the pattern, a MatchResult instance
   * representing the match is made accesible via
   * {@link #getMatch()}.
   * <p>
   * This method is useful for certain common token identification tasks
   * that are made more difficult without this functionality.
   * <p>
   * @param input  The char[] to test for a prefix match.
   * @param pattern  The Pattern to be matched.
   * @return True if input matches pattern, false otherwise.
   */
  public boolean matchesPrefix(char[] input, Pattern pattern){
    return matchesPrefix(input, pattern, 0);
  }


  /**
   * Determines if a prefix of a string matches a given pattern.
   * If a prefix of the string matches the pattern, a MatchResult instance
   * representing the match is made accesible via
   * {@link #getMatch()}.
   * <p>
   * This method is useful for certain common token identification tasks
   * that are made more difficult without this functionality.
   * <p>
   * @param input  The String to test for a prefix match.
   * @param pattern  The Pattern to be matched.
   * @return True if input matches pattern, false otherwise.
   */
  public boolean matchesPrefix(String input, Pattern pattern) {
    return matchesPrefix(input.toCharArray(), pattern, 0);
  }


  /**
   * Determines if a prefix of a PatternMatcherInput instance
   * matches a given pattern.  If there is a match, a MatchResult instance
   * representing the match is made accesible via
   * {@link #getMatch()}.  Unlike the
   * {@link #contains(PatternMatcherInput, Pattern)}
   * method, the current offset of the PatternMatcherInput argument
   * is not updated.  You should remember that the region starting
   * from the begin offset of the PatternMatcherInput will be
   * tested for a prefix match.
   * <p>
   * This method is useful for certain common token identification tasks
   * that are made more difficult without this functionality.
   * <p>
   * @param input  The PatternMatcherInput to test for a prefix match.
   * @param pattern  The Pattern to be matched.
   * @return True if input matches pattern, false otherwise.
   */
  public boolean matchesPrefix(PatternMatcherInput input, Pattern pattern){
    int result = -1;

    __awkPattern = (AwkPattern)pattern;
    __scratchBuffer._buffer       = input.getBuffer();
    __scratchBuffer._bufferOffset = input.getBeginOffset();
    __offsets[0] = input.getCurrentOffset();

    __scratchBuffer._bufferSize   = input.length();
    __scratchBuffer._endOfStreamReached = true;
    __streamSearchBuffer = __scratchBuffer;
    try {
      result = __streamMatchPrefix();
    } catch(IOException e) {
      // Don't do anything because we're not doing any I/O
      result = -1;
    }

    if(result < 0) {
      __lastMatchResult = null;
      return false;
    }

    __lastMatchResult =
      new AwkMatchResult(new String(__scratchBuffer._buffer, __offsets[0],
				    result), __offsets[0]);

    return true;
  }



  /**
   * Determines if a string (represented as a char[]) exactly 
   * matches a given pattern.  If
   * there is an exact match, a MatchResult instance
   * representing the match is made accesible via
   * {@link #getMatch()}.  The pattern must be
   * an AwkPattern instance, otherwise a ClassCastException will
   * be thrown.  You are not required to, and indeed should NOT try to
   * (for performance reasons), catch a ClassCastException because it
   * will never be thrown as long as you use an AwkPattern as the pattern
   * parameter.
   * <p>
   * @param input  The char[] to test for an exact match.
   * @param pattern  The AwkPattern to be matched.
   * @return True if input matches pattern, false otherwise.
   * @exception ClassCastException If a Pattern instance other than an
   *         AwkPattern is passed as the pattern parameter.
   */
  public boolean matches(char[] input, Pattern pattern) {
    int result = -1;

    __awkPattern = (AwkPattern)pattern;
    __scratchBuffer._buffer       = input;
    __scratchBuffer._bufferSize   = input.length;
    __scratchBuffer._bufferOffset = 0;
    __scratchBuffer._endOfStreamReached = true;
    __streamSearchBuffer = __scratchBuffer;
    __offsets[0] = 0;
    try {
      result = __streamMatchPrefix();
    } catch(IOException e){
      // Don't do anything because we're not doing any I/O
      result = -1;
    }

    if(result != input.length) {
      __lastMatchResult = null;
      return false;
    }

    __lastMatchResult =
      new AwkMatchResult(new String(input, 0, result), 0);

    return true;
  }




  /**
   * Determines if a string exactly matches a given pattern.  If
   * there is an exact match, a MatchResult instance
   * representing the match is made accesible via
   * {@link #getMatch()}.  The pattern must be
   * a AwkPattern instance, otherwise a ClassCastException will
   * be thrown.  You are not required to, and indeed should NOT try to
   * (for performance reasons), catch a ClassCastException because it
   * will never be thrown as long as you use an AwkPattern as the pattern
   * parameter.
   * <p>
   * @param input  The String to test for an exact match.
   * @param pattern  The AwkPattern to be matched.
   * @return True if input matches pattern, false otherwise.
   * @exception ClassCastException If a Pattern instance other than an
   *         AwkPattern is passed as the pattern parameter.
   */
  public boolean matches(String input, Pattern pattern){
    return matches(input.toCharArray(), pattern);
  }


  /**
   * Determines if the contents of a PatternMatcherInput instance
   * exactly matches a given pattern.  If
   * there is an exact match, a MatchResult instance
   * representing the match is made accesible via
   * {@link #getMatch()}.  Unlike the
   * {@link #contains(PatternMatcherInput, Pattern)}
   * method, the current offset of the PatternMatcherInput argument
   * is not updated.  You should remember that the region between
   * the begin and end offsets of the PatternMatcherInput will be
   * tested for an exact match.
   * <p>
   * The pattern must be an AwkPattern instance, otherwise a
   * ClassCastException will be thrown.  You are not required to, and 
   * indeed should NOT try to (for performance reasons), catch a
   * ClassCastException because it will never be thrown as long as you use
   * an AwkPattern as the pattern parameter.
   * <p>
   * @param input  The PatternMatcherInput to test for a match.
   * @param pattern  The AwkPattern to be matched.
   * @return True if input matches pattern, false otherwise.
   * @exception ClassCastException If a Pattern instance other than an
   *         AwkPattern is passed as the pattern parameter.
   */
  public boolean matches(PatternMatcherInput input, Pattern pattern){
    int result = -1;

    __awkPattern = (AwkPattern)pattern;
    __scratchBuffer._buffer       = input.getBuffer();
    __scratchBuffer._bufferSize   = input.length();
    __scratchBuffer._bufferOffset = input.getBeginOffset();
    __offsets[0] = input.getBeginOffset();
    __scratchBuffer._endOfStreamReached = true;
    __streamSearchBuffer = __scratchBuffer;
    try {
      result = __streamMatchPrefix();
    } catch(IOException e){
      // Don't do anything because we're not doing any I/O
      result = -1;
    }

    if(result != __scratchBuffer._bufferSize) {
      __lastMatchResult = null;
      return false;
    }

    __lastMatchResult =
      new AwkMatchResult(new String(__scratchBuffer._buffer, __offsets[0],
			     __scratchBuffer._bufferSize), __offsets[0]);

    return true;
  }



  /**
   * Determines if a string (represented as a char[]) contains a pattern.
   * If the pattern is
   * matched by some substring of the input, a MatchResult instance
   * representing the <b> first </b> such match is made acessible via 
   * {@link #getMatch()}.  If you want to access
   * subsequent matches you should either use a PatternMatcherInput object
   * or use the offset information in the MatchResult to create a substring
   * representing the remaining input.  Using the MatchResult offset 
   * information is the recommended method of obtaining the parts of the
   * string preceeding the match and following the match.
   * <p>
   * The pattern must be an AwkPattern instance, otherwise a
   * ClassCastException will be thrown.  You are not required to, and 
   * indeed should NOT try to (for performance reasons), catch a
   * ClassCastException because it will never be thrown as long as you use
   * an AwkPattern as the pattern parameter.
   * <p>
   * @param input  The char[] to test for a match.
   * @param pattern  The AwkPattern to be matched.
   * @return True if the input contains a pattern match, false otherwise.
   * @exception ClassCastException If a Pattern instance other than an
   *         AwkPattern is passed as the pattern parameter.
   */
  public boolean contains(char[] input, Pattern pattern) {
    __awkPattern = (AwkPattern)pattern;

    // Begin anchor requires match occur at beginning of input
    if(__awkPattern._hasBeginAnchor && !__awkPattern._fastMap[input[0]]){
      __lastMatchResult = null;
      return false;
    }

    __scratchBuffer._buffer       = input;
    __scratchBuffer._bufferSize   = input.length;
    __scratchBuffer._bufferOffset = 0;
    __scratchBuffer._endOfStreamReached = true;
    __streamSearchBuffer = __scratchBuffer;
    __lastMatchedBufferOffset = 0;
    try {
      _search();
    } catch(IOException e) {
      // do nothing
    }
    return (__lastMatchResult != null);
  }


  /**
   * Determines if a string contains a pattern.  If the pattern is
   * matched by some substring of the input, a MatchResult instance
   * representing the <b> first </b> such match is made acessible via 
   * {@link #getMatch()}.  If you want to access
   * subsequent matches you should either use a PatternMatcherInput object
   * or use the offset information in the MatchResult to create a substring
   * representing the remaining input.  Using the MatchResult offset 
   * information is the recommended method of obtaining the parts of the
   * string preceeding the match and following the match.
   * <p>
   * The pattern must be an AwkPattern instance, otherwise a
   * ClassCastException will be thrown.  You are not required to, and 
   * indeed should NOT try to (for performance reasons), catch a
   * ClassCastException because it will never be thrown as long as you use
   * an AwkPattern as the pattern parameter.
   * <p>
   * @param input  The String to test for a match.
   * @param pattern  The AwkPattern to be matched.
   * @return True if the input contains a pattern match, false otherwise.
   * @exception ClassCastException If a Pattern instance other than an
   *         AwkPattern is passed as the pattern parameter.
   */
  public boolean contains(String input, Pattern pattern){
    return contains(input.toCharArray(), pattern);
  }



  /**
   * Determines if the contents of a PatternMatcherInput, starting from the
   * current offset of the input contains a pattern.
   * If a pattern match is found, a MatchResult
   * instance representing the <b>first</b> such match is made acessible via 
   * {@link #getMatch()}.  The current offset of the
   * PatternMatcherInput is set to the offset corresponding to the end
   * of the match, so that a subsequent call to this method will continue
   * searching where the last call left off.  You should remember that the
   * region between the begin and end offsets of the PatternMatcherInput are
   * considered the input to be searched, and that the current offset
   * of the PatternMatcherInput reflects where a search will start from.
   * Matches extending beyond the end offset of the PatternMatcherInput
   * will not be matched.  In other words, a match must occur entirely
   * between the begin and end offsets of the input.  See
   * {@link org.apache.oro.text.regex.PatternMatcherInput PatternMatcherInput}
   * for more details.
   * <p>
   * As a side effect, if a match is found, the PatternMatcherInput match
   * offset information is updated.  See the PatternMatcherInput
   * {@link org.apache.oro.text.regex.PatternMatcherInput#setMatchOffsets
   * setMatchOffsets(int, int)} method for more details.                  
   * <p>
   * The pattern must be an AwkPattern instance, otherwise a
   * ClassCastException will be thrown.  You are not required to, and 
   * indeed should NOT try to (for performance reasons), catch a
   * ClassCastException because it will never be thrown as long as you use
   * an AwkPattern as the pattern parameter.
   * <p>
   * This method is usually used in a loop as follows:
   * <blockquote><pre>
   * PatternMatcher matcher;
   * PatternCompiler compiler;
   * Pattern pattern;
   * PatternMatcherInput input;
   * MatchResult result;
   *
   * compiler = new AwkCompiler();
   * matcher  = new AwkMatcher();
   *
   * try {
   *   pattern = compiler.compile(somePatternString);
   * } catch(MalformedPatternException e) {
   *   System.err.println("Bad pattern.");
   *   System.err.println(e.getMessage());
   *   return;
   * }
   *
   * input   = new PatternMatcherInput(someStringInput);
   *
   * while(matcher.contains(input, pattern)) {
   *   result = matcher.getMatch();  
   *   // Perform whatever processing on the result you want.
   * }
   *
   * </pre></blockquote>
   * <p>
   * @param input  The PatternMatcherInput to test for a match.
   * @param pattern  The Pattern to be matched.
   * @return True if the input contains a pattern match, false otherwise.
   * @exception ClassCastException If a Pattern instance other than an
   *         AwkPattern is passed as the pattern parameter.
   */
  public boolean contains(PatternMatcherInput input, Pattern pattern) {
    __awkPattern = (AwkPattern)pattern;
    __scratchBuffer._buffer       = input.getBuffer();
    __scratchBuffer._bufferOffset = input.getBeginOffset();
    __lastMatchedBufferOffset = input.getCurrentOffset();

    // Begin anchor requires match occur at beginning of input
    // No need to adjust current offset if no match found.
    if(__awkPattern._hasBeginAnchor) {
      int begin;

      begin = input.getBeginOffset();
      if(begin != __lastMatchedBufferOffset ||
	 !__awkPattern._fastMap[__scratchBuffer._buffer[begin]]) {
	__lastMatchResult = null;
	return false;
      }
    }

    __scratchBuffer._bufferSize   = input.length();
    __scratchBuffer._endOfStreamReached = true;
    __streamSearchBuffer = __scratchBuffer;
    try {
      _search();
    } catch(IOException e) {
      // do nothing
    }
    input.setCurrentOffset(__lastMatchedBufferOffset);

    if(__lastMatchResult == null)
      return false;

    input.setMatchOffsets(__lastMatchResult.beginOffset(0),
			  __lastMatchResult.endOffset(0));

    return true;
  }


  /**
   * Determines if the contents of an AwkStreamInput, starting from the
   * current offset of the input contains a pattern.
   * If a pattern match is found, a MatchResult
   * instance representing the <b>first</b> such match is made acessible via 
   * {@link #getMatch()}.  The current offset of the
   * input stream is advanced to the end offset corresponding to the end
   * of the match.  Consequently a subsequent call to this method will continue
   * searching where the last call left off.
   * See {@link AwkStreamInput} for more details.
   * <p>
   * Note, patterns matching the null string do NOT match at end of input
   * stream. This is different from the behavior you get from the other
   * contains() methods.
   * <p>
   * The pattern must be an AwkPattern instance, otherwise a
   * ClassCastException will be thrown.  You are not required to, and 
   * indeed should NOT try to (for performance reasons), catch a
   * ClassCastException because it will never be thrown as long as you use
   * an AwkPattern as the pattern parameter.
   * <p>
   * This method is usually used in a loop as follows:
   * <blockquote><pre>
   * PatternMatcher matcher;
   * PatternCompiler compiler;
   * Pattern pattern;
   * AwkStreamInput input;
   * MatchResult result;
   *
   * compiler = new AwkCompiler();
   * matcher  = new AwkMatcher();
   *
   * try {
   *   pattern = compiler.compile(somePatternString);
   * } catch(MalformedPatternException e) {
   *   System.err.println("Bad pattern.");
   *   System.err.println(e.getMessage());
   *   return;
   * }
   *
   * input   = new AwkStreamInput(
   *             new BufferedInputStream(new FileInputStream(someFileName)));
   *
   * while(matcher.contains(input, pattern)) {
   *   result = matcher.getMatch();  
   *   // Perform whatever processing on the result you want.
   * }
   *
   * </pre></blockquote>
   * <p>
   * @param input  The PatternStreamInput to test for a match.
   * @param pattern  The Pattern to be matched.
   * @return True if the input contains a pattern match, false otherwise.
   * @exception ClassCastException If a Pattern instance other than an
   *         AwkPattern is passed as the pattern parameter.
   */
  public boolean contains(AwkStreamInput input, Pattern pattern)
       throws IOException
  {
    __awkPattern = (AwkPattern)pattern;

    // Begin anchor requires match occur at beginning of input
    if(__awkPattern._hasBeginAnchor) {
      // Do read here instead of in _search() so we can test first char
      if(input._bufferOffset == 0) {
	if(input.read() && !__awkPattern._fastMap[input._buffer[0]]) {
	  __lastMatchResult = null;
	  return false;
	}
      } else {
	__lastMatchResult = null;
	return false;
      }
    }

    __lastMatchedBufferOffset = input._currentOffset;
    __streamSearchBuffer = input;
    _search();
    input._currentOffset = __lastMatchedBufferOffset;
    return (__lastMatchResult != null);
  }


  private int __streamMatchPrefix() throws IOException {
    int token, current = AwkPattern._START_STATE, lastState, transition;
    int offset, initialOffset;
    int lastMatchedOffset = -1;
    int[] tstateArray;

    offset = initialOffset = __offsets[0];
  test:
    while(offset < __streamSearchBuffer._bufferSize) {
      token = __streamSearchBuffer._buffer[offset++];

      if(current < __awkPattern._numStates) {
	lastState = current;
	tstateArray = __awkPattern._getStateArray(current);
	current = tstateArray[token];

	if(current == 0){
	  __awkPattern._createNewState(lastState, token, tstateArray);
	  current = tstateArray[token];
	}
	if(current == AwkPattern._INVALID_STATE){
	  break test;
	}
	else if(__awkPattern._endStates.get(current)){
	  lastMatchedOffset = offset;
	}
	if(offset == __streamSearchBuffer._bufferSize){
	  offset = __streamSearchBuffer._reallocate(initialOffset);

	  // If we're at the end of the stream, don't reset values
	  if(offset != __streamSearchBuffer._bufferSize){
	    if(lastMatchedOffset != -1)
	      lastMatchedOffset-=initialOffset;
	    initialOffset = 0;
	  }

	}
      }
      else
	break;
    }

    __offsets[0] = initialOffset;
    __offsets[1] = lastMatchedOffset - 1;

    if(lastMatchedOffset == -1 && __awkPattern._matchesNullString)
      return 0;

    // End anchor requires match occur at end of input
    if(__awkPattern._hasEndAnchor &&
       (!__streamSearchBuffer._endOfStreamReached ||
	lastMatchedOffset < __streamSearchBuffer._bufferSize))
      return -1;

    return (lastMatchedOffset - initialOffset);
  }



   void _search() throws IOException {
    char[] currentLine;
    int position, tokensMatched;
    String currentString;

    __lastMatchResult = null;

    while(true){
      if(__lastMatchedBufferOffset >= __streamSearchBuffer._bufferSize){
	if(__streamSearchBuffer._endOfStreamReached){
	  // Get rid of reference now that it should no longer be used.
	  __streamSearchBuffer = null;
	  return;
	} else {
	  if(!__streamSearchBuffer.read())
	    return;
	  __lastMatchedBufferOffset = 0;
	}
      }

      for(position=__lastMatchedBufferOffset;
	  position < __streamSearchBuffer._bufferSize;
	  position = __offsets[0] + 1) {

	__offsets[0] = position;
	if(__awkPattern._fastMap[__streamSearchBuffer._buffer[position]] &&
	   (tokensMatched = __streamMatchPrefix()) > -1) {

	  __lastMatchResult  = new AwkMatchResult(
	  new String(__streamSearchBuffer._buffer, __offsets[0],
		     tokensMatched),
	  __offsets[0] + __streamSearchBuffer._bufferOffset);

	  __lastMatchedBufferOffset =
	    (tokensMatched > 0 ? __offsets[1] + 1 : __offsets[0] + 1);

	  return;
	} else if(__awkPattern._matchesNullString) {
	  __lastMatchResult  = new AwkMatchResult(new String(),
			  position + __streamSearchBuffer._bufferOffset);

	  __lastMatchedBufferOffset = position + 1;

	  return;
	}
      }

      __lastMatchedBufferOffset = position;
    }
  }


  /**
   * Fetches the last match found by a call to a matches() or contains()
   * method.
   * <p>
   * @return A MatchResult instance containing the pattern match found
   *         by the last call to any one of the matches() or contains()
   *         methods.  If no match was found by the last call, returns
   *         null.
   */
  public MatchResult getMatch() { return __lastMatchResult; }

}