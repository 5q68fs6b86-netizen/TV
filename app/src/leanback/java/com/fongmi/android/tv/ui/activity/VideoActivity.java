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

import com.bumptech.glide.Glide; // Ensure Glide import is present
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.ui.custom.CustomSeekView;
import com.fongmi.android.tv.Constant; // For TMDB_API_KEY
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
import com.fongmi.android.tv.databinding.ActivityVideoBinding; // Ensure ViewBinding import is present
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
import com.fongmi.android.tv.utils.TmdbHelper; // Import TmdbHelper
import android.widget.ImageView; // Import ImageView
import android.util.Log;       // Import Log
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.view.ViewParent;
// --- End Imports ---

public class VideoActivity extends BaseActivity implements CustomKeyDownVod.Listener, TrackDialog.Listener, TrackDialog.ChooserListener, PlayerDialog.Listener, ArrayPresenter.OnClickListener, Clock.Callback {
    private AnimatorSet mControlShowAnimator;
    private AnimatorSet mControlHideAnimator;
    private boolean mIsControlAnimating = false;
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
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private CustomSeekView mSeekView;
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
        return (Flag) mFlagAdapter.get(getFlagPosition());
    }

    private Episode getEpisode() {
        return (Episode) mEpisodeAdapter.get(getEpisodePosition());
    }

    private int getFlagPosition() {
        for (int i = 0; i < mFlagAdapter.size(); i++) if (((Flag) mFlagAdapter.get(i)).isActivated()) return i;
        return 0;
    }

    private int getEpisodePosition() {
        for (int i = 0; i < mEpisodeAdapter.size(); i++) if (((Episode) mEpisodeAdapter.get(i)).isActivated()) return i;
        return 0;
    }

    private int getParsePosition() {
        for (int i = 0; i < mParseAdapter.size(); i++) if (((Parse) mParseAdapter.get(i)).isActivated()) return i;
        return 0;
    }

    private int getPlayer() {
        return mHistory != null && mHistory.getPlayer() != -1 ? mHistory.getPlayer() : getSite().getPlayerType() != -1 ? getSite().getPlayerType() : Setting.getPlayer();
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
        getEpisodeView().setSelectedPosition(position);
        if (hasKeyEvent) return;
        if (isFullscreen()) return;
        getEpisodeView().postDelayed(() -> {
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
        // Ensure mBinding is initialized correctly
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
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
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
        mSeekView = mBinding.control.seek;
        mCurrentTime = mBinding.control.currentTime;
        mTotalTime = mBinding.control.totalTime;
    // 设置播放器监听
        mSeekView.setListener(mPlayers.getPlayer());
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
        mBinding.video.setOnTouchListener((view, event) -> mKeyDown.onTouchEvent(event));
        mBinding.flag.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                if (mFlagAdapter.size() > 0) setFlagActivated((Flag) mFlagAdapter.get(position));
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
                if (mEpisodeAdapter.size() > getGroupSize() && position > 1 && hasKeyEvent) setEpisodeSelectedPosition((position - 2) * getGroupSize());
            }
        });
    }

    private void setEpisodeChildKeyListener(RecyclerView.ViewHolder child, int position) {
        if (getEpisodeView() != mBinding.episodeVert) return;
        int itemCount = getEpisodeView().getAdapter().getItemCount();
        if (itemCount <= 0) return;
        int columns = mEpisodePresenter.getNumColumns();
        if ((position + columns >= itemCount) && ((position % columns) + 1 > (itemCount % columns))) {
            child.itemView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                        View lastItem =  getEpisodeView().getLayoutManager().findViewByPosition(itemCount - 1);
                        if (lastItem != null) lastItem.requestFocus();
                    }
                    return false;
                }
            });
        }
    }

    private void setRecyclerView() {
        mBinding.flag.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.flag.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.flag.setAdapter(new ItemBridgeAdapter(mFlagAdapter = new ArrayObjectAdapter(mFlagPresenter = new FlagPresenter(this::setFlagActivated))));
        mBinding.quality.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.quality.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.quality.setAdapter(mQualityAdapter = new QualityAdapter(this::setQualityActivated));
        mBinding.array.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.array.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.array.setAdapter(new ItemBridgeAdapter(mArrayAdapter = new ArrayObjectAdapter(mArrayPresenter = new ArrayPresenter(this))));
        mBinding.part.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.part.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.part.setAdapter(new ItemBridgeAdapter(mPartAdapter = new ArrayObjectAdapter(mPartPresenter = new PartPresenter(item -> initSearch(item, false)))));
        mBinding.quick.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.quick.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.quick.setAdapter(new ItemBridgeAdapter(mQuickAdapter = new ArrayObjectAdapter(new QuickPresenter(this::setSearch))));
        mBinding.control.parse.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.control.parse.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.control.parse.setAdapter(new ItemBridgeAdapter(mParseAdapter = new ArrayObjectAdapter(new ParsePresenter(this::setParseActivated))));
        mParseAdapter.setItems(VodConfig.get().getParses(), null);
    }

    private void setEpisodeView() {
        mBinding.episodeVert.setVerticalSpacing(ResUtil.dp2px(8));
        mBinding.episodeHori.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.episodeVert.setHorizontalSpacing(ResUtil.dp2px(8));
        mBinding.episodeHori.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        getEpisodeView().setAdapter(new ItemBridgeAdapter(mEpisodeAdapter = new ArrayObjectAdapter(mEpisodePresenter = new EpisodePresenter(this::setEpisodeActivated))));
    }

    private void setVideoView() {
        mPlayers.init(getExo(), getIjk());
        ExoUtil.setSubtitleView(mBinding.exo);
        IjkUtil.setSubtitleView(mBinding.ijk);
        mBinding.control.reset.setText(ResUtil.getStringArray(R.array.select_reset)[Setting.getReset()]);
        mBinding.exo.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        mBinding.ijk.setBackgroundColor(android.graphics.Color.TRANSPARENT);
    }

    private void setDanmuViewSettings() {
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
        if (getId().startsWith("push://")) getIntent().putExtra("key", "push_agent").putExtra("id", getId().substring(7));
        if (getId().isEmpty() || getId().startsWith("msearch:")) setEmpty(false);
        else getDetail();
    }

    private void setPlayerView() {
        getIjk().setPlayer(mPlayers.getPlayer());
        mBinding.control.player.setText(mPlayers.getPlayerText());
        mBinding.control.speed.setEnabled(mPlayers.canAdjustSpeed());
        getExo().setVisibility(mPlayers.isExo() ? View.VISIBLE : View.GONE);
        getIjk().setVisibility(mPlayers.isIjk() ? View.VISIBLE : View.GONE);
        if (mHistory != null) { // Add null check for history
            mBinding.control.speed.setText(mPlayers.setSpeed(mHistory.getSpeed()));
        }
        if (mSeekView != null && mPlayers != null) {
            mSeekView.setListener(mPlayers.getPlayer());
        }
    }

    private void setDecodeView() {
        mBinding.control.decode.setText(mPlayers.getDecodeText());
    }

    private void setScale(int scale) {
        getExo().setResizeMode(scale);
        getIjk().setResizeMode(scale);
        mBinding.control.scale.setText(ResUtil.getStringArray(R.array.select_scale)[scale]);
    }

    private void getDetail() {
        mViewModel.detailContent(getKey(), getId());
    }

    private void getDetail(Vod item) {
        getIntent().putExtra("key", item.getSiteKey());
        getIntent().putExtra("pic", item.getVodPic());
        getIntent().putExtra("id", item.getVodId());
        mBinding.scroll.scrollTo(0, 0);
        mClock.setCallback(null);
        mPlayers.reset();
        mPlayers.stop();
        getDetail();
    }

    private void setDetail(Result result) {
        if (result.getList().isEmpty()) setEmpty(result.hasMsg());
        else setDetail(result.getList().get(0));
        Notify.show(result.getMsg());
    }

    // --- Modified getPlayer ---
    private void getPlayer(Flag flag, Episode episode, boolean replay) {
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
        // hidePreview(); // Removed duplicate call
        hideCenter();
    }
    // --- End Modified getPlayer ---

    private void setPlayer(Result result) {
        result.getUrl().set(mQualityAdapter.getPosition());
        setUseParse(VodConfig.hasParse() && ((result.getPlayUrl().isEmpty() && VodConfig.get().getFlags().contains(result.getFlag())) || result.getJx() == 1));
        mPlayers.start(result, isUseParse(), getSite().isChangeable() ? getSite().getTimeout() : -1);
        mBinding.control.parse.setVisibility(isUseParse() ? View.VISIBLE : View.GONE);
        setQualityVisible(result.getUrl().isMulti());
        checkDanmu(result.getDanmaku());
        mQualityAdapter.addAll(result);
    }

    private void checkDanmu(String danmu) {
        mBinding.danmaku.release();
        if (!Setting.isDanmuLoad()) return;
        mBinding.danmaku.setVisibility(danmu.isEmpty() ? View.GONE : View.VISIBLE);
        if (danmu.length() > 0) App.execute(() -> mBinding.danmaku.prepare(new Parser(danmu), mDanmakuContext));
    }

    private void setEmpty(boolean finish) {
        if (isFromCollect() || finish) {
            finish();
        } else if (getName().isEmpty()) { // Check original getName() from intent
            showEmpty();
        } else {
            // Set the fallback text view initially when detail is empty but name exists
             mBinding.nameTextView.setText(getName()); // Set fallback text initially
             mBinding.nameTextView.setVisibility(View.VISIBLE); // Make sure it's visible
             mBinding.logoImageView.setVisibility(View.GONE);
            App.post(mR4, 10000);
            checkSearch(false); // Start search based on the name from intent
        }
    }

    private void showEmpty() {
        mBinding.progressLayout.showEmpty();
        stopSearch();
    }

    // --- Modified setDetail(Vod item) ---
    private void setDetail(Vod item) {
        mBinding.progressLayout.showContent();
        mBinding.video.setTag(item.getVodPic(getPic()));

        // 1. Store original VOD name
        currentVodName = item.getVodName(getName());
        currentLogoUrl = null; // Reset logo url

        // 2. Remove reference to old mBinding.name (already done in layout)

        // 3. Set initial title state: show fallback text, hide logo image
        mBinding.logoImageView.setVisibility(View.GONE);
        mBinding.nameTextView.setVisibility(View.VISIBLE);
        mBinding.nameTextView.setText(currentVodName); // Display text title initially

        // Set other details (using existing setText method)
        setText(mBinding.remark, 0, item.getVodRemarks());
        setText(mBinding.year, R.string.detail_year, item.getVodYear());
        setText(mBinding.area, R.string.detail_area, item.getVodArea());
        setText(mBinding.type, R.string.detail_type, item.getTypeName());
        setText(mBinding.site, R.string.detail_site, getSite().getName());
        setText(mBinding.actor, R.string.detail_actor, Html.fromHtml(item.getVodActor()).toString());
        setText(mBinding.content, R.string.detail_content, Html.fromHtml(item.getVodContent()).toString());
        setText(mBinding.director, R.string.detail_director, Html.fromHtml(item.getVodDirector()).toString());

        mFlagAdapter.setItems(item.getVodFlags(), null);
        mBinding.content.setMaxLines(getMaxLines());
        // mBinding.video.requestFocus(); // Consider focus logic, maybe focus nameTextView or logoImageView later
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

    // --- Added fetchTmdbLogo Method ---

// 在 VideoActivity.java 中

private void fetchTmdbLogo(String title, String year, String typeName) {
    // API Key 检查 (假设这部分代码是正确的，并且在方法开头)
    if (TextUtils.isEmpty(Constant.TMDB_API_KEY) || "YOUR_TMDB_API_KEY_HERE".equals(Constant.TMDB_API_KEY)) {
        Log.e("VideoActivity", "TMDB API Key not set! Skipping logo fetch.");
        // Check Activity state even here, though less likely to be needed
        if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) return;
        mBinding.logoImageView.setVisibility(View.GONE);
        mBinding.nameTextView.setVisibility(View.VISIBLE);
        mBinding.nameTextView.setText(currentVodName);
        return;
    }

    // 调用 TmdbHelper.findLogoForVod 并传入回调
    TmdbHelper.findLogoForVod(title, year, typeName, new TmdbHelper.LogoCallback() {
        @Override
        public void onLogoFound(@NonNull String logoUrl) {
            // --- 1. Activity 状态检查放在最前面 ---
            if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                 Log.w("VideoActivity", "Activity is finishing or destroyed in onLogoFound, skipping Glide load.");
                 return; // 如果 Activity 无效，直接返回
            }
            // --- 结束检查 ---

            // --- 如果 Activity 有效，执行后续操作 ---
            currentLogoUrl = logoUrl;
            mBinding.nameTextView.setVisibility(View.GONE);
            mBinding.logoImageView.setVisibility(View.VISIBLE);

            try { // 包裹资源获取和计算，增加健壮性
                int targetPixelHeight = getResources().getDimensionPixelSize(R.dimen.detail_title_area_height);
                int targetPixelWidth = targetPixelHeight * 8; // Example width calculation
                Log.d("VideoActivity", "Glide override target size: " + targetPixelWidth + "x" + targetPixelHeight);

                Glide.with(VideoActivity.this) // 确认 VideoActivity.this 有效
                     .load(logoUrl)
                     .placeholder(R.drawable.ic_placeholder)
                     .error(R.drawable.ic_error)
                     .override(targetPixelWidth, targetPixelHeight)
                     .fitCenter() // 使用 fitCenter
                     .into(mBinding.logoImageView);
            } catch (Exception e) {
                 Log.e("VideoActivity", "Error during Glide load setup or execution in onLogoFound", e);
                 // 发生异常，可以考虑回退到显示文字
                 onLogoNotFound(); // 调用 onLogoNotFound 处理 UI 回退
            }
        } // <--- onLogoFound 结束大括号

        @Override
        public void onLogoNotFound() {
            // --- 1. Activity 状态检查放在最前面 ---
            if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                  Log.w("VideoActivity", "Activity is finishing or destroyed in onLogoNotFound, skipping UI update.");
                 return; // 如果 Activity 无效，直接返回
            }
            // --- 结束检查 ---

            // --- 如果 Activity 有效，执行后续操作 ---
            currentLogoUrl = null;
            mBinding.logoImageView.setVisibility(View.GONE);
            mBinding.nameTextView.setVisibility(View.VISIBLE);
            mBinding.nameTextView.setText(currentVodName);
        } // <--- onLogoNotFound 结束大括号

         @Override
         public void onError() {
            // --- 1. Activity 状态检查放在最前面 ---
            if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                  Log.w("VideoActivity", "Activity is finishing or destroyed in onError, skipping UI update.");
                 return; // 如果 Activity 无效，直接返回
            }
            // --- 结束检查 ---

            // --- 如果 Activity 有效，执行后续操作 ---
            Log.w("VideoActivity", "Error fetching TMDB logo for: " + title);
            // 调用 onLogoNotFound 来统一处理 UI 回退到显示文字的状态
            // onLogoNotFound 内部已经包含了 Activity 状态检查，所以这里调用是安全的
            onLogoNotFound();
         } // <--- onError 结束大括号

    }); // <--- **关键：添加了结束匿名类和方法调用的 );**

} // <--- fetchTmdbLogo 方法的结束大括号-- End Added fetchTmdbLogo Method ---

    private int getMaxLines() {
        int lines = 1;
        if (isGone(mBinding.actor)) ++lines;
        if (isGone(mBinding.remark)) ++lines;
        if (isGone(mBinding.director)) ++lines;
        return lines;
    }

    private void setText(TextView view, int resId, String text) {
        if (view == null || text == null) return; // Add null checks
        view.setText(getSpan(resId, text), TextView.BufferType.SPANNABLE);
        view.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
        view.setLinkTextColor(0xFFFFFFFF);
        CustomMovement.bind(view);
        view.setTag(text);
    }

    private SpannableStringBuilder getSpan(int resId, String text) {
        if (text == null) text = ""; // Handle null text
        if (resId > 0) text = getString(resId, text);
        Map<String, String> map = new HashMap<>();
        Matcher m = Sniffer.CLICKER.matcher(text);
        while (m.find()) {
            String group1 = m.group(1);
            String group2 = m.group(2);
            if (group1 != null && group2 != null) {
                String key = Trans.s2t(group2).trim();
                text = text.replace(m.group(), key);
                map.put(key, group1);
            }
        }
        SpannableStringBuilder span = SpannableStringBuilder.valueOf(text);
        for (String s : map.keySet()) {
            int index = text.indexOf(s);
            if (index == -1) continue; // Add check in case replace messed up indices
            Result result = Result.type(map.get(s));
            span.setSpan(getClickSpan(result), index, index + s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    private ClickableSpan getClickSpan(Result result) {
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                VodActivity.start(getActivity(), getKey(), result);
            }
        };
    }

    private void setFlagActivated(Flag item) {
        if (mFlagAdapter.size() == 0 || item == null || item.isActivated()) return; // Add null check
        if (mFlagAdapter.indexOf(item) == -1) {
             if(mFlagAdapter.size() > 0){ // Ensure adapter not empty before getting first item
                 item.setFlag(((Flag) mFlagAdapter.get(0)).getFlag());
             } else {
                 return; // Cannot set flag if adapter is empty
             }
        }
        for (int i = 0; i < mFlagAdapter.size(); i++) ((Flag) mFlagAdapter.get(i)).setActivated(item);
        mBinding.flag.setSelectedPosition(mFlagAdapter.indexOf(item));
        notifyItemChanged(mBinding.flag, mFlagAdapter);
        setEpisodeAdapter(item.getEpisodes());
        setQualityVisible(false);
        seamless(item);
    }

    private void setEpisodeAdapter(List<Episode> items) {
        getEpisodeView().setVisibility(items == null || items.isEmpty() ? View.GONE : View.VISIBLE); // Add null check
        if (items == null) return; // Prevent processing null list
        if (isVisible(mBinding.episodeVert)) setEpisodeView(items);
        mEpisodeAdapter.setItems(items, null);
        setArrayAdapter(items.size());
        setR2Callback(50);
    }

    private void setEpisodeView(List<Episode> items) {
        if (items == null) return; // Add null check
        int size = items.size();
        int episodeNameLength = 0;
        if(!items.isEmpty() && items.get(0) != null && items.get(0).getName() != null) { // Add null checks
             episodeNameLength = items.get(0).getName().length();
        }

        for (int i = 0; i < size; i++) {
            Episode episode = items.get(i);
            if (episode == null) continue; // Add null check for episode
            episode.setIndex(i);
            String name = episode.getName();
            int length = name == null ? 0 : name.length();
            if (length > episodeNameLength) episodeNameLength = length;
        }
        int numColumns = 10;
        if (episodeNameLength > 40) numColumns = 1;
        else if (episodeNameLength > 30) numColumns = 2;
        else if (episodeNameLength > 15) numColumns = 3;
        else if (episodeNameLength > 10) numColumns = 4;
        else if (episodeNameLength > 6) numColumns = 6;
        else if (episodeNameLength > 4) numColumns = 8;

        int rowNum = size == 0 ? 0 : (int) Math.ceil((double) size / (double) numColumns);
        int width = ResUtil.getScreenWidth() - ResUtil.dp2px(48);
        ViewGroup.LayoutParams params = mBinding.episodeVert.getLayoutParams();
        params.width = ResUtil.getScreenWidth();
        params.height = rowNum > 6 ? ResUtil.dp2px(300) : ResUtil.dp2px(Math.max(rowNum, 0) * 44); // Ensure rowNum >= 0
        mBinding.episodeVert.setNumColumns(numColumns);
        if (numColumns > 0) { // Avoid division by zero
            mBinding.episodeVert.setColumnWidth((width - ((numColumns - 1) * ResUtil.dp2px(8))) / numColumns);
        }
        mBinding.episodeVert.setLayoutParams(params);
        mBinding.episodeVert.setWindowAlignmentOffsetPercent(10f);
        mEpisodePresenter.setNumColumns(numColumns);
        mEpisodePresenter.setNumRows(rowNum);
    }

    private void seamless(Flag flag) {
        if (flag == null || mHistory == null) return; // Add null checks
        Episode episode = flag.find(mHistory.getVodRemarks(), getMark().isEmpty());
        setQualityVisible(episode != null && episode.isActivated() && mQualityAdapter.getItemCount() > 1);
        if (episode == null || episode.isActivated()) return;
        if (Setting.getFlag() == 1) {
            episode.setActivated(true);
            if (!isFullscreen()) getEpisodeView().requestFocus();
            setEpisodeSelectedPosition(getEpisodePosition());
            episode.setActivated(false);
        } else {
            mHistory.setVodRemarks(episode.getName());
            setEpisodeActivated(episode);
            hidePreview();
        }
    }

    public void setEpisodeActivated(Episode item) {
        if (item == null) return; // Add null check
        int flagPosition = getFlagPosition();
        if (shouldEnterFullscreen(item)) return;
        if (isFullscreen()) Notify.show(getString(R.string.play_ready, item.getName()));
        for (int i = 0; i < mFlagAdapter.size(); i++) {
            Flag flag = (Flag) mFlagAdapter.get(i);
            if(flag != null) flag.toggle(flagPosition == i, item); // Add null check
        }
        setEpisodeSelectedPosition(getEpisodePosition());
        notifyItemChanged(getEpisodeView(), mEpisodeAdapter);
        onRefresh();
    }

    private void setQualityVisible(boolean visible) {
        mBinding.quality.setVisibility(visible ? View.VISIBLE : View.GONE);
        setR2Callback(100);
    }

    private void setQualityActivated(Result result) {
        if(result == null) return; // Add null check
        try {
            mPlayers.start(result, isUseParse(), getSite().isChangeable() ? getSite().getTimeout() : -1);
            mBinding.danmaku.hide();
        } catch (Exception e) {
            ErrorEvent.extract(e.getMessage());
            e.printStackTrace();
        }
    }

    private void reverseEpisode(boolean scroll) {
        for (int i = 0; i < mFlagAdapter.size(); i++) {
             Flag flag = (Flag) mFlagAdapter.get(i);
             if (flag != null && flag.getEpisodes() != null) { // Add null checks
                 Collections.reverse(flag.getEpisodes());
             }
        }
        if(getFlag() != null) { // Add null check
           setEpisodeAdapter(getFlag().getEpisodes());
           if (scroll) setEpisodeSelectedPosition(getEpisodePosition());
        }
    }

    private void setParseActivated(Parse item) {
        if(item == null) return; // Add null check
        VodConfig.get().setParse(item);
        notifyItemChanged(mBinding.control.parse, mParseAdapter);
        onRefresh();
    }

    private void setArrayAdapter(int size) {
        if (size <= 0) { // Handle zero or negative size
             mBinding.array.setVisibility(View.GONE);
             return;
        }
        if (size > 200) setGroupSize(100);
        else if (size > 100) setGroupSize(40);
        else setGroupSize(20);
        List<String> items = new ArrayList<>();
        items.add(getString(R.string.play_reverse));
        items.add(getString(mHistory != null ? mHistory.getRevPlayText() : R.string.play_forward)); // Null check for history
        mBinding.array.setVisibility(size > 1 ? View.VISIBLE : View.GONE);
        if (mHistory != null && mHistory.isRevSort()) { // Null check for history
             for (int i = size; i > 0; i -= getGroupSize()) items.add(i + "-" + Math.max(i - (getGroupSize() - 1), 1));
        } else {
             for (int i = 0; i < size; i += getGroupSize()) items.add((i + 1) + "-" + Math.min(i + getGroupSize(), size));
        }
        mArrayAdapter.setItems(items, null);
    }

    private int findFocusDown(int index) {
        List<Integer> orders = Arrays.asList(R.id.flag, R.id.quality, R.id.episodeHori, R.id.array, R.id.episodeVert, R.id.part, R.id.quick);
        for (int i = 0; i < orders.size(); i++) {
             if (i > index) {
                 View v = findViewById(orders.get(i));
                 if (v != null && isVisible(v)) return orders.get(i); // Add null check for view
             }
        }
        return 0;
    }

    private int findFocusUp(int index) {
        List<Integer> orders = Arrays.asList(R.id.flag, R.id.quality, R.id.episodeHori, R.id.array, R.id.episodeVert, R.id.part, R.id.quick);
        for (int i = orders.size() - 1; i >= 0; i--) {
             if (i < index) {
                  View v = findViewById(orders.get(i));
                  if (v != null && isVisible(v)) return orders.get(i); // Add null check for view
             }
        }
        return 0;
    }

    private void updateFocus() {
        hasKeyEvent = false;
        mEpisodePresenter.setNextFocusDown(findFocusDown(Setting.getEpisode() == 0 ? 2 : 4));
        mEpisodePresenter.setNextFocusUp(findFocusUp(Setting.getEpisode() == 0 ? 2 : 4));
        mQualityAdapter.setNextFocusDown(findFocusDown(1));
        mArrayPresenter.setNextFocusDown(findFocusDown(3));
        mFlagPresenter.setNextFocusDown(findFocusDown(0));
        mArrayPresenter.setNextFocusUp(findFocusUp(3));
        mPartPresenter.setNextFocusUp(findFocusUp(5));
        notifyItemChanged(mBinding.flag, mFlagAdapter);
        notifyItemChanged(mBinding.quality, mQualityAdapter);
        notifyItemChanged(mBinding.array, mArrayAdapter);
        notifyItemChanged(getEpisodeView(), mEpisodeAdapter);
        notifyItemChanged(mBinding.part, mPartAdapter);
    }

    private void showDisplayInfo() {
        boolean hasDialog = false;
        try { // Add try-catch for potential FragmentManager issues
             for (Fragment f : getSupportFragmentManager().getFragments()) {
                 if (f instanceof BottomSheetDialogFragment) {
                      hasDialog = true;
                      break;
                 }
             }
        } catch (Exception e) {
            Log.e("VideoActivity", "Error checking for BottomSheetDialogFragment", e);
        }

        mBinding.display.clock.setVisibility(Setting.isDisplayTime() || isVisible(mBinding.widget.info) ? View.VISIBLE : View.GONE);
        mBinding.display.titleLayout.setVisibility(Setting.isDisplayVideoTitle() && !isVisible(mBinding.control.getRoot()) ? View.VISIBLE : View.GONE);
        mBinding.display.netspeed.setVisibility(Setting.isDisplaySpeed() && !isVisible(mBinding.control.getRoot()) && !hasDialog ? View.VISIBLE : View.GONE);
        mBinding.display.duration.setVisibility(Setting.isDisplayDuration() && !isVisible(mBinding.control.getRoot()) && (mPlayers.isVod()) && !hasDialog ? View.VISIBLE : View.GONE);
        mBinding.display.progress.setVisibility(Setting.isDisplayMiniProgress() && !isVisible(mBinding.control.getRoot()) && (mPlayers.isVod()) && !hasDialog ? View.VISIBLE : View.GONE);
    }

    private void onTimeChangeDisplaySpeed() {
        boolean visible = !isVisible(mBinding.control.getRoot());
        long position = mPlayers.getPosition();
        long duration = mPlayers.getDuration(); // Get duration
        if (Setting.isDisplaySpeed() && visible) Traffic.setSpeed(mBinding.display.netspeed);
        if (Setting.isDisplayDuration() && visible && position >= 0 && duration > 0) { // Check position >= 0 and duration > 0
             mBinding.display.duration.setText(mPlayers.getPositionTime(0) + "/" + mPlayers.getDurationTime());
        } else if(Setting.isDisplayDuration()) { // Hide if invalid
             mBinding.display.duration.setText("");
        }
        if (Setting.isDisplayMiniProgress() && visible && position >= 0 && duration > 0 && mPlayers.isVod()) { // Check position >= 0 and duration > 0
             mBinding.display.progress.setProgress((int) (position * 100 / duration));
        } else if(Setting.isDisplayMiniProgress()){ // Reset progress if invalid
            mBinding.display.progress.setProgress(0);
        }
        showDisplayInfo();
    }


    @Override
    public boolean onArrayItemTouch() {
        hasKeyEvent = true;
        return false;
    }

    @Override
    public void onRevSort() {
        if (mHistory == null) return; // Add null check
        mHistory.setRevSort(!mHistory.isRevSort());
        reverseEpisode(false);
    }

    @Override
    public void onRevPlay(TextView view) {
        if (mHistory == null || view == null) return; // Add null checks
        mHistory.setRevPlay(!mHistory.isRevPlay());
        view.setText(mHistory.getRevPlayText());
        Notify.show(mHistory.getRevPlayHint());
    }

    private boolean shouldEnterFullscreen(Episode item) {
        if (item == null) return false; // Add null check
        boolean enter = !isFullscreen() && item.isActivated();
        if (enter) enterFullscreen();
        return enter;
    }

    private void enterFullscreen() {
        mFocus1 = getCurrentFocus();
        mBinding.video.requestFocus();
        mBinding.video.setForeground(null);
        mBinding.video.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mBinding.video.setBackgroundColor(android.graphics.Color.BLACK);
        mBinding.video.setClipToOutline(false);
        mBinding.flag.setSelectedPosition(getFlagPosition());
        if(Setting.getDanmuSize() != 0) mDanmakuContext.setScaleTextSize(1.2f * Setting.getDanmuSize());
        mKeyDown.setFull(true);
        setFullscreen(true);
        mFocus2 = null;

        // --- 添加：隐藏非播放视图 ---
        hideDetailViews();
        // --- 结束添加 ---

        onPlay();
    }

    private void exitFullscreen() {
        mBinding.video.setForeground(ResUtil.getDrawable(R.drawable.selector_video));
        mBinding.video.setLayoutParams(mFrameParams);
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
        if(Setting.getDanmuSize() != 0) mDanmakuContext.setScaleTextSize(0.8f * Setting.getDanmuSize());
        if (getFocus1() != null) getFocus1().requestFocus();
        mKeyDown.setFull(false);
        setFullscreen(false);
        mFocus2 = null;

        // --- 添加：显示非播放视图 ---
        showDetailViews();
        // --- 结束添加 ---

        hideInfo();
    }

    // --- 添加：辅助方法 ---
    private void hideDetailViews() {
        // 隐藏标题区域 (Logo 或文字)
        mBinding.logoImageView.setVisibility(View.GONE);
        mBinding.nameTextView.setVisibility(View.GONE);
        // 如果你用了 titleContainer (策略二)，可以隐藏容器
        // if (mBinding.titleContainer != null) mBinding.titleContainer.setVisibility(View.GONE);

        // 隐藏其他详情
        mBinding.remark.setVisibility(View.GONE);
        mBinding.row1.setVisibility(View.GONE); // 包含 site, year, area, type
        mBinding.director.setVisibility(View.GONE);
        mBinding.actor.setVisibility(View.GONE);
        mBinding.content.setVisibility(View.GONE);
        mBinding.row2.setVisibility(View.GONE); // 包含 desc, keep, change1
        mBinding.flag.setVisibility(View.GONE);
        mBinding.scroll.setVisibility(View.GONE); // 隐藏包含下方列表的滚动视图
    }

    private void showDetailViews() {
        // 根据 Logo 是否加载成功，决定显示 Logo 还是文字
        if (currentLogoUrl != null) {
            mBinding.logoImageView.setVisibility(View.VISIBLE);
            mBinding.nameTextView.setVisibility(View.GONE);
        } else {
            mBinding.logoImageView.setVisibility(View.GONE);
            mBinding.nameTextView.setVisibility(View.VISIBLE);
        }
        // 如果你用了 titleContainer (策略二)，显示容器
        // if (mBinding.titleContainer != null) mBinding.titleContainer.setVisibility(View.VISIBLE);

        // 显示其他详情 (注意判空和内容是否为空)
        setText(mBinding.remark, 0, Objects.toString(mBinding.remark.getTag(), "")); // 从 Tag 恢复文本
        mBinding.row1.setVisibility(View.VISIBLE); // 总是显示行？或根据内部内容判断
        setText(mBinding.director, 0, Objects.toString(mBinding.director.getTag(), ""));
        setText(mBinding.actor, 0, Objects.toString(mBinding.actor.getTag(), ""));
        setText(mBinding.content, 0, Objects.toString(mBinding.content.getTag(), ""));
        mBinding.row2.setVisibility(View.VISIBLE); // 总是显示按钮行
        mBinding.flag.setVisibility(mFlagAdapter.size() > 0 ? View.VISIBLE : View.GONE); // 根据数据判断
        mBinding.scroll.setVisibility(View.VISIBLE); // 总是显示滚动区域
    }
    // --- 结束添加 ---
    private void onDesc() {
        CharSequence desc = mBinding.content.getText();
        if (desc != null && desc.length() > 3) { // Add null check
             DescDialog.show(this, desc.subSequence(3, desc.length()));
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
    }

    private void onChange() {
        checkSearch(true);
    }

    private void onLoop() {
        animateButtonClick(mBinding.control.loop);
        mBinding.control.loop.setActivated(!mBinding.control.loop.isActivated());
        updateButtonStyle(mBinding.control.loop, true);
    }

    private void onDanmu() {
        animateButtonClick(mBinding.control.danmu);
        Setting.putDanmu(!Setting.isDanmu());
        mBinding.control.danmu.setActivated(Setting.isDanmu());
        updateButtonStyle(mBinding.control.danmu, true);
        showDanmu();
    }

    private void showDanmu() {
        if (Setting.isDanmu()) mBinding.danmaku.show();
        else mBinding.danmaku.hide();
    }

    private void onDanmuAdd() {
        int line = Setting.getDanmuLine(3);
        line = Math.min(line + 1, 15);
        Setting.putDanmuLine(line);
        mBinding.control.danmu.setText(line + ResUtil.getString(R.string.lines));
        setDanmuViewSettings();
    }

    private void onDanmuSub() {
        int line = Setting.getDanmuLine(3);
        line = Math.max(line - 1, 1);
        Setting.putDanmuLine(line);
        mBinding.control.danmu.setText(line + ResUtil.getString(R.string.lines));
        setDanmuViewSettings();
    }

    private void onEpisodes() {
        animateButtonClick(mBinding.control.episodes);
        if(getFlag() == null || getFlag().getEpisodes() == null) return;
        EpisodeDialog.create().episodes(getFlag().getEpisodes()).show(this);
        hideControl();
    }

    private void checkNext() {
        if (mHistory != null && mHistory.isRevPlay()) onPrev(); // Add null check
        else onNext();
    }

    private void checkPrev() {
        if (mHistory != null && mHistory.isRevPlay()) onNext(); // Add null check
        else onPrev();
    }

    private void onNext() {
        animateButtonClick(mBinding.control.next);
        int current = getEpisodePosition();
        int max = mEpisodeAdapter.size() - 1;
        if (max < 0) return; // Adapter might be empty
        current = ++current > max ? max : current;
        Episode item = (Episode) mEpisodeAdapter.get(current);
        if (item != null) { // Add null check
            if (item.isActivated()) Notify.show(mHistory != null && mHistory.isRevPlay() ? R.string.error_play_prev : R.string.error_play_next);
            else setEpisodeActivated(item);
        }
    }

    private void onPrev() {
        animateButtonClick(mBinding.control.prev);
        int current = getEpisodePosition();
        if(current < 0) return; // Position might be invalid
        current = --current < 0 ? 0 : current;
        Episode item = (Episode) mEpisodeAdapter.get(current);
         if (item != null) { // Add null check
             if (item.isActivated()) Notify.show(mHistory != null && mHistory.isRevPlay() ? R.string.error_play_next : R.string.error_play_prev);
             else setEpisodeActivated(item);
         }
    }

    private void onScale() {
        animateButtonClick(mBinding.control.scale);
        int index = getScale();
        String[] array = ResUtil.getStringArray(R.array.select_scale);
        int newIndex = index >= array.length - 1 ? 0 : ++index;
        if (mHistory != null) mHistory.setScale(newIndex);
        setScale(newIndex);
    }

    private void onSpeed() {
        animateButtonClick(mBinding.control.speed);
        mBinding.control.speed.setText(mPlayers.addSpeed());
        if(mHistory != null) mHistory.setSpeed(mPlayers.getSpeed());
        updateButtonStyle(mBinding.control.speed, true);
    }

    private void onSpeedAdd() {
        mBinding.control.speed.setText(mPlayers.addSpeed(0.25f));
         if(mHistory != null) mHistory.setSpeed(mPlayers.getSpeed()); // Add null check
    }

    private void onSpeedSub() {
        mBinding.control.speed.setText(mPlayers.subSpeed(0.25f));
         if(mHistory != null) mHistory.setSpeed(mPlayers.getSpeed()); // Add null check
    }

    private boolean onSpeedLong() {
        mBinding.control.speed.setText(mPlayers.toggleSpeed());
        if(mHistory != null) mHistory.setSpeed(mPlayers.getSpeed()); // Add null check
        return true;
    }

    private void onRefresh() {
        onReset(false);
    }

    private void onReset() {
        animateButtonClick(mBinding.control.reset);
        onReset(isReplay());
    }

    private void onReset(boolean replay) {
        mClock.setCallback(null);
        if (mFlagAdapter.size() == 0) return;
        if (mEpisodeAdapter.size() == 0) return;
        Episode episode = getEpisode();
        Flag flag = getFlag();
        if(flag != null && episode != null){ // Add null checks
            getPlayer(flag, episode, replay);
        }
    }

    private boolean onResetToggle() {
        Setting.putReset(Math.abs(Setting.getReset() - 1));
        mBinding.control.reset.setText(ResUtil.getStringArray(R.array.select_reset)[Setting.getReset()]);
        return true;
    }

    private void onOpening() {
        long current = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        if (current < 0 || duration <= 0 || current > duration / 2) return; // Add duration check
        setOpening(current);
    }

    private void onOpeningAdd() {
        if (mHistory == null) return; // Add null check
        setOpening(Math.min(mHistory.getOpening() + 1000, mPlayers.getDuration() / 2));
    }

    private void onOpeningSub() {
        if (mHistory == null) return; // Add null check
        setOpening(Math.max(0, mHistory.getOpening() - 1000));
    }

    private boolean onOpeningReset() {
        setOpening(0);
        return true;
    }

    private void setOpening(long opening) {
         if (mHistory == null) return; // Add null check
        mHistory.setOpening(opening);
        mBinding.control.opening.setText(opening == 0 ? getString(R.string.play_op) : mPlayers.stringToTime(mHistory.getOpening()));
    }

    private void onEnding() {
        long current = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        if (current < 0 || duration <= 0 || current < duration / 2) return; // Add duration check
        setEnding(duration - current);
    }

    private void onEndingAdd() {
        if (mHistory == null) return; // Add null check
        setEnding(Math.min(mPlayers.getDuration() / 2, mHistory.getEnding() + 1000));
    }

    private void onEndingSub() {
         if (mHistory == null) return; // Add null check
        setEnding(Math.max(0, mHistory.getEnding() - 1000));
    }

    private boolean onEndingReset() {
        setEnding(0);
        return true;
    }

    private void setEnding(long ending) {
         if (mHistory == null) return; // Add null check
        mHistory.setEnding(ending);
        mBinding.control.ending.setText(ending == 0 ? getString(R.string.play_ed) : mPlayers.stringToTime(mHistory.getEnding()));
    }

    private boolean onChoose() {
        if (mPlayers.isEmpty()) return false;
        CharSequence title = mBinding.widget.title.getText(); // Get title from widget
        mPlayers.choose(this, title != null ? title : ""); // Pass title, handle null
        return true;
    }

    private void onPlayer() {
        animateButtonClick(mBinding.control.player);
        CharSequence title = mBinding.widget.title.getText();
        PlayerDialog.create().select(mPlayers.getPlayer()).title(title != null ? title.toString() : "").show(this);
        hideControl();
    }

    private void onDecode() {
        animateButtonClick(mBinding.control.decode);
        onDecode(true);
    }

    private void onDecode(boolean save) {
        mPlayers.toggleDecode(save);
        mPlayers.init(getExo(), getIjk());
        mPlayers.setMediaSource();
        setDecodeView();
    }

    private void onTrack(View view) {
        if (view == null || view.getTag() == null) return; // Add null checks
        TrackDialog.create().player(mPlayers).chooser(this).vod(true).type(Integer.parseInt(view.getTag().toString())).show(this);
        hideControl();
    }

    private void onToggle() {
        if (isVisible(mBinding.control.getRoot())) hideControl();
        else showControl(getFocus2());
    }

    private void showProgress() {
        mBinding.widget.progress.setVisibility(View.VISIBLE);
        App.post(mR3, 0);
        hideError();
    }

    private void hideProgress() {
        mBinding.widget.progress.setVisibility(View.GONE);
        App.removeCallbacks(mR3);
        Traffic.reset();
    }

    private void showError(String text) {
        mBinding.widget.error.setVisibility(View.VISIBLE);
        mBinding.widget.text.setText(text);
        hideProgress();
    }

    private void hideError() {
        mBinding.widget.error.setVisibility(View.GONE);
        mBinding.widget.text.setText("");
    }

    private void showInfo() {
        mBinding.widget.info.setVisibility(View.VISIBLE);
        showDisplayInfo();
    }

    private void hideInfo() {
        mBinding.widget.info.setVisibility(View.GONE);
        showDisplayInfo();
    }

    private void showInfoAndCenter() {
        showInfo();
        mBinding.widget.center.setVisibility(View.VISIBLE);
    }

    private void hideInfoAndCenter() {
        hideInfo();
        mBinding.widget.center.setVisibility(View.GONE);
    }


    private void showControl(View view) {
        if (mIsControlAnimating) return;
        
        if (view == null) view = getFocus2();
        if (view == null) view = mBinding.control.next;
        
        // 设置弹幕按钮可见性
        mBinding.control.danmu.setVisibility(mBinding.danmaku.isPrepared() ? View.VISIBLE : View.GONE);
        
        // 设置选集按钮可见性
        mBinding.control.episodes.setVisibility(Setting.getFullscreenMenuKey() == 0 ? View.VISIBLE : View.GONE);
        
        // 更新按钮状态
        updateControlButtonStates();
        
        // 显示控制栏
        mBinding.control.getRoot().setVisibility(View.VISIBLE);
        
        // 创建并执行显示动画
        createShowAnimation();
        
        if (view != null) view.requestFocus();
        setControlNextFocus();
        setR1Callback();
    }

    private void createShowAnimation() {
        if (mControlShowAnimator != null && mControlShowAnimator.isRunning()) {
            mControlShowAnimator.cancel();
        }
        
        mIsControlAnimating = true;
        
        // 淡入动画
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(
            mBinding.control.getRoot(), "alpha", 0f, 1f
        );
        fadeIn.setDuration(300);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        
        // 从底部滑入动画
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(
            mBinding.control.getRoot(), "translationY", 200f, 0f
        );
        slideUp.setDuration(350);
        slideUp.setInterpolator(new DecelerateInterpolator(1.5f));
        
        // 按钮组缩放动画
        animateControlGroups(true);
        
        mControlShowAnimator = new AnimatorSet();
        mControlShowAnimator.playTogether(fadeIn, slideUp);
        mControlShowAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsControlAnimating = false;
            }
        });
        mControlShowAnimator.start();
    }

    private void hideControl() {
        hideControl(true);
    }

    private void hideControl(boolean hideInfo) {
        if (mIsControlAnimating) return;
        
        if (hideInfo) hideInfo();
        
        // 重置文字
        mBinding.control.text.setText(R.string.play_track_text);
        
        // 创建并执行隐藏动画
        createHideAnimation();
        
        App.removeCallbacks(mR1);
    }

    private void createHideAnimation() {
        if (mControlHideAnimator != null && mControlHideAnimator.isRunning()) {
            mControlHideAnimator.cancel();
        }
        
        mIsControlAnimating = true;
        
        // 淡出动画
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(
            mBinding.control.getRoot(), "alpha", 1f, 0f
        );
        fadeOut.setDuration(200);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        
        // 向下滑出动画
        ObjectAnimator slideDown = ObjectAnimator.ofFloat(
            mBinding.control.getRoot(), "translationY", 0f, 100f
        );
        slideDown.setDuration(200);
        slideDown.setInterpolator(new AccelerateInterpolator());
        
        mControlHideAnimator = new AnimatorSet();
        mControlHideAnimator.playTogether(fadeOut, slideDown);
        mControlHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBinding.control.getRoot().setVisibility(View.GONE);
                mIsControlAnimating = false;
            }
        });
        mControlHideAnimator.start();
    }

    // 动画化控制按钮组
    private void animateControlGroups(boolean show) {
        ViewGroup actionLayout = mBinding.control.actionLayout;
        int childCount = actionLayout.getChildCount();
        
        for (int i = 0; i < childCount; i++) {
            View child = actionLayout.getChildAt(i);
            // 这里不需要检查 LinearLayout，直接处理所有子视图
            animateSingleButton(child, i * 50L, show);
        }
    }

    private void animateGroupEntry(View group, long delay, boolean show) {
        group.setScaleX(show ? 0.8f : 1f);
        group.setScaleY(show ? 0.8f : 1f);
        group.setAlpha(show ? 0f : 1f);
        
        group.animate()
            .scaleX(show ? 1f : 0.8f)
            .scaleY(show ? 1f : 0.8f)
            .alpha(show ? 1f : 0f)
            .setStartDelay(show ? delay : 0)
            .setDuration(show ? 300 : 200)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private void animateSingleButton(View button, long delay, boolean show) {
        if (button == null) return;
        
        button.setAlpha(show ? 0f : 1f);
        button.animate()
            .alpha(show ? 1f : 0f)
            .setStartDelay(show ? delay : 0)
            .setDuration(show ? 300 : 200)
            .start();
    }

        private void updateControlButtonStates() {
        // 更新播放器按钮文字
        mBinding.control.player.setText(mPlayers.getPlayerText());
        
        // 更新解码按钮文字
        mBinding.control.decode.setText(mPlayers.getDecodeText());
        
        // 更新倍速按钮
        mBinding.control.speed.setText(mPlayers.getSpeedText());
        mBinding.control.speed.setEnabled(mPlayers.canAdjustSpeed());
        updateButtonStyle(mBinding.control.speed, mPlayers.canAdjustSpeed());
        
        // 更新比例按钮
        mBinding.control.scale.setText(ResUtil.getStringArray(R.array.select_scale)[getScale()]);
        
        // 更新循环按钮状态
        mBinding.control.loop.setActivated(mBinding.control.loop.isActivated());
        updateButtonStyle(mBinding.control.loop, true);
        
        // 更新弹幕按钮状态
        mBinding.control.danmu.setActivated(Setting.isDanmu());
        updateButtonStyle(mBinding.control.danmu, mBinding.danmaku.isPrepared());
        
        // 更新轨道按钮
        updateTrackButtons();
        
        // 更新时间按钮
        updateTimeButtons();
    }
    private void updateButtonStyle(TextView button, boolean enabled) {
        if (button == null) return;
        
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1.0f : 0.5f);
        
        // 为激活状态的按钮添加特殊效果
        if (button.isActivated() && enabled) {
            addGlowEffect(button);
        } else {
            removeGlowEffect(button);
        }
    }

    // 添加发光效果
    private void addGlowEffect(View view) {
        view.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(200)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    // 移除发光效果
    private void removeGlowEffect(View view) {
        view.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(200)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    // 更新轨道按钮
    private void updateTrackButtons() {
        boolean hasText = mPlayers.haveTrack(C.TRACK_TYPE_TEXT) || mPlayers.isExo();
        boolean hasAudio = mPlayers.haveTrack(C.TRACK_TYPE_AUDIO);
        boolean hasVideo = mPlayers.haveTrack(C.TRACK_TYPE_VIDEO);
        
        mBinding.control.text.setVisibility(hasText ? View.VISIBLE : View.GONE);
        mBinding.control.audio.setVisibility(hasAudio ? View.VISIBLE : View.GONE);
        mBinding.control.video.setVisibility(hasVideo ? View.VISIBLE : View.GONE);
        
        // 修复类型转换问题
        ViewParent parent = mBinding.control.text.getParent();
        if (parent instanceof View) {
            View trackGroup = (View) parent;
            if (trackGroup instanceof LinearLayout) {
                trackGroup.setVisibility((hasText || hasAudio || hasVideo) ? View.VISIBLE : View.GONE);
            }
        }
    }
        // 更新时间按钮
    private void updateTimeButtons() {
        if (mHistory != null) {
            String openingText = mHistory.getOpening() == 0 ? 
                getString(R.string.play_op) : 
                mPlayers.stringToTime(mHistory.getOpening());
            String endingText = mHistory.getEnding() == 0 ? 
                getString(R.string.play_ed) : 
                mPlayers.stringToTime(mHistory.getEnding());
                
            mBinding.control.opening.setText(openingText);
            mBinding.control.ending.setText(endingText);
            
            // 高亮已设置的时间按钮
            updateButtonStyle(mBinding.control.opening, true);
            updateButtonStyle(mBinding.control.ending, true);
            
            if (mHistory.getOpening() > 0) {
                mBinding.control.opening.setActivated(false);
            }
            if (mHistory.getEnding() > 0) {
                mBinding.control.ending.setActivated(false);
            }
        }
    }

    // 优化焦点设置
    private void setControlNextFocus() {
        ViewGroup actionLayout = mBinding.control.actionLayout;
        View firstFocusable = null;
        View lastFocusable = null;
        View previousFocusable = null;
        
        // 遍历所有子视图设置焦点
        for (int i = 0; i < actionLayout.getChildCount(); i++) {
            View child = actionLayout.getChildAt(i);
            
            if (child instanceof LinearLayout) {
                // 处理按钮组
                LinearLayout group = (LinearLayout) child;
                for (int j = 0; j < group.getChildCount(); j++) {
                    View button = group.getChildAt(j);
                    if (button.isEnabled() && button.getVisibility() == View.VISIBLE) {
                        setupButtonFocus(button, previousFocusable);
                        if (firstFocusable == null) firstFocusable = button;
                        lastFocusable = button;
                        previousFocusable = button;
                    }
                }
            } else if (child.isEnabled() && child.getVisibility() == View.VISIBLE) {
                // 处理单个按钮
                setupButtonFocus(child, previousFocusable);
                if (firstFocusable == null) firstFocusable = child;
                lastFocusable = child;
                previousFocusable = child;
            }
        }
        
        // 设置循环焦点
        if (firstFocusable != null && lastFocusable != null) {
            firstFocusable.setNextFocusLeftId(lastFocusable.getId());
            lastFocusable.setNextFocusRightId(firstFocusable.getId());
        }
    }

    private void setupButtonFocus(View button, View previous) {
        // 设置上下焦点
        button.setNextFocusUpId(button.getId());
        button.setNextFocusDownId(mBinding.control.seek.getId());
        
        // 设置左右焦点
        if (previous != null) {
            button.setNextFocusLeftId(previous.getId());
            previous.setNextFocusRightId(button.getId());
        }
    }

    // 按钮点击优化 - 添加点击动画
    private void animateButtonClick(View button) {
        button.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setInterpolator(new DecelerateInterpolator())
            .withEndAction(() -> {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            })
            .start();
    }


    private void hideCenter() {
        mBinding.widget.action.setImageResource(R.drawable.ic_widget_play);
        mBinding.widget.center.setVisibility(View.GONE);
    }

    private void showPreview(Drawable preview) {
        if (preview == null || Setting.getFlag() == 0 || isGone(mBinding.widget.preview)) return; // Add null check
        mBinding.widget.preview.setVisibility(View.VISIBLE);
        mBinding.widget.preview.setImageDrawable(preview);
    }

    private void hidePreview() {
        mBinding.widget.preview.setVisibility(View.GONE);
        mBinding.widget.preview.setImageDrawable(null);
    }

    private void setTraffic() {
        Traffic.setSpeed(mBinding.widget.traffic);
        App.post(mR3, Constant.INTERVAL_TRAFFIC);
    }

    private void setR1Callback() {
        App.post(mR1, Constant.INTERVAL_HIDE);
    }

    private void setR2Callback(long delayMillis) {
        App.post(mR2, delayMillis);
    }

    private void setArtwork(String url) {
        ImgUtil.load(url, R.drawable.radio, new CustomTarget<>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                getExo().setDefaultArtwork(resource);
                getIjk().setDefaultArtwork(resource);
                showPreview(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable error) {
                 // Don't set error drawable as default artwork, keep previous one or placeholder
                 // getExo().setDefaultArtwork(error);
                 // getIjk().setDefaultArtwork(error);
                hidePreview();
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                 // Optionally reset to placeholder if needed
                 // getExo().setDefaultArtwork(placeholder);
                 // getIjk().setDefaultArtwork(placeholder);
            }
        });
    }


    private void getPart(String source) {
        if(TextUtils.isEmpty(source)) return; // Add null/empty check
        try {
            String encodedSource = URLEncoder.encode(source.trim(), "UTF-8");
             OkHttp.newCall("https://api.yesapi.cn/?service=App.Scws.GetWords&app_key=CEE4B8A091578B252AC4C92FB4E893C3&text=" + encodedSource)
                   .enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    List<String> items = new ArrayList<>(); // Initialize list
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                             items = Part.get(response.body().string());
                        } catch(Exception e) {
                            Log.e("VideoActivity", "Error parsing part response", e);
                            // Fallback to source if parsing fails
                            items = new ArrayList<>(Arrays.asList(source));
                        }
                    } else {
                         // Fallback to source on network failure
                         items = new ArrayList<>(Arrays.asList(source));
                    }

                    if (!items.contains(source)) {
                        items.add(0, source);
                    }
                    final List<String> finalItems = items; // Final variable for lambda
                    App.post(() -> setPartAdapter(finalItems), 1000);
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                     Log.e("VideoActivity", "Failed to get parts", e);
                    List<String> items = Arrays.asList(source);
                    App.post(() -> setPartAdapter(items), 1000);
                }
            });
        } catch (Exception e){
             Log.e("VideoActivity", "Error encoding source for getPart", e);
             List<String> items = Arrays.asList(source);
             App.post(() -> setPartAdapter(items), 1000); // Fallback on encoding error
        }
    }

    private void setPartAdapter(List<String> items) {
        if (items == null || items.isEmpty()) { // Add null/empty check
            mBinding.part.setVisibility(View.GONE);
            return;
        }
        mBinding.part.setVisibility(View.VISIBLE);
        mPartAdapter.setItems(items, null);
        setR2Callback(1000);
    }

    private void checkFlag(Vod item) {
        if (item == null || item.getVodFlags() == null) { // Add null checks
             mBinding.flag.setVisibility(View.GONE);
             ErrorEvent.flag(); // Keep error event if item or flags are null
             return;
        }
        boolean empty = item.getVodFlags().isEmpty();
        mBinding.flag.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            ErrorEvent.flag();
        } else {
            if (mHistory != null) { // Add null check for history
                setFlagActivated(mHistory.getFlag());
                 if (mHistory.isRevSort()) reverseEpisode(true);
            } else {
                // If history is null, activate the first flag
                setFlagActivated(item.getVodFlags().get(0));
            }
        }
    }

    // --- Modified checkHistory ---
    private void checkHistory(Vod item) {
        if (item == null) return; // Cannot check history without item
        mHistory = History.find(getHistoryKey());
        mHistory = mHistory == null ? createHistory(item) : mHistory; // createHistory uses item.getVodName()

        // Ensure History has the correct (original) VodName from the item
        if (mHistory != null && !item.getVodName().equals(mHistory.getVodName())) {
            mHistory.setVodName(item.getVodName());
        }

        if (mHistory != null) { // Proceed only if history is not null
            if (!TextUtils.isEmpty(getMark())) mHistory.setVodRemarks(getMark());
            if (Setting.isIncognito() && mHistory.getKey().equals(getHistoryKey())) mHistory.delete();
            mBinding.control.opening.setText(mHistory.getOpening() == 0 ? getString(R.string.play_op) : mPlayers.stringToTime(mHistory.getOpening()));
            mBinding.control.ending.setText(mHistory.getEnding() == 0 ? getString(R.string.play_ed) : mPlayers.stringToTime(mHistory.getEnding()));
            mHistory.setVodPic(item.getVodPic(getPic())); // Use pic from item, considering fallback
            mPlayers.setPlayer(getPlayer()); // getPlayer already handles null history
            setScale(getScale()); // getScale already handles null history
            setPlayerView(); // setPlayerView handles null history indirectly via getPlayer/getScale
            setDecodeView();
        } else {
            // Handle case where history is still null (e.g., DB error)
            // Maybe set defaults directly?
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
        History history = new History();
        history.setKey(getHistoryKey());
        history.setCid(VodConfig.getCid());
        history.setVodName(item.getVodName()); // Use original name from VOD item
        history.findEpisode(item.getVodFlags());
        history.setSpeed(Setting.getPlaySpeed());
        return history;
    }
    // --- End Modified createHistory ---

    private void updateHistory(Episode item, boolean replay) {
        if (mHistory == null || item == null || getFlag() == null) return; // Add null checks
        replay = replay || !item.equals(mHistory.getEpisode());
        long position = replay ? 0 : mHistory.getPosition();
        mHistory.setPosition(position);
        mHistory.setEpisodeUrl(item.getUrl());
        mHistory.setVodRemarks(item.getName());
        mHistory.setVodFlag(getFlag().getFlag());
        mHistory.setCreateTime(System.currentTimeMillis());
        mPlayers.setPosition(Math.max(mHistory.getOpening(), mHistory.getPosition()));
    }


    private void checkKeep() {
        mBinding.keep.setCompoundDrawablesWithIntrinsicBounds(Keep.find(getHistoryKey()) == null ? R.drawable.ic_detail_keep_off : R.drawable.ic_detail_keep_on, 0, 0, 0);
    }

    // --- Modified createKeep ---
    private void createKeep() {
        Keep keep = new Keep();
        keep.setKey(getHistoryKey());
        keep.setCid(VodConfig.getCid());
        keep.setSiteName(getSite() != null ? getSite().getName() : ""); // Add null check for site
        Object tag = mBinding.video.getTag();
        keep.setVodPic(tag != null ? tag.toString() : getPic()); // Use fallback pic if tag is null
        // Use stored original VOD name
        keep.setVodName(currentVodName); // <-- MODIFIED
        keep.setCreateTime(System.currentTimeMillis());
        keep.save();
    }
    // --- End Modified createKeep ---


    @Override
    public void showChooser(TrackDialog dialog) {
        if(dialog == null) return; // Add null check
        FileChooserDialog.create().player(mPlayers).trackDialog(dialog).show(this);
    }

    @Override
    public void onTrackClick(Track item) {
        if(item == null) return; // Add null check
        item.setKey(getHistoryKey());
        item.save();
    }

    @Override
    public void onSubtitleClick() {
        App.post(this::hideControl, 200);
        SubtitleView subtitleView = mPlayers.isIjk() ? getIjk().getSubtitleView() : getExo().getSubtitleView();
        if (subtitleView != null) { // Add null check
            App.post(() -> SubtitleDialog.create().view(subtitleView).full(isFullscreen()).show(this), 200);
        }
    }

    @Override
    public void onTimeChanged() {
        // 不要调用 super.onTimeChanged()，因为接口中没有默认实现
        onTimeChangeDisplaySpeed();
        if (mHistory == null) return;

        long position = mPlayers.getPosition();
        long duration = mPlayers.getDuration();

        if (position >= 0 && duration > 0) {
            mHistory.setPosition(position);
            mHistory.setDuration(duration);
            if (!Setting.isIncognito()) {
                App.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mHistory != null) {
                            mHistory.update();
                        }
                    }
                });
            }
        }

        if (mHistory.getEnding() > 0 && duration > 0 && mHistory.getEnding() + position >= duration) {
            mClock.setCallback(null);
            checkNext();
        }

        if (mSeekView != null) {
            mSeekView.updateProgress();
        }
         // 更新时间显示
        if (mCurrentTime != null && mTotalTime != null && mPlayers != null) {
            mCurrentTime.setText(mPlayers.getPositionTime(0));
            mTotalTime.setText(mPlayers.getDurationTime());
        }
    }

    private void updateSeekBarSmoothly() {
        // 这里可以实现进度条的平滑更新逻辑
        // 避免进度条跳动
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
        if (event == null || isBackground()) return; // Add null check
        if (event.getType() == RefreshEvent.Type.DETAIL) getDetail();
        else if (event.getType() == RefreshEvent.Type.PLAYER) onRefresh();
        else if (event.getType() == RefreshEvent.Type.DANMAKU) checkDanmu(event.getPath());
        else if (event.getType() == RefreshEvent.Type.SUBTITLE) mPlayers.setSub(Sub.from(event.getPath()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerEvent(PlayerEvent event) {
        if (event == null || isBackground()) return; // Add null check
        switch (event.getState()) {
            case 0: // Player.STATE_LOADING or equivalent custom state
                setInitTrack(true);
                setTrackVisible(false);
                mClock.setCallback(this);
                break;
            case Player.STATE_IDLE:
                // Maybe show preview or initial state?
                break;
            case Player.STATE_BUFFERING:
                showProgress();
                break;
            case Player.STATE_READY:
                stopSearch(); // Stop site search if playing successfully
                setMetadata(); // Uses original name from history
                resetToggle();
                resetError();
                hideProgress();
                mPlayers.reset(); // Reset player internal state if needed after ready
                setDefaultTrack();
                setTrackVisible(true);
                if (mHistory != null) { // Save player type to history
                    mHistory.setPlayer(mPlayers.getPlayer());
                }
                mBinding.widget.size.setText(mPlayers.getSizeText());
                mBinding.display.size.setText(mPlayers.getSizeText());
                break;
            case Player.STATE_ENDED:
                checkEnded();
                break;
        }
    }


    private void checkEnded() {
        if (mBinding.control.loop.isActivated()) {
            onReset(true);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            checkNext();
        }
    }

    private void setTrackVisible(boolean visible) {
        mBinding.control.text.setVisibility(visible && (mPlayers.haveTrack(C.TRACK_TYPE_TEXT) || mPlayers.isExo()) ? View.VISIBLE : View.GONE);
        mBinding.control.audio.setVisibility(visible && mPlayers.haveTrack(C.TRACK_TYPE_AUDIO) ? View.VISIBLE : View.GONE);
        mBinding.control.video.setVisibility(visible && mPlayers.haveTrack(C.TRACK_TYPE_VIDEO) ? View.VISIBLE : View.GONE);
    }

    private void setDefaultTrack() {
        if (isInitTrack()) {
            setInitTrack(false);
            mPlayers.prepared();
            mPlayers.setTrack(Track.find(getHistoryKey()));
        }
    }

    // --- Modified setMetadata ---
    private void setMetadata() {
        // Ensure uses original name from history
        String title = (mHistory != null) ? mHistory.getVodName() : currentVodName; // Use history or stored name
        Episode episode = getEpisode(); // Get current episode
        String episodeName = (episode != null) ? episode.getName() : ""; // Handle null episode
        String artist = title.equals(episodeName) ? "" : getString(R.string.play_now, episodeName);
        String pic = (mHistory != null) ? mHistory.getVodPic() : getPic(); // Use history pic or intent pic
        mPlayers.setMetadata(title, artist, pic, getDefaultArtwork());
    }
    // --- End Modified setMetadata ---


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(ErrorEvent event) {
        if (event == null || isBackground()) return; // Add null check
        if (addErrorCount() > 20) onErrorEnd(event);
        else if (mPlayers.addRetry() > event.getRetry()) checkError(event);
        else if (event.isDecode() && mPlayers.canToggleDecode()) onDecode(false);
        else if (event.isExo() && mPlayers.isExo()) onExoCheck(event);
        else onRefresh();
    }

    private void onExoCheck(ErrorEvent event) {
         if (event == null) return; // Add null check
        if (event.getCode() == PlaybackException.ERROR_CODE_IO_UNSPECIFIED || event.getCode() >= PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED && event.getCode() <= PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED) {
            mPlayers.setFormat(ExoUtil.getMimeType(event.getCode()));
        }
        mPlayers.setMediaSource();
    }

    private void checkError(ErrorEvent event) {
        if (event == null) return; // Add null check
        Site site = getSite();
        if (site != null && site.getPlayerType() == -1 && event.isUrl() && event.getRetry() > 0 && getToggleCount() < 2 && mPlayers.getPlayer() != Players.SYS) { // Add site null check
            toggleCount++;
            nextPlayer();
        } else {
            resetToggle();
            onError(event);
        }
    }

    private void nextPlayer() {
        mPlayers.nextPlayer();
        setPlayerView();
        setDecodeView();
        onRefresh();
    }

    private void onErrorEnd(ErrorEvent event) {
         if (event == null) return; // Add null check
        onErrorPlayer(event);
        resetError();
    }

    private void onErrorPlayer(ErrorEvent event) {
        if (event == null) return; // Add null check
        Track.delete(getHistoryKey());
        showError(event.getMsg());
        mClock.setCallback(null);
        mPlayers.reset();
        mPlayers.stop();
    }

    private void onError(ErrorEvent event) {
        if (event == null) return; // Add null check
        onErrorPlayer(event);
        startFlow();
    }

    private void startFlow() {
        Site site = getSite();
        if (site == null || !site.isChangeable()) return; // Add null check
        if (isUseParse()) checkParse();
        else checkFlag();
    }

    private void checkParse() {
        int position = getParsePosition();
        boolean last = position >= mParseAdapter.size() - 1; // Use >= for safety
        boolean pass = position == 0 || last;
        if (last) initParse();
        if (pass) checkFlag();
        else nextParse(position);
    }

    private void initParse() {
        if (mParseAdapter.size() == 0) return;
        VodConfig.get().setParse((Parse) mParseAdapter.get(0));
        notifyItemChanged(mBinding.control.parse, mParseAdapter);
    }

    private void checkFlag() {
        int position = isGone(mBinding.flag) ? -1 : getFlagPosition();
         if (position >= mFlagAdapter.size() - 1) checkSearch(false); // Use >= for safety
        else nextFlag(position);
    }

    // --- Modified checkSearch ---
    private void checkSearch(boolean force) {
        // Use stored original VOD name for initial search keyword
        if (mQuickAdapter.size() == 0) {
            // Make sure currentVodName is initialized before calling initSearch
             if(TextUtils.isEmpty(currentVodName)) {
                 // Try getting from intent again if detail loading failed initially
                 currentVodName = getName();
             }
             // Only start search if we have a name
             if(!TextUtils.isEmpty(currentVodName)) {
                 initSearch(currentVodName, true);
             } else {
                  Log.w("VideoActivity", "Cannot start search, currentVodName is empty.");
                  // Optionally show an error or do nothing
                  showEmpty(); // Show empty state if no name available
             }
        } else if (isAutoMode() || force) {
             nextSite();
        }
    }
    // --- End Modified checkSearch ---

    private void initSearch(String keyword, boolean auto) {
        if(TextUtils.isEmpty(keyword)) return; // Don't search empty keyword
        stopSearch();
        setAutoMode(auto);
        setInitAuto(auto);
        startSearch(keyword);
        mBinding.part.setTag(keyword);
    }

    private boolean isPass(Site item) {
        if(item == null) return false; // Add null check
        if (isAutoMode() && !item.isChangeable()) return false;
        return item.isSearchable();
    }

    private void startSearch(String keyword) {
         if(TextUtils.isEmpty(keyword)) return; // Don't search empty keyword
        mQuickAdapter.clear();
        List<Site> sites = new ArrayList<>();
        mExecutor = Executors.newFixedThreadPool(Constant.THREAD_POOL);
        for (Site site : VodConfig.get().getSites()) if (isPass(site)) sites.add(site);
        for (Site site : sites) mExecutor.execute(() -> search(site, keyword));
    }

    private void stopSearch() {
        if (mExecutor == null) return;
        try { // Add try-catch for shutdownNow
            mExecutor.shutdownNow();
        } catch (Exception e) {
             Log.e("VideoActivity", "Error shutting down search executor", e);
        }
        mExecutor = null;
    }

    private void search(Site site, String keyword) {
        if(site == null || TextUtils.isEmpty(keyword)) return; // Add null checks
        try {
            mViewModel.searchContent(site, keyword, true);
        } catch (Throwable ignored) {
             Log.w("VideoActivity", "Search ignored for site: " + site.getName() + ", keyword: " + keyword);
        }
    }

    private void setSearch(Result result) {
        if (result == null || result.getList() == null) return; // Add null checks
        List<Vod> items = result.getList();
        Iterator<Vod> iterator = items.iterator();
        while (iterator.hasNext()) if (mismatch(iterator.next())) iterator.remove();
        mQuickAdapter.addAll(mQuickAdapter.size(), items);
        mBinding.quick.setVisibility(View.VISIBLE);
        if (isInitAuto()) nextSite();
        if (items.isEmpty()) return;
        App.removeCallbacks(mR4);
    }

    private void setSearch(Vod item) {
        if (item == null) return; // Add null check
        setAutoMode(false);
        getDetail(item);
    }

    private boolean mismatch(Vod item) {
        if (item == null) return true; // Mismatch if item is null
        if (getId().equals(item.getVodId())) return true;
        if (mBroken.contains(item.getVodId())) return true;
        String keyword = Objects.toString(mBinding.part.getTag(), "");
         // Add null check for item name
         String itemName = item.getVodName() != null ? item.getVodName() : "";
        if (isAutoMode()) return !itemName.equals(keyword);
        else return !itemName.contains(keyword);
    }

    private void nextParse(int position) {
        if (position + 1 >= mParseAdapter.size()) return; // Check bounds
        Parse parse = (Parse) mParseAdapter.get(position + 1);
        if(parse != null) { // Add null check
            Notify.show(getString(R.string.play_switch_parse, parse.getName()));
            setParseActivated(parse);
        }
    }

    private void nextFlag(int position) {
        if (position + 1 >= mFlagAdapter.size()) return; // Check bounds
        Flag flag = (Flag) mFlagAdapter.get(position + 1);
        if(flag != null) { // Add null check
            Notify.show(getString(R.string.play_switch_flag, flag.getFlag()));
            setFlagActivated(flag);
        }
    }

    private void nextSite() {
        if (mQuickAdapter.size() == 0) return;
        Vod item = (Vod) mQuickAdapter.get(0);
        if (item == null) { // Remove null item and try again if possible
            mQuickAdapter.removeItems(0, 1);
            nextSite(); // Recursive call, be cautious with large empty lists
            return;
        }
        Notify.show(getString(R.string.play_switch_site, item.getSiteName()));
        mQuickAdapter.removeItems(0, 1);
        mBroken.add(getId());
        setInitAuto(false);
        getDetail(item);
    }


    private void onPaused() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBinding.widget.exoDuration.setText(mPlayers.getDurationTime());
        mBinding.widget.exoPosition.setText(mPlayers.getPositionTime(0));
        if (isFullscreen()) showInfoAndCenter();
        else hideInfoAndCenter();
        mPlayers.pause();
    }

    private void onPlay() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPlayers.play();
        hideCenter();
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
        groupSize = size;
    }

    private View getFocus1() {
        return mFocus1 == null ? mBinding.video : mFocus1;
    }

    private View getFocus2() {
        // Ensure focus defaults to a valid view if initial focus target is null or specific buttons
        View focusTarget = mFocus2;
        if (focusTarget == null || focusTarget == mBinding.control.opening || focusTarget == mBinding.control.ending) {
            focusTarget = mBinding.control.next; // Default to 'next' button
        }
        // Final fallback if even 'next' is somehow not focusable (though unlikely)
        if (focusTarget == null || !focusTarget.isFocusable()) {
             focusTarget = mBinding.control.getRoot().findFocus(); // Try finding any focusable element in control layout
             if(focusTarget == null) focusTarget = mBinding.video; // Absolute fallback
        }
        return focusTarget;
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event == null) return super.dispatchKeyEvent(null); // Basic null check
        hasKeyEvent = true;
        // Check if focus is valid before comparing
        View currentFocus = getCurrentFocus();
        if (currentFocus != null && mBinding.progressLayout.isContent() && !isFullscreen() && KeyUtil.isBackKey(event) && Setting.getSmallWindowBackKey() == 1 && currentFocus != mBinding.video) {
            mFocus1 = mBinding.video;
            if (getFocus1() != null) getFocus1().requestFocus(); // Null check before requesting focus
            return true;
        }
        if (isFullscreen() && KeyUtil.isMenuKey(event) && Setting.getFullscreenMenuKey() == 0) onToggle();
        if (isFullscreen() && KeyUtil.isMenuKey(event) && Setting.getFullscreenMenuKey() == 1) onEpisodes();
        if (isVisible(mBinding.control.getRoot())) setR1Callback();
        if (isVisible(mBinding.control.getRoot()) && currentFocus != null) mFocus2 = currentFocus; // Store focus only if valid
        if (isFullscreen() && isGone(mBinding.control.getRoot()) && mKeyDown != null && mKeyDown.hasEvent(event)) { // Add null check for mKeyDown
             return mKeyDown.onKeyDown(event);
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public void onBright(int progress) {
        mBinding.widget.bright.setVisibility(View.VISIBLE);
        mBinding.widget.brightProgress.setProgress(progress);
        if (progress < 35) mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_low);
        else if (progress < 70) mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_medium);
        else mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_high);
    }

    @Override
    public void onBrightEnd() {
        mBinding.widget.bright.setVisibility(View.GONE);
    }

    @Override
    public void onVolume(int progress) {
        mBinding.widget.volume.setVisibility(View.VISIBLE);
        mBinding.widget.volumeProgress.setProgress(progress);
        if (progress < 35) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_low);
        else if (progress < 70) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_medium);
        else mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_high);
    }

    @Override
    public void onVolumeEnd() {
        mBinding.widget.volume.setVisibility(View.GONE);
    }

    @Override
    public void onSeeking(int time) {
        mBinding.widget.exoDuration.setText(mPlayers.getDurationTime());
        mBinding.widget.exoPosition.setText(mPlayers.getPositionTime(time));
        mBinding.widget.action.setImageResource(time > 0 ? R.drawable.ic_widget_forward : R.drawable.ic_widget_rewind);
        mBinding.widget.center.setVisibility(View.VISIBLE);
        hideProgress();
    }

    @Override
    public void onSeekTo(int time) {
        mPlayers.seekTo(time);
        if (mKeyDown != null) mKeyDown.resetTime(); // Add null check
        showProgress();
        onPlay();
    }

    @Override
    public void onSpeedUp() {
        if (!mPlayers.isPlaying() || !mPlayers.canAdjustSpeed()) return;
        mBinding.control.speed.setText(mPlayers.setSpeed(mPlayers.getSpeed() < 3 ? 3 : 5));
        mBinding.widget.speed.startAnimation(ResUtil.getAnim(R.anim.forward));
        mBinding.widget.speed.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSpeedEnd() {
        if(mHistory != null) { // Add null check
             mBinding.control.speed.setText(mPlayers.setSpeed(mHistory.getSpeed()));
        }
        mBinding.widget.speed.setVisibility(View.GONE);
        mBinding.widget.speed.clearAnimation();
    }

    @Override
    public void onKeyUp() {
        long current = mPlayers.getPosition();
        long duration = mPlayers.getDuration();
        long half = duration > 0 ? duration / 2 : 0; // Calculate half safely
        showInfo();
        showControl(current < half ? mBinding.control.opening : mBinding.control.ending);
    }

    @Override
    public void onKeyDown() {
        showInfo();
        showControl(getFocus2());
    }

    @Override
    public void onKeyCenter() {
        if (mPlayers.isPlaying()) {
            onPaused();
            hideControl(false);
        } else {
            onPlay();
            hideControl(true);
        }
    }

    @Override
    public void onSingleTap() {
        if (isFullscreen()) onToggle();
    }

    @Override
    public void onDoubleTap() {
        if (isFullscreen()) onKeyCenter();
    }

    @Override
    public void onPlayerClick(Integer item) {
        if (item == null) return; // Add null check
        mPlayers.setPlayer(item);
        setPlayerView();
        setDecodeView();
        onRefresh();
    }

    @Override
    public void onPlayerShare(String title) {
        this.onChoose();
    }

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
                 if (data != null) mPlayers.checkData(data); // Add null check for data
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setBackground(false);
        mClock.start();
        onPlay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setBackground(true);
        mPlayers.pause();
        mClock.stop();
    }

    @Override
    public void onBackPressed() {
        if (isVisible(mBinding.control.getRoot())) {
            hideControl();
        } else if (isVisible(mBinding.widget.center)) {
            hideCenter();
        } else if (isFullscreen()) {
            exitFullscreen();
        } else {
            stopSearch();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (mControlShowAnimator != null) {
            mControlShowAnimator.cancel();
            mControlShowAnimator = null;
        }
        if (mControlHideAnimator != null) {
            mControlHideAnimator.cancel();
            mControlHideAnimator = null;
        }
        super.onDestroy();
        stopSearch();
        mClock.release();
        mPlayers.release();
        Source.get().stop();
        RefreshEvent.history();
        App.removeCallbacks(mR1, mR2, mR3, mR4);

        try {
             // Check if activity is finishing or destroyed before calling Glide
             if (!isFinishing() && !isDestroyed()) {
                Glide.with(this).onDestroy();
             }
        } catch (Exception e) {
             Log.e("VideoActivity", "Error during Glide onDestroy", e);
        }

        // Defensive clearing, ensure context is valid
        try {
            if (!isFinishing() && !isDestroyed()) {
                if (mBinding.exo != null) {
                     Glide.with(this).clear(mBinding.exo);
                }
                if (mBinding.ijk != null) {
                     Glide.with(this).clear(mBinding.ijk);
                }
                if (mBinding.widget != null && mBinding.widget.preview != null) {
                    Glide.with(this).clear(mBinding.widget.preview);
                }
                if (mBinding.video != null) {
                     Glide.with(this).clear(mBinding.video);
                }
            }
        } catch (Exception e) {
             Log.e("VideoActivity", "Error during Glide clear", e);
        }
    }
}
