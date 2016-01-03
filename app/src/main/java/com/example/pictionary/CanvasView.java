package com.example.pictionary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Philippe on 03.12.2015.
 */
public class CanvasView extends View {

    //private static CanvasView mInstance = null;
    private Path path;
    private Paint paint;
    private Canvas canvas;
    private Bitmap bitmap;

    private BLESingleton mBLE= BLESingleton.getInstance();


    /*
   private float touchX;
   private float touchY;


   public interface onTouchEvent{
       void screenTouched();
   }

   public onTouchEvent mTouchListener;
   */
    public CanvasView(Context context){
        super(context);
        setupCanvas();
    }

    public CanvasView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupCanvas();
    }

    /*
    //warning: uses only one of the 2 possible constructors
    public static CanvasView getInstance(Context context){
        if (mInstance==null)
            mInstance= new CanvasView(context);
        return mInstance;
    }
    */

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        mBLE.setCoordinates(touchX, touchY);
        /*
        if (mTouchListener!=null)
            mTouchListener.screenTouched();
            */


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                canvas.drawPath(path, paint);
                path.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }


    void setupCanvas(){

        path = new Path();
        paint = new Paint();

        paint.setAntiAlias(true);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

    }

    void setPaintColor(int color){
        paint.setColor(color);
    }

    /*
    public void setCoordinateListener(onTouchEvent listener){
        mTouchListener = listener;
    }

    public float [] getTouchEventCoord(){
        float [] array = new float[2];
        array[0] = touchX;
        array[1] = touchY;
        return array;

    }*/
}
