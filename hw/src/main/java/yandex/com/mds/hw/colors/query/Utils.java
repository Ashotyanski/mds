package yandex.com.mds.hw.colors.query;

import java.util.HashMap;

import yandex.com.mds.hw.colors.query.clauses.ClausesConstants;

import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.CREATION_DATE;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.LAST_MODIFICATION_DATE;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.LAST_VIEW_DATE;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.TITLE;

public class Utils {
    private static final HashMap<String, String> fieldMapper = new HashMap<>();

    static {
        fieldMapper.put("creation date", CREATION_DATE);
        fieldMapper.put("last modification date", LAST_MODIFICATION_DATE);
        fieldMapper.put("last view date", LAST_VIEW_DATE);
        fieldMapper.put("title", TITLE);
    }

    public static int getDateFilterFieldPosition(String field) {
        for (int i = 0; i < ClausesConstants.DATE_FILTER_FIELDS.length; i++) {
            if (field.equals(ClausesConstants.DATE_FILTER_FIELDS[i]))
                return i;
        }
        return -1;
    }

    public static int getSortFieldPosition(String field) {
        for (int i = 0; i < ClausesConstants.SORT_FIELDS.length; i++) {
            if (field.equals(ClausesConstants.SORT_FIELDS[i]))
                return i;
        }
        return -1;
    }

    public static String getDbColumnFromField(String field) {
        return fieldMapper.get(field);
    }
}
