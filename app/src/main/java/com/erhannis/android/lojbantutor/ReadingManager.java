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
    this.mLines = LojbanTextUtils.lojbanToSentences(LojbanTextUtils.clean(text));

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
        if ("readLojban1".equals(utteranceId)) {
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

  protected static final boolean READ_SENTENCE_BEFORE_DEFINITIONS = true;
  protected static final boolean READ_SPELLING = true;

  protected void playCurLine() {
    if (mCurLine < mLines.length) {
      if (mCurLine < 0) {
        mCurLine = 0;
      }
      String line = mLines[mCurLine];
      System.out.println("line " + line);
      String[] words = LojbanTextUtils.sentenceToWords(line);

      tts.speak("", TextToSpeech.QUEUE_FLUSH, null, "flushQueue");

      for (int i = 0; i < words.length; i++) {
        String word = words[i];
        System.out.println("word_" + i + " : " + word);
        if (mLearningManager.shouldGiveMeaning(word)) {
          if (READ_SENTENCE_BEFORE_DEFINITIONS) {
            readLojban(line, "readLojban0_" + i);
          }

          readLojban(word + " .", "readWord1_" + i);
          if (READ_SPELLING) {
            readSpellingEnglish(word, "spellWord_" + i);
          }
          readLojban(word + " .", "readWord2_" + i);

          readEnglish(mDictionary.getMeaning(word) + " . ", "readMeaning_" + i);

          readLojban(word + " .", "readWord3_" + i);
          mLearningManager.meaningGiven(word); //TODO Should come after actually given?
          //TODO Note that this may mess up the current sentence when hit back.
        }
      }

      readLojban(line, "readLojban1");
    }
  }

  protected void readEnglish(String text, String id) {
    tts.setPitch(1.5f);
    tts.setSpeechRate(0.85f);
    tts.setLanguage(EN_US);
    tts.speak(text, TextToSpeech.QUEUE_ADD, null, id);
  }

  protected void readLojban(String text, String id) {
    tts.setPitch(1.0f);
    tts.setSpeechRate(0.5f);
    tts.setLanguage(JBO);
    tts.speak(text, TextToSpeech.QUEUE_ADD, null, id);
  }

  protected void readSpellingEnglish(String word, String id) {
    tts.setPitch(1.5f);
    tts.setSpeechRate(0.85f);
    tts.setLanguage(EN_US);
    StringBuilder sb = new StringBuilder();
    for (char c : word.toCharArray()) {
      if (c == '\'') {
        sb.append(", apostrophe");
      } else {
        sb.append(", " + c);
      }
    }
    tts.speak(sb.toString(), TextToSpeech.QUEUE_ADD, null, id);
  }
}
