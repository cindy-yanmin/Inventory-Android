package com.mind.inventory;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;

import java.io.FileOutputStream;
import java.io.FileReader;

public class Settings extends AppCompatActivity {

    Button onNew;
    Button onApply;
    RadioGroup size;
    RadioGroup content;
    EditText address;
    TextView result;

    protected void addMACAddress() {
        String setting = "";
        try{
            FileReader fr = new FileReader(getFilesDir()+"/setting.txt");
            int i;
            while ((i=fr.read()) != -1) {
                setting += (char) i; }
            fr.close();
        } catch (Exception e){}
        address.setText(setting.substring(2));
    }

    protected void saveSettings() {
        String settings = "";
        switch (size.getCheckedRadioButtonId()) {
            case R.id.small:
                settings += "S";
                break;
            case R.id.large:
                settings += "L";
                break;
            default:
                settings += "S";
                break;
        }
        switch (content.getCheckedRadioButtonId()) {
            case R.id.text:
                settings += "T";
                break;
            case R.id.barcode:
                settings += "B";
                break;
            default:
                settings += "T";
                break;
        }
        settings += address.getText().toString();
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput("setting.txt", Context.MODE_PRIVATE);
            outputStream.write(settings.getBytes());
            outputStream.close();
        } catch (Exception e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set variables
        onNew = (Button) findViewById(R.id.newFile);
        onApply = (Button) findViewById(R.id.apply);
        size = (RadioGroup) findViewById(R.id.size);
        content = (RadioGroup) findViewById(R.id.content);
        address = (EditText) findViewById(R.id.address);
        result = (TextView) findViewById(R.id.result);

        addMACAddress();

        onNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileOutputStream outputStream;
                try {
                    outputStream = openFileOutput("inventory.txt", Context.MODE_PRIVATE);
                    outputStream.write("".getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    result.setText (e.getMessage());
                }
                result.setText ("New File Created");
            }
        });

        onApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (size.getCheckedRadioButtonId() == -1) {
                    result.setText("Choose a Label Size");
                } else if (content.getCheckedRadioButtonId() == -1) {
                    result.setText("Choose a Content Type");
                } else {
                    try {
                        Connection thePrinterConn = new BluetoothConnection(address.getText().toString());
                        thePrinterConn.open();
                        thePrinterConn.close();

                        saveSettings();

                        // Open Main Activity
                        Intent[] intent = {new Intent(Settings.this, MainActivity.class)};
                        startActivities(intent);
                    } catch (ConnectionException e) {
                        result.setText(e.getMessage());
                    }
                }
            }
        });
    }
}