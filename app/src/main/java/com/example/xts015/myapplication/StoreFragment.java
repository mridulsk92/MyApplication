package com.example.xts015.myapplication;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class StoreFragment extends android.support.v4.app.Fragment {

    ArrayList<String> shopImages = new ArrayList<String>();
    private SliderLayout mSlider;
    ListView options;
    LayoutInflater inflater;
    String[] optionArray;
    Typeface font;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.shop, container, false);

        //Initialise
        options = (ListView) v.findViewById(R.id.optionList);
        mSlider = (SliderLayout) v.findViewById(R.id.slider);
        font = Typeface.createFromAsset(getActivity().getAssets(), getString(R.string.font));


        //Set data to ListView
        shopImages = getArguments().getStringArrayList("Shop Images");
        optionArray = getActivity().getResources().getStringArray(R.array.Options);
        CustomStoreView adapter = new CustomStoreView(getActivity(), R.layout.banner_layout, optionArray);
        options.setAdapter(adapter);

        //onClick of ListView
        options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        Intent i = new Intent(getActivity(), NewArrivalActivity.class);
                        i.putExtra("Source", "New Arrival");
                        startActivity(i);
                        break;
                    case 2:
                        Intent j = new Intent(getActivity(), CollectionActivity.class);
                        startActivity(j);
                }
            }
        });

        for (int i = 0; i < shopImages.size(); i++) {
            Log.d("Test", shopImages.get(i));
            DefaultSliderView textSliderView = new DefaultSliderView(getActivity());
            textSliderView.image(shopImages.get(i));
            mSlider.addSlider(textSliderView);
        }

        return v;
    }

    private class CustomStoreView extends ArrayAdapter<String> {

        public CustomStoreView(Context context, int textViewResourceId, String[] Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            TextView item;
        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                //inflate the custom layout
                convertView = inflater.from(parent.getContext()).inflate(R.layout.layout_options_list, parent, false);
                viewHolder = new ViewHolder();

                //cache the views
                viewHolder.item = (TextView) convertView.findViewById(R.id.option_item);

                //link the cached views to the convertview
                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            //set the data to be displayed
            viewHolder.item.setTypeface(font);
            viewHolder.item.setText(optionArray[position]);

            return convertView;
        }
    }

}
