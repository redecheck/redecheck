package twalsh.redecheck;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;

/**
 * Created by thomaswalsh on 14/10/2015.
 */
public class ImageComparator {
    ImageInfo origInfo, testInfo;
    MagickImage origImage, testImage;

    public ImageComparator(String o, String t) {
        try {
            origInfo = new ImageInfo(o);
            testInfo = new ImageInfo(t);
            origImage = new MagickImage(origInfo);
            testImage = new MagickImage(testInfo);
        } catch (MagickException e) {
            e.printStackTrace();
        }

    }
}
