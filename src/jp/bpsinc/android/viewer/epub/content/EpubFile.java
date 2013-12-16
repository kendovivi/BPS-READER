package jp.bpsinc.android.viewer.epub.content;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipException;

import jp.bpsinc.android.epub.info.OpfItem;
import jp.bpsinc.android.epub.info.OpfMeta;
import jp.bpsinc.android.epub.info.PageDirection;
import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;
import jp.bpsinc.util.StringUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

@SuppressWarnings("serial")
public class EpubFile implements Serializable {
	private static final String CONTAINER_FILENAME = "META-INF/container.xml";

	public class NavPoint implements Serializable {
		public int order = -1;
		public OpfItem item = null;
		public String title = null;
	}

	/** OPFファイルのパス */
	private String mOpfPath;

	/** OPFファイルのあるディレクトリパス */
	private String mOpfDir;

	/** OPFファイルに含まれるメタデータ */
	private OpfMeta mOpfMeta;

	/** ページめくり（RTL, LTR）方向 */
	private PageDirection mPageDirection;

	/** すべてのitem要素（IDとitemのkey-value） */
	private HashMap<String, OpfItem> mItems;

	/** 単一ページ時のページ順 */
	private ArrayList<Page> mSpineSinglePages;

	/** 見開きページ時のページ順 */
	private ArrayList<Page> mSpineSpreadPages;

	private List<NavPoint> mNavMap = new ArrayList<NavPoint>();

