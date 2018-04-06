package mobile.media.abnormalalpaca.feature;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private SmartCanvas sc;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Why activity is not properly initialized with persistentInstanceState parameter included constructor

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.d("MainActivity_OnCreate", "After SetContentView");
        sc = (SmartCanvas)findViewById(R.id.smartcanvas);
        sc.setOnTouchListener(sc);

    }
}
