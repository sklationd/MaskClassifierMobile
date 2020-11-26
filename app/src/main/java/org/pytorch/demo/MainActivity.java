package org.pytorch.demo;

import android.content.Intent;
import android.os.Bundle;

import org.pytorch.demo.vision.DebugActivity;
import org.pytorch.demo.vision.ImageClassificationActivity;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.main_vision_click_view).setOnClickListener(
        v -> {
          final Intent intent = new Intent(MainActivity.this, ImageClassificationActivity.class);
          intent.putExtra(ImageClassificationActivity.INTENT_MODULE_ASSET_NAME,
                  "best_cpu_quantized.pt");
          intent.putExtra(ImageClassificationActivity.INTENT_INFO_VIEW_TYPE,
                  InfoViewFactory.INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_QMOBILENET);
          startActivity(intent);
        }
    );

    findViewById(R.id.debug_view).setOnClickListener(
      v -> {
          final Intent intent = new Intent(MainActivity.this, DebugActivity.class);
          startActivity(intent);
      }
    );
  }
}
