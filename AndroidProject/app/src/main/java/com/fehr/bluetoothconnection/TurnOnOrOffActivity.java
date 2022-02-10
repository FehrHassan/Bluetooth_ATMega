package com.fehr.bluetoothconnection;

import androidx.appcompat.app.AppCompatActivity;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.UUID;

public class TurnOnOrOffActivity extends AppCompatActivity {

    String address = null;
    ToggleButton onOff;
    Button offButton, fiftyButton, hundredButton, getTemperature;
    TextView receivedText, tempertatureTextView;

    private ConnectedThread mConnectedThread;
    Handler bluetoothIn;

    public final int handlerState = 0;                        //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private StringBuilder recDataString = new StringBuilder();




    Toast mToast;

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turn_on_or_off);

        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device




        receivedText = findViewById(R.id.text_received_from_BT);
        tempertatureTextView = findViewById(R.id.temperature_text_view);
        onOff = findViewById(R.id.button);
        onOff.setChecked(false);
        offButton = findViewById(R.id.off);
        fiftyButton = findViewById(R.id.fifty);
        hundredButton = findViewById(R.id.hundred);
        getTemperature = findViewById(R.id.getTemperature);

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        device.createBond();
//        if (device.createBond())
//        {
            try {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                msg("Socket creation failed");
            }
            // Establish the Bluetooth socket connection.
            try
            {
                btSocket.connect();

            } catch (IOException e) {
                try
                {
                    btSocket.close();
                } catch (IOException e2)
                {
                    //insert code to deal with this
                }
            }

            mConnectedThread = new ConnectedThread(btSocket);
            mConnectedThread.start();

            bluetoothIn = new MessageHandler(handlerState,recDataString,receivedText,tempertatureTextView, onOff);

            getStatus();

//        } else {
//            msg("not bond");
//           // msg("Failed to connect");
//        }

        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                {
                    if (btSocket!=null)
                    {
                        mConnectedThread.write((byte)1);    // Send "1" via Bluetooth
                    }
                    else
                    {
                        msg("Failed to connect");
                    }
                }
                else
                {
                    if (btSocket!=null)
                    {
                        mConnectedThread.write((byte)0);    // Send "0" via Bluetooth
                    }
                    else
                    {
                        msg("Failed to connect");
                    }
                }
            }
        });

        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btSocket!=null)
                {
                    mConnectedThread.write((byte) 2);    // Send "2" via Bluetooth
                }
                else
                {
                    msg("Failed to connect");
                }
            }
        });

        fiftyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btSocket!=null)
                {
                    mConnectedThread.write((byte)50);    // Send "50" via Bluetooth
                }
                else
                {
                    msg("Failed to connect");
                }
            }
        });

        hundredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btSocket!=null)
                {
                    mConnectedThread.write((byte) 100);    // Send "100" via Bluetooth
                }
                else
                {
                    msg("Failed to connect");
                }
            }
        });

        getTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btSocket!=null)
                {
                    mConnectedThread.write((byte) 3);    // Send "3" via Bluetooth
                }
                else
                {
                    msg("Failed to connect");
                }
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(myUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (btSocket != null)
        {
            try
            {
                //Don't leave Bluetooth sockets open when leaving activity
                btSocket.close();
                if (mConnectedThread != null)
                {
                    mConnectedThread.interrupt();
                    mConnectedThread = null;
                }
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }
    }

    private void getStatus()
    {
        if (btSocket!=null)
        {
            mConnectedThread.write((byte) 4);    // Send "100" via Bluetooth
        }
    }

    // fast way to call Toast
    private void msg(String s)
    {
        if (mToast != null)
        {
            mToast.cancel();
            mToast = Toast.makeText(TurnOnOrOffActivity.this,s,Toast.LENGTH_SHORT);
            mToast.show();

        } else
        {
            mToast = Toast.makeText(TurnOnOrOffActivity.this,s,Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        private ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {


            // Keep looping to listen for received messages
            while (true) {
                try {
                    int bytes = mmInStream.available();
                    if (bytes > 0) {
                        byte[] buffer = new byte[bytes];
                        bytes = mmInStream.read(buffer,0,bytes);           //read bytes from input buffer
                        if(bytes != -1) {
                            String readMessage = new String(buffer, 0, bytes);
                            // Send the obtained bytes to the UI Activity via handler
                            bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        private void write(byte input) {
            //byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(input);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

    private static class MessageHandler extends Handler {

        int handlerState;
        StringBuilder recDataString;
        TextView buttonStateText;
        TextView tempertatureText;
        ToggleButton toggleButton;

        private MessageHandler (int handlerState, StringBuilder recDataString, TextView buttonStateText, TextView tempertatureText, ToggleButton toggleButton) {
            this.handlerState = handlerState;
            this.recDataString = recDataString;
            this.buttonStateText = buttonStateText;
            this.tempertatureText = tempertatureText;
            this.toggleButton = toggleButton;
        }

        public void handleMessage(android.os.Message msg) {
            if (msg.what == handlerState) {                                     //if message is what we want
                String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                recDataString.append(readMessage);                                      //keep appending to string until ~
                int endOfLineIndex = recDataString.indexOf("\n");                    // determine the end-of-line
                if (endOfLineIndex > 0) {                                           // make sure there data before ~
                    int i = 0;
                    while (recDataString.charAt(i) != '#'){
                        i++;
                        if (i >= endOfLineIndex) {
                            break;
                        }
                    }
                    if (i < endOfLineIndex) {
                        i++;
                        if (recDataString.charAt(i) == 'B'){
                            String dataInPrint = recDataString.substring(i, endOfLineIndex);    // extract string

                            if (dataInPrint.equals("BP")){
                                dataInPrint = "Button is pressed";
                            } else if (dataInPrint.equals("BR")){
                                dataInPrint = "Button is released";
                            }
                            buttonStateText.setText(dataInPrint);

                        } else if (recDataString.charAt(i) == 'T') {
                            i++;
                            String dataInPrint = "Temp is: " + recDataString.substring(i, endOfLineIndex);    // extract string
                            tempertatureText.setText(dataInPrint);

                        } else if(recDataString.charAt(i) == 'L') {
                            String dataInPrint = recDataString.substring(i, endOfLineIndex);    // extract string

                            if (dataInPrint.equals("LN")){
                                toggleButton.setChecked(true);
                            } else if (dataInPrint.equals("LF")){
                                toggleButton.setChecked(false);
                            }
                        }
                    }
                    recDataString.delete(0, recDataString.length());                    //clear all string data

                }
            }
        }
    }
}
