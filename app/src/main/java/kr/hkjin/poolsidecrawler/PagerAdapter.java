package kr.hkjin.poolsidecrawler;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private List<String> mUrls = new ArrayList<>();

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(final int position) {
        String imageUrl;
        try {
            imageUrl = mUrls.get(position);
        } catch (IndexOutOfBoundsException e) {
            imageUrl = "";
        }
        boolean leftEnable = true, rightEnable = true;
        if (position == 0) {
            leftEnable = false;
        }
        if (position == getCount() - 1) {
            rightEnable = false;
        }
        PagerFragment fragment = PagerFragment.newInstance(imageUrl, leftEnable, rightEnable);

        return fragment;
    }

    @Override
    public int getCount() {
        return mUrls.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setImageUrls(List<String> urls) {
        mUrls = urls;
        notifyDataSetChanged();
    }
}
