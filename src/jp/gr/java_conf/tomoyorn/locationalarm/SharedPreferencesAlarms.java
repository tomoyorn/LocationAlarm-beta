package jp.gr.java_conf.tomoyorn.locationalarm;

import android.content.Context;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * データの永続化に SharedPreferences を利用した、Alarmsインタフェースの実装です。
 * @author tomoyorn
 */
public class SharedPreferencesAlarms implements Alarms {

    private static final String TAG = "SharedPreferencesAlarms";

    private static final IOFileFilter FILE_FILTER = new RegexFileFilter(
            SharedPreferencesAlarm.PREFS_FILE_NAME_PATTERN);

    private Context context;

    private final File sharedPrefsDir;

    /**
     * コンストラクタ
     * @param context コンテキスト
     */
    public SharedPreferencesAlarms(Context context) {
        this.context = context;
        this.sharedPrefsDir = new File(directoryPath(context));
    }

    public List<Alarm> getAll() {
        List<Alarm> alarms = new ArrayList<Alarm>();

        if (!sharedPrefsDir.exists()) {
            Log.d(TAG, "Not exists 'shared_prefs' directory");
            return alarms;
        }

        for (Iterator<File> files = FileUtils.iterateFiles(sharedPrefsDir,
                FILE_FILTER, null); files.hasNext();) {
            File file = files.next();
            Log.d(TAG, "file=" + file);
            int id = SharedPreferencesAlarm.getIdFrom(file.getName());
            alarms.add(new SharedPreferencesAlarm(context, id));
        }
        return alarms;
    }

    public int generateId() {
        List<Alarm> alarms = getAll();
        List<Integer> usedId = new ArrayList<Integer>();
        for (Alarm alarm : alarms) {
            usedId.add(alarm.getId());
        }

        int id = 1;
        while (true) {
            if (!usedId.contains(id)) {
                break;
            }
            id++;
        }
        return id;
    }

    /**
     * アラーム設定プリファレンスファイルの親のディレクトリのパスを返します。
     * @param context コンテキスト
     * @return アラーム設定プリファレンスファイルの親のディレクトリのパス
     */
    static String directoryPath(Context context) {
        return "/data/data/" + context.getPackageName() + "/shared_prefs";
    }
}
