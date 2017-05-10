package yandex.com.mds.hw.colors.query.presenters;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.EditText;

import yandex.com.mds.hw.R;

public class SearchPresenter {
    private Context context;
    private ViewGroup root;

    private EditText searchView;

    public SearchPresenter(Context context, ViewGroup root) {
        this.context = context;
        this.root = root;
        searchView = (EditText) root.findViewById(R.id.search_text);
    }

    public String getSearch() {
        String search = searchView.getText().toString();
        if (search.equals("")) search = null;
        return search;
    }

    public void clear() {
        searchView.setText("");
    }
}
