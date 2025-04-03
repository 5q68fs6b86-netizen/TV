package com.fongmi.android.tv.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.utils.Sniffer;
import com.github.catvod.utils.Trans;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Root(strict = false)
public class Vod implements Parcelable {

    // --- Fields (no changes here) ---
    @Element(name = "id", required = false)
    @SerializedName("vod_id")
    private String vodId;

    @Element(name = "name", required = false)
    @SerializedName("vod_name")
    private String vodName;

    @Element(name = "type", required = false)
    @SerializedName("type_name")
    private String typeName;

    @Element(name = "pic", required = false)
    @SerializedName("vod_pic")
    private String vodPic;

    @Element(name = "note", required = false)
    @SerializedName("vod_remarks")
    private String vodRemarks;

    @Element(name = "year", required = false)
    @SerializedName("vod_year")
    private String vodYear;

    @Element(name = "area", required = false)
    @SerializedName("vod_area")
    private String vodArea;

    @Element(name = "director", required = false)
    @SerializedName("vod_director")
    private String vodDirector;

    @Element(name = "actor", required = false)
    @SerializedName("vod_actor")
    private String vodActor;

    @Element(name = "des", required = false)
    @SerializedName("vod_content")
    private String vodContent;

    @SerializedName("vod_play_from")
    private String vodPlayFrom;

    @SerializedName("vod_play_url")
    private String vodPlayUrl;

    @SerializedName("vod_tag")
    private String vodTag;

    @SerializedName("action")
    private String action;

    @SerializedName("cate")
    private Cate cate;

    @SerializedName("style")
    private Style style;

    @SerializedName("land")
    private int land;

    @SerializedName("circle")
    private int circle;

    @SerializedName("ratio")
    private float ratio;

    @Path("dl")
    @ElementList(entry = "dd", required = false, inline = true)
    private List<Flag> vodFlags;

    private Site site;

    // --- Constant for Removal ---
    private static final String STRING_TO_REMOVE = "公众号关注:《《王二小放牛娃》》";

    // --- Static Method (no changes here) ---
    public static List<Vod> arrayFrom(String str) {
        Type listType = new TypeToken<List<Vod>>() {}.getType();
        List<Vod> items = App.gson().fromJson(str, listType);
        return items == null ? Collections.emptyList() : items;
    }

    // --- Constructor (no changes here) ---
    public Vod() {
    }

    // --- Getters and Setters (Getters MODIFIED) ---

    public String getVodId() {
        String originalValue = TextUtils.isEmpty(vodId) ? "" : vodId.trim();
        return originalValue.replace(STRING_TO_REMOVE, ""); // MODIFIED
    }

    public void setVodId(String vodId) {
        this.vodId = vodId;
    }

    public String getVodName() {
        String originalValue = TextUtils.isEmpty(vodName) ? "" : vodName.trim();
        return originalValue.replace(STRING_TO_REMOVE, ""); // MODIFIED
    }

    // Overloaded getter also modified
    public String getVodName(String name) {
        if (getVodName().isEmpty()) { // Use the modified getter here
             setVodName(name);
        }
        // Return the potentially modified name from the field via the main getter
        return getVodName();
    }

    public void setVodName(String vodName) {
        this.vodName = vodName;
    }

    public String getTypeName() {
        String originalValue = TextUtils.isEmpty(typeName) ? "" : typeName.trim();
        return originalValue.replace(STRING_TO_REMOVE, ""); // MODIFIED
    }

    public String getVodPic() {
        String originalValue = TextUtils.isEmpty(vodPic) ? "" : vodPic.trim();
        return originalValue.replace(STRING_TO_REMOVE, ""); // MODIFIED
    }

     // Overloaded getter also modified
    public String getVodPic(String pic) {
        if (getVodPic().isEmpty()) { // Use the modified getter here
            setVodPic(pic);
        }
         // Return the potentially modified pic from the field via the main getter
        return getVodPic();
    }

    public void setVodPic(String vodPic) {
        this.vodPic = vodPic;
    }

    public String getVodRemarks() {
        String originalValue = TextUtils.isEmpty(vodRemarks) ? "" : vodRemarks.trim();
        return originalValue.replace(STRING_TO_REMOVE, ""); // MODIFIED
    }

    public String getVodYear() {
        String originalValue = TextUtils.isEmpty(vodYear) ? "" : vodYear.trim();
        return originalValue.replace(STRING_TO_REMOVE, ""); // MODIFIED
    }

    public String getVodArea() {
        String originalValue = TextUtils.isEmpty(vodArea) ? "" : vodArea.trim();
        return originalValue.replace(STRING_TO_REMOVE, ""); // MODIFIED
    }

