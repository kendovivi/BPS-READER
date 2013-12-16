package jp.bpsinc.android.epub.info;

/**
 * ページめくり方向
 */
public enum PageDirection {
	/** 右から左 */
	RTL("rtl"),
	/** 左から右 */
	LTR("ltr"),
	/** 指定無し */
	DEFAULT("default"),;

	private String mValue;

	private PageDirection(String value) {
		mValue = value;
	}

	public String getValue() {
		return mValue;
	}

	public static PageDirection parse(String val) {
		if (RTL.getValue().equals(val)) {
			return RTL;
		}
		if (LTR.getValue().equals(val)) {
			return LTR;
		}
		return DEFAULT;
	}
}
