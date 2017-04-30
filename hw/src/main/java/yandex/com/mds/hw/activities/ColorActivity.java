package yandex.com.mds.hw.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

import yandex.com.mds.hw.ColorDatabaseHelper;
import yandex.com.mds.hw.R;
import yandex.com.mds.hw.colorpicker.ColorPickerView;
import yandex.com.mds.hw.colorpicker.colorview.EditableColorView;
import yandex.com.mds.hw.fragments.ColorPickerDialog;
import yandex.com.mds.hw.models.ColorRecord;

public class ColorActivity extends AppCompatActivity implements ColorPickerDialog.OnColorSavedListener {
    public static final String ID = "id";
    public static final String COLOR = "color";
    public static final String DEFAULT_COLOR = "default_color";
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";
    ColorDatabaseHelper dbHelper = new ColorDatabaseHelper(this);

    EditText titleView;
    EditText descriptionView;
    EditableColorView colorView;
    Button saveButton;
    ColorRecord colorRecord;

    public static Intent getInstance(Context c, int id) {
        Intent intent = new Intent(c, ColorActivity.class);
        intent.putExtra(ID, id);
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
        if (extras != null && extras.getInt(ID, -1) >= 0) {
            colorRecord = ColorDatabaseHelper.fromCursor(dbHelper.getColor(extras.getInt(ID)));
            fillForm(colorRecord.getTitle(), colorRecord.getDescription(), colorRecord.getColor(), colorRecord.getColor());
            getSupportActionBar().setTitle(R.string.title_color_edit);
        } else {
            getSupportActionBar().setTitle(R.string.title_color_create);
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
                if (colorRecord != null) {
                    colorRecord.setTitle(titleView.getText().toString());
                    colorRecord.setDescription(descriptionView.getText().toString());
                    colorRecord.setColor(colorView.getColor());
                    colorRecord.setLastModificationDate(new Date());
                    dbHelper.saveColor(colorRecord);
                } else {
                    colorRecord = new ColorRecord();
                    colorRecord.setTitle(titleView.getText().toString());
                    colorRecord.setDescription(descriptionView.getText().toString());
                    colorRecord.setColor(colorView.getColor());
                    colorRecord.setCreationDate(new Date());
                    dbHelper.addColor(colorRecord);
                }
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
            if (colorRecord != null)
                dbHelper.deleteColor(colorRecord.getId());
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
