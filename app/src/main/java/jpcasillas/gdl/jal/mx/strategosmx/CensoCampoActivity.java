package jpcasillas.gdl.jal.mx.strategosmx;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralContract;
import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralDBHelper;
import jpcasillas.gdl.jal.mx.strategosmx.models.CensoVO;
import jpcasillas.gdl.jal.mx.strategosmx.models.CobranzaVO;
import jpcasillas.gdl.jal.mx.strategosmx.models.FotografiasCensoVO;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.CopiarArchivos;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.LocationHelper;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.Utilities;

public class CensoCampoActivity extends AppCompatActivity {

    String ejec;
    public final static String EJECUTIVO_KEY = "ejecutivo_key";
    public final static String MODULOS_KEY = "modulos_key";
    public final static String IMEI_KEY = "imei_key";
    public final static String PROCESO_KEY = "proceso_jey";
    public final static String LECTURAQR = "lectura_qr";
    String modulos;
    String imei;
    String proceso = "103";
    String lecturaqr;
    LocationHelper locationHelper;
    private Location mLastLocation;
    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/censo/";
    double lat, lon;
    String fecha, hora;
    Long idreturn;
    public static int count = 0;
    final static String urlString = "http://strategosmx.com/strategosmxWS/ServiceStrategos.svc";
    List<CensoVO> censo;
    List<CensoVO> reportecampocenso;
    List<FotografiasCensoVO> fotos;
    boolean resul = false;
    ProgressDialog progressDialog;
    TextView ejecutivo, cuenta, calle, exterior, interior, colonia, codigopostal, uso, viviendas, locales, diamtoma, seriemedidor, marcamedidor, diammedidor, tomas, observaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_censo_campo);

        Intent inicio = getIntent();
        ejec = inicio.getStringExtra(EJECUTIVO_KEY);
        modulos = inicio.getStringExtra(MODULOS_KEY);
        imei = inicio.getStringExtra(IMEI_KEY);

        Intent intent = getIntent();
        ejecutivo = findViewById(R.id.txtEjecutivo);
        ejec = intent.getStringExtra(EJECUTIVO_KEY);
        ejecutivo.setText(ejec);

        cuenta = findViewById(R.id.txtCuentaSACMEX);
        calle = findViewById(R.id.txtCalle);
        exterior = findViewById(R.id.txtExterior);
        interior = findViewById(R.id.txtInterior);
        colonia = findViewById(R.id.txtColonia);
        codigopostal = findViewById(R.id.txtCodigoPostal);
        uso = findViewById(R.id.txtUso);
        viviendas = findViewById(R.id.txtViviendas);
        locales = findViewById(R.id.txtLocales);
        diamtoma = findViewById(R.id.txtDiamToma);
        seriemedidor = findViewById(R.id.txtSerieMedidor);
        marcamedidor = findViewById(R.id.txtMarcaMedidor);
        diammedidor = findViewById(R.id.txtDiamMedidor);
        tomas = findViewById(R.id.txtTomas);
        observaciones = findViewById(R.id.txtObservaciones);

        lecturaqr = intent.getStringExtra(LECTURAQR);
    }

    public void regresar(View v) {
        Intent inicio = new Intent(getApplicationContext(), CensoActivity.class);

        inicio.putExtra(EJECUTIVO_KEY, ejec);
        inicio.putExtra(PROCESO_KEY, proceso);
        inicio.putExtra(MODULOS_KEY, modulos);
        inicio.putExtra(IMEI_KEY, imei);
        startActivity(inicio);
        finish();
    }

    public void sendGrabar(View v) {


        mLastLocation = locationHelper.getLocation();

        if (mLastLocation != null) {
            lat = mLastLocation.getLatitude();
            lon = mLastLocation.getLongitude();
        } else {
            Toast.makeText(this, "Nó fue posible obtener la ubicación favor de habilitar el GPS", Toast.LENGTH_SHORT).show();
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
                if (a.getName().startsWith(cuenta.getText().toString().trim())) {
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
                values.put(DbCentralContract.CensoEntry.FECHA_REGISTRO, fecha);
                values.put(DbCentralContract.CensoEntry.HORA_REGISTRO, hora);
                values.put(DbCentralContract.CensoEntry.EJECUTIVO, ejec);
                values.put(DbCentralContract.CensoEntry.LATITUD, lat);
                values.put(DbCentralContract.CensoEntry.LONGITUD, lon);
                values.put(DbCentralContract.CensoEntry.PROCESO, 4);
                values.put(DbCentralContract.CensoEntry.IMEI, imei);
                values.put(DbCentralContract.CensoEntry.CALLE, calle.getText().toString());
                values.put(DbCentralContract.CensoEntry.CODIGOPOSTAL, codigopostal.getText().toString());
                values.put(DbCentralContract.CensoEntry.COLONIA, colonia.getText().toString());
                values.put(DbCentralContract.CensoEntry.CUENTA, cuenta.getText().toString());
                values.put(DbCentralContract.CensoEntry.DIAMMEDIDOR, diammedidor.getText().toString());
                values.put(DbCentralContract.CensoEntry.DIAMTOMA, diamtoma.getText().toString());
                values.put(DbCentralContract.CensoEntry.EXTERIOR, exterior.getText().toString());
                values.put(DbCentralContract.CensoEntry.INTERIOR, interior.getText().toString());
                values.put(DbCentralContract.CensoEntry.LOCALES, locales.getText().toString());
                values.put(DbCentralContract.CensoEntry.MARCAMEDIDOR, marcamedidor.getText().toString());
                values.put(DbCentralContract.CensoEntry.SERIEMEDIDOR, seriemedidor.getText().toString());
                values.put(DbCentralContract.CensoEntry.VIVIENDAS, viviendas.getText().toString());
                values.put(DbCentralContract.CensoEntry.USO, uso.getText().toString());
                values.put(DbCentralContract.CensoEntry.TOMAS, tomas.getText().toString());
                values.put(DbCentralContract.CensoEntry.OBSERVACIONES, observaciones.getText().toString());

                idreturn = dbHelper.saveRepCamCenso(values);

                values.put(DbCentralContract.CensoEntry.IDREPCAMCENSO, idreturn);

                for (File a : listOfFiles) {
                    values = new ContentValues();
                    values.put(DbCentralContract.FotograciasCensoEntry.IDREPCAMCENSO, idreturn);
                    values.put(DbCentralContract.FotograciasCensoEntry.FOTOGRAFIA, a.getName());

                    dbHelper.saveFotografiasCenso(values);
                }

                String qr[] = lecturaqr.split("\t");
                cuenta.setText(qr[0]);
                calle.setText(qr[1]);
                exterior.setText(qr[2]);
                interior.setText(qr[3]);
                colonia.setText(qr[4]);
                codigopostal.setText(qr[5]);
                uso.setText(qr[6]);
                viviendas.setText(qr[7]);
                locales.setText(qr[8]);
                diamtoma.setText(qr[9]);
                seriemedidor.setText(qr[10]);
                marcamedidor.setText(qr[11]);
                diammedidor.setText(qr[12]);
                tomas.setText(qr[13]);

                values = new ContentValues();
                values.put(DbCentralContract.CensoEntry.CALLE, calle.getText().toString());
                values.put(DbCentralContract.CensoEntry.CODIGOPOSTAL, codigopostal.getText().toString());
                values.put(DbCentralContract.CensoEntry.COLONIA, colonia.getText().toString());
                values.put(DbCentralContract.CensoEntry.CUENTA, cuenta.getText().toString());
                values.put(DbCentralContract.CensoEntry.DIAMMEDIDOR, diammedidor.getText().toString());
                values.put(DbCentralContract.CensoEntry.DIAMTOMA, diamtoma.getText().toString());
                values.put(DbCentralContract.CensoEntry.EXTERIOR, exterior.getText().toString());
                values.put(DbCentralContract.CensoEntry.INTERIOR, interior.getText().toString());
                values.put(DbCentralContract.CensoEntry.LOCALES, locales.getText().toString());
                values.put(DbCentralContract.CensoEntry.MARCAMEDIDOR, marcamedidor.getText().toString());
                values.put(DbCentralContract.CensoEntry.SERIEMEDIDOR, seriemedidor.getText().toString());
                values.put(DbCentralContract.CensoEntry.VIVIENDAS, viviendas.getText().toString());
                values.put(DbCentralContract.CensoEntry.USO, uso.getText().toString());
                values.put(DbCentralContract.CensoEntry.TOMAS, tomas.getText().toString());
                values.put(DbCentralContract.CensoEntry.IDREPCAMCENSO, idreturn);

                dbHelper.saveCenso(values);
                dbHelper.close();

                censo = dbHelper.getCenso();

                dbHelper = new DbCentralDBHelper(this);
                values = new ContentValues();
                values.put(DbCentralContract.EjecutivosEntry.EJECUTIVO, ejec);
                values.put(DbCentralContract.EjecutivosEntry.CONSECUTIVO, count);
                dbHelper.saveEjecutivos(values);
                dbHelper.close();

                Toast.makeText(CensoCampoActivity.this, "Registro Guardadado satisfactoriamente.", Toast.LENGTH_LONG).show();

                //si existe señal envia datos al server
                if (Utilities.CheckInternetConnection(this)) {
                    reportecampocenso = new ArrayList<>();
                    CensoVO l = new CensoVO();
                    l.setEjecutivo(ejec);
                    l.setFecharegistro(fecha);
                    l.setHoraregistro(hora);
                    l.setImei(imei);
                    l.setLatitud(lat);
                    l.setLongitud(lon);
                    l.setProceso(4);
                    l.setObservaciones(observaciones.getText().toString());
                    l.setCodigopostal(codigopostal.getText().toString());
                    l.setDiammedidor(Integer.parseInt(diammedidor.getText().toString()));
                    l.setMarcamedidor(marcamedidor.getText().toString());
                    l.setSeriemedidor(seriemedidor.getText().toString());
                    l.setDiamtoma(Integer.parseInt(diamtoma.getText().toString()));
                    l.setLocales(Integer.parseInt(locales.getText().toString()));
                    l.setViviendas(Integer.parseInt(viviendas.getText().toString()));
                    l.setUso(Integer.parseInt(uso.getText().toString()));
                    l.setColonia(colonia.getText().toString());
                    l.setInterior(interior.getText().toString());
                    l.setExterior(exterior.getText().toString());
                    l.setCalle(calle.getText().toString());
                    l.setCuenta(cuenta.getText().toString());
                    l.setTomas(Integer.parseInt(tomas.getText().toString()));
                    l.setIdrepcamcenso(idreturn.intValue());

                    reportecampocenso.add(l);

                    for (File a : listOfFiles) {
                        fotos = new ArrayList<>();
                        FotografiasCensoVO f = new FotografiasCensoVO();
                        f.setIdrepcamcenso(idreturn);
                        f.setFotogfrafia(a.getName());
                        fotos.add(f);
                    }

                    try {
                        new CensoCampoActivity.ReporteCampoWSAsync().execute(0);
                    } catch (Exception e) {
                        Toast.makeText(CensoCampoActivity.this, "Problema al intentar enviar informacion." + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    count = 0;
                    lat = 0;
                    lon = 0;
                    //locationManager.removeUpdates(locationListenerGPS);
                    locationHelper.connectApiClient();
                    Toast.makeText(CensoCampoActivity.this, "Registro grabado satisfactoriamente", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(CensoCampoActivity.this, "Es necesario al menos tomar una fotografía.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(CensoCampoActivity.this, "Es necesario al menos tomar una fotografía.", Toast.LENGTH_LONG).show();
        }
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
                AlertDialog.Builder builder = new AlertDialog.Builder(CensoCampoActivity.this);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(CensoCampoActivity.this);
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
            progressDialog = ProgressDialog.show(CensoCampoActivity.this, "Descarga Información", "Descargando Información, espere por favor...");
        }
    }
}
