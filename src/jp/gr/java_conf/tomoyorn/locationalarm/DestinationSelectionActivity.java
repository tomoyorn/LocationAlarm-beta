package jp.gr.java_conf.tomoyorn.locationalarm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 目的地選択画面のアクティビティです。
 * @author tomoyorn
 */
public class DestinationSelectionActivity extends MapActivity {

    private static final String TAG = "DestinationSelectionActivity";

    public static final int REQUEST_CODE = 3;

    private static final int DEFAULT_ZOOM_LEVEL = 16;
    private static final GeoPoint DEFAULT_GEO_POINT = new GeoPoint(35710139, 139810833);

    private int latitudeE6;
    private int longitudeE6;

    private LocationManager locationManager;
    DestinationSelectionLocationListener locationListener =
        new DestinationSelectionLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.destination_selection);

        initLocationManager();
        handleIntent();
        Log.i(TAG, "Start DestinationSelectionActivity: latitudeE6="
                + latitudeE6 + ", longitudeE6=" + longitudeE6);

        initMapView();
        initOkButton();
        initCancelButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.destination_selection, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // none
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Selected menu: item.getTitle()=" + item.getTitle());
        switch (item.getItemId()) {
        case R.id.menu_current_location:
            GeoPoint currentGeoPoint = currentGeoPoint();
            if (currentGeoPoint != null) {
                MapView mapView = (MapView) this.findViewById(R.id.map);
                moveToCenter(mapView, currentGeoPoint, true);
            }
            return true;
        case R.id.menu_search_location:
            // XXX 検索機能を将来サポートする予定。
            //     「検索」メニューを押すとテキスト入力ダイアログが現れて、
            //     マップを検索できる。
            Toast.makeText(this,
                    "検索機能は、現在サポートしていません。将来サポートする予定です。",
                    Toast.LENGTH_LONG).show();
        default:
            Log.i(TAG, "Selected menu: unknown");
            return super.onOptionsItemSelected(item);
        }
    }

    // 指定された geoPoint を mapView の中心に設定する
    private void moveToCenter(MapView mapView, GeoPoint currentGeoPoint,
            boolean animate) {
        MapController mapController = mapView.getController();
        if (animate) {
            mapController.animateTo(currentGeoPoint);
        } else {
            mapController.setCenter(currentGeoPoint);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(
                getBestProvider(), (60 * 1000L), 1, locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // アプリケーション一時停止中のバッテリ節約のために更新を停止する
        locationManager.removeUpdates(locationListener);
    }

    private void handleIntent() {
        Intent intent = getIntent();

        String latitudeE6 = intent.getStringExtra("latitudeE6");
        String longitudeE6 = intent.getStringExtra("longitudeE6");

        Validate.isTrue((latitudeE6 == null || NumberUtils.isNumber(latitudeE6)),
                "Invalid latitudeE6: " + latitudeE6);
        Validate.isTrue((longitudeE6 == null || NumberUtils.isNumber(longitudeE6)),
                "Invalid longitudeE6: " + longitudeE6);

        if (latitudeE6 == null || longitudeE6 == null) {
            GeoPoint geoPoint = currentGeoPoint(DEFAULT_GEO_POINT);
            this.latitudeE6 = geoPoint.getLatitudeE6();
            this.longitudeE6 = geoPoint.getLongitudeE6();
        } else {
            this.latitudeE6 = Integer.valueOf(latitudeE6);
            this.longitudeE6 = Integer.valueOf(longitudeE6);
        }
    }

    private void initLocationManager() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    private void initMapView() {
        MapView mapView = (MapView) this.findViewById(R.id.map);

        // ズーム機能を有効にし、デフォルトの縮尺を設定
        mapView.setBuiltInZoomControls(true);
        MapController mapController = mapView.getController();
        mapController.setZoom(DEFAULT_ZOOM_LEVEL);

        moveToCenter(mapView, new GeoPoint(latitudeE6, longitudeE6), true);
        // 地図の中央に目的地マークを表示する
        mapView.getOverlays().add(
                new DestinationMarkOverlay(BitmapFactory.decodeResource(
                        getResources(), android.R.drawable.ic_menu_mylocation)));
    }

    /**
     * 可能なかぎり最新の現在地を返します。
     * @return 現在地。取得出来なかった場合は null
     */
    private GeoPoint currentGeoPoint() {
        return currentGeoPoint(null);
    }

    /**
     * 可能なかぎり最新の現在地を返します。
     * @param defaultValue 取得出来なかった場合のデフォルト値
     * @return 現在地。取得できなかった場合は defaultValue
     */
    private GeoPoint currentGeoPoint(GeoPoint defaultValue) {
        Location location = locationListener.location;
        if (location == null) {
            location = locationManager.getLastKnownLocation(getBestProvider());
        }

        if (!enabledLocation(location)) {
            Toast.makeText(this, "現在値を取得できませんでした。", Toast.LENGTH_LONG).show();
            return defaultValue;
        } else {
            return new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
        }
    }

    private String getBestProvider() {
        Criteria criteria = new Criteria();
        // 精度よりもレスポンス速度を優先
        criteria.setSpeedRequired(false);                 // 速度情報; 不要
        criteria.setAltitudeRequired(false);              // 高度情報; 不要
        criteria.setBearingRequired(false);               // 方位情報; 不要
        criteria.setCostAllowed(false);                   // 費用発生; 不可
        return locationManager.getBestProvider(criteria, true);
    }

    private boolean enabledLocation(Location location) {
        return location != null;
        // XXX 何時間前に更新された位置情報かどうかも見たほうがいいかもしれない。
        //     例えば、1週間前の位置情報を現在地に設定してもあまり意味が無い。
//        return location != null
//                && (new Date().getTime() - location.getTime()) <= (5 * 60 * 1000L);
    }

    private void initOkButton() {
        Button button = (Button) findViewById(R.id.ok);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MapView mapView = (MapView) findViewById(R.id.map);
                GeoPoint center = mapView.getMapCenter();
                Intent data = new Intent();
                data.putExtra("latitudeE6", String.valueOf(center.getLatitudeE6()));
                data.putExtra("longitudeE6", String.valueOf(center.getLongitudeE6()));
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    private void initCancelButton() {
        Button button = (Button) findViewById(R.id.cancel);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    /**
     * マップの中心に目的地マークを表示するオーバーレイ
     */
    private static class DestinationMarkOverlay extends Overlay {
        private Bitmap destinationMark;

        public DestinationMarkOverlay(Bitmap destinationMark) {
            this.destinationMark = destinationMark;
        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (!shadow) {
                canvas.drawBitmap(destinationMark,
                        mapView.getWidth() / 2 - destinationMark.getWidth() / 2,
                        mapView.getHeight() / 2 - destinationMark.getHeight() / 2,
                        null);
            }
        }
    }

    /**
     * 常に最新の Location を保持するリスナー
     */
    private class DestinationSelectionLocationListener implements LocationListener {
        private Location location;
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
        public void onLocationChanged(Location location) {
            this.location = location;
        }
    }
}
