package yandex.com.mds.hw.notes.synchronizer.conflicts;

import android.content.Context;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.utils.TimeUtils;

public class Utils {
    public static String getNotePresentation(Context context, Note note) {
        if (null != note) {
            String result = "";
            result += String.format("Title: %s%n", note.getTitle());
            result += String.format("Description: %s%n", note.getDescription());
            result += String.format("Color: %s%n", note.getColor());
            result += String.format("Image: %s%n", note.getImageUrl());
            result += String.format("Created at: %s%n", note.getCreationDate() != null ?
                    TimeUtils.IsoDateFormat.format(note.getCreationDate()) : "");
            result += String.format("Edited at: %s%n", note.getLastModificationDate() != null ?
                    TimeUtils.IsoDateFormat.format(note.getLastModificationDate()) : "");
            result += String.format("Viewed at: %s%n", note.getLastViewDate() != null ?
                    TimeUtils.IsoDateFormat.format(note.getLastViewDate()) : "");
            return result;
        } else {
            return context.getString(R.string.deleted);
        }
    }
}
