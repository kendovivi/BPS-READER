package jp.bpsinc.android.epub.info;

/**
 * opfファイルSpine itemrefプロパティ
 */
public enum PageSpread {
	/** 見開きで左 */
	LEFT("page-spread-left"),

	/** 見開きで右 */
	RIGHT("page-spread-right"),

	/** 見開きしない, または不明 */
	NONE(null);

	private String mValue;

	private PageSpread(String value) {
		mValue = value;
	}

	public String getValue() {
		return mValue;
	}
}
