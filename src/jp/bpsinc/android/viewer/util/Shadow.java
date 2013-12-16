package jp.bpsinc.android.viewer.util;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;

public class Shadow {
	private static final int SHADOW_SIZE = 10;
	private static final int SHADOW_COUNT = 8;
	private static final int[] mColors = new int[] { 0xff333333, 0xff999999, 0xffcccccc, 0xffffffff };
	private static final float[] mPositions = null;
	private Paint mPaint = null;
	private Shader[] mShadow = null;
	private RectF[] mRect = null;

	public Shadow() {
		mPaint = new Paint();
		mRect = new RectF[SHADOW_COUNT];
		for (int i = 0; i < SHADOW_COUNT; i++) {
			mRect[i] = new RectF();
		}
	}

	/**
	 * 指定した座標の周囲に影(グラデーション)を描画する
	 * 
	 * @param canvas 影を表示するCanvas
	 * @param x0 X座標の始点
	 * @param y0 Y座標の始点
	 * @param x1 X座標の終点
	 * @param y1 Y座標の終点
	 */
	public void shadowDraw(Canvas canvas, int x0, int y0, int x1, int y1) {
		mShadow = new Shader[] {
				createLinearGradient(x0, 0, x0 - SHADOW_SIZE, 0),	// 左
				createLinearGradient(0, y0, 0, y0 - SHADOW_SIZE),	// 上
				createLinearGradient(x1, 0, x1 + SHADOW_SIZE, 0),	// 右
				createLinearGradient(0, y1, 0, y1 + SHADOW_SIZE),	// 下
				createRadialGradient(x0, y0, SHADOW_SIZE),			// 左上
				createRadialGradient(x1, y0, SHADOW_SIZE),			// 右上
				createRadialGradient(x1, y1, SHADOW_SIZE),			// 右下
				createRadialGradient(x0, y1, SHADOW_SIZE), };		// 左下
		// RectFは1つのオブジェクトをdrawRectのループ内で使いまわしても良い気がするが、
		// ハードウェアアクセラレーションが有効の場合drawRectに指定するRectFの参照先が同じため
		// setした値が書き換えられて影の描画がおかしくなることがある
		// Paintは同じの使っても大丈夫そう(多分)
		mRect[0].set(x0 - SHADOW_SIZE, y0 - SHADOW_SIZE, x0, y1);	// 左
		mRect[1].set(x0 - SHADOW_SIZE, y0 - SHADOW_SIZE, x1, y0);	// 上
		mRect[2].set(x1, y0, x1 + SHADOW_SIZE, y1);					// 右
		mRect[3].set(x0, y1, x1, y1 + SHADOW_SIZE);					// 下
		mRect[4].set(x0 - SHADOW_SIZE, y0 - SHADOW_SIZE, x0, y0);	// 左上
		mRect[5].set(x1, y0 - SHADOW_SIZE, x1 + SHADOW_SIZE, y0);	// 右上
		mRect[6].set(x1, y1, x1 + SHADOW_SIZE, y1 + SHADOW_SIZE);	// 右下
		mRect[7].set(x0 - SHADOW_SIZE, y1, x0, y1 + SHADOW_SIZE);	// 左下
		for (int i = 0; i < SHADOW_COUNT; i++) {
			mPaint.setShader(mShadow[i]);
			canvas.drawRect(mRect[i], mPaint);
		}
	}

	private LinearGradient createLinearGradient(int x0, int y0, int x1, int y1) {
		return new LinearGradient(x0, y0, x1, y1, mColors, mPositions, TileMode.CLAMP);
	}

	private RadialGradient createRadialGradient(int x, int y, int radius) {
		return new RadialGradient(x, y, radius, mColors, mPositions, TileMode.CLAMP);
	}
}
