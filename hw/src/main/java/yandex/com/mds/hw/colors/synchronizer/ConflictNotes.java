package yandex.com.mds.hw.colors.synchronizer;

import android.os.Parcel;
import android.os.Parcelable;

import yandex.com.mds.hw.models.ColorRecord;

public class ConflictNotes implements Parcelable {
    private ColorRecord local, remote;

    public ConflictNotes(ColorRecord local, ColorRecord remote) {
        this.local = local;
        this.remote = remote;
    }

    public ColorRecord getLocal() {
        return local;
    }

    public void setLocal(ColorRecord local) {
        this.local = local;
    }

    public ColorRecord getRemote() {
        return remote;
    }

    public void setRemote(ColorRecord remote) {
        this.remote = remote;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.local, flags);
        dest.writeParcelable(this.remote, flags);
    }

    protected ConflictNotes(Parcel in) {
        this.local = in.readParcelable(ColorRecord.class.getClassLoader());
        this.remote = in.readParcelable(ColorRecord.class.getClassLoader());
    }

    public static final Creator<ConflictNotes> CREATOR = new Creator<ConflictNotes>() {
        @Override
        public ConflictNotes createFromParcel(Parcel source) {
            return new ConflictNotes(source);
        }

        @Override
        public ConflictNotes[] newArray(int size) {
            return new ConflictNotes[size];
        }
    };

    @Override
    public String toString() {
        return "ConflictNotes{" +
                "local=" + local +
                ", remote=" + remote +
                '}';
    }
}
