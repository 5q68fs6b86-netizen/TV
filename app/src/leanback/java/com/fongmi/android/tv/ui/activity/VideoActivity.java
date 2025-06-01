package com.fongmi.android.tv.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Outline;
import android.view.ViewOutlineProvider;
import android.graphics.BlurMaskFilter; // <-- Import for Glow Effect
import android.graphics.Paint;       // <-- Import for Glow Effect

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat; // <-- Import for Glow Effect color
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.C;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.SubtitleView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException; // <-- Import for Glide Listener
import com.bumptech.glide.request.RequestListener;    // <-- Import for Glide Listener
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;      // <-- Import for Glide Listener
import com.bumptech.glide.request.transition.Transition;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Episode;
import com.fongmi.android.tv.bean.Flag;
import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.bean.Keep;
import com.fongmi.android.tv.bean.Parse;
import com.fongmi.android.tv.bean.Part;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Sub;
import com.fongmi.android.tv.bean.Track;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.ActivityVideoBinding;
import com.fongmi.android.tv.db.AppDatabase;
import com.fongmi.android.tv.event.ActionEvent;
import com.fongmi.android.tv.event.ErrorEvent;
import com.fongmi.android.tv.event.PlayerEvent;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.model.SiteViewModel;
import com.fongmi.android.tv.player.IjkUtil;
import com.fongmi.android.tv.player.exo.ExoUtil;
import com.fongmi.android.tv.player.Players;
import com.fongmi.android.tv.player.Source;
import com.fongmi.android.tv.player.danmu.Parser;
import com.fongmi.android.tv.ui.adapter.QualityAdapter;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.custom.CustomKeyDownVod;
import com.fongmi.android.tv.ui.custom.CustomMovement;
import com.fongmi.android.tv.ui.dialog.DescDialog;
import com.fongmi.android.tv.ui.dialog.EpisodeDialog;
import com.fongmi.android.tv.ui.dialog.FileChooserDialog;
import com.fongmi.android.tv.ui.dialog.PlayerDialog;
import com.fongmi.android.tv.ui.dialog.SubtitleDialog;
import com.fongmi.android.tv.ui.dialog.TrackDialog;
import com.fongmi.android.tv.ui.presenter.ArrayPresenter;
import com.fongmi.android.tv.ui.presenter.EpisodePresenter;
import com.fongmi.android.tv.ui.presenter.FlagPresenter;
import com.fongmi.android.tv.ui.presenter.ParsePresenter;
import com.fongmi.android.tv.ui.presenter.PartPresenter;
import com.fongmi.android.tv.ui.presenter.QuickPresenter;
import com.fongmi.android.tv.utils.Clock;
import com.fongmi.android.tv.utils.FileChooser;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.KeyUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Sniffer;
import com.fongmi.android.tv.utils.Traffic;
import com.github.bassaer.library.MDColor;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Trans;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.permissionx.guolindev.PermissionX;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import okhttp3.Call;
import okhttp3.Response;
import tv.danmaku.ijk.media.player.ui.IjkVideoView;

// --- Imports for TMDB modification ---
import com.fongmi.android.tv.utils.TmdbHelper;
import android.widget.ImageView;
import android.util.Log;
// --- End Imports ---

public class VideoActivity extends BaseActivity implements CustomKeyDownVod.Listener, TrackDialog.Listener, TrackDialog.ChooserListener, PlayerDialog.Listener, ArrayPresenter.OnClickListener, Clock.Callback {

    private ActivityVideoBinding mBinding;
    private ViewGroup.LayoutParams mFrameParams;
    private EpisodePresenter mEpisodePresenter;
    private ArrayObjectAdapter mEpisodeAdapter;
    private ArrayObjectAdapter mArrayAdapter;
    private ArrayObjectAdapter mParseAdapter;
    private ArrayObjectAdapter mQuickAdapter;
    private ArrayObjectAdapter mFlagAdapter;
    private ArrayObjectAdapter mPartAdapter;
    private QualityAdapter mQualityAdapter;
    private DanmakuContext mDanmakuContext;
    private ArrayPresenter mArrayPresenter;
    private FlagPresenter mFlagPresenter;
    private PartPresenter mPartPresenter;
    private CustomKeyDownVod mKeyDown;
    private ExecutorService mExecutor;
    private SiteViewModel mViewModel;
    private List<String> mBroken;
    private History mHistory;
    private Players mPlayers;
    private boolean background;
    private boolean fullscreen;
    private boolean initTrack;
    private boolean initAuto;
    private boolean autoMode;
    private boolean useParse;
    private int toggleCount;
    private int errorCount;
    private int groupSize;
    private Runnable mR1;
    private Runnable mR2;
    private Runnable mR3;
    private Runnable mR4;
    private Clock mClock;
    private View mFocus1;
    private View mFocus2;
    private boolean hasKeyEvent;
    // --- Variables for TMDB modification ---
    private String currentVodName = "";   // Store original VOD name
    private String currentLogoUrl = null; // Store found TMDB logo URL
    // --- Variables for Glow Effect ---
    private Paint logoGlowPaint = null; // Paint for logo glow effect

    public static void push(FragmentActivity activity, String text) {
        if (FileChooser.isValid(activity, Uri.parse(text))) file(activity, FileChooser.getPathFromUri(activity, Uri.parse(text)));
        else start(activity, Sniffer.getUrl(text));
    }

