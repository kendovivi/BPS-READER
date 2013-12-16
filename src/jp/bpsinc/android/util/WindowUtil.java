package jp.bpsinc.android.util;

import android.app.Activity;
import android.view.WindowManager;

public class WindowUtil {
	/**
	 * 画面の明るさを設定、0未満の値はデフォルト(システム設定)、1～100(実際には100fで割って0～1の値を設定)はユーザの好みの明るさに調節可能(0にすると画面が真っ暗になって操作不可能になる？)
	 * 
	 * @param activity 画面の明るさを変更するアクティビティ
	 * @param brightness 画面の明るさ(暗1～100明)
	 */
	public static void setBrightness(Activity activity, int brightness) {
		WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		lp.screenBrightness = brightness / 100f;
		activity.getWindow().setAttributes(lp);
	}
}
