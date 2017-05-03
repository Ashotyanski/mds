package yandex.com.mds.hw.colors.query.clauses;

import java.util.Date;

public class DateFilter {
    private Date date;
    private String field;

    public DateFilter() {
    }

    public DateFilter(Date date, String field) {
        this.date = date;
        this.field = field;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "DateFilter{" +
                "date=" + date +
                ", field='" + field + '\'' +
                '}';
    }
}
