package com.outerspace.threadweaver;

import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements SeekBarCallback {

    float ballHBias = 0.5F;
    float ballVBias = 0.5F;
    float ballHOffset = 0F;
    float ballVOffset = 0F;

    ImageView ball;
    SeekBar vSeekBar = null, hSeekBar = null;

    Handler handler;

    Thread threadTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ball = findViewById(R.id.img_ball);

        vSeekBar = findViewById(R.id.v_position);
        hSeekBar = findViewById(R.id.h_position);

        SeekBarListener listener = new SeekBarListener(this);
        vSeekBar.setOnSeekBarChangeListener(listener);
        hSeekBar.setOnSeekBarChangeListener(listener);

        handler = new Handler();        // handler sticks to the Main UI Thread

        threadTwo = initializeThreadTwo();
    }

    public void onClickBtnThreadOne(View view) {
        // since handler works on the UI Thread, this Runnable is posted and runs in the UI Thread.
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.setHorizontalBias(0.5F);
                        MainActivity.this.setVerticalBias(0.5F);
                    }
                },
                500);
    }

    private static int N_DIVISIONS = 16;
    private Thread initializeThreadTwo() {
        return new Thread(
                new Runnable() {
                    float alpha = 0;
                    float delta = (float) (2 * Math.PI / N_DIVISIONS);
                    float n = 0;
                    @Override
                    public void run() {
                        while(true) {
                            try {
                                sleep(20);
                            } catch (InterruptedException e) {
                                break;
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.this.setHorizontalOffset(0.05F * ((float) Math.cos(delta * n)));
                                    MainActivity.this.setVerticalOffset(0.05F * ((float) Math.sin(delta * n)));
                                    MainActivity.this.placeBall();
                                }
                            });
                            n = (n + 1) % N_DIVISIONS;
                        }
                    }
                }
        );
    }

    public void onClickBtnThreadTwo(View view) {
        threadTwo.start();
    }

    @Override
    public void setVerticalBias(float verticalBias) {
        ballVBias = verticalBias;
    }

    @Override
    public void setVerticalOffset(float verticalOffset) {
        ballVOffset = verticalOffset;
    }

    @Override
    public void setHorizontalBias(float horizontalBias) {
        ballHBias = horizontalBias;
    }

    @Override
    public void setHorizontalOffset(float horizontalOffset) {
        ballHOffset = horizontalOffset;
    }

    @Override
    public void placeBall() {
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.ground);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.setVerticalBias(R.id.img_ball, ballVBias + ballVOffset);
        constraintSet.setHorizontalBias(R.id.img_ball, ballHBias + ballHOffset);
        constraintSet.applyTo(layout);
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        SeekBarCallback callback;

        public SeekBarListener(SeekBarCallback callback) { this.callback = callback; }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()) {
                case R.id.v_position:
                    callback.setVerticalBias( 1.0F - (float) progress / (float) seekBar.getMax());
                    callback.placeBall();
                    break;
                case R.id.h_position:
                    callback.setHorizontalBias((float) progress / (float) seekBar.getMax());
                    callback.placeBall();
                    break;
            }
        }

        @Override public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    }

}
