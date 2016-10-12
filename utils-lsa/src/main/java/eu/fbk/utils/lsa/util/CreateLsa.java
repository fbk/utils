package eu.fbk.utils.lsa.util;

import eu.fbk.utils.analysis.stemmer.Stemmer;
import eu.fbk.utils.analysis.stemmer.StemmerFactory;
import eu.fbk.utils.analysis.stemmer.StemmerNotFoundException;
import eu.fbk.utils.lsa.io.DenseText2DenseBinary;
import eu.fbk.utils.lsa.io.FileFreqFilter;
import eu.fbk.utils.lsa.io.TFIDF;
import eu.fbk.utils.lsa.io.TermDocumentMatrixFileWriter;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.tartarus.snowball.SnowballStemmer;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: giuliano
 * Date: 3/4/13
 * Time: 12:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateLsa {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>CreateLsa</code>.
     */
    static Logger logger = Logger.getLogger(CreateLsa.class.getName());

    Configuration config;

    public static final int DEFAULT_CUTOFF = 5;

    public static final int DEFAULT_NUM_DOC = Integer.MAX_VALUE;

    public static final String DEFAULT_MODEL_DIR = System.getProperty("user.dir");

    SnowballStemmer snowballStemmer = null;

    public CreateLsa(File corpusFile, String modelDir, int dim, int cutoff, int numDoc, File stopWordFile,
            Stemmer stemmer) {
        try {
            String name = "configuration/config.xml";
            logger.info("configuration file " + name);
            DefaultConfigurationBuilder defaultConfigurationBuilder = new DefaultConfigurationBuilder(name);
            defaultConfigurationBuilder.setBasePath(".");
            config = defaultConfigurationBuilder.getConfiguration();

            String outputDirName = modelDir + "lsa-cutoff-" + cutoff + "-dim-" + dim;
            if (numDoc == Integer.MAX_VALUE) {
                outputDirName += "-size-all";
            } else {
                outputDirName += "-size-" + numDoc;
            }
            outputDirName += File.separator;

            File outputDir = new File(outputDirName);
            if (!outputDir.exists()) {
                outputDir.mkdir();
            }

            String rootFile = outputDirName + "X";
            logger.debug(rootFile);

            File devNull = new File("/dev/null");
            if (cutoff > 1) {
                logger.info("calculating df...");
                new TermDocumentMatrixFileWriter(corpusFile, rootFile, stopWordFile, devNull, numDoc, false, stemmer);

                File df = new File(outputDirName + "X-df");
                File filteredDf = new File(outputDirName + "X-df.cutoff-" + cutoff);
                logger.info("filtering " + (stemmer == null ? "terms" : "stems") + " with df < " + cutoff + "...");
                new FileFreqFilter(df, filteredDf, cutoff);

                logger.info("creating term-by-document matrix...");
                new TermDocumentMatrixFileWriter(corpusFile, rootFile, devNull, filteredDf, numDoc, true, stemmer);
            } else {
                logger.info("creating term-by-document matrix...");
                new TermDocumentMatrixFileWriter(corpusFile, rootFile, stopWordFile, devNull, numDoc, true, stemmer);
            }

            File matrixFile = new File(rootFile + "-matrix");
            File tfIdfMatrixFile = new File(rootFile + "-matrix-tf-idf");
            File rowFile = new File(rootFile + "-row");
            File dfFile = new File(rootFile + "-df");
            logger.info("calculating tfidf (" + matrixFile + ", " + tfIdfMatrixFile + ", " + rowFile + ", " + rowFile
                    + ")...");
            new TFIDF(matrixFile, tfIdfMatrixFile, rowFile, dfFile);

            logger.info(
                    "running svd (" + tfIdfMatrixFile.getAbsolutePath() + ", " + outputDirName + "X, " + dim + ")...");
            new SVD(tfIdfMatrixFile.getAbsolutePath(), outputDirName + "X", dim);

            File denseTextUtFile = new File(outputDirName + "X-Ut");
            File tmpDenseTextUtFile = new File(outputDirName + "X-Ut.tmp");
            File sparseBinaryUtFile = new File(outputDirName + "X-Ut");
            denseTextUtFile.renameTo(tmpDenseTextUtFile);

            logger.info("converting matrix (" + tmpDenseTextUtFile + ", " + sparseBinaryUtFile + ")...");
            DenseText2DenseBinary denseText2DenseBinary = new DenseText2DenseBinary(tmpDenseTextUtFile,
                    sparseBinaryUtFile);
            denseText2DenseBinary.read1();
            denseText2DenseBinary.close();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void setLanguage(String lang) {

    }

    public void init() {

    }

    public static void main(String[] args) {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);

        Options options = new Options();
        try {
            Option corpusFileOpt = OptionBuilder.withArgName("file").hasArg()
                    .withDescription("File from which to read the corpus").isRequired().withLongOpt("corpus-file")
                    .create("f");
            Option modelDirOpt = OptionBuilder.withArgName("string").hasArg().withDescription(
                    "Directory in which to store the resulting model (default is " + DEFAULT_MODEL_DIR + ")")
                    .withLongOpt("model-dir").create("o");
            Option dimOpt = OptionBuilder.withArgName("int").hasArg()
                    .withDescription("Desired SVD triples (default is " + SVD.DEFAULT_DIM + ")")
                    .withLongOpt("dimension").create("d");
            Option cutoffOpt = OptionBuilder.withArgName("int").hasArg()
                    .withDescription("Desired cutoff (default is " + DEFAULT_CUTOFF + ")").withLongOpt("cutoff")
                    .create("c");
            Option numDocOpt = OptionBuilder.withArgName("int").hasArg()
                    .withDescription("Desired number of documents (default is all)").withLongOpt("doc-num").create();
            Option stopWordsFileOpt = OptionBuilder.withArgName("file").hasArg()
                    .withDescription("file from which to read the stopwords").withLongOpt("stopwords").create("s");
            Option langOpt = OptionBuilder.withArgName("string").hasArg()
                    .withDescription("if specified, use a language-specific stemmer").withLongOpt("lang").create("l");

            options.addOption("h", "help", false, "print this message");
            options.addOption("v", "version", false, "output version information and exit");

            options.addOption(corpusFileOpt);
            options.addOption(modelDirOpt);
            options.addOption(dimOpt);

            options.addOption(cutoffOpt);
            options.addOption(stopWordsFileOpt);
            options.addOption(numDocOpt);
            options.addOption(langOpt);

            CommandLineParser parser = new PosixParser();
            CommandLine line = parser.parse(options, args);

            String modelDir = DEFAULT_MODEL_DIR;
            if (line.hasOption("model-dir")) {
                modelDir = line.getOptionValue("model-dir");
            }

            int dim = SVD.DEFAULT_DIM;
            if (line.hasOption("dim")) {
                dim = Integer.parseInt(line.getOptionValue("dim"));
            }

            int cutoff = DEFAULT_CUTOFF;
            if (line.hasOption("cutoff")) {
                cutoff = Integer.parseInt(line.getOptionValue("cutoff"));
            }

            int numDoc = DEFAULT_NUM_DOC;
            if (line.hasOption("doc-num")) {
                numDoc = Integer.parseInt(line.getOptionValue("doc-num"));
            }

            File stopWordFile = new File("/dev/null");
            if (line.hasOption("stopwords")) {
                stopWordFile = new File(line.getOptionValue("stopwords"));
            }

            Stemmer stemmer = null;
            if (line.hasOption("lang")) {
                try {
                    stemmer = StemmerFactory.getInstance(line.getOptionValue("lang"));
                } catch (StemmerNotFoundException e) {
                    logger.error(e);
                }
            }
            new CreateLsa(new File(line.getOptionValue("corpus-file")), modelDir, dim, cutoff, numDoc, stopWordFile,
                    stemmer);

        } catch (ParseException e) {
            // oops, something went wrong
            System.out.println("Parsing failed: " + e.getMessage() + "\n");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(400, "java -cp dist/jcore.jar eu.fbk.utils.lsa.util.CreateLsa", "\n", options, "\n",
                    true);
        }

    }
}
