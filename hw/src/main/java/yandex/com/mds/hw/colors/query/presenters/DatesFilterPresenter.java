package yandex.com.mds.hw.colors.query.presenters;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;

import java.text.ParseException;
import java.util.Calendar;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.colors.query.Utils;
import yandex.com.mds.hw.colors.query.clauses.ClausesConstants;
import yandex.com.mds.hw.colors.query.clauses.DateFilter;
import yandex.com.mds.hw.colors.query.clauses.DateIntervalFilter;
import yandex.com.mds.hw.utils.TimeUtils;

public class DatesFilterPresenter {
    private static final String TAG = DateIntervalFilter.class.getName();
    private Context context;
    private ViewGroup root;

    private Spinner dateFieldSpinner;
    private Spinner dateTypeSpinner;
    private EditText dateFrom;
    private EditText dateTo;
    private Calendar calendar;
    private Switch dateSwitch;

    public DatesFilterPresenter(Context context, ViewGroup root) {
        this.context = context;
        this.root = root;
        init();
    }

    private void init() {
        dateTypeSpinner = (Spinner) root.findViewById(R.id.spinner_filter_date_type);
        SpinnerAdapter typeSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, ClausesConstants.DATE_FILTER_TYPES);
        dateTypeSpinner.setAdapter(typeSpinnerAdapter);

        dateFieldSpinner = (Spinner) root.findViewById(R.id.spinner_filter_date_field);
        SpinnerAdapter fieldSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, ClausesConstants.DATE_FILTER_FIELDS);
        dateFieldSpinner.setAdapter(fieldSpinnerAdapter);

        dateFrom = (EditText) root.findViewById(R.id.filter_date_from);
        dateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        dateFrom.setText(TimeUtils.dateFormat.format(calendar.getTime()));
                    }
                }, 1990, 1, 1);
                dialog.show();
            }
        });
        dateTo = (EditText) root.findViewById(R.id.filter_date_to);
        dateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        dateTo.setText(TimeUtils.dateFormat.format(calendar.getTime()));
                    }
                }, 1990, 1, 1);
                dialog.show();
            }
        });
        dateTo.setVisibility(View.GONE);

        dateSwitch = (Switch) root.findViewById(R.id.switch_date);
        dateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggle(isChecked);
            }
        });
        dateSwitch.setChecked(false);
        toggle(false);

        dateTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switchDateMode(position == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void fill(DateFilter dateFilter, DateIntervalFilter dateIntervalFilter) {
        if (dateFilter == null && dateIntervalFilter == null) {
            dateSwitch.setChecked(false);
            toggle(false);
            return;
        }
        fillDateFilter(dateFilter);
        fillDateIntervalFilter(dateIntervalFilter);
    }

    public void clear() {
        switchDateMode(true);
        dateFrom.setText("");
        dateTo.setText("");
        dateTypeSpinner.setSelection(0);
        toggle(false);
        dateSwitch.setChecked(false);
    }

    public void switchDateMode(boolean toExact) {
        dateTo.setVisibility(toExact ? View.GONE : View.VISIBLE);
        dateFrom.setHint(toExact ? "" : "From");
    }

    private void toggle(boolean isEnabled) {
        dateTypeSpinner.setEnabled(isEnabled);
        dateFieldSpinner.setEnabled(isEnabled);
        dateFrom.setEnabled(isEnabled);
        dateTo.setEnabled(isEnabled);
    }

    private void fillDateFilter(DateFilter filter) {
        if (filter != null) {
            dateSwitch.setChecked(true);
            dateTypeSpinner.setSelection(0);
            dateFieldSpinner.setSelection(Utils.getDateFilterFieldPosition(filter.getField()));
            switchDateMode(true);
            dateFrom.setText(TimeUtils.formatDateTime(filter.getDate()));
        }
    }

    private void fillDateIntervalFilter(DateIntervalFilter filter) {
        if (filter != null) {
            dateSwitch.setChecked(true);
            dateTypeSpinner.setSelection(1);
            dateFieldSpinner.setSelection(Utils.getDateFilterFieldPosition(filter.getField()));
            dateFrom.setText(TimeUtils.formatDateTime(filter.getFrom()));
            switchDateMode(false);
            dateTo.setText(TimeUtils.formatDateTime(filter.getTo()));
        }
    }

    public DateFilter getDateFilter() throws ParseException {
        DateFilter filter = null;
        if (dateSwitch.isChecked() && dateTypeSpinner.getSelectedItemPosition() == 0) {
            // exact date
            filter = new DateFilter();
            filter.setField((String) dateFieldSpinner.getSelectedItem());
            try {
                filter.setDate(TimeUtils.dateFormat.parse(dateFrom.getText().toString()));
                dateFrom.setError(null);
            } catch (ParseException e) {
                dateFrom.setError(context.getString(R.string.error_date_incorrect));
                e.printStackTrace();
                throw e;
            }
        }
        return filter;
    }

    public DateIntervalFilter getDateIntervalFilter() throws ParseException {
        DateIntervalFilter filter = null;
        if (dateSwitch.isChecked() && dateTypeSpinner.getSelectedItemPosition() != 0) {
            filter = new DateIntervalFilter();
            try {
                filter.setFrom(TimeUtils.dateFormat.parse(dateFrom.getText().toString()));
            } catch (ParseException e) {
                dateFrom.setError(null);
                dateFrom.setError(context.getString(R.string.error_date_incorrect));
                e.printStackTrace();
                throw e;
            }
            try {
                filter.setTo(TimeUtils.dateFormat.parse(dateTo.getText().toString()));
            } catch (ParseException e) {
                dateTo.setError(null);
                dateTo.setError(context.getString(R.string.error_date_incorrect));
                e.printStackTrace();
                throw e;
            }
            filter.setField((String) dateFieldSpinner.getSelectedItem());
        }
        return filter;
    }
}
