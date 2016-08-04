/*
 * Copyright (2013) Fondazione Bruno Kessler (http://www.fbk.eu/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.fbk.utils.twm;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

/**
 * Created with IntelliJ IDEA. User: giuliano Date: 1/22/13 Time: 6:11 PM To change this template
 * use File | Settings | File Templates.
 */
public abstract class FreqSetSearcher extends AbstractSearcher {

    /**
     * Define a static logger variable so that it references the Logger instance named
     * <code>FreqSetSearcher</code>.
     */
    static Logger logger = Logger.getLogger(FreqSetSearcher.class.getName());

    public static final int DEFAULT_MIN_FREQ = 1000;

    public static final boolean DEFAULT_THREAD_SAFE = false;

    public final static String DEFAULT_KEY_FIELD_NAME = "KEY";

    public final static String DEFAULT_VALUE_FIELD_NAME = "VALUE";

    public static final String HORIZONTAL_TABULATION = "\t";

    protected static DecimalFormat df = new DecimalFormat("###,###,###,###");

    private static DecimalFormat tf = new DecimalFormat("000,000,000.#");

    private static Pattern tabPattern = Pattern.compile(HORIZONTAL_TABULATION);

    protected boolean threadSafe;

    private Map<String, Entry[]> cache;

    private Term keyTerm;

    protected String keyFieldName;

    protected String valueFieldName;

    private int cutoff;

    protected FreqSetSearcher(final String indexName) throws IOException {
        this(indexName, DEFAULT_KEY_FIELD_NAME, DEFAULT_VALUE_FIELD_NAME, DEFAULT_THREAD_SAFE);
    }

    protected FreqSetSearcher(final String indexName, final String keyFieldName,
            final String valueFieldName) throws IOException {
        this(indexName, keyFieldName, valueFieldName, false);
    }

    protected FreqSetSearcher(final String indexName, final String keyFieldName,
            final String valueFieldName, final boolean threadSafe) throws IOException {
        super(indexName);
        this.threadSafe = threadSafe;
        this.keyFieldName = keyFieldName;
        this.valueFieldName = valueFieldName;
        logger.debug(keyFieldName + "\t" + valueFieldName);

        this.keyTerm = new Term(keyFieldName, "");
        logger.debug(this.keyTerm);
    }

    public int getCutoff() {
        return this.cutoff;
    }

    public void setCutoff(final int cutoff) {
        this.cutoff = cutoff;
    }

    public void setKeyFieldName(final String keyFieldName) {
        this.keyTerm = new Term(keyFieldName, "");
        this.keyFieldName = keyFieldName;
    }

    public String getKeyFieldName() {
        return this.keyFieldName;
    }

    public String getValueFieldName() {
        return this.valueFieldName;
    }

    public void setValueFieldName(final String valueFieldName) {
        this.valueFieldName = valueFieldName;
    }

    public void loadCache(final String name) throws IOException {
        loadCache(new File(name));
    }

    public void loadCache(final String name, final int minFreq) throws IOException {
        loadCache(new File(name), minFreq);
    }

    public void loadCache(final File f) throws IOException {
        loadCache(f, DEFAULT_MIN_FREQ);
    }

