package com.mycompany.gbxflasher;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.IntentFilter;
import android.content.Intent;
import android.widget.Toast;
import android.widget.TextView;
import android.os.Handler;
import android.hardware.usb.UsbManager;
import java.util.Map;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Looper;

public class MainActivity extends AppCompatActivity {
    
	TextView cartInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		final TextView cartInfo = findViewById(R.id.text_status);
		
		final Handler handler = new Handler(Looper.getMainLooper());
		final Context context = getApplicationContext();
		handler.post(new Runnable() {
				@Override
				public void run() {
					cartInfo.setText("Initialized...");
					handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								cartInfo.setText("");
							}
						}, 3000);
				}
			});
	
        /*UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
		Intent intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
		intent.addCategory("android.hardware.usb.action.USB_DEVICE_DETACHED");
		Map<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
		Toast.makeText(this, "Deivce List Size: " + usbDeviceList.size(), Toast.LENGTH_SHORT).show();

		if (usbDeviceList.size() > 0) {

			//vid= vendor id .... pid= product id
      //      Toast.makeText(this, "device pid: " + device.getProductId() + " Device vid: " + device.getVendorId(), Toast.LENGTH_SHORT).show();

        } */
		
		
		
		
    }
    
    
}
