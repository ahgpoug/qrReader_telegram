package telegram.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ZXing {
    public static String readQRCode(File file) {
        BufferedImage image = null;
        BinaryBitmap bitmap = null;
        Result result = null;

        try {
            image = ImageIO.read(file);
            int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            RGBLuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
            bitmap = new BinaryBitmap(new HybridBinarizer(source));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap == null)
            return null;

        QRCodeReader reader = new QRCodeReader();
        try {
            result = reader.decode(bitmap);
            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String readQr(File file){
        try {
            Map hintMap = new HashMap();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(file))));
            Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
            return qrCodeResult.getText();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
