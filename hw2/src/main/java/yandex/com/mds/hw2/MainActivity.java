package yandex.com.mds.hw2;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;

import yandex.com.mds.hw2.views.colorView.FavoriteColorView;

public class MainActivity extends AppCompatActivity implements ColorPickerDialog.OnColorSavedListener {
    ColorDatabaseHelper dbHelper = new ColorDatabaseHelper(this);
    FavoriteColorView[] favoriteColors;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Cursor cursor = dbHelper.getFavoriteColors();

        // Let's put users favorite colors in a grid
        // Inspired by MS Paint color palette
        GridLayout grid = (GridLayout) findViewById(R.id.colors_grid);

        final int numOfCol = grid.getColumnCount();
        final int numOfRow = grid.getRowCount();

        favoriteColors = new FavoriteColorView[numOfCol * numOfRow];
        for (int i = 0; i < numOfRow; i++) {
            for (int j = 0; j < numOfCol; j++) {
                int index = i * numOfCol + j;
                cursor.moveToPosition(index);
                int color = cursor.getInt(1);

                FavoriteColorView favoriteColorView = new FavoriteColorView(this);
                favoriteColorView.setColor(color);
                favoriteColorView.setId(index);
                favoriteColors[index] = favoriteColorView;
                grid.addView(favoriteColorView);
            }
        }
        cursor.close();

        grid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int MARGIN = 5;
                int size = grid.getWidth() / numOfCol - 2 * MARGIN;

                for (int i = 0; i < numOfRow; i++) {
                    for (int j = 0; j < numOfCol; j++) {
                        GridLayout.LayoutParams params =
                                (GridLayout.LayoutParams) favoriteColors[i * numOfCol + j].getLayoutParams();
                        params.width = size;
                        params.height = size;
                        params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
                        favoriteColors[i * numOfCol + j].setLayoutParams(params);
                    }
                }
            }
        });
    }

    @Override
    public void onColorSave(int color, int viewId) {
        favoriteColors[viewId].setColor(color);
        dbHelper.saveFavoriteColor(viewId, color);
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

}