package jp.sato.kiyo.android.sahrekowaviewer;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

public class SharekowaWebViewClient extends WebViewClient {

	private ProgressDialog progressDialog = null;
	private Context con;
	private SharekowaViwerActivity act;
	private SharedPreferences shpref;
	private SharekowaStyle style;
	private SharekowaJavascriptInterface jsi;
	SharekowaWebViewClient(SharekowaViwerActivity act){
		super();
		this.act = act;
		this.con = act.getApplicationContext();
	    shpref = PreferenceManager.getDefaultSharedPreferences(this.con);
	    style = new SharekowaStyle(this.con);
	}

	//現在どのカテゴリにいるか
	public int category=R.string.blank;

	//現在のpart
	public String currentPart ="";

	private void showProgress() {
			act.setProgressBarIndeterminateVisibility(true);
	}

	public void setJsi(SharekowaJavascriptInterface jsi){
		this.jsi = jsi;
	}

	private void deleteProgress() {
		//画面遷移時のjavascript実行によるバタつきを抑止するためにUIスレッドをsleepする
		//WebViewのjavascript実行完了を待ち合わせる方法が分かればそれに修正したい。
		int sleeptime = 0;
		String value = shpref.getString(con.getString(R.string.loadWaitTime_key), "300");
		try {
			if (value.length() == 0) {
				value = "300";
			}
			sleeptime = Integer.valueOf(value);
			Thread.sleep(sleeptime);
			//Log.v("Shareowa", "sleep " + sleeptime + "ms");
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			Log.v("Sharekowa", "sleep time = " + value);
			e.printStackTrace();
		}
		act.setProgressBarIndeterminateVisibility(false);
	}

