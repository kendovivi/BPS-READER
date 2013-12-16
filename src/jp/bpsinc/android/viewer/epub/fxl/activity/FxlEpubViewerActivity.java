package jp.bpsinc.android.viewer.epub.fxl.activity;

import com.example.bps_reader.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.util.OrientationUtil;
import jp.bpsinc.android.util.WindowUtil;
import jp.bpsinc.android.viewer.epub.activity.EpubViewerActivity;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode.ContentMode;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode.PageAnimation;

public class FxlEpubViewerActivity extends EpubViewerActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.v();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected ViewMode getViewMode() {
		// ここの見開き設定は適当にfalse入れとく、直後にapplySettingで更新される
		return new ViewMode(ContentMode.FXL, OrientationUtil.isLandscape(this), false);
	}

	@Override
	protected void applySetting() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		// 回転設定
		OrientationUtil.setOrientation(this, pref.getString(
				getString(R.string.pre_key_fxl_epub_viewer_orientation),
				getString(R.string.pre_default_fxl_epub_viewer_orientation)));
		// 見開き表示設定
		mViewMode.setIsSpread(pref.getBoolean(
				getString(R.string.pre_key_fxl_epub_viewer_spread),
				getResources().getBoolean(R.bool.pre_default_fxl_epub_viewer_spread)));
		// ページめくりアニメーション設定
		String pageAnimation = pref.getString(
				getString(R.string.pre_key_fxl_epub_viewer_animation),
				getString(R.string.pre_default_fxl_epub_viewer_animation));
		if (PageAnimation.NONE.toString().equals(pageAnimation)) {
			mViewMode.setPageAnimation(PageAnimation.NONE);
		} else if (PageAnimation.SLIDE.toString().equals(pageAnimation)) {
			mViewMode.setPageAnimation(PageAnimation.SLIDE);
		} else {
			mViewMode.setPageAnimation(PageAnimation.SLIDE);
		}
		// 画面の明るさ設定
		WindowUtil.setBrightness(this, pref.getInt(
				getString(R.string.pre_key_fxl_epub_viewer_brightness),
				getResources().getInteger(R.integer.pre_default_fxl_epub_viewer_brightness)));
		if (mEpubScrollView != null) {
			// 見開き表示設定が切り替わってるかも知れないのでページリスト更新
			mEpubScrollView.pageListInit();
		}
	}

	@Override
	public void showSettingActivity() {
		Intent intent = new Intent(this, jp.bpsinc.android.viewer.epub.fxl.preference.activity.FxlEpubViewerPreferenceActivity.class);
		startActivityForResult(intent, REQUEST_CODE_SETTING);
	}
}
