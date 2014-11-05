package jp.sato.kiyo.android.sahrekowaviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import jp.sato.kiyo.android.sahrekowaviewer.SharekowaBookmarkListDialog.onBookmarkListDialogListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

public class SharekowaViwerActivity extends Activity implements SharekowaBookmarkListDialog.onBookmarkListDialogListener{
	/** Called when the activity is first created. */
	WebView webView;
	ScrollView scrollview;
	SharekowaJavascriptInterface jsi;
	SharekowaWebViewClient wbc;
	private SharedPreferences shpref;
	private boolean remove_bookmark = false;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);

			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);


			setContentView(R.layout.main);

			//デフォルトのプリファレンスを用意
			shpref = PreferenceManager.getDefaultSharedPreferences(this);

			webView = (WebView) findViewById(R.id.webview);

			//ブラウザの挙動をカスタマイズするためのクラスをロード
			webView.setWebChromeClient(new SharekowaChromeClient(this));
			wbc = new SharekowaWebViewClient(this);
			webView.setWebViewClient(wbc);

			//ブラウザの設定を変えるためのクラスをロード
			WebSettings wsettings = webView.getSettings();
			wsettings.setBuiltInZoomControls(true);
			wsettings.setJavaScriptEnabled(true);

			//javascripへのインターフェースをロード
			jsi = new SharekowaJavascriptInterface(this);
			webView.addJavascriptInterface(jsi, "skjs");
			wbc.setJsi(jsi);

			//黒スタイルが設定されていればWebViewの背景を黒に
			if(shpref.getBoolean(getString(R.string.black_style_key), false)){
				webView.setBackgroundColor(Color.BLACK);
			}
			else{
				webView.setBackgroundColor(Color.WHITE);
			}


			//画面の縦横が変わるなどconfigurationが変わった場合、onDestoryで現在
			//表示中のページをtempファイルに保存しておくので、存在すればそれを
			//ロードして画面を復旧させる
			File file = this.getFileStreamPath("temp");
			boolean isExists = file.exists();
			if(isExists){
				loadBookmark("temp");
			}
			else{
			// tmpファイルがない場合(前回意図して終了した場合)は全リストを表示
			wbc.category = R.string.all_list;
			setAppNameAndTitle("全リスト");
			webView.loadUrl(getString(R.string.all_list));
			//webView.loadUrl("http://www.google.co.jp");
			}

			setButton();


		} catch (Exception e) {
			Log.e("BrowserActivity", "error", e);
		}

	}

	//画面下の次へ、前へ、ランダムボタンの設定
	public void setButton() {
		Button btn_next = (Button) findViewById(R.id.button_navi_next);
		Button btn_prev = (Button) findViewById(R.id.button_navi_prev);
		Button btn_random = (Button) findViewById(R.id.button_navi_random);

		//次へボタンを押下したとき、コンテキストごとの「次」へ移動する
		btn_next.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				webView.loadUrl(jsi.getNext(wbc.category, wbc.currentPart, webView.getUrl()));
			}
		});

		//前へボタンを押したとき
		btn_prev.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				webView.loadUrl(jsi.getPrev(wbc.category, wbc.currentPart, webView.getUrl()));
			}
		});

		//
		btn_random.setEnabled(false);

	}


	//Backボタンを押したときに戻る
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (webView == null) {
			return super.onKeyDown(keyCode, event);
		}

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// カテゴリ配下のページでなければブラウザの履歴を戻る
			if (false == jsi.isAccessFromCategoryTop(wbc.category)) {
				if (webView.canGoBack()) {
					webView.goBack();
					return true;
				} else {
					return showExitDialog();
					//return super.onKeyDown(keyCode, event);
				}
			} else {
				// カテゴリ配下のページなら上位ページに移動する
				String back;
				back = jsi.getBackURL(wbc.category, wbc.currentPart, webView
						.getUrl());
				if (back == null) {
					return showExitDialog();
					//return super.onKeyDown(keyCode, event);
				} else {
					webView.loadUrl(back);
					return true;
				}
			}
		}
		else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ){
           	scrollview = (ScrollView) findViewById(R.id.scroll_view);
        	scrollview.scrollBy(0, 80);
        	return true;
        }else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
        	scrollview = (ScrollView) findViewById(R.id.scroll_view);
        	scrollview.scrollBy(0, -80);
        	return true;
        }
		return super.onKeyDown(keyCode, event);
	}


	// Menuボタンを押すと表示されるメニュー
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent intent;
		SharedPreferences.Editor editor;

		switch (item.getItemId()) {
		//カテゴリ
		case R.id.all_list: //全リスト
			wbc.category = R.string.all_list;
			webView.loadUrl(getString(R.string.all_list));
			return true;
		case R.id.series: //シリーズ・関連モノ
			wbc.category = R.string.series;
			webView.loadUrl(getString(R.string.series));
			return true;
		case R.id.good_ghost_story: //心霊ちょっといい話
			wbc.category = R.string.good_ghost_story;
			webView.loadUrl(getString(R.string.good_ghost_story));
			return true;
		case R.id.distortion_of_space_time: //時空の歪み
			wbc.category = R.string.distortion_of_space_time;
			webView.loadUrl(getString(R.string.distortion_of_space_time));
			return true;
		case R.id.legend_tokyo: //東京伝説
			wbc.category = R.string.legend_tokyo;
			webView.loadUrl(getString(R.string.legend_tokyo));
			return true;
		case R.id.matchless: //無双
			wbc.category = R.string.matchless;
			webView.loadUrl(getString(R.string.matchless));
			return true;
		case R.id.already: //ガイシュツ
			wbc.category = R.string.already;
			webView.loadUrl(getString(R.string.already));
			return true;
		case R.id.more_scary: //怖い話をもっと怖くする
			wbc.category = R.string.more_scary;
			webView.loadUrl(getString(R.string.more_scary));
			return true;
		case R.id.bbs_summary: //怖い話投稿掲示板まとめ
			wbc.category = R.string.bbs_summary;
			webView.loadUrl(getString(R.string.bbs_summary));
			return true;
		case R.id.bbs: //投稿掲示板
			wbc.category = R.string.bbs;
			webView.loadUrl(getString(R.string.bbs));
			return true;
		case R.id.mobile: //どこでも洒落コワ
			wbc.category = R.string.mobile;
			webView.loadUrl(getString(R.string.mobile));
			return true;


		//ランキング
			//R.id.matchless: //無双 はランキングにも表示
		case R.id.hall_of_frame_polling_place: //殿堂入り
			wbc.category = R.string.hall_of_frame_polling_space;
			webView.loadUrl(getString(R.string.hall_of_frame_polling_space));
			return true;
		case R.id.polling_place: //投票所
			wbc.category = R.string.polling_place;
			webView.loadUrl(getString(R.string.polling_place));
			return true;
		case R.id.pre_plling_place: //一桁・新規投票所
			wbc.category = R.string.pre_poling_place;
			webView.loadUrl(getString(R.string.pre_poling_place));
			return true;
		case R.id.good_ghost_story_in_hall_of_frame_polling_place: //いい話殿堂入り
			wbc.category = R.string.good_ghost_story_in_hall_of_frame;
			webView.loadUrl(getString(R.string.good_ghost_story_in_hall_of_frame));
			return true;
		case R.id.good_ghost_story_polling_place: //いい話投票所
			wbc.category = R.string.good_gohst_story_polling_place;
			webView.loadUrl(getString(R.string.good_gohst_story_polling_place));
			return true;
		case R.id.distortion_of_space_time_polling_place: //時空の歪み投票所
			wbc.category = R.string.distortion_of_space_time_polling_place;
			webView.loadUrl(getString(R.string.distortion_of_space_time_polling_place));
			return true;

		//設定
		case R.id.preference:
			intent = new Intent(SharekowaViwerActivity.this, SharekowaPreferenceActivity.class);
			intent.putExtra("TEST",100);
			startActivityForResult(intent, 0);
			return true;

		//URL共有
		case R.id.share_text:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			//Uri uri = Uri.parse(webView.getUrl());
			intent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
			try{
			startActivity(intent);
			}
			catch(Exception e){

			}
			return true;

		//ブラウザで開く
		case R.id.share_with_browser:
			intent = new Intent(Intent.ACTION_VIEW);
			Uri uri = Uri.parse(webView.getUrl());
			intent.setData(uri);
			try{
				startActivity(intent);
			}
			catch(Exception e){
			}
			return true;

		//bookmark
		case R.id.add_bookmark:
			saveBookmark(".bk");
			return true;

		case R.id.remove_bookmark:
			remove_bookmark = true;
			showBookmarkList();
			return true;

		case R.id.load_bookmark:
			remove_bookmark = false;
			showBookmarkList();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}


	private void saveBookmark(String suffix){
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		Bookmark bk = new Bookmark();
		String filename = null;
		bk.url = webView.getUrl();
		bk.category = wbc.category;
		bk.currentPart = wbc.currentPart;
		bk.partIndex = jsi.partIndex;
		bk.episodeIndex = jsi.episodeIndex;
		if(getTitle().length() > 0){
			filename = getTitle() + suffix;
		}else{
			filename = "bookmark" + suffix;
		}
		try {
			fos = this.openFileOutput(filename, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(bk);
			oos.close();
			fos.close();
			bk = null;

			Toast.makeText(this, getTitle() + "を登録しました", Toast.LENGTH_SHORT).show();

		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			Log.v("Sharekowa", "FileNotFound: " + e.getMessage());
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			Log.v("Sharekowa", "IOException; " + e.getMessage());
		}
	}

	private void loadBookmark(String filename){
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		Bookmark bk = new Bookmark();
		try {
			fis = this.openFileInput(filename);
			ois = new ObjectInputStream(fis);
			bk = (Bookmark) ois.readObject();
			wbc.category = bk.category;
			wbc.currentPart = bk.currentPart;
			if(null != bk.partIndex){
			  jsi.partIndex = bk.partIndex.clone();
			}
			if(null != bk.episodeIndex){
			  jsi.episodeIndex = bk.episodeIndex.clone();
			}
			webView.loadUrl(bk.url);
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			Log.v("Sharekowa", "FileNotFound: " + e.getMessage());
		} catch (StreamCorruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			Log.v("Sharekowa", "StreamCorruptedException" + e.getMessage());
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			Log.v("Sharekowa", "IOException: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			Log.v("Sharekowa", "ClassNotFound: " + e.getMessage());
		}
	}

	private void removeBookmark(String filename) {
		this.deleteFile(filename);

		String name = filename.substring(0, (filename.length()-3));
		Toast.makeText(this,  name + "を削除しました", Toast.LENGTH_SHORT).show();

	}

	//選択するためにブックマーク一覧ダイアログを表示
	private void showBookmarkList(){
		SharekowaBookmarkListDialog dlg = new SharekowaBookmarkListDialog(this);
		//リスナーの登録
		dlg.setOnSharekowaBookmarkListDialogListener((onBookmarkListDialogListener) this);
		//表示
		String path = "/data/data/" + this.getPackageName() + "/files/";
		dlg.show( path, "ブックマーク一覧");
	}

	//ブックマークが選択されたときの処理
	public void onClickFileList(File file) {
		if(file == null){
			Toast.makeText(this, "ブックマークが読み取れません", Toast.LENGTH_SHORT).show();
		}else{
			if(remove_bookmark){
				removeBookmark(file.getName());
			}else{
				loadBookmark(file.getName());
			}
		}
	}



	@Override
	public void onRestart(){
		super.onRestart();
		if(shpref.getBoolean(getString(R.string.black_style_key), false)){
			webView.setBackgroundColor(Color.BLACK);
		}
		else{
			webView.setBackgroundColor(Color.WHITE);
		}
		//設定画面を閉じた場合など、再表示された場合は画面更新のためリロード
		webView.reload();
	}


	@Override
	public void onDestroy(){
		super.onDestroy();
		if(isFinishing()){
			//終了確認ダイアログで「はい」を押して終了する場合、一時ファイルを削除
			deleteFile("temp");
		}
		else{
			//ユーザが意図しない終了(画面の縦横変更とか)の場合、現在のページを
			//一時ファイルに保存
			saveBookmark("temp");
		}

	}

	private boolean showExitDialog() {

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("終了確認");
			dialog.setMessage("終了しますか？");
			dialog.setPositiveButton("はい",
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int which) {
							dialog.dismiss();
							finish();
						}
					});
			dialog.setNegativeButton("いいえ",
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int which) {
							dialog.dismiss();
						}
					});
			dialog.setCancelable(false);
			dialog.show();
		return true;
	}

	public void setAppNameAndTitle(String title){
		setTitle(getString(R.string.app_name) + ":" +  title);
	}

	public void setTitleString(String title){
		setTitle(title);
	}

}



class Bookmark implements Serializable{
	public String url;
	public int category;
	public String currentPart;
	public String[][] partIndex;
	public String[][] episodeIndex;

	Bookmark(){

	}

}
