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

package tdb2.txn;

/**
 * Control interface for a transactional system.
 */
public interface TransactionalSystemControl_2 {

    /**
     * Block write activity - let active potential writers finish while blocking new potential writers.
     * On exit, there are no active writers or promote transactions.
     * (Promotes would otherwise become write mode in their lifetime.)
     */
    public void startReadOnlyDatabase();

    /**
     * Release any waiting potential writers.
     */
    public void finishReadOnlyDatabase();

    /**
     * Execute with a read-only database.
     */
    public default void execReadOnlyDatabase(Runnable action)  {
        startReadOnlyDatabase();
        try {
            action.run();
        } finally { finishReadOnlyDatabase(); }
    }

    /** Block new writers, wait until current writers finish.
     *  When this returns, this guarantees that the database is not changing.
     *  It does not guarantee
     *  and the journal is flushed to disk.
     * <p>
     * The application must call {@link #enableWriters} later.
     * <p>
     * This operation must not be nested (it will block).
     *
     * @see #tryBlockWriters()
     * @see #enableWriters()
     */
    public void blockWriters();

    /** Try to block all writers, or return if can't at the moment.
     * <p>
     * If it returns true, the application must call {@link #enableWriters} later.
     *
     * @see #blockWriters()
     * @see #enableWriters()
     *
     * @return true if the operation succeeded and writers are blocked
     */
    public boolean tryBlockWriters();

    /**
     * Block until no writers are active, optionally blocking or returning if can't at the moment.
     * <p>
     * If it returns true, the application must call {@link #enableWriters} later.
     * @param canBlock
     * @return true if the operation succeeded and writers are blocked
     */
    public boolean tryBlockWriters(boolean canBlock);

    /** Allow writers.
     * This must be used in conjunction with {@link #blockWriters()} or {@link #tryBlockWriters()}
     *
     * @see #blockWriters()
     * @see #tryBlockWriters()
     */
    public void enableWriters();

    // -- Exclusivity

    /**
     * Enter non-exclusive mode; block if necessary.
     */
    public void startNonExclusiveMode();

    /**
     * Try to enter non-exclusive mode; return true if successful.
     */
    public default boolean tryNonExclusiveMode() { return tryNonExclusiveMode(false); }

    /**
     * Try to enter non-exclusive mode; return true if successful.
     */
    public boolean tryNonExclusiveMode(boolean canBlock);

    /**
     * End non-exclusive mode.
     */
    public void finishNonExclusiveMode();


    /**
     * Enter exclusive mode; block if necessary.
     * There are no active transactions on return; new transactions will be held up in 'begin'.
     * Return to normal (release waiting transactions, allow new transactions)
     * with {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     */
    public void startExclusiveMode();

    /** Try to enter exclusive mode.
     *  If return is true, then there are no active transactions on return and new transactions will be held up in 'begin'.
     *  If false, there were in-progress transactions.
     *  Return to normal (release waiting transactions, allow new transactions)
     *  with {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     */
    public default boolean tryExclusiveMode() { return tryExclusiveMode(false); }

    /** Try to enter exclusive mode.
     *  If return is true, then there are no active transactions on return and new transactions will be held up in 'begin'.
     *  If false, there were in-progress transactions.
     *  Return to normal (release waiting transactions, allow new transactions)
     *  with {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     * @param canBlock Allow the operation block and wait for the exclusive mode lock.
     */
    public boolean tryExclusiveMode(boolean canBlock);

    /**
     * Return to normal (release waiting transactions, allow new transactions).
     * Must be paired with an earlier {@link #startExclusiveMode}.
     */
    public void finishExclusiveMode();

    /** Execute an action in exclusive mode.  This method can block.
     * Equivalent to:
     * <pre>
     *   startExclusiveMode();
     *   try { action.run(); }
     *   finally { finishExclusiveMode(); }
     * </pre>
     *
     * @param action
     */
    public default void execExclusive(Runnable action) {
        startExclusiveMode();
        try { action.run(); }
        finally { finishExclusiveMode(); }
    }
}
