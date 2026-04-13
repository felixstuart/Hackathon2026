import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;

/**
 * Styled chat sidebar — 300px wide, matches BookSelectPanel's warm-dark palette.
 *
 * Layout:
 *   NORTH  — "DISCUSSION" title bar
 *   CENTER — scrollable message log (read-only, per-speaker colors)
 *   SOUTH  — text input field (Enter to submit)
 */
public class ChatPanel extends JPanel {

    // ── palette (mirrors BookSelectPanel) ────────────────────────────────────
    private static final Color BG          = new Color(22,  18,  14);
    private static final Color BORDER_COL  = new Color(95,  58,  20);
    private static final Color TEXT_DIM    = new Color(140, 120, 90);
    private static final Color TEXT_BRIGHT = new Color(235, 220, 185);
    private static final Color ACCENT      = new Color(185, 140, 55);

    // ── per-speaker colors ────────────────────────────────────────────────────
    private static final Color   TEACHER_COLOR  = new Color(160, 200, 155);
    private static final Color[] STUDENT_COLORS = {
        new Color(200, 120,  90),   // seat 1 — dusty red
        new Color( 90, 160, 215),   // seat 2 — sky blue
        new Color(110, 195, 130),   // seat 3 — mint green
        new Color(175, 120, 210),   // seat 4 — lavender
        new Color(220, 165,  80),   // seat 5 — warm amber
        new Color( 80, 200, 205),   // seat 6 — teal
    };

    private static final Font MONO_12 = new Font("Monospaced", Font.PLAIN,  12);
    private static final Font MONO_11 = new Font("Monospaced", Font.PLAIN,  11);
    private static final Font MONO_13 = new Font("Monospaced", Font.BOLD,   13);

    private static final int WIDTH = 300;

    // ── components ────────────────────────────────────────────────────────────
    private final JTextPane  messageArea;
    private final JTextField inputField;

    private Consumer<String> onSubmit  = text -> {};
    private Runnable         onTyping  = () -> {};

    public ChatPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(WIDTH, 0));
        setBackground(BG);
        setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, BORDER_COL));

        messageArea = createMessageArea();
        inputField  = createInputField();

        add(buildTitleBar(),              BorderLayout.NORTH);
        add(buildScrollPane(messageArea), BorderLayout.CENTER);
        add(buildInputRow(inputField),    BorderLayout.SOUTH);
    }

    // ── public API ────────────────────────────────────────────────────────────

    public void addPlayerMessage(String text) {
        appendLine("> You: " + text, ACCENT);
    }

    public void addMessage(String speaker, String text) {
        appendLine(speaker + ": " + text, speakerColor(speaker));
    }

    public void requestFocusOnInput() {
        inputField.requestFocusInWindow();
    }

    public void clear() {
        try {
            StyledDocument doc = messageArea.getStyledDocument();
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ignored) {}
        inputField.setText("");
    }

    public void setOnSubmit(Consumer<String> handler) { this.onSubmit = handler; }
    public void setOnTyping(Runnable handler)         { this.onTyping = handler; }

    // ── color lookup ──────────────────────────────────────────────────────────

    private static Color speakerColor(String speaker) {
        if (speaker.equals("Teacher")) return TEACHER_COLOR;
        // Deterministically map any name to a slot by hashing, so the same
        // student always gets the same color within a session.
        int slot = Math.abs(speaker.hashCode()) % STUDENT_COLORS.length;
        return STUDENT_COLORS[slot];
    }

    // ── builder helpers ───────────────────────────────────────────────────────

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        bar.setBackground(BG);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COL));
        JLabel label = new JLabel("DISCUSSION");
        label.setFont(MONO_13);
        label.setForeground(TEXT_DIM);
        bar.add(label);
        return bar;
    }

    private JTextPane createMessageArea() {
        // Override to keep text wrapping within the scroll viewport
        JTextPane area = new JTextPane() {
            @Override public boolean getScrollableTracksViewportWidth() { return true; }
        };
        area.setEditable(false);
        area.setFont(MONO_11);
        area.setBackground(BG);
        area.setForeground(TEXT_BRIGHT);
        area.setCaretColor(BG);
        area.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return area;
    }

    private JScrollPane buildScrollPane(JComponent area) {
        JScrollPane scroll = new JScrollPane(area,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBackground(BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        styleScrollBar(scroll.getVerticalScrollBar());
        return scroll;
    }

    private JTextField createInputField() {
        JTextField field = new JTextField();
        field.setFont(MONO_12);
        field.setBackground(new Color(32, 26, 18));
        field.setForeground(TEXT_BRIGHT);
        field.setCaretColor(ACCENT);
        field.setSelectionColor(BORDER_COL);
        field.setSelectedTextColor(TEXT_BRIGHT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COL),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        field.addActionListener(e -> submitMessage());

        // Notify onTyping whenever the player adds characters (not on clear-after-submit)
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onTyping.run(); }
            public void removeUpdate(DocumentEvent e) {}
            public void changedUpdate(DocumentEvent e) {}
        });

        return field;
    }

    private JPanel buildInputRow(JTextField field) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // ── internal logic ────────────────────────────────────────────────────────

    private void submitMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        inputField.setText("");
        addPlayerMessage(text);
        inputField.requestFocusInWindow();
        onSubmit.accept(text);
    }

    private void appendLine(String line, Color color) {
        StyledDocument doc = messageArea.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, color);
        StyleConstants.setFontFamily(attrs, "Monospaced");
        StyleConstants.setFontSize(attrs, 11);
        String text = doc.getLength() == 0 ? line : "\n" + line;
        try {
            doc.insertString(doc.getLength(), text, attrs);
        } catch (BadLocationException ignored) {}
        // Keep scrolled to newest message
        messageArea.setCaretPosition(doc.getLength());
    }

    private static void styleScrollBar(JScrollBar bar) {
        bar.setBackground(BG);
        bar.setForeground(BORDER_COL);
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor            = BORDER_COL;
                trackColor            = BG;
                thumbDarkShadowColor  = BG;
                thumbHighlightColor   = BG;
                thumbLightShadowColor = BG;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }
}
