package jp.bpsinc.android.util;

import android.preference.ListPreference;

public class PreferenceUtil {
	/**
	 * entryValues内の値から、対応するentriesの値を取得
	 * 
	 * @param listPreference entriesを検索するIntListPreferenceオブジェクト
	 * @param setValue entryValuesから検索する値
	 * @return setValueに対応するentries、存在しない場合は空文字
	 */
	public static String getEntrys(ListPreference listPreference, String setValue) {
		String retVal = "";
		CharSequence[] entries = listPreference.getEntries();
		CharSequence[] values = listPreference.getEntryValues();

		// 変更された値を元に、タイトルを取得
		for (int i = 0; i < values.length; ++i) {
			if (values[i].toString().equals(setValue)) {
				retVal = entries[i].toString();
				break;
			}
		}
		return retVal;
	}
}
