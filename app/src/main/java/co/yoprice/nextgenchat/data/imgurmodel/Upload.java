package co.yoprice.nextgenchat.data.imgurmodel;

import java.io.File;

public class Upload {
    public File image;
    public String title;
    public String description;
    public String albumId;

    public static Upload createUpload(File image) {
        Upload upload = new Upload();
        upload.image = image;
        upload.title = "NextGenChat Upload";
        upload.description = "Uploaded Via NextGenChat";
        return upload;
    }
}
