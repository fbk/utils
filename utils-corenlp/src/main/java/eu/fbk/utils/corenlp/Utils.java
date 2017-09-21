package eu.fbk.utils.corenlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static void addBasicAnnotations(Annotation annotation, List<List<CoreLabel>> sTokens) {
        String text = annotation.get(CoreAnnotations.TextAnnotation.class);
        addBasicAnnotations(annotation, sTokens, text);
    }

    public static void addBasicAnnotations(Annotation annotation, List<List<CoreLabel>> sTokens, String text) {
        List<CoreMap> sentences = new ArrayList<>();
        ArrayList<CoreLabel> tokens = new ArrayList<>();

        int sIndex = 0;
        int tokenIndex = 0;

        for (List<CoreLabel> sentence : sTokens) {
            if (sentence.size() == 0) {
                continue;
            }

            CoreMap sent = new ArrayCoreMap(1);
            for (CoreLabel coreLabel : sentence) {
                coreLabel.setSentIndex(sIndex);
            }

            int begin = sentence.get(0).beginPosition();
            int end = sentence.get(sentence.size() - 1).endPosition();

            sent.set(CoreAnnotations.TokensAnnotation.class, sentence);

            sent.set(CoreAnnotations.SentenceIndexAnnotation.class, sIndex++);
            sent.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, begin);
            sent.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, end);

            sent.set(CoreAnnotations.TokenBeginAnnotation.class, tokenIndex);
            tokenIndex += sentence.size();
            sent.set(CoreAnnotations.TokenEndAnnotation.class, tokenIndex);
            sent.set(CoreAnnotations.TextAnnotation.class, text.substring(begin, end));

            sentences.add(sent);
            tokens.addAll(sentence);
        }

        annotation.set(CoreAnnotations.TokensAnnotation.class, tokens);
        annotation.set(CoreAnnotations.SentencesAnnotation.class, sentences);

    }
}
