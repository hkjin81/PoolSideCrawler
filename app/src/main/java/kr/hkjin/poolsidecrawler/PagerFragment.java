package kr.hkjin.poolsidecrawler;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PagerFragment extends Fragment {
    private static final String ARG_IMAGE_URL = "image_url";
    private static final String ARG_LEFT_ENABLE = "left_enable";
    private static final String ARG_RIGHT_ENABLE = "right_enable";

    @BindView(R.id.leftButton)
    Button mLeftButton;
    @BindView(R.id.rightButton)
    Button mRightButton;
    @BindView(R.id.image)
    ImageView mImageView;

    private String mImageUrl;
    private boolean mLeftEnable;
    private boolean mRightEnable;

    private Delegate mDelegate;

    public PagerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imageUrl image url to display.
     * @return A new instance of fragment PagerFragment.
     */
    public static PagerFragment newInstance(String imageUrl, boolean leftEnable, boolean rightEnable) {
        PagerFragment fragment = new PagerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        args.putBoolean(ARG_LEFT_ENABLE, leftEnable);
        args.putBoolean(ARG_RIGHT_ENABLE, rightEnable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImageUrl = getArguments().getString(ARG_IMAGE_URL);
            mLeftEnable = getArguments().getBoolean(ARG_LEFT_ENABLE);
            mRightEnable = getArguments().getBoolean(ARG_RIGHT_ENABLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pager, container, false);
        ButterKnife.bind(this, v);

        if (mLeftEnable) {
            mLeftButton.setVisibility(View.VISIBLE);
        } else {
            mLeftButton.setVisibility(View.INVISIBLE);
        }

        if (mRightEnable) {
            mRightButton.setVisibility(View.VISIBLE);
        } else {
            mRightButton.setVisibility(View.INVISIBLE);
        }

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Delegate) {
            mDelegate = (Delegate) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Delegate");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    @OnClick({R.id.leftButton, R.id.rightButton})
    public void onClick(View view) {
        if (mDelegate != null) {
            switch (view.getId()) {
                case R.id.leftButton:
                    mDelegate.onLeftClicked();
                    break;
                case R.id.rightButton:
                    mDelegate.onRightClicked();
                    break;
            }
        }
    }

    public interface Delegate {
        void onRightClicked();

        void onLeftClicked();
    }
}

