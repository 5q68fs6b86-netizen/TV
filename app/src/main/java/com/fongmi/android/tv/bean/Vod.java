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
import java.util.Objects;
import java.util.regex.Pattern; // Import Pattern for Regex

// Assuming Episode, Flag, Site, Cate, Style classes are accessible

@Root(strict = false)
public class Vod implements Parcelable {

    // --- Fields ---
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
    private String vodPic; // Raw data

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
    private String vodPlayFrom; // Raw data

    @SerializedName("vod_play_url")
    private String vodPlayUrl; // Raw data

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
    private List<Flag> vodFlags; // Can be populated by XML (needs standard cleaning)

    private Site site;

    // --- Constants for Replacement ---
    private static final String TARGET_STRING = "公众号关注:《《王二小放牛娃》》";
    private static final String VOD_PLAY_FROM_REPLACEMENT = "播放列表";
    private static final String STANDARD_REPLACEMENT = ""; // Empty string for removal

    // Common Replacements (URLs, Typos)
    private static final String OLD_URL_1 = "https://fs-im-kefu.7moor-fs1.com/ly/4d2c3f00-7d4c-11e5-af15-41bf63ae4ea0/1720514148900/26838917450215.png";
    private static final String NEW_URL_1 = "https://fs-im-kefu.7moor-fs1.com/ly/4d2c3f00-7d4c-11e5-af15-41bf63ae4ea0/1743708586188/7476E62F-3D13-451B-B386-B7152694B002.png";
    private static final String OLD_URL_2 = "https://uchat.cn-bj.ufileos.com/rw_1ce85ffd-1540-4eb2-b724-6d29e4a0bc99_123.png";
    private static final String NEW_URL_2 = "https://fs-im-kefu.7moor-fs1.com/ly/4d2c3f00-7d4c-11e5-af15-41bf63ae4ea0/1743741743563/86043B79-CAE8-4408-BE6D-78DC9C7312B2.png";
    private static final String OLD_URL_3 = "https://fs-im-kefu.7moor-fs1.com/ly/4d2c3f00-7d4c-11e5-af15-41bf63ae4ea0/1740327617800/tyyun.png";
    private static final String NEW_URL_3 = "https://fs-im-kefu.7moor-fs1.com/ly/4d2c3f00-7d4c-11e5-af15-41bf63ae4ea0/1743741742200/E0177078-7B17-4964-B409-36BA804A8DD6.png";
    private static final String[] OTHER_REPLACEMENTS_OLD = {"迅蕾", "优熙", "跨壳", "天逸", "TJ搜索服务器"};
    private static final String[] OTHER_REPLACEMENTS_NEW = {"迅雷", "UC", "夸克", "天翼", "TG搜索服务器"};

    // --- NEW: Specific vodPic replacement ---
    // Regex to match https://www.leijing1.com/file/avatar/{anything}/null OR https://www.leijing1.com/file/avatar/{anything}/{anything}.png
    // Explanation:
    // ^                                      - Start of the string
    // https://www\.leijing1\.com/file/avatar/ - Literal prefix (dots escaped)
    // [^/]+                                  - Matches one or more characters that are NOT a slash (the first {anything})
    // /                                      - Literal slash separator
    // (                                      - Start of group for OR condition
    //   null                                 - Matches the literal "null"
    //   |                                    - OR
    //   [^/]+\.png                           - Matches one or more non-slash chars (second {anything}) followed by literal ".png" (dot escaped)
    // )                                      - End of group
    // $                                      - End of the string
    private static final Pattern LEIJING_AVATAR_PATTERN = Pattern.compile("^https://www\\.leijing1\\.com/file/avatar/[^/]+/(null|[^/]+\\.png|[^/]+\\.jpg)$");
    // The replacement URL for the matched patterns
    private static final String NEW_AVATAR_URL = "https://fs-im-kefu.7moor-fs1.com/ly/4d2c3f00-7d4c-11e5-af15-41bf63ae4ea0/1743741742200/E0177078-7B17-4964-B409-36BA804A8DD6.png";
    // --- END NEW ---

    // --- Static Method ---
    public static List<Vod> arrayFrom(String str) {
        Type listType = new TypeToken<List<Vod>>() {}.getType();
        List<Vod> items = App.gson().fromJson(str, listType);
        return items == null ? Collections.emptyList() : items;
    }

    // --- Constructor ---
    public Vod() {
    }

    // --- Helper methods for processing strings ---

