package com.example.alertapp;
import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class MyTTS {
    public TextToSpeech tts;
    public TextToSpeech.OnInitListener initListener=
            new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status==TextToSpeech.SUCCESS)
                            tts.setLanguage(Locale.US);
                }
            };
    public MyTTS(Context context){
        tts=new TextToSpeech(context,initListener);
    }

    //function for Text to Speach checks what language is set in application and sets proper accent
    public void speak(String whattospeak,int language){
        if(language==1) {
            tts.setLanguage(Locale.US);
            tts.speak(whattospeak,TextToSpeech.QUEUE_ADD,null);
            tts.speak(whattospeak,TextToSpeech.QUEUE_ADD,null);
        }
        else if(language==2){
            tts.setLanguage(Locale.FRENCH);
            tts.speak(whattospeak,TextToSpeech.QUEUE_ADD,null);
            tts.speak(whattospeak,TextToSpeech.QUEUE_ADD,null);
        }
        else{
            tts.setLanguage(Locale.ITALIAN);
            tts.speak(whattospeak,TextToSpeech.QUEUE_ADD,null);
            tts.speak(whattospeak,TextToSpeech.QUEUE_ADD,null);
        }

    }
    //stops speach
    public void stop_speak(){
        if(tts != null){
            tts.stop();
        }
    }
}
