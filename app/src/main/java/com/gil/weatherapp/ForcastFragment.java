package com.gil.weatherapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gil.weatherapp.Adapter.WeatherForecastAdapter;
import com.gil.weatherapp.Common.Common;
import com.gil.weatherapp.Model.WeatherForecastResult;
import com.gil.weatherapp.Retrofit.IoOpenWeatherMap;
import com.gil.weatherapp.Retrofit.RetrofitClient;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForcastFragment extends Fragment {
    private static final String TAG = "ForcastFragment";
    CompositeDisposable mCompositeDisposable;
    IoOpenWeatherMap mService;

    TextView txt_city_name, txt_geo_coords;
    RecyclerView mRecyclerView_forecast;


    static ForcastFragment instance;

    public static ForcastFragment getInstance() {
        if (instance == null) {
            instance = new ForcastFragment();
        }

        return instance;
    }

    public ForcastFragment() {
        mCompositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IoOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forcast, container, false);

        txt_city_name = view.findViewById(R.id.txt_city_name);
        txt_geo_coords = view.findViewById(R.id.txt_geo_coords);

        mRecyclerView_forecast = view.findViewById(R.id.recycler_forecast);
        mRecyclerView_forecast.setHasFixedSize(true);
        mRecyclerView_forecast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        getForecastWeatherInfo();

        return view;
    }

    private void getForecastWeatherInfo() {

        mCompositeDisposable.add(mService.getForecastWeatherByLatLng(String.valueOf(MainActivity.latitude),
                String.valueOf(MainActivity.longitude),
                Common.APP_ID , "metric").subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).
                        subscribe(new Consumer<WeatherForecastResult>() {
                            @Override
                            public void accept(WeatherForecastResult weatherForecastResult) throws Exception {

                                displayForecastWeather(weatherForecastResult);

                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.d(TAG, "ERROR: " + throwable.getMessage());
                            }
                        }));

    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    private void displayForecastWeather(WeatherForecastResult weatherForecastResult) {

        txt_city_name.setText(new StringBuilder(weatherForecastResult.city.name));
        txt_geo_coords.setText(new StringBuilder(weatherForecastResult.city.coord.toString()));

        WeatherForecastAdapter adapter = new WeatherForecastAdapter(getContext(), weatherForecastResult);
        mRecyclerView_forecast.setAdapter(adapter);
    }

}
