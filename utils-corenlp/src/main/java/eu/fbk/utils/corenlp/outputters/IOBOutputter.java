package eu.fbk.utils.corenlp.outputters;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationOutputter;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class IOBOutputter extends AnnotationOutputter {

    public IOBOutputter() {
    }

    @Override
    public void print(Annotation doc, OutputStream target, Options options) throws IOException {
        PrintWriter writer = new PrintWriter(IOUtils.encodedOutputStreamWriter(target, options.encoding));

        if (doc.get(CoreAnnotations.SentencesAnnotation.class) != null) {
            for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
                if (sentence.get(CoreAnnotations.TokensAnnotation.class) != null) {
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        writer.append(token.originalText()).append("\t").append(token.ner()).println();
                    }
                }
                writer.println();
            }
        }

        writer.flush();
    }

    public static void iobPrint(Annotation annotation, OutputStream os) throws IOException {
        new IOBOutputter().print(annotation, os);
    }

    public static void iobPrint(Annotation annotation, OutputStream os, StanfordCoreNLP pipeline) throws IOException {
        new IOBOutputter().print(annotation, os, pipeline);
    }

    public static void iobPrint(Annotation annotation, OutputStream os, Options options) throws IOException {
        new IOBOutputter().print(annotation, os, options);
    }

}