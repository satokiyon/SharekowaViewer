package jp.sato.kiyo.android.sahrekowaviewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.WebView;

public class SharekowaStyle {
	WebView view;
	private Context con;
	private SharedPreferences shpref;
	
	SharekowaStyle(Context con){
		this.con = con;
		shpref = PreferenceManager.getDefaultSharedPreferences(this.con);

	}

	void setPartListColor(WebView view){
		if(shpref.getBoolean(con.getString(R.string.black_style_key), false)){
		//set color of "li" tag
		view.loadUrl("javascript:document.bgColor='#000000';" +
				                "document.vlinkColor='#a03030';" +
				                "document:fgColor='#999'");
		view.loadUrl("javascript:(function(){ var lilist = document.getElementsByTagName('li');" +
				                             "for(i=0;i<lilist.length;i++){" +
				                                "lilist[i].style.backgroundColor='#222';" +
				                                //"lilist[i].font.style.color='white'" +
				                             "}" +
				                            "}() )");
		}

	}

	void setEpisodeListColor(WebView view){
		if(shpref.getBoolean(con.getString(R.string.black_style_key), false)){
		view.loadUrl("javascript:document.bgColor='#000000';" +
	            "document.vlinkColor='#a03030';" +
	            "document:fgColor='#999'");

		view.loadUrl("javascript:(function(){ var lilist = document.getElementsByTagName('td');" +
	            "for(i=0;i<lilist.length;i++){" +
	               "lilist[i].style.backgroundColor='#222';" +
	               //"lilist[i].font.style.color='white'" +
	            "}" +
	           "}() )");
		}
		
		//for part59,etc.
		view.loadUrl("javascript:(function(){ var lilist = document.getElementsByTagName('li');" +
	            "for(i=0;i<lilist.length;i++){" +
	               "lilist[i].style.backgroundColor='#222';" +
	               "lilist[i].style.color='#999'" +
	            "}" +
	           "}() )");

	}

	void setEpisodeColor(WebView view){
		if(shpref.getBoolean(con.getString(R.string.black_style_key), false)){
		view.loadUrl("javascript:document.bgColor='#000000';" +
	            "document.vlinkColor='#a03030';" +
	            "document:fgColor='#999';"
	            );

		view.loadUrl("javascript:(function(){ var lilist = document.getElementsByTagName('table');" +
	            "for(i=0;i<lilist.length;i++){" +
	               "lilist[i].style.backgroundColor='#222';" +
	               "lilist[i].style.color='#999'" +
	            "}" +
	           "}() )");

		view.loadUrl("javascript:(function(){ var lilist = document.getElementsByTagName('td');" +
	            "for(i=0;i<lilist.length;i++){" +
	               "lilist[i].style.backgroundColor='#222';" +
	               "lilist[i].style.color='#999'" +
	            "}" +
	           "}() )");
		
		//シリーズ関連モノ→地方伝説→リョウメンスクナなど用
		view.loadUrl("javascript:(function(){ var lilist = document.getElementsByTagName('dd');" +
	            "for(i=0;i<lilist.length;i++){" +
	               "lilist[i].style.backgroundColor='#222';" +
	               "lilist[i].style.color='#999'" +
	            "}" +
	           "}() )");

		//part101などの形式用
		view.loadUrl("javascript:(function(){ var lilist = document.getElementsByClassName('MsoNormal');" +
	            "for(i=0;i<lilist.length;i++){" +
	               "lilist[i].style.backgroundColor='#222';" +
	               "lilist[i].style.color='#999'" +
	            "}" +
	           "}() )");
		

		
		}

	}

	void clearFontSize(WebView view){
		//fontタグが設定されていれば、フォントサイズをすべて削除
		view.loadUrl("javascript:(function(){ var flist = document.getElementsByTagName('font');" +
				                             "for(i=0;i<flist.length;i++){" +
				                                "flist[i].size='';" +
				                             "}" +
				                            "}() )");
	}

	void resizeWidthOfTag(WebView view, String tag, String width){
		view.loadUrl("javascript:(function(){ var llist = document.getElementsByTagName('"+ tag + "');" +
	            "for(i=0;i<llist.length;i++){" +
	               "llist[i].style.width='"+ width + "';" +
	            "}" +
	           "}() )");
	}

	void resizeWidthOfClass(WebView view, String classname, String width){
		view.loadUrl("javascript:(function(){ var llist = document.getElementsByClassName('" + classname + "');" +
	            "for(i=0;i<llist.length;i++){" +
	               "llist[i].style.width='" + width + "';" +
	            "}" +
	           "}() )");
	}

	void resizeFontSizeOfTag(WebView view, String tag, String fontsize){
		view.loadUrl("javascript:(function(){ var llist = document.getElementsByTagName('" + tag + "');" +
	            "for(i=0;i<llist.length;i++){" +
	               "llist[i].style.fontSize='" + fontsize + "';" +
	            "}" +
	           "}() )");
	}

	void resizeFontSizeOfClass(WebView view, String classname, String fontsize){
		view.loadUrl("javascript:(function(){ var llist = document.getElementsByClassName('" + classname + "');" +
	            "for(i=0;i<llist.length;i++){" +
	               "llist[i].style.fontSize='" + fontsize + "';" +
	            "}" +
	           "}() )");
	}

	public void resizeFontSize(WebView view, String fontsize){
	//フォントサイズを変更

		clearFontSize(view);

	//tableタグがあれば幅を100%に
		resizeFontSizeOfTag(view, "table", fontsize);
		resizeWidthOfTag(view, "table", "100%");

	//TDタグがあれば、すべてフォントサイズをセットして幅を100%に
		resizeWidthOfTag(view, "TD", fontsize);

	//全リストのpart59, 60の形式用
		resizeFontSizeOfTag(view, "li", fontsize);

	//hpb-cnt-tb3クラスのテーブルの幅を100%に
		resizeWidthOfClass(view, "hpb-cnt-tb3", "100%");

	//part80などの形式用
	//フォントサイズをセット
		resizeFontSizeOfClass(view, "hpb-cnt-tb-cell4", fontsize);
		resizeWidthOfClass(view, "hpb-cnt-tb-cell4", "100%");

		resizeFontSizeOfClass(view, "hpb-cnt-tb-cell3", fontsize);
		resizeWidthOfClass(view, "hpb-cnt-tb-cell3", "100%");

	//hpb-colm1
		resizeFontSizeOfClass(view, "hpb-colm1", fontsize);
		resizeWidthOfClass(view, "hpb-colm1", "100%");

	}


}
