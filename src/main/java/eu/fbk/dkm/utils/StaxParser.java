package eu.fbk.dkm.utils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;

public abstract class StaxParser {

    private static XMLInputFactory STAX_FACTORY;

    static {
        STAX_FACTORY = XMLInputFactory.newInstance();
        STAX_FACTORY.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    private XMLStreamReader reader;

    private int targetLevel;

    private int actualLevel;

    protected StaxParser(final Reader reader) {
        try {
            this.reader = STAX_FACTORY.createXMLStreamReader(reader);
            this.targetLevel = 0;
            this.actualLevel = 0;
        } catch (final XMLStreamException ex) {
            throw new Error("Unexpected exception (!)", ex);
        }
    }

    protected final void leave() throws XMLStreamException {
        --this.targetLevel;
        while (this.actualLevel > this.targetLevel && this.reader.hasNext()) {
            final int event = this.reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                ++this.actualLevel;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                --this.actualLevel;
            }
        }
    }

    protected final void enter(final String element) throws XMLStreamException {
        if (!tryEnter(element)) {
            throw new XMLStreamException("Expected element " + element);
        }
    }

    protected final boolean tryEnter(final String element) throws XMLStreamException {
        if (this.actualLevel > this.targetLevel
                && (element == null || this.reader.getLocalName().equals(element))) {
            ++this.targetLevel;
            return true;
        }
        while (this.actualLevel >= this.targetLevel && this.reader.hasNext()) {
            final int event = this.reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                ++this.actualLevel;
                if (this.actualLevel == this.targetLevel + 1
                        && (element == null || this.reader.getLocalName().equals(element))) {
                    ++this.targetLevel;
                    return true;
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                --this.actualLevel;
            }
        }
        return false;
    }

    protected final boolean tryEnterNext(final String element) throws XMLStreamException {
        if (this.actualLevel > this.targetLevel && this.reader.getLocalName().equals(element)) {
            ++this.targetLevel;
            return true;
        }
        while (this.actualLevel == this.targetLevel && this.reader.hasNext()) {
            final int event = this.reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                ++this.actualLevel;
                if (this.reader.getLocalName().equals(element)) {
                    ++this.targetLevel;
                    return true;
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                --this.actualLevel;
            }
        }
        return false;
    }

    protected final String attribute(final String name) {
        return this.reader.getAttributeValue("", name);
    }

    protected final String content() throws XMLStreamException {
        final StringBuilder builder = new StringBuilder();
        while (this.actualLevel >= this.targetLevel && this.reader.hasNext()) {
            final int event = this.reader.next();
            if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                builder.append(this.reader.getText());
            } else if (event == XMLStreamConstants.START_ELEMENT) {
                ++this.actualLevel;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                --this.actualLevel;
            }
        }
        return builder.toString();
    }

}