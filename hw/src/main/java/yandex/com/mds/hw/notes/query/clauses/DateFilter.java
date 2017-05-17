package yandex.com.mds.hw.notes.query.clauses;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import static yandex.com.mds.hw.utils.SerializationUtils.PARCEL_EMPTY_FIELD;

public class DateFilter implements Parcelable {
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
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "DateFilter{" +
                "date=" + date +
                ", field='" + field + '\'' +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.date != null ? this.date.getTime() : -1);
        dest.writeString(this.field);
    }

    protected DateFilter(Parcel in) {
        long tmpDate = in.readLong();
        this.date = tmpDate == PARCEL_EMPTY_FIELD ? null : new Date(tmpDate);
        this.field = in.readString();
    }

    public static final Parcelable.Creator<DateFilter> CREATOR = new Parcelable.Creator<DateFilter>() {
        @Override
        public DateFilter createFromParcel(Parcel source) {
            return new DateFilter(source);
        }

        @Override
        public DateFilter[] newArray(int size) {
            return new DateFilter[size];
        }
    };
}
