package com.example.imageclassifier;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.nl.translate.TranslateLanguage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class startPage extends AppCompatActivity {
    SpeechRecognizer speechRecognizer;
    ImageButton micBtn;
    Button languageBtn;
    Intent speechRecognizerIntent;
    TextToSpeech t1;

    ArrayList<ModelLanguage> languageArrayList;
    String destinationLanguageCode;
    String destinationLanguageTitle;

    private static final int RECORD_AUDIO_REQUEST_CODE = 1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startpage);

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            checkPermissions();
        }

        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        languageBtn = findViewById(R.id.languageBtn);
        micBtn = findViewById(R.id.micBtn);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!= TextToSpeech.ERROR){
                    t1.setLanguage(Locale.ENGLISH);
                    t1.speak("Pick a language.", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        loadAvailableLanguages();

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                String errorMessage;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        errorMessage = "Audio recording error";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        errorMessage = "Client side error";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        errorMessage = "Insufficient permissions";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        errorMessage = "Network error";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        errorMessage = "Network timeout";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        errorMessage = "Please try again. Your voice was not recognized.";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        errorMessage = "RecognitionService busy";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        errorMessage = "Server error";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        errorMessage = "No speech input";
                        break;
                    default:
                        errorMessage = "Unknown error";
                        break;
                }

                Toast.makeText(startPage.this, errorMessage, Toast.LENGTH_SHORT).show();
                t1.speak(errorMessage, TextToSpeech.QUEUE_FLUSH, null);
            }

            @Override
            public void onResults(Bundle results) {
                micBtn.setImageResource(R.drawable.mic_off);
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && !data.isEmpty()) {
                    String recognizedLanguage = data.get(0);

                    boolean isValidLanguage = isLanguageValid(recognizedLanguage);
                    if(isValidLanguage) {

                        languageBtn.setText(recognizedLanguage);

                        for (ModelLanguage language : languageArrayList) {
                            if (language.getLanguageTitle().equalsIgnoreCase(recognizedLanguage)) {
                                destinationLanguageCode = language.getLanguageCode();
                                destinationLanguageTitle = language.getLanguageTitle();
                                break;
                            }
                        }
                        t1.speak(recognizedLanguage, TextToSpeech.QUEUE_FLUSH, null);
                        // Delay before speaking the next message
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        // Speak the next message
                                        t1.speak("Click the button in the middle to proceed", TextToSpeech.QUEUE_FLUSH, null);
                                    }
                                }, 1000); // Delay in milliseconds
                    } else {
                        String errorMessage = "Invalid language. Please try again with a valid language.";
                        Toast.makeText(startPage.this, errorMessage, Toast.LENGTH_SHORT).show();
                        t1.speak(errorMessage, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }


            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        micBtn.setOnClickListener(new View.OnClickListener() {
            boolean isListening = false;

            @Override
            public void onClick(View v) {
                if (!isListening) {
                    // Stop the current speech if it's speaking
                    if (t1.isSpeaking()) {
                        t1.stop();
                    }

                    micBtn.setImageResource(R.drawable.mic_on);
                    speechRecognizer.startListening(speechRecognizerIntent);
                    isListening = true;
                    vibrator.vibrate(100);
                } else {
                    micBtn.setImageResource(R.drawable.mic_off);
                    speechRecognizer.stopListening();
                    isListening = false;
                    vibrator.vibrate(100);
                }
            }
        });


        languageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (destinationLanguageCode != null) {
                    // Create an intent to start the MainActivity
                    Intent intent = new Intent(startPage.this, MainActivity.class);
                    // Put extra values for destinationLanguageCode and sourceLanguageCode
                    intent.putExtra("destinationLanguageCode", destinationLanguageCode);
                    intent.putExtra("destinationLanguageTitle", destinationLanguageTitle);

                    vibrator.vibrate(200);
                    startActivity(intent);
                } else {
                    // If destinationLanguageCode is null, play an audio indicating no language has been selected
                    t1.speak("No language has been selected. Please select a destination language.", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });


    }

    private boolean isLanguageValid(String recognizedLanguage) {
        for (ModelLanguage language : languageArrayList) {
            if (language.getLanguageTitle().equalsIgnoreCase(recognizedLanguage)) {
                return true;
            }
        }
        return false;
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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        speechRecognizer.destroy();
    }

    void checkPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(startPage.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }
    }
}
