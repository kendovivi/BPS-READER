package jp.bpsinc.android.viewer.epub.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.util.StringUtil;

public class EpubZipFile implements EpubSource {
	/** ZIPファイル */
	private ZipFile mZipFile;
	/** キー：エントリ名、値：エントリ名に対応するZipEntry */
	private HashMap<String, ZipEntry> mZipEntries = new HashMap<String, ZipEntry>();

	public EpubZipFile(String path) throws FileNotFoundException, ZipException, EpubOtherException {
		File filePath = new File(path);
		if (filePath.canRead() == false) {
			throw new FileNotFoundException(filePath.getPath() + " is not found");
		}
		try {
			mZipFile = new ZipFile(filePath);
		} catch (ZipException e) {
			throw e;
		} catch (IOException e) {
			throw new EpubOtherException("zip file i/o error", e);
		}
		ZipEntry zipEntry = null;
		Enumeration<? extends ZipEntry> entries = mZipFile.entries();
		while (entries.hasMoreElements()) {
			zipEntry = entries.nextElement();

			// ディレクトリは無視
			if (zipEntry.isDirectory() == false) {
				mZipEntries.put(zipEntry.getName(), zipEntry);
			}
		}
	}

	@Override
	public void close() throws EpubOtherException {
		if (isClosed() == false) {
			try {
				mZipFile.close();
			} catch (IOException e) {
				throw new EpubOtherException("Failed to close ZipFile", e);
			}
		}
		mZipFile = null;
	}

	@Override
	public boolean isClosed() {
		return mZipFile == null;
	}

	@Override
	public byte[] getFileContents(String entryName) throws EpubOtherException {
		LogUtil.v("entryName = %s", entryName);
		if (isClosed()) {
			throw new EpubOtherException("ZipFile is closed");
		}
		entryName = StringUtil.trimHeadSlash(entryName);

		InputStream is = null;
		try {
			if (mZipEntries.containsKey(entryName)) {
				ZipEntry entry = mZipEntries.get(entryName);
				is = mZipFile.getInputStream(entry);

				int entrySize = (int)entry.getSize();
				byte[] buf = new byte[entrySize];

				int c;
				int total = 0;
				while ((c = is.read(buf, total, entrySize - total)) != -1) {
					total += c;
					if (total >= entrySize) {
						break;
					}
				}
				return buf;
			}
		} catch (IOException e) {
			LogUtil.e(e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				LogUtil.e(e);
			}
		}
		return null;
	}

	@Override
	public long getFileSize(String entryName) {
		LogUtil.v("entryName = %s", entryName);
		entryName = StringUtil.trimHeadSlash(entryName);

		if (mZipEntries.containsKey(entryName)) {
			return mZipEntries.get(entryName).getSize();
		}
		return -1l;
	}

	@Override
	public InputStream getInputStream(String entryName) throws EpubOtherException {
		LogUtil.v("entryName = %s", entryName);
		if (mZipFile == null) {
			throw new EpubOtherException("ZipFile is closed");
		}
		entryName = StringUtil.trimHeadSlash(entryName);

		try {
			if (mZipEntries.containsKey(entryName)) {
				return mZipFile.getInputStream(mZipEntries.get(entryName));
			}
		} catch (IOException e) {
			LogUtil.e(e);
		}
		return null;
	}

	@Override
	public boolean hasFile(String entryName) {
		LogUtil.v("entryName = %s", entryName);
		return mZipEntries.containsKey(StringUtil.trimHeadSlash(entryName));
	}
}
