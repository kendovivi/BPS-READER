package jp.bpsinc.android.viewer.epub.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jp.bpsinc.android.epub.info.OpfItem;
import jp.bpsinc.android.epub.info.PageDirection;
import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;
import jp.bpsinc.util.StringUtil;
import jp.bpsinc.util.XmlUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class EpubPageAccess {
	/** EPUBの各ファイルアクセスに使用 */
	private EpubSource mEpubSource;
	/** EPUB内のOPFファイルが存在するディレクトリ */
	private String mOpfDir;
	/** 単一ページ時のページ順 */
	protected ArrayList<Page> mSinglePages;
	/** 見開きページ時のページ順 */
	protected ArrayList<Page> mSpreadPages;
	/** ページめくり（RTL, LTR）方向 */
	private PageDirection mPageDirection;

	public EpubPageAccess(EpubSource epubSource, EpubFile epubFile) throws EpubOtherException {
		if (epubSource == null || epubSource.isClosed()) {
			throw new EpubOtherException("EpubSource is not open");
		}
		if (epubFile == null) {
			throw new EpubOtherException("EpubFile is not null");
		}
		mEpubSource = epubSource;
		mOpfDir = epubFile.getOpfDir();
		mSinglePages = epubFile.getSinglePages();
		mSpreadPages = epubFile.getSpreadPages();
		mPageDirection = epubFile.getPageDirection();
	}

	public void close() throws EpubOtherException {
		mEpubSource.close();
	}

	public InputStream getInputStream(OpfItem opfItem) throws EpubParseException, EpubOtherException {
		String uri = getImagePath(opfItem);
		String fileName = uriToFilename(uri);
		LogUtil.v("uri: %s, filename=%s", uri, fileName);
		return mEpubSource.getInputStream(fileName);
	}

	/**
	 * 設定に対応して正しい並び順に変更した単ページリストを返す。
	 * 
	 * @return 設定に対応したページ順に並び替えられた単ページリスト
	 */
	public abstract ArrayList<Page> getSinglePageList();

	/**
	 * 設定に対応して正しい並び順に変更した見開きページリストを返す。
	 * 
	 * @return 設定に対応したページ順に並び替えられた見開きページリスト
	 */
	public abstract ArrayList<Page> getSpreadPageList();

	/**
	 * 見開きページインデックスを単一ページインデックスに変換
	 * 
	 * @param index 見開きページインデックスを単一ページインデックスに変換した値、index < 0の場合は0、index >= mSpreadPages.size()の場合はmSinglePages.size() - 1
	 * @return
	 */
	public int getSingleIndexFromSpreadIndex(int index) {
		if (index < 0) {
			return 0;
		} else if (index >= mSpreadPages.size()) {
			return mSinglePages.size() - 1;
		}
		return mSpreadPages.get(index).getThisPage();
	}

	/**
	 * 単一ページインデックスを見開きページインデックスに変換
	 * 
	 * @param index 単一ページインデックスを見開きページインデックスに変換した値、index < 0の場合は0、index >= mSinglePages.size()の場合はmSpreadPages.size() - 1、<br>
	 *              mSpreadPages内に存在するPageオブジェクトを全て調べてもindex以上の値が見つからない場合はmSpreadPages.size() - 1
	 * @return
	 */
	public int getSpreadIndexFromSingleIndex(int index) {
		if (index < 0) {
			return 0;
		} else if (index >= mSinglePages.size()) {
			return mSpreadPages.size() - 1;
		}
		int spreadIndex = 0;
		for (Page page : mSpreadPages) {
			if (page.getLeftOpfItem() != null && page.getRightOpfItem() != null) {
				if (index <= page.getThisPage() + 1) {
					return spreadIndex;
				}
			} else {
				if (index <= page.getThisPage()) {
					return spreadIndex;
				}
			}
			spreadIndex++;
		}
		// 存在しない場合は最終ページのインデックスを返す
		return mSpreadPages.size() - 1;
	}

	public boolean isRTL() {
		return mPageDirection == PageDirection.RTL;
	}

	private byte[] getFileContents(String uri) throws EpubOtherException {
		String fileName = uriToFilename(uri);
		LogUtil.v("uri: %s, filename=%s", uri, fileName);
		return mEpubSource.getFileContents(fileName);
	}

	private String getImagePath(OpfItem item) throws EpubParseException, EpubOtherException {
		if (item == null) {
			return null;
		}

		if (item.isXhtml()) {
			String path;
			if ((path = getImagePathByBbook(item)) != null) {
				return path;
			}
			if ((path = getImagePathBySvg(item)) != null) {
				return path;
			}
			if ((path = getImagePathByImg(item)) != null) {
				return path;
			}
		} else if (item.isImage()) {
			return item.getHref();
		}
		return getImagePath(item.getFallback());
	}

	private String getImagePathByBbook(OpfItem item) throws EpubParseException, EpubOtherException {
		try {
			XmlPullParser parser = XmlUtil.newPullParser(getFileContents(item.getHref()));
			int event = parser.getEventType();

			while (event != XmlPullParser.END_DOCUMENT) {
				if (event == XmlPullParser.START_TAG) {
					if (parser.getName().equalsIgnoreCase("meta")) {
						String name = parser.getAttributeValue(null, "name");
						String content = parser.getAttributeValue(null, "content");
						if (name != null && name.equals("bbook-page-image")) {
							if (content != null && content.length() > 0) {
								return xmlBuildPath(content, item.getHref());
							}
						}
					}
				}
				event = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new EpubParseException("epub parse error", e);
		} catch (IOException e) {
			throw new EpubOtherException("epub parse i/o error", e);
		}
		return null;
	}

	private String getImagePathBySvg(OpfItem item) throws EpubParseException, EpubOtherException {
		try {
			XmlPullParser parser = XmlUtil.newPullParser(getFileContents(item.getHref()));
			int event = parser.getEventType();

			boolean svg = false;
			while (event != XmlPullParser.END_DOCUMENT) {
				if (event == XmlPullParser.START_TAG) {
					if (svg && parser.getName().equalsIgnoreCase("image")) {
						String href = parser.getAttributeValue("http://www.w3.org/1999/xlink", "href");
						if (href != null && href.length() > 0) {
							return xmlBuildPath(href, item.getHref());
						}
					} else if (parser.getName().equalsIgnoreCase("svg")) {
						svg = true;
					}
				} else if (event == XmlPullParser.END_TAG) {
					if (parser.getName().equalsIgnoreCase("svg")) {
						svg = false;
					}
				}
				event = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new EpubParseException("epub parse error", e);
		} catch (IOException e) {
			throw new EpubOtherException("epub parse i/o error", e);
		}
		return null;
	}

	private String getImagePathByImg(OpfItem item) throws EpubParseException, EpubOtherException {
		try {
			XmlPullParser parser = XmlUtil.newPullParser(getFileContents(item.getHref()));
			int event = parser.getEventType();

			while (event != XmlPullParser.END_DOCUMENT) {
				if (event == XmlPullParser.START_TAG) {
					if (parser.getName().equalsIgnoreCase("img")) {
						String href = parser.getAttributeValue(null, "src");
						if (href != null && href.length() > 0) {
							return xmlBuildPath(href, item.getHref());
						}
					}
				}
				event = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new EpubParseException("epub parse error", e);
		} catch (IOException e) {
			throw new EpubOtherException("epub parse i/o error", e);
		}
		return null;
	}

	private String xmlBuildPath(String href, String base) {
		if (base == null) {
			return href;
		}

		if (base.contains("/")) {
			base = base.substring(0, base.lastIndexOf("/"));
		} else {
			base = "";
		}

		while (href.startsWith("../")) {
			href = href.substring(3);
			if (base.contains("/")) {
				base = base.substring(0, base.lastIndexOf("/"));
			} else {
				base = "";
			}
		}

		if (base.length() == 0) {
			return href;
		} else {
			return base + "/" + href;
		}
	}

	private String uriToFilename(String uri) {
		String result = mOpfDir + StringUtil.trimUri(uri);
		LogUtil.v("%s => %s", uri, result);
		return result;
	}
}
