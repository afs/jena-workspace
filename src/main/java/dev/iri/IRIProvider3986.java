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

package dev.iri;

public class IRIProvider3986 {}

//import java.util.Objects;
//import java.util.function.Supplier;
//
//import org.apache.jena.atlas.lib.InternalErrorException;
//import org.apache.jena.irix.IRIException;
//import org.apache.jena.irix.IRIProvider;
//import org.apache.jena.irix.IRIx;
//import org.seaborne.rfc3986.IRI3986;
//import org.seaborne.rfc3986.IRIParseException;
//import org.seaborne.rfc3986.RFC3986;
//import org.seaborne.rfc3986.SystemIRI3986;
//
//public class IRIProvider3986 implements IRIProvider {
//
//    public IRIProvider3986() {}
//
//    // Convert exceptions
//    private static <X> X exec(Supplier<X> action) {
//        try {
//            return action.get();
//        } catch (IRIParseException ex) {
//            throw new IRIException(ex.getMessage());
//        }
//    }
//
//    static class IRIx3986 extends IRIx {
//        private final IRI3986 iri;
//        private IRIx3986(String iriStr, IRI3986 iri) {
//            super(iri.toString());
//            this.iri = iri;
//        }
//
//        @Override
//        public boolean isAbsolute() {
//            return iri.isAbsolute();
//        }
//
//        @Override
//        public boolean isRelative() {
//            return iri.isRelative();
//        }
//
//        @Override
//        public boolean isReference() {
//            if ( iri.isRootless() )
//                return true;
//            // isHierarchical.
//            // There is always a path even if it's ""
//            return iri.hasScheme();
//        }
//
//        @Override
//        public boolean hasScheme(String scheme) {
//            if ( ! iri.hasScheme() )
//                return false;
//            return iri.str().startsWith(scheme);
//        }
//
//        @Override
//        public IRIx resolve(String other) {
//            return exec(()->{
//                IRI3986 iriOther = create3986(other);
//                IRI3986 iri2 = this.iri.resolve(iriOther);
//                //exceptions(iri2);
//                return new IRIx3986(iri2.toString(), iri2);
//            });
//        }
//
//        @Override
//        public IRIx resolve(IRIx other) {
//            return exec(()->{
//                IRIx3986 iriOther = (IRIx3986)other;
//                IRI3986 iri2 = this.iri.resolve(iriOther.iri);
//                //exceptions(iri2);
//                return new IRIx3986(iri2.toString(), iri2);
//            });
//        }
//
//        @Override
//        public IRIx normalize() {
//            IRI3986 iri3986 = RFC3986.normalize(iri);
//            return new IRIx3986(iri3986.str(), iri3986);
//        }
//
//        @Override
//        public IRIx relativize(IRIx other) {
//            return exec(()->{
//                IRIx3986 iriOther = (IRIx3986)other;
//                IRI3986 iri2 = this.iri.relativize(iriOther.iri);
//                //exceptions(iri2);
//                return ( iri2 == null ) ? null : new IRIx3986(iri2.toString(), iri2);
//            });
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(iri);
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if ( this == obj )
//                return true;
//            if ( obj == null )
//                return false;
//            if ( getClass() != obj.getClass() )
//                return false;
//            IRIx3986 other = (IRIx3986)obj;
//            return Objects.equals(iri, other.iri);
//        }
//    }
//
//    @Override
//    public IRIx create(String iriStr) throws IRIException {
//        return exec(()->{
//            IRI3986 iri = create3986(iriStr);
//            return new IRIProvider3986.IRIx3986(iriStr, iri);
//        });
//    }
//
//    @Override
//    public void check(String iriStr) throws IRIException {
//        exec(()->{
//            IRI3986 iri = RFC3986.create(iriStr);
//            exceptions(iri);
//            return null;
//        });
//    }
//
//    @Override
//    public void strictMode(String scheme, boolean runStrict) {
//        SystemIRI3986.strictMode(scheme, strictness(runStrict));
//    }
//
//    @Override
//    public boolean isStrictMode(String scheme) {
//        SystemIRI3986.Compliance s = SystemIRI3986.getStrictMode(scheme);
//        if ( s == null )
//            return true;
//        return strictness(s);
//    }
//
//    private static SystemIRI3986.Compliance strictness(boolean runStrict) {
//        return runStrict ? SystemIRI3986.Compliance.STRICT : SystemIRI3986.Compliance.NOT_STRICT ;
//    }
//
//    private static boolean strictness(SystemIRI3986.Compliance strictness) {
//        switch(strictness) {
//            case NOT_STRICT : return false;
//            case STRICT : return true;
//            default: throw new InternalErrorException();
//        }
//    }
//
//
//    @Override
//    public String toString() {
//        return this.getClass().getSimpleName();
//    }
//
//    private static IRI3986 create3986(String iriStr) throws IRIException {
//        IRI3986 iri = RFC3986.create(iriStr);
//        exceptions(iri);
//        return iri;
//    }
//
//    private static void exceptions(IRI3986 iriObj) {
//        schemeSpecificRules(iriObj);
//    }
//
//    private static void schemeSpecificRules(IRI3986 iriObj) {
//        iriObj.schemeSpecificRules();
//    }
//}
