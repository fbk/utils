package eu.fbk.utils.corenlp;

import com.google.common.collect.HashMultimap;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.util.Pair;
import eu.fbk.utils.gson.JSONLabel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by alessio on 04/05/17.
 */

public class CustomAnnotations {

    @JSONLabel("features")
    public static class FeaturesAnnotation implements CoreAnnotation<Map<String, Collection<String>>> {

        public Class<Map<String, Collection<String>>> getType() {
            return ErasureUtils.uncheckedCast(Map.class);
        }
    }

    @JSONLabel("ud_misc")
    public static class MiscAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return ErasureUtils.uncheckedCast(String.class);
        }
    }

    @JSONLabel("ud_pos")
    public static class UPosAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return ErasureUtils.uncheckedCast(String.class);
        }
    }

    @JSONLabel("ud_deps")
    public static class DepsAnnotation implements CoreAnnotation<List<Pair<Integer, String>>> {

        public Class<List<Pair<Integer, String>>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }
    }

    @JSONLabel("simplecoref")
    public static class SimpleCorefAnnotation implements CoreAnnotation<HashMultimap<Integer, Integer>> {

        public SimpleCorefAnnotation() {
        }

        public Class<HashMultimap<Integer, Integer>> getType() {
            return (Class) ErasureUtils.uncheckedCast(HashMultimap.class);
        }
    }

}
