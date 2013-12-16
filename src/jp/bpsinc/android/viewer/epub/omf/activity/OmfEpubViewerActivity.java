package jp.bpsinc.android.viewer.epub.omf.activity;

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

public class OmfEpubViewerActivity extends EpubViewerActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.v();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected ViewMode getViewMode() {
		boolean isLandscape = OrientationUtil.isLandscape(this);
		boolean isSpread;
		if (mViewMode == null) {
			// OMFの初期表示は端末の向きごとに固定されてる
			if (isLandscape) {
				isSpread = true;
			} else {
				isSpread = false;
			}
		} else {
			// OMFの端末回転時は見開き設定を切り替える
			isSpread = mViewMode.isSpread() == false;
		}
		return new ViewMode(ContentMode.OMF, isLandscape, isSpread);
	}

	@Override
	protected void applySetting() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		// 回転設定
		OrientationUtil.setOrientation(this, pref.getString(
				getString(R.string.pre_key_omf_epub_viewer_orientation),
				getString(R.string.pre_default_omf_epub_viewer_orientation)));
		// ページめくりアニメーション設定
		String pageAnimation = pref.getString(
				getString(R.string.pre_key_omf_epub_viewer_animation),
				getString(R.string.pre_default_omf_epub_viewer_animation));
		if (PageAnimation.NONE.toString().equals(pageAnimation)) {
			mViewMode.setPageAnimation(PageAnimation.NONE);
		} else if (PageAnimation.SLIDE.toString().equals(pageAnimation)) {
			mViewMode.setPageAnimation(PageAnimation.SLIDE);
		} else {
			mViewMode.setPageAnimation(PageAnimation.SLIDE);
		}
		// 画面の明るさ設定
		WindowUtil.setBrightness(this, pref.getInt(
				getString(R.string.pre_key_omf_epub_viewer_brightness),
				getResources().getInteger(R.integer.pre_default_omf_epub_viewer_brightness)));
	}

	@Override
	public void showSettingActivity() {
		Intent intent = new Intent(this, jp.bpsinc.android.viewer.epub.omf.preference.activity.OmfEpubViewerPreferenceActivity.class);
		startActivityForResult(intent, REQUEST_CODE_SETTING);
	}
}
