package yandex.com.mds.hw.colors;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import java.text.ParseException;

import yandex.com.mds.hw.ColorListAdapter;
import yandex.com.mds.hw.R;
import yandex.com.mds.hw.color_edit.ColorEditActivity;
import yandex.com.mds.hw.colors.query.Query;
import yandex.com.mds.hw.colors.query.presenters.QueryPresenter;
import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.db.ColorDaoImpl;

public class ColorsActivity extends AppCompatActivity implements QueryPresenter.OnApplyQueryListener {
    public static final int COLOR_REQUEST_CODE = 1;
    private static final String CURRENT_POSITION = "currentPosition";
    ColorDao colorDao = new ColorDaoImpl();

    private ListView listView;
    private QueryPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Cursor c = colorDao.getColorsCursor();
        CursorAdapter adapter = new ColorListAdapter(this, c);

        listView = (ListView) findViewById(R.id.list);
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
    }

    @Override
    public void onApply(Query query) {
        updateListAdapter(query);
    }

    private void showColorEditActivity(int colorId) {
        startActivityForResult(ColorEditActivity.getInstance(this, colorId), COLOR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COLOR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                updateListAdapter();
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
            presenter.closeQuery();
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter: {
                presenter.toggleQuery();
                return true;
            }
            case R.id.action_import_export: {
                Intent intent = new Intent(this, ColorImportExportActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        listView.smoothScrollToPosition(savedInstanceState.getInt(CURRENT_POSITION));
        updateListAdapter();
    }


    private void updateListAdapter() {
        try {
            Query query = presenter.getQuery();
            updateListAdapter(query);
        } catch (ParseException e) {
            updateListAdapter(null);
        }
    }

    private void updateListAdapter(Query query) {
        Cursor c = colorDao.getColorsCursor(query);
        ((ColorListAdapter) listView.getAdapter()).changeCursor(c);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.closeQuery();
        outState.putInt(CURRENT_POSITION, listView.getFirstVisiblePosition());
    }
}