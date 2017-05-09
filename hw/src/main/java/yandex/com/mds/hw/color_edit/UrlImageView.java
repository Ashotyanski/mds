package yandex.com.mds.hw.color_edit;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

import yandex.com.mds.hw.R;

public class UrlImageView extends FrameLayout {
    private ImageView imageView;
    private ProgressBar progressBar;
    private TextView textView;
    private String url;
    private UrlImageLoadTask task;

    public UrlImageView(Context context) {
        super(context);
        init(context);
    }

    public UrlImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UrlImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        imageView = new ImageView(context);
        imageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(imageView);

        progressBar = new ProgressBar(context);
        progressBar.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(progressBar);

        textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        addView(textView);

        setEmpty();

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.title_dialog_url_image_edit);

                final EditText input = new EditText(v.getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(url);
                builder.setView(input);

                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        applyUrl(input.getText().toString());
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    public void setFailed() {
        progressBar.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        textView.setVisibility(VISIBLE);
        textView.setText(R.string.url_image_status_failed);
    }

    public void setEmpty() {
        progressBar.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        textView.setVisibility(VISIBLE);
        textView.setText(R.string.url_image_status_empty);
    }

    public void setBitmap(Bitmap bitmap) {
        progressBar.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(bitmap);
    }

    public void setLoading() {
        imageView.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void load() {
        if (task != null && (task.getStatus() == AsyncTask.Status.PENDING || task.getStatus() == AsyncTask.Status.RUNNING)) {
            task.cancel(true);
        }
        task = new UrlImageLoadTask();
        task.execute(url);
    }

    public void applyUrl(String path) {
        setUrl(path);
        if (path == null || path.equals("")) {
            setEmpty();
        } else {
            load();
        }
    }

    private class UrlImageLoadTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                publishProgress();
                Bitmap bitmap = BitmapFactory.decodeStream(new URL(params[0]).openStream());
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            setLoading();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null)
                setBitmap(bitmap);
            else
                setFailed();
        }
    }
}