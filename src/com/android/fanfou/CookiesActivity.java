package com.android.fanfou;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.android.fanfou.FanfouApi.ApiException;
import com.android.fanfou.FanfouApi.AuthException;

public class CookiesActivity extends BaseActivity implements OnTouchListener, OnGestureListener{
	private static String TAG = "CookiesActivity"; 
	
	private ImageView bt_Write;
	private TextView tv_UserID;
	private ImageView bt_Refresh;
	
	private TextView bt_HomePage;
	private TextView bt_Me;
	private TextView bt_Letter;
	private TextView bt_Space;
	private TextView bt_Search;
	private TextView bt_Cookies;
	
	private ListView mlv_list;
	
	private FanfouApi mApi;
	
	//���ڳ����¼�
	private String contextMenu_tweet_user_text;
	private String contextMenu_tweet_text;
	private String contextMenu_tweet_id;
	private String contextMenu_tweet_user_id;
	private String contextMenu_fav;
	private int contextMenu_position;
	
	
	private GestureDetector mGestureDetector; 
	private String[] from = new String[]{"profile_image","tweet_user_text","tweet_text","tweet_meta_text","tweet_fav","tweet_has_image"};
	private int[] to = new int[]{R.id.profile_image,R.id.tweet_user_text,R.id.tweet_text,R.id.tweet_meta_text,R.id.tweet_fav,R.id.tweet_has_image};
	private List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();//��Ϣ����
	private MyAdapter TweetAdapter;

	private String curUser;
	private ProgressDialog pDlg;
	private int position_more;
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.arg1){
			case 0 :TweetAdapter = new MyAdapter(CookiesActivity.this, data, R.layout.tweet, from, to);
			    	mlv_list.setAdapter(TweetAdapter);
			    	mlv_list.setSelectionFromTop(1, 0);
			    	pDlg.dismiss();
			    	break;
			default:TweetAdapter = new MyAdapter(CookiesActivity.this, data, R.layout.tweet, from, to);
			    	mlv_list.setAdapter(TweetAdapter);
			    	mlv_list.setSelectionFromTop(msg.arg1, 0);
			    	pDlg.dismiss();
			    	break;
			}
		}
	};
	@Override
	   public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE); 
	        setContentView(R.layout.main);
	        
	        
	        mApi = getApi();
	        curUser=mApi.getUsername();
	        mGestureDetector = new GestureDetector(this);
	        
	        bt_Write = (ImageView)findViewById(R.id.bt_Write);  
	        tv_UserID=(TextView)findViewById(R.id.tv_UserID);
	        bt_Refresh=(ImageView)findViewById(R.id.bt_Refresh);
	        
	        bt_HomePage=(TextView)findViewById(R.id.bt_HomePage);
	        bt_Me=(TextView)findViewById(R.id.bt_Me);
	        bt_Letter=(TextView)findViewById(R.id.bt_Letter);
	        bt_Space=(TextView)findViewById(R.id.bt_Space);
	        bt_Search=(TextView)findViewById(R.id.bt_Search);
	        bt_Cookies=(TextView)findViewById(R.id.bt_Cookies);
	        
	        mlv_list = (ListView)findViewById(R.id.mlv_list);
	        mlv_list.setOnTouchListener(this);
	        registerForContextMenu(mlv_list);
	        bt_Cookies.setClickable(false);
	        bt_Cookies.setBackgroundColor(this.getResources().getColor(R.color.focused));
	        
	        if(user_show_name==null){
				tv_UserID.setText("");
			}
			else{	
				tv_UserID.setText(user_show_name);
			}
			
	      //��ʾˢ�º͸��ఴť��������ݿ��������ݣ�����ˢ�º͸����м�������Ϣ
			init_reflesh();//����ˢ�°�ť
		//	TweetAdapter = new MyAdapter(FanDroidActivity.this, data, R.layout.tweet, from, to);
		//	mlv_list.setAdapter(TweetAdapter);
			
			//�����ݿ��������ݣ����ޣ�����
			
			init_more();//���Ӹ��ఴť
			TweetAdapter = new MyAdapter(CookiesActivity.this, data, R.layout.tweet, from, to);
			mlv_list.setAdapter(TweetAdapter);
			mlv_list.setSelectionFromTop(1, 0);
	        
	        //д�İ�ť�¼�
	        bt_Write.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		sendMsgDlg(CookiesActivity.this,"","");//�������ڶ���Ϊ�Ի���������Ļ���������Ϊ�ظ���Ϣ��ID���ǻظ��û���ID
	        	
	        	}
	        	
	        });
	        //ˢ�µİ�ť�¼�
	        bt_Refresh.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		//reflesh_status();
	        		pDlg = new ProgressDialog(CookiesActivity.this);
	        		pDlg.setMessage("����������...");
	        		pDlg.show();
	        		new Thread(){
	        			@Override
	        			public void run(){
	        				reflesh_status();
	        				Message msg = handler.obtainMessage();
	        				msg.arg1 = 0;		//0:reflesh
	        				handler.sendEmptyMessage(0);
	        			}
	        		}.start();
	        	}
	        	
	        });
	        
	        //��ҳ
	        bt_HomePage.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		//Toast.makeText(AtMeActivity.this, "HomePage", Toast.LENGTH_SHORT).show();
	        		Intent intent_home = new Intent(CookiesActivity.this, FanDroidActivity.class);
	        		startActivity(intent_home);
	        		finish();
	        	}
	        	
	        });
	        //@��
	        bt_Me.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		//Toast.makeText(CookiesActivity.this, "Me", Toast.LENGTH_SHORT).show();
	        		Intent intent_me = new Intent(CookiesActivity.this, AtMeActivity.class);
	        		startActivity(intent_me);
	        		finish();
	        	}
	        	
	        });
	        //˽��
	        bt_Letter.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		//Toast.makeText(AtMeActivity.this, "˽��", Toast.LENGTH_SHORT).show();
	        		Intent intent_letter = new Intent(CookiesActivity.this, LetterActivity.class);
	        		startActivity(intent_letter);
	        		finish();
	        	}
	        	
	        });
	        //�ռ�
	        bt_Space.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		//Toast.makeText(AtMeActivity.this, "Space", Toast.LENGTH_SHORT).show();
	        		Intent intent_space = new Intent(CookiesActivity.this, SpaceActivity.class);
	        		intent_space.putExtra("user_show_name", user_show_name);
	        		intent_space.putExtra("user_id", user_id);
	        		startActivity(intent_space);
	        		finish();
	        	}
	        	
	        });
	        //����
	        bt_Search.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		//Toast.makeText(FanDroidActivity.this, "����", Toast.LENGTH_SHORT).show();
	        		Intent intent_search = new Intent(CookiesActivity.this, SearchActivity.class);
					startActivity(intent_search);
					finish();
	        	}
	        	
	        });
