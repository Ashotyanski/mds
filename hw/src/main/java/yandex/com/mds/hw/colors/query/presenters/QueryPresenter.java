package yandex.com.mds.hw.colors.query.presenters;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.colors.query.Query;

public class QueryPresenter {
    private static final String TAG = QueryPresenter.class.getName();
    private static final String PREFERENCES_KEY = "query_templates";
    private Context context;
    private ViewGroup root;
    private OnApplyQueryListener onApplyListener;

    private Animator slideDownAnimator;
    private Animator slideUpAnimator;


    private SearchPresenter searchPresenter;
    private SortPresenter sortPresenter;
    private DatesFilterPresenter datesFilterPresenter;
    private boolean isShown = false;

    private Spinner queryTemplatesSpinner;

    public QueryPresenter(Context context, OnApplyQueryListener onApplyListener, ViewGroup root) {
        this.root = root;
        this.context = context;
        this.onApplyListener = onApplyListener;
        root.bringToFront();
        init();
        sortPresenter = new SortPresenter(context, (LinearLayout) root.findViewById(R.id.sort_root));
        datesFilterPresenter = new DatesFilterPresenter(context, (RelativeLayout) root.findViewById(R.id.filter_date_root));
        searchPresenter = new SearchPresenter(context, (LinearLayout) root.findViewById(R.id.search_root));
        initQueryTemplates();
        initAnimators();
    }

    private void init() {
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                root.requestFocus();
            }
        });
        Button applyButton = (Button) root.findViewById(R.id.button_apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Query query = getQuery();
                    onApplyListener.onApply(query);
                    closeQuery();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        Button cancelButton = (Button) root.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeQuery();
            }
        });
    }

    private void initQueryTemplates() {
        queryTemplatesSpinner = (Spinner) root.findViewById(R.id.spinner_templates);
        fillQueriesTemplatesAdapter(getQueriesTitles());

        queryTemplatesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String title = queryTemplatesSpinner.getItemAtPosition(position).toString();
                Query query = getQuery(title);
                fillQueryForm(query);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button saveButton = (Button) root.findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.title_dialog_query_save);

                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = input.getText().toString();
                        try {
                            saveQuery(title);
                            fillQueriesTemplatesAdapter(getQueriesTitles());
                        } catch (IOException e) {
                            Toast.makeText(context, context.getString(R.string.error_query_save) + title, Toast.LENGTH_SHORT)
                                    .show();
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    private void initAnimators() {
        slideDownAnimator = AnimatorInflater.loadAnimator(context, R.animator.slide_down);
        slideUpAnimator = AnimatorInflater.loadAnimator(context, R.animator.slide_up);
        slideUpAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                root.setVisibility(View.GONE);
            }
        });
        slideDownAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    root.setElevation(6);
            }
        });
        slideDownAnimator.setTarget(root);
        slideUpAnimator.setTarget(root);
    }

    public void toggleQuery() {
        if (!isShown) showQuery();
        else closeQuery();
    }

    public void showQuery() {
        root.setVisibility(View.VISIBLE);
        root.setAlpha(0);
        slideDownAnimator.start();
        isShown = true;
    }

    public void closeQuery() {
        slideUpAnimator.start();
        isShown = false;
    }

    public boolean isTouched(float x, float y) {
        Rect rect = new Rect();
        root.getGlobalVisibleRect(rect);
        return rect.contains((int) x, (int) y);
    }

    public boolean isShown() {
        return isShown;
    }

    public Query getQuery() throws ParseException {
        Query query = new Query();
        query.setSort(sortPresenter.getSort());
        query.setDateFilter(datesFilterPresenter.getDateFilter());
        query.setDateIntervalFilter(datesFilterPresenter.getDateIntervalFilter());
        query.setSearch(searchPresenter.getSearch());
        Log.d(TAG, String.format("Current query is %s", query));
        return query;
    }

    private void fillQueriesTemplatesAdapter(String[] queriesTitles) {
        SpinnerAdapter queryTemplatesSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, queriesTitles);
        queryTemplatesSpinner.setAdapter(queryTemplatesSpinnerAdapter);
    }

    private void fillQueryForm(Query query) {
        Log.d(TAG, String.format("Filling query form with %s", query));
        sortPresenter.fillSort(query.getSort());
        datesFilterPresenter.fillDates(query.getDateFilter(), query.getDateIntervalFilter());
    }

    private void saveQuery(String title) throws IOException, ParseException {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        Query query = getQuery();
        Log.d(TAG, String.format("Saving query %s into %s", query, title));
        preferences.edit().putString(title, Query.serialize(query)).apply();
    }

    private Query getQuery(String title) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        Query query = Query.deserialize(preferences.getString(title, null));
        Log.d(TAG, String.format("Got query %s from %s", query, title));
        return query;
    }

    private String[] getQueriesTitles() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        Map<String, ?> queries = preferences.getAll();
        return queries.keySet().toArray(new String[queries.size()]);
    }

    public interface OnApplyQueryListener {
        void onApply(Query query);
    }
}