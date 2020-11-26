package org.pytorch.demo.vision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.demo.Constants;
import org.pytorch.demo.R;
import org.pytorch.demo.Utils;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.IOException;
import java.util.Arrays;

public class DebugActivity extends AppCompatActivity {
    private Bitmap bitmap = null;
    private Module module = null;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        mHandler = new Handler();

        try{
            bitmap = BitmapFactory.decodeStream(getAssets().open("4.jpg"));
            bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
            module = Module.load(Utils.assetFilePath(this, "best_cpu_scripted.pt"));
        } catch(IOException e){
            Log.e("DEBUG", "WRONG", e);
            finish();
        }

        ImageView imageView = findViewById(R.id.debug_preview);
        imageView.setImageBitmap(bitmap);

        final Button button = findViewById(R.id.inference_button);
        button.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                       TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

               final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

               final float[] scores = outputTensor.getDataAsFloatArray();

               String classname = getLabel(scores);

               TextView tv = findViewById(R.id.debug_result_tv);
               tv.setText(classname);
           }
        });

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String[] list = new String[12];
                list[0] = "1.jpg";
                list[1] = "2.jpg";
                list[2] = "3.jpg";
                list[3] = "4.jpg";
                list[4] = "5.jpg";
                list[5] = "6.jpg";
                list[6] = "7.jpg";
                list[7] = "8.jpg";
                list[8] = "9.png";
                list[9] = "10.png";
                list[10] = "11.png";
                list[11] = "12.png";

                for(int i=0;i<12;i++){
                    try{
                        bitmap = BitmapFactory.decodeStream(getAssets().open(list[i]));
                        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
                    } catch(IOException e){
                        Log.e("DEBUG", "WRONG", e);
                        finish();
                    }
                    final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
                    final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

                    final float[] scores = outputTensor.getDataAsFloatArray();

                    Log.d("RESULT", getLabel(scores));
                }

            }
        });
    }


    private String getLabel(float[] scores){
        float maxScore = -Float.MAX_VALUE;
        int maxScoreIdx = -1;
        for (int i=0;i<scores.length;i++){
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                maxScoreIdx = i;
            }
        }
        return Constants.IMAGENET_CLASSES[maxScoreIdx];
    }
}
