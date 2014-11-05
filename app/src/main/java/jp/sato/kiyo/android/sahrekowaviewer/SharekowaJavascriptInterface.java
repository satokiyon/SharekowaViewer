package jp.sato.kiyo.android.sahrekowaviewer;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import jp.sato.kiyo.android.sahrekowaviewer.*;


public class SharekowaJavascriptInterface {
	private Context con;
	SharekowaViwerActivity skv;

	public String[][] partIndex; //partXへのリンク一覧
	public String[][] episodeIndex; //各partのエピソードへのリンク一覧

	public SharekowaJavascriptInterface(SharekowaViwerActivity skv){
		con = skv.getApplicationContext();
		this.skv = skv;

	}


	//次のpartなりエピソードなりを返す
	public String getNext(int category, String currentPart, String url){
		Log.v("Sharekowa", "getNext:category="+category + " currentPart="+currentPart+" url="+url);

		String next = "javascript:alert(\"次が見つかりません\")";

		if(currentPart == null || url == null){ return next;}

		if(isAccessFromCategoryTop(category)){
			//全リストページなら何もしない
			if(isCategoryTop(url)){
				Toast.makeText(con, "リストからpartを選択してください", Toast.LENGTH_LONG).show();
				next = url;
			}
			//あるpartのエピソード一覧ページなら次のpartのエピソード一覧へジャンプ
			//else if((urlStr.length >= 4) && (urlStr[urlStr.length - 1].startsWith("menu"))) {
			else if(currentPart.equals(url)){
				next = searchNextPart(url);
				skv.wbc.currentPart = next; //直接書き換える以外の方法を後で考える
			}
			//エピソードのページなら次のエピソードへ
			else{
				next = searchNextEpisode(currentPart, url);
			}
		}
		return next;
	}

	public String getPrev(int category, String currentPart, String url){
		//Log.v("Sharekowa", "getPrev:category="+category + " currentPart="+currentPart+" url="+url);

		String prev = "javascript:alert(\"前が見つかりません.\")";

		if(currentPart == null || url == null){ return prev;}

		if(isAccessFromCategoryTop(category)){
			//全リストページなら何もしない
			if(isCategoryTop(url)){
				Toast.makeText(con, "リストからpartを選択してください", Toast.LENGTH_SHORT).show();
				prev = url;
			}
			//あるpartのエピソード一覧ページなら前のpartのエピソード一覧へジャンプ
			//else if((urlStr.length >= 4) && (urlStr[urlStr.length - 1].startsWith("menu"))) {
			else if(currentPart.equals(url)){
				prev = searchPrevPart(url);
				skv.wbc.currentPart = prev;
			}
			//エピソードのページなら次のエピソードへ
			else{
				prev = searchPrevEpisode(currentPart, url);
			}
		}
		return prev;
	}

	//次のpartを返す
	private String searchNextPart(String url){
		if(null  == partIndex){ return url;};

		//i = ArrayUtils.indexOf(partIndex, url);
		for(int i=0;i<partIndex.length;i++){
			if(partIndex[i][0].equals(url)){
				Log.v("Sharekowa", "partlength = " + partIndex.length);
				if(isLastPart(i)) {
					Toast.makeText(con, "次はありません。これが最後のメニューです", Toast.LENGTH_SHORT).show();
					return url;
				}
				else {
					return partIndex[i+1][0];
				}
			}
		}// end of for loop
		Toast.makeText(con, "次が見つかりません", Toast.LENGTH_SHORT).show();
		return url;
	}



	//次のエピソードを返す
	private String searchNextEpisode(String currentPart, String url){
		if(null == episodeIndex){ return url; }

		//i = ArrayUtils.indexOf(partIndex, url);
		for(int i=0;i<episodeIndex.length;i++){
			if(episodeIndex[i][0].equals(url)){
				if(isLastEpisode(i)){
					Toast.makeText(con, "次のメニューに進みます", Toast.LENGTH_SHORT).show();
					return searchNextPart(currentPart);
				}
				else {
					return episodeIndex[i+1][0];
				}
			}
		}// end of for loop
		Toast.makeText(con, "次が見つかりません", Toast.LENGTH_SHORT).show();
		return url;
	}

	//前のpartを返す
	private String searchPrevPart(String url){
		if(null  == partIndex){ return url;};

		//i = ArrayUtils.indexOf(partIndex, url);
		for(int i=0;i<partIndex.length;i++){
			if(partIndex[i][0].equals(url)){
				if(is1stPart(i)){
					Toast.makeText(con, "最初のメニューです", Toast.LENGTH_SHORT).show();
					return url;
				}
				else {
					return partIndex[i-1][0];
				}
			}
		}// end of for loop
		Toast.makeText(con, "前が見つかりません", Toast.LENGTH_SHORT).show();
		return url;
	}

