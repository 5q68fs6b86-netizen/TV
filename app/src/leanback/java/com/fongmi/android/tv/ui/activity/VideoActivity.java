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
// import com.bumptech.glide.load.engine.GlideException; // Removed for simplicity
// import com.bumptech.glide.request.RequestListener;    // Removed for simplicity
import com.bumptech.glide.request.target.CustomTarget;
// import com.bumptech.glide.request.target.Target;      // Removed for simplicity
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

    // ... (Static start methods remain the same) ...
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
        // Simplified null check
        if (mFlagAdapter == null || mFlagAdapter.size() == 0) return null;
        int position = getFlagPosition();
        return (Flag) mFlagAdapter.get(position);
    }

    private Episode getEpisode() {
        // Simplified null check
        if (mEpisodeAdapter == null || mEpisodeAdapter.size() == 0) return null;
        int position = getEpisodePosition();
        // Check bounds before getting
        if (position >= 0 && position < mEpisodeAdapter.size()){
             return (Episode) mEpisodeAdapter.get(position);
        }
        return null; // Return null if position is invalid
    }

    private int getFlagPosition() {
        if (mFlagAdapter == null) return 0; // Added null check
        for (int i = 0; i < mFlagAdapter.size(); i++) if (((Flag) mFlagAdapter.get(i)).isActivated()) return i;
        return 0;
    }

    private int getEpisodePosition() {
        if (mEpisodeAdapter == null) return 0; // Added null check
        for (int i = 0; i < mEpisodeAdapter.size(); i++) if (((Episode) mEpisodeAdapter.get(i)).isActivated()) return i;
        return 0;
    }

    private int getParsePosition() {
        if (mParseAdapter == null) return 0; // Added null check
        for (int i = 0; i < mParseAdapter.size(); i++) if (((Parse) mParseAdapter.get(i)).isActivated()) return i;
        return 0;
    }

    private int getPlayer() {
        // Simplified logic (closer to original likely state)
        Site site = getSite();
        int sitePlayer = (site != null) ? site.getPlayerType() : -1;
        int historyPlayer = (mHistory != null) ? mHistory.getPlayer() : -1;

        if (historyPlayer != -1) return historyPlayer;
        if (sitePlayer != -1) return sitePlayer;
        return Setting.getPlayer();
    }

    private int getScale() {
        // Simplified logic
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
        if (mPlayers == null) return null; // Added null check
        return mPlayers.isExo() ? getExo().getDefaultArtwork() : getIjk().getDefaultArtwork();
    }

    private BaseGridView getEpisodeView() {
        return Setting.getEpisode() == 0 ? mBinding.episodeHori : mBinding.episodeVert;
    }

    private void setEpisodeSelectedPosition(int position) {
        BaseGridView episodeView = getEpisodeView();
        if (episodeView == null) return; // Added null check

        episodeView.setSelectedPosition(position);
        if (hasKeyEvent) return;
        if (isFullscreen()) return;
        episodeView.postDelayed(() -> {
            if (episodeView.getLayoutManager() != null) { // Add null check for layout manager
                View selectedItem = episodeView.getLayoutManager().findViewByPosition(position);
                View focusedView = getCurrentFocus();
                if (selectedItem != null) selectedItem.requestFocus();
                // Restore focus to video view if it was focused before
                if (focusedView == mBinding.video) mBinding.video.requestFocus();
            }
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
                // Simplified null check
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

        initGlowPaint(); // <-- Initialize Paint for glow effect

        setBackground(false);
        setRecyclerView();
        setEpisodeView(); // Call the method to set layout for vertical recycler view
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
                logoGlowPaint.setMaskFilter(new BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.OUTER));
            } catch (Exception e) {
                Log.e("VideoActivity", "Error initializing glow paint", e);
                logoGlowPaint = null;
            }
        }
    }
    // --- End Method ---

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initEvent() {
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

        // Simplified listener logic (closer to original)
        mBinding.flag.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                if (mFlagAdapter != null && mFlagAdapter.size() > 0) { // Basic check
                    setFlagActivated((Flag) mFlagAdapter.get(position));
                }
            }
        });

        getEpisodeView().addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                if (child != null) mFocus1 = child.itemView;
                setEpisodeChildKeyListener(child, position);
            }
        });

        mBinding.array.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                if (mEpisodeAdapter != null && mEpisodeAdapter.size() > getGroupSize() && position > 1 && hasKeyEvent) { // Basic check
                    setEpisodeSelectedPosition((position - 2) * getGroupSize());
                }
            }
        });
    }

    private void setEpisodeChildKeyListener(RecyclerView.ViewHolder child, int position) {
        // Simplified logic
        BaseGridView episodeView = getEpisodeView();
        if (episodeView != mBinding.episodeVert || child == null || mEpisodePresenter == null) return; // Basic checks
        RecyclerView.Adapter<?> adapter = episodeView.getAdapter();
        if (adapter == null) return;
        int itemCount = adapter.getItemCount();
        if (itemCount <= 0) return;

        int columns = mEpisodePresenter.getNumColumns();
        if (columns <= 0) return; // Avoid division by zero

        // Basic check for last row items needing special handling
        boolean isPotentiallyLastIncompleteRowItem = (position / columns == (itemCount - 1) / columns) && (position % columns >= (itemCount % columns)) && (itemCount % columns != 0);

        if (isPotentiallyLastIncompleteRowItem) {
            child.itemView.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (episodeView.getLayoutManager() != null) {
                        View lastItem = episodeView.getLayoutManager().findViewByPosition(itemCount - 1);
                        if (lastItem != null) lastItem.requestFocus();
                        return true;
                    }
                }
                return false;
            });
        } else {
            child.itemView.setOnKeyListener(null); // Remove listener if not needed
        }
    }


    private void setRecyclerView() {
        // Simplified setup
        mFlagAdapter = new ArrayObjectAdapter(mFlagPresenter = new FlagPresenter(this::setFlagActivated));
        mBinding.flag.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.flag.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.flag.setAdapter(new ItemBridgeAdapter(mFlagAdapter));

        mQualityAdapter = new QualityAdapter(this::setQualityActivated);
        mBinding.quality.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.quality.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.quality.setAdapter(mQualityAdapter);

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

    private void setEpisodeView() { // Renamed back
        // Simplified setup
        mEpisodeAdapter = new ArrayObjectAdapter(mEpisodePresenter = new EpisodePresenter(this::setEpisodeActivated));
        mBinding.episodeVert.setVerticalSpacing(ResUtil.dp2px(8));
        mBinding.episodeHori.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.episodeVert.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.episodeHori.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        BaseGridView episodeView = getEpisodeView();
        if (episodeView != null) {
             episodeView.setAdapter(new ItemBridgeAdapter(mEpisodeAdapter));
             // Call the layout logic specifically for vertical view if needed
             // This part might need adjustment based on original structure
             // if (episodeView == mBinding.episodeVert) {
             //     // Call the layout calculation logic here if it was separate
             //     calculateAndSetEpisodeVerticalLayout(...);
             // }
        }
    }

    // Optional: If layout calculation was complex and separate, keep it here
     private void calculateAndSetEpisodeVerticalLayout(List<Episode> items) {
         if (items == null || items.isEmpty() || mBinding == null || mEpisodePresenter == null) return;

         int size = items.size();
         int maxEpisodeNameLength = 0;
         for (int i = 0; i < size; i++) {
             Episode episode = items.get(i);
             if (episode == null) continue;
             episode.setIndex(i);
             String name = episode.getName();
             int length = (name == null) ? 0 : name.length();
             if (length > maxEpisodeNameLength) maxEpisodeNameLength = length;
         }

         int numColumns = 10;
         if (maxEpisodeNameLength > 40) numColumns = 1;
         else if (maxEpisodeNameLength > 30) numColumns = 2;
         else if (maxEpisodeNameLength > 15) numColumns = 3;
         else if (maxEpisodeNameLength > 10) numColumns = 4;
         else if (maxEpisodeNameLength > 6) numColumns = 6;
         else if (maxEpisodeNameLength > 4) numColumns = 8;
         if (numColumns <= 0) numColumns = 1;

         int rowNum = (int) Math.ceil((double) size / numColumns);
         int screenWidth = ResUtil.getScreenWidth();
         int availableWidth = screenWidth - ResUtil.dp2px(48);

         ViewGroup.LayoutParams params = mBinding.episodeVert.getLayoutParams();
         if (params == null) params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
         params.width = screenWidth;
         params.height = (rowNum > 6) ? ResUtil.dp2px(300) : ResUtil.dp2px(Math.max(rowNum, 1) * 44);
         mBinding.episodeVert.setLayoutParams(params);
         mBinding.episodeVert.setNumColumns(numColumns);

         int horizontalSpacing = ResUtil.dp2px(8);
         int columnWidth = (availableWidth - ((numColumns - 1) * horizontalSpacing)) / numColumns;
         if (columnWidth > 0) mBinding.episodeVert.setColumnWidth(columnWidth);
         else mBinding.episodeVert.setColumnWidth(ViewGroup.LayoutParams.WRAP_CONTENT);

         mBinding.episodeVert.setWindowAlignmentOffsetPercent(35f);
         mEpisodePresenter.setNumColumns(numColumns);
         mEpisodePresenter.setNumRows(rowNum);
     }


    private void setVideoView() {
        if (mPlayers == null) return; // Added check
        mPlayers.init(getExo(), getIjk());
        ExoUtil.setSubtitleView(mBinding.exo);
        IjkUtil.setSubtitleView(mBinding.ijk);
        mBinding.control.reset.setText(ResUtil.getStringArray(R.array.select_reset)[Setting.getReset()]);
        mBinding.exo.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        mBinding.ijk.setBackgroundColor(android.graphics.Color.TRANSPARENT);
    }

    private void setDanmuViewSettings() {
        if (mDanmakuContext == null) return; // Added check
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
        if (mPlayers == null || mDanmakuContext == null) return; // Added check
        mPlayers.setDanmuView(mBinding.danmaku);
        setDanmuViewSettings();
        mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDanmakuMargin(8);
        mBinding.control.danmu.setActivated(Setting.isDanmu());
    }

    private void setDisplayView() {
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
        else mBinding.progressLayout.showProgress();
    }

    private void checkId() {
        String id = getId(); // Get ID once
        if (id.startsWith("push://")) {
            getIntent().putExtra("key", "push_agent").putExtra("id", id.substring(7));
            id = getIntent().getStringExtra("id"); // Update local variable after modification
        }
        if (TextUtils.isEmpty(id) || id.startsWith("msearch:")) {
            setEmpty(false);
        } else {
            getDetail();
        }
    }

    private void setPlayerView() {
        if (mPlayers == null) return; // Added check
        getIjk().setPlayer(mPlayers.getPlayer());
        mBinding.control.player.setText(mPlayers.getPlayerText());
        mBinding.control.speed.setEnabled(mPlayers.canAdjustSpeed());
        getExo().setVisibility(mPlayers.isExo() ? View.VISIBLE : View.GONE);
        getIjk().setVisibility(mPlayers.isIjk() ? View.VISIBLE : View.GONE);
        if (mHistory != null) {
            mBinding.control.speed.setText(mPlayers.setSpeed(mHistory.getSpeed()));
        } else {
             mBinding.control.speed.setText(mPlayers.setSpeed(Setting.getPlaySpeed())); // Default if no history
        }
    }

    private void setDecodeView() {
        if (mPlayers == null) return; // Added check
        mBinding.control.decode.setText(mPlayers.getDecodeText());
    }

    private void setScale(int scale) {
        if (scale < 0 || scale >= ResUtil.getStringArray(R.array.select_scale).length) return; // Bounds check
        getExo().setResizeMode(scale);
        getIjk().setResizeMode(scale);
        mBinding.control.scale.setText(ResUtil.getStringArray(R.array.select_scale)[scale]);
    }

    private void getDetail() {
        // Basic check
        if (mViewModel != null && !TextUtils.isEmpty(getKey()) && !TextUtils.isEmpty(getId())) {
             mViewModel.detailContent(getKey(), getId());
        }
    }

    private void getDetail(Vod item) {
        // Basic check
        if (item == null || TextUtils.isEmpty(item.getSiteKey()) || TextUtils.isEmpty(item.getVodId())) return;
        getIntent().putExtra("key", item.getSiteKey());
        getIntent().putExtra("pic", item.getVodPic());
        getIntent().putExtra("id", item.getVodId());
        mBinding.scroll.scrollTo(0, 0);
        if (mClock != null) mClock.setCallback(null); // Added check
        if (mPlayers != null) { // Added check
            mPlayers.reset();
            mPlayers.stop();
        }
        getDetail();
    }

    private void setDetail(Result result) {
        // Basic check
        if (result == null) {
            setEmpty(true); // Consider null result as empty/error
            return;
        }
        if (result.getList().isEmpty()) {
            setEmpty(result.hasMsg());
        } else {
            setDetail(result.getList().get(0));
        }
        Notify.show(result.getMsg());
    }

    private void getPlayer(Flag flag, Episode episode, boolean replay) {
        // Basic checks
        if (mViewModel == null || flag == null || episode == null || mPlayers == null) return;

        String combinedTitle = getString(R.string.detail_title, currentVodName, episode.getName());
        mBinding.widget.title.setText(combinedTitle);
        mBinding.display.title.setText(combinedTitle);

        mViewModel.playerContent(getKey(), flag.getFlag(), episode.getUrl());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        updateHistory(episode, replay);
        mPlayers.clear();
        mPlayers.stop();
        showProgress();
        setMetadata();
        hidePreview();
        hideCenter();
    }

    private void setPlayer(Result result) {
        // Basic checks
        if (result == null || mPlayers == null || mQualityAdapter == null) return;
        Site site = getSite();
        if (site == null) return;

        result.getUrl().set(mQualityAdapter.getPosition());
        setUseParse(VodConfig.hasParse() && ((result.getPlayUrl().isEmpty() && VodConfig.get().getFlags().contains(result.getFlag())) || result.getJx() == 1));
        mPlayers.start(result, isUseParse(), site.isChangeable() ? site.getTimeout() : -1);
        mBinding.control.parse.setVisibility(isUseParse() ? View.VISIBLE : View.GONE);
        setQualityVisible(result.getUrl().isMulti());
        checkDanmu(result.getDanmaku());
        mQualityAdapter.addAll(result);
    }

    private void checkDanmu(String danmu) {
        // Basic checks
        if (mBinding == null || mDanmakuContext == null) return;
        mBinding.danmaku.release();
        if (!Setting.isDanmuLoad() || TextUtils.isEmpty(danmu)) {
            mBinding.danmaku.setVisibility(View.GONE);
            return;
        }
        mBinding.danmaku.setVisibility(View.VISIBLE);
        App.execute(() -> {
             // Check binding again in background thread
             if (mBinding != null) {
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
            if (mBinding != null) { // Added check
                mBinding.logoImageView.setVisibility(View.GONE);
                mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null); // Ensure no glow
                mBinding.nameTextView.setText(getName());
                mBinding.nameTextView.setVisibility(View.VISIBLE);
                App.post(mR4, 10000);
            }
            checkSearch(false);
        }
    }

    private void showEmpty() {
        if (mBinding != null) mBinding.progressLayout.showEmpty(); // Added check
        stopSearch();
    }

    // --- Modified setDetail(Vod item) with Glow Effect Logic ---
    private void setDetail(Vod item) {
        // Basic check
        if (item == null || mBinding == null) {
             setEmpty(true); // Treat null item as error/empty
             return;
        }

        mBinding.progressLayout.showContent();
        mBinding.video.setTag(item.getVodPic(getPic()));

        // 1. Store original VOD name
        currentVodName = item.getVodName(getName());
        currentLogoUrl = null; // Reset logo url

        // 2. Initial title state: show text, hide logo, remove glow
        mBinding.logoImageView.setVisibility(View.GONE);
        mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null); // <-- Remove glow
        mBinding.nameTextView.setVisibility(View.VISIBLE);
        mBinding.nameTextView.setText(currentVodName);

        // Set other details
        setText(mBinding.remark, 0, item.getVodRemarks());
        setText(mBinding.year, R.string.detail_year, item.getVodYear());
        setText(mBinding.area, R.string.detail_area, item.getVodArea());
        setText(mBinding.type, R.string.detail_type, item.getTypeName());
        Site site = getSite(); // Get site info
        setText(mBinding.site, R.string.detail_site, site != null ? site.getName() : ""); // Handle null site
        // Handle potential null values from HTML parsing more gracefully
        setText(mBinding.actor, R.string.detail_actor, Html.fromHtml(Objects.toString(item.getVodActor(),"")).toString());
        setText(mBinding.content, R.string.detail_content, Html.fromHtml(Objects.toString(item.getVodContent(),"")).toString());
        setText(mBinding.director, R.string.detail_director, Html.fromHtml(Objects.toString(item.getVodDirector(),"")).toString());


        if (mFlagAdapter != null) mFlagAdapter.setItems(item.getVodFlags(), null); // Added null check
        mBinding.content.setMaxLines(getMaxLines());
        setArtwork(item.getVodPic());
        getPart(item.getVodName());
        App.removeCallbacks(mR4);

        // 4. Call TMDB Logo fetch logic
        fetchTmdbLogo(currentVodName, item.getVodYear(), item.getTypeName());

        checkHistory(item);
        checkFlag(item);
        checkKeep();
    }
    // --- End Modified setDetail ---

    // --- fetchTmdbLogo Method with Glow Effect (Simplified) ---
    private void fetchTmdbLogo(String title, String year, String typeName) {
        if (mBinding == null) return; // Basic Check

        // API Key Check
        if (TextUtils.isEmpty(Constant.TMDB_API_KEY) || "YOUR_TMDB_API_KEY_HERE".equals(Constant.TMDB_API_KEY)) {
            Log.e("VideoActivity", "TMDB API Key not set! Skipping logo fetch.");
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
                 // No Activity state checks here (removed for simplicity)
                 if (mBinding == null) return; // Check binding

                currentLogoUrl = logoUrl;
                mBinding.nameTextView.setVisibility(View.GONE);
                mBinding.logoImageView.setVisibility(View.VISIBLE);

                 // Clear any previous layer
                 mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null);

                try {
                    int targetPixelHeight = getResources().getDimensionPixelSize(R.dimen.detail_title_area_height);
                    int targetPixelWidth = targetPixelHeight * 4; // Example ratio
                    Log.d("VideoActivity", "Glide override target size: " + targetPixelWidth + "x" + targetPixelHeight);

                    Glide.with(VideoActivity.this) // Use Activity context
                         .load(logoUrl)
                         .placeholder(R.drawable.ic_placeholder)
                         .error(R.drawable.ic_error)
                         .override(targetPixelWidth, targetPixelHeight)
                         .fitCenter()
                         .into(mBinding.logoImageView); // Load into ImageView

                    // Apply glow effect AFTER Glide starts loading (might show briefly without glow)
                    initGlowPaint();
                    if (logoGlowPaint != null) {
                         mBinding.logoImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, logoGlowPaint); // <-- Apply glow
                    }

                } catch (Exception e) {
                    Log.e("VideoActivity", "Error during Glide load setup or execution in onLogoFound", e);
                    // Ensure no glow layer on exception and fallback UI
                    mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null);
                    onLogoNotFound();
                }
            }

            @Override
            public void onLogoNotFound() {
                 // No Activity state checks here
                 if (mBinding == null) return; // Check binding

                currentLogoUrl = null;
                // Ensure no glow layer
                mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null); // <-- Remove glow
                mBinding.logoImageView.setVisibility(View.GONE);
                mBinding.nameTextView.setVisibility(View.VISIBLE);
                mBinding.nameTextView.setText(currentVodName);
            }

            @Override
            public void onError() {
                 // No Activity state checks here
                Log.w("VideoActivity", "Error fetching TMDB logo for: " + title);
                 if (mBinding == null) return; // Check binding
                // Ensure no glow layer and fallback UI
                mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null); // <-- Remove glow
                onLogoNotFound();
            }
        });
    }
    // --- End fetchTmdbLogo Method ---


    private int getMaxLines() {
        if (mBinding == null) return 1; // Basic check
        int lines = 1;
        if (isGone(mBinding.actor)) ++lines;
        if (isGone(mBinding.remark)) ++lines;
        if (isGone(mBinding.director)) ++lines;
        return lines;
    }

    private void setText(TextView view, int resId, String text) {
        if (view == null) return; // Basic check
        String displayText = (text == null) ? "" : text; // Handle null

        view.setText(getSpan(resId, displayText), TextView.BufferType.SPANNABLE);
        view.setVisibility(displayText.isEmpty() ? View.GONE : View.VISIBLE);
        view.setLinkTextColor(MDColor.WHITE);
        CustomMovement.bind(view);
        view.setTag(displayText);
    }

    private SpannableStringBuilder getSpan(int resId, String text) {
        // Simplified span logic (closer to original)
        if (text == null) text = "";
        String processedText = (resId > 0) ? getString(resId, text) : text;
        Map<String, String> map = new HashMap<>();
        Matcher m = Sniffer.CLICKER.matcher(processedText);
         StringBuffer sb = new StringBuffer(); // Use StringBuffer for replacement
        while (m.find()) {
            String group1 = m.group(1);
            String group2 = m.group(2);
            if (group1 != null && group2 != null) {
                String key = Trans.s2t(group2).trim();
                 map.put(key, group1); // Store mapping: display text -> URL
                 m.appendReplacement(sb, Matcher.quoteReplacement(key)); // Replace in buffer
            } else {
                 m.appendReplacement(sb, m.group()); // Append unchanged if no match
            }
        }
        m.appendTail(sb); // Append the rest of the text
        processedText = sb.toString();


        SpannableStringBuilder span = SpannableStringBuilder.valueOf(processedText);
        for (String s : map.keySet()) {
            int index = processedText.indexOf(s);
            if (index != -1) { // Check if the key exists in the processed text
                 Result result = Result.type(map.get(s));
                 ClickableSpan clickableSpan = getClickSpan(result);
                 if (clickableSpan != null && index + s.length() <= span.length()) { // Check bounds
                     span.setSpan(clickableSpan, index, index + s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                 }
            }
        }
        return span;
    }


    private ClickableSpan getClickSpan(Result result) {
        // Basic check
        if (result == null) return null;
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                VodActivity.start(getActivity(), getKey(), result);
            }
        };
    }


    private void setFlagActivated(Flag item) {
        // Simplified logic
        if (mFlagAdapter == null || mFlagAdapter.size() == 0 || item == null || item.isActivated() || mBinding == null) return;

        int itemIndex = mFlagAdapter.indexOf(item);
        if (itemIndex == -1) {
            // Try activating the first flag if the provided one isn't found
            item = (Flag) mFlagAdapter.get(0);
            if (item == null) return; // No flags at all
            itemIndex = 0;
        }

        for (int i = 0; i < mFlagAdapter.size(); i++) {
            Flag flag = (Flag) mFlagAdapter.get(i);
            if (flag != null) flag.setActivated(flag == item);
        }

        mBinding.flag.setSelectedPosition(itemIndex);
        notifyItemChanged(mBinding.flag, mFlagAdapter); // Use helper
        setEpisodeAdapter(item.getEpisodes());
        setQualityVisible(false);
        seamless(item);
    }


    private void setEpisodeAdapter(List<Episode> items) {
        BaseGridView episodeView = getEpisodeView();
        // Basic checks
        if (episodeView == null || mEpisodeAdapter == null) return;

        boolean isEmpty = (items == null || items.isEmpty());
        episodeView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            mEpisodeAdapter.clear();
            setArrayAdapter(0);
            return;
        }

        // Call layout calculation if it's the vertical view
        if (isVisible(mBinding.episodeVert)) {
             calculateAndSetEpisodeVerticalLayout(items);
        }
        mEpisodeAdapter.setItems(items, null);
        setArrayAdapter(items.size());
        setR2Callback(50);
    }


    private void seamless(Flag flag) {
        // Basic checks
        if (flag == null || mHistory == null || mEpisodeAdapter == null) return;
        Episode episode = flag.find(mHistory.getVodRemarks(), getMark().isEmpty());

        boolean showQuality = (episode != null && episode.isActivated() && mQualityAdapter != null && mQualityAdapter.getItemCount() > 1);
        setQualityVisible(showQuality);

        if (episode == null || episode.isActivated()) return;

        if (Setting.getFlag() == 1) {
            episode.setActivated(true); // Temp activate
            int position = getEpisodePosition();
            episode.setActivated(false); // Deactivate
            if (!isFullscreen()) {
                 BaseGridView ev = getEpisodeView();
                 if (ev != null) ev.requestFocus();
            }
            setEpisodeSelectedPosition(position);
        } else {
            mHistory.setVodRemarks(episode.getName());
            setEpisodeActivated(episode);
            hidePreview();
        }
    }


    public void setEpisodeActivated(Episode item) {
        // Basic checks
        if (item == null || mFlagAdapter == null || mEpisodeAdapter == null) return;

        int flagPosition = getFlagPosition();
        if (flagPosition < 0 || flagPosition >= mFlagAdapter.size()) return;

        if (shouldEnterFullscreen(item)) return;

        if (isFullscreen()) Notify.show(getString(R.string.play_ready, item.getName()));

        for (int i = 0; i < mFlagAdapter.size(); i++) {
            Flag flag = (Flag) mFlagAdapter.get(i);
            if (flag != null) flag.toggle(i == flagPosition, item);
        }

        int episodePosition = mEpisodeAdapter.indexOf(item);
        if (episodePosition != -1) setEpisodeSelectedPosition(episodePosition);

        notifyItemChanged(getEpisodeView(), mEpisodeAdapter); // Use helper
        onRefresh();
    }


    private void setQualityVisible(boolean visible) {
        if (mBinding != null) { // Added check
            mBinding.quality.setVisibility(visible ? View.VISIBLE : View.GONE);
            setR2Callback(100);
        }
    }

    private void setQualityActivated(Result result) {
        // Basic checks
        if (result == null || mPlayers == null || mBinding == null) return;
        Site site = getSite();
        if (site == null) return;

        try {
            mPlayers.start(result, isUseParse(), site.isChangeable() ? site.getTimeout() : -1);
            mBinding.danmaku.hide();
        } catch (Exception e) {
            Log.e("VideoActivity", "Error setting quality", e); // Log error
            ErrorEvent.post(e); // Post error event
        }
    }

    private void reverseEpisode(boolean scroll) {
        // Basic check
        if (mFlagAdapter == null) return;
        for (int i = 0; i < mFlagAdapter.size(); i++) {
            Flag flag = (Flag) mFlagAdapter.get(i);
            if (flag != null && flag.getEpisodes() != null) {
                Collections.reverse(flag.getEpisodes());
            }
        }
        Flag currentFlag = getFlag();
        if (currentFlag != null) {
            setEpisodeAdapter(currentFlag.getEpisodes());
            if (scroll) {
                 int position = getEpisodePosition(); // Find new position of active episode
                 setEpisodeSelectedPosition(position);
            }
        }
    }


    private void setParseActivated(Parse item) {
        // Basic checks
        if (item == null || mBinding == null || mParseAdapter == null) return;
        VodConfig.get().setParse(item);
        // Update activation state
        for(int i=0; i<mParseAdapter.size(); i++){
            Parse parse = (Parse) mParseAdapter.get(i);
            if(parse != null) parse.setActivated(parse.equals(item));
        }
        notifyItemChanged(mBinding.control.parse, mParseAdapter); // Use helper
        onRefresh();
    }


    private void setArrayAdapter(int size) {
        // Basic checks
        if (mBinding == null || mArrayAdapter == null) return;

        if (size <= 0) {
            mBinding.array.setVisibility(View.GONE);
            mArrayAdapter.clear();
            return;
        }

        if (size > 200) setGroupSize(100);
        else if (size > 100) setGroupSize(40);
        else setGroupSize(20);

        List<String> items = new ArrayList<>();
        items.add(getString(R.string.play_reverse));
        items.add(getString(mHistory != null ? mHistory.getRevPlayText() : R.string.play_forward));

        mBinding.array.setVisibility(size > 1 ? View.VISIBLE : View.GONE); // Show if more than 1 episode

        if (mHistory != null && mHistory.isRevSort()) {
            for (int i = size; i > 0; i -= getGroupSize()) items.add(i + "-" + Math.max(i - (getGroupSize() - 1), 1));
        } else {
            for (int i = 0; i < size; i += getGroupSize()) items.add((i + 1) + "-" + Math.min(i + getGroupSize(), size));
        }
        mArrayAdapter.setItems(items, null);
    }

    // findFocusDown/Up simplified, assume index is order index
    private int findFocusDown(int orderIndex) {
        List<Integer> viewOrder = Arrays.asList(R.id.flag, R.id.quality, R.id.episodeHori, R.id.array, R.id.episodeVert, R.id.part, R.id.quick);
        if (orderIndex < 0 || orderIndex >= viewOrder.size() -1) return 0; // Invalid index or already last

        for (int i = orderIndex + 1; i < viewOrder.size(); i++) {
            View v = findViewById(viewOrder.get(i));
            if (v != null && isVisible(v)) {
                return viewOrder.get(i);
            }
        }
        return 0;
    }

    private int findFocusUp(int orderIndex) {
        List<Integer> viewOrder = Arrays.asList(R.id.flag, R.id.quality, R.id.episodeHori, R.id.array, R.id.episodeVert, R.id.part, R.id.quick);
         if (orderIndex <= 0 || orderIndex >= viewOrder.size()) return 0; // Invalid index or already first

        for (int i = orderIndex - 1; i >= 0; i--) {
            View v = findViewById(viewOrder.get(i));
            if (v != null && isVisible(v)) {
                return viewOrder.get(i);
            }
        }
        return 0;
    }

    private void updateFocus() {
        hasKeyEvent = false;
        int episodeIndex = (Setting.getEpisode() == 0) ? 2 : 4; // Order index for episode view

        if (mEpisodePresenter != null) {
            mEpisodePresenter.setNextFocusDown(findFocusDown(episodeIndex));
            mEpisodePresenter.setNextFocusUp(findFocusUp(episodeIndex));
        }
        if (mQualityAdapter != null) mQualityAdapter.setNextFocusDown(findFocusDown(1));
        if (mArrayPresenter != null) {
            mArrayPresenter.setNextFocusDown(findFocusDown(3));
            mArrayPresenter.setNextFocusUp(findFocusUp(3));
        }
        if (mFlagPresenter != null) mFlagPresenter.setNextFocusDown(findFocusDown(0));
        if (mPartPresenter != null) mPartPresenter.setNextFocusUp(findFocusUp(5));

        // Notify relevant adapters/views
        notifyItemChanged(mBinding.flag, mFlagAdapter);
        notifyItemChanged(mBinding.quality, mQualityAdapter);
        notifyItemChanged(mBinding.array, mArrayAdapter);
        notifyItemChanged(getEpisodeView(), mEpisodeAdapter);
        notifyItemChanged(mBinding.part, mPartAdapter);
        // Quick presenter usually doesn't need focus rules set this way
    }


    private void showDisplayInfo() {
        // Basic checks
        if (mBinding == null || mPlayers == null) return;

        boolean hasBottomSheet = false;
        try {
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                if (f instanceof BottomSheetDialogFragment && f.isVisible()) {
                    hasBottomSheet = true;
                    break;
                }
            }
        } catch (Exception e) {
            // Ignore exception during check
        }

        boolean controlsVisible = isVisible(mBinding.control.getRoot());
        boolean infoWidgetVisible = isVisible(mBinding.widget.info);
        boolean isVod = mPlayers.isVod();

        mBinding.display.clock.setVisibility(Setting.isDisplayTime() || infoWidgetVisible ? View.VISIBLE : View.GONE);
        mBinding.display.titleLayout.setVisibility(Setting.isDisplayVideoTitle() && !controlsVisible ? View.VISIBLE : View.GONE);
        mBinding.display.netspeed.setVisibility(Setting.isDisplaySpeed() && !controlsVisible && !hasBottomSheet ? View.VISIBLE : View.GONE);
        mBinding.display.duration.setVisibility(Setting.isDisplayDuration() && !controlsVisible && isVod && !hasBottomSheet ? View.VISIBLE : View.GONE);
        mBinding.display.progress.setVisibility(Setting.isDisplayMiniProgress() && !controlsVisible && isVod && !hasBottomSheet ? View.VISIBLE : View.GONE);
    }

    private void onTimeChangeDisplaySpeed() {
        // Simplified check
        if (mBinding == null || mPlayers == null) return;

        boolean controlsVisible = isVisible(mBinding.control.getRoot());
        boolean displaySpeed = Setting.isDisplaySpeed() && !controlsVisible;
        boolean displayDuration = Setting.isDisplayDuration() && !controlsVisible && mPlayers.isVod();
        boolean displayProgress = Setting.isDisplayMiniProgress() && !controlsVisible && mPlayers.isVod();

        long position = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        boolean validTime = position >= 0 && duration > 0;

        if (displaySpeed) Traffic.setSpeed(mBinding.display.netspeed);

        if (displayDuration) {
            mBinding.display.duration.setText(validTime ? mPlayers.getPositionTime(0) + "/" + mPlayers.getDurationTime() : "");
        }

        if (displayProgress) {
             mBinding.display.progress.setProgress(validTime ? (int) (position * 100 / duration) : 0);
        }

        showDisplayInfo(); // Update overall visibility
    }


    @Override
    public boolean onArrayItemTouch() {
        hasKeyEvent = true;
        return false;
    }

    @Override
    public void onRevSort() {
        if (mHistory == null) return;
        mHistory.setRevSort(!mHistory.isRevSort());
        reverseEpisode(true);
        // Update reverse play button text indirectly by notifying adapter
        if (mArrayAdapter != null && mArrayAdapter.size() > 1) {
             mArrayAdapter.notifyArrayItemRangeChanged(1, 1);
        }
        mHistory.save();
    }

    @Override
    public void onRevPlay(TextView view) {
        if (mHistory == null || view == null) return;
        mHistory.setRevPlay(!mHistory.isRevPlay());
        view.setText(mHistory.getRevPlayText());
        Notify.show(mHistory.getRevPlayHint());
        mHistory.save();
    }

    private boolean shouldEnterFullscreen(Episode item) {
        // Basic check
        if (item == null) return false;
        boolean enter = !isFullscreen() && item.isActivated();
        if (enter) enterFullscreen();
        return enter;
    }

    private void enterFullscreen() {
        // Basic checks
        if (mBinding == null || mKeyDown == null || mDanmakuContext == null) return;

        mFocus1 = getCurrentFocus();
        mBinding.video.requestFocus();
        mBinding.video.setForeground(null);
        mBinding.video.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mBinding.video.setBackgroundColor(android.graphics.Color.BLACK);
        mBinding.video.setClipToOutline(false);

        if (mFlagAdapter != null && mFlagAdapter.size() > 0) { // Basic check
            mBinding.flag.setSelectedPosition(getFlagPosition());
        }

        if (Setting.getDanmuSize() != 0) mDanmakuContext.setScaleTextSize(1.2f * Setting.getDanmuSize());

        mKeyDown.setFull(true);
        setFullscreen(true);
        mFocus2 = null;

        hideDetailViews(); // Hide non-video UI

        onPlay();
    }

    private void exitFullscreen() {
        // Basic checks
        if (mBinding == null || mKeyDown == null || mDanmakuContext == null || mFrameParams == null) return;

        mBinding.video.setForeground(ResUtil.getDrawable(R.drawable.selector_video));
        mBinding.video.setLayoutParams(mFrameParams);
        mBinding.video.setBackgroundResource(R.drawable.rounded_corners);
        mBinding.video.setClipToOutline(true);

        if (Setting.getDanmuSize() != 0) mDanmakuContext.setScaleTextSize(0.8f * Setting.getDanmuSize());

        View targetFocus = getFocus1(); // getFocus1 has fallback
        if (targetFocus != null) targetFocus.requestFocus(); // Basic check

        mKeyDown.setFull(false);
        setFullscreen(false);
        mFocus2 = null;

        showDetailViews(); // Show non-video UI

        hideInfo();
    }

    // --- Helper method to hide non-video views (with Glow logic) ---
    private void hideDetailViews() {
        if (mBinding == null) return;

        // Hide Title Area & Remove Glow
        if (mBinding.logoImageView.getVisibility() == View.VISIBLE) {
            mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null); // <-- Remove glow
        }
        mBinding.logoImageView.setVisibility(View.GONE);
        mBinding.nameTextView.setVisibility(View.GONE);

        // Hide others
        mBinding.remark.setVisibility(View.GONE);
        mBinding.row1.setVisibility(View.GONE);
        mBinding.director.setVisibility(View.GONE);
        mBinding.actor.setVisibility(View.GONE);
        mBinding.content.setVisibility(View.GONE);
        mBinding.row2.setVisibility(View.GONE);
        mBinding.flag.setVisibility(View.GONE);
        mBinding.scroll.setVisibility(View.GONE);
    }
    // --- End Helper Method ---

    // --- Helper method to show non-video views (with Glow logic) ---
    private void showDetailViews() {
        if (mBinding == null) return;

        // Show Title Area & Apply Glow if needed
        if (currentLogoUrl != null) {
            mBinding.logoImageView.setVisibility(View.VISIBLE);
            // Re-apply glow effect
            initGlowPaint();
            if (logoGlowPaint != null) {
                 mBinding.logoImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, logoGlowPaint); // <-- Apply glow
            } else {
                 mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null);
            }
            mBinding.nameTextView.setVisibility(View.GONE);
        } else {
            mBinding.logoImageView.setVisibility(View.GONE);
            mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null); // <-- Remove glow
            mBinding.nameTextView.setVisibility(View.VISIBLE);
        }
         // Use setText which handles visibility based on tag content
         setText(mBinding.nameTextView, 0, currentVodName);

        // Show others using setText (restores from tag and sets visibility)
        setText(mBinding.remark, 0, Objects.toString(mBinding.remark.getTag(), ""));
        mBinding.row1.setVisibility(View.VISIBLE);
         setText(mBinding.site, 0, Objects.toString(mBinding.site.getTag(), ""));
         setText(mBinding.year, 0, Objects.toString(mBinding.year.getTag(), ""));
         setText(mBinding.area, 0, Objects.toString(mBinding.area.getTag(), ""));
         setText(mBinding.type, 0, Objects.toString(mBinding.type.getTag(), ""));
        setText(mBinding.director, 0, Objects.toString(mBinding.director.getTag(), ""));
        setText(mBinding.actor, 0, Objects.toString(mBinding.actor.getTag(), ""));
        setText(mBinding.content, 0, Objects.toString(mBinding.content.getTag(), ""));
        mBinding.row2.setVisibility(View.VISIBLE);

        mBinding.scroll.setVisibility(View.VISIBLE);
        // Restore list visibility based on adapter data
        mBinding.flag.setVisibility(mFlagAdapter != null && mFlagAdapter.size() > 0 ? View.VISIBLE : View.GONE);
         // Keep simplified visibility checks for lists
         mBinding.quality.setVisibility(mQualityAdapter != null && mQualityAdapter.getItemCount() > 0 && isVisible(mBinding.quality) ? View.VISIBLE : View.GONE);
         BaseGridView ev = getEpisodeView();
         if(ev != null) ev.setVisibility(mEpisodeAdapter != null && mEpisodeAdapter.size() > 0 ? View.VISIBLE : View.GONE);
         mBinding.array.setVisibility(mArrayAdapter != null && mArrayAdapter.size() > 2 ? View.VISIBLE : View.GONE);
         mBinding.part.setVisibility(mPartAdapter != null && mPartAdapter.size() > 0 ? View.VISIBLE : View.GONE);
         mBinding.quick.setVisibility(mQuickAdapter != null && mQuickAdapter.size() > 0 ? View.VISIBLE : View.GONE);


        updateFocus(); // Update focus rules after showing views
    }
    // --- End Helper Method ---


    private void onDesc() {
        if (mBinding == null) return; // Basic check
        CharSequence desc = mBinding.content.getText();
        if (desc != null && desc.length() > 0) { // Basic check
             String descStr = desc.toString();
             String prefix = getString(R.string.detail_content, "");
             if (descStr.startsWith(prefix)) descStr = descStr.substring(prefix.length());
             if (!TextUtils.isEmpty(descStr)) DescDialog.show(this, descStr);
        }
    }

    private void onKeep() {
        Keep keep = Keep.find(getHistoryKey());
        Notify.show(keep != null ? R.string.keep_del : R.string.keep_add);
        if (keep != null) keep.delete();
        else createKeep();
        RefreshEvent.keep();
        checkKeep();
    }

    private void onVideo() {
        if (!isFullscreen()) enterFullscreen();
        else onToggle(); // Toggle controls if already fullscreen
    }

    private void onChange() {
        checkSearch(true);
    }

    private void onLoop() {
        if (mBinding == null) return; // Basic check
        mBinding.control.loop.setActivated(!mBinding.control.loop.isActivated());
    }

    private void onDanmu() {
        if (mBinding == null) return; // Basic check
        Setting.putDanmu(!Setting.isDanmu());
        mBinding.control.danmu.setActivated(Setting.isDanmu());
        showDanmu();
    }

    private void showDanmu() {
        // Basic checks
        if (mBinding == null || mBinding.danmaku == null) return;
        if (Setting.isDanmu() && mBinding.danmaku.isPrepared()) {
            mBinding.danmaku.show();
        } else {
            mBinding.danmaku.hide();
        }
    }

    private void onDanmuAdd() {
        if (mBinding == null) return; // Basic check
        int line = Setting.getDanmuLine(3);
        line = Math.min(line + 1, 15);
        Setting.putDanmuLine(line);
        mBinding.control.danmu.setText(getString(R.string.danmu_lines, line)); // Use formatted string
        setDanmuViewSettings();
    }

    private void onDanmuSub() {
        if (mBinding == null) return; // Basic check
        int line = Setting.getDanmuLine(3);
        line = Math.max(line - 1, 1);
        Setting.putDanmuLine(line);
        mBinding.control.danmu.setText(getString(R.string.danmu_lines, line)); // Use formatted string
        setDanmuViewSettings();
    }

    private void onEpisodes() {
        Flag flag = getFlag();
        // Basic checks
        if (flag == null || flag.getEpisodes() == null || flag.getEpisodes().isEmpty()) return;
        EpisodeDialog.create().episodes(flag.getEpisodes()).show(this);
        hideControl();
    }

    private void checkNext() {
        if (mHistory != null && mHistory.isRevPlay()) onPrev();
        else onNext();
    }

    private void checkPrev() {
        if (mHistory != null && mHistory.isRevPlay()) onNext();
        else onPrev();
    }

    private void onNext() {
        // Basic checks
        if (mEpisodeAdapter == null || mEpisodeAdapter.size() == 0) return;
        int current = getEpisodePosition();
        int max = mEpisodeAdapter.size() - 1;
        if (current >= max) {
            Notify.show(mHistory != null && mHistory.isRevPlay() ? R.string.error_play_prev_eof : R.string.error_play_next_eof);
            return;
        }
        Episode item = (Episode) mEpisodeAdapter.get(current + 1);
        if (item != null) setEpisodeActivated(item); // Basic check
    }

    private void onPrev() {
        // Basic checks
        if (mEpisodeAdapter == null || mEpisodeAdapter.size() == 0) return;
        int current = getEpisodePosition();
        if (current <= 0) {
            Notify.show(mHistory != null && mHistory.isRevPlay() ? R.string.error_play_next_eof : R.string.error_play_prev_eof);
            return;
        }
        Episode item = (Episode) mEpisodeAdapter.get(current - 1);
        if (item != null) setEpisodeActivated(item); // Basic check
    }

    private void onScale() {
        int index = getScale();
        String[] array = ResUtil.getStringArray(R.array.select_scale);
        int newIndex = (index + 1) % array.length;
        if (mHistory != null) mHistory.setScale(newIndex);
        setScale(newIndex);
        if (mHistory != null) mHistory.save(); // Save history
    }

    private void onSpeed() {
        // Basic checks
        if (mPlayers == null || mBinding == null) return;
        mBinding.control.speed.setText(mPlayers.addSpeed());
        if (mHistory != null) {
            mHistory.setSpeed(mPlayers.getSpeed());
            mHistory.save(); // Save history
        }
    }

    private void onSpeedAdd() {
        // Basic checks
        if (mPlayers == null || mBinding == null) return;
        mBinding.control.speed.setText(mPlayers.addSpeed(0.25f));
        if (mHistory != null) {
            mHistory.setSpeed(mPlayers.getSpeed());
            mHistory.save(); // Save history
        }
    }

    private void onSpeedSub() {
        // Basic checks
        if (mPlayers == null || mBinding == null) return;
        mBinding.control.speed.setText(mPlayers.subSpeed(0.25f));
        if (mHistory != null) {
            mHistory.setSpeed(mPlayers.getSpeed());
            mHistory.save(); // Save history
        }
    }

    private boolean onSpeedLong() {
        // Basic checks
        if (mPlayers == null || mBinding == null) return false;
        mBinding.control.speed.setText(mPlayers.toggleSpeed());
        if (mHistory != null) {
            mHistory.setSpeed(mPlayers.getSpeed());
            mHistory.save(); // Save history
        }
        return true;
    }

    private void onRefresh() {
        onReset(false);
    }

    private void onReset() {
        onReset(isReplay());
    }

    private void onReset(boolean replay) {
        if (mClock != null) mClock.setCallback(null); // Added check
        // Basic checks
        if (mFlagAdapter == null || mFlagAdapter.size() == 0 || mEpisodeAdapter == null || mEpisodeAdapter.size() == 0) return;
        Episode episode = getEpisode();
        Flag flag = getFlag();
        if (flag != null && episode != null) { // Added checks
            getPlayer(flag, episode, replay);
        }
    }

    private boolean onResetToggle() {
        if (mBinding == null) return false; // Basic check
        int newReset = (Setting.getReset() + 1) % 2;
        Setting.putReset(newReset);
        mBinding.control.reset.setText(ResUtil.getStringArray(R.array.select_reset)[newReset]);
        return true;
    }

    private void onOpening() {
        if (mPlayers == null) return; // Added check
        long current = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        if (current < 0 || duration <= 0 || current > duration / 2) {
             Notify.show(R.string.error_set_op_range);
             return;
        }
        setOpening(current);
    }

    private void onOpeningAdd() {
        // Basic checks
        if (mHistory == null || mPlayers == null) return;
        long duration = mPlayers.getDuration();
        if (duration <= 0) return;
        setOpening(Math.min(mHistory.getOpening() + 1000, duration / 2));
    }

    private void onOpeningSub() {
        if (mHistory == null) return; // Added check
        setOpening(Math.max(0, mHistory.getOpening() - 1000));
    }

    private boolean onOpeningReset() {
        setOpening(0);
        return true;
    }

    private void setOpening(long opening) {
        // Basic checks
        if (mHistory == null || mBinding == null || mPlayers == null) return;
        mHistory.setOpening(opening);
        mBinding.control.opening.setText(opening == 0 ? getString(R.string.play_op) : mPlayers.stringToTime(opening));
        mHistory.save(); // Save history
    }

    private void onEnding() {
        if (mPlayers == null) return; // Added check
        long current = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        if (current < 0 || duration <= 0 || current < duration / 2) {
             Notify.show(R.string.error_set_ed_range);
             return;
        }
        setEnding(duration - current);
    }

    private void onEndingAdd() {
        // Basic checks
        if (mHistory == null || mPlayers == null) return;
        long duration = mPlayers.getDuration();
        if (duration <= 0) return;
        setEnding(Math.min(mHistory.getEnding() + 1000, duration / 2));
    }

    private void onEndingSub() {
        if (mHistory == null) return; // Added check
        setEnding(Math.max(0, mHistory.getEnding() - 1000));
    }

    private boolean onEndingReset() {
        setEnding(0);
        return true;
    }

    private void setEnding(long ending) {
        // Basic checks
        if (mHistory == null || mBinding == null || mPlayers == null) return;
        mHistory.setEnding(ending);
        mBinding.control.ending.setText(ending == 0 ? getString(R.string.play_ed) : mPlayers.stringToTime(ending));
        mHistory.save(); // Save history
    }

    private boolean onChoose() {
        // Basic checks
        if (mPlayers == null || mPlayers.isEmpty() || mBinding == null) return false;
        CharSequence title = mBinding.widget.title.getText();
        mPlayers.choose(this, title != null ? title : ""); // Added check
        return true;
    }

    private void onPlayer() {
        // Basic checks
        if (mPlayers == null || mBinding == null) return;
        CharSequence title = mBinding.widget.title.getText();
        PlayerDialog.create()
            .select(mPlayers.getPlayer())
            .title(title != null ? title.toString() : "") // Added check
            .listener(this)
            .show(this);
        hideControl();
    }

    private void onDecode() {
        onDecode(true);
    }

    private void onDecode(boolean save) {
        if (mPlayers == null) return; // Added check
        mPlayers.toggleDecode(save);
        mPlayers.init(getExo(), getIjk());
        mPlayers.setMediaSource();
        setDecodeView();
    }

    private void onTrack(View view) {
        // Basic checks
        if (view == null || view.getTag() == null || mPlayers == null) return;
        try {
            int type = Integer.parseInt(view.getTag().toString());
            TrackDialog.create()
                .player(mPlayers)
                .chooser(this)
                .listener(this)
                .vod(true)
                .type(type)
                .show(this);
            hideControl();
        } catch (NumberFormatException e) {
            Log.e("VideoActivity", "Invalid tag for track type", e);
        }
    }

    private void onToggle() {
        if (mBinding == null) return; // Basic check
        if (isVisible(mBinding.control.getRoot())) hideControl();
        else showControl(getFocus2());
    }

    private void showProgress() {
        if (mBinding == null) return; // Basic check
        mBinding.widget.progress.setVisibility(View.VISIBLE);
        App.post(mR3, 0);
        hideError();
    }

    private void hideProgress() {
        if (mBinding == null) return; // Basic check
        mBinding.widget.progress.setVisibility(View.GONE);
        App.removeCallbacks(mR3);
        Traffic.reset();
    }

    private void showError(String text) {
        if (mBinding == null) return; // Basic check
        mBinding.widget.error.setVisibility(View.VISIBLE);
        mBinding.widget.text.setText(text);
        hideProgress();
    }

    private void hideError() {
        if (mBinding == null) return; // Basic check
        mBinding.widget.error.setVisibility(View.GONE);
        mBinding.widget.text.setText("");
    }

    private void showInfo() {
        if (mBinding == null) return; // Basic check
        mBinding.widget.info.setVisibility(View.VISIBLE);
        showDisplayInfo();
    }

    private void hideInfo() {
        if (mBinding == null) return; // Basic check
        mBinding.widget.info.setVisibility(View.GONE);
        showDisplayInfo();
    }

    private void showInfoAndCenter() {
        if (mBinding == null) return; // Basic check
        showInfo();
        mBinding.widget.center.setVisibility(View.VISIBLE);
    }

    private void hideInfoAndCenter() {
        if (mBinding == null) return; // Basic check
        hideInfo();
        mBinding.widget.center.setVisibility(View.GONE);
    }

    private void setControlNextFocus() {
        // Simplified logic (might need adjustment based on original)
        if (mBinding == null || mBinding.control.actionLayout == null) return;
        int count = mBinding.control.actionLayout.getChildCount();
        View prevVisible = null;
        for(int i=0; i<count; i++) {
            View current = mBinding.control.actionLayout.getChildAt(i);
            if (current != null && isVisible(current) && current.isEnabled()) { // Basic check
                if (prevVisible != null) {
                    prevVisible.setNextFocusRightId(current.getId());
                    current.setNextFocusLeftId(prevVisible.getId());
                }
                prevVisible = current;
            }
        }
    }


    private void showControl(View view) {
        // Basic checks
        if (mBinding == null || mBinding.danmaku == null) return;

        if (view == null) view = getFocus2(); // Use helper with fallbacks
        // Final fallback if getFocus2 returns null or invalid view
        if (view == null || !isVisible(view) || !view.isEnabled()) view = mBinding.control.play;


        mBinding.control.danmu.setVisibility(mBinding.danmaku.isPrepared() ? View.VISIBLE : View.GONE);
        mBinding.control.getRoot().setVisibility(View.VISIBLE);
        mBinding.control.episodes.setVisibility(Setting.getFullscreenMenuKey() == 0 ? View.VISIBLE : View.GONE);

        setControlNextFocus(); // Setup focus chain

        if (view != null && isVisible(view) && view.isEnabled()) { // Check again before requesting focus
            view.requestFocus();
        }

        setR1Callback();
    }


    private void hideControl() {
        hideControl(true);
    }

    private void hideControl(boolean hideInfo) {
        if (mBinding == null) return; // Basic check
        if (hideInfo) hideInfo();
        mBinding.control.text.setText(R.string.play_track_text);
        mBinding.control.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(mR1);
    }

    private void hideCenter() {
        if (mBinding == null) return; // Basic check
        mBinding.widget.action.setImageResource(R.drawable.ic_widget_play);
        mBinding.widget.center.setVisibility(View.GONE);
    }

    private void showPreview(Drawable preview) {
        // Basic check
        if (preview != null && Setting.getFlag() != 0 && mBinding != null && isGone(mBinding.widget.preview)) {
            mBinding.widget.preview.setVisibility(View.VISIBLE);
            mBinding.widget.preview.setImageDrawable(preview);
        }
    }

    private void hidePreview() {
        if (mBinding != null && mBinding.widget.preview != null) { // Added check
            mBinding.widget.preview.setVisibility(View.GONE);
            mBinding.widget.preview.setImageDrawable(null);
        }
    }

    private void setTraffic() {
        if (mBinding != null && Setting.isDisplaySpeed()) { // Added check
            Traffic.setSpeed(mBinding.widget.traffic);
             Traffic.setSpeed(mBinding.display.netspeed); // Update both potentially
        }
         // Schedule next update only if needed
         if (Setting.isDisplaySpeed() && !isBackground()) {
             App.post(mR3, Constant.INTERVAL_TRAFFIC);
         }
    }

    private void setR1Callback() {
        App.removeCallbacks(mR1); // Remove previous
        App.post(mR1, Constant.INTERVAL_HIDE);
    }

    private void setR2Callback(long delayMillis) {
        App.removeCallbacks(mR2); // Remove previous
        App.post(mR2, delayMillis);
    }

    private void setArtwork(String url) {
        // Simplified logic, basic check
        if (TextUtils.isEmpty(url)) {
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
                // Basic checks
                if (mBinding != null && mPlayers != null) {
                    getExo().setDefaultArtwork(resource);
                    getIjk().setDefaultArtwork(resource);
                }
                showPreview(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                hidePreview();
                 Log.w("VideoActivity", "Failed to load artwork: " + url);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                // Optional: Reset to placeholder
            }
        });
    }


    private void getPart(String source) {
        // Simplified logic, basic check
        if (TextUtils.isEmpty(source)) {
             setPartAdapter(Collections.emptyList());
             return;
        }
        try {
            String encodedSource = URLEncoder.encode(source.trim(), "UTF-8");
            String url = "https://api.yesapi.cn/?service=App.Scws.GetWords&app_key=CEE4B8A091578B252AC4C92FB4E893C3&text=" + encodedSource + "&ignore_mark=1";

            OkHttp.newCall(url).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    List<String> items = new ArrayList<>();
                    String body = null;
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            body = response.body().string();
                            items = Part.get(body);
                        } else {
                             items.add(source.trim()); // Fallback
                        }
                    } catch (Exception e) {
                        Log.e("VideoActivity", "Error processing part response", e);
                        items.add(source.trim()); // Fallback
                    } finally {
                         if (response != null) response.close(); // Ensure close
                    }

                    items.remove(source.trim());
                    items.add(0, source.trim());
                    final List<String> finalItems = items;
                    App.post(() -> setPartAdapter(finalItems));
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    List<String> items = Collections.singletonList(source.trim());
                    App.post(() -> setPartAdapter(items));
                }
            });
        } catch (Exception e) {
            Log.e("VideoActivity", "Error in getPart", e);
            List<String> items = Collections.singletonList(source.trim());
            App.post(() -> setPartAdapter(items));
        }
    }


    private void setPartAdapter(List<String> items) {
        // Basic checks
        if (mBinding == null || mPartAdapter == null) return;
        boolean isEmpty = (items == null || items.isEmpty());
        mBinding.part.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (!isEmpty) {
            mPartAdapter.setItems(items, null);
            setR2Callback(100);
        } else {
             mPartAdapter.clear();
        }
    }

    private void checkFlag(Vod item) {
        // Basic checks
        if (item == null || mBinding == null) return;
        boolean hasFlags = item.getVodFlags() != null && !item.getVodFlags().isEmpty();

        mBinding.flag.setVisibility(hasFlags ? View.VISIBLE : View.GONE);

        if (!hasFlags) {
            ErrorEvent.flag();
            setEpisodeAdapter(Collections.emptyList()); // Clear episodes if no flags
        } else {
            Flag flagToActivate = null;
            if (mHistory != null) {
                 flagToActivate = mHistory.getFlag();
                 // Basic validation: Check if history flag is in current item's flags
                 if (flagToActivate == null || !item.getVodFlags().contains(flagToActivate)) {
                      flagToActivate = item.getVodFlags().get(0); // Default to first if invalid
                 }
                 if (mHistory.isRevSort()) reverseEpisode(true);
            } else {
                 flagToActivate = item.getVodFlags().get(0); // Default to first if no history
            }
            // Activate the determined flag (setFlagActivated handles null)
            setFlagActivated(flagToActivate);
        }
    }


    private void checkHistory(Vod item) {
        // Basic checks
        if (item == null || mPlayers == null || mBinding == null) return;

        mHistory = History.find(getHistoryKey());
        boolean createdNewHistory = false;
        if (mHistory == null) {
            mHistory = createHistory(item);
            createdNewHistory = (mHistory != null);
        }

        // Update name and pic from current item data
        if (mHistory != null) {
             mHistory.setVodName(item.getVodName());
             mHistory.setVodPic(item.getVodPic(getPic()));
        }


        if (mHistory != null) {
            String mark = getMark();
            if (!TextUtils.isEmpty(mark)) {
                mHistory.setVodRemarks(mark);
            } else if (createdNewHistory) {
                 // Remark should be set by findEpisode in createHistory
            }

            // Update UI based on history
            mBinding.control.opening.setText(mHistory.getOpening() == 0 ? getString(R.string.play_op) : mPlayers.stringToTime(mHistory.getOpening()));
            mBinding.control.ending.setText(mHistory.getEnding() == 0 ? getString(R.string.play_ed) : mPlayers.stringToTime(mHistory.getEnding()));

            mPlayers.setPlayer(getPlayer());
            setScale(getScale());
            setPlayerView();
            setDecodeView();
        } else {
            // Fallback if history creation failed
             mPlayers.setPlayer(Setting.getPlayer());
             setScale(Setting.getScale());
             setPlayerView();
             setDecodeView();
             mBinding.control.opening.setText(getString(R.string.play_op));
             mBinding.control.ending.setText(getString(R.string.play_ed));
        }
    }

    private History createHistory(Vod item) {
        // Basic check
        if (item == null) return null;
        try {
            History history = new History();
            history.setKey(getHistoryKey());
            history.setCid(VodConfig.getCid());
            history.setVodName(item.getVodName());
            history.setVodPic(item.getVodPic(getPic()));
            history.findEpisode(item.getVodFlags()); // Sets remark and flag
            // Set defaults based on settings/player state
            history.setSpeed(Setting.getPlaySpeed());
             history.setScale(Setting.getScale());
             history.setPlayer(getPlayer()); // Use getPlayer() which respects hierarchy
            history.setCreateTime(System.currentTimeMillis());
            return history;
        } catch (Exception e) {
             Log.e("VideoActivity", "Error creating history", e);
             return null;
        }
    }

    private void updateHistory(Episode item, boolean replay) {
        // Basic checks
        if (mHistory == null || item == null || mPlayers == null) return;
        Flag currentFlag = getFlag();
        if (currentFlag == null) return;

        boolean forceReplay = replay || !item.equals(mHistory.getEpisode());
        long position = forceReplay ? 0 : mHistory.getPosition();

        mHistory.setPosition(position);
        mHistory.setEpisodeUrl(item.getUrl());
        mHistory.setVodRemarks(item.getName());
        mHistory.setVodFlag(currentFlag.getFlag());
        mHistory.setCreateTime(System.currentTimeMillis());

        mPlayers.setPosition(Math.max(mHistory.getOpening(), position));

        if (!Setting.isIncognito()) {
            App.execute(mHistory::save); // Save in background
        }
    }


    private void checkKeep() {
        if (mBinding == null) return; // Basic check
        Keep keep = Keep.find(getHistoryKey());
        mBinding.keep.setCompoundDrawablesWithIntrinsicBounds(keep == null ? R.drawable.ic_detail_keep_off : R.drawable.ic_detail_keep_on, 0, 0, 0);
         mBinding.keep.setText(keep == null ? R.string.detail_keep_off : R.string.detail_keep_on); // Set text as well
    }

    private void createKeep() {
        // Simplified logic, basic checks
        Site site = getSite();
        if (site == null) {
             Notify.show(R.string.error_keep_add);
             return;
        }
        try {
            Keep keep = new Keep();
            keep.setKey(getHistoryKey());
            keep.setCid(VodConfig.getCid());
            keep.setSiteName(site.getName());
            Object tag = (mBinding != null) ? mBinding.video.getTag() : null; // Added check
            keep.setVodPic(tag instanceof String ? (String) tag : getPic());
            keep.setVodName(currentVodName); // Use stored name
            keep.setCreateTime(System.currentTimeMillis());
            keep.save();
        } catch (Exception e) {
             Log.e("VideoActivity", "Error creating keep", e);
             Notify.show(R.string.error_keep_add);
        }
    }


    @Override
    public void showChooser(TrackDialog dialog) {
        // Basic checks
        if (dialog == null || mPlayers == null) return;
        FileChooserDialog.create().player(mPlayers).trackDialog(dialog).show(this);
    }

    @Override
    public void onTrackClick(Track item) {
        // Basic check
        if (item == null) return;
        item.setKey(getHistoryKey());
        item.save();
    }

    @Override
    public void onSubtitleClick() {
        // Basic checks
        if (mPlayers == null || mBinding == null) return;
        App.post(this::hideControl, 100); // Hide controls slightly delayed

        SubtitleView subtitleView = mPlayers.isIjk() ? getIjk().getSubtitleView() : getExo().getSubtitleView();
        if (subtitleView != null) { // Added check
            App.post(() -> SubtitleDialog.create().view(subtitleView).full(isFullscreen()).show(this), 200);
        }
    }

    @Override
    public void onTimeChanged() {
        onTimeChangeDisplaySpeed();

        // Basic checks
        if (mHistory == null || mPlayers == null) return;

        long position = mPlayers.getPosition();
        long duration = mPlayers.getDuration();

        if (position >= 0 && duration > 0) {
            mHistory.setPosition(position);
            mHistory.setDuration(duration);
            if (!Setting.isIncognito()) {
                // Save history less frequently? Original likely saved often.
                App.execute(() -> {
                     if (mHistory != null) mHistory.update(); // Use update
                });
            }
        }

        // Check ED skip
        if (mHistory.getEnding() > 0 && duration > 0 && position > 0 && (mHistory.getEnding() + position >= duration)) {
            if (mClock != null) mClock.setCallback(null); // Added check
            checkNext();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActionEvent(ActionEvent event) {
        // Basic checks
        if (event == null || event.getAction() == null || isBackground() || mBinding == null) return;
        switch (event.getAction()) {
            case ActionEvent.PLAY:
            case ActionEvent.PAUSE:
                onKeyCenter();
                break;
            case ActionEvent.NEXT:
                if (mBinding.control.next != null) mBinding.control.next.performClick(); // Added check
                break;
            case ActionEvent.PREV:
                 if (mBinding.control.prev != null) mBinding.control.prev.performClick(); // Added check
                break;
            case ActionEvent.STOP:
                finish();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        // Basic checks
        if (event == null || event.getType() == null || isBackground() || mPlayers == null) return;
        switch (event.getType()) {
            case DETAIL: getDetail(); break;
            case PLAYER: onRefresh(); break;
            case DANMAKU: checkDanmu(event.getPath()); break;
            case SUBTITLE: mPlayers.setSub(Sub.from(event.getPath())); break;
             case HISTORY: checkKeep(); break; // Maybe just update keep on history refresh?
             case KEEP: checkKeep(); break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerEvent(PlayerEvent event) {
        // Basic checks
        if (event == null || isBackground() || mPlayers == null || mBinding == null) return;
        switch (event.getState()) {
            case 0: // Loading/Preparing
                setInitTrack(true);
                setTrackVisible(false);
                if (mClock != null) mClock.setCallback(this); // Added check
                break;
            case Player.STATE_IDLE: break; // Do nothing specific?
            case Player.STATE_BUFFERING: showProgress(); break;
            case Player.STATE_READY:
                stopSearch();
                setMetadata();
                resetToggle();
                resetError();
                hideProgress();
                mPlayers.reset(); // Reset internal player state?
                setDefaultTrack();
                setTrackVisible(true);
                // Save player choice
                if (mHistory != null && mHistory.getPlayer() != mPlayers.getPlayer()) { // Added check
                    mHistory.setPlayer(mPlayers.getPlayer());
                     if (!Setting.isIncognito()) mHistory.save();
                }
                String sizeText = mPlayers.getSizeText();
                mBinding.widget.size.setText(sizeText);
                mBinding.display.size.setText(sizeText);
                break;
            case Player.STATE_ENDED: checkEnded(); break;
        }
    }


    private void checkEnded() {
        // Basic check
        if (mBinding == null) return;
        if (mBinding.control.loop.isActivated()) {
            onReset(true);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            checkNext();
        }
    }

    private void setTrackVisible(boolean visible) {
        // Basic checks
        if (mBinding == null || mPlayers == null) return;
        mBinding.control.text.setVisibility(visible && mPlayers.haveTrack(C.TRACK_TYPE_TEXT) ? View.VISIBLE : View.GONE);
        mBinding.control.audio.setVisibility(visible && mPlayers.haveTrack(C.TRACK_TYPE_AUDIO) ? View.VISIBLE : View.GONE);
        mBinding.control.video.setVisibility(visible && mPlayers.haveTrack(C.TRACK_TYPE_VIDEO) ? View.VISIBLE : View.GONE);
        setControlNextFocus(); // Update focus chain
    }

    private void setDefaultTrack() {
        // Basic check
        if (isInitTrack() && mPlayers != null) {
            setInitTrack(false);
            mPlayers.prepared();
            Track savedTracks = Track.find(getHistoryKey());
            if (savedTracks != null) mPlayers.setTrack(savedTracks); // Added check
        }
    }

    private void setMetadata() {
        // Basic checks
        if (mPlayers == null) return;
        String title = (mHistory != null && !TextUtils.isEmpty(mHistory.getVodName())) ? mHistory.getVodName() : currentVodName;
        Episode episode = getEpisode();
        String episodeName = (episode != null && !TextUtils.isEmpty(episode.getName())) ? episode.getName() : "";
        String artist = "";
        if (!TextUtils.isEmpty(episodeName) && !Objects.equals(title, episodeName)) {
            artist = getString(R.string.play_now, episodeName);
        }
        String pic = (mHistory != null && !TextUtils.isEmpty(mHistory.getVodPic())) ? mHistory.getVodPic() : getPic();
        mPlayers.setMetadata(title, artist, pic, getDefaultArtwork());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(ErrorEvent event) {
        // Basic checks
        if (event == null || isBackground() || mPlayers == null) return;

        if (addErrorCount() > 20) {
            onErrorEnd(event);
        } else if (mPlayers.addRetry() > event.getRetry()) {
            checkError(event);
        } else if (event.isDecode() && mPlayers.canToggleDecode()) {
            onDecode(false);
        } else if (event.isExo() && mPlayers.isExo()) {
            onExoCheck(event);
        } else {
            onRefresh(); // Default retry current source
        }
    }


    private void onExoCheck(ErrorEvent event) {
        // Basic checks
        if (event == null || mPlayers == null) return;
        int code = event.getCode();
        if (code == PlaybackException.ERROR_CODE_IO_UNSPECIFIED || (code >= PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED && code <= PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED)) {
            // Simple handling: just try resetting the media source
            mPlayers.setMediaSource();
        } else {
             onRefresh(); // Default retry for other Exo errors
        }
    }

    private void checkError(ErrorEvent event) {
        // Basic checks
        if (event == null || mPlayers == null) return;
        Site site = getSite();
        if (site != null && site.getPlayerType() == -1 && event.isUrl() && event.getRetry() > 0 && getToggleCount() < 2 && mPlayers.getPlayer() != Players.SYS) {
            toggleCount++;
            nextPlayer();
        } else {
            resetToggle();
            onError(event);
        }
    }


    private void nextPlayer() {
        if (mPlayers == null) return; // Added check
        mPlayers.nextPlayer();
        setPlayerView();
        setDecodeView();
        onRefresh();
    }

    private void onErrorEnd(ErrorEvent event) {
        if (event == null) return; // Added check
        onErrorPlayer(event);
        resetError();
    }

    private void onErrorPlayer(ErrorEvent event) {
        // Basic checks
        if (event == null || mPlayers == null || mClock == null) return;
        Track.delete(getHistoryKey());
        showError(event.getMsg());
        mClock.setCallback(null);
        mPlayers.reset();
        mPlayers.stop();
         getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void onError(ErrorEvent event) {
        if (event == null) return; // Added check
        onErrorPlayer(event);
        startFlow();
    }

    private void startFlow() {
        Site site = getSite();
        // Basic checks
        if (site == null || !site.isChangeable()) {
             showEmpty(); // Show empty if no alternatives possible
             return;
        }
        if (isUseParse()) checkParse();
        else checkFlagFlow(); // Use renamed method
    }

    private void checkParse() {
        // Basic checks
        if (mParseAdapter == null || mParseAdapter.size() == 0) {
             checkFlagFlow();
             return;
        }
        int position = getParsePosition();
        boolean last = position >= mParseAdapter.size() - 1;
        boolean pass = position == 0 || last; // Original logic? Seems odd.
        if (last) initParse();
        if (pass) checkFlagFlow();
        else nextParse(position);
    }

    private void initParse() {
        // Basic check
        if (mParseAdapter == null || mParseAdapter.size() == 0) return;
        Parse firstParse = (Parse) mParseAdapter.get(0);
        if (firstParse != null) setParseActivated(firstParse); // Added check
    }

    // Renamed from checkFlag to avoid confusion
    private void checkFlagFlow() {
        // Basic checks
        if (mBinding == null || mFlagAdapter == null || mFlagAdapter.size() == 0 || isGone(mBinding.flag)) {
             checkSearch(false);
             return;
        }
        int position = getFlagPosition();
        if (position >= mFlagAdapter.size() - 1) checkSearch(false);
        else nextFlag(position);
    }


    private void checkSearch(boolean force) {
        // Simplified logic
        if (mQuickAdapter != null && mQuickAdapter.size() > 0 && (isAutoMode() || force)) { // Added null check
            nextSite();
        } else if (mQuickAdapter == null || mQuickAdapter.size() == 0) { // Added null check
            String keyword = currentVodName;
            if (TextUtils.isEmpty(keyword)) keyword = getName(); // Fallback
            if (!TextUtils.isEmpty(keyword)) {
                 initSearch(keyword, true);
            } else {
                 showEmpty(); // No keyword to search
            }
        } else {
             // Results available, but not auto-searching
             if (mBinding != null && isVisible(mBinding.quick)) mBinding.quick.requestFocus();
             else showEmpty();
        }
    }


    private void initSearch(String keyword, boolean auto) {
        // Basic checks
        if (TextUtils.isEmpty(keyword) || mBinding == null) return;
        stopSearch();
        setAutoMode(auto);
        setInitAuto(auto);
        startSearch(keyword);
        mBinding.part.setTag(keyword);
    }


    private boolean isPass(Site item) {
        // Basic check
        if (item == null) return false;
        if (isAutoMode() && !item.isChangeable()) return false;
        return item.isSearchable();
    }


    private void startSearch(String keyword) {
        // Basic checks
        if (TextUtils.isEmpty(keyword) || mQuickAdapter == null || mBinding == null) return;
        mQuickAdapter.clear();
        mBinding.quick.setVisibility(View.GONE); // Hide initially

        List<Site> sites = new ArrayList<>();
        for (Site site : VodConfig.get().getSites()) if (isPass(site)) sites.add(site);

        if(sites.isEmpty()){
            if(isAutoMode()) showEmpty();
            return;
        }

        mExecutor = Executors.newFixedThreadPool(Constant.THREAD_POOL);
        for (Site site : sites) mExecutor.execute(() -> search(site, keyword));
    }


    private void stopSearch() {
        if (mExecutor != null && !mExecutor.isShutdown()) { // Added check
            try {
                mExecutor.shutdownNow();
            } catch (Exception e) {
                Log.e("VideoActivity", "Error stopping search executor", e);
            }
            mExecutor = null;
        }
         setAutoMode(false); // Reset flags
         setInitAuto(false);
    }

    private void search(Site site, String keyword) {
        // Basic checks
        if (site == null || TextUtils.isEmpty(keyword) || mViewModel == null) return;
        try {
            mViewModel.searchContent(site, keyword, true);
        } catch (Throwable ignored) {
            // Ignore? Log?
             Log.w("VideoActivity", "Search ignored for site: " + site.getName());
        }
    }

    private void setSearch(Result result) {
        // Basic checks
        if (result == null || result.getList() == null || mQuickAdapter == null || mBinding == null) return;

        List<Vod> items = result.getList();
        Iterator<Vod> iterator = items.iterator();
        while (iterator.hasNext()) if (mismatch(iterator.next())) iterator.remove();

        if (!items.isEmpty()) {
            mQuickAdapter.addAll(mQuickAdapter.size(), items);
            mBinding.quick.setVisibility(View.VISIBLE);
            if (isInitAuto()) nextSite(); // Try first result if auto-searching
            App.removeCallbacks(mR4);
             setR2Callback(100); // Update focus
        }
    }

    private void setSearch(Vod item) {
        // Basic check
        if (item == null) return;
        setAutoMode(false);
        setInitAuto(false);
        stopSearch(); // Stop background search on manual selection
        getDetail(item);
    }

    private boolean mismatch(Vod item) {
        // Basic checks
        if (item == null || TextUtils.isEmpty(item.getVodId())) return true;
        if (getId().equals(item.getVodId())) return true;
        if (mBroken.contains(item.getVodId())) return true;

        String keyword = Objects.toString(mBinding.part.getTag(), "");
        String itemName = item.getVodName();
        if (TextUtils.isEmpty(itemName)) return true; // Mismatch if no name

        if (isAutoMode()) {
            // Simplified auto mode check: contains or contained by keyword
            return !(itemName.contains(keyword) || keyword.contains(itemName));
        } else {
            // Manual search: contains keyword (case insensitive)
             return !itemName.toLowerCase().contains(keyword.toLowerCase());
        }
    }


    private void nextParse(int position) {
        // Basic checks
        if (mParseAdapter == null || position + 1 >= mParseAdapter.size()) {
             checkFlagFlow(); // Move on if no next parse
             return;
        }
        Parse parse = (Parse) mParseAdapter.get(position + 1);
        if (parse != null) { // Added check
            Notify.show(getString(R.string.play_switch_parse, parse.getName()));
            setParseActivated(parse);
        } else {
             checkFlagFlow(); // Skip null parse
        }
    }

    private void nextFlag(int position) {
        // Basic checks
        if (mFlagAdapter == null || position + 1 >= mFlagAdapter.size()) {
             checkSearch(false); // Move on if no next flag
             return;
        }
        Flag flag = (Flag) mFlagAdapter.get(position + 1);
        if (flag != null) { // Added check
            Notify.show(getString(R.string.play_switch_flag, flag.getFlag()));
            setFlagActivated(flag);
        } else {
             checkSearch(false); // Skip null flag
        }
    }

    private void nextSite() {
        // Basic checks
        if (mQuickAdapter == null || mQuickAdapter.size() == 0) {
             if(isAutoMode()) showEmpty(); // Show empty if auto search failed
             return;
        }
        Vod item = (Vod) mQuickAdapter.get(0);
        mQuickAdapter.removeItems(0, 1);

        if (item == null) { // Added check for null item
            nextSite(); // Try next if first was null
            return;
        }

        Notify.show(getString(R.string.play_switch_site, item.getSiteName()));
        String currentId = getId(); // Get current failing ID
        if(!TextUtils.isEmpty(currentId) && !currentId.equals(item.getVodId())) {
             mBroken.add(currentId); // Add current failing ID to broken list
        }
        setInitAuto(false); // Turn off init auto flag
        stopSearch(); // Stop background search when loading a result
        getDetail(item);
    }


    private void onPaused() {
        // Basic checks
        if (mPlayers == null || mBinding == null) return;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBinding.widget.exoDuration.setText(mPlayers.getDurationTime());
        mBinding.widget.exoPosition.setText(mPlayers.getPositionTime(0));
        mBinding.widget.action.setImageResource(R.drawable.ic_widget_pause);

        if (isFullscreen()) showInfoAndCenter();
        else hideInfoAndCenter();
        mPlayers.pause();
    }

    private void onPlay() {
        // Basic checks
        if (mPlayers == null || mBinding == null) return;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPlayers.play();
        hideCenter();
        hideInfo(); // Hide info when playing starts
    }

    // ... (Getter/Setter methods remain mostly the same, simplified) ...
     public boolean isBackground() { return background; }
     public void setBackground(boolean background) { this.background = background; }
     public boolean isFullscreen() { return fullscreen; }
     private void setFullscreen(boolean fullscreen) { this.fullscreen = fullscreen; }
     private boolean isInitTrack() { return initTrack; }
     private void setInitTrack(boolean initTrack) { this.initTrack = initTrack; }
     private boolean isInitAuto() { return initAuto; }
     private void setInitAuto(boolean initAuto) { this.initAuto = initAuto; }
     private boolean isAutoMode() { return autoMode; }
     private void setAutoMode(boolean autoMode) { this.autoMode = autoMode; }
     public boolean isUseParse() { return useParse; }
     public void setUseParse(boolean useParse) { this.useParse = useParse; }
     public int getToggleCount() { return toggleCount; }
     public void resetToggle() { this.toggleCount = 0; }
     public int addErrorCount() { return ++errorCount; }
     public void resetError() { this.errorCount = 0; }
     public int getGroupSize() { return groupSize > 0 ? groupSize : 20; } // Ensure default
     public void setGroupSize(int size) { groupSize = Math.max(size, 1); } // Ensure positive

     private View getFocus1() {
         return (mFocus1 != null && mFocus1.isFocusable()) ? mFocus1 : mBinding.video; // Simplified
     }

     private View getFocus2() {
         // Simplified fallback logic
         View focusTarget = mFocus2;
         if (focusTarget == null || !isVisible(focusTarget) || !focusTarget.isEnabled()) {
              focusTarget = mBinding.control.play; // Default to play/pause
              if (focusTarget == null || !isVisible(focusTarget) || !focusTarget.isEnabled()) {
                   focusTarget = mBinding.control.next; // Fallback to next
                    if (focusTarget == null || !isVisible(focusTarget) || !focusTarget.isEnabled()) {
                         focusTarget = mBinding.video; // Final fallback
                    }
              }
         }
         return focusTarget;
     }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event == null) return super.dispatchKeyEvent(null); // Basic check

        hasKeyEvent = true;
        View currentFocus = getCurrentFocus();

        // Simplified back key logic
        if (mBinding != null && !isFullscreen() && KeyUtil.isBackKey(event) && Setting.getSmallWindowBackKey() == 1) {
            if (currentFocus != mBinding.video) {
                mFocus1 = mBinding.video;
                if (mFocus1 != null) mFocus1.requestFocus(); // Basic check
                return true;
            }
        }

        // Menu key logic
        if (isFullscreen() && KeyUtil.isMenuKey(event) && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (Setting.getFullscreenMenuKey() == 0) { onToggle(); return true; }
            if (Setting.getFullscreenMenuKey() == 1) { onEpisodes(); return true; }
        }

        // Store focus in controls
        if (mBinding != null && isVisible(mBinding.control.getRoot()) && currentFocus != null && currentFocus.getParent() == mBinding.control.actionLayout) { // Basic check for parent
            mFocus2 = currentFocus;
        }

        // Reset auto-hide timer
        if (mBinding != null && isVisible(mBinding.control.getRoot())) setR1Callback();

        // Custom key handling
        if (isFullscreen() && mBinding != null && isGone(mBinding.control.getRoot()) && mKeyDown != null && mKeyDown.hasEvent(event)) { // Basic check
            return mKeyDown.onKeyDown(event);
        }

        return super.dispatchKeyEvent(event);
    }


    // --- CustomKeyDownVod.Listener Callbacks (Simplified Checks) ---

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
        if (progress == 0) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_off);
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
        hideProgress();
    }

    @Override
    public void onSeekTo(int time) {
        if (mPlayers == null) return;
        mPlayers.seekTo(time);
        if (mKeyDown != null) mKeyDown.resetTime(); // Basic check
        showProgress();
        // onPlay(); // Player should handle resuming after seek
    }

    @Override
    public void onSpeedUp() {
        if (mPlayers == null || !mPlayers.isPlaying() || !mPlayers.canAdjustSpeed() || mBinding == null) return;
        float targetSpeed = (mPlayers.getSpeed() < 3) ? 3f : 5f;
        mBinding.control.speed.setText(mPlayers.setSpeed(targetSpeed));
        mBinding.widget.speed.startAnimation(ResUtil.getAnim(R.anim.forward));
         mBinding.widget.speedText.setText(getString(R.string.play_speed_val, String.valueOf(targetSpeed)));
        mBinding.widget.speed.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSpeedEnd() {
        if (mPlayers == null || mBinding == null) return;
        float previousSpeed = (mHistory != null) ? mHistory.getSpeed() : Setting.getPlaySpeed();
        mBinding.control.speed.setText(mPlayers.setSpeed(previousSpeed));
        mBinding.widget.speed.setVisibility(View.GONE);
        mBinding.widget.speed.clearAnimation();
    }


    @Override
    public void onKeyUp() {
        if (mPlayers == null || mBinding == null) return;
        long current = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        long half = (duration > 0) ? duration / 2 : 0;
        showInfo();
        showControl(current < half ? mBinding.control.opening : mBinding.control.ending);
    }

    @Override
    public void onKeyDown() {
        if (mBinding == null) return;
        showInfo();
        showControl(getFocus2());
    }

    @Override
    public void onKeyCenter() {
        if (mPlayers == null) return;
        if (mPlayers.isPlaying()) onPaused();
        else onPlay();
    }

    @Override
    public void onSingleTap() {
        if (isFullscreen()) onToggle();
    }

    @Override
    public void onDoubleTap() {
        if (isFullscreen()) onKeyCenter();
    }

    // --- PlayerDialog.Listener ---
    @Override
    public void onPlayerClick(Integer item) {
        // Basic checks
        if (item == null || mPlayers == null || mPlayers.getPlayer() == item) return;
        mPlayers.setPlayer(item);
        if (mHistory != null) { // Added check
             mHistory.setPlayer(item);
             if (!Setting.isIncognito()) mHistory.save();
        }
        setPlayerView();
        setDecodeView();
        onRefresh();
    }

    @Override
    public void onPlayerShare(String title) {
        this.onChoose();
    }

    // --- Activity Lifecycle & Results (Simplified Checks) ---

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case 1000:
                setResult(RESULT_OK);
                finish();
                break;
            case 1001:
                if (data != null && mPlayers != null) mPlayers.checkData(data); // Basic checks
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setBackground(false);
        if (mClock != null) mClock.start(); // Added check
        if (mPlayers != null) mPlayers.play(); // Basic check
         if (Setting.isDisplaySpeed()) App.post(mR3, 0); // Restart traffic
    }

    @Override
    protected void onPause() {
        super.onPause();
        setBackground(true);
        if (mPlayers != null) { // Added check
             // Save position on pause
             if (mHistory != null && !Setting.isIncognito()) {
                  mHistory.setPosition(mPlayers.getPosition());
                  mHistory.update();
             }
            mPlayers.pause();
        }
        if (mClock != null) mClock.stop(); // Added check
         App.removeCallbacks(mR3); // Stop traffic
         stopSearch();
    }

    @Override
    public void onBackPressed() {
        if (mBinding != null && isVisible(mBinding.control.getRoot())) { // Basic check
            hideControl();
        } else if (mBinding != null && isVisible(mBinding.widget.center)) { // Basic check
            hideCenter();
             if (mPlayers != null) mPlayers.play(); // Resume play
        } else if (isFullscreen()) {
            exitFullscreen();
        } else {
            stopSearch();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSearch();
        if (mClock != null) mClock.release(); // Added check
        if (mPlayers != null) mPlayers.release(); // Added check
        Source.get().stop();
        RefreshEvent.history();
        App.removeCallbacks(mR1, mR2, mR3, mR4);

        // Clean up glow effect resources
        if (mBinding != null && mBinding.logoImageView != null) { // Added checks
            mBinding.logoImageView.setLayerType(View.LAYER_TYPE_NONE, null);
        }
        logoGlowPaint = null;

        // Simplified Glide cleanup
        try {
            // No activity state check here, rely on Glide's internal handling
             Glide.with(getApplicationContext()).onDestroy();
        } catch (Exception e) {
            Log.e("VideoActivity", "Error during Glide onDestroy", e);
        }

        mBinding = null;
    }

    // Helper method to check if a View is visible
    private boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    // Helper method to check if a View is gone
    private boolean isGone(View view) {
        return view == null || view.getVisibility() == View.GONE;
    }

     // Simplified Helper method to notify item changes for adapters
     private void notifyItemChanged(RecyclerView view, RecyclerView.Adapter<?> adapter) {
         if (view != null && adapter != null) { // Basic check
             adapter.notifyDataSetChanged();
         }
     }
     private void notifyItemChanged(BaseGridView view, ArrayObjectAdapter adapter) {
         if (view != null && adapter != null) { // Basic check
             adapter.notifyArrayItemRangeChanged(0, adapter.size());
         }
     }
     private void notifyItemChanged(BaseGridView view, QualityAdapter adapter) {
         if (view != null && adapter != null) { // Basic check
             adapter.notifyDataSetChanged(); // Use notifyDataSetChanged for simplicity
         }
     }

}