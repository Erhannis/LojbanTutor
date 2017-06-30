package com.erhannis.android.lojbantutor;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;

import com.erhannis.lojban.core.Dictionary;
import com.erhannis.lojban.core.LearningManager;
import com.erhannis.lojban.core.LojbanTextUtils;

import java.io.File;
import java.util.Locale;

/**
 * Created by erhannis on 6/21/17.
 */

public class ReadingManager {
  protected static final Locale JBO = new Locale("jbo");
  protected static final Locale EN_US = new Locale("en_US");

  protected final Dictionary mDictionary;
  protected final LearningManager mLearningManager;
  protected final String[] mLines;
  protected int mCurLine = 0;
  protected boolean mPlaying = false;
  protected TextToSpeech tts;

  public ReadingManager(Context ctx, Dictionary dictionary, LearningManager learningManager, String text) {
    this.mDictionary = dictionary;
    this.mLearningManager = learningManager;
    this.mLines = LojbanTextUtils.lojbanToSentences(text);

    tts = new TextToSpeech(ctx, new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        System.out.println("tts init'd");

        for (Locale l : tts.getAvailableLanguages()) {
          System.out.println("locale " + l);
        }

        tts.setLanguage(JBO);

        for (Voice v : tts.getVoices()) {
          System.out.println("Voice " + v);
        }

        System.out.println("Current voice: " + tts.getVoice());

        tts.setSpeechRate(0.5f);
      }
    }, "com.reecedunn.espeak");

    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
      @Override
      public void onStart(String utteranceId) {

      }

      @Override
      public void onDone(String utteranceId) {
        System.out.println("onDone " + utteranceId);
        if ("readLojban".equals(utteranceId)) {
          if (mPlaying) {
            mCurLine++;
          }
          playCurLine();
        }
      }

      @Override
      public void onError(String utteranceId) {

      }
    });
  }

  public void play() {
    mPlaying = true;
    playCurLine();
  }

  public void stop() {
    mPlaying = false;
    tts.stop();
  }

  public void back() {
    //TODO Do
  }

  public void forward() {
    //TODO Do
  }

  public void backToBeginning() {
    mCurLine = -1;
    if (mPlaying) {
      playCurLine();
    }
  }

  public void forwardToEnd() {
    //TODO Do
  }

  protected void playCurLine() {
    if (mCurLine < mLines.length) {
      if (mCurLine < 0) {
        mCurLine = 0;
      }
      String line = mLines[mCurLine];
      String[] words = LojbanTextUtils.sentenceToWords(line);

      tts.speak("", TextToSpeech.QUEUE_FLUSH, null, "flushQueue");
      tts.setPitch(1.0f);
      tts.setSpeechRate(0.5f);

      for (int i = 0; i < words.length; i++) {
        String word = words[i];
        if (mLearningManager.shouldGiveMeaning(word)) {
          tts.setPitch(1.0f);
          tts.setSpeechRate(0.5f);
          tts.setLanguage(JBO);
          tts.speak(word + " .", TextToSpeech.QUEUE_ADD, null, "readWord_a_" + i);
          tts.speak(word + " .", TextToSpeech.QUEUE_ADD, null, "readWord_b_" + i);

          tts.setLanguage(EN_US);
          tts.setPitch(1.5f);
          tts.setSpeechRate(0.85f);
          tts.speak(mDictionary.getMeaning(word) + " . ", TextToSpeech.QUEUE_ADD, null, "readMeaning" + i);

          tts.setPitch(1.0f);
          tts.setSpeechRate(0.5f);
          tts.setLanguage(JBO);
          tts.speak(word + " .", TextToSpeech.QUEUE_ADD, null, "readWord_c_" + i);
          mLearningManager.meaningGiven(word); //TODO Should come after actually given?
          //TODO Note that this may mess up the current sentence when hit back.
        }
      }

      tts.setLanguage(JBO);
      tts.speak(line, TextToSpeech.QUEUE_ADD, null, "readLojban");
    }
  }
}