	//前のエピソードを返す
	private String searchPrevEpisode(String currentPart, String url){
		if(null  == episodeIndex){ return url;};

		//i = ArrayUtils.indexOf(partIndex, url);
		for(int i=0;i<episodeIndex.length;i++){
			if(episodeIndex[i][0].equals(url)){
				if(i == 0){
					Toast.makeText(con, "前のメニューに戻ります", Toast.LENGTH_SHORT).show();
					return searchPrevPart(currentPart);
				}
				else {
					return episodeIndex[i-1][0];
				}
			}
		}// end of for loop
		Toast.makeText(con, "前が見つかりません", Toast.LENGTH_SHORT).show();
		return url;
	}


	//partの最後を判定
	private boolean isLastPart(int i){
		if((partIndex.length - 1) == i){
			return true;
		}
		//次のリンクがサイト外リンクだったら、ここで終わりと判定
		if(!(partIndex[i+1][0].contains("syarecowa.moo.jp"))){
				return true;
		}
		//次がトップメニューと同じだったら、ここで終わりと判定
		if(partIndex[i+1][0].equals(con.getString(R.string.all_list))){
			return true;
		}
		return false;
	}

	private boolean is1stPart(int i){
		if(0 == i){
			return true;
		}
		//前のリンクがサイト外リンクだったら、ここが最初と判定
		if(!(partIndex[i-1][0].contains("syarecowa.moo.jp"))){
				return true;
		}
		//前がトップメニューと同じだったら、ここが最初と判定
		else if(partIndex[i-1][0].equals(con.getString(R.string.all_list))){
			return true;
		}
		return false;
	}

	//エピソードの最後を判定
	private boolean isLastEpisode(int i){
		if((episodeIndex.length - 1) == i){
			return true;
		}
		//次のリンクがサイト外リンクだったら、ここで終わりと判定
		if(!(episodeIndex[i+1][0].contains("syarecowa.moo.jp"))){
			return true;
		}

		return false;

	}


	public void getPartListUrl(String[] urllist){
		Log.v("Sharekowa", "getPartListUrl");
		String[][] plist = new String[urllist.length][2];
		for(int i=0; i < urllist.length; i++){
			plist[i][0] = urllist[i];
		}
		partIndex = plist.clone();
	}

	public void getPartListText(String[] textlist){
		Log.v("Sharekowa", "getPartListText");
		for(int i=0; i < textlist.length; i++){
			partIndex[i][1] = textlist[i];
		}
	}


	public void getEpisodeListUrl(String[] urllist){
		Log.v("Sharekowa", "getPartListUrl");
		String[][] elist = new String[urllist.length][2];
		for(int i=0; i < urllist.length; i++){
			elist[i][0] = urllist[i];
		}
		episodeIndex = elist.clone();
	}

	public void getEpisodeListText(String[] textlist){
		Log.v("Sharekowa", "getPartListText");
		for(int i=0; i < textlist.length; i++){
			episodeIndex[i][1] = textlist[i];
		}
	}


	public boolean isAccessFromCategoryTop(int category){
		switch (category){
		case R.string.all_list:
		case R.string.series:
		case R.string.good_ghost_story:
		case R.string.distortion_of_space_time:
		case R.string.legend_tokyo:
		case R.string.already:
		case R.string.more_scary:
		case R.string.bbs_summary:
			return true;
		default:
			return false;
		}
	}

	private boolean isCategoryTop(String url){
		if(url.equals(con.getString(R.string.all_list)) ||
	      url.equals(con.getString(R.string.series)) ||
	      url.equals(con.getString(R.string.good_ghost_story)) ||
	      url.equals(con.getString(R.string.distortion_of_space_time)) ||
	      url.equals(con.getString(R.string.legend_tokyo)) ||
	      url.equals(con.getString(R.string.already)) ||
	      url.equals(con.getString(R.string.more_scary)) ||
	      url.equals(con.getString(R.string.bbs_summary)) ||
	      url.equals(con.getString(R.string.bbs))
	      ){
			return true;
		}
		else{
			return false;
		}
	}

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

	public String getBackURL(int category, String currentPart, String url) {
		String backURL = null;

		if(currentPart == null || url == null){ return backURL;}

		// カテゴリのトップページなら戻るところが無いのでnull
		if (isCategoryTop(url) || isRankingTop(url)) {
			Log.v("Sharekowa", "goback to null");
			backURL = null;
		}
		// エピソード一覧ページならその上位のカテゴリトップページを返す
		else if (currentPart != "" && currentPart.equals(url)) {
			//Log.v("Sharekowa", "goback to category top = "con.getString(category));
			backURL = con.getString(category);
		} else {
			if (currentPart == "") {
				//現在のpart情報が無ければカテゴリトップを返す。ガイシュツのエピソード用
				backURL = con.getString(category);
			} else {
				// エピソードなら、上位のエピソード一覧ページを返す
				//Log.v("Sharekowa", "goback to currentPart = " + currentPart);
				backURL = currentPart;
			}
		}
		Log.v("Sharekowa", "backURL = " + backURL);

		return backURL;
	}

}
