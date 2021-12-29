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

package dev;

import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.SysRIOT;
import org.apache.logging.log4j.Logger;

public class ErrorHandlerBuilder {

    interface Action { void action(String message, long line, long col) ; }

    // Warning
    Action warningAction = null;
    Action errorAction = null;
    Action fatalAction = null;

    // Error
    // false


    public ErrorHandlerBuilder() {

    }

    public ErrorHandlerBuilder warnLog(Logger log) {
        warningAction = (message, line, col)-> {
            String msg = SysRIOT.fmtMessage(message, line, col);
            log.warn(msg);
        };
        return this;
    }

    public ErrorHandlerBuilder warnException() {
        warningAction = (message, line, col)-> {
            String msg = SysRIOT.fmtMessage(message, line, col);
            throw new RiotException(msg);
        };
        return this;
    }

    public ErrorHandlerBuilder warnParseException() {
        warningAction = (message, line, col)-> {
            throw new RiotParseException(message, line, line);
        };
        return this;
    }
}
