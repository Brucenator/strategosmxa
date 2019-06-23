package jpcasillas.gdl.jal.mx.strategosmx;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class OficialiasActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnDescarga, btnInicio, btnRegresar;
    TextView ejecutivo;
    public final static String EJECUTIVO_KEY = "jpcasillas.gdl.jal.mx.strategosmx.ejecutivo_key";
    public final static String LONGITUD_KEY = "jpcasillas.gdl.jal.mx.notificaciones.longitud_key";
    public final static String LATITUD_KEY = "jpcasillas.gdl.jal.mx.notificaciones.latitud_key";
    public final static String PROCESO_KEY = "proceso_jey";
    public final static String USO_KEY = "uso_key";
    public final static String MEDIDOR_KEY = "medidor_key";
    LocationManager locationManager;
    double lat, lon;
    String proceso;
    String medidor;
    String uso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Strategoxmx - Reporte de Boletas");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boletas);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        btnDescarga = findViewById(R.id.btnDescarga);
        btnInicio = findViewById(R.id.btnCaptura);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnDescarga.setOnClickListener(this);
        btnInicio.setOnClickListener(this);
        btnRegresar.setOnClickListener(this);

        Intent intent = getIntent();
        ejecutivo = findViewById(R.id.idEjecutivo);
        String ejec = intent.getStringExtra(EJECUTIVO_KEY);
        ejecutivo.setText(ejec);
        proceso = "103";
        medidor = "SINMEDIDOR";
        uso = "0";

        if (ActivityCompat.checkSelfPermission(OficialiasActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 225);
        }
        if (ActivityCompat.checkSelfPermission(OficialiasActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(OficialiasActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OficialiasActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnDescarga:
                startActivity(new Intent(OficialiasActivity.this, DescargaActivity.class).putExtra(USO_KEY, uso).putExtra(MEDIDOR_KEY, medidor).putExtra(PROCESO_KEY, proceso));
                finish();
                break;
            case R.id.btnCaptura:
                if (!checkLocation())
                    return;
                if (ActivityCompat.checkSelfPermission(OficialiasActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(OficialiasActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(OficialiasActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    Location loc;
                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    String provider = locationManager.getBestProvider(criteria, true);

                    locationManager.requestLocationUpdates(
                            provider, 60 * 1000, 10, locationListenerGPS);
                    //LocationManager.GPS_PROVIDER, 0, 10, locationListenerGPS);
                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc != null) {
                            lon = loc.getLongitude();
                            lat = loc.getLatitude();

                            locationManager.removeUpdates(locationListenerGPS);
                        }
                    }
                }

                ejecutivo = findViewById(R.id.idEjecutivo);
                String message = ejecutivo.getText().toString().trim();

                if (!TextUtils.isEmpty(message)) {
                    startActivity(new Intent(OficialiasActivity.this, InicioBoletasActivity.class).putExtra(EJECUTIVO_KEY, message).putExtra(LATITUD_KEY, lat).putExtra(LONGITUD_KEY, lon).putExtra(USO_KEY, uso).putExtra(MEDIDOR_KEY, medidor).putExtra(PROCESO_KEY, proceso));
                    finish();
                } else {
                    Toast.makeText(OficialiasActivity.this, "Es necesario teclear su numero de Ejecutivo, por favor", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case R.id.btnRegresar:
                Intent inicio = new Intent(getApplicationContext(), MainActivity.class);
                ejecutivo = findViewById(R.id.idEjecutivo);
                message = ejecutivo.getText().toString().trim();
                inicio.putExtra(EJECUTIVO_KEY, message);
                startActivity(inicio);
                finish();
                break;
        }
    }

    private boolean checkLocation() {
        if (!isLocationEnabled()) {
            showAlert();
        }
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            lon = location.getLongitude();
            lat = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("Longitud", String.valueOf(lon));
                    Log.e("Latitud", String.valueOf(lat));

                    Toast.makeText(OficialiasActivity.this, "GPS Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {
            Location loc;
            if (!checkLocation())
                return;
            if (ActivityCompat.checkSelfPermission(OficialiasActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(OficialiasActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(OficialiasActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(
                        provider, 60 * 1000, 10, locationListenerGPS);
                //LocationManager.GPS_PROVIDER, 0, 10, locationListenerGPS);
                if (locationManager != null) {
                    loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc != null) {
                        lon = loc.getLongitude();
                        lat = loc.getLatitude();

                        locationManager.removeUpdates(locationListenerGPS);
                    }
                }
            }
        }

        @Override
        public void onProviderDisabled(String s) {
            locationManager.removeUpdates(locationListenerGPS);
        }
    };

    @Override
    public void onBackPressed() {
        // super.onBackPressed(); commented this line in order to disable back press
        //Write your code here
        Toast.makeText(getApplicationContext(), "Click en botón regresar.", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            super.onKeyDown(keyCode, event);
            return true;
        }
        return false;
    }
}