	public EpubFile(EpubSource epubSource, String priorityOpfName) throws ZipException, EpubParseException, EpubOtherException {
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

	public List<NavPoint> getNavMap() {
		return mNavMap;
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

	private boolean isRTL() {
		return getPageDirection() == PageDirection.RTL;
	}

	public PageDirection getPageDirection() {
		return mPageDirection;
	}

	public OpfMeta getOpfMeta() {
		return mOpfMeta;
	}

	public OpfItem getItemById(String id) {
		return mItems.get(id);
	}

	public ArrayList<Page> getSinglePages() {
		return mSpineSinglePages;
	}

	public ArrayList<Page> getSpreadPages() {
		return mSpineSpreadPages;
	}

	/**
	 * itemに記載されているhrefは、OPFファイルからの相対パスになっているので、OPFのディレクトリ分を追加して返す
	 * 
	 * @param item
	 * @return
	 */
	private String getFileName(OpfItem item) {
		String href = item.getHref();
		int index = href.indexOf('#');
		if (index != -1) {
			href = href.substring(0, index);
		}
		return mOpfDir + File.separator + href;
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
		mPageDirection = PageDirection.DEFAULT;
		List<OpfItem> spineItems = new ArrayList<OpfItem>();
		HashMap<OpfItem, String> fallbackMap = new HashMap<OpfItem, String>();
		String tocId = null;

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

						String fallback = parser.getAttributeValue(null, OpfItem.ATT_FALLBACK);
						if (fallback != null && fallback.length() > 0) {
							fallbackMap.put(item, fallback);
						}
					} else if (tag.equals("spine")) {
						String direction = parser.getAttributeValue(null, "page-progression-direction");
						tocId = parser.getAttributeValue(null, "toc");

						mPageDirection = PageDirection.parse(direction);
					} else if (tag.equals("itemref")) {
						String properties = parser.getAttributeValue(null, "properties");
						String idref = parser.getAttributeValue(null, "idref");

						if (mItems.containsKey(idref)) {
							OpfItem it = mItems.get(idref);
							it.setSpineProperties(properties);
							spineItems.add(it);
						} else {
							throw new EpubOtherException("Spine item " + idref + " is not found");
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

		for (OpfItem item : fallbackMap.keySet()) {
			item.setFallback(mItems.get(fallbackMap.get(item)));
		}

		// 単一ページ・見開きページを解析
		setSpinePagesFromItems(spineItems);

		// 目次を生成
		for (String key : mItems.keySet()) {
			OpfItem item = mItems.get(key);
			if (item.isNav()) {
				parseNav(epubSource, item);
			}
		}
		if (mNavMap.isEmpty() && tocId != null && mItems.containsKey(tocId)) {
			String tocxml = StringUtil.trimBOM(new String(epubSource.getFileContents(getFileName(mItems.get(tocId)))));
			parseToc(tocxml);
		}

		if (mItems.size() > 0 && mSpineSinglePages.size() > 0 && mSpineSpreadPages.size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * spineに記載されたitemrefをもとに、単一ページと見開きページで表示すべき単位にまとめる
	 * 
	 * @param items
	 * @exception EpubParseException
	 * @throws EpubOtherException 
	 */
	private void setSpinePagesFromItems(List<OpfItem> items) throws EpubParseException, EpubOtherException {
		mSpineSinglePages = new ArrayList<Page>();
		mSpineSpreadPages = new ArrayList<Page>();

		// 単一ページはそのままspineの内容を入れれば良い
		int currentPage = 0;
		for (OpfItem item : items) {
			mSpineSinglePages.add(new Page(item, currentPage));
			currentPage++;
		}

		// 見開きページは、左右でまとめる
		OpfItem tmpLeft = null;
		OpfItem tmpRight = null;
		currentPage = 0;
		for (OpfItem item : items) {
			switch (item.getPageSpread()) {
			case NONE:
				if (tmpLeft != null || tmpRight != null) {
					mSpineSpreadPages.add(new Page(tmpLeft, tmpRight, currentPage));
					currentPage++;
				}
				mSpineSpreadPages.add(new Page(item, currentPage));
				currentPage++;
				tmpLeft = null;
				tmpRight = null;
				break;
			case LEFT:
				if (isRTL()) {
					mSpineSpreadPages.add(new Page(item, tmpRight, currentPage));
					if (tmpRight != null) {
						currentPage++;
					}
					currentPage++;
					tmpRight = null;
				} else {
					if (tmpLeft != null) {
						mSpineSpreadPages.add(new Page(tmpLeft, null, currentPage));
						currentPage++;
					}
					tmpLeft = item;
				}
				break;
			case RIGHT:
				if (isRTL()) {
					if (tmpRight != null) {
						mSpineSpreadPages.add(new Page(null, tmpRight, currentPage));
						currentPage++;
					}
					tmpRight = item;
				} else {
					mSpineSpreadPages.add(new Page(tmpLeft, item, currentPage));
					if (tmpLeft != null) {
						currentPage++;
					}
					currentPage++;
					tmpLeft = null;
				}
				break;
			}
		}
		if (tmpLeft != null || tmpRight != null) {
			mSpineSpreadPages.add(new Page(tmpLeft, tmpRight, currentPage));
		}
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

	/**
	 * toc.ncx(EPUB2)をもとに目次を生成
	 * 
	 * @param xml
	 * @throws EpubParseException
	 * @throws EpubOtherException
	 */
	private void parseToc(String xml) throws EpubParseException, EpubOtherException {
		LogUtil.v();
		XmlPullParser parser = Xml.newPullParser();

		try {
			parser.setInput(new StringReader(xml));

			NavPoint nav = null;
			for (int event = parser.getEventType(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
				if (event == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					if (tagName.equals("navPoint")) {
						nav = new NavPoint();
						nav.order = Integer.parseInt(parser.getAttributeValue(null, "playOrder"));
					} else if (tagName.equals("text") && nav != null) {
						nav.title = parser.nextText();
					} else if (tagName.equals("content")) {
						String filename = parser.getAttributeValue(null, "src");
						nav.item = getItemFromUrl(filename);
					}
				} else if (event == XmlPullParser.END_TAG) {
					String tagName = parser.getName();
					if (tagName.equals("navPoint")) {
						if (nav != null && nav.title != null && nav.item != null) {
							mNavMap.add(nav);
							LogUtil.v("navPoint added. Order:%d title:%s ref:%s",
									nav.order, nav.title, nav.item.getHref());
						}
						nav = null;
					}
				}
			}

			Collections.sort(mNavMap, new Comparator<NavPoint>() {
				@Override
				public int compare(NavPoint nav1, NavPoint nav2) {
					return nav1.order - nav2.order;
				}
			});
		} catch (XmlPullParserException e) {
			throw new EpubParseException("parser error", e);
		} catch (IOException e) {
			throw new EpubOtherException("i/o error", e);
		}
	}

	/**
	 * nav.xhtml(EPUB3)をもとに目次を生成
	 * 
	 * @param xml
	 * @throws EpubParseException
	 * @throws EpubOtherException
	 */
	private void parseNav(EpubSource epubSource, OpfItem navItem) throws EpubParseException, EpubOtherException {
		LogUtil.v();
		XmlPullParser parser = Xml.newPullParser();

		try {
			parser.setInput(new StringReader(StringUtil.trimBOM(new String(epubSource.getFileContents(getFileName(navItem))))));

			int event = parser.getEventType();
			boolean inNav = false;
			while (event != XmlPullParser.END_DOCUMENT) {
				if (event == XmlPullParser.START_TAG && parser.getName().equals("nav")) {
					inNav = true;
				} else if (event == XmlPullParser.END_TAG && parser.getName().equals("nav")) {
					inNav = false;
				}

				// <li class="chapter" id="toc0" data-item-id="page0">
				// <a>href="0.xhtml">見出しタイトル</a></li>
				if (inNav && event == XmlPullParser.START_TAG && parser.getName().equals("a")) {
					NavPoint nav = new NavPoint();
					String href = parser.getAttributeValue(null, "href");
					nav.order = mNavMap.size();
					nav.item = getItemFromUrl(navItem, href);
					nav.title = parser.nextText();
					if (nav.item != null) {
						mNavMap.add(nav);
						LogUtil.v("navPoint added. Order:%d title:%s ref:%s", nav.order, nav.title, nav.item.getHref());
					} else {
						LogUtil.w("parseNav item not found:%s", href);
					}
				}
				event = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new EpubParseException("parser error", e);
		} catch (IOException e) {
			throw new EpubOtherException("i/o error", e);
		}
	}

	private OpfItem getItemFromUrl(String uri) {
		String filename = uriToFilename(uri);
		for (String id : mItems.keySet()) {
			OpfItem item = mItems.get(id);
			String target = mOpfDir + File.separator + item.getHref();
			if (filename.equals(target)) {
				LogUtil.v("found! %s", target);
				return item;
			}
		}
		LogUtil.w("not found! %s", filename);
		return null;
	}

	/**
	 * URL(相対パス)からItemを取得
	 * 
	 * @param baseItem 基準になるItem
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private OpfItem getItemFromUrl(OpfItem baseItem, String url) {
		String path = new File(mOpfDir
				+ File.separator
				+ StringUtil.getParentPath(baseItem.getHref())
				+ StringUtil.trimUri(url)).getAbsolutePath();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		for (String id : mItems.keySet()) {
			OpfItem it = mItems.get(id);
			String target = mOpfDir + File.separator + it.getHref();
			if (path.equals(target)) {
				LogUtil.v("found! %s", target);
				return it;
			}
		}
		LogUtil.w("not found! %s", path);
		return null;
	}

	private String uriToFilename(String uri) {
		String result = mOpfDir + StringUtil.trimUri(uri);
		LogUtil.v("%s => %s", uri, result);
		return result;
	}
}
