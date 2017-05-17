package yandex.com.mds.hw.note_import_export;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import yandex.com.mds.hw.MainActivity;
import yandex.com.mds.hw.R;
import yandex.com.mds.hw.utils.NotificationUtils;

public class NoteImportExportFragment extends PreferenceFragmentCompat {
    private static final String TAG = NoteImportExportFragment.class.getName();
    public static final String SHARED_PREFERENCES_NAME = "notes_import_export";
    private NoteImporterExporter exporter;

    public NoteImportExportFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_note, rootKey);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar toolbar = ((MainActivity) getActivity()).getSupportActionBar();
        toolbar.setTitle("Import/Export notes");
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
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
                getActivity().setResult(Activity.RESULT_OK);
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
        SharedPreferences s = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return s.getString("import_export_file", "notes.json");
    }
}
