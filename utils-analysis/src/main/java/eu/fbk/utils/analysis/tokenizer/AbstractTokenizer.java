package eu.fbk.utils.analysis.tokenizer;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: giuliano
 * Date: 3/5/13
 * Time: 11:00 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractTokenizer implements Tokenizer {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>AbstractTokenizer</code>.
     */
    static Logger logger = Logger.getLogger(AbstractTokenizer.class.getName());

    public String tokenizedString(String text) {
        //logger.debug("tokenizing " + text.length() + "...");
        StringBuilder sb = new StringBuilder();
        String[] s = stringArray(text);
        if (s.length > 0) {
            sb.append(s[0]);
        }
        for (int i = 1; i < s.length; i++) {
            sb.append(CharacterTable.SPACE);
            sb.append(s[i]);
        }
        return sb.toString();
    }

    protected boolean isSeparatorChar(char ch) {
        if (ch == CharacterTable.SPACE) {
            return true;
        } else if (ch == CharacterTable.CARRIADGE_RETURN) {
            return true;
        } else if (ch == CharacterTable.LINE_FEED) {
            return true;
        } else if (ch == CharacterTable.HORIZONTAL_TABULATION) {
            return true;
        } else if (ch == CharacterTable.FORM_FEED) {
            return true;
        }

        return false;
    }

    protected static String read(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line = null;
        // run the rest of the filePageCounter
        while ((line = lnr.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        } // end while
        lnr.close();
        return sb.toString();
    }
}
