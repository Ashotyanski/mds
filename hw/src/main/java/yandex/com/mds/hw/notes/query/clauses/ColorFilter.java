package yandex.com.mds.hw.notes.query.clauses;

public class ColorFilter {
    private int color;

    public ColorFilter(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "ColorFilter{" +
                "color=" + color +
                '}';
    }
}
