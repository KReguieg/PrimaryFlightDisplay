package de.flowment.pirmaryflightdisplay;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    public Location mLocation;
    public Location mStartLocation;
    public Location mEndLocation;
    public ImageView mImageView;
    public String mCurrentPhotoPath;

    public TextView tripTitleEditText;
    public TextView speedTxtView;
    public TextView heightTxtView;
    public TextView latTxtView;
    public TextView longTxtView;
    public TextView distanceTxtView;
    public Chronometer chronometer;

    public boolean recordStarted;
    public KmlWriter kmlWriter;

    public File pathToKMLFile;

    TripSQLiteHelper tripSQLiteHelper;
    SQLiteDatabase db;
    //public String apiKey = "bc5a85509612dff7c669207612f8f34a";
    //private final OkHttpClient client = new OkHttpClient();

    public Trip t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        tripSQLiteHelper = new TripSQLiteHelper(MainActivity.this, "DBTrip", null, 1);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        try {
            pathToKMLFile = new File("/sdcard/" +
                    String.valueOf(System.currentTimeMillis()/1000) +
                    ".kml");
            kmlWriter = new KmlWriter(new FileOutputStream(pathToKMLFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mImageView = (ImageView) findViewById(R.id.snapshotImageView);
        final Button btn = (Button) findViewById(R.id.triggerBtn);
        final Button stopBtn = (Button) findViewById(R.id.stopBtn);
        final Button takeTaggedSnapshotBtn = (Button) findViewById(R.id.takeTaggedSnapshotBtn);
        tripTitleEditText = (TextView) findViewById(R.id.tripTitleEditText);
        speedTxtView = (TextView) findViewById(R.id.speedTextView);
        heightTxtView = (TextView) findViewById(R.id.heightTextView);
        latTxtView = (TextView) findViewById(R.id.latTextView);
        longTxtView = (TextView) findViewById(R.id.longTextView);
        distanceTxtView = (TextView) findViewById(R.id.temperatureTextView);
        getLocation();
        btn.setText("Start Trip");
        stopBtn.setText("Stop Trip");

        /*
        String forecastURL = "https://api.forecast.io/forecast/" + apiKey + "/" + mLocation.getLatitude() + "," + mLocation.getLongitude();
        Request request = new Request.Builder()
                .url(forecastURL)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                Log.v(TAG, response.body().string());
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception caught: ", e);
        }*/

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Started Recording...");
                Toast.makeText(MainActivity.this, "Started Recording GPS Data...", Toast.LENGTH_SHORT).show();
                mStartLocation = mLocation;
                chronometer.start();
                chronometer.setBase(SystemClock.elapsedRealtime());
                recordStarted = true;
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEndLocation = mLocation;
                recordStarted = false;
                chronometer.stop();
                kmlWriter.writeKml();
                saveTripToDatabase();
                float kilometersWalked = mStartLocation.distanceTo(mEndLocation);
                Toast.makeText(MainActivity.this, "Kilometerswalked: " + kilometersWalked/1000 + "km", LENGTH_LONG).show();
            }
        });

        takeTaggedSnapshotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

    }

    private void saveTripToDatabase() {

        db = tripSQLiteHelper.getWritableDatabase();

        t = new Trip(1, tripTitleEditText.getText().toString(), (int) (SystemClock.elapsedRealtime() - chronometer.getBase())/1000, (int) mStartLocation.distanceTo(mEndLocation));
        String title = t.getTitle();
        int time = t.getTimeInSeconds();
        double kilometersWalked = t.getKiloMetersWalked();
        Toast.makeText(MainActivity.this, String.valueOf(pathToKMLFile), LENGTH_LONG).show();
        if (db != null) {
            // preparing content values
            ContentValues newRecord = new ContentValues();
            newRecord.put("title", title);
            newRecord.put("time", time);
            newRecord.put("kilometersWalked", kilometersWalked);
            newRecord.put("pathToKMLFile", String.valueOf(pathToKMLFile));

            // Insert records
            db.insert("trip", null, newRecord);
            db.close();
        }
        Toast.makeText(MainActivity.this, "Your Trip \"" + title + "\" has been added.", LENGTH_LONG).show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        kmlWriter.pushImagePath(mCurrentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(MainActivity.this, "RESULT OF INTENT", Toast.LENGTH_SHORT).show();
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //mImageView.setImageBitmap(imageBitmap);
            /*Uri uri = Uri.fromFile(new File(mCurrentPhotoPath));
            Bitmap myImg = BitmapFactory.decodeFile(uri.getPath());
            mImageView.setImageBitmap(myImg);*/
            galleryAddPic();
        }
    }

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
                speedTxtView.setText("Speed: " + location.getSpeed()*3.6 + " km/h");
                heightTxtView.setText("Height: " + location.getAltitude() + " m" );
                latTxtView.setText("Latitude: " + location.getLatitude());
                longTxtView.setText("Longitude: " + location.getLongitude());
                if (recordStarted && kmlWriter != null)
                    kmlWriter.pushLocation(mLocation);
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

