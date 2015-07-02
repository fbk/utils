package eu.fbk.dkm.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.slf4j.LoggerFactory;

/**
 * Created by alessio on 26/05/15.
 */

public class TWMFormExtractor extends FormPageSearcher {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TWMFormExtractor.class);

    public TWMFormExtractor(final String indexName) throws IOException {
        super(indexName);
    }

    public static void main(final String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("TWM form extractor")
                    .withHeader("Extract forms from the TWM Lucene index")
                    .withOption("i", "input-folder", "the folder of the NAF corpus", "DIR",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output-file", "output file", "FILE", CommandLine.Type.FILE,
                            true, false, true)
                    .withOption("g", "greater",
                            "extract forms with greater fequency (default: smaller)")
                    .withOption("t", "threshold", "threshold", "NUM", CommandLine.Type.FLOAT,
                            true, false, true).withLogger(LoggerFactory.getLogger("eu.fbk"))
                    .parse(args);

            final File inputFolder = cmd.getOptionValue("input-folder", File.class);
            final File outputFile = cmd.getOptionValue("output-file", File.class);

            final Float threshold = cmd.getOptionValue("threshold", Float.class);

            final boolean greater = cmd.hasOption("greater");

            try {
                final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

                final TWMFormExtractor formPageSearcher = new TWMFormExtractor(
                        inputFolder.getAbsolutePath());
                final IndexReader indexReader = formPageSearcher.getIndexReader();

                for (int i = 0; i < indexReader.numDocs(); i++) {
                    final Document document = indexReader.document(i);

                    final String form = document.get("form");
                    final Entry[] entries = formPageSearcher.fromByte(document
                            .getBinaryValue("entry"));

                    for (final Entry entry : entries) {
                        if (greater && entry.getFreq() >= threshold || !greater
                                && entry.getFreq() <= threshold) {
                            writer.append(form + "\t" + entry.getFreq()).append("\n");
                        }
                    }
                }

                writer.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }

        } catch (final CommandLine.Exception e) {
            CommandLine.fail(e);
        }
    }
}
