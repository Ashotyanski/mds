package yandex.com.mds.hw.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class ColorRecord implements Parcelable {
    private int id;
    private int color;
    private String title;
    private String description;
    private Date created, edited, viewed;
    private String imageUrl;

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

    public Date getCreationDate() {
        return created;
    }

    public void setCreationDate(Date creationDate) {
        this.created = creationDate;
    }

    public Date getLastModificationDate() {
        return edited;
    }

    public void setLastModificationDate(Date lastModificationDate) {
        this.edited = lastModificationDate;
    }

    public Date getLastViewDate() {
        return viewed;
    }

    public void setLastViewDate(Date lastViewDate) {
        this.viewed = lastViewDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ColorRecord() {
    }

    public ColorRecord(int id, int color, String title, String description, Date creationDate, Date lastModificationDate, Date lastViewDate, String imageUrl) {
        this.id = id;
        this.color = color;
        this.title = title;
        this.description = description;
        this.created = creationDate;
        this.edited = lastModificationDate;
        this.viewed = lastViewDate;
        this.imageUrl = imageUrl;
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
        dest.writeLong(this.created != null ? this.created.getTime() : -1);
        dest.writeLong(this.edited != null ? this.edited.getTime() : -1);
        dest.writeLong(this.viewed != null ? this.viewed.getTime() : -1);
        dest.writeString(this.imageUrl);
    }

    protected ColorRecord(Parcel in) {
        this.id = in.readInt();
        this.color = in.readInt();
        this.title = in.readString();
        this.description = in.readString();
        long tmpCreationDate = in.readLong();
        this.created = tmpCreationDate == -1 ? null : new Date(tmpCreationDate);
        long tmpLastModificationDate = in.readLong();
        this.edited = tmpLastModificationDate == -1 ? null : new Date(tmpLastModificationDate);
        long tmpLastViewDate = in.readLong();
        this.viewed = tmpLastViewDate == -1 ? null : new Date(tmpLastViewDate);
        this.imageUrl = in.readString();
    }

    public static final Creator<ColorRecord> CREATOR = new Creator<ColorRecord>() {
        @Override
        public ColorRecord createFromParcel(Parcel source) {
            return new ColorRecord(source);
        }

        @Override
        public ColorRecord[] newArray(int size) {
            return new ColorRecord[size];
        }
    };

    @Override
    public String toString() {
        return "ColorRecord{" +
                "id=" + id +
                ", color=" + color +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", created=" + created +
                ", edited=" + edited +
                ", viewed=" + viewed +
                '}';
    }
}
