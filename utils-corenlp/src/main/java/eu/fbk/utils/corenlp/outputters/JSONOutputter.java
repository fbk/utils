package eu.fbk.utils.corenlp.outputters;

import com.google.gson.*;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;
import edu.stanford.nlp.util.TypesafeMap;
import eu.fbk.utils.gson.AnnotationExclusionStrategy;
import eu.fbk.utils.gson.JSONLabel;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Output an Annotation to JSON.
 *
 * @author Alessio Palmero Aprosio
 */
@SuppressWarnings("unused")
public class JSONOutputter extends edu.stanford.nlp.pipeline.AnnotationOutputter {

    private final ThreadLocal<Annotation> annotationThreadLocal = new ThreadLocal<>();
    GsonBuilder gsonBuilder;

    static private void add(JsonSerializationContext jsonSerializationContext, JsonObject jsonObject, TypesafeMap annotation) {
        for (Class<?> myClass : annotation.keySet()) {
            Object o = annotation.get((Class) myClass);
            if (o != null) {
                if (myClass.isAnnotationPresent(JSONLabel.class)) {
                    JSONLabel JsonAnnotation = myClass.getAnnotation(JSONLabel.class);
                    String name = JsonAnnotation.value();
                    if (name != null && name.length() > 0) {
//                        Method toJsonMethod = null;
//                        try {
//                            toJsonMethod = myClass.getMethod("toJSON");
//                            System.out.println("Method exist for class " + myClass);
//                        } catch (NoSuchMethodException e) {
//                            System.out.println("Method does not exist for class " + myClass);
//                            // Method does not exist
//                            // ignored
//                        }
//                        if (toJsonMethod == null) {
//                            try {
//                                jsonObject.add(name, jsonSerializationContext.serialize(o));
//                            } catch (Exception e) {
//                                // Something is not serializable
//                                // ignored
//                            }
//                        }
//                        else {
//                            // Invoke method
//                        }
                        try {
                            jsonObject.add(name, jsonSerializationContext.serialize(o));
                        } catch (Exception e) {
                            // Something is not serializable
                            // ignored
                        }
                    }

                    Class<?>[] serializerClasses = JsonAnnotation.serializer();
                    for (Class<?> serializerClass : serializerClasses) {
                        if (JsonSerializer.class.isAssignableFrom(serializerClass)) {
                            // do stuff
                        }
                    }

                }
            }
        }
    }

    class CounterSerializer implements JsonSerializer<Counter> {

        @Override
        public JsonElement serialize(Counter counter, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            Set<Map.Entry<?, Double>> set1 = counter.entrySet();
            for (Map.Entry<?, Double> entry : set1) {
                if (entry.getKey() instanceof String) {
                    object.add((String) entry.getKey(), jsonSerializationContext.serialize(entry.getValue()));
                }
            }
            return object;
        }
    }

    class CoreLabelSerializer implements JsonSerializer<CoreLabel> {

        @Override
        public JsonElement serialize(CoreLabel coreLabel, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(coreLabel.index());
        }
    }

    class SpanSerializer implements JsonSerializer<Span> {

        @Override
        public JsonElement serialize(Span span, Type type,
                                     JsonSerializationContext jsonSerializationContext) {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(span.start());
            jsonArray.add(span.end());
            return jsonArray;
        }
    }

    class SemanticGraphSerializer implements JsonSerializer<SemanticGraph> {

        @Override
        public JsonElement serialize(SemanticGraph semanticGraph, Type type,
                                     JsonSerializationContext jsonSerializationContext) {
            JsonArray jsonArray = new JsonArray();

            for (IndexedWord root : semanticGraph.getRoots()) {
                JsonObject object = new JsonObject();
                object.addProperty("dep", "ROOT");
                object.addProperty("governor", 0);
                object.addProperty("governorGloss", "ROOT");
                object.addProperty("dependent", root.index());
                object.addProperty("dependentGloss", root.word());
                jsonArray.add(object);
            }
            for (SemanticGraphEdge edge : semanticGraph.edgeListSorted()) {
                JsonObject object = new JsonObject();
                object.addProperty("dep", edge.getRelation().toString());
                object.addProperty("governor", edge.getGovernor().index());
                object.addProperty("governorGloss", edge.getGovernor().word());
                object.addProperty("dependent", edge.getDependent().index());
                object.addProperty("dependentGloss", edge.getDependent().word());
                jsonArray.add(object);
            }
            return jsonArray;
        }
    }

    class RelationTripleSerializer implements JsonSerializer<RelationTriple> {

