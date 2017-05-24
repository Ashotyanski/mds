package yandex.com.mds.hw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import yandex.com.mds.hw.notes.synchronizer.NoteSynchronizationService;
import yandex.com.mds.hw.notes.synchronizer.NoteSynchronizer;
import yandex.com.mds.hw.notes.synchronizer.SyncConflictFragment;
import yandex.com.mds.hw.utils.NetworkUtils;

import static yandex.com.mds.hw.notes.NotesFragment.getCurrentUserId;
import static yandex.com.mds.hw.notes.NotesFragment.setCurrentUserId;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SyncConflictFragment.OnAllConflictsResolvedListener {
    private static final String TAG = MainActivity.class.getName();

    private DrawerLayout drawer;
    private TextView userIdView;

    private NoteSynchronizer synchronizer;

    private NavigationManager navigationManager;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationManager = new NavigationManager(getSupportFragmentManager());
//        navigationManager.showNotes();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        synchronizer = NoteSynchronizer.getInstance();

        initDrawer();
    }

    private void initDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                initDrawerButton();
            }
        });

        userIdView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_id);
        userIdView.setText(String.format("%s #%d", "User", getCurrentUserId(this)));
        userIdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(v.getContext());
                editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                editText.setText(String.valueOf(getCurrentUserId(MainActivity.this)));
                AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                        .setTitle("Select user id")
                        .setView(editText)
                        .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int id = Integer.parseInt(editText.getText().toString());
                                if (getCurrentUserId(MainActivity.this) != id) {
                                    setCurrentUserId(MainActivity.this, id);
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

    private void initDrawerButton() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            toggle.setDrawerIndicatorEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);// show back button
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        } else {
            //show hamburger
            toggle.setDrawerIndicatorEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            toggle.syncState();
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawer.openDrawer(GravityCompat.START);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initDrawerButton();
    }

    @Override
    public void onBackPressed() {
        navigationManager.navigateBack(this);
    }

    private void switchUser(int userId) {
        syncNotes(userId);
    }

    private void syncNotes(int userId) {
        if (NetworkUtils.isConnected()) {
            Toast.makeText(this, R.string.sync_started_message, Toast.LENGTH_SHORT).show();
            NoteSynchronizationService.startNoteSynchronizer(this, userId);
        } else {
            Toast.makeText(this, R.string.connectivity_absent_message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_notes: {
                navigationManager.showNotes();
                drawer.closeDrawers();
                return true;
            }
            case R.id.action_sync: {
                syncNotes(getCurrentUserId(this));
                drawer.closeDrawers();
                return true;
            }
            case R.id.action_import_export: {
                navigationManager.showNotesImportExport();
                drawer.closeDrawers();
                return true;
            }
            case R.id.action_delete_sync_cache: {
                synchronizer.clearCache();
                drawer.closeDrawers();
                return true;
            }
        }
        return false;
    }

    public NavigationManager getNavigationManager() {
        return navigationManager;
    }

    @Override
    public void onAllConflictsResolved() {
//        loadColors();
    }

    public static class NetworkBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, R.string.connectivity_changed_message, Toast.LENGTH_SHORT).show();
            if (NetworkUtils.isConnected())
                NoteSynchronizationService.startNoteSynchronizer(context, getCurrentUserId(context));
        }
    }
}