package com.example.xts015.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.squareup.picasso.Picasso;


public class AppsFragment extends android.support.v4.app.Fragment {

    String deviceAddress, deviceName;
    String bannerImg;
    ImageView banner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.apps, container, false);

        //Initialise
        banner = (ImageView) v.findViewById(R.id.app_banner);

        bannerImg = getArguments().getString("banner");
        Log.d("Test Apps Fragment", bannerImg);
        if(!bannerImg.isEmpty()) {
            Picasso.with(getActivity()).load(bannerImg).into(banner);
        }
        return v;
    }
}
