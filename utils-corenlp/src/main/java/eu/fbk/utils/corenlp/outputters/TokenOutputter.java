package eu.fbk.utils.corenlp.outputters;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationOutputter;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TokenOutputter extends AnnotationOutputter {

    public TokenOutputter() {
    }

    @Override
    public void print(Annotation doc, OutputStream target, Options options) throws IOException {
        PrintWriter writer = new PrintWriter(IOUtils.encodedOutputStreamWriter(target, options.encoding));

        if (doc.get(CoreAnnotations.SentencesAnnotation.class) != null) {
            for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
                if (sentence.get(CoreAnnotations.TokensAnnotation.class) != null) {
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        writer.println(token.originalText());
                    }
                }
                writer.println();
            }
        }

        writer.flush();
    }

    public static void tpPrint(Annotation annotation, OutputStream os) throws IOException {
        new TokenOutputter().print(annotation, os);
    }

    public static void tpPrint(Annotation annotation, OutputStream os, StanfordCoreNLP pipeline) throws IOException {
        new TokenOutputter().print(annotation, os, pipeline);
    }

    public static void tpPrint(Annotation annotation, OutputStream os, Options options) throws IOException {
        new TokenOutputter().print(annotation, os, options);
    }

}