    public void loadCache(final File f, final int minFreq) throws IOException {
        logger.info("loading cache from " + f + " (freq>" + minFreq + ")...");
        final long begin = System.nanoTime();

        if (this.threadSafe) {
            logger.info(this.getClass().getName() + "'s cache is thread safe");
            this.cache = Collections.synchronizedMap(new HashMap<String, Entry[]>());
        } else {
            logger.warn(this.getClass().getName() + "'s cache isn't thread safe");
            this.cache = new HashMap<String, Entry[]>();
        }

        final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(
                new FileInputStream(f), "UTF-8"));
        String line;
        int i = 1;
        String[] t;
        int freq = 0;
        Entry[] result;
        Document doc;
        TermDocs termDocs;
        while ((line = lnr.readLine()) != null) {
            t = tabPattern.split(line);
            if (t.length == 2) {
                freq = Integer.parseInt(t[0]);
                if (freq < minFreq) {
                    break;
                }
                termDocs = indexReader.termDocs(this.keyTerm.createTerm(t[1]));
                if (termDocs.next()) {
                    doc = indexReader.document(termDocs.doc());
                    result = fromByte(doc.getBinaryValue(this.valueFieldName));
                    this.cache.put(t[1], result);
                }
            }
            if (i % notificationPoint == 0) {
                // System.out.print(CharacterTable.FULL_STOP);
                logger.debug(i + " keys read (" + this.cache.size() + ") " + new Date());
            }
            i++;
        }
        System.out.print(LINE_FEED);
        lnr.close();
        final long end = System.nanoTime();
        logger.info(df.format(this.cache.size()) + " (" + df.format(indexReader.numDocs())
                + ") keys cached in " + tf.format(end - begin) + " ns");
    }

    public Entry[] search(final String key) {
        // logger.debug("searching " + key + "...");
        // long begin = 0, end = 0;

        // begin = System.nanoTime();
        Entry[] result = null;
        if (this.cache != null) {
            result = this.cache.get(key);
        }
        // end = System.nanoTime();

        if (result != null) {
            // logger.debug("found in cache in " + tf.format(end - begin) + " ns");
            return result;
        }

        try {
            // begin = System.nanoTime();
            final TermDocs termDocs = indexReader.termDocs(this.keyTerm.createTerm(key));
            // end = System.nanoTime();
            // logger.debug("found in index in " + tf.format(end - begin) + " ns");

            if (termDocs.next()) {
                // begin = System.nanoTime();
                final Document doc = indexReader.document(termDocs.doc());
                result = fromByte(doc.getBinaryValue(this.valueFieldName));
                // end = System.nanoTime();
                // logger.debug(termDocs.freq() + " deserialized in " + tf.format(end - begin) +
                // " ns");

                return result;
            }
        } catch (final IOException e) {
            logger.error(e);
        }
        return new Entry[0];
    }

    public void interactive() throws Exception {
        InputStreamReader reader = null;
        BufferedReader myInput = null;
        while (true) {
            System.out
                    .println("\nPlease write a key and type <return> to continue (CTRL C to exit):");

            reader = new InputStreamReader(System.in);
            myInput = new BufferedReader(reader);
            final String query = myInput.readLine().toString();
            final String[] s = tabPattern.split(query);

            if (s.length == 1) {
                final long begin = System.nanoTime();
                final Entry[] result = search(s[0]);
                final long end = System.nanoTime();

                if (result != null && result.length > 0) {
                    for (int i = 0; i < result.length; i++) {
                        logger.info(i + "\t" + result[i]);
                    }
                    logger.info(s[0] + " found in " + tf.format(end - begin) + " ns");

                } else {
                    logger.info(s[0] + " not found in " + tf.format(end - begin) + " ns");
                }
            }
        }
    }

    public class Entry {

        double freq;

        String value;

        public Entry(final String value, final double freq) {
            this.value = value;
            this.freq = freq;
        }

        public String getValue() {
            return this.value;
        }

        public double getFreq() {
            return this.freq;
        }

        @Override
        public String toString() {
            return this.freq + "\t" + this.value;
        }
    }

    protected Entry[] fromByte(final byte[] byteArray) throws IOException {
        final ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);
        final DataInputStream dataStream = new DataInputStream(byteStream);

        // number of distinct forms
        final int size = dataStream.readInt();

        // total number of forms
        final int sum = dataStream.readInt();

        // logger.debug("size " + size);
        // logger.debug("sum " + sum);
        final Entry[] entryArray = new Entry[size];
        for (int j = 0; j < size; j++) {
            entryArray[j] = new Entry(dataStream.readUTF(), (double) dataStream.readInt() / sum);
            // entryArray[j] = new Entry(dataStream.readUTF(), (double) dataStream.readInt());
            // logger.debug(j + "\t" + entryArray[j]);
        }

        return entryArray;
    }

}
