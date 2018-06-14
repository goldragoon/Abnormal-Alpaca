package mobile.media.abnormalalpaca.feature;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gyu Jin Choi <paganinist@gmail.com>
 *
 */

public class SmartCanvas extends View implements View.OnTouchListener{

    // Length in pixels of each dimension for the bitmap displayed on the screen.
    public static final int BITMAP_DIMENSION = 128;

    // Length in pixels of each dimension for the bitmap to be fed into the model.
    public static final int FEED_DIMENSION = 64;

    private static boolean isForeground = false;
    private static final String TAG = "SmartCanvas";
    public enum Toolbox{
        PENCIL,
        ERASER,
        CURSOR
    };

    private Context mContext;

    private ScaleGestureDetector scaleGestureDetector; // To conveniently recognize pinch zoom.

    private float scale = 1.0f;

    private Paint paint;
    private Paint debugPaint;

    private int brushColor = Color.BLACK;
    private int defaultBrushColor = Color.BLACK;
    private int debugBrushColor = Color.RED;

    Path tempPath;

    private ArrayList<Path> paths;
    private ArrayList<Integer> pathColors;
    public ArrayList<Rect> boundingBoxes = new ArrayList<>();


    // Multitouch related variables
    private int arrTouchX[] = new int[10], arrTouchY[] = new int[10];
    private boolean arrTouched[] = new boolean[10];


    public SmartCanvas(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        Log.d("SmartCanvas", "constructor");

        brushColor = defaultBrushColor;

        paths = new ArrayList<>();
        pathColors = new ArrayList<>();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10f);
        paint.setColor(brushColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);

        debugPaint = new Paint();
        debugPaint.setStrokeWidth(10f);
        debugPaint.setColor(debugBrushColor);
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setStrokeJoin(Paint.Join.ROUND);

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


    public Bitmap getBitmap() {
        // Get raster image of SmartCanvas
        Bitmap temp_bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp_bitmap);
        canvas.drawColor(Color.WHITE);
        for(int i = 0; i <paths.size(); i++) { canvas.drawPath(paths.get(i), paint); }

