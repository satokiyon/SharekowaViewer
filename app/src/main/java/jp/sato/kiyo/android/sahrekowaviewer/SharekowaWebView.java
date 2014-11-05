package jp.sato.kiyo.android.sahrekowaviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

public class SharekowaWebView extends WebView {

	public int waitDrawCount = 0;
	private boolean lockCanvas = false;

	private Canvas offCanvas;
	private Bitmap offscreen;

	//public Handler handler= new Handler();

	/*public Runnable callback = new Runnable(){
		public void run(){
		unlockCanvasAndDraw();
		}
	};*/


	public SharekowaWebView(Context context) {
		super(context);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public SharekowaWebView(Context context, AttributeSet attrs){
		super(context,  attrs);
	}

	public SharekowaWebView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas){


		if (waitDrawCount > 0) {
			if (offscreen == null) {
				offscreen = Bitmap.createBitmap(canvas.getWidth(), canvas
						.getHeight(), Bitmap.Config.ARGB_8888);
				offCanvas = new Canvas(offscreen);
				// offCanvas.setBitmap(offscreen);
			}
			super.onDraw(offCanvas);
			waitDrawCount -= 1;
			Log.v("Sharekowa", "offscreen: " + waitDrawCount);

		} else {
			if (offscreen != null) {
				canvas.drawBitmap(offscreen, 0, 0, null);
			}
			super.onDraw(canvas);

			offscreen = null;
			offCanvas = null;

			Log.v("Sharekowa", "onscreen");

		}

/*		//super.onDraw(canvas);
        if (offscreen == null) {
            offscreen = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            offCanvas = new Canvas(offscreen);
            //offCanvas.setBitmap(offscreen);
        }
        if(count > 0){
        	count -= 1;
        	super.onDraw(offCanvas);
        }
        else {
        	canvas.drawBitmap(offscreen, 0, 0, null);
        	super.onDraw(canvas);
        	offscreen = null;
        	offCanvas = null;
        }*/
	//Log.v("Sharekowa", "onDraw, count= " + count);
	}


	public void lockCanvas(){
		//Log.v("Sharekowa", "locked");
		//lockCanvas = true;
	}

	public void unlockCanvasAndDraw(){
		//Log.v("Sharekowa", "un-locked");
		//lockCanvas = false;
		//invalidate();
	}

	public void incrementWaitDrawCount(int count){
		waitDrawCount += count;
	}
	public void resetWaitDrawCount(){
		waitDrawCount = 0;
	}


}
