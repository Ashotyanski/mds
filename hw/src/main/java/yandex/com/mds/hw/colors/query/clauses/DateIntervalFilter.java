package yandex.com.mds.hw.colors.query.clauses;

import java.util.Date;

public class DateIntervalFilter {
    private Date from, to;
    private String field;

    public DateIntervalFilter() {
    }

    public DateIntervalFilter(Date from, Date to, String field) {
        this.from = from;
        this.to = to;
        this.field = field;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "DateIntervalFilter{" +
                "from=" + from +
                ", to=" + to +
                ", field='" + field + '\'' +
                '}';
    }
}
