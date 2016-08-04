package eu.fbk.utils.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public final class GAF {

    public static final String PREFIX = "gaf";

    public static final String NAMESPACE = "http://groundedannotationframework.org/gaf#";

    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // PROPERTIES

    public static final URI DENOTED_BY = createURI("denotedBy");

    // HELPER METHODS

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private GAF() {
    }

}
