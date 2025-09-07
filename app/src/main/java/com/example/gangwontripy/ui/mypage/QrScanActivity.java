package com.example.gangwontripy.ui.mypage;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Surface;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.util.Log;

import com.example.gangwontripy.R;
import com.example.gangwontripy.data.api.ApiClient;
import com.example.gangwontripy.data.api.VisitApi;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrScanActivity extends AppCompatActivity {

    private static final String TAG = "QRSCAN";

    private PreviewView previewView;
    private BarcodeScanner scanner;
    private ProcessCameraProvider provider;
    private ImageAnalysis analysis;
    private ExecutorService analyzerExecutor;
    private final AtomicBoolean analyzing = new AtomicBoolean(false); // ✅ 동시 처리 방지
    private volatile boolean handled = false;
    private final long userId = 1L;

    private final ActivityResultLauncher<String> camPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> { if (granted) startCamera(); else finish(); });

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan_floating);

        previewView = findViewById(R.id.preview);
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        analyzerExecutor = Executors.newSingleThreadExecutor();

        scanner = BarcodeScanning.getClient(new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            camPerm.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                provider = ProcessCameraProvider.getInstance(this).get();

                int rotation = (previewView.getDisplay() != null)
                        ? previewView.getDisplay().getRotation() : Surface.ROTATION_0;

                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setTargetRotation(rotation)
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                analysis = new ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setTargetRotation(rotation)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // ✅ 전용 실행 스레드
                analysis.setAnalyzer(analyzerExecutor, this::analyze);

                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);

                Log.d(TAG, "Camera started (aspect=4:3, rotation=" + rotation + ")");
            } catch (Exception e) {
                Log.e(TAG, "Camera start error", e);
                show("카메라 오류: " + e.getMessage());
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyze(@NonNull ImageProxy ip) {
        // 이미 처리 중이면 최신 프레임만 유지하고 바로 닫음
        if (!analyzing.compareAndSet(false, true)) {
            ip.close();
            return;
        }
        try {
            if (handled || ip.getImage() == null) {
                analyzing.set(false);
                ip.close();
                return;
            }

            final int rot = ip.getImageInfo().getRotationDegrees();
            final InputImage img = InputImage.fromMediaImage(ip.getImage(), rot);

            scanner.process(img)
                    .addOnSuccessListener(list -> {
                        if (handled || list == null || list.isEmpty()) return;

                        Log.d(TAG, "Barcodes detected: " + list.size());
                        Barcode qr = null;
                        for (Barcode b : list) {
                            if (b.getFormat() == Barcode.FORMAT_QR_CODE) { qr = b; break; }
                        }
                        if (qr == null) return;

                        String token = qr.getRawValue();
                        Log.d(TAG, "QR detected, length=" + (token == null ? -1 : token.length()));

                        if (token != null && !token.trim().isEmpty()) {
                            handled = true;

                            runOnUiThread(() -> {
                                try { previewView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); } catch (Exception ignore) {}
                                Toast.makeText(this, "QR 감지", Toast.LENGTH_SHORT).show();
                            });

                            VisitApi.VisitClaimRequest body = new VisitApi.VisitClaimRequest(token, null, null);
                            ApiClient.visitApi().claim(userId, body).enqueue(new Callback<VisitApi.VisitClaimResponse>() {
                                @Override public void onResponse(Call<VisitApi.VisitClaimResponse> call, Response<VisitApi.VisitClaimResponse> res) {
                                    String msg = (res.isSuccessful() && res.body()!=null) ? res.body().message : "서버 오류";
                                    Log.d(TAG, "Claim response: " + msg);
                                    show(msg);
                                    finish();
                                }
                                @Override public void onFailure(Call<VisitApi.VisitClaimResponse> call, Throwable t) {
                                    Log.e(TAG, "Claim failure", t);
                                    show("네트워크 오류: " + t.getMessage());
                                    finish();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        // ❗️여기서도 닫지 말고 onComplete에서 통합 처리
                        Log.e(TAG, "MLKit process failure", e);
                    })
                    .addOnCompleteListener(t -> {
                        // ✅ 항상 마지막에 프레임 닫고 락 해제
                        analyzing.set(false);
                        try { ip.close(); } catch (Exception ignore) {}
                    });

        } catch (Throwable t) {
            // 예외 시에도 확실히 닫고 락 해제
            Log.e(TAG, "Analyzer error", t);
            analyzing.set(false);
            try { ip.close(); } catch (Exception ignore) {}
        }
    }

    private void show(String msg){
        runOnUiThread(() ->
                new AlertDialog.Builder(this)
                        .setTitle("QR 스캔")
                        .setMessage(msg)
                        .setPositiveButton("확인",(d,w)->{})
                        .show()
        );
    }

    @Override protected void onDestroy() {
        try { if (provider != null) provider.unbindAll(); } catch (Exception ignore) {}
        try { if (scanner != null) scanner.close(); } catch (Exception ignore) {}
        try { if (analyzerExecutor != null) analyzerExecutor.shutdownNow(); } catch (Exception ignore) {}
        super.onDestroy();
    }
}
