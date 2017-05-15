package yandex.com.mds.hw.color_edit;

import android.os.AsyncTask;

import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.models.ColorRecord;

public class AddTask extends AsyncTask<ColorRecord, Void, Void> {
    private ColorDao colorDao;

    public AddTask(ColorDao colorDao) {
        this.colorDao = colorDao;
    }

    @Override
    protected Void doInBackground(ColorRecord[] params) {
        colorDao.addColor(params[0]);
        return null;
    }
}
