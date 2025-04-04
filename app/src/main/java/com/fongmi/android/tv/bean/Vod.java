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
    private static final String STRING_TO_REMOVE = "公众号关注：《《王二小放牛娃》》";
    // --- MODIFIED: Added constants for URLs ---
    private static final String OLD_URL = "https://fs-im-kefu.7moor-fs1.com/ly/4d2c3f00-7d4c-11e5-af15-41bf63ae4ea0/1720514148900/26838917450215.png";
    private static final String NEW_URL = "https://fs-im-kefu.7moor-fs1.com/ly/4d2c3f00-7d4c-11e5-af15-41bf63ae4ea0/1743708586188/7476E62F-3D13-451B-B386-B7152694B002.png";
    // --- END MODIFICATION ---

    // --- Static Method (no changes here) ---
    public static List<Vod> arrayFrom(String str) {
        Type listType = new TypeToken<List<Vod>>() {}.getType();
        List<Vod> items = App.gson().fromJson(str, listType);
        return items == null ? Collections.emptyList() : items;
    }

    // --- Constructor (no changes here) ---
    public Vod() {
    }

    // --- MODIFIED: Helper methods for processing strings ---
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
    // --- END MODIFICATION ---


    // --- Getters and Setters (Getters MODIFIED to use helpers for cleaning) ---

    public String getVodId() {
        // Original logic used trim implicitly in some places, let's be consistent
        return processTrimmedString(this.vodId); // MODIFIED: Use helper
    }

    public void setVodId(String vodId) {
        this.vodId = vodId;
    }

    public String getVodName() {
        return processTrimmedString(this.vodName); // MODIFIED: Use helper
    }

    // Overloaded getter relies on the main getter now
    public String getVodName(String name) {
        if (getVodName().isEmpty()) { // Uses the modified getter
            setVodName(name);
        }
        return getVodName(); // Uses the modified getter
    }

    public void setVodName(String vodName) {
        this.vodName = vodName;
    }

    public String getTypeName() {
        return processTrimmedString(this.typeName); // MODIFIED: Use helper
    }

    public String getVodPic() {
        // Trim is usually safe for URLs, kept consistent with other fields
        return processTrimmedString(this.vodPic); // MODIFIED: Use helper
    }

    // Overloaded getter relies on the main getter now
    public String getVodPic(String pic) {
        if (getVodPic().isEmpty()) { // Uses the modified getter
            setVodPic(pic);
        }
        return getVodPic(); // Uses the modified getter
    }

    public void setVodPic(String vodPic) {
        this.vodPic = vodPic;
    }

    public String getVodRemarks() {
        return processTrimmedString(this.vodRemarks); // MODIFIED: Use helper
    }

    public String getVodYear() {
        return processTrimmedString(this.vodYear); // MODIFIED: Use helper
    }

    public String getVodArea() {
        return processTrimmedString(this.vodArea); // MODIFIED: Use helper
    }

    public String getVodDirector() {
        return processTrimmedString(this.vodDirector); // MODIFIED: Use helper
    }

    public String getVodActor() {
        return processTrimmedString(this.vodActor); // MODIFIED: Use helper
    }

    public String getVodContent() {
        // Special handling: trim, replace newline, then apply standard replacements
        if (TextUtils.isEmpty(this.vodContent)) {
            return "";
        }
        String processed = this.vodContent.trim().replace("\n", "<br>");
        // MODIFIED: Apply both replacements after trim and newline handling
        return processed.replace(STRING_TO_REMOVE, "").replace(OLD_URL, NEW_URL);
    }

    public String getVodPlayFrom() {
        // No trim originally, use processString directly
        return processString(this.vodPlayFrom); // MODIFIED: Use helper
    }

    public String getVodPlayUrl() {
        // No trim originally, use processString directly
        return processString(this.vodPlayUrl); // MODIFIED: Use helper
    }

    public String getVodTag() {
        // No trim originally, use processString directly
        return processString(this.vodTag); // MODIFIED: Use helper
    }

    public String getAction() {
        // No trim originally, use processString directly
        return processString(this.action); // MODIFIED: Use helper
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
        // MODIFIED: Optionally re-process flags when set externally to ensure cleaning
        if (this.vodFlags != null) {
            for (Flag item : this.vodFlags) {
                processExistingFlagItem(item); // Ensure flags set this way are also cleaned
            }
        }
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    // MODIFIED: Clean the name coming from the Site object
    public String getSiteName() {
        Site currentSite = getSite();
        if (currentSite == null) return "";
        // Use processString (no trim assumed for site name)
        return processString(currentSite.getName()); // MODIFIED
    }

    // MODIFIED: Clean the key coming from the Site object
    public String getSiteKey() {
        Site currentSite = getSite();
        if (currentSite == null) return "";
        // Use processString (no trim assumed for site key)
        return processString(currentSite.getKey()); // MODIFIED
    }

    // --- Visibility and boolean checks (rely on modified getters, no changes needed) ---
    // These methods use the modified getters, so they automatically benefit from the cleaning.
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
        // Getters will handle STRING_TO_REMOVE and URL replacement later when accessed
    }

    // --- MODIFIED: setVodFlags() to use cleaned data and clean existing flags ---
    public void setVodFlags() {
        // Use the getters which already perform replacements
        String playFromData = getVodPlayFrom(); // Already cleaned via getter
        String playUrlData = getVodPlayUrl();   // Already cleaned via getter

        // Clear existing flags ONLY if we are populating from PlayFrom/PlayUrl
        boolean populatedFromApi = !TextUtils.isEmpty(playFromData) && !TextUtils.isEmpty(playUrlData);
        if (populatedFromApi) {
            getVodFlags().clear(); // Clear flags presumably populated via XML if API data exists
        }

        if (populatedFromApi) {
            String[] playFlags = playFromData.split("\\$\\$\\$");
            String[] playUrls = playUrlData.split("\\$\\$\\$");

            for (int i = 0; i < playFlags.length; i++) {
                if (playFlags[i].isEmpty() || i >= playUrls.length) continue;
                // Flag name needs trim + standard cleaning (though already cleaned by getter, trim ensures consistency)
                String flagName = processTrimmedString(playFlags[i]); // Apply trim + full cleaning
                Flag item = Flag.create(flagName);
                // URLs are already cleaned by getVodPlayUrl(), pass them directly to createEpisode
                item.createEpisode(playUrls[i]); // playUrls[i] comes from the already cleaned playUrlData split
                getVodFlags().add(item);
            }
        }

        // Always process flags that might have been populated via XML (or added manually)
        // This ensures consistency regardless of the source (API vs XML vs manual)
        if (this.vodFlags != null) {
            for (Flag item : this.vodFlags) {
                processExistingFlagItem(item); // Clean flags potentially from XML or other sources
            }
        }
    }

    // MODIFIED: Helper method to process existing flag items (potentially from XML or Parcel)
    private void processExistingFlagItem(Flag item) {
        if (item == null) return;
        // Clean flag name
        if (item.getFlag() != null) {
            // Apply trim + both replacements
            item.setFlag(item.getFlag().trim().replace(STRING_TO_REMOVE, "").replace(OLD_URL, NEW_URL));
        }
        // Clean URLs if they exist as a single string attribute (less common)
        if (item.getUrls() != null) {
            // No trim for URLs, apply both replacements
            String cleanedUrls = item.getUrls().replace(STRING_TO_REMOVE, "").replace(OLD_URL, NEW_URL);
            item.createEpisode(cleanedUrls); // This might replace existing episodes based on the cleaned URL string
        } else if (item.getEpisodes() != null) {
            // If URLs are already parsed into episodes, clean each episode's URL and Name
            for (Episode episode : item.getEpisodes()) {
                if (episode.getUrl() != null) {
                    episode.setUrl(episode.getUrl().replace(STRING_TO_REMOVE, "").replace(OLD_URL, NEW_URL));
                }
                if (episode.getName() != null) {
                    // Also clean episode names, just in case
                    episode.setName(episode.getName().replace(STRING_TO_REMOVE, "").replace(OLD_URL, NEW_URL));
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
        // Comparison uses getVodId() which is now modified to clean the ID
        return getVodId().equals(it.getVodId());
    }

    // --- Parcelable implementation (writes/reads raw fields, getters handle cleaning on retrieval) ---
    // No fundamental changes needed here, but added comments for clarity.
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
        // Write potentially uncleaned Flag data. Cleaning happens via processExistingFlagItem if needed.
        dest.writeTypedList(this.vodFlags);
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
        // Reads potentially uncleaned Flag data
        this.vodFlags = in.createTypedArrayList(Flag.CREATOR);
        this.site = in.readParcelable(Site.class.getClassLoader());

        // MODIFIED: Optionally re-process flags immediately after reading from parcel
        // This ensures flags read from a Parcel are also cleaned.
        if (this.vodFlags != null) {
            for (Flag item : this.vodFlags) {
                processExistingFlagItem(item);
            }
        }
        // --- END MODIFICATION ---
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