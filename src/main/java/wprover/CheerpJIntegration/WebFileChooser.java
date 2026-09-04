package wprover.CheerpJIntegration;

import java.io.File;

public class WebFileChooser {

    public static final int APPROVE_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    private File selectedFile;

    public int showOpenDialog(Object parent) {
        selectedFile = null;

        String virtualPath = triggerJsFileDialog();

        if (virtualPath != null && !virtualPath.isEmpty()) {
            this.selectedFile = new File(virtualPath);
            return APPROVE_OPTION;
        }

        return CANCEL_OPTION;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    // Declare native method for calling JavaScript
    private static native String triggerJsFileDialog();
}