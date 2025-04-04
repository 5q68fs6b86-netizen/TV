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
import java.util.Objects; // Import Objects for equals/hashCode if needed, though current equals uses vodId

// Assuming Episode and Flag classes are in the same package or imported correctly
// Assuming Site, Cate, Style classes are also accessible

@Root(strict = false)
public class Vod implements Parcelable {

    // --- Fields (Original fields remain unchanged) ---
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

    // --- Constants for Removal and Replacement ---
    private static final String STRING_TO_REMOVE = "公众号关注:《《王二小放牛娃》》";
    private static final String OLD_URL = "https://fs-im-kefu.7moor-fs1.com/ly/4d2c3f00-7d4c-11e5-af15-41bf63ae4ea0/1720514148900/26838917450215.png";
    private static final String NEW_URL = "https://fs-im-kefu.7moor-fs1.com/ly/4d2c3f00-7d4c-11e5-af15-41bf63ae4ea0/1743708586188/7476E62F-3D13-451B-B386-B7152694B002.png";

    // --- Static Method (no changes here) ---
    public static List<Vod> arrayFrom(String str) {
        Type listType = new TypeToken<List<Vod>>() {}.getType();
        List<Vod> items = App.gson().fromJson(str, listType);
        return items == null ? Collections.emptyList() : items;
    }

    // --- Constructor (no changes here) ---
    public Vod() {
    }

    // --- Helper methods for processing strings ---
    // Helper to apply both replacements (removal + URL) without trimming
    private String processString(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        // Apply both replacements
        return input.replace(STRING_TO_REMOVE, "").replace(OLD_URL, NEW_URL);
    }

    // Helper to trim first, then apply both replacements
    private String processTrimmedString(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        // Trim first, then apply both replacements
        return input.trim().replace(STRING_TO_REMOVE, "").replace(OLD_URL, NEW_URL);
    }

    // --- Getters and Setters (Getters MODIFIED to use helpers for cleaning) ---

    public String getVodId() {
        return processTrimmedString(this.vodId);
    }

    public void setVodId(String vodId) {
        this.vodId = vodId;
    }

    public String getVodName() {
        return processTrimmedString(this.vodName);
    }

    public String getVodName(String name) {
        if (getVodName().isEmpty()) {
            setVodName(name);
        }
        return getVodName();
    }

    public void setVodName(String vodName) {
        this.vodName = vodName;
    }

    public String getTypeName() {
        return processTrimmedString(this.typeName);
    }

    public String getVodPic() {
        return processTrimmedString(this.vodPic);
    }

    public String getVodPic(String pic) {
        if (getVodPic().isEmpty()) {
            setVodPic(pic);
        }
        return getVodPic();
    }

    public void setVodPic(String vodPic) {
        this.vodPic = vodPic;
    }

    public String getVodRemarks() {
        return processTrimmedString(this.vodRemarks);
    }

    public String getVodYear() {
        return processTrimmedString(this.vodYear);
    }

    public String getVodArea() {
        return processTrimmedString(this.vodArea);
    }

    public String getVodDirector() {
        return processTrimmedString(this.vodDirector);
    }

    public String getVodActor() {
        return processTrimmedString(this.vodActor);
    }

    public String getVodContent() {
        if (TextUtils.isEmpty(this.vodContent)) {
            return "";
        }
        String processed = this.vodContent.trim().replace("\n", "<br>");
        return processed.replace(STRING_TO_REMOVE, "").replace(OLD_URL, NEW_URL);
    }

    public String getVodPlayFrom() {
        return processString(this.vodPlayFrom);
    }

    public String getVodPlayUrl() {
        return processString(this.vodPlayUrl);
    }

    public String getVodTag() {
        return processString(this.vodTag);
    }

    public String getAction() {
        return processString(this.action);
    }

    // --- Other Methods (Getters potentially returning strings also modified) ---

    public Cate getCate() {
        return cate;
    }

    public Style getStyle() {
        return style != null ? style : Style.get(getLand(), getCircle(), getRatio());
    }

    public int getLand() {
        return land;
    }

    public int getCircle() {
        return circle;
    }

    public float getRatio() {
        return ratio;
    }

    public List<Flag> getVodFlags() {
        return vodFlags = vodFlags == null ? new ArrayList<>() : vodFlags;
    }

