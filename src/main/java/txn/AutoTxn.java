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

import java.util.function.Supplier;

import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.system.Txn;

/**
 * Execute inside the current transaction, or wrap in a transaction. This is
 * "autocommit" support.
 * <p>
 * This library simply executes inside the current transaction if there is one
 * otherwise it runs the action in a new transaction.
 * It is expected that there is normally a transaction.
 * Creating and running a transaction for each action may be expensive
 * if the action is small. This library does not promote transactions.
 * <p>
 * {@link Txn} provides an application API for executing actions in transactions.
 * It is normally used when there is no transaction already running.
 * {@link Txn} should be used by code libraries and applications wishing to execute a transaction.
 * <p>
 * Example: Ensure {@code DatasetGraph.add(Quad)} is transactional:
 * <pre>
 *   {@literal @}Override
 *   public void add(Quad quad) {
 *      AutoTxn.execWrite(this, this::addInternal);
 *   }
 *
 *   private void addInternal(Quad quad) {
 *       ... do the real work, knowing we are inside a transaction ...
 *   }
 * <pre>
 *
 * @see Txn
 */

public class AutoTxn {

    // Replacement in Txn:
    /**
     * Application utilities for executing code in transactions. It is normally used
     * when there is no transaction already running.
     * <p>
     * If there is already a transaction for the current thread, that transaction is
     * used; if the action is an update ({@code executeWrite} or
     * {@code calculateWrite}), then this library will promote an existing
     * transaction is possible and needed.
     * <p>
     * Nested transactions are not supported; it is continuing the current
     * transaction.
     * <p>
     * Exceptions thrown in the action cause the transaction to be aborted.
     * {@link AutoTxn} provides support for "autocommit" - wrap the action in a
     * transaction if necessary but no other support. It primarily for systenm
     * classes needing to be sure a transaction is used, and the code would normally
     * be expected to run in an existing transaction started by the application.
     *
     * @see AutoTxn
     */

    /** Execute in a read transaction */
    public static <T extends Transactional> void execRead(T txn, Runnable action) {
        exec(txn, TxnType.READ, action);
    }

    /** Execute and return a value in a read transaction */
    public static <T extends Transactional, X> X calcRead(T txn, Supplier<X> action) {
        return calc(txn, TxnType.READ, action);
    }

    /** Execute the Runnable in a write transaction */
    public static <T extends Transactional> void execWrite(T txn, Runnable action) {
        exec(txn, TxnType.WRITE, action);
    }

    /** Execute and return a value in a write transaction. */
    public static <T extends Transactional, X> X calcWrite(T txn, Supplier<X> action) {
        return calc(txn, TxnType.WRITE, action);
    }

    /** Execute in a transaction with the given {@link TxnType transaction type}. */
    public static <T extends Transactional> void exec(T txn, TxnType txnType, Runnable action) {
        if ( txn.isInTransaction() ) {
            // Direct
            action.run();
            return;
        }
        onStart(txn, txnType);
        try { action.run(); }
        catch (Throwable th) {
            onThrowable(th, txn);
            throw th;
        }
        onFinish(txn);
    }

    /** Execute and return a value in a transaction with the given {@link TxnType transaction type}. */
    public static <T extends Transactional, X> X calc(T txn, TxnType txnType, Supplier<X> action) {
        if ( txn.isInTransaction() )
            // Direct
            return action.get() ;
        onStart(txn, txnType);
        X x;
        try { x = action.get() ; }
        catch (Throwable th) {
            onThrowable(th, txn);
            throw th ;
        }
        onFinish(txn);
        return x;
    }

    private static <T extends Transactional, X> void onStart(T txn, TxnType txnType) {
        txn.begin(txnType) ;
    }

    // Attempt some kind of cleanup.
    private static <T extends Transactional> void onThrowable(Throwable th, T txn) {
        try {
            txn.abort() ;
            txn.end() ;
        } catch (Throwable th2) { th.addSuppressed(th2); }
    }

    // Attempt some kind of cleanup.
    private static <T extends Transactional> void onFinish(T txn) {
        txn.commit() ;
        txn.end() ;
    }

}
