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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements PagerFragment.Delegate {
    private static final String TAG = "MainActivity";
    @BindView(R.id.layout) SwipeRefreshLayout mLayout;
    @BindView(R.id.pager) ViewPager mPager;

    private List<String> sliderUrls = new ArrayList<>();
    private List<String> gridUrls = new ArrayList<>();
    private PagerAdapter mPagerAdapter;

    private static final String SOURCE_DOMAIN = "http://www.gettyimagesgallery.com";
    private static final String SOURCE_PATH = "/exhibitions/archive/poolside.aspx";

    private PoolSideDBHelper dbHelper;

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
        tryLoadFromDB();
    }

    private void tryLoadFromDB() {
        new LoadingFromDBTask().execute();
    }

    private void reload() {
        new CrawlTask().execute(SOURCE_DOMAIN + SOURCE_PATH);
    }

    @Override
    public void onRightClicked() {
        int current = mPager.getCurrentItem();
        if (current + 1 < mPagerAdapter.getCount()) {
            mPager.setCurrentItem(current + 1);
        }
    }

    @Override
    public void onLeftClicked() {
        int current = mPager.getCurrentItem();
        if (current > 0) {
            mPager.setCurrentItem(current - 1);
        }
    }

    private class PoolSideImageUrls {
        public List<String> sliderUrls = new ArrayList<>();
        public List<String> gridUrls = new ArrayList<>();
    }

    private class CrawlTask extends AsyncTask<String, Void, PoolSideImageUrls> {
        @Override
        protected PoolSideImageUrls doInBackground(String[] params) {
            Log.d(TAG, "Start crawl");
            PoolSideImageUrls result = null;
            try {
                result = new PoolSideImageUrls();
                Document doc = Jsoup.connect(params[0]).get();

                Elements sliderContents = doc.select("div#slider li img");
                for (String url : sliderContents.eachAttr("src")) {
                    result.sliderUrls.add(SOURCE_DOMAIN + url);
                }

                Elements gridContents = doc.select("div.gallery-item-group.exitemrepeater img.picture");
                for (String url : gridContents.eachAttr("src")) {
                    result.gridUrls.add(SOURCE_DOMAIN + url);
                }

                saveToDB(result);
            } catch (IOException e) { // for Jsoup.connect
                e.printStackTrace();
            } catch (SQLException e) { // for saveToDB
                Log.e(TAG, "Save to DB failed");
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(PoolSideImageUrls result) {
            if (result != null) {
                update(result);
                Log.d(TAG, "Loaded from crawling");
            } else {
                Log.d(TAG, "Crawling failed");
                // TODO: crawling failed. show message
            }
            mLayout.setRefreshing(false);
        }

        private void saveToDB(PoolSideImageUrls urls) throws SQLException {
            Log.d(TAG, "Start saving to DB");
            PoolSideDataSource dataSource = new PoolSideDataSource(MainActivity.this);
            dataSource.open();
            dataSource.clear();
            dataSource.insertCarouselImageUrl(urls.sliderUrls);
            dataSource.insertGridImageUrl(urls.gridUrls);
            dataSource.close();
            Log.d(TAG, "Saving to DB done");
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

    private class LoadingFromDBTask extends AsyncTask<Void, Void, PoolSideImageUrls> {
        @Override
        protected PoolSideImageUrls doInBackground(Void... params) {
            Log.d(TAG, "Try loading from db");
            PoolSideDataSource dataSource = new PoolSideDataSource(MainActivity.this);
            dataSource.open();
            PoolSideImageUrls result = new PoolSideImageUrls();
            result.sliderUrls = dataSource.getCarouselImageUrls();
            result.gridUrls = dataSource.getGridImageUrls();
            dataSource.close();
            if (result.sliderUrls.size() > 0 && result.gridUrls.size() > 0) {
                return result;
            }
            else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(PoolSideImageUrls result) {
            if (result != null) {
                Log.d(TAG, "Loaded from DB");
                update(result);
                mLayout.setRefreshing(false);
            } else {
                Log.d(TAG, "No data in DB");
                // not saved on db. start crawl
                reload();
            }
        }
    }

    private void update(PoolSideImageUrls urls) {
        sliderUrls.clear();
        sliderUrls.addAll(urls.sliderUrls);
        gridUrls.clear();
        gridUrls.addAll(urls.gridUrls);
        Log.d(TAG, sliderUrls.toString());
        Log.d(TAG, gridUrls.toString());

        mPagerAdapter.setImageUrls(sliderUrls);
    }
}
