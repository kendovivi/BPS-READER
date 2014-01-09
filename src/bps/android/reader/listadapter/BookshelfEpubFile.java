package bps.android.reader.listadapter;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipException;

import jp.bpsinc.android.epub.info.OpfItem;
import jp.bpsinc.android.epub.info.OpfMeta;
import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.content.EpubSource;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;
import jp.bpsinc.util.StringUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

@SuppressWarnings("serial")
public class BookshelfEpubFile implements Serializable {
	private static final String CONTAINER_FILENAME = "META-INF/container.xml";

	/** OPFファイルのパス */
	private String mOpfPath;

	/** OPFファイルのあるディレクトリパス */
	private String mOpfDir;

	/** OPFファイルに含まれるメタデータ */
	private OpfMeta mOpfMeta;

	/** すべてのitem要素（IDとitemのkey-value） */
	private HashMap<String, OpfItem> mItems;

	/** カバー画像のitem要素 */
	private OpfItem mCoverItem;

	public BookshelfEpubFile(EpubSource epubSource, String priorityOpfName) throws ZipException, EpubParseException, EpubOtherException {
		if (epubSource == null || epubSource.isClosed()) {
			throw new EpubOtherException("EpubSource is not open");
		}
		open(epubSource, priorityOpfName);
	}

	private void open(EpubSource epubSource, String priorityOpfName) throws ZipException, EpubParseException, EpubOtherException {
		// container.xmlを読んで、OPFパスを取得
		byte[] byteContainer = epubSource.getFileContents(CONTAINER_FILENAME);
		if (byteContainer == null) {
			throw new EpubOtherException("Cannot read " + CONTAINER_FILENAME);
		}
		mOpfPath = getOpfPath(StringUtil.trimBOM(new String(byteContainer)), priorityOpfName);
		if (mOpfPath == null) {
			throw new EpubParseException("Cannot resolve opf file path. Maybe this file is not a EPUB file");
		}
		mOpfDir = StringUtil.getParentPath(mOpfPath);

		// OPFを読み込む
		byte[] byteOpf = epubSource.getFileContents(mOpfPath);
		if (byteOpf == null) {
			throw new EpubOtherException("Cannot read opf file.");
		}
		String opfXml = StringUtil.trimBOM(new String(byteOpf));
		mOpfMeta = OpfMeta.parseOpf(opfXml);
		if (mOpfMeta == null) {
			throw new EpubParseException("OPF MetaData is invalid");
		}
		if (parseOpf(epubSource, opfXml) == false) {
			throw new EpubOtherException("Failed to parse OPF item/itemref");
		}
	}

	public String getOpfDir() {
		return mOpfDir;
	}

	public String getId() {
		return mOpfMeta.getId();
	}

	public String getTitle() {
		return mOpfMeta.getTitle();
	}

	public String getAuthor() {
		return mOpfMeta.getAuthor();
	}

	public OpfMeta getOpfMeta() {
		return mOpfMeta;
	}

	public OpfItem getCoverItem() {
		return mCoverItem;
	}

	/**
	 * OPFファイルから、itemとitemrefの要素を読み込む
	 * 
	 * @param opfXml
	 * @return
	 * @throws EpubParseException
	 * @throws EpubOtherException
	 */
	private boolean parseOpf(EpubSource epubSource, String opfXml) throws EpubParseException, EpubOtherException {
		mItems = new HashMap<String, OpfItem>();
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(new StringReader(opfXml));
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				if (event == XmlPullParser.START_TAG) {
					String tag = parser.getName();
					if (tag.equals("item")) {
						String id = parser.getAttributeValue(null, OpfItem.ATT_ID);
						String href = parser.getAttributeValue(null, OpfItem.ATT_HREF);
						String mediaType = parser.getAttributeValue(null, OpfItem.ATT_MEDIA_TYPE);
						String properties = parser.getAttributeValue(null, OpfItem.ATT_PROPERTIES);

						OpfItem item = new OpfItem(id, href, mediaType, properties);
						mItems.put(id, item);
						String idref = parser.getAttributeValue(null, "idref");
						mCoverItem = mItems.get(idref);
						if (item.isCoverImage()) {
							// カバー見つかったらすぐ終了
							mCoverItem = item;
							break;
						}
					} else if (tag.equals("itemref")) {
						String idref = parser.getAttributeValue(null, "idref");

						if (mItems.containsKey(idref)) {
							// カバーない場合は1枚目をカバーの変わりにする
							mCoverItem = mItems.get(idref);
							break;
						}
					}
				}
				event = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new EpubParseException("parser error", e);
		} catch (IOException e) {
			throw new EpubOtherException("i/o error", e);
		}
		return true;
	}

	/**
	 * META-INF/container.xmlを読み、適切なOPFファイルのパスを返す
	 * 
	 * @param containerXml
	 * @param priorityName 指定すると、この名前のファイルを優先的に使う。nullの場合は先頭のものを使う
	 * @return
	 * @throws EpubParseException
	 * @throws EpubOtherException
	 */
	private static String getOpfPath(String containerXml, String priorityName) throws EpubParseException, EpubOtherException {
		List<String> paths = new ArrayList<String>();
		XmlPullParser parser = Xml.newPullParser();

		try {
			parser.setInput(new StringReader(containerXml));

			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG && parser.getName().equals("rootfile")) {
					String fullpath = parser.getAttributeValue(null, "full-path");
					LogUtil.v("opf found %s", fullpath);
					if (priorityName == null) {
						return fullpath;
					}
					paths.add(fullpath);
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new EpubParseException("parser error", e);
		} catch (IOException e) {
			throw new EpubOtherException("i/o error", e);
		}

		for (String path : paths) {
			if (path.contains(priorityName)) {
				LogUtil.v("OPF path =%s", path);
				return path;
			}
		}
		if (paths.size() > 0) {
			String path = paths.get(0);
			LogUtil.v("OPF path =%s", path);
			return path;
		}
		return null;
	}
}
