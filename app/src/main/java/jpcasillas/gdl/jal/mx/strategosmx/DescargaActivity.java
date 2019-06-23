package jpcasillas.gdl.jal.mx.strategosmx;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jpcasillas.gdl.jal.mx.strategosmx.dao.NotificacionesDBHelper;
import jpcasillas.gdl.jal.mx.strategosmx.dto.NotificacionVO;

public class DescargaActivity extends AppCompatActivity {

    ProgressBar pg;
    List<NotificacionVO> lista;
    String imei;
    boolean resul = false;
    String proceso;
    String uso;
    String medidor;
    public final static String PROCESO_KEY = "proceso_jey";
    public final static String USO_KEY = "uso_key";
    public final static String MEDIDOR_KEY = "medidor_key";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Strategoxmx - Reporte de Boletas - Descarga Información");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descarga);

        //Display progress bar until web service invocation completes
        pg = findViewById(R.id.pg);
        pg.setVisibility(View.VISIBLE);

        NotificacionesDBHelper dbHelper = new NotificacionesDBHelper(this);
        lista = dbHelper.getNotificaciones();
        Intent intent = getIntent();
        try {
            proceso = intent.getStringExtra(PROCESO_KEY);
            uso = intent.getStringExtra(USO_KEY);
            medidor = intent.getStringExtra(MEDIDOR_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/notificaciones/";

        if (lista.size() > 0) {
            File folder = new File(directorio);
            File[] listOfFiles = folder.listFiles();
            pg.setMax(listOfFiles.length);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

                if (tel != null) {
                    imei = tel.getDeviceId();
                }

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 225);
                TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

                if (tel != null) {
                    imei = tel.getDeviceId();
                }
            }

            progressDialog = ProgressDialog.show(DescargaActivity.this,"Descarga Información","Descargando Información, espere por favor...");

            try {
                new ClienteWSAsync().execute(listOfFiles.length).get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*if (resul) {
                Toast.makeText(this, "Información descargada correctamente.", Toast.LENGTH_LONG).show();
                /*switch (proceso) {
                    case "100":
                        startActivity(new Intent(DescargaActivity.this, BoletasActivity.class).putExtra(USO_KEY, uso).putExtra(MEDIDOR_KEY, medidor).putExtra(PROCESO_KEY, proceso));
                        finish();
                        break;
                    case "101":
                        startActivity(new Intent(DescargaActivity.this, TomaLecturasActivity.class).putExtra(USO_KEY, uso).putExtra(MEDIDOR_KEY, medidor).putExtra(PROCESO_KEY, proceso));
                        finish();
                        break;
                }
            } else {
                Toast.makeText(this, "Error al descargar datos. Intente nuevamente más tarde", Toast.LENGTH_LONG).show();
                /*switch (proceso) {
                    case "100":
                        startActivity(new Intent(DescargaActivity.this, BoletasActivity.class).putExtra(USO_KEY, uso).putExtra(MEDIDOR_KEY, medidor).putExtra(PROCESO_KEY, proceso));
                        finish();
                        break;
                    case "101":
                        startActivity(new Intent(DescargaActivity.this, TomaLecturasActivity.class).putExtra(USO_KEY, uso).putExtra(MEDIDOR_KEY, medidor).putExtra(PROCESO_KEY, proceso));
                        finish();
                        break;
                }
            }*/

            Log.d("Resultado", String.valueOf(resul));

        } else {
            Toast.makeText(this, "No existe información para descargar.", Toast.LENGTH_LONG).show();
            //finish();
            /*switch (proceso) {
                case "100":
                    startActivity(new Intent(DescargaActivity.this, BoletasActivity.class).putExtra(USO_KEY, uso).putExtra(MEDIDOR_KEY, medidor).putExtra(PROCESO_KEY, proceso));
                    finish();
                    break;
                case "101":
                    startActivity(new Intent(DescargaActivity.this, TomaLecturasActivity.class).putExtra(USO_KEY, uso).putExtra(MEDIDOR_KEY, medidor).putExtra(PROCESO_KEY, proceso));
                    finish();
                    break;
            }*/
        }

    }

    class ClienteWSAsync extends AsyncTask<Integer, Integer, Boolean> {



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
                publishProgress(count);

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
                        request.addProperty("Medidor", medidor);
                        request.addProperty("Uso", uso);
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
                            resul = true;
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
                                    Log.i("Resultado: ", resultado.toString());
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
            resul = result;
            if(resul){
                progressDialog = ProgressDialog.show(DescargaActivity.this,"Descarga Información","Informacion descargada satisfactoriamente");
                limpiaBase();
            }else{
                progressDialog = ProgressDialog.show(DescargaActivity.this,"Descarga Información","no fué posible descargar su información. Intenta mas tarde");
            }
            progressDialog.dismiss();
            pg.setVisibility(View.GONE);

            /*switch (proceso) {
                case "100":
                    startActivity(new Intent(DescargaActivity.this, BoletasActivity.class));
                    finish();
                    break;
                case "101":
                    startActivity(new Intent(DescargaActivity.this, TomaLecturasActivity.class));
                    finish();
                    break;
            }*/

        }

        @Override
        protected void onPreExecute() {
            pg.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            pg.setProgress(values[0]);
        }
    }

    private void limpiaBase() {
        NotificacionesDBHelper dbHelper = new NotificacionesDBHelper(this);
        dbHelper.limpiaNotificaciones();
        dbHelper.close();
    }
}
