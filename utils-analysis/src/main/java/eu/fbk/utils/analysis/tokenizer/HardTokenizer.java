package eu.fbk.utils.analysis.tokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: giuliano
 * Date: 3/5/13
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class HardTokenizer extends AbstractTokenizer implements Tokenizer {
	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named <code>HardTokenizer</code>.
	 */
	static Logger logger = Logger.getLogger(HardTokenizer.class.getName());

	private static HardTokenizer ourInstance = null;

	public static synchronized HardTokenizer getInstance() {
		if (ourInstance == null) {
			ourInstance = new HardTokenizer();
		}
		return ourInstance;
	}

	public String[] stringArray(String text) {
		List<String> list=stringList(text);
		return list.toArray(new String[list.size()]);
	}

	public List<String> stringList(String text) {
		if (text.length() == 0) {
			return new ArrayList<String>();
		}
		List<String> list = new ArrayList<String>();
		char currentChar = text.charAt(0);
		char previousChar = currentChar;
		int start = 0;
		boolean isCurrentCharLetterOrDigit;
		boolean isPreviousCharLetterOrDigit;
		if (!Character.isLetterOrDigit(currentChar)) {
			if (!isSeparatorChar(currentChar)) {
				list.add(new String(new char[]{currentChar}));
			}
		}

		//logger.debug("0\t" + (int) previousChar + "\t<" + previousChar + ">");
		for (int i = 1; i < text.length(); i++) {
			currentChar = text.charAt(i);
			isCurrentCharLetterOrDigit = Character.isLetterOrDigit(currentChar);
			isPreviousCharLetterOrDigit = Character.isLetterOrDigit(previousChar);
			//logger.debug(i + (int) currentChar + "\t<" + currentChar + ">");
			if (isCurrentCharLetterOrDigit) {
				if (!isPreviousCharLetterOrDigit) {
					start = i;
				}
			}
			else {
				if (isPreviousCharLetterOrDigit) {
					// word o number
					list.add(text.substring(start, i));
					if (!isSeparatorChar(currentChar)) {
						list.add(new String(new char[]{currentChar}));
					}
				}
				else {
					//otherPageCounter
					if (!isSeparatorChar(currentChar)) {
						list.add(new String(new char[]{currentChar}));
					}
				}
			}
			previousChar = currentChar;
		}
		if (Character.isLetterOrDigit(previousChar)) {
			list.add(text.substring(start, text.length()));
		}

		return list;
	}

	public List<Token> tokenList(String text)
	{
		if (text.length() == 0) {
			return new ArrayList<>();
		}
		List<Token> list = new ArrayList<Token>();
		char currentChar = text.charAt(0);
		char previousChar = currentChar;
		int start = 0;
		boolean isCurrentCharLetterOrDigit;
		boolean isPreviousCharLetterOrDigit;
		Token token;

		if (!Character.isLetterOrDigit(currentChar)) {
			if (!isSeparatorChar(currentChar)) {
				list.add(new Token(0, 1, new String(new char[]{currentChar})));
			}
		}

		//logger.debug("0\t" + (int) previousChar + "\t<" + previousChar + ">");
		for (int i = 1; i < text.length(); i++) {
			currentChar = text.charAt(i);
			isCurrentCharLetterOrDigit = Character.isLetterOrDigit(currentChar);
			isPreviousCharLetterOrDigit = Character.isLetterOrDigit(previousChar);
			//logger.debug(i + (int) currentChar + "\t<" + currentChar + ">");
			if (isCurrentCharLetterOrDigit) {
				if (!isPreviousCharLetterOrDigit) {
					start = i;
				}
			}
			else {
				if (isPreviousCharLetterOrDigit) {
					// word o number

					list.add(new Token(start, i, text.substring(start, i)));
					if (!isSeparatorChar(currentChar)) {
						// otherPageCounter
						list.add(new Token(i, i + 1, new String(new char[]{currentChar})));
					}
				}
				else {
					//otherPageCounter
					if (!isSeparatorChar(currentChar)) {
						list.add(new Token(i, i + 1, new String(new char[]{currentChar})));
					}
				}
			}
			previousChar = currentChar;
		}
		if (Character.isLetterOrDigit(previousChar)) {
			list.add(new Token(start, text.length(), text.substring(start, text.length())));
		}

		return list;
	}

	public Token[] tokenArray(String text)
	{
		List<Token> list=tokenList(text);
		return list.toArray(new Token[list.size()]);
	}

	public String toString(){
		return "HardTokenizer";
	}

	public static void main(String argv[]) throws IOException {
		String logConfig = System.getProperty("log-config");
		if (logConfig == null) {
			logConfig = "configuration/log-config.txt";
		}

		PropertyConfigurator.configure(logConfig);
		// java -cp dist/thewikimachine.jar org.fbk.cit.hlt.thewikimachine.analysis.HardTokenizer

		File f = new File(argv[0]);
		String s = null;
		if (f.exists()) {
			s = read(new File(argv[0]));
		}
		else {
			s = argv[0];
		}
		HardTokenizer hardTokenizer = new HardTokenizer();
		String t = hardTokenizer.tokenizedString(s);
		logger.info(t);
	}

}
