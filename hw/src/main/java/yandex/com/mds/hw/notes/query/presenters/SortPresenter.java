package yandex.com.mds.hw.notes.query.presenters;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.notes.query.Utils;
import yandex.com.mds.hw.notes.query.clauses.ClausesConstants;
import yandex.com.mds.hw.notes.query.clauses.Sort;

public class SortPresenter {
    private Context context;
    private ViewGroup root;

    private Spinner sortOrderSpinner;
    private Spinner sortFieldSpinner;
    private Switch sortSwitch;


    public SortPresenter(Context context, ViewGroup root) {
        this.context = context;
        this.root = root;
        init();
    }

    private void init() {
        sortFieldSpinner = (Spinner) root.findViewById(R.id.spinner_sort_field);
        SpinnerAdapter fieldSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, ClausesConstants.SORT_FIELDS);
        sortFieldSpinner.setAdapter(fieldSpinnerAdapter);

        sortOrderSpinner = (Spinner) root.findViewById(R.id.spinner_sort_order);
        SpinnerAdapter orderSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, ClausesConstants.SORT_ORDER);
        sortOrderSpinner.setAdapter(orderSpinnerAdapter);

        sortSwitch = (Switch) root.findViewById(R.id.switch_sort);
        sortSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggle(isChecked);
            }
        });
        sortSwitch.setChecked(false);
        toggle(false);
    }

    private void toggle(boolean isEnable) {
        sortFieldSpinner.setEnabled(isEnable);
        sortOrderSpinner.setEnabled(isEnable);
    }

    public void fill(Sort sort) {
        if (sort != null) {
            sortSwitch.setChecked(true);
            sortOrderSpinner.setSelection(sort.isDescending() ? 0 : 1);
            sortFieldSpinner.setSelection(Utils.getSortFieldPosition(sort.getField()));
        } else {
            sortSwitch.setChecked(false);
            toggle(false);
        }
    }

    public Sort getSort() {
        Sort sort = null;
        if (sortSwitch.isChecked()) {
            sort = new Sort(sortOrderSpinner.getSelectedItemPosition() == 0, (String) sortFieldSpinner.getSelectedItem());
        }
        return sort;
    }

    public void clear() {
        sortOrderSpinner.setSelection(0);
        sortFieldSpinner.setSelection(0);
        sortSwitch.setChecked(false);
        toggle(false);
    }
}
