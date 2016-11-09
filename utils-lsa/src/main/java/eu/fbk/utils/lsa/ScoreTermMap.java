/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.lsa;

import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.*;

/**
 * to do
 *
 * @version %I%, %G%
 * @author Claudio Giuliano
 * @since 1.0
 */
public class ScoreTermMap {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>ScoreTermMap</code>.
     */
    static Logger logger = Logger.getLogger(ScoreTermMap.class.getName());

    /**
     * to do.
     */
    private SortedMap<Double, List<String>> map;

    //
    private String term;

    //
    private int size;

    //
    private DecimalFormat formatter;

    /**
     * Constructs a <code>ScoreTermMap</code> object.
     */
    public ScoreTermMap(String term, int size) {
        formatter = new DecimalFormat("0.000000");

        this.term = term;
        this.size = size;

        map = new TreeMap<Double, List<String>>(new Comparator<Double>() {

            public int compare(Double s1, Double s2) {
                if (s1.doubleValue() == s2.doubleValue()) {
                    return 0;
                } else if (s1.doubleValue() < s2.doubleValue()) {
                    return 1;
                }
                return -1;
            }

        }

        );
    } // end constructor

    //
    public String term() {
        return term;
    } // end term

    /**
     * Add a token to the index ScoreTermMap
     *
     * @param token the token.
     */
    public void put(double score, String term) {
        //logger.debug("put: " + score + " " + term);
        Double key = new Double(score);
        List<String> list = map.get(key);

        if (list == null) {
            list = new ArrayList<String>();
            list.add(term);
            map.put(key, list);
        } else {
            list.add(term);
        }
    } // end put

    //
    public int size() {
        return map.size();
    } // end size

    //
    public int maxSize() {
        return size;
    } // end maxSize

    //
    public Set<Double> scoreSet() {
        return map.keySet();
    } // end scores

    //
    //
    public List<String> termList(double score) {
        return map.get(score);
    } // end termList

    //
    public List<String> termList() {
        //logger.debug("toString: " + term + "\t" + map.size() + "\t" + size);
        List<String> list = new ArrayList<String>();

        Iterator it = map.keySet().iterator();
        //logger.debug("set: " + map.keySet());
        while (it.hasNext()) {
            Double score = (Double) it.next();

            list.addAll(map.get(score));

        } // end while

        return list;
    } // end toString

    /**
     * Returns a <code>String</code> object representing this
     * <code>Word</code>.
     *
     * @return a string representation of this object.
     */
    public String toString() {
        //logger.debug("toString: " + term + "\t" + map.size() + "\t" + size);
        StringBuffer sb = new StringBuffer();

        sb.append(term);
        sb.append("\n");

        int count = 0;
        int tcount = 0;
        int ocount = 0;
        int lcount = 0;
        Iterator it = map.keySet().iterator();
        //logger.debug("set: " + map.keySet());
        while (it.hasNext()) {
            Double score = (Double) it.next();
            //logger.debug("key: " + count + "\t" + score);

            sb.append(count);
            sb.append("\t");
            sb.append(formatter.format(score));
            sb.append("\t");

            List<String> list = map.get(score);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    if (i != 0) {
                        sb.append(", ");
                    }

                    String t = list.get(i);
                    sb.append(t);
                    //logger.debug(tcount + "\t" + t);
                    tcount++;
                }
                sb.append("\n");
                count++;

            } // end if

            if (count > size) {
                break;
            }
        }

        return sb.toString();
    } // end toString

    /**
     * Returns a <code>String</code> object representing this
     * <code>Word</code>.
     *
     * @return a string representation of this object.
     */
    public String toString2() {
        logger.info("toString2: " + term + "\t" + map.size() + "\t" + size);
        StringBuffer sb = new StringBuffer();

        int count = 1;
        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            Double score = (Double) it.next();
            sb.append(count);
            sb.append("\t");

            List<String> list = map.get(score);
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i));
                sb.append("\t");
                sb.append(score);
                count++;

                if (count > size) {
                    sb.append("\n");
                    return sb.toString();
                }

            } // end for i
            sb.append("\n");
        }

        return sb.toString();
    } // end toString2

    /**
     * Returns a <code>String</code> object representing this
     * <code>Word</code>.
     *
     * @return a string representation of this object.
     */
    public String toString1() {
        logger.debug("toString: " + term + "\t" + map.size());
        StringBuffer sb = new StringBuffer();

        sb.append(term);
        sb.append("\n");

        int count = 0;
        int tcount = 0;
        int ocount = 0;
        int lcount = 0;
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            logger.debug(count + "\t" + me.getKey());

            sb.append(formatter.format(me.getKey()));
            sb.append("\t");

            List tlist = (List) me.getValue();
            for (int i = 0; i < tlist.size(); i++) {
                if (i != 0) {
                    sb.append(", ");
                }

                String t = (String) tlist.get(i);
                sb.append(t);
                logger.debug(tcount + "\t" + t);
                tcount++;
            }
            sb.append("\n");
            count++;

            if (count > size) {
                break;
            }
        }

        return sb.toString();
    } // end toString1

    //
    public Set entrySet() {
        return map.entrySet();
    } // end entrySet

    void x() {
        Comparator<String> comparator = new Comparator<String>() {

            public int compare(String s1, String s2) {
                String[] strings1 = s1.split("\\s");
                String[] strings2 = s2.split("\\s");
                return strings1[strings1.length - 1]
                        .compareTo(strings2[strings2.length - 1]);
            }
        };

        Comparator<Double> comparator1 = new Comparator<Double>() {

            public int compare(Double s1, Double s2) {
                //String[] strings1 = s1.split("\\s");
                //String[] strings2 = s2.split("\\s");
                //return strings1[strings1.length - 1]
                //.compareTo(strings2[strings2.length - 1]);
                return (int) s1.doubleValue();
            }
        };

    }

    //
    private class ScoreComparator implements Comparator
            //public class ScoreComparator<Double> implements Comparator<Double>
    {
        //
        /*public int compare(Double s1, Double s2)
		{
			//Double d1 = (Double) o1;
			//Double d2 = (Double) o2;
			//logger.info(((Double) o1).doubleValue());
			//double diff = d1.doubleValue() - d2.doubleValue();
			if (s1.doubleValue() == s2.doubleValue())
			{
				return 0;
			}
			else if (s1.doubleValue() < s2.doubleValue())
			{
				return 1;
			}
			
			
					
			return -1;
			//logger.debug(d1 + " - " + d2 + " = " + (int) (d1.doubleValue() - d2.doubleValue()));
			
		} // end compare*/

        //
        public int compare(Object o1, Object o2) {
            Double d1 = (Double) o1;
            Double d2 = (Double) o2;
            double diff = d1.doubleValue() - d2.doubleValue();

            if (diff == 0) {
                return 0;
            } else if (diff < 0) {
                return 1;
            }

            return -1;
            //logger.debug(d1 + " - " + d2 + " = " + (int) (d1.doubleValue() - d2.doubleValue()));

        } // end compare

        //
        public boolean equals(Object obj) {
            return this == obj;
        } // end equals
    } // end class ScoreComparator

} // end class ScoreTermMap