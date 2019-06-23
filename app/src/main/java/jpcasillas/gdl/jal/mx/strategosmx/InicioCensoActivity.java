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
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralContract;
import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralDBHelper;
import jpcasillas.gdl.jal.mx.strategosmx.models.CobranzaVO;
import jpcasillas.gdl.jal.mx.strategosmx.qrReader.IntentIntegrator;
import jpcasillas.gdl.jal.mx.strategosmx.qrReader.IntentResult;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.LocationHelper;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.Utilities;

public class InicioCensoActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CAPTURE_IMAGE = 100;
    LocationHelper locationHelper;
    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/censo/";
    int newWidth = 1500;
    int newHeight = 2500;
    private int count;
    static String ejec;
    TextView ejecutivo, cuenta, calle, exterior, interior, colonia, codigopostal, uso, viviendas, locales, diamtoma, seriemedidor, marcamedidor, diammedidor, tomas;
    public final static String EJECUTIVO_KEY = "ejecutivo_key";
    public final static String QR = "qr";
    public final static String CONSECUTIVO = "consecutivo_key";
    public final static String PROCESO_KEY = "proceso_jey";
    public final static String LECTURAQR = "lectura_qr";
    public final static String MODULOS_KEY = "modulos_key";
    public final static String IMEI_KEY = "imei_key";
    String modulos;
    String imei;
    String proceso = "103";
    String lecturaqr;
    private File file = new File(ruta_fotos);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_censo);

        locationHelper = new LocationHelper(this);
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

        Intent intent = getIntent();
        ejecutivo = findViewById(R.id.txtEjecutivo);
        ejec = intent.getStringExtra(EJECUTIVO_KEY);
        ejecutivo.setText(ejec);

        cuenta = findViewById(R.id.txtCuenta);
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

        lecturaqr = intent.getStringExtra(QR);
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

        modulos = intent.getStringExtra(MODULOS_KEY);
        imei = intent.getStringExtra(IMEI_KEY);

        String consecutivo = intent.getStringExtra(CONSECUTIVO);
        if (consecutivo != null) {
            count = Integer.parseInt(consecutivo);
        }

        try {
            file.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationHelper.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {

            // get new image here like this
            count++;
            //cuenta = findViewById(R.id.txtCuenta);
            String file = ruta_fotos + getCode(count, cuenta.getText().toString()) + ".jpg";
            File mi_foto = new File(file);
            try {
                mi_foto.createNewFile();
            } catch (IOException ex) {
                Toast.makeText(InicioCensoActivity.this, "Problema al guardar fotografía." + ex.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR ", "Error:" + ex);
            }
            //
            //Uri uri = Uri.fromFile(mi_foto);
            Uri uri = FileProvider.getUriForFile(InicioCensoActivity.this,BuildConfig.APPLICATION_ID+".provider",mi_foto);
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
                        } else {
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
    }

    private String getCode(int count, String cuenta) {
        return cuenta + "_" + count;
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

    //abre la camara para tomar fotografia y regresa a este mismo activity
    @TargetApi(Build.VERSION_CODES.M)
    public void tomaFotografia(View v) {

        if (ActivityCompat.checkSelfPermission(InicioCensoActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(InicioCensoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            count++;

            //cuenta = findViewById(R.id.txtCuenta);
            String file = ruta_fotos + getCode(count, cuenta.getText().toString()) + ".jpg";
            File mi_foto = new File(file);
            try {
                mi_foto.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.e("ERROR ", "Error:" + ex);
            }
            //
            //Uri uri = Uri.fromFile(mi_foto);
            Uri uri = FileProvider.getUriForFile(InicioCensoActivity.this,BuildConfig.APPLICATION_ID+".provider",mi_foto);
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

    public void regresarCenso(View v) {
        Intent inicio = new Intent(getApplicationContext(), CensoActivity.class);
        ejecutivo = findViewById(R.id.txtEjecutivo);
        ejec = ejecutivo.getText().toString();

        inicio.putExtra(EJECUTIVO_KEY, ejec);
        inicio.putExtra(MODULOS_KEY, modulos);
        inicio.putExtra(IMEI_KEY, imei);
        startActivity(inicio);
        finish();
    }

    public void verificaCampo(View v){

        Intent campo = new Intent(getApplicationContext(),CensoCampoActivity.class);
        ejecutivo = findViewById(R.id.txtEjecutivo);
        ejec = ejecutivo.getText().toString();

        campo.putExtra(EJECUTIVO_KEY, ejec);
        campo.putExtra(MODULOS_KEY, modulos);
        campo.putExtra(IMEI_KEY, imei);
        campo.putExtra(LECTURAQR, lecturaqr);
        startActivity(campo);

        finish();

    }
}
