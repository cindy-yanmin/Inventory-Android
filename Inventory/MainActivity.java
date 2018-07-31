package com.mind.inventory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;

public class MainActivity extends AppCompatActivity {

    final int numTrackKeep = 5;
    String labelSize;
    String labelContent;
    String theAddress;

    Button onSetting;
    Button onEmail;
    Button onButton;
    EditText shipper;
    EditText track;
    EditText size;
    EditText weight;
    EditText id;
    EditText date;
    EditText loc;
    TextView result;

    protected void setPrinterVariables(){
        String setting = "";
        try{
            FileReader fr = new FileReader(getFilesDir()+"/setting.txt");
            int i;
            while ((i=fr.read()) != -1) {
                setting += (char) i;
            }
            fr.close();
        } catch (Exception e){ }

        if (setting.length()>3) {
            switch (setting.substring(0, 1)) {
                case "S":
                    labelSize = "228";
                    break;
                case "L":
                    labelSize = "285";
                    break;
            }
            labelContent = setting.substring(1, 2);
            theAddress = setting.substring(2);
        }
        else {
            result.setText ("RESET PRINTER SETTINGS");
        }
    }

    protected void onSave(String [] row){
        // Create the content to be saved
        String fileContents = "\n";
        for (String i: row) {
            fileContents += (i+",");
        }
        // Save to inventory.txt
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput("inventory.txt", Context.MODE_APPEND);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {}
    }

    protected void onPrint(final String [] row){
        new Thread(new Runnable() {
            public void run() {
                try {
                    // Instantiate connection for given Bluetooth&reg; MAC Address.
                    Connection thePrinterConn = new BluetoothConnection(theAddress);
                    // Initialize
                    Looper.prepare();
                    // Open the connection - physical connection is established here.
                    thePrinterConn.open();

                    String data = "! 200 200 200 "+ labelSize + " 1"; //Horizontal dpi, Vertical dpi, LABEL HEIGHT, # OF COPIES
                    switch (labelContent) {
                        case "T": //Print TEXT
                            data += "\r\nT 4 0 30 1 TRACK: ***" + row[11] //FONT, SIZE, X, Y
                                    + "\r\nT 4 0 30 40 ID: " + row[7]
                                    + "\r\nT 4 0 30 80 LOC: " + row[10];
                            break;
                        case "B": //Print BARCODE
                            data += "\r\nB 128 3.9 1 105 30 1 " + row[11] //TYPE, WIDTH, RATIO, HEIGHT, X, Y
                                    + "\r\nT 4 0 30 110 ID: " + row[7] + " LOC: " + row[10];
                            break;
                    }
                    // Send the data to printer as a byte array.
                    thePrinterConn.write((data+"\r\nPRINT\r\n").getBytes());

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(500);

                    // Close the connection to release resources.
                    thePrinterConn.close();

                    Looper.myLooper().quit();
                } catch (Exception e) {
                    result.setText("CANNOT");
                }
            }
        }).start();
    }

    protected void sendEmail() {
        // Get data from txt file
        String data = "运输方式,运单号,入仓箱型, 入仓体积(立方米),体积重(千克KG)," +
                "入仓实重(磅LB),入仓实重(千克KG),收货人编号,收货日期,服务类型,箱子位置,货物编号";
        try{
            FileReader fr = new FileReader(getFilesDir()+"/inventory.txt");
            int i;
            while ((i=fr.read()) != -1) {
                data += (char) i;
            }
        } catch (Exception e){ }

        // Create csv file
        File file = new File(getExternalFilesDir(null), "inventory.csv");
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

        // Send the csv file
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String [] {"cf1265700151@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "Inventory.csv");
        email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        email.setType("text/plain");
        try {
            startActivity(Intent.createChooser(email, "Send mail..."));
            finish();
        } catch (Exception e) { }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set local variables
        result = (TextView) findViewById(R.id.result);
        onSetting= (Button) findViewById(R.id.setting);
        onEmail = (Button) findViewById(R.id.email);
        onButton = (Button) findViewById(R.id.button);
        shipper = (EditText) findViewById(R.id.shipper);
        track = (EditText) findViewById(R.id.tracking);
        size = (EditText) findViewById(R.id.size);
        weight = (EditText) findViewById(R.id.weight);
        id = (EditText) findViewById(R.id.id);
        date = (EditText) findViewById(R.id.date);
        loc = (EditText) findViewById(R.id.location);

        // Define the setting variables
        setPrinterVariables();

        onSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Setting Activity
                Intent [] intent = {new Intent(MainActivity.this, Settings.class)};
                startActivities(intent);
            }
        });

        onEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Email CSV
                sendEmail();
            }
        });

        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tracking = track.getText().toString();
                String [] newInventory = new String [] {
                        shipper.getText().toString(), tracking,
                        size.getText().toString(), "", "",
                        weight.getText().toString(), "",
                        id.getText().toString(),
                        date.getText().toString(), "",
                        loc.getText().toString(), "InvID"};

                if (newInventory[0].length() == 0 ||
                        newInventory[1].length() == 0 ||
                        newInventory[2].length() == 0 ||
                        newInventory[7].length() == 0 ||
                        newInventory[8].length() == 0 ||
                        newInventory[10].length() == 0){
                    result.setText ("FILL IN EVERYTHING");
                }
                else if (tracking.length() < numTrackKeep) {
                    result.setText ("INVALID TRACKING");
                }
                else {
                    // Save Inventory to CSV
                    newInventory[11] = tracking.substring(tracking.length() - numTrackKeep); // Fix InvID
                    onSave(newInventory);
                    // Print Inventory to CSV
                    onPrint(newInventory);
                    // Show Result
                    track.setText("");
                    weight.setText("");
                    id.setText("");
                    result.setText("SAVED + PRINTED");
                }
            }
        });
    }
}