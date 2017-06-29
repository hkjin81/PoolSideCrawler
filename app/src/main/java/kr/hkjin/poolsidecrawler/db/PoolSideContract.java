package kr.hkjin.poolsidecrawler.db;

import android.provider.BaseColumns;

public final class PoolSideContract {
    private PoolSideContract() {}

    public static class CarouselContent implements BaseColumns {
        public static final String TABLE_NAME = "carousel_content";
        public static final String COLUMN_NAME_INDEX = "_index";
        public static final String COLUMN_NAME_IMAGE_URL = "image_url";
    }

    public static class GridContent implements BaseColumns {
        public static final String TABLE_NAME = "grid_content";
        public static final String COLUMN_NAME_INDEX = "_index";
        public static final String COLUMN_NAME_IMAGE_URL = "image_url";
    }
}
