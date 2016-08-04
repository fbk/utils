package eu.fbk.utils.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public final class KS {

    public static final String PREFIX = "ks";

    public static final String NAMESPACE = "http://dkm.fbk.eu/ontologies/knowledgestore#";

    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // Not emitted by RDFGenerator (if opinion layer not present in NAF)

    public static final URI TERM = createURI("Term");

    public static final URI ROOT_TERM = createURI("RootTerm");

    public static final URI OPINION = createURI("Opinion");

    public static final URI NEUTRAL_OPINION = createURI("NeutralOpinion");

    public static final URI POSITIVE_OPINION = createURI("PositiveOpinion");

    public static final URI NEGATIVE_OPINION = createURI("NegativeOpinion");

    public static final URI WORD = createURI("word");

    public static final URI STEM = createURI("stem");

    public static final URI POS = createURI("pos");

    public static final URI MORPHOFEAT = createURI("morphofeat");

    public static final URI HYPERNYM = createURI("hypernym");

    public static final URI BBN = createURI("bbn");

    public static final URI INDEX = createURI("index");

    public static final URI OFFSET = createURI("offset");

    public static final URI HAS_TERM = createURI("term");

    public static final URI HAS_HEAD = createURI("head");

    public static final URI EXPRESSION = createURI("expression");

    public static final URI HOLDER = createURI("holder");

    public static final URI TARGET = createURI("target");

    public static final URI EXPRESSION_SPAN = createURI("expressionSpan");

    public static final URI HOLDER_SPAN = createURI("holderSpan");

    public static final URI TARGET_SPAN = createURI("targetSpan");

    // public static final URI SENTENCE = createURI("sentence");

    // public static final URI DOCUMENT = createURI("document");

    // RESOURCE LAYER

    public static final URI RESOURCE = createURI("Resource");

    public static final URI TEXT = createURI("Text");

    public static final URI NAF = createURI("NAF");

    public static final URI TEXT_HASH = createURI("textHash");

    public static final URI ANNOTATED_WITH = createURI("annotatedWith");

    public static final URI ANNOTATION_OF = createURI("annotationOf");

    public static final URI VERSION = createURI("version");

    public static final URI LAYER = createURI("layer");

    public static final URI NAF_FILE_NAME = createURI("nafFileName");

    public static final URI NAF_FILE_TYPE = createURI("nafFileType");

    public static final URI NAF_PAGES = createURI("nafPages");

    // Mention layer

    public static final URI MENTION = createURI("Mention");

    public static final URI ENTITY_MENTION = createURI("EntityMention");

    public static final URI TIME_MENTION = createURI("TimeMention");

    public static final URI PREDICATE_MENTION = createURI("PredicateMention");

    public static final URI ATTRIBUTE_MENTION = createURI("AttributeMention");

    public static final URI NAME_MENTION = createURI("NameMention");

    public static final URI PARTICIPATION_MENTION = createURI("ParticipationMention");

    public static final URI COREFERENCE_MENTION = createURI("CoreferenceMention"); // TODO

    public static final URI EXPRESSED_BY = createURI("expressedBy"); // TODO

    public static final URI LEMMA = createURI("lemma");

    public static final URI SYNSET = createURI("synset");

    public static final URI SST = createURI("sst");

    public static final URI MENTION_OF = createURI("mentionOf"); // TODO only one needed

    public static final URI HAS_MENTION = createURI("hasMention");

    public static final URI COMPOUND_STRING = createURI("CompoundString");

    public static final URI COMPONENT_SUB_STRING = createURI("componentSubString");

    // public static final URI CONFIDENCE = createURI("confidence"); // double

    // ENTITY LAYER

    public static final URI ENTITY = createURI("Entity");

    public static final URI PREDICATE = createURI("Predicate");

    public static final URI TIME = createURI("Time");

    public static final URI ATTRIBUTE = createURI("Attribute");

    public static final URI INCLUDE = createURI("include");

    // public static final URI ARGUMENT = createURI("argument");

    public static final URI PROVENANCE = createURI("provenance"); // TODO string or URI?

    public static final URI LANGUAGE = createURI("language"); // TODO string or URI

    public static final URI PLURAL = createURI("plural"); // boolean

    public static final URI QUANTITY = createURI("quantity"); // decimal or string

    public static final URI RANK = createURI("rank"); // int or string

    public static final URI PERCENTAGE = createURI("percentage"); // decimal or string

    public static final URI MOD = createURI("mod");

    public static final URI HEAD_SYNSET = createURI("headSynset");

    public static final URI FACTUALITY = createURI("factuality");

    // HELPER METHODS

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private KS() {
    }

}
