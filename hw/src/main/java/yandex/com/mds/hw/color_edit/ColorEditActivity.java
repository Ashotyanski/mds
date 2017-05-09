package yandex.com.mds.hw.color_edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.colorpicker.ColorPickerDialog;
import yandex.com.mds.hw.colorpicker.ColorPickerView;
import yandex.com.mds.hw.colorpicker.colorview.EditableColorView;
import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.db.ColorDaoImpl;
import yandex.com.mds.hw.models.ColorRecord;
import yandex.com.mds.hw.utils.TimeUtils;

public class ColorEditActivity extends AppCompatActivity implements ColorPickerDialog.OnColorSavedListener {
    public static final String ID = "id";
    public static final String COLOR = "color";
    public static final String DEFAULT_COLOR = "default_color";
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";
    public static final String IMAGE_URL = "image_url";
    public static final String IS_VIEW_COUNTED = "isViewCounted";

    private ColorDao colorDao = new ColorDaoImpl();

    EditText titleView;
    EditText descriptionView;
    EditableColorView colorView;
    Button saveButton;
    UrlImageView urlImageView;
    ColorRecord colorRecord;

    private boolean isViewCounted = false;

    public static Intent getInstance(Context c, int id) {
        Intent intent = new Intent(c, ColorEditActivity.class);
        intent.putExtra(ID, id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);
        titleView = (EditText) findViewById(R.id.title);
        descriptionView = (EditText) findViewById(R.id.description);
        colorView = (EditableColorView) findViewById(R.id.filter_color);
        saveButton = (Button) findViewById(R.id.save_button);
        urlImageView = (UrlImageView) findViewById(R.id.url_image);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getInt(ID, -1) >= 0) {
            colorRecord = colorDao.getColor(extras.getInt(ID));
            fillForm(colorRecord);
            getSupportActionBar().setTitle(R.string.title_activity_color_edit);
            Log.d("ColorEditActivity", String.format("Created at %s, last edit at %s, last seen at %s,change to %s",
                    TimeUtils.formatDateTime(colorRecord.getCreationDate()),
                    TimeUtils.formatDateTime(colorRecord.getLastModificationDate()),
                    TimeUtils.formatDateTime(colorRecord.getLastViewDate()),
                    TimeUtils.formatDateTime(new Date())
            ));
            if (!isViewCounted) {
                colorRecord.setLastViewDate(new Date());
                colorDao.saveColor(colorRecord);
                isViewCounted = true;
            }
        } else {
            getSupportActionBar().setTitle(R.string.title_activity_color_create);
        }

        colorView.setOnPickListener(new ColorPickerView.OnPickListener() {
            @Override
            public void onPick(int color) {
                ColorPickerDialog dialog = new ColorPickerDialog(ColorEditActivity.this, color);
                dialog.setOnColorSavedListener(ColorEditActivity.this);
                dialog.show();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecord();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void saveRecord() {
        if (colorRecord != null) {
            colorRecord.setTitle(titleView.getText().toString());
            colorRecord.setDescription(descriptionView.getText().toString());
            colorRecord.setColor(colorView.getColor());
            colorRecord.setImageUrl(urlImageView.getUrl());
            colorRecord.setLastModificationDate(new Date());
            colorDao.saveColor(colorRecord);
        } else {
            colorRecord = new ColorRecord();
            colorRecord.setTitle(titleView.getText().toString());
            colorRecord.setDescription(descriptionView.getText().toString());
            colorRecord.setColor(colorView.getColor());
            colorRecord.setImageUrl(urlImageView.getUrl());
            colorRecord.setCreationDate(new Date());
            colorDao.addColor(colorRecord);
        }
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
                colorDao.deleteColor(colorRecord.getId());
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
                savedInstanceState.getInt(DEFAULT_COLOR),
                savedInstanceState.getString(IMAGE_URL));
        isViewCounted = savedInstanceState.getBoolean(IS_VIEW_COUNTED);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TITLE, titleView.getText().toString());
        outState.putString(DESCRIPTION, descriptionView.getText().toString());
        outState.putInt(COLOR, colorView.getColor());
        outState.putInt(DEFAULT_COLOR, colorView.getDefaultColor());
        outState.putString(IMAGE_URL, urlImageView.getUrl());
        outState.putBoolean(IS_VIEW_COUNTED, isViewCounted);
    }

    private void fillForm(ColorRecord record) {
        fillForm(record.getTitle(), record.getDescription(), record.getColor(), record.getColor(), record.getImageUrl());
    }

    private void fillForm(String title, String description, int color, int defaultColor, String url) {
        titleView.setText(title);
        descriptionView.setText(description);
        colorView.setDefaultColor(defaultColor);
        colorView.setColor(color);
        urlImageView.applyUrl(url);
    }

    @Override
    public void onColorSave(int color) {
        colorView.setDefaultColor(color);
        colorView.setColorToDefault();
    }
}