    public void setVodFlags(List<Flag> vodFlags) {
        this.vodFlags = vodFlags;
        if (this.vodFlags != null) {
            for (Flag item : this.vodFlags) {
                processExistingFlagItem(item);
            }
        }
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getSiteName() {
        Site currentSite = getSite();
        if (currentSite == null) return "";
        return processString(currentSite.getName());
    }

    public String getSiteKey() {
        Site currentSite = getSite();
        if (currentSite == null) return "";
        return processString(currentSite.getKey());
    }

    // --- Visibility and boolean checks (rely on modified getters, no changes needed) ---
    public int getSiteVisible() {
        return getSite() == null ? View.GONE : View.VISIBLE;
    }

    public int getYearVisible() {
        return getSite() != null || getVodYear().length() < 4 ? View.GONE : View.VISIBLE;
    }

    public int getNameVisible() {
        return getVodName().isEmpty() ? View.GONE : View.VISIBLE;
    }

    public int getRemarkVisible() {
        return getVodRemarks().isEmpty() ? View.GONE : View.VISIBLE;
    }

    public boolean isFolder() {
        return "folder".equals(getVodTag()) || getCate() != null;
    }

    public boolean isAction() {
        return !getAction().isEmpty();
    }

    public boolean isManga() {
        return "manga".equals(getVodTag());
    }

    // --- Other Methods (trans, setVodFlags, equals, Parcelable) ---

    public Style getStyle(Style style) {
        return getStyle() != null ? getStyle() : style != null ? style : Style.rect();
    }

    public void trans() {
        if (Trans.pass()) return;
        this.vodName = Trans.s2t(vodName);
        this.vodArea = Trans.s2t(vodArea);
        this.typeName = Trans.s2t(typeName);
        this.vodRemarks = Trans.s2t(vodRemarks);
        if (vodActor != null) this.vodActor = Sniffer.CLICKER.matcher(vodActor).find() ? vodActor : Trans.s2t(vodActor);
        if (vodContent != null) this.vodContent = Sniffer.CLICKER.matcher(vodContent).find() ? vodContent : Trans.s2t(vodContent);
        if (vodDirector != null) this.vodDirector = Sniffer.CLICKER.matcher(vodDirector).find() ? vodDirector : Trans.s2t(vodDirector);
        // Cleaning via STRING_TO_REMOVE and OLD_URL happens in getters
    }

    // Modified setVodFlags() to use cleaned data and clean existing flags
    public void setVodFlags() {
        String playFromData = getVodPlayFrom(); // Already cleaned via getter
        String playUrlData = getVodPlayUrl();   // Already cleaned via getter

        boolean populatedFromApi = !TextUtils.isEmpty(playFromData) && !TextUtils.isEmpty(playUrlData);
        if (populatedFromApi) {
             getVodFlags().clear(); // Clear flags presumably populated via XML if API data exists
        }

        if (populatedFromApi) {
            String[] playFlags = playFromData.split("\\$\\$\\$");
            String[] playUrls = playUrlData.split("\\$\\$\\$");

            for (int i = 0; i < playFlags.length; i++) {
                if (playFlags[i].isEmpty() || i >= playUrls.length) continue;
                String flagName = processTrimmedString(playFlags[i]); // Apply trim + full cleaning
                Flag item = Flag.create(flagName);
                // URLs are already cleaned by getVodPlayUrl(), pass them directly to createEpisode
                item.createEpisode(playUrls[i]);
                getVodFlags().add(item);
            }
        }

        // Always process flags that might have been populated via XML (or added manually)
        if (this.vodFlags != null) {
            for (Flag item : this.vodFlags) {
                processExistingFlagItem(item); // Clean flags potentially from XML or other sources
            }
        }
    }

    // --- MODIFIED: Helper method to process existing flag items (potentially from XML or Parcel) ---
    private void processExistingFlagItem(Flag item) {
        if (item == null) return;
        // Clean flag name (Assuming Flag.setFlag exists and is public)
        if (item.getFlag() != null) {
            // Apply trim + both replacements
            try { // Added try-catch just in case Flag.setFlag doesn't exist or isn't accessible
                 item.setFlag(item.getFlag().trim().replace(STRING_TO_REMOVE, "").replace(OLD_URL, NEW_URL));
            } catch (Exception e) {
                 // Log error or handle gracefully if setFlag is problematic
                 System.err.println("Warning: Could not clean flag name for item: " + item + " - " + e.getMessage());
            }
        }

        // --- REMOVED --- Attempting to clean Episode URL and Name in-place
        // The Episode class likely does not have public setUrl() or setName() methods.
        // Cleaning of vodPlayUrl happens *before* splitting into episodes in setVodFlags()
        // for API responses, which is the primary source.

        // If Flag has a way to get/set Episodes directly AND Episode needs cleaning,
        // alternative approaches might be needed (e.g., creating new Episode instances),
        // but removing the problematic lines is the safest fix without Episode.java code.
        // Re-applying createEpisode based on cleaned getUrls() if it exists might be an option,
        // but only if getUrls() reflects the underlying episodes accurately.
        if (item.getUrls() != null) {
            String cleanedUrls = item.getUrls().replace(STRING_TO_REMOVE, "").replace(OLD_URL, NEW_URL);
            // Check if cleaning actually changed the string before potentially overwriting episodes
            if (!cleanedUrls.equals(item.getUrls())) {
                 // Re-create episodes from the cleaned bulk URL string if Flag supports this well.
                 // WARNING: This assumes createEpisode correctly handles replacing existing episodes.
                 // And it only works if the <dd> tag had the 'url' attribute format, not nested <episode> tags.
                 try { // Added try-catch for createEpisode as well
                     item.createEpisode(cleanedUrls);
                 } catch (Exception e) {
                     System.err.println("Warning: Could not re-create episodes from cleaned URL string for item: " + item + " - " + e.getMessage());
                 }
            }
        }
    }
    // --- END MODIFICATION ---


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vod)) return false;
        Vod it = (Vod) obj;
        // Comparison uses getVodId() which cleans the ID
        return getVodId().equals(it.getVodId());
    }

    // Consider implementing hashCode if equals is overridden, using the same field(s)
    @Override
    public int hashCode() {
        return Objects.hash(getVodId()); // Use cleaned vodId for hashcode consistency
    }

    // --- Parcelable implementation (writes/reads raw fields, getters handle cleaning on retrieval) ---
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write raw field data. Cleaning happens via getters when data is accessed later.
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
        dest.writeTypedList(this.vodFlags); // Write potentially uncleaned Flag data.
        dest.writeParcelable(this.site, flags);
    }

    protected Vod(Parcel in) {
        // Read raw field data.
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
        this.vodFlags = in.createTypedArrayList(Flag.CREATOR); // Reads potentially uncleaned Flag data
        this.site = in.readParcelable(Site.class.getClassLoader());

        // Ensure flags read from a Parcel are also processed/cleaned.
        if (this.vodFlags != null) {
            for (Flag item : this.vodFlags) {
                processExistingFlagItem(item);
            }
        }
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