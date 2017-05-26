package yandex.com.mds.hw.color_import_export;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

    public static class ColorImportExportFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        public static final String SHARED_PREFERENCES_NAME = "colors_import_export";
        private ColorImporterExporter exporter;
        private String importExportFilename;

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            SharedPreferences s = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
            importExportFilename = s.getString("import_export_file", "colors.json");
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
            addPreferencesFromResource(R.xml.pref_color);
            exporter = ColorImporterExporter.getInstance(getActivity());
            exporter.setExportListener(new ColorImporterExporter.OnColorsExportListener() {
                @Override
                public void OnColorsExport(int result) {
                    Activity activity = getActivity();
                    if (result == ColorImporterExporter.SUCCESS_FLAG) {
                        if (activity != null)
                            Toast.makeText(activity, R.string.success_colors_export, Toast.LENGTH_SHORT).show();
                    } else {
                        if (activity != null)
                            Toast.makeText(activity, R.string.error_colors_export, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            exporter.setImportListener(new ColorImporterExporter.OnColorsImportListener() {
                @Override
                public void OnColorsImport(int result) {
                    Activity activity = getActivity();
                    if (result == ColorImporterExporter.SUCCESS_FLAG) {
                        if (activity != null)
                            Toast.makeText(activity, R.string.success_colors_import, Toast.LENGTH_SHORT).show();
                    } else {
                        if (activity != null)
                            Toast.makeText(activity, R.string.error_colors_import, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Preference importPref = findPreference("import_colors");
            importPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), R.string.started_color_import, Toast.LENGTH_SHORT).show();
                    exporter.importColors(importExportFilename);
                    getActivity().setResult(RESULT_OK);
                    return true;
                }
            });
            final Preference exportPref = findPreference("export_colors");
            exportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), R.string.started_color_export, Toast.LENGTH_SHORT).show();
                    exporter.exportColors(importExportFilename);
                    return true;
                }
            });
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
            if (key.contentEquals("import_export_file")) {
                importExportFilename = sharedPreferences.getString(key, "colors.json");
            }
        }
    }
}
