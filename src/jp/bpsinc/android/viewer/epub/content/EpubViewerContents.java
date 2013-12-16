package jp.bpsinc.android.viewer.epub.content;

import java.io.Serializable;

@SuppressWarnings("serial")
public class EpubViewerContents implements Serializable {
	private String mPath;

	public String getPath() {
		return mPath;
	}

	public void setPath(String mPath) {
		this.mPath = mPath;
	}
}
