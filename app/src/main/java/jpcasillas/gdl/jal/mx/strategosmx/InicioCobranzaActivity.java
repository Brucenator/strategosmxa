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
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import jpcasillas.gdl.jal.mx.strategosmx.models.CobranzaVO;
import jpcasillas.gdl.jal.mx.strategosmx.qrReader.IntentIntegrator;
import jpcasillas.gdl.jal.mx.strategosmx.qrReader.IntentResult;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.LocationHelper;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.Utilities;

public class InicioCobranzaActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,ActivityCompat.OnRequestPermissionsResultCallback{

    //parametros para pasar y recibir valores
    TextView datoqr, ejecutivo, oficio, ruta, manzana;
    public final static String EJECUTIVO_KEY = "ejecutivo_key";
    public final static String QR_KEY = "qr_key";
    public final static String CONSECUTIVO = "consecutivo_key";
    public final static String PROCESO_KEY = "proceso_jey";
    public final static String MODULOS_KEY = "modulos_key";
    public final static String RUTA_KEY = "ruta_key";
    public final static String IMEI_KEY = "imei_key";
    //para grabar las fotografias
    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/cobranza/";
    private File file = new File(ruta_fotos);
    public static int count = 0;
    static String ejec;
    private static final int REQUEST_CAPTURE_IMAGE = 100;
    private static final int REQUEST_CAPTURE_QR = 0x0000c0de;
    String proceso;
    double lat, lon;
    String imei;
    List<CobranzaVO> cobranza = new ArrayList<>();
    boolean resul = false;
    String modulos;
    String fecha, hora;
    int newWidth = 1500;
    int newHeight = 2500;
    Long idreturn;
    private int vuelta = 0;
    LocationHelper locationHelper;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("StrategosMX - Captura");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_cobranza);

        Intent intent = getIntent();
        ejecutivo = findViewById(R.id.txtEjecutivo);
        ejec = intent.getStringExtra(EJECUTIVO_KEY);
        ejecutivo.setText(ejec);

        proceso = intent.getStringExtra(PROCESO_KEY);

        String lecturaqr = intent.getStringExtra(QR_KEY);
        datoqr = findViewById(R.id.txtOficio);
        datoqr.setText(lecturaqr);

        manzana = findViewById(R.id.txtManzana);

        ruta = findViewById(R.id.txtruta);
        ruta.setText(intent.getStringExtra(RUTA_KEY));
        modulos = intent.getStringExtra(MODULOS_KEY);
        imei = intent.getStringExtra(IMEI_KEY);

        try {
            int consecutivo = Integer.parseInt(intent.getStringExtra(CONSECUTIVO));
            if (consecutivo != 0) {
                count = consecutivo;
            }
        } catch (Exception e) {
            count =0;
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try {
            file.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }

        locationHelper=new LocationHelper(this);
        locationHelper.checkpermission();

        // check availability of play services
        if (locationHelper.checkPlayServices()) {
            // Building the GoogleApi client
            locationHelper.buildGoogleApiClient();
        }

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

        ruta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(count>0){
                    count=0;
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                count =0;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //manda llamar activity de lector QR
    public void sendLectorQR(View v) {
        //Intent intent = getIntent();
        Intent intent = new Intent(InicioCobranzaActivity.this, QrCodeActivity.class);

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

        if (ActivityCompat.checkSelfPermission(InicioCobranzaActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(InicioCobranzaActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            count++;
            datoqr = findViewById(R.id.txtOficio);
            ruta = findViewById(R.id.txtruta);
            String rut = ruta.getText().toString().trim();
            String oficio = datoqr.getText().toString();
            String file = ruta_fotos + getCode(rut, count, oficio) + ".jpg";
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
        String ru = ruta.getText().toString().trim();

        if (TextUtils.isEmpty(ru)) {
            Toast.makeText(getApplicationContext(), "Es necesario llenar todos los campos", Toast.LENGTH_LONG).show();
        } else {

            mLastLocation=locationHelper.getLocation();

            if (mLastLocation != null) {
                lat = mLastLocation.getLatitude();
                lon = mLastLocation.getLongitude();
            } else {
                Toast.makeText(this,"Nó fue posible obtener la ubicación favor de habilitar el GPS",Toast.LENGTH_SHORT).show();
            }

            //limpia fotos con size 0
            //String files;
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
                    if (a.getName().startsWith(ruta.getText().toString().trim() + datoqr.getText().toString())) {
                        tienefoto = true;
                        break;
                    } else {
                        tienefoto = false;
                    }
                }
                if (tienefoto) {

                    DbCentralDBHelper dbHelper = new DbCentralDBHelper(this);

                    // Create a new map of values, where column names are the keys
                    Date date = new Date();
                    DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

                    fecha = dateFormat.format(date);
                    hora = hourFormat.format(date);

                    ContentValues values = new ContentValues();
                    values.put(DbCentralContract.CobranzaEntry.MANZANA, manzana.getText().toString());
                    values.put(DbCentralContract.CobranzaEntry.FECHA_REGISTRO, fecha);
                    values.put(DbCentralContract.CobranzaEntry.HORA_REGISTRO, hora);
                    values.put(DbCentralContract.CobranzaEntry.EJECUTIVO, ejec);
                    values.put(DbCentralContract.CobranzaEntry.LATITUD, lat);
                    values.put(DbCentralContract.CobranzaEntry.LONGITUD, lon);
                    values.put(DbCentralContract.CobranzaEntry.PROCESO, 3);
                    values.put(DbCentralContract.CobranzaEntry.RUTA, ruta.getText().toString());
                    values.put(DbCentralContract.CobranzaEntry.IMEI, imei);
                    values.put(DbCentralContract.CobranzaEntry.VUELTA, vuelta);
                    values.put(DbCentralContract.CobranzaEntry.OFICIO, datoqr.getText().toString().trim());
                    values.put(DbCentralContract.CobranzaEntry.ENVIADO, 0);
                    idreturn = dbHelper.saveCobranza(values);
                    dbHelper.close();

                    dbHelper = new DbCentralDBHelper(this);
                    values = new ContentValues();
                    values.put(DbCentralContract.EjecutivosEntry.EJECUTIVO, ejec);
                    values.put(DbCentralContract.EjecutivosEntry.CONSECUTIVO, count);
                    dbHelper.saveEjecutivos(values);
                    dbHelper.close();

                    Toast.makeText(InicioCobranzaActivity.this, "Registro Guardadado satisfactoriamente.", Toast.LENGTH_LONG).show();

                    //si existe señal envia datos al server
                    if (Utilities.CheckInternetConnection(this)) {
                        cobranza = new ArrayList<>();
                        CobranzaVO l = new CobranzaVO();
                        l.setRuta(ruta.getText().toString());
                        l.setEjecutivo(ejec);
                        l.setFecharegistro(fecha);
                        l.setHoraregistro(hora);
                        l.setImei(imei);
                        l.setLatitud(lat);
                        l.setLongitud(lon);
                        l.setManzana(manzana.getText().toString().trim());
                        l.setProceso(3);
                        l.setVuelta(vuelta);
                        l.setOficio(datoqr.getText().toString());

                        cobranza.add(l);

                        try {
                            new ReporteCampoWSAsync().execute(0);
                        } catch (Exception e) {
                            Toast.makeText(InicioCobranzaActivity.this, "Problema al intentar enviar informacion." +e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                        count = 0;
                        vuelta = 1;
                        manzana.setText("");
                        datoqr.setText("");
                        lat = 0;
                        lon = 0;
                        //locationManager.removeUpdates(locationListenerGPS);
                        locationHelper.connectApiClient();
                        Toast.makeText(InicioCobranzaActivity.this, "Registro grabado satisfactoriamente", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(InicioCobranzaActivity.this, "Es necesario al menos tomar una fotografía.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(InicioCobranzaActivity.this, "Es necesario al menos tomar una fotografía.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void regresar(View v) {
        Intent inicio = new Intent(getApplicationContext(), CobranzaActivity.class);
        ejecutivo = findViewById(R.id.txtEjecutivo);
        ejec = ejecutivo.getText().toString();

        inicio.putExtra(EJECUTIVO_KEY, ejec);
        inicio.putExtra(MODULOS_KEY, modulos);
        inicio.putExtra(IMEI_KEY, imei);
        startActivity(inicio);
        finish();
    }


    private String getCode(String ruta, int count, String oficio) {
        try {
            vuelta = new VueltaOficioAsync().execute().get();

        } catch (Exception e) {

        }
        return ruta + oficio + "_" + vuelta + "_" + count;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationHelper.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(InicioCobranzaActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(InicioCobranzaActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    count++;
                    datoqr = findViewById(R.id.txtOficio);
                    ruta = findViewById(R.id.txtruta);
                    String rut = ruta.getText().toString().trim();
                    String oficio = datoqr.getText().toString();
                    String file = ruta_fotos + getCode(rut, count, oficio) + ".jpg";
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationHelper.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {

            // get new image here like this
            count++;
            datoqr = findViewById(R.id.txtOficio);
            ruta = findViewById(R.id.txtruta);
            String rut = ruta.getText().toString().trim();
            String oficio = datoqr.getText().toString();
            String file = ruta_fotos + getCode(rut, count, oficio) + ".jpg";
            File mi_foto = new File(file);
            try {
                mi_foto.createNewFile();
            } catch (IOException ex) {
                Toast.makeText(InicioCobranzaActivity.this, "Problema al guardar fotografía."+ ex.getMessage(), Toast.LENGTH_LONG).show();
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
                        }else{
                            //comprime foto
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
                                Toast.makeText(getApplicationContext(), "Fotografía guardada.", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
            if(data != null) {
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
            final String METHOD_NAME = "ReportaActividadCobranza";
            String SOAP_ACTION = "http://tempuri.org/IServiceStrategos/ReportaActividadCobranza";
            int resultado_xml = 0;
            SoapObject result = null;
            //Integer count = 1;

            try {
                Thread.sleep(1000);

                //envia informacion de la base de datos
                for (CobranzaVO n : cobranza) {
                    SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                    try {

                        request.addProperty("manzana", n.getManzana());
                        request.addProperty("fecharegistro", n.getFecharegistro() + " " + n.getHoraregistro());
                        request.addProperty("idejecutivo", n.getEjecutivo());
                        request.addProperty("latitud", String.valueOf(n.getLatitud()));
                        request.addProperty("longitud", String.valueOf(n.getLongitud()));
                        request.addProperty("imei", imei);
                        request.addProperty("idproceso", 3);
                        request.addProperty("ruta", n.getRuta());
                        request.addProperty("oficio", n.getOficio());
                        request.addProperty("vuelta", n.getVuelta());

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
                            ContentValues values = new ContentValues();
                            values.put(DbCentralContract.CobranzaEntry.IDCOBRANZA, idreturn);
                            values.put(DbCentralContract.CobranzaEntry.ENVIADO, 1);
                            db.UpdateCobranza(values);
                            db.close();
                        }

                    } catch (Exception e) {
                        resul = false;
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                resul = false;
                Toast.makeText(InicioCobranzaActivity.this, "Problema al descargar informacion."+e.getMessage(), Toast.LENGTH_LONG).show();
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

    class VueltaOficioAsync extends AsyncTask<Void, Void, Integer> {

        @Override
        @TargetApi(26)
        protected Integer doInBackground(Void... params) {

            final String NAMESPACE = "http://tempuri.org/";
            final String URL = "http://strategosmx.com/strategosmxWS/ServiceStrategos.svc";
            final String METHOD_NAME = "VueltaOficio";
            String SOAP_ACTION = "http://tempuri.org/IServiceStrategos/VueltaOficio";
            SoapObject result = null;

            try {
                Thread.sleep(1000);

                //envia informacion de la base de datos
                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                try {

                    request.addProperty("oficio", datoqr.getText().toString().trim());
                    request.addProperty("ruta", ruta.getText().toString().trim());

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

                    vuelta = Integer.parseInt(result.getProperty(0).toString());

                } catch (Exception e) {
                    Toast.makeText(InicioCobranzaActivity.this, "Problema al descargar informacion."+e.getMessage(), Toast.LENGTH_LONG).show();
                    resul = false;
                    e.printStackTrace();
                }
                //}
            } catch (Exception e) {
                resul = false;
                Toast.makeText(InicioCobranzaActivity.this, "Problema al descargar informacion."+e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            return vuelta;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.i("Result onPostExecute: ", result.toString());
            vuelta = result;
        }
    }
}
