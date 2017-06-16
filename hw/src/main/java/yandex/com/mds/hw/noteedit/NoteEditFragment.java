package yandex.com.mds.hw.noteedit;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

import yandex.com.mds.hw.MainActivity;
import yandex.com.mds.hw.R;
import yandex.com.mds.hw.colorpicker.ColorPickerDialog;
import yandex.com.mds.hw.colorpicker.ColorPickerView;
import yandex.com.mds.hw.colorpicker.colorview.EditableColorView;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.noteedit.tasks.AddTask;
import yandex.com.mds.hw.noteedit.tasks.DeleteTask;
import yandex.com.mds.hw.noteedit.tasks.SaveTask;
import yandex.com.mds.hw.utils.TimeUtils;

public class NoteEditFragment extends Fragment {
    private static final String TAG = NoteEditFragment.class.getName();
    public static final String ID = "id";
    public static final String OWNER_ID = "owner_id";
    public static final String COLOR = "color";
    public static final String DEFAULT_COLOR = "default_color";
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";
    public static final String IMAGE_URL = "image_url";
    public static final String IS_VIEW_COUNTED = "isViewCounted";

    public static final String TRANSITION_NAME = "TRANSITION_NAME";

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

    public static NoteEditFragment newInstance(int noteId, int ownerId, String transitionName) {
        Bundle args = new Bundle();
        args.putInt(ID, noteId);
        args.putInt(OWNER_ID, ownerId);
        args.putString(TRANSITION_NAME, transitionName);
        NoteEditFragment fragment = new NoteEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public NoteEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
            setSharedElementReturnTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_edit, container, false);

        titleView = (EditText) view.findViewById(R.id.title);
        descriptionView = (EditText) view.findViewById(R.id.description);
        saveButton = (Button) view.findViewById(R.id.save_button);
        urlImageView = (UrlImageView) view.findViewById(R.id.url_image);
        colorView = (EditableColorView) view.findViewById(R.id.color);

        ActionBar toolbar = ((MainActivity) getActivity()).getSupportActionBar();

        Bundle arguments = getArguments();

        if (savedInstanceState != null) {
            fillForm(savedInstanceState);
            isViewCounted = savedInstanceState.getBoolean(IS_VIEW_COUNTED);
        }

        if (arguments != null) {
            if (arguments.getInt(ID, -1) >= 0) {
                toolbar.setTitle(R.string.title_note_edit);
                note = noteDao.getNote(arguments.getInt(ID));
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
                ownerId = arguments.getInt(OWNER_ID, -1);
                toolbar.setTitle(R.string.title_note_create);
            }
        } else {
            ownerId = -1;
            toolbar.setTitle(R.string.title_note_create);
        }

        colorView.setOnPickListener(new ColorPickerView.OnPickListener() {
            @Override
            public void onPick(int color) {
                ColorPickerDialog dialog = new ColorPickerDialog(getContext(), color);
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
                if (titleView.getText() != null && titleView.getText().length() != 0) {
                    if (note != null) {
                        saveRecord();
                    } else {
                        addRecord();
                    }
                    ((MainActivity) getActivity()).getNavigationManager().navigateBack(getActivity());
                } else {
                    titleView.setError("Title cannot be empty");
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String transitionName = getArguments().getString(TRANSITION_NAME);
        ViewCompat.setTransitionName(colorView, transitionName);
        startPostponedEnterTransition();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_note_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            if (note != null) {
                deleteRecord();
            }
            ((MainActivity) getActivity()).getNavigationManager().navigateBack(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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

    public EditableColorView getSharedView() {
        return colorView;
    }
}
