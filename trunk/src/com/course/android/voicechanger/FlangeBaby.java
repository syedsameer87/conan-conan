package com.course.android.voicechanger;

import android.util.Log;

public class FlangeBaby {

	int SampleRate;
	
	int MAX_WG_DELAY = 16384;
	float[] buffer = new float[2048 + MAX_WG_DELAY * 2 + 2];	
    int counter;
    int buffer0;
    int buffer1;
    float feedback;
    float delay;
    float tdelay0;
    float tdelay1;
    float rate;
    float mix;
    float beat;
    float triangle;
    float sinusoid;
    float lfo;
    float twopi;
    float offset;
    float sdelay0;
    float sdelay1;
    float depth;
    float tpos;
    float trate;
    int dir0;
    int dir1;
    
    float slider1;
    float slider2;
    float slider3;
    float slider4;
    float slider5;
    float slider6;
    float slider7;
    float slider8;
	
    public FlangeBaby(int _SampleRate){
    	
    	SampleRate = _SampleRate;
    	
    	init();
    }
    
    public void init(){
        feedback = 0;
        delay = 5;
        tdelay0 = delay;
        tdelay1 = delay;
        rate = 0;
        mix = 0;
        beat = 0.25f;
        triangle = 0;
        sinusoid = 1;
        lfo = triangle;
        twopi = (float)(2 * Math.PI);    
        
        Slider();
    }
    
    public void Slider()
    {
    	
//        AddSlider(5, 1, 10, 0.01f, "Flange (Delay)");
//        AddSlider(0.5f, 0, 1, 0.01f, "Depth");
//        AddSlider(0, -1, 1, 0.01f, "Feedback");
//        AddSlider(0, 0, 10, 0.01f, "Speed (Hz, 0=tempo)");
//        AddSlider(0.5f, 0, 1, 0.01f, "Mix");
//        AddSlider(0, 0, 5, 0.01f, "Channel Offset");
//        AddSlider(0.25f, 0.0625f, 4, 0.0625f, "beatsync - fraction of whole note");
//        Slider waveformSlider = AddSlider(0, 0, 1, 1, "LFO Waveform");
//    	slider1 = 5;
//    	slider2 = 0.5f;
//    	slider3 = 0;
//    	slider4 = 0;
//    	slider5 = 0.5f;
//    	slider6 = 0;
//    	slider7 = 0.25f;
//    	slider8 = 0;    	
    	slider1 = 9.01f;
    	slider2 = 1;
    	slider3 = 0.93f;
    	slider4 = 8.87f;
    	slider5 = 0.9f;
    	slider6 = 1.85f;
    	slider7 = 1.43f;
    	slider8 = 0;    	
    	
        delay = slider1;
        offset = slider6;
        beat = 240 * slider7;
        tdelay0 = delay;
        tdelay1 = (delay + offset);
        sdelay0 = tdelay0 / 1000 * SampleRate;
        sdelay1 = tdelay1 / 1000 * SampleRate;
        feedback = slider3;
        depth = (delay - 0.1f) * slider2;
        mix = slider5;
//        lfo = (slider8 == triangle ? triangle : sinusoid);
        lfo = triangle;
        tpos = 0;
    }
    

    public void filter(short[] audio, int length){
    	
    	int audioLength = length;
		float[] faudio = new float[audioLength];
		for (int i = 0; i < audioLength; i++)
			faudio[i] = ((float)audio[i])/32768;
    	
        	
    	Log.d("Liwei", "audio.length: " + audioLength);
    	float ori = 0;
    	int c = 0;
    	for(int i=0;i<audioLength;i++){
    		ori = faudio[i];
    		faudio[i] = Sample(faudio[i], faudio[i]);
    		if(ori != faudio[i])
    			c++;
    	}
    	Log.d("Liwei", "# of diff: " + c);
    	Log.d("Liwei", "mix: " + mix);

		for (int i = 0; i < audioLength; i++)
			audio[i] = (short)(faudio[i]*32768);
    	
    }

    public float Sample(float spl0, float spl1)
    {    	
        float back0 = counter - sdelay0;
        float back1 = counter - sdelay1;
        if (back0 < 0) back0 = MAX_WG_DELAY + back0;
        if (back1 < 0) back1 = MAX_WG_DELAY + back1;
        int index00 = (int)back0;
        int index01 = (int)back1;
        int index_10 = index00 - 1;
        int index_11 = index01 - 1;
        int index10 = index00 + 1;
        int index11 = index01 + 1;
        int index20 = index00 + 2;
        int index21 = index01 + 2;
        if (index_10 < 0) index_10 = MAX_WG_DELAY + 1;
        if (index_11 < 0) index_11 = MAX_WG_DELAY + 1;
        if (index10 >= MAX_WG_DELAY) index10 = 0;
        if (index11 >= MAX_WG_DELAY) index11 = 0;
        if (index20 >= MAX_WG_DELAY) index20 = 0;
        if (index21 >= MAX_WG_DELAY) index21 = 0;
        float y_10 = buffer[buffer0 + index_10];
        float y_11 = buffer[buffer1 + index_11];
        float y00 = buffer[buffer0 + index00];
        float y01 = buffer[buffer1 + index01];
        float y10 = buffer[buffer0 + index10];
        float y11 = buffer[buffer1 + index11];
        float y20 = buffer[buffer0 + index20];
        float y21 = buffer[buffer1 + index21];
        float x0 = back0 - index00;
        float x1 = back1 - index01;
        float c00 = y00;
        float c01 = y01;
        float c10 = 0.5f * (y10 - y_10);
        float c11 = 0.5f * (y11 - y_11);
        float c20 = y_10 - 2.5f * y00 + 2.0f * y10 - 0.5f * y20;
        float c21 = y_11 - 2.5f * y01 + 2.0f * y11 - 0.5f * y21;
        float c30 = 0.5f * (y20 - y_10) + 1.5f * (y00 - y10);
        float c31 = 0.5f * (y21 - y_11) + 1.5f * (y01 - y11);
        float output0 = ((c30 * x0 + c20) * x0 + c10) * x0 + c00;
        float output1 = ((c31 * x1 + c21) * x1 + c11) * x1 + c01;
        buffer[buffer0 + counter] = spl0 + output0 * feedback;
        buffer[buffer1 + counter] = spl1 + output1 * feedback;
        spl0 = spl0 * (1 - mix) + output0 * mix;
        spl1 = spl1 * (1 - mix) + output1 * mix;
        counter += 1;
        if (counter >= MAX_WG_DELAY) counter = 0;
        if (lfo == triangle)
        {
            if (dir0 != 0) tdelay0 += trate; else tdelay0 -= trate;
            if (dir1 != 0) tdelay1 += trate; else tdelay1 -= trate;
            if (tdelay0 >= delay + depth) dir0 = 0;
            if (tdelay1 >= delay + depth) dir1 = 0;
            if (tdelay0 <= delay - depth) dir0 = 1;
            if (tdelay1 <= delay - depth) dir1 = 1;
        }
        else
        {
            tdelay0 = delay + (delay - 0.1f) * (float)Math.sin(tpos);
            tdelay1 = delay + (delay - 0.1f) * (float)Math.sin(tpos + offset);
            tpos += trate;
            if (tpos > twopi) tpos = 0;
        }
        sdelay0 = tdelay0 / 1000 * SampleRate;
        sdelay1 = tdelay1 / 1000 * SampleRate;
        
        return spl0;
//        makeAudio[index] = (short)spl0; 
//        makeAudio[index+1] = (short)spl1;
    }
    
    
}
