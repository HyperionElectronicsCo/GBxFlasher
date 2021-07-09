package com.mycompany.gbxflasher;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

	TextView mFw;
	TextView Tv1;
	TextView fSelect;
	TextView mDeviceText;
	UsbManager mUsbManager;
	UsbDevice mDevice;
	PendingIntent mPermissionIntent;

	Button mConnect;
	Button mReadInfo;
	Button mReadRom;
	//Button mWriteRom;
	Button mBackupSave;
	Button mRestoreSave;

	private static final int REQUEST_TYPE = 0x80;
	private static final int REQUEST = 0x06;
	private static final int REQ_VALUE = 0x200;
	private static final int REQ_INDEX = 0x00;
	private static final int LENGTH = 64;
	
	private static final String TAG = "UsbHost";
	private BroadcastReceiver detachReceiver;
	private String path;
	
	String[] permissions = new String[]{
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
	};

	private static final int PICKFILE_RESULT_CODE = 0;

	private Uri uri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		path = ("/storage/3AD8-565D/");
		
		Tv1 = findViewById(R.id.Tv1);
		mFw = findViewById(R.id.mFw);
		fSelect = findViewById(R.id.fSelect);
		mDeviceText = findViewById(R.id.text_status);
		mConnect = findViewById(R.id.button_connect);
		mReadInfo = findViewById(R.id.read_Info);
		mReadRom = findViewById(R.id.read_Rom);
		//mWriteRom = findViewById(R.id.write_Rom);
		mBackupSave = findViewById(R.id.backup_Save);
		mRestoreSave = findViewById(R.id.restore_Save);
		
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

		
	
		}
	private static final String ACTION_USB_PERMISSION = "com.mycompany.gbxflasher.USB_PERMISSION";
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    && device != null) {

					getDeviceStatus(device);
				} else {
					Log.d(TAG, "permission denied for device " + device);
				}
			}
		}
	};
	private void getDeviceStatus(UsbDevice device) {
		UsbDeviceConnection connection = mUsbManager.openDevice(device);
		byte[] buffer = new byte[LENGTH];
		connection.controlTransfer(REQUEST_TYPE, REQUEST, REQ_VALUE, REQ_INDEX,
								   buffer, LENGTH, 2000);
		connection.close();
	}

	private void updateDeviceList() {
		HashMap<String, UsbDevice> connectedDevices = mUsbManager
            .getDeviceList();
		if (connectedDevices.isEmpty()) {
			mDevice = null;
			mDeviceText.setText("No Devices Currently Connected");
			mConnect.setEnabled(false);
		} else {
			for (UsbDevice device : connectedDevices.values()) {
				mDevice = device;
			}
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
					public void run() {
						// Actions to do after 5 seconds
						
			mDeviceText.setText("USB Device connected");
			mConnect.setEnabled(true);
			Tv1.setBackgroundResource(R.drawable.ic_online);
			read();
					}
				}, 2000);
		}
		
		detachReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED))
					stopSelf();
			}

			private void stopSelf() {
				mDeviceText.setText("USB Device Disconnected");
				Tv1.setBackgroundResource(R.drawable.ic_offline);
				mFw.setText("N/A");
			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(detachReceiver, filter);
	}

	private void read() {
		//Read FW Info T-DRIVER.DMP
		StringBuilder text = new StringBuilder();
		try {
			File file = new File(path, "T-DRIVER.DMP");

			BufferedReader br = new BufferedReader(new FileReader(file));  
			String line;   
			while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
			}
			br.close() ;
		} catch (IOException e) {
			e.printStackTrace();           
		}
		mFw.setText(text.toString()); 
	}
	
	

	/* public void onConnectClick(View v) {
		mUsbManager.requestPermission(mDevice, mPermissionIntent);
		if (mDevice == null) {
			return;
		}
		mUsbManager.requestPermission(mDevice, mPermissionIntent);
	}
	*/
	public void writeROM(View v)
    {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        startActivityForResult(
            Intent.createChooser(chooseFile, "Choose a file"),
            PICKFILE_RESULT_CODE
        );
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == Activity.RESULT_OK){
            String path = new File(data.getData().getPath()).getAbsolutePath();

            if(path != null){
                uri = data.getData();

                String filename;
                Cursor cursor = getContentResolver().query(uri,null,null,null,null);

                if(cursor == null) filename=uri.getPath();
                else{
                    cursor.moveToFirst();
                    int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    filename = cursor.getString(idx);
                    cursor.close();
                }

                String name = filename.substring(0,filename.lastIndexOf("."));
                String extension = filename.substring(filename.lastIndexOf(".")+1);
                fSelect.setText(name + extension);
            }
		}
	}
	@Override
	protected void onResume() {
		super.onResume();

		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbReceiver, filter);
		updateDeviceList();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mUsbReceiver);
	}
}

	

	


	

