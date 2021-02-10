package tdb2.stress_tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb2.TDB2Factory;
//import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class TDB2StressTest {

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

//	@Test
//	public void stackOverflowErrorInBPTree() throws Exception {
//		// Big commit sizes tend to make StackOverflowErrors in B+Tree more likely
//		runStressTest(8, 8, 10, 200000);
//	}

	@Test
    public void npeInTransactionalComponentLifecycle() throws Exception {
        // Small commit sizes tend to make NPEs in TransactionalComponentLifecycle more likely
        runStressTest(8, 8, 1000, 100);
    }

	private void runStressTest(int databaseCount, int threadCount, int iterations, int commitSize) throws Exception {
		final List<Dataset> datasets = new ArrayList<>();
		for (int i = 0; i < databaseCount; i++) {
			final File dir = tmpFolder.newFolder();
			final Dataset dataset = TDB2Factory.createDataset();
			//final Dataset dataset = TDB2Factory.connectDataset(dir.getAbsolutePath());
			datasets.add(dataset);
		}
		final List<Future<?>> futures = new ArrayList<>();
		final ExecutorService executors = Executors.newFixedThreadPool(threadCount);
		try {
			for (int i = 0; i < threadCount; i++) {
				futures.add(executors.submit(() -> {
					runStressTestWorker(executors, datasets, threadCount, iterations, commitSize);
				}));
			}
			for (Future<?> f : futures) {
				f.get();
			}
		} finally {
			executors.shutdown();
		}
	}

	private void runStressTestWorker(ExecutorService executors, List<Dataset> datasets, int threadCount, int iterations,
			int commitSize) {
		final Random rand = new Random();
		try {
			for (int i = 0; i < iterations; i++) {
				if (executors.isShutdown()) {
					return;
				}
				final Dataset dataset = datasets.get(rand.nextInt(datasets.size()));
				if (rand.nextBoolean()) {
					randomRead(dataset, threadCount, rand);
				}
				if (rand.nextBoolean()) {
					randomWrite(dataset, threadCount, commitSize, rand);
				}
			}
		} catch (Throwable t) {
			executors.shutdown();
			t.printStackTrace();
			throw t;
		}
	}

	private void randomRead(Dataset dataset, int threadCount, Random rand) {
		dataset.begin(ReadWrite.READ);
		try {
			final Model m = dataset.getNamedModel("test://model#" + rand.nextInt(threadCount));
			final StmtIterator iter = m.listStatements();
			try {
				iter.hasNext();
			} finally {
				iter.close();
			}
			//dataset.commit(); // *************
			dataset.abort();
		} finally {
			dataset.end();
		}
	}

	private void randomWrite(Dataset dataset, int threadCount, int commitSize, Random rand) {
		boolean success = false;
		dataset.begin(ReadWrite.WRITE);
		try {
			final Model m = dataset.getNamedModel("test://model#" + rand.nextInt(threadCount));
			for (int i = rand.nextInt(threadCount); i < commitSize; i++) {
				final Statement stmt = m.createStatement(m.getResource("test://subj#" + i), m.getProperty("test://prop"),
						m.getResource("test://obj#" + i));
				if (rand.nextBoolean()) {
					m.add(stmt);
				} else {
					m.removeAll(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
				}
			}
			dataset.abort();
			success = true;
		} finally {
			if (!success) {
				dataset.abort();
			}
			dataset.end();
		}
	}

}
