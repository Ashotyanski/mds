package yandex.com.mds.hw3dev.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import yandex.com.mds.hw3dev.fragments.ColorActivityFragment;

public class ColorActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
            return;
        }
        if (savedInstanceState == null) {
            int id = getIntent().getExtras().getInt("id");
            ColorActivityFragment colorFragment = ColorActivityFragment.newInstance(id);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(android.R.id.content, colorFragment);
            ft.commit();

        }
    }
}
