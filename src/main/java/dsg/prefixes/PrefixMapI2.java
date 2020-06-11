/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package dsg.prefixes;


public interface PrefixMapI2 {}

//import org.apache.jena.atlas.lib.Pair;
//import org.apache.jena.dboe.storage.prefixes.PrefixLib;
//import org.apache.jena.dboe.storage.prefixes.PrefixMapIOverStorage;
//import org.apache.jena.dboe.storage.prefixes.PrefixMapI;
//import org.apache.jena.dboe.storage.prefixes.StoragePrefixMap;
//import org.apache.jena.shared.PrefixMapping;
//import java.util.Map;
///** Implementation-view of prefix mappings that also provides {@link PrefixMapping}.
// *
// * @apiNote
// * <p>See {@link StoragePrefixMap} for the storage implementation view.
// * <p>See {@link PrefixMapIOverStorage} for an implementation over {@link StoragePrefixMap}
// */
//
//// Merge interfaces: Alternative to PrefixMappingOverPrefixMapI
//// Is an adapter better? Easier to isolate PrefixMapping to model.
//
//public interface PrefixMapI2 extends PrefixMapI, PrefixMapping
//{
//    // ---- PrefixMapping implementation over PrefixMapI using default methods.
//    @Override
//    public default PrefixMapping setNsPrefix(String prefix, String uri) {
//        add(prefix, uri);
//        return this;
//    }
//
//    @Override
//    public default PrefixMapping removeNsPrefix(String prefix) {
//        delete(prefix);
//        return this;
//    }
//
//    @Override
//    public default PrefixMapping clearNsPrefixMap() {
//        clear();
//        return this;
//    }
//
//    @Override
//    public default PrefixMapping setNsPrefixes(PrefixMapping other) {
//        return setNsPrefixes(other.getNsPrefixMap());
//    }
//
//    @Override
//    public default PrefixMapping setNsPrefixes(Map<String, String> map) {
//        for ( Map.Entry<String, String> e : map.entrySet() ) {
//            String prefix = e.getKey();
//            String iriStr = e.getValue();
//            add(prefix, iriStr);
//        }
//        return this;
//    }
//
//    @Override
//    public default PrefixMapping withDefaultMappings(PrefixMapping map) {
//        Map<String, String> emap = map.getNsPrefixMap();
//        for ( Map.Entry<String, String> e : emap.entrySet() ) {
//            String prefix = e.getKey();
//            String iriStr = e.getValue();
//            if ( !containsPrefix(prefix) )
//                add(prefix, iriStr);
//        }
//        return this;
//    }
//
//    @Override
//    public default String getNsPrefixURI(String prefix) {
//        return get(prefix);
//    }
//
//    @Override
//    public default String getNsURIPrefix(String uri) {
//        Pair<String, String> abbrev = abbrev(uri);
//        if ( abbrev == null )
//            return null;
//        return abbrev.getLeft();
//    }
//
//    @Override
//    public default Map<String, String> getNsPrefixMap() {
//        return getMappingCopy();
//    }
//
//    @Override
//    public default String expandPrefix(String prefixed) {
//        String str = PrefixLib.expand(this, prefixed);
//        if ( str == null )
//            return prefixed;
//        return str;
//    }
//
//    @Override
//    public default String shortForm(String uri) {
//        String s = PrefixLib.abbreviate(this, uri);
//        if ( s == null )
//            return uri;
//        return s;
//    }
//
//    @Override
//    public default String qnameFor(String uri) {
//        return PrefixLib.abbreviate(this, uri);
//    }
//
//    @Override
//    public default boolean hasNoMappings() {
//        return isEmpty();
//    }
//
//    @Override
//    public default int numPrefixes() {
//        return size();
//    }
//
//    @Override
//    public default PrefixMapping lock() {
//        return this;
//    }
//
//    @Override
//    public default boolean samePrefixMappingAs(PrefixMapping other) {
//        return this.getNsPrefixMap().equals(other.getNsPrefixMap());
//    }
//}