//	        //�ղ�
//	        bt_Cookies.setOnClickListener(new OnClickListener(){
//	        	@Override
//				public void onClick(View v) {
//	        		//Toast.makeText(AtMeActivity.this, "�ղ�", Toast.LENGTH_SHORT).show();
//	        		Intent intent_cookies = new Intent(CookiesActivity.this, CookiesActivity.class);
//					startActivity(intent_cookies);
//	        	}
//	        	
//	        });
	        

	        mlv_list.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					//��ø�����Ϣ������������������onCreateContextMenu
					 
					 String info = arg0.getItemAtPosition(arg2).toString();
					 info = info.substring(1, info.length() - 1);

					 String[] tokens = info.split(",");
					 //Log.v("1111",info);
					 contextMenu_position = arg2;
					 for(int i = 0; i < tokens.length; i++){
						 if(tokens[i].contains("tweet_user_text")){
							 contextMenu_tweet_user_text = (tokens[i].substring(17));
						 }
						 else if(tokens[i].contains("tweet_user_id")){
							 contextMenu_tweet_user_id = (tokens[i].substring(15));
						 }
						 else if(tokens[i].contains("tweet_id")){
							 contextMenu_tweet_id = (tokens[i].substring(10));
						 }
						 else if(tokens[i].contains("tweet_text")){
							 contextMenu_tweet_text = (tokens[i].substring(12));
						 }
						 else if(tokens[i].contains("fav")){
							 contextMenu_fav = (tokens[i].substring(5));
						 }
					 }	
				
					 if(contextMenu_tweet_id.equals("")){
						 return true;
					 }
