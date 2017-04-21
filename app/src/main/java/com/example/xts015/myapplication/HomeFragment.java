package com.example.xts015.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HomeFragment extends android.support.v4.app.Fragment {

    ListView bannerList;
    ArrayList<String> bannerImages = new ArrayList<String>();
    LayoutInflater inflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.home, container, false);

        //Initialise
        bannerList = (ListView) v.findViewById(R.id.bannerList);

        Bundle bundle = new Bundle();
        bannerImages = getArguments().getStringArrayList("Banner Images");

//        for (int i = 0; i < count; i++) {
//            bannerImages.add(i, getArguments().getStringArrayList("Banner Images").toString());
//        }

        CustomHomeView adapter = new CustomHomeView(getActivity(), R.layout.banner_layout, bannerImages);
        bannerList.setAdapter(adapter);


//        for (int j = 0; j < imgList.size(); j++) {
//
//            String str = imgList.get(j);
//            Log.d("test", str);
//            textSliderView
//                    .image(imgList.get(j))
//                    .setScaleType(BaseSliderView.ScaleType.Fit);
//            mCarousel.addSlider(textSliderView);
//        }

        return v;
    }

    private class CustomHomeView extends ArrayAdapter<String> {

        public CustomHomeView(Context context, int textViewResourceId, ArrayList<String> Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            ImageView bannerImg;
        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                //inflate the custom layout
                convertView = inflater.from(parent.getContext()).inflate(R.layout.banner_layout, parent, false);
                viewHolder = new ViewHolder();

                //cache the views
                viewHolder.bannerImg = (ImageView) convertView.findViewById(R.id.imageView_banner);

                //link the cached views to the convertview
                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            //set the data to be displayed
            Log.d("IMG", bannerImages.get(position));
            Picasso.with(getActivity()).load(bannerImages.get(position)).fit().into(viewHolder.bannerImg);

            return convertView;
        }
    }
}
