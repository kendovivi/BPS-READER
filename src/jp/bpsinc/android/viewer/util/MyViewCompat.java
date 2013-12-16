/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.bpsinc.android.viewer.util;

import android.view.View;

public class MyViewCompat {
	private static final long FAKE_FRAME_TIME = 10;

	interface ViewCompatImpl {
		public void postInvalidateOnAnimation(View view);

		public void postInvalidateOnAnimation(View view, int left, int top, int right, int bottom);

		public void postOnAnimation(View view, Runnable action);

		public void postOnAnimationDelayed(View view, Runnable action, long delayMillis);
	}

	static class BaseViewCompatImpl implements ViewCompatImpl {
		public void postInvalidateOnAnimation(View view) {
			view.postInvalidateDelayed(getFrameTime());
		}

		public void postInvalidateOnAnimation(View view, int left, int top, int right, int bottom) {
			view.postInvalidateDelayed(getFrameTime(), left, top, right, bottom);
		}

		public void postOnAnimation(View view, Runnable action) {
			view.postDelayed(action, getFrameTime());
		}

		public void postOnAnimationDelayed(View view, Runnable action, long delayMillis) {
			view.postDelayed(action, getFrameTime() + delayMillis);
		}

		long getFrameTime() {
			return FAKE_FRAME_TIME;
		}
	}

	static class GBViewCompatImpl extends BaseViewCompatImpl {
	}

	static class HCViewCompatImpl extends GBViewCompatImpl {
	}

	static class ICSViewCompatImpl extends HCViewCompatImpl {
	}

	static class JBViewCompatImpl extends ICSViewCompatImpl {
		@Override
		public void postInvalidateOnAnimation(View view) {
			ViewCompatJB.postInvalidateOnAnimation(view);
		}

		@Override
		public void postInvalidateOnAnimation(View view, int left, int top, int right, int bottom) {
			ViewCompatJB.postInvalidateOnAnimation(view, left, top, right, bottom);
		}

		@Override
		public void postOnAnimation(View view, Runnable action) {
			ViewCompatJB.postOnAnimation(view, action);
		}

		@Override
		public void postOnAnimationDelayed(View view, Runnable action, long delayMillis) {
			ViewCompatJB.postOnAnimationDelayed(view, action, delayMillis);
		}
	}

	static class JbMr1ViewCompatImpl extends JBViewCompatImpl {
	}

	static final ViewCompatImpl IMPL;
	static {
		final int version = android.os.Build.VERSION.SDK_INT;
		if (version >= 17) {
			IMPL = new JbMr1ViewCompatImpl();
		} else if (version >= 16) {
			IMPL = new JBViewCompatImpl();
		} else if (version >= 14) {
			IMPL = new ICSViewCompatImpl();
		} else if (version >= 11) {
			IMPL = new HCViewCompatImpl();
		} else if (version >= 9) {
			IMPL = new GBViewCompatImpl();
		} else {
			IMPL = new BaseViewCompatImpl();
		}
	}

	public static void postInvalidateOnAnimation(View view) {
		IMPL.postInvalidateOnAnimation(view);
	}

	public static void postInvalidateOnAnimation(View view, int left, int top, int right, int bottom) {
		IMPL.postInvalidateOnAnimation(view, left, top, right, bottom);
	}

	public static void postOnAnimation(View view, Runnable action) {
		IMPL.postOnAnimation(view, action);
	}

	public static void postOnAnimationDelayed(View view, Runnable action, long delayMillis) {
		IMPL.postOnAnimationDelayed(view, action, delayMillis);
	}
}
