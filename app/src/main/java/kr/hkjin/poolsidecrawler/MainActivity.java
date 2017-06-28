package kr.hkjin.poolsidecrawler;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements PagerFragment.Delegate {
    private static final String TAG = "MainActivity";
    @BindView(R.id.layout) SwipeRefreshLayout mLayout;
    @BindView(R.id.pager) ViewPager mPager;

    private ArrayList<String> sliderUrls = new ArrayList<>();
    private ArrayList<String> gridUrls = new ArrayList<>();
    private PagerAdapter mPagerAdapter;

    private static final String SOURCE_DOMAIN = "http://www.gettyimagesgallery.com";
    private static final String SOURCE_PATH = "/exhibitions/archive/poolside.aspx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
            }
        });
        mLayout.setRefreshing(true);
        reload();
    }

    private void reload() {
        new CrawlTask().execute(SOURCE_DOMAIN + SOURCE_PATH);
    }

    @Override
    public void onRightClicked() {

    }

    @Override
    public void onLeftClicked() {

    }

    private class CrawlResult {
        public List<String> sliderUrls = new ArrayList<>();
        public List<String> gridUrls = new ArrayList<>();
    }

    private class CrawlTask extends AsyncTask<String, Void, CrawlResult> {
        @Override
        protected CrawlResult doInBackground(String[] params) {
            CrawlResult result = null;
            try {
                result = new CrawlResult();
                Document doc = Jsoup.connect(params[0]).get();

                Elements sliderContents = doc.select("div#slider li img");
                for (String url : sliderContents.eachAttr("src")) {
                    result.sliderUrls.add(SOURCE_DOMAIN + url);
                }

                Elements gridContents = doc.select("div.gallery-item-group.exitemrepeater img.picture");
                for (String url : gridContents.eachAttr("src")) {
                    result.gridUrls.add(SOURCE_DOMAIN + url);
                }
            } catch (IOException e) { // for Jsoup.connect
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(CrawlResult result) {
            if (result != null) {
                sliderUrls.clear();
                sliderUrls.addAll(result.sliderUrls);
                gridUrls.clear();
                gridUrls.addAll(result.gridUrls);
                Log.d(TAG, sliderUrls.toString());
                Log.d(TAG, gridUrls.toString());

                mPagerAdapter.setImageUrls(sliderUrls);
            } else {
                // crawling failed
            }
            mLayout.setRefreshing(false);
        }
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
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
}
