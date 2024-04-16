package com.example.imageclassifier;

import static com.example.imageclassifier.imageClassifierPage.TAG;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class characterRecognitionPage extends AppCompatActivity {
    TextToSpeech t1, t2;
    ImageView imageView3;
    ImageButton selectBtn, translateBtn, backBtn, captureBtn;
    ArrayList<ModelLanguage> languageArrayList;
    Bitmap bitmap;
    TextView resultText;
    Button clearBtn, playBtn, stopBtn;
    LanguageIdentifier languageIdentifier;
    String sourceLanguageCode, sourceLanguageTitle, destinationLanguageCode, destinationLanguageTitle;
    TextRecognizer textRecognizer;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.characterrecognitionpage);

        backBtn = findViewById(R.id.backBtn);
        clearBtn = findViewById(R.id.clearBtn);
        imageView3 = findViewById(R.id.imageView3);
        selectBtn = findViewById(R.id.selectBtn);
        captureBtn = findViewById(R.id.captureBtn);
        translateBtn = findViewById(R.id.translateBtn);
        playBtn = findViewById(R.id.playBtn);
        resultText = findViewById(R.id.resultText);
        stopBtn = findViewById(R.id.stopBtn);

        Intent intent = getIntent();
        destinationLanguageCode = intent.getStringExtra("destinationLanguageCode");
        destinationLanguageTitle = intent.getStringExtra("destinationLanguageTitle");

        loadAvailableLanguages();

        languageIdentifier = LanguageIdentification.getClient();
        getLifecycle().addObserver(languageIdentifier);

        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        imageView3.setImageResource(R.drawable.placeholder1);

        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                    t1.speak("Input an image from text", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between clicks to be considered a double click
            private long lastClickTime = 0;

            @Override
            public void onClick(View view) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // Single click detected, clear the image and text
                    imageView3.setImageResource(R.drawable.placeholder1);
                    imageView3.setBackgroundResource(R.drawable.image_border);
                    resultText.setText("Enter image");
                    bitmap = null;
                    sourceLanguageTitle="";
                    sourceLanguageCode="";
                } else {
                    // Double click detected, perform double-click action

                    if (bitmap!=null){
                        t1.speak("Clear the image", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        t1.speak("No image to clear", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                lastClickTime = clickTime;
            }
        });

        captureBtn.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between clicks to be considered a double click
            private long lastClickTime = 0;

            @Override
            public void onClick(View view) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // Double click detected, perform double-click action
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 12);
                } else {
                    // Single click detected, start image capture activity
                    t1.speak("Capture an image", TextToSpeech.QUEUE_FLUSH, null);
                }
                lastClickTime = clickTime;
            }
        });

        selectBtn.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between clicks to be considered a double click
            private long lastClickTime = 0;

            @Override
            public void onClick(View view) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // Double click detected, perform double-click action
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 10);
                } else {
                    // Single click detected, start image selection activity
                    t1.speak("Select an image", TextToSpeech.QUEUE_FLUSH, null);
                }
                lastClickTime = clickTime;
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between clicks to be considered a double click
            private long lastClickTime = 0;

            @Override
            public void onClick(View view) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    if (t2 != null && t2.isSpeaking()) {
                        t2.stop();
                    }
                    if (bitmap==null){
                        return;
                    }
                    t2 = new TextToSpeech(characterRecognitionPage.this, status -> {
                        if (status != TextToSpeech.ERROR) {
                            int result = t2.setLanguage(new Locale(sourceLanguageCode));
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TextToSpeech", "Language not supported");
                            } else {
                                t2.speak(resultText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                            }
                        } else {
                            Log.e("TextToSpeech", "Initialization failed");
                        }
                    });
                } else {
                    if (bitmap==null) {
                        if (t1 != null) {
                            t1.speak("No translation to play", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    } else {
                        if (t1 != null) {
                            t1.speak("Play text to speech", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                }

                lastClickTime = clickTime;
            }
        });


        stopBtn.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between clicks to be considered a double click
            private long lastClickTime = 0;

            @Override
            public void onClick(View view) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    if (t2 != null && t2.isSpeaking()) {
                        t2.stop();
                    }
                } else {
                    if (bitmap == null) {
                        if (t1 != null) {
                            t1.speak("There is no speech to stop", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    } else {
                        if (t1 != null) {
                            t1.speak("Stop text to speech", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                }
                lastClickTime = clickTime;
            }
        });


        translateBtn.setOnClickListener(new View.OnClickListener(){
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between clicks to be considered a double click
            private long lastClickTime = 0;

            @Override
            public void onClick(View v){
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // Double click detected, perform double-click action
                    if (bitmap != null) {
                        Intent intent = new Intent(characterRecognitionPage.this, recognitionTranslatePage.class);
                        intent.putExtra("sourceLanguageCode", sourceLanguageCode);
                        intent.putExtra("resultText", resultText.getText().toString());
                        intent.putExtra("destinationLanguageCode", destinationLanguageCode);
                        intent.putExtra("destinationLanguageTitle", destinationLanguageTitle);
                        startActivity(intent);
                    } else {
                        t1.speak("No image to translate. Please select or capture an image first.", TextToSpeech.QUEUE_FLUSH, null);
                    }
                } else {
                    // Single click detected, ignore or perform single-click action
                    if (bitmap == null) {
                        t1.speak("No image to translate.", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        t1.speak("Translate", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                lastClickTime = clickTime;
            }
        });


    }

    private String getLanguageTitle(String languageCode) {
        for (ModelLanguage language : languageArrayList) {
            if (language.languageCode.equals(languageCode)) {
                return language.languageTitle;
            }
        }
        return languageCode;
    }

    void identifyLanguage(String inputText) {
        languageIdentifier.identifyLanguage(inputText)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String identifiedLanguage) {
                        sourceLanguageTitle = getLanguageTitle(identifiedLanguage);
                        sourceLanguageCode = identifiedLanguage;
                        t1.speak("The identified language is ", TextToSpeech.QUEUE_FLUSH, null);

                        // Introduce a delay before speaking the language title
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                t1.speak(sourceLanguageTitle, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }, 2000); // Delay in milliseconds (adjust as needed)
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(characterRecognitionPage.this, "Language was not identified.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void loadAvailableLanguages(){
        languageArrayList = new ArrayList<>();

        List<String> languageCodeList = TranslateLanguage.getAllLanguages();

        for(String languageCode: languageCodeList){
            String languageTitle = new Locale(languageCode).getDisplayLanguage();

            Log.d(TAG, "loadAvailableLanguages: languageCode: "+languageCode);
            Log.d(TAG, "loadAvailableLanguages: languageTitle: "+languageTitle);

            ModelLanguage modelLanguage = new ModelLanguage(languageCode, languageTitle);
            languageArrayList.add(modelLanguage);
        }
    }

    void getTextFromImage(Bitmap bitmap) {
        progressDialog.setMessage("Getting text from image...");
        progressDialog.show();
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        Task<Text> result = recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        resultText.setText(visionText.getText());
                        identifyLanguage(visionText.getText());
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Text recognition failed: " + e.getMessage(), e);
                        Toast.makeText(getApplicationContext(), "Text recognition failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    void getPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(characterRecognitionPage.this, new String[]{Manifest.permission.CAMERA}, 11);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==11){
            if(grantResults.length>0){
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    this.getPermission();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10 && data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    imageView3.setImageBitmap(bitmap);
                    imageView3.setBackgroundResource(R.drawable.image_border);
                    getTextFromImage(bitmap);
                    if (TextUtils.isEmpty(resultText.getText())) {
                        resultText.setText("No text is detected in this image.");
                    }
                } catch (IOException e) {
                }
            } else if (requestCode == 12 && data != null && data.getExtras() != null) {
                bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    imageView3.setImageBitmap(bitmap);
                    imageView3.setBackgroundResource(R.drawable.image_border);
                    getTextFromImage(bitmap);
                }
            }
        }
    }



}
