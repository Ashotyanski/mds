package yandex.com.mds.hw.notes.query.clauses;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClausesConstants {
    public static final List<String> DATE_FILTER_TYPES = Collections.unmodifiableList(
            Arrays.asList("exact date", "date interval"));
    public static final List<String> DATE_FILTER_FIELDS = Collections.unmodifiableList(
            Arrays.asList("creation date", "last modification date", "last view date"));

    public static final List<String> SORT_FIELDS = Collections.unmodifiableList(
            Arrays.asList("title", "creation date", "last modification date", "last view date"));
    public static final List<String> SORT_ORDER = Collections.unmodifiableList(
            Arrays.asList("descending", "ascending"));
}
