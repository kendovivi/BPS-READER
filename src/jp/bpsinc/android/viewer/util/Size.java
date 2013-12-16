package jp.bpsinc.android.viewer.util;

public class Size {
	public final int width;
	public final int height;

	public Size(int w, int h) {
		width = w;
		height = h;
	}

	public Size halfWidth() {
		return new Size(width / 2, height);
	}

	@Override
	public String toString() {
		return "Size w=" + width + ", h=" + height;
	}

	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof Size) {
			Size otherSize = (Size) other;
			return width == otherSize.width && height == otherSize.height;
		}
		return false;
	}

	/**
	 * このSizeは、引数で渡されたotherと同じか、それより大きい場合true。「大きい」とは、幅と高さが両方同値以上を指す。
	 */
	public boolean isGreaterThanOrEqualTo(Size other) {
		return width >= other.width && height >= other.height;
	}
}
