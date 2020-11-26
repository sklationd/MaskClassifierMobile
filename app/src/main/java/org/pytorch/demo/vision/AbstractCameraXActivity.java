package org.pytorch.demo.vision;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.widget.Toast;

import org.pytorch.demo.BaseModuleActivity;
import org.pytorch.demo.StatusBarUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;


public abstract class AbstractCameraXActivity<R> extends BaseModuleActivity {
  private static final int REQUEST_CODE_CAMERA_PERMISSION = 200;
  private static final String[] PERMISSIONS = {Manifest.permission.CAMERA};

  private long mLastAnalysisResultTime;

  protected abstract int getContentViewLayoutId();

  protected abstract PreviewView getCameraPreviewTextureView();
  private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
  private PreviewView previewView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StatusBarUtils.setStatusBarOverlay(getWindow(), true);
    setContentView(getContentViewLayoutId());

    startBackgroundThread();

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          this,
          PERMISSIONS,
          REQUEST_CODE_CAMERA_PERMISSION);
    } else {
      setupCameraX();
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
      if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
        Toast.makeText(
            this,
            "You can't use image classification example without granting CAMERA permission",
            Toast.LENGTH_LONG)
            .show();
        finish();
      } else {
        setupCameraX();
      }
    }
  }

  private void setupCameraX() {
    previewView = getCameraPreviewTextureView();
    cameraProviderFuture = ProcessCameraProvider.getInstance(this);
    cameraProviderFuture.addListener(() -> {
      try {
        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
        bindPreviewAndSetAnalysis(cameraProvider);
      } catch (ExecutionException | InterruptedException e) {
        // No errors need to be handled for this Future.
        // This should never be reached.
      }
    }, ContextCompat.getMainExecutor(this));
  }

  void bindPreviewAndSetAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
    Preview preview = new Preview.Builder()
            .build();

    CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build();

    preview.setSurfaceProvider(previewView.getSurfaceProvider());

    ImageAnalysis imageAnalysis =
            new ImageAnalysis.Builder()
                    .setTargetResolution(new Size(224,224))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();

    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
      @Override
      public void analyze(@NonNull ImageProxy image) {
        int rotationDegrees = image.getImageInfo().getRotationDegrees();
        if (SystemClock.elapsedRealtime() - mLastAnalysisResultTime < 500) {
          Log.d("DEBUG", Long.toString(SystemClock.elapsedRealtime() - mLastAnalysisResultTime));
          image.close();
        return;
        }

        final R result = analyzeImage(image, rotationDegrees);
        //여기서 ImageClassificationActivity로 Image넘김

        if (result != null) {
          mLastAnalysisResultTime = SystemClock.elapsedRealtime();
          runOnUiThread(() -> applyToUiAnalyzeImageResult(result));
        }
        image.close();
      }
    });

    cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
  }


  @WorkerThread
  @Nullable
  protected abstract R analyzeImage(ImageProxy image, int rotationDegrees);

  @UiThread
  protected abstract void applyToUiAnalyzeImageResult(R result);
}
