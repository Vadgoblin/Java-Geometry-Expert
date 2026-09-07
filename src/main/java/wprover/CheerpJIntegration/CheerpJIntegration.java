package wprover.CheerpJIntegration;

public final class CheerpJIntegration {

    public static final boolean IS_RUNNING_IN_CHEERPJ = checkCheerpJ();

    private CheerpJIntegration() {

    }

    /**
     * Checks if the application is running in a CheerpJ web environment.
     *
     * @return true if likely running in CheerpJ, false otherwise.
     */
    private static boolean checkCheerpJ() {
        try {
            String vmName = System.getProperty("java.vm.name", "");
            String vmVendor = System.getProperty("java.vm.vendor", "");
            if (vmName.toLowerCase().contains("cheerpj") || vmVendor.toLowerCase().contains("leaning technologies")) {
                return true;
            }

            if (Boolean.getBoolean("cheerpj") || Boolean.getBoolean("browser")) {
                return true;
            }

            try {
                Class.forName("com.leaningtech.cheerpj.CheerpJUtils");
                return true;
            } catch (ClassNotFoundException ignored) {
                // Runtime class not present, fall through
            }

        } catch (SecurityException ignored) {
            // Sandboxed environments or restricted applet-like contexts
            return false;
        }

        return false;
    }

    public static boolean isRunningInCheerpJ() {
        return IS_RUNNING_IN_CHEERPJ;
    }
}