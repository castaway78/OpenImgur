package com.kenny.openimgur;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.kenny.openimgur.classes.FragmentListener;
import com.kenny.openimgur.classes.OpenImgurApp;
import com.kenny.openimgur.fragments.GalleryFragment;
import com.kenny.openimgur.fragments.NavFragment;
import com.kenny.openimgur.fragments.ProfileFragment;
import com.kenny.openimgur.fragments.RedditFragment;
import com.kenny.openimgur.ui.FloatingActionButton;
import com.kenny.openimgur.util.LogUtil;
import com.kenny.openimgur.util.ViewUtils;

/**
 * Created by kcampagna on 10/19/14.
 */
public class MainActivity extends BaseActivity implements NavFragment.NavigationListener, FragmentListener, View.OnClickListener {
    private static final String KEY_CURRENT_PAGE = "current_page";

    private static final int PAGE_GALLERY = 0;

    private static final int PAGE_SUBREDDIT = 1;

    private static final int PAGE_PROFILE = 2;

    private int mCurrentPage = -1;

    private NavFragment mNavFragment;

    private DrawerLayout mDrawer;

    private View mUploadMenu;

    private FloatingActionButton mCameraUpload;

    private FloatingActionButton mGalleryUpload;

    private FloatingActionButton mLinkUpload;

    private FloatingActionButton mUploadButton;

    private float mUploadButtonHeight;

    private float mUploadMenuButtonHeight;

    private int mNavBarHeight = -1;

    private boolean uploadMenuOpen = false;

