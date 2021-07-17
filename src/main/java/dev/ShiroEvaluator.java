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

public class ShiroEvaluator {}
//
///**
// * Class to use Shiro to provide credentials.
// *
// * An example evaluator that only provides access to messages in the graph that
// * are from or to the principal.
// *
// */
//public class ShiroEvaluator implements SecurityEvaluator {
//
//    private static final Logger LOG = LoggerFactory.getLogger(ShiroEvaluator.class);
//
//    public ShiroEvaluator( Model model )
//    {}
//
//    @Override
//    public boolean evaluate(Object principal, Action action, Node graphIRI, Triple triple) {
//
//        return false;
//    }
//
//    @Override
//    public boolean evaluate(Object principal, Set<Action> actions, Node graphIRI, Triple triple) {
//
//        return false;
//
//    }
//
//    @Override
//    public boolean evaluateAny(Object principal, Set<Action> actions, Node graphIRI, Triple triple) {
//
//        return false;
//
//    }
//
//    @Override
//    public boolean evaluate(Object principal, Set<Action> actions, Node graphIRI) {
//
//        return false;
//    }
//
//    @Override
//    public boolean evaluate(Object principal, Action action, Node graphIRI) {
//
//        return false;
//    }
//
//    @Override
//    public boolean evaluateAny(Object principal, Set<Action> actions, Node graphIRI) {
//
//        return false;
//    }
//
//
//    @Override
//    public boolean evaluateUpdate(Object principal, Node graphIRI, Triple from, Triple to) {
//
//        return false;
//    }
//
//    /**
//     * Return the Shiro subject.  This is the subject that Shiro currently has logged in.
//     */
//    @Override
//    public Object getPrincipal() {
//        return SecurityUtils.getSubject();
//    }
//
//    /**
//     * Verify the Shiro subject is authenticated.
//     */
//    @Override
//    public boolean isPrincipalAuthenticated(Object principal) {
//        if (principal instanceof Subject)
//        {
//            return ((Subject)principal).isAuthenticated();
//        }
//        return false;
//    }
//}
