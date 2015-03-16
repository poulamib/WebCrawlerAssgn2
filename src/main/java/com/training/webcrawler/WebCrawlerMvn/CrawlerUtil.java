package com.training.webcrawler.WebCrawlerMvn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlerUtil
{
	/**
	 * the static variable dirpath is to maintain the location where the email's
	 * are to be downloaded
	 */
	private static String dirPath = new String();

	/**
	 * the static variable fileInc is a sequence that has been used in file
	 * naming
	 */
	private static int fileInc = 0;

	private static String folderName = "";

	private static String folderDownloaded = "";

	public static String getDirPath()
	{
		return dirPath;
	}

	public static void setDirPath(String dirPath)
	{
		CrawlerUtil.dirPath = dirPath;
	}

	public static int getFileInc()
	{
		return fileInc;
	}

	public static void setFileInc(int fileInc)
	{
		CrawlerUtil.fileInc = fileInc;
	}

	public static String getFolderName()
	{
		return folderName;
	}

	public static void setFolderName(String folderName)
	{
		CrawlerUtil.folderName = folderName;
	}

	public static String getFolderDownloaded()
	{
		return folderDownloaded;
	}

	public static void setFolderDownloaded(String folderDownloaded)
	{
		CrawlerUtil.folderDownloaded = folderDownloaded;
	}

	/**
	 * To create a directory to store the emails
	 * 
	 * @param url
	 * @return
	 */
	public static String createDirectory(String url)
	{
		setFolderName(getDirectoryName(url));
		new File(getDirPath() + "/" + folderName).mkdir();
		return getDirPath() + "/" + folderName;

	}

	/**
	 * To get the directory name
	 * 
	 * @param url
	 * @return
	 */
	public static String getDirectoryName(String url)
	{
		int firstIndex = url.indexOf("maven-users/") + "maven-users/".length();
		int lastIndex = url.indexOf(".mbox");
		return url.substring(firstIndex, lastIndex);
	}

	/**
	 * A method to delete a directory recursively
	 * 
	 * @param directory
	 * @return
	 */
	public static boolean deleteDirectory(File directory)
	{
		if (directory.exists())
		{
			File[] files = directory.listFiles();
			if (null != files)
			{
				for (int i = 0; i < files.length; i++)
				{
					if (files[i].isDirectory())
					{
						deleteDirectory(files[i]);
					}
					else
					{
						files[i].delete();
					}
				}
			}
		}
		return (directory.delete());
	}

	/**
	 * A method to create a new directory if the prev run of the web crawler
	 * completed successfully
	 * 
	 * @return
	 * @throws IOException
	 */
	public static void setNewDir(String path) throws IOException
	{
		CrawlerUtil.setDirPath(path);
		if (checkIfPrevSucc().length() == 0)
		{
			CrawlerUtil.deleteDirectory(new File(path));
			new File(path).mkdir();
		}
	}

	/**
	 * A method to check if the web crawler completed successfully in the prev
	 * run
	 * 
	 * @return
	 * @throws IOException
	 */

	public static String checkIfPrevSucc() throws IOException
	{
		BufferedReader br = null;

		try
		{

			String sCurrentLine;

			br = new BufferedReader(
					new FileReader(getDirPath() + "/status.txt"));

			while ((sCurrentLine = br.readLine()) != null)
			{
				if (sCurrentLine.contains("Timed out"))
				{
					setFolderDownloaded(sCurrentLine.substring(
							sCurrentLine.length() - 9,
							sCurrentLine.length() - 3));
					setFileInc(Integer.parseInt(sCurrentLine.substring(
							sCurrentLine.length() - 2, sCurrentLine.length())));
					return getFolderDownloaded();
				}
			}

		}
		catch (IOException e)
		{
			if (e.getMessage().contains("No such file or directory"))
				return "";
			throw e;
		}
		finally
		{
			if (br != null) br.close();
		}

		return "";
	}

	/**
	 * This method checks if the link has already been parsed.
	 * 
	 * @param link
	 *            This parameter will be checked against the list of links to
	 *            see if it already exists in the links
	 * @param links
	 *            This maintains the list of links that has already been parsed
	 *            by the web crawler
	 * @return
	 */
	public static int checkIfExists(String link, LinkedList<String> links)
	{
		int index = 0;
		index = links.indexOf(link);
		return index;
	}

	/**
	 * This method will parse the web page based on the selector specified in
	 * docSelector
	 * 
	 * @param doc
	 *            The web page document that needs to be parsed
	 * @param docSelector
	 *            The selector on the basis of which the web page will be parsed
	 * @param depth
	 *            This parameter determines how deep the crawler has gone into
	 *            the root web page
	 * @throws IOException
	 */

	public static void parseUrl(Document doc, String docSelector, int depth)
			throws IOException
	{
		Elements links = new Elements();
		String[] selectors = docSelector.split("~");
		for (String selector : selectors)
		{
			links = links.size() == 0 ? doc.select(selector) : links
					.select(selector);
		}

		for (Element link : links)
		{

			if (depth == 0
					&& (getDirectoryName(link.attr("abs:href")).compareTo(
							getFolderDownloaded()) < 0 || getFolderDownloaded()
							.length() == 0))
			{
				CrawlerUtil.createDirectory(link.attr("abs:href"));
				CrawlerUtil.setFileInc(0); // if new month create a folder for
				URLCrawler.processPage(link.attr("abs:href"), depth + 1);
			}
			else if (depth > 0)
			{
				URLCrawler.processPage(link.attr("abs:href"), depth + 1);
			}

		}
	}

	/**
	 * This method is used for parsing the web page that do not contain all the
	 * links in a single page and also checks if that web page has been parsed
	 * before
	 * 
	 * @param doc
	 *            The web page document that needs to be parsed
	 * @param depth
	 *            The selector on the basis of which the web page will be parsed
	 * @throws IOException
	 */
	public static void doPagination(Document doc, String docSelector, int depth)
			throws IOException
	{
		LinkedList<String> absLinksList = new LinkedList<String>();
		Elements links = doc.select(docSelector);
		for (Element link : links)
		{
			if (CrawlerUtil.checkIfExists(link.attr("abs:href"), absLinksList) == -1)
			{
				URLCrawler.processPage(link.attr("abs:href"), depth + 1);
				absLinksList.add(link.attr("abs:href"));
			}

		}
	}

	/**
	 * A method to save data in a file
	 * 
	 * @param str
	 *            : The data to be saved
	 * @param src
	 *            : whether the data to be saved is an email or status
	 * @throws IOException
	 */
	public static void storeDataInFile(String str, String src)
			throws IOException
	{
		FileWriter fw;
		BufferedWriter bw;
		File file = new File(".");
		file = createFile(src);

		if (!file.exists())
		{
			file.createNewFile();
		}

		fw = new FileWriter(file.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		try
		{
			bw.append(str);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			bw.close();
			fw.close();
		}

	}

	/**
	 * 
	 * @param src
	 * @return
	 */
	public static File createFile(String src)
	{
		File file = new File(".");
		if ("emails".equalsIgnoreCase(src))
		{
			file = new File(CrawlerUtil.getDirPath() + "/" + getFolderName()
					+ "/email-" + (fileInc++) + ".txt");
		}
		else if ("statusDir".equalsIgnoreCase(src))
		{
			file = new File(CrawlerUtil.getDirPath() + "/" + "status.txt");
		}
		else if ("statusFile".equalsIgnoreCase(src))
		{
			file = new File(CrawlerUtil.getDirPath() + "/" + getFolderName()
					+ "/" + "status.txt");
		}
		return file;
	}

	/**
	 * 
	 * @param doc
	 * @throws IOException
	 */
	public static void downloadEmails(Document doc) throws IOException
	{
		Elements links = doc.select("pre");
		for (Element link : links)
		{
			storeDataInFile(link.text(), "emails");
		}
	}

}
