package jp.bpsinc.android.viewer.epub.menu;

import com.example.bps_reader.R;

import jp.bpsinc.android.epub.info.OpfMeta;
import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.activity.EpubViewerActivity;
import jp.bpsinc.android.viewer.epub.view.EpubScrollView;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class EpubViewerMenu implements OnTouchListener, OnClickListener {
	private final EpubViewerActivity mEpubViewerActivity;
	private final EpubScrollView mEpubScrollView;
	private final Paint mPaint;
	private final ViewGroup mEpubViewerLayout;
	private final View mMenu;
	private final LinearLayout mHeaderMenuLayout;
	private final LinearLayout mFooterMenuLayout;
	private final ImageButton mHeaderBackShelfButton;
	private final TextView mHeaderBookTitle;
	private final TextView mHeaderBookAuther;
	private final SeekBar mFooterMenuPageSeekBar;
	private final TextView mFooterMenuPageSeekText;
	private final ImageButton mFooterMenuSettingButton;
	private final ImageButton mFooterMenuBookmarkButton;

	private String mPageCountStr;

	public EpubViewerMenu(EpubViewerActivity epubViewerActivity, EpubScrollView epubScrollView, OpfMeta opfMeta) {
		mEpubViewerActivity = epubViewerActivity;
		mEpubScrollView = epubScrollView;
		mPaint = new Paint();

		// 大本のレイアウト取得
		mEpubViewerLayout = ((ViewGroup) mEpubViewerActivity.findViewById(R.id.epub_viewer_layout));
		// オプションメニュー用レイアウトを取得し、大本レイアウトに追加
		mMenu = mEpubViewerActivity.getLayoutInflater().inflate(R.layout.epub_viewer_menu, null);
		mMenu.setOnTouchListener(this);
		mEpubViewerLayout.addView(mMenu);

		// ヘッダやフッタ部分のレイアウトを取得して必要なリスナー追加
		mHeaderMenuLayout = (LinearLayout) mEpubViewerActivity.findViewById(R.id.epub_viewer_header_menu_layout);
		mHeaderBackShelfButton = (ImageButton) mEpubViewerActivity.findViewById(R.id.epub_viewer_header_back_shelf_button);
		mHeaderBookTitle = (TextView) mEpubViewerActivity.findViewById(R.id.epub_viewer_header_bibliography_book_title);
		mHeaderBookAuther = (TextView) mEpubViewerActivity.findViewById(R.id.epub_viewer_header_bibliography_book_auther);
		mFooterMenuLayout = (LinearLayout) mEpubViewerActivity.findViewById(R.id.epub_viewer_footer_menu_layout);
		mFooterMenuPageSeekText = (TextView) mEpubViewerActivity.findViewById(R.id.epub_viewer_footer_seek_text);
		mFooterMenuSettingButton = (ImageButton) mEpubViewerActivity.findViewById(R.id.epub_viewer_footer_setting_button);
		mFooterMenuBookmarkButton = (ImageButton) mEpubViewerActivity.findViewById(R.id.epub_viewer_footer_bookmark_button);
		mHeaderMenuLayout.setOnTouchListener(this);
		mFooterMenuLayout.setOnTouchListener(this);
		mHeaderBackShelfButton.setOnClickListener(this);
		mFooterMenuSettingButton.setOnClickListener(this);
		mFooterMenuBookmarkButton.setOnClickListener(this);

		// ページシークバー取得し、各種値設定
		mFooterMenuPageSeekBar = (SeekBar) mEpubViewerActivity.findViewById(R.id.epub_viewer_footer_seek_bar);
		mFooterMenuPageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			// トラッキング開始時に呼び出されます
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				LogUtil.v("progress=%d", seekBar.getProgress());
			}

			// トラッキング中に呼び出されます
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				LogUtil.v("progress=%d, fromUser=%b", progress, fromUser);

				// 1回シークバー動かしたあとに端末回転すると、ビュー生成前にこのメソッドが走りNullPointerExceptionで落ちるので判定追加
				// オプションメニュー開いた時(setProgressした時)に呼ばれた場合も無駄なので何もしない
				if (fromUser) {
					setSeekBarText(getSeekBarConvertProgress(progress));
					mEpubScrollView.jumpToPage(getSeekBarConvertProgress(progress));
				}
			}

			// トラッキング終了時に呼び出されます
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				LogUtil.v("progress=%d", seekBar.getProgress());
			}
		});
		// 本が右開きの場合と左開きの場合でページシークバーの向き(色の付き方など)を変更
		if (mEpubScrollView.getEpubPageAccess().isRTL()) {
			mFooterMenuPageSeekBar.setProgressDrawable(mEpubViewerActivity.getResources().getDrawable(R.drawable.seek_bar_rtl));
			mFooterMenuPageSeekBar.setIndeterminateDrawable(mEpubViewerActivity.getResources().getDrawable(R.drawable.seek_bar_rtl));
		} else {
			mFooterMenuPageSeekBar.setProgressDrawable(mEpubViewerActivity.getResources().getDrawable(R.drawable.seek_bar_ltr));
			mFooterMenuPageSeekBar.setIndeterminateDrawable(mEpubViewerActivity.getResources().getDrawable(R.drawable.seek_bar_ltr));
		}

		// 書誌情報設定
		mHeaderBookTitle.setText(opfMeta.getTitle());
		mHeaderBookAuther.setText(opfMeta.getAuthor());

		// 書誌情報位置を真ん中にするためのダミーレイアウトを非表示に設定
		mEpubViewerActivity.findViewById(R.id.epub_viewer_header_back_shelf_button2).setVisibility(View.INVISIBLE);

		// メニュー消去
		mMenu.setVisibility(View.GONE);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == mHeaderMenuLayout || v == mFooterMenuLayout) {
			// ヘッダやフッタ部分のタッチ無効化(EpubScrollViewのonTouchEventが動かないようにする)
			return true;
		} else if (v == mMenu) {
			// ヘッダとフッタ以外の箇所タッチしたらメニューを閉じる
			closeOptionMenu();
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v == mHeaderBackShelfButton) {
			// 本棚へ戻るボタンを押したらビューア終了
			mEpubViewerActivity.finish();
		} else if (v == mFooterMenuSettingButton) {
			// 設定ボタンを押したら設定画面表示
			mEpubViewerActivity.showSettingActivity();
		} else if (v == mFooterMenuBookmarkButton) {
			// ブックマークボタン押したらブックマーク画面表示
			mEpubViewerActivity.showBookmarkActivity();
		}
	}

	public boolean isShowOptionMenu() {
		return mMenu.getVisibility() == View.VISIBLE;
	}

	public void showOptionMenu() {
		mMenu.setVisibility(View.VISIBLE);
		seekBarInit();
	}

	public void closeOptionMenu() {
		mMenu.setVisibility(View.GONE);
	}

	/**
	 * ページシークバー関連の各値を初期化
	 */
	private void seekBarInit() {
		LinearLayout.LayoutParams linearLayoutParams;
		String pageCountStr = String.valueOf(mEpubScrollView.getPageCount());
		mPageCountStr = " / " + pageCountStr;

		mPaint.setTextSize(mFooterMenuPageSeekText.getTextSize());
		linearLayoutParams = (LinearLayout.LayoutParams) mFooterMenuPageSeekText.getLayoutParams();
		linearLayoutParams.width = (int) mPaint.measureText(pageCountStr + mPageCountStr) + 1;
		mFooterMenuPageSeekText.setLayoutParams(linearLayoutParams);

		// ページシークバーの各種値を設定
		mFooterMenuPageSeekBar.setMax(mEpubScrollView.getPageCount() - 1);
		// ページシークバー表示したまま端末回転した直後にオプションメニュー表示するとバーの表示がおかしいことがあるので一旦0にする
		// 原因としてはsetProgressの値が現在と同じ位置の場合に何も変化しない(onProgressChangedも呼ばれない)からだと思われる(シークバー再取得しても端末回転前の状態が残ってて、シークバーの横幅が縦横で異なるからずれる？)
		mFooterMenuPageSeekBar.setProgress(0);
		mFooterMenuPageSeekBar.setProgress(getSeekBarConvertProgress(mEpubScrollView.getCurrentPageIndex()));
		setSeekBarText(mEpubScrollView.getCurrentPageIndex());
	}

	/**
	 * 渡されたプログレスに1を足し(0始まりのため)、後ろに全体のページ数表示を付与してページシークバー横のテキストを更新する
	 * 
	 * @param progress ページシークバーのプログレス値、右開きの本の時は反転させる必要がある
	 */
	private void setSeekBarText(int progress) {
		mFooterMenuPageSeekText.setText((progress + 1) + mPageCountStr);
	}

	/**
	 * ページシークバーのプログレス値をLTRとRTLの場合で反転させる
	 * 
	 * @param progress 元のプログレス値
	 * @return LTRの場合はprogress、RTLの場合は全ページ数 - (progress + 1)
	 */
	private int getSeekBarConvertProgress(int progress) {
		int seekBarConvertIndex = progress;
		if (mEpubScrollView.getEpubPageAccess().isRTL()) {
			seekBarConvertIndex = mEpubScrollView.getPageCount() - (seekBarConvertIndex + 1);
		}
		return seekBarConvertIndex;
	}
}
