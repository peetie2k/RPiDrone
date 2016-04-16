package com.peetie.rpidrone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;


/**
 * TODO: document your custom view class.
 */
public class HUDOverlayView extends SurfaceView implements Callback {
    private SurfaceHolder holder;
    private Canvas canvas;
    private Paint paint;
    private Thread tDraw;
    private boolean isRunning = false;
    private float compassDegrees;
    private TelemMock telemMock;
    private FrSkyTelemetryData telemData;


    //Hud Config Variables====================================
    //General.

    private int sidePadding = 300;
    private int topPadding = 100;
    private int alpha = 80;
    private int color = Color.WHITE;
    private int HUDStrokeWidth = 10;

    //Compass
    private int compassLineThickness60deg = 15;  //Thickness N,NE,E,SE,S,SW,W,NW
    private int compassLineLength60deg = 60;     //Length N,NE,E,SE,S,SW,W,NW
    private int compassLineThickness = 5;
    private int compassLineLength15deg = 60;
    private int compassLineLength5deg = 30;
    private int compassBottomPadding = 120;
    private int compassTextSizeBig = 50;
    private int compassTextSizeSmall = 30;


    //Position
    private int positionSidePadding = 240;
    private int positionMidPadding = 60;

    private int maxPitchTransform = 300;


    //Throttle + Battery Indicator
    private int throttleAndBatteryIndicatorWidth = 50;
    private int throttleLineThicknessMajor = 10;
    private int throttleLineThicknessMinor = 5;
    private int throttleLineLengthMajor = 30;
    private int throttleLineLengthMinor = 15;
    private int throttleIndicatorAlpha = 40;
    private int throttleIndTextSize = 30;


    //middle Indicator
    private int middleIndWidth = 0;
    private int middleIndHeight = 40;




    //LowPasses
    private LowPass throttleLowPass;



    //================================================


    public HUDOverlayView(Context context, AttributeSet attrs,
                          int defStyle) {
        super(context, attrs, defStyle);
        init();
        setZOrderMediaOverlay(true);
        this.getHolder().setFormat(PixelFormat.TRANSPARENT);

    }