    private boolean uploadMenuShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setContentView(R.layout.activity_main);
        mNavFragment = (NavFragment) getFragmentManager().findFragmentById(R.id.navDrawer);
        mDrawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavFragment.configDrawerLayout(mDrawer);
        mUploadMenu = findViewById(R.id.uploadMenu);
        mUploadButton = (FloatingActionButton) mUploadMenu.findViewById(R.id.uploadButton);
        mLinkUpload = (FloatingActionButton) mUploadMenu.findViewById(R.id.linkUpload);
        mCameraUpload = (FloatingActionButton) mUploadMenu.findViewById(R.id.cameraUpload);
        mGalleryUpload = (FloatingActionButton) mUploadMenu.findViewById(R.id.galleryUpload);
        mUploadButton.setOnClickListener(this);
        mLinkUpload.setOnClickListener(this);
        mCameraUpload.setOnClickListener(this);
        mGalleryUpload.setOnClickListener(this);
        mUploadButtonHeight = getResources().getDimension(R.dimen.fab_button_radius);
        mUploadMenuButtonHeight = getResources().getDimension(R.dimen.fab_button_radius_smaller);
    }

    @Override
    public void onListItemSelected(int position) {
        mDrawer.closeDrawers();
        if (mCurrentPage == position) return;
        mCurrentPage = position;
        changePage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mNavFragment.onOptionsItemSelected(item)) return true;

        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(SettingsActivity.createIntent(getApplicationContext()));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            menu.clear();
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mCurrentPage = app.getPreferences().getInt(KEY_CURRENT_PAGE, PAGE_GALLERY);
            changePage();
        } else {
            mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE, PAGE_GALLERY);
        }

        mNavFragment.setSelectedPage(mCurrentPage);
    }

    /**
     * Changes the fragment
     */
    private void changePage() {
        Fragment fragment = null;

        switch (mCurrentPage) {
            case PAGE_GALLERY:
                fragment = GalleryFragment.createInstance();
                break;

            case PAGE_PROFILE:
                fragment = ProfileFragment.createInstance(null);
                break;

            case PAGE_SUBREDDIT:
                fragment = RedditFragment.createInstance();
                break;
        }

        if (fragment != null) {
            getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        } else {
            LogUtil.w(TAG, "Unable to determine fragment for page " + mCurrentPage);
        }
    }

    @Override
    protected void onDestroy() {
        app.getPreferences().edit().putInt(KEY_CURRENT_PAGE, mCurrentPage).commit();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURRENT_PAGE, mCurrentPage);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onHideActionBar(boolean shouldShow) {
        setActionBarVisibility(shouldShow);
        animateUploadMenuButton(shouldShow);
    }

    @Override
    public void onLoadingComplete() {
        mUploadMenu.setVisibility(View.VISIBLE);
        uploadMenuShowing = true;
    }

    @Override
    public void onLoadingStarted() {
        mUploadMenu.setVisibility(View.GONE);
        uploadMenuShowing = false;
    }

    @Override
    public void onError(int errorCode) {
        mUploadMenu.setVisibility(View.GONE);
        uploadMenuShowing = false;
    }

    @Override
    public void onUpdateActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onDrawerToggle(boolean isOpen) {
        if (isOpen) {
            setActionBarVisibility(true);
        }

        supportInvalidateOptionsMenu();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.uploadButton:
                animateUploadMenu();
                break;

            case R.id.cameraUpload:
                startActivity(UploadActivity.createIntent(getApplicationContext(), UploadActivity.UPLOAD_TYPE_CAMERA));
                animateUploadMenu();
                break;

            case R.id.galleryUpload:
                startActivity(UploadActivity.createIntent(getApplicationContext(), UploadActivity.UPLOAD_TYPE_GALLERY));
                animateUploadMenu();
                break;

            case R.id.linkUpload:
                startActivity(UploadActivity.createIntent(getApplicationContext(), UploadActivity.UPLOAD_TYPE_LINK));
                animateUploadMenu();
                break;
        }
    }

    /**
     * Animates the opening/closing of the Upload button
     */
    private void animateUploadMenu() {
        AnimatorSet set = new AnimatorSet().setDuration(500L);
        String translation = isLandscape() ? "translationX" : "translationY";

        if (!uploadMenuOpen) {
            uploadMenuOpen = true;

            set.playTogether(
                    ObjectAnimator.ofFloat(mCameraUpload, translation, 0, (mUploadButtonHeight + 25) * -1),
                    ObjectAnimator.ofFloat(mLinkUpload, translation, 0, ((2 * mUploadMenuButtonHeight) + mUploadButtonHeight + 75) * -1),
                    ObjectAnimator.ofFloat(mGalleryUpload, translation, (mUploadMenuButtonHeight + mUploadButtonHeight + 50) * -1),
                    ObjectAnimator.ofFloat(mCameraUpload, "alpha", 0.0f, 1.0f),
                    ObjectAnimator.ofFloat(mGalleryUpload, "alpha", 0.0f, 1.0f),
                    ObjectAnimator.ofFloat(mLinkUpload, "alpha", 0.0f, 1.0f),
                    ObjectAnimator.ofFloat(mUploadButton, "rotation", 0.0f, 135.0f)
            );

            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mCameraUpload.setVisibility(View.VISIBLE);
                    mGalleryUpload.setVisibility(View.VISIBLE);
                    mLinkUpload.setVisibility(View.VISIBLE);
                    animation.removeAllListeners();
                }
            });

            set.setInterpolator(new OvershootInterpolator());
            set.start();
        } else {
            uploadMenuOpen = false;

            set.playTogether(
                    ObjectAnimator.ofFloat(mCameraUpload, translation, 0),
                    ObjectAnimator.ofFloat(mLinkUpload, translation, 0),
                    ObjectAnimator.ofFloat(mGalleryUpload, translation, 0),
                    ObjectAnimator.ofFloat(mCameraUpload, "alpha", 1.0f, 0.0f),
                    ObjectAnimator.ofFloat(mGalleryUpload, "alpha", 1.0f, 0.0f),
                    ObjectAnimator.ofFloat(mLinkUpload, "alpha", 1.0f, 0.0f),
                    ObjectAnimator.ofFloat(mUploadButton, "rotation", 135.0f, 0.0f)
            );

            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    mCameraUpload.setVisibility(View.GONE);
                    mGalleryUpload.setVisibility(View.GONE);
                    mLinkUpload.setVisibility(View.GONE);
                    animation.removeAllListeners();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mCameraUpload.setVisibility(View.GONE);
                    mGalleryUpload.setVisibility(View.GONE);
                    mLinkUpload.setVisibility(View.GONE);
                    animation.removeAllListeners();
                }
            });

            set.setInterpolator(new AccelerateDecelerateInterpolator());
            set.start();
        }
    }

    /**
     * Animates the upload button
     *
     * @param shouldShow If the button should be shown
     */
    private void animateUploadMenuButton(boolean shouldShow) {
        if (!shouldShow && uploadMenuShowing) {
            float hideDistance;
            uploadMenuShowing = false;
            hideDistance = mUploadButtonHeight + (mUploadButtonHeight / 2);
            // Add extra distance to the hiding of the button if on KitKat due to the translucent nav bar
            if (OpenImgurApp.SDK_VERSION >= Build.VERSION_CODES.KITKAT) {
                if (mNavBarHeight == -1) mNavBarHeight = ViewUtils.getNavigationBarHeight(getApplicationContext());
                hideDistance += mNavBarHeight;
            }

            mUploadMenu.animate().setInterpolator(new AccelerateDecelerateInterpolator()).translationY(hideDistance).setDuration(350).start();
            // Close the menu if it is open
            if (uploadMenuOpen) animateUploadMenu();
        } else if (shouldShow && !uploadMenuShowing) {
            uploadMenuShowing = true;
            mUploadMenu.animate().setInterpolator(new AccelerateDecelerateInterpolator()).translationY(0).setDuration(350).start();
        }
    }
}
