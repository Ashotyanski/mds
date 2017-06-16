package yandex.com.mds.hw.notes.query.presenters;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.EditText;

import yandex.com.mds.hw.R;

public class SearchPresenter {

    private EditText searchView;

    public SearchPresenter(Context context, ViewGroup root) {
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
