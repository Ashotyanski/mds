package yandex.com.mds.hw.notes.query.presenters;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.util.Calendar;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.notes.query.Utils;
import yandex.com.mds.hw.notes.query.clauses.ClausesConstants;
import yandex.com.mds.hw.notes.query.clauses.DateFilter;
import yandex.com.mds.hw.notes.query.clauses.DateIntervalFilter;
import yandex.com.mds.hw.utils.TimeUtils;

public class DatesFilterPresenter {
    private static final String TAG = DateIntervalFilter.class.getName();
    private Context context;
    private ViewGroup root;

    private Spinner dateFieldSpinner;
    private Spinner dateTypeSpinner;
    private TextView dateFrom;
    private TextView dateTo;
    private Switch dateSwitch;

    private Calendar dateFromCalendar, dateToCalendar;

    public DatesFilterPresenter(Context context, ViewGroup root) {
        this.context = context;
        this.root = root;
        init();
    }

    private void init() {
        dateTypeSpinner = (Spinner) root.findViewById(R.id.spinner_filter_date_type);
        SpinnerAdapter typeSpinnerAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, ClausesConstants.DATE_FILTER_TYPES);
        dateTypeSpinner.setAdapter(typeSpinnerAdapter);

        dateFieldSpinner = (Spinner) root.findViewById(R.id.spinner_filter_date_field);
        SpinnerAdapter fieldSpinnerAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, ClausesConstants.DATE_FILTER_FIELDS);
        dateFieldSpinner.setAdapter(fieldSpinnerAdapter);

        dateFrom = (TextView) root.findViewById(R.id.filter_date_from);
        dateFromCalendar = Calendar.getInstance();
        dateFrom.setText(TimeUtils.dateFormatWithoutSeconds.format(dateFromCalendar.getTime()));
        dateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateTimePickerDialog(context, dateFromCalendar, dateFrom).show();
            }
        });
        dateTo = (TextView) root.findViewById(R.id.filter_date_to);
        dateToCalendar = Calendar.getInstance();
        dateTo.setText(TimeUtils.dateFormatWithoutSeconds.format(dateToCalendar.getTime()));
        dateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateTimePickerDialog(context, dateToCalendar, dateTo).show();
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

    private static DatePickerDialog getDateTimePickerDialog(final Context context, final Calendar dateToCalendar, final TextView dateTo) {
        return new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                // Bug: https://issuetracker.google.com/issues/36951008
                if (view.isShown()) {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            dateToCalendar.set(Calendar.YEAR, year);
                            dateToCalendar.set(Calendar.MONTH, month);
                            dateToCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            dateToCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            dateToCalendar.set(Calendar.MINUTE, minute);
                            dateTo.setText(TimeUtils.dateFormatWithoutSeconds.format(dateToCalendar.getTime()));
                        }
                    }, dateToCalendar.get(Calendar.HOUR_OF_DAY), dateToCalendar.get(Calendar.MINUTE), true);
                    timePickerDialog.show();
                }
            }
        }, dateToCalendar.get(Calendar.YEAR), dateToCalendar.get(Calendar.MONTH), dateToCalendar.get(Calendar.DAY_OF_MONTH));
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

    public DateFilter getDateFilter() {
        DateFilter filter = null;
        if (dateSwitch.isChecked() && dateTypeSpinner.getSelectedItemPosition() == 0) {
            // exact date
            filter = new DateFilter();
            filter.setField((String) dateFieldSpinner.getSelectedItem());
            try {
                filter.setDate(TimeUtils.dateFormatWithoutSeconds.parse(dateFrom.getText().toString()));
                dateFrom.setError(null);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return filter;
    }

    public DateIntervalFilter getDateIntervalFilter() {
        DateIntervalFilter filter = null;
        if (dateSwitch.isChecked() && dateTypeSpinner.getSelectedItemPosition() != 0) {
            filter = new DateIntervalFilter();
            try {
                filter.setFrom(TimeUtils.dateFormatWithoutSeconds.parse(dateFrom.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            try {
                filter.setTo(TimeUtils.dateFormatWithoutSeconds.parse(dateTo.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            filter.setField((String) dateFieldSpinner.getSelectedItem());
        }
        return filter;
    }
}
