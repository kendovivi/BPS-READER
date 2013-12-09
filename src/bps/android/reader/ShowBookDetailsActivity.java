
package bps.android.reader;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import bps.android.reader.fragment.BookDetailsFragment;

import com.example.bps_reader.R;

public class ShowBookDetailsActivity extends Activity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentBookId = getIntent().getIntExtra("bookId", 0);
        
        setContentView(R.layout.bookdetails_vertical);
        BookDetailsFragment bdf = new BookDetailsFragment();
        bdf.setArguments(getIntent().getExtras());
        FragmentManager manager = getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(R.id.detailsv, bdf);
        ft.commit();
        
        mBtn_read = findViewById(R.id.btn_read);
        mBtn_read.setOnClickListener(this);
        
    }
    
    private View mBtn_read;
    
    private int mCurrentBookId;

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.btn_read:
                intent.setClass(this, ShowArticle.class);
                intent.putExtra("bookId", mCurrentBookId);
                startActivity(intent);
                break;
        }
        
    }

   
    
}
