package com.example.function.townicloudinncom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.example.function.townicloudinncom.customView.CustomInfoWindowAdapter;
import com.example.function.townicloudinncom.mapUtils.ClusterClickListener;
import com.example.function.townicloudinncom.mapUtils.ClusterItem;
import com.example.function.townicloudinncom.mapUtils.ClusterOverlay;
import com.example.function.townicloudinncom.mapUtils.ClusterRender;
import com.example.function.townicloudinncom.mapUtils.RegionItem;
import com.example.function.townicloudinncom.mode.TownLocation;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ClusterRender,
        AMap.OnMapLoadedListener, ClusterClickListener {
    private AMapLocationClient mLocationClient = null; //定位发起端
    private AMapLocationClientOption mLocationOption = null; //定位参数
    private LocationSource.OnLocationChangedListener mListener = null; //定位监听器
    private boolean isFirstLoc = true;  //标识，用于判断是否只显示一次定位信息和用户重新定位
    private LatLng centerTXpoint = new LatLng(19.032262,109.835815);// 测试经纬度
    private LatLng housePoint;
    private UiSettings mUiSettings;//定义一个UiSettings对象
    private MapView textureMapView;
    private AMap aMap;
    private List<TownLocation.DataBean> pointList = new ArrayList<>();
    private ClusterOverlay mClusterOverlay;
    private int clusterRadius = 100;
    List<ClusterItem> items = new ArrayList<ClusterItem>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureMapView = (MapView) findViewById(R.id.map);
        textureMapView.onCreate(savedInstanceState);
        aMap = textureMapView.getMap();
        initLocation();
        initData();
        showMark();


    }

    private void initData() {
        OkHttpUtils.get().url("http://api.town.icloudinn.com/v1/town")
                .build()
                .execute(new StringCallback() {
            @Override
            public void onError(okhttp3.Call call, Exception e, int id) {
                Toast.makeText(MainActivity.this,"网络错误" + e.toString(),Toast.LENGTH_LONG).show();
                Log.d("town",e.toString());

            }

            @Override
            public void onResponse(String response, int id) {
                TownLocation townLocation  =  JSON.parseObject(response,TownLocation.class);
                if (townLocation.getCode()==100){
                    pointList = townLocation.getData();
                    showMark();
                }else {
                    Toast.makeText(MainActivity.this,"解析错误",Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void showMark() {
        for (int i = 0; i < pointList.size()-1 ; i++) {
            housePoint=new LatLng(pointList.get(i).getLat(),pointList.get(i).getLng());
            RegionItem regionItem = new RegionItem(housePoint,"",pointList.get(i).getThumb());
            items.add(regionItem);
        }
        mClusterOverlay = new ClusterOverlay(aMap,items , dp2px(this, clusterRadius), getApplicationContext(),pointList);
        mClusterOverlay.setClusterRenderer(this);
        mClusterOverlay.setOnClusterClickListener(this);
    }

    public int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //初始化Marker
    private Bitmap chageBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 设置想要的大小
        int newWidth = 100;
        int newHeight = 100;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    //初始化地图
    private void initLocation() {
        if (aMap == null) {
            aMap = textureMapView.getMap();
        }
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        //设置定位监听
        //aMap.setLocationSource(this);
        // 是否显示定位按钮
        mUiSettings.setMyLocationButtonEnabled(false);
        // 是否可触发定位并显示定位层
        aMap.setMyLocationEnabled(false);
        //是否缩放
        mUiSettings.setZoomControlsEnabled(true);
        //默认地图移动到Marker位置
        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(centerTXpoint, 10f, 0, 0)));
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        textureMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        textureMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        textureMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
//        setCameraPosition(aMap.getCameraPosition());//保存地图状态
        super.onDestroy();
        textureMapView.onDestroy();
    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public Drawable getDrawAble(int clusterNum) {
        return null;
    }

    @Override
    public void onClick(Marker marker, List<ClusterItem> clusterItems) {
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Toast.makeText(getApplicationContext(), "点击Maker", Toast.LENGTH_LONG).show();
                return false;
            }
        });

    }
}
