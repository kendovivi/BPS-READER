
package bps.android.reader.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import bps.android.reader.fragment.BookDetailsFragment;

import com.example.bps_reader.R;

public class ShowBookDetailsActivity extends Activity{
    private BookDetailsFragment bdf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookdetails_vertical);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        if (bdf != null){
            ft.remove(bdf).commit();
        }
        bdf = new BookDetailsFragment();
        bdf.setArguments(getIntent().getExtras());
        
        ft.replace(R.id.detailsv, bdf);
        ft.commit();
    }
}
