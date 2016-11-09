package eu.fbk.utils.analysis.tokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created with IntelliJ IDEA.
 * User: giuliano
 * Date: 3/5/13
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class Token extends Extent {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>Token</code>.
     */
    static Logger logger = Logger.getLogger(Token.class.getName());

    //
    protected String form;

    //
    public Token(int start, int end, String form) {
        super(start, end);
        this.form = form;
    } // constructor

    public void setForm(String form) {
        this.form = form;
    }

    public String getForm() {
        return form;
    }

    //
    public boolean equals(Object obj) {
        if (obj instanceof Token) {
            return equals((Token) obj);
        }

        return false;
    } // end equals

	/*
    public String toString()
	{
		StringWriter w = new StringWriter();
		try
		{
			JsonFactory f = new JsonFactory();
			JsonGenerator g = f.createJsonGenerator(w);
			g.writeStartObject();
			g.writeStringField("form", form);
			g.writeObjectFieldStart("span");
			g.writeNumberField("start", getStart());
			g.writeNumberField("end", getEnd());
			g.writeEndObject();
			g.writeEndObject();
			g.close();

		}
		catch (IOException e)
		{
			logger.error(e);
		}
		return w.toString();
	} // end toString
  */

    @Override
    public String toString() {
        return form + "\t" + super.toString();
    }

    public static void main(String args[]) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);

        if (args.length == 0) {
            logger.info("java com.machinelinking.annotation.tok.Token");
            System.exit(1);
        }

    }

}
