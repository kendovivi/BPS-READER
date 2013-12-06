package bps.android.reader.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import bps.android.reader.book.BookInfo;
import bps.android.reader.listadapter.BookAdapter;

import com.example.bps_reader.R;

public class BookListFragment extends Fragment {
    private ArrayList<BookInfo> bookList = new ArrayList<BookInfo>();
    private BookInfo book1 = new BookInfo(1, "rails1", "matsu","publisher","20010608", "this is a rails1 book", "urlH", "urlV");
    private BookInfo book2 = new BookInfo(2, "rails2", "matsu","publisher","20010809", "this is a rails2 book", "urlH", "urlV");
    private BookInfo book3 = new BookInfo(2, "rails3", "matsu","publisher","20010809", "this is a rails2 book", "urlH", "urlV");
    private BookInfo book4 = new BookInfo(2, "rails4", "matsu","publisher","20010809", "this is a rails2 book", "urlH", "urlV");
    private BookInfo book5 = new BookInfo(2, "rails5", "matsu","publisher","20010809", "this is a rails2 book", "urlH", "urlV");
    private GridView mGrid;
    private BookAdapter adapter;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //GridView mGrid = (GridView) this.getActivity().findViewById(R.id.booklistgrid);
        bookList.add(book1);
        bookList.add(book2);
        bookList.add(book3);
        bookList.add(book4);
        bookList.add(book5);
        adapter = new BookAdapter(getActivity(), R.id.booklistgridv, bookList);
        //setAdapter(adapter);
        mGrid.setAdapter(adapter);
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context = getActivity();
        LinearLayout root = new LinearLayout(context);
        GridView gv = new GridView(getActivity());
        gv.setNumColumns(GridView.AUTO_FIT);
        gv.setId(android.R.id.list);
        root.addView(gv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));

        return root;
    }
    
    @SuppressLint("NewApi")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        ensureGrid();
    }
    
    private void ensureGrid() {
        if (mGrid != null) {
            return;
        }
        
        View root = getView();
        if (root == null) {
            throw new IllegalStateException("Content view not yet created");
        }
        if (root instanceof GridView) {
            mGrid = (GridView) root;
        }else {
            mGrid = (GridView) root.findViewById(android.R.id.list);
        }
    }
}
