package com.gil.weatherapp.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gil.weatherapp.Common.Common;
import com.gil.weatherapp.Model.WeatherForecastResult;
import com.gil.weatherapp.R;
import com.squareup.picasso.Picasso;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.MyHolder> {

    Context mContext;
    WeatherForecastResult mWeatherForecastResult;

    public WeatherForecastAdapter(Context context, WeatherForecastResult weatherForecastResult) {
        mContext = context;
        mWeatherForecastResult = weatherForecastResult;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_weather_forecast, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //load icon
        Picasso.with(mContext).load(new StringBuilder("https://openweathermap.org/img/wn/")
                .append(mWeatherForecastResult.list.get(position).weather.get(0).getIcon())
                .append(".png").toString()).into(holder.img_weather);

        holder.txt_date.setText(new StringBuilder(Common.convertUnixToHour(mWeatherForecastResult.list.get(position).dt)));

        holder.txt_description_forecast.setText(new StringBuilder(mWeatherForecastResult.list.get(position).weather.get(0).getDescription()));

        double temp = mWeatherForecastResult.list.get(position).main.getTemp();
        holder.txt_temprature_forecast.setText(new StringBuilder(
                String.format("%.2f", temp)).append("°C").toString());
    }

    @Override
    public int getItemCount() {
        return mWeatherForecastResult.list.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        TextView txt_date, txt_temprature_forecast, txt_description_forecast;
        ImageView img_weather;

        public MyHolder(View itemView) {
            super(itemView);

            img_weather = itemView.findViewById(R.id.img_weather);
            txt_date = itemView.findViewById(R.id.txt_date);
            txt_temprature_forecast = itemView.findViewById(R.id.txt_temprature_forecast);
            txt_description_forecast = itemView.findViewById(R.id.txt_description_forecast);
        }
    }
}
