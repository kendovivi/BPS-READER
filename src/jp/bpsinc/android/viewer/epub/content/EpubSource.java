package jp.bpsinc.android.viewer.epub.content;

import java.io.InputStream;

import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;

/**
 * EPUBの実ファイル（ZIPまたはDRMパッケージになっている）
 * 
 */
public interface EpubSource {
	/**
	 * EPUBをクローズする
	 * 
	 * @throws EpubOtherException クローズ処理に失敗
	 */
	public void close() throws EpubOtherException;

	/**
	 * EPUBがクローズしているか判定
	 * 
	 * @return クローズしていたらtrue、クローズしていないならfalse
	 */
	public boolean isClosed();

	/**
	 * 指定ファイルの中身をバイト列で取得
	 * 
	 * @param entryName エントリ名
	 * @return
	 * @throws EpubOtherException クローズしている
	 */
	public byte[] getFileContents(String entryName) throws EpubOtherException;

	/**
	 * 指定ファイルのサイズを取得
	 * 
	 * @param entryName エントリ名
	 * @return
	 * @throws EpubOtherException クローズしている
	 */
	public long getFileSize(String entryName);

	/**
	 * 指定ファイルのInputStreamを取得
	 * 
	 * @param entryName エントリ名
	 * @return
	 * @throws EpubOtherException クローズしている
	 */
	public InputStream getInputStream(String entryName) throws EpubOtherException;

	/**
	 * 指定したファイルが存在するかどうかチェック
	 * 
	 * @param entryName エントリ名
	 * @return
	 * @throws EpubOtherException クローズしている
	 */
	public boolean hasFile(String entryName);
}
