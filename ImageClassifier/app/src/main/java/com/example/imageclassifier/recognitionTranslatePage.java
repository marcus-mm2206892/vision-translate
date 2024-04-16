package com.example.imageclassifier;

import static com.example.imageclassifier.imageClassifierPage.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.Locale;

public class recognitionTranslatePage extends AppCompatActivity {

    TextToSpeech t1, t2;
    ImageButton backBtn;
    TextView resultTranslated;
    Button stopBtn, playBtn;
    TranslatorOptions translatorOptions2;
    Translator translator2;
    String destinationLanguageCode, destinationLanguageTitle;
    ProgressDialog progress;

    String sourceLanguageCode;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translatepage);

        backBtn = findViewById(R.id.backBtn);
        stopBtn = findViewById(R.id.stopBtn);
        playBtn = findViewById(R.id.playBtn);
        resultTranslated = findViewById(R.id.resultTranslated);

        Intent intent = getIntent();
        sourceLanguageCode = intent.getStringExtra("sourceLanguageCode");
        String resultTextValue = intent.getStringExtra("resultText");
        destinationLanguageCode = intent.getStringExtra("destinationLanguageCode");
        destinationLanguageTitle = intent.getStringExtra("destinationLanguageTitle");

        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                    t1.speak("Start translating", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        t2 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t2.setLanguage(new Locale(destinationLanguageCode));
                }
            }
        });

        startTranslations(resultTextValue, sourceLanguageCode);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (t2 != null && t2.isSpeaking()) {
                    t2.stop();
                }
                t2 = new TextToSpeech(recognitionTranslatePage.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            int result = t2.setLanguage(new Locale(destinationLanguageCode));
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TextToSpeech", "Language not supported");
                            } else {
                                t2.speak(resultTranslated.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                            }
                        } else {
                            Log.e("TextToSpeech", "Initialization failed");
                        }
                    }
                });
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (t2 != null && t2.isSpeaking()) {
                    t2.stop();
                } else {
                    t1.speak("Stop text to speech", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    void startTranslations(String answer, String sourceLanguageCode) {
        progress = new ProgressDialog(this);
        progress.setMessage("Checking language model...");
        progress.show();

        translatorOptions2 = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(destinationLanguageCode)
                .build();
        translator2 = Translation.getClient(translatorOptions2);

        DownloadConditions downloadConditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        // First, check if the model needs to be downloaded or updated.
        translator2.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progress.setMessage("Translating...");
                        translateText(answer);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progress.dismiss();
                        Log.e(TAG, "Model download or update failed", e);
                        Toast.makeText(recognitionTranslatePage.this, "Model download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void translateText(String textToTranslate) {
        translator2.translate(textToTranslate)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String translation) {
                        progress.dismiss();
                        resultTranslated.setText(translation);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progress.dismiss();
                        Log.e(TAG, "Translation failed: " + e.getMessage(), e);
                        Toast.makeText(recognitionTranslatePage.this, "Failed to translate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
