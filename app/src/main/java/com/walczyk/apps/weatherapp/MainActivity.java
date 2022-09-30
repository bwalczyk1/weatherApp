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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    WeatherAPI weatherAPI;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherAPI = RetrofitWeather.getClient().create(WeatherAPI.class);
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 100);
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, 100);

        locationManager =  (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                getWeatherData(lat, lon);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                LocationListener.super.onProviderDisabled(provider);
            }
        };
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, locationListener);
        }

        TextView addBtn = findViewById(R.id.add_btn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void getWeatherData(double lat, double lon){
        Call<OpenWeatherMap> callForWeather = weatherAPI.getWeatherWithLocation(lat, lon);
        callForWeather.enqueue(new Callback<OpenWeatherMap>() {
            @Override
            public void onResponse(Call<OpenWeatherMap> call, Response<OpenWeatherMap> response) {
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

            @Override
            public void onFailure(Call<OpenWeatherMap> call, Throwable t) {

            }
        });
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
        }
    }
}