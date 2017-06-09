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
import com.yc.peddemo.sdk.WriteCommandToBLE;
import com.yc.peddemo.utils.GlobalVariable;

public class AppsFragment extends android.support.v4.app.Fragment {

    String deviceAddress, deviceName;
    String bannerImg, steps_st, distance_st, calories_st;
    ImageView banner;
    TextView steps, kms, calories;
    PreferencesHelper pref;
    WriteCommandToBLE mWriteCommand;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.apps, container, false);

        //Initialise
        banner = (ImageView) v.findViewById(R.id.app_banner);
        steps = (TextView) v.findViewById(R.id.steps_view);
        kms = (TextView) v.findViewById(R.id.km_view);
        calories = (TextView) v.findViewById(R.id.calories_view);
        pref = new PreferencesHelper(getActivity());

        steps_st = pref.GetPreferences("Steps");
        distance_st = pref.GetPreferences("Distance");
        calories_st = pref.GetPreferences("Calories");

        //Set TextView Values
        steps.setText(steps_st + " Steps");
        kms.setText(distance_st + " km");
        calories.setText(calories_st + " Calories");

        calories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mWriteCommand = WriteCommandToBLE.getInstance(getActivity().getApplicationContext());
                mWriteCommand.sendNameToBLE("Test");
                mWriteCommand.sendIncallCommand(5);
                mWriteCommand.findBand(100);
                mWriteCommand.syncAllStepData();
                mWriteCommand.sendTextToBle("TEST", GlobalVariable.TYPE_SMS);

            }
        });

        bannerImg = getArguments().getString("banner");
        Log.d("Test Apps Fragment", bannerImg);
        if (!bannerImg.isEmpty()) {
            Picasso.with(getActivity()).load(bannerImg).into(banner);
        }

        return v;
    }
}
