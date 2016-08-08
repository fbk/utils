package eu.fbk.utils.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public final class MAP {

    public static final String PREFIX = "map";

    public static final String NAMESPACE = "http://dkm.fbk.eu/ontologies/mapping#";

    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    public static final URI FROM = createURI("from");

    public static final URI FROM_NS = createURI("fromNS");

    public static final URI FROM_PATTERN = createURI("fromPattern");

    public static final URI TO = createURI("to");

    public static final URI TO_NS = createURI("toNS");

    public static final URI TO_PATTERN = createURI("toPattern");

    // HELPER METHODS

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private MAP() {
    }

}
