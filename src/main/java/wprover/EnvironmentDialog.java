package wprover;

import UI.DropShadowBorder;
import wprover.CheerpJIntegration.CheerpJIntegration;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A lightweight popup dialog to display the current execution environment.
 */
public class EnvironmentDialog extends JPopupMenu {

    private static final Color BG_COLOR = new Color(206, 223, 242);
    private final GExpert gx;

    /**
     * Constructs the popup displaying the supplied execution environment string.
     *
     * @param f                  the parent GExpert frame
     * @param executionEnvString the name/details of the execution environment (e.g. "Native Desktop", "CheerpJ Browser")
     */
    public EnvironmentDialog(GExpert f, String executionEnvString) {
        this.gx = f;

        // Apply drop-shadow border only on desktop (fails in CheerpJ)
        if (!CheerpJIntegration.isRunningInCheerpJ()) {
            this.setBorder(BorderFactory.createCompoundBorder(
                    new DropShadowBorder(),
                    BorderFactory.createLineBorder(BG_COLOR, 4)
            ));
        } else {
            this.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }

        // Main content container
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(12, 18, 12, 18));

        // Title Label
        JLabel titleLabel = new JLabel("Execution Environment");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13.0f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);

        contentPanel.add(Box.createVerticalStrut(8));

        // Environment Value Label
        JLabel envLabel = new JLabel(executionEnvString != null ? executionEnvString : "Unknown");
        envLabel.setFont(envLabel.getFont().deriveFont(Font.PLAIN, 12.0f));
        envLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(envLabel);

        contentPanel.add(Box.createVerticalStrut(10));

        // Dismiss hint / button
        JLabel dismissLabel = new JLabel("(Click to close)");
        dismissLabel.setFont(dismissLabel.getFont().deriveFont(Font.ITALIC, 10.0f));
        dismissLabel.setForeground(Color.DARK_GRAY);
        dismissLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(dismissLabel);

        // Close when clicking anywhere on the popup
        MouseAdapter clickToDismiss = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
            }
        };

        contentPanel.addMouseListener(clickToDismiss);
        titleLabel.addMouseListener(clickToDismiss);
        envLabel.addMouseListener(clickToDismiss);
        dismissLabel.addMouseListener(clickToDismiss);

        this.add(contentPanel);
    }

    /**
     * Shows the dialog centered relative to the parent frame.
     */
    public void showCentered() {
        if (gx != null && gx.isVisible()) {
            int x = (gx.getWidth() - getPreferredSize().width) / 2;
            int y = (gx.getHeight() - getPreferredSize().height) / 3;
            show(gx, Math.max(x, 0), Math.max(y, 0));
        }
    }
}