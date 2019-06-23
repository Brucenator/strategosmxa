package jpcasillas.gdl.jal.mx.strategosmx;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blikoon.qrcodescanner.QrCodeActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralContract;
import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralDBHelper;
import jpcasillas.gdl.jal.mx.strategosmx.models.BoletasVO;
import jpcasillas.gdl.jal.mx.strategosmx.qrReader.IntentIntegrator;
import jpcasillas.gdl.jal.mx.strategosmx.qrReader.IntentResult;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.LocationHelper;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.Utilities;


public class InicioBoletasActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,ActivityCompat.OnRequestPermissionsResultCallback {

    //parametros para pasar y recibir valores
    TextView datoqr, ejecutivo;
    public final static String EJECUTIVO_KEY = "ejecutivo_key";
    public final static String QR_KEY = "qr_key";
    public final static String CONSECUTIVO = "consecutivo_key";
    public final static String PROCESO_KEY = "proceso_jey";
    public final static String MEDIDOR_KEY = "medidor_key";
    public final static String MODULOS_KEY = "modulos_key";
    public final static String IMEI_KEY = "imei_key";
    //para grabar las fotografias
    //private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/notificaciones/";
    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/boletas/";
    private File file = new File(ruta_fotos);
    public static int count = 0;
    static String ejec;
    private static final int REQUEST_CAPTURE_IMAGE = 100;
    private static final int REQUEST_CAPTURE_QR = 0x0000c0de;
    //LocationManager locationManager;
    double lat, lon;
    String medidor;
    String proceso = "100";
    String bimestre = "0";
    boolean resul = false;
    List<BoletasVO> boletas = new ArrayList<>();
    String modulos;
    String fecha, hora;
    int newWidth = 1500;
    int newHeight = 2500;
    String imei;
    Long idreturn;
    LocationHelper locationHelper;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstaneState) {
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setSubtitle("Reporte de Boletas - Captura");

        super.onCreate(savedInstaneState);
        setContentView(R.layout.activity_inicio_boletas);

        Intent intent = getIntent();
        ejecutivo = findViewById(R.id.txtInicioEjecutivo);
        ejec = intent.getStringExtra(EJECUTIVO_KEY);
        ejecutivo.setText(ejec);

        String lecturaqr = intent.getStringExtra(QR_KEY);
        datoqr = findViewById(R.id.txtInicioManzanaCuenta);
        datoqr.setText(lecturaqr);

        modulos = intent.getStringExtra(MODULOS_KEY);
        imei = intent.getStringExtra(IMEI_KEY);

        try {
            medidor = intent.getStringExtra(MEDIDOR_KEY);
            proceso = intent.getStringExtra(PROCESO_KEY);
        } catch (Exception e) {
        }

        String consecutivo = intent.getStringExtra(CONSECUTIVO);
        if (consecutivo != null) {
            count = Integer.parseInt(consecutivo);
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try {
            file.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationHelper=new LocationHelper(this);
        locationHelper.checkpermission();

        // check availability of play services
        if (locationHelper.checkPlayServices()) {

            // Building the GoogleApi client
            locationHelper.buildGoogleApiClient();
        }

    }

    //manda llamar activity de lector QR
    public void sendLectorQR(View v) {
        //Intent intent = getIntent();
        Intent intent = new Intent(InicioBoletasActivity.this, QrCodeActivity.class);

        count = 0;
        // Se instancia un objeto de la clase IntentIntegrator
        //IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        // Se procede con el proceso de scaneo
        intent.putExtra("requestCode", REQUEST_CAPTURE_QR);
        //scanIntegrator.initiateScan();
        startActivityForResult(intent, REQUEST_CAPTURE_QR);
    }

    //abre la camara para tomar fotografia y regresa a este mismo activity
    //@TargetApi(22)
    @TargetApi(Build.VERSION_CODES.M)
    public void sendFotografia(View v) {

        if (ActivityCompat.checkSelfPermission(InicioBoletasActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(InicioBoletasActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            count++;
            datoqr = findViewById(R.id.txtInicioManzanaCuenta);
            String manzana = datoqr.getText().toString();
            String file = ruta_fotos + getCode(manzana, count) + ".jpg";
            File mi_foto = new File(file);
            try {
                mi_foto.createNewFile();
            } catch (IOException ex) {
                Log.e("ERROR ", "Error:" + ex);
            }
            //
            Uri uri = Uri.fromFile(mi_foto);
            //Abre la camara para tomar la foto
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //Guarda imagen
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            //Retorna a la actividad
            startActivityForResult(cameraIntent, REQUEST_CAPTURE_IMAGE);

        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(getApplicationContext(), "Permission Needed.", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    public void regresar(View v) {
        Intent inicio = new Intent(getApplicationContext(), BoletasActivity.class);
        ejecutivo = findViewById(R.id.txtInicioEjecutivo);
        ejec = ejecutivo.getText().toString();
        inicio.putExtra(EJECUTIVO_KEY, ejec);
        inicio.putExtra(PROCESO_KEY, proceso);
        inicio.putExtra(MODULOS_KEY, modulos);
        inicio.putExtra(IMEI_KEY,imei);
        startActivity(inicio);
        finish();
    }


    private String getCode(String manzana, int count) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM", Locale.US);
        String date = dateFormat.format(new Date());
        return manzana + "_" + count + "_" + date;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationHelper.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(InicioBoletasActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(InicioBoletasActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    count++;
                    datoqr = findViewById(R.id.txtInicioManzanaCuenta);
                    String manzana = datoqr.getText().toString();
                    String file = ruta_fotos + getCode(manzana, count) + ".jpg";
                    File mi_foto = new File(file);
                    try {
                        mi_foto.createNewFile();
                    } catch (IOException ex) {
                        Log.e("ERROR ", "Error:" + ex);
                    }
                    //
                    Uri uri = Uri.fromFile(mi_foto);
                    //Abre la camara para tomar la fot0
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //Guarda imagen
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                    //limpia fotos con size 0
                    String files;
                    File folder = new File(ruta_fotos);
                    File[] listOfFiles = folder.listFiles();
                    if (listOfFiles != null) {
                        for (File a : listOfFiles) {
                            if (a.isFile()) {
                                files = a.getName();
                                File archivo = new File(ruta_fotos + files);
                                if (archivo.length() == 0) {
                                    archivo.delete();
                                }
                            }
                        }
                    }

                    //Retorna a la actividad
                    startActivityForResult(cameraIntent, REQUEST_CAPTURE_IMAGE);
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(getApplicationContext(), "Permission Needed.", Toast.LENGTH_LONG).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Permission Needed.", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /*private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación " +
                        "usa esta app")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
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
        public void onLocationChanged(final Location location) {
            lon = location.getLongitude();
            lat = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lon = location.getLongitude();
                    lat = location.getLatitude();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            isLocationEnabled();
        }

        @Override
        public void onProviderEnabled(String s) {        }

        @Override
        public void onProviderDisabled(String s) {
            locationManager.removeUpdates(locationListenerGPS);
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationHelper.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {

            // get new image here like this
            count++;
            datoqr = findViewById(R.id.txtInicioManzanaCuenta);
            String manzana = datoqr.getText().toString();
            String file = ruta_fotos + getCode(manzana, count) + ".jpg";
            File mi_foto = new File(file);
            try {
                mi_foto.createNewFile();
            } catch (IOException ex) {
                Log.e("ERROR ", "Error:" + ex);
            }
            //
            Uri uri = Uri.fromFile(mi_foto);
            //Abre la camara para tomar la foto
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //Guarda imagen
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            //Retorna a la actividad
            startActivityForResult(cameraIntent, REQUEST_CAPTURE_IMAGE);
        }

        if (requestCode == REQUEST_CAPTURE_QR && resultCode == Activity.RESULT_OK)

        {
            //IntentResult scanningResult = IntentIntegrator.parseActivityResult(
            //requestCode, resultCode, data);
            //if (scanningResult != null) {
            if(data!=null){
                String lectura = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                // Quiere decir que se obtuvo resultado pro lo tanto:
                // Desplegamos en pantalla el contenido del codigo de barra scaneado
                //String scanContent = scanningResult.getContents();
                datoqr.setText(lectura);

            } else {
                // Quiere decir que NO se obtuvo resultado
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No se ha recibido datos del scaneo!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed(); commented this line in order to disable back press
        //Write your code here
        Toast.makeText(getApplicationContext(), "Click el botón regresar.", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            super.onKeyDown(keyCode, event);
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendGrabarCampo(View v) {
        String qr = datoqr.getText().toString().trim();

        if (!TextUtils.isEmpty(qr)) {
            /*Location loc;
            if (!checkLocation())
                return;
            if (ActivityCompat.checkSelfPermission(InicioBoletasActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(InicioBoletasActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(InicioBoletasActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(true);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                String provider = locationManager.getBestProvider(criteria, true);
                //locationManager.requestLocationUpdates(
                //        provider, 60 * 1000, 10, locationListenerGPS);
                locationManager.requestLocationUpdates(
                        provider, 20 * 1000, 10, locationListenerGPS);
                if (locationManager != null) {
                    loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc != null) {
                        lon = loc.getLongitude();
                        lat = loc.getLatitude();
                    }
                }
            }*/

            mLastLocation=locationHelper.getLocation();

            if (mLastLocation != null) {
                lat = mLastLocation.getLatitude();
                lon = mLastLocation.getLongitude();
            } else {
                Toast.makeText(this,"Nó fue posible obtener la ubicación favor de habilitar el GPS",Toast.LENGTH_SHORT).show();
            }

            //limpia fotos con size 0
            String files;
            final File folder = new File(ruta_fotos);

            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    String[] extension = {"jpg", "JPG"};
                    if (folder.isDirectory() && !folder.isHidden()) {
                        return true;
                    }
                    // loops through and determines the extension of all files in the directory
                    for (String ext : extension) {
                        if (folder.getName().toLowerCase().endsWith(ext)) {
                            return true;
                        }
                    }
                    return false;
                }
            };

            File[] listOfFiles = folder.listFiles(filter);
            boolean tienefoto = false;
            if (listOfFiles.length > 0) {
                for (File a : listOfFiles) {
                    if (a.getName().startsWith(datoqr.getText().toString())) {
                        tienefoto = true;
                        break;
                    } else {
                        tienefoto = false;
                    }
                }
                if (tienefoto) {
                    for (File a : listOfFiles) {
                        if (a.isFile()) {
                            files = a.getName();
                            File archivo = new File(ruta_fotos + files);
                            if (archivo.length() == 0) {
                                archivo.delete();
                            } else {
                                if (files.endsWith(".jpg") || files.endsWith(".JPG")) {
                                    if (a.getName().startsWith(datoqr.getText().toString())) {
                                        //comprimir imagenes
                                        try {

                                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                            bmOptions.inSampleSize = 2;
                                            //Bitmap bitmap = BitmapFactory.decodeFile(file.getName(), bmOptions);
                                            Bitmap bitmap = BitmapFactory.decodeFile(archivo.getAbsolutePath(), bmOptions);
                                            // make a new bitmap from your file
                                            //Bitmap bitmap = BitmapFactory.decodeFile(file.getName());

                                            OutputStream outStream = new FileOutputStream(archivo);
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream);
                                            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                                            outStream.flush();
                                            outStream.close();

                                            Log.i("Se comprimio: ", archivo.getName() + " Bytes:" + archivo.length() + " OK");

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }

                    /*if(lat==0 && lon==0){
                        if (!checkLocation())
                            return;
                        if (ActivityCompat.checkSelfPermission(InicioBoletasActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(InicioBoletasActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(InicioBoletasActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        } else {
                            Criteria criteria = new Criteria();
                            criteria.setAccuracy(Criteria.ACCURACY_FINE);
                            criteria.setAltitudeRequired(false);
                            criteria.setBearingRequired(false);
                            criteria.setCostAllowed(true);
                            criteria.setPowerRequirement(Criteria.POWER_LOW);
                            String provider = locationManager.getBestProvider(criteria, true);
                            locationManager.requestLocationUpdates(
                                    provider, 60 * 1000, 10, locationListenerGPS);
                            locationManager.requestLocationUpdates(
                                    provider, 20*1000, 5, locationListenerGPS);
                            if (locationManager != null) {
                                loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (loc != null) {
                                    lon = loc.getLongitude();
                                    lat = loc.getLatitude();
                                }
                            }
                        }
                    }*/
                    DbCentralDBHelper dbHelper = new DbCentralDBHelper(this);

                    // Create a new map of values, where column names are the keys
                    Date date = new Date();
                    DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

                    fecha = dateFormat.format(date);
                    hora = hourFormat.format(date);

                    ContentValues values = new ContentValues();
                    values.put(DbCentralContract.BoletasEntry.MANZANA, qr);
                    values.put(DbCentralContract.BoletasEntry.FECHA_REGISTRO, fecha);
                    values.put(DbCentralContract.BoletasEntry.HORA_REGISTRO, hora);
                    values.put(DbCentralContract.BoletasEntry.EJECUTIVO, ejec);
                    values.put(DbCentralContract.BoletasEntry.LATITUD, lat);
                    values.put(DbCentralContract.BoletasEntry.LONGITUD, lon);
                    values.put(DbCentralContract.BoletasEntry.PROCESO, proceso);
                    values.put(DbCentralContract.BoletasEntry.BIMESTRE, 0);
                    values.put(DbCentralContract.BoletasEntry.IMEI, imei);
                    //values.put(DbCentralContract.BoletasEntry.ENVIADO,0);

                    idreturn = dbHelper.saveBoletas(values);
                    dbHelper.close();

                    dbHelper = new DbCentralDBHelper(this);
                    values = new ContentValues();
                    values.put(DbCentralContract.EjecutivosEntry.EJECUTIVO, ejec);
                    values.put(DbCentralContract.EjecutivosEntry.CONSECUTIVO, count);
                    dbHelper.saveEjecutivos(values);
                    dbHelper.close();

                    //si tiene conexion envia informacion al server
                    if (Utilities.CheckInternetConnection(this)) {
                        boletas = new ArrayList<>();
                        Log.i("enviando datos: ", "true");
                        BoletasVO b = new BoletasVO();
                        b.setImei(imei);
                        b.setProceso(1);
                        b.setBimestre(bimestre);
                        b.setEjecutivo(ejec);
                        b.setFecharegistro(fecha);
                        b.setHoraregistro(hora);
                        b.setLatitud(lat);
                        b.setLongitud(lon);
                        b.setManzana(qr);

                        boletas.add(b);
                        try {
                            new ReporteCampoWSAsync().execute(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    count = 0;
                    datoqr = findViewById(R.id.txtInicioManzanaCuenta);
                    datoqr.setText("");
                    lat = 0;
                    lon = 0;
                    //locationManager.removeUpdates(locationListenerGPS);
                    locationHelper.connectApiClient();
                    Toast.makeText(InicioBoletasActivity.this, "Registro grabado satisfactoriamente", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(InicioBoletasActivity.this, "Es necesario tomar al menos una fotografía", Toast.LENGTH_LONG).show();
                    return;
                }

            } else {
                Toast.makeText(InicioBoletasActivity.this, "Es necesario tomar al menos una fotografía", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            Toast.makeText(getApplicationContext(), "Es necesario leer un QR, código de barras o teclarlo.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastLocation=locationHelper.getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        locationHelper.connectApiClient();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    class ReporteCampoWSAsync extends AsyncTask<Integer, Void, Boolean> {

        @Override
        @TargetApi(26)
        protected Boolean doInBackground(Integer... params) {

            final String NAMESPACE = "http://tempuri.org/";
            final String URL = "http://strategosmx.com/strategosmxWS/ServiceStrategos.svc";
            final String METHOD_NAME = "ReportaActividadBoletas";
            String SOAP_ACTION = "http://tempuri.org/IServiceStrategos/ReportaActividadBoletas";
            int resultado_xml = 0;
            SoapObject result = null;
            Integer count = 1;

            try {
                Thread.sleep(1000);

                //envia informacion de la base de datos
                for (BoletasVO n : boletas) {
                    SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                    try {

                        request.addProperty("manzana", n.getManzana());
                        request.addProperty("bimestre", n.getBimestre());
                        request.addProperty("fecharegistro", n.getFecharegistro() + " " + n.getHoraregistro());
                        request.addProperty("idejecutivo", n.getEjecutivo());
                        request.addProperty("latitud", String.valueOf(n.getLatitud()));
                        request.addProperty("longitud", String.valueOf(n.getLongitud()));
                        request.addProperty("imei", n.getImei());
                        request.addProperty("idproceso", 1);

                        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                        envelope.implicitTypes = true;
                        envelope.setOutputSoapObject(request);
                        envelope.dotNet = true;

                        HttpTransportSE httpTransport = new HttpTransportSE(URL);
                        httpTransport.debug = true;

                        Log.i("envelope: ", envelope.bodyOut.toString());
                        try {
                            httpTransport.call(SOAP_ACTION, envelope);
                            result = (SoapObject) envelope.bodyIn;
                            Log.i("RESPONSE DATOS", String.valueOf(result));
                            httpTransport.reset();

                        } catch (HttpResponseException e) {
                            Log.e("HTTPLOG", e.getMessage());
                            e.printStackTrace();
                        } catch (IOException e) {
                            Log.e("IOLOG", e.getMessage());
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            Log.e("XMLLOG", e.getMessage());
                            e.printStackTrace();
                        } catch (InternalError e) {
                            Log.e("Internal", e.getMessage());
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        resultado_xml = Integer.parseInt(result.getProperty(0).toString());
                        Log.i("Resultado XML ",String.valueOf(resultado_xml));
                        if (resultado_xml == 1) {
                            resul = true;
                            DbCentralDBHelper db = new DbCentralDBHelper(getApplicationContext());
                            db.deleteBoletas(idreturn);
                            db.close();
                        }

                    } catch (Exception e) {
                        resul = false;
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                resul = false;
                e.printStackTrace();
            }
            return resul;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i("Result onPostExecute: ", result.toString());
            resul = result;
        }
    }
}