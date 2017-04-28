package yandex.com.mds.hw3.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import yandex.com.mds.hw3.ColorDatabaseHelper;
import yandex.com.mds.hw3.R;
import yandex.com.mds.hw3.colorpicker.colorview.ColorView;
import yandex.com.mds.hw3.fragments.ColorPickerDialog;
import yandex.com.mds.hw3.models.Color;

public class ColorActivity extends AppCompatActivity implements ColorPickerDialog.OnColorSavedListener {
    public static final String COLOR = "color";
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";
    ColorDatabaseHelper dbHelper = new ColorDatabaseHelper(this);

    EditText titleView;
    EditText descriptionView;
    ColorView colorView;
    Button saveButton;
    Color color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);
        titleView = (EditText) findViewById(R.id.title);
        descriptionView = (EditText) findViewById(R.id.description);
        colorView = (ColorView) findViewById(R.id.color);
        saveButton = (Button) findViewById(R.id.save_button);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getExtras() != null) {
            color = getIntent().getExtras().getParcelable("color");
            fillForm(color.getTitle(), color.getDescription(), color.getColor());
            getSupportActionBar().setTitle("Edit color");
        } else {
            getSupportActionBar().setTitle("Create color");
        }

        colorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog pickerDialog = ColorPickerDialog.newInstance(colorView.getColor());
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
                savedInstanceState.getInt(COLOR));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TITLE, titleView.getText().toString());
        outState.putString(DESCRIPTION, descriptionView.getText().toString());
        outState.putInt(COLOR, colorView.getColor());

    }

    private void fillForm(String title, String description, int color) {
        titleView.setText(title);
        descriptionView.setText(description);
        colorView.setColor(color);
    }

    @Override
    public void onColorSave(int color) {
        colorView.setColor(color);
    }
}
