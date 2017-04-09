package yandex.com.mds.hw1;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends Activity {

    private static final String TAG = "ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        Log.d(TAG, "Created");

        // Let's create a container for our custom MainViews and a pair of buttons to add and remove them.
        // Thus we can see Views lifecycle from their creation to removal.

        MainViewGroup layout = (MainViewGroup) findViewById(R.id.view_container);

        Button add = (Button) findViewById(R.id.button);
        add.setOnClickListener(v -> layout.addView(new MainView(this, "Child view - " + (layout.getChildCount() + 1))));

        Button remove = (Button) findViewById(R.id.button2);
        remove.setOnClickListener(v -> {
            if (layout.getChildCount() > 0) {
                layout.removeViewAt(layout.getChildCount() - 1);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Started");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "Restarted");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroyed");
    }
}
