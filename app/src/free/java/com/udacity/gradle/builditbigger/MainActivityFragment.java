package com.udacity.gradle.builditbigger;

import android.content.Context;
import android.os.Bundle;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private static final String TAG = MainActivityFragment.class.getSimpleName();
    private InterstitialAd mInterstitialAd;
    private ButtonClickListener mListener;
    private AdRequest mAdRequest;
    private CountingIdlingResource mIdlingRes;

    public MainActivityFragment() {
    }

    void setIdlingRes(CountingIdlingResource idlingRes){
        Log.d(TAG,"set idling res");
        this.mIdlingRes = idlingRes;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (ButtonClickListener) context;
        }catch (ClassCastException ex){
            Log.d(TAG, "The Activity must implement "
                    + ButtonClickListener.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        //setup the ads
        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                //mInterstitialAd.loadAd(mAdRequest);
                mListener.onButtonClicked();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mIdlingRes.decrement();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                mIdlingRes.decrement();
            }
        });
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        AdView mAdView = (AdView) root.findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        mAdView.loadAd(getAdRequest());
        mInterstitialAd.loadAd(getAdRequest());
        //setup the button
        Button button = root.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mIdlingRes.increment();
                if(mInterstitialAd.isLoaded()){
                    mInterstitialAd.show();
                } else {
                    //mIdlingRes.decrement();
                    Log.d(TAG, "The interstitial wasn't loaded yet.");
                    mListener.onButtonClicked();
                }
            }
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mIdlingRes.increment();
    }

    private AdRequest getAdRequest(){
        if(mAdRequest == null){
            mAdRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
        }
        return mAdRequest;
    }
    interface ButtonClickListener {
        void onButtonClicked();
    }
}
