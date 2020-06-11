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

package test4.next.runners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.sparql.junit.EarlReport;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import test4.next.manifest.Label;
import test4.next.manifest.Manifest;
import test4.next.manifest.ManifestEntry;
import test4.next.manifest.Manifests;

/**
 * Common super class for {@code @Runner(....)}.
 * <p>
 * Creates a runner for a manifest that has children for each included manifest and
 * each test defined in the manifest. It follow included manifests Annotations
 * supported:
 * <ul>
 * <li><tt>@Label("Some name")</tt></li>
 * <li><tt>@Manifests({"manifest1","manifest2",...})</tt></li>
 * </ul>
 * This class sorts out the annotations, including providing before/after class, then
 * creates a hierarchy of tests to run.
 *
 * @see RunnerOneTest
 */
public abstract class RunnerOfTests extends ParentRunner<Runner> {
    private Description  description;
    private List<Runner> children = new ArrayList<>();

    // Need unique names.

    public RunnerOfTests(Class<? > klass, Function<ManifestEntry, Runnable> maker) throws InitializationError {
        super(klass);
        String label = getLabel(klass);
        if ( label == null )
            label = klass.getName();
        String[] manifests = getManifests(klass);
        if ( manifests.length == 0 )
            System.err.println("No manifests: "+label);
            //throw new InitializationError("No manifests");
        description = Description.createSuiteDescription(label);

        for ( String manifestFile : manifests ) {
            Manifest manifest = Manifest.parse(manifestFile);
            Runner runner = build(null, manifest, maker);
            description.addChild(runner.getDescription());
            children.add(runner);
        }
    }

    /**
     * Do one level of tests. test are {@link Runnable Runnables} that each succeed or fail with an exception.
     */
    public static RunnerOneManifest build(EarlReport report, Manifest manifest, Function<ManifestEntry, Runnable> maker) {
        //System.err.println("Manifest: "+manifest.getName());
        Description description = Description.createSuiteDescription(manifest.getName());

        RunnerOneManifest thisLevel = new RunnerOneManifest(description);

        Iterator<String> sub = manifest.includedManifests();
        while(sub.hasNext() ) {
            String mf = sub.next();
            Manifest manifestSub = Manifest.parse(mf);
            Runner runner = build(report, manifestSub, maker);
            thisLevel.add(runner);
        }
        prepareTests(report, thisLevel, manifest, maker);
        return thisLevel;
    }

    public static void prepareTests(EarlReport report, RunnerOneManifest level, Manifest manifest, Function<ManifestEntry, Runnable> maker) {
        manifest.entries().forEach(entry->{
            String label = entry.getName();
            label = fixupName(label);
            Runnable runnable = maker.apply(entry);
            Runner r = new RunnerOneTest(label, runnable, entry.getURI(), report);
            level.add(r);
        });
    }

    // Keep Eclipse happy.
    public static String fixupName(String string) {
        string = string.replace('(', '[');
        string = string.replace(')', ']');
        return string;
    }


    private static String getLabel(Class<? > klass) {
        Label annotation = klass.getAnnotation(Label.class);
        if ( annotation == null )
            return null;
        return annotation.value();
    }

    private static String[] getManifests(Class<? > klass) throws InitializationError {
        Manifests annotation = klass.getAnnotation(Manifests.class);
        if ( annotation == null ) {
            throw new InitializationError(String.format("class '%s' must have a @Manifests annotation", klass.getName()));
        }
        return annotation.value();
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    protected List<Runner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(Runner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
        child.run(notifier);
    }
}
