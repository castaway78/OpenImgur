package com.kenny.openimgur.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kenny.openimgur.R;
import com.kenny.openimgur.util.ViewUtils;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by kcampagna on 10/25/14.
 */
public class GalleryFilterFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener,
        RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    private static final String KEY_SECTION = "section";

    private static final String KEY_SORT = "sort";

    private static final String KEY_VIRAL = "showViral";

    @InjectView(R.id.sectionGroup)
    RadioGroup mSectionRG;

    @InjectView(R.id.time)
    TextView mTime;

    @InjectView(R.id.rising)
    TextView mRising;

    @InjectView(R.id.viral)
    TextView mViral;

    @InjectView(R.id.sortSeekBar)
    SeekBar mSeekBar;

    @InjectView(R.id.showViral)
    CheckBox mShowViral;

    private FilterListener mListener;

    private boolean mHasThreeSortOpts = false;

    /**
     * Creates a new instance of GalleryFilterFragment
     *
     * @param sort      The current GallerySort
     * @param section   The current GallerySection
     * @param showViral If viral images should be shown in User Sub
     * @return
     */
    public static GalleryFilterFragment createInstance(GalleryFragment.GallerySort sort, GalleryFragment.GallerySection section, boolean showViral) {
        Bundle args = new Bundle();
        GalleryFilterFragment fragment = new GalleryFilterFragment();
        args.putString(KEY_SECTION, section.getSection());
        args.putString(KEY_SORT, sort.getSort());
        args.putBoolean(KEY_VIRAL, showViral);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_gallery, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configToolBar((Toolbar) view.findViewById(R.id.toolBar));
        ((TextView) view.findViewById(R.id.sectionTitle)).setTextColor(getResources().getColor(theme.darkColor));
        ((TextView) view.findViewById(R.id.sortTitle)).setTextColor(getResources().getColor(theme.darkColor));
        Bundle args = getArguments();
        GalleryFragment.GallerySort sort = GalleryFragment.GallerySort.getSortFromString(args.getString(KEY_SORT, null));
        GalleryFragment.GallerySection section = GalleryFragment.GallerySection.getSectionFromString(args.getString(KEY_SECTION, null));
        mShowViral.setChecked(args.getBoolean(KEY_VIRAL, true));

        switch (section) {
            case USER:
                mHasThreeSortOpts = true;
                mSectionRG.check(R.id.userSubRB);
                mShowViral.setAlpha(1.0f);
                mShowViral.setVisibility(View.VISIBLE);
                break;

            case HOT:
                mHasThreeSortOpts = false;
                mRising.setAlpha(0.0f);
                mSectionRG.check(R.id.viralRB);
                mShowViral.setAlpha(0.0f);
                mShowViral.setVisibility(View.INVISIBLE);
                break;
        }

        switch (sort) {
            case RISING:
                mRising.setTextColor(getResources().getColor(theme.accentColor));
                mSeekBar.setProgress(50);
                mRising.setTypeface(null, Typeface.BOLD);
                break;

            case VIRAL:
                mViral.setTextColor(getResources().getColor(theme.accentColor));
                mSeekBar.setProgress(100);
                mViral.setTypeface(null, Typeface.BOLD);
                break;

            case TIME:
                mSeekBar.setProgress(0);
                mTime.setTextColor(getResources().getColor(theme.accentColor));
                mTime.setTypeface(null, Typeface.BOLD);
                break;
        }

        mSeekBar.setOnSeekBarChangeListener(this);
        mSectionRG.setOnCheckedChangeListener(this);

        // I've never found fragment transaction animations to work properly, so we will animate the view
        // when it is added to the fragment manager
        view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.filter_appear));
    }

    private void configToolBar(Toolbar tb) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tb.getLayoutParams();
        lp.setMargins(0, ViewUtils.getStatusBarHeight(getActivity()), 0, 0);
        tb.setLayoutParams(lp);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(null, null);
            }
        });
        tb.setBackgroundColor(getResources().getColor(theme.primaryColor));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        //NOOP
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //NOOP
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int position = seekBar.getProgress();

        if (mHasThreeSortOpts) {
            if (position <= 33) {
                mSeekBar.setProgress(0);
                mTime.setTextColor(getResources().getColor(theme.accentColor));
                mViral.setTextColor(Color.BLACK);
                mRising.setTextColor(Color.BLACK);
                mTime.setTypeface(null, Typeface.BOLD);
                mViral.setTypeface(null, Typeface.NORMAL);
                mRising.setTypeface(null, Typeface.NORMAL);
            } else if (position <= 66) {
                mSeekBar.setProgress(50);
                mTime.setTextColor(Color.BLACK);
                mViral.setTextColor(Color.BLACK);
                mRising.setTextColor(getResources().getColor(theme.accentColor));
                mTime.setTypeface(null, Typeface.NORMAL);
                mViral.setTypeface(null, Typeface.NORMAL);
                mRising.setTypeface(null, Typeface.BOLD);
            } else {
                mSeekBar.setProgress(100);
                mTime.setTextColor(Color.BLACK);
                mViral.setTextColor(getResources().getColor(theme.accentColor));
                mRising.setTextColor(Color.BLACK);
                mTime.setTypeface(null, Typeface.NORMAL);
                mViral.setTypeface(null, Typeface.BOLD);
                mRising.setTypeface(null, Typeface.NORMAL);
            }
        } else {
            if (position <= 50) {
                mSeekBar.setProgress(0);
                mTime.setTextColor(getResources().getColor(theme.accentColor));
                mViral.setTextColor(Color.BLACK);
                mRising.setTextColor(Color.BLACK);
                mTime.setTypeface(null, Typeface.BOLD);
                mViral.setTypeface(null, Typeface.NORMAL);
                mRising.setTypeface(null, Typeface.NORMAL);
            } else {
                mSeekBar.setProgress(100);
                mTime.setTextColor(Color.BLACK);
                mViral.setTextColor(getResources().getColor(theme.accentColor));
                mRising.setTextColor(Color.BLACK);
                mTime.setTypeface(null, Typeface.NORMAL);
                mViral.setTypeface(null, Typeface.BOLD);
                mRising.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.userSubRB:
                mHasThreeSortOpts = true;
                mRising.animate().alpha(1.0f).setDuration(300L);
                mShowViral.animate().alpha(1.0f).setDuration(300L)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                animation.removeAllListeners();
                                mShowViral.setVisibility(View.VISIBLE);
                            }
                        });
                break;

            case R.id.viralRB:
                mHasThreeSortOpts = false;
                mRising.animate().alpha(0.0f).setDuration(300L);
                mShowViral.animate().alpha(0.0f).setDuration(300L)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                animation.removeAllListeners();
                                mShowViral.setVisibility(View.INVISIBLE);
                            }
                        });

                // Need to reset the progress bar if it previously had three options
                if (mSeekBar.getProgress() == 50) {
                    mSeekBar.setProgress(100);
                    mViral.setTextColor(getResources().getColor(theme.accentColor));
                    mRising.setTextColor(Color.BLACK);
                }
                break;
        }
    }

    @OnClick({R.id.negative, R.id.positive})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.negative:
                dismiss(null, null);
                break;

            case R.id.positive:
                GalleryFragment.GallerySort sort;
                GalleryFragment.GallerySection section;
                int position = mSeekBar.getProgress();

                if (mSectionRG.getCheckedRadioButtonId() == R.id.userSubRB) {
                    section = GalleryFragment.GallerySection.USER;

                    if (position == 0) {
                        sort = GalleryFragment.GallerySort.TIME;
                    } else if (position == 50) {
                        sort = GalleryFragment.GallerySort.RISING;
                    } else {
                        sort = GalleryFragment.GallerySort.VIRAL;
                    }
                } else {
                    section = GalleryFragment.GallerySection.HOT;
                    sort = position == 0 ? GalleryFragment.GallerySort.TIME : GalleryFragment.GallerySort.VIRAL;
                }

                dismiss(sort, section);
                break;
        }
    }

    /**
     * Animates the removal of the dialog
     *
     * @param sort
     * @param section
     */
    public void dismiss(final GalleryFragment.GallerySort sort, final GalleryFragment.GallerySection section) {
        Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.filter_disappear);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (mListener != null)
                    mListener.onFilterChange(section, sort, mShowViral.isChecked());
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        getView().startAnimation(anim);
    }

    /**
     * Sets the listener for the Filter Fragment
     *
     * @param listener
     */
    public void setFilterListener(FilterListener listener) {
        mListener = listener;
    }

    public static interface FilterListener {
        void onFilterChange(GalleryFragment.GallerySection section, GalleryFragment.GallerySort sort, boolean showViral);
    }

}
