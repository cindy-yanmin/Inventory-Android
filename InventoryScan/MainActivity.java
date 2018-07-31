package com.mind.android.invscan;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    Button email;
    Button search;
    EditText track;
    TextView result;
    MediaPlayer player;

    public void play(boolean isFound) {
        if (isFound)
            player = MediaPlayer.create(this, R.raw.correct);
        else
            player = MediaPlayer.create(this, R.raw.incorrect);
        player.start();
    }

    protected String readCSV () {
        String csvFile = "";
        try{
            FileReader fr = new FileReader(new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "TEST.csv"));
            int i;
            while ((i=fr.read()) != -1) {
                csvFile += (char) i;
            }
            fr.close();
        } catch (Exception e){ }
        return csvFile;
    }

    protected void newCSV (String[] tokens) {
        String data = "";
        for (String i: tokens) {
            data += (i + ",");
        }
        Log.i("++",data);
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "TEST.csv");
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(data.getBytes());
        } catch (IOException e) {
        } finally {
            try{
                os.close();
            } catch (Exception e) {}
        }
    }

    protected void sendEmail() {

        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "TEST.csv");
        // Send the csv file
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String [] {"cf1265700151@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "InventoryScan.csv");
        email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        email.setType("text/plain");
        try {
            startActivity(Intent.createChooser(email, "Send mail..."));
            finish();
        } catch (Exception e) { }
    }


    public void showPopup(View v, String toBeDisplayed) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pop_up, null);

        TextView display = (TextView) popupView.findViewById(R.id.display);
        display.setText(toBeDisplayed);

        // create the popup window
        boolean focusable = true; // let taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, 450, 500, focusable);
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        Button btn = (Button) popupView.findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = (Button) findViewById(R.id.email);
        search = (Button) findViewById(R.id.search);
        track = (EditText) findViewById(R.id.track);
        result = (TextView) findViewById(R.id.result);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Email CSV
                sendEmail();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = track.getText().toString();
                if (input.length() < 5)
                    result.setText("INVALID INPUT");
                else {
                    String tokens[] = readCSV().split(",");
                    boolean found = false;
                    int index;
                    for (index = 0; index < tokens.length; index++) {
                        if ((tokens[index]).toUpperCase().equals(input.toUpperCase())) {
                            found = true;
                            break;
                        }
                    }
                    // Play the Sound
                    play(found);
                    // Show the Result
                    if (tokens.length == 1)
                        result.setText("NO FILE / EMPTY FILE");
                    else if (! found)
                        result.setText("NOT FOUND");
                    else {
                        String toBeDisplayed = "";
                        if (input.length() == 5) {
                            toBeDisplayed += tokens[index - 1];
                            index += 2;
                        }
                        else {
                            toBeDisplayed += input;
                            index += 3;
                        }
                        showPopup(v, toBeDisplayed);
                        tokens[index] = "FOUND";
                        newCSV(tokens);
                        result.setText("");
                    }
                }
                track.setText("");
            }
        });
    }
}