    public String getVodDirector() {
        String originalValue = TextUtils.isEmpty(vodDirector) ? "" : vodDirector.trim();
        return originalValue.replace(STRING_TO_REMOVE, ""); // Already MODIFIED
    }

    public String getVodActor() {
        String originalValue = TextUtils.isEmpty(vodActor) ? "" : vodActor.trim();
        return originalValue.replace(STRING_TO_REMOVE, ""); // Already MODIFIED
    }

    public String getVodContent() {
        // Process newline first, then remove the string
        String processedValue = TextUtils.isEmpty(vodContent) ? "" : vodContent.trim().replace("\n", "<br>");
        return processedValue.replace(STRING_TO_REMOVE, ""); // Already MODIFIED (logic confirmed)
    }

    public String getVodPlayFrom() {
        // Original didn't trim, so we keep it that way before replacing
        String originalValue = TextUtils.isEmpty(vodPlayFrom) ? "" : vodPlayFrom;
        return originalValue.replace(STRING_TO_REMOVE, ""); // MODIFIED
    }

    public String getVodPlayUrl() {
        // Original didn't trim
        String originalValue = TextUtils.isEmpty(vodPlayUrl) ? "" : vodPlayUrl;
        return originalValue.replace(STRING_TO_REMOVE, ""); // MODIFIED
    }

    public String getVodTag() {
        // Original didn't trim
        String originalValue = TextUtils.isEmpty(vodTag) ? "" : vodTag;
        return originalValue.replace(STRING_TO_REMOVE, ""); // MODIFIED
    }

     public String getAction() {
         // Original didn't trim
         String originalValue = TextUtils.isEmpty(action) ? "" : action;
         return originalValue.replace(STRING_TO_REMOVE, ""); // MODIFIED
     }

    // --- Other Methods (Getters potentially returning strings also modified) ---

    public Cate getCate() {
        return cate; // Returns Cate object, no change needed
    }

    public Style getStyle() {
        return style != null ? style : Style.get(getLand(), getCircle(), getRatio()); // Returns Style object, no change needed
    }

    public int getLand() {
        return land; // Returns int
    }

    public int getCircle() {
        return circle; // Returns int
    }

    public float getRatio() {
        return ratio; // Returns float
    }

    public List<Flag> getVodFlags() {
        return vodFlags = vodFlags == null ? new ArrayList<>() : vodFlags; // Returns List, no change needed
    }

    public void setVodFlags(List<Flag> vodFlags) {
        this.vodFlags = vodFlags;
    }

    public Site getSite() {
        return site; // Returns Site object, no change needed
    }

    public void setSite(Site site) {
        this.site = site;
    }

     // Modified to clean the name/key coming from the Site object
    public String getSiteName() {
        Site currentSite = getSite(); // Get site once
        if (currentSite == null) return "";
        String originalName = currentSite.getName(); // Assume Site.getName() returns String
        // Apply replace only if originalName is not null/empty
        return TextUtils.isEmpty(originalName) ? "" : originalName.replace(STRING_TO_REMOVE, ""); // MODIFIED
    }

    // Modified to clean the name/key coming from the Site object
    public String getSiteKey() {
         Site currentSite = getSite(); // Get site once
         if (currentSite == null) return "";
         String originalKey = currentSite.getKey(); // Assume Site.getKey() returns String
         // Apply replace only if originalKey is not null/empty
         return TextUtils.isEmpty(originalKey) ? "" : originalKey.replace(STRING_TO_REMOVE, ""); // MODIFIED
    }


    // --- Visibility and boolean checks (no changes needed here) ---
    public int getSiteVisible() {
        return getSite() == null ? View.GONE : View.VISIBLE;
    }

    public int getYearVisible() {
        // Logic uses getVodYear() which is already modified, so this is fine
        return getSite() != null || getVodYear().length() < 4 ? View.GONE : View.VISIBLE;
    }

    public int getNameVisible() {
        // Logic uses getVodName() which is already modified
        return getVodName().isEmpty() ? View.GONE : View.VISIBLE;
    }

    public int getRemarkVisible() {
         // Logic uses getVodRemarks() which is already modified
        return getVodRemarks().isEmpty() ? View.GONE : View.VISIBLE;
    }

    public boolean isFolder() {
        // Logic uses getVodTag() which is already modified
        return "folder".equals(getVodTag()) || getCate() != null;
    }

     public boolean isAction() {
         // Logic uses getAction() which is already modified
        return !getAction().isEmpty();
     }

    public boolean isManga() {
         // Logic uses getVodTag() which is already modified
        return "manga".equals(getVodTag());
    }

    // --- Other Methods (trans, setVodFlags, equals, Parcelable - no changes needed here) ---

    public Style getStyle(Style style) {
        return getStyle() != null ? getStyle() : style != null ? style : Style.rect();
    }

