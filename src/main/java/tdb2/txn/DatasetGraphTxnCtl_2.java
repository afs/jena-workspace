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

import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.util.Context;

/**
 * DatasetGraph wrapper that adds control of transactions.
 * <p>
 * Controls:
 * <ul>
 * <li>Blocking writers
 * <li>Exclusive mode
 * </ul>
 */

public class DatasetGraphTxnCtl_2 extends DatasetGraphWrapper implements TransactionalSystemControl_2 {
    // TranactionalSystem?

    public DatasetGraphTxnCtl_2(DatasetGraph dsg) {
        super(dsg);
    }

    public DatasetGraphTxnCtl_2(DatasetGraph dsg, Context context) {
        super(dsg, context);
    }

    // ---- Blockable transactions

    @Override
    public void begin(TxnType txnType) {
        Objects.nonNull(txnType);
        checkNotActive();
        enterTransaction(txnType, true);
        super.begin(txnType);
    }

    final
    protected void checkNotActive() {
        checkNotShutdown();
        if ( isInTransaction() )
            throw new TransactionException(label("Currently in an active transaction"));
    }

    private String label(String msg) {
        // Optional labelling
        return msg;
    }

    final
    protected void checkActive() {
        // Check no transaction on this thread.
        checkNotShutdown();
        if ( ! isInTransaction() )
            throw new TransactionException(label("Not in an active transaction"));
    }


    private void checkNotShutdown() {}


    @Override
    public void begin(ReadWrite readWrite) {
        Objects.nonNull(readWrite);
        begin(TxnType.convert(readWrite));
    }

    /*
     * Admission policy.
     * - exclusive mode (no transactions running)
     * - possibleWriters
     * - single writer, multiple readers
     */
    private void enterTransaction(TxnType txnType, boolean canBlock) {
        boolean canBlockWriters = canBlock; // false => "bounce writers mode"

        boolean bExclusive = tryNonExclusiveMode(canBlock);
        if ( !bExclusive )
            throw new TransactionException("Can't start transaction at the moment.");

        // Readers never block.
        // Writers admitted one at a time.
        // Promotes are reader until they promote.
        switch(txnType) {
            case READ :
                break;
            case READ_COMMITTED_PROMOTE :
            case READ_PROMOTE : {
                startPossibleWriter();
                break;
            }
            case WRITE : {
                startPossibleWriter();
                boolean bWriter = acquireWriterLock(canBlockWriters);
                if ( !bWriter ) {
                    // Failed. Release the exclusivity lock.
                    finishNonExclusiveMode();
                    throw new TransactionException("Can't start write transaction at the moment.");
                }
                break;
            }
        }
    }

    private void exitTransaction() {
        TxnType txnType = transactionType();
        ReadWrite rwMode = transactionMode();
        if ( rwMode == ReadWrite.WRITE )
            releaseWriterLock();
        finishNonExclusiveMode();
    }

//    private void admitWriter(boolean canBlock) {
//        // Writers take a WRITE permit from the semaphore to ensure there
//        // is at most one active writer, else the attempt to start the
//        // transaction blocks.
//
//        // Already in write mode?
//        if ( transactionMode() == ReadWrite.WRITE )
//            return;
//
//        boolean b = acquireWriterLock(canBlock);
//        if ( !b ) {
//            // Failed. Release the exclusivity lock.
//            finishNonExclusiveMode();
//            throw new TransactionException("Can't start write transaction at the moment.");
//        }
//    }

    @Override
    public boolean promote() {
        return promote(Promote.ISOLATED);
    }

    @Override
    public boolean promote(Promote type) {
        boolean b = onPromote(type == Promote.READ_COMMITTED);
        if ( ! b )
            return false;
        boolean b2 = super.promote(type);
        if ( ! b2 )
            // Not a promoter after all.
            releaseWriterLock();
        return b2;
    }

    /**
     * Attempt to promote a transaction from READ mode to WRITE mode. Whether
     * intervening commits are seen is determined by the boolean flag. Returns true
     * if a writer on exit - includes the case where the transaction is already a
     * writer.
     */
    private boolean onPromote(boolean readCommittedPromotion) {
        if ( transactionMode() == ReadWrite.WRITE )
            return true;
        // Even if promotion of TxnType.READ were allowed, this ability is usually
        // rejected by the transaction system around it. e.g. TransactionalBase.
        if ( transactionType() == TxnType.READ )
            return false;
        // Not admitWriter(true) which throws an exception.
        boolean b = acquireWriterLock(true);
        return b;
    }

    @Override
    public void commit() {
        super.commit();
        exitTransaction();
    }

    @Override
    public void abort() {
        super.abort();
        exitTransaction();
    }

    @Override
    public void end() {
        if ( isInTransaction() )
            exitTransaction();
        super.end();
    }

    // ---- Controls

    // Multiple state X or single state Y.
    // Do not confuse with read/write transactions. We need a "one exclusive, or many
    // other" lock which happens to be called {@code ReadWriteLock}.

