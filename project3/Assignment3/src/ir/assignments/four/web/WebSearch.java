package ir.assignments.four.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ir.assignments.four.ResultSet;
import ir.assignments.four.SearchFiles;
import ir.assignments.four.storage.DocumentStorage;
import ir.assignments.four.storage.HtmlDocument;

public class WebSearch {
	private SearchFiles indexSearch;
	private DocumentStorage docStorage;

	public WebSearch() {
		try {
			this.indexSearch = new SearchFiles("docIndexEnhanced", "docIndexAutoComplete");
			this.docStorage = new DocumentStorage("docStorage\\docStorage");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public WebResultSet search(String query, int page, int maxPerPage) {
		ResultSet results = this.indexSearch.search(query, page, maxPerPage);

		// Create the results 
		ArrayList<SearchResult> displayResults = new ArrayList<SearchResult>();
		List<String> urls = results.getUrls();

		for (int i = 0; i < urls.size(); i++) {
			String url = urls.get(i);
			String html = docStorage.getDocument(url);
			HtmlDocument doc = new HtmlDocument(url, html);

			// Use Lucene to highlight terms in the title and extract relevant fragments from the content
			String title = "";
			String description = "";
			try {
				title = this.indexSearch.getHighlights("title", query, doc.getTitle(), true); // entire "document"
				description = this.indexSearch.getHighlights("content", query, doc.getBody(), false); // get fragments
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			SearchResult result = new SearchResult(title, url, description);
			displayResults.add(result);
		}

		SearchResult[] displayResultsArray = displayResults.toArray(new SearchResult[displayResults.size()]);
		return new WebResultSet(displayResultsArray, results.getTotalHits(), page); 
	}
	
	public String[] getAutoCompleteSuggestions(String query) {
		Set<String> completions = new HashSet<String>();
		
		if (!query.endsWith(" ")) { // no "last term"
			// Use only the last term since we can only autocomplete a single word
			String[] terms = query.trim().split(" ");
			if (terms.length > 0) {
				String termToComplete = terms[terms.length - 1];
				Set<String> completeTerms = this.indexSearch.suggestTerms(termToComplete);
				for (String completeTerm : completeTerms) {
					if (completeTerm.equals(termToComplete))
						continue; // no need to autocomplete
					
					// Just keep the completion (the part missing from the current query)
					String completion = completeTerm.substring(termToComplete.length());
					completions.add(completion);
				}
			}
		}
		
		return completions.toArray(new String[completions.size()]);
	}

	public void close() {
		if (this.indexSearch != null) {
			this.indexSearch.close();
		}

		if (this.docStorage != null) {
			this.docStorage.close();
		}
	}
}
