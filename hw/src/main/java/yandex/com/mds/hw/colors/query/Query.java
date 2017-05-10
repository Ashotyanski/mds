package yandex.com.mds.hw.colors.query;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

import yandex.com.mds.hw.colors.query.clauses.ColorFilter;
import yandex.com.mds.hw.colors.query.clauses.DateFilter;
import yandex.com.mds.hw.colors.query.clauses.DateIntervalFilter;
import yandex.com.mds.hw.colors.query.clauses.Sort;
import yandex.com.mds.hw.utils.SerializationUtils;

public class Query implements Parcelable {
    private String search;

    private Sort sort;
    private ColorFilter colorFilter;
    private DateFilter dateFilter;
    private DateIntervalFilter dateIntervalFilter;

    public Query() {
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public DateFilter getDateFilter() {
        return dateFilter;
    }

    public void setDateFilter(DateFilter dateFilter) {
        this.dateFilter = dateFilter;
    }

    public DateIntervalFilter getDateIntervalFilter() {
        return dateIntervalFilter;
    }

    public void setDateIntervalFilter(DateIntervalFilter dateIntervalFilter) {
        this.dateIntervalFilter = dateIntervalFilter;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public ColorFilter getColorFilter() {
        return colorFilter;
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.colorFilter = colorFilter;
    }

    public static String serialize(Query query) throws IOException {
        String sort = SerializationUtils.GSON.toJson(query.getSort());
        String dateFilter = SerializationUtils.GSON.toJson(query.getDateFilter());
        String dateInterval = SerializationUtils.GSON.toJson(query.getDateIntervalFilter());
        String colorFilter = SerializationUtils.GSON.toJson(query.getColorFilter());
        String result = String.format("%s#%s#%s#%s", sort, dateFilter, dateInterval, colorFilter);
        return result;
    }

    public static Query deserialize(String serialized) {
        Query query = new Query();
        String[] imploded = serialized.split("#");
        query.setSort(SerializationUtils.GSON.fromJson(imploded[0], Sort.class));
        query.setDateFilter(SerializationUtils.GSON.fromJson(imploded[1], DateFilter.class));
        query.setDateIntervalFilter(SerializationUtils.GSON.fromJson(imploded[2], DateIntervalFilter.class));
        query.setColorFilter(SerializationUtils.GSON.fromJson(imploded[3], ColorFilter.class));
        return query;
    }

    protected Query(Parcel in) {
        this.search = in.readString();
        this.sort = in.readParcelable(Sort.class.getClassLoader());
        this.dateFilter = in.readParcelable(DateFilter.class.getClassLoader());
        this.dateIntervalFilter = in.readParcelable(DateIntervalFilter.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.search);
        dest.writeParcelable(this.sort, flags);
        dest.writeParcelable(this.dateFilter, flags);
        dest.writeParcelable(this.dateIntervalFilter, flags);
    }

    public static final Parcelable.Creator<Query> CREATOR = new Parcelable.Creator<Query>() {
        @Override
        public Query createFromParcel(Parcel source) {
            return new Query(source);
        }

        @Override
        public Query[] newArray(int size) {
            return new Query[size];
        }
    };

    @Override
    public String toString() {
        return "Query{" +
                "search='" + search + '\'' +
                ", sort=" + sort +
                ", colorFilter=" + colorFilter +
                ", dateFilter=" + dateFilter +
                ", dateIntervalFilter=" + dateIntervalFilter +
                '}';
    }

}