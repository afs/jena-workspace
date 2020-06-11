/**
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

package serviceloader_general;

import java.util.Collections ;
import java.util.Comparator ;
import java.util.List ;
import java.util.function.Consumer ;

/** Subsystem loader "system" - simple controls for ensuring components are loaded and initialized.
 * This typically uses {@link java.util.ServiceLoader} and it adds the concept of a "level"
 * so that loaded plugins are executed in a controlled order.
 * Levels are executed in order, from 0 upwards.
 * Within a level, plugins are executed in arbitrary order.
 */

// Jena usage.
 /* This is achieved by "levels": levels less than 100 are considered "Jena system levels"
 * and are reserved.
 * <ul>
 * <li>0 - reserved
 * <li>10 - jena-core
 * <li>20 - RIOT
 * <li>30 - ARQ
 * <li>40 - TDB
 * <li>100-500 - Fuseki initialization, including customizations
 * <li>9999 - other
 * </ul>
 * See also the <a href="http://jena.apache.org/documentation/notes/system-initialization.html">notes on Jena initialization</a>.
 */
public class Subsystem<T extends SubsystemLifecycle> {

    public Subsystem(Class<T> cls) {
    }



    /** Development support - flag to enable output during
     * initialization. Output to {@code System.err}, not a logger
     * to avoid the risk of recursive initialization.
     */
    public boolean DEBUG_INIT = false ;

    // A correct way to manage without synchonized using the double checked locking pattern.
    //   http://en.wikipedia.org/wiki/Double-checked_locking
    //   http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
    private volatile boolean initialized = false ;
    private Object initLock = new Object() ;

    /** Initialize Jena.
     * <p>
     * This function is cheap to call when already initialized so can be called to be sure.
     * A commonly used idiom in jena is a static initializer in key classes.
     * <p>
     * By default, initialization happens by using {@code ServiceLoader.load} to find
     * {@link SubsystemLifecycle} objects.
     * See {@link #setSubsystemRegistry} to intercept that choice.
     */
    public void init() {
        // Any other thread attempting to initialize as well will
        // first test the volatile outside the lock; if it's
        // not INITIALIZED, the thread will attempt to grab the lock
        // and hence wait, then see initialized as true.

        // But we need to cope with recursive calls of JenaSystem.init() as well.
        // The same thread will not stop at the lock.
        // Setting initialized to true before a recursive call is possible
        // handles this.  The recursive call will see initialized true and
        // and return on the first test.

        // Net effect:
        // After a top level call of init() returns, tjena has
        // finishes initialization.
        // Recursive calls do not have this property.

        if ( initialized )
            return ;
        synchronized(initLock) {
            if ( initialized )  {
                logLifecycle("JenaSystem.init - return");
                return ;
            }
            // Catches recursive calls, same thread.
            initialized = true ;
            logLifecycle("JenaSystem.init - start");

            if ( get() == null )
                setSubsystemRegistry(new JenaSubsystemRegistryBasic()) ;

            get().load() ;

            // Debug : what did we find?
            if ( DEBUG_INIT ) {
                logLifecycle("Found:") ;
                get().snapshot().forEach(mod->
                logLifecycle("  %-20s [%d]", mod.getClass().getSimpleName(), mod.level())) ;
            }

            //get().add(new JenaInitLevel0()) ;

            if ( DEBUG_INIT ) {
                logLifecycle("Initialization sequence:") ;
                forEach( module ->
                    logLifecycle("  %-20s [%d]", module.getClass().getSimpleName(), module.level()) ) ;
            }

            forEach( module -> {
                logLifecycle("Init: %s", module.getClass().getSimpleName());
                module.start() ;
            }) ;
            logLifecycle("JenaSystem.init - finish");
        }
    }

    /** Shutdown subsystems */
    public void shutdown() {
        if ( ! initialized ) {
            logLifecycle("JenaSystem.shutdown - not initialized");
            return ;
        }
        synchronized(initLock) {
            if ( ! initialized ) {
                logLifecycle("JenaSystem.shutdown - return");
                return ;
            }
            logLifecycle("JenaSystem.shutdown - start");
            forEachReverse(module -> {
                logLifecycle("Stop: %s", module.getClass().getSimpleName());
                module.stop() ;
            }) ;
            initialized = false ;
            logLifecycle("JenaSystem.shutdown - finish");
        }
    }

    private SubsystemRegistry singleton = null;

    /**
     * Set the {@link SubsystemRegistry}.
     * To have any effect, this function
     * must be called before any other Jena code,
     * and especially before calling {@code JenaSystem.init()}.
     */
    public void setSubsystemRegistry(SubsystemRegistry thing) {
        singleton = thing;
    }

    /** The SubsystemRegistry */
    public SubsystemRegistry get() {
        return singleton;
    }

    /**
     * Call an action on each item in the registry. Calls are made sequentially
     * and in increasing level order. The exact order within a level is not
     * specified; it is not registration order.
     *
     * @param action
     */
    public void forEach(Consumer<SubsystemLifecycle> action) {
        forEach(action, comparator);
    }

    /**
     * Call an action on each item in the registry but in the reverse
     * enumeration order. Calls are made sequentially and in decreasing level
     * order. The "reverse" is opposite order to {@link #forEach}, which may not
     * be stable within a level. It is not related to registration order.
     *
     * @param action
     */
    public void forEachReverse(Consumer<SubsystemLifecycle> action) {
        forEach(action, reverseComparator);
    }

    // Order by level (increasing)
    private static Comparator<SubsystemLifecycle> comparator        = (obj1, obj2) -> Integer.compare(obj1.level(), obj2.level()) ;
    // Order by level (decreasing)
    private static Comparator<SubsystemLifecycle> reverseComparator = comparator.reversed();

    private synchronized void forEach(Consumer<SubsystemLifecycle> action, Comparator<SubsystemLifecycle> ordering) {
        synchronized(initLock) {
            List<SubsystemLifecycle> x = get().snapshot() ;
            Collections.sort(x, ordering);
            x.forEach(action);
        }
    }

    /** Output a debugging message if DEBUG_INIT is set */
    public void logLifecycle(String fmt, Object ...args) {
        if ( ! DEBUG_INIT )
            return ;
        System.err.printf(fmt, args) ;
        System.err.println() ;
    }
//
//    /** The level 0 subsystem - inserted without using the Registry load function.
//     *  There should be only one such level 0 handler.
//     */
//    private static class JenaInitLevel0 implements SubsystemLifecycle {
//        @Override
//        public void start() {
//            logLifecycle("Jena initialization");
//        }
//
//        @Override
//        public void stop() {
//            logLifecycle("Jena shutdown");
//        }
//
//        @Override
//        public int level() {
//            return 0;
//        }
//    }
}
