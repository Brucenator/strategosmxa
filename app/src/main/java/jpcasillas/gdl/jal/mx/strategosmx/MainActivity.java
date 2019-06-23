package jpcasillas.gdl.jal.mx.strategosmx;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnBoletas, btnLecturas, btnCobranza, btnOficialias, btnServivcio, btnSalir, btnCenso;
    TextView ejecutivo;
    ImageButton imgCobranza, imgServicio, imgOficialias, imgBoletas, imgLecturas, imgCenso;
    public final static String EJECUTIVO_KEY = "ejecutivo_key";
    public final static String MODULOS_KEY = "modulos_key";
    public final static String IMEI_KEY = "imei_key";
    private String IMEI;
    private String modulos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.support.v7.app.ActionBar ab = getSupportActionBar();

        btnBoletas = findViewById(R.id.btnboletas);
        btnLecturas = findViewById(R.id.btnLecturas);
        btnCobranza = findViewById(R.id.btnCobranza);
        btnOficialias = findViewById(R.id.btnOficialias);
        btnServivcio = findViewById(R.id.btnServicio);
        btnCenso = findViewById(R.id.btnCenso);
        btnSalir = findViewById(R.id.btnSalir);

        imgOficialias = findViewById(R.id.imageButton11);
        imgServicio = findViewById(R.id.imageButton12);
        imgCobranza = findViewById(R.id.imageButton10);
        imgBoletas = findViewById(R.id.imageButton8);
        imgLecturas = findViewById(R.id.imageButton9);
        imgCenso = findViewById(R.id.imgCenso);

        btnBoletas.setOnClickListener(this);
        btnLecturas.setOnClickListener(this);
        btnCobranza.setOnClickListener(this);
        btnOficialias.setOnClickListener(this);
        btnServivcio.setOnClickListener(this);
        btnCenso.setOnClickListener(this);

        btnServivcio.setEnabled(false);
        btnOficialias.setEnabled(false);
        imgServicio.setEnabled(false);
        imgOficialias.setEnabled(false);
        btnCobranza.setEnabled(false);
        imgCobranza.setEnabled(false);
        btnBoletas.setEnabled(false);
        imgBoletas.setEnabled(false);
        btnLecturas.setEnabled(false);
        imgLecturas.setEnabled(false);
        imgCenso.setEnabled(false);
        btnCenso.setEnabled(false);

        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
                /*Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);*/
                ejecutivo.setText("");
                finish();
                finishAffinity();
            }
        });

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 225);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        try {
            Intent intent = getIntent();
            ejecutivo = findViewById(R.id.txtEjecutivo);
            String ejec = intent.getStringExtra(EJECUTIVO_KEY);
            ejecutivo.setText(ejec);
            ejecutivo.setEnabled(false);
            modulos = intent.getStringExtra(MODULOS_KEY);
            IMEI = intent.getStringExtra(IMEI_KEY);

            Log.i("Modulos: ",modulos);

            String[] separa = modulos.split(",");
            for(String s : separa){
                switch(s){
                    case "100"://boletas
                        imgBoletas.setEnabled(true);
                        btnBoletas.setEnabled(true);
                        break;
                    case "101"://lecturas
                        btnLecturas.setEnabled(true);
                        imgLecturas.setEnabled(true);
                        break;
                    case "102"://cobranza
                        btnCobranza.setEnabled(true);
                        imgCobranza.setEnabled(true);
                        break;
                    case "105"://oficialias
                        btnOficialias.setEnabled(true);
                        imgOficialias.setEnabled(true);
                        break;
                    case "104"://servicio
                        btnServivcio.setEnabled(true);
                        imgServicio.setEnabled(true);
                        break;
                    case "103"://censo
                        btnCenso.setEnabled(true);
                        imgCenso.setEnabled(true);
                        break;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

            if (tel != null) {
                IMEI = tel.getDeviceId();
            }

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 225);
            TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

            if (tel != null) {
                IMEI = tel.getDeviceId();
            }
        }*/

        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int versionNumber = pinfo.versionCode;
            String versionName = pinfo.versionName;

            ab.setSubtitle("Versión: " + versionNumber + "-" + versionName + " (Equipo: "+IMEI+")");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {

        ejecutivo = findViewById(R.id.txtEjecutivo);
        String message = ejecutivo.getText().toString().trim();
        ejecutivo.setEnabled(false);

        switch (v.getId()) {
            case R.id.imageButton8:
            case R.id.btnboletas://proceso 100
                if (!TextUtils.isEmpty(message)) {
                    startActivity(new Intent(MainActivity.this, BoletasActivity.class).putExtra(EJECUTIVO_KEY, message).putExtra(MODULOS_KEY,modulos).putExtra(IMEI_KEY,IMEI));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Es necesario teclear su numero de Ejecutivo, por favor", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case R.id.imageButton9:
            case R.id.btnLecturas://proceso 101
                if (!TextUtils.isEmpty(message)) {
                    startActivity(new Intent(MainActivity.this, TomaLecturasActivity.class).putExtra(EJECUTIVO_KEY, message).putExtra(MODULOS_KEY,modulos).putExtra(IMEI_KEY,IMEI));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Es necesario teclear su numero de Ejecutivo, por favor", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case R.id.imageButton10:
            case R.id.btnCobranza://proceso 102
                if (!TextUtils.isEmpty(message)) {
                    startActivity(new Intent(MainActivity.this, CobranzaActivity.class).putExtra(EJECUTIVO_KEY, message).putExtra(MODULOS_KEY,modulos).putExtra(IMEI_KEY,IMEI));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Es necesario teclear su numero de Ejecutivo, por favor", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case R.id.btnOficialias://proceso 105
                if (!TextUtils.isEmpty(message)) {
                    //startActivity(new Intent(MainActivity.this, OficialiasActivity.class).putExtra(EJECUTIVO_KEY, message));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Es necesario teclear su numero de Ejecutivo, por favor", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case R.id.btnServicio://proceso 104
                if (!TextUtils.isEmpty(message)) {
                    startActivity(new Intent(MainActivity.this, OrdenServicioActivity.class).putExtra(EJECUTIVO_KEY, message).putExtra(MODULOS_KEY,modulos).putExtra(IMEI_KEY,IMEI));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Es necesario teclear su numero de Ejecutivo, por favor", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case R.id.imgCenso://proceso 103
            case R.id.btnCenso:
                if(!TextUtils.isEmpty(message)){
                    startActivity(new Intent(MainActivity.this, CensoActivity.class).putExtra(EJECUTIVO_KEY, message).putExtra(MODULOS_KEY,modulos).putExtra(IMEI_KEY,IMEI));
                    finish();
                }else{
                    Toast.makeText(MainActivity.this, "Es necesario teclear su numero de Ejecutivo, por favor", Toast.LENGTH_SHORT).show();
                    return;
                }
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed(); commented this line in order to disable back press
        //Write your code here
        Toast.makeText(getApplicationContext(), "Click en botón Salir.", Toast.LENGTH_LONG).show();
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
