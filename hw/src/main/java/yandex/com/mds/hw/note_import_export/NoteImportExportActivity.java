package yandex.com.mds.hw.note_import_export;

import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.utils.NotificationUtils;

public class NoteImportExportActivity extends AppCompatActivity {
    private static final String TAG = NoteImportExportActivity.class.getName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new NoteImportExportFragment())
                .commit();
    }

    public static class NoteImportExportFragment extends PreferenceFragment {
        public static final String SHARED_PREFERENCES_NAME = "notes_import_export";
        private NoteImporterExporter exporter;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
            addPreferencesFromResource(R.xml.pref_note);
            exporter = NoteImporterExporter.getInstance(getActivity());
            exporter.setExportListener(new NoteImporterExporter.OnColorsExportListener() {
                @Override
                public void OnColorsExport(int result) {
                    if (result == NoteImporterExporter.SUCCESS_FLAG) {
                        Notification.Builder builder = NotificationUtils
                                .initNotificationBuilder(R.drawable.ic_import_export, " Colors export", "Colors exported");
                        builder.setProgress(0, 0, false);
                        NotificationUtils.send(builder.build(), 2);

                        Log.d(TAG, "Notes exported to " + getImportExportFilename());
                        Toast.makeText(getActivity(), R.string.success_notes_export, Toast.LENGTH_SHORT).show();
                    } else {
                        Notification.Builder builder = NotificationUtils
                                .initNotificationBuilder(R.drawable.ic_import_export, " Notes export", "Export failed");
                        builder.setProgress(0, 0, false);
                        NotificationUtils.send(builder.build(), 2);

                        Log.d(TAG, "Could not export notes to " + getImportExportFilename());
                        Toast.makeText(getActivity(), R.string.error_notes_export, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            exporter.setImportListener(new NoteImporterExporter.OnColorsImportListener() {
                @Override
                public void OnColorsImport(int result) {
                    if (result == NoteImporterExporter.SUCCESS_FLAG) {
                        Notification.Builder builder = NotificationUtils
                                .initNotificationBuilder(R.drawable.ic_import_export, " Notes import", "Notes imported");
                        builder.setProgress(0, 0, false);
                        NotificationUtils.send(builder.build(), 1);

                        Log.d(TAG, "Notes imported from " + getImportExportFilename());
                        Toast.makeText(getActivity(), R.string.success_notes_import, Toast.LENGTH_SHORT).show();
                    } else {
                        Notification.Builder builder = NotificationUtils
                                .initNotificationBuilder(R.drawable.ic_import_export, " Notes import", "Import failed");
                        builder.setProgress(0, 0, false);
                        NotificationUtils.send(builder.build(), 1);

                        Log.d(TAG, "Could not import notes from " + getImportExportFilename());
                        Toast.makeText(getActivity(), R.string.error_notes_import, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Preference importPref = findPreference("import_notes");
            importPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Notification.Builder builder = NotificationUtils
                            .initNotificationBuilder(R.drawable.ic_import_export, " Notes import", "Importing notes");
                    builder.setProgress(0, 0, true);
                    NotificationUtils.send(builder.build(), 1);

                    String file = getImportExportFilename();
                    exporter.importColors(file);
                    getActivity().setResult(RESULT_OK);
                    return true;
                }
            });
            final Preference exportPref = findPreference("export_notes");
            exportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Notification.Builder builder = NotificationUtils
                            .initNotificationBuilder(R.drawable.ic_import_export, " Note export", "Exporting notes");
                    builder.setProgress(0, 0, true);
                    NotificationUtils.send(builder.build(), 2);

                    String file = getImportExportFilename();
                    exporter.exportColors(file);
                    return true;
                }
            });
        }

        private String getImportExportFilename() {
            SharedPreferences s = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
            return s.getString("import_export_file", "notes.json");
        }
    }
}
