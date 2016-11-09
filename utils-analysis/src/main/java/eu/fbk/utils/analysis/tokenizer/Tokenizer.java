package eu.fbk.utils.analysis.tokenizer;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: giuliano
 * Date: 3/5/13
 * Time: 11:04 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Tokenizer {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>Tokenizer</code>.
     */
    static Logger logger = Logger.getLogger(Tokenizer.class.getName());

    public abstract Token[] tokenArray(String text);

    public abstract String[] stringArray(String text);

    public abstract String tokenizedString(String text);

    public abstract List<Token> tokenList(String text);

    public abstract List<String> stringList(String text);
}
