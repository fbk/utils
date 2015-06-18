package eu.fbk.dkm.utils.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public final class NWR {

    public static final String PREFIX = "nwr";

    public static final String NAMESPACE = "http://www.newsreader-project.eu/ontologies/";

    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    public static final URI PERSON = createURI("PERSON");

    public static final URI ORGANIZATION = createURI("ORGANIZATION");

    public static final URI LOCATION = createURI("LOCATION");

    public static final URI MISC = createURI("MISC");

    // HELPER METHODS

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private NWR() {
    }

}
