package com.example.function.townicloudinncom.mapUtils;

import com.amap.api.maps.model.LatLng;;

/**
 * Created by yiyi.qi on 16/10/10.
 */

public class RegionItem implements ClusterItem {
    private LatLng mLatLng;
    private String mTitle;
    private String mImageUrl;

    public RegionItem(LatLng latLng, String title,String imageUrl) {
        mLatLng=latLng;
        mTitle=title;
        mImageUrl = imageUrl;
    }

    @Override
    public LatLng getPosition() {
        // TODO Auto-generated method stub
        return mLatLng;
    }

    @Override
    public String getImageUrl() {
        return mImageUrl;
    }

    public String getTitle(){
        return mTitle;
    }

}
