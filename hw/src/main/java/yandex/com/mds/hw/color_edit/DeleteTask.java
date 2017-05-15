package yandex.com.mds.hw.color_edit;

import android.os.AsyncTask;

import yandex.com.mds.hw.db.ColorDao;

public class DeleteTask extends AsyncTask<Integer, Void, Void> {
    private ColorDao colorDao;

    public DeleteTask(ColorDao colorDao) {
        this.colorDao = colorDao;
    }

    @Override
    protected Void doInBackground(Integer[] params) {
        colorDao.deleteColor(params[0]);
        return null;
    }
}
