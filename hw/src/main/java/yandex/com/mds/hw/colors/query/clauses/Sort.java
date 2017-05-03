package yandex.com.mds.hw.colors.query.clauses;

public class Sort {
    private boolean descending;
    private String field;

    public Sort(boolean descending, String field) {
        this.descending = descending;
        this.field = field;
    }

    public boolean isDescending() {
        return descending;
    }

    public void setDescending(boolean descending) {
        this.descending = descending;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "Sort{" +
                "descending=" + descending +
                ", field='" + field + '\'' +
                '}';
    }
}
