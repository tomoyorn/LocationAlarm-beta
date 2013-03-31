package jp.gr.java_conf.tomoyorn.locationalarm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * アラーム一覧画面のアクティビティです。
 * @author tomoyorn
 */
public class AlarmListActivity extends Activity {

    private static final String TAG = "AlarmListActivity";

    public static final int REQUEST_CODE = 1;

    /** アラーム一覧ビュー */
    private ListView alarmListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_list);

        initAlarmListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.alarm_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (selectedAlarm() == null) {
            menu.findItem(R.id.menu_start_alarm).setEnabled(false);
            menu.findItem(R.id.menu_edit_alarm).setEnabled(false);
            menu.findItem(R.id.menu_delete_alarm).setEnabled(false);
        } else {
            menu.findItem(R.id.menu_start_alarm).setEnabled(true);
            menu.findItem(R.id.menu_edit_alarm).setEnabled(true);
            menu.findItem(R.id.menu_delete_alarm).setEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Selected menu: item.getTitle()=" + item.getTitle());
        Alarm alarm = selectedAlarm();
        switch (item.getItemId()) {
        case R.id.menu_start_alarm:
            Validate.notNull(alarm, "alarm must not be null!");
            startAlarm(alarm);
            notiryStartAlarm(alarm);
            Toast.makeText(this,
                    "アラーム「" + alarm.getLavel() + "」を開始しました。",
                    Toast.LENGTH_LONG).show();
            finish();
            return true;
        case R.id.menu_add_alarm:
            startDestinationSelectionActivity();
            return true;
        case R.id.menu_edit_alarm:
            Validate.notNull(alarm, "Alarm must not be null!");
            startAlarmDetailActivity(alarm);
            return true;
        case R.id.menu_delete_alarm:
            Validate.notNull(alarm, "Alarm must not be null!");
            deleteAlarm(alarm);
            return true;
        case R.id.menu_preferences:
            startSettingsActivity();
            return true;
        default:
            Log.i(TAG, "Selected menu: unknown");
            return super.onOptionsItemSelected(item);
        }
    }

    private void startAlarm(Alarm alarm) {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        double latitude = (double) alarm.getDestination().getLatitudeE6() / 1E6;
        double longitude = (double) alarm.getDestination().getLongitudeE6() / 1E6;
        float radius = alarm.getNotificationDistance() * 1000;
        PendingIntent intent = PendingIntent.getBroadcast(this,
                0,
                new Intent(this, ProximityAlertReceiver.class),
                0);
        long noExpiration = -1;
        lm.addProximityAlert(latitude, longitude, radius, noExpiration , intent);
    }

    private void notiryStartAlarm(Alarm alarm) {
        long when = System.currentTimeMillis();
        Notification notification = new Notification(R.drawable.ic_launcher,
                "アラーム「" + alarm.getLavel() + "」を開始しました。", when);
        // 「通知を消去」による消去を抑制
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        Intent intent = new Intent(this, AlarmStopConfirmationActivity.class);
        // 最近起動したアクティビティのリストに表示しない
        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra("alarm.id", alarm.getId());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notification.setLatestEventInfo(this, getString(R.string.app_name),
                "アラーム「" + alarm.getLavel() + "」を停止する場合に選択します。",
                contentIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int id = alarm.getId();
        nm.notify(id , notification);
    }

    // 新しいアラームを生成する。
    // ラベルのデフォルト値は目的地の文字列表現。住所 > 緯度経度 の順で
    // 取得を試みる。
    private Alarm createAlarm(int latitudeE6, int longitudeE6) {
        Alarms alarms = new SharedPreferencesAlarms(this);
        Alarm alarm = new SharedPreferencesAlarm(this, alarms.generateId());

        GeoPoint destination = new GeoPoint(latitudeE6, longitudeE6);
        alarm.setDestination(destination);
        String locationAddress = getLocationAddress(destination);
        if (!StringUtils.isBlank(locationAddress)) {
            alarm.setLavel(locationAddress);
        } else {
            alarm.setLavel(destination.toString());
        }
        alarm.store();
        Log.i(TAG, "Create alarm: alarm=" + alarm);
        return alarm;
    }

    // 住所を取得する。取得できない場合は null
    private String getLocationAddress(GeoPoint geoPoint) {
        String result = null;
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    (double) geoPoint.getLatitudeE6() / 1E6,
                    (double) geoPoint.getLongitudeE6() / 1E6, 5);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                List<String> aLines = new ArrayList<String>();
                String aLine;
                for (int i = 0; (aLine = address.getAddressLine(i)) != null; i++){
                    aLines.add(aLine);
                }
                String separator = ",";
                result = StringUtils.join(aLines, separator);
            }
        } catch (IOException e) {
            Log.d(TAG, "Can't get address: " + e.getMessage());
        }
        return result;
    }

    // 指定された ID のアラームを削除する
    private void deleteAlarm(Alarm alarm) {
        ListView alarmListView = getAlarmListView();
        AlarmListAdapter adapter = (AlarmListAdapter) alarmListView.getAdapter();
        adapter.remove(alarm);
        alarm.delete();
    }

    // アラーム詳細画面へ遷移する
    private void startAlarmDetailActivity(Alarm alarm) {
        Intent intent = new Intent(this, AlarmDetailActivity.class);
        intent.putExtra("alarm.id", alarm.getId());
        startActivityForResult(intent, AlarmDetailActivity.REQUEST_CODE);
    }

    private void startDestinationSelectionActivity() {
        Intent intent = new Intent(this, DestinationSelectionActivity.class);
        startActivityForResult(intent, DestinationSelectionActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "Return PosAlarmBetaActivity: requestCode=" + requestCode
                + ", resultCode=" + resultCode);
        switch (requestCode) {
        case AlarmDetailActivity.REQUEST_CODE:
            reloadAlarmListView();
            break;
        case DestinationSelectionActivity.REQUEST_CODE:
            if (resultCode == RESULT_OK) {
                String latitudeE6 = data.getStringExtra("latitudeE6");
                String longitudeE6 = data.getStringExtra("longitudeE6");
                Validate.isTrue(NumberUtils.isNumber(latitudeE6), "Invalid latitudeE6: " + latitudeE6);
                Validate.isTrue(NumberUtils.isNumber(longitudeE6), "Invalid longitudeE6: " + longitudeE6);

                Alarm alarm = createAlarm(Integer.parseInt(latitudeE6),
                        Integer.parseInt(longitudeE6));
                startAlarmDetailActivity(alarm);
            } else if (resultCode == RESULT_CANCELED) {
            }
            break;
        default:
            throw new AssertionError("requestCode=" + requestCode);
        }
    }

    // 設定画面へ遷移する
    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // アラーム一覧ビューを取得する
    private ListView getAlarmListView() {
        if (alarmListView == null) {
            alarmListView = (ListView) findViewById(R.id.alarm_list_view);
            alarmListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
        return alarmListView;
    }

    private void initAlarmListView() {
        ListView alarmListView = getAlarmListView();
        Alarms alarms = new SharedPreferencesAlarms(this);
        AlarmListAdapter adapter = new AlarmListAdapter(this,
                R.layout.alarm_list_item);
        for (Alarm alarm : alarms.getAll()) {
            adapter.add(alarm);
        }
        alarmListView.setAdapter(adapter);
    }

    private void reloadAlarmListView() {
        initAlarmListView();
    }

    private Alarm selectedAlarm() {
        ListView alarmListView = getAlarmListView();
        AlarmListAdapter adapter = (AlarmListAdapter) alarmListView.getAdapter();
        if (adapter.isEmpty()) { return null; }
        int position = alarmListView.getCheckedItemPosition();
        return (Alarm) alarmListView.getItemAtPosition(position);
    }

    // アラーム一覧ビューに項目を設定するためのアダプター
    private static class AlarmListAdapter extends ArrayAdapter<Alarm> {
        private int resourceId;

        public AlarmListAdapter(Context context, int resourceId) {
            super(context, resourceId);
            this.resourceId = resourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resourceId, null);
            }
            Alarm alarm = getItem(position);
            CheckedTextView view = (CheckedTextView) convertView
                    .findViewById(R.id.alarm_list_item);
            view.setText(alarm.getLavel());
            return convertView;
        }
    }
}
