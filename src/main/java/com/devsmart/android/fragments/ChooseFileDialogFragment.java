package com.devsmart.android.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.devsmart.android.R;

import java.io.File;

public class ChooseFileDialogFragment extends DialogFragment {

    private static final String ARG_ROOT = "root";


    public static ChooseFileDialogFragment newInstance() {
        final File root = Environment.getExternalStorageDirectory();
        ChooseFileDialogFragment retval = new ChooseFileDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOT, root.getAbsolutePath());
        retval.setArguments(args);
        return retval;
    }

    private File mCurrentDir;
    private FrameLayout mFrameLayout;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mCurrentDir = new File(args.getString(ARG_ROOT));

    }


    private View createContentView() {

        mFrameLayout = new FrameLayout(getActivity());
        mFrameLayout.setMinimumHeight(500);

        mListView = new ListView(getActivity());
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mListView.setAdapter(new DirListAdapter(mCurrentDir));
        mListView.setOnItemClickListener(mOnItemClicked);
        mFrameLayout.addView(mListView);

        return mFrameLayout;
    }

    class DirListAdapter extends BaseAdapter {

        private final File[] mFiles;

        public DirListAdapter(File file) {
            mFiles = file.listFiles();
        }

        @Override
        public int getCount() {
            return mFiles.length + 1;
        }

        @Override
        public Object getItem(int i) {
            return mFiles[i-1];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View retval = view;
            if(retval == null){
                retval = LayoutInflater.from(getActivity()).inflate(R.layout.filelistitem, viewGroup, false);
            }

            TextView filenameText = (TextView)retval.findViewById(R.id.filename);

            if(i == 0){
                filenameText.setText("..");
                retval.setTag(new File(".."));
            } else {
                final File file = mFiles[i-1];
                retval.setTag(file);
                filenameText.setText(file.getName());
            }


            return retval;
        }
    }

    private AdapterView.OnItemClickListener mOnItemClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final File file = (File) view.getTag();

            if("..".equals(file.getName())){
                popDir();
            } else if(file.isDirectory()){
                pushDir(file.getName());
            } else {
                adapterView.setSelection(i);
            }

        }
    };



    private void pushDir(final String dirName) {

        final ListView oldListView = mListView;
        Animation out = AnimationUtils.makeOutAnimation(getActivity(), false);
        out.setDuration(125);
        out.setFillAfter(false);
        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFrameLayout.removeView(oldListView);

                mCurrentDir = new File(mCurrentDir, dirName);
                ListView newListView = new ListView(getActivity());
                newListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                newListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                newListView.setAdapter(new DirListAdapter(mCurrentDir));
                newListView.setOnItemClickListener(mOnItemClicked);
                mFrameLayout.addView(newListView);
                mListView = newListView;

                Animation in = AnimationUtils.makeInAnimation(getActivity(), false);
                in.setDuration(125);
                in.setFillAfter(true);
                newListView.startAnimation(in);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mListView.startAnimation(out);


    }

    private void popDir() {
        final ListView oldListView = mListView;
        Animation out = AnimationUtils.makeOutAnimation(getActivity(), true);
        out.setDuration(125);
        out.setFillAfter(false);
        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFrameLayout.removeView(oldListView);

                mCurrentDir = mCurrentDir.getParentFile();
                ListView newListView = new ListView(getActivity());
                newListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                newListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                newListView.setAdapter(new DirListAdapter(mCurrentDir));
                newListView.setOnItemClickListener(mOnItemClicked);
                mFrameLayout.addView(newListView);
                mListView = newListView;

                Animation in = AnimationUtils.makeInAnimation(getActivity(), true);
                in.setDuration(125);
                in.setFillAfter(true);
                newListView.startAnimation(in);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mListView.startAnimation(out);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Dialog retval = super.onCreateDialog(savedInstanceState);

        AlertDialog retval = new AlertDialog.Builder(getActivity())
                .setView(createContentView())
                .setTitle("Choose File")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Select", null)
                .create();



        //retval.setTitle("Choose File");
        retval.show();
        return retval;
    }
}
