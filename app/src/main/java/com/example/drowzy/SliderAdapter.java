package com.example.drowzy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context) {

        this.context =context;
    }

    //arrays
    public  int[] slide_images = {

            R.drawable.car_icon,
            R.drawable.coffee_icon,
            R.drawable.phone_icon

    };

    public  String[] slide_headings = {

            "Dont Rush",
            "Drink Caffeine",
            "Placing mobile"

    };

    public String[] slide_text = {

            "Stop by at the rest area if you feel sleepy",
            "Caffeine can improve the alertness of a person for several hours",
            "Place the phone where you not distracted and the camera is facing on you"

    };


    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(View view,Object o) {

        return view == (RelativeLayout) o;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_activity, container, false);

        ImageView slideImageView = (ImageView) view.findViewById(R.id.image_tips);
        TextView slideHeading = (TextView) view.findViewById(R.id.heading);
        TextView slideDescription = (TextView) view.findViewById(R.id.tips);

        slideImageView.setImageResource(slide_images[position]);
        slideHeading.setText(slide_headings[position]);
        slideDescription.setText(slide_text[position]);

        container.addView(view);

        return view;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((RelativeLayout)object);
    }
}
