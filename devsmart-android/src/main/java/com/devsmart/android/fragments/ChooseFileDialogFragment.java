package com.devsmart.android.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.devsmart.android.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

public class ChooseFileDialogFragment extends DialogFragment {



    public interface ChooseFileCallback {
        void onCancel();
        void onFileSelected(File file);
    }

    private static final String ARG_ROOT = "root";
    private static final String ARG_GOBELOW = "goBelow";
    private static final String ARG_FILENAME_REGEX = "filenameRegex";
    private static final String DEFAULT_FILENAME_REGEX = ".*";

    public static class Builder {

        private File mRootDir;
        private boolean mGoBelow = false;
        private ArrayList<String> mFilenameFilters = new ArrayList<String>();

        public Builder() {
            mRootDir = Environment.getExternalStorageDirectory();
        }

        public Builder rootDir(File dir) {
            mRootDir = dir;
            return this;
        }

        public Builder canGoBelowRoot(boolean goBelow) {
            mGoBelow = goBelow;
            return this;
        }

        public Builder addFilenameFilter(String regex) {
            mFilenameFilters.add(regex);
            return this;
        }

        public ChooseFileDialogFragment build() {
            ChooseFileDialogFragment retval = new ChooseFileDialogFragment();
            Bundle args = new Bundle();
            args.putString(ARG_ROOT, mRootDir.getAbsolutePath());
            args.putBoolean(ARG_GOBELOW, mGoBelow);
            args.putStringArray(ARG_FILENAME_REGEX, mFilenameFilters.toArray(new String[mFilenameFilters.size()]));
            retval.setArguments(args);
            return retval;
        }
    }


    public static ChooseFileDialogFragment newInstance() {
        return new Builder()
                .addFilenameFilter(DEFAULT_FILENAME_REGEX)
                .build();
    }

    private boolean mCanGoBelow;
    private File mRootDir;
    private File mCurrentDir;
    private FrameLayout mFrameLayout;
    private ListView mListView;
    private File mSelectedFile;
    private ChooseFileCallback mCallback;
    private ArrayList<Pattern> mFilenameRegex = new ArrayList<Pattern>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mRootDir = new File(args.getString(ARG_ROOT));
        mCurrentDir = mRootDir;

        mCanGoBelow = args.getBoolean(ARG_GOBELOW);
        for(String regex : args.getStringArray(ARG_FILENAME_REGEX)){
            mFilenameRegex.add(Pattern.compile(regex));
        }
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
            load();
        }

        public void load() {
            mFiles = mDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if(f.isDirectory()) {
                        return true;
                    }

                    for(Pattern p : mFilenameRegex) {
                        if(p.matcher(f.getName()).find()) {
                            return true;
                        }
                    }
                    return false;
                }
            });

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
                retval = LayoutInflater.from(getActivity()).inflate(R.layout.devsmart_filelistitem, viewGroup, false);
            }

            TextView filenameText = (TextView) retval.findViewById(R.id.filename);
            ImageView icon = (ImageView) retval.findViewById(R.id.icon);

            int imageResource = android.R.color.transparent;

            if(hasBack()) {
                if (i == 0) {
                    filenameText.setText("..");
                    retval.setTag(new File(".."));

                } else {
                    final File file = mFiles[i - 1];
                    imageResource = file.isFile() ? R.drawable.ic_insert_drive_file_white_24dp : R.drawable.ic_folder_white_24dp;
                    retval.setTag(file);
                    filenameText.setText(file.getName());

                }
            } else {
                final File file = mFiles[i];
                imageResource = file.isFile() ? R.drawable.ic_insert_drive_file_white_24dp : R.drawable.ic_folder_white_24dp;
                retval.setTag(file);
                filenameText.setText(file.getName());
            }

            icon.setImageResource(imageResource);


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
        if(mCallback != null) {
            if(mSelectedFile != null) {
                mCallback.onFileSelected(mSelectedFile);
            } else {
                mCallback.onCancel();
            }
        }
    }
}