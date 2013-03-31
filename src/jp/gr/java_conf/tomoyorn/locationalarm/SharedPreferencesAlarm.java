package jp.gr.java_conf.tomoyorn.locationalarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.maps.GeoPoint;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.File;

/**
 * データの永続化にSharedPreferencesを利用した、Alarmインタフェースの実装です。
 * @author tomoyorn
 */
public class SharedPreferencesAlarm implements Alarm {

    private static final String TAG = "SharedPreferencesAlarm";

    /** アラーム設定プリファレンスファイル名の接頭辞 */
    static final String PREFS_FILE_NAME_PREFIX = "alarm_";
    /** アラーム設定プリファレンスファイル名の拡張子 */
    static final String PREFS_FILE_NAME_EXTENSION = ".xml";
    /** アラーム設定プリファレンスファイル名の正規表現パターン */
    static final String PREFS_FILE_NAME_PATTERN = PREFS_FILE_NAME_PREFIX
            + "(\\d)" + PREFS_FILE_NAME_EXTENSION;

    private static final String PREFS_KEY_ID = "id";
    private static final String PREFS_KEY_DESTINATION_LATITUDE_E6 = "destination.latitudeE6";
    private static final String PREFS_KEY_DESTINATION_LONGITUDE_E6 = "destination.longitudeE6";
    private static final String PREFS_KEY_LAVEL = "lavel";
    private static final String PREFS_KEY_TIME = "notificationTime";
    private static final String PREFS_KEY_DISTANCE = "notificationDistance";
//    private static final String PREFS_KEY_ENABLED = "enabled";

    private Context context;
    private String prefsFileName;

    private int id;
    private GeoPoint destination;
    private String lavel;
    private int notificationTime; // min
    private int notificationDistance; // km

    ;

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param id ID
     */
    public SharedPreferencesAlarm(Context context, int id) {
        this.context = context;
        this.prefsFileName = PREFS_FILE_NAME_PREFIX + id + PREFS_FILE_NAME_EXTENSION;

        SharedPreferences generalSettings = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences alarmSettings = context.getSharedPreferences(
                PREFS_FILE_NAME_PREFIX + id, Context.MODE_PRIVATE);

        this.id = id;
        this.destination = new GeoPoint(
                alarmSettings.getInt(PREFS_KEY_DESTINATION_LATITUDE_E6, 0),
                alarmSettings.getInt(PREFS_KEY_DESTINATION_LONGITUDE_E6, 0));
        this.lavel = alarmSettings.getString(PREFS_KEY_LAVEL, "");
        this.notificationTime = Integer.parseInt(
                alarmSettings.getString(PREFS_KEY_TIME,
                        generalSettings.getString(PREFS_KEY_TIME, "1")));
        this.notificationDistance = Integer.parseInt(
                alarmSettings.getString(PREFS_KEY_DISTANCE,
                        generalSettings.getString(PREFS_KEY_DISTANCE, "1")));
//        this.enabled = alarmSettings.getBoolean(PREFS_KEY_ENABLED, false);
    }

    public int getId() {
        return id;
    }

    public void setLavel(String lavel) {
        this.lavel = lavel;
    }
    public String getLavel() {
        return lavel;
    }

    public void setDestination(GeoPoint destination) {
        this.destination = destination;

    }
    public GeoPoint getDestination() {
        return destination;
    }

//    public void setEnabled(boolean enabled) {
//        this.enabled = enabled;
//
//    }
//    public boolean isEnabled() {
//        return enabled;
//    }

    public int getNotificationTime() {
        return notificationTime;
    }

    public int getNotificationDistance() {
        return notificationDistance;
    }

    public void store() {
        SharedPreferences settings = context.getSharedPreferences(
                PREFS_FILE_NAME_PREFIX + id, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(PREFS_KEY_ID, id);
        editor.putInt(PREFS_KEY_DESTINATION_LATITUDE_E6, destination.getLatitudeE6());
        editor.putInt(PREFS_KEY_DESTINATION_LONGITUDE_E6, destination.getLongitudeE6());
        editor.putString(PREFS_KEY_LAVEL, lavel);
//        editor.putBoolean(PREFS_KEY_ENABLED, enabled);
        editor.commit();
    }

    public void delete() {
        String dir = SharedPreferencesAlarms.directoryPath(context);
        File file = new File(dir + File.separator + prefsFileName);
        if (file.exists()) {
            FileUtils.deleteQuietly(file);
        } else {
            Log.w(TAG, "Not exists preferences file: " + file.getAbsolutePath());
        }
    }

    /**
     * アラーム設定プリファレンスファイル名からIDを取得します。
     * @param fileName アラーム設定プリファレンスファイル名
     * @return ID
     */
    static int getIdFrom(String fileName) {
        Validate.isTrue(fileName.matches(PREFS_FILE_NAME_PATTERN),
                "Invalid argument: fileName=" + fileName);
        try {
            return Integer.parseInt(fileName.replaceFirst(
                    PREFS_FILE_NAME_PATTERN, "$1"));
        } catch (NumberFormatException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
