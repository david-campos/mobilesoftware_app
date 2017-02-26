package com.campos.david.appointments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = AboutActivity.class.getSimpleName();

    TextureView mTextureView = null;
    MyRenderer mRenderer = null;

    Point mInitPos;
    private static final String SAVED_POINT_Y = "point-y";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextureView = new TextureView(this);
        setContentView(mTextureView);
        mInitPos = new Point(0, 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVED_POINT_Y, mRenderer.getProportionalCameraPos().y);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mInitPos.y = savedInstanceState.getInt(SAVED_POINT_Y);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRenderer = new MyRenderer(this);
        mTextureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (mRenderer != null) {
                        mRenderer.onScreenClicked(event.getX(), event.getY());
                    }
                }
                return true;
            }
        });
        mTextureView.setSurfaceTextureListener(mRenderer);
        mRenderer.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRenderer != null)
            mRenderer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRenderer != null)
            mRenderer.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRenderer != null)
            mRenderer.onDestroy();
    }


    private class MyRenderer extends Thread implements TextureView.SurfaceTextureListener {
        public static final int STOPPED = 0;
        public static final int WAITING_TO_RUN = 1;
        public static final int RUNNING = 2;
        public static final int WAITING_TO_PAUSE = 3;
        public static final int PAUSED = 4;
        public static final int WAITING_TO_END = 5;
        public static final int ENDED = 6;

        private final Object mSync = new Object();
        public final Object mCameraLock = new Object();

        private int mState = STOPPED;
        private SurfaceTexture mSurface = null;
        private Context mContext;

        private int mBackgroundColor;
        private float mConstantDownCameraSpeed = 0.0f;
        private int mWidth = 0;
        private int mHeight = 0;
        private Paint mPaintTexts;
        private Paint mPaintStains;
        private Bitmap mStainBitmap;
        private int mStainBitmapHalfWidth;
        private int mStainBitmapHalfHeight;
        private Text[] mTexts;
        private ArrayList<int[]> mStains;
        private Rect mCameraRect;
        private boolean mInitialized = false;
        private float mSizeProportion;

        public void onResume() {
            synchronized (mSync) {
                mState = WAITING_TO_RUN;
                mSync.notify();
            }
        }

        public void onPause() {
            synchronized (mSync) {
                mState = WAITING_TO_PAUSE;
                mSync.notify();
            }
        }

        public void onDestroy() {
            synchronized (mSync) {
                mState = WAITING_TO_END;
                mSync.notify();
            }
        }

        public void onScreenClicked(float x, float y) {
            mStains.add(new int[]{
                    Math.round(mCameraRect.left + x),
                    Math.round(mCameraRect.top + y),
                    (int) Math.round(Math.random() * 359), // Rotation
                    (int) Math.round(Math.random() * 100) + 100 // Alpha
            });
        }

        public MyRenderer(Context context) {
            this.mContext = context;
        }

        private void initialize() {
            if (!mInitialized) {
                mBackgroundColor = mContext.getResources().getColor(R.color.colorPrimary);
                mPaintTexts = new Paint(Paint.ANTI_ALIAS_FLAG);
                mPaintTexts.setColor(mContext.getResources().getColor(R.color.textColorOverPrimary));
                mPaintTexts.setTextAlign(Paint.Align.CENTER);
                mPaintStains = new Paint(Paint.ANTI_ALIAS_FLAG);
                mPaintStains.setColorFilter(new PorterDuffColorFilter(
                        mContext.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP));
                mStainBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.stain);
                mStainBitmapHalfWidth = mStainBitmap.getWidth() / 8; // We want to draw it at one quarter scale
                mStainBitmapHalfHeight = mStainBitmap.getHeight() / 8;
                mStains = new ArrayList<>();
                String licenseInfoStr = GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(mContext);
                String[] licenseInfo;
                mSizeProportion = 6.0f;
                if (licenseInfoStr != null) {
                    licenseInfo = licenseInfoStr.split("\n");

                    mTexts = new Text[licenseInfo.length + 4]; // Change also in else clause
                    int widest = 0;
                    for (String aLicenseInfo : licenseInfo) {
                        if (aLicenseInfo.length() > widest)
                            widest = aLicenseInfo.length();
                    }
                    mSizeProportion = (mWidth * 1.1f) / widest;
                    for (int i = 0; i < licenseInfo.length; i++) {
                        mTexts[i + 4] = new Text(0, Math.round((15 + 2 * i) * mSizeProportion), licenseInfo[i], 2 * mSizeProportion);
                    }
                } else {
                    mTexts = new Text[4]; // Change also in if clause
                }
                mTexts[0] = new Text(0, 0, mContext.getString(R.string.app_name), 4 * mSizeProportion);
                mTexts[1] = new Text(0, Math.round(4 * mSizeProportion),
                        mContext.getString(R.string.text_author, mContext.getString(R.string.author)), 3 * mSizeProportion);
                mTexts[2] = new Text(0, Math.round(7 * mSizeProportion),
                        mContext.getString(R.string.author_mail), 3 * mSizeProportion);
                mTexts[3] = new Text(0, Math.round(12 * mSizeProportion),
                        mContext.getString(R.string.flags_attributions), 2 * mSizeProportion);

                // Proportional original position of the camera
                mCameraRect.top = Math.round(mCameraRect.top * mSizeProportion);

                mInitialized = true;
            }
        }

        @Override
        public void run() {
            while (true) {
                // Latch the SurfaceTexture when it becomes available.  We have to wait for
                // the TextureView to create it.
                synchronized (mSync) {
                    if (mState == WAITING_TO_PAUSE) {
                        mState = PAUSED;
                    } else if (mState == WAITING_TO_END) {
                        break;
                    }
                    while (mSurface == null || mState != WAITING_TO_RUN) {
                        try {
                            mSync.wait();
                        } catch (InterruptedException ie) {
                            throw new RuntimeException(ie);     // not expected
                        }
                    }
                    mState = RUNNING;
                }
                doAnimation();
            }
            synchronized (mSync) {
                mState = ENDED;
            }
        }

        private void doAnimation() {
            Surface surface;
            synchronized (mSync) {
                if (mSurface == null) {
                    Log.e(TAG, "Surface null on entry to doAnimation.");
                    return;
                }
                surface = new Surface(mSurface);
            }
            initialize();
            double last = SystemClock.elapsedRealtime();
            // Main loop of animation
            while (true) {
                try {
                    Canvas canvas = surface.lockCanvas(null);
                    try {
                        float millisDelta;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            millisDelta = (float) (SystemClock.elapsedRealtimeNanos() / 1e6f - last);
                            last = SystemClock.elapsedRealtimeNanos() / 1e6f;
                        } else {
                            millisDelta = (float) (SystemClock.elapsedRealtime() - last);
                            last = SystemClock.elapsedRealtime();
                        }
                        if (millisDelta == 0) millisDelta = 1;
                        updateCamera(millisDelta);
                        drawFrame(canvas);
                    } finally {
                        surface.unlockCanvasAndPost(canvas);
                    }
                } catch (IllegalArgumentException iae) {
                    // Do nothing, this should happen only
                    // if surface has been destroyed
                }
                synchronized (mSync) {
                    if (mSurface == null || mState != RUNNING) {
                        break;
                    }
                }
            }
            surface.release();
            // After this loop in run will start again to check
            // status and surface
        }

        private void updateCamera(float millisDelta) {
            synchronized (mCameraLock) {
                mCameraRect.offset(0, Math.round(mConstantDownCameraSpeed * millisDelta));
            }
        }

        public Point getProportionalCameraPos() {
            return new Point(
                    Math.round(mCameraRect.left / mSizeProportion),
                    Math.round(mCameraRect.top / mSizeProportion));
        }

        private void drawFrame(Canvas canvas) {
            canvas.drawColor(mBackgroundColor);
            synchronized (mCameraLock) {
                int drawnTexts = 0;
                for (Text text : mTexts) {
                    if (text.text != null) {
                        mPaintTexts.setTextSize(text.size);
                        Point pos = toCameraCoordinates(text.posX, text.posY);
                        float margin = 1.1f * text.size;
                        if (pos.y > -margin && pos.y < mHeight + margin) {
                            drawnTexts++;
                            canvas.drawText(text.text, pos.x, pos.y, mPaintTexts);
                        }
                    }
                }

                if (drawnTexts == 0) {
                    // If not a single text has been drawn, move to the start
                    mCameraRect.offsetTo(-mWidth / 2, -mHeight / 2);
                    mStains.clear();
                } else {
                    ArrayList<Integer> toRemove = new ArrayList<>();
                    for (int i = 0; i < mStains.size(); i++) {
                        int[] stain = mStains.get(i);
                        Rect stainRect = new Rect(
                                stain[0], stain[1],
                                stain[0] + 2 * mStainBitmapHalfWidth, stain[1] + 2 * mStainBitmapHalfHeight);
                        if (stainRect.intersect(mCameraRect)) {
                            Point pos = toCameraCoordinates(stain[0], stain[1]);
                            canvas.translate(pos.x, pos.y);
                            canvas.rotate(stain[2]);
                            Rect draw = new Rect(-mStainBitmapHalfWidth, -mStainBitmapHalfHeight,
                                    mStainBitmapHalfWidth, mStainBitmapHalfHeight);
                            mPaintStains.setAlpha(stain[3]);
                            canvas.drawBitmap(mStainBitmap, null, draw, mPaintStains);
                            canvas.rotate(-stain[2]);
                            canvas.translate(-pos.x, -pos.y);
                        } else {
                            // If it is out of screen remove it
                            toRemove.add(i);
                        }
                    }
                    for (int i = 0; i < toRemove.size(); i++) {
                        mStains.remove((int) toRemove.get(i));
                    }
                }
            }
        }

        private Point toCameraCoordinates(int posX, int posY) {
            return new Point(
                    posX - mCameraRect.left,
                    posY - mCameraRect.top
            );
        }

        @Override // Called on UI thread
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            synchronized (mSync) {
                mSurface = surface;
                synchronized (mCameraLock) {
                    mConstantDownCameraSpeed = height / 20000.0f; // One height each twenty seconds
                    int x = mInitPos.x - width / 2;
                    int y = mInitPos.y - height / 2;
                    Log.d(TAG, "Initial position " + x + "," + y);
                    mWidth = width;
                    mHeight = height;
                    mCameraRect = new Rect(x, y, x + width, y + height);
                }
                mSync.notify();
            }
        }

        @Override // Called on UI thread
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            synchronized (mCameraLock) {
                mConstantDownCameraSpeed = height / 20000.0f; // One height each twenty seconds
                mWidth = width;
                mHeight = height;
                mCameraRect.set(mCameraRect.left, mCameraRect.top,
                        mCameraRect.left + width, mCameraRect.top + height);
            }
        }

        @Override // Called on UI thread
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            synchronized (mSync) {
                mSurface = null;
                mState = WAITING_TO_PAUSE;
                mSync.notify();
            }
            return true;
        }

        @Override // Called on UI thread
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // Do nothing
        }
    }

    private static class Text {
        public int posX;
        public int posY;
        public String text;
        public float size;

        public Text(int posX, int posY, String text, float size) {
            this.posX = posX;
            this.posY = posY;
            this.text = text;
            this.size = size;
        }
    }
}
