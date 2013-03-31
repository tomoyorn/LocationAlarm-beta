package jp.gr.java_conf.tomoyorn.locationalarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.widget.Toast;

import org.apache.commons.lang3.Validate;

/**
 * 目的地周辺に着いたことを通知するサービスです。
 * @author tomoyorn
 */
public class ProximityAlertService extends Service {

    private static final String TAG = "ProximityAlertService";

    /** 操作中のアラーム */
    private Alarm itsAlarm;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this,
                "目的地周辺です。",
                Toast.LENGTH_LONG).show();

        notiryStopAlarm();
        stopAlarm();
        vibrate();
        startDestinationArrivalActivity();
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void handleIntent(Intent intent) {
        int alarmId = intent.getIntExtra("alarm.id", -1);
        Validate.isTrue((1 <= alarmId), "Invalid alarmId!: " + alarmId);
        itsAlarm = new SharedPreferencesAlarm(this, alarmId);
    }

    private void notiryStopAlarm() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    private void stopAlarm() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Context context = getApplicationContext();
        PendingIntent intent = PendingIntent.getBroadcast(context,
                0,
                new Intent(context, ProximityAlertReceiver.class),
                0);
        lm.removeProximityAlert(intent);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long milliseconds = 1 * 1000;
        vibrator.vibrate(milliseconds);
    }

    private void startDestinationArrivalActivity() {
    }
}
