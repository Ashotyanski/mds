package yandex.com.mds.hw.colors.query.clauses;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import static yandex.com.mds.hw.utils.SerializationUtils.PARCEL_EMPTY_FIELD;

public class DateIntervalFilter implements Parcelable {
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.from != null ? this.from.getTime() : PARCEL_EMPTY_FIELD);
        dest.writeLong(this.to != null ? this.to.getTime() : PARCEL_EMPTY_FIELD);
        dest.writeString(this.field);
    }

    protected DateIntervalFilter(Parcel in) {
        long tmpFrom = in.readLong();
        this.from = tmpFrom == -1 ? null : new Date(tmpFrom);
        long tmpTo = in.readLong();
        this.to = tmpTo == -1 ? null : new Date(tmpTo);
        this.field = in.readString();
    }

    public static final Parcelable.Creator<DateIntervalFilter> CREATOR = new Parcelable.Creator<DateIntervalFilter>() {
        @Override
        public DateIntervalFilter createFromParcel(Parcel source) {
            return new DateIntervalFilter(source);
        }

        @Override
        public DateIntervalFilter[] newArray(int size) {
            return new DateIntervalFilter[size];
        }
    };

    @Override
    public String toString() {
        return "DateIntervalFilter{" +
                "from=" + from +
                ", to=" + to +
                ", field='" + field + '\'' +
                '}';
    }
}
