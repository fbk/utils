package eu.fbk.dkm.utils.vocab;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class OWLTIME {

    public static final String PREFIX = "owltime";

    public static final String NAMESPACE = "http://www.w3.org/TR/owl-time#";

    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

    // CLASSES

    public static final URI DATE_TIME_DESCRIPTION = createURI("DateTimeDescription");

    public static final URI DATE_TIME_INTERVAL = createURI("DateTimeInterval");

    public static final URI DAY_OF_WEEK_CLASS = createURI("DayOfWeek");

    public static final URI DURATION_DESCRIPTION = createURI("DurationDescription");

    public static final URI INSTANT = createURI("Instant");

    public static final URI INTERVAL = createURI("Interval");

    public static final URI PROPER_INTERVAL = createURI("ProperInterval");

    public static final URI TEMPORAL_ENTITY = createURI("TemporalEntity");

    public static final URI TEMPORAL_UNIT = createURI("TemporalUnit");

    // PROPERTIES

    public static final URI AFTER = createURI("after");

    public static final URI BEFORE = createURI("before");

    public static final URI DAY = createURI("day");

    public static final URI DAY_OF_WEEK = createURI("dayOfWeek");

    public static final URI DAY_OF_YEAR = createURI("dayOfYear");

    public static final URI DAYS = createURI("days");

    public static final URI HAS_BEGINNING = createURI("hasBeginning");

    public static final URI HAS_DATE_TIME_DESCRIPTION = createURI("hasDateTimeDescription");

    public static final URI HAS_DURATION_DESCRIPTION = createURI("hasDurationDescription");

    public static final URI HAS_END = createURI("hasEnd");

    public static final URI HOUR = createURI("hour");

    public static final URI HOURS = createURI("hours");

    public static final URI IN_DATE_TIME = createURI("inDateTime");

    public static final URI INSIDE = createURI("inside");

    public static final URI INTERVAL_AFTER = createURI("intervalAfter");

    public static final URI INTERVAL_BEFORE = createURI("intervalBefore");

    public static final URI INTERVAL_CONTAINS = createURI("intervalContains");

    public static final URI INTERVAL_DURING = createURI("intervalDuring");

    public static final URI INTERVAL_EQUALS = createURI("intervalEquals");

    public static final URI INTERVAL_FINISHED_BY = createURI("intervalFinishedBy");

    public static final URI INTERVAL_FINISHES = createURI("intervalFinishes");

    public static final URI INTERVAL_MEETS = createURI("intervalMeets");

    public static final URI INTERVAL_MET_BY = createURI("intervalMetBy");

    public static final URI INTERVAL_OVERLAPPED_BY = createURI("intervalOverlappedBy");

    public static final URI INTERVAL_OVERLAPS = createURI("intervalOverlaps");

    public static final URI INTERVAL_STARTED_BY = createURI("intervalStartedBy");

    public static final URI INTERVAL_STARTS = createURI("intervalStarts");

    public static final URI IN_XSD_DATE_TIME = createURI("inXSDDateTime");

    public static final URI MINUTE = createURI("minute");

    public static final URI MINUTES = createURI("minutes");

    public static final URI MONTH = createURI("month");

    public static final URI MONTHS = createURI("months");

    public static final URI SECOND = createURI("second");

    public static final URI SECONDS = createURI("seconds");

    public static final URI TIME_ZONE = createURI("timeZone");

    public static final URI UNIT_TYPE = createURI("unitType");

    public static final URI WEEK = createURI("week");

    public static final URI WEEKS = createURI("weeks");

    public static final URI XSD_DATE_TIME = createURI("xsdDateTime");

    public static final URI YEAR = createURI("year");

    public static final URI YEARS = createURI("years");

    // INDIVIDUALS

    public static final URI UNIT_SECOND = createURI("unitSecond");

    public static final URI UNIT_MINUTE = createURI("unitMinute");

    public static final URI UNIT_HOUR = createURI("unitHour");

    public static final URI UNIT_DAY = createURI("unitDay");

    public static final URI UNIT_WEEK = createURI("unitWeek");

    public static final URI UNIT_MONTH = createURI("unitMonth");

    public static final URI UNIT_YEAR = createURI("unitYear");

    public static final URI MONDAY = createURI("Monday");

    public static final URI TUESDAY = createURI("Tuesday");

    public static final URI WEDNESDAY = createURI("Wednesday");

    public static final URI THURSDAY = createURI("Thursday");

    public static final URI FRIDAY = createURI("Friday");

    public static final URI SATURDAY = createURI("Saturday");

    public static final URI SUNDAY = createURI("Sunday");

    // HELPER METHODS

    private static URI createURI(final String localName) {
        return ValueFactoryImpl.getInstance().createURI(NAMESPACE, localName);
    }

    private OWLTIME() {
    }

}
