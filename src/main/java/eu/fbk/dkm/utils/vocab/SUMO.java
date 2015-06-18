package eu.fbk.dkm.utils.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public final class SUMO {

    public static final String PREFIX = "sumo";

    public static final String NAMESPACE = "http://www.ontologyportal.org/SUMO.owl#";

    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    public static final URI ENTITY = createURI("Entity");

    public static final URI PROCESS = createURI("Process");

    public static final URI RELATION = createURI("Relation");

    // HELPER METHODS

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private SUMO() {
    }

}
