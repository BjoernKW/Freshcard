package com.freshcard.backend.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willy on 02.11.14.
 */
public class UploadHelper {
    private List<String> validExtensions = new ArrayList<String>();

    public UploadHelper() {
        validExtensions.add("jpg");
        validExtensions.add("jpeg");
        validExtensions.add("gif");
        validExtensions.add("png");
        validExtensions.add("svg");
        validExtensions.add("svgz");
    }

    public Boolean isValidImageSuffix(String suffix) {
        return validExtensions.contains(suffix);
    }
}
