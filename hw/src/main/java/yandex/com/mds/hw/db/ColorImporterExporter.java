package yandex.com.mds.hw.db;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import yandex.com.mds.hw.models.ColorRecord;

import static yandex.com.mds.hw.utils.SerializationUtils.GSON;

public class ColorImporterExporter {
    private static final String TAG = ColorImporterExporter.class.getName();
    private ColorDao colorDao;
    private Context c;

    public ColorImporterExporter(Context c) {
        this.c = c;
        colorDao = new ColorDaoImpl();
    }

    public void exportColors(String filename) throws IOException {
        ColorRecord[] records = colorDao.getColors();
        writeColors(records, filename);
    }

    public void importColors(String filename) throws IOException {
        ColorRecord[] records = readColors(filename);
        colorDao.deleteColors();
        for (ColorRecord record : records)
            colorDao.addColor(record);
    }

    private void writeColors(ColorRecord[] colors, String filename) throws IOException {
        File exportFile = new File(c.getCacheDir(), filename);
        Log.d(TAG, "Writing data to " + exportFile);
        if (!exportFile.exists()) {
            exportFile.createNewFile();
        }
        FileWriter writer = new FileWriter(exportFile);
        GSON.toJson(colors, writer);
        writer.flush();
        writer.close();
    }

    private ColorRecord[] readColors(String filename) throws IOException {
        File importFile = new File(c.getCacheDir(), filename);
        Log.d(TAG, "Reading data from " + importFile);
        FileReader reader = new FileReader(importFile);
        return GSON.fromJson(reader, ColorRecord[].class);
    }

}
