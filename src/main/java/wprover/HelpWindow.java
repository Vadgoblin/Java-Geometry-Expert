package wprover;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

public class HelpWindow extends JFrame {

    private static HelpWindow instance;

    private final JEditorPane tocPane;
    private final JEditorPane contentPane;
    private final JButton btnBack;
    private final JButton btnForward;

    private final Deque<URL> backHistory = new ArrayDeque<>();
    private final Deque<URL> forwardHistory = new ArrayDeque<>();
    private URL currentUrl = null;

    public HelpWindow() {
        super("Java Geometry Expert - Help");
        setSize(1100, 750);
        setMinimumSize(new Dimension(700, 450));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        // 1. Navigation Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        btnBack = new JButton("◀ Back");
        btnBack.setEnabled(false);
        btnBack.addActionListener(e -> goBack());

        btnForward = new JButton("Forward ▶");
        btnForward.setEnabled(false);
        btnForward.addActionListener(e -> goForward());

        JButton btnHome = new JButton("Home");
        btnHome.addActionListener(e -> {
            URL homeUrl = getClass().getResource("/docs/help/what_is_jgex.html");
            if (homeUrl != null) {
                navigateTo(homeUrl, true);
            }
        });

        toolBar.add(btnBack);
        toolBar.add(btnForward);
        toolBar.add(btnHome);

        // 2. Panes
        tocPane = createHtmlPane();
        contentPane = createHtmlPane();

        tocPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                handleHyperlinkClick(e);
            }
        });

        contentPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                handleHyperlinkClick(e);
            }
        });

        // 3. Layout
        JScrollPane tocScroll = new JScrollPane(tocPane);
        tocScroll.setMinimumSize(new Dimension(200, 200));

        JScrollPane contentScroll = new JScrollPane(contentPane);
        contentScroll.setMinimumSize(new Dimension(400, 200));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tocScroll, contentScroll);
        splitPane.setDividerLocation(260);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        // 4. Initial Load
        URL tocUrl = getClass().getResource("/docs/help/Contents.html");
        if (tocUrl != null) {
            loadHtmlAsync(tocPane, tocUrl);
        }

        URL startUrl = getClass().getResource("/docs/help/what_is_jgex.html");
        if (startUrl == null) {
            startUrl = getClass().getResource("/docs/help/index.html");
        }
        if (startUrl != null) {
            navigateTo(startUrl, false);
        }
    }

    private JEditorPane createHtmlPane() {
        JEditorPane pane = new JEditorPane();
        pane.setEditable(false);
        pane.setContentType("text/html");
        pane.setBackground(Color.WHITE);

        HTMLEditorKit kit = new HTMLEditorKit();
        kit.getStyleSheet().addRule("body { font-family: sans-serif; font-size: 13pt; margin: 12px; color: #111; }");
        kit.getStyleSheet().addRule("a { color: #0044cc; text-decoration: underline; }");
        kit.getStyleSheet().addRule("h1, h2, h3 { color: #1a4f8a; }");
        pane.setEditorKit(kit);

        return pane;
    }

    private void handleHyperlinkClick(HyperlinkEvent e) {
        String desc = e.getDescription();

        // 1. Intercept JavaScript calls (prevents freeze on video/dynamic buttons)
        if (desc != null && desc.toLowerCase().trim().startsWith("javascript:")) {
            JOptionPane.showMessageDialog(this,
                    "Interactive JavaScript features (like embedded video players) are not supported in the built-in documentation reader.",
                    "Interactive Feature",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        URL targetUrl = e.getURL();
        if (targetUrl == null) {
            // Sometimes local relative anchors only provide description
            if (desc != null && (desc.endsWith(".html") || desc.endsWith(".htm"))) {
                targetUrl = getClass().getResource("/docs/help/" + desc);
            }
        }

        if (targetUrl == null) return;

        String path = targetUrl.getPath().toLowerCase();

        // 2. Intercept Video, Flash, and non-HTML media files
        if (path.endsWith(".swf") || path.endsWith(".flv") || path.endsWith(".avi")
                || path.endsWith(".mp4") || path.endsWith(".wmv") || path.endsWith(".mov")
                || path.endsWith(".pdf")) {
            JOptionPane.showMessageDialog(this,
                    "This link points to an external media file:\n" + targetUrl.getFile() +
                            "\n\nVideo and multimedia playback cannot run inside this help window.",
                    "Unsupported Media Format",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        navigateTo(targetUrl, true);
    }

    private void navigateTo(URL url, boolean recordHistory) {
        if (url == null) return;

        if (recordHistory && currentUrl != null && !currentUrl.equals(url)) {
            backHistory.push(currentUrl);
            forwardHistory.clear();
        }

        currentUrl = url;
        loadHtmlAsync(contentPane, url);
        updateNavButtons();
    }

    private void goBack() {
        if (!backHistory.isEmpty()) {
            forwardHistory.push(currentUrl);
            currentUrl = backHistory.pop();
            loadHtmlAsync(contentPane, currentUrl);
            updateNavButtons();
        }
    }

    private void goForward() {
        if (!forwardHistory.isEmpty()) {
            backHistory.push(currentUrl);
            currentUrl = forwardHistory.pop();
            loadHtmlAsync(contentPane, currentUrl);
            updateNavButtons();
        }
    }

    private void updateNavButtons() {
        btnBack.setEnabled(!backHistory.isEmpty());
        btnForward.setEnabled(!forwardHistory.isEmpty());
    }

    /**
     * Loads and sanitizes HTML on a worker thread to keep the Swing UI responsive.
     */
    private void loadHtmlAsync(JEditorPane pane, URL url) {
        new Thread(() -> {
            try (InputStream in = url.openStream()) {
                String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);

                // 1. Strip Byte Order Marks (BOM) & leading whitespace
                content = content.replace("\uFEFF", "").trim();

                // 2. Aggressively strip XML prologues and DOCTYPE headers
                content = content.replaceAll("(?is)^<\\?xml[^>]*\\?>", "");
                content = content.replaceAll("(?is)<\\?xml[^>]*\\?>", "");
                content = content.replaceAll("(?is)^\\?xml[^>]*\\?>", ""); // in case '<' was stripped
                content = content.replaceAll("(?is)<\\!DOCTYPE[^>]*>", "");
                content = content.trim();

                // Fallback check: if it still begins with ?xml, strip up to the closing tag or newline
                if (content.startsWith("?xml")) {
                    int endIdx = content.indexOf("?>");
                    if (endIdx != -1) {
                        content = content.substring(endIdx + 2).trim();
                    } else {
                        int newlineIdx = content.indexOf('\n');
                        if (newlineIdx != -1) {
                            content = content.substring(newlineIdx + 1).trim();
                        }
                    }
                }

                // 3. Strip charset meta tags
                content = content.replaceAll("(?i)<meta[^>]+charset[^>]*>", "");
                content = content.replaceAll("(?i)<meta[^>]+http-equiv[^>]*>", "");

                // 4. Strip scripts, objects, embeds, and external css
                content = content.replaceAll("(?is)<script.*?>.*?</script>", "");
                content = content.replaceAll("(?is)<object.*?>.*?</object>", "");
                content = content.replaceAll("(?is)<embed.*?>", "");
                content = content.replaceAll("(?is)<link[^>]*stylesheet[^>]*>", "");

                // 5. Normalize image paths
                content = content.replaceAll("(?i)src=[\"'](?:\\.\\./|/)*(images[0-9_a-z]*/[^\"']+)[\"']", "src=\"$1\"");
                content = content.replaceAll("(?i)src=[\"'](?:\\.\\./|/)*(effects/[^\"']+)[\"']", "src=\"$1\"");

                final String finalContent = content;

                SwingUtilities.invokeLater(() -> {
                    try {
                        HTMLDocument doc = (HTMLDocument) pane.getEditorKit().createDefaultDocument();
                        doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);

                        URL baseUrl = getClass().getResource("/docs/help/");
                        doc.setBase(baseUrl != null ? baseUrl : url);

                        pane.setDocument(doc);
                        pane.getEditorKit().read(new StringReader(finalContent), doc, 0);
                        pane.setCaretPosition(0);
                    } catch (Exception e) {
                        pane.setText("<html><body><p>Error rendering document.</p></body></html>");
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    pane.setText("<html><body style='padding:20px; color:red;'>" +
                            "<h3>Could not load document:</h3><p>" + url + "</p></body></html>");
                });
            }
        }).start();
    }

    public static void openHelp(JFrame parent) {
        SwingUtilities.invokeLater(() -> {
            if (instance == null) {
                instance = new HelpWindow();
            }
            instance.setVisible(true);
            instance.toFront();
        });
    }
}