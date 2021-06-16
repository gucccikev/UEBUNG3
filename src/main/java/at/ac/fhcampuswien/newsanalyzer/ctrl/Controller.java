package at.ac.fhcampuswien.newsanalyzer.ctrl;

import at.ac.fhcampuswien.NewsApiException;
import at.ac.fhcampuswien.downloads.ArticleDownloader;
import at.ac.fhcampuswien.newsapi.NewsApi;
import at.ac.fhcampuswien.newsapi.NewsApiBuilder;
import at.ac.fhcampuswien.newsapi.beans.Article;
import at.ac.fhcampuswien.newsapi.beans.NewsResponse;
import at.ac.fhcampuswien.newsapi.enums.Country;
import at.ac.fhcampuswien.newsapi.enums.Endpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {

	public static final String APIKEY = "cd941bb0debb45429510106faff7c646";  //TODO add your api key

	public void process(String choice, String fromDate) {
		System.out.println("Start process");
		//TODO implement Error handling

		//TODO load the news based on the parameters
		try {
			NewsApi newsApi = getData(choice, fromDate);
			//TODO implement methods for analysis
			NewsResponse newsResponse = newsApi.getNews();

			if (newsResponse != null) {
				List<Article> articles = newsResponse.getArticles();
				articles.stream().forEach(article -> System.out.println(article.toString()));
				System.out.println("--------------Analytics--------------");
				System.out.println("Number of articles: " + articles.size());
				System.out.println("Provides most articles: " + analiseProvider(articles));
				System.out.println("Author with shortest name: " + shortestAuthorName(articles));
				System.out.println("Articles sorted alphabetically: ");
				List<Article> sortedArticleList = sortArticles(articles);
				sortedArticleList.stream().forEach(article -> System.out.println(article.toString()));
				//download Articles in .txt file
				try {
					String saveLocation = "C:\\Users\\bjkev\\IdeaProjects\\UEBUNG3\\src\\main\\java\\at\\ac\\fhcampuswien\\downloads\\DownloadedArticles.txt";
					ArticleDownloader.downloadUsingStream(newsApi.getUrlBase(), saveLocation);
					System.out.println("All articles have been downloaded successfully!\nLocation: " + saveLocation);
				}
				catch (IOException e){
					throw new NewsApiException("Problem with downloading the articles" + "\nERROR: " + e, e);
				}
			}
		}
		catch(NewsApiException e){
			System.out.println(e);
		}
		System.out.println("End process");
	}

	public NewsApi getData(String choice, String fromDate) {
		NewsApi newsApi = new NewsApiBuilder()
				.setApiKey(APIKEY)                        	//API Key is needed
				.setQ(choice)                            	//"keyword"
				.setSourceCountry(Country.at)          		//relevant country
				.setFrom(fromDate)                          //from which date on
				.setEndPoint(Endpoint.TOP_HEADLINES)        //"how many articles to retrieve"
				.createNewsApi();
		return newsApi;
	}

	public String analiseProvider(List<Article> articleList) {
		if(articleList.isEmpty()){
			throw new NewsApiException("No news available, be more general with your choices");
		}
		//Name as Key, and the number of occurrences as Integer
		Map<String, Integer> providerNames = new HashMap<>();
		for (Article article : articleList) {
			//store the value
			Integer value = providerNames.get(article.getSource().getName());
			//if value == null => article not in map yet => set value to 0
			if (value == null) {
				value = 0;
			}
			value++;
			//add the article
			providerNames.put(article.getSource().getName(), value);
		}
		int value = 0;
		String mostCommonName = null;
		//checking all entries and saving the "most common one"
		for (String name : providerNames.keySet()) {
			int test = providerNames.get(name);
			if (test > value){
				value = test;
				mostCommonName = name;
			}
		}
		return mostCommonName;
	}

	public String shortestAuthorName(List<Article> articleList){
		int maxLength = 100;
		String shortestName = null;
		//check the whole article list and save the author with the shortest name
		for (Article article  :articleList) {
			if(article.getAuthor() != null) {
				int length = article.getAuthor().length();
				if (length < maxLength) {
					maxLength = length;
					shortestName = article.getAuthor();
				}
			}
		}
		return shortestName;
	}

	//TODO
	public List<Article> sortArticles(List<Article> articleList){
		List<Article> sortedList = new ArrayList<>();
		int listSize = articleList.size();
		Article toMove = null;

		//sorting the list by length
		for (int i = 0; i < listSize; i++) {
			int maxLength = 0;
			for (Article article : articleList) {
				int length = article.getTitle().length();
				if (length > maxLength) {
					toMove = article;
					maxLength = length;
				}
			}
			//remove the item in articleList and add it to sortedList
			sortedList.add(toMove);
			articleList.remove(toMove);
		}

		List<Article> sortedListFinal = new ArrayList<>();
		toMove = sortedList.get(0);

		//now we sort it alphabetically
		for (int j = 0; j < listSize; j++) {
			for (int i = 0; i < sortedList.size(); i++) {
				//if the first element(the longest) has same length => check alphabetically
				if (sortedList.get(0).getTitle().length() <= sortedList.get(i).getTitle().length()) {
					//case 1: the first article is longer than others => toMove = the first
					if (sortedList.get(0).getTitle().compareTo(sortedList.get(i).getTitle()) < 0) {
						toMove = sortedList.get(0);
						//case 2: some other article is alphabetically greater => toMove = the one at (i)
					} else if (sortedList.get(0).getTitle().compareTo(sortedList.get(i).getTitle()) > 0) {
						toMove = sortedList.get(i);
						//case 3: it checks itself => toMove = the first/longest
					} else{
						toMove = sortedList.get(0);
					}
				}
			}
			//remove from sortedList and add it to sortedListFinal
			sortedListFinal.add(toMove);
			sortedList.remove(toMove);
		}
		return sortedListFinal;
	}
}