//					 Log.v("contextMenu_tweet_user_text",contextMenu_tweet_user_text);
//					 Log.v("contextMenu_tweet_user_id",contextMenu_tweet_user_id);
//					 Log.v("contextMenu_tweet_id",contextMenu_tweet_id);
//					 Log.v("contextMenu_tweet_text",contextMenu_tweet_text);
//					 Log.v("contextMenu_fav",contextMenu_fav);
					 
					 return false;
				}
			});
	        
	        
	        mlv_list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					String info = arg0.getItemAtPosition(arg2).toString();
					info = info.substring(1, info.length() - 1);

					String[] tokens = info.split(",");
					Log.v("tweet_id",info);
					if(info.contains("tweet_id=,")){
						String refleshandmore_text = "";
						 for(int i = 0; i < tokens.length; i++){
							 if(tokens[i].contains("tweet_text")){
								 refleshandmore_text = (tokens[i].substring(12));
							 }
						 }

						 if(refleshandmore_text.equals("\t\t\t\t\t\tˢ��")){
							 Log.v("ˢ��","reflesh_status");	
							 //reflesh_status();
							 pDlg = new ProgressDialog(CookiesActivity.this);
				        		pDlg.setMessage("����������...");
				        		pDlg.show();
				        		new Thread(){
				        			@Override
				        			public void run(){
				        				reflesh_status();
				        				Message msg = handler.obtainMessage();
				        				msg.arg1 = 0;	//0:reflesh
				        				handler.sendMessage(msg);
				        			}
				        		}.start();
						 }
						 else if(refleshandmore_text.equals("\t\t\t\t\t\t����")){

							 Log.v("����","more_status");						 
							 //more_status();
							 pDlg = new ProgressDialog(CookiesActivity.this);
				        		pDlg.setMessage("����������...");
				        		pDlg.show();
				        		new Thread(){
				        			@Override
				        			public void run(){
				        				more_status();
				        				Message msg = handler.obtainMessage();
				        				msg.arg1 = position_more;	//more:position_more
				        				Log.v("pos", String.valueOf(position_more));
				        				handler.sendMessage(msg);
				        			}
				        		}.start();
						 }
					 }
				}
			});
	   }
	
	
	

	   
	   @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
			if(BaseActivity.CookiesActivityCache!=null){
				data.clear();
				init_reflesh();
				for(int i=0;i<BaseActivity.CookiesActivityCache.size();i++){
					data.add(BaseActivity.CookiesActivityCache.get(i));
				}
				TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from, to);
				mlv_list.setAdapter(TweetAdapter);
				mlv_list.setSelectionFromTop(contextMenu_position, 0);
			}
		
			else{
				pDlg = new ProgressDialog(CookiesActivity.this);
		   		pDlg.setMessage("����������...");
				pDlg.show();
				new Thread(){
					@Override
					public void run(){
						reflesh_status();
						Message msg = handler.obtainMessage();
						msg.arg1 = 0;	//0:reflesh
						handler.sendMessage(msg);
					}
				}.start();
			}
		
	}





	@Override
	   public boolean onOptionsItemSelected(MenuItem item){
		   Log.v("111",String.valueOf(item.getItemId()));
		   switch(item.getItemId()){
			   case 1:  pDlg = new ProgressDialog(CookiesActivity.this);
			       		pDlg.setMessage("����������...");
			    		pDlg.show();
			    		new Thread(){
			    			@Override
			    			public void run(){
			    				reflesh_status();
			    				Message msg = handler.obtainMessage();
			    				msg.arg1 = 0;	//0:reflesh
			    				handler.sendMessage(msg);
			    			}
			    		}.start();
				   		break;
			   case 2: Intent intent_search = new Intent(this, SearchActivity.class);
					   startActivity(intent_search);
					   this.finish();
					   break;
			   case 3: LoginInfo loginInfo=mCache.getLoginInfo();
				   	   loginInfo.AutoCheckIn=0;
				   	   mCache.SaveLoginInfo(loginInfo);
				   	   getApi().logout();
				       Intent intent_login = new Intent(this, Login.class);
			   		   startActivity(intent_login);
			   		   this.finish();
			   		   break;
			   case 4: finish();
			   		   System.exit(0);
			   		   break;
			   case 5: Intent intent_pub = new Intent(this, PublicTimeline.class);
			   			startActivity(intent_pub);
				   		break;
			   case 6: Intent intent_about = new Intent(this, AboutActivity.class);
					   startActivity(intent_about);
					   break;
		   }
		   return true;
	   }
	   
	   @Override
	   public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo){
		   //�������¼�
		   menu.add(1,1,1,"����" + contextMenu_tweet_user_text + "�Ŀռ�");
		   menu.add(1,2,2,"ת��");		   
		   menu.add(1,3,3,"ȡ���ղ�");
		   menu.add(1,5,5,"�ظ�");
		   menu.add(1,6,6,"ȡ��");
	   }
	   
	   @Override
	   public boolean onContextItemSelected(MenuItem item){
		   switch(item.getItemId()){
			   case 1: 	Intent intent_space= new Intent(CookiesActivity.this, SpaceActivity.class);
			       		intent_space.putExtra("user_show_name", contextMenu_tweet_user_text);
			       		intent_space.putExtra("user_id", contextMenu_tweet_user_id);
						startActivity(intent_space);
						break;
			   case 2: 	sendMsgDlg(CookiesActivity.this, "ת@" + contextMenu_tweet_user_text + " " + contextMenu_tweet_text, "");
		   				break;
			   case 3: 	try {
			   			if(contextMenu_fav.equals("true")){
			   				getApi().deleteFav(contextMenu_tweet_id);				   				
					   		Toast.makeText(this, "��ȡ���ղظ���Ϣ", Toast.LENGTH_SHORT).show();
					   		mCache.updateUsersg(curUser, FanDroidCache.FDMsg_fav, 0,FanDroidCache.FDMsg_id, contextMenu_tweet_id);
					   		mCache.updateFDMsg(curUser, FanDroidCache.FDMsg_fav, 0,FanDroidCache.FDMsg_id, contextMenu_tweet_id);
					   		mCache.updateAtMeMsg(curUser, FanDroidCache.FDMsg_fav, 0,FanDroidCache.FDMsg_id, contextMenu_tweet_id);
					   		if(BaseActivity.FDActivity!=null){
					   			for(int i=0;i<BaseActivity.FDActivity.size();i++){
					   				if(BaseActivity.FDActivity.get(i).get("tweet_id").toString().equals(contextMenu_tweet_id)){
					   					
					   					Map<String, Object> item_fav = new HashMap<String, Object>();
								   		item_fav = BaseActivity.FDActivity.get(i);
											
										item_fav.put("tweet_fav" , R.drawable.none);
										
					   					
					   				}
					   			
					   			}
					   		}
					   		
					   		if(BaseActivity.AtMeActivityCache!=null){
					   			for(int i=0;i<BaseActivity.AtMeActivityCache.size();i++){
					   				if(BaseActivity.AtMeActivityCache.get(i).get("tweet_id").toString().equals(contextMenu_tweet_id)){
					   					
					   					Map<String, Object> item_fav = new HashMap<String, Object>();
								   		item_fav = BaseActivity.AtMeActivityCache.get(i);
											
										item_fav.put("tweet_fav" , R.drawable.none);
										
					   					
					   				}
					   			
					   			}
					   		}
					   		
					   		
					   		data.remove(contextMenu_position);
							
							TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from, to);
							mlv_list.setAdapter(TweetAdapter);
							mlv_list.setSelectionFromTop(contextMenu_position, 0);
			   			}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (AuthException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ApiException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				   		break;
			   case 5: 	sendMsgDlg(CookiesActivity.this, "@" + contextMenu_tweet_user_text + " ", contextMenu_tweet_id);
				   		break;
			   case 6: 	break;
		   }
		   return true;
	   }
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		if (e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 200 ) {
			
			return true;
		} else if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 200 ) {
			Intent intent_search = new Intent(this, SearchActivity.class);
			startActivity(intent_search);
			this.finish();
			return true;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return mGestureDetector.onTouchEvent(event); 
	}
	
	public  void sendMsgDlg(final Context context, String atwho, final String replyto){
		atWhoStr = atwho;
		final LinearLayout ll = (LinearLayout)getLayoutInflater().inflate(R.layout.sendmsg_dialog, null);
		
		EditText et = (EditText)ll.findViewById(R.id.et_word_number);
		if(!atwho.equals("")){
			TextView tv = (TextView)ll.findViewById(R.id.tv_word_number);
			et.setText(atwho);
			int len = 140 - (atwho.length());
			if(len >= 0){
				tv.setText("���������� " + len + " ����");
			}
			else{
				tv.setText("�������ѳ��� " + -len + " ����");
			}
		}
		et.addTextChangedListener((new TextWatcher(){
	           @Override
	           public void afterTextChanged(Editable s) {
	               // TODO Auto-generated method stub
	        	   TextView tv_temp = (TextView)ll.findViewById(R.id.tv_word_number);
	        	   EditText et_temp = (EditText)ll.findViewById(R.id.et_word_number);
	        	   int length = 140 - et_temp.getText().toString().length();
	        	   if(length >= 0){
	        		   tv_temp.setText("���������� " + length + " ����");
		   			}
		   			else{
		   				tv_temp.setText("�������ѳ��� " + -length + " ����");
		   			}
	           }

	           @Override
	           public void beforeTextChanged(CharSequence s, int start, int count,
	                   int after) {
	               // TODO Auto-generated method stub
	        	   
	           }

	           @Override
	           public void onTextChanged(CharSequence s, int start, int before,
	                   int count) {
	               // TODO Auto-generated method stub
	              
	               
	           }
	       }));
		
        new AlertDialog.Builder(context).setView(ll).setTitle("������Ϣ")
        .setPositiveButton("����", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					EditText et_temp = (EditText)ll.findViewById(R.id.et_word_number);
					if(!et_temp.getText().toString().equals("")){
						mApi.update(et_temp.getText().toString(), replyto);
						Toast.makeText(context, "����Ϣ�ѷ���", Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.v("IOException",e.toString());
				} catch (AuthException e) {
					// TODO Auto-generated catch block
					Log.v("AuthException",e.toString());
				} catch (ApiException e) {
					// TODO Auto-generated catch block
					Log.v("ApiException",e.toString());
				}						
			}
		}).setNeutralButton("��ͼ", new DialogInterface.OnClickListener() {
			//�����Ի���ѡ����������
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				final CharSequence[] items = { "���", "����" };
				new AlertDialog.Builder(context).setTitle("ѡ��ͼƬ").setItems(items,
						new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int item) {									
								if(item == 1){ 
									Intent getImageByCamera= new Intent("android.media.action.IMAGE_CAPTURE"); 
									startActivityForResult(getImageByCamera, 1);   
								}else{
									Intent getImage = new Intent(Intent.ACTION_GET_CONTENT); 
							        getImage.addCategory(Intent.CATEGORY_OPENABLE); 
							        getImage.setType("image/*"); 
							        startActivityForResult(getImage, 0); 
								}
							}
						}).show();

			}
		}).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).show();
	}
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		
        //ͼ��
        if (requestCode == 0) { 
            try { 
            	//���ͼƬ��uri 
                Uri originalUri = data.getData(); 				
                sendMsgDlg_pic(CookiesActivity.this, atWhoStr, originalUri);
                
            } catch (Exception e) { 
                Log.v("1", e.toString()); 
            } 

        }
        //camera
        else if(requestCode == 1){
        	try {
	        	super.onActivityResult(requestCode, resultCode, data);	        	
	        	Bundle extras = data.getExtras(); 
	        	Bitmap bitmap = (Bitmap)extras.get("data");
        		sendMsgDlg_cam(CookiesActivity.this, atWhoStr, bitmap);
		    	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }

    } 
	public void sendMsgDlg_pic(final Context context, String string, final Uri uri){
		
		final LinearLayout ll_2 = (LinearLayout)getLayoutInflater().inflate(R.layout.sendmsg_dialog, null);
		EditText et = (EditText)ll_2.findViewById(R.id.et_word_number);
		if(!string.equals("")){
			TextView tv = (TextView)ll_2.findViewById(R.id.tv_word_number);
			et.setText(string );
			int len = 140 - (string.length());
			if(len >= 0){
				tv.setText("���������� " + len + " ����");
			}
			else{
				tv.setText("�������ѳ��� " + -len + " ����");
			}
		}
		et.addTextChangedListener((new TextWatcher(){
	           @Override
	           public void afterTextChanged(Editable s) {
	               // TODO Auto-generated method stub
	        	   TextView tv_temp = (TextView)ll_2.findViewById(R.id.tv_word_number);
	        	   EditText et_temp = (EditText)ll_2.findViewById(R.id.et_word_number);
	        	   int length = 140 - et_temp.getText().toString().length();
	        	   if(length >= 0){
	        		   tv_temp.setText("���������� " + length + " ����");
		   			}
		   			else{
		   				tv_temp.setText("�������ѳ��� " + -length + " ����");
		   			}
	           }

	           @Override
	           public void beforeTextChanged(CharSequence s, int start, int count,
	                   int after) {
	               // TODO Auto-generated method stub
	        	   
	           }

	           @Override
	           public void onTextChanged(CharSequence s, int start, int before,
	                   int count) {
	               // TODO Auto-generated method stub
	              
	               
	           }
	       }));
		
		new AlertDialog.Builder(context).setView(ll_2).setTitle("����ͼƬ")
        .setPositiveButton("����ͼƬ", new DialogInterface.OnClickListener() {
        	@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					EditText et_temp = (EditText)ll_2.findViewById(R.id.et_word_number);
					Cursor cursor = getContentResolver().query(uri, null, null, null, null);
					cursor.moveToFirst();

					mApi.postTwitPic(new File(cursor.getString(1)), et_temp.getText().toString());

					Toast.makeText(context, "����Ϣ�ѷ���", Toast.LENGTH_SHORT).show();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.v("IOException",e.toString());
				} catch (AuthException e) {
					// TODO Auto-generated catch block
					Log.v("AuthException",e.toString());
				} catch (ApiException e) {
					// TODO Auto-generated catch block
					Log.v("ApiException",e.toString());
				}						
			}
        	
        }).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).show();
	}
	
	public void sendMsgDlg_cam(final Context context, String string, final Bitmap bitmap){
		
		final LinearLayout ll_2 = (LinearLayout)getLayoutInflater().inflate(R.layout.sendmsg_dialog, null);
		EditText et = (EditText)ll_2.findViewById(R.id.et_word_number);
		if(!string.equals("")){
			TextView tv = (TextView)ll_2.findViewById(R.id.tv_word_number);
			et.setText(string);
			int len = 140 - ( string.length());
			if(len >= 0){
				tv.setText("���������� " + len + " ����");
			}
			else{
				tv.setText("�������ѳ��� " + -len + " ����");
			}
		}
		et.addTextChangedListener((new TextWatcher(){
	           @Override
	           public void afterTextChanged(Editable s) {
	               // TODO Auto-generated method stub
	        	   TextView tv_temp = (TextView)ll_2.findViewById(R.id.tv_word_number);
	        	   EditText et_temp = (EditText)ll_2.findViewById(R.id.et_word_number);
	        	   int length = 140 - et_temp.getText().toString().length();
	        	   if(length >= 0){
	        		   tv_temp.setText("���������� " + length + " ����");
		   			}
		   			else{
		   				tv_temp.setText("�������ѳ��� " + -length + " ����");
		   			}
	           }

	           @Override
	           public void beforeTextChanged(CharSequence s, int start, int count,
	                   int after) {
	               // TODO Auto-generated method stub
	        	   
	           }

	           @Override
	           public void onTextChanged(CharSequence s, int start, int before,
	                   int count) {
	               // TODO Auto-generated method stub
	              
	               
	           }
	       }));
		
		new AlertDialog.Builder(context).setView(ll_2).setTitle("����ͼƬ")
        .setPositiveButton("����ͼƬ", new DialogInterface.OnClickListener() {
        	@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					EditText et_temp = (EditText)ll_2.findViewById(R.id.et_word_number);
					String filePath = "/sdcard/temp.jpeg";
					File f = new File(filePath);
					OutputStream os = new FileOutputStream(f); 					
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os); 
					os.close();
					mApi.postTwitPic(f, et_temp.getText().toString());
					f.delete();
					bitmap.recycle();
					Toast.makeText(context, "����Ϣ�ѷ���", Toast.LENGTH_SHORT).show();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.v("IOException",e.toString());
				} catch (AuthException e) {
					// TODO Auto-generated catch block
					Log.v("AuthException",e.toString());
				} catch (ApiException e) {
					// TODO Auto-generated catch block
					Log.v("ApiException",e.toString());
				}						
			}
        	
        }).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).show();
	}
	
	
	
	
	public void fillFriendsTimeline(int page){
		JSONArray jsonArray = null;
		try{
			jsonArray = mApi.getFavorites(page);
	    } catch (IOException e) {
	        Log.e(TAG, e.getMessage(), e);
	    } catch (AuthException e) {
	        Log.i(TAG, "Invalid authorization.");
	    } catch (ApiException e) {
	        Log.e(TAG, e.getMessage(), e);
	    }
	      
	    if(jsonArray != null){	 
	    	JSONObject[] objects = new JSONObject[jsonArray.length()];	
	    	
	    	for(int i = 0; i < jsonArray.length(); i++){
	    		try {
	    			Map<String, Object> item = new HashMap<String, Object>();

					objects[i] = jsonArray.getJSONObject(i);
					
					Bitmap user_img = null;
					String user_text = "";
					String user_id = "";
					
					
					
					boolean fav = objects[i].getBoolean("favorited");
				
					
										
									
					String tweet_id =objects[i].getString("id");
					String tweet_text = Html.fromHtml(objects[i].getString("text")).toString();
					String meta_text = changeDate(objects[i].getString("created_at"))+ "  ����"
										 + Html.fromHtml(objects[i].getString("source")).toString();
					
					
					user_img =getBitMap(objects[i].getJSONObject("user").getString("profile_image_url"));
					
					
					user_text = objects[i].getJSONObject("user").getString("name");
					
					user_id = objects[i].getJSONObject("user").getString("id");
					
					if(fav) {
	    				item.put("tweet_fav", R.drawable.fav_on);
	    			}
					if(!fav){
						item.put("tweet_fav" , R.drawable.none);
					}
					
					if(objects[i].has("photo")){
						item.put("tweet_has_image", R.drawable.pic);
						String imgUrl = objects[i].getJSONObject("photo").getString("largeurl");
						tweet_text = objects[i].getString("text");
						tweet_text =tweet_text.replaceAll("<a href=\"(.*)\">(.+?)</a>", imgUrl);						
					}
					else{
						item.put("tweet_has_image", R.drawable.none);
					}
					
					item.put("fav", fav);
					item.put("tweet_user_id", user_id);
					item.put("tweet_id", tweet_id);
	    			item.put("profile_image", user_img);
	    			item.put("tweet_user_text", user_text);
	    			item.put("tweet_text", tweet_text);
	    			item.put("tweet_meta_text", meta_text);
	    			
	    			data.add(item);
	    			
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("jsonArray Error", e.toString());
				}
				
	    	}
		
	    //	TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from, to);

	    //    mlv_list.setAdapter(TweetAdapter);
	        Log.i(TAG, "jsonArray");
	    }
	      
	}
	
	
	public void fillFriendsTimeline(int count ,String signature){//signature����������string����������������page����
		JSONArray jsonArray = null;
		try{
			jsonArray = mApi.getFavorites(count);
	    } catch (IOException e) {
	        Log.e(TAG, e.getMessage(), e);
	    } catch (AuthException e) {
	        Log.i(TAG, "Invalid authorization.");
	    } catch (ApiException e) {
	        Log.e(TAG, e.getMessage(), e);
	    }
	      
	    if(jsonArray != null){	 
	    	JSONObject[] objects = new JSONObject[jsonArray.length()];	
	    	
	    	for(int i = 0; i < jsonArray.length(); i++){
	    		try {
	    			Map<String, Object> item = new HashMap<String, Object>();

					objects[i] = jsonArray.getJSONObject(i);
					
					Bitmap user_img = null;
					String user_text = "";
					String user_id = "";
					
					
					
					boolean fav = objects[i].getBoolean("favorited");
				
					
										
									
					String tweet_id =objects[i].getString("id");
					String tweet_text = Html.fromHtml(objects[i].getString("text")).toString();
					String meta_text = changeDate(objects[i].getString("created_at"))+ "  ����"
										 + Html.fromHtml(objects[i].getString("source")).toString();
					
					
					user_img =getBitMap(objects[i].getJSONObject("user").getString("profile_image_url"));
					
					
					user_text = objects[i].getJSONObject("user").getString("name");
					
					user_id = objects[i].getJSONObject("user").getString("id");
					
					if(fav) {
	    				item.put("tweet_fav", R.drawable.fav_on);
	    			}
					if(!fav){
						item.put("tweet_fav" , R.drawable.none);
					}
					
					if(objects[i].has("photo")){
						item.put("tweet_has_image", R.drawable.pic);
						String imgUrl = objects[i].getJSONObject("photo").getString("largeurl");
						tweet_text = objects[i].getString("text");
						tweet_text =tweet_text.replaceAll("<a href=\"(.*)\">(.+?)</a>", imgUrl);						
					}
					else{
						item.put("tweet_has_image", R.drawable.none);
					}
					
					item.put("fav", fav);
					item.put("tweet_user_id", user_id);
					item.put("tweet_id", tweet_id);
	    			item.put("profile_image", user_img);
	    			item.put("tweet_user_text", user_text);
	    			item.put("tweet_text", tweet_text);
	    			item.put("tweet_meta_text", meta_text);
	    			
	    			data.add(item);
	    			
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("jsonArray Error", e.toString());
				}
				
	    	}
		
	    //	TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from, to);

	    //    mlv_list.setAdapter(TweetAdapter);
	        Log.i(TAG, "jsonArray");
	    }
	      
	}
	
	
	
	
	
	
	
	
	
	public String changeDate(String strTime){
		String[] tokens = strTime.split(" ");
		if(tokens[1].equals("Jan")) tokens[1] = "01";
		else if(tokens[1].equals("Feb")) tokens[1] = "02";
		else if(tokens[1].equals("Mar")) tokens[1] = "03";
		else if(tokens[1].equals("Apr")) tokens[1] = "04";
		else if(tokens[1].equals("May")) tokens[1] = "05";
		else if(tokens[1].equals("Jun")) tokens[1] = "06";
		else if(tokens[1].equals("Jul")) tokens[1] = "07";
		else if(tokens[1].equals("Aug")) tokens[1] = "08";
		else if(tokens[1].equals("Sep")) tokens[1] = "09";
		else if(tokens[1].equals("Out")) tokens[1] = "10";
		else if(tokens[1].equals("Nov")) tokens[1] = "11";
		else if(tokens[1].equals("Dec")) tokens[1] = "12";
		String time = tokens[5] + "-" + tokens[1] + "-" + tokens[2] + " " + tokens[3];
		return time;
	}
	
	public void init_reflesh(){
		Map<String, Object> item_reflesh = new HashMap<String, Object>();
    	item_reflesh.put("fav", false);
    	item_reflesh.put("tweet_fav",R.drawable.none);
    	item_reflesh.put("tweet_user_id", "");
    	item_reflesh.put("tweet_id", "");
    	item_reflesh.put("profile_image", R.drawable.none);
    	item_reflesh.put("tweet_user_text","" );
    	item_reflesh.put("tweet_text", "\t\t\t\t\t\tˢ��");
    	item_reflesh.put("tweet_meta_text", "");
    	item_reflesh.put("tweet_has_image", R.drawable.none);

		data.add(item_reflesh);

	//	TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from, to);
	//	mlv_list.setAdapter(TweetAdapter);
	}
	
	public void init_more(){
		Map<String, Object> item_more = new HashMap<String, Object>();
		item_more.put("fav", false);
    	item_more.put("tweet_fav",R.drawable.none);
    	item_more.put("tweet_user_id", "");
    	item_more.put("tweet_id", "");
    	item_more.put("profile_image", R.drawable.none);
    	item_more.put("tweet_user_text","" );
    	item_more.put("tweet_text", "\t\t\t\t\t\t����");
    	item_more.put("tweet_meta_text", "");
    	item_more.put("tweet_has_image", R.drawable.none);
    	
		data.add(item_more);
	//	TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from ,to);
	//	mlv_list.setAdapter(TweetAdapter);
	}
	
	public void reflesh_status(){
		
		data.clear();
		init_reflesh();
			
		fillFriendsTimeline(1);
			
		init_more();
		
		
		
		List<Map<String,Object>> data_temp2 = new ArrayList<Map<String,Object>>();
		
		
		for(int i=0;i<data.size();i++){
	    	data_temp2.add(data.get(i)); 
	    }
		data_temp2.remove(0);
		CookiesSaveInMemCache(data_temp2);
	}
	
	public void more_status(){
		if(mlv_list.getAdapter().getCount() <= 2){
			data.clear();
			init_reflesh();
			
			fillFriendsTimeline(1);
			
			init_more();
			
		}
		else{//�����ݵ����
			String maxId = (String)data.get(data.size() - 2).get("tweet_id");
			position_more = data.size() - 2;
			
			data.remove(data.size() - 1);
			
			int page = (data.size() - 2)/10;
			
			fillFriendsTimeline(page + 1);
			
			init_more();
		    
	//		mlv_list.setSelectionFromTop(position, 0);
		}
		List<Map<String,Object>> data_temp2 = new ArrayList<Map<String,Object>>();
		
		
		for(int i=0;i<data.size();i++){
	    	data_temp2.add(data.get(i)); 
	    }
		data_temp2.remove(0);
		CookiesSaveInMemCache(data_temp2);

	}
}