        @Override
        public JsonElement serialize(RelationTriple triple, Type type,
                                     JsonSerializationContext jsonSerializationContext) {
            JsonObject ieObject = new JsonObject();
            ieObject.addProperty("subject", triple.subjectGloss());
            ieObject.add("subjectSpan", jsonSerializationContext.serialize(Span.fromPair(triple.subjectTokenSpan())));
            ieObject.addProperty("relation", triple.relationGloss());
            ieObject.add("relationSpan", jsonSerializationContext.serialize(Span.fromPair(triple.relationTokenSpan())));
            ieObject.addProperty("object", triple.objectGloss());
            ieObject.add("objectSpan", jsonSerializationContext.serialize(Span.fromPair(triple.objectTokenSpan())));
            return ieObject;
        }
    }

    class TimexSerializer implements JsonSerializer<Timex> {

        @Override
        public JsonElement serialize(Timex time, Type type,
                                     JsonSerializationContext jsonSerializationContext) {
            JsonObject timexObj = new JsonObject();
            timexObj.addProperty("tid", time.tid());
            timexObj.addProperty("type", time.timexType());
            timexObj.addProperty("value", time.value());
            timexObj.addProperty("altValue", time.altVal());
            return timexObj;
        }
    }

    class DoubleSerializer implements JsonSerializer<Double> {

        @Override
        public JsonElement serialize(Double aDouble, Type type, JsonSerializationContext jsonSerializationContext) {
            if (aDouble != null && aDouble.isNaN()) {
                aDouble = null;
            }
            if (aDouble != null && aDouble.isInfinite()) {
                aDouble = null;
            }

            return new JsonPrimitive(aDouble);
        }
    }

    class AnnotationSerializer implements JsonSerializer<Annotation> {

        private Options options;

        public AnnotationSerializer(Options options) {
            this.options = options;
        }

        @Override
        public JsonElement serialize(Annotation doc, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();

            String text = doc.get(CoreAnnotations.TextAnnotation.class);
            jsonObject.addProperty("docId", doc.get(CoreAnnotations.DocIDAnnotation.class));
            jsonObject.addProperty("docDate", doc.get(CoreAnnotations.DocDateAnnotation.class));
            jsonObject.addProperty("docSourceType", doc.get(CoreAnnotations.DocSourceTypeAnnotation.class));
            jsonObject.addProperty("docType", doc.get(CoreAnnotations.DocTypeAnnotation.class));
            jsonObject.addProperty("author", doc.get(CoreAnnotations.AuthorAnnotation.class));
            jsonObject.addProperty("location", doc.get(CoreAnnotations.LocationAnnotation.class));
            if (options.includeText) {
                jsonObject.addProperty("text", text);
            }

            List<CoreMap> quotes = doc.get(CoreAnnotations.QuotationsAnnotation.class);
            if (quotes != null && quotes.size() > 0) {
                JsonArray jsonQuotesArray = new JsonArray();
                for (CoreMap quote : quotes) {
                    JsonObject quoteObj = new JsonObject();

                    List<CoreLabel> tokens = quote.get(CoreAnnotations.TokensAnnotation.class);
                    int begin = tokens.get(0).beginPosition();
                    int end = tokens.get(tokens.size() - 1).endPosition();

                    int beginContext = Math.max(0, begin - 100);
                    int endContext = Math.min(end + 100, text.length());
                    quoteObj.addProperty("text", quote.get(CoreAnnotations.TextAnnotation.class));
                    quoteObj.addProperty("context", text.substring(beginContext, endContext));
                    quoteObj.addProperty("characterOffsetBegin", begin);
                    quoteObj.addProperty("characterOffsetEnd", end);
                    jsonQuotesArray.add(quoteObj);
                }
                jsonObject.add("quotes", jsonQuotesArray);
            }

            add(jsonSerializationContext, jsonObject, doc);

            // Sentences
            if (doc.get(CoreAnnotations.SentencesAnnotation.class) != null) {
                addSentences(jsonSerializationContext, jsonObject, doc.get(CoreAnnotations.SentencesAnnotation.class), options);
            }

            // Add coref values
            annotationThreadLocal.set(doc);
            jsonObject.add("corefs", jsonSerializationContext.serialize(doc.get(CorefCoreAnnotations.CorefChainAnnotation.class)));

            return jsonObject;
        }
    }

    class CorefChainSerializer implements JsonSerializer<CorefChain> {

