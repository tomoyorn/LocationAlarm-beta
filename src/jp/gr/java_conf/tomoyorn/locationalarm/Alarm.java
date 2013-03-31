package jp.gr.java_conf.tomoyorn.locationalarm;

import com.google.android.maps.GeoPoint;

/**
 * ひとつのアラーム設定を表すインタフェースです。
 * @author tomoyorn
 */
public interface Alarm {

    /**
     * ID を返します。ID は、数字の 1 からの連番です。
     * @return ID
     */
    int getId();

    /**
     * ラベルを設定します。
     * @param lavel ラベル
     */
    void setLavel(String lavel);
    /**
     * ラベルを返します。
     * @return ラベル
     */
    String getLavel();

    /**
     * 目的地を設定します。
     * @param destination 目的地
     */
    void setDestination(GeoPoint destination);
    /**
     * 目的地を返します。
     * @return 目的地
     */
    GeoPoint getDestination();

    /**
     * 通知時間を返します。
     * @param 通知時間（分）
     */
    int getNotificationTime();

    /**
     * 通知距離を返します。
     * @param 通知距離（km）
     */
    int getNotificationDistance();

    /**
     * アラーム設定を永続化します。
     */
    void store();

    /**
     *  アラーム設定の永続化データを削除します。
     */
    void delete();
}
