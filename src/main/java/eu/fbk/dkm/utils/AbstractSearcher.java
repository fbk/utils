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

package eu.fbk.dkm.utils;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

/**
 * Created with IntelliJ IDEA. User: giuliano Date: 1/22/13 Time: 6:05 PM To change this template
 * use File | Settings | File Templates.
 */
public class AbstractSearcher extends Index {

    public static final char HORIZONTAL_TABULATION = 0x0009;

    public static final char LINE_FEED = 0x000a;

    /**
     * Define a static logger variable so that it references the Logger instance named
     * <code>AbstractSearcher</code>.
     */
    static Logger logger = Logger.getLogger(AbstractSearcher.class.getName());

    public static final int DEFAULT_SHOW_SIZE = 10;

    protected IndexSearcher indexSearcher;

    protected IndexReader indexReader;

    public IndexReader getIndexReader() {
        return this.indexReader;
    }

    public AbstractSearcher(final String indexName) throws IOException {
        super(indexName);

        logger.debug("opening " + indexName + "...");
        final FSDirectory directory = FSDirectory.open(new File(indexName));
        // logger.debug("FSDirectory implementation " + directory.getClass().toString());
        this.indexReader = IndexReader.open(directory, true);
        // indexSearcher = new IndexSearcher(directory, true);
        // indexReader = indexSearcher.getIndexReader();
        setNotificationPoint(1000);
        // logger.debug("notification point " + getNotificationPoint());
    }

    public void close() throws IOException {
        logger.info("closing...");
        // indexSearcher.close();
        this.indexReader.close();
    }

    public String toString(final int size) {
        final StringBuilder sb = new StringBuilder();
        Document doc;
        int s = size;
        sb.append(size + "/" + this.indexReader.numDocs() + "\n");
        if (s > this.indexReader.numDocs()) {
            s = this.indexReader.numDeletedDocs();
        }
        logger.info(this.indexReader.numDocs() + " documents");
        for (int i = 0; i < s; i++) {
            try {
                doc = this.indexReader.document(i);
                // logger.debug(i + "\t" + doc);
                sb.append(i);
                sb.append(HORIZONTAL_TABULATION);
                sb.append(doc);
                sb.append(LINE_FEED);

            } catch (final IOException e) {
                logger.error(e);
            }
        }
        return sb.toString();
    }
}
