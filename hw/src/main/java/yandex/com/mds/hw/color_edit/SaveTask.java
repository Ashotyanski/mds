package yandex.com.mds.hw.color_edit;

import android.os.AsyncTask;

import yandex.com.mds.hw.colors.synchronizer.NoteSynchronizer;
import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.models.ColorRecord;

public class SaveTask extends AsyncTask<ColorRecord, Void, Void> {
    private final NoteSynchronizer synchronizer;
    private ColorDao colorDao;

    public SaveTask(ColorDao colorDao) {
        this.colorDao = colorDao;
        synchronizer = NoteSynchronizer.getInstance();
    }

    @Override
    protected Void doInBackground(ColorRecord[] params) {
        colorDao.saveColor(params[0]);
        synchronizer.save(params[0]);
        return null;
    }
}
