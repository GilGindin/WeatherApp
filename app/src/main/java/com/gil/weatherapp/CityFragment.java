package com.gil.weatherapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.label305.asynctask.SimpleAsyncTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class CityFragment extends Fragment {
    private static final String TAG = "CityFragment";
    private List<String> listCities;
    private MaterialSearchBar mMaterialSearchBar;

    ImageView img_weather;
    TextView text_city_name, txt_temprature, txt_description, txt_date_time, txt_wind, txt_pressure, txt_humidity, txt_sunrise, txt_sunset, txt_geo_coords;
    LinearLayout weather_panel;
    ProgressBar loading;

    CompositeDisposable mCompositeDisposable;
    IoOpenWeatherMap mService;

    static CityFragment instance;

    public static CityFragment getInstance() {
        if (instance == null) {
            instance = new CityFragment();
        }
        return instance;
    }

    public CityFragment() {
        mCompositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IoOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_city, container, false);

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

        mMaterialSearchBar = itemView.findViewById(R.id.searchBar);
        mMaterialSearchBar.setEnabled(false);

        //asynctask class to load cities
        new loadCities().execute();

        return itemView;
    }

    private class loadCities extends SimpleAsyncTask<List<String>> {
        @Override
        protected List<String> doInBackground() {

            listCities = new ArrayList<>();
            try {
                StringBuilder builder = new StringBuilder();
                InputStream inputStream = getResources().openRawResource(R.raw.city_list);
                GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);

                InputStreamReader reader = new InputStreamReader(gzipInputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                Log.d(TAG, "doInBackground: "+gzipInputStream.toString());
                String reading;
                while ((reading = bufferedReader.readLine()) != null) {
                    builder.append((reading));

                    listCities = new Gson().fromJson(builder.toString(), new TypeToken<List<String>>() {
                    }.getType());
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return listCities;
        }

        @Override
        protected void onSuccess(final List<String> listCity) {
            super.onSuccess(listCity);

            mMaterialSearchBar.setEnabled(true);
            mMaterialSearchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    List<String> suggest = new ArrayList<>();
                    for (String search : listCity) {
                        if (search.toLowerCase().contains(mMaterialSearchBar.getText().toLowerCase())) {
                            suggest.add(search);
                        }
                        mMaterialSearchBar.setLastSuggestions(suggest);
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            mMaterialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {

                }

                @Override
                public void onSearchConfirmed(CharSequence text) {

                    getWeatherInformation(text.toString());
                    mMaterialSearchBar.setLastSuggestions(listCity);
                }

                @Override
                public void onButtonClicked(int buttonCode) {

                }
            });

            mMaterialSearchBar.setLastSuggestions(listCity);
            loading.setVisibility(View.GONE);
        }
    }

    private void getWeatherInformation(String cityName) {

        mCompositeDisposable.add(mService.getWeatherByCityName(cityName, Common.APP_ID )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {
                        Log.d(TAG, "accept: ---------------------" + MainActivity.latitude + " / " + MainActivity.longitude);
                        Log.d(TAG, "accept: ---------------------" + Common.current_location.getLatitude() + "," + String.valueOf(Common.current_location.getLongitude()));
                        //load image
                        Picasso.with(getActivity()).load(new StringBuilder("https://openweathermap.org/img/wn/")
                                .append(weatherResult.getWeather().get(0).getIcon())
                                .append(".png").toString()).into(img_weather);

                        //Load information
                        text_city_name.setText(weatherResult.getName());
                        txt_description.setText(new StringBuilder("Weather in ")
                                .append(weatherResult.getName()));
                        txt_temprature.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getTemp())).append("°C").toString());
                        txt_date_time.setText(Common.convertTextToDate(weatherResult.getId()));
                        txt_pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                        txt_humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append("%").toString());
                        txt_sunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                        txt_sunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
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
    public void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }
}
