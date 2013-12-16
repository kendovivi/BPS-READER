package jp.bpsinc.android.viewer.epub.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.activity.EpubViewerActivity;
import jp.bpsinc.android.viewer.epub.content.EpubPageAccess;
import jp.bpsinc.android.viewer.epub.content.PageOperation;
import jp.bpsinc.android.viewer.epub.content.PageOperation.LoadQuality;
import jp.bpsinc.android.viewer.epub.util.PageLoadThread.PageLoadCompleteListener;

public class PageLoadManager {
	private EpubViewerActivity mEpubViewerActivity;

	private EpubPageAccess mEpubPageAccess;

	private PageLoadCompleteListener mCompleteListener;

	private Map<PageOperation, PageLoadThread> mPageLoadThreadMap = new HashMap<PageOperation, PageLoadThread>();

	private ThreadPoolExecutor mExecutor;

	public PageLoadManager(EpubViewerActivity epubViewerActivity, EpubPageAccess epubPageAccess, PageLoadCompleteListener listener) {
		mEpubViewerActivity = epubViewerActivity;
		mEpubPageAccess = epubPageAccess;
		mCompleteListener = listener;
		mExecutor = newThreadPoolExecutor();
	}

	private ThreadPoolExecutor newThreadPoolExecutor() {
		return new ThreadPoolExecutor(1, 1, 3000L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public void addPageLoadThread(PageOperation pageOperation, LoadQuality loadQuality) {
		LogUtil.v();
		stopPageLoadThread(pageOperation);
		PageLoadThread thread = new PageLoadThread(mEpubViewerActivity, mEpubPageAccess, pageOperation, mCompleteListener, loadQuality);
		mExecutor.execute(thread);
		mPageLoadThreadMap.put(pageOperation, thread);
	}

	public void stopPageLoadThread(PageOperation pageOperation) {
		LogUtil.v();
		PageLoadThread pageLoadThread = mPageLoadThreadMap.get(pageOperation);
		if (pageLoadThread != null) {
			mExecutor.remove(pageLoadThread);
			pageLoadThread.halt();
			mPageLoadThreadMap.remove(pageOperation);
		}
	}

	public void stopAllTasks() {
		LogUtil.v();
		try {
			for (PageLoadThread th : mPageLoadThreadMap.values()) {
				th.halt();
			}
			mExecutor.shutdown();
			if (mExecutor.awaitTermination(10, TimeUnit.SECONDS) == false) {
				mExecutor.shutdownNow();
			}
		} catch (InterruptedException e) {
			LogUtil.w("PageImageLoadManager shutdownNow!!", e);
			mExecutor.shutdownNow();
		}
		mExecutor = newThreadPoolExecutor();
		mPageLoadThreadMap.clear();
	}
}
