package jp.bpsinc.android.viewer.epub.util;

import java.io.InputStream;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.activity.EpubViewerActivity;
import jp.bpsinc.android.viewer.epub.content.EpubPageAccess;
import jp.bpsinc.android.viewer.epub.content.PageOperation;
import jp.bpsinc.android.viewer.epub.content.PageOperation.OnBeforeReplaceHandler;
import jp.bpsinc.android.viewer.epub.dialog.EpubViewerDialog;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;
import jp.bpsinc.android.viewer.exception.LoadImageException;
import android.os.Handler;

public class PageLoadThread implements Runnable {
	public interface PageLoadCompleteListener {
		public void onLoadComplete(PageOperation pageOperation);
	}

	private EpubViewerActivity mEpubViewerActivity;

	private PageOperation mPageOperation;

	private EpubPageAccess mEpubPageAccess;

	private PageLoadCompleteListener mCompleteListener;

	private PageOperation.LoadQuality mLoadQuality;

	private Handler mHandler;

	private boolean mIsHalt;

	public PageLoadThread(EpubViewerActivity epubViewerActivity, EpubPageAccess epubPageAccess,
			PageOperation pageOperation, PageLoadCompleteListener callback, PageOperation.LoadQuality loadQuality) {
		mEpubViewerActivity = epubViewerActivity;
		mEpubPageAccess = epubPageAccess;
		mPageOperation = pageOperation;
		mCompleteListener = callback;
		mLoadQuality = loadQuality;
		mHandler = new Handler();
		mIsHalt = false;
	}

	public boolean shouldAbort() {
		return mIsHalt || mPageOperation == null;
	}

    @Override
    public void run() {
		LogUtil.d("ImageLoadThread run loadType=%s page=%s", mLoadQuality.name(), mPageOperation.getPage().toString());
		if (shouldAbort()) {
			return;
		}

		OnBeforeReplaceHandler beforeUpdate = new OnBeforeReplaceHandler() {
			@Override
			public boolean onBeforeReplace() {
				if (shouldAbort()) {
					return false;
				}
				if (mPageOperation.canReplaceToQuality(mLoadQuality) == false) {
					// すでに同高解像度なので、読み込む必要なし
					LogUtil.i("This page is already high quality. skip.");
					return false;
				}
				return true;
			}
		};

		try {
			if (mPageOperation.canReplaceToQuality(mLoadQuality) == false) {
				// すでに同高解像度なので、読み込む必要なし
				LogUtil.i("This page is already high quality. skip.");
				return;
			}

			if (mPageOperation.isPageSpread() == false) {
				InputStream is = getCenterInputStream();
				if (is == null) {
					throw new LoadImageException("center input stream is null");
				} else {
					if (shouldAbort()) {
						return;
					}
					mPageOperation.detectBitmapSize(is);
					is = getCenterInputStream();

					if (shouldAbort()) {
						return;
					}
					if (mLoadQuality == PageOperation.LoadQuality.HIGH) {
						mPageOperation.replaceToOriginalSize(is, beforeUpdate);
					} else {
						mPageOperation.replaceToDisplayFitSize(is, beforeUpdate);
					}
				}
			} else {
				InputStream lis = getLeftInputStream();
				InputStream ris = getRightInputStream();
				if (lis == null && ris == null) {
					throw new LoadImageException("left and right input stream is null");
				} else {
					if (shouldAbort()) {
						return;
					}
					mPageOperation.detectBitmapSize(lis, ris);
					lis = getLeftInputStream();
					ris = getRightInputStream();
					if (shouldAbort()) {
						return;
					}
					if (mLoadQuality == PageOperation.LoadQuality.HIGH) {
						mPageOperation.replaceToOriginalSize(lis, ris, beforeUpdate);
					} else {
						mPageOperation.replaceToDisplayFitSize(lis, ris, beforeUpdate);
					}
				}
			}
			if (shouldAbort()) {
				return;
			}
			mPageOperation.onChangeBitmap();
			if (mCompleteListener != null) {
				mCompleteListener.onLoadComplete(mPageOperation);
			}
		} catch (RuntimeException e) {
			LogUtil.e("unexpected error", e);
			postShowDialog(EpubViewerDialog.ID_UNEXPECTED_ERR);
		} catch (EpubParseException e) {
			LogUtil.e("epub parser error", e);
			postShowDialog(EpubViewerDialog.ID_EPUB_PARSER_ERR);
		} catch (EpubOtherException e) {
			LogUtil.e("epub file error", e);
			postShowDialog(EpubViewerDialog.ID_EPUB_OTHER_ERR);
		} catch (LoadImageException e) {
			// ストリームがnullだったりデコードに失敗した場合
			LogUtil.e("Failed to load image", e);
			postShowDialog(EpubViewerDialog.ID_LOAD_IMAGE_ERR);
		} catch (OutOfMemoryError e) {
			LogUtil.e("out of memory in the read method of the bitmap", e);
			postShowDialog(EpubViewerDialog.ID_OUT_OF_MEMORY_ERR);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		LogUtil.d("thread is finalize");
	}

	public void halt() {
		mIsHalt = true;
		LogUtil.d("thread halt call page=%s quality=%s", mPageOperation.getPage().toString(), mLoadQuality.name());
	}

	private InputStream getCenterInputStream() throws EpubParseException, EpubOtherException {
		if (mPageOperation.getPage().getCenterOpfItem() != null) {
			return mEpubPageAccess.getInputStream(mPageOperation.getPage().getCenterOpfItem());
		}
		return null;
	}

	private InputStream getLeftInputStream() throws EpubParseException, EpubOtherException {
		if (mPageOperation.getPage().getLeftOpfItem() != null) {
			return mEpubPageAccess.getInputStream(mPageOperation.getPage().getLeftOpfItem());
		}
		return null;
	}

	private InputStream getRightInputStream() throws EpubParseException, EpubOtherException {
		if (mPageOperation.getPage().getRightOpfItem() != null) {
			return mEpubPageAccess.getInputStream(mPageOperation.getPage().getRightOpfItem());
		}
		return null;
	}

	private void postShowDialog(final int id) {
		mHandler.post(new Runnable() {
			@SuppressWarnings("deprecation")
            @Override
			public void run() {
				mEpubViewerActivity.showDialog(id);
			}
		});
	}
}
