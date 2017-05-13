package yandex.com.mds.hw.colors.query;

import java.io.IOException;

import yandex.com.mds.hw.colors.query.clauses.ColorFilter;
import yandex.com.mds.hw.colors.query.clauses.DateFilter;
import yandex.com.mds.hw.colors.query.clauses.DateIntervalFilter;
import yandex.com.mds.hw.colors.query.clauses.Sort;
import yandex.com.mds.hw.utils.SerializationUtils;

public class Query {
    private String search;

    private Sort sort;
    private ColorFilter colorFilter;
    private DateFilter dateFilter;
    private DateIntervalFilter dateIntervalFilter;

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