package com.rndtechnosoft.fynder.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Ravi on 2/19/2017.
 */

@IgnoreExtraProperties
public class RoomImage {
    private String filename;
    private String thumbPic;
    private String originalPic;

    public RoomImage(){}

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getThumbPic() {
        return thumbPic;
    }

    public void setThumbPic(String thumbPic) {
        this.thumbPic = thumbPic;
    }

    public String getOriginalPic() {
        return originalPic;
    }

    public void setOriginalPic(String originalPic) {
        this.originalPic = originalPic;
    }
}
