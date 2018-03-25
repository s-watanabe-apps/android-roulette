package com.example.swatanabe.roulette;

import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by swatanabe on 2017/09/21.
 */
public class ArcAnimation extends Animation {

    public static final int ANIM_COUNT_BASE = 24;

    public static final int ANIM_MOVE_BASE = 20;

    List<Integer> buffers;

    int count = 0;

    private MainActivity.RouletteView rouletteView;

    public void setBuffers(List<Integer> buffers) {
        this.buffers = buffers;
    }

    public ArcAnimation(MainActivity.RouletteView rouletteView) {
        this.rouletteView = rouletteView;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {

        int i = count / ANIM_COUNT_BASE;

        // バッファが設定してある場合カウンタを止める
        if(buffers != null && buffers.get(i) > 0) {
            buffers.set(i, buffers.get(i) - 1);
        } else{
            count++;
        }

        int move = ANIM_MOVE_BASE - i;

        rouletteView.addPos(move);
        rouletteView.requestLayout();

        if(move == 0){
            cancel();
            rouletteView.setIsAnimation(false);
        }
    }
}
