package jp.bpsinc.android.util;

import com.example.bps_reader.BuildConfig;

import android.util.Log;

public class LogUtil {
	private static final int LOG_LEVEL = Log.VERBOSE;
	public static final String KARA = "";

	public static final void v() {
		if (BuildConfig.DEBUG && LOG_LEVEL <= Log.VERBOSE) {
			printLog(Log.VERBOSE, KARA, null);
		}
	}

	public static final void v(String msg, Object... args) {
		if (BuildConfig.DEBUG && LOG_LEVEL <= Log.VERBOSE) {
			printLog(Log.VERBOSE, String.format(msg, args), null);
		}
	}

	public static final void d() {
		if (BuildConfig.DEBUG && LOG_LEVEL <= Log.DEBUG) {
			printLog(Log.DEBUG, KARA, null);
		}
	}

	public static final void d(String msg, Object... args) {
		if (BuildConfig.DEBUG && LOG_LEVEL <= Log.DEBUG) {
			printLog(Log.DEBUG, String.format(msg, args), null);
		}
	}

	public static final void i() {
		if (BuildConfig.DEBUG && LOG_LEVEL <= Log.INFO) {
			printLog(Log.INFO, KARA, null);
		}
	}

	public static final void i(String msg, Object... args) {
		if (BuildConfig.DEBUG && LOG_LEVEL <= Log.INFO) {
			printLog(Log.INFO, String.format(msg, args), null);
		}
	}

	public static final void w(Throwable err) {
		if (BuildConfig.DEBUG && LOG_LEVEL <= Log.WARN) {
			printLog(Log.WARN, KARA, err);
		}
	}

	public static final void w(String msg, Object... args) {
		if (BuildConfig.DEBUG && LOG_LEVEL <= Log.WARN) {
			printLog(Log.WARN, String.format(msg, args), null);
		}
	}

	public static final void w(String msg, Throwable err, Object... args) {
		if (BuildConfig.DEBUG && LOG_LEVEL <= Log.WARN) {
			printLog(Log.WARN, String.format(msg, args), err);
		}
	}

	public static final void e(Throwable err) {
		if (BuildConfig.DEBUG && LOG_LEVEL <= Log.ERROR) {
			printLog(Log.ERROR, KARA, err);
		}
	}

	public static final void e(String msg, Object... args) {
		if (BuildConfig.DEBUG && LOG_LEVEL <= Log.ERROR) {
			printLog(Log.ERROR, String.format(msg, args), null);
		}
	}

	public static final void e(String msg, Throwable err, Object... args) {
		if (BuildConfig.DEBUG && LOG_LEVEL <= Log.ERROR) {
			printLog(Log.ERROR, String.format(msg, args), err);
		}
	}

	private static void printLog(int level, String msg, Throwable err) {
		StringBuilder outMsg = null;
		String tag = null;

		// スタックトレースの6番目を呼出し元情報として扱います。
		//   stack[2] : LogUtil.printLog()
		//   stack[3] : LogUtil.i()
		//   stack[4] : 呼出し元クラス.xxx()
		// stack[4]にあたるクラスが呼出し元情報として出力されます。
		StackTraceElement stack = Thread.currentThread().getStackTrace()[4];
		if (stack == null) {
			outMsg = new StringBuilder(KARA);
			tag = "FailedGetTag";
		} else {
			// Log format: ファイル名(行数)メソッド名:<log message>
			outMsg = new StringBuilder(stack.getFileName());
			outMsg.append("(")
					.append(stack.getLineNumber())
					.append(")")
					.append(stack.getMethodName())
					.append(":");
			tag = stack.getClassName();

			// パッケージ名消去
			int index = tag.lastIndexOf('.');
			if (index != -1) {
				tag = tag.substring(index + 1);
			}
		}
		outMsg.append(msg);
		if (err != null) {
			outMsg.append("\n");
			outMsg.append(Log.getStackTraceString(err));
		}
		Log.println(level, tag, outMsg.toString());
	}
}