    public HUDOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        setZOrderMediaOverlay(true);
        this.getHolder().setFormat(PixelFormat.TRANSPARENT);
    }

    public HUDOverlayView(Context context) {
        super(context);
        init();
        setZOrderMediaOverlay(true);
        this.getHolder().setFormat(PixelFormat.TRANSPARENT);
    }

    public void init() {

        //Mock the telemetry
        //telemMock = new TelemMock(this.getContext());
        telemData = FrSkyTelemetryData.getInstance();


        //Initiate Low Pass Filters
        throttleLowPass = new LowPass(0.05f, 0.3f,1.0f);


        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(50);

        //Thread zum Zeichnen
        tDraw = new Thread(new Runnable() {
            public void run() {
                while (isRunning) {
                    draw(canvas);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }

            }
        });

        tDraw.start();

        isRunning = true;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void surfaceCreated(SurfaceHolder holder) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void draw(Canvas c) {

        c = holder.lockCanvas();
        if (c != null) {

            //c.drawColor(Color.TRANSPARENT);
            c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            int currentDegree = telemData.getData(FrSkyTelemetryData.ID_COURSE_BP);
            currentDegree = this.smoothCompass(currentDegree);
            currentDegree = currentDegree - 180;
            if (currentDegree < 0)
                currentDegree = currentDegree + 360;
            //c.drawCircle(50, 50, 50, paint);
            this.drawCompass(c, currentDegree);
            this.drawMiddleIndicator(c);



            // Throttle drawing smoothed
            float throttle = telemData.getData(FrSkyTelemetryData.ID_RPM);
            if (throttle < 230.0f)
                    throttle = 0;
            else
            throttle = (throttle - 230.0f) / 135.0f ;
            throttle = throttleLowPass.smooth(throttle);
            this.drawThrottle(c, throttle);



            ///////////////////Data for Poisition
            double accXaxis = telemData.getData(FrSkyTelemetryData.ID_ACC_X) ;
            double accYaxis = telemData.getData(FrSkyTelemetryData.ID_ACC_Y) ;
            double accZaxis = telemData.getData(FrSkyTelemetryData.ID_ACC_Z) ;
              if( accXaxis > 32768 )
                accXaxis = (accXaxis - 65536) / 4069 * -1;
            else
                accXaxis = accXaxis / 4069 * -1;

            if( accYaxis > 32768 )
                accYaxis = (accYaxis - 65536) / 4069* -1;
            else
                accYaxis = accYaxis / 4069* -1;

            if( accZaxis > 32768 )
                accZaxis = (accZaxis - 65536) / 4069* -1;
            else
                accZaxis = accZaxis / 4069* -1;

            float roll = (float) calculateRoll(accXaxis, accYaxis, accZaxis);
            float pitch =  (float) calculatePitch(accXaxis, accYaxis, accZaxis);
            this.drawPosition(c, pitch, roll);
            //////////////////////////////////////////////////////


            holder.unlockCanvasAndPost(c);





        }

    }

    private void drawCompass(Canvas canvas, int degree) {
        float middle = this.getWidth() / 2;
        float drawWidth = this.getWidth() - sidePadding * 2;
        float stepWidth = drawWidth / 90;


        float degreeFrom = degree - 45;
        float degreeTo = (degree + 45);
        if (degreeFrom < 0)
            degreeFrom = 360 + degreeFrom;
        if (degreeTo > 360)
            degreeTo = 360 - degreeTo;

        String text = new String();


        //draw Triangle as indicator
        Paint tpaint = new Paint();
        tpaint.setColor(color);
        tpaint.setAlpha(alpha);
        tpaint.setStyle(Paint.Style.FILL_AND_STROKE);
        tpaint.setAntiAlias(true);
        tpaint.setStrokeWidth(4);
        Point a = new Point((int) middle, (int) (this.getHeight() - compassBottomPadding - 10));
        Point b = new Point((int) middle + 25, (int) (this.getHeight() - compassBottomPadding - 40));
        Point c = new Point((int) middle - 25, (int) (this.getHeight() - compassBottomPadding - 40));

        Path path = new Path();
        path.moveTo(b.x, b.y);
        path.setFillType(Path.FillType.EVEN_ODD);
        path.lineTo(b.x, b.y);
        path.lineTo(c.x, c.y);
        path.lineTo(a.x, a.y);
        path.close();

        canvas.drawPath(path, tpaint);


        float currentDegree = degreeFrom;
        float currentPosition = sidePadding;
        //draw degree
        for (int i = 0; i < 91; i++) {
            if (currentDegree % 45 == 0) { //N,NE,...
                float left = currentPosition - compassLineThickness60deg / 2;
                float top = this.getHeight() - compassBottomPadding;
                float right = currentPosition + compassLineThickness60deg / 2;
                float bottom = top + compassLineLength60deg;

                canvas.drawRect(left, top, right, bottom, paint);

                switch ((int) currentDegree) {
                    case 0: //N
                        text = "N";
                        break;
                    case 45: //NE
                        text = "NE";
                        break;
                    case 90: //E
                        text = "E";
                        break;
                    case 135: //SE
                        text = "SE";
                        break;
                    case 180: //S
                        text = "S";
                        break;
                    case 225: //SW
                        text = "SW";
                        break;
                    case 270: //W
                        text = "W";
                        break;
                    case 315: //NW
                        text = "NW";
                        break;
                }
                paint.setTextSize(compassTextSizeBig);
                canvas.drawText(text, currentPosition, top + compassLineLength60deg + 50, paint);

            } else {
                if (currentDegree % 15 == 0) {

                    float left = currentPosition - compassLineThickness / 2;
                    float top = this.getHeight() - compassBottomPadding;
                    float right = currentPosition + compassLineThickness / 2;
                    float bottom = top + compassLineLength15deg;

                    canvas.drawRect(left, top, right, bottom, paint);
                    paint.setTextSize(compassTextSizeSmall);
                    text = String.valueOf((int) currentDegree);
                    canvas.drawText(text, currentPosition, top + compassLineLength60deg + 50, paint);
                } else {
                    if (currentDegree % 5 == 0) {
                        float left = currentPosition - compassLineThickness / 2;
                        float top = this.getHeight() - compassBottomPadding;
                        float right = currentPosition + compassLineThickness / 2;
                        float bottom = top + compassLineLength5deg;

                        canvas.drawRect(left, top, right, bottom, paint);


                    }
                }
            }


            currentDegree = currentDegree + 1;
            if (currentDegree == 360)
                currentDegree = 0;
            currentPosition = currentPosition + stepWidth;
        }

    }


    private void drawSpeed(Canvas canvas, float speed) {

    }

    private void drawFuel(Canvas canvas, float speed) {

    }

    private void drawAlt(Canvas canvas, float alt) {

    }

    private void drawPosition(Canvas canvas, float pitch, float roll) {  //pitch -180 <-> 180, roll -180 <-> 180

        int pitchAvailable = ( this.getHeight() - compassBottomPadding - topPadding - 100 ) / 2;


        int startLeft = this.sidePadding + this.throttleAndBatteryIndicatorWidth + this.positionSidePadding;
        int endLeft = (int) (this.getWidth() / 2 - 4 * this.middleIndWidth / 8) - this.positionMidPadding;

        int startRight = (int) (this.getWidth() / 2 + 4 * this.middleIndWidth / 8) + this.positionMidPadding;
        int endRight = startRight + (endLeft - startLeft);

        float pitchTransform = pitch;//-167.0f; //this.maxPitchTransform;
        float rollTransform = roll;//50.0f;

        Point l1 = new Point(startLeft, this.getHeight() / 2);
        Point l2 = new Point(endLeft, this.getHeight() / 2);

        Point r1 = new Point(startRight, this.getHeight() / 2);
        Point r2 = new Point(endRight, this.getHeight() / 2);

        Paint tpaint = new Paint();
        tpaint.setColor(color);
        tpaint.setAlpha(alpha);
        tpaint.setStyle(Paint.Style.STROKE);
        tpaint.setAntiAlias(true);
        tpaint.setStrokeWidth(this.HUDStrokeWidth);

        Path path = new Path();
        path.moveTo(l1.x, l1.y);
        path.lineTo(l2.x, l2.y);
        path.moveTo(r1.x, r1.y);
        path.lineTo(r2.x, r2.y);

        Matrix mMatrix = new Matrix();
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        mMatrix.postRotate(rollTransform, bounds.centerX(), bounds.centerY());
        mMatrix.postTranslate(0.0f, pitchAvailable/90 * pitchTransform);
        path.transform(mMatrix);


        canvas.drawPath(path, tpaint);
    }

    private void drawMiddleIndicator(Canvas canvas) {

        //Initialize middle indicator if no settings stored
        if (middleIndWidth == 0)
            middleIndWidth = (this.getWidth() - 2 * sidePadding) / 8;


        Point midpoint = new Point(this.getWidth() / 2, this.getHeight() / 2);
        Point l1 = new Point((int) (this.getWidth() / 2 - middleIndWidth / 8), (int) (this.getHeight() / 2 + middleIndHeight));
        Point l2 = new Point((int) (this.getWidth() / 2 - 2 * middleIndWidth / 8), (int) (this.getHeight() / 2));
        Point l3 = new Point((int) (this.getWidth() / 2 - 4 * middleIndWidth / 8), (int) (this.getHeight() / 2));


        Point r1 = new Point((int) (this.getWidth() / 2 + middleIndWidth / 8), (int) (this.getHeight() / 2 + middleIndHeight));
        Point r2 = new Point((int) (this.getWidth() / 2 + 2 * middleIndWidth / 8), (int) (this.getHeight() / 2));
        Point r3 = new Point((int) (this.getWidth() / 2 + 4 * middleIndWidth / 8), (int) (this.getHeight() / 2));


        //draw Triangle as indicator
        Paint tpaint = new Paint();
        tpaint.setColor(color);
        tpaint.setAlpha(alpha);
        tpaint.setStyle(Paint.Style.STROKE);
        tpaint.setAntiAlias(true);
        tpaint.setStrokeWidth(this.HUDStrokeWidth);


        Path path = new Path();
        path.moveTo(l3.x, l3.y);
        path.lineTo(l2.x, l2.y);
        path.lineTo(l1.x, l1.y);
        path.lineTo(midpoint.x, midpoint.y);
        path.lineTo(r1.x, r1.y);
        path.lineTo(r2.x, r2.y);
        path.lineTo(r3.x, r3.y);


        canvas.drawPath(path, tpaint);

    }


    private void drawThrottle(Canvas canvas, float throttleLevel) {

        int height = this.getHeight() - compassBottomPadding - topPadding - 100;

        int scalaSize = 5;
        int level = 100 / scalaSize;
        int pixelStepSize = height / level;

        Point topLeft = new Point(this.sidePadding, topPadding);
        Point bottomRight = new Point(topLeft.x + this.throttleAndBatteryIndicatorWidth, topPadding + height);


        Paint rpaint = new Paint();
        rpaint.setTextSize(this.throttleIndTextSize);
        rpaint.setColor(color);
        rpaint.setAlpha(alpha);
        rpaint.setStyle(Paint.Style.STROKE);
        rpaint.setStrokeWidth(this.HUDStrokeWidth);
        rpaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawRect(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y, rpaint);


        //draw the scale
        Point pScale = new Point(this.sidePadding, this.topPadding + height);
        for (int i = 0; i <= level; i++) {
            Path scalePath = new Path();
            if (((i * scalaSize) % 25) == 0) {
                rpaint.setStrokeWidth(this.throttleLineThicknessMajor);
                scalePath.moveTo(pScale.x - this.HUDStrokeWidth / 2, (pScale.y - i * pixelStepSize));
                scalePath.lineTo(pScale.x - this.HUDStrokeWidth / 2 - throttleLineLengthMajor, (pScale.y - i * pixelStepSize));
                String scaleText = String.valueOf(i * scalaSize);
                paint.setTextSize(this.throttleIndTextSize);
                canvas.drawText(scaleText, pScale.x - this.HUDStrokeWidth / 2 - this.throttleLineLengthMajor - 50, (pScale.y - i * pixelStepSize + this.throttleLineThicknessMajor), paint);

            } else {
                rpaint.setStrokeWidth(this.throttleLineThicknessMinor);
                scalePath.moveTo(pScale.x - this.HUDStrokeWidth / 2, (pScale.y - i * pixelStepSize));
                scalePath.lineTo(pScale.x - this.HUDStrokeWidth / 2 - throttleLineLengthMinor, (pScale.y - i * pixelStepSize));
            }
            canvas.drawPath(scalePath, rpaint);

        }


        //draw the value
        int indHeight = height - this.HUDStrokeWidth;
        int indWidth = this.throttleAndBatteryIndicatorWidth - this.HUDStrokeWidth;


        int topLeftHeight = (int) (indHeight * (1 - throttleLevel)) + this.topPadding;


        Point indTopLeft = new Point(this.sidePadding + this.HUDStrokeWidth / 2, topLeftHeight);
        Point indBottomRight = new Point(topLeft.x + this.throttleAndBatteryIndicatorWidth - this.HUDStrokeWidth / 2, topPadding + height - this.HUDStrokeWidth / 2);
        rpaint.setAlpha(this.throttleIndicatorAlpha);
        rpaint.setStyle(Paint.Style.FILL);
        rpaint.setStrokeWidth(0);
        canvas.drawRect(indTopLeft.x, indTopLeft.y, indBottomRight.x, indBottomRight.y, rpaint);

    }


    private float SmoothFactorCompass = 0.2f;
    private float SmoothThresholdCompass = 180.0f;
    private float oldCompass = 0.0f;
    private float smoothedDegrees;

    private int smoothCompass(int newValue) {

        if (Math.abs(newValue - oldCompass) < 180) {
            if (Math.abs(newValue - oldCompass) > SmoothThresholdCompass) {
                oldCompass = newValue;
            } else {
                oldCompass = oldCompass + SmoothFactorCompass * (newValue - oldCompass);
            }
        } else {
            if (360.0 - Math.abs(newValue - oldCompass) > SmoothThresholdCompass) {
                oldCompass = newValue;
            } else {
                if (oldCompass > newValue) {
                    oldCompass = (oldCompass + SmoothFactorCompass * ((360 + newValue - oldCompass) % 360) + 360) % 360;
                } else {
                    oldCompass = (oldCompass - SmoothFactorCompass * ((360 - newValue + oldCompass) % 360) + 360) % 360;
                }
            }
        }
        this.smoothedDegrees = oldCompass;
        return (int) this.smoothedDegrees;

    }

    private double calculateRoll(double xAxis, double yAxis, double zAxis) {
        double roll = Math.atan(yAxis / Math.sqrt(Math.pow(xAxis, 2) + Math.pow(zAxis, 2)));
        roll = roll * (180.0/Math.PI);
        return roll;
    }

    private double calculatePitch(double xAxis, double yAxis, double zAxis) {
        double pitch = Math.atan((xAxis/Math.sqrt(Math.pow(yAxis,2) + Math.pow(zAxis,2))));
        pitch = pitch * (180.0/Math.PI);
        return pitch;
    }
}
