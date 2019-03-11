// Copyright (C) 2015  Julián Urbano <urbano.julian@gmail.com>
// Distributed under the terms of the MIT License.
package ti;


import org.jsoup.Jsoup;
import org.jsoup.internal.Normalizer;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A processor to extract terms from HTML documents.
 */
public class HtmlProcessor implements DocumentProcessor
{
	/**
	 * Creates a new HTML processor.
	 *
	 * @param pathToStopWords the path to the file with stopwords, or {@code null} if stopwords are not filtered.
	 * @throws IOException if an error occurs while reading stopwords.
	 */
	private List<String> StopWordsList;

	public HtmlProcessor(File pathToStopWords) throws IOException
	{
		BufferedReader abc = new BufferedReader(new FileReader(pathToStopWords));
		StopWordsList = new ArrayList<String>();

		String line = abc.readLine();

		while(line != null)
		{
			StopWordsList.add(line);
			line = abc.readLine();
		}
		abc.close();
	}

	/**
	 * {@inheritDoc}
	 */
	public Tuple<String, String> parse(String html)
	{
		// P2
		// Parse document
		try
		{
			String title, body;
			Document doc = Jsoup.parse(html); //We create a Document object which will allow us to parse the html due to the library JSOUP
			body = doc.body().text(); //Transform the body of html to text
			title = doc.title(); //Get the title of the html
			if (body != null && title != null) {
				return new Tuple<>(title , body); //return a Tuple with the title and the body (TITLE, BODY)
			}
			else if(body != null)
			{
				return new Tuple<>("",body);
			}
			else
			{
				return new Tuple<>(title, "");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Process the given text (tokenize, normalize, filter stopwords and stemize) and return the list of terms to index.
	 *
	 * @param text the text to process.
	 * @return the list of index terms.
	 */
	public ArrayList<String> processText(String text)
	{
		ArrayList<String> terms = new ArrayList<>();
		ArrayList<String> terms_v2 = new ArrayList<>();
		// P2

		terms = tokenize(text); //We tokenize all the text recieved
		if (terms == null) System.exit(0);
		else
		{
			System.out.println("Text is " + text);
			System.out.println("List of terms is: " + terms);
		}
		//Normalize
		int i = 0;
		for (String s : terms)
		{
			System.out.println("String is " + s);
			//Stopwords
			if (!isStopWord(s)) //Is not stop word
			{
				String norm_word = normalize(s); //Normalized word is: norm_word
				String stemmed_word = stem(norm_word); //Stemmed word: stemmed_word
				terms_v2.add(stemmed_word); //We add the word to the new list once is normalized and stemmed
			}
			i++;
		}

		return terms_v2; //We return the new list of words
	}

	/**
	 * Tokenize the given text.
	 *
	 * @param text the text to tokenize.
	 * @return the list of tokens.
	 */
	protected ArrayList<String> tokenize(String text)
	{
		ArrayList<String> tokens = new ArrayList<>(); //Create array of words
		Pattern p = Pattern.compile("[\\w']+"); //We create a pattern using a regular expression
		Matcher m = p.matcher(text); //We create a matcher of the text using the pattern

		while ( m.find() ) { //We iterate finding all the words
			tokens.add(text.substring(m.start(), m.end())); //We add each word in our array list
		}

		return tokens; //We return our array of words
	}

	/**
	 * Normalize the given term.
	 *
	 * @param text the term to normalize.
	 * @return the normalized term.
	 */
	protected String normalize(String text)
	{
		return Normalizer.normalize(text); //Convert the word into a lowercase word
	}

	/**
	 * Checks whether the given term is a stopword.
	 *
	 * @param term the term to check.
	 * @return {@code true} if the term is a stopword and {@code false} otherwise.
	 */
	protected boolean isStopWord(String term)
	{
		return StopWordsList.contains(term); //We look if this word is an stop word or not (true or false)
	}

	/**
	 * Stem the given term.
	 *
	 * @param term the term to stem.
	 * @return the stem of the term.
	 */
	protected String stem(String term)
	{
		Stemmer stemmer = new Stemmer(); //We create a new stemmer
		System.out.println("Word before char array: " + term);

		for (int i = 0; i<term.length(); i++) {
			stemmer.add(term.charAt(i)); //We add all the characters of the word into the stemmer
			System.out.println("Letter added to stem: " + term.charAt(i));
		}
		stemmer.stem(); //We stem the word
		char[] stemmed_word = stemmer.getResultBuffer(); //We get the result of the stemmed word but with null characters

		StringBuilder builder = new StringBuilder();
		String word = "";

		for (int i = 0; i<stemmed_word.length; i++)
		{
			if (stemmed_word[i] != '\0') //We check if the character is null -> we don't put it in the buffer
			{
				builder.append(stemmed_word[i]); //If its not null we put it in the buffer
			}
		}
		word = builder.toString(); //We store our word without null characters into the variable word
		return word; //Finnally we return the word stemmed
	}
}
