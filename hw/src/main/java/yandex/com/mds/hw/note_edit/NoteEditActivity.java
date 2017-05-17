package yandex.com.mds.hw.note_edit;

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
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.utils.TimeUtils;

public class NoteEditActivity extends AppCompatActivity {
    public static final String ID = "id";
    public static final String OWNER_ID = "owner_id";
    public static final String COLOR = "color";
    public static final String DEFAULT_COLOR = "default_color";
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";
    public static final String IMAGE_URL = "image_url";
    public static final String IS_VIEW_COUNTED = "isViewCounted";

    private NoteDao noteDao = new NoteDaoImpl();

    EditText titleView;
    EditText descriptionView;
    EditableColorView colorView;
    Button saveButton;
    UrlImageView urlImageView;
    Note note;

    SaveTask saveTask;
    AddTask addTask;
    DeleteTask deleteTask;

    private boolean isViewCounted = false;
    private int ownerId = -1;

    public static Intent getInstance(Context c, int id, int ownerId) {
        Intent intent = new Intent(c, NoteEditActivity.class);
        intent.putExtra(ID, id);
        intent.putExtra(OWNER_ID, ownerId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);
        titleView = (EditText) findViewById(R.id.title);
        descriptionView = (EditText) findViewById(R.id.description);
        colorView = (EditableColorView) findViewById(R.id.color);
        saveButton = (Button) findViewById(R.id.save_button);
        urlImageView = (UrlImageView) findViewById(R.id.url_image);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        if (savedInstanceState != null) {
            fillForm(savedInstanceState);
            isViewCounted = savedInstanceState.getBoolean(IS_VIEW_COUNTED);
        }

        if (extras != null) {
            if (extras.getInt(ID, -1) >= 0) {
                getSupportActionBar().setTitle(R.string.title_activity_note_edit);
                note = noteDao.getNote(extras.getInt(ID));
                ownerId = note.getOwnerId();
                if (savedInstanceState == null) {
                    fillForm(note);
                    if (!isViewCounted) {
                        note.setLastViewDate(new Date());
                        noteDao.saveNote(note);
                        isViewCounted = true;
                    }
                }
                Log.d("NoteEditActivity", String.format("Created at %s, last edit at %s, last seen at %s,change to %s",
                        TimeUtils.formatDateTime(note.getCreationDate()),
                        TimeUtils.formatDateTime(note.getLastModificationDate()),
                        TimeUtils.formatDateTime(note.getLastViewDate()),
                        TimeUtils.formatDateTime(new Date())
                ));
            } else {
                ownerId = extras.getInt(OWNER_ID, -1);
                getSupportActionBar().setTitle(R.string.title_activity_note_create);
            }
        } else {
            ownerId = -1;
            getSupportActionBar().setTitle(R.string.title_activity_note_create);
        }

        colorView.setOnPickListener(new ColorPickerView.OnPickListener() {
            @Override
            public void onPick(int color) {
                ColorPickerDialog dialog = new ColorPickerDialog(NoteEditActivity.this, color);
                dialog.setOnColorSavedListener(new ColorPickerDialog.OnColorSavedListener() {
                    @Override
                    public void onColorSave(int color) {
                        colorView.setDefaultColor(color);
                        colorView.setColorToDefault();
                    }
                });
                dialog.show();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (note != null) {
                    saveRecord();
                } else {
                    addRecord();
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void addRecord() {
        note = new Note();
        note.setTitle(titleView.getText().toString());
        note.setDescription(descriptionView.getText().toString());
        note.setColor(colorView.getColor());
        note.setImageUrl(urlImageView.getUrl());
        note.setCreationDate(new Date());
        note.setOwnerId(ownerId);
        note.setServerId(-1);
        addTask = new AddTask(noteDao);
        addTask.execute(note);
    }

    private void saveRecord() {
        note.setTitle(titleView.getText().toString());
        note.setDescription(descriptionView.getText().toString());
        note.setColor(colorView.getColor());
        note.setImageUrl(urlImageView.getUrl());
        note.setLastModificationDate(new Date());
        saveTask = new SaveTask(noteDao);
        saveTask.execute(note);
    }

    private void deleteRecord() {
        deleteTask = new DeleteTask(noteDao);
        deleteTask.execute(note.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            if (note != null) {
                deleteRecord();
                setResult(RESULT_OK);
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void fillForm(Note record) {
        fillForm(record.getTitle(), record.getDescription(), record.getColor(), record.getColor(), record.getImageUrl());
    }

    private void fillForm(Bundle savedInstanceState) {
        fillForm(
                savedInstanceState.getString(TITLE),
                savedInstanceState.getString(DESCRIPTION),
                savedInstanceState.getInt(COLOR),
                savedInstanceState.getInt(DEFAULT_COLOR),
                savedInstanceState.getString(IMAGE_URL));
    }

    private void fillForm(String title, String description, int color, int defaultColor, String url) {
        titleView.setText(title);
        descriptionView.setText(description);
        colorView.setDefaultColor(defaultColor);
        colorView.setColor(color);
        urlImageView.applyUrl(url);
    }
}
