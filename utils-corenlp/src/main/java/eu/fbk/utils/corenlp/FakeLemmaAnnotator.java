package eu.fbk.utils.corenlp;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;

import java.util.Collections;
import java.util.Set;

public class FakeLemmaAnnotator implements Annotator {
    @Override
    public void annotate(Annotation annotation) {
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            token.set(CoreAnnotations.LemmaAnnotation.class, token.originalText());
        }

    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(CoreAnnotations.LemmaAnnotation.class);
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.singleton(CoreAnnotations.TokensAnnotation.class);
    }
}
