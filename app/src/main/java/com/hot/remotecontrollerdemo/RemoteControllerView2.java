package com.hot.remotecontrollerdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by 有人@我 on 2017/9/6.
 */

public class RemoteControllerView2 extends View {
    private static final String TAG = "RemoteControllerView";
    private static final float SCALE_OF_PADDING = 0.125F;
    private static final float SCALE_OF_BIG_CIRCLE = 288.F / 320;
    //小圆的半径比例
    private static final float SCALE_OF_SMALL_CIRCLE = 150.F / 320;
    private static final float DEF_VIEW_SIZE = 300;
    private static final float SCALE_OF_TRIANGLE_LENGTH = 0.114F;
    private OnRemoteControllerClickListener remoteControllerClickListener;
    private static final int SELECT_RIGHT = 0;
    private static final int SELECT_BOTTOM = 1;
    private static final int SELECT_LEFT = 2;
    private static final int SELECT_TOP = 3;
    private static final int SELECT_CENTER = 4;
    private static final int SELECT_NO_SELECTED = -1;


    private int viewContentHeight;
    private int viewContentWidht;
    private Point centerPoint;
    private int rcvTextColor;
    private int rcvShadowColor;
    private int rcvStrokeColor;
    private int rcvStrokeWidth;
    private int rcvTextSize;
    private int rcvDegree;
    private int rcvOtherDegree;
    private Paint rcvTextPaint;
    private Paint rcvShadowPaint;
    private Paint rcvStrokePaint;
    private Paint rcvWhitePaint;

    //外面大圆--@{
    private float bigCircleRadius;
    private Paint bigCircleShadowPaint;
    //--@}
    //四个扇形区域 --@{
    private List<Path> ovalPaths;
    private List<Region> ovalRegions;
    private List<Paint> ovalPaints;
    private int ovalStrokeWidth=10;
    //--@}
    //四个扇形在靠近小圆边缘的 --@{
    private Path[] smallOvalPaths;
    private Paint smallOvalPaint;
    //--@}
    private float smallCircleRadius;
    //中间小圆Path---@{
    private Path smallCirclePath;
    private Region smallCircleRegion;
    private Paint smallCirclePaint;

    //--@}
    //中间小圆外面阴影---@{
    private Paint smallCircleShadowPaint;
    //--@}
    private int selected =SELECT_NO_SELECTED;
    private Point textPointInView;
    private Path[] trianglePaths;
    private Paint[] trianglePaints;
    private int triangleLength;


    public RemoteControllerView2(Context context) {
        this(context, null);
    }

    public RemoteControllerView2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RemoteControllerView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttribute(context, attrs, defStyleAttr);
        initPaints();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.e("HLA", "w:" + w + " h:" + h + " oldW:" + oldw + " oldh:" + oldh);
        super.onSizeChanged(w, h, oldw, oldh);
        centerPoint = new Point(w / 2, h / 2);
        ovalPaths = new ArrayList<>();
        ovalRegions = new ArrayList<>();
        ovalPaints = new ArrayList<>();
        trianglePaths = new Path[4];
        trianglePaints = new Paint[4];
        smallOvalPaths=new Path[4];
        int rcvPadding = (int) (Math.min(w, h) * SCALE_OF_PADDING);
        viewContentWidht = w - rcvPadding;
        viewContentHeight = h - rcvPadding;
        bigCircleRadius = Math.min(viewContentWidht, viewContentHeight) / 2;
        smallCircleRadius = Math.min(w, h) * SCALE_OF_SMALL_CIRCLE / 2;
        //小三角形的边长
        triangleLength = (int) (Math.min(viewContentWidht, viewContentHeight) * 0.5 * SCALE_OF_TRIANGLE_LENGTH);
        textPointInView = getTextPointInView(rcvTextPaint, "OK", 0, 0);
        // 注意外环的线宽占用的尺寸
        RectF ovalRectF = new RectF(-viewContentWidht / 2 + rcvStrokeWidth,
                -viewContentHeight / 2 + rcvStrokeWidth,
                viewContentWidht / 2 - rcvStrokeWidth,
                viewContentHeight / 2 - rcvStrokeWidth);
        //小圆的宽带
        RectF smallOvalRectF = new RectF(-smallCircleRadius  - ovalStrokeWidth/2,
                -smallCircleRadius  - ovalStrokeWidth/2,
                smallCircleRadius  + ovalStrokeWidth/2,
                smallCircleRadius  + ovalStrokeWidth/2);
        for (int i = 0; i < 4; i++) {
            Path smallOPath = new Path();
            Region tempRegin = new Region();

            Path tempPath = new Path();
            float tempStarAngle = 0;
            float tempSweepAngle;
            if (i % 2 == 0) {
                tempSweepAngle = rcvDegree;
            } else {
                tempSweepAngle = rcvOtherDegree;
            }
            switch (i) {
                case 0:
                    tempStarAngle = -rcvDegree / 2;
                    break;
                case 1:
                    tempStarAngle = rcvDegree / 2;
                    break;
                case 2:
                    tempStarAngle = rcvDegree / 2 + rcvOtherDegree;
                    break;
                case 3:
                    tempStarAngle = rcvDegree / 2 + rcvOtherDegree + rcvDegree;
                    break;

            }
            tempPath.moveTo(0, 0);
            tempPath.addArc(ovalRectF, tempStarAngle, tempSweepAngle);
            tempPath.lineTo(0, 0);
            tempPath.close();
            smallOPath.moveTo(0, 0);
            smallOPath.addArc(smallOvalRectF, tempStarAngle, tempSweepAngle);
            smallOvalPaths[i]=smallOPath;
            RectF tempRectF = new RectF();
            tempPath.computeBounds(tempRectF, true);
            tempRegin.setPath(tempPath, new Region((int) tempRectF.left, (int) tempRectF.top, (int) tempRectF.right, (int) tempRectF.bottom));
            ovalPaths.add(tempPath);
            ovalRegions.add(tempRegin);
            ovalPaints.add(creatPaint(Color.WHITE, 0, Paint.Style.STROKE, ovalStrokeWidth));
        }

