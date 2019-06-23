package jpcasillas.gdl.jal.mx.strategosmx;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class LectorQRActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String intentData = "";
    //boolean isEmail = false;
    public final static String QR_KEY = "jpcasillas.gdl.jal.mx.notificaciones.qr_key";
    public final static String EJECUTIVO_KEY = "jpcasillas.gdl.jal.mx.strategosmx.ejecutivo_key";
    public final static String CONSECUTIVO = "jpcasillas.gdl.jal.mx.notificaciones.consecutivo";
    public final static String PROCESO_kEY = "proceso_key";
    String ejec;
    Button btnAction;
    int proceso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Strategoxmx - Reporte de Boletas - Lector QR");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lector_qr);
        Intent intent = getIntent();
        ejec = intent.getStringExtra(EJECUTIVO_KEY);
        proceso = Integer.parseInt(intent.getStringExtra(PROCESO_kEY));
        initViews();
    }

    private void initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);
        btnAction = findViewById(R.id.btnAction);

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent refresh = new Intent(getApplicationContext(), LectorQRActivity.class);
                refresh.putExtra(EJECUTIVO_KEY, ejec);
                startActivity(refresh);
                finish();
            }
        });
    }

    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(LectorQRActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(LectorQRActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Toast.makeText(getApplicationContext(), "Para prevenir problemas de memoria, se detuvo el scanner de QR", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {

                    txtBarcodeValue.post(new Runnable() {

                        @Override
                        public void run() {
                            //isEmail = false;
                            intentData = barcodes.valueAt(0).displayValue;
                            txtBarcodeValue.setText(intentData);
                            Intent intent;
                            switch (proceso) {
                                case 100:
                                    intent = new Intent(LectorQRActivity.this, InicioBoletasActivity.class);
                                    intent.putExtra(QR_KEY, intentData);
                                    intent.putExtra(EJECUTIVO_KEY, ejec);
                                    intent.putExtra(CONSECUTIVO, 0);
                                    startActivityForResult(intent, 0);
                                    break;
                                case 101:
                                    break;

                            }
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
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

    public void regresar(View v) {
        Intent inicio;
        switch (proceso) {
            case 100:
                inicio = new Intent(getApplicationContext(), InicioBoletasActivity.class);
                ejec = inicio.getStringExtra(EJECUTIVO_KEY);
                startActivity(inicio);
                finish();
                break;
            case 101:
                break;
        }

    }
}
