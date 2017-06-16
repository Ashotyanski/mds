package yandex.com.mds.hw.notes.query.clauses;

import android.os.Parcel;
import android.os.Parcelable;

public class Sort implements Parcelable {
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.descending ? (byte) 1 : (byte) 0);
        dest.writeString(this.field);
    }

    protected Sort(Parcel in) {
        this.descending = in.readByte() != 0;
        this.field = in.readString();
    }

    public static final Parcelable.Creator<Sort> CREATOR = new Parcelable.Creator<Sort>() {
        @Override
        public Sort createFromParcel(Parcel source) {
            return new Sort(source);
        }

        @Override
        public Sort[] newArray(int size) {
            return new Sort[size];
        }
    };

    @Override
    public String toString() {
        return "Sort{" +
                "descending=" + descending +
                ", field='" + field + '\'' +
                '}';
    }
}
