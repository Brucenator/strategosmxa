package jpcasillas.gdl.jal.mx.strategosmx;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralContract;
import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralDBHelper;
import jpcasillas.gdl.jal.mx.strategosmx.models.LecturasVO;
import jpcasillas.gdl.jal.mx.strategosmx.models.NotasLecturaVO;
import jpcasillas.gdl.jal.mx.strategosmx.qrReader.IntentIntegrator;
import jpcasillas.gdl.jal.mx.strategosmx.qrReader.IntentResult;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.LocationHelper;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.Utilities;

public class InicioLecturasActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,ActivityCompat.OnRequestPermissionsResultCallback{

    //parametros para pasar y recibir valores
    TextView datoqr, ejecutivo, consumo, medidor, ruta;
    //, bimestre;
    public final static String EJECUTIVO_KEY = "ejecutivo_key";
    public final static String QR_KEY = "qr_key";
    public final static String CONSECUTIVO = "consecutivo_key";
    public final static String PROCESO_KEY = "proceso_jey";
    public final static String MODULOS_KEY = "modulos_key";
    public final static String RUTA_KEY = "ruta_key";
    public final static String IMEI_KEY = "imei_key";
    //para grabar las fotografias
    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/lecturas/";
    private File file = new File(ruta_fotos);
    public static int count = 0;
    static String ejec;
    private static final int REQUEST_CAPTURE_IMAGE = 100;
    private static final int REQUEST_CAPTURE_QR = 0x0000c0de;
    String proceso;
    //LocationManager locationManager;
    double lat, lon;
    String imei;
    List<LecturasVO> lecturas = new ArrayList<LecturasVO>();
    boolean resul = false;
    String modulos;
    String fecha, hora;
    int newWidth = 1500;
    int newHeight = 2500;
    Long idreturn;
    LocationHelper locationHelper;
    private Location mLastLocation;
    Spinner idnotaslectura;
    final String urlString = "http://strategosmx.com/strategosmxWS/ServiceStrategos.svc";
    ArrayList<NotasLecturaVO> lista = new ArrayList<NotasLecturaVO>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("StrategosMX - Captura");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_lecturas);

        Intent intent = getIntent();
        ejecutivo = findViewById(R.id.txtEjecutivo);
        ejec = intent.getStringExtra(EJECUTIVO_KEY);
        ejecutivo.setText(ejec);

        proceso = intent.getStringExtra(PROCESO_KEY);

        String lecturaqr = intent.getStringExtra(QR_KEY);
        datoqr = findViewById(R.id.txtInicioManzanaCuenta);
        datoqr.setText(lecturaqr);

        consumo = findViewById(R.id.txtconsumo);
        medidor = findViewById(R.id.txtmedidor);
        //bimestre = findViewById(R.id.txtbimestre);
        ruta = findViewById(R.id.txtruta);
        ruta.setText(intent.getStringExtra(RUTA_KEY));
        modulos = intent.getStringExtra(MODULOS_KEY);
        imei = intent.getStringExtra(IMEI_KEY);
        idnotaslectura = findViewById((R.id.idnotaslectura));

        try {
            int consecutivo = Integer.parseInt(intent.getStringExtra(CONSECUTIVO));
            if (consecutivo != 0) {
                count = consecutivo;
            }
        } catch (Exception e) {
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try {
            file.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //limpia fotos con size 0
        String files;
        File folder = new File(ruta_fotos);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    files = listOfFiles[i].getName();
                    File arch = new File(ruta_fotos + files);
                    if (arch.length() == 0) {
                        arch.delete();
                    }
                }
            }
        }

        locationHelper=new LocationHelper(this);
        locationHelper.checkpermission();

        // check availability of play services
        if (locationHelper.checkPlayServices()) {

            // Building the GoogleApi client
            locationHelper.buildGoogleApiClient();
        }

        ListaNotasAsync lasync = new ListaNotasAsync();
        try{
            lista = lasync.execute().get();
        }catch (Exception e){
            e.printStackTrace();
        }

        ArrayList<String> listado = new ArrayList<String>();
        listado.add(0,"Seleccione Nota...");

        if(lista.size()>0) {
            for (NotasLecturaVO n : lista) {
                listado.addAll(n.getIdnotalectura().intValue(), Collections.singleton(n.getCodigo().trim() + "-" + n.getDescripcion().trim()));
            }
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getApplicationContext(),  android.R.layout.simple_spinner_dropdown_item, listado);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        idnotaslectura.setAdapter(adapter);

    }

    //manda llamar activity de lector QR
    public void sendLectorQR(View v) {
        //Intent intent = getIntent();
        Intent intent = new Intent(InicioLecturasActivity.this, QrCodeActivity.class);

        count = 0;
        // Se instancia un objeto de la clase IntentIntegrator
        //IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        // Se procede con el proceso de scaneo
        intent.putExtra("requestCode", REQUEST_CAPTURE_QR);
        //scanIntegrator.initiateScan();
        startActivityForResult(intent, REQUEST_CAPTURE_QR);

    }

    //abre la camara para tomar fotografia y regresa a este mismo activity
    @TargetApi(Build.VERSION_CODES.M)
    public void sendFotografia(View v) {

        if (ActivityCompat.checkSelfPermission(InicioLecturasActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(InicioLecturasActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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

    //graba la informacion de ejecutivo, manzana y geolocalizacion
    public void sendGrabar(View v) {
        String qr = datoqr.getText().toString().trim();
        //String bim = bimestre.getText().toString().trim();
        //String med = medidor.getText().toString().trim();
        String ru = ruta.getText().toString().trim();

        if (TextUtils.isEmpty(ru) || TextUtils.isEmpty(qr)) {
            Toast.makeText(getApplicationContext(), "Es necesario llenar todos los campos", Toast.LENGTH_LONG).show();
        } else {
            /*
            if (Integer.parseInt(bim) > 6 || Integer.parseInt(bim) == 0) {
                Toast.makeText(getApplicationContext(), "El bimestre debe estar entre 1 y 6", Toast.LENGTH_LONG).show();
                return;
            }
            */

            /*Location loc;
            if (!checkLocation())
                return;
            if (ActivityCompat.checkSelfPermission(InicioLecturasActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(InicioLecturasActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(InicioLecturasActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(true);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                String provider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(
                        provider, 20*1000, 5, locationListenerGPS);
                if (locationManager != null) {
                    loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc != null) {
                        lon = loc.getLongitude();
                        lat = loc.getLatitude();
                        locationManager.removeUpdates(locationListenerGPS);
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

                    /*if (lat == 0 && lon == 0) {
                        if (!checkLocation())
                            return;
                        if (ActivityCompat.checkSelfPermission(InicioLecturasActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(InicioLecturasActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(InicioLecturasActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        } else {
                            Criteria criteria = new Criteria();
                            criteria.setAccuracy(Criteria.ACCURACY_FINE);
                            criteria.setAltitudeRequired(false);
                            criteria.setBearingRequired(false);
                            criteria.setCostAllowed(true);
                            criteria.setPowerRequirement(Criteria.POWER_LOW);
                            String provider = locationManager.getBestProvider(criteria, true);
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
                    values.put(DbCentralContract.LecturasEntry.MANZANA, datoqr.getText().toString());
                    values.put(DbCentralContract.LecturasEntry.FECHA_REGISTRO, fecha);
                    values.put(DbCentralContract.LecturasEntry.HORA_REGISTRO, hora);
                    values.put(DbCentralContract.LecturasEntry.EJECUTIVO, ejec);
                    values.put(DbCentralContract.LecturasEntry.LATITUD, lat);
                    values.put(DbCentralContract.LecturasEntry.LONGITUD, lon);
                    values.put(DbCentralContract.LecturasEntry.MEDIDOR, TextUtils.isEmpty(medidor.getText())?"0":medidor.getText().toString().trim());
                    values.put(DbCentralContract.LecturasEntry.PROCESO, 2);
                    values.put(DbCentralContract.LecturasEntry.LECTURA, TextUtils.isEmpty(consumo.getText())?"0": consumo.getText().toString().trim());
                    values.put(DbCentralContract.LecturasEntry.BIMESTRE, 0);
                    values.put(DbCentralContract.LecturasEntry.RUTA, ruta.getText().toString());
                    values.put(DbCentralContract.LecturasEntry.IMEI, imei);
                    values.put(DbCentralContract.LecturasEntry.NOTASLECTURA, idnotaslectura.getSelectedItemId()!=0?idnotaslectura.getSelectedItemId():0);

                    idreturn = dbHelper.saveLecturas(values);
                    dbHelper.close();

                    dbHelper = new DbCentralDBHelper(this);
                    values = new ContentValues();
                    values.put(DbCentralContract.EjecutivosEntry.EJECUTIVO, ejec);
                    values.put(DbCentralContract.EjecutivosEntry.CONSECUTIVO, count);
                    dbHelper.saveEjecutivos(values);
                    dbHelper.close();

                    Toast.makeText(InicioLecturasActivity.this, "Registro Guardadado satisfactoriamente.", Toast.LENGTH_LONG).show();

                    //si existe señal envia datos al server
                    if (Utilities.CheckInternetConnection(this)) {
                        lecturas = new ArrayList<>();
                        LecturasVO l = new LecturasVO();
                        l.setRuta(ruta.getText().toString());
                        l.setMedidor(TextUtils.isEmpty(medidor.getText())?"0":medidor.getText().toString().trim());
                        l.setLectura(Long.parseLong(TextUtils.isEmpty(consumo.getText())?"0": consumo.getText().toString().trim()));
                        l.setBimestre("0");
                        l.setEjecutivo(ejec);
                        l.setFecharegistro(fecha);
                        l.setHoraregistro(hora);
                        l.setImei(imei);
                        l.setLatitud(lat);
                        l.setLongitud(lon);
                        l.setManzana(datoqr.getText().toString());
                        l.setProceso(2);
                        l.setIdnotalectura(idnotaslectura.getSelectedItemId()!=0?idnotaslectura.getSelectedItemId():0);

                        lecturas.add(l);

                        try {
                            new ReporteCampoWSAsync().execute(0);
                        } catch (Exception e) {
                        }

                        count = 0;
                        consumo = findViewById(R.id.txtconsumo);
                        consumo.setText("");
                        medidor = findViewById(R.id.txtmedidor);
                        medidor.setText("");
                        idnotaslectura.setSelection(0);
                        //bimestre = findViewById(R.id.txtbimestre);
                        //bimestre.setText("");
                        datoqr = findViewById(R.id.txtInicioManzanaCuenta);
                        datoqr.setText("");
                        lat = 0;
                        lon = 0;
                        //locationManager.removeUpdates(locationListenerGPS);
                        locationHelper.connectApiClient();
                        Toast.makeText(InicioLecturasActivity.this, "Registro grabado satisfactoriamente", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(InicioLecturasActivity.this, "Es necesario al menos tomar una fotografía.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(InicioLecturasActivity.this, "Es necesario al menos tomar una fotografía.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void regresar(View v) {
        Intent inicio = new Intent(getApplicationContext(), TomaLecturasActivity.class);
        ejecutivo = findViewById(R.id.txtEjecutivo);
        ejec = ejecutivo.getText().toString();

        inicio.putExtra(EJECUTIVO_KEY, ejec);
        inicio.putExtra(MODULOS_KEY, modulos);
        inicio.putExtra(IMEI_KEY, imei);
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
                if (ActivityCompat.checkSelfPermission(InicioLecturasActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(InicioLecturasActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
                    //Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                    //Guarda imagen
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                    //limpia fotos con size 0
                    String files;
                    File folder = new File(ruta_fotos);
                    File[] listOfFiles = folder.listFiles();
                    if (listOfFiles != null) {
                        for (int i = 0; i < listOfFiles.length; i++) {
                            if (listOfFiles[i].isFile()) {
                                files = listOfFiles[i].getName();
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

            if (location != null) {
                lon = location.getLongitude();
                lat = location.getLatitude();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("Longitud", String.valueOf(lon));
                    Log.e("Latitud", String.valueOf(lat));
                    //Toast.makeText(InicioBoletasActivity.this, "Localización GPS obtenida", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            isLocationEnabled();
        }

        @Override
        public void onProviderEnabled(String s) {
            Location loc;
            if (!checkLocation())
                return;
            if (ActivityCompat.checkSelfPermission(InicioLecturasActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(InicioLecturasActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(InicioLecturasActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(true);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                String provider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(
                        provider, 1000, 0, locationListenerGPS);
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

            //limpia fotos con size 0
            String files;
            File folder = new File(ruta_fotos);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        files = listOfFiles[i].getName();
                        File archivo = new File(files);
                        if (archivo.length() == 0) {
                            archivo.delete();
                        }
                    }
                }
            }

            //Retorna a la actividad
            startActivityForResult(cameraIntent, REQUEST_CAPTURE_IMAGE);

        }

        if (requestCode == REQUEST_CAPTURE_QR && resultCode == Activity.RESULT_OK) {
            //IntentResult scanningResult = IntentIntegrator.parseActivityResult(
            //        requestCode, resultCode, data);
            //if (scanningResult != null) {
            if(data!=null){
                // Quiere decir que se obtuvo resultado pro lo tanto:
                // Desplegamos en pantalla el contenido del codigo de barra scaneado
                //String scanContent = scanningResult.getContents();
                String lectura = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                datoqr.setText(lectura);

                //startActivityForResult(data, REQUEST_CAPTURE_QR);
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
            final String METHOD_NAME = "ReportaActividadLecturas";
            String SOAP_ACTION = "http://tempuri.org/IServiceStrategos/ReportaActividadLecturas";
            int resultado_xml = 0;
            SoapObject result = null;
            //Integer count = 1;

            try {
                Thread.sleep(1000);

                //envia informacion de la base de datos
                for (LecturasVO n : lecturas) {
                    SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                    try {

                        request.addProperty("manzana", n.getManzana());
                        request.addProperty("bimestre", n.getBimestre());
                        request.addProperty("fecharegistro", n.getFecharegistro() + " " + n.getHoraregistro());
                        request.addProperty("idejecutivo", n.getEjecutivo());
                        request.addProperty("latitud", String.valueOf(n.getLatitud()));
                        request.addProperty("longitud", String.valueOf(n.getLongitud()));
                        request.addProperty("imei", imei);
                        request.addProperty("idproceso", 2);
                        request.addProperty("ruta", n.getRuta());
                        request.addProperty("lectura", n.getLectura());
                        request.addProperty("medidor", n.getMedidor());
                        request.addProperty("idnotalecturas",n.getIdnotalectura());

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
                            Log.e("Exception ", e.getMessage());
                            e.printStackTrace();
                        }

                        resultado_xml = Integer.parseInt(result.getProperty(0).toString());
                        if (resultado_xml == 1) {
                            resul = true;
                            DbCentralDBHelper db = new DbCentralDBHelper(getApplicationContext());
                            db.deleteLecturas(idreturn);
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

    public class ListaNotasAsync extends AsyncTask<Void, Void, ArrayList<NotasLecturaVO>> {

        @Override
        protected ArrayList<NotasLecturaVO> doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                //return false;
            }

            final String NAMESPACE = "http://tempuri.org/";
            String METHOD_NAME = "ListaNotas";
            String SOAP_ACTION = "http://tempuri.org/IServiceStrategos/ListaNotas";
            SoapObject result;

            try {
                Thread.sleep(1000);
                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                try {

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.implicitTypes = true;
                    envelope.setOutputSoapObject(request);
                    envelope.dotNet = true;

                    HttpTransportSE httpTransport = new HttpTransportSE(urlString);
                    httpTransport.debug = true;

                    try {
                        httpTransport.call(SOAP_ACTION, envelope);
                        result = (SoapObject) envelope.bodyIn;
                        Log.i("RESPONSE ListaNotas", String.valueOf(result));
                        //String[] separa = result.getProperty(0).toString().split(";");

                        SoapObject obj2 =(SoapObject) result.getProperty(0);

                        for(int i=0; i<obj2.getPropertyCount(); i++)
                        {
                            SoapObject obj3 =(SoapObject) obj2.getProperty(i);
                            NotasLecturaVO notas = new NotasLecturaVO();
                            notas.setIdnotalectura(Long.parseLong(obj3.getProperty(3).toString()));
                            notas.setCodigo(obj3.getProperty(1).toString());
                            notas.setDescripcion(obj3.getProperty(2).toString());
                            lista.add(notas);
                        }

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

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO: register the new account here.
            return lista;
        }

        @Override
        protected void onPostExecute(ArrayList<NotasLecturaVO> result) {
            Log.i("Result onPostExecute: ", result.toString());
            //lista = result;
        }

    }
}
