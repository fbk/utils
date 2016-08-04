package eu.fbk.utils.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public final class GR {

    public static final String PREFIX = "gr";

    public static final String NAMESPACE = "http://purl.org/goodrelations/v1#";

    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // TERMS

    public static final URI PRICE_SPECIFICATION = createURI("PriceSpecification");

    public static final URI HAS_CURRENCY = createURI("hasCurrency");

    public static final URI HAS_CURRENCY_VALUE = createURI("hasCurrencyValue");

    // HELPER METHODS

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private GR() {
    }

}