        smallCircleRegion = new Region();
        smallCirclePath = new Path();
        smallCirclePath.moveTo(0, 0);
        smallCirclePath.addCircle(0, 0, smallCircleRadius, Path.Direction.CW);
        RectF tempRectF = new RectF();
        smallCirclePath.computeBounds(tempRectF, true);
        smallCircleRegion.setPath(smallCirclePath, new Region((int) tempRectF.left, (int) tempRectF.top, (int) tempRectF.right, (int) tempRectF.bottom));

        float x = (viewContentWidht / 2 - smallCircleRadius) / 2 + smallCircleRadius;
        float y = (viewContentHeight / 2 - smallCircleRadius) / 2 + smallCircleRadius;
        Path path0 = new Path();//右侧三角号
        path0.moveTo(x - 0.25F * triangleLength, 0.866F * triangleLength);
        path0.lineTo(x - 0.25F * triangleLength, -0.866F * triangleLength);
        path0.lineTo(x + 0.616F * triangleLength, 0);
        path0.close();
        Path path1 = new Path();//左侧三角号
        path1.moveTo(-x + 0.25F * triangleLength, 0.866F * triangleLength);
        path1.lineTo(-x + 0.25F * triangleLength, -0.866F * triangleLength);
        path1.lineTo(-x - 0.616F * triangleLength, 0);
        path1.close();
        Path path2 = new Path();//上方三角号
        path2.moveTo(0.866F * triangleLength, -y + 0.25F * triangleLength);
        path2.lineTo(-0.866F * triangleLength, -y + 0.25F * triangleLength);
        path2.lineTo(0, -y - 0.616F * triangleLength);
        path2.close();
        Path path3 = new Path();//下方三角号
        path3.moveTo(0.866F * triangleLength, y - 0.25F * triangleLength);
        path3.lineTo(-0.866F * triangleLength, y - 0.25F * triangleLength);
        path3.lineTo(0, y + 0.616F * triangleLength);
        path3.close();
        trianglePaths[0] = path0;
        trianglePaths[1] = path3;
        trianglePaths[2] = path1;
        trianglePaths[3] = path2;
        trianglePaints[0] = creatPaint(Color.GRAY, 0, Paint.Style.FILL, 0);
        trianglePaints[1] = creatPaint(Color.GRAY, 0, Paint.Style.FILL, 0);
        trianglePaints[2] = creatPaint(Color.GRAY, 0, Paint.Style.FILL, 0);
        trianglePaints[3] = creatPaint(Color.GRAY, 0, Paint.Style.FILL, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int widthSize;
        int heightSize;

        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_VIEW_SIZE, getResources().getDisplayMetrics());
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        }

        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_VIEW_SIZE, getResources().getDisplayMetrics());
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(centerPoint.x, centerPoint.y);
        canvas.drawCircle(0, 0, bigCircleRadius, bigCircleShadowPaint);
        canvas.drawCircle(0, 0, smallCircleRadius, smallCircleShadowPaint);
        switch (selected) {
            case SELECT_RIGHT:
            case SELECT_BOTTOM:
            case SELECT_LEFT:
            case SELECT_TOP:
                canvas.drawPath(ovalPaths.get(selected), ovalPaints.get(selected));
                canvas.drawPath(smallOvalPaths[selected], smallOvalPaint);
                break;
        }