    // trans() modifies fields directly, getters handle cleaning on retrieval
    public void trans() {
        if (Trans.pass()) return;
        // Apply translation first
        this.vodName = Trans.s2t(vodName);
        this.vodArea = Trans.s2t(vodArea);
        this.typeName = Trans.s2t(typeName);
        this.vodRemarks = Trans.s2t(vodRemarks);
        if (vodActor != null) this.vodActor = Sniffer.CLICKER.matcher(vodActor).find() ? vodActor : Trans.s2t(vodActor);
        if (vodContent != null) this.vodContent = Sniffer.CLICKER.matcher(vodContent).find() ? vodContent : Trans.s2t(vodContent);
        if (vodDirector != null) this.vodDirector = Sniffer.CLICKER.matcher(vodDirector).find() ? vodDirector : Trans.s2t(vodDirector);
        // The getters will handle removing STRING_TO_REMOVE when these fields are accessed later
    }

    public void setVodFlags() {
        // Logic uses getVodPlayFrom() and getVodPlayUrl() which are modified
        String[] playFlags = getVodPlayFrom().split("\\$\\$\\$");
        String[] playUrls = getVodPlayUrl().split("\\$\\$\\$");
        // Check if flags/URLs contained the string, though unlikely for structure
        for (int i = 0; i < playFlags.length; i++) {
            if (playFlags[i].isEmpty() || i >= playUrls.length) continue;
            // Clean flag name just in case
            String cleanedFlag = playFlags[i].trim().replace(STRING_TO_REMOVE,"");
            Flag item = Flag.create(cleanedFlag);
            // Clean URLs segment (less likely needed but for completeness)
            String cleanedUrls = playUrls[i].replace(STRING_TO_REMOVE,"");
            item.createEpisode(cleanedUrls);
            getVodFlags().add(item);
        }
        // This loop processes flags added via XML potentially
        for (Flag item : getVodFlags()) {
             if (item.getUrls() == null) continue;
             // Clean URLs provided via XML 'dd urls' attribute
             String cleanedUrls = item.getUrls().replace(STRING_TO_REMOVE, "");
             item.createEpisode(cleanedUrls);
             // Clean flag name provided via XML 'dd flag' attribute
             if (item.getFlag() != null) {
                 item.setFlag(item.getFlag().replace(STRING_TO_REMOVE,""));
             }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vod)) return false;
        Vod it = (Vod) obj;
        // Comparison uses getVodId() which is now modified
        return getVodId().equals(it.getVodId());
    }

    // --- Parcelable implementation (writes/reads raw fields, getters handle cleaning) ---
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write raw data
        dest.writeString(this.vodId);
        dest.writeString(this.vodName);
        dest.writeString(this.typeName);
        dest.writeString(this.vodPic);
        dest.writeString(this.vodRemarks);
        dest.writeString(this.vodYear);
        dest.writeString(this.vodArea);
        dest.writeString(this.vodDirector);
        dest.writeString(this.vodActor);
        dest.writeString(this.vodContent);
        dest.writeString(this.vodPlayFrom);
        dest.writeString(this.vodPlayUrl);
        dest.writeString(this.vodTag);
        dest.writeString(this.action);
        dest.writeInt(this.land);
        dest.writeInt(this.circle);
        dest.writeFloat(this.ratio);
        dest.writeParcelable(this.cate, flags);
        dest.writeParcelable(this.style, flags);
        dest.writeTypedList(this.vodFlags);
        dest.writeParcelable(this.site, flags);
    }

    protected Vod(Parcel in) {
        // Read raw data
        this.vodId = in.readString();
        this.vodName = in.readString();
        this.typeName = in.readString();
        this.vodPic = in.readString();
        this.vodRemarks = in.readString();
        this.vodYear = in.readString();
        this.vodArea = in.readString();
        this.vodDirector = in.readString();
        this.vodActor = in.readString();
        this.vodContent = in.readString();
        this.vodPlayFrom = in.readString();
        this.vodPlayUrl = in.readString();
        this.vodTag = in.readString();
        this.action = in.readString();
        this.land = in.readInt();
        this.circle = in.readInt();
        this.ratio = in.readFloat();
        this.cate = in.readParcelable(Cate.class.getClassLoader());
        this.style = in.readParcelable(Style.class.getClassLoader());
        this.vodFlags = in.createTypedArrayList(Flag.CREATOR);
        this.site = in.readParcelable(Site.class.getClassLoader());
        // Cleaning happens when getters are called on this reconstructed object
    }

    public static final Creator<Vod> CREATOR = new Creator<>() {
        @Override
        public Vod createFromParcel(Parcel source) {
            return new Vod(source);
        }

        @Override
        public Vod[] newArray(int size) {
            return new Vod[size];
        }
    };
}
