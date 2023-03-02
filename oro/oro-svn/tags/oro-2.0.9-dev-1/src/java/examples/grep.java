/* 
 * $Id: grep.java 54450 2004-02-13 22:02:00Z dfs $
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


package examples;

import java.io.*;
import java.util.*;

import org.apache.oro.text.*;
import org.apache.oro.text.regex.*;

/**
 * This is a no-frills implementation of grep using Perl regular expressions.
 * You can easily add most of the options present in most grep versions by
 * creating a MatchAction class or classes whose behavior varies based on
 * the provided flags.
 *
 * @version @version@
 */
public final class grep {
  static int _file = 0;

  // args[] is declared final so that Inner Class may reference it.
  public static final void main(final String[] args) {
    MatchActionProcessor processor = new MatchActionProcessor();

    if(args.length < 2) {
      System.err.println("Usage: grep <pattern> <filename>");
      System.exit(1);
    }

    try {
      if(args.length > 2) {
	// Print filename before line if more than one file is specified.
	// Rely on _file to point to current file being processed.
	processor.addAction(args[0], new MatchAction() {
	  public void processMatch(MatchActionInfo info) {
	    info.output.println(args[_file] + ":" + info.line);
	  }
	});
      } else {
	// We rely on the default action of printing matched 
	// lines to the given OutputStream
	processor.addAction(args[0]);
      }
    } catch(MalformedPatternException e) {
      System.err.println("Bad pattern.");
      e.printStackTrace();
      System.exit(1);
    }

    for(_file = 1; _file < args.length; _file++) {
      try {
	processor.processMatches(new FileInputStream(args[_file]), System.out);
      } catch(IOException e) {
	System.err.println("Error opening or reading " + args[_file]);
	e.printStackTrace();
	System.exit(1);
      }
    }
  }

}
