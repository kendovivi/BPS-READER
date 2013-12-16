package jp.bpsinc.android.viewer.epub.fxl.preference.activity;

import com.example.bps_reader.R;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.util.OrientationUtil;
import jp.bpsinc.android.util.PreferenceUtil;
import jp.bpsinc.android.util.WindowUtil;
import jp.bpsinc.android.viewer.preference.BrightnessPreference;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class FxlEpubViewerPreferenceActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
	/** 回転設定 */
	private ListPreference mOrientationPreference;
	/** 見開き表示 */
	private CheckBoxPreference mSpreadPreference;
	/** ページめくりアニメーション */
	private ListPreference mAnimationPreference;
	/** 画面の明るさ */
	private BrightnessPreference mBrightnessPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.v();
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.fxl_epub_viewer_preference);

		mOrientationPreference = (ListPreference) findPreference(getString(R.string.pre_key_fxl_epub_viewer_orientation));
		mSpreadPreference = (CheckBoxPreference) findPreference(getString(R.string.pre_key_fxl_epub_viewer_spread));
		mAnimationPreference = (ListPreference) findPreference(getString(R.string.pre_key_fxl_epub_viewer_animation));
		mBrightnessPreference = (BrightnessPreference) findPreference(getString(R.string.pre_key_fxl_epub_viewer_brightness));

		mOrientationPreference.setOnPreferenceChangeListener(this);
		mSpreadPreference.setOnPreferenceChangeListener(this);
		mAnimationPreference.setOnPreferenceChangeListener(this);
		mBrightnessPreference.setOnPreferenceChangeListener(this);

		mOrientationPreference.setSummary(PreferenceUtil.getEntrys(mOrientationPreference, mOrientationPreference.getValue()));
		mAnimationPreference.setSummary(PreferenceUtil.getEntrys(mAnimationPreference, mAnimationPreference.getValue()));

		mBrightnessPreference.setSummary(this, PreferenceManager.getDefaultSharedPreferences(this).getInt(
				getString(R.string.pre_key_fxl_epub_viewer_brightness),
				getResources().getInteger(R.integer.pre_default_fxl_epub_viewer_brightness)));

		OrientationUtil.setOrientation(this, mOrientationPreference.getValue());
		WindowUtil.setBrightness(this, mBrightnessPreference.getValue());

		// 回転設定変えた場合など、resultCodeが初期化されることがあるのでここで設定
		if (savedInstanceState != null) {
			setResult(RESULT_OK);
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		LogUtil.v();
		boolean retBool = true;
		if (preference instanceof ListPreference) {
			// リストプリファレンスの処理
			ListPreference listPreference = (ListPreference) preference;
			String strValue = (String) newValue;

			// 違う値が設定されたらサマリー更新
			if (listPreference.getValue().equals(strValue) == false) {
				preference.setSummary(PreferenceUtil.getEntrys(listPreference, strValue));

				if (listPreference == mOrientationPreference) {
					// 回転設定の場合、このアクティビティにも即座に適用
					OrientationUtil.setOrientation(this, strValue);
				}
			} else {
				retBool = false;
			}
		} else if (preference == mBrightnessPreference) {
			// 画面の明るさが変更されたら実際の設定値とサマリーを変更
			int intValue = ((Integer) newValue).intValue();
			WindowUtil.setBrightness(this, intValue);
			mBrightnessPreference.setSummary(this, intValue);
		}
		if (retBool) {
			setResult(RESULT_OK);
		}
		return retBool;
	}
}
