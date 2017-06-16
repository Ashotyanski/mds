package yandex.com.mds.hw.notes.query;

import java.util.HashMap;

import yandex.com.mds.hw.notes.query.clauses.ClausesConstants;

import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.CREATION_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.LAST_MODIFICATION_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.LAST_VIEW_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.TITLE;

public class Utils {
    private static final HashMap<String, String> fieldMapper = new HashMap<>();

    static {
        fieldMapper.put("creation date", CREATION_DATE);
        fieldMapper.put("last modification date", LAST_MODIFICATION_DATE);
        fieldMapper.put("last view date", LAST_VIEW_DATE);
        fieldMapper.put("title", TITLE);
    }

    public static int getDateFilterFieldPosition(String field) {
        if (field != null)
            for (int i = 0; i < ClausesConstants.DATE_FILTER_FIELDS.size(); i++) {
                if (field.equals(ClausesConstants.DATE_FILTER_FIELDS.get(i)))
                    return i;
            }
        return -1;
    }

    public static int getSortFieldPosition(String field) {
        if (field != null)
            for (int i = 0; i < ClausesConstants.SORT_FIELDS.size(); i++) {
                if (field.equals(ClausesConstants.SORT_FIELDS.get(i)))
                    return i;
            }
        return -1;
    }

    public static String getDbColumnFromField(String field) {
        return fieldMapper.get(field);
    }
}
