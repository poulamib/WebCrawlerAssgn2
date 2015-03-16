package com.training.webcrawler.WebCrawlerMvn;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class URLCrawler
{
	/**
	 * This is the main method which will start the web crawler to download
	 * emails from 2015 and save them in a directory where email's from each
	 * month will be in a separate folder The main program deletes the
	 * EmailDirectory if it exists and creates a new one with the same name
	 * where the emails will be downloaded.
	 * 
	 * It will take the URL of the web page to crawl and the path where the
	 * emails are to be downloaded as arguements
	 * 
	 * */
	public static void main(String[] args) throws IOException
	{
		System.out.println("******Web Crawler Started*******");
		processPageWrapper(args[0], 0, args[1]);
		System.out.println("*******Web Crawler Completed Successfully*******");
	}

	public static void processPageWrapper(String url, int depth, String path)
			throws IOException
	{

		CrawlerUtil.setNewDir(path);
		try
		{
			processPage(url, depth);
			CrawlerUtil.storeDataInFile("Success", "statusDir");
		}
		catch (SocketTimeoutException e)
		{
			CrawlerUtil.storeDataInFile(
					"Timed out " + CrawlerUtil.getFolderName() + " "
							+ CrawlerUtil.getFileInc(), "statusDir");
		}

	}

	/**
	 * This method is used for crawling the web page and parsing the urls
	 * 
	 * @param url
	 *            - This is the web page that will be parsed to return the
	 *            required links
	 * @param depth
	 *            - This parameter determines how deep the crawler has gone into
	 *            the root web page
	 */
	public static void processPage(String url, int depth) throws IOException
	{
		Document doc = null;
		try
		{
			doc = Jsoup.connect(url).get();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
			throw e1;
		}
		if (depth == 0)
		{
			CrawlerUtil.parseUrl(doc, "a[href^=2015]~a[href*=thread]", depth);
		}
		else if (depth == 1)
		{
			processPage(url, 2);
			CrawlerUtil.doPagination(doc, "a[href*=thread?]", depth);
			CrawlerUtil.storeDataInFile("Success", "statusFile");
		}
		else if (depth == 2)
		{
			CrawlerUtil.parseUrl(doc, "a[href*=@]", depth);
		}
		else if (depth == 3)
		{
			CrawlerUtil.downloadEmails(doc);
		}
	}

}
