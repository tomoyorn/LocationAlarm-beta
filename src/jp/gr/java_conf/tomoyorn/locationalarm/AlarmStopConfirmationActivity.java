package jp.gr.java_conf.tomoyorn.locationalarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.lang3.Validate;

/**
 * アラーム停止確認画面のアクティビティです。
 * @author tomoyorn
 */
public class AlarmStopConfirmationActivity extends Activity {

    private static final String TAG = "AlarmStopConfirmationActivity";

    public static final int REQUEST_CODE = 4;

    /** 操作中のアラーム */
    private Alarm itsAlarm;

    private DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            Context context = getApplicationContext();
            PendingIntent intent = PendingIntent.getBroadcast(context,
                    0,
                    new Intent(context, ProximityAlertReceiver.class),
                    0);
            lm.removeProximityAlert(intent);

            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(itsAlarm.getId());

            Toast.makeText(AlarmStopConfirmationActivity.this,
                    "アラーム「" + itsAlarm.getLavel() + "」を停止しました。",
                    Toast.LENGTH_LONG).show();
            AlarmStopConfirmationActivity.this.finish();
        }
    };

    private DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            AlarmStopConfirmationActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleIntent();
        Log.i(TAG, "Start AlarmStopConfirmationActivity: alarm=" + itsAlarm);

        new AlertDialog.Builder(this).setTitle(getString(R.string.app_name))
            .setMessage("アラーム「" + itsAlarm.getLavel() + "」を停止しますか？")
            .setPositiveButton(getString(R.string.ok), okListener)
            .setNegativeButton(getString(R.string.cancel), cancelListener)
            .show();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        int alarmId = intent.getIntExtra("alarm.id", -1);
        Validate.isTrue((1 <= alarmId), "Invalid alarmId!: " + alarmId);
        itsAlarm = new SharedPreferencesAlarm(this, alarmId);
    }
}
