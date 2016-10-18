package eu.fbk.utils.data.dataset.bow;


import java.util.List;

/**
 * Commong methods for different feature mapping's implementations
 *
 * @author Yaroslav Nechaev (remper@me.com)
 */
public interface FeatureMappingInterface {
    FeatureMapping.Feature lookup(String ngram);

    List<FeatureMapping.Feature> lookup(List<String> ngrams);
}