    /**
     * Applies ONLY the common replacements (URLs, Typos) to a string.
     */
    private String applyCommonReplacements(String input) {
        if (input == null) return "";
        String result = input;
        result = result.replace(OLD_URL_1, NEW_URL_1);
        result = result.replace(OLD_URL_2, NEW_URL_2);
        result = result.replace(OLD_URL_3, NEW_URL_3);
        for (int i = 0; i < OTHER_REPLACEMENTS_OLD.length; i++) {
            result = result.replace(OTHER_REPLACEMENTS_OLD[i], OTHER_REPLACEMENTS_NEW[i]);
        }
        return result;
    }

    /**
     * Applies the STANDARD processing rule: REMOVE the target string, then apply common replacements.
     * Used for all fields EXCEPT vodPlayFrom. Does NOT trim.
     */
    private String processStringStandardRemove(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        // Apply standard REMOVAL first
        String processed = input.replace(TARGET_STRING, STANDARD_REPLACEMENT);
        // Then apply common replacements
        return applyCommonReplacements(processed);
    }

    /**
     * Applies the STANDARD processing rule with TRIM: Trim, REMOVE the target string, then apply common replacements.
     * Used for most fields EXCEPT vodPlayFrom, vodContent, and non-trimmed fields.
     */
    private String processTrimmedStringStandardRemove(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        // Trim, then apply standard REMOVAL
        String processed = input.trim().replace(TARGET_STRING, STANDARD_REPLACEMENT);
        // Then apply common replacements
        return applyCommonReplacements(processed);
    }

    /**
     * Applies SPECIAL processing for vodContent: Trim, handle newline, standard REMOVAL, common replacements.
     */
    private String processVodContent(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        // Trim, handle newline, standard REMOVAL
        String processed = input.trim().replace("\n", "<br>").replace(TARGET_STRING, STANDARD_REPLACEMENT);
        // Then apply common replacements
        return applyCommonReplacements(processed);
    }

    /**
     * Applies SPECIAL processing for vodPlayFrom: Apply common replacements, THEN specific REPLACEMENT.
     */
    private String processVodPlayFrom(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        // Apply common replacements first
        String processed = applyCommonReplacements(input);
        // THEN apply the SPECIAL replacement rule for THIS field
        return processed.replace(TARGET_STRING, VOD_PLAY_FROM_REPLACEMENT);
    }


    // --- Getters and Setters (Applying appropriate processing) ---

    public String getVodId() {
        return processTrimmedStringStandardRemove(this.vodId);
    }

    public void setVodId(String vodId) {
        this.vodId = vodId;
    }

    public String getVodName() {
        return processTrimmedStringStandardRemove(this.vodName);
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
        return processTrimmedStringStandardRemove(this.typeName);
    }

    // --- MODIFIED: getVodPic applies specific pattern replacement AFTER standard cleaning ---
    public String getVodPic() {
         // 1. Apply standard cleaning (trim, remove target string, common replacements)
         String pic = processTrimmedStringStandardRemove(this.vodPic);

         // 2. Check if the cleaned string matches the specific leijing avatar pattern
         if (!TextUtils.isEmpty(pic) && LEIJING_AVATAR_PATTERN.matcher(pic).matches()) {
             // 3. If it matches, return the predefined replacement URL
             return NEW_AVATAR_URL;
         }

         // 4. Otherwise, return the standard cleaned string
         return pic;
    }
    // --- END MODIFICATION ---

    public String getVodPic(String pic) {
        // This helper method now also benefits from the logic in getVodPic()
        if (getVodPic().isEmpty()) {
            setVodPic(pic);
        }
        return getVodPic();
    }

    public void setVodPic(String vodPic) {
        this.vodPic = vodPic;
    }

    public String getVodRemarks() {
         return processTrimmedStringStandardRemove(this.vodRemarks);
    }

    public String getVodYear() {
         return processTrimmedStringStandardRemove(this.vodYear);
    }

    public String getVodArea() {
        return processTrimmedStringStandardRemove(this.vodArea);
    }

    public String getVodDirector() {
        return processTrimmedStringStandardRemove(this.vodDirector);
    }

    public String getVodActor() {
        return processTrimmedStringStandardRemove(this.vodActor);
    }

    public String getVodContent() {
        // Uses its specific helper
        return processVodContent(this.vodContent);
    }

    // --- vodPlayFrom Getter ---
    public String getVodPlayFrom() {
        // Uses its specific helper for REPLACEMENT
        return processVodPlayFrom(this.vodPlayFrom);
    }

