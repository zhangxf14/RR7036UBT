package com.example.rr7036ubt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Iso15693Activity extends Activity implements OnClickListener, OnItemClickListener{
	private String mode;
	private Map<String,Integer> data;
	
	Button scan;
	ListView listView;
	TextView txNum;
	static Map<String, Integer> scanResult = new HashMap<String, Integer>();
	static Map<String, byte[]> epcBytes = new HashMap<String, byte[]>();
	public static Timer timer;
	private MyAdapter myAdapter;
	private Handler mHandler;
	private boolean isCanceled = true;
	private static final int SCAN_INTERVAL = 50;
	
	private static final int MSG_UPDATE_LISTVIEW = 0;
	private boolean Scanflag=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_iso15693);
		scan = (Button)findViewById(R.id.button_scanrr9);
		scan.setOnClickListener(this);
		listView = (ListView)findViewById(R.id.listrr9);//
		listView.setOnItemClickListener(this);
		data = new HashMap<String, Integer>();
		txNum = (TextView)findViewById(R.id.tx_numrr9);
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if(isCanceled) return;
				switch (msg.what) {
				case MSG_UPDATE_LISTVIEW:
					data = scanResult;
					if(myAdapter == null){
						myAdapter = new MyAdapter(Iso15693Activity.this, new ArrayList(data.keySet()));
						listView.setAdapter(myAdapter);
					}else{
						myAdapter.mList = new ArrayList(data.keySet());
					}
					txNum.setText(String.valueOf(myAdapter.getCount()));
					myAdapter.notifyDataSetChanged();
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
		
		
	}
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Thread thread=new Thread(new Runnable()  
        {  
            @Override  
            public void run()  
            { 
            	BTClient.ChangeTo15693();
            }  
        }); 
		thread.start();  
	}
class MyAdapter extends BaseAdapter{
		
		private Context mContext;
		private List<String> mList;
		private LayoutInflater layoutInflater;
		
		public MyAdapter(Context context, List<String> list) {
			mContext = context;
			mList = list;
			layoutInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mList.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup viewParent) {
			// TODO Auto-generated method stub
			ItemView iv = null;
			if(view == null){
				iv = new ItemView();
				view = layoutInflater.inflate(R.layout.list, null);
				iv.tvCode = (TextView)view.findViewById(R.id.list_lable);
				iv.tvNum = (TextView)view.findViewById(R.id.list_number);
				view.setTag(iv);
			}else{
				iv = (ItemView)view.getTag();
			}
			iv.tvCode.setText(mList.get(position));
			iv.tvNum.setText(data.get(mList.get(position)).toString());
			return view;
		}
		
		public class ItemView{
			TextView tvCode;
			TextView tvNum;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		String id = myAdapter.mList.get(position);
		Intent intent = new Intent();
		intent.setClass(Iso15693Activity.this, ReadWActivity.class);
		intent.putExtra("mode", "ISO15693");
		BTClient.settag_id(myAdapter.mList.get(position));
		Iso15693Activity.this.startActivity(intent);
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(timer == null){
			if (myAdapter != null) {
				scanResult.clear();
				myAdapter.mList.clear();
				myAdapter.notifyDataSetChanged();
				mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
				mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
			}
			isCanceled = false;
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if(Scanflag)return;
					Scanflag=true;
					readuid();
					mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
					mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
					Scanflag=false;
				}
			}, 0, SCAN_INTERVAL);
			scan.setText("Stop");
		}else{
			cancelScan();
		}
	}
	private void cancelScan(){
		isCanceled = true;
		mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
		if(timer != null){
			timer.cancel();
			timer = null;
			scan.setText("Scan");
			scanResult.clear();
			if (myAdapter != null) {
				myAdapter.mList.clear();
				myAdapter.notifyDataSetChanged();
			}
			txNum.setText("0");
		}
		isCanceled =false;
	}
	private void readuid(){
		int scaned_num=0;
		String[] lable = ScanUID();
		if(lable == null){ 
			scaned_num = 0;
			return ;
		}
		scaned_num = lable.length;
		for (int i = 0; i < scaned_num; i++) {
			String key = lable[i];
			if(key == null || key.equals("")) return;
			int num = scanResult.get(key) == null ? 0 : scanResult.get(key);
			scanResult.put(key, num + 1);
		}
	}

	public String[] ScanUID()//
	{	
		Map<String, Integer> RepeatUID = new HashMap<String, Integer>();
		int count=0;
		String UIDList="";
		byte[]UID=new byte[800];
		byte[]Number=new byte[2];
		Number[0]=0;
		int result=BTClient.Inventory((byte)6, UID, Number);
		if((Number[0]&255)>0)
		{
			int cnum=Number[0]&255;
			byte[]arr_uid=new byte[8*cnum];
			for(int m=0;m<cnum;m++)//ÌÞ³ýDSFID
			{
				StringBuffer bf = null;
				String str;
				String str_uid="";
				for(int n=0;n<8;n++)
				{
					bf = new StringBuffer("");
					arr_uid[m*8+n]=UID[m*9+n+1];
					str = Integer.toHexString(arr_uid[8*m+n] & 0xff);
		    		if(str.length() == 1){
		    			bf.append("0");
		    		}
		    		bf.append(str);
		    		str_uid=str_uid+bf.toString().toUpperCase();
		    		UIDList=UIDList+ bf.toString().toUpperCase();
				}
			}
			if(UIDList!="")
			{
				int num=UIDList.length()/16;
				String[] lable = new String[num];
				for(int i=0;i<num;i++)
				{
					lable[i]=UIDList.substring(i*16,i*16+16);
				}
				return lable;
			}
		}
		
		return null;
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.i("zhouxin",">>>>>>>>>111111111>>>>>>");
		cancelScan();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("zhouxin",">>>>>>>>>222222222>>>>>>");
			
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	public boolean onKeyDown(int keyCode, KeyEvent event)  
    {   
		if((keyCode == KeyEvent.KEYCODE_BACK)){
			cancelScan();
			finish();
	        return false;  
        }else { 
            return super.onKeyDown(keyCode, event); 
        } 
    }

}
