package jp.gr.java_conf.tomoyorn.locationalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 目的地周辺に着いたことを受信するブロードキャストレシーバです。
 * @author tomoyorn
 */
public class ProximityAlertReceiver extends BroadcastReceiver {

    private static final String TAG = "ProximityAlertReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Start ProximityAlertService");
        Intent service = new Intent(context, ProximityAlertService.class);
        context.startService(service);
    }
}
