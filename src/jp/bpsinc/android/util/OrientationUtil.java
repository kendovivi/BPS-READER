package jp.bpsinc.android.util;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class OrientationUtil {
	/** 回転設定 */
	private enum OrientationSetting {
		/** 端末の設定に従う */
		SCREEN_ORIENTATION_SENSOR,
		/** 縦固定 */
		SCREEN_ORIENTATION_PORTRAIT,
		/** 横固定 */
		SCREEN_ORIENTATION_LANDSCAPE,
	}

	/**
	 * 横向きか判定
	 * 
	 * @param activity
	 * @return 横向きならtrue、それ以外ならfalse
	 */
	public static boolean isLandscape(Activity activity) {
		return activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	/**
	 * 端末の向きを設定する
	 * 
	 * @param activity 設定するアクティビティ
	 * @param orientation <br>
	 *            ・SCREEN_ORIENTATION_SENSOR:端末の設定に従う<br>
	 *            ・SCREEN_ORIENTATION_PORTRAIT:縦固定<br>
	 *            ・SCREEN_ORIENTATION_LANDSCAPE：横固定<br>
	 *            ・その他の値：端末の設定に従う
	 */
	public static void setOrientation(Activity activity, String orientation) {
		if (OrientationSetting.SCREEN_ORIENTATION_SENSOR.toString().equals(orientation)) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		} else if (OrientationSetting.SCREEN_ORIENTATION_PORTRAIT.toString().equals(orientation)) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (OrientationSetting.SCREEN_ORIENTATION_LANDSCAPE.toString().equals(orientation)) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}
}
