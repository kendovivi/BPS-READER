package jp.bpsinc.android.viewer.exception;

public class LoadImageException extends Exception {
	private static final long serialVersionUID = -4183851297911815914L;

	public LoadImageException(String message) {
		super(message);
	}

	public LoadImageException(String message, Throwable ex) {
		super(message, ex);
	}
}