    // --- vodPlayUrl Getter ---
    public String getVodPlayUrl() {
        // Uses standard helper for REMOVAL
        return processStringStandardRemove(this.vodPlayUrl);
    }

    public String getVodTag() {
        // Uses standard helper for REMOVAL
        return processStringStandardRemove(this.vodTag);
    }

    public String getAction() {
        // Uses standard helper for REMOVAL
        return processStringStandardRemove(this.action);
    }

    // --- Other Methods ---

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
        // When flags are set externally (e.g., deserialized), ensure they are cleaned
        // using the STANDARD REMOVAL process.
        if (this.vodFlags != null) {
            for (Flag item : this.vodFlags) {
                processExistingFlagItemStandard(item); // Apply standard cleaning
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
        // Site name uses standard REMOVAL
        return processStringStandardRemove(currentSite.getName());
    }

    public String getSiteKey() {
        Site currentSite = getSite();
        if (currentSite == null) return "";
        // Site key uses standard REMOVAL
        return processStringStandardRemove(currentSite.getKey());
    }

    // --- Visibility and boolean checks (rely on cleaned getters, no changes needed) ---
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

    // --- Other Methods ---

    public Style getStyle(Style style) {
        return getStyle() != null ? getStyle() : style != null ? style : Style.rect();
    }

    // trans() modifies raw fields; cleaning happens in getters.
    public void trans() {
        if (Trans.pass()) return;
        this.vodName = Trans.s2t(vodName);
        this.vodArea = Trans.s2t(vodArea);
        this.typeName = Trans.s2t(typeName);
        this.vodRemarks = Trans.s2t(vodRemarks);
        if (vodActor != null) this.vodActor = Sniffer.CLICKER.matcher(vodActor).find() ? vodActor : Trans.s2t(vodActor);
        if (vodContent != null) this.vodContent = Sniffer.CLICKER.matcher(vodContent).find() ? vodContent : Trans.s2t(vodContent);
        if (vodDirector != null) this.vodDirector = Sniffer.CLICKER.matcher(vodDirector).find() ? vodDirector : Trans.s2t(vodDirector);
        // No need to process flags here, done elsewhere.
    }

    /**
     * Populates or updates the vodFlags list based on vodPlayFrom and vodPlayUrl fields.
     * It uses the specific getters which apply the correct cleaning rules (replacement for From, removal for Url).
     * It then ensures *all* flags (newly created or pre-existing from XML) undergo standard cleaning.
     */
    public void setVodFlags() {
        String playFromData = getVodPlayFrom(); // Gets data with SPECIAL replacement rule applied
        String playUrlData = getVodPlayUrl();   // Gets data with STANDARD removal rule applied

        boolean populatedFromApi = !TextUtils.isEmpty(playFromData) && !TextUtils.isEmpty(playUrlData);

        List<Flag> existingFlags = new ArrayList<>(getVodFlags()); // Copy existing flags (e.g., from XML)
        getVodFlags().clear(); // Clear the main list before potentially adding API flags

        if (populatedFromApi) {
            String[] playFlags = playFromData.split("\\$\\$\\$"); // Already processed flags
            String[] playUrls = playUrlData.split("\\$\\$\\$");   // Already processed urls

            for (int i = 0; i < playFlags.length; i++) {
                if (playFlags[i].isEmpty() || i >= playUrls.length) continue;

                String flagName = playFlags[i]; // Use directly (contains "播放列表" if applicable)
                Flag apiFlag = Flag.create(flagName);
                apiFlag.createEpisode(playUrls[i]); // Use directly (TARGET_STRING removed if applicable)
                getVodFlags().add(apiFlag); // Add the flag created from API data
            }
        } else {
             // If no API data, restore the original flags (likely from XML)
             getVodFlags().addAll(existingFlags);
        }

        // IMPORTANT: Ensure ALL flags in the final list (whether from API or XML)
        // undergo the STANDARD cleaning process. This primarily ensures common replacements
        // are applied and that any TARGET_STRING in XML-loaded flags is REMOVED.
        // It will NOT affect "播放列表" because that doesn't match TARGET_STRING.
        if (this.vodFlags != null) {
            for (Flag item : this.vodFlags) {
                processExistingFlagItemStandard(item);
            }
        }
    }

    /**
     * Applies STANDARD cleaning rules (TARGET_STRING removal + common replacements)
     * to an existing Flag item (name and URLs). Typically used for flags loaded from XML or Parcel.
     */
    private void processExistingFlagItemStandard(Flag item) {
        if (item == null) return;

        // Clean flag name using STANDARD processing (trim, REMOVE target, common replacements)
        if (item.getFlag() != null) {
            try {
                 String cleanedFlag = item.getFlag().trim().replace(TARGET_STRING, STANDARD_REPLACEMENT); // Trim + standard removal
                 cleanedFlag = applyCommonReplacements(cleanedFlag); // Apply common
                 item.setFlag(cleanedFlag);
            } catch (Exception e) {
                 System.err.println("Warning: Could not clean flag name (standard) for item: " + item + " - " + e.getMessage());
            }
        }

        // Clean the bulk URL string using STANDARD processing (REMOVE target, common replacements)
        if (item.getUrls() != null) {
             String originalUrls = item.getUrls();
             String cleanedUrls = originalUrls.replace(TARGET_STRING, STANDARD_REPLACEMENT); // Standard removal
             cleanedUrls = applyCommonReplacements(cleanedUrls); // Apply common

             // Re-create episodes only if the cleaning actually changed the URL string
             if (!cleanedUrls.equals(originalUrls)) {
                 try {
                     item.createEpisode(cleanedUrls); // Recreate episodes with cleaned URL list
                 } catch (Exception e) {
                     System.err.println("Warning: Could not re-create episodes from standard cleaned URL string for item: " + item + " - " + e.getMessage());
                 }
             }
        } else {
            // If URLs are null but episodes exist, try cleaning individual episode URLs/names (less common)
            // This part might be omitted if flags always have a bulk URL string when needing cleaning.
             if (item.getEpisodes() != null) {
                 for(Episode episode : item.getEpisodes()) {
                     if (episode.getName() != null) {
                         String cleanedName = episode.getName().replace(TARGET_STRING, STANDARD_REPLACEMENT);
                         cleanedName = applyCommonReplacements(cleanedName);
                         episode.setName(cleanedName);
                     }
                     if (episode.getUrl() != null) {
                          String cleanedUrl = episode.getUrl().replace(TARGET_STRING, STANDARD_REPLACEMENT);
                          cleanedUrl = applyCommonReplacements(cleanedUrl);
                          episode.setUrl(cleanedUrl);
                     }
                 }
             }
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vod)) return false;
        Vod it = (Vod) obj;
        // Comparison uses getVodId() which applies standard cleaning
        return getVodId().equals(it.getVodId());
    }

