package jp.gr.java_conf.tomoyorn.locationalarm;

import java.util.List;

/**
 * Alarmの集合を操作するためのインタフェースです。
 * @author tomoyorn
 */
public interface Alarms {

    /**
     * 登録されている Alarm のリストを返します。
     * @return
     */
    List<Alarm> getAll();

    /**
     * アプリケーションで一意なアラームIDを生成します。
     * @return アラームID
     */
    int generateId();

}
