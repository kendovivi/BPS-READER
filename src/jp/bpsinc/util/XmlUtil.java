package jp.bpsinc.util;

import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class XmlUtil {
	public static XmlPullParser newPullParser(String sourceXml)
			throws XmlPullParserException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(new StringReader(StringUtil.trimBOM(sourceXml)));
		return parser;
	}

	public static XmlPullParser newPullParser(byte[] sourceXml)
			throws XmlPullParserException {
		return newPullParser(new String(sourceXml));
	}
}
