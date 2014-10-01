package com.devsmart.android.activities;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.devsmart.android.R;

import java.io.File;

public class ChooseFileActivity extends DialogFragment {

    private static final String ARG_ROOT = "root";

    public static ChooseFileActivity newInstance() {
        final File root = Environment.getExternalStorageDirectory();
        ChooseFileActivity retval = new ChooseFileActivity();
        Bundle args = new Bundle();
        args.putString(ARG_ROOT, root.getAbsolutePath());
        retval.setArguments(args);
        return retval;
    }

    private File mCurrentDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mCurrentDir = new File(args.getString(ARG_ROOT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout fragmentFrame = new FrameLayout(getActivity());
        fragmentFrame.setId(R.id.devsmart_container);
        fragmentFrame.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        FragmentTransaction tr = getChildFragmentManager().beginTransaction();
        tr.add(R.id.devsmart_container, DirListFragment.newInstance(mCurrentDir));
        tr.commit();


        return fragmentFrame;
    }

    /*
    @Override
    public void onResume() {
        super.onResume();
        FragmentTransaction tr = getChildFragmentManager().beginTransaction();
        tr.add(R.id.devsmart_container, DirListFragment.newInstance(mCurrentDir));
        tr.commit();
    }
    */

    /*
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                //.setView(getView())
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
    }
    */





    public static class DirListFragment extends Fragment {

        public static final java.lang.String ARG_FILE = "file";

        public static DirListFragment newInstance(File dir) {
            Bundle args = new Bundle();
            args.putString(ARG_FILE, dir.getAbsolutePath());
            DirListFragment retval = new DirListFragment();
            retval.setArguments(args);
            return retval;
        }

        private ListView mListView;
        private DirListAdapter mAdapter;
        private File mFile;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            String filename = getArguments().getString(ARG_FILE);
            mFile = new File(filename);
            mAdapter = new DirListAdapter(mFile);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mListView = new ListView(getActivity());
            mListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mListView.setAdapter(mAdapter);

            return mListView;
        }

        class DirListAdapter extends BaseAdapter {

            private final File[] mFiles;

            public DirListAdapter(File file) {
                mFiles = file.listFiles();
            }

            @Override
            public int getCount() {
                return mFiles.length;
            }

            @Override
            public Object getItem(int i) {
                return mFiles[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {

                final File file = mFiles[i];

                View retval = view;
                if(retval == null){
                    retval = LayoutInflater.from(getActivity()).inflate(R.layout.filelistitem, viewGroup, false);
                }

                TextView filenameText = (TextView)retval.findViewById(R.id.filename);
                filenameText.setText(file.getName());


                return retval;
            }
        }
    }




}
