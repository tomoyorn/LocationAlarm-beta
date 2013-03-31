package jp.gr.java_conf.tomoyorn.locationalarm;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.google.android.maps.GeoPoint;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * アラーム詳細設定画面のアクティビティです。
 * @author tomoyorn
 */
public class AlarmDetailActivity extends PreferenceActivity {

    private static final String TAG = "AlarmDetailActivity";

    public static final int REQUEST_CODE = 2;

    /** 操作中のアラーム */
    private Alarm itsAlarm;

    /**
     * 「目的地」がクリックされたときに呼び出されるリスナー。
     * 目的地選択画面へ遷移します。
     */
    OnPreferenceClickListener onDestinationPreferenceClickListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent(AlarmDetailActivity.this, DestinationSelectionActivity.class);
            GeoPoint destination = itsAlarm.getDestination();
            intent.putExtra("latitudeE6", String.valueOf(destination.getLatitudeE6()));
            intent.putExtra("longitudeE6", String.valueOf(destination.getLongitudeE6()));
            startActivityForResult(intent, DestinationSelectionActivity.REQUEST_CODE);
            return true;
        }
    };

    /**
     * 「目的地」が変更されたときに呼び出されるリスナー。
     * アラーム設定に永続化し、Summary に表示します。
     */
    private final OnPreferenceChangeListener onDestinationPreferenceChangeListener =
        new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue != null) {
                    GeoPoint p = (GeoPoint) newValue;
                    itsAlarm.setDestination(p);
                    itsAlarm.store();
                    preference.setSummary(p.toString());
                    return true;
                }
                return false;
            }
        };

    /**
     * 「ラベル」が変更されたときに呼び出されるリスナー。
     * アラーム設定に永続化し、Summary に表示します。
     */
    private final OnPreferenceChangeListener onLavalPreferenceChangeListener =
        new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
               if (newValue != null) {
                    String s = (String) newValue;
                    itsAlarm.setLavel(s);
                    itsAlarm.store();
                    preference.setSummary(s);
                    return true;
                }
                return false;
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleIntent();
        Log.i(TAG, "Start AlarmDetailActivity: alarm=" + itsAlarm);

        addPreferencesFromResource(R.xml.alarm_detail);
        initDestinationPreference();
        initLavelPreference();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        int alarmId = intent.getIntExtra("alarm.id", -1);
        Validate.isTrue((1 <= alarmId), "Invalid alarmId!: " + alarmId);
        itsAlarm = new SharedPreferencesAlarm(this, alarmId);
    }

    /**
     * 「目的地」項目を初期化します。
     */
    private void initDestinationPreference() {
        Preference preference = findPreference("alarm_destination");
        preference.setSummary(itsAlarm.getDestination().toString());
        preference.setOnPreferenceChangeListener(onDestinationPreferenceChangeListener);
        preference.setOnPreferenceClickListener(onDestinationPreferenceClickListener);
    }

    /**
     * 「ラベル」項目を初期化します。
     */
    private void initLavelPreference() {
        Preference preference = findPreference("alarm_label");
        ((EditTextPreference) preference).setText(itsAlarm.getLavel());
        preference.setSummary(itsAlarm.getLavel());
        preference.setOnPreferenceChangeListener(onLavalPreferenceChangeListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "Return AlarmDetailActivity: requestCode=" + requestCode
                + ", resultCode=" + resultCode);
        switch (requestCode) {
        case DestinationSelectionActivity.REQUEST_CODE:
            if (resultCode == RESULT_OK) {
                String latitudeE6 = data.getStringExtra("latitudeE6");
                String longitudeE6 = data.getStringExtra("longitudeE6");
                Validate.isTrue(NumberUtils.isNumber(latitudeE6), "Invalid latitudeE6: " + latitudeE6);
                Validate.isTrue(NumberUtils.isNumber(longitudeE6), "Invalid longitudeE6: " + longitudeE6);

                Preference preference = findPreference("alarm_destination");
                preference.getOnPreferenceChangeListener().onPreferenceChange(
                        preference,
                        new GeoPoint(
                                Integer.valueOf(latitudeE6),
                                Integer.valueOf(longitudeE6)));
            } else if (resultCode == RESULT_CANCELED) {
                // none
            }
            break;
        default:
            throw new AssertionError("requestCode=" + requestCode);
        }
    }
}

