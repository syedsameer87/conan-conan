package com.course.android.voicechanger;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class VoiceChanger extends Activity {	
	
	private static boolean DEBUG = true;
	
//    private Button mButton;
//    private TextView mText;
    
    private static int intScreenX;
    private static int intScreenY;
    private static Rect screenRect;

    private static final int MENU_EFFECT_1 = 1;
    private static final int MENU_EFFECT_2 = 2;
    private static final int MENU_EFFECT_3 = 3;
    private static final int MENU_EFFECT_4 = 4;
    private static final int MENU_EFFECT_5 = 5;
    
    private static int soundEffect = MENU_EFFECT_1;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		intScreenX = dm.widthPixels;
		intScreenY = dm.heightPixels;
		screenRect = new Rect(0, 0, intScreenX, intScreenY);
		
        SampleView view = new SampleView(this);
        setContentView(view);
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

        menu.add(0, MENU_EFFECT_1, 0, R.string.menu_effect_1);
        menu.add(0, MENU_EFFECT_2, 0, R.string.menu_effect_2);
        menu.add(0, MENU_EFFECT_3, 0, R.string.menu_effect_3);
        menu.add(0, MENU_EFFECT_4, 0, R.string.menu_effect_4);
        menu.add(0, MENU_EFFECT_5, 0, R.string.menu_effect_5);
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
    	soundEffect = item.getItemId();
    	Log.d("Liwei", "SoundEffect Used: " + soundEffect);
    	return true;
    }    
    
    private static class SampleView extends View {    	
    	
        private Bitmap mBitmap;
        private Canvas mCanvas;

        private Bitmap mTouchBitmap;
        private Bitmap mRecordingBitmap;
        private Bitmap mPlayingBitmap;
    	
        private final Paint mPaint;
        private final Paint mRectPaint;
                
        private Rect recordRect;
        private Rect playRect;
        
        private boolean onRecording;
        private boolean onPlaying;    
        
    	public SampleView(Context context) {
            
    		super(context);            
    		setFocusable(true);
    		
            mTouchBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.touch);
            mRecordingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.recording);
            mPlayingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.playing);
            
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setARGB(255, 255, 255, 255);    
            mRectPaint = new Paint();
            mRectPaint.setAntiAlias(true);
            mRectPaint.setARGB(50, 0, 255, 0); 
            
            int size = 100/2;
            recordRect = new Rect(intScreenX/2 - size, intScreenY/2 - size, intScreenX/2 + size, intScreenY/2 + size);            
            playRect = new Rect(0, 0, size*2, size*2);            
    	}

//        public void clear() {
//            if (mCanvas != null) {
//                mPaint.setARGB(0xff, 0, 0, 0);
//                mCanvas.drawPaint(mPaint);
//                invalidate();
//            }
//        }

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
            setBitmapViewFull(mTouchBitmap);
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
            		Log.d("Liwei","Action Down");
	            	break;
	            case MotionEvent.ACTION_MOVE:
	            	break;
	            case MotionEvent.ACTION_UP:
	            	releaseAction();
	            	Log.d("Liwei","Action Up");
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
    			mCanvas.drawRect(recordRect, mRectPaint);
    			mCanvas.drawRect(playRect, mRectPaint);
    		}    			
        	invalidate();  
        }
        
        private void checkAction(int touchX, int touchY){

            if(recordRect.contains(touchX, touchY)){
            	setBitmapViewFull(mRecordingBitmap);
            	onRecording = true;            	
            	// do something to start recording
            	
            }
            else if(playRect.contains(touchX, touchY)){
            	setBitmapViewFull(mPlayingBitmap);
            	onPlaying = true;            	
            	// do something to start Playing
            	
            	// do something to apply sound effect, according to soundEffect
            	switch(soundEffect){
	            	case MENU_EFFECT_1:
	            		break;
	            	case MENU_EFFECT_2:
	            		break;
	            	case MENU_EFFECT_3:
	            		break;
	            	case MENU_EFFECT_4:
	            		break;
	            	case MENU_EFFECT_5:
	            		break;
            	}
            }        	
        }
        
        private void releaseAction(){
        	
        	if(onRecording){
        		setBitmapViewFull(mTouchBitmap);        		
        		// do something to stop recording here

        		onRecording = false;
        		
        	}else if(onPlaying){
        		setBitmapViewFull(mTouchBitmap);        		
        		// do something to stop playing here

        		onPlaying = false;
        	}        	
        }
        

    }
}