package jp.bpsinc.android.epub.info;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * EPUBのOPFファイルに含まれるメタデータを管理
 */
@SuppressWarnings("serial")
public class OpfMeta implements Serializable {
	/** EPUBバージョン */
	private String mVersion;

	/** packageのunique-identifierで指定されたIDの種類 */
	private String mPrimaryIdKey;

	/** IDの種類・値の一覧 */
	private HashMap<String, String> mIds;

	/** 出版物のタイプ */
	private String mPublicationType;

	/** タイトル */
	private String mTitle;

	/** 副題 */
	private String mSubject;

	/** 著者 */
	private String mAuthor;

	/** 言語 */
	private String mLanguage;

	/** 出版社 */
	private String mPublisher;

	/** OMFバージョン */
	private String mOmfVersion;

	public static final String TAG_METADATA = "metadata";
	public static final String TAG_PACKAGE = "package";
	public static final String TAG_IDENTIFIER = "identifier";
	public static final String TAG_TITLE = "title";
	public static final String TAG_LANGUAGE = "language";
	public static final String TAG_CREATOR = "creator";
	public static final String TAG_PUBLISHER = "publisher";
	public static final String TAG_SUBJECT = "subject";
	public static final String TAG_META = "meta";
	public static final String ATT_PACKAGE_VERSION = "version";
	public static final String ATT_PACKAGE_UNIQUE_ID = "unique-identifier";
	public static final String ATT_IDENTIFIER_ID = "id";
	public static final String ATT_META_PROPERTY = "property";
	public static final String ATT_META_ID = "id";

	/**
	 * OPFファイルを読み込み、メタデータを生成する
	 * 
	 * @param xml
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static OpfMeta parseOpf(String xml) throws EpubParseException, EpubOtherException {
		OpfMeta meta = null;
		try {
			meta = new OpfMeta(xml);
		} catch (XmlPullParserException e) {
			throw new EpubParseException("Opf meta parse error", e);
		} catch (IOException e) {
			throw new EpubOtherException("Opf meta parse i/o error", e);
		}
		if (!meta.isValid()) {
			LogUtil.w("OpfMeta is not valid");
		}
		return meta;
	}

	/**
	 * OPFファイルを読み込む
	 * 
	 * @param xml
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private OpfMeta(String xml) throws XmlPullParserException, IOException {
		mIds = new HashMap<String, String>();

		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(new StringReader(xml));

		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			String tag = parser.getName();
			if (event == XmlPullParser.END_TAG && TAG_METADATA.equals(tag)) {
				break; // <metadata>を読み終われば完了。<manifest>や<spine>は読む必要なし
			}
			if (event == XmlPullParser.START_TAG) {
				if (tag.equals(TAG_PACKAGE)) {
					mVersion = parser.getAttributeValue(null, ATT_PACKAGE_VERSION);
					mPrimaryIdKey = parser.getAttributeValue(null, ATT_PACKAGE_UNIQUE_ID);
				} else if (tag.equals(TAG_IDENTIFIER)) {
					String k = parser.getAttributeValue(null, ATT_IDENTIFIER_ID);
					String v = parser.nextText();
					LogUtil.v("ID(%s) : %s", k, v);
					mIds.put(k, v);
				} else if (tag.equals(TAG_TITLE)) {
					mTitle = parser.nextText();
				} else if (tag.equals(TAG_LANGUAGE)) {
					mLanguage = parser.nextText();
				} else if (tag.equals(TAG_CREATOR)) {
					mAuthor = parser.nextText();
				} else if (tag.equals(TAG_PUBLISHER)) {
					mPublisher = parser.nextText();
				} else if (tag.equals(TAG_SUBJECT)) {
					mSubject = parser.nextText();
				} else if (tag.equals(TAG_META)) {
					String property = parser.getAttributeValue(null, ATT_META_PROPERTY);
					String id = parser.getAttributeValue(null, ATT_META_ID);
					String value = parser.nextText();
					if ("dcterms:creator".equals(property)) {
						mIds.put(id, value);
					} else if ("publication-type".equals(property)) {
						mPublicationType = value;
					} else if ("dcterms:publisher".equals(property)) {
						mPublisher = value;
					} else if ("omf:version".equals(property)) {
						mOmfVersion = value;
					}
				}
			}
			event = parser.next();
		}
	}

	/**
	 * 簡易validation
	 * 
	 * @return
	 */
	public boolean isValid() {
		return mVersion != null && mPrimaryIdKey != null && mIds != null
				&& mIds.containsKey(mPrimaryIdKey) && mTitle != null;
	}

	public String getId() {
		return mIds.get(mPrimaryIdKey);
	}

	public HashMap<String, String> getIds() {
		return mIds;
	}

	public String getVersion() {
		return mVersion;
	}

	public String getPublicationType() {
		return mPublicationType;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getSubject() {
		return mSubject;
	}

	public String getAuthor() {
		return mAuthor;
	}

	public String getPublisher() {
		return mPublisher;
	}

	public String getLanguage() {
		return mLanguage;
	}

	public String getPrimaryIdKey() {
		return mPrimaryIdKey;
	}

	public String getOmfVersion() {
		return mOmfVersion;
	}
}
