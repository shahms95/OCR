package com.almalence.plugins.processing.ocr;

/**
 * Created by maulik on 6/8/16.
 */

import com.almalence.opencam.ConfigParser;
import com.almalence.opencam.PluginManager;
import com.almalence.opencam.PluginProcessing;
import com.almalence.plugins.processing.bestshot.AlmaShotBestShot;


public class OCRProcessingPlugin extends PluginProcessing {
    private long	sessionID	= 0;

    public OCRProcessingPlugin()
    {
        super("com.almalence.plugins.ocrprocessing", "ocrmode", 0, 0, 0, null);
    }

    @Override
    public void onStartProcessing(long SessionID) {
        sessionID = SessionID;

        PluginManager.getInstance().addToSharedMem("modeSaveName" + sessionID,
                ConfigParser.getInstance().getMode(mode).modeSaveName);

        int mImageWidth = Integer.parseInt(PluginManager.getInstance().getFromSharedMem("imageWidth" + sessionID));
        int mImageHeight = Integer.parseInt(PluginManager.getInstance().getFromSharedMem("imageHeight" + sessionID));

        String num = PluginManager.getInstance().getFromSharedMem("amountofcapturedframes" + sessionID);
        if (num == null)
            return;
        int imagesAmount = Integer.parseInt(num);

        if (imagesAmount == 0)
            imagesAmount = 1;

        int orientation = Integer.parseInt(PluginManager.getInstance()
                .getFromSharedMem("frameorientation1" + sessionID));
        AlmaShotBestShot.Initialize();      //TODO something about initialize
        int[] compressed_frame = new int[imagesAmount];
        int[] compressed_frame_len = new int[imagesAmount];

        for (int i = 0; i < imagesAmount; i++) {
            compressed_frame[i] = Integer.parseInt(PluginManager.getInstance().getFromSharedMem(
                    "frame" + (i + 1) + sessionID));
            compressed_frame_len[i] = Integer.parseInt(PluginManager.getInstance().getFromSharedMem(
                    "framelen" + (i + 1) + sessionID));
        }
        OCR.SkewCorrection(compressed_frame,imagesAmount,mImageWidth,mImageHeight);
        AlmaShotBestShot.Release();

        if (orientation == 90 || orientation == 270) {
            PluginManager.getInstance().addToSharedMem("saveImageWidth" + sessionID, String.valueOf(mImageHeight));
            PluginManager.getInstance().addToSharedMem("saveImageHeight" + sessionID, String.valueOf(mImageWidth));
        } else {
            PluginManager.getInstance().addToSharedMem("saveImageWidth" + sessionID, String.valueOf(mImageWidth));
            PluginManager.getInstance().addToSharedMem("saveImageHeight" + sessionID, String.valueOf(mImageHeight));
        }
        boolean cameraMirrored = Boolean.parseBoolean(PluginManager.getInstance().getFromSharedMem(
                "framemirrored1" + sessionID));
        PluginManager.getInstance().addToSharedMem("resultframeorientation1" + sessionID, String.valueOf(orientation));
        PluginManager.getInstance().addToSharedMem("resultframemirrored1" + sessionID, String.valueOf(cameraMirrored));

        PluginManager.getInstance().addToSharedMem("amountofresultframes" + sessionID, String.valueOf(1));
    }

        @Override
    public boolean isPostProcessingNeeded()
    {
        return false;
    }

    @Override
    public void onStartPostProcessing()
    {
    }
}
