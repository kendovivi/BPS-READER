package jp.bpsinc.android.viewer.epub.exception;

public class EpubParseException extends Exception {
	private static final long serialVersionUID = -5652550386071503273L;

	public EpubParseException(String message) {
		super(message);
	}

	public EpubParseException(String message, Throwable ex) {
		super(message, ex);
	}
}
