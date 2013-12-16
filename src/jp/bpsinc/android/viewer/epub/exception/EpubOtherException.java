package jp.bpsinc.android.viewer.epub.exception;

public class EpubOtherException extends Exception {
	private static final long serialVersionUID = -9128641473529566918L;

	public EpubOtherException(String message) {
		super(message);
	}

	public EpubOtherException(String message, Throwable ex) {
		super(message, ex);
	}
}
