package com.mycompany.gbxflasher;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.InputStream;
import java.io.IOException;
import android.os.Environment;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

public class MainActivity extends AppCompatActivity {
	
	private static final String TAG = "UsbHost";
	TextView mFw;
	TextView Tv1;
	TextView mDeviceText;
	UsbManager mUsbManager;
	UsbDevice mDevice;
	PendingIntent mPermissionIntent;
	
	Button mConnect;
	Button mReadInfo;
	Button mReadRom;
	Button mWriteRom;
	Button mBackupSave;
	Button mRestoreSave;
	
	private static final int REQUEST_TYPE = 0x80;
	private static final int REQUEST = 0x06;
	private static final int REQ_VALUE = 0x200;
	private static final int REQ_INDEX = 0x00;
	private static final int LENGTH = 64;

	private String path;
	
	String[] permissions = new String[]{
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
	};
	
	private BroadcastReceiver detachReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Tv1 = (TextView) findViewById(R.id.Tv1);
		mDeviceText = (TextView) findViewById(R.id.text_status);
		mFw = (TextView) findViewById(R.id.mFw);
		
		mConnect = (Button) findViewById(R.id.button_connect);
		mReadInfo = (Button) findViewById(R.id.read_Info);
		mReadRom = (Button) findViewById(R.id.read_Rom);
		mWriteRom = (Button) findViewById(R.id.write_Rom);
		mBackupSave = (Button) findViewById(R.id.backup_Save);
		mRestoreSave = (Button) findViewById(R.id.restore_Save);
		
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		
		
		path = ("/storage/3AD8-565D/");

    
		
		mReadInfo.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// TODO Auto-generated method stub
					read();
				}
			});
		mReadRom.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// TODO Auto-generated method stub
				}
			});
		mWriteRom.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// TODO Auto-generated method stub
				}
			});
		mBackupSave.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// TODO Auto-generated method stub
				}
			});
		mRestoreSave.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// TODO Auto-generated method stub
				}
			});
			
			
			
		detachReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED))
					stopSelf();
			}

			private void stopSelf() {
				mDeviceText.append("\n" + "Disconnected");
				Tv1.setBackgroundResource(R.drawable.ic_offline);
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(detachReceiver, filter);
	
	}
	private boolean checkPermissions() {
		int result;
		List<String> listPermissionsNeeded = new ArrayList<>();
		for (String p : permissions) {
			result = ContextCompat.checkSelfPermission(this, p);
			if (result != PackageManager.PERMISSION_GRANTED) {
				listPermissionsNeeded.add(p);
			}
		}
		if (!listPermissionsNeeded.isEmpty()) {
			ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
			return false;
		}
		return true;
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (requestCode == 100) {
			if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// do something
				
				
			}
			return;
		}
	}
	
	private void read() {
		//Read FW Info T-DRIVER.DMP
		StringBuilder text = new StringBuilder();
		try {
			
			File file = new File(path,"T-DRIVER.DMP");

			BufferedReader br = new BufferedReader(new FileReader(file));  
			String line;   
			while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
			}
			br.close() ;
		}catch (IOException e) {
			e.printStackTrace();           
		}

		
		mFw.setText(text.toString()); ////Set the text to text view.
		}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		recreate();
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

	public void onConnectClick(View v) {
		if (mDevice == null) {
			return;
		}
		mUsbManager.requestPermission(mDevice, mPermissionIntent);
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
			mDeviceText.setText("USB Device connected");
			mConnect.setEnabled(true);
			Tv1.setBackgroundResource(R.drawable.ic_online);
			checkPermissions();
			read();
		}
	}
}
