package com.xu.roomhunter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Omistaja on 01/04/2017.
 */

public class SettingsFragment extends DialogFragment {

    static Boolean isDateEdit;
    SharedPreferences defaultDates;
    SharedPreferences.Editor edit;
    SeekBar priceBar;
    SeekBar bedBar;
    SeekBar distBar;

    String pricefilt;
    String bedfilt;
    String distfilt;

    TextView priceView;
    TextView bedView;
    TextView distView;
    Button apply;

    SettingsFragment.updateMapListener interfacer;
    private Context mContext;



    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NO_TITLE, 0);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //need to design g(STYLE_NO_TITLE,0);
        mContext = getActivity();

        View dialogView = inflater.inflate(R.layout.fragment_settings, container, false);

        priceBar = (SeekBar)dialogView.findViewById(R.id.priceBar);
        bedBar = (SeekBar)dialogView.findViewById(R.id.bedBar);
        distBar = (SeekBar)dialogView.findViewById(R.id.distBar);

        priceView = (TextView)dialogView.findViewById(R.id.priceView);
        bedView = (TextView)dialogView.findViewById(R.id.bedroomView);
        distView=(TextView)dialogView.findViewById(R.id.distanceTextView);

        apply = (Button)dialogView.findViewById(R.id.btnapply);

        priceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                pricefilt= String.valueOf(progress);
                 priceView.setText("£"+ String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        bedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                bedfilt =String.valueOf(progress);
               bedView.setText( String.valueOf(progress)+ " bedrooms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        distBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                distfilt = String.valueOf(progress);

                distView.setText( String.valueOf(progress)+" km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interfacer.updateMap(pricefilt,bedfilt,distfilt);
                dismiss();
            }
        });













        return dialogView;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            interfacer= (SettingsFragment.updateMapListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement updateMapListener");
        }
    }


    public interface updateMapListener{
        public void updateMap(String maxp,String maxb, String maxd);
    }

}

