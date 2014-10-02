package com.devsmart.android.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import java.util.Arrays;
import java.util.Comparator;

public class ChooseFileDialogFragment extends DialogFragment {

    public interface ChooseFileCallback {
        void onCancel();
        void onFileSelected(File file);
    }

    private static final String ARG_ROOT = "root";


    public static ChooseFileDialogFragment newInstance() {
        final File root = Environment.getExternalStorageDirectory();
        ChooseFileDialogFragment retval = new ChooseFileDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOT, root.getAbsolutePath());
        retval.setArguments(args);
        return retval;
    }

    private boolean mCanGoBelow = false;
    private File mRootDir;
    private File mCurrentDir;
    private FrameLayout mFrameLayout;
    private ListView mListView;
    private File mSelectedFile;
    private ChooseFileCallback mCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mRootDir = new File(args.getString(ARG_ROOT));
        mCurrentDir = mRootDir;

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

    class FilenameComparator implements Comparator<File> {

        @Override
        public int compare(File file, File file2) {
            int a = file.isDirectory() ? 0 : 1;
            int b = file2.isDirectory() ? 0 : 1;

            if(a == b){
                return file.getName().compareTo(file2.getName());
            } else {
                return a - b;
            }
        }
    }

    class DirListAdapter extends BaseAdapter {

        private final File mDir;
        private File[] mFiles;


        public DirListAdapter(File file) {
            mDir = file;
            mFiles = file.listFiles();
            if(mFiles == null) {
                mFiles = new File[0];
            }

            Arrays.sort(mFiles, new FilenameComparator());
        }

        private boolean hasBack() {
            return !(mDir.equals(mRootDir) && !mCanGoBelow);
        }

        @Override
        public int getCount() {
            if(hasBack()){
                return mFiles.length + 1;
            } else {
                return mFiles.length;
            }
        }

        @Override
        public Object getItem(int i) {
            if(hasBack()){
                return mFiles[i - 1];
            } else {
                return mFiles[i];
            }
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

            if(hasBack()) {
                if (i == 0) {
                    filenameText.setText("..");
                    retval.setTag(new File(".."));
                } else {
                    final File file = mFiles[i - 1];
                    retval.setTag(file);
                    filenameText.setText(file.getName());
                }
            } else {
                final File file = mFiles[i];
                retval.setTag(file);
                filenameText.setText(file.getName());
            }


            return retval;
        }
    }


    private AdapterView.OnItemClickListener mOnItemClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mSelectedFile = null;

            final File file = (File) view.getTag();
            if("..".equals(file.getName())){
                popDir();
            } else if(file.isDirectory()){
                pushDir(file.getName());
            } else {
                adapterView.setSelection(i);
                mSelectedFile = file;
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
        AlertDialog retval = new AlertDialog.Builder(getActivity())
                .setView(createContentView())
                .setTitle("Choose File")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mCallback != null) {
                            mCallback.onCancel();
                        }
                        dismiss();
                    }
                })
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onSelectedClicked();
                    }
                })
                .create();

        retval.show();
        return retval;
    }

    public void setCallback(ChooseFileCallback cb) {
        mCallback = cb;
    }

    void onSelectedClicked() {
        if(mSelectedFile != null) {
            if(mCallback != null){
                mCallback.onFileSelected(mSelectedFile);
            }
            dismiss();
        }
    }
}
