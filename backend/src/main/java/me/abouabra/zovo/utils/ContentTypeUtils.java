package me.abouabra.zovo.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ContentTypeUtils {

    private static final Map<String, String> mimeToExt;
    static {
        Map<String, String> m = new HashMap<>();
        m.put("image/jpeg", ".jpg");
        m.put("image/jpg",  ".jpg");   // sometimes used interchangeably
        m.put("image/png",  ".png");
        m.put("image/gif",  ".gif");
        m.put("image/webp", ".webp");
        m.put("image/bmp",  ".bmp");
        m.put("image/svg+xml", ".svg");
        m.put("image/tiff", ".tif");
        // add more as needed...
        mimeToExt = Collections.unmodifiableMap(m);
    }

    /**
     * Convert a MIME content-type to a file extension.
     *
     * @param contentType the MIME type, e.g. "image/png"
     * @return the extension including the dot, e.g. ".png"; or empty string if unknown
     */
    public static String convertContentTypeToExtension(String contentType) {
        if (contentType == null) {
            return "";
        }
        // strip off any charset or parameters, e.g. "image/jpeg; charset=UTF-8"
        String ct = contentType.split(";")[0].trim().toLowerCase();
        return mimeToExt.getOrDefault(ct, "");
    }
}