	// リンクをクリックしたときに既存のブラウザではなく、このアプリで
	// 表示するためにオーバーライド
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		//外部サイトへのリンクをクリックしても移動しないように制限
		if(url.startsWith("http://syarecowa.moo.jp/") || url.startsWith("http://dokosyare.akazunoma.com/")){
			view.loadUrl(url);
			return true;
		}
		else{
			view.reload();
			return true;
		}
	}

	//ページ読み込み開始時の動作
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
			showProgress();
	}

	//ページを読み終わったときに実行する処理
	@Override
	public void onPageFinished(WebView view, String url) {
		Log.v("Sharekowa", "pageFinished");


		//設定値からフォントサイズを取得
		String fontsize  = shpref.getString(con.getString(R.string.menu_fontsize_key), "24");
		if(fontsize.equals("") ){ fontsize = "24"; }
		fontsize += "pt"; //単位はpt

		if (isAllPartList(url)) {

			// トップページの各partが並んでいるメニューなら、その一覧を
			// 取得
			readPartListFromHtml(view);

			//色やサイズを変える
			  style.setPartListColor(view);
			style.resizeFontSizeOfClass(view, "menu-html", fontsize);

			//現在のカテゴリをall_listにセット
			category = R.string.all_list;
			act.setAppNameAndTitle(con.getString(R.string.all_list_title));
			//partを初期化
			currentPart = "";
			view.clearHistory();

		}
		else if(isSeriesList(url)){
			//view.loadUrl("javascript:document.body.style.fontSize='32pt'");
			//シリーズ・関連モノのトップメニュー
			readPartListFromHtml(view);
			//現在のカテゴリをseriesにセット
			category = R.string.series;
			act.setAppNameAndTitle(con.getString(R.string.series_title));
			currentPart = "";
			view.clearHistory();
			//Log.v("Sharekowa", "Series List TOP");

			//色やサイズを変える
					style.setEpisodeListColor(view);
			//hpb-cnt-tb3クラスのテーブルの幅を100%に
			style.resizeWidthOfClass(view, "hpb-cnt-tb3", "100%");
			//「ヒサルキ系」のフォントがなぜか-1されているので、その指定を解除。
			style.clearFontSize(view);
			//フォントを設定
			style.resizeFontSizeOfClass(view, "hpb-cnt-tb-cell4", fontsize);

		}
		//心霊ちょっといい話のトップなら
		else if(isGoodGhostStoryTop(url)){
			style.resizeFontSizeOfTag(view, "A", fontsize);
			category = R.string.good_ghost_story;
			act.setAppNameAndTitle(con.getString(R.string.good_ghost_story_title));
			currentPart = "";
			view.clearHistory();
			style.setPartListColor(view);

			readPartListFromHtml(view);

			}
		//怖い話をもっと怖くするのトップなら
		else if(isMoreScaryTop(url)){
			style.resizeFontSizeOfTag(view, "A", fontsize);
			category = R.string.more_scary;
			act.setAppNameAndTitle(con.getString(R.string.more_scary_title));
			currentPart = "";
			view.clearHistory();
			style.setPartListColor(view);
			readPartListFromHtml(view);
		}
		//ガイシュツのトップなら
		else if(isAlreadyTop(url)){
			style.resizeFontSize(view, fontsize);
			style.resizeFontSizeOfTag(view, "A", fontsize);
			category = R.string.already;
			act.setAppNameAndTitle(con.getString(R.string.already_title));
			currentPart = "";
			view.clearHistory();
			style.setPartListColor(view);
			//1階層少なく、トップページ→エピソードなのでエピソードリストを読みこむ
			readEpisodeListFromHtml(view);
		}
		//時空の歪みトップなら
		else if(isDistortionOfSpaceTimeTop(url)){
			style.resizeFontSize(view, fontsize);
			category = R.string.distortion_of_space_time;
			act.setAppNameAndTitle(con.getString(R.string.distortion_of_space_time_title));
			currentPart = "";
			view.clearHistory();
			style.setPartListColor(view);
			readPartListFromHtml(view);
		}
		//東京伝説トップなら
		else if(isLegendOfTokyoTop(url)){
			style.resizeFontSize(view, fontsize);
			category = R.string.legend_tokyo;
			act.setAppNameAndTitle(con.getString(R.string.legend_tokyo_title));
			currentPart = "";
			view.clearHistory();
			style.setPartListColor(view);
			readPartListFromHtml(view);
		}
		//怖い話をさらに怖くするトップなら
		else if(isMoreScaryTop(url)){
			style.resizeFontSize(view, fontsize);
			category = R.string.more_scary;
			act.setAppNameAndTitle(con.getString(R.string.more_scary_title));
			currentPart = "";
			view.clearHistory();
			style.setPartListColor(view);
			readPartListFromHtml(view);
		}
		//怖い話掲示板まとめのトップなら
		else if(isBbsSummaryTop(url)){
			style.resizeFontSize(view, fontsize);
			category = R.string.bbs_summary;
			act.setAppNameAndTitle(con.getString(R.string.bbs_summary_title));
			currentPart = "";
			view.clearHistory();
			style.setPartListColor(view);
			readPartListFromHtml(view);
		}
		//怖い話投稿掲示板なら
		else if(isBbsTop(url)){
			category = R.string.bbs;
			act.setAppNameAndTitle(con.getString(R.string.bbs_title));
			currentPart = "";
			jsi.partIndex = null;
			jsi.episodeIndex = null;
			view.clearHistory();
		}
		//どこでも洒落コワなら
		else if(isMobileTop(url)){
			category = R.string.mobile;
			act.setAppNameAndTitle(con.getString(R.string.mobile_title));
			currentPart = "";
			jsi.partIndex = null;
			jsi.episodeIndex = null;
			view.clearHistory();
		}
		else if(isRankingTop(url)){
			category = R.string.blank;
			act.setAppNameAndTitle(con.getString(R.string.blank_title));
			currentPart = "";
			jsi.partIndex = null;
			jsi.episodeIndex = null;
			view.clearHistory();
		}
		else if( !isMobile(url) && (isEpisodeList(url) || isSeriesEpisodeList(url)) ){
			//episode一覧なら、そのエピソード一覧を取得
				style.resizeFontSize(view, fontsize);

				//ランキングページでなければ色を変更する
				if(!(isRankingTop(url))){
					style.setEpisodeListColor(view);
				}

				currentPart = url;

				readEpisodeListFromHtml(view);

				//タイトル文字列の設定
				if (jsi.partIndex != null) {
					for (int i = 0; i < jsi.partIndex.length; i++) {
						if (jsi.partIndex[i][0].equals(currentPart)) {
							act.setTitleString(jsi.partIndex[i][1]);
						}
					}
				}
		}

		//エピソードのページならページの先頭を表示し、横幅を画面幅にあわせる
		if(!isMobile(url) && isEpisodePage(url)){
			//レイアウトは ScrollView.LinearLayout.LinearLayout.WebViewなので、getParent()で3つ上のScrollViewを取得して先頭にスクロールする
			((ScrollView) view.getParent().getParent().getParent()).scrollTo(10, 0);

			//黒スタイルが設定されていれば色を変える
			if(!(isRankingTop(url))){
				style.setEpisodeColor(view);
			}
			//table要素のサイズを書き換えてサイズを調整する
			style.resizeWidthOfTag(view, "table", "95%");
			style.resizeWidthOfTag(view, "td", "95%");

			//タイトル文字列の設定
			if (jsi.episodeIndex != null) {
				for (int i = 0; i < jsi.episodeIndex.length; i++) {
					if (jsi.episodeIndex[i][0].equals(url)) {
						act.setTitleString(jsi.episodeIndex[i][1]);
					}
				}
			}
		}

		//ダイアログを削除
		deleteProgress();
	}


	//エピソード一覧を表示しているページなら
	public boolean isEpisodeList(String url){
		String[] urlStr = url.split("/");
		//Log.v("Sharekowa", "length="+ urlStr.length);
		//Log.v("Sharekowa", "pagename=" + urlStr[urlStr.length - 1]);
		if((urlStr.length >= 4) && (
			(urlStr[urlStr.length - 1].contains("menu")) ||
			(urlStr[urlStr.length - 1].equals("index.html")) ||
			(urlStr[urlStr.length - 1].equals("top.html")) ||
			(urlStr[urlStr.length - 1].startsWith("newpage")) ||
			(urlStr[urlStr.length - 1].contains(urlStr[urlStr.length - 2]+".html"))
			)
		 ){
			//URLのパスがトップページより深くて、ファイル名がmenuで始まるかindex.htmlなら一覧ページと判定
			return true;
		}
		else {
			return false;
		}
	}

	//part一覧を表示しているページなら
	public boolean isAllPartList(String url){
//		if(url.equals("http://syarecowa.moo.jp/menu.html")){
		if( url.equals(con.getString(R.string.all_list)) ){
			return true;
		}
		else{
			return false;
		}
	}


	public boolean isEpisodePage(String url){
		String[] urlStr = url.split("/");
		if(urlStr.length >= 4){
			if(!isEpisodeList(url)){
				//エピソード一覧でないなら、エピソードののページと判定する。
				return true;
			}
			else { return false; }
		}
		else {
			return false;
		}
	}

	//シリーズ・関連モノのトップメニューかを判定
	public boolean isSeriesList(String url){
		if(url.equals(con.getString(R.string.series))){
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isSeriesEpisodeList(String url){
		String[] urlStr = url.split("/");
		//Log.v("Sharekowa", "length="+ urlStr.length);
		//Log.v("Sharekowa", "pagename=" + urlStr[3]);
		if((urlStr.length >= 4) && (urlStr[3].equals("kanren")) ){
			return true;
		}
		else if( (urlStr.length >= 4) && (urlStr[3].equals("jikuu")) && (urlStr[urlStr.length-1].contains("menu")) ){
			//時空のおっさん
			return true;
		}
		else if( (urlStr.length >= 4) && (urlStr[3].equals("sisyou")) && (urlStr[urlStr.length-1].contains("menu")) ){
			//師匠シリーズ
			return true;
		}
		else {
			return false;
		}
	}

	//心霊ちょっといい話のトップページかを判定
	public boolean isGoodGhostStoryTop(String url){
		if(url.equals(con.getString(R.string.good_ghost_story))){
			return true;
		}
		else {
		    return false;
		}
	}

	//怖い話をもっと怖くのトップページかを判定
	public boolean isMoreScaryTop(String url){
		if(url.equals(con.getString(R.string.more_scary))){
			return true;
		}
		else {
			return false;
		}
	}
	//ガイシュツのトップページかを判定
	public boolean isAlreadyTop(String url){
		if(url.equals(con.getString(R.string.already))){
			return true;
		}
		else {
			return false;
		}
	}
	//時空のゆがみトップページかを判定
	public boolean isDistortionOfSpaceTimeTop(String url){
		if(url.equals(con.getString(R.string.distortion_of_space_time))){
			return true;
		}
		else {
			return false;
		}
	}
	//東京伝説のトップページかを判定
	public boolean isLegendOfTokyoTop(String url){
		if(url.equals(con.getString(R.string.legend_tokyo))){
			return true;
		}
		else {
			return false;
		}
	}

	//怖い話掲示板まとめのトップページかを判定
	public boolean isBbsSummaryTop(String url){
		if(url.equals(con.getString(R.string.bbs_summary))){
			return true;
		}
		else {
			return false;
		}
	}
	//怖い話高校掲示板のトップページかを判定
	public boolean isBbsTop(String url){
		if(url.equals(con.getString(R.string.bbs))){
			return true;
		}
		else {
			return false;
		}
	}

	//どこでもモバイルのトップページかを判定
	public boolean isMobileTop(String url){
		if(url.equals(con.getString(R.string.mobile))){
			return true;
		}
		else {
			return false;
		}
	}
	//
	public boolean isMobile(String url){
		if(url.startsWith("http://dokosyare.akazunoma.com/")){
			return true;
		}
		else{
			return false;
		}

	}

	//ランキング関連のページトップかを判定
	public boolean isRankingTop(String url){
		if(url.equals(con.getString(R.string.matchless)) ||
		   url.equals(con.getString(R.string.hall_of_frame_polling_space)) ||
		   url.equals(con.getString(R.string.polling_place)) ||
		   url.equals(con.getString(R.string.pre_poling_place)) ||
		   url.equals(con.getString(R.string.good_ghost_story_in_hall_of_frame)) ||
		   url.equals(con.getString(R.string.good_gohst_story_polling_place)) ||
		   url.equals(con.getString(R.string.distortion_of_space_time_polling_place))
		){
			return true;
		}
		else {
			return false;
		}

	}


	//part一覧を取得
	private void readPartListFromHtml(WebView view){
    //なぜか2つに分けないと画面表示されなくなるので、hrefとtextを分ける
		String urllist = "javascript:"
	           + "var urllist = new Array(document.links.length);"
	           + "var i;"
	           + "for (i = 0; i < document.links.length; i++) {"
	               + "urllist[i] = document.links[i].href;"
	            + "}"
	            + "skjs.getPartListUrl(urllist);";

	    		String textlist = "javascript:"
	 	           + "var textlist = new Array(document.links.length); "
	 	           + "var i;"
	 	           + "for (i = 0; i < document.links.length; i++) {"
	 	               + "textlist[i] = document.links[i].text;"
	 	            + "}"
	 	            + "skjs.getPartListText(textlist);";

		Log.v("Sharekowa", "readPartListFromHtml");
		//view.getSettings().setJavaScriptEnabled(true);
		view.loadUrl(urllist);
		view.loadUrl(textlist);


	}

	//表示されたpartのエピソード一覧を取得
	private void readEpisodeListFromHtml(WebView view) {

		String urllist = "javascript:"
           + "var urllist = new Array(document.links.length);"
           + "var i;"
           + "for (i = 0; i < document.links.length; i++) {"
               + "urllist[i] = document.links[i].href;"
            + "}"
           + "skjs.getEpisodeListUrl(urllist);";

		String textlist = "javascript:"
	           + "var textlist = new Array(document.links.length); "
	           + "var i;"
	           + "for (i = 0; i < document.links.length; i++) {"
	               + "textlist[i] = document.links[i].text;"
	            + "}"
	           + "skjs.getEpisodeListText(textlist);";

		Log.v("Sharekowa", "readEpisodeListFromHtml");
		//view.getSettings().setJavaScriptEnabled(true);
		view.loadUrl(urllist);
		view.loadUrl(textlist);
		//view.getSettings().setJavaScriptEnabled(false);
	}

}
