package com.course.android.voicechanger;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class StartPage extends Activity {

	private static final boolean DEBUG = false;
    private static int intScreenX;
    private static int intScreenY;
    private static Rect screenRect;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		intScreenX = dm.widthPixels;
		intScreenY = dm.heightPixels;
		screenRect = new Rect(0, 0, intScreenX, intScreenY);
		
		hideTheWindowTitle();
		
        SampleView view = new SampleView(this);
        setContentView(view);
        
//        MediaPlayer mp = new MediaPlayer(); 
//        mp = MediaPlayer.create(StartPage.this, R.raw.conanaudio);
//        try {
//			mp.prepare();
//	        mp.start();
//        } catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}   
    }
    
    /**
     * Invoked during init to give the Activity a chance to set up its Menu.
     * 
     * @param menu the Menu to which entries may be added
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }    

    /**
     * Invoked when the user selects an item from the Menu.
     * 
     * @param item the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     *         otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	return true;
    }    

    private void hideTheWindowTitle() {
		// Hide the window title.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}    
    
    private void gotoTieActivity(){
    	Intent intent = new Intent();
    	intent.setClass(StartPage.this, TieVoice.class);
    	startActivity(intent);
    	StartPage.this.finish();
    	
    	Log.d("Liwei", "gotoTieActivity");
    }
    
    private void gotoEyeActivity(){
    	Intent intent = new Intent();
    	intent.setClass(StartPage.this, MovingEye.class);
    	startActivity(intent);
    	StartPage.this.finish();    	
    } 
    
    private class SampleView extends View {    	
    	    	    	
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Bitmap mConanBitmap;

    	
        private final Paint mPaint;
        private final Paint mRectPaint;
                
        private Rect tieRect;
        private Rect eyeRect;        
        
    	public SampleView(Context context) {
            
    		super(context);            
    		setFocusable(true);
    		
            mConanBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.front);
            
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setARGB(255, 255, 255, 255);    
            mRectPaint = new Paint();
            mRectPaint.setAntiAlias(true);
            mRectPaint.setARGB(50, 0, 255, 0); 
            
            int tL = (int)((float)40/360* intScreenX);
            int tT = (int)((float)270/480* intScreenY);
            int tR = (int)((float)150/360* intScreenX);
            int tB = (int)((float)390/480* intScreenY);
            tieRect = new Rect(tL,tT,tR,tB);

            int eL = (int)((float)170/360* intScreenX);
            int eT = (int)((float)250/480* intScreenY);
            int eR = (int)((float)230/360* intScreenX);
            int eB = (int)((float)410/480* intScreenY);
            eyeRect = new Rect(eL,eT,eR,eB);            
    	}

        @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            int curW = mBitmap != null ? mBitmap.getWidth() : 0;
            int curH = mBitmap != null ? mBitmap.getHeight() : 0;
            if (curW >= w && curH >= h) {
                return;
            }
            
            if (curW < w) curW = w;
            if (curH < h) curH = h;
            
            Bitmap newBitmap = Bitmap.createBitmap(curW, curH, Bitmap.Config.RGB_565);
            Canvas newCanvas = new Canvas();
            newCanvas.setBitmap(newBitmap);
            if (mBitmap != null) {
                newCanvas.drawBitmap(mBitmap, 0, 0, null);
            }
            mBitmap = newBitmap;
            mCanvas = newCanvas;
            
            // set initial view
            setBitmapViewFull(mConanBitmap);
        }
    	
        @Override protected void onDraw(Canvas canvas) {
        	if (mBitmap != null) {        	                
        		canvas.drawBitmap(mBitmap, 0, 0, null);
        	}
        }        
        
        @Override public boolean onTouchEvent(MotionEvent event) {            
            switch(event.getAction()){            	
	            case MotionEvent.ACTION_DOWN:
	            	checkAction((int)event.getX(), (int)event.getY());
	            	break;
	            case MotionEvent.ACTION_MOVE:
	            	break;
	            case MotionEvent.ACTION_UP:
	            	releaseAction();
	            	break;
            }                       
            return true;
        }
        
        private void setBitmapViewFull(Bitmap bitmap){        	
    		// clear canvas
//    		mPaint.setARGB(0xff, 0, 0, 0);
//          mCanvas.drawPaint(mPaint);
            
        	mCanvas.drawBitmap(bitmap, 0, 0, null);
    		mCanvas.drawBitmap(bitmap, null, screenRect, null);
    		
    		if(DEBUG){
    			mCanvas.drawRect(tieRect, mRectPaint);
    			mCanvas.drawRect(eyeRect, mRectPaint);
    		}    			
        	invalidate();  
        }
        
        private void checkAction(int touchX, int touchY){

            if(tieRect.contains(touchX, touchY)){
            	// goto Tie
            	gotoTieActivity();            	
            }
            else if(eyeRect.contains(touchX, touchY)){
            	// goto Eye
            	gotoEyeActivity();
            }        	
        }
        
        private void releaseAction(){
        	
        }
        
    }
}