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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralContract;
import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralDBHelper;
import jpcasillas.gdl.jal.mx.strategosmx.models.ClavesOrdenServicioVO;
import jpcasillas.gdl.jal.mx.strategosmx.models.OrdenServicioVO;
import jpcasillas.gdl.jal.mx.strategosmx.qrReader.IntentIntegrator;
import jpcasillas.gdl.jal.mx.strategosmx.qrReader.IntentResult;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.LocationHelper;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.Utilities;

import com.blikoon.qrcodescanner.QrCodeActivity;

public class InicioOrdenServicioActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    TextView ejecutivo, ordenservicio, claveservicio;
    public final static String EJECUTIVO_KEY = "ejecutivo_key";
    public final static String CONSECUTIVO = "consecutivo_key";
    public final static String PROCESO_KEY = "proceso_jey";
    public final static String MODULOS_KEY = "modulos_key";
    public final static String IMEI_KEY = "imei_key";
    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ordenservicio/";
    private File file = new File(ruta_fotos);
    public static int count = 0;
    static String ejec;
    private static final int REQUEST_CAPTURE_IMAGE = 100;
    private static final int REQUEST_CAPTURE_QR = 0x0000c0de;
    double lat, lon;
    String proceso = "104";
    String bimestre = "0";
    boolean resul = false;
    List<OrdenServicioVO> ordenes = new ArrayList<>();
    String modulos;
    String fecha, hora;
    int newWidth = 1500;
    int newHeight = 2500;
    String imei;
    Long idreturn;
    LocationHelper locationHelper;
    private Location mLastLocation;
    final String urlString = "http://strategosmx.com/strategosmxWS/ServiceStrategos.svc";
    ArrayList<ClavesOrdenServicioVO> lista = new ArrayList<ClavesOrdenServicioVO>();
    private int vuelta = 0;
    AutoCompleteTextView claves;
    Long idclaveorden;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setSubtitle("Orden de Servicio - Captura");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_orden_servicio);

        Intent intent = getIntent();
        ejecutivo = findViewById(R.id.txtInicioEjecutivo);
        ejec = intent.getStringExtra(EJECUTIVO_KEY);
        ejecutivo.setText(ejec);

        ordenservicio = findViewById(R.id.txtOrdenServicio);

        modulos = intent.getStringExtra(MODULOS_KEY);
        imei = intent.getStringExtra(IMEI_KEY);

        try {
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

        locationHelper = new LocationHelper(this);
        locationHelper.checkpermission();

        // check availability of play services
        if (locationHelper.checkPlayServices()) {

            // Building the GoogleApi client
            locationHelper.buildGoogleApiClient();
        }

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            int responseCode = urlConnection.getResponseCode();
            urlConnection.disconnect();

            if (responseCode != 200) {
                Toast.makeText(getApplicationContext(), "Intente nuevamente, fallo al intentar conectarse al servidor.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {

        }
        ListaClavesServicioAsync lasync = new ListaClavesServicioAsync();
        try {
            lista = lasync.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<String> listado = new ArrayList<String>();
        listado.add(0, "Seleccione Nota...");

        if (lista.size() > 0) {
            for (ClavesOrdenServicioVO n : lista) {
                //listado.addAll(n.getIdorden().intValue(), Collections.singleton(n.getClave().trim() + "-" + n.getDescripcion().trim()));
                listado.addAll(n.getIdorden().intValue(), Collections.singleton(n.getClave().trim()));
            }
        }
        adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, listado);
        claves = findViewById(R.id.txtClaveServicio);
        claves.setThreshold(1);
        claves.setAdapter(adapter);
        claves.setOnItemClickListener(onItemClickListener);
        //claves.setValidator(new Validator());
        //claves.setOnFocusChangeListener(new FocusListener());
        claves.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (count > 0) {
                    count = 0;
                    idclaveorden = new Long(0);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                count = 0;
                idclaveorden = new Long(0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                count = 0;
                idclaveorden = new Long(0);
            }
        });
    }

    /*class Validator implements AutoCompleteTextView.Validator {

        @Override
        public boolean isValid(CharSequence text) {
            Log.v("Test", "Checking if valid: "+ text);

            String str = claves.getText().toString();
            ListAdapter listAdapter = claves.getAdapter();
            for(int i = 0; i < listAdapter.getCount(); i++) {
                String temp = listAdapter.getItem(i).toString();
                if(str.compareTo(temp ) == 0)
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public CharSequence fixText(CharSequence invalidText) {
            Log.v("Test", "Returning fixed text");
            return "";
        }
    }

    class FocusListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Log.v("Test", "Focus changed");
            if (v.getId() == R.id.txtClaveServicio && !hasFocus) {
                Log.v("Test", "Performing validation");
                ((AutoCompleteTextView)v).performValidation();
            }
        }
    }*/

    private AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    idclaveorden = lista.get(i).getIdorden();

                    /*Toast.makeText(InicioOrdenServicioActivity.this,
                            "Clicked item from auto completion list "
                                    + adapterView.getItemAtPosition(i)+idclaveorden
                            , Toast.LENGTH_SHORT).show();*/
                }
            };


    //manda llamar activity de lector QR
    public void sendLectorQR(View v) {
        //Intent intent = getIntent();
        Intent intent = new Intent(InicioOrdenServicioActivity.this, QrCodeActivity.class);

        count = 0;
        // Se instancia un objeto de la clase IntentIntegrator
        //IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        // Se procede con el proceso de scaneo
        //intent.putExtra("requestCode", REQUEST_CAPTURE_QR);
        //scanIntegrator.initiateScan();
        intent.putExtra(PROCESO_KEY, proceso);
        intent.putExtra(EJECUTIVO_KEY, ejec);
        startActivityForResult(intent, REQUEST_CAPTURE_QR);
    }

    //fotografia
    @TargetApi(Build.VERSION_CODES.M)
    public void sendFotografia(View v) {

        if (ActivityCompat.checkSelfPermission(InicioOrdenServicioActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(InicioOrdenServicioActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (idclaveorden != 0) {
                count++;
                claveservicio = findViewById(R.id.txtClaveServicio);
                String manzana = claveservicio.getText().toString();
                String orden = ordenservicio.getText().toString();
                String file = ruta_fotos + getCode(manzana, count, orden) + ".jpg";
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
                Toast.makeText(InicioOrdenServicioActivity.this, "Es necesario seleccionar una clave de servicio de la lista", Toast.LENGTH_LONG).show();
                return;
            }

        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(getApplicationContext(), "Permission Needed.", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    public void regresar(View v) {
        Intent inicio = new Intent(getApplicationContext(), OrdenServicioActivity.class);
        ejecutivo = findViewById(R.id.txtInicioEjecutivo);
        ejec = ejecutivo.getText().toString();
        inicio.putExtra(EJECUTIVO_KEY, ejec);
        inicio.putExtra(PROCESO_KEY, proceso);
        inicio.putExtra(MODULOS_KEY, modulos);
        inicio.putExtra(IMEI_KEY, imei);
        startActivity(inicio);
        finish();
    }

    private String getCode(String manzana, int count, String orden) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM", Locale.US);
        String date = dateFormat.format(new Date());

        try {
            vuelta = new VueltaAsync().execute().get();
        } catch (Exception e) {
        }
        return orden + manzana + vuelta + "_" + count + "_" + date;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(InicioOrdenServicioActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(InicioOrdenServicioActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    count++;
                    claveservicio = findViewById(R.id.txtClaveServicio);
                    String manzana = claveservicio.getText().toString();
                    String orden = ordenservicio.getText().toString();
                    String file = ruta_fotos + getCode(manzana, count, orden) + ".jpg";
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationHelper.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {

            // get new image here like this
            count++;
            claveservicio = findViewById(R.id.txtClaveServicio);
            String manzana = claveservicio.getText().toString();
            String orden = ordenservicio.getText().toString();
            String file = ruta_fotos + getCode(manzana, count, orden) + ".jpg";
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

        if (requestCode == REQUEST_CAPTURE_QR && resultCode == Activity.RESULT_OK) {
            /*
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(
                    requestCode, resultCode, data);
            if (scanningResult != null) {
                String scanContent = scanningResult.getContents();
                ordenservicio.setText(scanContent);

            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No se ha recibido datos del scaneo!", Toast.LENGTH_SHORT);
                toast.show();
            }
            */
            if (data != null) {
                String lectura = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                ordenservicio.setText(lectura);

            }else{
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastLocation = locationHelper.getLocation();
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
        protected Boolean doInBackground(Integer... params) {

            final String NAMESPACE = "http://tempuri.org/";
            final String URL = "http://strategosmx.com/strategosmxWS/ServiceStrategos.svc";
            final String METHOD_NAME = "ReportaActividadServicio";
            String SOAP_ACTION = "http://tempuri.org/IServiceStrategos/ReportaActividadServicio";
            int resultado_xml = 0;
            SoapObject result = null;
            //Integer count = 1;

            try {
                Thread.sleep(1000);

                //envia informacion de la base de datos
                for (OrdenServicioVO n : ordenes) {
                    SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                    try {

                        request.addProperty("manzana", n.getManzana());
                        request.addProperty("bimestre", n.getBimestre());
                        request.addProperty("fecharegistro", n.getFecharegistro() + " " + n.getHoraregistro());
                        request.addProperty("idejecutivo", n.getEjecutivo());
                        request.addProperty("latitud", String.valueOf(n.getLatitud()));
                        request.addProperty("longitud", String.valueOf(n.getLongitud()));
                        request.addProperty("imei", n.getImei());
                        request.addProperty("idproceso", 5);
                        request.addProperty("ordenservicio", n.getOrdenservicio());
                        request.addProperty("vuelta", n.getVuelta());
                        request.addProperty("idclaveorden", n.getIdclaveorden());

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
                        Log.i("Resultado XML ", String.valueOf(resultado_xml));
                        if (resultado_xml == 1) {
                            resul = true;
                            DbCentralDBHelper db = new DbCentralDBHelper(getApplicationContext());
                            db.deleteOrdenServicio(idreturn);
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

    public void sendGrabarCampo(View v) {

        String qr = ordenservicio.getText().toString().trim();

        if (!TextUtils.isEmpty(qr) || idclaveorden != null) {

            mLastLocation = locationHelper.getLocation();

            if (mLastLocation != null) {
                lat = mLastLocation.getLatitude();
                lon = mLastLocation.getLongitude();
            } else {
                Toast.makeText(this, "Nó fue posible obtener la ubicación favor de habilitar el GPS", Toast.LENGTH_SHORT).show();
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
                    if (a.getName().startsWith(ordenservicio.getText().toString().trim() + claveservicio.getText().toString().trim())) {
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
                                    if (a.getName().startsWith(ordenservicio.getText().toString() + claveservicio.getText().toString())) {
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

                    DbCentralDBHelper dbHelper = new DbCentralDBHelper(this);

                    // Create a new map of values, where column names are the keys
                    Date date = new Date();
                    DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

                    fecha = dateFormat.format(date);
                    hora = hourFormat.format(date);

                    ContentValues values = new ContentValues();
                    values.put(DbCentralContract.OrdenServicioEntry.MANZANA, qr);
                    values.put(DbCentralContract.OrdenServicioEntry.FECHA_REGISTRO, fecha);
                    values.put(DbCentralContract.OrdenServicioEntry.HORA_REGISTRO, hora);
                    values.put(DbCentralContract.OrdenServicioEntry.EJECUTIVO, ejec);
                    values.put(DbCentralContract.OrdenServicioEntry.LATITUD, lat);
                    values.put(DbCentralContract.OrdenServicioEntry.LONGITUD, lon);
                    values.put(DbCentralContract.OrdenServicioEntry.PROCESO, proceso);
                    values.put(DbCentralContract.OrdenServicioEntry.BIMESTRE, 0);
                    values.put(DbCentralContract.OrdenServicioEntry.IMEI, imei);
                    values.put(DbCentralContract.OrdenServicioEntry.ORDENSERVICIO, ordenservicio.getText().toString());
                    values.put(DbCentralContract.OrdenServicioEntry.VUELTA, vuelta);
                    values.put(DbCentralContract.OrdenServicioEntry.IDCLAVEORDEN, idclaveorden);

                    idreturn = dbHelper.saveOrdenServicio(values);
                    dbHelper.close();

                    dbHelper = new DbCentralDBHelper(this);
                    values = new ContentValues();
                    values.put(DbCentralContract.EjecutivosEntry.EJECUTIVO, ejec);
                    values.put(DbCentralContract.EjecutivosEntry.CONSECUTIVO, count);
                    dbHelper.saveEjecutivos(values);
                    dbHelper.close();

                    //si tiene conexion envia informacion al server
                    if (Utilities.CheckInternetConnection(this)) {
                        ordenes = new ArrayList<>();
                        Log.i("enviando datos: ", "true");
                        OrdenServicioVO b = new OrdenServicioVO();
                        b.setImei(imei);
                        b.setProceso(5);
                        b.setBimestre(bimestre);
                        b.setEjecutivo(ejec);
                        b.setFecharegistro(fecha);
                        b.setHoraregistro(hora);
                        b.setLatitud(lat);
                        b.setLongitud(lon);
                        b.setManzana(qr);
                        b.setOrdenservicio(ordenservicio.getText().toString());
                        b.setVuelta(vuelta);
                        b.setIdclaveorden(idclaveorden);

                        ordenes.add(b);
                        try {
                            new InicioOrdenServicioActivity.ReporteCampoWSAsync().execute(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    count = 0;
                    claveservicio = findViewById(R.id.txtClaveServicio);
                    claveservicio.setText("");
                    ordenservicio = findViewById(R.id.txtOrdenServicio);
                    ordenservicio.setText("");
                    lat = 0;
                    lon = 0;
                    locationHelper.connectApiClient();
                    Toast.makeText(InicioOrdenServicioActivity.this, "Registro grabado satisfactoriamente", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(InicioOrdenServicioActivity.this, "Es necesario tomar al menos una fotografía", Toast.LENGTH_LONG).show();
                    return;
                }

            } else {
                Toast.makeText(InicioOrdenServicioActivity.this, "Es necesario tomar al menos una fotografía", Toast.LENGTH_LONG).show();
                return;
            }
        } else {

            Toast.makeText(InicioOrdenServicioActivity.this, "Es necesario seleccionar una clave de servicio de la lista y telcar o scanear una orden", Toast.LENGTH_LONG).show();
            return;
        }
    }

    public class ListaClavesServicioAsync extends AsyncTask<Void, Void, ArrayList<ClavesOrdenServicioVO>> {

        @Override
        protected ArrayList<ClavesOrdenServicioVO> doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                //return false;
            }

            final String NAMESPACE = "http://tempuri.org/";
            String METHOD_NAME = "ListaOrdenes";
            String SOAP_ACTION = "http://tempuri.org/IServiceStrategos/ListaOrdenes";
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
                        Log.i("RESPONSE ListaOrdenes", String.valueOf(result));
                        //String[] separa = result.getProperty(0).toString().split(";");

                        SoapObject obj2 = (SoapObject) result.getProperty(0);

                        for (int i = 0; i < obj2.getPropertyCount(); i++) {
                            SoapObject obj3 = (SoapObject) obj2.getProperty(i);
                            ClavesOrdenServicioVO notas = new ClavesOrdenServicioVO();
                            notas.setIdorden(Long.parseLong(obj3.getProperty(4).toString()));
                            notas.setClave(obj3.getProperty(1).toString());
                            notas.setDescripcion(obj3.getProperty(3).toString());
                            notas.setConcepto(obj3.getProperty(2).toString());
                            notas.setTipo(obj3.getProperty(5).toString());
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

    }

    class VueltaAsync extends AsyncTask<Void, Void, Integer> {

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

                    request.addProperty("orden", ordenservicio.getText().toString().trim());
                    request.addProperty("clave", claveservicio.getText().toString().trim());

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
                    Toast.makeText(InicioOrdenServicioActivity.this, "Problema al consultar informacion." + e.getMessage(), Toast.LENGTH_LONG).show();
                    resul = false;
                    e.printStackTrace();
                }
                //}
            } catch (Exception e) {
                resul = false;
                Toast.makeText(InicioOrdenServicioActivity.this, "Problema al consultar informacion." + e.getMessage(), Toast.LENGTH_LONG).show();
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
