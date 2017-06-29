package kr.hkjin.poolsidecrawler;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.GridLayout;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kr.hkjin.poolsidecrawler.db.PoolSideDataSource;

public class MainActivity extends AppCompatActivity implements PagerFragment.Delegate {
    private static final String TAG = "MainActivity";

    private static final String SOURCE_DOMAIN = "http://www.gettyimagesgallery.com";
    private static final String SOURCE_PATH = "/exhibitions/archive/poolside.aspx";

    @BindView(R.id.layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.pager) ViewPager mPager;
    @BindView(R.id.grid) GridLayout mGridLayout;

    private List<String> mCarouselUrls = new ArrayList<>();
    private List<String> mGridUrls = new ArrayList<>();
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
            }
        });
        mSwipeRefreshLayout.setRefreshing(true);
        tryLoadFromLocal();
    }

    private void tryLoadFromLocal() {
        new LocalLoadingTask().execute();
    }

    private void reload() {
        mCarouselUrls.clear();
        mGridUrls.clear();
        update();

        new CrawlTask().execute(SOURCE_DOMAIN + SOURCE_PATH);
    }

    private class PoolSideImageUrls {
        public List<String> carouselUrls = new ArrayList<>();
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

                Elements carouselContents = doc.select("div#slider li img");
                for (String url : carouselContents.eachAttr("src")) {
                    result.carouselUrls.add(SOURCE_DOMAIN + url);
                }

                Elements gridContents = doc.select("div.gallery-item-group.exitemrepeater img.picture");
                for (String url : gridContents.eachAttr("src")) {
                    result.gridUrls.add(SOURCE_DOMAIN + url);
                }

                saveToLocal(result);
            } catch (IOException e) { // for Jsoup.connect
                e.printStackTrace();
            } catch (SQLException e) { // for saveToLocal
                Log.e(TAG, "Save to local failed");
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
                showErrorDialog();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }

        private void saveToLocal(PoolSideImageUrls urls) throws SQLException {
            Log.d(TAG, "Start saving to local");
            PoolSideDataSource dataSource = new PoolSideDataSource(MainActivity.this);
            dataSource.open();
            dataSource.clear();
            dataSource.insertCarouselImageUrl(urls.carouselUrls);
            dataSource.insertGridImageUrl(urls.gridUrls);
            dataSource.close();
            Log.d(TAG, "Saving to local done");
        }
    }

    private class LocalLoadingTask extends AsyncTask<Void, Void, PoolSideImageUrls> {
        @Override
        protected PoolSideImageUrls doInBackground(Void... params) {
            Log.d(TAG, "Try loading from local");
            PoolSideDataSource dataSource = new PoolSideDataSource(MainActivity.this);
            dataSource.open();
            PoolSideImageUrls result = new PoolSideImageUrls();
            result.carouselUrls = dataSource.getCarouselImageUrls();
            result.gridUrls = dataSource.getGridImageUrls();
            dataSource.close();
            if (result.carouselUrls.size() > 0 && result.gridUrls.size() > 0) {
                return result;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(PoolSideImageUrls result) {
            if (result != null) {
                Log.d(TAG, "Loaded from local");
                update(result);
                mSwipeRefreshLayout.setRefreshing(false);
            } else {
                Log.d(TAG, "No data in local");
                // No data on local. Start crawling
                reload();
            }
        }
    }

    private void update(PoolSideImageUrls urls) {
        mCarouselUrls.clear();
        mCarouselUrls.addAll(urls.carouselUrls);
        mGridUrls.clear();
        mGridUrls.addAll(urls.gridUrls);

        update();
    }

    private void update() {
        updateCarousel();
        updateGrid();
    }

    private void updateCarousel() {
        mPagerAdapter.setImageUrls(mCarouselUrls);
    }

    private void updateGrid() {
        mGridLayout.removeAllViews();
        for (String gridUrl : mGridUrls) {
            SquareImageView imageView = new SquareImageView(this);
            mGridLayout.addView(imageView);

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1);
            imageView.setLayoutParams(param);

            Picasso.with(this)
                    .load(gridUrl)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.ic_action_picture)
                    .error(R.drawable.ic_action_picture)
                    .into(imageView);
        }
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

    private AlertDialog mDialog;
    public void showErrorDialog() {
        if (mDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.error))
                    .setMessage(getString(R.string.message_image_load_failed))
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            mDialog = null;
                        }
                    });
            mDialog = builder.create();
            mDialog.show();
        }
    }
}
