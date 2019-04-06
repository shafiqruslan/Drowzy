package com.example.drowzy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TipsActivity extends AppCompatActivity {

    private ViewPager mSlideViewPager;
    private LinearLayout mDotLayout;

    private TextView[] mDots;

    private SliderAdapter sliderAdapter;

    private Button mNextBtn;
    private Button mStartBtn;
    private Button mBackBtn;

    private int mCurrentPage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tips_activity);

        mSlideViewPager = (ViewPager) findViewById(R.id.slideViewPager);
        mDotLayout = (LinearLayout) findViewById(R.id.dotsLayout);

        mNextBtn = (Button) findViewById(R.id.nextBtn);
        mBackBtn = (Button) findViewById(R.id.prevBtn);
        mStartBtn = (Button) findViewById(R.id.startBtn);
        sliderAdapter = new SliderAdapter(this);

        mSlideViewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);

        mSlideViewPager.addOnPageChangeListener(viewListener);

        //OnClickListeners

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSlideViewPager.setCurrentItem(mCurrentPage + 1);

            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSlideViewPager.setCurrentItem(mCurrentPage - 1);

            }
        });
    }

    public void addDotsIndicator(int position) {

        mDots = new TextView[3];
        mDotLayout.removeAllViews();

        for (int i=0; i < mDots.length; i++) {

            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(40);
            mDots[i].setTextColor(getResources().getColor(R.color.colorTransparentWhite));

            mDotLayout.addView(mDots[i]);

        }

        if(mDots.length > 0){

            mDots[position].setTextColor(getResources().getColor(R.color.colorDrowzy));
        }

    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            addDotsIndicator(i);

            mCurrentPage = i;

                if(i == 0) {

                    mNextBtn.setEnabled(true);
                    mBackBtn.setEnabled(false);
                    mStartBtn.setEnabled(false);
                    mBackBtn.setVisibility(View.INVISIBLE);
                    mNextBtn.setVisibility(View.VISIBLE);
                    mStartBtn.setVisibility(View.INVISIBLE);

                    mNextBtn.setText("Next");
                    mBackBtn.setText("");
                }
                else if(i == mDots.length -1) {

                    mNextBtn.setEnabled(false);
                    mBackBtn.setEnabled(true);
                    mStartBtn.setEnabled(true);
                    mBackBtn.setVisibility(View.VISIBLE);
                    mNextBtn.setVisibility(View.INVISIBLE);
                    mStartBtn.setVisibility(View.VISIBLE);

                    mNextBtn.setText("Start");
                    mBackBtn.setText("Back");

                    mStartBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(TipsActivity.this, LivePreviewActivity.class);
                            startActivity(intent);
                            finish();

                        }
                    });
                }
                else {

                    mNextBtn.setEnabled(true);
                    mBackBtn.setEnabled(true);
                    mStartBtn.setEnabled(false);
                    mBackBtn.setVisibility(View.VISIBLE);
                    mNextBtn.setVisibility(View.VISIBLE);
                    mStartBtn.setVisibility(View.INVISIBLE);

                    mNextBtn.setText("Next");
                    mBackBtn.setText("Back");

            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };
}
