package jpcasillas.gdl.jal.mx.strategosmx;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

import org.kobjects.base64.Base64;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralDBHelper;
import jpcasillas.gdl.jal.mx.strategosmx.dao.NotificacionesDBHelper;
import jpcasillas.gdl.jal.mx.strategosmx.dto.NotificacionVO;
import jpcasillas.gdl.jal.mx.strategosmx.models.BoletasVO;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.CopiarArchivos;

public class BoletasActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnDescarga, btnInicio, btnRegresar;
    TextView ejecutivo;
    public final static String EJECUTIVO_KEY = "ejecutivo_key";
    public final static String PROCESO_KEY = "proceso_jey";
    public final static String MODULOS_KEY = "modulos_key";
    public final static String IMEI_KEY = "imei_key";
    final static String urlString = "http://strategosmx.com/strategosmxWS/ServiceStrategos.svc";
    String proceso = "100";
    ProgressDialog progressDialog;
    List<NotificacionVO> lista;
    List<BoletasVO> boletas;
    String imei;
    boolean resul = false;
    String modulos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setSubtitle("Reporte de Boletas");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boletas);
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
        ejecutivo.setEnabled(false);
        modulos = intent.getStringExtra(MODULOS_KEY);
        imei = intent.getStringExtra(IMEI_KEY);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnDescarga:
                try {
                    DescargaInformacionCampo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnCaptura:

                ejecutivo = findViewById(R.id.idEjecutivo);
                String message = ejecutivo.getText().toString().trim();

                if (!TextUtils.isEmpty(message)) {
                    startActivity(new Intent(BoletasActivity.this, InicioBoletasActivity.class).putExtra(EJECUTIVO_KEY, message).putExtra(PROCESO_KEY, proceso).putExtra(PROCESO_KEY, proceso).putExtra(MODULOS_KEY,modulos).putExtra(IMEI_KEY,imei));
                    finish();
                } else {
                    Toast.makeText(BoletasActivity.this, "Es necesario teclear su numero de Ejecutivo, por favor", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case R.id.btnRegresar:
                Intent inicio = new Intent(getApplicationContext(), MainActivity.class);
                ejecutivo = findViewById(R.id.idEjecutivo);
                message = ejecutivo.getText().toString().trim();
                inicio.putExtra(EJECUTIVO_KEY, message);
                inicio.putExtra(MODULOS_KEY,modulos);
                inicio.putExtra(IMEI_KEY,imei);
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

   private void DescargaInformacion() {

        try {
            NotificacionesDBHelper dbHelper = new NotificacionesDBHelper(this);
            lista = dbHelper.getNotificaciones();

            Intent intent = getIntent();
            try {
                proceso = intent.getStringExtra(PROCESO_KEY);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/notificaciones/";

            if (lista.size() > 0) {
                File folder = new File(directorio);
                File[] listOfFiles = folder.listFiles();

                new ClienteWSAsync().execute(listOfFiles.length);

            } else {
                Toast.makeText(this, "No existe información para descargar.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClienteWSAsync extends AsyncTask<Integer, Void, Boolean> {

        @Override
        @TargetApi(26)
        protected Boolean doInBackground(Integer... params) {

            final String NAMESPACE = "http://tempuri.org/";
            final String URL = "http://strategosmx.com/wswfactiv/ServicioWFActiv.asmx";
            final String METHOD_NAME = "Reporta_Actividad";
            final String SOAP_ACTION = "http://tempuri.org/Reporta_Actividad";
            String result = null;
            Integer count = 1;
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyyMM", Locale.US);

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            try {
                Thread.sleep(1000);

                //envia informacion de la base de datos
                for (NotificacionVO n : lista) {
                    try {

                        request.addProperty("Ruta", n.getFecharegistro());
                        request.addProperty("Cuenta", n.getManzana());
                        request.addProperty("Proceso", proceso);
                        request.addProperty("Latitud", String.valueOf(n.getLatitud()));
                        request.addProperty("Longitud", String.valueOf(n.getLongitud()));
                        //request.addProperty("Consumo", n.getManzana() + "_" + dateFormat.format(date));
                        request.addProperty("Consumo", n.getEjecutivo() + "_" + dateFormat.format(date));
                        //request.addProperty("Medidor", medidor);
                        //request.addProperty("Uso", uso);
                        request.addProperty("Ejecutivo", String.valueOf(n.getEjecutivo()));
                        request.addProperty("Equipo", imei);
                        request.addProperty("Fecha", n.getFecharegistro() + " " + n.getHoraregistro());

                        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
                        envelope.implicitTypes = true;
                        envelope.setOutputSoapObject(request);
                        envelope.dotNet = true;

                        HttpTransportSE httpTransport = new HttpTransportSE(URL);

                        try {
                            httpTransport.call(SOAP_ACTION, envelope);
                            SoapPrimitive resultado_xml = (SoapPrimitive) envelope.getResponse();
                            result = resultado_xml.toString();
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (String.valueOf(result).equals("true")) {
                            resul = Boolean.valueOf(result);
                            NotificacionesDBHelper db = new NotificacionesDBHelper(getApplicationContext());
                            db.delete(n.getId());
                            db.close();
                        }

                    } catch (Exception e) {
                        resul = false;
                        e.printStackTrace();
                    }
                }

                //envia fotos
                for (; count <= params[0]; count++) {
                    String directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/notificaciones/";

                    String files;
                    File folder = new File(directorio);
                    File[] listOfFiles = folder.listFiles();
                    final String METHOD_NAME_FOTOS = "Descarga_Archivos";
                    HttpTransportSE httpTransport = new HttpTransportSE(URL);
                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);

                    for (File a : listOfFiles) {
                        if (a.isFile()) {
                            files = a.getName();
                            File arch = new File(directorio + files);
                            if (arch.length() == 0) {
                                arch.delete();
                            } else {
                                int size = (int) arch.length();
                                byte[] bytes = new byte[size];

                                try {
                                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(arch));
                                    buf.read(bytes, 0, bytes.length);
                                    buf.close();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (files.endsWith(".jpg") || files.endsWith(".JPG")) {
                                    Log.d("Archivo: ", files);
                                    request = new SoapObject(NAMESPACE, METHOD_NAME_FOTOS);
                                    request.addProperty("Barr", Base64.encode(bytes));
                                    request.addProperty("Arch", files);

                                    new MarshalBase64().register(envelope);
                                    envelope.implicitTypes = true;
                                    envelope.dotNet = true;
                                    envelope.setOutputSoapObject(request);

                                    try {
                                        httpTransport.call(SOAP_ACTION, envelope);
                                        SoapPrimitive resultado_xml = (SoapPrimitive) envelope.getResponse();
                                        result = resultado_xml.toString();
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

                                    Boolean resultado = Boolean.parseBoolean(result);
                                    if (resultado) {
                                        resul = true;
                                        arch.delete();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(BoletasActivity.this);
                builder.setMessage("Información descargada satisfactoriamente!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(BoletasActivity.this);
                builder.setMessage("No fué posible descargar su información. Intente más tarde!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
            progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(BoletasActivity.this, "Descarga Información", "Descargando Información, espere por favor...");
        }
    }

    class ReporteCampoWSAsync extends AsyncTask<Integer, Void, Boolean> {

        @Override
        @TargetApi(26)
        protected Boolean doInBackground(Integer... params) {

            final String NAMESPACE = "http://tempuri.org/";
            final String URL = urlString;
            final String METHOD_NAME = "ReportaActividadBoletas";
            String SOAP_ACTION = "http://tempuri.org/IServiceStrategos/ReportaActividadBoletas";
            int resultado_xml = 0;
            SoapObject result=null;
            Integer count = 1;

            try {
                Thread.sleep(1000);

                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url
                        .openConnection();
                int responseCode = urlConnection.getResponseCode();
                urlConnection.disconnect();

                if(responseCode!=200){
                    Toast.makeText(getApplicationContext(), "Intente nuevamente, fallo al intentar conectarse al servidor.", Toast.LENGTH_LONG).show();
                }

                //envia informacion de la base de datos
                for (BoletasVO n : boletas) {
                    SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                    try {

                        request.addProperty("manzana",n.getManzana());
                        request.addProperty("bimestre", n.getBimestre());
                        request.addProperty("fecharegistro", n.getFecharegistro() + " " + n.getHoraregistro());
                        request.addProperty("idejecutivo", n.getEjecutivo());
                        request.addProperty("latitud", String.valueOf(n.getLatitud()));
                        request.addProperty("longitud", String.valueOf(n.getLongitud()));
                        request.addProperty("imei", imei);
                        request.addProperty("idproceso", 1);

                        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                        envelope.implicitTypes = true;
                        envelope.setOutputSoapObject(request);
                        envelope.dotNet = true;

                        HttpTransportSE httpTransport = new HttpTransportSE(URL);
                        httpTransport.debug=true;

                        Log.i("envelope: ",envelope.bodyOut.toString());
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
                        } catch (InternalError e){
                            Log.e("Internal", e.getMessage());
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        resultado_xml = Integer.parseInt(result.getProperty(0).toString());
                        if (resultado_xml==1) {
                            resul = true;
                            DbCentralDBHelper db = new DbCentralDBHelper(getApplicationContext());
                            db.deleteBoletas(n.getIdBoletas());
                            db.close();
                        }

                    } catch (Exception e) {
                        resul = false;
                        e.printStackTrace();
                    }
                }


                //envia fotos
                for (; count <= params[0]; count++) {
                    String directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/boletas/";

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
                            File arch = new File(directorio + files);
                            if (arch.length() == 0) {
                                arch.delete();
                            } else {
                                int size = (int) arch.length();
                                byte[] bytes = new byte[size];

                                try {
                                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(arch));
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
                                    request.addProperty("proceso","100");

                                    new MarshalBase64().register(envelope);
                                    envelope.implicitTypes = true;
                                    envelope.dotNet = true;
                                    envelope.setOutputSoapObject(request);

                                    HttpTransportSE httpTransport = new HttpTransportSE(URL);
                                    httpTransport.debug = true;

                                    try {
                                        httpTransport.call(SOAP_ACTION, envelope);
                                        Log.i("in: ",envelope.bodyIn.toString());
                                        //SoapPrimitive resultado_xml = (SoapPrimitive) envelope.getResponse();
                                        result=(SoapObject)envelope.bodyIn;
                                        Log.i("RESPONSE FOTOS", String.valueOf(result)); // see output in the cons|ole
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
                                    Log.i("Resultado foto: ",resultado.toString());
                                    if (resultado) {
                                        resul = true;
                                        folder = new File(directorio+"backup");
                                        folder.mkdirs();
                                        new CopiarArchivos(directorio+a.getName(),directorio+"backup/"+a.getName());
                                        arch.delete();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(BoletasActivity.this);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(BoletasActivity.this);
                builder.setMessage("No fué posible descargar su información. Intente más tarde!")
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
            btnDescarga.setEnabled(true);
            progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(BoletasActivity.this, "Descarga Información", "Descargando Información, espere por favor...");
        }
    }

    private void DescargaInformacionCampo() {

        try {

            if (!Patterns.WEB_URL.matcher(urlString).matches()) {
                Toast.makeText(getApplicationContext(), "Intente nuevamente, fallo al intentar conectarse al servidor.", Toast.LENGTH_LONG).show();
            }

            DbCentralDBHelper dbcentral = new DbCentralDBHelper(this);
            boletas = dbcentral.getBoletas();

            Intent intent = getIntent();
            try {
                proceso = intent.getStringExtra(PROCESO_KEY);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/boletas/";

            File folder = new File(directorio);
            File[] listOfFiles = folder.listFiles();

            if (boletas.size() > 0 || listOfFiles.length>0) {

                new ReporteCampoWSAsync().execute(listOfFiles.length);

                this.btnDescarga.setEnabled(false);
            } else {
                Toast.makeText(this, "No existe información para descargar.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

