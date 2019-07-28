package com.gil.weatherapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gil.weatherapp.Common.Common;
import com.gil.weatherapp.Model.WeatherResult;
import com.gil.weatherapp.Retrofit.IoOpenWeatherMap;
import com.gil.weatherapp.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodayWeatherFragment extends Fragment {
    private static final String TAG = "TodayWeatherFragment";
    ImageView img_weather;
    TextView text_city_name, txt_temprature, txt_description, txt_date_time, txt_wind, txt_pressure, txt_humidity, txt_sunrise, txt_sunset, txt_geo_coords;
    LinearLayout weather_panel;
    ProgressBar loading;

    CompositeDisposable mCompositeDisposable;
    IoOpenWeatherMap mService;

    static TodayWeatherFragment instance;

    public static TodayWeatherFragment getInstance() {
        if (instance == null) {
            instance = new TodayWeatherFragment();
        }
        return instance;
    }

    public TodayWeatherFragment() {
        mCompositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IoOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_blank, container, false);

        img_weather = itemView.findViewById(R.id.img_weather);
        text_city_name = itemView.findViewById(R.id.text_city_name);
        txt_description = itemView.findViewById(R.id.txt_description);
        txt_temprature = itemView.findViewById(R.id.txt_temprature);
        txt_date_time = itemView.findViewById(R.id.txt_date_time);
        txt_wind = itemView.findViewById(R.id.txt_wind);
        txt_pressure = itemView.findViewById(R.id.txt_pressure);
        txt_humidity = itemView.findViewById(R.id.txt_humidity);
        txt_sunrise = itemView.findViewById(R.id.txt_sunrise);
        txt_sunset = itemView.findViewById(R.id.txt_sunset);
        txt_geo_coords = itemView.findViewById(R.id.txt_geo_coords);

        weather_panel = itemView.findViewById(R.id.weather_panel);
       loading = itemView.findViewById(R.id.loading);

        getWeatherInformation();

        return itemView;
    }

    private void getWeatherInformation() {

        mCompositeDisposable.add(mService.getWeatherByLatLng(String.valueOf(MainActivity.latitude), String.valueOf(MainActivity.longitude), Common.APP_ID , "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {
                        Log.d(TAG, "accept: ---------------------" + MainActivity.latitude + " / " + MainActivity.longitude);
                        //load image
                        Picasso.with(getActivity()).load(new StringBuilder("https://openweathermap.org/img/wn/")
                                .append(weatherResult.getWeather().get(0).getIcon())
                                .append(".png").toString()).into(img_weather);

                        //Load information
                        text_city_name.setText(weatherResult.getName());
                        txt_description.setText(new StringBuilder("Weather in")
                                .append(weatherResult.getName().toString()));
                        double temp = weatherResult.getMain().getTemp();
                        txt_temprature.setText(new StringBuilder(
                                String.format("%.2f",temp)).append("°C").toString());
                        txt_date_time.setText(Common.convertTextToDate(weatherResult.getId()));
                        txt_pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                        txt_humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append(" %").toString());
                        txt_sunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
                        txt_sunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                        txt_geo_coords.setText(new StringBuilder(weatherResult.getCoord().toString()).toString());

                        //Display panel
                        weather_panel.setVisibility(View.VISIBLE);
                      loading.setVisibility(View.GONE);


                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getActivity(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "exceptionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn " + throwable.getMessage());
                    }
                })
        );
    }

    @Override
    public void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }
}
