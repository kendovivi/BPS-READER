package bps.android.reader.listadapter;

import java.io.IOException;
import java.io.InputStream;

import jp.bpsinc.android.epub.info.OpfItem;
import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.content.EpubSource;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;
import jp.bpsinc.util.StringUtil;
import jp.bpsinc.util.XmlUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class BookshelfEpubPageAccess {
	/** EPUBの各ファイルアクセスに使用 */
	private EpubSource mEpubSource;
	/** EPUB内のOPFファイルが存在するディレクトリ */
	private String mOpfDir;

	public BookshelfEpubPageAccess(EpubSource epubSource, BookshelfEpubFile epubFile) throws EpubOtherException {
		if (epubSource == null || epubSource.isClosed()) {
			throw new EpubOtherException("EpubSource is not open");
		}
		if (epubFile == null) {
			throw new EpubOtherException("EpubFile is not null");
		}
		mEpubSource = epubSource;
		mOpfDir = epubFile.getOpfDir();
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

	private byte[] getFileContents(String uri) throws EpubOtherException {
		String fileName = uriToFilename(uri);
		LogUtil.v("uri: %s, filename=%s", uri, fileName);
		return mEpubSource.getFileContents(fileName);
	}

	public String getImagePath(OpfItem item) throws EpubParseException, EpubOtherException {
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
