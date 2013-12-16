package jp.bpsinc.android.viewer.preference;

import com.example.bps_reader.R;

import jp.bpsinc.android.util.LogUtil;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

public class BrightnessPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {
	/** 明るさのデフォルト値 */
	private final int mDefaultBrightness;
	private SeekBar mSeekBar;
	private CheckBox mCheckBox;
	/**
	 * 内部で一時的に保持する設定値、ダイアログを閉じるときにOKを押したらプリファレンスに反映<br>
	 * プリファレンスに保存される値は1～100(チェックボックスがONの場合は-100～-1、マイナス値の場合は一律で「端末の設定に従う」になる)、<br>
	 * シークバーのprogress値は0～99なので気をつけること
	 */
	private int mBrightness;

	public BrightnessPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 明るさ設定用のダイアログレイアウトを設定
		setDialogLayoutResource(R.layout.brightness_dialog);
		// 現状同じ値なのでFXL用のデフォルト値を使用、もしもOMFなどの時にデフォルト値が変化する場合はその時対処
		mDefaultBrightness = context.getResources().getInteger(R.integer.pre_default_fxl_epub_viewer_brightness);
	}

	@Override
	protected void onBindDialogView(View view) {
		LogUtil.v();
		super.onBindDialogView(view);
		// ここの処理、ダイアログ開く度に取得し直さないとダメ
		mSeekBar = (SeekBar) view.findViewById(R.id.brightness_seekbar);
		mSeekBar.setOnSeekBarChangeListener(this);
		mCheckBox = (CheckBox) view.findViewById(R.id.brightness_checkbox);
		mCheckBox.setOnCheckedChangeListener(this);
		mBrightness = getPersistedInt(mDefaultBrightness);

		// 保存されている値が正か負かでチェックボックスのON/OFFを切り替える
		if (mBrightness < 0) {
			mCheckBox.setChecked(true);
			mSeekBar.setProgress(-(mBrightness));
		} else {
			mCheckBox.setChecked(false);
			mSeekBar.setProgress(mBrightness);
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		LogUtil.v("positiveResult=%b", positiveResult);
		if (positiveResult) {
			// OK押したらプリファレンスに反映、リスナーは値を変える度に呼び出してるのでここでは必要なし
			persistInt(mBrightness);
		} else {
			// キャンセルした場合は明るさなどを戻す必要があるので、ダイアログ開いた時に保存されていた値でリスナー呼び出す
			callChangeListener(getPersistedInt(mDefaultBrightness));
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		LogUtil.v("isChecked=%b, brightness=%d", isChecked, mBrightness);
		if (isChecked) {
			if (mBrightness > 0) {
				mBrightness = -mBrightness;
			}
			mSeekBar.setEnabled(false);
		} else {
			if (mBrightness < 0) {
				mBrightness = -mBrightness;
			}
			mSeekBar.setEnabled(true);
		}
		// 不正値とかないし、ここではまだプリファレンスに保存はされないのでリスナーの戻り値は無視
		callChangeListener(mBrightness);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		LogUtil.v("progress=%d, fromUser=%b", progress, fromUser);
		if (fromUser) {
			// 不正値とかないし、ここではまだプリファレンスに保存はされないのでリスナーの戻り値は無視
			mBrightness = progress + 1;
			callChangeListener(mBrightness);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// 特に何もしない
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// 特に何もしない
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		persistInt(restoreValue ? getPersistedInt(mDefaultBrightness) : (defaultValue != null ? Integer.valueOf((Integer) defaultValue) : mDefaultBrightness));
	}

	/**
	 * サマリーを設定、正の値の場合「brightness%」、負の値の場合「context.getString(R.string.brightness_checkbox_text)」を表示
	 * 
	 * @param context getStringのためのコンテキスト
	 * @param brightness 画面の明るさ
	 */
	public void setSummary(Context context, int brightness) {
		if (brightness < 0) {
			setSummary(context.getString(R.string.brightness_checkbox_text));
		} else {
			setSummary(brightness + "%");
		}
	}

	public int getValue() {
		return getPersistedInt(mDefaultBrightness);
	}
}
