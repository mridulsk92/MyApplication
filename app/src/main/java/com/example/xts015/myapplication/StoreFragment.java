package com.example.xts015.myapplication;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;

import java.util.ArrayList;

public class StoreFragment extends android.support.v4.app.Fragment {

    ArrayList<String> shopImages = new ArrayList<String>();
    private SliderLayout mSlider;
    ListView options;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.shop, container, false);

        //Initialise
        options = (ListView) v.findViewById(R.id.optionList);
        mSlider = (SliderLayout) v.findViewById(R.id.slider);

        //Set data to ListView
        shopImages = getArguments().getStringArrayList("Shop Images");
        String[] optionArray = getActivity().getResources().getStringArray(R.array.Options);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, optionArray);
        options.setAdapter(adapter);

        //onClick of ListView
        options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position){
                    case 0:
                        Intent i = new Intent(getActivity(),NewArrivalActivity.class);
                        i.putExtra("Source", "New Arrival");
                        startActivity(i);
                        break;

                }

            }
        });

        for (int i = 0; i < shopImages.size(); i++) {
            Log.d("Test", shopImages.get(i));
            TextSliderView textSliderView = new TextSliderView(getActivity());
            textSliderView.image(shopImages.get(i));
            mSlider.addSlider(textSliderView);
        }

        return v;
    }
}
