package yandex.com.mds.hw.note_import_export;

import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import yandex.com.mds.hw.MainApplication;
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

    public static class NoteImportExportFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        public static final String SHARED_PREFERENCES_NAME = "notes_import_export";
        public static final String IMPORT_EXPORT_FILE_PREFERENCE = "import_export_file";
        public static final String DEFAULT_FILENAME = "notes.json";
        private ProgressDialog progressDialog;
        private NoteImporterExporter exporter;
        private String importExportFilename;

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            SharedPreferences s = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
            importExportFilename = s.getString(IMPORT_EXPORT_FILE_PREFERENCE, DEFAULT_FILENAME);
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
            addPreferencesFromResource(R.xml.pref_note);
            exporter = NoteImporterExporter.getInstance(getActivity());
            exporter.setImportListener(new NoteImporterExporter.OnColorsImportListener() {
                @Override
                public void OnColorsImport(NoteImporterExporter.ImportExportStatus status) {
                    Notification.Builder builder = NotificationUtils
                            .initNotificationBuilder(R.drawable.ic_import_export, "Notes import", status.message);
                    if (status.progress == 0.0) {
                        builder.setProgress(1, 0, false);
                        NotificationUtils.send(builder.build(), 1);

                        progressDialog.setMessage(status.message);
                        progressDialog.setProgress(0);
                        progressDialog.setMax(100);
                        if (getActivity() != null && !getActivity().isFinishing())
                            progressDialog.show();
                    } else if (status.progress == 1.0) {
                        builder.setProgress(0, 0, false);
                        NotificationUtils.send(builder.build(), 1);

                        progressDialog.dismiss();
                        showResultToast(false, true);
                    } else if (status.progress == -1.0) {
                        builder.setProgress(0, 0, false);
                        NotificationUtils.send(builder.build(), 1);
                        progressDialog.setProgress(0);
                        progressDialog.dismiss();
                        showResultToast(false, false);
                    } else {
                        builder.setProgress(100, (int) (status.progress * 100), false);
                        NotificationUtils.send(builder.build(), 1);

                        if (progressDialog.isShowing()) {
                            progressDialog.setMessage(status.message);
                            progressDialog.setProgress((int) (status.progress * 100));
                        }
                    }
                }
            });
            exporter.setExportListener(new NoteImporterExporter.OnColorsExportListener() {
                @Override
                public void OnColorsExport(NoteImporterExporter.ImportExportStatus status) {
                    Notification.Builder builder = NotificationUtils
                            .initNotificationBuilder(R.drawable.ic_import_export, "Colors export", status.message);
                    if (status.progress == 0.0) {
                        builder.setProgress(2, 0, false);
                        NotificationUtils.send(builder.build(), 2);

                        progressDialog.setMessage(status.message);
                        progressDialog.setProgress(0);
                        progressDialog.setMax(100);
                        if (getActivity() != null && !getActivity().isFinishing())
                            progressDialog.show();
                    } else if (status.progress == 1.0) {
                        builder.setProgress(0, 0, false);
                        NotificationUtils.send(builder.build(), 2);
                        progressDialog.setProgress(0);
                        progressDialog.dismiss();
                        showResultToast(true, true);
                    } else if (status.progress == -1.0) {
                        builder.setProgress(0, 0, false);
                        NotificationUtils.send(builder.build(), 2);

                        progressDialog.dismiss();
                        showResultToast(true, false);
                    } else {
                        builder.setProgress(100, (int) (status.progress * 100), false);
                        NotificationUtils.send(builder.build(), 2);

                        if (progressDialog.isShowing()) {
                            progressDialog.setMessage(status.message);
                            progressDialog.setProgress((int) (status.progress * 100));
                        }
                    }
                }
            });
            Preference importPref = findPreference("import_notes");
            importPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), R.string.started_notes_import, Toast.LENGTH_SHORT).show();
                    exporter.importColors(importExportFilename);
                    getActivity().setResult(RESULT_OK);
                    return true;
                }
            });
            final Preference exportPref = findPreference("export_notes");
            exportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), R.string.started_notes_export, Toast.LENGTH_SHORT).show();
                    exporter.exportColors(importExportFilename);
                    return true;
                }
            });
        }

        private void showResultToast(boolean isExport, boolean isSuccess) {
            Context context = MainApplication.getContext();
            if (isExport) {
                Toast.makeText(context, isSuccess ? R.string.success_notes_export :
                        R.string.error_notes_export, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, isSuccess ? R.string.success_notes_import :
                        R.string.error_notes_import, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.contentEquals(IMPORT_EXPORT_FILE_PREFERENCE)) {
                importExportFilename = sharedPreferences.getString(key, DEFAULT_FILENAME);
            }
        }
    }
}