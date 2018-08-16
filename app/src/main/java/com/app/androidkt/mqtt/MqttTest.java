package com.app.androidkt.mqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by xhy on 2018/6/26 0026.
 *
 * @author xhy
 * @data 2018/6/26 0026
 */

public class MqttTest {
    private static final String TAG = "MqttTest";
    private static final String MQTT_BROKER = "后台的地址"; // Broker URL or IP Address
    private static final String MQTT_PORT = "端口号"; // Broker Port
    private static final String MQTT_URL_FORMAT = "tcp://%s:%s"; // URL Format normally don't change
    private String myTopic = "test/topic";
    private ScheduledExecutorService scheduler;
    private MqttAsyncClient mqttClient;
    private String userName = "admin"; // 连接的用户名
    private String passWord = "123456"; //连接的密码
    private String mDeviceId = "";       // Device ID, Secure.ANDROID_ID
    private ConnectivityManager mConnectivityManager; // To check for connectivity changes
    private MqttConnectOptions options;
    private Context context;

    public MqttTest(Context context) {
        this.context = context;
    }

    /**
     * 初始化相关数据
     */
    public void init() {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            String url = String.format(Locale.US, MQTT_URL_FORMAT, MQTT_BROKER, MQTT_PORT);
            String clientId = String.valueOf(System.currentTimeMillis());
            mqttClient = new MqttAsyncClient(url, clientId, new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            //设置回调

            options.setAutomaticReconnect(true);//设置自动重连
//            mqttAsyncClient.connect(connOpts).waitForCompletion();
            mqttClient.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    if (isNetworkAvailable()) {
                        reconnectIfNecessary();
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    Log.e(TAG, "message=:" + message.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    long messageId = token.getMessageId();
                    Log.e(TAG, "messageId=:" + messageId);

                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        mConnectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        context.registerReceiver(mConnectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    /**
     * Query's the NetworkInfo via ConnectivityManager
     * to return the current connected state
     * 通过ConnectivityManager查询网络连接状态
     *
     * @return boolean true if we are connected false otherwise
     * 如果网络状态正常则返回true反之flase
     */
    private boolean isNetworkAvailable() {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();

        return (info == null) ? false : info.isConnected();
    }

    /**
     * Checkes the current connectivity
     * and reconnects if it is required.
     * 重新连接如果他是必须的
     */
    public synchronized void reconnectIfNecessary() {
        if (mqttClient == null || !mqttClient.isConnected()) {
            connect();
        }
    }

    private void connect() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    mqttClient.connect(options);
                    // 连接成功之后，处理相关逻辑
                } catch (Exception e) {
                    e.printStackTrace();
                    // 连接出错，可以设置重新连接
                }
            }
        }).start();
    }

    /**
     *  调用init() 方法之后，调用此方法。
     */
    public void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (!mqttClient.isConnected() && isNetworkAvailable()) {
                    connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Receiver that listens for connectivity chanes
     * via ConnectivityManager
     * 网络状态发生变化接收器
     */
    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("BroadcastReceiver", "Connectivity Changed...");
            if (!isNetworkAvailable()) {
                Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show();
                scheduler.shutdownNow();
            } else {
                startReconnect();
            }
        }
    };
}
