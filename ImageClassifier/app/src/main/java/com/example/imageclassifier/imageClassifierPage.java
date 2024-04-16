package com.example.imageclassifier;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
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

import com.example.imageclassifier.ml.MobilenetV110224Quant;
import com.example.imageclassifier.ml.MyNursingHomeImageClassifier;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;


public class imageClassifierPage extends AppCompatActivity {

    TextToSpeech t1, t2;
    ImageButton selectBtn, captureBtn, classifyBtn, backBtn;
    Button clearBtn, replayBtn;
    TextView result, translatedText;
    ImageView imageView3;
    Bitmap bitmap;
    String[] labels = new String[1001];

    String[] labels2 = new String[25];
    TranslatorOptions translatorOptions;
    Translator translator;
    ProgressDialog progressDialog;
    TextView translateLanguageTitle, resultTitle;
    static final String TAG = "MAIN_TAG";
    String destinationLanguageCode, destinationLanguageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageclassifierpage);

        getPermission();
        getClasses();

        backBtn = findViewById(R.id.backBtn);
        selectBtn = findViewById(R.id.selectBtn);
        captureBtn = findViewById(R.id.captureBtn);
        classifyBtn = findViewById(R.id.classifyBtn);
        clearBtn = findViewById(R.id.clearBtn);
        result = findViewById(R.id.result);
        imageView3 = findViewById(R.id.imageView3);
        translatedText = findViewById(R.id.translatedText);
        translateLanguageTitle = findViewById(R.id.translateLanguageTitle);
        resultTitle = findViewById(R.id.resultTitle);
        replayBtn = findViewById(R.id.replayBtn);

        Intent intent = getIntent();
        destinationLanguageCode = intent.getStringExtra("destinationLanguageCode");
        destinationLanguageTitle = intent.getStringExtra("destinationLanguageTitle");

        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                    t1.speak("Input an image. It is to be translated to ", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        // Put some delay in milliseconds
        int delayInMillis = 1000;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Initialize TextToSpeech with the destination language after the delay
                t2 = new TextToSpeech(imageClassifierPage.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            int result = t2.setLanguage(new Locale(destinationLanguageCode));
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TextToSpeech", "Language not supported");
                            } else {
                                t2.speak(destinationLanguageTitle, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        } else {
                            Log.e("TextToSpeech", "Initialization failed");
                        }
                    }
                });
            }
        }, delayInMillis);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        imageView3.setImageResource(R.drawable.placeholder1);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                    result.setText("Enter image");
                    translatedText.setText("Resulting Text");
                    bitmap = null;
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

        replayBtn.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between clicks to be considered a double click
            private long lastClickTime = 0;

            @Override
            public void onClick(View view) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // Double click detected, perform double-click action
                    t2.speak(translatedText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    // Single click detected, perform replay translation action
                    if (translatedText.getText().toString().isEmpty()) {
                        t1.speak("No translation to replay", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                lastClickTime = clickTime;
            }
        });



        classifyBtn.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between clicks to be considered a double click
            private long lastClickTime = 0;

            @SuppressLint("SetTextI18n")
            public void onClick(View view) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // Double click detected, perform double-click action
                    if (bitmap == null) {
                        Toast.makeText(imageClassifierPage.this, "Select an image first", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String classificationResult = classifyImage(bitmap);

                    result.setText(classificationResult);
                    resultToDestination(classificationResult);

                } else {
                    // Single click detected, perform classification
                    if (bitmap!=null){
                        if(translatedText.getText().toString().isEmpty()) {
                            t1.speak("Classify the image", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    } else {
                        t1.speak("No image to classify", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                lastClickTime = clickTime;
            }
        });


    }

    private void resultToDestination(String resultInEnglish) {
        progressDialog.setMessage("Processing language model...");
        progressDialog.show();

        translatorOptions = new TranslatorOptions.Builder()
                .setSourceLanguage("en")
                .setTargetLanguage(destinationLanguageCode)
                .build();
        translator = Translation.getClient(translatorOptions);

        DownloadConditions downloadConditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: model ready, starting to translate...");
                        progressDialog.setMessage("Translating...");

                        translator.translate(resultInEnglish)
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String translation) {
                                        Log.d(TAG, "onSuccess: translatedText: " + translation);
                                        progressDialog.dismiss();
                                        translatedText.setText(translation);
                                        t2.speak(translation, TextToSpeech.QUEUE_FLUSH, null);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Log.e(TAG, "Translation failed: " + e.getMessage(), e);
                                        Toast.makeText(imageClassifierPage.this, "Failed to translate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.e(TAG, "Model download failed: " + e.getMessage(), e);
                        Toast.makeText(imageClassifierPage.this, "Failed to download model: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

//    UNCOMMENT THIS IF YOU WANT TO USE THE IMAGENET DATASET
//    private String classifyImage(Bitmap bitmap) {
//        String resultString = ""; // Declare a variable to hold the result
//
//        try {
//            MobilenetV110224Quant model = MobilenetV110224Quant.newInstance(imageClassifierPage.this);
//
//            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);
//
//            bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
//            inputFeature0.loadBuffer(TensorImage.fromBitmap(bitmap).getBuffer());
//
//            MobilenetV110224Quant.Outputs outputs = model.process(inputFeature0);
//
//            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//
//            // Set the result string
//            resultString = labels[getMax(outputFeature0.getFloatArray())] + " ";
//
//            model.close();
//
//        } catch (IOException e) {
//            // Handle the exception
//        }
//
//        return resultString;
//    }

    // THIS classifyImage USES THE MYNURSINGHOME DATASET
    private String classifyImage(Bitmap bitmap) {
        String resultString = "";
        try {
            MyNursingHomeImageClassifier model = MyNursingHomeImageClassifier.newInstance(imageClassifierPage.this);
            int imageSize = 224;

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true);

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());
            for (int val : intValues) {
                byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255)); // Red
                byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));  // Green
                byteBuffer.putFloat((val & 0xFF) * (1.f / 255));         // Blue
            }

            inputFeature0.loadBuffer(byteBuffer);

            MyNursingHomeImageClassifier.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] probabilities = outputFeature0.getFloatArray();
            int maxIndex = getMax(probabilities);
            resultString = labels2[maxIndex] + " ";
            model.close();
        } catch (Exception e) {
            resultString = "Error: " + (e.getMessage() != null ? e.getMessage() : "Check log for details");
            e.printStackTrace();
        }

        return resultString;
    }

    //    UNCOMMENT THIS IF YOU WANT TO USE THE IMAGENET DATASET
//    private void getClasses() {
//        int count = 0;
//        try {
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));
//            String line = bufferedReader.readLine();
//            while(line != null){
//                labels[count] = line;
//                count++;
//                line = bufferedReader.readLine();
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    // THIS getClasses USES THE MYNURSINGHOME DATASET
    private void getClasses() {
        int count = 0;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("labels2.txt")));
            String line = bufferedReader.readLine();
            while(line != null){
                labels2[count] = line;
                count++;
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    int getMax(float[] arr){
        int max = 0;
        for(int i = 0; i<arr.length; i++){
            if(arr[i]>arr[max])
                max = i;
        }
        return max;
    }

    void getPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(imageClassifierPage.this, new String[]{Manifest.permission.CAMERA}, 11);
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
        if(requestCode==10){
            if(data!=null){
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    imageView3.setImageBitmap(bitmap);
                    imageView3.setBackgroundResource(R.drawable.image_border);
                    result.setText("");
                    translatedText.setText("");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else if(requestCode==12) {
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView3.setImageBitmap(bitmap);
            imageView3.setBackgroundResource(R.drawable.image_border);
        }
    }
}