        @Override
        public JsonElement serialize(CorefChain chain, Type type,
                                     JsonSerializationContext jsonSerializationContext) {
            CorefChain.CorefMention representative = chain.getRepresentativeMention();
            JsonArray chainArray = new JsonArray();
            for (CorefChain.CorefMention mention : chain.getMentionsInTextualOrder()) {
                JsonObject mentionObj = new JsonObject();
                mentionObj.addProperty("id", mention.mentionID);
                mentionObj.add("text", jsonSerializationContext.serialize(mention.mentionSpan));
                mentionObj.add("type", jsonSerializationContext.serialize(mention.mentionType));
                mentionObj.add("number", jsonSerializationContext.serialize(mention.number));
                mentionObj.add("gender", jsonSerializationContext.serialize(mention.gender));
                mentionObj.add("animacy", jsonSerializationContext.serialize(mention.animacy));
                mentionObj.addProperty("startIndex", mention.startIndex);
                mentionObj.addProperty("endIndex", mention.endIndex);
                mentionObj.addProperty("sentNum", mention.sentNum);
                mentionObj.add("position", jsonSerializationContext.serialize(mention.position.elems()));
                mentionObj.addProperty("isRepresentativeMention", mention == representative);
                chainArray.add(mentionObj);
            }
            return chainArray;
        }
    }

    public JSONOutputter(GsonBuilder gsonBuilder) {
        this.gsonBuilder = gsonBuilder;
    }

    public JSONOutputter() {
        this.gsonBuilder = new GsonBuilder();
    }

    @Override
    public void print(Annotation doc, OutputStream target, Options options) throws IOException {

        if (options.pretty) {
            gsonBuilder.setPrettyPrinting();
        }

        gsonBuilder.registerTypeAdapter(SemanticGraph.class, new SemanticGraphSerializer());
        gsonBuilder.registerTypeAdapter(Span.class, new SpanSerializer());
        gsonBuilder.registerTypeAdapter(RelationTriple.class, new RelationTripleSerializer());
        gsonBuilder.registerTypeAdapter(Timex.class, new TimexSerializer());
        gsonBuilder.registerTypeAdapter(CorefChain.class, new CorefChainSerializer());
        gsonBuilder.registerTypeAdapter(CoreLabel.class, new CoreLabelSerializer());
        gsonBuilder.registerTypeAdapter(Double.class, new DoubleSerializer());
        gsonBuilder.registerTypeAdapter(Annotation.class, new AnnotationSerializer(options));
        gsonBuilder.registerTypeAdapter(Counter.class, new CounterSerializer());

        gsonBuilder.serializeSpecialFloatingPointValues();
        gsonBuilder.setExclusionStrategies(new AnnotationExclusionStrategy());
        Gson gson = gsonBuilder.create();

        Writer w = new OutputStreamWriter(target);
        w.write(gson.toJson(doc));
        w.flush();
    }

    private static void addSentences(JsonSerializationContext jsonSerializationContext, JsonObject jsonObject, List<CoreMap> sentences,
                                     Options options) {
        JsonArray jsonSentenceArray = new JsonArray();
        for (CoreMap sentence : sentences) {
            JsonObject sentenceObj = new JsonObject();

            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            sentenceObj.addProperty("id", sentence.get(CoreAnnotations.SentenceIDAnnotation.class));
            sentenceObj.addProperty("index", sentence.get(CoreAnnotations.SentenceIndexAnnotation.class));
            sentenceObj.addProperty("line", sentence.get(CoreAnnotations.LineNumberAnnotation.class));
            sentenceObj.addProperty("characterOffsetBegin", tokens.get(0).beginPosition());
            sentenceObj.addProperty("characterOffsetEnd", tokens.get(tokens.size() - 1).endPosition());
            sentenceObj.addProperty("text", sentence.get(CoreAnnotations.TextAnnotation.class));

            // Dependencies
            sentenceObj.add("basic-dependencies",
                    jsonSerializationContext.serialize(sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class)));
            sentenceObj.add("collapsed-dependencies", jsonSerializationContext.serialize(
                    sentence.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class)));
            sentenceObj.add("collapsed-ccprocessed-dependencies", jsonSerializationContext.serialize(
                    sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class)));

            // Constituents
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            StringWriter treeStrWriter = new StringWriter();
            TreePrint treePrinter = options.constituencyTreePrinter;
