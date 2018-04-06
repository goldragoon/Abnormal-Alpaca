package mobile.media.abnormalalpaca.feature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;



public class SmartCanvas extends View implements View.OnTouchListener{

    public enum Toolbox{
        PENCIL,
        ERASER,
        CURSOR
    };

    private ScaleGestureDetector scaleGestureDetector; // To conveniently recognize pinch zoom.

    private float scale = 1.0f;

    private Paint paint;
    private int brushColor = Color.BLACK;

    Path tempPath;

    private ArrayList<Path> paths;
    private ArrayList<Integer> pathColors;

    // Multitouch related variables
    private int arrTouchX[] = new int[10], arrTouchY[] = new int[10];
    private boolean arrTouched[] = new boolean[10];


    public SmartCanvas(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        Log.d("SmartCanvas", "constructor");
        paths = new ArrayList<>();
        pathColors = new ArrayList<>();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10f);
        paint.setColor(brushColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scale *= detector.getScaleFactor();
                if(scale < 0.1f) scale = 0.1f;
                else if(scale > 10.0f) scale = 10.0f;
                invalidate();
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return false;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        });
    }

    public void unDo() {

    }

    public void reDo() {

    }

    public Bitmap getBitmap() {
        // Get raster image of SmartCanvas
        Bitmap returnedBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE);
        this.draw(canvas);
        return returnedBitmap;
    }

    public int getBrushColor() {
        return brushColor;
    }

    public void setBrushColor(int brushColor) {
        this.brushColor = brushColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(int i = 0; i <paths.size(); i++)
        {
            paint.setColor(pathColors.get(i));
            canvas.drawPath(paths.get(i), paint);
        }

        /*
        // Canvas Scaling
        canvas.save();
        canvas.scale(scale, scale);
        canvas.restore();
        */
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //Log.d("Touched", "TopOfEvent");

        int action_index = event.getActionIndex();
        int pointer_index = event.getPointerId(action_index);
        int action = event.getActionMasked();
        int touchX = (int) event.getX(action_index), touchY = (int) event.getY(action_index);

        // Since pointer information is already acquired by action_index and pointer_index,
        // event.getActionMasked is called to get touch event action code.

        // Difference between MotionEvent.getAction and MotionEvent.getActionMasked?
        // pointer information(id) is excluded on getActionMasked
        // Reference: https://bit.ly/2GAMwc1


        if(!scaleGestureDetector.onTouchEvent(event)) {
            // If scale gesture is detected
            Log.d("Touched", "ScaleTouch");
            switch(action) {

            }
        }
        else{
            //Log.d("Touched", "OrdinaryTouch");
            switch(action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    tempPath = new Path();
                    tempPath.moveTo(touchX, touchY);
                    paths.add(tempPath);
                    pathColors.add(brushColor);
                    invalidate();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    tempPath.lineTo(touchX, touchY);
                    invalidate();
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    invalidate();
                    return true;

                default:
                    return true;
            }
        }

        invalidate();
        return true;
    }


}
