package de.work.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;



public class MainActivity extends Activity {

	public static final int REQUEST_ENABLE_BT = 1;
	public static final String ACTION_DISCOVERY_FINISHED = "android.bluetooth.adapter.action.DISCOVERY_FINISHED";
	
	public List<String> mArrayAdapter = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setupBluetooth();
		
	}
	
	public void setupBluetooth(){
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if(bluetoothAdapter == null){
			System.out.println("No Bluetooth supported");
		}
		
		if(!bluetoothAdapter.isEnabled()){
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		
		if(resultCode == RESULT_OK){
			System.out.println("Bluetooth enabled");
			
			// Suche nach gekoppelten Geräten starten
			findPairedDevices();
			
			// Suche nach verfügbaren Geräten starten
			findAvailableDevices();
						
			// Gerät Sichtbar machen
			enableBluetoothVisibility(300);
			
			// Daten über Bluetooth verschicken
        	sendBluetooth("/storage/extSdCard/test.jpg");
			
		}else if(resultCode == RESULT_CANCELED){
			System.out.println("Error");
		}
		
	}
	
	private void enableBluetoothVisibility(int i) {
		
		Intent discoverability = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverability.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, i);
		startActivity(discoverability);
		
	}

	private void findAvailableDevices() {
		
		if(BluetoothAdapter.getDefaultAdapter().startDiscovery()){
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			registerReceiver(mReceiver, filter);
			IntentFilter filter2 = new IntentFilter(ACTION_DISCOVERY_FINISHED);
			registerReceiver(discoveryReceiver, filter2);
		}
		
	}

	private void findPairedDevices() {
		
		Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		
		// If there are paired devices
		if (pairedDevices.size() > 0) {
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView
		        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		    }
		}
		
	}

	private void sendBluetooth(String uri) {
		
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		 
		if (btAdapter == null) {
		   System.out.println("No Bluetooth available");  
		 }
		
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("image/jpg");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + uri) );
		
		PackageManager pm = getPackageManager();
		List<ResolveInfo> appsList = pm.queryIntentActivities( intent, 0);
		 
		if(appsList.size() > 0 ){
			//select bluetooth
			String packageName = null;
			String className = null;
			boolean found = false;
			 
			for(ResolveInfo info: appsList){
			  packageName = info.activityInfo.packageName;
			  if( packageName.equals("com.android.bluetooth")){
			     className = info.activityInfo.name;
			     found = true;
			     break;// found
			  }
			}
			if(! found){
			  Toast.makeText(this, "Not Found!", Toast.LENGTH_SHORT).show();
			  // exit
			}
			
			//set our intent to launch Bluetooth
			intent.setClassName(packageName, className);
			
			startActivity(intent);
		}	
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
	            System.out.println("Geadded: " + device.getName());	
	        }
	    }
	};
	
	private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	    	
	        String action = intent.getAction();
	    	
	    	if(ACTION_DISCOVERY_FINISHED.equals(action)){
	        	System.out.println("Discovery finished");
	        	for(String s : mArrayAdapter){
	        		System.out.println(s);
	        	}
	        	
				unregisterReceiver(discoveryReceiver);
	        	BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
	        }
	    	
	    }
	};
}


