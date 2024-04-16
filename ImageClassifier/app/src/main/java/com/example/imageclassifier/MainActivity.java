package com.example.imageclassifier;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextToSpeech t1;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);

        Button imageClassifierBtn = findViewById(R.id.imageClassifierBtn);
        Button characterRecognitionBtn = findViewById(R.id.characterRecognitionBtn);
        Button backBtn = findViewById(R.id.backBtn);

        // Retrieve the destination language code and title from the intent extras
        Intent intent = getIntent();
        final String destinationLanguageCode = intent.getStringExtra("destinationLanguageCode");
        final String destinationLanguageTitle = intent.getStringExtra("destinationLanguageTitle");

        TextView destinationLangTitle = findViewById(R.id.destinationLangTitle);
        destinationLangTitle.setText("Destination Language: "+ destinationLanguageTitle);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    // Set language here if needed
                }
            }
        });

        imageClassifierBtn.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between clicks to be considered a double click
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // Double click detected, navigate to the associated activity
                    Intent intent = new Intent(MainActivity.this, imageClassifierPage.class);
                    intent.putExtra("destinationLanguageCode", destinationLanguageCode);
                    intent.putExtra("destinationLanguageTitle", destinationLanguageTitle);
                    startActivity(intent);
                } else {
                    // Single click detected, speak the name of the button
                    speakButtonName(imageClassifierBtn.getText().toString());
                }
                lastClickTime = clickTime;
            }
        });

        characterRecognitionBtn.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between clicks to be considered a double click
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // Double click detected, navigate to the associated activity
                    Intent intent = new Intent(MainActivity.this, characterRecognitionPage.class);
                    intent.putExtra("destinationLanguageCode", destinationLanguageCode);
                    intent.putExtra("destinationLanguageTitle", destinationLanguageTitle);
                    startActivity(intent);
                } else {
                    // Single click detected, speak the name of the button
                    speakButtonName(characterRecognitionBtn.getText().toString());
                }
                lastClickTime = clickTime;
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between clicks to be considered a double click
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    finish();
                } else {
                    String buttonDescription = "BACK";
                    speakButtonDescription(buttonDescription);
                }
                lastClickTime = clickTime;
            }
        });

    }

    private void speakButtonName(String buttonName) {
        t1.speak(buttonName, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void speakButtonDescription(String buttonDescription) {
        t1.speak(buttonDescription, TextToSpeech.QUEUE_FLUSH, null);
    }

}
