package jp.bpsinc.android.exception;

public class UnexpectedException extends Exception {
	private static final long serialVersionUID = -4191461609204939777L;

	public UnexpectedException(String message) {
		super(message);
	}

	public UnexpectedException(String message, Throwable ex) {
		super(message, ex);
	}
}
