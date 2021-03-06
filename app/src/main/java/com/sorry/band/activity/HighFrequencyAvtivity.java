package com.sorry.band.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sorry.band.BandApplication;
import com.sorry.band.R;
import com.sorry.core.MiBandHanlder;
import com.sorry.model.MibandMessage;
import com.sorry.model.ViewMessage;
import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.listeners.HeartRateNotifyListener;
import com.zhaoxiaodan.miband.listeners.RealtimeStepsNotifyListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by sorry on 2016/8/16.
 */
public class HighFrequencyAvtivity extends BaseActivity {

    private boolean isStart = false;

    private final int scanInterval = 60000;
    private final int countDownInterval = 1000;
    private final int warningHeartRate = 30;

    private TextView heartRateTextView;
    private TextView countdownTextView;
    private TextView minTextView;
    private ImageButton exitButton;
    private FloatingActionButton startButton;

    private Typeface tf;

    private HeartRateScanTimer heartRateScanTimer;

    private LineChartView lineChart;
    private List<AxisValue> axisValues;
    private List<PointValue> values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.high_frequency_activity);
        initView();
        initChart();
    }

    private void initView(){
        heartRateTextView = (TextView) findViewById(R.id.heartRateNumTextView);
        countdownTextView = (TextView) findViewById(R.id.countdownTextView);
        minTextView = (TextView) findViewById(R.id.minTextView);
        exitButton = (ImageButton) findViewById(R.id.exitButton);
        startButton = (FloatingActionButton) findViewById(R.id.startButton);
        lineChart = (LineChartView) findViewById(R.id.heartRateChart);
        tf = Typeface.createFromAsset(getResources().getAssets(), "fonts/BEBAS.ttf");
        heartRateTextView.setTypeface(tf);
        minTextView.setTypeface(tf);

        heartRateScanTimer = new HeartRateScanTimer(scanInterval, countDownInterval);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartRateScanTimer.cancel();
                HighFrequencyAvtivity.this.finish();
            }
        });

        startButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(!isStart) {
                        HeartRateNotifyListener heartRateNotifyListener = new HeartRateNotifyListener() {
                            @Override
                            public void onNotify(int heartRate) {
                                Log.i("HeartRate", "heart rate: " + heartRate);
                                Calendar c = Calendar.getInstance();
                                String hour = c.get(Calendar.HOUR_OF_DAY)+"";
                                String minute = c.get(Calendar.MINUTE)+"";
                                uiAction.setText(new ViewMessage<TextView, String>(heartRateTextView, heartRate+""));
                                appAction.insertIntoPerdayHeartrateData(heartRate+"");
                                heartRateScanTimer.start();
                                if(heartRate < warningHeartRate){
                                    emergencyProgram();
                                }
                                addChartValue(heartRate);
                            }
                        };
                        MibandMessage<Void, HeartRateNotifyListener, MiBand> heartMsg = new MibandMessage<Void, HeartRateNotifyListener, MiBand>(null, heartRateNotifyListener, miBand);
                        mibandAction.setBand(MiBandHanlder.SET_HEART_LISTENER, heartMsg);
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if(!isStart) {
                        miBand.startHeartRateScan();
                        uiAction.changeFABSrc(new ViewMessage<FloatingActionButton, Integer>(startButton, R.mipmap.ic_pause));
                        isStart = true;
                    }
                    else{
                        isStart = false;
                        heartRateScanTimer.cancel();
                        countdownTextView.setText("已暂停");
                        uiAction.changeFABSrc(new ViewMessage<FloatingActionButton, Integer>(startButton, R.mipmap.ic_start));

                    }
                }
                return true;

            }
        });
        axisValues = new ArrayList<AxisValue>();
        values = new ArrayList<PointValue>();
    }

    private void initChart(){
        lineChart.setInteractive(true);
        Line line = new Line(values).setColor(Color.parseColor("#445a65"));
        line.setStrokeWidth(3);
        line.setPointRadius(4);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);
        LineChartData data = new LineChartData();

        data.setLines(lines);

        Axis axisX = new Axis(axisValues);
        axisX.setHasSeparationLine(false);
        data.setAxisXBottom(axisX);
        axisX.setTextColor(Color.parseColor("#445a65"));
        axisX.setTypeface(tf);

        Axis axisY = new Axis();
        axisY.setMaxLabelChars(3);
        axisY.setTextColor(Color.parseColor("#445a65"));
        axisY.setTypeface(tf);
        data.setAxisYLeft(axisY);
        lineChart.setViewportCalculationEnabled(false);

        // And set initial max viewport and current viewport- remember to set viewports after data.
        Viewport v = new Viewport(0, 220, 10.5f, 0);
        lineChart.setMaximumViewport(v);
        lineChart.setCurrentViewport(v);

        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setValueSelectionEnabled(true);
        lineChart.setLineChartData(data);

    }



    class HeartRateScanTimer extends CountDownTimer {
        public HeartRateScanTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onFinish() {
            if(isStart) {
                countdownTextView.setText("正在检测...");
                miBand.startHeartRateScan();
            }
        }
        @Override
        public void onTick(long millisUntilFinished) {
            int min = (int)(millisUntilFinished / 60000);
            int sec = (int)(millisUntilFinished % 60000)/1000;
            String text = "距离下次检测还有" + min + "m" + sec + "s";
            countdownTextView.setText(text);
        }
    }



    private void emergencyProgram(){
        Intent phoneIntent = new Intent("android.intent.action.CALL",
                Uri.parse("tel:" + application.getPersonalData().getEmergencyNumber()));
        //启动
        startActivity(phoneIntent);
    }

    private void addChartValue(int heartRate){
        values.add(new PointValue(values.size(), Integer.valueOf(heartRate)));
        Calendar c = Calendar.getInstance();
        axisValues.add(new AxisValue(values.size()-1).setLabel(String.format("%02d",c.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d",c.get(Calendar.MINUTE))));
        initChart();
    }
}
