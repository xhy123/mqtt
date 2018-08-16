package com.app.androidkt.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by xhy on 2018/6/26 0026.
 *
 * @author xhy
 * @data 2018/6/26 0026
 */

public class MqttUtil {
    private static MqttUtil instance;
    private final PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private static final String TAG= "MqttUtil";

    public static MqttUtil getInstance() {
        if (instance == null) {
            instance = new MqttUtil();
        }
        return instance;
    }
    private MqttUtil(){
        pahoMqttClient = new PahoMqttClient();
    }
    public void init(Context context){


        mqttAndroidClient = pahoMqttClient.getMqttClient(context, Constants.MQTT_BROKER_URL, Constants.CLIENT_ID,Constants.USERNAME,Constants.PASSWORD);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                Log.d(TAG, "messageArrived topic=  "+s+"     new String(mqttMessage.getPayload())  "+new String(mqttMessage.getPayload()));
               // setMessageNotification(s, new String(mqttMessage.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
