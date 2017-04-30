package yandex.com.mds.hw3.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import yandex.com.mds.hw3.ColorDatabaseHelper;
import yandex.com.mds.hw3.R;
import yandex.com.mds.hw3.colorpicker.ColorPickerView;
import yandex.com.mds.hw3.colorpicker.colorview.EditableColorView;
import yandex.com.mds.hw3.fragments.ColorPickerDialog;
import yandex.com.mds.hw3.models.Color;

public class ColorActivity extends AppCompatActivity implements ColorPickerDialog.OnColorSavedListener {
    public static final String COLOR = "color";
    public static final String DEFAULT_COLOR = "default_color";
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";
    ColorDatabaseHelper dbHelper = new ColorDatabaseHelper(this);

    EditText titleView;
    EditText descriptionView;
    EditableColorView colorView;
    Button saveButton;
    Color color;

    public static Intent getInstance(Context c, @Nullable Color color) {
        Intent intent = new Intent(c, ColorActivity.class);
        if (color != null)
            intent.putExtra("color", color);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);
        titleView = (EditText) findViewById(R.id.title);
        descriptionView = (EditText) findViewById(R.id.description);
        colorView = (EditableColorView) findViewById(R.id.color);
        saveButton = (Button) findViewById(R.id.save_button);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null && (color = extras.getParcelable("color")) != null) {
            fillForm(color.getTitle(), color.getDescription(), color.getColor(), color.getColor());
            getSupportActionBar().setTitle("Edit color");
        } else {
            getSupportActionBar().setTitle("Create color");
        }

        colorView.setOnPickListener(new ColorPickerView.OnPickListener() {
            @Override
            public void onPick(int color) {
                ColorPickerDialog pickerDialog = ColorPickerDialog.newInstance(color);
                pickerDialog.show(getSupportFragmentManager(), "COLOR_PICKER");
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (color != null)
                    dbHelper.saveColor(color.getId(), titleView.getText().toString(),
                            descriptionView.getText().toString(), colorView.getColor());
                else
                    dbHelper.addColor(titleView.getText().toString(),
                            descriptionView.getText().toString(), colorView.getColor());
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_color, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            if (color != null)
                dbHelper.deleteColor(color.getId());
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fillForm(
                savedInstanceState.getString(TITLE),
                savedInstanceState.getString(DESCRIPTION),
                savedInstanceState.getInt(COLOR),
                savedInstanceState.getInt(DEFAULT_COLOR));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TITLE, titleView.getText().toString());
        outState.putString(DESCRIPTION, descriptionView.getText().toString());
        outState.putInt(COLOR, colorView.getColor());
        outState.putInt(DEFAULT_COLOR, colorView.getDefaultColor());
    }

    private void fillForm(String title, String description, int color, int defaultColor) {
        titleView.setText(title);
        descriptionView.setText(description);
        colorView.setDefaultColor(defaultColor);
        colorView.setColor(color);
    }

    @Override
    public void onColorSave(int color) {
        colorView.setDefaultColor(color);
        colorView.setColor(color);
    }
}
