package yandex.com.mds.hw3.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Color implements Parcelable {
    private int id;
    private int color;
    private String title;
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.color);
        dest.writeString(this.title);
        dest.writeString(this.description);
    }

    public Color(int id, String title, String description, int color) {
        this.id = id;
        this.color = color;
        this.title = title;
        this.description = description;
    }

    protected Color(Parcel in) {
        this.id = in.readInt();
        this.color = in.readInt();
        this.title = in.readString();
        this.description = in.readString();
    }

    public static final Parcelable.Creator<Color> CREATOR = new Parcelable.Creator<Color>() {
        @Override
        public Color createFromParcel(Parcel source) {
            return new Color(source);
        }

        @Override
        public Color[] newArray(int size) {
            return new Color[size];
        }
    };
}
