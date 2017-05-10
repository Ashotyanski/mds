package yandex.com.mds.hw.color_import_export;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import yandex.com.mds.hw.R;

public class ColorImportExportActivity extends AppCompatActivity {
    private static final String TAG = ColorImportExportActivity.class.getName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ColorImportExportFragment())
                .commit();
    }

    public static class ColorImportExportFragment extends PreferenceFragment {
        public static final String SHARED_PREFERENCES_NAME = "colors_import_export";
        private ColorImporterExporter exporter;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
            addPreferencesFromResource(R.xml.pref_color);
            exporter = ColorImporterExporter.getInstance(getActivity());
            exporter.setExportListener(new ColorImporterExporter.OnColorsExportListener() {
                @Override
                public void OnColorsExport(int result) {
                    if (result == ColorImporterExporter.SUCCESS_FLAG) {
                        Log.d(TAG, "Colors exported to " + getImportExportFilename());
                        Toast.makeText(getActivity(), R.string.success_colors_export, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Could not export colors to " + getImportExportFilename());
                        Toast.makeText(getActivity(), R.string.error_colors_export, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            exporter.setImportListener(new ColorImporterExporter.OnColorsImportListener() {
                @Override
                public void OnColorsImport(int result) {
                    if (result == ColorImporterExporter.SUCCESS_FLAG) {
                        Log.d(TAG, "Colors imported from " + getImportExportFilename());
                        Toast.makeText(getActivity(), R.string.success_colors_import, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Could not import colors from " + getImportExportFilename());
                        Toast.makeText(getActivity(), R.string.error_colors_import, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Preference importPref = findPreference("import_colors");
            importPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String file = getImportExportFilename();
                    exporter.importColors(file);
                    getActivity().setResult(RESULT_OK);
                    return true;
                }
            });
            final Preference exportPref = findPreference("export_colors");
            exportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String file = getImportExportFilename();
                    exporter.exportColors(file);
                    getActivity().setResult(RESULT_OK);
                    return true;
                }
            });
        }

        private String getImportExportFilename() {
            SharedPreferences s = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
            return s.getString("import_export_file", "colors.json");
        }
    }
}
