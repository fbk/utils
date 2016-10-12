package eu.fbk.utils.analysis.tokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created with IntelliJ IDEA.
 * User: giuliano
 * Date: 3/5/13
 * Time: 11:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class Extent {
	//
	private static final long serialVersionUID = 5024396602591514710L;

	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named <code>Extent</code>.
	 */
	static Logger logger = Logger.getLogger(Extent.class.getName());

	//
	protected int start;

	//
	protected int end;

	/**
	 * Constructs an empty Extent Object.
	 */
	public Extent() {
		this(0, 0);
	} // end constructor

	/**
	 * Constructs a new Extent Object.
	 *
	 * @param start start of extent.
	 * @param end   end of extent.
	 */
	public Extent(int start, int end) {
		this.start = start;
		this.end = end;
	} // end constructor

	/**
	 * Return the start of a extent.
	 *
	 * @return the start of a extent.
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Return the end of a extent.
	 *
	 * @return the end of a extent.
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Returns the length of this extent.
	 *
	 * @return the length of the extent.
	 */
	public int length() {
		return end - start;
	}

	/**
	 * Returns <code>true</code> if the specified extent follows by this extent.
	 * Identical extents are considered to contain each otherPageCounter.
	 *
	 * @param s The extent to compare with this extent.
	 * @return <code>true</code> if the specified extent follows this extent; <code>false</code> otherwise.
	 */
	public boolean follows(Extent s) {
		return (start > s.getEnd());
	} // end follows

	/**
	 * Returns <code>true</code> if the specified extent precedes by this extent.
	 * Identical extents are considered to contain each otherPageCounter.
	 *
	 * @param s The extent to compare with this extent.
	 * @return <code>true</code> if the specified extent precedes this extent; <code>false</code> otherwise.
	 */
	public boolean precedes(Extent s) {
		return (end < s.getStart());
	} // end precedes

	/**
	 * Returns <code>true</code> if the specified extent is contained by this extent.
	 * Identical extents are considered to contain each otherPageCounter.
	 *
	 * @param s The extent to compare with this extent.
	 * @return <code>true</code> if the specified extent is contained by this extent; <code>false</code> otherwise.
	 */
	public boolean contains(Extent s) {
		return (start <= s.getStart() && s.getEnd() <= end);
	}

	/**
	 * Returns <code>true</code> if the specified extent intersects with this extent.
	 *
	 * @param s The extent to compare with this extent.
	 * @return <code>true</code> if the extents overlap; <code>false</code> otherwise.
	 */
	public boolean intersects(Extent s) {
		int sstart = s.getStart();
		//either s's start is in this or this' start is in s
		return (this.contains(s) || s.contains(this) ||
						(start <= sstart && sstart < end || sstart <= start && start < s.getEnd()));
	}

	/**
	 * Returns <code>true</code> is the specified extent crosses this extent.
	 *
	 * @param s The extent to compare with this extent.
	 * @return <code>true</code> if the specified extent overlaps this extent and contains a non-overlapping section; <code>false</code> otherwise.
	 */
	public boolean crosses(Extent s) {
		int sstart = s.getStart();
		//either s's start is in this or this' start is in s
		return (!this.contains(s) && !s.contains(this) &&
						(start <= sstart && sstart < end || sstart <= start && start < s.getEnd()));
	}

	//
	public int compareTo(Extent o) {
		if (end < o.getStart()) {
			return -1;
		}
		else if (start > o.getEnd()) {
			return 1;
		}

		return 0;
	} // end compareTo

	/*public int hashCode()
	 {
	 return((this.getStart << 16) | (0x0000FFFF | this.end));
	 }*/

	//
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (this == o) {
			return true;
		}

		Extent s = (Extent) o;
		return (start == s.getStart() && end == s.getEnd());
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
		return start + "\t" + end;
	}

	//
	public static void main(String args[]) throws Exception {
		String logConfig = System.getProperty("log-config");
		if (logConfig == null) {
			logConfig = "log-config.txt";
		}
		PropertyConfigurator.configure(logConfig);

		if (args.length == 0) {
			logger.info("java org.fbk.cit.hlt.jtext.analysis.Extent (start,end)+");
			System.exit(1);
		}

		Extent[] f = new Extent[args.length];
		for (int i = 0; i < args.length; i++) {
			String[] s = args[i].split(",");
			f[i] = new Extent(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
			logger.info(i + "\t" + f[i]);
		} // end for i

		for (int i = 0; i < args.length; i++) {
			for (int j = i + 1; j < args.length; j++) {
				logger.info(i + ", " + j + "\t" + f[i] + (f[i].intersects(f[j]) ? " intersects " : " doesn't intersect ") + f[j]);
				logger.info(i + ", " + j + "\t" + f[i] + (f[i].contains(f[j]) ? " contains " : " doesn't contain ") + f[j]);
				logger.info(i + ", " + j + "\t" + f[i] + (f[i].crosses(f[j]) ? " crosses " : " doesn't cross ") + f[j]);

			} // end for i

		} // end for i
	} // end main


}
