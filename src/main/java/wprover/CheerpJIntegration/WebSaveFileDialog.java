package wprover.CheerpJIntegration;

import java.io.File;

public class WebSaveFileDialog {

    private WebSaveFileDialog(){}

    public static void showSaveDialog(Object parent, String virtualPath, String suggestedFileName) {
        if (suggestedFileName == null || suggestedFileName.trim().isEmpty()) {
            suggestedFileName = "untitled.gex";
        }

        File file  = new File(virtualPath);

        triggerJsFileDownload(file.getAbsolutePath(), file.getName());
    }


    // Native bridge to trigger download in browser
    private static native void triggerJsFileDownload(String virtualPath, String defaultName);
}