    // All transaction need "read" state X through out their lifetime.
    // The "write" state Y is used for exclusive mode.
    private ReadWriteLock exclusivitylock = new ReentrantReadWriteLock();

    // Semaphore to implement "Single Active Writer" - independent of readers
    // This is not reentrant.
    private Semaphore writersWaiting = new Semaphore(1, true);

    // Lock to guarantee only readers are present.
    // Writers and promote transaction need to take a lock on entry.
    // This is not reentrant.
    private ReadWriteLock writeableDatabase = new ReentrantReadWriteLock();

    // Lock naming
    //   MultipleOrSingle (MOS)
    //   MultipleAndSingle (MAS, M+S)

    // -- Allow/block writers.

    /** Acquire the writer lock - no effect on readers - returns true if succeeded */
    private boolean acquireWriterLock(boolean canBlock) {
        if ( !canBlock )
            return writersWaiting.tryAcquire();
        try {
            writersWaiting.acquire();
            return true;
        } catch (InterruptedException e) {
            throw new TransactionException(e);
        }
    }

    private void releaseWriterLock() {
//        int x = writersWaiting.availablePermits();
//        if ( x != 0 )
//            throw new TransactionException("TransactionCoordinator: Probably mismatch of acquireWriterLock/releaseWriterLock calls");
        writersWaiting.release();
    }

    /**
     * Block until no writers are active. When this returns, this guarantees that the
     * database is not changing and the journal is flushed to disk.
     * <p>
     * The application must call {@link #enableWriters} later.
     * <p>
     * This operation must not be nested (it will block).
     *
     * @see #tryBlockWriters()
     * @see #enableWriters()
     */
    @Override
    public void blockWriters() {
        acquireWriterLock(true);
    }

    /**
     * Try to block all writers, or return if can't at the moment.
     * <p>
     * Unlike a write transaction, there is no associated transaction.
     * <p>
     * If it returns true, the application must call {@link #enableWriters} later.
     *
     * @see #blockWriters()
     * @see #enableWriters()
     * @return true if the operation succeeded and writers are blocked
     */
    @Override
    public boolean tryBlockWriters() {
        return tryBlockWriters(false);
    }

    /**
     * Block until no writers are active, optionally blocking or returning if can't
     * at the moment.
     * <p>
     * Unlike a write transaction, there is no associated transaction.
     * <p>
     * If it returns true, the application must call {@link #enableWriters} later.
     *
     * @param canBlock
     * @return true if the operation succeeded and writers are blocked
     */
    @Override
    public boolean tryBlockWriters(boolean canBlock) {
        return acquireWriterLock(canBlock);
    }

    /**
     * Allow writers. This must be used in conjunction with {@link #blockWriters()}
     * or {@link #tryBlockWriters()}
     *
     * @see #blockWriters()
     * @see #tryBlockWriters()
     */
    @Override
    public void enableWriters() {
        releaseWriterLock();
    }

    // XXX Lock naming.
    //  MultipleOrSingle (MOS) == MRSW
    //  MultipleAndSingle (MAS) == MR+SW

    /**
     * Block until no writers or promote transactions present are present.
     */
    @Override
    public void startReadOnlyDatabase()  {
        startReadOnly();
    }

    /**
     * Release any waiting potential writers.
     */
    @Override
    public void finishReadOnlyDatabase() {
        finishReadOnly();
    }

    // Any writers - WRITE, READ_PROMOTE, READ_COMMITTED_PROMOTE
    // Writer take this lock (slightly confusingly as a "read" lock)
    // READ-ONLY operation do no tneed to take this block.

    protected void startReadOnly() {
        startReadOnly(true);
    }

    // -- better
    // start(TxnType)
    // finish(TxnType)

    // XXX Merge into enterTransaction and exitTransaction.

    private void start(TxnType txnType) {
        Objects.requireNonNull(txnType);
        switch(txnType) {
            case READ :
                // Take multi-side of exclusivity.
                break;
            // "Single writer" is managed by the storage database.
            case READ_COMMITTED_PROMOTE :
            case READ_PROMOTE :
            case WRITE :
             // Take multi-side of exclusivity.
             // Take single writeableDatabase
                break;
        }
    }

    private void finish(TxnType txnType) {
        Objects.requireNonNull(txnType);
        switch(txnType) {
            case READ :
                // Take multi-side of exclusivity.
                break;
            // "Single writer" is managed by the storage database.
            case READ_COMMITTED_PROMOTE :
            case READ_PROMOTE :
            case WRITE :
             // Take multi-side of exclusivity.
             // Take single writeableDatabase
                break;

        }
    }


    /**
     * Indicate the database is to be readonly.
     * Any writer, or potential writer, takes this lock in "multi" mode.
     * To block, call this method.
     */
    protected void startReadOnly(boolean canBlock) {
        beginSingleMode(writeableDatabase, canBlock);
    }

    protected void finishReadOnly() {
        endSingleMode(writeableDatabase);
    }

    protected void startPossibleWriter() {
        beginMultiMode(writeableDatabase, true);
    }

//    protected void startPossibleWriter(boolean canBlock) {
//        beginMultiMode(writeableDatabase, canBlock);
//    }

