package net.codebot.pdfviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.util.ArrayList;

@SuppressLint("AppCompatCustomView")
public class PDFimage extends ImageView {

    final String LOGNAME = "pdf_image";

    // drawing path
    //Path path = null;
    Mpath path = null;
    //ArrayList<Path> paths = new ArrayList();

    // image to display
    Bitmap bitmap;
    Paint dPaint;
    Paint ePaint;
    Paint hPaint;
    String type;
    float startx;
    float starty;
    float dx;
    float dy;
    float posx;
    float posy;


    private ScaleGestureDetector sg;
    private float scale = 1.0f;

    // constructor
    public PDFimage(Context context) {
        super(context);
       dPaint  = new Paint();
       dPaint.setColor(Color.BLUE);
       dPaint.setAntiAlias(true);
       dPaint.setStyle(Paint.Style.STROKE);
       dPaint.setStrokeWidth(12);
       dPaint.setStrokeCap(Paint.Cap.ROUND);
       dPaint.setStrokeJoin(Paint.Join.ROUND);

        ePaint  = new Paint();
        ePaint.setColor(Color.TRANSPARENT);
        ePaint.setAntiAlias(true);
        ePaint.setStyle(Paint.Style.STROKE);
        ePaint.setStrokeWidth(5);
        ePaint.setStrokeCap(Paint.Cap.ROUND);
        ePaint.setStrokeJoin(Paint.Join.ROUND);
        //ePaint.setAlpha(0x00);
        ePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        hPaint  = new Paint();
        hPaint.setColor(Color.YELLOW);
        hPaint.setAntiAlias(true);
        hPaint.setStyle(Paint.Style.STROKE);
        hPaint.setStrokeWidth(20);
        hPaint.setStrokeCap(Paint.Cap.ROUND);
        hPaint.setStrokeJoin(Paint.Join.ROUND);
        hPaint.setAlpha(100);


       type = "DRAW";
        sg = new ScaleGestureDetector(context, new ScaleListener());

        //c = new Canvas();
    }


    // capture touch events (down/move/up) to create a path
    // and use that to create a stroke that we can draw
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                Log.d(LOGNAME, "Action down");
//                path = new Path();
//                path.moveTo(event.getX(), event.getY());
//                break;
//            case MotionEvent.ACTION_MOVE:
//                Log.d(LOGNAME, "Action move");
//                path.lineTo(event.getX(), event.getY());
//                break;
//            case MotionEvent.ACTION_UP:
//                Log.d(LOGNAME, "Action up");
//                paths.add(path);
//                break;
//        }

        sg.onTouchEvent(event);

        if (event.getPointerCount() == 1 && !type.equals("PAN")){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(LOGNAME, "Action down");
                    path = new Mpath();
                    path.setType(type);
                    if (!type.equals("ERASE")) {
                        Model.getInstance().addTransition(path);

                    }
                    path.moveTo(event.getX(), event.getY());
                    path.addPoint(new Mpoint((int)event.getX(), (int)event.getY()));
                    if (type.equals("ERASE")){
                        Model.getInstance().check(new Mpoint((int)event.getX(), (int)event.getY()));
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d(LOGNAME, "Action move");
                    path.lineTo(event.getX(), event.getY());
                    path.addPoint(new Mpoint((int)event.getX(), (int)event.getY()));
                    if (type.equals("ERASE")){
                        Model.getInstance().check(new Mpoint((int)event.getX(), (int)event.getY()));
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(LOGNAME, "Action up");
                    Log.d("NUMPOINTs", String.valueOf(path.getNumPoints()));
                    //c.drawPath(path, paint);
                    //c.invalidate();

                    break;
            }
        }
        else if (event.getPointerCount() == 1 && type.equals("PAN")){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(LOGNAME, "Action down");
                    startx = event.getX();
                    starty = event.getY();

                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d(LOGNAME, "Action move");
                    dx = event.getX() - startx;
                    dy = event.getY() - starty;
                    startx = event.getX();
                    starty = event.getY();
                    posx += dx;
                    posy += dy;

                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(LOGNAME, "Action up");
                    //Log.d("NUMPOINTs", String.valueOf(path.getNumPoints()));


                    break;
            }
        }

        invalidate();
        return true;
    }

    // set image as background
    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    // set brush characteristics
    // e.g. color, thickness, alpha
//    public void setBrush(Paint paint) {
//        this.paint = paint;
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw background

        super.onDraw(canvas);
        if (bitmap != null) {
            this.setImageBitmap(bitmap);
        }

        this.setScaleX(scale);
        this.setScaleY(scale);
        this.setTranslationX(posx * scale);
        this.setTranslationY(posy * scale);

        canvas.save();
        canvas.scale(scale,scale);
        canvas.translate(posx / scale,posy / scale);
        canvas.restore();


        for (Mpath path : Model.getInstance().getPaths()) {
            switch (path.getType()){
                case "DRAW" : canvas.drawPath(path, dPaint); break;
                //case "ERASE" : canvas.drawPath(path, ePaint); break;
                case "HL" : canvas.drawPath(path, hPaint); break;
            }
            //canvas.drawPath(path, paint);
        }


        //canvas


    }

    public void setType(String type) {
        this.type = type;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            scale *= detector.getScaleFactor();
            scale = Math.max(0.1f, Math.min(10.0f, scale));

            //Model.getInstance().applyScale(scale);

            Log.d("Scale", String.valueOf(scale));



            invalidate();

            //return true;
            return super.onScale(detector);
        }
    }
}
