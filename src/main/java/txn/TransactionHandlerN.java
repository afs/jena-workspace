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

package txn;

import java.util.*;

import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.impl.TransactionHandlerBase;

public class TransactionHandlerN extends TransactionHandlerBase {

    // Entry order
    private List<TransactionHandler> handlers;
    // Exit order.
    private List<TransactionHandler> handlersReverse = new ArrayList<>();

    public TransactionHandlerN(List<TransactionHandler> others) {
        handlers = others;
        handlersReverse.addAll(handlers);
        Collections.reverse(handlers);
    }

    @Override
    public boolean transactionsSupported() {
        return true;
    }

    @Override
    public void begin() {
        handlers.forEach(TransactionHandler::begin);
    }

    @Override
    public void abort() {
        handlersReverse.forEach(TransactionHandler::abort);
    }

    @Override
    public void commit() {
        handlersReverse.forEach(TransactionHandler::commit);
    }
}
