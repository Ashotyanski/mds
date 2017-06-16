package yandex.com.mds.hw.notes.synchronizer.conflicts;

import android.os.Parcel;
import android.os.Parcelable;

import yandex.com.mds.hw.models.Note;

public class ConflictNotes implements Parcelable {
    private Note local, remote;

    public ConflictNotes(Note local, Note remote) {
        this.local = local;
        this.remote = remote;
    }

    public Note getLocal() {
        return local;
    }

    public void setLocal(Note local) {
        this.local = local;
    }

    public Note getRemote() {
        return remote;
    }

    public void setRemote(Note remote) {
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
        this.local = in.readParcelable(Note.class.getClassLoader());
        this.remote = in.readParcelable(Note.class.getClassLoader());
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
