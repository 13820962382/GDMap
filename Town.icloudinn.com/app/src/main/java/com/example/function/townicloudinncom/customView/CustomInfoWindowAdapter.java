package com.example.function.townicloudinncom.customView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.InfoWindowAnimationManager;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.bumptech.glide.Glide;
import com.example.function.townicloudinncom.R;
import com.example.function.townicloudinncom.mode.TownLocation;

import java.util.List;

/**
 * Created by function on 2017/10/26.
 */

public class CustomInfoWindowAdapter implements AMap.InfoWindowAdapter {
    private Context context;
    private List<TownLocation.DataBean> towns;
    private TownLocation.DataBean dataBean;
    private LatLng latLng2;
    private LatLng latLng1;

    public CustomInfoWindowAdapter(Context context, List<TownLocation.DataBean> towns){
        this.context = context;
        this.towns = towns;

    }

    @Override
    public View getInfoWindow(Marker marker) {
        View infoWindowView = LayoutInflater.from(context).inflate(R.layout.map_info_window,null);
        ImageView imageView = infoWindowView.findViewById(R.id.info_image);
        TextView textTitle = infoWindowView.findViewById(R.id.info_title);
        TextView textName = infoWindowView.findViewById(R.id.info_name);
        latLng1 = marker.getOptions().getPosition();
        for (int i = 0; i < towns.size() - 1; i++) {
            dataBean = towns.get(i);
            latLng2 = new LatLng(dataBean.getLat(),towns.get(i).getLng());
            if (latLng1.equals(latLng2)){
                textTitle.setText(dataBean.getCity_name());
                textName.setText(dataBean.getName());
                Glide.with(context).load(dataBean.getThumb()).placeholder(R.drawable.default_house).into(imageView);
            }
        }
        return infoWindowView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
