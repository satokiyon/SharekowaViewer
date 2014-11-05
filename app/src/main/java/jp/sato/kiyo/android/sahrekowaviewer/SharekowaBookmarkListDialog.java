package jp.sato.kiyo.android.sahrekowaviewer;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;

public class SharekowaBookmarkListDialog extends Activity implements
		OnClickListener, android.content.DialogInterface.OnClickListener {

	private Context _parentcon = null;
	private File[] _dialog_file_list;

    private int _select_count = -1;                         //何番目を選択したか
    private onBookmarkListDialogListener _listener = null;      //リスナー


    SharekowaBookmarkListDialog(Context con){
    	_parentcon = con;
    }

    public void setOnSharekowaBookmarkListDialogListener(onBookmarkListDialogListener listener){
        _listener = listener;
    }

    /**
     * クリックイベントのインターフェースクラス
     */
    public interface onBookmarkListDialogListener{
        public void onClickFileList(File file);
    }


	public void onClick(View arg0) {
		// 何もしない

	}

	//ファイルを選択されたときに呼ばれる処理
	public void onClick(DialogInterface arg0, int which) {
        //選択されたので位置を保存
        _select_count = which;
        if((_dialog_file_list == null) || (_listener == null)){
        }else{
            File file = _dialog_file_list[which];

//          Util.outputDebugLog("getAbsolutePath : " + file.getAbsolutePath());
//          Util.outputDebugLog("getPath : " + file.getPath());
//          Util.outputDebugLog("getName : " + file.getName());
//          Util.outputDebugLog("getParent : " + file.getParent());

            if(file.isDirectory()){
                //ディレクトリなら何もしない
            }else{
                //それ以外は終了なので親のハンドラ呼び出す
                _listener.onClickFileList(file);
            }
        }
	}

    public void show(String path, String title){

        try{
            _dialog_file_list = new File(path).listFiles();
            if(_dialog_file_list == null){
                //NG
                if(_listener != null){
                    //リスナーが登録されてたら空で呼び出す
                    _listener.onClickFileList(null);
                }
            }else{
                String[] list = new String[_dialog_file_list.length];
                int count = 0;
                String name = "";

                //ファイル名のリストを作る
                for (File file : _dialog_file_list) {
                    if(file.isDirectory()){
                        //ディレクトリの場合、リストには表示しない
                        //name = file.getName() + "/";

                    }else{
                        //通常のファイル(bk_で始まるブックマークファイルだけ)
                    	if(file.getName().endsWith(".bk")){
                          name = file.getName();
                          name = name.substring(0, (name.length() -3));//表示上は".bk"を削除する
                    	}
                    }
                    list[count] = name;
                    count++;
                }

                //ダイアログ表示
                new AlertDialog.Builder(_parentcon).setTitle(title).setItems(list, this).show();
            }
        }catch(SecurityException se){
            //Util.outputDebugLog(se.getMessage());
        }catch(Exception e){
            //Util.outputDebugLog(e.getMessage());
        }

    }


	//選択されたファイル名を取得
    public String getSelectedFileName(){
        String selected_file = "";
        if(_select_count < 0){

        }else{
            selected_file = _dialog_file_list[_select_count].getName();
        }
        return selected_file;
    }


}