    protected void finishPossibleWriter() {
        endMultiMode(writeableDatabase);
    }

    // -- Lock ops.
    /* Function for enter-leave for two lock types:
     *    Multiple active threads or exclusive access. (MRSW)
     *      beginMultiMode, beginSingleMode
     *    Multiple active threads and one distinguished thread (MR+SW).
     *      beginSingleGate
     *
     * == Multiple active threads or exclusive access. (MRSW)
     * This uses a ReadWriteLock where "read" is multiple outstanding threads
     * and "write" is exclusive access.
     * Used for:
     *   Exclusivity lock
     *   No potential writer lock (TxnType.WRITE or PROMOTE).
     *
     * The use of R and W for the exclusive access locks are slightly confusing.
     * It is not related to whether a transaction is R or W.
     * R means "multiple", W means "exclusive"
     *
     * == Multiple active threads and one distinguished thread (MR+SW).
     * This is a semaphore with one permit taken by the distinguished thread.
     * The other active threads are not affected.
     * Use for:
     *   Multiple ReadWrite==READ and single ReadWrite.WRITE
     */

    // MRSW - two modes: multiple OR a single thread.

    private final boolean beginMultiMode(ReadWriteLock lock, boolean canBlock) {
        if ( !canBlock )
            return lock.readLock().tryLock();
        lock.readLock().lock();
        return true;
    }

    private static void endMultiMode(ReadWriteLock lock) {
        lock.readLock().unlock();
    }

    private static boolean beginSingleMode(ReadWriteLock lock, boolean canBlock) {
        if ( !canBlock )
            return lock.writeLock().tryLock();
        lock.writeLock().lock();
        return true;
    }

    private static void endSingleMode(ReadWriteLock lock) {
        lock.writeLock().unlock();
    }

    // MR+SW - limit a single thread: multiple AND at most one single mode. Only applies to the "single" thread.

    private static boolean beginSingleGate(Semaphore lock, boolean canBlock) {
        if ( !canBlock )
            return lock.tryAcquire();
        try {
            lock.acquire();
            return true;
        } catch (InterruptedException e) {
            throw new TransactionException(e);
        }
    }

    private static void endSingleGate(Semaphore lock) {
        int x = lock.availablePermits();
        if ( x != 0 )
            throw new TransactionException("TransactionCoordinator: Probably mismatch of acquireWriterLock/releaseWriterLock calls");
        lock.release();
    }

    // -- Exclusivity

    @Override
    public void startNonExclusiveMode() {
        boolean b = beginMultiMode(exclusivitylock, true);
        // XXX boolean b = tryNonExclusiveMode(true);
        if ( !b )
            throw new TransactionException("Can't start transaction at the moment.");
    }

    @Override
    public boolean tryNonExclusiveMode(boolean canBlock) {
        return beginMultiMode(exclusivitylock, canBlock);
//        if ( !canBlock )
//            return exclusivitylock.readLock().tryLock();
//        exclusivitylock.readLock().lock();
//        return true;
    }

    @Override
    public void finishNonExclusiveMode() {
        endMultiMode(exclusivitylock);
        //exclusivitylock.readLock().unlock();
    }

    /**
     * Enter exclusive mode; block if necessary. There are no active transactions on
     * return; new transactions will be held up in 'begin'. Return to normal (release
     * waiting transactions, allow new transactions) with
     * {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     */
    @Override
    public void startExclusiveMode() {
        startExclusiveMode(true);
    }

    /**
     * Try to enter exclusive mode. If return is true, then there are no active
     * transactions on return and new transactions will be held up in 'begin'. If
     * false, there were in-progress transactions. Return to normal (release waiting
     * transactions, allow new transactions) with {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     */
    @Override
    public boolean tryExclusiveMode() {
        return tryExclusiveMode(false);
    }

    /**
     * Try to enter exclusive mode. If return is true, then there are no active
     * transactions on return and new transactions will be held up in 'begin'. If
     * false, there were in-progress transactions. Return to normal (release waiting
     * transactions, allow new transactions) with {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     *
     * @param canBlock Allow the operation block and wait for the exclusive mode
     *     lock.
     */
    @Override
    public boolean tryExclusiveMode(boolean canBlock) {
        return startExclusiveMode(canBlock);
    }

    /**
     * Return to normal (release waiting transactions, allow new transactions). Must
     * be paired with an earlier {@link #startExclusiveMode}.
     */
    @Override
    public void finishExclusiveMode() {
        exclusivitylock.writeLock().unlock();
    }

    /**
     * Enter exclusive mode; block if necessary. There are no active transactions on
     * return; new transactions will be held up in 'begin'. Return to normal (release
     * waiting transactions, allow new transactions) with
     * {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     */
    private boolean startExclusiveMode(boolean canBlock) {
        if ( canBlock ) {
            exclusivitylock.writeLock().lock();
            return true;
        }
        return exclusivitylock.writeLock().tryLock();
    }
}