    public static void file(FragmentActivity activity, String path) {
        if (TextUtils.isEmpty(path)) return;
        String name = new File(path).getName();
        PermissionX.init(activity).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).request((allGranted, grantedList, deniedList) -> start(activity, "push_agent", "file://" + path, name, true));
    }

    public static void cast(Activity activity, History history) {
        start(activity, history.getSiteKey(), history.getVodId(), history.getVodName(), history.getVodPic(), null, true, true, false);
    }

    public static void collect(Activity activity, String key, String id, String name, String pic) {
        start(activity, key, id, name, pic, null, false, false, true);
    }

    public static void start(Activity activity, String url) {
        start(activity, url, true);
    }

    public static void start(Activity activity, String url, boolean clear) {
        start(activity, "push_agent", url, url, clear);
    }

    public static void start(Activity activity, String id, String name, String pic) {
        start(activity, VodConfig.get().getHome().getKey(), id, name, pic);
    }

    public static void start(Activity activity, String key, String id, String name, String pic) {
        start(activity, key, id, name, pic, null, false);
    }

    public static void start(Activity activity, String key, String id, String name, String pic, String mark) {
        start(activity, key, id, name, pic, mark, false);
    }

    public static void start(Activity activity, String key, String id, String name, boolean clear) {
        start(activity, key, id, name, null, null, clear, false, false);
    }

    public static void start(Activity activity, String key, String id, String name, String pic, String mark, boolean clear) {
        start(activity, key, id, name, pic, mark, clear, false, false);
    }

    public static void start(Activity activity, String key, String id, String name, String pic, String mark, boolean clear, boolean cast, boolean collect) {
        Intent intent = new Intent(activity, VideoActivity.class);
        if (clear) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("collect", collect);
        intent.putExtra("cast", cast);
        intent.putExtra("mark", mark);
        intent.putExtra("name", name);
        intent.putExtra("pic", pic);
        intent.putExtra("key", key);
        intent.putExtra("id", id);
        activity.startActivityForResult(intent, 1000);
    }

    private boolean isCast() {
        return getIntent().getBooleanExtra("cast", false);
    }

    private String getName() {
        return Objects.toString(getIntent().getStringExtra("name"), "");
    }

    private String getPic() {
        return Objects.toString(getIntent().getStringExtra("pic"), "");
    }

    private String getMark() {
        return Objects.toString(getIntent().getStringExtra("mark"), "");
    }

    private String getKey() {
        return Objects.toString(getIntent().getStringExtra("key"), "");
    }

    private String getId() {
        return Objects.toString(getIntent().getStringExtra("id"), "");
    }

    private String getHistoryKey() {
        return getKey().concat(AppDatabase.SYMBOL).concat(getId()).concat(AppDatabase.SYMBOL) + VodConfig.getCid();
    }

    private Site getSite() {
        return VodConfig.get().getSite(getKey());
    }

    private Flag getFlag() {
        if (mFlagAdapter == null || mFlagAdapter.size() == 0) return null;
        int position = getFlagPosition();
        if (position >= 0 && position < mFlagAdapter.size()) {
            return (Flag) mFlagAdapter.get(position);
        }
        return (Flag) mFlagAdapter.get(0); // Fallback to first if position invalid
    }

    private Episode getEpisode() {
        if (mEpisodeAdapter == null || mEpisodeAdapter.size() == 0) return null;
        int position = getEpisodePosition();
         if (position >= 0 && position < mEpisodeAdapter.size()) {
             return (Episode) mEpisodeAdapter.get(position);
         }
         return null; // No fallback makes sense here
    }

    private int getFlagPosition() {
        if (mFlagAdapter == null) return 0;
        for (int i = 0; i < mFlagAdapter.size(); i++) {
            Flag flag = (Flag) mFlagAdapter.get(i);
            if (flag != null && flag.isActivated()) return i;
        }
        return 0;
    }

    private int getEpisodePosition() {
        if (mEpisodeAdapter == null) return 0;
        for (int i = 0; i < mEpisodeAdapter.size(); i++) {
            Episode episode = (Episode) mEpisodeAdapter.get(i);
            if (episode != null && episode.isActivated()) return i;
        }
        return 0;
    }

    private int getParsePosition() {
        if (mParseAdapter == null) return 0;
        for (int i = 0; i < mParseAdapter.size(); i++) {
            Parse parse = (Parse) mParseAdapter.get(i);
            if (parse != null && parse.isActivated()) return i;
        }
        return 0;
    }

    private int getPlayer() {
        int sitePlayer = (getSite() != null) ? getSite().getPlayerType() : -1;
        int historyPlayer = (mHistory != null) ? mHistory.getPlayer() : -1;
        if (historyPlayer != -1) return historyPlayer;
        if (sitePlayer != -1) return sitePlayer;
        return Setting.getPlayer();
    }

    private int getScale() {
        int historyScale = (mHistory != null) ? mHistory.getScale() : -1;
        return historyScale != -1 ? historyScale : Setting.getScale();
    }

    private PlayerView getExo() {
        return mBinding.exo;
    }

    private IjkVideoView getIjk() {
        return mBinding.ijk;
    }

    private Drawable getDefaultArtwork() {
        if (mPlayers == null) return null;
        return mPlayers.isExo() ? getExo().getDefaultArtwork() : getIjk().getDefaultArtwork();
    }

    private BaseGridView getEpisodeView() {
        return Setting.getEpisode() == 0 ? mBinding.episodeHori : mBinding.episodeVert;
    }

    private void setEpisodeSelectedPosition(int position) {
        if (getEpisodeView() == null || position < 0) return;
        getEpisodeView().setSelectedPosition(position);
        if (hasKeyEvent) return;
        if (isFullscreen()) return;
        getEpisodeView().postDelayed(() -> {
             if (getEpisodeView() == null || getEpisodeView().getLayoutManager() == null) return;
            View selectedItem = getEpisodeView().getLayoutManager().findViewByPosition(position);
            View focusedView = getCurrentFocus();
            if (selectedItem != null) selectedItem.requestFocus();
            if (focusedView == mBinding.video && mBinding.video != null) mBinding.video.requestFocus();
        }, 300);
    }

    private boolean isReplay() {
        return Setting.getReset() == 1;
    }

    private boolean isFromCollect() {
        return getIntent().getBooleanExtra("collect", false);
    }

    @Override
    protected ViewBinding getBinding() {
        mBinding = ActivityVideoBinding.inflate(getLayoutInflater());
        return mBinding;
    }

    @Override
    protected void initView() {
        mKeyDown = CustomKeyDownVod.create(this, mBinding.video);
        mFrameParams = mBinding.video.getLayoutParams();
        mBinding.video.setBackgroundResource(R.drawable.rounded_corners);
        final float cornerRadius = ResUtil.dp2px(8);
        mBinding.video.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (view != null && outline != null) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
                }
            }
        });
        mBinding.video.setClipToOutline(true);
        mClock = Clock.create(mBinding.display.clock);
        mDanmakuContext = DanmakuContext.create();
        mPlayers = Players.create(this);
        mBroken = new ArrayList<>();
        mR1 = this::hideControl;
        mR2 = this::updateFocus;
        mR3 = this::setTraffic;
        mR4 = this::showEmpty;

        initGlowPaint(); // Initialize Paint for glow effect

        setBackground(false);
        setRecyclerView();
        setEpisodeView();
        setVideoView();
        setDisplayView();
        setDanmuView();
        setViewModel();
        checkCast();
        checkId();
    }

    // --- Method to initialize Paint for Glow Effect ---
    private void initGlowPaint() {
        if (logoGlowPaint == null) {
            try {
                logoGlowPaint = new Paint();
                logoGlowPaint.setColor(ContextCompat.getColor(this, R.color.logo_glow_color));
                float glowRadius = getResources().getDimensionPixelSize(R.dimen.logo_glow_radius);
                // Use Blur.OUTER to draw only the blur outside the shape
                logoGlowPaint.setMaskFilter(new BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.OUTER));
            } catch (Exception e) {
                Log.e("VideoActivity", "Error initializing glow paint", e);
                logoGlowPaint = null; // Ensure paint is null if init fails
            }
        }
    }
    // --- End Method ---

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initEvent() {
         if (mBinding == null) return; // Safety check

        mBinding.control.seek.setListener(mPlayers);
        mBinding.desc.setOnClickListener(view -> onDesc());
        mBinding.keep.setOnClickListener(view -> onKeep());
        mBinding.video.setOnClickListener(view -> onVideo());
        mBinding.change1.setOnClickListener(view -> onChange());
        mBinding.control.text.setOnClickListener(this::onTrack);
        mBinding.control.audio.setOnClickListener(this::onTrack);
        mBinding.control.video.setOnClickListener(this::onTrack);
        mBinding.control.speed.setUpListener(this::onSpeedAdd);
        mBinding.control.speed.setDownListener(this::onSpeedSub);
        mBinding.control.ending.setUpListener(this::onEndingAdd);
        mBinding.control.ending.setDownListener(this::onEndingSub);
        mBinding.control.opening.setUpListener(this::onOpeningAdd);
        mBinding.control.opening.setDownListener(this::onOpeningSub);
        mBinding.control.text.setUpListener(this::onSubtitleClick);
        mBinding.control.text.setDownListener(this::onSubtitleClick);
        mBinding.control.loop.setOnClickListener(view -> onLoop());
        mBinding.control.danmu.setOnClickListener(view -> onDanmu());
        mBinding.control.danmu.setUpListener(this::onDanmuAdd);
        mBinding.control.danmu.setDownListener(this::onDanmuSub);
        mBinding.control.next.setOnClickListener(view -> checkNext());
        mBinding.control.prev.setOnClickListener(view -> checkPrev());
        mBinding.control.episodes.setOnClickListener(view -> onEpisodes());
        mBinding.control.scale.setOnClickListener(view -> onScale());
        mBinding.control.speed.setOnClickListener(view -> onSpeed());
        mBinding.control.reset.setOnClickListener(view -> onReset());
        mBinding.control.player.setOnClickListener(view -> onPlayer());
        mBinding.control.decode.setOnClickListener(view -> onDecode());
        mBinding.control.ending.setOnClickListener(view -> onEnding());
        mBinding.control.opening.setOnClickListener(view -> onOpening());
        mBinding.control.player.setOnLongClickListener(view -> onChoose());
        mBinding.control.speed.setOnLongClickListener(view -> onSpeedLong());
        mBinding.control.reset.setOnLongClickListener(view -> onResetToggle());
        mBinding.control.ending.setOnLongClickListener(view -> onEndingReset());
        mBinding.control.opening.setOnLongClickListener(view -> onOpeningReset());
        mBinding.video.setOnTouchListener((view, event) -> mKeyDown.onTouchEvent(event));

        if (mBinding.flag != null) {
            mBinding.flag.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
                 @Override
                 public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                      if (mFlagAdapter != null && mFlagAdapter.size() > 0 && position >= 0 && position < mFlagAdapter.size()) {
                           setFlagActivated((Flag) mFlagAdapter.get(position));
                      }
                 }
            });
        }

        BaseGridView episodeView = getEpisodeView();
        if (episodeView != null) {
            episodeView.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
                 @Override
                 public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                      if (child != null) mFocus1 = child.itemView;
                      setEpisodeChildKeyListener(child, position);
                 }
            });
        }

        if (mBinding.array != null) {
            mBinding.array.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
                @Override
                public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                    if (mEpisodeAdapter != null && mEpisodeAdapter.size() > getGroupSize() && position > 1 && hasKeyEvent) {
                        setEpisodeSelectedPosition((position - 2) * getGroupSize());
                    }
                }
            });
        }
    }

    private void setEpisodeChildKeyListener(RecyclerView.ViewHolder child, int position) {
        BaseGridView episodeView = getEpisodeView();
        if (episodeView == null || episodeView != mBinding.episodeVert || child == null || child.itemView == null) return;
        RecyclerView.Adapter<?> adapter = episodeView.getAdapter();
        if (adapter == null) return;
        int itemCount = adapter.getItemCount();
        if (itemCount <= 0 || mEpisodePresenter == null) return;

        int columns = mEpisodePresenter.getNumColumns();
        if (columns <= 0) return; // Avoid division by zero or invalid state

        // Check if it's the last row and if the item's column index exceeds the count in the last row
        boolean isLastRow = (position / columns) == ((itemCount - 1) / columns);
        boolean isBeyondLastRowItem = (position % columns) >= (itemCount % columns) && (itemCount % columns != 0); // More precise check

        if (isLastRow && isBeyondLastRowItem) {
            child.itemView.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                     if (episodeView.getLayoutManager() != null) {
                          View lastItem = episodeView.getLayoutManager().findViewByPosition(itemCount - 1);
                          if (lastItem != null) lastItem.requestFocus();
                          return true; // Consume the event
                     }
                }
                return false;
            });
        } else {
             // Remove listener if not applicable anymore
             child.itemView.setOnKeyListener(null);
        }
    }


    private void setRecyclerView() {
        if (mBinding == null) return; // Safety check

        mFlagAdapter = new ArrayObjectAdapter(mFlagPresenter = new FlagPresenter(this::setFlagActivated));
        mBinding.flag.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.flag.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.flag.setAdapter(new ItemBridgeAdapter(mFlagAdapter));

        mQualityAdapter = new QualityAdapter(this::setQualityActivated);
        mBinding.quality.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.quality.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.quality.setAdapter(mQualityAdapter); // Directly set adapter

        mArrayAdapter = new ArrayObjectAdapter(mArrayPresenter = new ArrayPresenter(this));
        mBinding.array.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.array.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.array.setAdapter(new ItemBridgeAdapter(mArrayAdapter));

        mPartAdapter = new ArrayObjectAdapter(mPartPresenter = new PartPresenter(item -> initSearch(item, false)));
        mBinding.part.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.part.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.part.setAdapter(new ItemBridgeAdapter(mPartAdapter));

        mQuickAdapter = new ArrayObjectAdapter(new QuickPresenter(this::setSearch));
        mBinding.quick.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.quick.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.quick.setAdapter(new ItemBridgeAdapter(mQuickAdapter));

        mParseAdapter = new ArrayObjectAdapter(new ParsePresenter(this::setParseActivated));
        mBinding.control.parse.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.control.parse.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.control.parse.setAdapter(new ItemBridgeAdapter(mParseAdapter));
        mParseAdapter.setItems(VodConfig.get().getParses(), null);
    }

    private void setEpisodeView() {
         if (mBinding == null) return; // Safety check

        mEpisodeAdapter = new ArrayObjectAdapter(mEpisodePresenter = new EpisodePresenter(this::setEpisodeActivated));
        mBinding.episodeVert.setVerticalSpacing(ResUtil.dp2px(8));
        mBinding.episodeHori.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.episodeVert.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.episodeHori.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        BaseGridView episodeView = getEpisodeView();
        if (episodeView != null) {
             episodeView.setAdapter(new ItemBridgeAdapter(mEpisodeAdapter));
        }
    }

    private void setVideoView() {
         if (mBinding == null || mPlayers == null) return; // Safety check

        mPlayers.init(getExo(), getIjk());
        ExoUtil.setSubtitleView(mBinding.exo);
        IjkUtil.setSubtitleView(mBinding.ijk);
        mBinding.control.reset.setText(ResUtil.getStringArray(R.array.select_reset)[Setting.getReset()]);
        mBinding.exo.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        mBinding.ijk.setBackgroundColor(android.graphics.Color.TRANSPARENT);
    }

    private void setDanmuViewSettings() {
        if (mDanmakuContext == null) return;
        float[] range = {2.4f, 1.8f, 1.2f, 0.8f};
        float speed = range[Setting.getDanmuSpeed()];
        float alpha = Setting.getDanmuAlpha() / 100.0f;
        float sizeScale = isFullscreen() ? 1.2f * Setting.getDanmuSize() : 0.8f * Setting.getDanmuSize();
        int maxLine = Setting.getDanmuLine(3);
        HashMap<Integer, Integer> maxLines = new HashMap<>();
        maxLines.put(BaseDanmaku.TYPE_FIX_TOP, maxLine);
        maxLines.put(BaseDanmaku.TYPE_SCROLL_RL, maxLine);
        maxLines.put(BaseDanmaku.TYPE_SCROLL_LR, maxLine);
        maxLines.put(BaseDanmaku.TYPE_FIX_BOTTOM, maxLine);
        mDanmakuContext.setMaximumLines(maxLines).setScrollSpeedFactor(speed).setDanmakuTransparency(alpha).setScaleTextSize(sizeScale);
    }

    private void setDanmuView() {
        if (mBinding == null || mPlayers == null || mDanmakuContext == null) return; // Safety check
        mPlayers.setDanmuView(mBinding.danmaku);
        setDanmuViewSettings();
        mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDanmakuMargin(8);
        mBinding.control.danmu.setActivated(Setting.isDanmu());
    }

    private void setDisplayView() {
         if (mBinding == null) return; // Safety check
        mBinding.display.getRoot().setVisibility(View.VISIBLE);
        showDisplayInfo();
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(SiteViewModel.class);
        mViewModel.result.observe(this, this::setDetail);
        mViewModel.player.observe(this, this::setPlayer);
        mViewModel.search.observe(this, this::setSearch);
    }

    private void checkCast() {
        if (isCast()) onVideo();
        else if (mBinding != null) mBinding.progressLayout.showProgress();
    }

    private void checkId() {
        String id = getId();
        if (id.startsWith("push://")) {
            getIntent().putExtra("key", "push_agent").putExtra("id", id.substring(7));
            id = getIntent().getStringExtra("id"); // Update id after modification
        }
        if (TextUtils.isEmpty(id) || id.startsWith("msearch:")) {
            setEmpty(false);
        } else {
            getDetail();
        }
    }

    private void setPlayerView() {
        if (mBinding == null || mPlayers == null) return; // Safety check

        getIjk().setPlayer(mPlayers.getPlayer());
        mBinding.control.player.setText(mPlayers.getPlayerText());
        mBinding.control.speed.setEnabled(mPlayers.canAdjustSpeed());
        getExo().setVisibility(mPlayers.isExo() ? View.VISIBLE : View.GONE);
        getIjk().setVisibility(mPlayers.isIjk() ? View.VISIBLE : View.GONE);
        if (mHistory != null) {
            mBinding.control.speed.setText(mPlayers.setSpeed(mHistory.getSpeed()));
        } else {
             // Set default speed text if history is null
             mBinding.control.speed.setText(mPlayers.setSpeed(Setting.getPlaySpeed()));
        }
    }

    private void setDecodeView() {
        if (mBinding == null || mPlayers == null) return; // Safety check
        mBinding.control.decode.setText(mPlayers.getDecodeText());
    }

    private void setScale(int scale) {
        if (mBinding == null || scale < 0 || scale >= ResUtil.getStringArray(R.array.select_scale).length) return; // Safety check
        getExo().setResizeMode(scale);
        getIjk().setResizeMode(scale);
        mBinding.control.scale.setText(ResUtil.getStringArray(R.array.select_scale)[scale]);
    }

    private void getDetail() {
        if (mViewModel == null || TextUtils.isEmpty(getKey()) || TextUtils.isEmpty(getId())) return;
        mViewModel.detailContent(getKey(), getId());
    }

    private void getDetail(Vod item) {
        if (item == null || TextUtils.isEmpty(item.getSiteKey()) || TextUtils.isEmpty(item.getVodId())) return;
        getIntent().putExtra("key", item.getSiteKey());
        getIntent().putExtra("pic", item.getVodPic());
        getIntent().putExtra("id", item.getVodId());
        if (mBinding != null) mBinding.scroll.scrollTo(0, 0);
        if (mClock != null) mClock.setCallback(null);
        if (mPlayers != null) {
            mPlayers.reset();
            mPlayers.stop();
        }
        getDetail();
    }

    private void setDetail(Result result) {
         if (mBinding == null) return; // Safety check
        if (result == null || result.getList().isEmpty()) {
            setEmpty(result != null && result.hasMsg());
        } else {
            setDetail(result.getList().get(0));
        }
        Notify.show(result != null ? result.getMsg() : "");
    }

    // --- Modified getPlayer ---
    private void getPlayer(Flag flag, Episode episode, boolean replay) {
        if (mBinding == null || mViewModel == null || mPlayers == null || flag == null || episode == null) return; // Safety check

        // Use currentVodName (original name) for titles
        String combinedTitle = getString(R.string.detail_title, currentVodName, episode.getName());
        mBinding.widget.title.setText(combinedTitle); // Set widget title with combined text
        mBinding.display.title.setText(combinedTitle); // Set display title with combined text

        mViewModel.playerContent(getKey(), flag.getFlag(), episode.getUrl());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        updateHistory(episode, replay);
        mPlayers.clear();
        mPlayers.stop();
        showProgress();
        setMetadata(); // Metadata uses original name from history
        hidePreview();
        hideCenter();
    }
    // --- End Modified getPlayer ---

    private void setPlayer(Result result) {
        if (mBinding == null || mPlayers == null || mQualityAdapter == null || result == null) return; // Safety check
        Site site = getSite();
        if (site == null) return; // Need site info

        result.getUrl().set(mQualityAdapter.getPosition());
        setUseParse(VodConfig.hasParse() && ((result.getPlayUrl().isEmpty() && VodConfig.get().getFlags().contains(result.getFlag())) || result.getJx() == 1));
        mPlayers.start(result, isUseParse(), site.isChangeable() ? site.getTimeout() : -1);
        mBinding.control.parse.setVisibility(isUseParse() ? View.VISIBLE : View.GONE);
        setQualityVisible(result.getUrl().isMulti());
        checkDanmu(result.getDanmaku());
        mQualityAdapter.addAll(result);
    }

    private void checkDanmu(String danmu) {
        if (mBinding == null || mDanmakuContext == null) return; // Safety check
        mBinding.danmaku.release();
        if (!Setting.isDanmuLoad() || TextUtils.isEmpty(danmu)) {
             mBinding.danmaku.setVisibility(View.GONE);
             return;
        }
        mBinding.danmaku.setVisibility(View.VISIBLE);
        App.execute(() -> {
            if (mBinding != null) { // Check binding again in background thread
                 mBinding.danmaku.prepare(new Parser(danmu), mDanmakuContext);
            }
        });
    }

    private void setEmpty(boolean finish) {
        if (isFromCollect() || finish) {
            finish();
        } else if (getName().isEmpty()) {
            showEmpty();
        } else {
            // Fallback: Show name from intent, hide logo, start search
            if (mBinding != null) {
                mBinding.logoImageView.setVisibility(View.GONE);
                mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null); // Ensure no glow
                mBinding.nameTextView.setText(getName());
                mBinding.nameTextView.setVisibility(View.VISIBLE);
                App.post(mR4, 10000);
            }
            checkSearch(false); // Start search based on the name from intent
        }
    }

    private void showEmpty() {
        if (mBinding != null) mBinding.progressLayout.showEmpty();
        stopSearch();
    }

    // --- Modified setDetail(Vod item) ---
    private void setDetail(Vod item) {
        if (mBinding == null || item == null) { // Safety check
             setEmpty(true); // Consider it an error if item is null
             return;
        }

        mBinding.progressLayout.showContent();
        mBinding.video.setTag(item.getVodPic(getPic()));

        // 1. Store original VOD name
        currentVodName = item.getVodName(getName());
        currentLogoUrl = null; // Reset logo url

        // 2. Initial title state: show fallback text, hide logo, remove glow
        mBinding.logoImageView.setVisibility(View.GONE);
        mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null);
        mBinding.nameTextView.setVisibility(View.VISIBLE);
        mBinding.nameTextView.setText(currentVodName); // Display text title initially

        // Set other details
        setText(mBinding.remark, 0, item.getVodRemarks());
        setText(mBinding.year, R.string.detail_year, item.getVodYear());
        setText(mBinding.area, R.string.detail_area, item.getVodArea());
        setText(mBinding.type, R.string.detail_type, item.getTypeName());
        Site site = getSite();
        setText(mBinding.site, R.string.detail_site, site != null ? site.getName() : "");
        setText(mBinding.actor, R.string.detail_actor, Html.fromHtml(Objects.toString(item.getVodActor(), "")).toString());
        setText(mBinding.content, R.string.detail_content, Html.fromHtml(Objects.toString(item.getVodContent(), "")).toString());
        setText(mBinding.director, R.string.detail_director, Html.fromHtml(Objects.toString(item.getVodDirector(), "")).toString());

        if (mFlagAdapter != null) mFlagAdapter.setItems(item.getVodFlags(), null);
        mBinding.content.setMaxLines(getMaxLines());
        setArtwork(item.getVodPic());
        getPart(item.getVodName()); // Use original name for related search
        App.removeCallbacks(mR4);

        // 4. Call TMDB Logo fetch logic
        fetchTmdbLogo(currentVodName, item.getVodYear(), item.getTypeName());

        checkHistory(item); // Uses original name stored in item/history
        checkFlag(item);
        checkKeep(); // Uses history key which includes original ID
    }
    // --- End Modified setDetail ---

    // --- fetchTmdbLogo Method with Glow Effect ---
    private void fetchTmdbLogo(String title, String year, String typeName) {
        if (mBinding == null) return; // Safety Check

        // API Key Check
        if (TextUtils.isEmpty(Constant.TMDB_API_KEY) || "YOUR_TMDB_API_KEY_HERE".equals(Constant.TMDB_API_KEY)) {
            Log.e("VideoActivity", "TMDB API Key not set! Skipping logo fetch.");
            if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) return;
            // Ensure UI is in text mode without glow
            mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null);
            mBinding.logoImageView.setVisibility(View.GONE);
            mBinding.nameTextView.setVisibility(View.VISIBLE);
            mBinding.nameTextView.setText(currentVodName);
            return;
        }

        TmdbHelper.findLogoForVod(title, year, typeName, new TmdbHelper.LogoCallback() {
            @Override
            public void onLogoFound(@NonNull String logoUrl) {
                if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                    Log.w("VideoActivity", "Activity is finishing or destroyed in onLogoFound, skipping Glide load.");
                    return;
                }
                if (mBinding == null) return; // Double check binding

                currentLogoUrl = logoUrl;
                mBinding.nameTextView.setVisibility(View.GONE);
                mBinding.logoImageView.setVisibility(View.VISIBLE);

                // Clear any previous layer before loading new image
                 mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null);

                try {
                    int targetPixelHeight = getResources().getDimensionPixelSize(R.dimen.detail_title_area_height);
                     // Make width proportional, e.g., 4:1 ratio, adjust as needed
                    int targetPixelWidth = targetPixelHeight * 4; // Adjusted ratio
                    Log.d("VideoActivity", "Glide override target size: " + targetPixelWidth + "x" + targetPixelHeight);

                    Glide.with(VideoActivity.this)
                         .load(logoUrl)
                         .placeholder(R.drawable.ic_placeholder) // Placeholder shouldn't have glow
                         .error(R.drawable.ic_error)           // Error shouldn't have glow
                         .override(targetPixelWidth, targetPixelHeight)
                         .fitCenter() // Use fitCenter for logos
                         .listener(new RequestListener<Drawable>() { // Use Glide listener
                             @Override
                             public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                 // Load failed, ensure no glow layer and fallback UI
                                 if (mBinding != null) {
                                     mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null);
                                 }
                                 onLogoNotFound(); // Handles UI fallback
                                 return false; // Let Glide handle the error drawable
                             }

                             @Override
                             public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, @NonNull Target<Drawable> target, @NonNull com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                 // Load succeeded, apply glow effect
                                 if (mBinding != null) {
                                     initGlowPaint(); // Ensure paint is ready
                                     if (logoGlowPaint != null) { // Check if paint init succeeded
                                          mBinding.logoImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, logoGlowPaint);
                                     } else {
                                          mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null); // Fallback if paint failed
                                     }
                                 }
                                 return false; // Let Glide set the resource to the ImageView
                             }
                         })
                         .into(mBinding.logoImageView);
                } catch (Exception e) {
                    Log.e("VideoActivity", "Error during Glide load setup or execution in onLogoFound", e);
                     // Ensure no glow layer on exception and fallback UI
                     if (mBinding != null) {
                         mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null);
                     }
                    onLogoNotFound();
                }
            }

            @Override
            public void onLogoNotFound() {
                if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                    Log.w("VideoActivity", "Activity is finishing or destroyed in onLogoNotFound, skipping UI update.");
                    return;
                }
                 if (mBinding == null) return; // Double check binding

                currentLogoUrl = null;
                // Ensure no glow layer
                mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null);
                mBinding.logoImageView.setVisibility(View.GONE);
                mBinding.nameTextView.setVisibility(View.VISIBLE);
                mBinding.nameTextView.setText(currentVodName);
            }

            @Override
            public void onError() {
                if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                    Log.w("VideoActivity", "Activity is finishing or destroyed in onError, skipping UI update.");
                    return;
                }
                Log.w("VideoActivity", "Error fetching TMDB logo for: " + title);
                // Ensure no glow layer and fallback UI (onLogoNotFound handles this)
                 if (mBinding != null) {
                      mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null);
                 }
                onLogoNotFound();
            }
        });
    }
    // --- End fetchTmdbLogo Method ---


    private int getMaxLines() {
        if (mBinding == null) return 1;
        int lines = 1;
        if (isGone(mBinding.actor)) ++lines;
        if (isGone(mBinding.remark)) ++lines;
        if (isGone(mBinding.director)) ++lines;
        return lines;
    }

    private void setText(TextView view, int resId, String text) {
        if (view == null) return; // Add null checks
        String displayText = (text == null) ? "" : text; // Ensure text is not null

        view.setText(getSpan(resId, displayText), TextView.BufferType.SPANNABLE);
        view.setVisibility(displayText.isEmpty() ? View.GONE : View.VISIBLE);
        view.setLinkTextColor(MDColor.WHITE); // Use a defined white color if possible
        CustomMovement.bind(view);
        view.setTag(displayText); // Store the original text
    }

    private SpannableStringBuilder getSpan(int resId, String text) {
        if (text == null) text = ""; // Handle null text
        String processedText = (resId > 0) ? getString(resId, text) : text;
        Map<String, String> map = new HashMap<>();
        try {
            Matcher m = Sniffer.CLICKER.matcher(processedText);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String group1 = m.group(1);
                String group2 = m.group(2);
                if (group1 != null && group2 != null) {
                    String key = Trans.s2t(group2).trim(); // Use Trans for consistency if needed
                    String replacementKey = "{$" + map.size() + "}"; // Unique placeholder
                    map.put(replacementKey, group1);
                    m.appendReplacement(sb, Matcher.quoteReplacement(key)); // Replace with display text
                    // Store the original URL mapped to the placeholder
                } else {
                     m.appendReplacement(sb, m.group()); // Append as is if groups are null
                }
            }
            m.appendTail(sb);
            processedText = sb.toString();
        } catch (Exception e) {
             Log.e("VideoActivity", "Error processing span text: " + text, e);
             // Fallback to original text if regex fails
        }


        SpannableStringBuilder span = SpannableStringBuilder.valueOf(processedText);
        // Re-iterate based on the replaced text and placeholders if the above map approach is complex
        // Simpler approach: Iterate again on the final text searching for the keys (less robust if keys overlap)
        try {
            // This part assumes the map keys (Trans.s2t(group2).trim()) are present in the final processedText
             for (Map.Entry<String, String> entry : map.entrySet()) { // Need the original map logic back
                String displayTextKey = entry.getKey(); // This was the display text (e.g., Trans.s2t(group2).trim())
                String targetUrl = entry.getValue(); // This was group1 (the URL)

                int index = processedText.indexOf(displayTextKey);
                 while (index != -1) {
                     if (index >= 0 && index + displayTextKey.length() <= span.length()) {
                          Result result = Result.type(targetUrl);
                          ClickableSpan clickableSpan = getClickSpan(result);
                          if (clickableSpan != null) {
                               span.setSpan(clickableSpan, index, index + displayTextKey.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                          }
                     }
                      index = processedText.indexOf(displayTextKey, index + 1); // Find next occurrence
                 }
             }
        } catch (Exception e) {
             Log.e("VideoActivity", "Error applying spans: " + processedText, e);
        }

        return span;
    }


    private ClickableSpan getClickSpan(Result result) {
        if (result == null) return null;
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                VodActivity.start(getActivity(), getKey(), result);
            }
            // Optional: Override updateDrawState if you want to change text appearance (e.g., underline, color)
            // @Override
            // public void updateDrawState(@NonNull TextPaint ds) {
            //     super.updateDrawState(ds);
            //     ds.setUnderlineText(false); // Example: remove underline
            // }
        };
    }


    private void setFlagActivated(Flag item) {
        if (mBinding == null || mFlagAdapter == null || mFlagAdapter.size() == 0 || item == null || item.isActivated()) return;

        int itemIndex = mFlagAdapter.indexOf(item);
        if (itemIndex == -1) {
            // Item not found, maybe activate the first one?
             Flag firstFlag = (Flag) mFlagAdapter.get(0);
             if (firstFlag != null) {
                  item = firstFlag; // Activate the first one instead
                  itemIndex = 0;
             } else {
                  return; // No flags available
             }
        }

        for (int i = 0; i < mFlagAdapter.size(); i++) {
             Flag flag = (Flag) mFlagAdapter.get(i);
             if (flag != null) flag.setActivated(flag == item); // Use object comparison
        }

        mBinding.flag.setSelectedPosition(itemIndex);
        notifyItemChanged(mBinding.flag, mFlagAdapter);
        setEpisodeAdapter(item.getEpisodes());
        setQualityVisible(false);
        seamless(item);
    }


    private void setEpisodeAdapter(List<Episode> items) {
        BaseGridView episodeView = getEpisodeView();
        if (episodeView == null || mEpisodeAdapter == null) return;

        boolean isEmpty = (items == null || items.isEmpty());
        episodeView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
             mEpisodeAdapter.clear(); // Clear adapter if items are empty
             setArrayAdapter(0); // Update array adapter for episode ranges
             return;
        }

        if (isVisible(mBinding.episodeVert)) setEpisodeViewLayout(items); // Adjust layout only for vertical view
        mEpisodeAdapter.setItems(items, null);
        setArrayAdapter(items.size());
        setR2Callback(50); // Update focus rules after layout change
    }

    // Renamed from setEpisodeView to avoid confusion with the getter
    private void setEpisodeViewLayout(List<Episode> items) {
        if (items == null || items.isEmpty() || mBinding == null || mEpisodePresenter == null) return; // Safety checks

        int size = items.size();
        int maxEpisodeNameLength = 0;

        // Calculate max length and set index
        for (int i = 0; i < size; i++) {
            Episode episode = items.get(i);
            if (episode == null) continue;
            episode.setIndex(i);
            String name = episode.getName();
            int length = (name == null) ? 0 : name.length();
            if (length > maxEpisodeNameLength) maxEpisodeNameLength = length;
        }

        // Determine number of columns based on max length
        int numColumns = 10; // Default
        if (maxEpisodeNameLength > 40) numColumns = 1;
        else if (maxEpisodeNameLength > 30) numColumns = 2;
        else if (maxEpisodeNameLength > 15) numColumns = 3;
        else if (maxEpisodeNameLength > 10) numColumns = 4;
        else if (maxEpisodeNameLength > 6) numColumns = 6;
        else if (maxEpisodeNameLength > 4) numColumns = 8;

        if (numColumns <= 0) numColumns = 1; // Ensure at least one column

        int rowNum = (int) Math.ceil((double) size / numColumns);
        int screenWidth = ResUtil.getScreenWidth();
        // Calculate available width considering padding
        int availableWidth = screenWidth - ResUtil.dp2px(48); // Assuming 24dp padding on each side

        ViewGroup.LayoutParams params = mBinding.episodeVert.getLayoutParams();
        if (params == null) {
             params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        params.width = screenWidth; // Span full width
        // Adjust height based on rows, ensure minimum height if needed
        params.height = (rowNum > 6) ? ResUtil.dp2px(300) : ResUtil.dp2px(Math.max(rowNum, 1) * 44); // Ensure at least 1 row height

        mBinding.episodeVert.setLayoutParams(params);
        mBinding.episodeVert.setNumColumns(numColumns);

        // Calculate column width based on available space and spacing
        int horizontalSpacing = ResUtil.dp2px(8);
        int columnWidth = (availableWidth - ((numColumns - 1) * horizontalSpacing)) / numColumns;
        if (columnWidth > 0) {
             mBinding.episodeVert.setColumnWidth(columnWidth);
        } else {
             // Fallback if calculation is wrong
             mBinding.episodeVert.setColumnWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Adjust alignment offset if needed, 10f might be too small
        mBinding.episodeVert.setWindowAlignmentOffsetPercent(35f); // Default Leanback alignment

        mEpisodePresenter.setNumColumns(numColumns);
        mEpisodePresenter.setNumRows(rowNum); // Set row count for potential focus logic
    }


    private void seamless(Flag flag) {
        if (flag == null || mHistory == null) return;
        Episode episode = flag.find(mHistory.getVodRemarks(), getMark().isEmpty());

        boolean shouldShowQuality = (episode != null && episode.isActivated() && mQualityAdapter != null && mQualityAdapter.getItemCount() > 1);
        setQualityVisible(shouldShowQuality);

        if (episode == null || episode.isActivated()) return;

        // Seamless logic based on setting
        if (Setting.getFlag() == 1) { // Focus episode without playing
            episode.setActivated(true); // Temporarily activate to find position
            int position = getEpisodePosition();
            episode.setActivated(false); // Deactivate again

            if (!isFullscreen()) {
                BaseGridView episodeView = getEpisodeView();
                 if (episodeView != null) episodeView.requestFocus();
            }
            setEpisodeSelectedPosition(position);
        } else { // Play the found episode
            mHistory.setVodRemarks(episode.getName()); // Update history remark before playing
            setEpisodeActivated(episode); // This will trigger play
            hidePreview(); // Hide preview when auto-playing seamless episode
        }
    }


    public void setEpisodeActivated(Episode item) {
        if (item == null || mFlagAdapter == null || mEpisodeAdapter == null) return; // Safety checks

        int flagPosition = getFlagPosition();
        if (flagPosition < 0 || flagPosition >= mFlagAdapter.size()) return; // Invalid flag position

        if (shouldEnterFullscreen(item)) return; // Enter fullscreen if needed

        if (isFullscreen()) Notify.show(getString(R.string.play_ready, item.getName()));

        // Update activation state in all flags' episode lists
        for (int i = 0; i < mFlagAdapter.size(); i++) {
            Flag flag = (Flag) mFlagAdapter.get(i);
            if (flag != null) {
                flag.toggle(i == flagPosition, item); // Activate in current flag, deactivate in others
            }
        }

        // Update UI
        int episodePosition = mEpisodeAdapter.indexOf(item);
        if (episodePosition != -1) {
             setEpisodeSelectedPosition(episodePosition);
        }
        notifyItemChanged(getEpisodeView(), mEpisodeAdapter);
        onRefresh(); // Trigger player refresh/reload
    }


    private void setQualityVisible(boolean visible) {
        if (mBinding != null) {
             mBinding.quality.setVisibility(visible ? View.VISIBLE : View.GONE);
             setR2Callback(100); // Update focus
        }
    }

    private void setQualityActivated(Result result) {
        if (result == null || mPlayers == null || mBinding == null) return; // Safety check
        Site site = getSite();
        if (site == null) return;

        try {
            mPlayers.start(result, isUseParse(), site.isChangeable() ? site.getTimeout() : -1);
            mBinding.danmaku.hide(); // Hide danmaku when switching quality
        } catch (Exception e) {
            Log.e("VideoActivity", "Error setting quality", e);
            ErrorEvent.post(e); // Post error event
        }
    }

    private void reverseEpisode(boolean scroll) {
        if (mFlagAdapter == null) return;
        for (int i = 0; i < mFlagAdapter.size(); i++) {
            Flag flag = (Flag) mFlagAdapter.get(i);
            if (flag != null && flag.getEpisodes() != null) {
                Collections.reverse(flag.getEpisodes());
            }
        }
        Flag currentFlag = getFlag();
        if (currentFlag != null) {
            setEpisodeAdapter(currentFlag.getEpisodes()); // Update adapter with reversed list
            if (scroll) {
                 // Find the currently active episode's new position after reversal
                 int newPosition = -1;
                 Episode activeEpisode = getEpisode(); // Find which episode is active *now*
                 if (activeEpisode != null && mEpisodeAdapter != null) {
                      newPosition = mEpisodeAdapter.indexOf(activeEpisode);
                 }
                 if (newPosition != -1) {
                      setEpisodeSelectedPosition(newPosition);
                 } else {
                      setEpisodeSelectedPosition(0); // Fallback to first item
                 }
            }
        }
    }


    private void setParseActivated(Parse item) {
        if (item == null || mBinding == null || mParseAdapter == null) return; // Safety check
        VodConfig.get().setParse(item); // Update global config
        // Update activation state in adapter
        for (int i = 0; i < mParseAdapter.size(); i++) {
             Parse parse = (Parse) mParseAdapter.get(i);
             if (parse != null) {
                  parse.setActivated(parse.equals(item));
             }
        }
        notifyItemChanged(mBinding.control.parse, mParseAdapter);
        onRefresh(); // Refresh player with new parse setting
    }


    private void setArrayAdapter(int size) {
        if (mBinding == null || mArrayAdapter == null) return; // Safety check

        if (size <= 0) {
            mBinding.array.setVisibility(View.GONE);
            mArrayAdapter.clear();
            return;
        }

        // Determine group size based on total size
        if (size > 200) setGroupSize(100);
        else if (size > 100) setGroupSize(40);
        else setGroupSize(20);

        List<String> items = new ArrayList<>();
        items.add(getString(R.string.play_reverse)); // "倒序排列"
        items.add(getString(mHistory != null ? mHistory.getRevPlayText() : R.string.play_forward)); // "正序播放" or "倒序播放"

        mBinding.array.setVisibility(size > 1 ? View.VISIBLE : View.GONE); // Show only if more than one episode

        // Generate range strings based on sort order
        if (mHistory != null && mHistory.isRevSort()) { // If sorted reversed
            for (int i = size; i > 0; i -= getGroupSize()) {
                items.add(i + "-" + Math.max(i - (getGroupSize() - 1), 1)); // e.g., 50-31, 30-11, 10-1
            }
        } else { // If sorted forward
            for (int i = 0; i < size; i += getGroupSize()) {
                items.add((i + 1) + "-" + Math.min(i + getGroupSize(), size)); // e.g., 1-20, 21-40, 41-50
            }
        }
        mArrayAdapter.setItems(items, null);
    }

    private int findFocusDown(int currentIndex) {
        List<Integer> viewOrder = Arrays.asList(
                R.id.flag, R.id.quality, R.id.episodeHori, R.id.array, R.id.episodeVert, R.id.part, R.id.quick
        );
        int currentOrderIndex = -1;
        // Find the order index of the current view's ID
        if (currentIndex >= 0 && currentIndex < viewOrder.size()) {
             currentOrderIndex = currentIndex; // Assume currentIndex is the order index
        } else {
             // Fallback: Find order index by view ID (less efficient)
             View currentView = findViewById(currentIndex); // This might be wrong if currentIndex is not an ID
             if (currentView != null) {
                  // This logic requires currentIndex to be the actual ID, not the order index
                  // Let's stick to assuming currentIndex is the order index for simplicity here.
                  // If currentIndex is an ID, you need to find its position in viewOrder first.
                  Log.w("VideoActivity", "findFocusDown called with unexpected index type");
                  return 0; // Cannot proceed reliably
             }
        }


        // Search downwards from the current position + 1
        for (int i = currentOrderIndex + 1; i < viewOrder.size(); i++) {
            View v = findViewById(viewOrder.get(i));
            if (v != null && isVisible(v)) {
                return viewOrder.get(i); // Return the ID of the next visible view
            }
        }
        return 0; // No view found below
    }

    private int findFocusUp(int currentIndex) {
        List<Integer> viewOrder = Arrays.asList(
                R.id.flag, R.id.quality, R.id.episodeHori, R.id.array, R.id.episodeVert, R.id.part, R.id.quick
        );
         int currentOrderIndex = -1;
         if (currentIndex >= 0 && currentIndex < viewOrder.size()) {
              currentOrderIndex = currentIndex;
         } else {
             Log.w("VideoActivity", "findFocusUp called with unexpected index type");
              return 0;
         }

        // Search upwards from the current position - 1
        for (int i = currentOrderIndex - 1; i >= 0; i--) {
            View v = findViewById(viewOrder.get(i));
            if (v != null && isVisible(v)) {
                return viewOrder.get(i); // Return the ID of the previous visible view
            }
        }
        return 0; // No view found above
    }


    private void updateFocus() {
        hasKeyEvent = false; // Reset key event flag

        // Determine the order index for episode views
        int episodeHoriIndex = 2;
        int episodeVertIndex = 4;
        int currentEpisodeIndex = (Setting.getEpisode() == 0) ? episodeHoriIndex : episodeVertIndex;

        // Update focus for Episode Presenter
        if (mEpisodePresenter != null) {
            mEpisodePresenter.setNextFocusDown(findFocusDown(currentEpisodeIndex));
            mEpisodePresenter.setNextFocusUp(findFocusUp(currentEpisodeIndex));
        }

        // Update focus for other presenters/adapters
        if (mQualityAdapter != null) {
            mQualityAdapter.setNextFocusDown(findFocusDown(1)); // Quality is at index 1
        }
        if (mArrayPresenter != null) {
            mArrayPresenter.setNextFocusDown(findFocusDown(3)); // Array is at index 3
            mArrayPresenter.setNextFocusUp(findFocusUp(3));
        }
        if (mFlagPresenter != null) {
            mFlagPresenter.setNextFocusDown(findFocusDown(0)); // Flag is at index 0
        }
         if (mPartPresenter != null) {
             mPartPresenter.setNextFocusUp(findFocusUp(5)); // Part is at index 5
         }
         // Quick presenter might need focus rules too if it becomes focusable

        // Notify adapters to apply the new focus rules
        notifyItemChanged(mBinding.flag, mFlagAdapter);
        notifyItemChanged(mBinding.quality, mQualityAdapter); // Use the adapter directly
        notifyItemChanged(mBinding.array, mArrayAdapter);
        notifyItemChanged(getEpisodeView(), mEpisodeAdapter); // Use the current episode view
        notifyItemChanged(mBinding.part, mPartAdapter);
        notifyItemChanged(mBinding.quick, mQuickAdapter); // Notify quick adapter too
    }


    private void showDisplayInfo() {
        if (mBinding == null || mPlayers == null) return; // Safety check

        boolean hasBottomSheet = false;
        try {
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                if (f instanceof BottomSheetDialogFragment && f.isVisible()) { // Check visibility too
                    hasBottomSheet = true;
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("VideoActivity", "Error checking for BottomSheetDialogFragment", e);
        }

        boolean controlsVisible = isVisible(mBinding.control.getRoot());
        boolean infoWidgetVisible = isVisible(mBinding.widget.info);
        boolean isVod = mPlayers.isVod();

        // Clock visibility
        mBinding.display.clock.setVisibility(Setting.isDisplayTime() || infoWidgetVisible ? View.VISIBLE : View.GONE);

        // Title visibility
        mBinding.display.titleLayout.setVisibility(Setting.isDisplayVideoTitle() && !controlsVisible ? View.VISIBLE : View.GONE);

        // Netspeed visibility
        mBinding.display.netspeed.setVisibility(Setting.isDisplaySpeed() && !controlsVisible && !hasBottomSheet ? View.VISIBLE : View.GONE);

        // Duration visibility (only if VOD, not live)
        mBinding.display.duration.setVisibility(Setting.isDisplayDuration() && !controlsVisible && isVod && !hasBottomSheet ? View.VISIBLE : View.GONE);

         // Mini Progress visibility (only if VOD, not live)
         mBinding.display.progress.setVisibility(Setting.isDisplayMiniProgress() && !controlsVisible && isVod && !hasBottomSheet ? View.VISIBLE : View.GONE);

    }

    private void onTimeChangeDisplaySpeed() {
        if (mBinding == null || mPlayers == null) return; // Safety check

        boolean controlsVisible = isVisible(mBinding.control.getRoot());
        boolean displaySpeed = Setting.isDisplaySpeed() && !controlsVisible;
        boolean displayDuration = Setting.isDisplayDuration() && !controlsVisible && mPlayers.isVod();
        boolean displayProgress = Setting.isDisplayMiniProgress() && !controlsVisible && mPlayers.isVod();

        long position = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        boolean validTime = position >= 0 && duration > 0;

        // Update Speed
        if (displaySpeed) {
            Traffic.setSpeed(mBinding.display.netspeed);
        }

        // Update Duration Text
        if (displayDuration) {
            if (validTime) {
                mBinding.display.duration.setText(mPlayers.getPositionTime(0) + "/" + mPlayers.getDurationTime());
            } else {
                mBinding.display.duration.setText(""); // Clear if time is invalid
            }
        }

        // Update Mini Progress Bar
        if (displayProgress) {
            if (validTime) {
                 // Calculate progress carefully to avoid division by zero
                 mBinding.display.progress.setProgress((int) (position * 100 / duration));
            } else {
                mBinding.display.progress.setProgress(0); // Reset progress if time is invalid
            }
        }

        // Ensure overall visibility is updated based on settings and state
        showDisplayInfo();
    }


    @Override
    public boolean onArrayItemTouch() {
        hasKeyEvent = true;
        return false; // Allow touch event to proceed (e.g., for click)
    }

    @Override
    public void onRevSort() {
        if (mHistory == null) return;
        mHistory.setRevSort(!mHistory.isRevSort());
        reverseEpisode(true); // Reverse list and scroll to current item
        // Update the "Reverse Play" button text immediately
        if (mArrayAdapter != null && mArrayAdapter.size() > 1) {
             mArrayAdapter.notifyArrayItemRangeChanged(1, 1); // Notify the second item (Reverse Play button) changed
        }
        mHistory.save(); // Save history change
    }

    @Override
    public void onRevPlay(TextView view) {
        if (mHistory == null || view == null) return;
        mHistory.setRevPlay(!mHistory.isRevPlay());
        view.setText(mHistory.getRevPlayText()); // Update button text
        Notify.show(mHistory.getRevPlayHint()); // Show hint
        mHistory.save(); // Save history change
    }

    private boolean shouldEnterFullscreen(Episode item) {
        if (item == null) return false;
        // Enter fullscreen only if not already fullscreen AND the selected episode is the one currently activated
        boolean enter = !isFullscreen() && item.isActivated();
        if (enter) {
            enterFullscreen();
        }
        return enter;
    }

    private void enterFullscreen() {
        if (mBinding == null || mKeyDown == null || mDanmakuContext == null) return; // Safety check

        mFocus1 = getCurrentFocus(); // Store focus before changing layout
        mBinding.video.requestFocus();
        mBinding.video.setForeground(null); // Remove selector foreground
        mBinding.video.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mBinding.video.setBackgroundColor(android.graphics.Color.BLACK); // Use black background
        mBinding.video.setClipToOutline(false); // Allow drawing outside bounds (needed for some players?)

        // Ensure flag selection is visually updated if flags were visible
        if (mFlagAdapter != null && mFlagAdapter.size() > 0) {
             mBinding.flag.setSelectedPosition(getFlagPosition());
        }

        // Adjust Danmaku size for fullscreen
        if (Setting.getDanmuSize() != 0) {
            mDanmakuContext.setScaleTextSize(1.2f * Setting.getDanmuSize());
        }

        mKeyDown.setFull(true); // Enable fullscreen key handling
        setFullscreen(true);
        mFocus2 = null; // Reset potential control focus storage

        hideDetailViews(); // Hide non-video UI elements

        onPlay(); // Ensure player is playing
    }

    private void exitFullscreen() {
        if (mBinding == null || mKeyDown == null || mDanmakuContext == null || mFrameParams == null) return; // Safety check

        mBinding.video.setForeground(ResUtil.getDrawable(R.drawable.selector_video)); // Restore selector
        mBinding.video.setLayoutParams(mFrameParams); // Restore original layout params
        mBinding.video.setBackgroundResource(R.drawable.rounded_corners); // Restore rounded background
        mBinding.video.setClipToOutline(true); // Re-enable clipping

        // Adjust Danmaku size back
        if (Setting.getDanmuSize() != 0) {
            mDanmakuContext.setScaleTextSize(0.8f * Setting.getDanmuSize());
        }

        // Restore focus
        View targetFocus = getFocus1(); // getFocus1 has fallback to video view
        if (targetFocus != null) targetFocus.requestFocus();

        mKeyDown.setFull(false); // Disable fullscreen key handling
        setFullscreen(false);
        mFocus2 = null; // Reset potential control focus storage

        showDetailViews(); // Show non-video UI elements again

        hideInfo(); // Hide playback info overlay
    }

    // --- Helper method to hide non-video views for fullscreen ---
    private void hideDetailViews() {
        if (mBinding == null) return;

        // Hide Title Area (Logo or Text) and remove glow layer
        if (mBinding.logoImageView.getVisibility() == View.VISIBLE) {
            mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null); // Remove glow
        }
        mBinding.logoImageView.setVisibility(View.GONE);
        mBinding.nameTextView.setVisibility(View.GONE);

        // Hide other details
        mBinding.remark.setVisibility(View.GONE);
        mBinding.row1.setVisibility(View.GONE); // Contains site, year, area, type
        mBinding.director.setVisibility(View.GONE);
        mBinding.actor.setVisibility(View.GONE);
        mBinding.content.setVisibility(View.GONE);
        mBinding.row2.setVisibility(View.GONE); // Contains desc, keep, change1 buttons
        mBinding.flag.setVisibility(View.GONE);
        mBinding.scroll.setVisibility(View.GONE); // Hides episode lists, part, quick search
    }
    // --- End Helper Method ---

    // --- Helper method to show non-video views when exiting fullscreen ---
    private void showDetailViews() {
        if (mBinding == null) return;

        // Show Title Area (Logo with glow or Text)
        if (currentLogoUrl != null) {
            mBinding.logoImageView.setVisibility(View.VISIBLE);
            // Re-apply glow effect only if paint is valid
            initGlowPaint();
            if (logoGlowPaint != null) {
                 mBinding.logoImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, logoGlowPaint);
            } else {
                 mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null);
            }
            mBinding.nameTextView.setVisibility(View.GONE);
        } else {
            mBinding.logoImageView.setVisibility(View.GONE);
            mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null); // Ensure no glow
            mBinding.nameTextView.setVisibility(View.VISIBLE);
            // setText will handle visibility and content if needed elsewhere,
            // but ensure it's visible here if logo is not available.
        }
        // Use setText for nameTextView to ensure tag is updated and visibility is correct
         setText(mBinding.nameTextView, 0, currentVodName);


        // Show other details using setText (which handles visibility based on content)
        // Retrieve text from tag to restore state correctly
        setText(mBinding.remark, 0, Objects.toString(mBinding.remark.getTag(), ""));
        mBinding.row1.setVisibility(View.VISIBLE); // Show the row container
         setText(mBinding.site, 0, Objects.toString(mBinding.site.getTag(), "")); // Use tag for site too
         setText(mBinding.year, 0, Objects.toString(mBinding.year.getTag(), ""));
         setText(mBinding.area, 0, Objects.toString(mBinding.area.getTag(), ""));
         setText(mBinding.type, 0, Objects.toString(mBinding.type.getTag(), ""));

        setText(mBinding.director, 0, Objects.toString(mBinding.director.getTag(), ""));
        setText(mBinding.actor, 0, Objects.toString(mBinding.actor.getTag(), ""));
        setText(mBinding.content, 0, Objects.toString(mBinding.content.getTag(), ""));
        mBinding.row2.setVisibility(View.VISIBLE); // Show the button row container

        // Show lists container
        mBinding.scroll.setVisibility(View.VISIBLE);
        // Restore visibility of lists based on data
        mBinding.flag.setVisibility(mFlagAdapter != null && mFlagAdapter.size() > 0 ? View.VISIBLE : View.GONE);
        mBinding.quality.setVisibility(mQualityAdapter != null && mQualityAdapter.getItemCount() > 0 && isVisible(mBinding.quality) ? View.VISIBLE : View.GONE); // Check previous visibility? Or just data?
        BaseGridView episodeView = getEpisodeView();
        if (episodeView != null) {
             episodeView.setVisibility(mEpisodeAdapter != null && mEpisodeAdapter.size() > 0 ? View.VISIBLE : View.GONE);
        }
        mBinding.array.setVisibility(mArrayAdapter != null && mArrayAdapter.size() > 2 ? View.VISIBLE : View.GONE); // Array has 2 fixed items
        mBinding.part.setVisibility(mPartAdapter != null && mPartAdapter.size() > 0 ? View.VISIBLE : View.GONE);
        mBinding.quick.setVisibility(mQuickAdapter != null && mQuickAdapter.size() > 0 ? View.VISIBLE : View.GONE);

        // Update focus rules as layout has changed
        updateFocus();
    }
    // --- End Helper Method ---


    private void onDesc() {
        if (mBinding == null) return;
        CharSequence desc = mBinding.content.getText();
        // Check length and prefix "简介：" which might be added by setText
        if (desc != null && desc.length() > 0) {
            String descStr = desc.toString();
            String prefix = getString(R.string.detail_content, ""); // Get "简介：" prefix
             if (descStr.startsWith(prefix)) {
                 descStr = descStr.substring(prefix.length());
             }
            if (!TextUtils.isEmpty(descStr)) {
                 DescDialog.show(this, descStr);
            }
        }
    }

    private void onKeep() {
        Keep keep = Keep.find(getHistoryKey());
        Notify.show(keep != null ? R.string.keep_del : R.string.keep_add);
        if (keep != null) {
            keep.delete();
        } else {
            createKeep(); // This now uses currentVodName
        }
        RefreshEvent.keep(); // Notify other parts of the app
        checkKeep(); // Update button icon
    }

    private void onVideo() {
        if (!isFullscreen()) {
            enterFullscreen();
        } else {
            // Optional: Handle click when already fullscreen (e.g., toggle controls)
             onToggle();
        }
    }

    private void onChange() {
        checkSearch(true); // Force search for alternatives
    }

    private void onLoop() {
        if (mBinding == null) return;
        boolean current = mBinding.control.loop.isActivated();
        mBinding.control.loop.setActivated(!current);
        // Maybe save loop state? Depends on requirements.
    }

    private void onDanmu() {
        if (mBinding == null) return;
        boolean danmuEnabled = !Setting.isDanmu();
        Setting.putDanmu(danmuEnabled);
        mBinding.control.danmu.setActivated(danmuEnabled);
        showDanmu();
    }

    private void showDanmu() {
        if (mBinding == null || mBinding.danmaku == null) return;
        if (Setting.isDanmu() && mBinding.danmaku.isPrepared()) { // Show only if enabled AND prepared
             mBinding.danmaku.show();
        } else {
             mBinding.danmaku.hide();
        }
    }

    private void onDanmuAdd() {
        if (mBinding == null) return;
        int line = Setting.getDanmuLine(3); // Default 3 lines
        line = Math.min(line + 1, 15); // Max 15 lines
        Setting.putDanmuLine(line);
        mBinding.control.danmu.setText(getString(R.string.danmu_lines, line)); // Use formatted string
        setDanmuViewSettings(); // Apply new settings
    }

    private void onDanmuSub() {
        if (mBinding == null) return;
        int line = Setting.getDanmuLine(3);
        line = Math.max(line - 1, 1); // Min 1 line
        Setting.putDanmuLine(line);
        mBinding.control.danmu.setText(getString(R.string.danmu_lines, line)); // Use formatted string
        setDanmuViewSettings(); // Apply new settings
    }

    private void onEpisodes() {
        Flag flag = getFlag();
        if (flag == null || flag.getEpisodes() == null || flag.getEpisodes().isEmpty()) return;
        EpisodeDialog.create().episodes(flag.getEpisodes()).show(this);
        hideControl(); // Hide controls after opening dialog
    }

    private void checkNext() {
        if (mHistory != null && mHistory.isRevPlay()) {
            onPrev(); // If reverse play is on, "next" action plays previous episode
        } else {
            onNext();
        }
    }

    private void checkPrev() {
        if (mHistory != null && mHistory.isRevPlay()) {
            onNext(); // If reverse play is on, "prev" action plays next episode
        } else {
            onPrev();
        }
    }

    private void onNext() {
        if (mEpisodeAdapter == null || mEpisodeAdapter.size() == 0) return;
        int current = getEpisodePosition();
        int max = mEpisodeAdapter.size() - 1;
        if (current >= max) { // Already at the last episode
            Notify.show(mHistory != null && mHistory.isRevPlay() ? R.string.error_play_prev_eof : R.string.error_play_next_eof);
            return;
        }
        int nextPos = current + 1;
        Episode item = (Episode) mEpisodeAdapter.get(nextPos);
        if (item != null) {
            setEpisodeActivated(item);
        }
    }

    private void onPrev() {
        if (mEpisodeAdapter == null || mEpisodeAdapter.size() == 0) return;
        int current = getEpisodePosition();
        if (current <= 0) { // Already at the first episode
            Notify.show(mHistory != null && mHistory.isRevPlay() ? R.string.error_play_next_eof : R.string.error_play_prev_eof);
            return;
        }
        int prevPos = current - 1;
        Episode item = (Episode) mEpisodeAdapter.get(prevPos);
        if (item != null) {
            setEpisodeActivated(item);
        }
    }

    private void onScale() {
        int index = getScale();
        String[] array = ResUtil.getStringArray(R.array.select_scale);
        int newIndex = (index + 1) % array.length; // Cycle through scales
        if (mHistory != null) mHistory.setScale(newIndex);
        setScale(newIndex); // Apply the new scale
        if (mHistory != null) mHistory.save(); // Save history
    }

    private void onSpeed() {
        if (mPlayers == null || mBinding == null) return;
        mBinding.control.speed.setText(mPlayers.addSpeed()); // Cycle through speeds
        if (mHistory != null) {
             mHistory.setSpeed(mPlayers.getSpeed());
             mHistory.save(); // Save history
        }
    }

    private void onSpeedAdd() {
         if (mPlayers == null || mBinding == null) return;
        mBinding.control.speed.setText(mPlayers.addSpeed(0.25f));
        if (mHistory != null) {
            mHistory.setSpeed(mPlayers.getSpeed());
             mHistory.save(); // Save history
        }
    }

    private void onSpeedSub() {
         if (mPlayers == null || mBinding == null) return;
        mBinding.control.speed.setText(mPlayers.subSpeed(0.25f));
        if (mHistory != null) {
            mHistory.setSpeed(mPlayers.getSpeed());
             mHistory.save(); // Save history
        }
    }

    private boolean onSpeedLong() {
         if (mPlayers == null || mBinding == null) return false;
        mBinding.control.speed.setText(mPlayers.toggleSpeed()); // Reset to 1.0x
        if (mHistory != null) {
             mHistory.setSpeed(mPlayers.getSpeed());
             mHistory.save(); // Save history
        }
        return true; // Consume long click
    }

    private void onRefresh() {
        onReset(false); // Refresh usually means replay from start (or saved pos if supported)
    }

    private void onReset() {
        onReset(isReplay()); // Reset based on setting (start or current pos)
    }

    private void onReset(boolean replay) {
        if (mClock != null) mClock.setCallback(null); // Stop time updates during reset
        if (mFlagAdapter == null || mFlagAdapter.size() == 0 || mEpisodeAdapter == null || mEpisodeAdapter.size() == 0) return;

        Episode episode = getEpisode();
        Flag flag = getFlag();
        if (flag != null && episode != null) {
            getPlayer(flag, episode, replay); // Get player content again
        } else {
             Log.w("VideoActivity", "Cannot reset, flag or episode is null");
             // Optionally show an error message
        }
    }

    private boolean onResetToggle() {
        if (mBinding == null) return false;
        int newReset = (Setting.getReset() + 1) % 2; // Toggle between 0 and 1
        Setting.putReset(newReset);
        mBinding.control.reset.setText(ResUtil.getStringArray(R.array.select_reset)[newReset]);
        return true; // Consume long click
    }

    private void onOpening() {
        if (mPlayers == null) return;
        long current = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        if (current < 0 || duration <= 0 || current > duration / 2) {
             Notify.show(R.string.error_set_op_range);
             return;
        }
        setOpening(current); // Set current position as opening time
    }

    private void onOpeningAdd() {
        if (mHistory == null || mPlayers == null) return;
        long duration = mPlayers.getDuration();
        if (duration <= 0) return;
        setOpening(Math.min(mHistory.getOpening() + 1000, duration / 2)); // Add 1s, max half duration
    }

    private void onOpeningSub() {
        if (mHistory == null) return;
        setOpening(Math.max(0, mHistory.getOpening() - 1000)); // Subtract 1s, min 0
    }

    private boolean onOpeningReset() {
        setOpening(0); // Reset opening to 0
        return true; // Consume long click
    }

    private void setOpening(long opening) {
        if (mHistory == null || mBinding == null || mPlayers == null) return;
        mHistory.setOpening(opening);
        mBinding.control.opening.setText(opening == 0 ? getString(R.string.play_op) : mPlayers.stringToTime(opening));
        mHistory.save(); // Save history
    }

    private void onEnding() {
        if (mPlayers == null) return;
        long current = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        if (current < 0 || duration <= 0 || current < duration / 2) {
             Notify.show(R.string.error_set_ed_range);
             return;
        }
        setEnding(duration - current); // Set time remaining as ending duration
    }

    private void onEndingAdd() {
        if (mHistory == null || mPlayers == null) return;
         long duration = mPlayers.getDuration();
         if (duration <= 0) return;
        setEnding(Math.min(mHistory.getEnding() + 1000, duration / 2)); // Add 1s, max half duration
    }

    private void onEndingSub() {
        if (mHistory == null) return;
        setEnding(Math.max(0, mHistory.getEnding() - 1000)); // Subtract 1s, min 0
    }

    private boolean onEndingReset() {
        setEnding(0); // Reset ending to 0
        return true; // Consume long click
    }

    private void setEnding(long ending) {
        if (mHistory == null || mBinding == null || mPlayers == null) return;
        mHistory.setEnding(ending);
        mBinding.control.ending.setText(ending == 0 ? getString(R.string.play_ed) : mPlayers.stringToTime(ending));
        mHistory.save(); // Save history
    }

    private boolean onChoose() {
        if (mPlayers == null || mPlayers.isEmpty() || mBinding == null) return false;
        CharSequence title = mBinding.widget.title.getText();
        mPlayers.choose(this, title != null ? title : ""); // Use title from widget
        return true; // Consume long click
    }

    private void onPlayer() {
         if (mPlayers == null || mBinding == null) return;
        CharSequence title = mBinding.widget.title.getText();
        PlayerDialog.create()
            .select(mPlayers.getPlayer())
            .title(title != null ? title.toString() : "")
            .listener(this) // Implement PlayerDialog.Listener if needed
            .show(this);
        hideControl(); // Hide controls after opening dialog
    }

    private void onDecode() {
        onDecode(true); // Default to saving the decode choice
    }

    private void onDecode(boolean save) {
        if (mPlayers == null) return;
        mPlayers.toggleDecode(save);
        mPlayers.init(getExo(), getIjk()); // Re-init player views after toggle
        mPlayers.setMediaSource(); // Re-set media source with new decoder
        setDecodeView(); // Update decode button text
    }

    private void onTrack(View view) {
        if (view == null || view.getTag() == null || mPlayers == null) return; // Safety checks
        try {
            int type = Integer.parseInt(view.getTag().toString());
            TrackDialog.create()
                .player(mPlayers)
                .chooser(this) // For file chooser
                .listener(this) // For track selection
                .vod(true) // Indicate it's for VOD
                .type(type) // Pass track type (Audio/Text/Video)
                .show(this);
            hideControl(); // Hide controls after opening dialog
        } catch (NumberFormatException e) {
            Log.e("VideoActivity", "Invalid tag for track type", e);
        }
    }

    private void onToggle() {
        if (mBinding == null) return;
        if (isVisible(mBinding.control.getRoot())) {
            hideControl();
        } else {
            showControl(getFocus2()); // Show controls, try to focus last known control
        }
    }

    private void showProgress() {
        if (mBinding == null) return;
        mBinding.widget.progress.setVisibility(View.VISIBLE);
        App.post(mR3, 0); // Start traffic update immediately
        hideError();
    }

    private void hideProgress() {
        if (mBinding == null) return;
        mBinding.widget.progress.setVisibility(View.GONE);
        App.removeCallbacks(mR3); // Stop traffic update
        Traffic.reset();
    }

    private void showError(String text) {
        if (mBinding == null) return;
        mBinding.widget.error.setVisibility(View.VISIBLE);
        mBinding.widget.text.setText(text);
        hideProgress();
    }

    private void hideError() {
        if (mBinding == null) return;
        mBinding.widget.error.setVisibility(View.GONE);
        mBinding.widget.text.setText("");
    }

    private void showInfo() {
        if (mBinding == null) return;
        mBinding.widget.info.setVisibility(View.VISIBLE);
        showDisplayInfo(); // Update display elements based on info visibility
    }

    private void hideInfo() {
        if (mBinding == null) return;
        mBinding.widget.info.setVisibility(View.GONE);
        showDisplayInfo(); // Update display elements based on info visibility
    }

    private void showInfoAndCenter() {
        if (mBinding == null) return;
        showInfo();
        mBinding.widget.center.setVisibility(View.VISIBLE);
    }

    private void hideInfoAndCenter() {
        if (mBinding == null) return;
        hideInfo();
        mBinding.widget.center.setVisibility(View.GONE);
    }

    private void setControlNextFocus() {
        if (mBinding == null || mBinding.control.actionLayout == null) return;

        View firstVisible = null;
        View lastVisible = null;
        View prevVisible = null;

        int count = mBinding.control.actionLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View current = mBinding.control.actionLayout.getChildAt(i);
            if (current != null && isVisible(current) && current.isEnabled()) {
                if (firstVisible == null) {
                    firstVisible = current; // Found the first one
                }
                if (prevVisible != null) {
                    // Set focus from previous visible to current
                    prevVisible.setNextFocusRightId(current.getId());
                    current.setNextFocusLeftId(prevVisible.getId());
                }
                lastVisible = current; // Update last visible button found so far
                prevVisible = current; // Current becomes previous for the next iteration
            }
        }

        // Optional: Handle wrap-around focus (last focuses first, first focuses last)
        // if (firstVisible != null && lastVisible != null && firstVisible != lastVisible) {
        //     lastVisible.setNextFocusRightId(firstVisible.getId());
        //     firstVisible.setNextFocusLeftId(lastVisible.getId());
        // }
    }


    private void showControl(View view) {
        if (mBinding == null || mBinding.danmaku == null) return; // Safety check

        // Determine default focus if provided view is invalid
        if (view == null) view = getFocus2(); // Try last focused control
        if (view == null || !isVisible(view) || !view.isEnabled()) { // Fallback if still invalid
             view = mBinding.control.next; // Default to 'next' button
             if (view == null || !isVisible(view) || !view.isEnabled()) { // Final fallback
                  view = mBinding.control.play; // Try play/pause
             }
        }

        // Set visibility of controls
        mBinding.control.danmu.setVisibility(mBinding.danmaku.isPrepared() ? View.VISIBLE : View.GONE);
        mBinding.control.getRoot().setVisibility(View.VISIBLE);
        mBinding.control.episodes.setVisibility(Setting.getFullscreenMenuKey() == 0 ? View.VISIBLE : View.GONE);
        // Make other controls visible/gone based on player capabilities or state if needed

        setControlNextFocus(); // Setup horizontal focus chain

        if (view != null && isVisible(view) && view.isEnabled()) { // Request focus if view is valid
            view.requestFocus();
        } else {
             // If even fallback view is invalid, maybe focus the first available control?
             mBinding.control.play.requestFocus(); // Example: focus play/pause
        }

        setR1Callback(); // Start timer to hide controls
    }


    private void hideControl() {
        hideControl(true); // Default to hiding the info overlay as well
    }

    private void hideControl(boolean hideInfo) {
        if (mBinding == null) return;

        if (hideInfo) hideInfo(); // Hide top info bar if requested

        // Reset track button text (might be dynamic)
        mBinding.control.text.setText(R.string.play_track_text);
        // Hide the main control layout
        mBinding.control.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(mR1); // Cancel auto-hide timer
    }

    private void hideCenter() {
        if (mBinding == null) return;
        mBinding.widget.action.setImageResource(R.drawable.ic_widget_play); // Reset icon
        mBinding.widget.center.setVisibility(View.GONE);
    }

    private void showPreview(Drawable preview) {
         // Show preview only if enabled, not null, and preview widget is currently gone
        if (preview != null && Setting.getFlag() != 0 && mBinding != null && isGone(mBinding.widget.preview)) {
            mBinding.widget.preview.setVisibility(View.VISIBLE);
            mBinding.widget.preview.setImageDrawable(preview);
        }
    }

    private void hidePreview() {
        if (mBinding != null && mBinding.widget.preview != null) {
            mBinding.widget.preview.setVisibility(View.GONE);
            mBinding.widget.preview.setImageDrawable(null); // Release drawable reference
        }
    }

    private void setTraffic() {
        if (mBinding != null && Setting.isDisplaySpeed()) { // Only update if speed display is enabled
            Traffic.setSpeed(mBinding.widget.traffic); // Update traffic view in widget
            Traffic.setSpeed(mBinding.display.netspeed); // Update traffic view in display overlay
        }
        // Schedule next update only if needed
        if (Setting.isDisplaySpeed() && !isBackground()) {
             App.post(mR3, Constant.INTERVAL_TRAFFIC);
        }
    }

    private void setR1Callback() {
        // Set callback to hide controls after a delay
        App.removeCallbacks(mR1); // Remove previous callbacks
        App.post(mR1, Constant.INTERVAL_HIDE);
    }

    private void setR2Callback(long delayMillis) {
        // Set callback to update focus rules after a delay
        App.removeCallbacks(mR2); // Remove previous callbacks
        App.post(mR2, delayMillis);
    }

    private void setArtwork(String url) {
        if (TextUtils.isEmpty(url)) {
             // No URL, maybe set a default placeholder immediately?
             Drawable defaultArt = ContextCompat.getDrawable(this, R.drawable.radio);
             if (mBinding != null && mPlayers != null) {
                  getExo().setDefaultArtwork(defaultArt);
                  getIjk().setDefaultArtwork(defaultArt);
             }
             hidePreview();
             return;
        }

        ImgUtil.load(url, R.drawable.radio, new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                // Set as default artwork for players
                if (mBinding != null && mPlayers != null) {
                    getExo().setDefaultArtwork(resource);
                    getIjk().setDefaultArtwork(resource);
                }
                // Show preview if setting allows
                showPreview(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                // Failed to load artwork, hide preview. Keep existing default artwork (or placeholder).
                hidePreview();
                 // Log error for debugging
                 Log.w("VideoActivity", "Failed to load artwork: " + url);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                // Called when the load is cancelled or replaced.
                // Optionally reset to placeholder if needed, but usually not required here.
            }
        });
    }


    private void getPart(String source) {
        if (TextUtils.isEmpty(source)) {
             setPartAdapter(Collections.emptyList()); // Clear part adapter if source is empty
             return;
        }
        try {
            // Encode source for URL query parameter
            String encodedSource = URLEncoder.encode(source.trim(), "UTF-8");
            // Construct the API URL (Ensure API Key is valid and URL is correct)
            String url = "https://api.yesapi.cn/?service=App.Scws.GetWords&app_key=CEE4B8A091578B252AC4C92FB4E893C3&text=" + encodedSource + "&ignore_mark=1"; // Added ignore_mark

            OkHttp.newCall(url).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    List<String> items = new ArrayList<>();
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String body = response.body().string();
                            items = Part.get(body); // Parse the response
                        } catch (Exception e) {
                            Log.e("VideoActivity", "Error parsing part response", e);
                            // Fallback to source if parsing fails
                            items.add(source.trim());
                        } finally {
                             response.close(); // Ensure response body is closed
                        }
                    } else {
                        Log.e("VideoActivity", "Failed to get parts, response code: " + response.code());
                        // Fallback to source on network failure
                        items.add(source.trim());
                         response.close(); // Ensure response body is closed
                    }

                    // Ensure the original source is always the first item
                    items.remove(source.trim()); // Remove if it exists elsewhere
                    items.add(0, source.trim()); // Add to the beginning

                    // Update UI on the main thread
                    final List<String> finalItems = items;
                    App.post(() -> setPartAdapter(finalItems)); // Shorter delay?
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("VideoActivity", "Failed to get parts network call", e);
                    // Fallback to source on network failure
                    List<String> items = Collections.singletonList(source.trim());
                    // Update UI on the main thread
                    App.post(() -> setPartAdapter(items));
                }
            });
        } catch (Exception e) {
            Log.e("VideoActivity", "Error encoding source or making call for getPart", e);
            // Fallback on encoding error or other exceptions
            List<String> items = Collections.singletonList(source.trim());
            App.post(() -> setPartAdapter(items));
        }
    }


    private void setPartAdapter(List<String> items) {
        if (mBinding == null || mPartAdapter == null) return; // Safety check

        boolean isEmpty = (items == null || items.isEmpty());
        mBinding.part.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (!isEmpty) {
            mPartAdapter.setItems(items, null);
            setR2Callback(100); // Update focus after setting items
        } else {
             mPartAdapter.clear(); // Clear adapter if list is empty
        }
    }

    private void checkFlag(Vod item) {
        if (mBinding == null || item == null) return; // Safety Checks

        boolean hasFlags = item.getVodFlags() != null && !item.getVodFlags().isEmpty();

        mBinding.flag.setVisibility(hasFlags ? View.VISIBLE : View.GONE);

        if (!hasFlags) {
            ErrorEvent.flag(); // Post flag error event if no flags
            // Also hide related views like episodes if there are no flags
            setEpisodeAdapter(Collections.emptyList());
        } else {
            // Flags exist, try to activate based on history or default to first
            Flag flagToActivate = null;
            if (mHistory != null) {
                flagToActivate = mHistory.getFlag(); // Get flag from history
                 // Validate if the flag from history actually exists in the current item's flags
                 if (flagToActivate == null || !item.getVodFlags().contains(flagToActivate)) {
                      // History flag is invalid or not found, try finding by name?
                      // Or default to the first flag of the current item
                      flagToActivate = item.getVodFlags().get(0);
                      Log.w("VideoActivity", "History flag not found in current VOD, defaulting to first flag.");
                 }

                // Apply reverse sort from history if needed
                if (mHistory.isRevSort()) {
                    reverseEpisode(true); // Reverse and scroll
                }
            } else {
                // No history, default to the first flag
                flagToActivate = item.getVodFlags().get(0);
            }

            // Activate the determined flag
            if (flagToActivate != null) {
                 setFlagActivated(flagToActivate);
            } else {
                 // Should not happen if hasFlags is true, but as a fallback:
                 Log.e("VideoActivity", "Error: No valid flag to activate even though flags exist.");
                 ErrorEvent.flag();
                 setEpisodeAdapter(Collections.emptyList());
            }
        }
    }


    // --- Modified checkHistory ---
    private void checkHistory(Vod item) {
        if (item == null) return; // Cannot check history without item

        mHistory = History.find(getHistoryKey());
        boolean createdNewHistory = false;
        if (mHistory == null) {
            mHistory = createHistory(item); // createHistory uses item.getVodName()
            createdNewHistory = (mHistory != null);
        }

        // Ensure History has the correct (original) VodName from the item
        // Especially important if history existed but name changed in source
        if (mHistory != null && !Objects.equals(item.getVodName(), mHistory.getVodName())) {
            Log.d("VideoActivity", "Updating history VOD name: " + item.getVodName());
            mHistory.setVodName(item.getVodName());
        }
        // Also update Pic, might change
        if (mHistory != null) {
             mHistory.setVodPic(item.getVodPic(getPic())); // Use pic from item, considering fallback
        }


        if (mHistory != null) {
            // Apply mark from intent if provided, overriding history's remark
            String mark = getMark();
            if (!TextUtils.isEmpty(mark)) {
                mHistory.setVodRemarks(mark);
                 Log.d("VideoActivity", "Applying mark from intent: " + mark);
            } else if (createdNewHistory) {
                 // If new history was created, ensure remark is set from findEpisode result
                 // createHistory already calls findEpisode which sets remark
                 Log.d("VideoActivity", "New history created with remark: " + mHistory.getVodRemarks());
            }

            // Handle Incognito mode
            if (Setting.isIncognito()) {
                 // Don't save history, maybe delete existing?
                 // For now, let's just not save updates. If it was found, it exists.
                 Log.d("VideoActivity", "Incognito mode, history will not be saved.");
            }

            // Update UI based on history
            mBinding.control.opening.setText(mHistory.getOpening() == 0 ? getString(R.string.play_op) : mPlayers.stringToTime(mHistory.getOpening()));
            mBinding.control.ending.setText(mHistory.getEnding() == 0 ? getString(R.string.play_ed) : mPlayers.stringToTime(mHistory.getEnding()));

            mPlayers.setPlayer(getPlayer()); // Applies history or site or setting player
            setScale(getScale()); // Applies history or setting scale
            setPlayerView(); // Updates player UI (speed, button text)
            setDecodeView(); // Updates decode button text
        } else {
            // Handle case where history is still null (e.g., DB error or createHistory failed)
            Log.e("VideoActivity", "History is null after check/create attempt.");
            // Set defaults directly?
            mPlayers.setPlayer(Setting.getPlayer());
            setScale(Setting.getScale());
            setPlayerView();
            setDecodeView();
            mBinding.control.opening.setText(getString(R.string.play_op));
            mBinding.control.ending.setText(getString(R.string.play_ed));
        }
    }
    // --- End Modified checkHistory ---

    // --- Modified createHistory ---
    private History createHistory(Vod item) {
        if (item == null) return null; // Cannot create history without item
        try {
            History history = new History();
            history.setKey(getHistoryKey());
            history.setCid(VodConfig.getCid());
            history.setVodName(item.getVodName()); // Use original name from VOD item
            history.setVodPic(item.getVodPic(getPic())); // Set Pic during creation
            history.findEpisode(item.getVodFlags()); // Finds first episode and sets remark/flag
            history.setSpeed(Setting.getPlaySpeed()); // Default speed
            history.setScale(Setting.getScale()); // Default scale
            history.setPlayer(getPlayer()); // Default player based on hierarchy
            history.setOpening(0); // Default OP
            history.setEnding(0); // Default ED
            history.setPosition(0); // Default position
            history.setDuration(0); // Default duration
            history.setCreateTime(System.currentTimeMillis());
            // Don't save here, save happens during playback or explicit action
            return history;
        } catch (Exception e) {
             Log.e("VideoActivity", "Error creating history object", e);
             return null;
        }
    }
    // --- End Modified createHistory ---

    private void updateHistory(Episode item, boolean replay) {
        if (mHistory == null || item == null) return; // Safety check

        Flag currentFlag = getFlag();
        if (currentFlag == null) return; // Need flag info

        // Determine if we are actually replaying from the start
        boolean forceReplay = replay || !item.equals(mHistory.getEpisode());
        long position = forceReplay ? 0 : mHistory.getPosition(); // Use 0 if replaying or different episode

        // Update history fields
        mHistory.setPosition(position);
        mHistory.setEpisodeUrl(item.getUrl());
        mHistory.setVodRemarks(item.getName()); // Episode name becomes the remark
        mHistory.setVodFlag(currentFlag.getFlag()); // Store the current flag identifier
        mHistory.setCreateTime(System.currentTimeMillis()); // Update timestamp

        // Set player start position (considering opening time)
        long startPosition = Math.max(mHistory.getOpening(), position);
        if (mPlayers != null) mPlayers.setPosition(startPosition);

        // Save history immediately if not incognito
        if (!Setting.isIncognito()) {
            App.execute(mHistory::save); // Save in background
        }
    }


    private void checkKeep() {
        if (mBinding == null) return;
        Keep keep = Keep.find(getHistoryKey());
        mBinding.keep.setCompoundDrawablesWithIntrinsicBounds(
                keep == null ? R.drawable.ic_detail_keep_off : R.drawable.ic_detail_keep_on,
                0, 0, 0);
        mBinding.keep.setText(keep == null ? R.string.detail_keep_off : R.string.detail_keep_on);
    }

    // --- Modified createKeep ---
    private void createKeep() {
        Site site = getSite();
        if (site == null) {
             Log.w("VideoActivity", "Cannot create keep, site is null");
             Notify.show(R.string.error_keep_add); // Show error message
             return;
        }
        try {
            Keep keep = new Keep();
            keep.setKey(getHistoryKey());
            keep.setCid(VodConfig.getCid());
            keep.setSiteName(site.getName());

            // Get Pic: Use tag if available, otherwise fallback to intent pic
            Object tag = (mBinding != null) ? mBinding.video.getTag() : null;
            keep.setVodPic(tag instanceof String ? (String) tag : getPic());

            // Use stored original VOD name
            keep.setVodName(currentVodName); // <-- Uses stored name

            keep.setCreateTime(System.currentTimeMillis());
            keep.save(); // Save the new keep item
        } catch (Exception e) {
             Log.e("VideoActivity", "Error creating keep object", e);
             Notify.show(R.string.error_keep_add); // Show error message
        }
    }
    // --- End Modified createKeep ---


    @Override
    public void showChooser(TrackDialog dialog) {
        if (dialog == null || mPlayers == null) return;
        FileChooserDialog.create().player(mPlayers).trackDialog(dialog).show(this);
    }

    @Override
    public void onTrackClick(Track item) {
        if (item == null) return;
        item.setKey(getHistoryKey()); // Associate track with this history item
        item.save(); // Save the selected track preference
        // Player should already be applying the track via TrackDialog's interaction
    }

    @Override
    public void onSubtitleClick() {
        if (mPlayers == null) return;
        // Hide controls slightly delayed to allow dialog to appear smoothly
        App.post(this::hideControl, 100);

        SubtitleView subtitleView = mPlayers.isIjk() ? getIjk().getSubtitleView() : getExo().getSubtitleView();
        if (subtitleView != null) {
            // Show dialog slightly delayed
            App.post(() -> SubtitleDialog.create().view(subtitleView).full(isFullscreen()).show(this), 200);
        } else {
             Log.w("VideoActivity", "SubtitleView is null, cannot show dialog.");
        }
    }

    @Override
    public void onTimeChanged() {
        onTimeChangeDisplaySpeed(); // Update speed/duration/progress display

        if (mHistory == null || mPlayers == null) return; // Nothing to update

        long position = mPlayers.getPosition();
        long duration = mPlayers.getDuration();

        // Update history only if time is valid and not incognito
        if (position >= 0 && duration > 0) {
            mHistory.setPosition(position);
            mHistory.setDuration(duration);
            if (!Setting.isIncognito()) {
                // Save periodically or on specific events? Debounce this?
                // For now, save on every valid time update (might be heavy)
                 // Consider saving less frequently, e.g., every 5-10 seconds or on pause/stop
                App.execute(() -> {
                    if (mHistory != null) { // Double check history isn't nulled concurrently
                        mHistory.update(); // Use update which might be more efficient than save
                    }
                });
            }
        }

        // Check ED (Ending) skip condition
        if (mHistory.getEnding() > 0 && duration > 0 && position > 0 && (mHistory.getEnding() + position >= duration)) {
            Log.d("VideoActivity", "Ending time reached, checking next episode.");
            if (mClock != null) mClock.setCallback(null); // Stop clock updates
            checkNext(); // Play next episode
        }
         // Check OP (Opening) skip condition - less common to auto-skip OP, but possible
         // if (mHistory.getOpening() > 0 && position < mHistory.getOpening() && mPlayers.isPlaying()) {
         //      Log.d("VideoActivity", "Skipping opening credits.");
         //      mPlayers.seekTo(mHistory.getOpening());
         // }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActionEvent(ActionEvent event) {
        if (event == null || event.getAction() == null || isBackground() || mBinding == null) return; // Safety checks

        Log.d("VideoActivity", "ActionEvent received: " + event.getAction());
        switch (event.getAction()) {
            case ActionEvent.PLAY:
            case ActionEvent.PAUSE:
                onKeyCenter(); // Toggle play/pause
                break;
            case ActionEvent.NEXT:
                 if (mBinding.control.next != null) {
                     mBinding.control.next.performClick();
                 }
                break;
            case ActionEvent.PREV:
                 if (mBinding.control.prev != null) {
                     mBinding.control.prev.performClick();
                 }
                break;
            case ActionEvent.STOP:
                finish();
                break;
            // Add cases for other actions if needed (e.g., seek, volume)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (event == null || event.getType() == null || isBackground()) return; // Safety checks

        Log.d("VideoActivity", "RefreshEvent received: " + event.getType());
        switch (event.getType()) {
            case DETAIL:
                getDetail(); // Refresh entire detail view
                break;
            case PLAYER:
                onRefresh(); // Refresh player content for current episode
                break;
            case DANMAKU:
                checkDanmu(event.getPath()); // Reload danmaku from path
                break;
            case SUBTITLE:
                 if (mPlayers != null) {
                     mPlayers.setSub(Sub.from(event.getPath())); // Set external subtitle
                 }
                break;
            case HISTORY:
                 // Maybe re-check history if it could have changed externally?
                 // checkHistory(vod); // Need the current Vod item here
                 Log.d("VideoActivity", "History refresh event received, might need re-check.");
                 break;
            case KEEP:
                 checkKeep(); // Update keep button status
                 break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerEvent(PlayerEvent event) {
        if (event == null || isBackground() || mPlayers == null || mBinding == null) return; // Safety checks

        // Log.v("VideoActivity", "PlayerEvent received: " + event.getState()); // Verbose logging
        switch (event.getState()) {
            case 0: // Custom state often used for Player.STATE_LOADING or before READY
                setInitTrack(true); // Flag to set default tracks on READY
                setTrackVisible(false); // Hide track buttons during load
                if (mClock != null) mClock.setCallback(this); // Start clock updates
                // Optionally show progress here too, though BUFFERING state handles it
                // showProgress();
                break;
            case Player.STATE_IDLE:
                // Player is stopped or hasn't started. Maybe hide progress.
                hideProgress();
                break;
            case Player.STATE_BUFFERING:
                showProgress(); // Show loading indicator
                break;
            case Player.STATE_READY:
                stopSearch(); // Stop site search if playing successfully
                setMetadata(); // Update media session metadata (uses history/currentVodName)
                resetToggle(); // Reset player toggle count on success
                resetError(); // Reset error count on success
                hideProgress(); // Hide loading indicator
                mPlayers.reset(); // Reset internal player flags (like retry count?) after successful ready
                setDefaultTrack(); // Apply default/saved tracks now
                setTrackVisible(true); // Show track buttons if tracks exist

                // Save player choice to history if it changed
                if (mHistory != null && mHistory.getPlayer() != mPlayers.getPlayer()) {
                    mHistory.setPlayer(mPlayers.getPlayer());
                    if (!Setting.isIncognito()) mHistory.save();
                }

                // Update size display
                String sizeText = mPlayers.getSizeText();
                mBinding.widget.size.setText(sizeText);
                mBinding.display.size.setText(sizeText);

                // Ensure playback starts if it was paused during buffering/loading
                // onPlay(); // Let the player handle autoPlay=true internally
                break;
            case Player.STATE_ENDED:
                checkEnded(); // Handle playback completion
                break;
        }
    }


    private void checkEnded() {
         if (mBinding == null) return;
        if (mBinding.control.loop.isActivated()) {
            Log.d("VideoActivity", "Looping current episode.");
            onReset(true); // Replay from the beginning
        } else {
            Log.d("VideoActivity", "Playback ended, checking next episode.");
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Allow screen to turn off
            checkNext(); // Try to play the next episode
        }
    }

    private void setTrackVisible(boolean visible) {
        if (mBinding == null || mPlayers == null) return;
        // Show button only if player has corresponding track type AND visibility is requested
        mBinding.control.text.setVisibility(visible && mPlayers.haveTrack(C.TRACK_TYPE_TEXT) ? View.VISIBLE : View.GONE);
        mBinding.control.audio.setVisibility(visible && mPlayers.haveTrack(C.TRACK_TYPE_AUDIO) ? View.VISIBLE : View.GONE);
        mBinding.control.video.setVisibility(visible && mPlayers.haveTrack(C.TRACK_TYPE_VIDEO) ? View.VISIBLE : View.GONE);

        // Refresh control focus chain as visibility changed
        setControlNextFocus();
    }

    private void setDefaultTrack() {
        if (isInitTrack() && mPlayers != null) {
            setInitTrack(false); // Only do this once per load
            mPlayers.prepared(); // Notify player it's ready (might trigger internal logic)
            // Apply saved tracks from database
            Track savedTracks = Track.find(getHistoryKey());
            if (savedTracks != null) {
                 Log.d("VideoActivity", "Applying saved tracks.");
                 mPlayers.setTrack(savedTracks);
            } else {
                 Log.d("VideoActivity", "No saved tracks found, using player defaults.");
                 // Player should use its own default track selection logic
            }
            // Update track button text/status after setting tracks if needed
             // e.g., mBinding.control.text.setText(mPlayers.getCurrentTrackInfo(C.TRACK_TYPE_TEXT));
        }
    }

    // --- Modified setMetadata ---
    private void setMetadata() {
        if (mPlayers == null) return;

        // Determine Title: Use history name if available, otherwise use stored currentVodName
        String title = (mHistory != null && !TextUtils.isEmpty(mHistory.getVodName())) ? mHistory.getVodName() : currentVodName;

        // Determine Artist (Episode Name): Get current episode, handle null
        Episode episode = getEpisode();
        String episodeName = (episode != null && !TextUtils.isEmpty(episode.getName())) ? episode.getName() : "";

        // Construct artist string, avoid redundancy if title and episode name are the same
        String artist = "";
        if (!TextUtils.isEmpty(episodeName) && !Objects.equals(title, episodeName)) {
            artist = getString(R.string.play_now, episodeName);
        }

        // Determine Picture URL: Use history pic if available, otherwise use intent pic
        String pic = (mHistory != null && !TextUtils.isEmpty(mHistory.getVodPic())) ? mHistory.getVodPic() : getPic();

        // Set metadata on the player
        mPlayers.setMetadata(title, artist, pic, getDefaultArtwork());
    }
    // --- End Modified setMetadata ---


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(ErrorEvent event) {
        if (event == null || isBackground() || mPlayers == null) return; // Safety checks

        Log.e("VideoActivity", "onErrorEvent: " + event.getMsg() + " | Code: " + event.getCode() + " | Retry: " + mPlayers.getRetryCount() + "/" + event.getRetry() + " | Decode: " + event.isDecode() + " | Exo: " + event.isExo() + " | Url: " + event.isUrl());

        if (addErrorCount() > 20) { // Too many consecutive errors, give up
            Log.e("VideoActivity", "Max error count reached, stopping playback attempts.");
            onErrorEnd(event);
        } else if (mPlayers.addRetry() > event.getRetry()) { // Retry limit exceeded for this specific error/source
            Log.w("VideoActivity", "Retry limit exceeded for this source.");
            checkError(event); // Proceed to check next source/parse/site
        } else if (event.isDecode() && mPlayers.canToggleDecode()) { // If it's a decode error and we can toggle
             Log.w("VideoActivity", "Decode error, attempting to toggle decoder.");
             onDecode(false); // Toggle decoder without saving preference yet
             // Reset retry count for the new decoder attempt? Maybe not, let it fail if both decoders fail.
        } else if (event.isExo() && mPlayers.isExo()) { // If it's an ExoPlayer specific error
             Log.w("VideoActivity", "ExoPlayer error, attempting specific handling.");
             onExoCheck(event); // Handle specific ExoPlayer errors (like format)
             // No automatic refresh here, onExoCheck might trigger setMediaSource
        } else {
             // General error or retry limit not exceeded yet, just retry the current source
             Log.w("VideoActivity", "General error, attempting refresh/retry.");
             onRefresh(); // Retry the current source/episode
        }
    }


    private void onExoCheck(ErrorEvent event) {
        if (event == null || mPlayers == null) return;

        int code = event.getCode();
        // Check for specific ExoPlayer errors that might indicate format issues
        if (code == PlaybackException.ERROR_CODE_IO_UNSPECIFIED || // General I/O error
            (code >= PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED && code <= PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED) || // Parsing errors
            code == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ||
            code == PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED ||
            code == PlaybackException.ERROR_CODE_DECODING_FAILED ||
            code == PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED ||
            code == PlaybackException.ERROR_CODE_VIDEO_TRACK_INIT_FAILED)
        {
            Log.w("VideoActivity", "ExoPlayer specific error detected (Code: " + code + "), potentially format related.");
            // Try setting format hint based on error code if possible (might need more specific mapping)
            // mPlayers.setFormat(ExoUtil.getMimeType(event.getCode())); // This helper might not exist or be accurate enough

            // Instead of setting format hint, maybe just force a refresh or try toggling decoder?
            // For now, let's just re-set the media source, which might re-evaluate things.
             mPlayers.setMediaSource();
        } else {
             // For other ExoPlayer errors, maybe just retry?
             Log.w("VideoActivity", "Unhandled ExoPlayer error code: " + code);
             onRefresh(); // Default to refresh for unhandled codes
        }
    }

    private void checkError(ErrorEvent event) {
        if (event == null) return;

        Site site = getSite();
        // Check if we should toggle player (if not using SYS, site allows, URL error, within toggle limit)
        if (site != null && site.getPlayerType() == -1 && // Site does not force a player
            event.isUrl() && // Error is likely related to URL/connection/format
            event.getRetry() > 0 && // Error occurred after at least one attempt
            getToggleCount() < 2 && // Limit player toggles (e.g., Exo <-> Ijk)
            mPlayers != null && mPlayers.getPlayer() != Players.SYS) // Don't toggle away from SYS
        {
            Log.w("VideoActivity", "URL error, toggling player. Toggle count: " + getToggleCount());
            toggleCount++;
            nextPlayer(); // Switch player (e.g., Exo -> Ijk or vice-versa)
        } else {
            // Cannot toggle player or toggle limit reached, proceed to next error flow step
            resetToggle(); // Reset toggle count for the next source/flag/parse
            onError(event); // Go to the main error handling flow (parse/flag/site)
        }
    }


    private void nextPlayer() {
        if (mPlayers == null) return;
        mPlayers.nextPlayer(); // Switch to the other player (Exo/Ijk)
        setPlayerView(); // Update player button text
        setDecodeView(); // Update decode button text (might change with player)
        onRefresh(); // Retry playback with the new player
    }

    private void onErrorEnd(ErrorEvent event) {
        if (event == null) return;
        Log.e("VideoActivity", "onErrorEnd: Max retries/errors reached. Stopping. Error: " + event.getMsg());
        onErrorPlayer(event); // Show error message, stop player
        resetError(); // Reset error counter
        // Optionally show a persistent message or exit?
        // showEmpty(); // Show empty state?
    }

    private void onErrorPlayer(ErrorEvent event) {
        if (event == null) return;
        Track.delete(getHistoryKey()); // Clear saved tracks for this VOD on error
        showError(event.getMsg()); // Display error message on screen
        if (mClock != null) mClock.setCallback(null); // Stop clock updates
        if (mPlayers != null) {
            mPlayers.reset(); // Reset player state
            mPlayers.stop(); // Stop playback fully
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Allow screen off
    }

    private void onError(ErrorEvent event) {
        if (event == null) return;
        Log.w("VideoActivity", "onError: Handling error flow. Error: " + event.getMsg());
        onErrorPlayer(event); // Stop player, show error message first
        startFlow(); // Start the flow to find alternative source
    }

    private void startFlow() {
        Site site = getSite();
        if (site == null || !site.isChangeable()) {
             Log.w("VideoActivity", "Error flow stopped: Site is null or not changeable.");
             showEmpty(); // Show empty state if no alternatives can be tried
             return;
        }

        if (isUseParse()) {
             Log.d("VideoActivity", "Error flow: Checking next parse.");
             checkParse(); // Try next parser first if using parse
        } else {
             Log.d("VideoActivity", "Error flow: Checking next flag.");
             checkFlagFlow(); // Otherwise, try next flag/source directly
        }
    }

    private void checkParse() {
        if (mParseAdapter == null || mParseAdapter.size() == 0) {
             Log.w("VideoActivity", "Error flow: No parses available, checking flag.");
             checkFlagFlow(); // No parses, move to flags
             return;
        }

        int position = getParsePosition();
        boolean isLastParse = (position >= mParseAdapter.size() - 1);

        if (isLastParse) {
            Log.d("VideoActivity", "Error flow: Last parse tried, resetting to first and checking flag.");
            initParse(); // Reset to the first parser for the next potential attempt
            checkFlagFlow(); // Move to check flags after trying all parses
        } else {
            Log.d("VideoActivity", "Error flow: Trying next parse.");
            nextParse(position); // Try the next available parser
        }
    }

    private void initParse() {
        if (mParseAdapter == null || mParseAdapter.size() == 0) return;
        Parse firstParse = (Parse) mParseAdapter.get(0);
        if (firstParse != null) {
             Log.d("VideoActivity", "Error flow: Resetting active parse to: " + firstParse.getName());
             setParseActivated(firstParse); // Activate the first parse visually/logically
        }
    }

    // Renamed from checkFlag to avoid confusion with the initial checkFlag(Vod)
    private void checkFlagFlow() {
         if (mBinding == null || mFlagAdapter == null || mFlagAdapter.size() == 0 || isGone(mBinding.flag)) {
              Log.w("VideoActivity", "Error flow: No flags available or visible, checking search.");
              checkSearch(false); // No flags, move to search
              return;
         }

        int position = getFlagPosition();
        boolean isLastFlag = (position >= mFlagAdapter.size() - 1);

        if (isLastFlag) {
            Log.d("VideoActivity", "Error flow: Last flag tried, checking search.");
            checkSearch(false); // Tried all flags, move to search
        } else {
            Log.d("VideoActivity", "Error flow: Trying next flag.");
            nextFlag(position); // Try the next available flag
        }
    }


    // --- Modified checkSearch ---
    private void checkSearch(boolean force) {
        if (mQuickAdapter != null && mQuickAdapter.size() > 0 && (isAutoMode() || force)) {
            // If quick search results exist and auto mode is on or forced, try next site
             Log.d("VideoActivity", "Error flow: Quick search results exist, trying next site.");
             nextSite();
        } else if (mQuickAdapter == null || mQuickAdapter.size() == 0) {
            // If no quick search results, initiate a new search
             Log.d("VideoActivity", "Error flow: No quick search results, initiating search.");

             // Use stored original VOD name for initial search keyword
             String keyword = currentVodName;
             if (TextUtils.isEmpty(keyword)) {
                  // Try getting from intent again if detail loading failed initially
                  keyword = getName();
             }

             // Only start search if we have a name
             if (!TextUtils.isEmpty(keyword)) {
                  initSearch(keyword, true); // Start search in auto mode
             } else {
                  Log.w("VideoActivity", "Cannot start search, VOD name is empty.");
                  showEmpty(); // Show empty state if no name available
             }
        } else {
             // Quick search results exist, but not in auto mode and not forced
             Log.d("VideoActivity", "Error flow: Quick search results available, but not auto-searching.");
             // User needs to manually select from mQuickAdapter or trigger force search
             // Maybe focus the quick search list?
             if (mBinding != null && isVisible(mBinding.quick)) {
                  mBinding.quick.requestFocus();
             } else {
                  showEmpty(); // Or show empty if quick search isn't visible/available
             }
        }
    }
    // --- End Modified checkSearch ---

    private void initSearch(String keyword, boolean auto) {
        if (TextUtils.isEmpty(keyword)) {
             Log.w("VideoActivity", "initSearch called with empty keyword.");
             return;
        }
        Log.d("VideoActivity", "initSearch: Keyword='" + keyword + "', Auto=" + auto);
        stopSearch(); // Stop any previous search executor
        setAutoMode(auto);
        setInitAuto(auto); // Mark that this search was started automatically
        startSearch(keyword);
        if (mBinding != null) mBinding.part.setTag(keyword); // Store keyword for mismatch check
    }


    private boolean isPass(Site item) {
        if (item == null) return false;
        // If in auto mode, only consider sites marked as changeable/searchable
        // If not in auto mode (manual search), consider all searchable sites
        boolean checkChangeable = isAutoMode(); // Check changeable only if auto-searching alternatives
        if (checkChangeable && !item.isChangeable()) return false;
        return item.isSearchable();
    }


    private void startSearch(String keyword) {
        if (TextUtils.isEmpty(keyword)) return;
        Log.d("VideoActivity", "Starting search for keyword: " + keyword);

        // Clear previous quick search results
        if (mQuickAdapter != null) mQuickAdapter.clear();
        if (mBinding != null) mBinding.quick.setVisibility(View.GONE); // Hide initially

        List<Site> sitesToSearch = new ArrayList<>();
        for (Site site : VodConfig.get().getSites()) {
            if (isPass(site)) { // Check if site should be included in this search
                sitesToSearch.add(site);
            }
        }

        if (sitesToSearch.isEmpty()) {
             Log.w("VideoActivity", "No searchable sites found matching criteria.");
             // If initiated automatically, maybe show empty state?
             if (isAutoMode()) showEmpty();
             return;
        }

        // Create new executor for this search session
        mExecutor = Executors.newFixedThreadPool(Constant.THREAD_POOL);
        Log.d("VideoActivity", "Searching on " + sitesToSearch.size() + " sites.");
        for (Site site : sitesToSearch) {
            mExecutor.execute(() -> search(site, keyword));
        }
        // Executor will be stopped when search is done or activity is destroyed/paused
    }


    private void stopSearch() {
        if (mExecutor != null && !mExecutor.isShutdown()) {
            Log.d("VideoActivity", "Stopping search executor.");
            try {
                mExecutor.shutdownNow(); // Attempt to stop all executing tasks
            } catch (Exception e) {
                Log.e("VideoActivity", "Error shutting down search executor", e);
            }
            mExecutor = null;
        }
        setAutoMode(false); // Turn off auto mode when search stops or is stopped
        setInitAuto(false);
    }

    private void search(Site site, String keyword) {
        if (site == null || TextUtils.isEmpty(keyword) || mViewModel == null) return;
        try {
            Log.v("VideoActivity", "Executing search on site: " + site.getName() + " for: " + keyword); // Verbose log
            mViewModel.searchContent(site, keyword, true); // isVod = true for video search
        } catch (Throwable e) {
            // Catch potential exceptions from searchContent itself (though unlikely if implemented well)
            Log.e("VideoActivity", "Error executing search for site: " + site.getName(), e);
        }
    }

    // This method receives results from SiteViewModel.search observer
    private void setSearch(Result result) {
        if (result == null || result.getList() == null || mQuickAdapter == null || mBinding == null) {
             // Log if result is null or list is null, might indicate an API error upstream
             if (result == null || result.getList() == null) {
                  Log.w("VideoActivity", "Received null search result or null list.");
             }
             return;
        }

        List<Vod> items = result.getList();
        Log.d("VideoActivity", "Received " + items.size() + " search results from site: " + (items.isEmpty() ? "N/A" : items.get(0).getSiteKey())); // Assuming siteKey is set

        // Filter out mismatched items
        Iterator<Vod> iterator = items.iterator();
        while (iterator.hasNext()) {
            if (mismatch(iterator.next())) {
                iterator.remove();
            }
        }

        Log.d("VideoActivity", "Adding " + items.size() + " matched results to QuickSearch.");
        if (!items.isEmpty()) {
            mQuickAdapter.addAll(mQuickAdapter.size(), items);
            mBinding.quick.setVisibility(View.VISIBLE); // Show quick search list
            App.removeCallbacks(mR4); // Remove empty state callback if we found results

            // If this search was started automatically, try playing the first result immediately
            if (isInitAuto()) {
                 Log.d("VideoActivity", "Auto mode: Initiating nextSite to play first result.");
                 nextSite();
            }
             // Update focus rules as visibility changed
             setR2Callback(100);
        } else {
             // If no matched items were found from this site, do nothing here.
             // The overall process continues until all sites respond or a match is played.
             // If all sites finish and no results, showEmpty might be called eventually.
        }
    }

    // This method is called when a user clicks an item in the QuickSearch list
    private void setSearch(Vod item) {
        if (item == null) return;
        Log.d("VideoActivity", "User selected item from QuickSearch: " + item.getVodName() + " (" + item.getSiteName() + ")");
        setAutoMode(false); // Turn off auto mode as user made a selection
        setInitAuto(false);
        stopSearch(); // Stop any ongoing background searches
        getDetail(item); // Load details for the selected item
    }


    private boolean mismatch(Vod item) {
        if (item == null || TextUtils.isEmpty(item.getVodId())) return true; // Invalid item is a mismatch

        // Don't show the exact same VOD ID we are currently trying to play (or failed from)
        if (getId().equals(item.getVodId())) return true;

        // Don't show VOD IDs that previously failed in this session (mBroken list)
        if (mBroken.contains(item.getVodId())) return true;

        // Get the search keyword (should be the original title or selected part)
        String keyword = Objects.toString(mBinding.part.getTag(), "");
        if (TextUtils.isEmpty(keyword)) return false; // No keyword? Allow everything (shouldn't happen ideally)

        String itemName = item.getVodName();
        if (TextUtils.isEmpty(itemName)) return true; // Item without name is a mismatch

        // Matching logic:
        // In Auto Mode: Require exact name match (or very close similarity?)
        // In Manual Mode (user clicked part): Require name contains keyword
        if (isAutoMode()) {
            // Exact match seems too strict, allow for minor variations?
            // Maybe use a similarity score? For now, let's try exact match.
             // Consider normalizing names (lowercase, remove spaces) before comparing?
             // return !itemName.equalsIgnoreCase(keyword); // Case-insensitive exact match
             // Let's relax it slightly: check if names contain each other for auto mode too?
             return !(itemName.contains(keyword) || keyword.contains(itemName));
        } else {
            // Manual search: Check if item name contains the keyword (case-insensitive)
             return !itemName.toLowerCase().contains(keyword.toLowerCase());
        }
    }


    private void nextParse(int currentPosition) {
         if (mParseAdapter == null || currentPosition + 1 >= mParseAdapter.size()) {
              Log.w("VideoActivity", "nextParse called but no next parse available.");
              // Should already be handled by checkParse logic, but double-check
              checkFlagFlow(); // Move to flags if no more parses
              return;
         }
        Parse nextParse = (Parse) mParseAdapter.get(currentPosition + 1);
        if (nextParse != null) {
            Notify.show(getString(R.string.play_switch_parse, nextParse.getName()));
            setParseActivated(nextParse); // This will trigger onRefresh
        } else {
             // Skip null parse and try the one after? Or move to flags?
             Log.w("VideoActivity", "Found null Parse object at index " + (currentPosition + 1));
             checkFlagFlow(); // Move to flags if parse object is unexpectedly null
        }
    }

    private void nextFlag(int currentPosition) {
         if (mFlagAdapter == null || currentPosition + 1 >= mFlagAdapter.size()) {
              Log.w("VideoActivity", "nextFlag called but no next flag available.");
              checkSearch(false); // Move to search if no more flags
              return;
         }
        Flag nextFlag = (Flag) mFlagAdapter.get(currentPosition + 1);
        if (nextFlag != null) {
            Notify.show(getString(R.string.play_switch_flag, nextFlag.getFlag()));
            setFlagActivated(nextFlag); // This should trigger seamless or getPlayer via setEpisodeAdapter/seamless
        } else {
             Log.w("VideoActivity", "Found null Flag object at index " + (currentPosition + 1));
             checkSearch(false); // Move to search if flag object is unexpectedly null
        }
    }

    private void nextSite() {
        if (mQuickAdapter == null || mQuickAdapter.size() == 0) {
             Log.w("VideoActivity", "nextSite called but QuickSearch adapter is empty.");
             // If this was called automatically, it means search yielded no results yet or failed.
             if (isAutoMode()) {
                 stopSearch(); // Stop searching
                  showEmpty(); // Show empty state as auto search failed
             }
             return;
        }

        Vod item = (Vod) mQuickAdapter.get(0); // Get the first item from the quick search results
        mQuickAdapter.removeItems(0, 1); // Remove it from the list

        if (item == null) {
            Log.w("VideoActivity", "nextSite found null item at index 0, trying next.");
            nextSite(); // Recursively call to try the next item if the first was null
            return;
        }

        Notify.show(getString(R.string.play_switch_site, item.getSiteName()));
        Log.d("VideoActivity", "Switching to site: " + item.getSiteName() + " | VOD: " + item.getVodName() + " (" + item.getVodId() + ")");

        // Add the ID of the *currently failing* VOD to the broken list
        // to prevent switching back to it immediately if it appears in search results.
        String currentFailedId = getId();
        if (!TextUtils.isEmpty(currentFailedId) && !currentFailedId.equals(item.getVodId())) {
             mBroken.add(currentFailedId);
        }

        setInitAuto(false); // Mark that we are now loading this specific item, not just auto-searching
        // Stop further background searching as we are loading a result
        // Don't call stopSearch() here if you want other site searches to complete
        // and potentially add more items to mQuickAdapter for manual selection later.
        // Let's stop it for now to prevent unnecessary background work.
        stopSearch();

        getDetail(item); // Load details for the new VOD item
    }


    private void onPaused() {
        if (mPlayers == null || mBinding == null) return;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Allow screen off
        mBinding.widget.exoDuration.setText(mPlayers.getDurationTime());
        mBinding.widget.exoPosition.setText(mPlayers.getPositionTime(0));
        mBinding.widget.action.setImageResource(R.drawable.ic_widget_pause); // Show pause icon in center

        if (isFullscreen()) {
            showInfoAndCenter(); // Show info + center icon when paused in fullscreen
        } else {
            hideInfoAndCenter(); // Hide info + center when paused in windowed mode
        }
        mPlayers.pause();
    }

    private void onPlay() {
        if (mPlayers == null || mBinding == null) return;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on
        mPlayers.play();
        hideCenter(); // Hide center icon (play/pause/ff/rw)
        hideInfo(); // Hide top info bar
    }

    public boolean isBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    private void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        // Optionally, update system UI visibility flags here if needed
        // ResUtil.toggleSystemUI(this, fullscreen);
    }

    private boolean isInitTrack() {
        return initTrack;
    }

    private void setInitTrack(boolean initTrack) {
        this.initTrack = initTrack;
    }

    private boolean isInitAuto() {
        return initAuto;
    }

    private void setInitAuto(boolean initAuto) {
        this.initAuto = initAuto;
    }

    private boolean isAutoMode() {
        return autoMode;
    }

    private void setAutoMode(boolean autoMode) {
        this.autoMode = autoMode;
    }

    public boolean isUseParse() {
        return useParse;
    }

    public void setUseParse(boolean useParse) {
        this.useParse = useParse;
    }

    public int getToggleCount() {
        return toggleCount;
    }

    public void resetToggle() {
        this.toggleCount = 0;
    }

    public int addErrorCount() {
        return ++errorCount;
    }

    public void resetError() {
        this.errorCount = 0;
    }

    public int getGroupSize() {
        return groupSize > 0 ? groupSize : 20; // Ensure groupSize has a default positive value
    }

    public void setGroupSize(int size) {
        groupSize = Math.max(size, 1); // Ensure group size is at least 1
    }

    private View getFocus1() {
        // Return the stored focus view when exiting fullscreen, fallback to video view
        return (mFocus1 != null && mFocus1.isFocusable()) ? mFocus1 : mBinding.video;
    }

    private View getFocus2() {
        // Return the stored focused view within controls, with fallbacks
        View focusTarget = mFocus2;

        // Check if stored focus is valid and visible within the controls layout
        boolean isFocus2Valid = focusTarget != null
                                && isVisible(focusTarget)
                                && focusTarget.isEnabled()
                                && focusTarget.isFocusable()
                                && isDescendantOf(mBinding.control.getRoot(), focusTarget);

        if (!isFocus2Valid) {
            // Fallback 1: Try the 'next' button
            focusTarget = mBinding.control.next;
            if (focusTarget == null || !isVisible(focusTarget) || !focusTarget.isEnabled()) {
                // Fallback 2: Try the 'play/pause' button
                focusTarget = mBinding.control.play;
                 if (focusTarget == null || !isVisible(focusTarget) || !focusTarget.isEnabled()) {
                      // Fallback 3: Find the first focusable view in the controls
                      focusTarget = mBinding.control.getRoot().findFocus();
                      if (focusTarget == null) {
                           // Absolute Fallback: video view itself (though controls should have *something*)
                           focusTarget = mBinding.video;
                      }
                 }
            }
        }
        return focusTarget;
    }

    // Helper to check if view is a descendant of parent
    private boolean isDescendantOf(ViewGroup parent, View view) {
        if (parent == null || view == null) return false;
        ViewParent currentParent = view.getParent();
        while (currentParent != null) {
            if (currentParent == parent) return true;
            currentParent = currentParent.getParent();
        }
        return false;
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event == null) return super.dispatchKeyEvent(null);

        hasKeyEvent = true; // Mark that a key event occurred

        // Back key handling for small window mode
        View currentFocus = getCurrentFocus();
        if (mBinding != null && mBinding.progressLayout.isContent() && !isFullscreen() && KeyUtil.isBackKey(event) && Setting.getSmallWindowBackKey() == 1) {
            // If focus is not on the video view itself, move focus to video view first
            if (currentFocus != mBinding.video) {
                 mFocus1 = mBinding.video; // Store video view as the target focus
                 if (mFocus1 != null) mFocus1.requestFocus();
                 return true; // Consume back press event, focus moved
            }
            // If focus is already on video view, let default back behavior happen (exit fullscreen or activity)
        }

        // Menu key handling in fullscreen
        if (isFullscreen() && KeyUtil.isMenuKey(event) && event.getAction() == KeyEvent.ACTION_DOWN) { // Check action_down
             if (Setting.getFullscreenMenuKey() == 0) {
                 onToggle(); // Toggle controls
                 return true; // Consume event
             } else if (Setting.getFullscreenMenuKey() == 1) {
                 onEpisodes(); // Show episodes dialog
                 return true; // Consume event
             }
        }

        // Store focus within controls when controls are visible
        if (mBinding != null && isVisible(mBinding.control.getRoot()) && currentFocus != null && isDescendantOf(mBinding.control.getRoot(), currentFocus)) {
            mFocus2 = currentFocus;
        }

        // Auto-hide controls timer reset
        if (mBinding != null && isVisible(mBinding.control.getRoot())) {
            setR1Callback();
        }

        // Custom key handling in fullscreen when controls are hidden
        if (isFullscreen() && mBinding != null && isGone(mBinding.control.getRoot()) && mKeyDown != null && mKeyDown.hasEvent(event)) {
            return mKeyDown.onKeyDown(event); // Let CustomKeyDownVod handle it
        }

        return super.dispatchKeyEvent(event); // Default dispatch
    }


    // --- CustomKeyDownVod.Listener Callbacks ---

    @Override
    public void onBright(int progress) {
        if (mBinding == null) return;
        mBinding.widget.bright.setVisibility(View.VISIBLE);
        mBinding.widget.brightProgress.setProgress(progress);
        if (progress < 35) mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_low);
        else if (progress < 70) mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_medium);
        else mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_high);
    }

    @Override
    public void onBrightEnd() {
        if (mBinding != null) mBinding.widget.bright.setVisibility(View.GONE);
    }

    @Override
    public void onVolume(int progress) {
        if (mBinding == null) return;
        mBinding.widget.volume.setVisibility(View.VISIBLE);
        mBinding.widget.volumeProgress.setProgress(progress);
        // Ensure icons are set correctly based on progress thresholds
        if (progress == 0) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_off); // Mute icon
        else if (progress < 35) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_low);
        else if (progress < 70) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_medium);
        else mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_high);
    }

    @Override
    public void onVolumeEnd() {
        if (mBinding != null) mBinding.widget.volume.setVisibility(View.GONE);
    }

    @Override
    public void onSeeking(int time) {
        if (mBinding == null || mPlayers == null) return;
        mBinding.widget.exoDuration.setText(mPlayers.getDurationTime());
        mBinding.widget.exoPosition.setText(mPlayers.getPositionTime(time));
        mBinding.widget.action.setImageResource(time > 0 ? R.drawable.ic_widget_forward : R.drawable.ic_widget_rewind);
        mBinding.widget.center.setVisibility(View.VISIBLE);
        hideProgress(); // Hide buffering progress while seeking preview is shown
    }

    @Override
    public void onSeekTo(int time) {
        if (mPlayers == null) return;
        mPlayers.seekTo(time);
        if (mKeyDown != null) mKeyDown.resetTime(); // Reset fast-forward/rewind timer in CustomKeyDownVod
        showProgress(); // Show progress indicator while player buffers after seek
        // onPlay(); // Ensure playback resumes/continues after seek - Player should handle this
    }

    @Override
    public void onSpeedUp() {
        if (mPlayers == null || !mPlayers.isPlaying() || !mPlayers.canAdjustSpeed() || mBinding == null) return;
        // Cycle through high speeds (e.g., 3x, 5x) - adjust logic as needed
        float targetSpeed = (mPlayers.getSpeed() < 3) ? 3f : 5f;
        mBinding.control.speed.setText(mPlayers.setSpeed(targetSpeed)); // Update text and set speed
        // Show visual feedback
        mBinding.widget.speed.startAnimation(ResUtil.getAnim(R.anim.forward));
        mBinding.widget.speedText.setText(getString(R.string.play_speed_val, String.valueOf(targetSpeed))); // Show speed value
        mBinding.widget.speed.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSpeedEnd() {
        if (mPlayers == null || mBinding == null) return;
        // Restore speed from history or default setting
        float previousSpeed = (mHistory != null) ? mHistory.getSpeed() : Setting.getPlaySpeed();
        mBinding.control.speed.setText(mPlayers.setSpeed(previousSpeed));
        // Hide visual feedback
        mBinding.widget.speed.setVisibility(View.GONE);
        mBinding.widget.speed.clearAnimation();
    }


    @Override
    public void onKeyUp() {
        if (mPlayers == null || mBinding == null) return;
        long current = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        long half = (duration > 0) ? duration / 2 : 0;

        showInfo(); // Show top info bar
        // Show controls, focusing on OP/ED buttons depending on position
        showControl(current < half ? mBinding.control.opening : mBinding.control.ending);
    }

    @Override
    public void onKeyDown() {
        if (mBinding == null) return;
        showInfo(); // Show top info bar
        showControl(getFocus2()); // Show controls, focusing on last known/default control
    }

    @Override
    public void onKeyCenter() {
        if (mPlayers == null) return;
        if (mPlayers.isPlaying()) {
            onPaused();
            // Keep controls visible briefly after pausing? Or hide immediately?
            // hideControl(false); // Hide controls but keep info bar?
        } else {
            onPlay();
            // hideControl(true); // Hide controls and info bar when resuming play
        }
    }

    @Override
    public void onSingleTap() {
        if (isFullscreen()) {
            onToggle(); // Toggle controls on single tap in fullscreen
        } else {
             // Optional: Handle single tap in windowed mode (e.g., enter fullscreen?)
             // onVideo();
        }
    }

    @Override
    public void onDoubleTap() {
        if (isFullscreen()) {
            onKeyCenter(); // Toggle play/pause on double tap in fullscreen
        }
    }

    // --- PlayerDialog.Listener ---
    @Override
    public void onPlayerClick(Integer item) {
        if (item == null || mPlayers == null) return;
        if (mPlayers.getPlayer() == item) return; // No change

        Log.d("VideoActivity", "Player selected from dialog: " + item);
        mPlayers.setPlayer(item); // Set the new player type
        // Save choice to history immediately? Or wait for successful playback?
        if (mHistory != null) {
             mHistory.setPlayer(item);
             if (!Setting.isIncognito()) mHistory.save();
        }
        setPlayerView(); // Update UI button
        setDecodeView(); // Update decode button (might change)
        onRefresh(); // Refresh playback with the new player
    }

    @Override
    public void onPlayerShare(String title) {
        Log.d("VideoActivity", "Share action clicked in PlayerDialog.");
        this.onChoose(); // Trigger the system share chooser
    }

    // --- Activity Lifecycle & Results ---

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case 1000: // Result from subsequent activity indicating finish
                setResult(RESULT_OK);
                finish();
                break;
            case 1001: // Result from external player/chooser
                if (data != null && mPlayers != null) {
                    mPlayers.checkData(data); // Let Players handle data from external player
                }
                break;
            // Handle other request codes if needed
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("VideoActivity", "onResume");
        setBackground(false);
        if (mClock != null) mClock.start();
        if (mPlayers != null) {
            // Resume playback only if it was playing before pause
            // Player class should ideally handle its own state persistence across pause/resume
             mPlayers.play(); // Or player.resume() if it has that method
        }
         // Restart traffic updates if enabled
         if (Setting.isDisplaySpeed()) {
             App.post(mR3, 0);
         }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("VideoActivity", "onPause");
        setBackground(true);
        if (mPlayers != null) {
            // Store position before pausing if needed, though onTimeChanged should handle it
             if (mHistory != null && !Setting.isIncognito()) {
                  mHistory.setPosition(mPlayers.getPosition());
                  mHistory.update(); // Save position on pause
             }
            mPlayers.pause();
        }
        if (mClock != null) mClock.stop();
         // Stop traffic updates
         App.removeCallbacks(mR3);
         stopSearch(); // Stop background searches when pausing activity
    }

    @Override
    public void onBackPressed() {
        if (mBinding != null && isVisible(mBinding.control.getRoot())) {
            hideControl(); // Hide controls first if visible
        } else if (mBinding != null && isVisible(mBinding.widget.center)) {
            hideCenter(); // Hide center icon (seek preview) if visible
             if (mPlayers != null) mPlayers.play(); // Resume play if seek was cancelled by back press
        } else if (isFullscreen()) {
            exitFullscreen(); // Exit fullscreen mode
        } else {
            stopSearch(); // Ensure search is stopped before exiting
            super.onBackPressed(); // Default back behavior (finish activity)
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("VideoActivity", "onDestroy");
        super.onDestroy();
        stopSearch(); // Stop search executor
        if (mClock != null) mClock.release();
        if (mPlayers != null) mPlayers.release(); // Release player resources
        Source.get().stop(); // Stop any ongoing source operations
        RefreshEvent.history(); // Notify history list might need update
        App.removeCallbacks(mR1, mR2, mR3, mR4); // Remove all callbacks

        // Clean up glow effect resources
        if (mBinding != null && mBinding.logoImageView != null) {
            mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null); // Remove layer
        }
        logoGlowPaint = null; // Clear paint reference

        // Clean up Glide resources (defensively)
        try {
            if (!isFinishing() && !isDestroyed()) {
                Glide.with(getApplicationContext()).onDestroy(); // Use application context
            }
        } catch (Exception e) {
            Log.e("VideoActivity", "Error during Glide onDestroy", e);
        }

        mBinding = null; // Release view binding
    }

    // Helper method to check if a View is visible
    private boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    // Helper method to check if a View is gone
    private boolean isGone(View view) {
        return view == null || view.getVisibility() == View.GONE;
    }

    // Helper method to notify item changes for adapters safely
    private void notifyItemChanged(RecyclerView view, RecyclerView.Adapter<?> adapter) {
        if (view != null && adapter != null && adapter.getItemCount() > 0) {
            // Avoid full notifyDataSetChanged if possible
            // adapter.notifyItemRangeChanged(0, adapter.getItemCount());
             adapter.notifyDataSetChanged(); // Use simpler notify for now
        }
    }
     private void notifyItemChanged(BaseGridView view, ArrayObjectAdapter adapter) {
         if (view != null && adapter != null && adapter.size() > 0) {
             adapter.notifyArrayItemRangeChanged(0, adapter.size());
         }
     }
     private void notifyItemChanged(BaseGridView view, QualityAdapter adapter) {
         if (view != null && adapter != null && adapter.getItemCount() > 0) {
             adapter.notifyItemRangeChanged(0, adapter.getItemCount());
         }
     }


} // End of VideoActivity class