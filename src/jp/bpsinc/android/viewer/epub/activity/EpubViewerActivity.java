package jp.bpsinc.android.viewer.epub.activity;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipException;

import com.example.bps_reader.R;

import bps.android.reader.book.BookInfo;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.db.AutoBookmarkTable;
import jp.bpsinc.android.viewer.epub.content.EpubPageAccess;
import jp.bpsinc.android.viewer.epub.content.EpubFile;
import jp.bpsinc.android.viewer.epub.content.EpubViewerContents;
import jp.bpsinc.android.viewer.epub.content.EpubZipFile;
import jp.bpsinc.android.viewer.epub.content.Page;
import jp.bpsinc.android.viewer.epub.dialog.EpubViewerDialog;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;
import jp.bpsinc.android.viewer.epub.menu.EpubViewerMenu;
import jp.bpsinc.android.viewer.epub.view.EpubScrollView;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode;
import jp.bpsinc.android.viewer.function.activity.BookmarkActivity;
import jp.bpsinc.android.viewer.function.content.BookmarkInfo;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public abstract class EpubViewerActivity extends Activity {
	/** ハードウェアアクセラレーション有効化用フラグ、APIレベル11未満用に定義 */
	private static final int FLAG_HARDWARE_ACCELERATED = 0x01000000;
	/** 本棚から受け渡されるインテントのキー */
	public static final String INTENT_KEY_EPUB_CONTENTS = "jp.bpsinc.android.viewer.epub.activity.INTENT_KEY_EPUB_CONTENTS";
	/** 設定画面起動用リクエストコード */
	public static final int REQUEST_CODE_SETTING = 1;
	/** ブックマーク画面起動用リクエストコード */
	public static final int REQUEST_CODE_BOOKMARK = 2;
	/** セーブしたEpubFileのキー */
	private static final String SAVE_EPUB_FILE = "SAVE_EPUB_FILE";
	/** セーブしたViewModeのキー */
	private static final String SAVE_VIEW_MODE = "SAVE_VIEW_MODE";
	/** 本棚から受け渡されるデータ */
	private BookInfo mBookInfo;
	/** 解析したEPUBの各種情報を格納する */
	protected EpubFile mEpubFile;
	/** EPUBビューアの各種設定の取得などに使用 */
	protected ViewMode mViewMode;
	/** EPUBエントリにアクセスするためのインスタンス */
	protected EpubPageAccess mEpubPageAccess;
	/** 画像を描画してスクロールなどするためのビュー */
	protected EpubScrollView mEpubScrollView;
	/** 自動しおり機能 */
	protected AutoBookmarkTable mAutoBookmarkTable;
	/** 非UIスレッドからの処理受け付け用ハンドラ */
	private Handler mHandler;

	/** しおりとかをユーザごとに区別する必要が出た時用のユーザID:デフォルトで「userId」を指定、変更の必要が出たらonCreateで設定すること */
	private String mUserId = "userId";
	/** オプションメニュー表示用のクラス */
	protected EpubViewerMenu mEpubViewerMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.v();
		super.onCreate(savedInstanceState);

		// タイトルとステータスバー非表示、ハードウェアアクセラレーション有効化
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(FLAG_HARDWARE_ACCELERATED, FLAG_HARDWARE_ACCELERATED);

		try {
			// 本棚からの情報を取得
		    mBookInfo = (BookInfo)getIntent().getSerializableExtra(INTENT_KEY_EPUB_CONTENTS);
			if (mBookInfo == null) {
				throw new IllegalArgumentException("EpubViewerContents is null");
			}

			String filePath = mBookInfo.getmEpubPath();
			if (filePath == null) {
				throw new IllegalArgumentException("epub file path is null");
			}
			LogUtil.v("read file = %s", filePath);

			// TODO DRM解除

			EpubZipFile epubZipFile = new EpubZipFile(filePath);
			if (savedInstanceState == null) {
				mEpubFile = new EpubFile(epubZipFile, null);
			} else {
				// 端末回転などでアクティビティが再生成された場合は前回パースしたEPUB情報を保存してあるのでそれを使う
				mEpubFile = (EpubFile) savedInstanceState.getSerializable(SAVE_EPUB_FILE);
				// ViewModeも前回情報を取得するが、これはgetViewModeのタイミングで上書きする(getViewMode内などで前回の情報を参照したい場合用)
				mViewMode = (ViewMode) savedInstanceState.getSerializable(SAVE_VIEW_MODE);
			}
			mViewMode = getViewMode();

			// 設定適用、基本的にウィンドウやmViewModeに設定するのでここでやる、EpubScrollView作成後だと見開き状態などが変わり読み込みが2度走る可能性がある、この処理を移動する場合色々考慮すること
			applySetting();

			mEpubPageAccess = new EpubPageAccess(epubZipFile, mEpubFile) {
				@SuppressWarnings("unchecked")
				@Override
				public ArrayList<Page> getSpreadPageList() {
					ArrayList<Page> list = mSpreadPages;
					if (isRTL() == false) {
						// LTRならページリストをシャローコピーして逆順にしたオブジェクトを返す
						list = (ArrayList<Page>)list.clone();
						Collections.reverse(list);
					}
					return list;
				}
				@SuppressWarnings("unchecked")
				@Override
				public ArrayList<Page> getSinglePageList() {
					ArrayList<Page> list = mSinglePages;
					if (isConvertedPage()) {
						// LTRかつ縦向きならページリストをシャローコピーして逆順にしたオブジェクトを返す
						list = (ArrayList<Page>)list.clone();
						Collections.reverse(list);
					}
					return list;
				}
			};
			if (mEpubFile.getId() == null) {
				mAutoBookmarkTable = null;
			} else {
				// TODO ユーザごとに自動しおりを分ける必要が出るまでユーザIDは固定値
				mAutoBookmarkTable = new AutoBookmarkTable(getApplicationContext(), mUserId, mEpubFile.getId());
			}
			setContentView(R.layout.epub_viewer);
			mEpubScrollView = new EpubScrollView(this, mEpubPageAccess, mViewMode, getAutoBookmarkPageIndex());
			((ViewGroup) findViewById(R.id.epub_viewer_layout)).addView(mEpubScrollView);
			mEpubViewerMenu = new EpubViewerMenu(this, mEpubScrollView, mEpubFile.getOpfMeta());
		} catch (IllegalArgumentException e) {
			LogUtil.e("illegal argument error", e);
			showDialog(EpubViewerDialog.ID_ILLEGAL_ARG_ERR);
		} catch (FileNotFoundException e) {
            LogUtil.e("specified path not found error", e);
            showDialog(EpubViewerDialog.ID_PATH_NOTFOUND_ERR);
		} catch (ZipException e) {
			LogUtil.e("zip file error", e);
			showDialog(EpubViewerDialog.ID_UNZIP_ERR);
		} catch (EpubParseException e) {
			LogUtil.e("epub parser error", e);
			showDialog(EpubViewerDialog.ID_EPUB_PARSER_ERR);
		} catch (EpubOtherException e) {
			LogUtil.e("epub file error", e);
			showDialog(EpubViewerDialog.ID_EPUB_OTHER_ERR);
		}
	}

	@Override
	protected void onDestroy() {
		LogUtil.v();

		// onDestroyの一番始めにスレッド停止する、ここでスレッド消えるまで待機が発生
		if (mEpubScrollView != null) {
			mEpubScrollView.destroy();
		}
		if (mEpubViewerMenu != null) {
			mEpubViewerMenu.closeOptionMenu();
		}

		// スレッド停止後にZIPクローズすれば不具合無いと思ってたけど、端末回転を繰り返しながら画面をタップし続けると画像読み込みスレッドで何故かZIP close errorになる
		// ログを見る限りwaitは効いてるしonDestroy→onCreate→onLayout→スレッドでの画像読み込みの順になってる
		// ZIP closeになる理由は分からないが、仕方ないのでcloseするのはやめる、manifestでビューアは別プロセス起動にしているので開きっぱなしになって本棚画面で本を削除できないとかにはならないはず

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		LogUtil.v();
		if (mAutoBookmarkTable != null) {
			int currentPageIndex = mEpubScrollView.getCurrentPageIndex();
			if (isSpreadPageList()) {
				currentPageIndex = mEpubPageAccess.getSingleIndexFromSpreadIndex(currentPageIndex);
				if (mEpubScrollView.isSubsequentPagePosition()) {
					// 見開き表示状態で後続ページ側にポジションが寄っている場合は単ページインデックスを+1する
					currentPageIndex++;
				}
			}
			mAutoBookmarkTable.setAutoBookmark(currentPageIndex);
		}
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		LogUtil.v();
		outState.putSerializable(SAVE_EPUB_FILE, mEpubFile);
		outState.putSerializable(SAVE_VIEW_MODE, mViewMode);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		LogUtil.v();
		return EpubViewerDialog.createAlertDialog(this, id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		LogUtil.v();
		EpubViewerDialog.prepareAlertDialog(this, id, dialog);
		super.onPrepareDialog(id, dialog);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogUtil.v();

		switch (requestCode) {
		case REQUEST_CODE_SETTING:
			// 設定画面から戻ってきた時
			if (resultCode == RESULT_OK) {
				applySetting();
			}
			break;
		case REQUEST_CODE_BOOKMARK:
			// しおり画面から戻ってきた時
			if (resultCode == RESULT_OK) {
				// しおり画面で端末回転してる場合、onDestroy→onCreate→onActivityResultの順に動くらしい(色んな端末で試したわけではない)
				// onLayout前にonActivityResultが動くのでjumpToPageでカレントページだけ更新される、その後onLayoutが動き、通常通りの流れで画像読み込みが行われる
				// 試した限りでは無かったが、onLayoutの後にonActivityResultが動いた場合、ページシークバーで移動した時と同じ動作となるのでこちらも問題なし
				int bookmarkPage = data.getIntExtra(BookmarkActivity.INTENT_DATA_KEY_BOOKMARK_PAGE, 0);
				mEpubScrollView.jumpToPage(pageCountCorrection(bookmarkPage));

				if (mEpubViewerMenu.isShowOptionMenu()) {
					// しおり画面から戻ったらオプションメニュー閉じる(ここの仕様変える場合、変わりにページシークバー更新しないといけない)
					mEpubViewerMenu.closeOptionMenu();
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mEpubViewerMenu.isShowOptionMenu()) {
			mEpubViewerMenu.closeOptionMenu();
		} else {
			mEpubViewerMenu.showOptionMenu();
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		if (mEpubViewerMenu.isShowOptionMenu()) {
			mEpubViewerMenu.closeOptionMenu();
		} else {
			super.onBackPressed();
		}
	}

	public EpubViewerMenu getEpubViewerMenu() {
		return mEpubViewerMenu;
	}

	/**
	 * ページ数を逆転する必要があるか判定する
	 * 
	 * @return LTR、かつ、横スクロールならtrue、それ以外ならfalse
	 */
	public boolean isConvertedPage() {
		return mEpubPageAccess.isRTL() == false && mViewMode.isVerticalScroll() == false;
	}

	public boolean isSpreadPageList() {
		switch (mViewMode.getMode()) {
		case PORTRAIT_SPREAD:
		case LANDSCAPE_SPREAD:
			return true;
		case PORTRAIT_STANDARD:
		case LANDSCAPE_STANDARD:
		default:
			return false;
		}
	}

	public void postShowDialog(final int id) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				showDialog(id);
			}
		});
	}

	/**
	 * 自動しおりが存在する場合、その値を習得して現在のビュー設定に合わせた値に補正したページインデックスを返す
	 * 
	 * @return 自動しおりを見開き設定に合わせた値に補正したページインデックス、自動しおりが存在しない場合は0
	 */
	private int getAutoBookmarkPageIndex() {
		int currentPageIndex = 0;
		if (mAutoBookmarkTable != null) {
			AutoBookmarkTable.Row row = mAutoBookmarkTable.getAutoBookmark();
			if (row != null) {
				currentPageIndex = pageCountCorrection(row.getLastPage());
			}
		}
		return currentPageIndex;
	}

	public void showBookmarkActivity() {
		Intent intent = new Intent(this, jp.bpsinc.android.viewer.function.activity.BookmarkActivity.class);
		BookmarkInfo bookmarkInfo = new BookmarkInfo(mUserId, mEpubFile.getId(), mEpubScrollView.getCurrentPageIndex(), mEpubFile.getSinglePages().size());
		intent.putExtra(BookmarkActivity.INTENT_KEY_BOOKMARK_INFO, bookmarkInfo);
		startActivityForResult(intent, REQUEST_CODE_BOOKMARK);
	}

	/**
	 * 単ページ表示のページインデックスを渡し、現在の見開き設定に合ったページインデックスに補正する<br>
	 * 見開き設定の場合、singlePageIndexが見開き時の後続ページだったらViewModeの後続ページポジションフラグをtrueにする<br>
	 * 値が範囲外の場合は一番近い正常値に補正する
	 * 
	 * @param singlePageIndex 単ページ表示のページインデックス
	 * @return 見開き設定に合ったページインデックス、範囲外の場合は一番近い正常値
	 */
	private int pageCountCorrection(int singlePageIndex) {
		int currentPageIndex = singlePageIndex;
		if (isSpreadPageList()) {
			// 保存されているページ数は単ページのもののため、見開き設定なら変換かける
			currentPageIndex = mEpubPageAccess.getSpreadIndexFromSingleIndex(currentPageIndex);
			spreadSubsequentPageJudgment(singlePageIndex, currentPageIndex);
		} else {
			// 自動しおりに保存されてた値が範囲外だったら補正する
			if (currentPageIndex < 0) {
				currentPageIndex = 0;
			} else if (currentPageIndex >= mEpubPageAccess.getSinglePageList().size()) {
				currentPageIndex = mEpubPageAccess.getSinglePageList().size() - 1;
			}
		}
		return currentPageIndex;
	}

	/**
	 * 単ページインデックスが見開きページの後続ページの場合、ViewModeの後続ページポジションフラグをtrueにする
	 * 
	 * @param singlePageIndex 単ページインデックス
	 * @param spreadPageIndex 見開きページインデックス
	 */
	public void spreadSubsequentPageJudgment(int singlePageIndex, int spreadPageIndex) {
		if (mEpubPageAccess.isRTL() == false) {
			// LTR時はgetSpreadPageListの中身が反転してるのでインデックスを補正
			spreadPageIndex = mEpubPageAccess.getSpreadPageList().size() - spreadPageIndex - 1;
		}
		if (singlePageIndex > mEpubPageAccess.getSpreadPageList().get(spreadPageIndex).getThisPage()) {
			mViewMode.setIsSubsequentPagePositionFlag(true);
		}
	}

	/** 実装クラスごとの設定を行ったViewModeを返す */
	protected abstract ViewMode getViewMode();
	/** 実装クラスごとの設定画面を呼び出す */
	public abstract void showSettingActivity();
	/** 実装クラスごとの設定適用をする */
	protected abstract void applySetting();
}
