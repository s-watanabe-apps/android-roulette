package com.example.swatanabe.roulette;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by swatanabe on 2017/09/21.
 */
public class MainActivity extends AppCompatActivity {

    // 当選決定用乱数オブジェクト
    private Random random;

    // ルーレットパネルリスト配列
    private List<RouletteItem> items;

    // ルーレットView
    private RouletteView rouletteView;

    // ルーレットのパネルカラー
    private int[] colors = {
            Color.argb(255, 255, 255, 128),
            Color.argb(255, 255, 128, 255),
            Color.argb(255, 128, 255, 255),
    };

    // アニメーションオブジェクト
    private ArcAnimation animation;

    // Canvas 中心点
    private float xc = 0.0f;
    private float yc = 0.0f;

    // ルーレットのパネル描画用
    private RectF rectF = null;

    // ルーレットの針描画用
    private Path path;

    // タッチイベントを処理するためのインタフェース
    private GestureDetector gestureDetector;

    // タッチイベント
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    // タッチイベントのリスナー
    private final GestureDetector.SimpleOnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
        // フリックイベント
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if(!rouletteView.isAnimation) {
                // 当選パネルを決定
                int win = random.nextInt(items.size());

                // 当選パネルまでに必要な角度を算出
                int moveMinAngle = 270 - rouletteView.getAngle() * (win + 1);
                int moveMaxAngle = 270 - rouletteView.getAngle() * win;

                if(moveMinAngle < 0 && moveMaxAngle >= 0) {
                    moveMinAngle = 0;
                } else if(moveMinAngle < 0 && moveMaxAngle < 0) {
                    moveMinAngle += 360;
                    moveMaxAngle += 360;
                }

                List<Integer> buffers = new ArrayList<>();
                for(int i = 0; i < ArcAnimation.ANIM_COUNT_BASE; i++) {
                    buffers.add(0);
                }

                // 移動する値の調整
                buffers.set(0, (moveMinAngle - rouletteView.getPos()) / ArcAnimation.ANIM_MOVE_BASE + 1);

                // アニメーション開始
                rouletteView.setIsAnimation(true);
                animation = new ArcAnimation(rouletteView);
                animation.setDuration(20000);
                animation.setBuffers(buffers);
                rouletteView.startAnimation(animation);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    /**
     * 初期化
     */
    private void init() {
        items = new ArrayList<>();
        items.add(new RouletteItem("カレーライス"));
        items.add(new RouletteItem("肉じゃが"));
        items.add(new RouletteItem("オムレツ"));

        // 当選決定用乱数オブジェクト生成
        random = new Random(System.currentTimeMillis());

        gestureDetector = new GestureDetector(this, onGestureListener);

        // 全体のレイアウト
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        ));

        // ルーレットView作成
        rouletteView = new RouletteView(this, items);
        linearLayout.addView(rouletteView);

        setContentView(linearLayout);

    }

    /**
     * ルーレットViewクラス
     */
    class RouletteView extends View {
        // アニメーション実行フラグ
        private boolean isAnimation = false;

        // アニメーション実行判定
        public void setIsAnimation(boolean isAnimation) {
            this.isAnimation = isAnimation;
        }

        // パネルのPaintオブジェクト
        Paint paint;

        // テキストのPaintオブジェクト
        Paint textPaint;

        // 針のPaintオブジェクト
        Paint needlePaint;

        // パネル１つ分の角度
        int angle;

        // ルーレットの要素
        List<RouletteItem> items;

        // Move値
        int pos;

        // 初期設定フラグ
        int init = 0;

        public RouletteView(Context context, List<RouletteItem> items) {
            super(context);
            paint = new Paint();
            paint.setAntiAlias(true);

            textPaint = new Paint(Color.DKGRAY);
            textPaint.setTextSize(60);
            textPaint.setAntiAlias(true);

            needlePaint = new Paint(Color.BLACK);
            needlePaint.setAntiAlias(true);

            pos = 0;

            this.items = items;
            angle = 360 / this.items.size();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // 背景
            canvas.drawColor(Color.WHITE);

            if(init == 0) {
                // Canvas 中心点
                if(xc == 0.0f) xc = canvas.getWidth() / 2;
                if(yc == 0.0f) yc = canvas.getHeight() / 2;

                // 画面の中心から横幅に合わせた正方形を作る
                if(rectF == null) {
                    rectF = new RectF(0.0f, yc - xc, canvas.getWidth(), yc + xc);
                }

                if(path == null) {
                    path = new Path();
                    path.moveTo(xc - 30, yc - xc - 50);
                    path.lineTo(xc, yc - 480);
                    path.lineTo(xc + 30, yc - xc - 50);
                    path.close();
                }

                init = 1;
            }

            // パネルの描画
            for (int i = 0; i < items.size(); i++) {
                paint.setColor(colors[i]);
                canvas.drawArc(rectF, (i * angle) + pos, angle, true, paint);
            }

            // 針の描画
            canvas.drawPath(path, needlePaint);

            // テキストの描画
            for (int j = 0; j < items.size(); j++) {
                int intTextAngle;
                if(j == 0) {
                    intTextAngle = angle / 2 + pos;
                } else{
                    intTextAngle = angle;
                }
                canvas.rotate(intTextAngle, xc, yc);
                canvas.drawText(items.get(j).getName(), xc + 100, yc + 20, textPaint);
            }

        }

        public void addPos(int move) {
            pos += move;

            if(pos >= 360) {
                pos -= 360;
            }
        }

        public int getPos() {
            return pos;
        }

        public int getAngle() {
            return angle;
        }
    }
}
