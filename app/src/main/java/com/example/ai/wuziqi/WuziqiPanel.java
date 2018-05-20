package com.example.ai.wuziqi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WuziqiPanel extends View {

    private int mPanelWidth;//棋盘宽度
    private float mLineHeight;//行高
    private int MAX_LINE = 10;//最大行数

    private int MAX_COUNT_IN_LINE = 5;

    private Paint mPaint = new Paint();

    private Bitmap mWhitePiece;
    private Bitmap mBlackPiece;

    //约束棋子的大小的比例值
    //棋子的直径占mLineHeight的3/4
    private float radioPieceOfLineHeight = 3 * 1.0f / 4;


    //白棋先手，当前轮到白棋
    private boolean mIsWhite = true;
    private ArrayList<Point> mWhiteArray = new ArrayList<>();
    private ArrayList<Point> mBlackArray = new ArrayList<>();


    private boolean mIsGameOver;
    private boolean mIsWhiteWinner;

    //是否是胜利后第一次出现对话框
    private boolean mIsFirstDialog=true;

    public void setAnotherGameListener(AnotherGame anotherGame) {
        this.mAnotherGame = anotherGame;
    }

    private AnotherGame mAnotherGame;


    public WuziqiPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setBackgroundColor(0x44ff0000);

        init();


    }

    private void init() {
        mPaint.setColor(0x88000000);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);//画线

        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.white_chess);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.black_chess);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heigthMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize, heightSize);
        /**
         * 防止ScrollView嵌套而导致的看不到自定义View问题
         * 有可能取最小宽度会等于0，防止这种情况
         *
         */
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heigthMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }

        //棋盘宽高设置一样的值
        setMeasuredDimension(width, width);
    }

    /**
     * View的宽高发生改变时调用
     * w，h是view当前的宽和高;oldw ,oldh是改变之前的宽和高。
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    //和尺寸相关的值在这里进行设置和修改
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mPanelWidth = w;
        mLineHeight = mPanelWidth * 1.0f / MAX_LINE;

        int pieceWidth = (int) (mLineHeight * radioPieceOfLineHeight);
        //将原棋子按照比例放缩
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mIsGameOver) {
            return false;
        }

        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            Point p = getValidPoint(x, y);
            //判断一个点是否已经下过棋子
            if (mWhiteArray.contains(p) || mBlackArray.contains(p)) {

                return false;

            }

            if (mIsWhite) {
                mWhiteArray.add(p);
            } else {
                mBlackArray.add(p);
            }
            //请求重绘
            invalidate();

            //当前轮到另外一种棋子
            mIsWhite = !mIsWhite;

        }
        return true;


    }

    private Point getValidPoint(int x, int y) {
        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        drawBoard(canvas);

        drawPieces(canvas);

        checkGameOver();
    }

    private void checkGameOver() {

        boolean whiteWin = checkFiveInLine(mWhiteArray);
        boolean blackWin = checkFiveInLine(mBlackArray);

        if (whiteWin || blackWin) {

            mIsGameOver = true;

            mIsWhiteWinner = whiteWin;
            String text = mIsWhiteWinner ? "白棋胜利" : "黑棋胜利";
            if (mIsFirstDialog){
                mAnotherGame.AnotherGame(text);
                mIsFirstDialog=false;
            }

            //Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        }

    }

    public interface AnotherGame {
        public void AnotherGame(String str);
    }

    /**
     * 判断是否五子连珠
     *
     * @param points
     * @return
     */
    private boolean checkFiveInLine(List<Point> points) {
        for (Point p : points) {
            int x = p.x;
            int y = p.y;

            boolean win = checkHorizontal(x, y, points);
            if (win) return true;

            win = checkVertical(x, y, points);
            if (win) return true;

            win = checkLeftSlant(x, y, points);
            if (win) return true;

            win = checkRightSlant(x, y, points);
            if (win) return true;
        }

        return false;
    }

    /**
     * 判断横向是否五子连珠
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkHorizontal(int x, int y, List<Point> points) {

        int count = 1;
        //判断左边
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }

        return false;
    }

    /**
     * 判断纵向是否五子连珠
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkVertical(int x, int y, List<Point> points) {

        int count = 1;
        //判断左边
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }

        return false;
    }

    /**
     * 判断左斜是否五子连珠
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkLeftSlant(int x, int y, List<Point> points) {

        int count = 1;
        //判断左边
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }

        return false;
    }

    /**
     * 判断右斜是否五子连珠
     *
     * @param x
     * @param y
     * @param points
     * @return
     */

    private boolean checkRightSlant(int x, int y, List<Point> points) {

        int count = 1;
        //判断左边
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }

        return false;
    }


    //画棋子
    private void drawPieces(Canvas canvas) {

        //画白棋
        for (int i = 0, n = mWhiteArray.size(); i < n; i++) {
            Point whitePoint = mWhiteArray.get(i);

            canvas.drawBitmap(mWhitePiece,
                    (whitePoint.x + (1 - radioPieceOfLineHeight) / 2) * mLineHeight,
                    (whitePoint.y + (1 - radioPieceOfLineHeight) / 2) * mLineHeight,
                    null);
        }
        //画黑棋
        for (int i = 0, n = mBlackArray.size(); i < n; i++) {
            Point blackPoint = mBlackArray.get(i);

            canvas.drawBitmap(mBlackPiece,
                    (blackPoint.x + (1 - radioPieceOfLineHeight) / 2) * mLineHeight,
                    (blackPoint.y + (1 - radioPieceOfLineHeight) / 2) * mLineHeight,
                    null);
        }


    }

    //画棋盘
    private void drawBoard(Canvas canvas) {

        int w = mPanelWidth;

        float lineHeight = mLineHeight;

        for (int i = 0; i < MAX_LINE; i++) {

            int startX = (int) (lineHeight / 2);//离左边界半个行高处开始
            int endX = (int) (w - lineHeight / 2);//离右边界半个行高处结束

            int y = (int) ((0.5 + i) * lineHeight);//每一行开始的高度

            //棋盘横向线
            canvas.drawLine(startX, y, endX, y, mPaint);
            //棋盘纵向线
            canvas.drawLine(y, startX, y, endX, mPaint);


        }

    }

    /**
     * 重新开始一局
     */
    public void start() {

        mWhiteArray.clear();
        mBlackArray.clear();
        mIsGameOver = false;
        mIsWhiteWinner = false;
        mIsFirstDialog=true;
        invalidate();
    }

    /**
     * 注意：：：：自定义的View状态的存储与恢复，前提是view在布局文件中必须有id，这样才会有用
     */
    private static final String INSTANCE = "instance";
    private static final String INSTANCE_GAME_OVER = "instance_game_over";
    private static final String INSTANCE_FIRST_DIALOG="instance_first_dialog";
    private static final String INSTANCE_WHITE_ARRAY = "instance_white_array";
    private static final String INSTANCE_BLACK_ARRAY = "instance_black_array";

    /**
     * 保存状态
     *
     * @return
     */
    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();

        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAME_OVER, mIsGameOver);
        bundle.putBoolean(INSTANCE_FIRST_DIALOG,mIsFirstDialog);
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY, mWhiteArray);
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY, mBlackArray);
        return bundle;

    }

    /**
     * 恢复状态
     *
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {

            Bundle bundle = (Bundle) state;
            mIsGameOver = bundle.getBoolean(INSTANCE_GAME_OVER);
            mIsFirstDialog=bundle.getBoolean(INSTANCE_FIRST_DIALOG);
            mWhiteArray = bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
            mBlackArray = bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);

            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

}
