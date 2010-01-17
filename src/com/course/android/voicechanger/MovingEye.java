package com.course.android.voicechanger;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MovingEye extends Activity {

	private static final boolean DEBUG = false;

	private static final int GOTO_FRONT = 5;

	// private Button mButton;
	// private TextView mText;
	private TextView myTv;

	private static int intScreenX;
	private static int intScreenY;
	private static Rect screenRect;

	private static float currX, currY, currZ;
	private static float defaultX, defaultY, defaultZ;

	// private Bitmap mFaceBitmap;
	// private Bitmap mEyeBitmap;
	// private Bitmap mBgBitmap;

	// private Bitmap mBitmap;
	// private Canvas mCanvas;

	private SensorManager mySensorMgr;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		intScreenX = dm.widthPixels;
		intScreenY = dm.heightPixels;
		screenRect = new Rect(0, 0, intScreenX, intScreenY);

		// getWindow().setBackgroundDrawable( new ColorDrawable(Color.WHITE));

		hideTheWindowTitle();

		SampleView view = new SampleView(this);
		setContentView(view);

		mySensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // Get
		// sensor
		List<Sensor> sensorList = mySensorMgr
				.getSensorList(Sensor.TYPE_ORIENTATION); // SensorManager.SENSOR_ORIENTATION(SENSOR_ACCELEROMETER)
		mySensorMgr.registerListener(mySensorListener, sensorList.get(0),
				SensorManager.SENSOR_DELAY_UI);

	}

	/**
	 * Invoked during init to give the Activity a chance to set up its Menu.
	 * 
	 * @param menu
	 *            the Menu to which entries may be added
	 * @return true
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, GOTO_FRONT, 0, R.string.menu_back);
		return true;
	}

	/**
	 * Invoked when the user selects an item from the Menu.
	 * 
	 * @param item
	 *            the Menu entry which was selected
	 * @return true if the Menu item was legit (and we consumed it), false
	 *         otherwise
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == GOTO_FRONT)
			gotoFrontActivity();
		return true;
	}

	private void gotoFrontActivity() {
		Intent intent = new Intent();
		intent.setClass(MovingEye.this, StartPage.class);
		startActivity(intent);
		MovingEye.this.finish();
	}	
	
	/*
	 * @Override protected void onResume() {
	 * mySensorMgr.registerListener(mySensorListener,
	 * SensorManager.SENSOR_ACCELEROMETER,SensorManager.SENSOR_DELAY_UI);
	 * super.onResume(); }
	 * 
	 * @Override protected void onPause() {
	 * mySensorMgr.unregisterListener(mySensorListener); super.onPause(); }
	 */

	private final SensorEventListener mySensorListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {

			currX = event.values[0];
			currY = event.values[1];
			currZ = event.values[2];

			String str = "X= " + currX + ",Y= " + currX + "Z= " + currX;
			// Log.d("shooting",str);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
		/*
		 * @Override public void onSensorChanged(int sensor,float[] value) {
		 * float x = value[SensorManager.DATA_X]; float y =
		 * value[SensorManager.DATA_Y]; float z = value[SensorManager.DATA_Z];
		 * 
		 * String str = "X= "+x+",Y= "+y+"Z= "+z; Log.d("shooting",str);
		 * //myTv.setText("ABc"); //Toast.makeText(VoiceChanger.this,
		 * "X= "+x+",Y= "+y+"Z= "+z,Toast.LENGTH_LONG).show(); }
		 * 
		 * @Override public void onAccuracyChanged(int sensor,int accuracy) {
		 * 
		 * }
		 */
	};

	private void hideTheWindowTitle() {
		// Hide the window title.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	private class SampleView extends View {

		private Bitmap mBitmap;
		private Canvas mCanvas;

		private Bitmap mFaceBitmap;
		private Bitmap mEyeBitmap;
		private Bitmap mBgBitmap;
		
		float currPosX;
		float currPosY;
		//510
		float currPosXe;
		float currPosYe;
		
		float defaultPosX;
		float defaultPosY;
		//510
		//float defaultCenPosX;
		//float defaultCenPosY;
		
		boolean firstAccess = false;

		// private final Paint mPaint;
		// private final Paint mRectPaint;
		private final Paint mTestPaint;

		// private Rect recordRect;
		// private Rect playRect;
		private Rect testRect;

		private Timer update_timer = new Timer(); // 定義一個Timer
		private Handler handler = new Handler(){  
	        public void handleMessage(Message msg) {  
	            switch (msg.what) {      
	            case 1:    
	            	doWhat();
	                break; 
	            }
	            super.handleMessage(msg);  
	        }  
	    };  

		private TimerTask update_task = new TimerTask() {
			public void run() {
	            Message message = new Message();      
	            message.what = 1;      
	            handler.sendMessage(message);   				
			}
		};
		
		private void doWhat(){
			// TODO Auto-generated method stub
			Log.d("shooting", "(" + currX + "," + currY + "," + currZ
							+ ")");
			Log.d("shooting", "Default" + "(" + defaultX + "," + defaultY
					+ "," + defaultZ + ")");
			Log.d("shooting", "pos  "
					+ (intScreenX / 2.0 + (currX - defaultX) * 1) + ","
					+ (intScreenY / 2.0 + (currZ - defaultZ) * 1));
			//mTestPaint.setARGB(100, 0, 255, 0);

//			if(!firstAccess)
//			{
//				currPosX -= (currZ-defaultZ)*1;
//				currPosY += (currX-defaultX)*1;
//			}
//			else
//			{
//				firstAccess = true;
//				currPosX = defaultPosX;
//				currPosY = defaultPosY;
//			}
			
			//currPosX = defaultPosX - (currZ-defaultZ)*1;
			//currPosY = defaultPosY + (currX-defaultX)*1;

			//510
			float radius = 500;
			float distancepow =(float) ((float) Math.pow((currZ-defaultZ)*2, 2)+ Math.pow((currX-defaultX)*1.5, 2));
			float distance = (float) Math.pow(distancepow, 0.5);
			if(distance > radius)
			{
				//float currPosXend = defaultPosX - (currZ-defaultZ)* radius / distance;
				//float currPosYend = defaultPosY + (currX-defaultX)* radius / distance;				
				//currPosX = currPosXe;
				//currPosY = currPosYe;
				//for(float i=currPosXe ; i<currPosXend ; i++)
				//{
				//	currPosX ++;
				//	
				//}
			
				currPosX = (float) (defaultPosX - (currZ-defaultZ)*2* radius / distance);
				currPosY = (float) (defaultPosY + (currX-defaultX)*1.5* radius / distance);		
			} 
			else
			{
				currPosX = (float) (defaultPosX - (currZ-defaultZ)*2);
				currPosY = (float) (defaultPosY + (currX-defaultX)*1.5);
			}
			 
			mCanvas.drawBitmap(mBgBitmap,0,0,null);
			mCanvas.drawBitmap(mFaceBitmap, 0, 0, null);
			mCanvas.drawBitmap(mEyeBitmap, (float) currPosX , (float) currPosY , null);
			mCanvas.drawRect(testRect, mTestPaint);
			// mCanvas.drawBitmap(mEyeBitmap, (float) 100, (float) 100,
			// null);

			invalidate();
		}

		public SampleView(Context context) {

			super(context);
			setFocusable(true);

			mFaceBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.conaneye1b);
			mEyeBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.eye5);
			mBgBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.conanbackground);

			/*
			 * mPaint = new Paint(); mPaint.setAntiAlias(true);
			 * mPaint.setARGB(255, 255, 255, 255);
			 * 
			 * mRectPaint = new Paint(); mRectPaint.setAntiAlias(true);
			 * mRectPaint.setARGB(50, 0, 255, 0);
			 */

			testRect = new Rect(intScreenX-100, intScreenY-100,
					intScreenX , intScreenY);
			mTestPaint = new Paint();
			mTestPaint.setAntiAlias(true);
			mTestPaint.setARGB(100, 255, 0, 0);

			update_timer.schedule(update_task, 1000, 100);
		}

		// public void MoveEye()
		// {
		// mCanvas.drawRect(testRect, mTestPaint);
		// mCanvas.drawBitmap(mEyeBitmap, (float) 100, (float) 100, null);
		//
		// invalidate();
		// }

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			int curW = mBitmap != null ? mBitmap.getWidth() : 0;
			int curH = mBitmap != null ? mBitmap.getHeight() : 0;
			if (curW >= w && curH >= h) {
				return;
			}

			if (curW < w)
				curW = w;
			if (curH < h)
				curH = h;

			Bitmap newBitmap = Bitmap.createBitmap(curW, curH,
					Bitmap.Config.RGB_565);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);
			if (mBitmap != null) {
				newCanvas.drawBitmap(mBitmap, 0, 0, null);
			}
			mBitmap = newBitmap;
			mCanvas = newCanvas;

			Paint eyePaint = new Paint();

			// set initial view
			// setBitmapViewFull(mBgBitmap);
			// mCanvas.drawBitmap(mEyeBitmap, 0, 0, null);
			defaultPosX = (intScreenX/2 - 50);
			defaultPosY = (intScreenY/2 - 80);
		
			//510
			currPosXe = defaultPosX;
			currPosYe = defaultPosY;	
			//defaultCenPosX = defaultPosX + mEyeBitmap.getWidth();
			//defaultCenPosY = defaultPosY + mEyeBitmap.getHeight();	
			
			mCanvas.drawBitmap(mFaceBitmap, 0, 0, null);
			mCanvas.drawBitmap(mEyeBitmap, (float) defaultPosX, (float)defaultPosY , eyePaint);
			mCanvas.drawRect(testRect, mTestPaint);
			// setBitmapViewFull(mFaceBitmap);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (mBitmap != null) {
				canvas.drawBitmap(mBitmap, 0, 0, null);
			}
		}

		private void setBitmapViewFull(Bitmap bitmap) {
			// clear canvas
			// mPaint.setARGB(0xff, 0, 0, 0);
			// mCanvas.drawPaint(mPaint);

			mCanvas.drawBitmap(bitmap, 0, 0, null);
			mCanvas.drawBitmap(bitmap, null, screenRect, null);

			if (DEBUG) {
				// mCanvas.drawRect(recordRect, mRectPaint);
				// mCanvas.drawRect(playRect, mRectPaint);

				// mCanvas.drawRect(testRect,mTestPaint);
			}
			invalidate();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				releaseAction((int) event.getX(), (int) event.getY());
				break;
			}
			return true;
		}

		public void releaseAction(int touchX, int touchY) {
			if (testRect.contains(touchX, touchY)) {
				// keep frontal direction
				Log.d("shooting", "inRegion");
				defaultX = currX;
				defaultY = currY;
				defaultZ = currZ;
				String str = "Default" + defaultX + "," + defaultY + ","
						+ defaultZ;
				Log.d("shooting", str);
			}
		}

		// @Override
		// public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Log.d("shooting", "OnKeyDown");
		//
		// if (keyCode == KeyEvent.KEYCODE_MENU) {
		// String str = "Menu";
		// Log.d("shooting", str);
		//
		//				
		//
		// mTestPaint.setARGB(100, 0, 255, 0);
		// mCanvas.drawRect(testRect, mTestPaint);
		// mCanvas.drawBitmap(mEyeBitmap, (float) 100, (float) 100, null);
		//
		// invalidate();
		// } else {
		// Log.d("shooting", "Failed");
		// }
		// return super.onKeyDown(keyCode, event);
		// }

	}
}