        return temp_bitmap;
    }

    public Bitmap getBitmap(Rect r) {
        // Get Cropped image of SmartCanvas

        int threshold_ratio_s = 4, threshold_ratio_b = 7;
        int rl = r.left, rt = r.top, rr = r.right, rb = r.bottom;
        int rw = rr - rl, rh = rb - rt;

        if(rw / rh > threshold_ratio_b)
        {
            int adjustment = rh / 2;
            rt -= adjustment;
            rb += adjustment;
        }
        else if(rh / rw > threshold_ratio_b)
        {
            int adjustment = rw / 2;
            rl -= adjustment;
            rr += adjustment;

        }
        else if(rw / rh > threshold_ratio_s)
        {
            int adjustment = rh * threshold_ratio_s*2;
            rt -= adjustment;
            rb += adjustment;
        }
        else if(rh / rw > threshold_ratio_s)
        {
            int adjustment = rw * threshold_ratio_s*2;
            rl -= adjustment;
            rr += adjustment;

        }


        if(rl <= 0) rl = 0;
        if(rr > this.getWidth()) rr = this.getWidth() - 1;
        if(rt <= 0) rt = 0;
        if(rb > this.getHeight()) rb = this.getHeight() - 1;
        // Update
        rw = rr - rl; rh = rb - rt;
        Log.d("Values", String.format("%d %d %d %d", rl, rr, rt, rb));
        Bitmap temp_bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp_bitmap);
        canvas.drawColor(Color.WHITE);
        for(int i = 0; i <paths.size(); i++) { canvas.drawPath(paths.get(i), paint); }

        return Bitmap.createBitmap(temp_bitmap, rl, rt, rw, rh);

    }

    public float[] getPixelsToClassify() {

        // To convert the bitmap pixels to usable input to our TensorFlow model.
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(getBitmap(), FEED_DIMENSION,
                FEED_DIMENSION, false);

        int width = FEED_DIMENSION;
        int height = FEED_DIMENSION;
        int[] pixels = new int[width * height];
        resizedBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        float[] returnPixels = new float[pixels.length];

        // Here we want to convert each pixel to a floating point number between 0.0 and 1.0 with
        // 1.0 being white and 0.0 being black.
        for (int i = 0; i < pixels.length; ++i) {
            int pix = pixels[i];
            int b = pix & 0xff;
            returnPixels[i] = (float) (b/255.0);
        }
        return returnPixels;
    }

    public float[] getPixelsToClassify(Rect r) {

        // To convert the bitmap pixels to usable input to our TensorFlow model.
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(getBitmap(r), FEED_DIMENSION,
                FEED_DIMENSION, false);

        int width = FEED_DIMENSION;
        int height = FEED_DIMENSION;
        int[] pixels = new int[width * height];
        resizedBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        float[] returnPixels = new float[pixels.length];

        // Here we want to convert each pixel to a floating point number between 0.0 and 1.0 with
        // 1.0 being white and 0.0 being black.
        for (int i = 0; i < pixels.length; ++i) {
            int pix = pixels[i];
            int b = pix & 0xff;
            returnPixels[i] = (float) (b/255.0);
        }
        Log.d("getPixels", returnPixels.toString());

        return returnPixels;
    }

    public int getBrushColor() {
        return brushColor;
    }

    public void setBrushColor(int brushColor) {
        this.brushColor = brushColor;
    }

    private void printMatOfPoint(MatOfPoint point){
        List<Point> points = point.toList();
        Log.i(TAG, "POINT");
        for(Point p : points) {
            Log.i(TAG, String.format("(%s, %s)", p.x, p.y));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(isForeground) {
            Log.i("onDraw", "BoundingBox Size : " + Integer.toString(boundingBoxes.size()));
            //for(MatOfPoint mop : boundingBoxes) printMatOfPoint(mop);
            updateBoundingBoxes();

        }

        for(int path = 0; path <paths.size(); path++)
        {
            paint.setColor(pathColors.get(path));
            canvas.drawPath(paths.get(path), paint);
        }


        // Drawing debugging bounding box
        for(int boundingBox = 0; boundingBox < boundingBoxes.size(); boundingBox++)
        {
            Rect mop = boundingBoxes.get(boundingBox);
            canvas.drawRect(mop, debugPaint);
        }


        /*
        // Canvas scaling
        canvas.save();
        canvas.scale(scale, scale);
        canvas.restore();
        */
    }

    private void updateBoundingBoxes() {
        Bitmap canvasBitmap = this.getBitmap();
        Mat canvasMat = new Mat();

        Utils.bitmapToMat(canvasBitmap, canvasMat);
        Imgproc.cvtColor(canvasMat, canvasMat, Imgproc.COLOR_BGR2GRAY);

        Core.bitwise_not(canvasMat, canvasMat);
        //Imgproc.adaptiveThreshold(canvasMat, canvasMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, 75, 10, 0);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        ArrayList<Rect> rects = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(canvasMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_KCOS, new Point(0, 0));
        boundingBoxes.clear();
        for(int c = 0; c < contours.size(); c++)
        {
            MatOfPoint2f contour_poly = new MatOfPoint2f(), contour_mat2f = new MatOfPoint2f();
            MatOfPoint contour_poly_mat = new MatOfPoint();
            contours.get(c).convertTo(contour_mat2f, CvType.CV_32FC2);
            Imgproc.approxPolyDP(contour_mat2f, contour_poly, 3, true);

            contour_poly.convertTo(contour_poly_mat, CvType.CV_32S);
            boundingBoxes.add(convertRectO2A(Imgproc.boundingRect(new MatOfPoint(contour_poly_mat))));
        }
    }

    private android.graphics.Rect convertRectO2A(org.opencv.core.Rect rect) { return new android.graphics.Rect(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height); }
    private org.opencv.core.Rect convertRectA2O(android.graphics.Rect rect) { return new org.opencv.core.Rect(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top); }

    public void clear() {
        paths.clear();
        pathColors.clear();
        boundingBoxes.clear();
        invalidate();
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

    public static void activityResumed(){
        isForeground = true;
    }

    public static void activityPaused(){
        isForeground = false;
    }
}
