package wprover.CheerpJIntegration;

import java.io.File;

public class WebSaveFileDialog {

    public static final int APPROVE_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    public int showSaveDialog(Object parent, String virtualPath, String suggestedFileName) {
        if (suggestedFileName == null || suggestedFileName.trim().isEmpty()) {
            suggestedFileName = "untitled.gex";
        }

        File file  = new File(virtualPath);

        triggerJsFileDownload(file.getAbsolutePath(), file.getName());

        return APPROVE_OPTION;
    }


    // Native bridge to trigger download in browser
    private static native void triggerJsFileDownload(String virtualPath, String defaultName);
}