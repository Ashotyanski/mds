package yandex.com.mds.hw.colors;

import android.content.SharedPreferences;
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
                    try {
                        exporter.importColors(file);
                        Log.d(TAG, "Colors imported from " + file);
                        Toast.makeText(getActivity(), R.string.success_color_import, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.d(TAG, "Could not import colors from " + file);
                        e.printStackTrace();
                        Toast.makeText(getActivity(), R.string.error_color_import, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            Preference exportPref = findPreference("export_colors");
            exportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String file = getImportExportFilename();
                    try {
                        exporter.exportColors(file);
                        Log.d(TAG, "Colors exported to " + file);
                        Toast.makeText(getActivity(), R.string.success_color_export, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "Could not export colors to " + file);
                        Toast.makeText(getActivity(), R.string.error_colors_export, Toast.LENGTH_SHORT).show();
                    }
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
