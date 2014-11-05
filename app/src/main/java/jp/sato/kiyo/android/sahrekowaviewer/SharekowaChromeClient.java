package jp.sato.kiyo.android.sahrekowaviewer;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class SharekowaChromeClient extends WebChromeClient {
	Activity act;

	SharekowaChromeClient(Activity act){
		super();
		this.act = act;
	}
	
  public void onProgressChanged(WebView view, int progress) {
	     // Activities and WebViews measure progress with different scales.
	     // The progress meter will automatically disappear when we reach 100%
	     act.setProgress(progress * 1000);
	   }

}
