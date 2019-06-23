package jpcasillas.gdl.jal.mx.strategosmx;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.kobjects.base64.Base64;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralDBHelper;
import jpcasillas.gdl.jal.mx.strategosmx.models.CensoVO;
import jpcasillas.gdl.jal.mx.strategosmx.qrReader.IntentIntegrator;
import jpcasillas.gdl.jal.mx.strategosmx.qrReader.IntentResult;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.CopiarArchivos;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.LocationHelper;

public class CensoActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,ActivityCompat.OnRequestPermissionsResultCallback {

    Button btnDescarga, btnInicio, btnRegresar;
    TextView ejecutivo;
    public final static String EJECUTIVO_KEY = "ejecutivo_key";
    public final static String PROCESO_KEY = "proceso_key";
    public final static String MODULOS_KEY = "modulos_key";
    public final static String IMEI_KEY = "imei_key";
    public final static String QR = "qr";
    final static String urlString = "http://strategosmx.com/strategosmxWS/ServiceStrategos.svc";
    //LocationManager locationManager;
    String proceso = "103";
    String imei;
    List<CensoVO> censo;
    List<CensoVO> reportecampocenso;
    boolean resul = false;
    ProgressDialog progressDialog;
    String modulos;
    String ejec;
    private int count;
    LocationHelper locationHelper;
    private static final int REQUEST_CAPTURE_QR = 0x0000c0de;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setSubtitle("Censo");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_censo);

        btnDescarga = findViewById(R.id.btnDescarga);
        btnInicio = findViewById(R.id.btnCaptura);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnDescarga.setOnClickListener(this);
        btnInicio.setOnClickListener(this);
        btnRegresar.setOnClickListener(this);

        Intent intent = getIntent();
        ejecutivo = findViewById(R.id.idEjecutivo);
        ejec = intent.getStringExtra(EJECUTIVO_KEY);
        ejecutivo.setText(ejec);
        ejecutivo.setEnabled(false);

        modulos = intent.getStringExtra(MODULOS_KEY);
        imei = intent.getStringExtra(IMEI_KEY);
        proceso = intent.getStringExtra(PROCESO_KEY);

        locationHelper = new LocationHelper(this);
        locationHelper.checkpermission();

        // check availability of play services
        if (locationHelper.checkPlayServices()) {
            // Building the GoogleApi client
            locationHelper.buildGoogleApiClient();
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnDescarga:
                try {
                    DescargaInformacionCampo();
                    this.btnDescarga.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnCaptura:

                //ejecutivo = findViewById(R.id.idEjecutivo);
                //message = ejecutivo.getText().toString().trim();

                if (!TextUtils.isEmpty(ejec)) {
                    //startActivity(new Intent(CobranzaActivity.this, InicioCobranzaActivity.class).putExtra(EJECUTIVO_KEY, message).putExtra(LATITUD_KEY, lat).putExtra(LONGITUD_KEY, lon).putExtra(MODULOS_KEY, modulos).putExtra(IMEI_KEY, imei));
                    sendLectorQR();
                    //startActivity(new Intent(CensoActivity.this, InicioCensoActivity.class).putExtra(EJECUTIVO_KEY, message).putExtra(MODULOS_KEY, modulos).putExtra(IMEI_KEY, imei));
                    //finish();
                } else {
                    Toast.makeText(CensoActivity.this, "Es necesario teclear su numero de Ejecutivo, por favor", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case R.id.btnRegresar:
                Intent inicio = new Intent(getApplicationContext(), MainActivity.class);
                ejecutivo = findViewById(R.id.idEjecutivo);
                ejec = ejecutivo.getText().toString().trim();
                inicio.putExtra(EJECUTIVO_KEY, ejec);
                inicio.putExtra(MODULOS_KEY, modulos);
                inicio.putExtra(IMEI_KEY, imei);
                startActivity(inicio);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed(); commented this line in order to disable back press
        //Write your code here
        Toast.makeText(getApplicationContext(), "Click en botón salir del módulo.", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            super.onKeyDown(keyCode, event);
            return true;
        }
        return false;
    }

    private void DescargaInformacionCampo() {

        try {

            if (!Patterns.WEB_URL.matcher(urlString).matches()) {
                Toast.makeText(getApplicationContext(), "Intente nuevamente, fallo al intentar conectarse al servidor.", Toast.LENGTH_LONG).show();
                return;
            }

            DbCentralDBHelper dbcentral = new DbCentralDBHelper(this);
            censo = dbcentral.getCenso();

            reportecampocenso = dbcentral.getReporteCampoCenso();

            Intent intent = getIntent();
            try {
                proceso = intent.getStringExtra(PROCESO_KEY);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/censo/";

            File folder = new File(directorio);
            File[] listOfFiles = folder.listFiles();

            if (censo.size() > 0 || listOfFiles.length > 0) {
                new ReporteCampoWSAsync().execute(listOfFiles.length);
            } else {
                Toast.makeText(this, "No existe información para descargar.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    class ReporteCampoWSAsync extends AsyncTask<Integer, Void, Boolean> {

        @Override
        @TargetApi(26)
        protected Boolean doInBackground(Integer... params) {

            final String NAMESPACE = "http://tempuri.org/";
            final String URL = urlString;
            final String METHOD_NAME = "ReportaActividadCenso";
            String SOAP_ACTION = "http://tempuri.org/IServiceStrategos/ReportaActividadCenso";
            int resultado_xml;
            SoapObject result = null;
            Integer count = 1;

            try {
                Thread.sleep(1000);

                java.net.URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url
                        .openConnection();
                int responseCode = urlConnection.getResponseCode();
                urlConnection.disconnect();

                if (responseCode != 200) {
                    Toast.makeText(getApplicationContext(), "Intente nuevamente, fallo al intentar conectarse al servidor.", Toast.LENGTH_LONG).show();
                }else {

                    //envia informacion de la base de datos
                    //Reporte campo Censo
                    for (CensoVO n : reportecampocenso) {
                        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                        try {

                            request.addProperty("cuenta", n.getCuenta());
                            request.addProperty("calle", n.getCalle());
                            request.addProperty("exterior", n.getExterior());
                            request.addProperty("interior", n.getInterior());
                            request.addProperty("colonia", n.getColonia());
                            request.addProperty("codigopostal", n.getCodigopostal());
                            request.addProperty("uso", n.getUso());
                            request.addProperty("viviendas", n.getViviendas());
                            request.addProperty("locales", n.getLocales());
                            request.addProperty("diamtoma", n.getDiamtoma());
                            request.addProperty("seriemedidor", n.getSeriemedidor());
                            request.addProperty("marcamedidor", n.getMarcamedidor());
                            request.addProperty("diammedidor", n.getDiammedidor());
                            request.addProperty("tomas", n.getTomas());
                            request.addProperty("idrepcamcenso", n.getIdrepcamcenso());
                            request.addProperty("observaciones", n.getObservaciones());
                            request.addProperty("fecharegistro", n.getFecharegistro() + " " + n.getHoraregistro());
                            request.addProperty("idejecutivo", n.getEjecutivo());
                            request.addProperty("latitud", String.valueOf(n.getLatitud()));
                            request.addProperty("longitud", String.valueOf(n.getLongitud()));
                            request.addProperty("imei", imei);
                            request.addProperty("idproceso", 4);

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
                            if (resultado_xml == 1) {
                                resul = true;
                            }

                        } catch (Exception e) {
                            resul = false;
                            e.printStackTrace();
                        }
                    }

                    //envia informacion de la base de datos
                    //Censo

                    for (CensoVO n : censo) {
                        SoapObject request = new SoapObject(NAMESPACE, "Censo");
                        try {

                            request.addProperty("cuenta", n.getCuenta());
                            request.addProperty("calle", n.getCalle());
                            request.addProperty("exterior", n.getExterior());
                            request.addProperty("interior", n.getInterior());
                            request.addProperty("colonia", n.getColonia());
                            request.addProperty("codigopostal", n.getCodigopostal());
                            request.addProperty("uso", n.getUso());
                            request.addProperty("viviendas", n.getViviendas());
                            request.addProperty("locales", n.getLocales());
                            request.addProperty("diamtoma", n.getDiamtoma());
                            request.addProperty("seriemedidor", n.getSeriemedidor());
                            request.addProperty("marcamedidor", n.getMarcamedidor());
                            request.addProperty("diammedidor", n.getDiammedidor());
                            request.addProperty("tomas", n.getTomas());
                            request.addProperty("idrepcamcenso", n.getIdrepcamcenso());

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
                            if (resultado_xml == 1) {
                                resul = true;
                            }

                        } catch (Exception e) {
                            resul = false;
                            e.printStackTrace();
                        }
                    }

                    //envia fotos
                    for (; count <= params[0]; count++) {
                        String directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/censo/";

                        String files;
                        File folder = new File(directorio);
                        File[] listOfFiles = folder.listFiles();
                        final String METHOD_NAME_FOTOS = "DescargaArchivos";
                        SOAP_ACTION = "http://tempuri.org/IServiceStrategos/DescargaArchivos";
                        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                        result = null;

                        for (File a : listOfFiles) {
                            if (a.isFile()) {
                                files = a.getName();
                                //File arch = new File(directorio + files);
                                if (a.length() == 0) {
                                    a.delete();
                                } else {
                                    int size = (int) a.length();
                                    byte[] bytes = new byte[size];

                                    try {
                                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(a));
                                        buf.read(bytes, 0, bytes.length);
                                        buf.close();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    if (files.endsWith(".jpg") || files.endsWith(".JPG")) {
                                        Log.d("Archivo: ", files);

                                        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME_FOTOS);
                                        request.addProperty("archivo", Base64.encode(bytes));
                                        request.addProperty("nombre", files);
                                        request.addProperty("proceso", "103");

                                        new MarshalBase64().register(envelope);
                                        envelope.implicitTypes = true;
                                        envelope.dotNet = true;
                                        envelope.setOutputSoapObject(request);

                                        HttpTransportSE httpTransport = new HttpTransportSE(URL);
                                        httpTransport.debug = true;

                                        try {
                                            httpTransport.call(SOAP_ACTION, envelope);
                                            Log.i("in: ", envelope.bodyIn.toString());
                                            //SoapPrimitive resultado_xml = (SoapPrimitive) envelope.getResponse();
                                            result = (SoapObject) envelope.bodyIn;
                                            Log.i("RESPONSE FOTOS", String.valueOf(result)); // see output in the console
                                        } catch (HttpResponseException e) {
                                            Log.e("HTTPLOG", e.getMessage());
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            Log.e("IOLOG", e.getMessage());
                                            e.printStackTrace();
                                        } catch (XmlPullParserException e) {
                                            Log.e("XMLLOG", e.getMessage());
                                            e.printStackTrace();
                                        } catch (Exception e) {
                                            Log.e("ERROR GRAL", e.getMessage());
                                            e.printStackTrace();
                                            //send request
                                        }

                                        Boolean resultado = Boolean.parseBoolean(result.getProperty(0).toString());
                                        Log.i("Resultado foto: ", resultado.toString());
                                        if (resultado) {
                                            resul = true;
                                            folder = new File(directorio+"backup");
                                            folder.mkdirs();
                                            new CopiarArchivos(directorio+a.getName(),directorio+"backup/"+a.getName());
                                            a.delete();
                                        }
                                    }
                                }
                            }
                        }
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

            if (resul) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CensoActivity.this);
                builder.setMessage("Información descargada satisfactoriamente!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                                progressDialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(CensoActivity.this);
                builder.setMessage("No fué posible descargar su información. Intente más tarde!"+resul)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                                progressDialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }

            progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CensoActivity.this, "Descarga Información", "Descargando Información, espere por favor...");
        }
    }

    private void sendLectorQR() {
        Intent intent = getIntent();

        count = 0;
        // Se instancia un objeto de la clase IntentIntegrator
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        // Se procede con el proceso de scaneo
        intent.putExtra("requestCode", REQUEST_CAPTURE_QR);
        scanIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationHelper.onActivityResult(requestCode,resultCode,data);

        if (requestCode == REQUEST_CAPTURE_QR && resultCode == Activity.RESULT_OK) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(
                    requestCode, resultCode, data);
            if (scanningResult != null) {
                // Quiere decir que se obtuvo resultado pro lo tanto:
                // Desplegamos en pantalla el contenido del codigo de barra scaneado
                String scanContent = scanningResult.getContents();
                /*String qr[] = scanContent.split("\t");
                cuenta.setText(qr[0]);//cuenta
                calle.setText(qr[1]);//calle
                exterior.setText(qr[2]);//exterior
                interior.setText(qr[3]);//interior
                colonia.setText(qr[4]);//colonia
                codigopostal.setText(qr[5]);//codigoposta
                uso.setText(qr[6]);//codigoposta
                viviendas.setText(qr[7]);//codigoposta
                locales.setText(qr[8]);//codigoposta
                diamtoma.setText(qr[9]);//codigoposta
                seriemedidor.setText(qr[10]);//codigoposta
                marcamedidor.setText(qr[11]);//codigoposta
                diammedidor.setText(qr[12]);//codigoposta
                tomas.setText(qr[13]);//codigoposta*/

                startActivity(new Intent(CensoActivity.this, InicioCensoActivity.class).putExtra(EJECUTIVO_KEY, ejec).putExtra(MODULOS_KEY, modulos).putExtra(IMEI_KEY, imei).putExtra(QR,scanContent));
                finish();
            } else {
                // Quiere decir que NO se obtuvo resultado
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No se ha recibido datos del scaneo!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
