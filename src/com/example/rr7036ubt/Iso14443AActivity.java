package com.example.rr7036ubt;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Iso14443AActivity extends Activity {

	EditText et_sn;
	EditText et_psd;
	EditText et_shanqu;
	EditText et_num;
	EditText et_data;
	Button btClose;
	Button btOpen;
	Button btGetUID;
	Button btAuthkey;
	Button rButton;
	Button wButton;
	public byte[] rsq=new byte[2];
	public byte[] UL_SNR = new byte[7];
	public byte[] SNR = new byte[4];
    public byte[]ErrorCode=new byte[2];
    public byte[]keys=new byte[6];
    public byte[] data=new byte[16];
    public int SecBlocknum=0;
    public byte Sec=0;
    public byte blocknum=0;
    public boolean isUrlt=false;
    private static final int MSG_UPDATE_UID = 0;
    private static final int MSG_UPDATE_DATA = 1;
    private static final int MSG_UPDATE_WRITE = 2;
    public String str_update="";
    public Handler myHandler = new Handler() {  
        //2.重写消息处理函数
        public void handleMessage(Message msg) {   
             switch (msg.what) {   
                  //判断发送的消息
	              case MSG_UPDATE_UID:
	              {
	            	 et_sn.setText(str_update);
	            	 break;
	              }
                  case MSG_UPDATE_DATA:   
                  {
                	 et_data.setText(str_update);
                     break;   
                  }  
                  case MSG_UPDATE_WRITE:   
                  {
                	  if(str_update=="00")
                	  {
                		  Toast.makeText(Iso14443AActivity.this, "Success！", Toast.LENGTH_SHORT).show();
                	  }else
                	  {
                		  Toast.makeText(Iso14443AActivity.this, "Failed！", Toast.LENGTH_SHORT).show();
                	  }
                      break;   
                  }  
             }
             super.handleMessage(msg);   
        }  
   };  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_iso14443a);
		btClose=(Button)findViewById(R.id.btCloseRf);
		btClose.setOnClickListener(myListener);
		btOpen=(Button)findViewById(R.id.btOpenRf);
		btOpen.setOnClickListener(myListener);
		btGetUID=(Button)findViewById(R.id.btGet1443A);
		btGetUID.setOnClickListener(myListener);
		btAuthkey=(Button)findViewById(R.id.btAuthKey);
		btAuthkey.setOnClickListener(myListener);
		rButton=(Button)findViewById(R.id.btRead14443);
		rButton.setOnClickListener(myListener);
		wButton=(Button)findViewById(R.id.btWrite14443);
		wButton.setOnClickListener(myListener);
		et_sn=(EditText)findViewById(R.id.et_sn);
		et_psd=(EditText)findViewById(R.id.et_14443apsd);
		et_shanqu=(EditText)findViewById(R.id.et_shanqu);
		et_shanqu.setText("0");
		et_num=(EditText)findViewById(R.id.et_kuaihao);
		et_num.setText("0");
		et_data=(EditText)findViewById(R.id.et_context);
	}
	private OnClickListener myListener= new OnClickListener(){

		@Override
		public void onClick(View v) {
			
			if(v==rButton)
			{
				String temp1= et_shanqu.getText().toString();
				if((temp1==null)||(temp1.equals(""))||(temp1.length()>2))
				{
					return;
				}
				String temp2= et_num.getText().toString();
				if((temp2==null)||(temp2.equals(""))||(temp2.length()>2))
				{
					return;
				}
				int sec=Integer.valueOf(temp1);
				int num=Integer.valueOf(temp2);
				int block=0;
				if(sec >= 32)
		           block = (byte)(128 + (sec - 32) * 16 + num);
		        else
		           block = (byte)(sec * 4 + num);
			    blocknum=(byte)block;
				str_update="";
				
				Thread mythread =new Thread(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
					}
				});
				Thread thread=new Thread(new Runnable()  
	            {  
	                @Override  
	                public void run()  
	                { 
	                	int result=BTClient.ISO14443ARead(blocknum, data, ErrorCode);
	    				if(result==0)
	    				{
	    					str_update=BTClient.bytesToHexString(data, 0, 16);
	    					myHandler.removeMessages(MSG_UPDATE_DATA);
	                		myHandler.sendEmptyMessage(MSG_UPDATE_DATA);
	    				}else
	    				{
	    					str_update="";
	    					myHandler.removeMessages(MSG_UPDATE_DATA);
	                		myHandler.sendEmptyMessage(MSG_UPDATE_DATA);
	                	}
	                }  
	            });  
				thread.start();
			}else if(v==wButton)
			{
				String temp1= et_shanqu.getText().toString();
				if((temp1==null)||(temp1.equals(""))||(temp1.length()>2))
				{
					return;
				}
				String temp2= et_num.getText().toString();
				if((temp2==null)||(temp2.equals(""))||(temp2.length()>2))
				{
					return;
				}
				
				String temp3= et_data.getText().toString();
				if((temp3==null)||(temp3.equals(""))||(temp3.length()!=32))
				{
					return;
				}
				int sec=Integer.valueOf(temp1);
				int num=Integer.valueOf(temp2);
				int block=0;
				if(sec >= 32)
		           block = (byte)(128 + (sec - 32) * 16 + num);
		        else
		           block = (byte)(sec * 4 + num);
				blocknum=(byte)block;
				data=BTClient.hexStringToBytes(temp3);
				Thread thread=new Thread(new Runnable()  
	            {  
	                @Override  
	                public void run()  
	                { 
	                	int result=BTClient.ISO14443AWrite(blocknum, data, ErrorCode);
	    				if(result==0)
	    				{
	    					str_update="00";
	    					myHandler.removeMessages(MSG_UPDATE_WRITE);
	                		myHandler.sendEmptyMessage(MSG_UPDATE_WRITE);
	    				}else
	    				{
	    					str_update="";
	    					myHandler.removeMessages(MSG_UPDATE_WRITE);
	                		myHandler.sendEmptyMessage(MSG_UPDATE_WRITE);
	    				}
	                }  
	            }); 
				thread.start();  
			}
			else if(v==btClose)
			{
				Thread thread=new Thread(new Runnable()  
	            {  
	                @Override  
	                public void run()  
	                { 
	                	BTClient.CloseRf();
	                }  
	            }); 
				thread.start();  
			}
			else if(v==btOpen)
			{
				Thread thread=new Thread(new Runnable()  
	            {  
	                @Override  
	                public void run()  
	                { 
	                	BTClient.OpenRf();
	                }  
	            }); 
				thread.start();  
			}
			else if(v==btGetUID)
			{
				Thread thread=new Thread(new Runnable()  
	            {  
	                @Override  
	                public void run()  
	                { 
	                	int result=BTClient.ISO14443ARequest(rsq, ErrorCode);
	    				if(result==0)
	    				{
	    					String temp=BTClient.bytesToHexString(rsq, 0, 2);
	    					if(temp.equals("4400"))
	    					{
	    						isUrlt=true;
	    						result=BTClient.ISO14443AULAnticoll(UL_SNR, ErrorCode);
	    						if(result==0)
	    						{
	    							str_update=BTClient.bytesToHexString(UL_SNR, 0, 7);
	    							byte[]Size=new byte[2];
	    							result=BTClient.ISO14443ASelect(UL_SNR, Size, ErrorCode);
	    							myHandler.removeMessages(MSG_UPDATE_UID);
	    	                		myHandler.sendEmptyMessage(MSG_UPDATE_UID);
	    						}
	    					}
	    					else
	    					{
	    						isUrlt=false;
	    						result=BTClient.ISO14443AAnticoll(SNR, ErrorCode);
	    						if(result==0)
	    						{
	    							str_update=BTClient.bytesToHexString(SNR, 0, 4);
	    							byte[]Size=new byte[2];
	    							result=BTClient.ISO14443ASelect(SNR, Size, ErrorCode);
	    							myHandler.removeMessages(MSG_UPDATE_UID);
	    	                		myHandler.sendEmptyMessage(MSG_UPDATE_UID);
	    						}
	    					}
	    					
	    				}
	    				else
	    				{
	    					str_update="";
	    					myHandler.removeMessages(MSG_UPDATE_UID);
	                		myHandler.sendEmptyMessage(MSG_UPDATE_UID);
	    				}
	                }  
	            }); 
				thread.start();  
				
			}
			else if(v==btAuthkey)
			{
				String temp1= et_shanqu.getText().toString();
				if((temp1==null)||(temp1.equals(""))||(temp1.length()>2))
				{
					return;
				}
				String temp2= et_psd.getText().toString();
				if((temp2==null)||(temp2.equals(""))||(temp2.length()>12))
				{
					return;
				}
				keys=BTClient.hexStringToBytes(temp2);
				int secnum=0;
				secnum=Integer.valueOf(temp1);
				Sec=(byte)secnum;
				Thread thread=new Thread(new Runnable()  
	            {  
	                @Override  
	                public void run()  
	                { 
	                	int result=BTClient.ISO14443AAuthKey(keys, Sec, ErrorCode);
	    				if(result==0)
	    				{
	    					str_update="00";
	    					myHandler.removeMessages(MSG_UPDATE_WRITE);
	                		myHandler.sendEmptyMessage(MSG_UPDATE_WRITE);
	    				}else
	    				{
	    					str_update="";
	    					myHandler.removeMessages(MSG_UPDATE_WRITE);
	                		myHandler.sendEmptyMessage(MSG_UPDATE_WRITE);
	    				}
	                }  
	            }); 
				thread.start();  				
			}
		}
	};
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Thread thread=new Thread(new Runnable()  
        {  
            @Override  
            public void run()  
            { 
            	BTClient.ChangeTo14443A();
            }  
        }); 
		thread.start();  
	}
}
