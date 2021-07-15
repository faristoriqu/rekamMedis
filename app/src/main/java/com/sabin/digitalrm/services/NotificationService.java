package com.sabin.digitalrm.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.sabin.digitalrm.LoginActivity;
import com.sabin.digitalrm.R;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationService extends Service {
    private static final String SYSTEM_MESSAGE = "sysmsg";
    private static final String USER_MESSAGE = "usrmsg";
    private static final String wsURI = "ws://128.199.235.99:9191";
    public static final String BROADCAST_RESULT = "com.sabin.NotificationService.RESULT";
    private WebSocketClient mWebSocketClient;
    private static int openChance = 0;
    SharedPreferences pref;
    SharedPreferences.Editor edit;

    public NotificationService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        int uid = Integer.valueOf(pref.getString("uid", "-1"));
        int pid = Integer.valueOf(pref.getString("id_poli", "-1"));
        String poliname = pref.getString("nama_poli", null);

        if(uid!=-1 || pid!=-1 || poliname!=null) {
            connectWebSocket(uid, pid, poliname);
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void connectWebSocket(int uid, int pid, String pname) {
        URI uri;
        try {
            uri = new URI(wsURI);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri, new Draft_6455()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
//                String defaultWelcomeMsg = "UserID " + uid + " in UnitForBRMDetail " + pid + " has been connected";
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                String time = sdf.format(new Date());
                openChance = 0;
                Log.e("[X-SOCKET-DEBUG]", "Opened");
                String jsonify = "" +
                        "{" +
                        "\"message_type\": \"" + SYSTEM_MESSAGE + "\"," +
                        "\"message_text\": \"null\"," +
                        "\"user_sender_id\": \"" + uid + "\"," +
                        "\"user_receiver_id\": \"null\"," +
                        "\"poly_sender_id\": \"" + pid + "\"," +
                        "\"poly_receiver_id\": \"null\"," +
                        "\"poly_sender_name\": \"" + pname + "\"," +
                        "\"timestamp\": \"" + time + "\"" +
                        "}";

                mWebSocketClient.send(jsonify);
            }

            @Override
            public void onMessage(String s) {
                openChance = 0;
                Log.e("[X-SOCKET-DEBUG]", "Mesage Received: "+s);

                int counter = pref.getInt("notifcount", 0);
                counter += 1;

                edit = pref.edit();
                edit.putInt("notifcount", counter);
                edit.apply();

                showNotification(s, counter);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                openChance += 1;
                Log.e("[X-SOCKET-DEBUG]", "Socket closed. Trying to open again ...");

                if (openChance < 10){
                    connectWebSocket(uid, pid, pname);
                }else{
                    openChance = 0;
                    Log.e("[X-SOCKET-DEBUG]", "Couldn't open socket. " + s);
                }

            }

            @Override
            public void onError(Exception e) {
                openChance = 0;
                Log.e("[X-SOCKET-DEBUG]", "Connection error. " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    private void showNotification(String msg, int counter) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "DMRNOTIF")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(counter + " Notifikasi Baru Telah diterima")
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        mBuilder.setSound(alarmSound);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1001, mBuilder.build());

        Intent ii = new Intent(BROADCAST_RESULT);
        ii.putExtra("notifcount", counter);
        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(ii);
    }

}
