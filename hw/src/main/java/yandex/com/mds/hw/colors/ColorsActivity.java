package yandex.com.mds.hw.colors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.color_edit.ColorEditActivity;
import yandex.com.mds.hw.color_import_export.ColorImportExportActivity;
import yandex.com.mds.hw.colors.query.Query;
import yandex.com.mds.hw.colors.query.presenters.QueryPresenter;
import yandex.com.mds.hw.colors.synchronizer.ConflictNotes;
import yandex.com.mds.hw.colors.synchronizer.NoteSynchronizationService;
import yandex.com.mds.hw.colors.synchronizer.NoteSynchronizer;
import yandex.com.mds.hw.colors.synchronizer.SyncConflictFragment;
import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.db.ColorDaoImpl;
import yandex.com.mds.hw.network.NoteService;
import yandex.com.mds.hw.network.ServiceGenerator;
import yandex.com.mds.hw.utils.NetworkUtils;

import static yandex.com.mds.hw.colors.synchronizer.NoteSynchronizationService.KEY_CONFLICT_NOTES;
import static yandex.com.mds.hw.colors.synchronizer.NoteSynchronizationService.KEY_ILLEGAL_FORMAT;

public class ColorsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = ColorsActivity.class.getName();
    public static final int COLOR_EDIT_REQUEST_CODE = 1;
    public static final int COLOR_IMPORT_EXPORT_REQUEST_CODE = 2;
    private static final String QUERY_BUNDLE_KEY = "query";
    private static final String USER_ID_BUNDLE_KEY = "user_id";
    private static final String CURRENT_POSITION = "currentPosition";

    private ColorDao colorDao = new ColorDaoImpl();
    private ColorBulkInserter bulkInserter;

    private ListView listView;
    private QueryPresenter presenter;
    private ColorLoaderAdapter colorLoaderAdapter;
    private DrawerLayout drawer;
    private TextView userIdView;

    private NoteService noteService;
    private NoteSynchronizer synchronizer;
    private BroadcastReceiver syncCompleteReceiver;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        syncCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                final Bundle extras = intent.getExtras();
                if (extras != null) {
                    if (extras.getBoolean(KEY_ILLEGAL_FORMAT)) {
                        Snackbar.make(listView, R.string.sync_format_illegal, Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    if (extras.<ConflictNotes>getParcelableArrayList(KEY_CONFLICT_NOTES) != null) {
                        Snackbar snack = Snackbar.make(listView, R.string.sync_conflict_message, Snackbar.LENGTH_INDEFINITE);
                        snack.setAction(R.string.sync_conflict_action_resolve, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ColorsActivity.this.getSupportFragmentManager()
                                                .beginTransaction()
                                                .add(SyncConflictFragment.newInstance(
                                                        extras.<ConflictNotes>getParcelableArrayList(KEY_CONFLICT_NOTES)), "CONFLICT"
                                                )
                                                .commit();
                                    }
                                }
                        );
                        snack.show();
                    }
                }
                loadColors();
            }
        };
        noteService = ServiceGenerator.createService(NoteService.class);
        synchronizer = NoteSynchronizer.getInstance();

        initDrawer(toolbar);

        listView = (ListView) findViewById(R.id.list);
        CursorAdapter adapter = new ColorListAdapter(this, null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int colorId = ((ColorListAdapter.ViewHolder) view.getTag()).getId();
                showColorEditActivity(colorId, getCurrentUserId(ColorsActivity.this));
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorEditActivity(-1, getCurrentUserId(ColorsActivity.this));
            }
        });

        presenter = new QueryPresenter(this, new QueryPresenter.OnApplyQueryListener() {
            @Override
            public void onApply(Query query) {
                loadColors(query, getCurrentUserId(ColorsActivity.this));
            }
        }, (LinearLayout) findViewById(R.id.query));

        loadColors();
    }

    private void initDrawer(Toolbar toolbar) {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        userIdView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_id);
        userIdView.setText(String.format("%s #%d", "User", getCurrentUserId(this)));
        userIdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(v.getContext());
                editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                editText.setText(String.valueOf(getCurrentUserId(ColorsActivity.this)));
                AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                        .setTitle("Select user id")
                        .setView(editText)
                        .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int id = Integer.parseInt(editText.getText().toString());
                                if (getCurrentUserId(ColorsActivity.this) != id) {
                                    setCurrentUserId(ColorsActivity.this, id);
                                    userIdView.setText(String.format("%s #%d", "User", id));
                                    switchUser(id);
                                }
                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(syncCompleteReceiver, new IntentFilter(NoteSynchronizer.SYNC_COMPLETE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(syncCompleteReceiver);
    }

    private void switchUser(int userId) {
        loadColors(null, userId);
        syncNotes(userId);
    }

    private void syncNotes(int userId) {
        if (NetworkUtils.isConnected()) {
            Toast.makeText(this, "Synchronization started", Toast.LENGTH_SHORT).show();
            NoteSynchronizationService.startNoteSynchronizer(this, userId);
        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void showColorEditActivity(int colorId, int userId) {
        startActivityForResult(ColorEditActivity.getInstance(this, colorId, userId), COLOR_EDIT_REQUEST_CODE);
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
        if (presenter.isShown() && !presenter.isTouched(ev.getX(), ev.getY()) && !drawer.isDrawerOpen(GravityCompat.START)) {
            presenter.close();
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter: {
                presenter.toggle();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync: {
                syncNotes(getCurrentUserId(this));
                return true;
            }
            case R.id.action_delete_all: {
//                colorDao.deleteColors();
                synchronizer.clearCache();
                loadColors();
                return true;
            }
            case R.id.action_import_export: {
                Intent intent = new Intent(this, ColorImportExportActivity.class);
                startActivityForResult(intent, COLOR_IMPORT_EXPORT_REQUEST_CODE);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loadColors();
    }

    private void loadColors() {
        loadColors(presenter.getQuery(), getCurrentUserId(this));
    }

    private void loadColors(Query query, int userId) {
        if (colorLoaderAdapter == null) colorLoaderAdapter = new ColorLoaderAdapter();
        if (getSupportLoaderManager().getLoader(1) == null)
            getSupportLoaderManager().initLoader(1, getColorLoaderBundle(query, userId), colorLoaderAdapter).forceLoad();
        else
            getSupportLoaderManager().restartLoader(1, getColorLoaderBundle(query, userId), colorLoaderAdapter).forceLoad();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.close();
        outState.putInt(CURRENT_POSITION, listView.getFirstVisiblePosition());
    }

    private Bundle getColorLoaderBundle(Query query, int userId) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(QUERY_BUNDLE_KEY, query);
        bundle.putInt(USER_ID_BUNDLE_KEY, userId);
        return bundle;
    }

    public static int getCurrentUserId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("USER", MODE_PRIVATE);
        return preferences.getInt("USER_ID", 0);
    }

    public static void setCurrentUserId(Context context, int id) {
        SharedPreferences preferences = context.getSharedPreferences("USER", MODE_PRIVATE);
        preferences.edit().putInt("USER_ID", id).apply();
    }


    private class ColorLoaderAdapter implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<Cursor>(ColorsActivity.this) {
                @Override
                public Cursor loadInBackground() {
                    Query query = args.getParcelable(QUERY_BUNDLE_KEY);
                    int userId = args.getInt(USER_ID_BUNDLE_KEY);
                    return colorDao.getColorsCursor(query, userId);
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

    public static class NetworkBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Connectivity changed", Toast.LENGTH_SHORT).show();
            if (NetworkUtils.isConnected())
                NoteSynchronizationService.startNoteSynchronizer(context, getCurrentUserId(context));
        }
    }
}