//            if (treePrinter == AnnotationOutputter.DEFAULT_CONSTITUENCY_TREE_PRINTER) {
//                treePrinter = new TreePrint("oneline");
//            }
            treePrinter.printTree(tree,
                    new PrintWriter(treeStrWriter, true));
            sentenceObj.addProperty("parse", treeStrWriter.toString().trim());

            // Sentiment
            Tree sentimentTree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            if (sentimentTree != null) {
                int sentiment = RNNCoreAnnotations.getPredictedClass(sentimentTree);
                String sentimentClass = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                sentenceObj.addProperty("sentimentValue", Integer.toString(sentiment));
                sentenceObj.addProperty("sentiment", sentimentClass.replaceAll("\\s+", ""));
            }

            // OpenIE
            sentenceObj.add("openie", jsonSerializationContext.serialize(sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class)));

            // Tokens
            if (sentence.get(CoreAnnotations.TokensAnnotation.class) != null) {
                addTokens(jsonSerializationContext, sentenceObj, sentence.get(CoreAnnotations.TokensAnnotation.class));
            }

            add(jsonSerializationContext, sentenceObj, sentence);

            jsonSentenceArray.add(sentenceObj);
        }
        jsonObject.add("sentences", jsonSentenceArray);
    }

    private static void addTokens(JsonSerializationContext jsonSerializationContext, JsonObject sentenceObj, List<CoreLabel> tokens) {
        JsonArray jsonTokenArray = new JsonArray();
        for (CoreLabel token : tokens) {
            JsonObject tokenObj = new JsonObject();

            tokenObj.addProperty("index", token.index());
            tokenObj.addProperty("word", token.word());
            tokenObj.addProperty("originalText", token.originalText());
            tokenObj.addProperty("lemma", token.lemma());
            tokenObj.addProperty("characterOffsetBegin", token.beginPosition());
            tokenObj.addProperty("characterOffsetEnd", token.endPosition());
            tokenObj.addProperty("pos", token.tag());
            tokenObj.addProperty("featuresText", token.get(CoreAnnotations.FeaturesAnnotation.class));
            tokenObj.addProperty("ner", token.ner());
            tokenObj.addProperty("normalizedNER",
                    token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
            tokenObj.addProperty("speaker", token.get(CoreAnnotations.SpeakerAnnotation.class));
            tokenObj.addProperty("truecase", token.get(CoreAnnotations.TrueCaseAnnotation.class));
            tokenObj.addProperty("truecaseText", token.get(CoreAnnotations.TrueCaseTextAnnotation.class));
            tokenObj.addProperty("before", token.get(CoreAnnotations.BeforeAnnotation.class));
            tokenObj.addProperty("after", token.get(CoreAnnotations.AfterAnnotation.class));
            tokenObj.addProperty("isMultiwordToken", token.get(CoreAnnotations.IsMultiWordTokenAnnotation.class));
            tokenObj.addProperty("isMultiwordFirstToken", token.get(CoreAnnotations.IsFirstWordOfMWTAnnotation.class));
            if (token.containsKey(CoreAnnotations.CoNLLUTokenSpanAnnotation.class)) {
                IntPair tokenSpan = token.get(CoreAnnotations.CoNLLUTokenSpanAnnotation.class);
                tokenObj.addProperty("multiwordSpan", "" + tokenSpan.getSource() + "-" + tokenSpan.getTarget());
            }

            // Timex
            tokenObj.add("timex", jsonSerializationContext.serialize(token.get(TimeAnnotations.TimexAnnotation.class)));

            add(jsonSerializationContext, tokenObj, token);

            jsonTokenArray.add(tokenObj);
        }

        sentenceObj.add("tokens", jsonTokenArray);
    }

    public static String jsonPrint(GsonBuilder gsonBuilder, Annotation annotation) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new JSONOutputter(gsonBuilder).print(annotation, outputStream);
        return new String(outputStream.toByteArray(), "UTF-8");
    }

    public static void jsonPrint(GsonBuilder gsonBuilder, Annotation annotation, OutputStream os) throws IOException {
        new JSONOutputter(gsonBuilder).print(annotation, os);
    }

    public static void jsonPrint(GsonBuilder gsonBuilder, Annotation annotation, OutputStream os,
                                 StanfordCoreNLP pipeline) throws IOException {
        new JSONOutputter(gsonBuilder).print(annotation, os, pipeline);
    }

    public static void jsonPrint(GsonBuilder gsonBuilder, Annotation annotation, OutputStream os, Options options)
            throws IOException {
        new JSONOutputter(gsonBuilder).print(annotation, os, options);
    }

    public static String jsonPrint(Annotation annotation) throws IOException {
        return jsonPrint(annotation, new Options());
    }

    public static String jsonPrint(Annotation annotation, Options options) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new JSONOutputter().print(annotation, outputStream, options);
        return new String(outputStream.toByteArray(), "UTF-8");
    }

    public static void jsonPrint(Annotation annotation, OutputStream os) throws IOException {
        new JSONOutputter().print(annotation, os);
    }

    public static void jsonPrint(Annotation annotation, OutputStream os,
                                 StanfordCoreNLP pipeline) throws IOException {
        new JSONOutputter().print(annotation, os, pipeline);
    }

    public static void jsonPrint(Annotation annotation, OutputStream os, Options options)
            throws IOException {
        new JSONOutputter().print(annotation, os, options);
    }

}
