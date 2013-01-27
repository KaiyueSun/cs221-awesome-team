package ir.assignments.three.tests;

import static org.junit.Assert.*;
import ir.assignments.three.*;
import ir.assignments.two.a.Frequency;
import ir.assignments.two.tests.TestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class IntegrationTest {
	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	@Test
	public void testCrawl() {
		// Do an actual crawl of www.vcskicks.com (Armando's website)
		StopWatch watch = new StopWatch();

		Collection<String> crawledUrls = new ArrayList<String>();
		DocumentStorage documentStorage = null;
		
		try {
			// Parameters (use temporary folders to storage)
			String seedURL = "http://www.vcskicks.com";
			String intermediateStoragePath = tmpFolder.newFolder().getAbsolutePath();
			String documentStoragePath = tmpFolder.newFolder().getAbsolutePath();
			int maxDepth = 1;
			int maxPages = -1; //unlimited

			documentStorage = new DocumentStorage(documentStoragePath);
			
			watch.start();
			crawledUrls = Crawler.crawl(seedURL, intermediateStoragePath, documentStorage, maxDepth, maxPages);
			watch.stop();
		}
		catch (IOException ex) {
			fail("IOException: " + ex.getMessage());
		}
		
		System.out.println("Crawled " + crawledUrls.size() + " pages(s)");
		
		// Check something was crawled
		assertTrue(crawledUrls.size() >= 10);

		// Question 1
		double secondsElapsed = watch.getTotalElapsedSeconds();
		assertTrue(secondsElapsed >= 30); // any faster and something probably not right

		// Question 2
		int uniquePageCount = UrlStatistics.countUniquePages(crawledUrls);
		assertTrue(uniquePageCount >= 17 && uniquePageCount <= 25);

		// Question 3
		List<Frequency> subdomains = UrlStatistics.countSubdomains(crawledUrls);
		ArrayList<Frequency> expectedSubdomains = new ArrayList<Frequency>();
		expectedSubdomains.add(new Frequency("http://www.vcskicks.com", uniquePageCount));
		TestUtils.compareFrequencyLists(expectedSubdomains, subdomains);

		// Question 4
		String longestPageUrlString = UrlStatistics.getLongestPage(crawledUrls, documentStorage);
		assertEquals("http://www.vcskicks.com/csharp-programming.php", longestPageUrlString);
	}
}
