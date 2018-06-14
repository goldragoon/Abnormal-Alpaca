package mobile.media.abnormalalpaca.feature;

import android.content.res.AssetManager;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.graphics.Rect;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MenuItem.OnMenuItemClickListener{

    private static final String LABEL_FILE = "math.txt";
    private static final String MODEL_FILE = "optimized_tensorflow.pb";
    private final static String TAG = "MainActivity";
    private SmartCanvas sc;
    int tcounter = 0;

    private SymbolClassifier classifier;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadModel();
        sc = (SmartCanvas)findViewById(R.id.smartcanvas);
        sc.setOnTouchListener(sc);
    }

    /**
     * Load pre-trained model in memory.
     */
    private void loadModel() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = SymbolClassifier.create(getAssets(),
                            MODEL_FILE, LABEL_FILE, SmartCanvas.FEED_DIMENSION,
                            "input", "keep_prob", "output");
                } catch (final Exception e) {
                    throw new RuntimeException("Error loading pre-trained model.", e);
                }
            }
        }).start();
    }

    private void classify() {
        Collections.sort(sc.boundingBoxes, new Comparator<Rect>() {
            @Override
            public int compare(Rect r1, Rect r2) {
                return r1.left - r2.left;
            }
        });
        String s = "";
        for(Rect r : sc.boundingBoxes) {

            float[] temp_s = sc.getPixelsToClassify(r);

            if(temp_s != null)
                s += classifier.classify(temp_s)[0];

        }
        Toast.makeText(this, s + "=" + new Calculator().evaluate(s.replaceAll("X", "*")), Toast.LENGTH_SHORT).show();
        sc.clear();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MainActivity", "onResume");
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mBaseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mBaseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        SmartCanvas.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SmartCanvas.activityPaused();
    }

    private BaseLoaderCallback mBaseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch(status)
            {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i("BaseLoaderCallback", "SUCCESS");
                    break;

            }
        }
    };

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.mbtn_evaluation)
        {
            classify();
            return true;
        }
        return false;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        return false;
    }
}
