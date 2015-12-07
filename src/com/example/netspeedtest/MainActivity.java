package com.example.netspeedtest;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.example.netspeedtest.R.id;




import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{

	private TextView tv_type,tv_now_speed,tv_ave_speed;
	private EditText edit_fileurl;
	private Button btn_testspeed;
	private ImageView needle;
	private TFileInfo fileInfo;
	private byte[] imageBytes;
	private boolean flag;
	private int last_degree=0,cur_degree;
	private Context mContext;
	
	private Handler handler=new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			if(msg.what==0x123)
			{
				tv_now_speed.setText(msg.arg1+"KB/S");
				tv_ave_speed.setText(msg.arg2+"KB/S");
				startAnimation(msg.arg1);
			}
			if(msg.what==0x100)
			{
				tv_now_speed.setText("0KB/S");
				startAnimation(0);
				btn_testspeed.setText("开始测试");
				btn_testspeed.setEnabled(true);
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mContext=this;
		setContentView(R.layout.main);
		findViews();
		bindEvents();
	}


	private void bindEvents() {
		btn_testspeed.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View arg0)
			{
				String url=edit_fileurl.getText().toString();
				if(TextUtils.isEmpty(url))
				{
					Toast.makeText(mContext, "请输入地址", Toast.LENGTH_SHORT).show();
					return ;
				}
				
				// TODO Auto-generated method stub
				ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
				tv_type.setText(networkInfo.getTypeName());
				btn_testspeed.setText("测试中");
				btn_testspeed.setEnabled(false);
				
				fileInfo=new TFileInfo();
				fileInfo.setFileUrl(url);
				fileInfo.setFinishByte(0);
				fileInfo.setSpeed(0);
				fileInfo.setTotalByte(1024);
				
				new DownloadThread().start();
				new GetInfoThread().start();
			}
		});
	}


	private void findViews() {
		tv_type=(TextView) findViewById(R.id.connection_type);
		tv_now_speed=(TextView) findViewById(R.id.now_speed);
		tv_ave_speed=(TextView) findViewById(R.id.ave_speed);
		needle=(ImageView) findViewById(R.id.needle);
		btn_testspeed=(Button) findViewById(R.id.start_btn);
		edit_fileurl=(EditText)findViewById(R.id.edit_testfileurl);

		String url_string="http://192.168.0.6/server/upload/video.mp4";
		edit_fileurl.setText(url_string);
	}


	class DownloadThread extends Thread
	{

		@Override
		public void run()
		{
			long start_time,cur_time;
			URL url;
			URLConnection connection;
			InputStream iStream;
			
			try
			{
				url=new URL(fileInfo.getFileUrl());
				connection=url.openConnection();
				
				fileInfo.setTotalByte(connection.getContentLength());
				
				iStream=connection.getInputStream();
				start_time=System.currentTimeMillis();
				while(iStream.read()!=-1 && flag)
				{
					
					fileInfo.finishBytePlus();
					cur_time=System.currentTimeMillis();
					if(cur_time-start_time==0)
					{
						fileInfo.setSpeed(1000);
					}
					else {
						
						fileInfo.setSpeed(fileInfo.getFinishByte()/(cur_time-start_time)*1000);
					}
				}
				iStream.close();
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	class GetInfoThread extends Thread
	{

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			double sum,counter;
			int cur_speed,ave_speed;
			try
			{
				sum=0;
				counter=0;
				while(fileInfo.getFinishByte()<fileInfo.getTotalByte() && flag)
				{
					Thread.sleep(1000);
					
					sum+=fileInfo.getSpeed();
					counter++;
					
					cur_speed=(int) fileInfo.getSpeed();
					ave_speed=(int) (sum/counter);
					Log.e("Test", "cur_speed:"+fileInfo.getSpeed()/1024+"KB/S ave_speed:"+ave_speed/1024);
					Message msg=new Message();
					msg.arg1=((int)fileInfo.getSpeed()/1024);
					msg.arg2=((int)ave_speed/1024);
					msg.what=0x123;
					handler.sendMessage(msg);
				}
				if(fileInfo.getFinishByte()==fileInfo.getTotalByte() && flag)
				{
					handler.sendEmptyMessage(0x100);
				}
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	@Override
	public void onBackPressed()
	{
		// TODO Auto-generated method stub
		flag=false;
		super.onBackPressed();
	}

	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		flag=true;
		super.onResume();
	}

	private void startAnimation(int cur_speed)
	{
		cur_degree=getDegree(cur_speed);
		
		RotateAnimation rotateAnimation=new RotateAnimation(last_degree, cur_degree, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateAnimation.setFillAfter(true);
		rotateAnimation.setDuration(1000);
		last_degree=cur_degree;
		needle.startAnimation(rotateAnimation);
	}
	
	private int getDegree(double cur_speed)
	{
		int ret=0;
		if(cur_speed>=0 && cur_speed<=512)
		{
			ret=(int) (15.0*cur_speed/128.0);
		}
		else if(cur_speed>=512 && cur_speed<=1024)
		{
			ret=(int) (60+15.0*cur_speed/256.0);
		}
		else if(cur_speed>=1024 && cur_speed<=10*1024)
		{
			ret=(int) (90+15.0*cur_speed/1024.0);
		}else {
			ret=180;
		}
		return ret;
	}
	
}
