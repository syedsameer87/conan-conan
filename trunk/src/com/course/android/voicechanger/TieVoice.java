package com.course.android.voicechanger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.os.Bundle;
import android.os.Environment;
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

public class TieVoice extends Activity {

	private static final boolean DEBUG = false;

	// private Button mButton;
	// private TextView mText;

	private static int intScreenX;
	private static int intScreenY;
	private static Rect screenRect;
	
	private static final int TRUNAROUND_PLAYBACK = 1;
	private static final int REALTIME_PLAYBACK = 1;
	
	private static int PLAY_MODE; 
	private static final int SAMPLE_RATE = 11025;
	private AudioRecord audioRecorder;
	private AudioTrack audioTracker;

	// sound effect
	FlangeBaby fb = new FlangeBaby(SAMPLE_RATE);

	private static final int MENU_EFFECT_1 = 1;
	private static final int MENU_EFFECT_2 = 2;
	private static final int MENU_EFFECT_3 = 3;
	private static final int MENU_EFFECT_4 = 4;
	private static final int GOTO_FRONT = 5;

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

		hideTheWindowTitle();

		SampleView view = new SampleView(this);
		setContentView(view);

		setupRecord();
	}

	private void setupRecord() {
		// Create AudioRecord object to record the audio.
		int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);

		// Create AudioTrack object to playback the audio.
		int iMinBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_STEREO,
				AudioFormat.ENCODING_PCM_16BIT);
		audioTracker = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, iMinBufSize * 10,
				AudioTrack.MODE_STREAM);

		audioTracker.play();
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
		menu.add(0, MENU_EFFECT_1, 0, R.string.menu_effect_1);
		menu.add(0, MENU_EFFECT_2, 0, R.string.menu_effect_2);
		menu.add(0, MENU_EFFECT_3, 0, R.string.menu_effect_3);
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
		else
			soundEffect = item.getItemId();

		return true;
	}

	private void hideTheWindowTitle() {
		// Hide the window title.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	private void gotoFrontActivity() {
		Intent intent = new Intent();
		intent.setClass(TieVoice.this, StartPage.class);
		startActivity(intent);
		TieVoice.this.finish();
	}

	private class SampleView extends View {

		private Thread reocrdThread;
		private Thread blinkThread;

		private short[] audio;

		private Bitmap mBitmap;
		private Canvas mCanvas;

		private Bitmap mTieBitmap;
		private Bitmap mBTieBitmap;
		private Bitmap mRecordBitmap;
		private Bitmap mBRecordBitmap;

		private final Paint mPaint;
		private final Paint mRectPaint;

		private Rect recordRect;
		private Rect playRect;

		private boolean onRecording;
		private boolean recorded;
		private boolean onPlaying;
		private boolean onBlinkOne;

		private Timer timer;
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					if (onRecording) {
						if (onBlinkOne)
							setBitmapViewFull(mBRecordBitmap);
						else
							setBitmapViewFull(mRecordBitmap);
						onBlinkOne = !onBlinkOne;
					}

					// Log.d("Liwei", "handle");
					break;
				}
				super.handleMessage(msg);
			}
		};

		public SampleView(Context context) {

			super(context);
			setFocusable(true);

			mTieBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.necktie);
			mBTieBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.blingnecktie);
			mRecordBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.recording2);
			mBRecordBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.recording2b);

			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setARGB(255, 255, 255, 255);
			mRectPaint = new Paint();
			mRectPaint.setAntiAlias(true);
			mRectPaint.setARGB(50, 0, 255, 0);

			int size = 100 / 2;
			recordRect = new Rect(intScreenX / 2 - size, intScreenY / 2 - size,
					intScreenX / 2 + size, intScreenY / 2 + size);
			playRect = new Rect(0, 0, size * 2, size * 2);

			timer = new Timer();
			TimerTask task = new TimerTask() {
				public void run() {
					Message message = new Message();
					message.what = 1;
					handler.sendMessage(message);
				}
			};
			timer.schedule(task, 1000, 200);

			// blinkThread = new Thread(new Runnable() {
			// public void run() {
			// while (true) {
			// try {
			// Message message = new Message();
			// message.what = 1;
			// handler.sendMessage(message);
			//
			// // Thread.sleep(1000);
			// blinkThread.sleep(500);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			// }
			// });
			// blinkThread.start();

		}

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

			// set initial view
			setBitmapViewFull(mTieBitmap);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (mBitmap != null) {
				canvas.drawBitmap(mBitmap, 0, 0, null);
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				checkAction((int) event.getX(), (int) event.getY());
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				releaseAction();
				break;
			}
			return true;
		}

		private void setBitmapViewFull(Bitmap bitmap) {
			// clear canvas
			// mPaint.setARGB(0xff, 0, 0, 0);
			// mCanvas.drawPaint(mPaint);

			mCanvas.drawBitmap(bitmap, 0, 0, null);
			mCanvas.drawBitmap(bitmap, null, screenRect, null);

			if (DEBUG) {
				mCanvas.drawRect(recordRect, mRectPaint);
				mCanvas.drawRect(playRect, mRectPaint);
			}
			invalidate();
		}

		private void checkAction(int touchX, int touchY) {

			if (recordRect.contains(touchX, touchY)) {
				setBitmapViewFull(mRecordBitmap);

				// do something to start recording
				onRecording = true;
				reocrdThread = new Thread(new Runnable() {
					public void run() {

						recordPlayback();

						// record();
						// pushData();
						recorded = true;
					}
				});
				reocrdThread.start();
			} else if (recorded && playRect.contains(touchX, touchY)) {

				if (onRecording)
					return;

				onPlaying = true;
				// do something to start Playing
				Thread thread = new Thread(new Runnable() {
					public void run() {
						play();
					}
				});
				thread.start();
			}
		}

		private void releaseAction() {

			if (onRecording) {
				setBitmapViewFull(mTieBitmap);
				// do something to stop recording here
				onRecording = false;

			} else if (onPlaying) {
				setBitmapViewFull(mTieBitmap);
				// do something to stop playing here

				// onPlaying = false;
			}
		}

		public void recordPlayback() {

			try {
				// Create a new AudioRecord object to record the audio.
				int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				short[] buffer = new short[bufferSize];
				
				// start recording
				audioRecorder.startRecording();
				while (onRecording) {
					
					// read raw data
					int bufferReadResult = audioRecorder.read(buffer, 0,
							bufferSize);

					// filter the raw data
					fb.filter(buffer, bufferReadResult);

					// playback the raw data
					audioTracker.write(buffer, 0, bufferReadResult);
				}
				audioRecorder.stop();
			} catch (Throwable t) {
				Log.e("AudioRecord", "Recording Failed");
			}
		}

		public void record() {

			int frequency = SAMPLE_RATE;
			int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
			int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
			File file = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/reverseme.pcm");

			// Delete any previous recording.
			if (file.exists())
				file.delete();

			// Create the new file.
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new IllegalStateException("Failed to create "
						+ file.toString());
			}

			try {
				// Create a DataOuputStream to write the audio data into the
				// saved file.
				OutputStream os = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(os);
				DataOutputStream dos = new DataOutputStream(bos);

				// Create a new AudioRecord object to record the audio.
				int bufferSize = AudioRecord.getMinBufferSize(frequency,
						channelConfiguration, audioEncoding);
				AudioRecord audioRecord = new AudioRecord(
						MediaRecorder.AudioSource.MIC, frequency,
						channelConfiguration, audioEncoding, bufferSize);

				short[] buffer = new short[bufferSize];

				audioRecord.startRecording();

				while (onRecording) {
					int bufferReadResult = audioRecord.read(buffer, 0,
							bufferSize);
					Log.d("Liwei", "bufferReadResult: " + bufferReadResult);
					for (int i = 0; i < bufferReadResult; i++)
						dos.writeShort(buffer[i]);
				}

				audioRecord.stop();
				dos.close();

			} catch (Throwable t) {
				Log.e("AudioRecord", "Recording Failed");
			}
		}

		public void pushData() {

			// Get the file we want to playback.
			File file = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/reverseme.pcm");
			// Get the length of the audio stored in the file (16 bit so 2 bytes
			// per short)
			// and create a short array to store the recorded audio.
			int audioLength = (int) (file.length() / 2);
			audio = new short[audioLength];

			try {
				// Create a DataInputStream to read the audio data back from the
				// saved file.
				InputStream is = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(is);
				DataInputStream dis = new DataInputStream(bis);

				// Read the file into the music array.
				int i = 0;
				while (dis.available() > 0) {
					audio[i] = dis.readShort();
					i++;
				}

				// Close the input streams.
				dis.close();

			} catch (Throwable t) {
				Log.e("AudioTrack", "Playback Failed");
			}
		}

		public void playback(short[] data) {
			// // Create a new AudioTrack object using the same parameters as
			// the
			// // AudioRecord
			// // object used to create the file.
			// AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
			// SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
			// AudioFormat.ENCODING_PCM_16BIT, data.length,
			// AudioTrack.MODE_STREAM);
			// // Start playback
			// audioTrack.play();
			//
			// // Write the music buffer to the AudioTrack object
			// audioTrack.write(data, 0, data.length);

			audioTracker.write(data, 0, data.length);
		}

		public void play() {

			int audioLength = audio.length;

			// duplicate audio data for manipulation
			float[] faudio = new float[audioLength];
			for (int i = 0; i < audio.length; i++)
				faudio[i] = ((float) audio[i]) / 32768;

			// for (int i = 0; i < audio.length; i++){
			// if(audio[i] > 32768)
			// Log.d("Liwei", "bad, big");
			// if(audio[i] < -32768)
			// Log.d("Liwei", "bad, small");
			// }

			short[] makeAudio = new short[audioLength];

			// do something to apply sound effect, according to soundEffect
			switch (soundEffect) {
			case MENU_EFFECT_1: // normal
				for (int i = 0; i < audioLength; i++)
					makeAudio[i] = audio[i];
				break;

			case MENU_EFFECT_2: // inverse
				fb.filter(audio, audio.length);
				// Log.d("Liwei", "EFFECT 2");
				// PitchShifter2.PitchShift(1.5f, audioLength, SAMPLE_RATE,
				// faudio);
				// Log.d("Liwei", "PitchShift-ed");
				// for (int i = 0; i < audioLength; i++)
				// makeAudio[i] = (short)(faudio[i]*32768);
				// Log.d("Liwei", "Scale-ed");
				break;

			case MENU_EFFECT_3: //
				Log.d("Liwei", "EFFECT 3");
				PitchShifter2
						.PitchShift(0.5f, audioLength, SAMPLE_RATE, faudio);
				for (int i = 0; i < audioLength; i++)
					makeAudio[i] = (short) (faudio[i] * 32768);
				break;

			case MENU_EFFECT_4:
				break;

			case GOTO_FRONT:
				break;
			}

			// Create a new AudioTrack object using the same parameters as the
			// AudioRecord
			// object used to create the file.
			AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
					SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, audioLength,
					AudioTrack.MODE_STREAM);
			// Start playback
			audioTrack.play();
			audioTrack.setNotificationMarkerPosition(audioLength);
			audioTrack.setPositionNotificationPeriod(SAMPLE_RATE / 6);
			audioTrack
					.setPlaybackPositionUpdateListener(new OnPlaybackPositionUpdateListener() {
						public void onMarkerReached(AudioTrack track) {
							setBitmapViewFull(mTieBitmap);
						}

						public void onPeriodicNotification(AudioTrack track) {
							if (onBlinkOne)
								setBitmapViewFull(mBTieBitmap);
							else
								setBitmapViewFull(mTieBitmap);
							onBlinkOne = !onBlinkOne;
						}
					});

			// Write the music buffer to the AudioTrack object
			audioTrack.write(makeAudio, 0, audioLength);
		}
	}
}