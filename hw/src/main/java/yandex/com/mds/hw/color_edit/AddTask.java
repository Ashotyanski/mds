package yandex.com.mds.hw.color_edit;

import android.os.AsyncTask;

import yandex.com.mds.hw.colors.synchronizer.NoteSynchronizer;
import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.models.ColorRecord;

public class AddTask extends AsyncTask<ColorRecord, Void, Void> {
    private ColorDao colorDao;
    private NoteSynchronizer synchronizer;

    public AddTask(ColorDao colorDao) {
        this.colorDao = colorDao;
        synchronizer = NoteSynchronizer.getInstance();
    }

    @Override
    protected Void doInBackground(ColorRecord[] params) {
        ColorRecord record = params[0];
        long newId = colorDao.addColor(record);
        record.setId((int) newId);
        synchronizer.add(record);
        return null;
    }
}
