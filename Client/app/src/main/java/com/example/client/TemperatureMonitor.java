package com.example.client;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 장치 온도를 모니터링 하는 Activity Class.
 * (MLkit 안정성 문제)
 */
public final class TemperatureMonitor implements SensorEventListener {

    private static final String TAG = "TemperatureMonitor";

    public Map<String, Float> sensorReadingsCelsius = new HashMap<>();

    private final SensorManager sensorManager;

    public TemperatureMonitor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : allSensors) {
            // 이름에 "온도" 하위 문자열이 있는 센서가 온도 센서라고 가정
            // 센서들은 디바이스의 상이한 부분들의 온도를 측정
            // 특정 시간에 절대값에 의존하기보다는 검출기를 실행하기 전후의 판독값을 일정 시간 비교하는 등 자신의 변화를 추적하는 것이 더 타당
            if (sensor.getName().toLowerCase().contains("temperature")) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

            }
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public void logTemperature() {
        for (Map.Entry<String, Float> entry : sensorReadingsCelsius.entrySet()) {
            float tempC = entry.getValue();
            // 잘못된 센서 판독값을 건너뛸 수 있음
            if (tempC < 0) {
                continue;
            }
            float tempF = tempC * 1.8f + 32f;
            Log.i(TAG, String.format(Locale.US, "%s:\t%.1fC\t%.1fF", entry.getKey(), tempC, tempF));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        sensorReadingsCelsius.put(sensorEvent.sensor.getName(), sensorEvent.values[0]);
    }
}
