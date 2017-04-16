package yandex.com.mds.hw2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val grid = findViewById(R.id.colors_grid) as GridLayout

//        val color_width = resources.getDimension(R.dimen.color_width).toInt()
        val color_width = grid.width / 3
        Log.d("COLOR_WIDTH", color_width.toString())

        for (i in 1..10)
            grid.addView(ColorView(this, Color.rgb(255 % i, 255 % i, 255 % i)), color_width, color_width)

//        val v = ColorView(this)
//        v.color = Color.GREEN
//        grid.addView(v)
//        grid.addView(ColorView(this))
//        grid.addView(ColorView(this))
    }
}

class ColorView(context: Context, var color: Int = Color.RED) : View(context) {

    init {
        setOnClickListener { _ -> Toast.makeText(getContext(), "Color " + color, LENGTH_SHORT).show() }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawColor(color)
    }
}