//        for (int i = 0; i < ovalPaths.size(); i++) {
//            canvas.drawPath(ovalPaths.get(i), ovalPaints.get(i));
//        }
        for (int i = 0; i < 4; i++) {
            canvas.drawPath(trianglePaths[i], trianglePaints[i]);
        }
        canvas.drawPath(smallCirclePath, smallCirclePaint);
        canvas.drawText("OK", textPointInView.x, textPointInView.y, rcvTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x;
        float y;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX() - centerPoint.x;
                y = event.getY() - centerPoint.y;
                selected = SELECT_NO_SELECTED;
                if (smallCircleRegion.contains((int) x, (int) y)) {
                    resetPaints();
                    smallCirclePaint.setColor(rcvShadowColor);
                    selected = SELECT_CENTER;
                    invalidate();
                    break;
                }
                for (int i = 0; i < ovalRegions.size(); i++) {
                    Region tempRegin = ovalRegions.get(i);
                    boolean contains = tempRegin.contains((int) x, (int) y);
                    if (contains) {
                        selected = i;
                        resetPaints();
                        ovalPaints.get(selected).setColor(rcvShadowColor);
                        trianglePaints[selected].setColor(Color.RED);
                        Log.e("HLA", "t:" + i);
                        invalidate();
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                remoteClickAction();
                selected = SELECT_NO_SELECTED;
                resetPaints();
                invalidate();

                break;

        }
        return true;
    }

    private void remoteClickAction() {
        if (remoteControllerClickListener != null) {
            switch (selected) {
                case SELECT_RIGHT:
                    remoteControllerClickListener.rightClick();
                    break;
                case SELECT_BOTTOM:
                    remoteControllerClickListener.bottomClick();
                    break;
                case SELECT_LEFT:
                    remoteControllerClickListener.leftClick();
                    break;
                case SELECT_TOP:
                    remoteControllerClickListener.topClick();
                    break;
                case SELECT_CENTER:
                    remoteControllerClickListener.centerOkClick();
                    break;
            }
        }
    }


    private void initAttribute(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RemoteControllerView, defStyleAttr, R.style.def_remote_controller);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.RemoteControllerView_rcv_text_color:
                    rcvTextColor = typedArray.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.RemoteControllerView_rcv_text_size:
                    rcvTextSize = typedArray.getDimensionPixelSize(attr, 0);
                    break;
                case R.styleable.RemoteControllerView_rcv_shadow_color:
                    rcvShadowColor = typedArray.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.RemoteControllerView_rcv_stroke_color:
                    rcvStrokeColor = typedArray.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.RemoteControllerView_rcv_stroke_width:
                    rcvStrokeWidth = typedArray.getDimensionPixelOffset(attr, 0);
                    break;
                case R.styleable.RemoteControllerView_rcv_oval_degree:
                    rcvDegree = typedArray.getInt(attr, 0);
                    rcvOtherDegree = (int) ((360 - rcvDegree * 2) / 2.F);
                    break;

            }
        }
        typedArray.recycle();
    }


    private void initPaints() {
        rcvTextPaint = creatPaint(rcvTextColor, rcvTextSize, Paint.Style.FILL, 0);
        rcvShadowPaint = creatPaint(rcvShadowColor, 0, Paint.Style.FILL, 0);
        rcvStrokePaint = creatPaint(rcvStrokeColor, 0, Paint.Style.STROKE, 0);
        rcvWhitePaint = creatPaint(Color.WHITE, 0, Paint.Style.FILL, 0);

        smallCirclePaint = creatPaint(rcvTextColor, rcvTextSize, Paint.Style.FILL, 0);
        bigCircleShadowPaint = creatPaint(Color.WHITE, 0, Paint.Style.FILL_AND_STROKE, 0);
        bigCircleShadowPaint.setShadowLayer(20f, 0, 0, Color.BLUE);
        smallCircleShadowPaint = creatPaint(rcvTextColor, 0, Paint.Style.STROKE, 0);
        smallCircleShadowPaint.setShadowLayer(20f, 0, 0, Color.BLUE);
        smallOvalPaint=creatPaint(rcvTextColor, rcvTextSize, Paint.Style.STROKE, ovalStrokeWidth);
    }


    private Paint creatPaint(int paintColor, int textSize, Paint.Style style, int lineWidth) {
        Paint paint = new Paint();
        paint.setColor(paintColor);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(lineWidth);
        paint.setDither(true);
        paint.setTextSize(textSize);
        paint.setStyle(style);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        return paint;
    }

    private void resetPaints() {
        for (Paint p : ovalPaints) {
            p.setColor(Color.WHITE);
        }
        for (Paint p : trianglePaints) {
            p.setColor(Color.GRAY);
        }
        smallCirclePaint.setColor(Color.WHITE);
    }

    private Point getTextPointInView(Paint textPaint, String textDesc, int w, int h) {
        if (null == textDesc) return null;
        Point point = new Point();
        int textW = (w - (int) textPaint.measureText(textDesc)) / 2;
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        int textH = (int) Math.ceil(fm.descent - fm.top);
        point.set(textW, h / 2 + textH / 2 - textH / 4);
        return point;
    }


    public interface OnRemoteControllerClickListener {
        void topClick();

        void leftClick();

        void rightClick();

        void bottomClick();

        void centerOkClick();
    }

    public void setRemoteControllerClickListener(OnRemoteControllerClickListener remoteControllerClickListener) {
        this.remoteControllerClickListener = remoteControllerClickListener;
    }
}
