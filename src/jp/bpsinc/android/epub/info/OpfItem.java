package jp.bpsinc.android.epub.info;

import java.io.Serializable;

@SuppressWarnings("serial")
public class OpfItem implements Serializable {
	private static final String MEDIA_TYPE_HTML = "text/html";
	private static final String MEDIA_TYPE_XHTML = "application/xhtml+xml";

	public static final String ATT_ID = "id";
	public static final String ATT_HREF = "href";
	public static final String ATT_MEDIA_TYPE = "media-type";
	public static final String ATT_FALLBACK = "fallback";
	public static final String ATT_PROPERTIES = "properties";

	private final String mId;
	private final String mHref;
	private final String mMediaType;
	private final String[] mProperties;
	private String[] mSpineProperties;
	private OpfItem mFallback;

	public OpfItem(String id, String href, String mediaType, String properties) {
		mId = id;
		mHref = href;
		mMediaType = mediaType;
		if (properties != null) {
			mProperties = properties.split(" ");
		} else {
			mProperties = null;
		}
	}

	public void setSpineProperties(String properties) {
		if (properties != null) {
			mSpineProperties = properties.split(" ");
		}
	}

	public String getId() {
		return mId;
	}

	public String getHref() {
		return mHref;
	}

	public String getMediaType() {
		return mMediaType;
	}

	/**
	 * HTML, XHTMLならtrueを返す
	 * 
	 * @return
	 */
	public boolean isHtml() {
		if (mMediaType != null) {
			if (mMediaType.equals(MEDIA_TYPE_XHTML) || mMediaType.equals(MEDIA_TYPE_HTML)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * XHTMLならtrueを返す
	 * 
	 * @return
	 */
	public boolean isXhtml() {
		if (mMediaType != null) {
			if (mMediaType.equals(MEDIA_TYPE_XHTML)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * image/jpeg, image/pngなど画像ならtrueを返す
	 * 
	 * @return
	 */
	public boolean isImage() {
		if (mMediaType != null) {
			if (mMediaType.startsWith("image/") && !mMediaType.contains("svg")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * EPUBのnav要素かどうかを判定
	 * 
	 * @return
	 */
	public boolean isNav() {
		if (mProperties != null) {
			for (String p : mProperties) {
				if (p.equals("nav")) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isCoverImage() {
		if (mProperties != null) {
			for (String p : mProperties) {
				if (p.equals("cover-image")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * このページの表示位置を取得
	 * 
	 * @return
	 */
	public PageSpread getPageSpread() {
		if (mSpineProperties != null) {
			for (String p : mSpineProperties) {
				if (p.equals(PageSpread.LEFT.getValue())) {
					return PageSpread.LEFT;
				} else if (p.equals(PageSpread.RIGHT.getValue())) {
					return PageSpread.RIGHT;
				}
			}
		}
		return PageSpread.NONE;
	}

	public OpfItem getFallback() {
		return mFallback;
	}

	public void setFallback(OpfItem mFallback) {
		this.mFallback = mFallback;
	}
}
