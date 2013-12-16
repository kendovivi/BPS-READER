/*
 * Copyright (C) 2012 The Android Open Source Project
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

import java.lang.reflect.Method;

import jp.bpsinc.android.util.LogUtil;
import android.view.View;

/**
 * Jellybean-specific View API access
 */
public class ViewCompatJB {
	private static Method sMethodPostInvalidateOnAnimation;
	private static Method sMethodPostOnAnimation;
	private static Method sMethodPostOnAnimationDelayed;

	static {
		try {
			sMethodPostInvalidateOnAnimation = View.class
					.getDeclaredMethod("postInvalidateOnAnimation");
			sMethodPostOnAnimation = View.class
					.getDeclaredMethod("postOnAnimation", Runnable.class);
			sMethodPostOnAnimationDelayed = View.class.getDeclaredMethod("postOnAnimationDelayed",
					Runnable.class, long.class);
		} catch (Exception e) {
			LogUtil.e("ViewCompatJB", e);
		}
	}

	public static void postInvalidateOnAnimation(View view) {
		try {
			sMethodPostInvalidateOnAnimation.invoke(view);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	public static void postInvalidateOnAnimation(View view, int left, int top, int right, int bottom) {
		view.postInvalidate(left, top, right, bottom);
	}

	public static void postOnAnimation(View view, Runnable action) {
		try {
			sMethodPostOnAnimation.invoke(view, action);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void postOnAnimationDelayed(View view, Runnable action, long delayMillis) {
		try {
			sMethodPostOnAnimationDelayed.invoke(view, action, delayMillis);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
