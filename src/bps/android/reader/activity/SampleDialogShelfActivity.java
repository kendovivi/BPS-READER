
package bps.android.reader.activity;

import jp.bpsinc.android.viewer.epub.activity.EpubViewerActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class SampleDialogShelfActivity extends Activity {
    Intent mIntent = null;

    AlertDialog mDialog = null;

    final Activity mActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder selectViewerBuilder = new AlertDialog.Builder(this);
        selectViewerBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mActivity.finish();
            }
        });
        selectViewerBuilder.setSingleChoiceItems(new CharSequence[] {
                "FXLビューア", "OMFビューア"
        }, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    mIntent = new Intent(mActivity,
                            jp.bpsinc.android.viewer.epub.fxl.activity.FxlEpubViewerActivity.class);
                } else if (which == 1) {
                    mIntent = new Intent(mActivity,
                            jp.bpsinc.android.viewer.epub.omf.activity.OmfEpubViewerActivity.class);
                } else if (which == 2) {
                    mIntent = new Intent(mActivity, bps.android.reader.activity.ShowArticleActivity.class);
                }
                mIntent.putExtra("bookId", getIntent().getIntExtra("bookId", 0));
                mIntent.putExtra(EpubViewerActivity.INTENT_KEY_EPUB_CONTENTS, getIntent()
                        .getSerializableExtra(EpubViewerActivity.INTENT_KEY_EPUB_CONTENTS));
                mActivity.startActivity(mIntent);
            }
        });

        mDialog = selectViewerBuilder.create();
        mDialog.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mDialog.show();
    }
}
