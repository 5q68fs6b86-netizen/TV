package com.fongmi.android.tv.ui.activity;

import android.view.ViewParent;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap; // <-- Added for glow effect
import android.graphics.BlurMaskFilter; // <-- Added for glow effect
import android.graphics.Canvas; // <-- Added for glow effect
import android.graphics.Color; // <-- Added for glow effect
import android.graphics.Outline;
import android.graphics.Paint; // <-- Added for glow effect
import android.graphics.drawable.BitmapDrawable; // <-- Added for glow effect
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable; // <-- Added for glow effect
import android.net.Uri;
import android.os.Build; // <-- Added for version checks
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.ImageView; // <-- Keep ImageView import
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.bumptech.glide.request.target.CustomTarget;
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
import com.fongmi.android.tv.utils.TmdbHelper; // Import TmdbHelper
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
    // --- End Variables ---

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
        // Add boundary check
        int position = getFlagPosition();
        if (position >= 0 && position < mFlagAdapter.size()) {
            return (Flag) mFlagAdapter.get(position);
        }
        return null; // Return null if position is invalid
    }


    private Episode getEpisode() {
        // Add boundary check
        int position = getEpisodePosition();
        if (position >= 0 && position < mEpisodeAdapter.size()) {
            return (Episode) mEpisodeAdapter.get(position);
        }
        return null; // Return null if position is invalid
    }


    private int getFlagPosition() {
        for (int i = 0; i < mFlagAdapter.size(); i++) {
            Flag flag = (Flag) mFlagAdapter.get(i);
            if (flag != null && flag.isActivated()) return i; // Add null check
        }
        return 0;
    }

    private int getEpisodePosition() {
        for (int i = 0; i < mEpisodeAdapter.size(); i++) {
            Episode episode = (Episode) mEpisodeAdapter.get(i);
            if (episode != null && episode.isActivated()) return i; // Add null check
        }
        return 0;
    }

    private int getParsePosition() {
        for (int i = 0; i < mParseAdapter.size(); i++) {
            Parse parse = (Parse) mParseAdapter.get(i);
            if (parse != null && parse.isActivated()) return i; // Add null check
        }
        return 0;
    }

    private int getPlayer() {
        Site site = getSite(); // Get site once
        if (mHistory != null && mHistory.getPlayer() != -1) return mHistory.getPlayer();
        if (site != null && site.getPlayerType() != -1) return site.getPlayerType(); // Add null check for site
        return Setting.getPlayer();
    }

    private int getScale() {
        return mHistory != null && mHistory.getScale() != -1 ? mHistory.getScale() : Setting.getScale();
    }

    private PlayerView getExo() {
        return mBinding.exo;
    }

    private IjkVideoView getIjk() {
        return mBinding.ijk;
    }

    private Drawable getDefaultArtwork() {
        if (mPlayers.isExo()) return getExo().getDefaultArtwork();
        return getIjk().getDefaultArtwork();
    }

    private BaseGridView getEpisodeView() {
        return Setting.getEpisode() == 0 ? mBinding.episodeHori : mBinding.episodeVert;
    }

    private void setEpisodeSelectedPosition(int position) {
        // Add boundary checks
        if (position < 0 || mEpisodeAdapter == null || position >= mEpisodeAdapter.size()) {
            return;
        }
        getEpisodeView().setSelectedPosition(position);
        if (hasKeyEvent) return;
        if (isFullscreen()) return;
        getEpisodeView().postDelayed(() -> {
            if (getEpisodeView() == null || getEpisodeView().getLayoutManager() == null) return; // Null check
            View selectedItem = getEpisodeView().getLayoutManager().findViewByPosition(position);
            View focusedView = getCurrentFocus();
            if (selectedItem != null) selectedItem.requestFocus();
            if (focusedView == mBinding.video) mBinding.video.requestFocus();
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
                if (view != null && outline != null) { // Add null checks
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
        mBinding.video.setOnTouchListener((view, event) -> mKeyDown != null && mKeyDown.onTouchEvent(event)); // Add null check for mKeyDown
        mBinding.flag.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                if (mFlagAdapter != null && mFlagAdapter.size() > 0 && position >= 0 && position < mFlagAdapter.size()) { // Add null and boundary checks
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
                if (mEpisodeAdapter != null && mEpisodeAdapter.size() > getGroupSize() && position > 1 && hasKeyEvent) { // Add null check
                    setEpisodeSelectedPosition((position - 2) * getGroupSize());
                }
            }
        });
    }

    private void setEpisodeChildKeyListener(RecyclerView.ViewHolder child, int position) {
        if (getEpisodeView() != mBinding.episodeVert || child == null || child.itemView == null) return; // Add null checks
        RecyclerView.Adapter<?> adapter = getEpisodeView().getAdapter();
        if (adapter == null) return; // Add adapter null check
        int itemCount = adapter.getItemCount();
        if (itemCount <= 0) return;
        int columns = mEpisodePresenter != null ? mEpisodePresenter.getNumColumns() : 1; // Add null check and default
        // Check if the item is in the last row and potentially not filling all columns
        boolean isLastRow = (position / columns) == ((itemCount - 1) / columns);
        boolean isBeyondLastActualItemInRow = (position % columns) >= (itemCount % columns) && (itemCount % columns != 0);

        if (isLastRow) { // More robust check for last row items needing special handling
            child.itemView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                         RecyclerView.LayoutManager layoutManager = getEpisodeView().getLayoutManager();
                         if (layoutManager == null) return false; // Null check
                        View lastItem = layoutManager.findViewByPosition(itemCount - 1);
                        if (lastItem != null) {
                            lastItem.requestFocus();
                            return true; // Consume the event
                        }
                    }
                    return false;
                }
            });
        } else {
            // Remove listener if not in the last potentially incomplete row
            child.itemView.setOnKeyListener(null);
        }
    }


    private void setRecyclerView() {
        mBinding.flag.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.flag.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mFlagPresenter = new FlagPresenter(this::setFlagActivated); // Init presenter before adapter
        mBinding.flag.setAdapter(new ItemBridgeAdapter(mFlagAdapter = new ArrayObjectAdapter(mFlagPresenter)));

        mBinding.quality.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.quality.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.quality.setAdapter(mQualityAdapter = new QualityAdapter(this::setQualityActivated));

        mBinding.array.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.array.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mArrayPresenter = new ArrayPresenter(this); // Init presenter before adapter
        mBinding.array.setAdapter(new ItemBridgeAdapter(mArrayAdapter = new ArrayObjectAdapter(mArrayPresenter)));

        mBinding.part.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.part.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPartPresenter = new PartPresenter(item -> initSearch(item, false)); // Init presenter before adapter
        mBinding.part.setAdapter(new ItemBridgeAdapter(mPartAdapter = new ArrayObjectAdapter(mPartPresenter)));

        mBinding.quick.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.quick.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // Init presenter before adapter
        mBinding.quick.setAdapter(new ItemBridgeAdapter(mQuickAdapter = new ArrayObjectAdapter(new QuickPresenter(this::setSearch))));

        mBinding.control.parse.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.control.parse.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
         // Init presenter before adapter
        mBinding.control.parse.setAdapter(new ItemBridgeAdapter(mParseAdapter = new ArrayObjectAdapter(new ParsePresenter(this::setParseActivated))));
        // Ensure VodConfig is initialized before getting parses
        if (VodConfig.get().getParses() != null) {
            mParseAdapter.setItems(VodConfig.get().getParses(), null);
        }
    }


    private void setEpisodeView() {
        mBinding.episodeVert.setVerticalSpacing(ResUtil.dp2px(8));
        mBinding.episodeHori.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.episodeVert.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.episodeHori.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mEpisodePresenter = new EpisodePresenter(this::setEpisodeActivated); // Init presenter before adapter
        getEpisodeView().setAdapter(new ItemBridgeAdapter(mEpisodeAdapter = new ArrayObjectAdapter(mEpisodePresenter)));
    }

    private void setVideoView() {
        // Ensure mPlayers is initialized
        if (mPlayers == null) mPlayers = Players.create(this);
        mPlayers.init(getExo(), getIjk());
        ExoUtil.setSubtitleView(mBinding.exo);
        IjkUtil.setSubtitleView(mBinding.ijk);
        mBinding.control.reset.setText(ResUtil.getStringArray(R.array.select_reset)[Setting.getReset()]);
        mBinding.exo.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        mBinding.ijk.setBackgroundColor(android.graphics.Color.TRANSPARENT);
    }

    private void setDanmuViewSettings() {
        if (mDanmakuContext == null) return; // Null check
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
        // Ensure mPlayers and mDanmakuContext are initialized
        if (mPlayers == null || mDanmakuContext == null) return;
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
        else if (mBinding.progressLayout != null) mBinding.progressLayout.showProgress(); // Null check
    }

    private void checkId() {
        // Ensure intent and ID are not null before processing
        Intent intent = getIntent();
        String id = getId(); // getId already handles null intent/extra
        if (intent == null) {
             setEmpty(false); // Cannot proceed without intent
             return;
        }

        if (id.startsWith("push://")) {
            intent.putExtra("key", "push_agent");
            intent.putExtra("id", id.substring(7));
            id = intent.getStringExtra("id"); // Update local id variable
        }

        if (TextUtils.isEmpty(id) || id.startsWith("msearch:")) {
            setEmpty(false);
        } else {
            getDetail();
        }
    }

    private void setPlayerView() {
        // Ensure mPlayers is initialized
        if (mPlayers == null) return;
        getIjk().setPlayer(mPlayers.getPlayer());
        mBinding.control.player.setText(mPlayers.getPlayerText());
        mBinding.control.speed.setEnabled(mPlayers.canAdjustSpeed());
        getExo().setVisibility(mPlayers.isExo() ? View.VISIBLE : View.GONE);
        getIjk().setVisibility(mPlayers.isIjk() ? View.VISIBLE : View.GONE);
        if (mHistory != null) { // Add null check for history
            mBinding.control.speed.setText(mPlayers.setSpeed(mHistory.getSpeed()));
        }
    }

    private void setDecodeView() {
        if (mPlayers != null) { // Null check
             mBinding.control.decode.setText(mPlayers.getDecodeText());
        }
    }

    private void setScale(int scale) {
        // Add null checks for views
        if (getExo() != null) getExo().setResizeMode(scale);
        if (getIjk() != null) getIjk().setResizeMode(scale);
        // Ensure scale index is valid for the array
        String[] scaleArray = ResUtil.getStringArray(R.array.select_scale);
        if (scale >= 0 && scale < scaleArray.length) {
            mBinding.control.scale.setText(scaleArray[scale]);
        }
    }


    private void getDetail() {
        // Ensure mViewModel is initialized and key/id are valid
        if (mViewModel == null || TextUtils.isEmpty(getKey()) || TextUtils.isEmpty(getId())) {
            Log.e("VideoActivity", "Cannot get detail, ViewModel or key/id is invalid.");
            setEmpty(true); // Show empty and finish if key/id invalid
            return;
        }
        mViewModel.detailContent(getKey(), getId());
    }

    private void getDetail(Vod item) {
        if (item == null) return; // Add null check
        getIntent().putExtra("key", item.getSiteKey());
        getIntent().putExtra("pic", item.getVodPic());
        getIntent().putExtra("id", item.getVodId());
        mBinding.scroll.scrollTo(0, 0);
        if (mClock != null) mClock.setCallback(null); // Null check for mClock
        if (mPlayers != null) { // Null check for mPlayers
           mPlayers.reset();
           mPlayers.stop();
        }
        getDetail();
    }

    private void setDetail(Result result) {
        if (result == null) { // Add null check
             setEmpty(true); // Consider finishing if result is null
             return;
        }
        if (result.getList().isEmpty()) {
            setEmpty(result.hasMsg());
        } else {
            Vod firstVod = result.getList().get(0);
            if (firstVod != null) { // Null check for the item
                 setDetail(firstVod);
            } else {
                 setEmpty(result.hasMsg()); // Treat null item as empty
            }
        }
        Notify.show(result.getMsg());
    }

    // --- Modified getPlayer (using combined title) ---
    private void getPlayer(Flag flag, Episode episode, boolean replay) {
        if (flag == null || episode == null || mViewModel == null) { // Add null checks
            Log.e("VideoActivity", "Cannot get player, flag, episode, or ViewModel is null.");
            // Optionally show an error message to the user
            Notify.show(R.string.error_play_load);
            return;
        }

        // Use currentVodName (original name) for titles
        String combinedTitle = getString(R.string.detail_title, currentVodName, episode.getName());
        mBinding.widget.title.setText(combinedTitle); // Set widget title
        mBinding.display.title.setText(combinedTitle); // Set display title

        mViewModel.playerContent(getKey(), flag.getFlag(), episode.getUrl());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        updateHistory(episode, replay);
        if (mPlayers != null) { // Null check for mPlayers
            mPlayers.clear();
            mPlayers.stop();
        }
        showProgress();
        setMetadata(); // Metadata uses original name from history
        hidePreview();
        hideCenter();
    }
    // --- End Modified getPlayer ---


    private void setPlayer(Result result) {
        if (result == null || mPlayers == null || mQualityAdapter == null) return; // Add null checks

        result.getUrl().set(mQualityAdapter.getPosition());
        Site site = getSite(); // Get site once
        boolean siteIsChangeable = (site != null) && site.isChangeable();
        int siteTimeout = (site != null) ? site.getTimeout() : -1; // Default timeout if site is null

        // Determine if parsing should be used
        boolean shouldUseParse = VodConfig.hasParse() &&
                                 ((result.getPlayUrl().isEmpty() && VodConfig.get().getFlags().contains(result.getFlag())) || result.getJx() == 1);
        setUseParse(shouldUseParse);

        mPlayers.start(result, isUseParse(), siteIsChangeable ? siteTimeout : -1);
        mBinding.control.parse.setVisibility(isUseParse() ? View.VISIBLE : View.GONE);
        setQualityVisible(result.getUrl().isMulti());
        checkDanmu(result.getDanmaku());
        mQualityAdapter.addAll(result);
    }


    private void checkDanmu(String danmu) {
        if (mBinding.danmaku == null || mDanmakuContext == null) return; // Null checks
        mBinding.danmaku.release();
        if (!Setting.isDanmuLoad() || TextUtils.isEmpty(danmu)) { // Simplified condition
             mBinding.danmaku.setVisibility(View.GONE);
             return;
        }
        mBinding.danmaku.setVisibility(View.VISIBLE);
        App.execute(() -> {
            // Ensure context is still valid in background thread
            if (mBinding.danmaku != null && mDanmakuContext != null) {
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
            // Use the name from intent as fallback title
            currentVodName = getName(); // Store it in currentVodName
             mBinding.nameTextView.setText(currentVodName); // Set fallback text initially
             mBinding.nameTextView.setVisibility(View.VISIBLE); // Make sure it's visible
             mBinding.logoImageView.setVisibility(View.GONE); // Hide logo placeholder
             mBinding.progressLayout.showContent(); // Show content area containing the text view
            App.post(mR4, 10000); // Timeout to show empty screen if search fails
            checkSearch(false); // Start search based on the name from intent
        }
    }


    private void showEmpty() {
        if (mBinding.progressLayout != null) { // Null check
             mBinding.progressLayout.showEmpty();
        }
        stopSearch();
    }

    // --- Modified setDetail(Vod item) ---
    private void setDetail(Vod item) {
        if (item == null || mBinding == null) return; // Add null checks

        mBinding.progressLayout.showContent();
        mBinding.video.setTag(item.getVodPic(getPic()));

        // 1. Store original VOD name
        currentVodName = item.getVodName(getName());
        currentLogoUrl = null; // Reset logo url

        // 2. Set initial title state: show fallback text, hide logo image
        mBinding.logoImageView.setVisibility(View.GONE); // Hide logo initially
        mBinding.logoImageView.setImageDrawable(null); // Clear any previous drawable (like LayerDrawable)
        mBinding.nameTextView.setVisibility(View.VISIBLE);
        mBinding.nameTextView.setText(currentVodName); // Display text title initially

        // Set other details
        setText(mBinding.remark, 0, item.getVodRemarks());
        setText(mBinding.year, R.string.detail_year, item.getVodYear());
        setText(mBinding.area, R.string.detail_area, item.getVodArea());
        setText(mBinding.type, R.string.detail_type, item.getTypeName());
        Site site = getSite(); // Get site once
        setText(mBinding.site, R.string.detail_site, site != null ? site.getName() : ""); // Handle null site
        setText(mBinding.actor, R.string.detail_actor, Html.fromHtml(Objects.toString(item.getVodActor(),"")).toString()); // Handle null html
        setText(mBinding.content, R.string.detail_content, Html.fromHtml(Objects.toString(item.getVodContent(),"")).toString());// Handle null html
        setText(mBinding.director, R.string.detail_director, Html.fromHtml(Objects.toString(item.getVodDirector(),"")).toString()); // Handle null html

        if (mFlagAdapter != null) { // Null check for adapter
            mFlagAdapter.setItems(item.getVodFlags(), null);
        }
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
    private void onLogoNotFound() {
        // 确保在主线程执行 UI 更新
        runOnUiThread(() -> {
            if (isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                Log.w("VideoActivity", "Activity is finishing or destroyed in onLogoNotFound (Activity level), skipping UI update.");
                return;
            }
            Log.d("VideoActivity", "Logo not found or error occurred.");
            currentLogoUrl = null;
            // 隐藏 ImageView 并清除内容
            mBinding.logoImageView.setVisibility(View.GONE);
            mBinding.logoImageView.setImageDrawable(null);
            mBinding.nameTextView.setVisibility(View.VISIBLE);
            mBinding.nameTextView.setText(currentVodName); // 使用存储的 VOD 名称
        });
    }

    // --- fetchTmdbLogo Method with BlurMaskFilter + LayerDrawable ---
    private void fetchTmdbLogo(String title, String year, String typeName) {
        if (TextUtils.isEmpty(Constant.TMDB_API_KEY) || "YOUR_TMDB_API_KEY_HERE".equals(Constant.TMDB_API_KEY)) {
            Log.e("VideoActivity", "TMDB API Key not set! Skipping logo fetch.");
            if (isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) return;
            // Ensure UI falls back to text state if API key is missing
            onLogoNotFound(); // Use this to manage UI state
            return;
        }

        TmdbHelper.findLogoForVod(title, year, typeName, new TmdbHelper.LogoCallback() {
            @Override
            public void onLogoFound(@NonNull String logoUrl) {
                if (isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                    Log.w("VideoActivity", "Activity is finishing or destroyed in onLogoFound, skipping Glide load.");
                    return;
                }

                currentLogoUrl = logoUrl;
                mBinding.nameTextView.setVisibility(View.GONE);
                // Target ImageView (only one needed now)
                ImageView targetImageView = mBinding.logoImageView;
                targetImageView.setVisibility(View.VISIBLE); // Make ImageView visible to receive the drawable

                // Use Glide to request Bitmap
                Glide.with(VideoActivity.this)
                        .asBitmap() // Request Bitmap
                        .load(logoUrl)
                        .placeholder(R.drawable.ic_placeholder) // Optional placeholder
                        .error(R.drawable.ic_error)         // Optional error drawable
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                // Re-check activity state as loading is async
                                if (isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                                    return;
                                }
                                try {
                                    // --- 1. Create Glow Bitmap ---
                                    float blurRadius = ResUtil.dp2px(6); // Adjust radius (e.g., 6dp)
                                    int glowColor = Color.argb(170, 255, 255, 255); // Adjust color and alpha (e.g., semi-transparent white)
                                    Bitmap glowBitmap = createGlowBitmap(resource, blurRadius, glowColor);

                                    if (glowBitmap != null) {
                                        // --- 2. Create LayerDrawable ---
                                        Drawable[] layers = new Drawable[2];
                                        layers[0] = new BitmapDrawable(getResources(), glowBitmap); // Glow at bottom
                                        layers[1] = new BitmapDrawable(getResources(), resource);   // Original logo on top

                                        LayerDrawable layerDrawable = new LayerDrawable(layers);

                                        // --- 3. Set to ImageView ---
                                        targetImageView.setImageDrawable(layerDrawable);
                                    } else {
                                        // Fallback: Show original bitmap if glow creation failed
                                        targetImageView.setImageBitmap(resource);
                                    }

                                } catch (Exception e) {
                                    Log.e("VideoActivity", "Error creating glow or setting LayerDrawable", e);
                                    // Fallback on exception
                                    targetImageView.setImageBitmap(resource);
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                // Clear the ImageView
                                targetImageView.setImageDrawable(placeholder);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                Log.w("VideoActivity", "Glide failed to load logo bitmap: " + logoUrl);
                                // Handle UI reset by calling onLogoNotFound
                                onLogoNotFound();
                            }
                        });
            }

            @Override
            public void onLogoNotFound() {
                if (isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                    Log.w("VideoActivity", "Activity is finishing or destroyed in onLogoNotFound, skipping UI update.");
                    return;
                }
                Log.d("VideoActivity", "TMDB Logo not found for: " + title);
                currentLogoUrl = null;
                // Hide the single ImageView and clear its content
                mBinding.logoImageView.setVisibility(View.GONE);
                mBinding.logoImageView.setImageDrawable(null);
                mBinding.nameTextView.setVisibility(View.VISIBLE);
                mBinding.nameTextView.setText(currentVodName);
            }

            @Override
            public void onError() {
                if (isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                    Log.w("VideoActivity", "Activity is finishing or destroyed in onError, skipping UI update.");
                    return;
                }
                Log.w("VideoActivity", "Error fetching TMDB logo for: " + title);
                // Use onLogoNotFound to handle UI reset
                onLogoNotFound();
            }
        });
    }

    /**
     * Creates a Bitmap with an outer glow effect.
     *
     * @param originalBitmap The source Bitmap.
     * @param blurRadius     The blur radius in pixels.
     * @param glowColor      The color of the glow (including alpha).
     * @return A new Bitmap with the glow effect, or null on error.
     */
    private Bitmap createGlowBitmap(Bitmap originalBitmap, float blurRadius, int glowColor) {
        if (originalBitmap == null || originalBitmap.isRecycled()) {
            return null;
        }
        // Ensure blurRadius is positive
        if (blurRadius <= 0) {
            blurRadius = 1;
        }

        try {
            // ARGB_8888 is required for alpha channel and transparency
            Bitmap glowBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(glowBitmap);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(glowColor);
            // Use Blur.OUTER for a glow effect outside the original shape
            paint.setMaskFilter(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.OUTER));

            // Extract the alpha mask from the original bitmap. This defines the shape for the glow.
            // Requires the original bitmap to have an alpha channel (like ARGB_8888).
            Bitmap alphaBitmap = originalBitmap.extractAlpha();

            if (alphaBitmap != null) {
                // Draw the alpha mask onto the glow canvas using the blur paint.
                // The paint color and blur filter will be applied based on the mask's shape.
                canvas.drawBitmap(alphaBitmap, 0, 0, paint);
                // Recycle the temporary alpha mask bitmap
                alphaBitmap.recycle();
            } else {
                // Fallback if alpha mask couldn't be extracted (e.g., bitmap format issue)
                // This might blur the entire original bitmap, which is usually not the desired glow effect.
                Log.w("VideoActivity", "Could not extract alpha mask, falling back to blurring original bitmap.");
                paint.setMaskFilter(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)); // Use NORMAL blur as fallback
                canvas.drawBitmap(originalBitmap, 0, 0, paint);
            }

            return glowBitmap;

        } catch (OutOfMemoryError oom) {
            Log.e("VideoActivity", "OutOfMemoryError while creating glow bitmap", oom);
            System.gc(); // Suggest garbage collection
            return null;
        } catch (Exception e) {
            Log.e("VideoActivity", "Exception while creating glow bitmap", e);
            return null;
        }
    }
    // --- End fetchTmdbLogo Method ---


    private int getMaxLines() {
        int lines = 1;
        // Check visibility using the isGone helper method or direct check
        if (mBinding.actor.getVisibility() == View.GONE) ++lines;
        if (mBinding.remark.getVisibility() == View.GONE) ++lines;
        if (mBinding.director.getVisibility() == View.GONE) ++lines;
        return lines;
    }

    @Override // 添加 Override 注解
    protected boolean isGone(View view) { // 改为 protected
        return view == null || view.getVisibility() == View.GONE;
    }

    @Override // 添加 Override 注解
    protected boolean isVisible(View view) { // 改为 protected
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    private void setText(TextView view, int resId, String text) {
        if (view == null) return; // Null check for view
        String finalText = Objects.toString(text, ""); // Ensure text is not null

        view.setText(getSpan(resId, finalText), TextView.BufferType.SPANNABLE);
        view.setVisibility(finalText.isEmpty() ? View.GONE : View.VISIBLE);
        view.setLinkTextColor(MDColor.WHITE); // Use library color or Color.WHITE
        CustomMovement.bind(view);
        view.setTag(finalText); // Store the original text in tag
    }

    private SpannableStringBuilder getSpan(int resId, String text) {
        String processedText = (resId > 0) ? getString(resId, text) : text;
        processedText = Objects.toString(processedText, ""); // Ensure not null

        Map<String, String> map = new HashMap<>();
        Matcher m = Sniffer.CLICKER.matcher(processedText);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String group1 = m.group(1); // URL or action string
            String group2 = m.group(2); // Display text
            if (group1 != null && group2 != null) {
                String key = Trans.s2t(group2).trim(); // Use display text as key
                m.appendReplacement(sb, Matcher.quoteReplacement(key)); // Replace with key text
                map.put(key, group1); // Map display text to action string
            }
        }
        m.appendTail(sb);
        processedText = sb.toString(); // Text with replacements done

        SpannableStringBuilder span = SpannableStringBuilder.valueOf(processedText);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String action = entry.getValue();
            int start = processedText.indexOf(key);
            if (start != -1) {
                int end = start + key.length();
                Result result = Result.type(action); // Assuming Result.type handles the action string
                span.setSpan(getClickSpan(result), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return span;
    }


    private ClickableSpan getClickSpan(Result result) {
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                // Ensure result and key are valid before starting activity
                if (result != null && !TextUtils.isEmpty(getKey())) {
                     VodActivity.start(getActivity(), getKey(), result);
                }
            }
        };
    }

    private void setFlagActivated(Flag item) {
        if (mFlagAdapter == null || mFlagAdapter.size() == 0 || item == null || item.isActivated()) return;

        int itemIndex = mFlagAdapter.indexOf(item);
        // If item not found, try activating the first flag as fallback (if adapter not empty)
        if (itemIndex == -1 && mFlagAdapter.size() > 0) {
            Flag firstFlag = (Flag) mFlagAdapter.get(0);
             if(firstFlag != null) {
                 item = firstFlag; // Use the first flag
                 itemIndex = 0;
             } else {
                 return; // Cannot proceed if first flag is also null
             }
        } else if (itemIndex == -1) {
             return; // Item not found and adapter is empty
        }


        for (int i = 0; i < mFlagAdapter.size(); i++) {
            Flag flag = (Flag) mFlagAdapter.get(i);
            if (flag != null) flag.setActivated(item); // Activate the selected one
        }

        if (itemIndex != -1) { // Ensure index is valid before setting selection
             mBinding.flag.setSelectedPosition(itemIndex);
        }
        notifyItemChanged(mBinding.flag, mFlagAdapter);
        setEpisodeAdapter(item.getEpisodes());
        setQualityVisible(false);
        seamless(item);
    }

    private void setEpisodeAdapter(List<Episode> items) {
        // Ensure adapter and view are not null
        if (mEpisodeAdapter == null || getEpisodeView() == null) return;

        boolean hasItems = items != null && !items.isEmpty();
        getEpisodeView().setVisibility(hasItems ? View.VISIBLE : View.GONE);

        if (hasItems) {
            if (isVisible(mBinding.episodeVert)) {
                setEpisodeView(items); // Configure vertical layout if visible
            }
            mEpisodeAdapter.setItems(items, null);
            setArrayAdapter(items.size());
        } else {
            mEpisodeAdapter.clear(); // Clear adapter if items are null or empty
            setArrayAdapter(0);
        }
        setR2Callback(50);
    }

    private void setEpisodeView(List<Episode> items) {
        // Ensure items and presenter are not null
        if (items == null || items.isEmpty() || mEpisodePresenter == null) return;

        int size = items.size();
        int episodeNameLength = 0;

        // Calculate max episode name length safely
        for (int i = 0; i < size; i++) {
            Episode episode = items.get(i);
            if (episode == null) continue;
            episode.setIndex(i); // Set index regardless of name
            String name = episode.getName();
            int length = (name == null) ? 0 : name.length();
            if (length > episodeNameLength) episodeNameLength = length;
        }

        // Determine number of columns based on max length
        int numColumns = 10; // Default
        if (episodeNameLength > 40) numColumns = 1;
        else if (episodeNameLength > 30) numColumns = 2;
        else if (episodeNameLength > 15) numColumns = 3;
        else if (episodeNameLength > 10) numColumns = 4;
        else if (episodeNameLength > 6) numColumns = 6;
        else if (episodeNameLength > 4) numColumns = 8;

        int rowNum = (numColumns == 0) ? 0 : (int) Math.ceil((double) size / (double) numColumns); // Avoid division by zero
        int width = ResUtil.getScreenWidth() - ResUtil.dp2px(48); // Available width

        ViewGroup.LayoutParams params = mBinding.episodeVert.getLayoutParams();
        params.width = ResUtil.getScreenWidth(); // Use full screen width for container
        // Calculate height based on rows, ensure minimum height if rowNum is 0
        params.height = (rowNum > 6) ? ResUtil.dp2px(300) : ResUtil.dp2px(Math.max(rowNum, 1) * 44); // Min height for 1 row

        mBinding.episodeVert.setNumColumns(numColumns);
        if (numColumns > 0) { // Avoid division by zero
            int spacing = (numColumns - 1) * ResUtil.dp2px(8);
            mBinding.episodeVert.setColumnWidth((width - spacing) / numColumns);
        }
        mBinding.episodeVert.setLayoutParams(params);
        mBinding.episodeVert.setWindowAlignmentOffsetPercent(10f);

        // Update presenter with calculated values
        mEpisodePresenter.setNumColumns(numColumns);
        mEpisodePresenter.setNumRows(rowNum);
    }


    private void seamless(Flag flag) {
        if (flag == null || mHistory == null) return;
        Episode episode = flag.find(mHistory.getVodRemarks(), getMark().isEmpty());
        boolean shouldShowQuality = (episode != null && episode.isActivated() && mQualityAdapter != null && mQualityAdapter.getItemCount() > 1);
        setQualityVisible(shouldShowQuality);

        if (episode == null || episode.isActivated()) return; // Nothing to do if not found or already active

        if (Setting.getFlag() == 1) { // Preview mode: select but don't play
            // Temporarily activate to find position, then deactivate
            episode.setActivated(true);
            int position = getEpisodePosition(); // Get position while it's marked active
             if (!isFullscreen()) getEpisodeView().requestFocus(); // Focus list if not fullscreen
            setEpisodeSelectedPosition(position);
            episode.setActivated(false); // Deactivate again
            notifyItemChanged(getEpisodeView(), mEpisodeAdapter); // Update UI to reflect deselection
        } else { // Auto-play mode: activate and play
            mHistory.setVodRemarks(episode.getName()); // Update history remark first
            setEpisodeActivated(episode); // This will trigger playback
            hidePreview(); // Hide preview image if switching episode
        }
    }

    public void setEpisodeActivated(Episode item) {
        if (item == null || mFlagAdapter == null || mEpisodeAdapter == null) return; // Add null checks

        int flagPosition = getFlagPosition();
        if (flagPosition < 0) return; // Invalid flag position

        if (shouldEnterFullscreen(item)) return; // Enter fullscreen if needed and item is active

        if (isFullscreen()) {
             Notify.show(getString(R.string.play_ready, item.getName())); // Show notification in fullscreen
        }

        // Update activation state in all flags' episode lists
        for (int i = 0; i < mFlagAdapter.size(); i++) {
            Flag flag = (Flag) mFlagAdapter.get(i);
            if (flag != null) {
                flag.toggle(i == flagPosition, item); // Activate in current flag, deactivate in others
            }
        }

        // Update UI selection and trigger playback refresh
        setEpisodeSelectedPosition(getEpisodePosition()); // Move focus/selection
        notifyItemChanged(getEpisodeView(), mEpisodeAdapter); // Update adapter UI
        onRefresh(); // Start playing the newly activated episode
    }

    private void setQualityVisible(boolean visible) {
        if (mBinding.quality != null) { // Null check
            mBinding.quality.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        setR2Callback(100);
    }

    private void setQualityActivated(Result result) {
        if (result == null || mPlayers == null || getSite() == null) return; // Add null checks
        try {
            boolean siteIsChangeable = getSite().isChangeable();
            int siteTimeout = getSite().getTimeout();
            mPlayers.start(result, isUseParse(), siteIsChangeable ? siteTimeout : -1);
            if (mBinding.danmaku != null) mBinding.danmaku.hide(); // Null check
        } catch (Exception e) {
            ErrorEvent.extract(e.getMessage()); // Assuming ErrorEvent handles null message
            Log.e("VideoActivity", "Error setting quality activated", e);
        }
    }

    private void reverseEpisode(boolean scroll) {
        if (mFlagAdapter == null) return; // Null check
        for (int i = 0; i < mFlagAdapter.size(); i++) {
            Flag flag = (Flag) mFlagAdapter.get(i);
            if (flag != null && flag.getEpisodes() != null) {
                Collections.reverse(flag.getEpisodes());
            }
        }
        Flag currentFlag = getFlag();
        if (currentFlag != null) { // Null check for current flag
            setEpisodeAdapter(currentFlag.getEpisodes());
            if (scroll) setEpisodeSelectedPosition(getEpisodePosition());
        }
    }

    private void setParseActivated(Parse item) {
        if (item == null || VodConfig.get() == null || mParseAdapter == null) return; // Add null checks
        VodConfig.get().setParse(item); // Update global config
        // Update UI to reflect selection
        for(int i=0; i<mParseAdapter.size(); ++i) {
            Parse parse = (Parse) mParseAdapter.get(i);
            if(parse != null) parse.setActivated(parse == item);
        }
        notifyItemChanged(mBinding.control.parse, mParseAdapter);
        onRefresh(); // Refresh player with new parse setting
    }

    private void setArrayAdapter(int size) {
        // Ensure adapters and history are initialized
        if (mArrayAdapter == null || mBinding.array == null) return;

        if (size <= 0) {
            mBinding.array.setVisibility(View.GONE);
            mArrayAdapter.clear(); // Clear items if size is 0 or less
            return;
        }

        // Determine group size based on total size
        if (size > 200) setGroupSize(100);
        else if (size > 100) setGroupSize(40);
        else setGroupSize(20);

        List<String> items = new ArrayList<>();
        items.add(getString(R.string.play_reverse)); // Reverse sort button
        // Play order button (Forward/Reverse Play) - handle null history
        items.add(getString((mHistory != null) ? mHistory.getRevPlayText() : R.string.play_forward));

        // Show array view only if there's more than 1 episode
        mBinding.array.setVisibility(size > 1 ? View.VISIBLE : View.GONE);

        // Generate range strings based on sort order (handle null history)
        boolean isRevSort = (mHistory != null) && mHistory.isRevSort();
        if (isRevSort) {
            for (int i = size; i > 0; i -= getGroupSize()) {
                items.add(i + "-" + Math.max(i - (getGroupSize() - 1), 1));
            }
        } else {
            for (int i = 0; i < size; i += getGroupSize()) {
                items.add((i + 1) + "-" + Math.min(i + getGroupSize(), size));
            }
        }
        mArrayAdapter.setItems(items, null);
    }

    private int findFocusDown(int index) {
        List<Integer> orders = Arrays.asList(R.id.flag, R.id.quality, R.id.episodeHori, R.id.array, R.id.episodeVert, R.id.part, R.id.quick);
        for (int i = index + 1; i < orders.size(); i++) { // Start from next index
             View v = findViewById(orders.get(i));
             if (isVisible(v)) return orders.get(i); // Use helper
        }
        return 0; // No focusable view found below
    }

    private int findFocusUp(int index) {
        List<Integer> orders = Arrays.asList(R.id.flag, R.id.quality, R.id.episodeHori, R.id.array, R.id.episodeVert, R.id.part, R.id.quick);
        for (int i = index - 1; i >= 0; i--) { // Start from previous index
              View v = findViewById(orders.get(i));
              if (isVisible(v)) return orders.get(i); // Use helper
        }
        return 0; // No focusable view found above
    }


    private void updateFocus() {
        hasKeyEvent = false;
        // Ensure presenters and adapters are not null before setting focus targets
        if (mEpisodePresenter != null) {
            mEpisodePresenter.setNextFocusDown(findFocusDown(Setting.getEpisode() == 0 ? 2 : 4));
            mEpisodePresenter.setNextFocusUp(findFocusUp(Setting.getEpisode() == 0 ? 2 : 4));
        }
        if (mQualityAdapter != null) {
            mQualityAdapter.setNextFocusDown(findFocusDown(1));
        }
         if (mArrayPresenter != null) {
            mArrayPresenter.setNextFocusDown(findFocusDown(3));
             mArrayPresenter.setNextFocusUp(findFocusUp(3));
        }
        if (mFlagPresenter != null) {
            mFlagPresenter.setNextFocusDown(findFocusDown(0));
        }
        if (mPartPresenter != null) {
            mPartPresenter.setNextFocusUp(findFocusUp(5));
        }

        // Notify adapters to update focus properties if necessary
        notifyItemChanged(mBinding.flag, mFlagAdapter);
        notifyItemChanged(mBinding.quality, mQualityAdapter);
        notifyItemChanged(mBinding.array, mArrayAdapter);
        notifyItemChanged(getEpisodeView(), mEpisodeAdapter);
        notifyItemChanged(mBinding.part, mPartAdapter);
    }

    private void showDisplayInfo() {
        // Ensure views are not null
        if (mBinding.display == null || mBinding.widget == null || mBinding.control == null) return;

        boolean hasDialog = false;
        try {
            // Check if activity is in a valid state to access fragment manager
            if (!isFinishing() && !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                for (Fragment f : getSupportFragmentManager().getFragments()) {
                    if (f instanceof BottomSheetDialogFragment && f.isVisible()) { // Check visibility too
                        hasDialog = true;
                        break;
                    }
                }
            }
        } catch (IllegalStateException e) {
            // FragmentManager might not be available yet or activity state issues
            Log.w("VideoActivity", "Error checking for BottomSheetDialogFragment", e);
            hasDialog = false; // Assume no dialog if error occurs
        }

        boolean controlsVisible = isVisible(mBinding.control.getRoot());
        boolean infoVisible = isVisible(mBinding.widget.info);
        boolean isVod = (mPlayers != null) && mPlayers.isVod(); // Null check for mPlayers

        mBinding.display.clock.setVisibility(Setting.isDisplayTime() || infoVisible ? View.VISIBLE : View.GONE);
        mBinding.display.titleLayout.setVisibility(Setting.isDisplayVideoTitle() && !controlsVisible ? View.VISIBLE : View.GONE);
        mBinding.display.netspeed.setVisibility(Setting.isDisplaySpeed() && !controlsVisible && !hasDialog ? View.VISIBLE : View.GONE);
        mBinding.display.duration.setVisibility(Setting.isDisplayDuration() && !controlsVisible && isVod && !hasDialog ? View.VISIBLE : View.GONE);
        mBinding.display.progress.setVisibility(Setting.isDisplayMiniProgress() && !controlsVisible && isVod && !hasDialog ? View.VISIBLE : View.GONE);
    }


    private void onTimeChangeDisplaySpeed() {
        // Ensure necessary views and player exist
        if (mBinding.display == null || mPlayers == null) return;

        boolean controlsVisible = isVisible(mBinding.control.getRoot());
        long position = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        boolean isValidTime = position >= 0 && duration > 0;

        // Display Speed
        if (Setting.isDisplaySpeed() && !controlsVisible) {
            Traffic.setSpeed(mBinding.display.netspeed); // Assuming Traffic handles null view
        } else if (Setting.isDisplaySpeed()) {
            // Optionally hide or clear speed if controls are visible
            mBinding.display.netspeed.setText(""); // Clear text
        }

        // Display Duration Text
        if (Setting.isDisplayDuration() && !controlsVisible && isValidTime) {
            mBinding.display.duration.setText(mPlayers.getPositionTime(0) + "/" + mPlayers.getDurationTime());
        } else if (Setting.isDisplayDuration()) {
            // Hide or clear duration text if controls visible or time invalid
            mBinding.display.duration.setText("");
        }

        // Display Mini Progress Bar
        if (Setting.isDisplayMiniProgress() && !controlsVisible && isValidTime && mPlayers.isVod()) {
            mBinding.display.progress.setProgress((int) (position * 100 / duration));
        } else if (Setting.isDisplayMiniProgress()) {
            // Hide or reset progress bar if controls visible or time invalid
            mBinding.display.progress.setProgress(0);
        }

        // Update overall visibility based on settings and state
        showDisplayInfo();
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
        reverseEpisode(false); // Reverse adapter data
        // Update the text on the button immediately
        if (mArrayAdapter != null && mArrayAdapter.size() > 1) {
            // Assuming the reverse play button is always at index 1 after "Reverse Order"
            mArrayAdapter.notifyArrayItemRangeChanged(1, 1);
        }
         // Refresh the array adapter to show new ranges if needed
         setArrayAdapter(mEpisodeAdapter != null ? mEpisodeAdapter.size() : 0);
    }


    @Override
    public void onRevPlay(TextView view) {
        if (mHistory == null || view == null) return;
        mHistory.setRevPlay(!mHistory.isRevPlay());
        view.setText(mHistory.getRevPlayText()); // Update button text
        Notify.show(mHistory.getRevPlayHint()); // Show hint toast
    }

    private boolean shouldEnterFullscreen(Episode item) {
        if (item == null) return false;
        boolean enter = !isFullscreen() && item.isActivated();
        if (enter) enterFullscreen();
        return enter;
    }

    private void enterFullscreen() {
        mFocus1 = getCurrentFocus(); // Store focus before changing layout
        mBinding.video.requestFocus();
        mBinding.video.setForeground(null); // Remove foreground selector
        mBinding.video.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mBinding.video.setBackgroundColor(android.graphics.Color.BLACK); // Full black background
        mBinding.video.setClipToOutline(false); // Don't clip to outline in fullscreen

        // Ensure flag position is selected for potential later use (though flag list is hidden)
        int flagPos = getFlagPosition();
        if (flagPos >= 0) mBinding.flag.setSelectedPosition(flagPos);

        // Adjust Danmaku size if enabled
        if (mDanmakuContext != null && Setting.getDanmuSize() != 0) { // Null check
            mDanmakuContext.setScaleTextSize(1.2f * Setting.getDanmuSize());
        }

        if (mKeyDown != null) mKeyDown.setFull(true); // Update key handler state
        setFullscreen(true);
        mFocus2 = null; // Reset potential control focus

        hideDetailViews(); // Hide non-video UI elements

        onPlay(); // Ensure playback continues/starts
    }

    private void exitFullscreen() {
        mBinding.video.setForeground(ResUtil.getDrawable(R.drawable.selector_video)); // Restore selector
        mBinding.video.setLayoutParams(mFrameParams); // Restore original layout params
        mBinding.video.setBackgroundResource(R.drawable.rounded_corners); // Restore rounded background

        // Re-apply rounded corners outline
        final float cornerRadius = ResUtil.dp2px(8);
        mBinding.video.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (view != null && outline != null) { // Null checks
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
                }
            }
        });
        mBinding.video.setClipToOutline(true); // Clip to outline again

        // Adjust Danmaku size if enabled
        if (mDanmakuContext != null && Setting.getDanmuSize() != 0) { // Null check
            mDanmakuContext.setScaleTextSize(0.8f * Setting.getDanmuSize());
        }

        // Restore focus
        View focusTarget = getFocus1(); // Get stored focus
        if (focusTarget != null) {
            focusTarget.requestFocus();
        } else {
            mBinding.video.requestFocus(); // Fallback focus
        }

        if (mKeyDown != null) mKeyDown.setFull(false); // Update key handler state
        setFullscreen(false);
        mFocus2 = null; // Reset potential control focus

        showDetailViews(); // Show non-video UI elements again

        hideInfo(); // Hide playback info overlay
    }

    // --- Helper methods to hide/show non-video UI ---
    private void hideDetailViews() {
        // Hide title area (Logo or Text)
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
        mBinding.scroll.setVisibility(View.GONE); // Hide the main scroll view containing lists
    }

    private void showDetailViews() {
        // Restore title area based on logo availability
        if (currentLogoUrl != null) {
            mBinding.logoImageView.setVisibility(View.VISIBLE);
            mBinding.nameTextView.setVisibility(View.GONE);
        } else {
            mBinding.logoImageView.setVisibility(View.GONE);
            mBinding.nameTextView.setVisibility(View.VISIBLE);
        }

        // Restore other details - use setText to handle visibility based on content
        setText(mBinding.remark, 0, Objects.toString(mBinding.remark.getTag(), ""));
        mBinding.row1.setVisibility(View.VISIBLE); // Show the row container
        setText(mBinding.director, 0, Objects.toString(mBinding.director.getTag(), ""));
        setText(mBinding.actor, 0, Objects.toString(mBinding.actor.getTag(), ""));
        setText(mBinding.content, 0, Objects.toString(mBinding.content.getTag(), ""));
        mBinding.row2.setVisibility(View.VISIBLE); // Show the button row container

        // Restore flag list visibility based on adapter content
        mBinding.flag.setVisibility(mFlagAdapter != null && mFlagAdapter.size() > 0 ? View.VISIBLE : View.GONE);

        mBinding.scroll.setVisibility(View.VISIBLE); // Show the main scroll view again
    }
    // --- End Helper methods ---

    private void onDesc() {
        // Ensure content view and its text are not null/empty
        if (mBinding.content == null || TextUtils.isEmpty(mBinding.content.getText())) return;

        CharSequence desc = mBinding.content.getText();
        // Check length after potentially removing prefix "简介："
        // Assuming the prefix is always 3 characters if present
        if (desc.length() > 3) {
             // Check if the prefix actually exists before removing
             String prefix = getString(R.string.detail_content).replace("%s", ""); // Get "简介：" part
             String descString = desc.toString();
             CharSequence textToShow = descString.startsWith(prefix) ? desc.subSequence(prefix.length(), desc.length()) : desc;
             DescDialog.show(this, textToShow);
        } else {
             // If text is too short (e.g., just "简介："), show the original text
             DescDialog.show(this, desc);
        }
    }


    private void onKeep() {
        Keep keep = Keep.find(getHistoryKey());
        Notify.show(keep != null ? R.string.keep_del : R.string.keep_add);
        if (keep != null) {
            keep.delete();
        } else {
            createKeep(); // This will save the new keep item
        }
        RefreshEvent.keep(); // Notify other parts of the app
        checkKeep(); // Update the keep button UI
    }

    private void onVideo() {
        if (!isFullscreen()) enterFullscreen();
    }

    private void onChange() {
        checkSearch(true); // Force search for alternatives
    }

    private void onLoop() {
        boolean currentLoop = mBinding.control.loop.isActivated();
        mBinding.control.loop.setActivated(!currentLoop);
        // Optionally, notify the player about the loop state if it needs it
        // if (mPlayers != null) mPlayers.setLooping(!currentLoop);
    }


    private void onDanmu() {
        boolean currentDanmu = Setting.isDanmu();
        Setting.putDanmu(!currentDanmu);
        mBinding.control.danmu.setActivated(!currentDanmu);
        showDanmu();
    }

    private void showDanmu() {
        if (mBinding.danmaku == null) return; // Null check
        if (Setting.isDanmu() && mBinding.danmaku.isPrepared()) { // Show only if enabled and prepared
             mBinding.danmaku.show();
        } else {
             mBinding.danmaku.hide();
        }
    }

    private void onDanmuAdd() {
        int line = Setting.getDanmuLine(3); // Get current line setting (default 3)
        line = Math.min(line + 1, 15); // Increment, max 15
        Setting.putDanmuLine(line);
        mBinding.control.danmu.setText(line + ResUtil.getString(R.string.lines));
        setDanmuViewSettings(); // Apply new settings
    }

    private void onDanmuSub() {
        int line = Setting.getDanmuLine(3); // Get current line setting
        line = Math.max(line - 1, 1); // Decrement, min 1
        Setting.putDanmuLine(line);
        mBinding.control.danmu.setText(line + ResUtil.getString(R.string.lines));
        setDanmuViewSettings(); // Apply new settings
    }

    private void onEpisodes() {
        Flag currentFlag = getFlag();
        // Ensure flag and its episodes are not null/empty
        if (currentFlag == null || currentFlag.getEpisodes() == null || currentFlag.getEpisodes().isEmpty()) {
             Notify.show(R.string.error_no_episodes); // Inform user
            return;
        }
        EpisodeDialog.create().episodes(currentFlag.getEpisodes()).show(this);
        hideControl(); // Hide controls after opening dialog
    }

    private void checkNext() {
        if (mHistory != null && mHistory.isRevPlay()) onPrev(); // Play previous if reverse play is on
        else onNext();
    }

    private void checkPrev() {
        if (mHistory != null && mHistory.isRevPlay()) onNext(); // Play next if reverse play is on
        else onPrev();
    }

    private void onNext() {
        if (mEpisodeAdapter == null || mEpisodeAdapter.size() == 0) return; // Adapter empty check

        int current = getEpisodePosition();
        int max = mEpisodeAdapter.size() - 1;
        if (current >= max) { // Already at the last episode
             int msgResId1 = (mHistory != null && mHistory.isRevPlay()) ? R.string.error_play_prev_end : R.string.error_play_next_end;
             Notify.show(getString(msgResId1)); // <--- 修改为此行 (假设资源已添加)
            return;
        }

        int nextPos = current + 1;
        Episode item = (Episode) mEpisodeAdapter.get(nextPos);
        if (item != null) {
            setEpisodeActivated(item);
        }
    }

    private void onPrev() {
        if (mEpisodeAdapter == null || mEpisodeAdapter.size() == 0) return; // Adapter empty check

        int current = getEpisodePosition();
        if (current <= 0) { // Already at the first episode
             int msgResId2 = (mHistory != null && mHistory.isRevPlay()) ? R.string.error_play_next_end : R.string.error_play_prev_end;
             Notify.show(getString(msgResId2)); // <--- 修改为此行 (假设资源已添加)            return;
        }

        int prevPos = current - 1;
        Episode item = (Episode) mEpisodeAdapter.get(prevPos);
        if (item != null) {
            setEpisodeActivated(item);
        }
    }

    private void onScale() {
        int currentScale = getScale();
        String[] array = ResUtil.getStringArray(R.array.select_scale);
        int nextScale = (currentScale + 1) % array.length; // Cycle through scales

        if (mHistory != null) mHistory.setScale(nextScale);
        setScale(nextScale); // Apply the new scale
    }

    private void onSpeed() {
        if (mPlayers == null) return; // Null check
        mBinding.control.speed.setText(mPlayers.addSpeed()); // Cycle speed
        if (mHistory != null) mHistory.setSpeed(mPlayers.getSpeed()); // Save speed
    }

    private void onSpeedAdd() {
        if (mPlayers == null) return; // Null check
        mBinding.control.speed.setText(mPlayers.addSpeed(0.25f)); // Increase speed
        if (mHistory != null) mHistory.setSpeed(mPlayers.getSpeed()); // Save speed
    }

    private void onSpeedSub() {
        if (mPlayers == null) return; // Null check
        mBinding.control.speed.setText(mPlayers.subSpeed(0.25f)); // Decrease speed
        if (mHistory != null) mHistory.setSpeed(mPlayers.getSpeed()); // Save speed
    }

    private boolean onSpeedLong() {
        if (mPlayers == null) return false; // Null check
        mBinding.control.speed.setText(mPlayers.toggleSpeed()); // Reset speed to 1.0x
        if (mHistory != null) mHistory.setSpeed(mPlayers.getSpeed()); // Save speed (1.0f)
        return true; // Consume long click
    }

    private void onRefresh() {
        onReset(false); // Reset without forcing position to 0
    }

    private void onReset() {
        onReset(isReplay()); // Reset based on replay setting
    }

    private void onReset(boolean replay) {
        if (mClock != null) mClock.setCallback(null); // Stop time updates during reset

        // Ensure adapters and current selection are valid
        if (mFlagAdapter == null || mFlagAdapter.size() == 0 || mEpisodeAdapter == null || mEpisodeAdapter.size() == 0) {
            Log.w("VideoActivity", "Cannot reset, adapters or items are empty.");
            // Maybe show a notification?
            // Notify.show(R.string.error_play_load);
            return;
        }

        Episode episode = getEpisode();
        Flag flag = getFlag();

        if (flag != null && episode != null) {
            getPlayer(flag, episode, replay); // Get player content for the current episode
        } else {
            Log.w("VideoActivity", "Cannot reset, could not get current flag or episode.");
            // Notify.show(R.string.error_play_load);
        }
    }

    private boolean onResetToggle() {
        int currentReset = Setting.getReset();
        int nextReset = (currentReset + 1) % ResUtil.getStringArray(R.array.select_reset).length; // Cycle through options
        Setting.putReset(nextReset);
        mBinding.control.reset.setText(ResUtil.getStringArray(R.array.select_reset)[nextReset]);
        return true; // Consume long click
    }

    private void onOpening() {
        if (mPlayers == null) return; // Null check
        long current = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        // Set opening only if position is valid and in the first half
        if (current >= 0 && duration > 0 && current <= duration / 2) {
            setOpening(current);
        } else {
            Notify.show(R.string.error_play_op_fail);
        }
    }

    private void onOpeningAdd() {
        if (mHistory == null || mPlayers == null) return; // Null checks
        long duration = mPlayers.getDuration();
        long maxOpening = (duration > 0) ? duration / 2 : 0; // Max opening is half duration
        setOpening(Math.min(mHistory.getOpening() + 1000, maxOpening)); // Add 1s, cap at half duration
    }

    private void onOpeningSub() {
        if (mHistory == null) return; // Null check
        setOpening(Math.max(0, mHistory.getOpening() - 1000)); // Subtract 1s, min 0
    }

    private boolean onOpeningReset() {
        setOpening(0); // Reset opening to 0
        return true; // Consume long click
    }

    private void setOpening(long opening) {
        if (mHistory == null || mPlayers == null) return; // Null checks
        mHistory.setOpening(opening);
        mBinding.control.opening.setText(opening == 0 ? getString(R.string.play_op) : mPlayers.stringToTime(opening));
        // Optionally seek if the current position is now before the new opening
        // if (mPlayers.getPosition() < opening) mPlayers.seekTo(opening);
    }

    private void onEnding() {
        if (mPlayers == null) return; // Null check
        long current = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        // Set ending only if position is valid and in the second half
        if (current >= 0 && duration > 0 && current >= duration / 2) {
            setEnding(duration - current); // Ending is time remaining
        } else {
            Notify.show(R.string.error_play_ed_fail);
        }
    }

    private void onEndingAdd() {
        if (mHistory == null || mPlayers == null) return; // Null checks
        long duration = mPlayers.getDuration();
        long maxEnding = (duration > 0) ? duration / 2 : 0; // Max ending is half duration
        setEnding(Math.min(mHistory.getEnding() + 1000, maxEnding)); // Add 1s, cap at half duration
    }

    private void onEndingSub() {
        if (mHistory == null) return; // Null check
        setEnding(Math.max(0, mHistory.getEnding() - 1000)); // Subtract 1s, min 0
    }

    private boolean onEndingReset() {
        setEnding(0); // Reset ending to 0
        return true; // Consume long click
    }

    private void setEnding(long ending) {
        if (mHistory == null || mPlayers == null) return; // Null checks
        mHistory.setEnding(ending);
        mBinding.control.ending.setText(ending == 0 ? getString(R.string.play_ed) : mPlayers.stringToTime(ending));
    }

    private boolean onChoose() {
        if (mPlayers == null || mPlayers.isEmpty()) return false; // Null checks
        CharSequence title = mBinding.widget.title.getText();
        // Pass title, defaulting to empty string if null
        mPlayers.choose(this, title != null ? title.toString() : "");
        return true; // Consume long click
    }

    private void onPlayer() {
        if (mPlayers == null) return; // Null check
        CharSequence title = mBinding.widget.title.getText();
        PlayerDialog.create()
                    .select(mPlayers.getPlayer())
                    .title(title != null ? title.toString() : "") // Handle null title
                    .show(this);
        hideControl(); // Hide controls after opening dialog
    }

    private void onDecode() {
        onDecode(true); // Toggle decode and save preference
    }

    private void onDecode(boolean save) {
        if (mPlayers == null) return; // Null check
        mPlayers.toggleDecode(save);
        mPlayers.init(getExo(), getIjk()); // Re-init player views if necessary
        mPlayers.setMediaSource(); // Re-apply media source with new decoder
        setDecodeView(); // Update decode button text
    }

    private void onTrack(View view) {
        if (view == null || view.getTag() == null || mPlayers == null) return; // Null checks
        try {
            int trackType = Integer.parseInt(view.getTag().toString());
            TrackDialog.create()
                       .player(mPlayers)
                       .chooser(this)
                       .vod(true)
                       .type(trackType)
                       .show(this);
            hideControl(); // Hide controls after opening dialog
        } catch (NumberFormatException e) {
            Log.e("VideoActivity", "Invalid track type tag: " + view.getTag().toString(), e);
        }
    }

    private void onToggle() {
        if (isVisible(mBinding.control.getRoot())) {
            hideControl();
        } else {
            showControl(getFocus2()); // Show controls, focus last known control element
        }
    }

    private void showProgress() {
        if (mBinding.widget == null) return; // Null check
        mBinding.widget.progress.setVisibility(View.VISIBLE);
        App.post(mR3, 0); // Start traffic update runnable
        hideError(); // Hide error message when showing progress
    }

    private void hideProgress() {
        if (mBinding.widget == null) return; // Null check
        mBinding.widget.progress.setVisibility(View.GONE);
        App.removeCallbacks(mR3); // Stop traffic update runnable
        Traffic.reset(); // Reset traffic counter
    }

    private void showError(String text) {
        if (mBinding.widget == null) return; // Null check
        mBinding.widget.error.setVisibility(View.VISIBLE);
        mBinding.widget.text.setText(Objects.toString(text,"")); // Show error text, handle null
        hideProgress(); // Hide progress indicator when showing error
    }

    private void hideError() {
        if (mBinding.widget == null) return; // Null check
        mBinding.widget.error.setVisibility(View.GONE);
        mBinding.widget.text.setText(""); // Clear error text
    }

    private void showInfo() {
        if (mBinding.widget == null) return; // Null check
        mBinding.widget.info.setVisibility(View.VISIBLE);
        showDisplayInfo(); // Update display elements based on info visibility
    }

    private void hideInfo() {
        if (mBinding.widget == null) return; // Null check
        mBinding.widget.info.setVisibility(View.GONE);
        showDisplayInfo(); // Update display elements based on info visibility
    }

    private void showInfoAndCenter() {
        showInfo(); // Show top info bar
        if (mBinding.widget != null) { // Null check
             mBinding.widget.center.setVisibility(View.VISIBLE); // Show center action icon (play/pause/seek)
        }
    }

    private void hideInfoAndCenter() {
        hideInfo(); // Hide top info bar
        if (mBinding.widget != null) { // Null check
            mBinding.widget.center.setVisibility(View.GONE); // Hide center action icon
        }
    }

    private void setControlNextFocus() {
        // Ensure the layout exists
        ViewGroup actionLayout = mBinding.control.actionLayout;
        if (actionLayout == null) return;

        int count = actionLayout.getChildCount();
        View lastVisibleEnabled = null; // Track the previous visible and enabled button

        // Find the first visible and enabled button from the left
        View firstVisibleEnabled = null;
        for (int i = 0; i < count; i++) {
            View btn = actionLayout.getChildAt(i);
            if (btn != null && isVisible(btn) && btn.isEnabled()) {
                firstVisibleEnabled = btn;
                break;
            }
        }

        // Iterate through buttons to set focus links
        for (int i = 0; i < count; i++) {
            View btn = actionLayout.getChildAt(i);
            if (btn == null || !isVisible(btn) || !btn.isEnabled()) continue; // Skip hidden/disabled

            // Set next focus left
            if (lastVisibleEnabled != null) {
                btn.setNextFocusLeftId(lastVisibleEnabled.getId());
            } else {
                // If this is the first visible button, make left wrap to itself or disable?
                 btn.setNextFocusLeftId(btn.getId()); // Wrap to self
            }

            // Find next visible and enabled button to the right
            View nextVisibleEnabled = null;
            for (int j = i + 1; j < count; j++) {
                View next = actionLayout.getChildAt(j);
                if (next != null && isVisible(next) && next.isEnabled()) {
                    nextVisibleEnabled = next;
                    break;
                }
            }

            // Set next focus right
            if (nextVisibleEnabled != null) {
                btn.setNextFocusRightId(nextVisibleEnabled.getId());
            } else {
                // If this is the last visible button, make right wrap to the first visible one
                 if (firstVisibleEnabled != null) {
                     btn.setNextFocusRightId(firstVisibleEnabled.getId());
                 } else {
                     btn.setNextFocusRightId(btn.getId()); // Wrap to self if only one button
                 }
            }

            lastVisibleEnabled = btn; // Update the last seen visible/enabled button
        }
    }


    private void showControl(View view) {
        // Ensure root control view exists
        if (mBinding.control == null || mBinding.control.getRoot() == null) return;

        // Determine visibility of Danmu button
        boolean danmuVisible = (mBinding.danmaku != null && mBinding.danmaku.isPrepared());
        mBinding.control.danmu.setVisibility(danmuVisible ? View.VISIBLE : View.GONE);

        // Show Episodes button based on setting
        mBinding.control.episodes.setVisibility(Setting.getFullscreenMenuKey() == 0 ? View.VISIBLE : View.GONE);

        // Make the control layout visible
        mBinding.control.getRoot().setVisibility(View.VISIBLE);

        // Attempt to set focus
        View targetFocus = view;
        if (targetFocus == null) targetFocus = getFocus2(); // Try stored focus
        if (targetFocus == null || !targetFocus.isFocusable()) targetFocus = mBinding.control.next; // Fallback to 'next'
        if (targetFocus != null && targetFocus.isFocusable()) {
             targetFocus.requestFocus();
        } else {
             // If even 'next' isn't focusable, try finding the first focusable child
             View firstFocusable = mBinding.control.getRoot().findFocus();
             if(firstFocusable != null) firstFocusable.requestFocus();
        }


        setControlNextFocus(); // Setup horizontal navigation
        setR1Callback(); // Set timer to hide controls automatically
    }


    private void hideControl() {
        hideControl(true); // Hide controls and associated info overlay
    }

    private void hideControl(boolean hideInfo) {
        // Ensure root control view exists
        if (mBinding.control == null || mBinding.control.getRoot() == null) return;

        if (hideInfo) hideInfo(); // Hide info overlay if requested

        // Reset track button text (optional, but good practice)
        mBinding.control.text.setText(R.string.play_track_text);

        // Hide the control layout
        mBinding.control.getRoot().setVisibility(View.GONE);

        // Remove the auto-hide timer callback
        App.removeCallbacks(mR1);
    }

    private void hideCenter() {
        if (mBinding.widget == null) return; // Null check
        mBinding.widget.action.setImageResource(R.drawable.ic_widget_play); // Set icon to play (assuming hidden means paused state ended)
        mBinding.widget.center.setVisibility(View.GONE); // Hide center icon container
    }

    private void showPreview(Drawable preview) {
        // Ensure preview widget and setting allow showing preview
        if (preview == null || Setting.getFlag() == 0 || mBinding.widget == null || isGone(mBinding.widget.preview)) return;

        mBinding.widget.preview.setVisibility(View.VISIBLE);
        mBinding.widget.preview.setImageDrawable(preview);
    }

    private void hidePreview() {
        if (mBinding.widget == null || mBinding.widget.preview == null) return; // Null checks
        mBinding.widget.preview.setVisibility(View.GONE);
        mBinding.widget.preview.setImageDrawable(null); // Clear drawable to free memory
    }

    private void setTraffic() {
        // Ensure traffic view exists
        if (mBinding.widget != null && mBinding.widget.traffic != null) {
            Traffic.setSpeed(mBinding.widget.traffic);
            App.post(mR3, Constant.INTERVAL_TRAFFIC); // Schedule next update
        }
    }

    private void setR1Callback() {
        // Remove any existing callback first
        App.removeCallbacks(mR1);
        // Post the new callback
        App.post(mR1, Constant.INTERVAL_HIDE);
    }

    private void setR2Callback(long delayMillis) {
        // Remove any existing callback first
        App.removeCallbacks(mR2);
        // Post the new callback
        App.post(mR2, delayMillis);
    }

    private void setArtwork(String url) {
        // Check context validity before starting Glide request
        //if (!ImgUtil.isValid(this)) return;

        ImgUtil.load(url, R.drawable.radio, new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                // Check context validity again inside the callback
                //if (!ImgUtil.isValid(VideoActivity.this)) return;
                // Set artwork only if views are not null
                if (getExo() != null) getExo().setDefaultArtwork(resource);
                if (getIjk() != null) getIjk().setDefaultArtwork(resource);
                showPreview(resource); // Show preview if applicable
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                // Check context validity
                //if (!ImgUtil.isValid(VideoActivity.this)) return;
                // Don't set error drawable as default artwork, just hide preview
                hidePreview();
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                // Optional: Can clear artwork or set placeholder if needed when load is cleared
            }
        });
    }


    private void getPart(String source) {
        if (TextUtils.isEmpty(source)) return; // Add null/empty check
        try {
            String encodedSource = URLEncoder.encode(source.trim(), "UTF-8");
            // Ensure OkHttp and the URL are valid
            String url = "https://api.yesapi.cn/?service=App.Scws.GetWords&app_key=CEE4B8A091578B252AC4C92FB4E893C3&text=" + encodedSource;

            OkHttp.newCall(url).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    List<String> items = new ArrayList<>(); // Initialize list
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                             items = Part.get(response.body().string()); // Assuming Part.get handles parsing
                        } catch(Exception e) {
                            Log.e("VideoActivity", "Error parsing part response", e);
                            // Fallback to source if parsing fails
                            items = new ArrayList<>(Collections.singletonList(source));
                        } finally {
                            response.close(); // Ensure response body is closed
                        }
                    } else {
                         // Fallback to source on network failure
                         Log.w("VideoActivity", "Failed to get parts, response code: " + response.code());
                         items = new ArrayList<>(Collections.singletonList(source));
                         response.close(); // Ensure response body is closed even on failure
                    }

                    // Ensure the original source term is included, preferably at the beginning
                    if (!items.contains(source)) {
                        items.add(0, source);
                    }
                    final List<String> finalItems = items; // Final variable for lambda/post
                    // Post update to UI thread
                    App.post(() -> setPartAdapter(finalItems)); // Removed delay
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                     Log.e("VideoActivity", "Failed to get parts network request", e);
                    List<String> items = Collections.singletonList(source);
                    // Post update to UI thread
                    App.post(() -> setPartAdapter(items)); // Removed delay
                }
            });
        } catch (Exception e){
             Log.e("VideoActivity", "Error encoding source or creating call for getPart", e);
             List<String> items = Collections.singletonList(source);
             App.post(() -> setPartAdapter(items)); // Fallback on error
        }
    }

    private void setPartAdapter(List<String> items) {
        // Ensure adapter and view are not null
        if (mPartAdapter == null || mBinding.part == null) return;

        if (items == null || items.isEmpty()) {
            mBinding.part.setVisibility(View.GONE);
            mPartAdapter.clear(); // Clear adapter if no items
        } else {
            mBinding.part.setVisibility(View.VISIBLE);
            mPartAdapter.setItems(items, null);
            setR2Callback(100); // Schedule focus update check
        }
    }


    private void checkFlag(Vod item) {
        if (item == null || mFlagAdapter == null) { // Null checks for item and adapter
             if (mBinding.flag != null) mBinding.flag.setVisibility(View.GONE); // Hide flag view if item is null
             ErrorEvent.flag(); // Send error event
             return;
        }

        List<Flag> flags = item.getVodFlags();
        boolean empty = (flags == null || flags.isEmpty());

        mBinding.flag.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (empty) {
            ErrorEvent.flag(); // Send error event if no flags
        } else {
            Flag flagToActivate = null;
            if (mHistory != null) { // History exists, try to restore previous flag
                 flagToActivate = mHistory.getFlag(); // This might return null if history flag doesn't match current flags
                 // Verify the flag from history actually exists in the current item's flags
                 boolean historyFlagExists = false;
                 if (flagToActivate != null) {
                     for (Flag currentFlag : flags) {
                         if (currentFlag != null && currentFlag.getFlag().equals(flagToActivate.getFlag())) {
                             historyFlagExists = true;
                             flagToActivate = currentFlag; // Use the instance from the current item
                             break;
                         }
                     }
                 }
                 if (!historyFlagExists) {
                     flagToActivate = null; // Reset if history flag not found
                 }

                 // Handle reverse sort from history
                 if (mHistory.isRevSort()) {
                     reverseEpisode(true); // Reverse episodes and scroll to position
                 }
            }

            // If no valid flag from history, activate the first flag from the item
            if (flagToActivate == null && !flags.isEmpty()) {
                 flagToActivate = flags.get(0);
            }

            // Activate the determined flag
            if (flagToActivate != null) {
                 setFlagActivated(flagToActivate);
            } else {
                 // Should not happen if flags list is not empty, but handle defensively
                 ErrorEvent.flag();
                 Log.w("VideoActivity", "Could not determine flag to activate.");
            }
        }
    }


    // --- Modified checkHistory ---
    private void checkHistory(Vod item) {
        if (item == null) {
            Log.w("VideoActivity", "checkHistory called with null item.");
            // Initialize player with defaults if no history can be created
             if (mPlayers != null) mPlayers.setPlayer(Setting.getPlayer());
             setScale(Setting.getScale());
             setPlayerView();
             setDecodeView();
             mBinding.control.opening.setText(getString(R.string.play_op));
             mBinding.control.ending.setText(getString(R.string.play_ed));
            return;
        }

        mHistory = History.find(getHistoryKey());
        if (mHistory == null) {
            mHistory = createHistory(item); // createHistory uses item.getVodName()
        } else {
            // Ensure History has the correct (original) VodName from the item
            // This handles cases where the name might have changed in the source
            if (!item.getVodName().equals(mHistory.getVodName())) {
                mHistory.setVodName(item.getVodName());
                // Optionally save the updated name immediately
                // if (!Setting.isIncognito()) App.execute(() -> mHistory.save());
            }
             // Update Pic URL from the latest detail item, considering user-provided pic
             mHistory.setVodPic(item.getVodPic(getPic()));
        }


        if (mHistory != null) {
            // Apply mark from intent if present
            String mark = getMark();
            if (!TextUtils.isEmpty(mark)) {
                mHistory.setVodRemarks(mark);
            }

            // Handle Incognito mode
            if (Setting.isIncognito() && mHistory.getKey().equals(getHistoryKey())) {
                mHistory.delete(); // Delete history if in incognito mode
                mHistory = createHistory(item); // Recreate a temporary history for this session
                 if (mHistory == null) { // Handle creation failure
                     Log.e("VideoActivity", "Failed to recreate temporary history in incognito mode.");
                     return; // Cannot proceed without history object
                 }
            }

            // Set UI elements based on history
            mBinding.control.opening.setText(mHistory.getOpening() == 0 ? getString(R.string.play_op) : mPlayers.stringToTime(mHistory.getOpening()));
            mBinding.control.ending.setText(mHistory.getEnding() == 0 ? getString(R.string.play_ed) : mPlayers.stringToTime(mHistory.getEnding()));

            // Set player, scale, and related views
            if (mPlayers != null) mPlayers.setPlayer(getPlayer()); // getPlayer handles history priority
            setScale(getScale()); // getScale handles history priority
            setPlayerView();
            setDecodeView();

        } else {
            // This case should ideally not be reached if createHistory is robust
            Log.e("VideoActivity", "History is null even after attempting creation.");
            // Set defaults as a last resort
            if (mPlayers != null) mPlayers.setPlayer(Setting.getPlayer());
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

        History history = new History();
        history.setKey(getHistoryKey());
        history.setCid(VodConfig.getCid());
        history.setVodName(item.getVodName()); // Use original name from VOD item
        history.setVodPic(item.getVodPic(getPic())); // Use pic from item, considering intent fallback
        history.findEpisode(item.getVodFlags()); // Sets initial episode/flag/remark
        history.setSpeed(Setting.getPlaySpeed()); // Set default speed
        history.setScale(Setting.getScale());     // Set default scale
        history.setPlayer(Setting.getPlayer());   // Set default player
        // Opening and Ending default to 0
        history.setCreateTime(System.currentTimeMillis()); // Set creation time
        return history;
    }
    // --- End Modified createHistory ---

    private void updateHistory(Episode item, boolean replay) {
        // Ensure history, item, and flag are valid
        if (mHistory == null || item == null) return;
        Flag flag = getFlag(); // Get current flag
        if (flag == null) return;


        // Determine if playback should restart from beginning
        boolean shouldReplay = replay || !item.equals(mHistory.getEpisode());
        long position = shouldReplay ? 0 : mHistory.getPosition(); // Use 0 if replaying or new episode

        // Update history fields
        mHistory.setPosition(position);
        mHistory.setEpisodeUrl(item.getUrl());
        mHistory.setVodRemarks(item.getName()); // Episode name as remark
        mHistory.setVodFlag(flag.getFlag());    // Current flag identifier
        mHistory.setCreateTime(System.currentTimeMillis()); // Update timestamp

        // Update player start position, considering opening time
        if (mPlayers != null) { // Null check
             long startPosition = Math.max(mHistory.getOpening(), position);
             // Ensure start position doesn't exceed duration if known (though duration might not be set yet)
             // long duration = mHistory.getDuration();
             // if (duration > 0 && startPosition >= duration) {
             //     startPosition = Math.max(0, duration - 1000); // Start near the end if position is invalid
             // }
            mPlayers.setPosition(startPosition);
        }

        // Save history immediately unless in incognito mode
        if (!Setting.isIncognito()) {
            App.execute(() -> {
                 if (mHistory != null) mHistory.save(); // Save changes to DB
            });
        }
    }


    private void checkKeep() {
        // Ensure keep button exists
        if (mBinding.keep == null) return;
        Keep keep = Keep.find(getHistoryKey());
        int iconRes = (keep == null) ? R.drawable.ic_detail_keep_off : R.drawable.ic_detail_keep_on;
        mBinding.keep.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        // Optionally update text if needed (e.g., "收藏" / "已收藏")
        // mBinding.keep.setText(keep == null ? R.string.keep_add : R.string.keep_already_added);
    }


    // --- Modified createKeep ---
    private void createKeep() {
        // Ensure necessary info is available
        Site site = getSite();
        if (TextUtils.isEmpty(currentVodName) || TextUtils.isEmpty(getHistoryKey())) {
            Log.w("VideoActivity", "Cannot create keep, missing VOD name or history key.");
            Notify.show(R.string.error_keep_add_fail); // Inform user
            return;
        }

        Keep keep = new Keep();
        keep.setKey(getHistoryKey()); // Unique key based on site, ID, and CID
        keep.setCid(VodConfig.getCid());
        keep.setSiteName(site != null ? site.getName() : ""); // Site name, handle null site
        Object tag = mBinding.video.getTag(); // Get pic URL from video view tag if set
        keep.setVodPic(tag instanceof String ? (String) tag : getPic()); // Use tag or intent pic
        keep.setVodName(currentVodName); // Use stored original VOD name
        keep.setCreateTime(System.currentTimeMillis());
        keep.save(); // Save to database
    }
    // --- End Modified createKeep ---


    @Override
    public void showChooser(TrackDialog dialog) {
        if (dialog == null || mPlayers == null) return; // Add null checks
        FileChooserDialog.create().player(mPlayers).trackDialog(dialog).show(this);
    }

    @Override
    public void onTrackClick(Track item) {
        if (item == null) return; // Add null check
        item.setKey(getHistoryKey()); // Associate track with this history item
        item.save(); // Save track selection to DB
        // Player side applies the track immediately, DB save is for persistence
    }

    @Override
    public void onSubtitleClick() {
        // Hide controls after a short delay
        App.post(this::hideControl, 200);

        // Ensure player and subtitle view exist
        if (mPlayers == null) return;
        SubtitleView subtitleView = mPlayers.isIjk() ? getIjk().getSubtitleView() : getExo().getSubtitleView();

        if (subtitleView != null) {
            // Show dialog after a small delay to allow controls to hide smoothly
            App.post(() -> SubtitleDialog.create().view(subtitleView).full(isFullscreen()).show(this), 200);
        } else {
            Log.w("VideoActivity", "SubtitleView is null, cannot open dialog.");
            // Optionally notify user?
            // Notify.show("Subtitle adjustment not available.");
        }
    }

    @Override
    public void onTimeChanged() {
        onTimeChangeDisplaySpeed(); // Update speed/duration display

        // Ensure history and player are available
        if (mHistory == null || mPlayers == null) return;

        long position = mPlayers.getPosition();
        long duration = mPlayers.getDuration();

        // Update history only if time is valid and not in incognito
        if (position >= 0 && duration > 0 && !Setting.isIncognito()) {
            mHistory.setPosition(position);
            mHistory.setDuration(duration);
            // Update database in background
            App.execute(() -> {
                // Re-check history validity in background thread
                if (mHistory != null && mHistory.getKey().equals(getHistoryKey())) {
                    mHistory.update(); // Use update for existing record
                }
            });
        }

        // Check for ending time skip if ending is set and duration is valid
        if (mHistory.getEnding() > 0 && duration > 0 && (position + mHistory.getEnding() >= duration)) {
             if (position < duration) { // Ensure we don't trigger multiple times if already past duration
                 Log.d("VideoActivity", "Ending time reached, checking next episode.");
                 if (mClock != null) mClock.setCallback(null); // Stop clock updates during transition
                 checkNext(); // Proceed to next episode
             }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActionEvent(ActionEvent event) {
        if (event == null || isBackground()) return; // Add null check
        if (ActionEvent.PLAY.equals(event.getAction()) || ActionEvent.PAUSE.equals(event.getAction())) {
            onKeyCenter();
        } else if (ActionEvent.NEXT.equals(event.getAction())) {
            mBinding.control.next.performClick();
        } else if (ActionEvent.PREV.equals(event.getAction())) {
            mBinding.control.prev.performClick();
        } else if (ActionEvent.STOP.equals(event.getAction())) {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (event == null || event.getType() == null || isBackground()) return; // Null checks and background check

        switch (event.getType()) {
            case DETAIL:
                getDetail(); // Refresh video details
                break;
            case PLAYER:
                onRefresh(); // Refresh player (reload current source)
                break;
            case DANMAKU:
                checkDanmu(event.getPath()); // Load new Danmaku file/URL
                break;
            case SUBTITLE:
                if (mPlayers != null) { // Null check
                    mPlayers.setSub(Sub.from(event.getPath())); // Load new subtitle file/URL
                }
                break;
             default:
                 // Optional: Log unknown refresh type
                 // Log.w("VideoActivity", "Unknown refresh event type: " + event.getType());
                 break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerEvent(PlayerEvent event) {
        if (event == null || isBackground() || mPlayers == null) return; // Null checks and background check

        switch (event.getState()) {
            case 0: // Assuming 0 represents a custom loading/preparing state before STATE_BUFFERING
                setInitTrack(true); // Ready to set default tracks when player is ready
                setTrackVisible(false); // Hide track buttons during load
                if (mClock != null) mClock.setCallback(this); // Start updating time display
                break;
            case Player.STATE_IDLE:
                // Player is stopped or hasn't been prepared yet.
                // Maybe hide progress, show preview?
                hideProgress();
                break;
            case Player.STATE_BUFFERING:
                showProgress(); // Show buffering indicator
                break;
            case Player.STATE_READY:
                stopSearch(); // Stop background site search if playback starts successfully
                setMetadata(); // Update media session metadata
                resetToggle(); // Reset player toggle counter
                resetError(); // Reset error counter
                hideProgress(); // Hide buffering indicator
                mPlayers.reset(); // Reset internal player flags (like retry count) after successful playback
                setDefaultTrack(); // Apply default/saved tracks
                setTrackVisible(true); // Show track buttons if tracks are available

                // Save the currently used player type to history
                if (mHistory != null) {
                    mHistory.setPlayer(mPlayers.getPlayer());
                    // Optionally save history immediately
                    // if (!Setting.isIncognito()) App.execute(() -> mHistory.save());
                }

                // Update displayed video size
                String sizeText = mPlayers.getSizeText();
                 if (mBinding.widget != null) mBinding.widget.size.setText(sizeText);
                 if (mBinding.display != null) mBinding.display.size.setText(sizeText);
                break;
            case Player.STATE_ENDED:
                checkEnded(); // Handle playback completion (loop or next)
                break;
        }
    }


    private void checkEnded() {
        // Ensure control view is available
        if (mBinding.control == null) return;

        if (mBinding.control.loop.isActivated()) {
            onReset(true); // Replay the current video from the beginning
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Allow screen to turn off
            checkNext(); // Proceed to the next episode/item
        }
    }

    private void setTrackVisible(boolean visible) {
        // Ensure control view and player exist
        if (mBinding.control == null || mPlayers == null) return;

        // Text Track Button Visibility
        boolean hasTextTrack = mPlayers.haveTrack(C.TRACK_TYPE_TEXT);
        // Show if tracks exist OR if using ExoPlayer (which might load external subs later)
        mBinding.control.text.setVisibility(visible && (hasTextTrack || mPlayers.isExo()) ? View.VISIBLE : View.GONE);

        // Audio Track Button Visibility
        boolean hasAudioTrack = mPlayers.haveTrack(C.TRACK_TYPE_AUDIO);
        mBinding.control.audio.setVisibility(visible && hasAudioTrack ? View.VISIBLE : View.GONE);

        // Video Track Button Visibility (Less common, but possible)
        boolean hasVideoTrack = mPlayers.haveTrack(C.TRACK_TYPE_VIDEO);
        mBinding.control.video.setVisibility(visible && hasVideoTrack ? View.VISIBLE : View.GONE);
    }


    private void setDefaultTrack() {
        // Ensure player exists
        if (mPlayers == null) return;

        if (isInitTrack()) { // Only set default tracks once per source load
            setInitTrack(false); // Mark default tracks as applied
            mPlayers.prepared(); // Notify player it's ready (might trigger internal actions)
            // Find and apply saved tracks from database
            mPlayers.setTrack(Track.find(getHistoryKey()));
        }
    }

    // --- Modified setMetadata ---
    private void setMetadata() {
        // Ensure player exists
        if (mPlayers == null) return;

        // Determine Title: Use history name if available, fallback to stored current name
        String title = (mHistory != null && !TextUtils.isEmpty(mHistory.getVodName()))
                       ? mHistory.getVodName()
                       : currentVodName;

        // Determine Artist (Episode Name): Get current episode name, handle null
        Episode episode = getEpisode();
        String episodeName = (episode != null) ? Objects.toString(episode.getName(),"") : "";
        // Show episode name as artist only if it's different from the main title
        String artist = (!TextUtils.isEmpty(title) && title.equals(episodeName))
                        ? "" // Avoid redundant title/episode name
                        : getString(R.string.play_now, episodeName); // Format as "正在播放：xxx"

        // Determine Picture URL: Use history pic if available, fallback to intent pic
        String pic = (mHistory != null && !TextUtils.isEmpty(mHistory.getVodPic()))
                     ? mHistory.getVodPic()
                     : getPic();

        // Set metadata on the player
        mPlayers.setMetadata(title, artist, pic, getDefaultArtwork());
    }
    // --- End Modified setMetadata ---


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(ErrorEvent event) {
        if (event == null || isBackground() || mPlayers == null) return; // Null checks and background check

        if (addErrorCount() > 20) { // Too many consecutive errors
            onErrorEnd(event); // Give up and show final error
        } else if (mPlayers.addRetry() > event.getRetry()) { // Player internal retry limit exceeded
            checkError(event); // Try higher-level error handling (toggle player, next source, etc.)
        } else if (event.isDecode() && mPlayers.canToggleDecode()) { // Specific decode error and can toggle
            onDecode(false); // Try toggling decoder without saving preference yet
        } else if (event.isExo() && mPlayers.isExo()) { // Specific ExoPlayer error
            onExoCheck(event); // Handle ExoPlayer specific error codes
        } else {
            // General error or retry limit not reached, just refresh the current source
            onRefresh();
        }
    }


    private void onExoCheck(ErrorEvent event) {
        if (event == null || mPlayers == null) return; // Null checks

        int code = event.getCode();
        // Check for specific ExoPlayer I/O or Parsing errors
        if (code == PlaybackException.ERROR_CODE_IO_UNSPECIFIED ||
           (code >= PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED &&
            code <= PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED)) {
            // If it's a known parsing/container issue, try setting the format hint
            mPlayers.setFormat(ExoUtil.getMimeType(code));
        }
        // Re-apply the media source, potentially with the new format hint
        mPlayers.setMediaSource();
    }

    private void checkError(ErrorEvent event) {
        if (event == null) return; // Null check

        Site site = getSite();
        // Try switching player type if:
        // 1. Site allows player switching (playerType == -1)
        // 2. Error is URL related (likely network or source issue)
        // 3. Player has retried at least once (event.getRetry() > 0)
        // 4. We haven't already toggled players too many times (toggleCount < 2)
        // 5. Current player is not the System player (SYS usually has fewer issues or is last resort)
        if (site != null && site.getPlayerType() == -1 && event.isUrl() && event.getRetry() > 0 && getToggleCount() < 2 && mPlayers.getPlayer() != Players.SYS) {
            toggleCount++;
            Log.d("VideoActivity", "Toggling player due to error (attempt " + toggleCount + ")");
            nextPlayer(); // Switch to the next player type
        } else {
            resetToggle(); // Reset toggle count if not switching player
            onError(event); // Proceed to next level of error handling (try parse/flag/site)
        }
    }

    private void nextPlayer() {
        if (mPlayers == null) return; // Null check
        mPlayers.nextPlayer(); // Cycle to next player implementation
        setPlayerView(); // Update player button text
        setDecodeView(); // Update decode button text
        onRefresh(); // Reload source with the new player
    }

    private void onErrorEnd(ErrorEvent event) {
        if (event == null) return; // Null check
        Log.e("VideoActivity", "Max error count reached. Stopping playback attempts.");
        onErrorPlayer(event); // Show error UI and stop player
        resetError(); // Reset error count
        // Optionally, could finish the activity here: finish();
    }

    private void onErrorPlayer(ErrorEvent event) {
        if (event == null || mPlayers == null) return; // Null checks
        Track.delete(getHistoryKey()); // Clear saved tracks for this item on error
        showError(event.getMsg()); // Display error message
        if (mClock != null) mClock.setCallback(null); // Stop clock updates
        mPlayers.reset(); // Reset player state
        mPlayers.stop(); // Stop playback
    }

    private void onError(ErrorEvent event) {
        if (event == null) return; // Null check
        Log.w("VideoActivity", "Playback error occurred: " + event.getMsg());
        onErrorPlayer(event); // Stop player and show error UI
        startFlow(); // Attempt to find alternative source/parse/site
    }

    private void startFlow() {
        Site site = getSite();
        // Only proceed if site allows switching sources
        if (site == null || !site.isChangeable()) {
            Log.d("VideoActivity", "Site does not allow changing, stopping error flow.");
            return;
        }

        if (isUseParse()) {
            checkParse(); // Try next parser first if using parse
        } else {
            checkFlag(); // Otherwise, try next playback flag (source)
        }
    }

    private void checkParse() {
        // Ensure adapter exists
        if (mParseAdapter == null) return;

        int position = getParsePosition();
        boolean isLast = position >= mParseAdapter.size() - 1; // Check if current is last or beyond

        if (isLast) {
             // If already tried the last parser, reset to the first one
             // and then proceed to check flags/sites.
             if (mParseAdapter.size() > 0) initParse(); // Reset to first parser
             checkFlag(); // Try next flag/site
        } else {
            // Try the next available parser
            nextParse(position);
        }
    }


    private void initParse() {
        // Ensure adapter is not null or empty
        if (mParseAdapter == null || mParseAdapter.size() == 0) return;
        Parse firstParse = (Parse) mParseAdapter.get(0);
        if (firstParse != null) { // Null check for the item
            VodConfig.get().setParse(firstParse); // Set global config
             // Update UI selection
             for(int i=0; i<mParseAdapter.size(); ++i) {
                 Parse p = (Parse) mParseAdapter.get(i);
                 if(p != null) p.setActivated(i == 0);
             }
            notifyItemChanged(mBinding.control.parse, mParseAdapter);
        }
    }

    private void checkFlag() {
        // Ensure adapter exists
        if (mFlagAdapter == null) {
             checkSearch(false); // No flags to check, proceed to search
             return;
        }

        int position = getFlagPosition();
        boolean isLast = position < 0 || position >= mFlagAdapter.size() - 1; // Invalid position or last flag

        if (isLast) {
            checkSearch(false); // Tried all flags, proceed to search other sites
        } else {
            nextFlag(position); // Try the next flag
        }
    }

    // --- Modified checkSearch ---
    private void checkSearch(boolean force) {
        // Ensure quick adapter exists
        if (mQuickAdapter == null) {
             Log.e("VideoActivity", "QuickAdapter is null, cannot perform search.");
             showEmpty(); // Show empty state as search isn't possible
             return;
        }

        // Use stored original VOD name for initial search keyword
        if (mQuickAdapter.size() == 0) { // No search results yet
            // Make sure currentVodName is initialized before calling initSearch
            if (TextUtils.isEmpty(currentVodName)) {
                // Try getting from intent again if detail loading failed initially
                currentVodName = getName();
            }
            // Only start search if we have a name
            if (!TextUtils.isEmpty(currentVodName)) {
                Log.d("VideoActivity", "Initiating search for: " + currentVodName);
                initSearch(currentVodName, true); // Start initial search in auto mode
            } else {
                Log.w("VideoActivity", "Cannot start search, currentVodName is empty.");
                showEmpty(); // Show empty state if no name available
            }
        } else if (isAutoMode() || force) { // Already have results, proceed if in auto mode or forced
             Log.d("VideoActivity", "Proceeding to next site in search results.");
             nextSite();
        } else {
             Log.d("VideoActivity", "Search check: Not in auto mode or forced, not starting new search.");
             // Do nothing if not auto mode and not forced, user must manually select from quick results
        }
    }
    // --- End Modified checkSearch ---

    private void initSearch(String keyword, boolean auto) {
        if (TextUtils.isEmpty(keyword)) {
            Log.w("VideoActivity", "initSearch called with empty keyword.");
            return;
        }
        stopSearch(); // Stop any previous search executor
        setAutoMode(auto);
        setInitAuto(auto); // Flag indicating this is the initial auto search trigger
        startSearch(keyword);
        if (mBinding.part != null) mBinding.part.setTag(keyword); // Store keyword for mismatch check
        // Optionally show a "Searching..." indicator?
    }


    private boolean isPass(Site item) {
        if (item == null) return false;
        // If in auto mode, only consider sites marked as changeable (usually means reliable/preferred)
        if (isAutoMode() && !item.isChangeable()) return false;
        // Always check if the site is searchable
        return item.isSearchable();
    }

    private void startSearch(String keyword) {
        if (TextUtils.isEmpty(keyword) || VodConfig.get() == null) return; // Null checks

        // Ensure quick adapter and view are ready
        if (mQuickAdapter == null || mBinding.quick == null) return;
        mQuickAdapter.clear(); // Clear previous quick search results
        mBinding.quick.setVisibility(View.GONE); // Hide quick results initially

        List<Site> sitesToSearch = new ArrayList<>();
        List<Site> allSites = VodConfig.get().getSites();
        if (allSites != null) { // Null check for site list
            for (Site site : allSites) {
                if (isPass(site)) { // Check if site should be included in search
                    sitesToSearch.add(site);
                }
            }
        }

        if (sitesToSearch.isEmpty()) {
            Log.w("VideoActivity", "No searchable sites found to start search.");
            // Optionally show a message?
            return;
        }

        // Start thread pool for searching
        mExecutor = Executors.newFixedThreadPool(Constant.THREAD_POOL);
        Log.d("VideoActivity", "Starting search for '" + keyword + "' on " + sitesToSearch.size() + " sites.");
        for (Site site : sitesToSearch) {
            mExecutor.execute(() -> search(site, keyword));
        }
        // Show quick results view container, even if empty initially
        mBinding.quick.setVisibility(View.VISIBLE);
    }

    private void stopSearch() {
        if (mExecutor != null && !mExecutor.isShutdown()) { // Check if executor exists and is running
            try {
                mExecutor.shutdownNow(); // Attempt to stop all executing tasks
                Log.d("VideoActivity", "Search executor shut down.");
            } catch (Exception e) {
                Log.e("VideoActivity", "Error shutting down search executor", e);
            }
        }
        mExecutor = null; // Set to null after shutdown attempt
    }

    private void search(Site site, String keyword) {
        // Ensure ViewModel, site, and keyword are valid
        if (mViewModel == null || site == null || TextUtils.isEmpty(keyword)) return;
        try {
            // Check if the executor has been shut down before making the call
            if (mExecutor == null || mExecutor.isShutdown()) {
                Log.w("VideoActivity", "Search executor is null or shut down, skipping search for site: " + site.getName());
                return;
            }
            mViewModel.searchContent(site, keyword.trim(), true); // Trim keyword whitespace
        } catch (Throwable e) { // Catch potential exceptions during search call setup
             Log.w("VideoActivity", "Search ignored or failed for site: " + site.getName() + ", keyword: " + keyword, e);
        }
    }

    private void setSearch(Result result) {
        // Ensure result, list, adapter, and view are valid
        if (result == null || result.getList() == null || mQuickAdapter == null || mBinding.quick == null) return;
        // Check if search executor is still active; if not, results might be stale
        if (mExecutor == null) {
             Log.w("VideoActivity", "Search executor is null, discarding potentially stale search results.");
             return;
        }


        List<Vod> items = result.getList();
        // Filter out null items and mismatches
        items.removeIf(Objects::isNull); // Remove nulls first
        items.removeIf(this::mismatch); // Then remove mismatches

        if (!items.isEmpty()) {
            mQuickAdapter.addAll(mQuickAdapter.size(), items); // Add filtered items
            mBinding.quick.setVisibility(View.VISIBLE); // Ensure visible
            App.removeCallbacks(mR4); // Remove empty timeout if we got results
        }

        // If this was the initial auto search and we got results, trigger next site check
        if (isInitAuto() && !items.isEmpty()) {
            Log.d("VideoActivity", "Initial auto search returned results, checking next site.");
            nextSite();
        } else if (isInitAuto() && mExecutor != null && mExecutor.isTerminated()) {
             // If initial auto search finished with no results from any site
             Log.w("VideoActivity", "Initial auto search finished with no matching results.");
             // Maybe show "No results found" after a delay? Or rely on mR4 timeout.
             // setInitAuto(false); // No longer in initial auto mode
        }

        // Consider stopping the search early if in manual mode and results are found?
        // if (!isAutoMode() && !items.isEmpty()) {
        //     stopSearch();
        // }
    }


    private void setSearch(Vod item) {
        if (item == null) return; // Add null check
        Log.d("VideoActivity", "User selected search result: " + item.getVodName() + " from " + item.getSiteName());
        setAutoMode(false); // User manually selected, turn off auto mode
        stopSearch(); // Stop searching other sites
        getDetail(item); // Load details for the selected item
    }

    private boolean mismatch(Vod item) {
        if (item == null) return true; // Mismatch if item is null

        // Don't show the exact same video (identified by ID) in search results
        if (getId().equals(item.getVodId())) return true;

        // Don't show items previously identified as broken/failed in this session
        if (mBroken.contains(item.getVodId())) return true;

        // Get the keyword used for the current search batch
        String keyword = Objects.toString(mBinding.part.getTag(), "");
        if (TextUtils.isEmpty(keyword)) return false; // No keyword to compare against

        // Get the name of the search result item
        String itemName = Objects.toString(item.getVodName(), ""); // Handle null name

        // Mismatch logic:
        // In Auto Mode: Require an exact match with the keyword (likely the original title)
        // In Manual Mode (or if keyword came from 'part' selection): Require item name to contain the keyword
        if (isAutoMode()) {
            return !itemName.equals(keyword);
        } else {
            // Case-insensitive contains check for manual search
            return !itemName.toLowerCase().contains(keyword.toLowerCase());
        }
    }


    private void nextParse(int position) {
        // Ensure adapter exists and position is valid
        if (mParseAdapter == null || position < 0 || position + 1 >= mParseAdapter.size()) return;

        Parse nextParse = (Parse) mParseAdapter.get(position + 1);
        if (nextParse != null) {
            Log.d("VideoActivity", "Trying next parser: " + nextParse.getName());
            Notify.show(getString(R.string.play_switch_parse, nextParse.getName()));
            setParseActivated(nextParse); // Activate and refresh
        }
    }

    private void nextFlag(int position) {
        // Ensure adapter exists and position is valid
        if (mFlagAdapter == null || position < 0 || position + 1 >= mFlagAdapter.size()) return;

        Flag nextFlag = (Flag) mFlagAdapter.get(position + 1);
        if (nextFlag != null) {
            Log.d("VideoActivity", "Trying next flag: " + nextFlag.getFlag());
            Notify.show(getString(R.string.play_switch_flag, nextFlag.getFlag()));
            setFlagActivated(nextFlag); // Activate and refresh (seamless or direct play)
        }
    }

    private void nextSite() {
        // Ensure adapter exists and has items
        if (mQuickAdapter == null || mQuickAdapter.size() == 0) {
            Log.w("VideoActivity", "nextSite called but quick adapter is empty.");
            // If this was the initial auto search, show empty state after timeout (mR4)
             if (isInitAuto()) {
                 setInitAuto(false); // No longer initial auto
                 // Rely on mR4 to show empty if no results ever came
             } else if (!isInitAuto() && TextUtils.isEmpty(currentLogoUrl)) {
                 // If manual search finished with no results
                 showEmpty();
             }
            return;
        }

        Vod item = (Vod) mQuickAdapter.get(0); // Get the first available result
        mQuickAdapter.removeItems(0, 1); // Remove it from the list

        if (item == null) {
            Log.w("VideoActivity", "Removed null item from quick adapter, trying next.");
            nextSite(); // Recursively try the next item
            return;
        }

        Log.d("VideoActivity", "Auto/Forced switching to site: " + item.getSiteName() + " for VOD: " + item.getVodName());
        Notify.show(getString(R.string.play_switch_site, item.getSiteName()));

        mBroken.add(getId()); // Add the *current* video's ID to broken list before switching
        setInitAuto(false); // No longer in the initial auto-triggered phase
        getDetail(item); // Load details for the new item
    }


    private void onPaused() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Allow screen off

        // Ensure player and widget exist
        if (mPlayers == null || mBinding.widget == null) return;

        // Update time display in the center overlay
        mBinding.widget.exoDuration.setText(mPlayers.getDurationTime());
        mBinding.widget.exoPosition.setText(mPlayers.getPositionTime(0));
        mBinding.widget.action.setImageResource(R.drawable.ic_widget_pause); // Show pause icon

        // Show center overlay in fullscreen, hide otherwise
        if (isFullscreen()) {
            showInfoAndCenter();
        } else {
            hideInfoAndCenter(); // Hide center overlay if not fullscreen
        }

        mPlayers.pause(); // Pause the actual playback
    }

    private void onPlay() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on

        // Ensure player exists
        if (mPlayers == null) return;

        mPlayers.play(); // Start or resume playback
        hideCenter(); // Hide the center action icon (play/pause/seek)
        hideInfo(); // Hide the top info bar as well after resuming play
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
        return groupSize;
    }

    public void setGroupSize(int size) {
        groupSize = Math.max(1, size); // Ensure group size is at least 1
    }

    private View getFocus1() {
        // Return stored focus, fallback to video view if null or not focusable anymore
        if (mFocus1 != null && mFocus1.isFocusable()) {
            return mFocus1;
        }
        return mBinding.video; // Default fallback
    }

    private View getFocus2() {
        // Return stored control focus, apply fallbacks if needed
        View focusTarget = mFocus2;

        // If stored focus is invalid or specific problematic buttons, default to 'next'
        if (focusTarget == null || !focusTarget.isFocusable() || focusTarget == mBinding.control.opening || focusTarget == mBinding.control.ending) {
            focusTarget = mBinding.control.next;
        }

        // Final fallback if 'next' is also not focusable
        if (focusTarget == null || !focusTarget.isFocusable()) {
            focusTarget = mBinding.control.getRoot() != null ? mBinding.control.getRoot().findFocus() : null; // Try finding any focusable in control layout
            if (focusTarget == null) {
                focusTarget = mBinding.video; // Absolute fallback
            }
        }
        return focusTarget;
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event == null) return super.dispatchKeyEvent(null); // Basic null check

        hasKeyEvent = true; // Mark that a key event occurred

        // Handle back key for small window setting
        View currentFocus = getCurrentFocus();
        // Ensure progressLayout is content, not fullscreen, back key pressed, setting enabled, and focus is not on video player itself
        if (mBinding.progressLayout != null && mBinding.progressLayout.isContent() &&
            !isFullscreen() && KeyUtil.isBackKey(event) && Setting.getSmallWindowBackKey() == 1 &&
            currentFocus != null && currentFocus != mBinding.video) {

            mFocus1 = mBinding.video; // Store video as the focus target
            if (mFocus1 != null) mFocus1.requestFocus(); // Request focus on video
            return true; // Consume the back key event
        }

        // Handle menu key in fullscreen based on setting
        if (isFullscreen() && KeyUtil.isMenuKey(event)) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) { // Handle on ACTION_DOWN to avoid repeats
                if (Setting.getFullscreenMenuKey() == 0) {
                    onToggle(); // Toggle controls
                    return true; // Consume event
                } else if (Setting.getFullscreenMenuKey() == 1) {
                    onEpisodes(); // Show episodes dialog
                    return true; // Consume event
                }
            } else {
                 return true; // Consume ACTION_UP as well if needed
            }
        }


        // Store focused view within controls when controls are visible
        if (isVisible(mBinding.control.getRoot())) {
            setR1Callback(); // Reset hide timer on any key press while controls are visible
            if (currentFocus != null && event.getAction() == KeyEvent.ACTION_DOWN) { // Store focus on key down
                // Check if focus is actually within the control root before storing
                ViewParent parent = currentFocus.getParent();
                while (parent != null) {
                    if (parent == mBinding.control.getRoot()) {
                        mFocus2 = currentFocus; // Store focus if it's a child of controls
                        break;
                    }
                    parent = parent.getParent();
                }
            }
        }


        // Handle custom key events (like seeking) in fullscreen when controls are hidden
        if (isFullscreen() && isGone(mBinding.control.getRoot()) && mKeyDown != null && mKeyDown.hasEvent(event)) {
            // Let CustomKeyDownVod handle the event (e.g., seeking)
            return mKeyDown.onKeyDown(event);
        }

        // Allow default dispatching if not handled above
        return super.dispatchKeyEvent(event);
    }


    // --- CustomKeyDownVod Listener Methods ---

    @Override
    public void onBright(int progress) {
        if (mBinding.widget == null) return; // Null check
        mBinding.widget.bright.setVisibility(View.VISIBLE);
        mBinding.widget.brightProgress.setProgress(progress);
        // Set icon based on brightness level
        if (progress < 35) mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_low);
        else if (progress < 70) mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_medium);
        else mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_high);
    }

    @Override
    public void onBrightEnd() {
        if (mBinding.widget != null) { // Null check
            mBinding.widget.bright.setVisibility(View.GONE);
        }
    }

    @Override
    public void onVolume(int progress) {
        if (mBinding.widget == null) return; // Null check
        mBinding.widget.volume.setVisibility(View.VISIBLE);
        mBinding.widget.volumeProgress.setProgress(progress);
        // Set icon based on volume level
        if (progress == 0) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_off); // Mute icon
        else if (progress < 35) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_low);
        else if (progress < 70) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_medium);
        else mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_high);
    }

    @Override
    public void onVolumeEnd() {
        if (mBinding.widget != null) { // Null check
            mBinding.widget.volume.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSeeking(int time) {
        // Ensure player and widget exist
        if (mPlayers == null || mBinding.widget == null) return;

        // Show seeking UI
        mBinding.widget.exoDuration.setText(mPlayers.getDurationTime());
        mBinding.widget.exoPosition.setText(mPlayers.getPositionTime(time)); // Show target time
        mBinding.widget.action.setImageResource(time > 0 ? R.drawable.ic_widget_forward : R.drawable.ic_widget_rewind);
        mBinding.widget.center.setVisibility(View.VISIBLE); // Show center icon
        hideProgress(); // Hide buffering indicator during seek display
    }

    @Override
    public void onSeekTo(int time) {
        // Ensure player exists
        if (mPlayers == null) return;

        mPlayers.seekTo(time); // Perform the actual seek
        if (mKeyDown != null) mKeyDown.resetTime(); // Reset seek time accumulator
        showProgress(); // Show progress indicator while player seeks
        onPlay(); // Ensure player is playing after seek (might have been paused)
    }

    @Override
    public void onSpeedUp() {
        // Ensure player and widget exist
        if (mPlayers == null || mBinding.widget == null || !mPlayers.isPlaying() || !mPlayers.canAdjustSpeed()) return;

        // Set speed to high value (3x or 5x)
        // float targetSpeed = (mPlayers.getSpeed() < 3) ? 3.0f : 5.0f; // Toggle between 3x and 5x?
        float targetSpeed = 5.0f; // Or just set to 5x directly
        mBinding.control.speed.setText(mPlayers.setSpeed(targetSpeed)); // Apply speed and update button

        // Show speed indicator animation
        mBinding.widget.speed.setText(String.format(java.util.Locale.getDefault(), "%.1fx", targetSpeed)); // Show speed value
        mBinding.widget.speed.startAnimation(ResUtil.getAnim(R.anim.forward)); // Play animation
        mBinding.widget.speed.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSpeedEnd() {
        // Ensure player, widget, and history exist
        if (mPlayers == null || mBinding.widget == null) return;

        // Restore speed from history or default
        float previousSpeed = (mHistory != null) ? mHistory.getSpeed() : 1.0f;
        mBinding.control.speed.setText(mPlayers.setSpeed(previousSpeed)); // Restore speed and update button

        // Hide speed indicator
        mBinding.widget.speed.setVisibility(View.GONE);
        mBinding.widget.speed.clearAnimation();
    }


    @Override
    public void onKeyUp() {
        // Ensure player exists
        if (mPlayers == null) return;

        long current = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        long half = (duration > 0) ? duration / 2 : 0; // Calculate half safely

        showInfo(); // Show top info bar
        // Show controls, focusing on OP or ED button based on current position
        showControl(current < half ? mBinding.control.opening : mBinding.control.ending);
    }

    @Override
    public void onKeyDown() {
        showInfo(); // Show top info bar
        showControl(getFocus2()); // Show controls, focusing on last known/default control
    }

    @Override
    public void onKeyCenter() {
        // Ensure player exists
        if (mPlayers == null) return;

        if (mPlayers.isPlaying()) {
            onPaused();
            hideControl(false); // Hide controls but keep info overlay visible
        } else {
            onPlay();
            hideControl(true); // Hide controls and info overlay when resuming play
        }
    }

    @Override
    public void onSingleTap() {
        if (isFullscreen()) onToggle(); // Toggle controls on single tap in fullscreen
    }

    @Override
    public void onDoubleTap() {
        if (isFullscreen()) onKeyCenter(); // Toggle play/pause on double tap in fullscreen
    }

    // --- PlayerDialog Listener Methods ---

    @Override
    public void onPlayerClick(Integer item) {
        if (item == null || mPlayers == null) return; // Null checks
        if (item == mPlayers.getPlayer()) return; // No change if same player selected

        Log.d("VideoActivity", "User selected player: " + item);
        mPlayers.setPlayer(item); // Set the new player type
        setPlayerView(); // Update player button text
        setDecodeView(); // Update decode button text (might change with player)
        onRefresh(); // Reload the source with the new player
    }

    @Override
    public void onPlayerShare(String title) {
        // Call the existing long-press action for sharing/casting
        this.onChoose();
    }

    // --- Activity Result ---

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return; // Only process successful results

        switch (requestCode) {
            case 1000: // Result from starting another VideoActivity (e.g., switching source)
                setResult(RESULT_OK); // Pass the result back up if needed
                finish(); // Finish this instance as a new one took over
                break;
            case 1001: // Result from external player (e.g., MX Player)
                if (data != null && mPlayers != null) { // Null checks
                    mPlayers.checkData(data); // Let Players handle potential position data
                }
                break;
             // Handle other request codes if necessary
        }
    }

    // --- Activity Lifecycle Methods ---

    @Override
    protected void onResume() {
        super.onResume();
        setBackground(false); // Mark as foreground
        if (mClock != null) mClock.start(); // Start clock updates
        // Resume playback only if not showing center overlay (which indicates pause state)
        if (mPlayers != null && (mBinding.widget == null || isGone(mBinding.widget.center))) {
             onPlay();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        setBackground(true); // Mark as background
        if (mPlayers != null) mPlayers.pause(); // Pause playback
        if (mClock != null) mClock.stop(); // Stop clock updates
        // Save history state on pause if not incognito
        if (mHistory != null && mPlayers != null && !Setting.isIncognito()){
             long position = mPlayers.getPosition();
             long duration = mPlayers.getDuration();
             if(position >= 0 && duration > 0) {
                 mHistory.setPosition(position);
                 mHistory.setDuration(duration);
                 App.execute(() -> {
                      if (mHistory != null) mHistory.save(); // Save in background
                 });
             }
        }
    }

    @Override
    public void onBackPressed() {
        if (isVisible(mBinding.control.getRoot())) {
            hideControl(); // Hide controls first if visible
        } else if (isVisible(mBinding.widget.center)) {
             hideCenter(); // Hide center icon (seek/pause state) if visible
             if (mPlayers != null && !mPlayers.isPlaying()) {
                 // If paused state was cancelled, maybe resume play? Optional.
                 // onPlay();
             }
        } else if (isFullscreen()) {
            exitFullscreen(); // Exit fullscreen mode
        } else {
            stopSearch(); // Stop any ongoing search
            super.onBackPressed(); // Default back behavior (finish activity)
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSearch(); // Ensure search executor is stopped
        if (mClock != null) mClock.release(); // Release clock resources
        if (mPlayers != null) mPlayers.release(); // Release player resources
        Source.get().stop(); // Stop any ongoing source fetching
        RefreshEvent.history(); // Notify history view needs refresh
        App.removeCallbacks(mR1, mR2, mR3, mR4); // Remove all runnables

        // --- Glide Cleanup ---
        try {
            // Check if activity is valid before calling Glide
            if (!isFinishing() && !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                // Clear Glide targets to prevent memory leaks
                 if (mBinding.logoImageView != null) { // Clear logo view
                     Glide.with(this).clear(mBinding.logoImageView);
                 }
                if (mBinding.exo != null) {
                    Glide.with(this).clear(mBinding.exo); // Might have artwork target
                }
                if (mBinding.ijk != null) {
                    Glide.with(this).clear(mBinding.ijk); // Might have artwork target
                }
                if (mBinding.widget != null && mBinding.widget.preview != null) {
                    Glide.with(this).clear(mBinding.widget.preview); // Clear preview target
                }
                 if (mBinding.video != null) { // Clear main video view target (used for tag?)
                     // Glide.with(this).clear(mBinding.video); // Usually not needed unless loading into it directly
                 }
                // Call Glide's own cleanup
                Glide.with(this).onDestroy();
            }
        } catch (Exception e) {
            Log.e("VideoActivity", "Error during Glide cleanup in onDestroy", e);
        }
        // --- End Glide Cleanup ---

        // Nullify references to help GC (optional but good practice)
        mBinding = null;
        mPlayers = null;
        mViewModel = null;
        mHistory = null;
        mKeyDown = null;
        mClock = null;
        // etc. for adapters, presenters...
    }

    // Helper to notify item changed with null checks
    @Override // 添加 Override 注解
    protected void notifyItemChanged(RecyclerView view, ArrayObjectAdapter adapter) { // 改为 protected        if (view != null && adapter != null && view.getAdapter() instanceof ItemBridgeAdapter) {
            ((ItemBridgeAdapter) view.getAdapter()).notifyDataSetChanged();
            // More specific notifications are better if possible, e.g.,
            // adapter.notifyArrayItemRangeChanged(0, adapter.size());
        } else if (view != null && view.getAdapter() != null) {
             // Fallback for other adapter types
             view.getAdapter().notifyDataSetChanged();
        }
    }
}
