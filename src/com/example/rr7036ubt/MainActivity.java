package com.example.rr7036ubt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{
    private boolean mConnected = false;
    private String status="disconnected";
	private String mDeviceName;
    private String mDeviceAddress;
    private Bundle b;
    private boolean nConnect=false;
    private TextView rev_tv,connect_state;
    private Button getState;
    private EditText send_et;
    private ScrollView rev_sv;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    public static String RecvString="";
    public Object obtstr;
    public static MyService mys=new MyService();
    //public BluetoothGattCharacteristic target_chara2=null;
    private Handler myHandler = new Handler() {  
        //2.重写消息处理函数
        public void handleMessage(Message msg) {   
             switch (msg.what) {   
                  //判断发送的消息
	              case 0:
	              {
	            	  mleDeviceListAdapter.clear();
	            	  if(!mys.mBleArray.isEmpty()){
		            	  for(BluetoothDevice mdevice:mys.mBleArray)
		            	  {
		            		  mleDeviceListAdapter.addDevice(mdevice);
		            	  }
		            	  mleDeviceListAdapter.notifyDataSetChanged();
	            	  }
	            	 break;
	              }
                  case 1:   
                  {
                       connect_state.setText("connected");
                       break;   
                   }  
                  case 2:
                  {
                	  connect_state.setText("disconnected");
                      break;   
                  }
             }
             super.handleMessage(msg);   
        }  
        
   };  
   LeDeviceListAdapter mleDeviceListAdapter;
   ListView lv;
   private Button btOpen;
   private Button btSearch;
   private Button scan_btn;

   private boolean mScanning;
   private boolean scan_flag;
   int REQUEST_ENABLE_BT=1;
   public boolean stopdiscover=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent=new Intent(this,MyService.class);
        startService(intent);
		init();
		init_ble();
		scan_flag=true;
	    mleDeviceListAdapter=new LeDeviceListAdapter();
		lv.setAdapter(mleDeviceListAdapter); 
		/*listview点击函数*/
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0,  View v, int position, long id) {
				// TODO Auto-generated method stub
				final BluetoothDevice device = mleDeviceListAdapter.getDevice(position);
		        if (device == null) return;
		        mDeviceAddress= device.getAddress();
		        if (MyService.mScanning){
		        	 stopdiscover=false;
					 scanLeDevice(false);
					 scan_btn.setText("Scan BT");
					 scan_flag=true;
		        }
		        mys.ConnectBT(mDeviceAddress);
		        Thread thread=new Thread(new Runnable()  
	            {  
		        	boolean connect_state=true;
		        	int count=0;
	                @Override  
	                public void run()  
	                {  
	                	while(connect_state)
	                	{
	                		SystemClock.sleep(100);
	                		count++;
		                	if(mys.GetConnectState())
		                	{
		                		myHandler.removeMessages(1);
		                		myHandler.sendEmptyMessage(1);
		        				Intent intent = new Intent();
		        				intent.setClass(MainActivity.this, TabsActivity.class);
		        				startActivity(intent);
		                		break;
		                	}
		                	if(count==30)break;
	                	}
	                	if(!mys.GetConnectState())
	                	{
	                		myHandler.removeMessages(2);
	                		myHandler.sendEmptyMessage(2);
	                	}
	                }  
	            });  
	            thread.start();  
			}
		});
	}

	private void init()
	{
		scan_btn=(Button)this.findViewById(R.id.scan_dev_btn);
		scan_btn.setOnClickListener(this);
		lv=(ListView)this.findViewById(R.id.lv);
		connect_state=(TextView)this.findViewById(R.id.connect_state);
		connect_state.setText(status);
	}
	
	private void init_ble()
	{
		//手机硬件支持蓝牙
				if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
				    Toast.makeText(this, "Not support BLE", Toast.LENGTH_SHORT).show();
				    finish();
				}
				
				// Initializes Bluetooth adapter.
				//获取手机本地的蓝牙适配器
				final BluetoothManager bluetoothManager =
				        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
				mys.mBluetoothAdapter = bluetoothManager.getAdapter();
				
				// Ensures Bluetooth is available on the device and it is enabled. If not,
				// displays a dialog requesting user permission to enable Bluetooth.
				//打开蓝牙权限
				if (mys.mBluetoothAdapter == null || !mys.mBluetoothAdapter.isEnabled()) {
				    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
	}

	 
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		if(view ==scan_btn)
		{
			 if(scan_flag)
			 {
				 if(mys.nConnect)
				 {
					 mys.DisconnectBT(); 
				 }
				 scan_flag=false;
				 stopdiscover=true;
				 mleDeviceListAdapter=new LeDeviceListAdapter();
				 lv.setAdapter(mleDeviceListAdapter);
				 scanLeDevice(true);
				 scan_btn.setText("Stop");
				 Thread thread=new Thread(new Runnable()  
		            {  
		                @Override  
		                public void run()  
		                {  
		                	while(stopdiscover)
		                	{
		                		SystemClock.sleep(100);
			                	if(!mys.mBleArray.isEmpty())
			                	{
			                		myHandler.removeMessages(0);
			                		myHandler.sendEmptyMessage(0);
			                	}
		                	}
		                }  
		            });  
		            thread.start();  
			 }else{
				 scan_flag=true;
				 stopdiscover=false;
				 connect_state.setText("disconnected");
				 scanLeDevice(false);
				 scan_btn.setText("Scan BT");
			 }
		}
	}  
	
	/*扫描蓝牙设备      */
	private void scanLeDevice(final boolean enable) {
        mys.ScanBtDevice(enable);
    }
	
	 // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        
        private LayoutInflater mInflator;

        public LeDeviceListAdapter(){
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
          
            // General ListView optimization code.
           
            view = mInflator.inflate(R.layout.listitem, null);
            TextView deviceAddress = (TextView) view.findViewById(R.id.tv_deviceAddr);
            TextView deviceName = (TextView) view.findViewById(R.id.tv_deviceName);
              
            BluetoothDevice device = mLeDevices.get(i);
            deviceAddress.setText( device.getAddress());
            deviceName.setText(device.getName());
            return view;
        }
    }
	
	
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onPause();
		Intent intent=new Intent(this,MyService.class);
        stopService(intent);
	}


}
