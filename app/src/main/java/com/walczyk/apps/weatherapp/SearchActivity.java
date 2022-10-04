package com.walczyk.apps.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    WeatherAPI weatherAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ActionBar actionBar =  getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        weatherAPI = RetrofitWeather.getClient().create(WeatherAPI.class);

        ImageView search = findViewById(R.id.search_btn);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText searchName = findViewById(R.id.search_name);
                getWeatherData(searchName.getText().toString());
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchName.getWindowToken(), 0);
            }
        });

        getWeatherData("London");
    }

    public void getWeatherData(String name){
            Call<OpenWeatherMap> callForWeather = weatherAPI.getWeatherWithName(name);
            callForWeather.enqueue(new Callback<OpenWeatherMap>() {
                @Override
                public void onResponse(Call<OpenWeatherMap> call, Response<OpenWeatherMap> response) {
                    try {
                        OpenWeatherMap weatherMap = response.body();
                        Weather weather = weatherMap.getWeather().get(0);
                        TextView description = findViewById(R.id.description);
                        description.setText(weather.getDescription());
                        ImageView image = findViewById(R.id.image);
                        String iconUrl = "http://openweathermap.org/img/w/" + weather.getIcon() + ".png";
                        Picasso.get().load(iconUrl).into(image);
                        Main main = weatherMap.getMain();
                        TextView temp = findViewById(R.id.temp);
                        temp.setText(main.getTemp().toString() + " °C");
                        TextView maxTemp = findViewById(R.id.max_temp);
                        maxTemp.setText(": " + main.getTempMax().toString() + " °C");
                        TextView minTemp = findViewById(R.id.min_temp);
                        minTemp.setText(": " + main.getTempMin().toString() + " °C");
                        TextView pressure = findViewById(R.id.pressure);
                        pressure.setText(": " + main.getPressure().toString());
                        TextView humidity = findViewById(R.id.humidity);
                        humidity.setText(": " + main.getHumidity().toString() + "%");
                        TextView windSpeed = findViewById(R.id.wind_speed);
                        windSpeed.setText(": " + weatherMap.getWind().getSpeed().toString());
                        TextView location = findViewById(R.id.location);
                        String name = weatherMap.getName();
                        String country = weatherMap.getSys().getCountry();
                        String locationText = name + ", " + country;
                        location.setText(locationText);
                    }
                    catch(Exception e){

                        Toast.makeText(SearchActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<OpenWeatherMap> call, Throwable t) {
                }
            });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}