package telegram.dbx;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import telegram.util.Crypto;

import java.io.*;

public class DbxHelper {
    private static DbxRequestConfig config = new DbxRequestConfig("dropbox/telegramClient1");

    public static boolean downloadDb(String userId, String token){
        try {
            token = Crypto.decrypt(token);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        DbxClientV2 client = new DbxClientV2(config, token);

        File path = new File("sqlite");
        if (!path.exists()) {
            if (!path.mkdirs()) {
                return false;
            }
        } else if (!path.isDirectory()) {
            return false;
        }

        String name = String.format("sqlite_%s.db", userId);
        File file = new File(path, name);

        try {
            OutputStream out = new FileOutputStream(file);
            client.files().download("/sqlite.db").download(out);
        } catch (Exception e) {
            System.out.println("Invalid token");
            return false;
        }
        return true;
    }

    public static void uploadTaskPDF(File file){
        DbxClientV2 client = new DbxClientV2(config, "Gtb6zMf7DEIAAAAAAAABeYIOWPWqKQybGn5CjBFDcv3itVm9F5eou_wdCy2XWNvZ");

        try (InputStream in = new FileInputStream(file)) {
            String path = "/1.jpg";
            client.files().uploadBuilder(path).uploadAndFinish(in);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
