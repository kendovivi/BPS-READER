package jp.bpsinc.android.viewer.epub.dialog;


import com.example.bps_reader.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;

public class EpubViewerDialog {
	public static final int ID_OK					= 0;
	public static final int ID_ILLEGAL_ARG_ERR		= -11;
	public static final int ID_PATH_NOTFOUND_ERR	= -12;
	public static final int ID_SETTING_ERR			= -13;
	public static final int ID_OUT_OF_MEMORY_ERR	= -20;
	public static final int ID_LOAD_IMAGE_ERR		= -21;
	public static final int ID_DRM_RELEASE_ERR		= -30;
	public static final int ID_UNZIP_ERR			= -31;
	public static final int ID_EPUB_PARSER_ERR		= -32;
	public static final int ID_EPUB_OTHER_ERR		= -39;
	public static final int ID_UNEXPECTED_ERR		= -1024;

	/**
	 * onCreateDialog用のアラートダイアログ生成メソッド<br>
	 * 複数のアクティビティで同一の内容のダイアログを使うために使用
	 * 
	 * @param activity
	 * @param id ダイアログのID
	 * @return
	 */
	public static Dialog createAlertDialog(final Activity activity, int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setPositiveButton(activity.getString(R.string.epub_viewer_dlg_btn_positive), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				activity.finish();
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				activity.finish();
			}
		});
		// 表示メッセージをidごとに変更(可変メッセージの場合もここでsetMessageしないとメッセージが表示されない)
		switch (id) {
		case ID_OK:
			break;
		case ID_ILLEGAL_ARG_ERR:
		case ID_PATH_NOTFOUND_ERR:
		case ID_SETTING_ERR:
		case ID_OUT_OF_MEMORY_ERR:
		case ID_LOAD_IMAGE_ERR:
		case ID_DRM_RELEASE_ERR:
		case ID_UNZIP_ERR:
		case ID_EPUB_PARSER_ERR:
		case ID_EPUB_OTHER_ERR:
		case ID_UNEXPECTED_ERR:
		default:
			builder.setMessage("");
			builder.setTitle(activity.getString(R.string.epub_viewer_dlg_err_title));
			break;
		}
		return builder.create();
	}

	/**
	 * onPrepareDialog用のアラートダイアログ再設定メソッド<br>
	 * 複数のアクティビティで同一の内容のダイアログを使うために使用
	 * 
	 * @param activity
	 * @param id
	 * @param dialog
	 */
	public static void prepareAlertDialog(Activity activity, int id, Dialog dialog) {
		AlertDialog alertDialog = (AlertDialog)dialog;

		// 表示メッセージをidごとに変更
		switch (id) {
		case ID_OK:
			break;
		case ID_ILLEGAL_ARG_ERR:
		case ID_PATH_NOTFOUND_ERR:
		case ID_SETTING_ERR:
		case ID_LOAD_IMAGE_ERR:
			alertDialog.setMessage(String.format(activity.getString(R.string.epub_viewer_dlg_err_mes_browse), id));
			break;
		case ID_OUT_OF_MEMORY_ERR:
			alertDialog.setMessage(String.format(activity.getString(R.string.epub_viewer_dlg_err_mes_memory), id));
			break;
		case ID_DRM_RELEASE_ERR:
		case ID_UNZIP_ERR:
		case ID_EPUB_PARSER_ERR:
		case ID_EPUB_OTHER_ERR:
			alertDialog.setMessage(String.format(activity.getString(R.string.epub_viewer_dlg_err_mes_contents), id));
			break;
		case ID_UNEXPECTED_ERR:
			alertDialog.setMessage(String.format(activity.getString(R.string.epub_viewer_dlg_err_mes_unexpected), id));
			break;
		default:
			break;
		}
	}
}
