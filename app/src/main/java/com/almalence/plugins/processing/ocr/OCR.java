package com.almalence.plugins.processing.ocr;

/**
 * Created by maulik on 9/8/16.
 */
public class OCR {
    static
    {
        System.loadLibrary("utils-image");
        System.loadLibrary("almalib");
    }
    public static native void SkewCorrection(int[] compressed_frame,int imagesAmount,int mImageWidth,int mImageHeight);
}
