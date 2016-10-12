/*
 * Copyright (2011) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.analysis.stemmer.ext;

import eu.fbk.utils.analysis.stemmer.AbstractStemmer;
import eu.fbk.utils.analysis.stemmer.Stemmer;
import eu.fbk.utils.analysis.stemmer.StemmerNotFoundException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.tartarus.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

/**
 * TO DO
 *
 * @author 	Claudio Giuliano
 * @version %I%, %G%
 * @since		1.0
 */
public class SnowballWrapper extends AbstractStemmer implements Stemmer
{
	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named <code>SnowballWrapper</code>.
	 */
	static Logger logger = Logger.getLogger(SnowballWrapper.class.getName()); 

	//
	SnowballStemmer stemmer;
	
	//
	private SnowballWrapper(SnowballStemmer stemmer)
	{
		this.stemmer = stemmer;
	} // end constructor

	//
	public static SnowballWrapper getInstance(String lang) throws StemmerNotFoundException
	{
		SnowballStemmer snowballStemmer = null;
		try
		{
			Class stemClass = Class.forName("org.tartarus.snowball.ext." + lang.toLowerCase() + "Stemmer");
			logger.info("stemmer class " + stemClass);
			snowballStemmer = (SnowballStemmer) stemClass.newInstance();
		}
		catch (Exception e)
		{
			logger.error(e);
			throw new StemmerNotFoundException(lang);
		}
				
		return new SnowballWrapper(snowballStemmer);
	} // end getInstance

	
	//
	public String stem(String s)
	{
		if (stemmer == null)
		{
			return s;
		}
		
		stemmer.setCurrent(s);
		stemmer.stem();
		return stemmer.getCurrent();
	} // end stem

	//
	public String toString()
	{
		return "Snowball Stemmer";
	} // end toString

	//
	public void interactive() throws Exception
	{
		DecimalFormat df = new DecimalFormat("000,000,000.#");
		InputStreamReader reader = null;
		BufferedReader myInput = null;
		long begin = 0, end = 0;
		while (true)
		{
			System.out.println("\nPlease write a query and type <return> to continue (CTRL C to exit):");
			
			reader = new InputStreamReader(System.in);
			myInput = new BufferedReader(reader);
			//String query = myInput.readLine().toString().replace(' ', '_');
			String query = myInput.readLine().toString();
			String stem = null;
			begin = System.nanoTime();
			stem = stem(query);			
			end = System.nanoTime();
			logger.info(query + "\t" + stem + "\t" + df.format(end - begin) + " ns");
			begin = System.nanoTime();
			
		} // end while
	} // end interactive
	
	//
	public static void main(String args[]) throws Exception
	{
		String logConfig = System.getProperty("log-config");
		if (logConfig == null)
			logConfig = "log-config.txt";
		PropertyConfigurator.configure(logConfig);
		
		SnowballWrapper myStemmer = SnowballWrapper.getInstance(args[0]);
		//PorterStemmer porterStemmer = PorterStemmer.getInstance();
		
		if (args.length == 1)
		{
			//System.out.println("java SnowballWrapper term+");
			myStemmer.interactive();
			
		}
		
/*		for (int i=1;i<args.length;i++)
		{
			String stem = stemmer.stem(args[i]);
			logger.info(args[i] + "\t" + stem);
		}
	*/
	/*
		StringBuilder sb = new StringBuilder();
		LineNumberReader lr = new LineNumberReader(new FileReader(args[1])); 
		String line = null;
		while ((line = lr.readLine()) != null)
		{
			sb.append(line);
			sb.append("\n");
		} // end while
		System.out.println("token\tporter\tsnowball\tYES/NO");
		String in = sb.toString();
		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(in);
		int start = boundary.first();
		for (int end = boundary.next(); end != BreakIterator.DONE;start = end, end = boundary.next())
		{
			String token = in.substring(start,end);
			if (token.matches("\\w+"))
			{
				String ps = porterStemmer.stem(token);
				String ms = myStemmer.stem(token);
				
				System.out.println(token + "\t" + ps + "\t" + ms + "\t" + (ps.equals(ms) ? "YES" : "NO"));
			}
		}
	*/
		
	} // end main
	
} // end class SnowballWrapper