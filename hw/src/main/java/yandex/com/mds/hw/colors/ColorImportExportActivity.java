package yandex.com.mds.hw.colors;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.db.ColorImporterExporter;

public class ColorImportExportActivity extends AppCompatActivity {
    private static final String TAG = ColorImportExportActivity.class.getName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .add(android.R.id.content, new ColorImportExportFragment())
                .commit();
    }

    public static class ColorImportExportFragment extends PreferenceFragment {
        public static final String SHARED_PREFERENCES_NAME = "colors_import_export";
        private ColorImporterExporter exporter;
        private ColorImportTask importTask;
        private ColorExportTask exportTask;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
            addPreferencesFromResource(R.xml.pref_color);
            exporter = new ColorImporterExporter(getActivity());
            Preference importPref = findPreference("import_colors");
            importPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String file = getImportExportFilename();
                    if (importTask != null && (importTask.getStatus() == AsyncTask.Status.PENDING || importTask.getStatus() == AsyncTask.Status.RUNNING)) {
                        importTask.cancel(true);
                    }
                    importTask = new ColorImportTask(file);
                    importTask.execute();
                    getActivity().setResult(RESULT_OK);
                    return true;
                }
            });
            Preference exportPref = findPreference("export_colors");
            exportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String file = getImportExportFilename();
                    if (exportTask != null && (exportTask.getStatus() == AsyncTask.Status.PENDING || exportTask.getStatus() == AsyncTask.Status.RUNNING)) {
                        exportTask.cancel(true);
                    }
                    exportTask = new ColorExportTask(file);
                    exportTask.execute();
                    getActivity().setResult(RESULT_OK);
                    return true;
                }
            });
        }

        private String getImportExportFilename() {
            SharedPreferences s = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
            return s.getString("import_export_file", "colors.json");
        }

        private class ColorImportTask extends AsyncTask<String, Void, Void> {
            private String filename;

            ColorImportTask(String filename) {
                this.filename = filename;
            }

            @Override
            protected Void doInBackground(String... params) {
                try {
                    exporter.importColors(filename);
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                Log.d(TAG, "Could not import colors from " + filename);
                Toast.makeText(getActivity(), R.string.error_colors_import, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d(TAG, "Colors imported from " + filename);
                Toast.makeText(getActivity(), R.string.success_colors_import, Toast.LENGTH_SHORT).show();
            }
        }

        private class ColorExportTask extends AsyncTask<String, Void, Void> {
            private String filename;

            public ColorExportTask(String filename) {
                this.filename = filename;
            }

            @Override
            protected Void doInBackground(String... params) {
                try {
                    exporter.exportColors(filename);
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                Log.d(TAG, "Could not export colors to " + filename);
                Toast.makeText(getActivity(), R.string.error_colors_export, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d(TAG, "Colors exported to " + filename);
                Toast.makeText(getActivity(), R.string.success_colors_export, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
