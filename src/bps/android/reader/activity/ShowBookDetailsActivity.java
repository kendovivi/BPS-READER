
package bps.android.reader.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import bps.android.reader.fragment.BookDetailsFragment;

import com.example.bps_reader.R;

public class ShowBookDetailsActivity extends FragmentActivity {
    private BookDetailsFragment bdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookdetails_vertical);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        if (bdf != null) {
            ft.remove(bdf).commit();
        }
        bdf = new BookDetailsFragment();
        bdf.setArguments(getIntent().getExtras());

        ft.replace(R.id.detailsv, bdf);
        ft.commit();
    }
}
