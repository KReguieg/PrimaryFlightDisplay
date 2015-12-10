package de.flowment.pirmaryflightdisplay;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public Location mLocation;
    public TextView speedTxtView;
    public TextView heightTxtView;
    public TextView latTxtView;
    public TextView longTxtView;
    public TextView temperatureTxtView;
    public boolean recordStarted;
    /*public String apiKey = "bc5a85509612dff7c669207612f8f34a";
    private final OkHttpClient client = new OkHttpClient();*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button btn = (Button) findViewById(R.id.triggerBtn);
        speedTxtView = (TextView) findViewById(R.id.speedTextView);
        heightTxtView = (TextView) findViewById(R.id.heightTextView);
        latTxtView = (TextView) findViewById(R.id.latTextView);
        longTxtView = (TextView) findViewById(R.id.longTextView);
        temperatureTxtView = (TextView) findViewById(R.id.temperatureTextView);
        getLocation();
        btn.setText("Start GPS Recording");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Started Recording...");
                Toast.makeText(MainActivity.this, "Started Recording GPS Data...", Toast.LENGTH_SHORT).show();
                recordStarted = true;
                btn.setText("Stop GPS Recording");
                Toast.makeText(getApplicationContext(), "LONG: " + mLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                /*String forecastURL = "https://api.forecast.io/forecast/" + apiKey + "/" + mLocation.getLatitude() + "," + mLocation.getLongitude();
                Toast.makeText(getApplicationContext(), "url: " + forecastURL, Toast.LENGTH_SHORT).show();
                try {
                    run(forecastURL);
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            }
        });
    }

    /*public void run(String forecastURL) throws Exception {
        Request request = new Request.Builder()
                .url(forecastURL)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d(TAG, response.body().toString());
            }
        });
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location= " + mLocation.getLatitude(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You did not allow to access your current location", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void getLocation() {
        LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocation = location;
                speedTxtView.setText("Speed: " + location.getSpeed());
                heightTxtView.setText("Höhe: " + location.getAltitude());
                latTxtView.setText("Latitude: " + location.getLatitude());
                longTxtView.setText("Longitude: " + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                System.out.println("Status= " + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(getApplicationContext(), "GPS Enabled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(getApplicationContext(), "GPS Disabled", Toast.LENGTH_SHORT).show();
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                MainActivity.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
}