    @Override
    public int hashCode() {
        // Use cleaned vodId for hashcode consistency
        return Objects.hash(getVodId());
    }

    // --- Parcelable implementation (writes/reads raw fields) ---
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write raw field data. Cleaning happens via getters or specific processing steps.
        dest.writeString(this.vodId);
        dest.writeString(this.vodName);
        dest.writeString(this.typeName);
        dest.writeString(this.vodPic); // Write raw
        dest.writeString(this.vodRemarks);
        dest.writeString(this.vodYear);
        dest.writeString(this.vodArea);
        dest.writeString(this.vodDirector);
        dest.writeString(this.vodActor);
        dest.writeString(this.vodContent);
        dest.writeString(this.vodPlayFrom); // Write raw
        dest.writeString(this.vodPlayUrl); // Write raw
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
        this.vodPic = in.readString(); // Read raw
        this.vodRemarks = in.readString();
        this.vodYear = in.readString();
        this.vodArea = in.readString();
        this.vodDirector = in.readString();
        this.vodActor = in.readString();
        this.vodContent = in.readString();
        this.vodPlayFrom = in.readString(); // Read raw
        this.vodPlayUrl = in.readString(); // Read raw
        this.vodTag = in.readString();
        this.action = in.readString();
        this.land = in.readInt();
        this.circle = in.readInt();
        this.ratio = in.readFloat();
        this.cate = in.readParcelable(Cate.class.getClassLoader());
        this.style = in.readParcelable(Style.class.getClassLoader());
        this.vodFlags = in.createTypedArrayList(Flag.CREATOR); // Reads potentially uncleaned Flag data
        this.site = in.readParcelable(Site.class.getClassLoader());

        // Ensure flags read from a Parcel are processed using STANDARD cleaning rules (REMOVAL).
        if (this.vodFlags != null) {
            for (Flag item : this.vodFlags) {
                processExistingFlagItemStandard(item);
            }
        }
        // Note: The specific vodPic pattern replacement happens in the getter,
        // so it will be applied when getVodPic() is called after reading from Parcel.
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