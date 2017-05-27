package yandex.com.mds.hw.colors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.color_edit.ColorEditActivity;
import yandex.com.mds.hw.color_import_export.ColorImportExportActivity;
import yandex.com.mds.hw.color_import_export.ColorImporterExporter;
import yandex.com.mds.hw.colors.query.Query;
import yandex.com.mds.hw.colors.query.presenters.QueryPresenter;
import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.db.ColorDaoImpl;

public class ColorsActivity extends AppCompatActivity implements QueryPresenter.OnApplyQueryListener {
    private static final String TAG = ColorsActivity.class.getName();
    public static final int COLOR_EDIT_REQUEST_CODE = 1;
    public static final int COLOR_IMPORT_EXPORT_REQUEST_CODE = 2;
    private static final String QUERY_BUNDLE_KEY = "query";
    private static final String CURRENT_POSITION = "currentPosition";

    private ColorDao colorDao = new ColorDaoImpl();
    private ColorBulkInserter bulkInserter;

    private ListView listView;
    private QueryPresenter presenter;
    private ColorLoaderAdapter colorLoaderAdapter;
    private BroadcastReceiver importReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.list);
        CursorAdapter adapter = new ColorListAdapter(this, null);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int colorId = ((ColorListAdapter.ViewHolder) view.getTag()).getId();
                showColorEditActivity(colorId);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorEditActivity(-1);
            }
        });
        presenter = new QueryPresenter(this, this, (LinearLayout) findViewById(R.id.query));

        bulkInserter = ColorBulkInserter.getInstance(this);
        bulkInserter.setOnInsertFinishListener(new ColorBulkInserter.OnInsertFinishListener() {
            @Override
            public void onFinishInsert() {
                Toast.makeText(ColorsActivity.this, "Insertion done", Toast.LENGTH_SHORT).show();
                loadColors();
            }
        });

        importReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadColors();
            }
        };
        loadColors();
    }

    @Override
    public void onApply(Query query) {
        loadColors(query);
    }

    private void showColorEditActivity(int colorId) {
        startActivityForResult(ColorEditActivity.getInstance(this, colorId), COLOR_EDIT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COLOR_EDIT_REQUEST_CODE || requestCode == COLOR_IMPORT_EXPORT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                loadColors();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (presenter.isShown() && !presenter.isTouched(ev.getX(), ev.getY())) {
            presenter.close();
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(importReceiver, ColorImporterExporter.importIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(importReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter: {
                presenter.toggle();
                return true;
            }
            case R.id.action_import_export: {
                Intent intent = new Intent(this, ColorImportExportActivity.class);
                startActivityForResult(intent, COLOR_IMPORT_EXPORT_REQUEST_CODE);
                return true;
            }
            case R.id.action_bulk_add: {
                bulkInserter.start();
                return true;
            }
            case R.id.action_delete_all: {
                colorDao.deleteColors();
                loadColors();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loadColors();
    }

    private void loadColors() {
        Query query = presenter.getQuery();
        loadColors(query);
    }

    private void loadColors(Query query) {
        if (colorLoaderAdapter == null) colorLoaderAdapter = new ColorLoaderAdapter();
        if (getSupportLoaderManager().getLoader(1) == null)
            getSupportLoaderManager().initLoader(1, getColorLoaderBundle(query), colorLoaderAdapter).forceLoad();
        else
            getSupportLoaderManager().restartLoader(1, getColorLoaderBundle(query), colorLoaderAdapter).forceLoad();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.close();
        outState.putInt(CURRENT_POSITION, listView.getFirstVisiblePosition());
    }

    private Bundle getColorLoaderBundle(Query query) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(QUERY_BUNDLE_KEY, query);
        return bundle;
    }

    private class ColorLoaderAdapter implements LoaderManager.LoaderCallbacks<Cursor> {
        // Here we use simple AsyncTaskLoader instead of CursorLoader for sake of speed.
        // Further it can (should) be replaced by the mentioned, with subsequent use of
        // ContentProvider and ContentResolver for robust and flexible architecture
        @Override
        public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<Cursor>(ColorsActivity.this) {
                @Override
                public Cursor loadInBackground() {
                    Query query = args.getParcelable(QUERY_BUNDLE_KEY);
                    return colorDao.getColorsCursor(query);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            ((ColorListAdapter) listView.getAdapter()).changeCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

}