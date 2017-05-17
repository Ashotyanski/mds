package yandex.com.mds.hw.color_edit;

import android.os.AsyncTask;

import yandex.com.mds.hw.colors.synchronizer.NoteSynchronizer;
import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.models.ColorRecord;

public class DeleteTask extends AsyncTask<Integer, Void, Void> {
    private ColorDao colorDao;
    private NoteSynchronizer synchronizer;

    public DeleteTask(ColorDao colorDao) {
        this.colorDao = colorDao;
        synchronizer = NoteSynchronizer.getInstance();
    }

    @Override
    protected Void doInBackground(Integer[] params) {
        ColorRecord record = colorDao.getColor(params[0]);
        colorDao.deleteColor(params[0]);
        synchronizer.delete(record);
        return null;
    }
}
