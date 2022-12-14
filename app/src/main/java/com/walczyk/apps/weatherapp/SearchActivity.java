package com.walczyk.apps.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    WeatherAPI weatherAPI;
    String currentLocation = "London";
    ImageView favoriteView;

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_search);
        prepareLayout();
        getWeatherData(currentLocation);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ActionBar actionBar =  getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        weatherAPI = RetrofitWeather.getClient().create(WeatherAPI.class);

        prepareLayout();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        getWeatherData(sharedPref.getString("defaultWeatherLocation", currentLocation));
    }

    public void clearEditTextFocus(View v){
        hideSoftKeyboard(this);
        EditText searchEdit = findViewById(R.id.search_name);
        searchEdit.clearFocus();
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText() && activity.getCurrentFocus() != null){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    public void prepareLayout(){
        EditText searchEdit = findViewById(R.id.search_name);
        searchEdit.setOnFocusChangeListener((v, hasFocus) -> {
            LinearLayout searchBorder = findViewById(R.id.search_border);
            if(searchBorder == null)
                return;
            if(hasFocus) {
                searchBorder.setBackgroundResource(R.drawable.white_full);
                ((EditText)findViewById(R.id.search_name)).setTextColor(getColor(R.color.light_blue));
                ((EditText)findViewById(R.id.search_name)).setHintTextColor(getColor(R.color.light_blue));
                ((ImageView)findViewById(R.id.city_image)).setColorFilter(getColor(R.color.light_blue));
            }else {
                searchBorder.setBackgroundResource(R.drawable.white_border);
                ((EditText)findViewById(R.id.search_name)).setTextColor(getColor(R.color.white));
                ((EditText)findViewById(R.id.search_name)).setHintTextColor(getColor(R.color.white));
                ((ImageView)findViewById(R.id.city_image)).setColorFilter(getColor(R.color.white));
            }
        });

        ImageView search = findViewById(R.id.search_btn);
        search.setOnClickListener(view -> {
            getWeatherData(searchEdit.getText().toString());
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
            searchEdit.clearFocus();
        });

        favoriteView = findViewById(R.id.fav_view);
        favoriteView.setOnClickListener(v -> {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            String defaultLocation = sharedPref.getString("defaultWeatherLocation", "London");
            if(!currentLocation.equals(defaultLocation)){
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("defaultWeatherLocation", currentLocation);
                editor.apply();
                favoriteView.setImageResource(R.drawable.ic_favorite);
            }
        });
    }

    public void getWeatherData(String searchText){
            Call<OpenWeatherMap> callForWeather = weatherAPI.getWeatherWithName(searchText);
            callForWeather.enqueue(new Callback<OpenWeatherMap>() {
                @Override
                public void onResponse(@NonNull Call<OpenWeatherMap> call, @NonNull Response<OpenWeatherMap> response) {
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
                        String tempValue = main.getTemp().toString();
                        temp.setText(tempValue + " °C");
                        float tempIndex = (float)(Float.parseFloat(tempValue) + 50.0) / (float) 100;
                        temp.setTextColor(Color.rgb(tempIndex, 0, 1 - tempIndex));
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
                        currentLocation = weatherMap.getName();
                        String country = weatherMap.getSys().getCountry();
                        String locationText = currentLocation + ", " + country;
                        location.setText(locationText);

                        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                        String defaultLocation = sharedPref.getString("defaultWeatherLocation", "London");
                        if(currentLocation.equals(defaultLocation))
                            favoriteView.setImageResource(R.drawable.ic_favorite);
                        else
                            favoriteView.setImageResource(R.drawable.ic_favorite_border);
                    }
                    catch(Exception e){
                        favoriteView.setImageResource(R.drawable.ic_favorite_border);
                        Toast.makeText(SearchActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<OpenWeatherMap> call, @NonNull Throwable t) {
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