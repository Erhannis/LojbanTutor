package com.erhannis.android.lojbantutor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.erhannis.lojban.core.Dictionary;
import com.erhannis.lojban.core.LearningManager;
import com.erhannis.lojban.core.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  protected EditText etText;

  protected Button btnReload;
  protected Button btnToBeginning;
  protected Button btnBack;
  protected Button btnPlayPause;
  protected Button btnForward;
  protected Button btnToEnd;

  protected Dictionary mDictionary;
  protected ReadingManager mReadingManager;
  protected LearningManager mLearningManager; //TODO Hmm.  Not convinced about these interactions.

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    etText = (EditText)findViewById(R.id.etText);

    btnReload = (Button)findViewById(R.id.btnReload);
    btnToBeginning = (Button)findViewById(R.id.btnToBeginning);
    btnBack = (Button)findViewById(R.id.btnBack);
    btnPlayPause = (Button)findViewById(R.id.btnPlayPause);
    btnForward = (Button)findViewById(R.id.btnForward);
    btnToEnd = (Button)findViewById(R.id.btnToEnd);

    mLearningManager = new LearningManager(); //TODO Allow save progress

    //TODO Handle errors
    try {
      List<String> dictLines = readAllLines(getResources().openRawResource(R.raw.jbo_eng_cleaned));
      mDictionary = new Dictionary(dictLines);

      String alis = readAll(getResources().openRawResource(R.raw.alis));
      etText.setText(alis);
      mReadingManager = new ReadingManager(this, mDictionary, mLearningManager, alis);
    } catch (IOException e) {
      e.printStackTrace();
    }

    btnReload.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mReadingManager.stop();

        String text = etText.getText().toString();
        mReadingManager = new ReadingManager(MainActivity.this, mDictionary, mLearningManager, text);
      }
    });

    btnToBeginning.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mReadingManager.backToBeginning();
      }
    });
    btnBack.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mReadingManager.back();
      }
    });
    btnPlayPause.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        switch (btnPlayPause.getText().toString()) {
          case "|>":
            mReadingManager.play();
            btnPlayPause.setText("||");
            break;
          case "||":
            mReadingManager.stop();
            btnPlayPause.setText("|>");
            break;
        }
      }
    });
    btnForward.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mReadingManager.forward();
      }
    });
    btnToEnd.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mReadingManager.forwardToEnd();
      }
    });
  }

  // https://stackoverflow.com/a/2549222/513038
  public static String readAll(InputStream stream) throws IOException {
    BufferedReader r = new BufferedReader(new InputStreamReader(stream));
    StringBuilder total = new StringBuilder();
    String line;
    while ((line = r.readLine()) != null) {
      total.append(line).append('\n');
    }
    return total.toString();
  }

  // kinda https://stackoverflow.com/a/2549222/513038
  public static List<String> readAllLines(InputStream stream) throws IOException {
    BufferedReader r = new BufferedReader(new InputStreamReader(stream));
    ArrayList<String> lines = new ArrayList<>();
    String line;
    while ((line = r.readLine()) != null) {
      lines.add(line);
    }
    return lines;
  }
}
