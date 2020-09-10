package com.example.rr7036ubt;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class GetActive extends Activity implements OnCheckedChangeListener {
	 private Handler myHandler = new Handler() {  
	        //2.重写消息处理函数
	        public void handleMessage(Message msg) {   
	             switch (msg.what) {   
	                  //判断发送的消息
		              case 0:
		              {
		            	  String uid = msg.getData().getString("str_uid");
		            	  if((uid.length()==26)&&(m_type==0))
		            	  {
		            		  myAdapter.addDevice(uid.substring(6, 22));
		            		  myAdapter.notifyDataSetChanged();
		            	  }
		            	  else if((uid.length()==18)&&(m_type==1))
		            	  {
		            		  myAdapter.addDevice(uid.substring(6, 14));
		            		  myAdapter.notifyDataSetChanged();
		            	  }
		            	  else if((uid.length()==22)&&(m_type==2))
		            	  {
		            		  myAdapter.addDevice(uid.substring(6, 20));
		            		  myAdapter.notifyDataSetChanged();
		            	  }
		            	  break;
		              }
	             }
	             super.handleMessage(msg);   
	        }  
	   };  
	   ListAdapter myAdapter;
	   ListView lv;
	   public Timer timer;
	   private static final int SCAN_INTERVAL = 10;
	   private boolean Scanflag=false;
	   public int m_type=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_active);
		RadioButton rb_15=(RadioButton)findViewById(R.id.rb_15);
		rb_15.setOnCheckedChangeListener(this);
		RadioButton rb_mf1=(RadioButton)findViewById(R.id.rb_mf1);
		rb_mf1.setOnCheckedChangeListener(this);
		RadioButton rb_ul=(RadioButton)findViewById(R.id.rb_ul);
		rb_ul.setOnCheckedChangeListener(this);
		lv=(ListView)findViewById(R.id.list_act);
		myAdapter=new ListAdapter();
		lv.setAdapter(myAdapter);
		m_type=0;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			Drawable drawable;
			switch (buttonView.getId()) {
			case R.id.rb_15:
				m_type=0;
				myAdapter.mList.clear();
				myAdapter.notifyDataSetChanged();
				/*Thread thread=new Thread(new Runnable()  
		        {  
		            @Override  
		            public void run()  
		            { 
		            	BTClient.ChangeTo15693();
		            }  
		        }); 
				thread.start();  */
				break;
			case R.id.rb_mf1:
				m_type=1;
				myAdapter.mList.clear();
				myAdapter.notifyDataSetChanged();
				/*Thread thread1=new Thread(new Runnable()  
		        {  
		            @Override  
		            public void run()  
		            { 
		            	BTClient.ChangeTo14443A();
		            }  
		        }); 
				thread1.start();  */
				break;
			case R.id.rb_ul:
				m_type=2;
				myAdapter.mList.clear();
				myAdapter.notifyDataSetChanged();
				/*Thread thread2=new Thread(new Runnable()  
		        {  
		            @Override  
		            public void run()  
		            { 
		            	BTClient.ChangeTo14443A();
		            }  
		        }); 
				thread2.start();*/
				break;
			}
		}
		
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Scanflag=false;
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(Scanflag)return;
				Scanflag=true;
				if(!MyService.ObjectRecv.equals(""))
				{
					updateuid(MyService.ObjectRecv);
					MyService.ObjectRecv="";
				}
				Scanflag=false;
			}
		}, 0, SCAN_INTERVAL);
	}
	 private void updateuid( String uid)
	 {
	       Message msg =new Message();
	       msg.what=0;
	       Bundle b = new Bundle();
	       b.putString("str_uid", uid);
	       msg.setData(b);
	   	   myHandler.sendMessage(msg);
	   	   System.out.println("str_uid:"+uid);
	 }
	 
	 private class ListAdapter extends BaseAdapter {
	        private ArrayList<String> mList;

	        private LayoutInflater mInflator;

	        public ListAdapter(){
	            super();
	            mList = new ArrayList<String>();
	            mInflator = getLayoutInflater();
	        }
 
	        public void addDevice(String uid) {

	        	mList.add(uid);
	        }

	        public String getDevice(int position) {
	            return mList.get(position);
	        }

	        public void clear() {
	        	mList.clear();
	        }

	        @Override
	        public int getCount() {
	            return mList.size();
	        }

	        @Override
	        public Object getItem(int i) {
	            return mList.get(i);
	        }

	        @Override
	        public long getItemId(int i) {
	            return i;
	        }

	        @Override
	        public View getView(int i, View view, ViewGroup viewGroup) {
	          
	            // General ListView optimization code.
	           
	            view = mInflator.inflate(R.layout.listgl, null);
	            TextView txt_uid = (TextView) view.findViewById(R.id.txt_uid);
	            String device = mList.get(i);
	            txt_uid.setText( device);
	            return view;
	        }
	    }
		
		
	    @Override
		protected void onPause() {
			// TODO Auto-generated method stub
			super.onPause();
			myHandler.removeMessages(0);
			if(timer != null){
				timer.cancel();
				timer = null;
			}
		}
	    
	    @Override
		protected void onDestroy() {
			// TODO Auto-generated method stub
			super.onPause();
			Intent intent=new Intent(this,MyService.class);
	        stopService(intent);
		}

}
