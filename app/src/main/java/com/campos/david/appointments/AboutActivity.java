package com.campos.david.appointments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
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

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = AboutActivity.class.getSimpleName();

    TextureView mTextureView = null;
    MyRenderer mRenderer = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextureView = new TextureView(this);

        setContentView(mTextureView);
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


    private static class MyRenderer extends Thread implements TextureView.SurfaceTextureListener {
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
        private float mCameraSpeed = 0.0f;
        private int mWidth = 0;
        private int mHeight = 0;
        private Paint mPaintTexts;
        private Text[] mTexts;
        private Rect mCameraRect;
        private Point mCameraDestiny = null;
        private boolean mDoConstantDown = true;

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
            synchronized (mCameraLock) {
                float centerX = mCameraRect.width() / 2.0f;
                float centerY = mCameraRect.height() / 2.0f;
                mCameraDestiny = new Point(
                        Math.round(mCameraRect.left + x - centerX),
                        Math.round(mCameraRect.top + y - centerY));
                mDoConstantDown = false;
            }
        }

        public MyRenderer(Context context) {
            this.mContext = context;
        }

        private void initialize() {
            mBackgroundColor = mContext.getResources().getColor(R.color.colorPrimary);
            mPaintTexts = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintTexts.setColor(mContext.getResources().getColor(R.color.textColorOverPrimary));
            mPaintTexts.setTextAlign(Paint.Align.CENTER);
            String licenseInfoStr = GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(mContext);
            String[] licenseInfo;
            float sizeProportion = 6.0f;
            if (licenseInfoStr != null) {
                licenseInfo = licenseInfoStr.split("\n");

                mTexts = new Text[licenseInfo.length + 4]; // Change also in else clause
                int widest = 0;
                for (String aLicenseInfo : licenseInfo) {
                    if (aLicenseInfo.length() > widest)
                        widest = aLicenseInfo.length();
                }
                sizeProportion = (mWidth * 1.1f) / widest;
                for (int i = 0; i < licenseInfo.length; i++) {
                    mTexts[i + 4] = new Text(0, Math.round((15 + 2 * i) * sizeProportion), licenseInfo[i], 2 * sizeProportion);
                }
            } else {
                mTexts = new Text[4]; // Change also in if clause
            }
            mTexts[0] = new Text(0, 0, mContext.getString(R.string.app_name), 4 * sizeProportion);
            mTexts[1] = new Text(0, Math.round(4 * sizeProportion),
                    mContext.getString(R.string.text_author, mContext.getString(R.string.author)), 3 * sizeProportion);
            mTexts[2] = new Text(0, Math.round(7 * sizeProportion),
                    mContext.getString(R.string.author_mail), 3 * sizeProportion);
            mTexts[3] = new Text(0, Math.round(12 * sizeProportion),
                    mContext.getString(R.string.flags_attributions), 2 * sizeProportion);
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
                if (mCameraDestiny != null) {
                    Point distance = new Point(
                            mCameraDestiny.x - mCameraRect.left,
                            mCameraDestiny.y - mCameraRect.top);
                    float speedLength = (float) Math.sqrt(distance.x * distance.x + distance.y * distance.y);
                    Point speed = new Point(
                            Math.round(distance.x / speedLength * mCameraSpeed * millisDelta),
                            Math.round(distance.y / speedLength * mCameraSpeed * millisDelta));

                    // Is speed in some direction more than needed?
                    boolean inX = Math.abs(distance.x) <= Math.abs(speed.x);
                    boolean inY = Math.abs(distance.y) <= Math.abs(speed.y);

                    // Move...
                    if (inX) {
                        mCameraRect.offsetTo(mCameraDestiny.x, mCameraRect.top);
                    } else {
                        mCameraRect.offset(speed.x, 0);
                    }
                    if (inY) {
                        mCameraRect.offsetTo(mCameraRect.left, mCameraDestiny.y);
                    } else {
                        mCameraRect.offset(0, speed.y);
                    }

                    if (inX && inY) {
                        mCameraDestiny = null;
                    }
                } else if (mDoConstantDown) {
                    mCameraRect.offset(0, Math.round(mConstantDownCameraSpeed * millisDelta));
                }
            }
        }

        private void drawFrame(Canvas canvas) {
            canvas.drawColor(mBackgroundColor);
            synchronized (mCameraLock) {
                for (Text text : mTexts) {
                    if (text.text != null) {
                        mPaintTexts.setTextSize(text.size);
                        Point pos = toCameraCoordinates(text.posX, text.posY);
                        float margin = 1.1f * text.size;
                        if (pos.y > -margin && pos.y < mHeight + margin) {
                            canvas.drawText(text.text, pos.x, pos.y, mPaintTexts);
                        }
                    }
                }
                // For debug
//                mPaintTexts.setTextSize(10);
//                Paint.Align oldAlign = mPaintTexts.getTextAlign();
//                mPaintTexts.setTextAlign(Paint.Align.LEFT);
//                String info0, info1;
//                info0 = "Cam(" + mCameraRect.left + ", " + mCameraRect.top + "; " +
//                        mCameraRect.width() + ", " + mCameraRect.height() + ")";
//                if(mCameraDestiny != null) {
//                     info1 = "Dst(" + mCameraDestiny.x + ", " + mCameraDestiny.y + ")";
//                } else {
//                    info1 = "Dst(no, no)";
//                }
//                canvas.drawText(info0, 0, 40, mPaintTexts);
//                canvas.drawText(info1, 0, 50, mPaintTexts);
//                mPaintTexts.setTextAlign(oldAlign);
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
                    mCameraSpeed = width / 1000.0f; // One complete width each second
                    int x = -width / 2;
                    int y = -height / 2;
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
                mCameraSpeed = width / 1000.0f; // One complete width each second
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
