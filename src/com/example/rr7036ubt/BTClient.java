package com.example.rr7036ubt;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.Format;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class BTClient {
	 public static BluetoothLeService mBluetoothLeService=null;
	 public static byte[] RecvBuff = new byte[300];
	 public static int RecvLength=0;
	 private static long time1=0;  
	 public static byte ComAddr;
	 public static long CmdTime=500;
	 public static String DeviceName="";
	 public static String tagid="";
	 public static boolean CmdIng=false;
	 public static String ActiveModeStr="";
	 public static String GetDevName()
	 {
		 return DeviceName;
	 }

	 //Len：2个字节，高字节在前，包长度，包括Len本身。即包长度等于5+m+n。
	 //Cmd：1个字节，命令代码。用于通知蓝牙助手透传命令帧Frame[]前需要进行的操作。
	 //Data[]：m个字节，对应于Cmd命令码的操作数据。
	 //Frame[]：n个字节，要发送给读写器的完整的命令帧。该部分数据将直接透传给读写器，因此要保证数据格式和内容的正确。
	 //CRC-16：2个字节，低字节在前。CRC16是从Len到Frame []的CRC16值。
	 
	 public static String init_com(byte Baudrate,byte Parity)
	 {
		byte[] data=new byte[7];
		data[0] = 0;
		data[1] = 7;
		data[2] = 1;
		data[3] = Baudrate;
		data[4] = Parity;
		getCRC(data,5);
		return bytesToHexString(data,0,7) ;
	 }
	 
	 public static void SetDevName(String name)
	 {
		 DeviceName=name;
	 }
	 public static void settag_id(String name){
		 tagid=name;
	 }
	 
	 public static String gettag_id()
	 {
		 return tagid;
	 }

	 public static void getCRC(byte[] data,int Len)
	 {
		int i, j;
		int current_crc_value = 0xFFFF;
		for (i = 0; i <Len ; i++)
		{
		    current_crc_value = current_crc_value ^ (data[i] & 0xFF);
		    for (j = 0; j < 8; j++)
		    {
		        if ((current_crc_value & 0x01) != 0)
		            current_crc_value = (current_crc_value >> 1) ^ 0x8408;
		        else
		            current_crc_value = (current_crc_value >> 1);
		    }
		}
		data[i++] = (byte) (current_crc_value & 0xFF);
		data[i] = (byte) ((current_crc_value >> 8) & 0xFF); 
	 }
	 public static boolean CheckCRC(byte[] data,int len)
	 {
		 getCRC(data,len);
		 if(0==data[len+1] && 0==data[len])
		 {
			 return true;
		 }
		 else
		 {
			 return false;
		 }
	 }

	 public static void ArrayClear(byte[] Msg,int Size)
	 {
		 for(int i=0;i<Size;i++){
			 Msg[i]=0;
		 }
	 }
	 public static void memcpy(byte[] SourceByte,int StartBit_1,byte[] Targetbyte,int StartBit_2,int Length )
	 {
		 for(int m=0;m<Length;m++){
			 Targetbyte[StartBit_2+m]=SourceByte[StartBit_1+m];
		 }
	 }
	 public static void memcpy(byte[] SourceByte,byte[] Targetbyte,int Length )
	 {
		 for(int m=0;m<Length;m++){
			 Targetbyte[m]=SourceByte[+m];
		 }
	 }
	 
	 public static String bytesToHexString(byte[] src, int offset, int length,int blockNums) {     
		    StringBuilder sb = new StringBuilder(""); 
		    byte[] src_div=new byte[5];
		    Format f1 = new DecimalFormat("000");
		    for (int i=0;i<blockNums;i++)    
		    {   
		    	for(int j=0;j<5;j++){
		    		src_div[j]=src[i*5+j];	
		    	}
		    	sb.append(f1.format(i));
		        sb.append(": "+bytesToHexString(src_div,offset,length));  
		        sb.append("\n");
		    }    
		    return sb.toString().toUpperCase().trim();  
	 }
	 
	 public static String bytesToHexString(byte[] src, int offset, int length) {     
		    String stmp="";    
		    StringBuilder sb = new StringBuilder("");    
		    for (int n=0;n<length;n++)    
		    {    
		        stmp = Integer.toHexString(src[n+offset] & 0xFF);    
		        sb.append((stmp.length()==1)? "0"+stmp : stmp);     
		    }    
		    return sb.toString().toUpperCase().trim();  
	 }


	 @SuppressLint("DefaultLocale")
	public static byte[] hexStringToBytes(String hexString) {  
	        if (hexString == null || hexString.equals("")) {  
	            return null;  
	        }  
	        hexString = hexString.toUpperCase();  
	        int length = hexString.length() / 2;   
	        char[] hexChars = hexString.toCharArray();  
	        byte[] d = new byte[length];  
	        for (int i = 0; i < length; i++) {  
	            int pos = i * 2;  
	            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
	        }  
	        return d;  
	 }   
	 private static byte charToByte(char c) {  
	        return (byte) "0123456789ABCDEF".indexOf(c);  
	    } 

	 public static int GetData()
	 {
	   time1= System.currentTimeMillis();
	   while((System.currentTimeMillis()-time1)<3000){
		   SystemClock.sleep(50);
		   int recvLen=MyService.RecvString.length()/2;
		   if(recvLen>0)
		   {
			   byte[] buffer =new byte[recvLen];
			   buffer=hexStringToBytes(MyService.RecvString);
			   memcpy(buffer,0,RecvBuff,0,recvLen);
			   RecvLength=recvLen;
			   int activelen=buffer[0]+1;
			   if(CheckCRC(RecvBuff,activelen))
			   {
				   Log.d("read data:", MyService.RecvString);
			       CmdIng=false;
				   return 0;
			   }
		   } 
	   }
	   CmdIng=false;
	   return -1;
	 }
	 
	 public static int ChangeTo15693()
	 {
		 byte[] Msg=new byte[6];
		 Msg[0]=5;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=0x00;
		 Msg[3]=(byte)0x06;
		 getCRC(Msg,4);
		 CmdTime=500;
//		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
//		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0){
        	ComAddr = RecvBuff[1];
      		return RecvBuff[2];
         }
		 return -1;
		 
	 }
	 public static int ChangeTo14443A()
	 {
		 byte[] Msg=new byte[6];
		 Msg[0]=5;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=0x00;
		 Msg[3]=(byte)0x05;
		 getCRC(Msg,4);
		 CmdTime=500;
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0){
        	ComAddr = RecvBuff[1];
      		return RecvBuff[2];
         }
		 return -1;
	 }
	 public static int Inventory(byte state, byte[] UID, byte[] Number) {
		 byte[] Msg=new byte[6];
		 Msg[0]=5;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=1;
		 Msg[3]=state;
		 getCRC(Msg,4);
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0)
		 {
			 Number[0]=-1;
    	     Number[0]=(byte)((RecvLength-5)/9);
    	     int count = Number[0]&255;
       		 if( count > 0 )
       		 {
       			memcpy(RecvBuff,3,UID,0,count*9);
       		 }
       		 return (byte)RecvBuff[2];
		 }
		 return 0x30;
	}
	 
	 public static int ReadSingleBlock(byte[] UID,byte blockNumber,byte[]data)
	 {
		 byte[] Msg=new byte[20];
		 Msg[0]=0x0E;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=0x20;
		 Msg[3]= 0x00;//(state固定0，4字节)
		 memcpy(UID,0,Msg,4,8);
		 Msg[12]=blockNumber;
		 getCRC(Msg,13);
		 CmdTime=500;
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0)
         {
       	    ComAddr = RecvBuff[1];
       	    memcpy(RecvBuff,3,data,0,RecvLength-5);
      	    return 0;
         }
		 return -1;
	 }
	 
	 public static int ReadMultipleBlock(byte[] UID,byte fisrtBlock,byte blockNumbers,byte[]data)
	 {
		 byte[] Msg=new byte[20];
		 Msg[0]=0x0F;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=0x23;
		 Msg[3]= 0x00;//(state固定0，4字节)
		 memcpy(UID,0,Msg,4,8);
		 Msg[12]=fisrtBlock;
		 Msg[13]=blockNumbers;
		 getCRC(Msg,14);
		 CmdTime=500;
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0)
         {
       	    ComAddr = RecvBuff[1];
       	    memcpy(RecvBuff,3,data,0,RecvLength-5);
      	    return 0;
         }
		 return -1;
	 }

	 public static int WriteSingleBlock(byte state,byte[] UID,byte blockNumber,byte[]data)
	 {
		 byte[] Msg=new byte[20];
		 Msg[0]=0x12;
		 Msg[1]=(byte)(BTClient.ComAddr & 255);
		 Msg[2]=0x21;
		 Msg[3]= state;
		 BTClient.memcpy(UID,0,Msg,4,8);
		 Msg[12]=blockNumber;
		 memcpy(data,0,Msg,13,4);
		 getCRC(Msg,17);
		 CmdTime=600;
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0)
         {
        	 ComAddr = RecvBuff[1];
        	 if(RecvBuff[2]==0)
        		 return 0;
        	 else
        		 return -1;	 
         }
		 return -1;
	 }
	 
	 public static int CloseRf()
	 {
		 byte[] Msg=new byte[6];
		 Msg[0]=5;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=0x00;
		 Msg[3]=(byte)0x01;
		 getCRC(Msg,4);
		 CmdTime=500;
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0){
        	ComAddr = RecvBuff[1];
      		return RecvBuff[2];
         }
		 return -1;
	 }
	 
	 public static int OpenRf()
	 {
		 byte[] Msg=new byte[6];
		 Msg[0]=5;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=0x00;
		 Msg[3]=(byte)0x02;
		 getCRC(Msg,4);
		 CmdTime=500;
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0){
        	ComAddr = RecvBuff[1];
      		return RecvBuff[2];
         }
		 return -1;
	 }
	 
	 public static int ISO14443ARequest(byte[]Data,byte[]ErrorCode)
	 {
		 byte[] Msg=new byte[7];
		 Msg[0]=6;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=(byte)0x41;
		 Msg[3]=(byte)0x10;
		 Msg[4]=0x01;
		 getCRC(Msg,5);
		 CmdTime=500;
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0)
         { 
        	 ComAddr = RecvBuff[1];
        	 if(RecvBuff[2]==0)
        	 {
        		 Data[0]=RecvBuff[3];
        		 Data[1]=RecvBuff[4];
        	 }else if(RecvBuff[2]==0x10)
        	 {
        		 ErrorCode[0]=RecvBuff[3];  
        	 }
        	 return RecvBuff[2];
         }
		 return -1;
	 }
	 
	 public static int ISO14443AAnticoll(byte[]Data,byte[]ErrorCode)
	 {
		 byte[] Msg=new byte[7];
		 Msg[0]=6;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=(byte)0x42;
		 Msg[3]=(byte)0x10;
		 Msg[4]=0x00;
		 getCRC(Msg,5);
		 CmdTime=500;
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0)
         { 
        	 ComAddr = RecvBuff[1];
        	 if(RecvBuff[2]==0)
        	 {
        		 Data[0]=RecvBuff[3];
        		 Data[1]=RecvBuff[4];
        		 Data[2]=RecvBuff[5];
        		 Data[3]=RecvBuff[6];
        	 }else if(RecvBuff[2]==0x10)
        	 {
        		 ErrorCode[0]=RecvBuff[3];  
        	 }
        	 return RecvBuff[2];
         }
		 return -1;
	 }
	 
	 public static int ISO14443AULAnticoll(byte[]Data,byte[]ErrorCode)
	 {
		 byte[] Msg=new byte[7];
		 Msg[0]=6;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=(byte)0x7A;
		 Msg[3]=(byte)0x10;
		 Msg[4]=0x00;
		 getCRC(Msg,5);
		 CmdTime=500;
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0)
         { 
        	 ComAddr = RecvBuff[1];
        	 if(RecvBuff[2]==0)
        	 {
        		 Data[0]=RecvBuff[3];
        		 Data[1]=RecvBuff[4];
        		 Data[2]=RecvBuff[5];
        		 Data[3]=RecvBuff[6];
        		 Data[4]=RecvBuff[7];
        		 Data[5]=RecvBuff[8];
        		 Data[6]=RecvBuff[9];
        	 }else if(RecvBuff[2]==0x10)
        	 {
        		 ErrorCode[0]=RecvBuff[3];  
        	 }
        	 return RecvBuff[2];
         }
		 return -1;
	 }
	 
	public static int ISO14443ASelect(byte[]SN,byte[]Size,byte[]ErrorCode)
	{
		 byte[] Msg=new byte[16];
		 Msg[0]=9;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=(byte)0x43;
		 Msg[3]=(byte)0x10;
		 Msg[4]=SN[0];
		 Msg[5]=SN[1];
		 Msg[6]=SN[2];
		 Msg[7]=SN[3];
		 getCRC(Msg,8);
		 CmdTime=500;
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0)
         { 
        	 ComAddr = RecvBuff[1];
        	 if(RecvBuff[2]==0)
        	 {
        		 Size[0]=RecvBuff[3];
        	 }
        	 else if(RecvBuff[2]==0x10)
        	 {
        		 ErrorCode[0]=RecvBuff[3];  
        	 }
        	 return RecvBuff[2];
         }
		 return -1;
	 }
	
	public static int ISO14443AAuthKey (byte[]keys,byte Sec,byte[]ErrorCode)
	 {
		 byte[] Msg=new byte[14];
		 Msg[0]=0x0D;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=(byte)0x73;
		 Msg[3]=(byte)0x10;
		 Msg[4]=0x00;//(密钥A认证)
		 Msg[5]=Sec;
		 Msg[6]=keys[0];
		 Msg[7]=keys[1];
		 Msg[8]=keys[2];
		 Msg[9]=keys[3];
		 Msg[10]=keys[4];
		 Msg[11]=keys[5];
		 getCRC(Msg,12);
		 String temos=bytesToHexString(Msg,0,14);
		 CmdTime=500;
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0)
         { 
	      	  ComAddr = RecvBuff[1];
	          if(RecvBuff[2]==0x10)
	      	  {
	      		 ErrorCode[0]=RecvBuff[3];  
	      	  }
	      	  return RecvBuff[2];
         }
		 return -1;
	 }
	
	 public static int ISO14443ARead(byte block,byte[]data,byte[]ErrorCode)
	 {
		 byte[] Msg=new byte[7];
		 Msg[0]=0x06;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=(byte)0x46;
		 Msg[3]=(byte)0x10;
		 Msg[4]=block;
		 getCRC(Msg,5);
		 CmdTime=500;
		 MyService.target_chara.setValue(Msg); 
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 if(GetData()==0)
         { 
     	    ComAddr = RecvBuff[1];
     	    if(RecvBuff[2]==0x00)
     	    {
     	    	memcpy(RecvBuff,3,data,0,16);
     	    }
           if(RecvBuff[2]==0x10)
     	    {
     		  ErrorCode[0]=RecvBuff[3];  
     	    }
     	    return RecvBuff[2];
         }
		 return -1;
	 }
	 
	 public static int ISO14443AWrite(byte block,byte[]data,byte[]ErrorCode)
	 {
		 byte[] Msg=new byte[30];//超过20字节的必须分段
		 Msg[0]=0x16;
		 Msg[1]=(byte)(ComAddr & 255);
		 Msg[2]=(byte)0x47;
		 Msg[3]=(byte)0x10;
		 Msg[4]=block;
		 memcpy(data,0, Msg,5, 16);
		 getCRC(Msg,21);
		 CmdTime=500;
		 Log.d("write data:", bytesToHexString(Msg,0,23));
		 ArrayClear(RecvBuff,300);
		 RecvLength=0;
		 MyService.RecvString="";
		 byte[]data1=new byte[15];
		 memcpy(Msg,0,data1,0,15);
		 MyService.target_chara.setValue(data1); 
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 long time1= System.currentTimeMillis();
		 byte[]data2=new byte[15];
		 memcpy(Msg,15,data2,0,8);
		 MyService.target_chara.setValue(data2); 
		 mBluetoothLeService.writeCharacteristic(MyService.target_chara);
		 //BluetoothGattCharacteristic characteristic=null;;
		 //mBluetoothLeService.readCharacteristic(characteristic);
		 if(GetData()==0)
         { 
     	    ComAddr = RecvBuff[1];
            if(RecvBuff[2]==0x10)
     	    {
     		  ErrorCode[0]=RecvBuff[3];  
     	    }
     	    return RecvBuff[2];
         }
		 return -1;
	 }
}
