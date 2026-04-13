import java.awt.*;
import javax.swing.*;

/**
 * End-of-discussion screen. Shows score, letter grade, AI feedback, and navigation buttons.
 * Call showLoading() immediately, then setResults() when scoring finishes.
 */
public class GameOverPanel extends JPanel {

    // ── palette ───────────────────────────────────────────────────────────────
    private static final Color BG         = new Color(22,  18,  14);
    private static final Color BORDER_COL = new Color(95,  58,  20);
    private static final Color TEXT_DIM   = new Color(140, 120, 90);
    private static final Color TEXT_BRIGHT= new Color(235, 220, 185);
    private static final Color ACCENT     = new Color(185, 140, 55);
    private static final Color SCANLINE   = new Color(0, 0, 0, 35);

    // ── inner cards ───────────────────────────────────────────────────────────
    private static final String CARD_LOADING = "loading";
    private static final String CARD_RESULTS = "results";

    private final CardLayout innerCards = new CardLayout();
    private final JPanel     innerRoot  = new JPanel(innerCards);

    // ── result widgets ────────────────────────────────────────────────────────
    private final JLabel    levelLabel   = centeredLabel("", "Monospaced", Font.PLAIN,  13, TEXT_DIM);
    private final JLabel    scoreLabel   = centeredLabel("—", "Monospaced", Font.BOLD,  52, TEXT_BRIGHT);
    private final JLabel    outOfLabel   = centeredLabel("/ 100", "Monospaced", Font.PLAIN, 16, TEXT_DIM);
    private final JLabel    gradeLabel   = centeredLabel("", "Monospaced", Font.BOLD,  32, TEXT_BRIGHT);
    private final JTextArea feedbackArea = buildFeedbackArea();

    // ── constructor ───────────────────────────────────────────────────────────

    public GameOverPanel(Runnable onNextLevel, Runnable onPlayAgain) {
        setBackground(BG);
        setLayout(new GridBagLayout());

        innerRoot.setOpaque(false);
        innerRoot.add(buildLoadingCard(), CARD_LOADING);
        innerRoot.add(buildResultsCard(onNextLevel, onPlayAgain), CARD_RESULTS);
        innerCards.show(innerRoot, CARD_LOADING);

        add(innerRoot);
    }

    // ── public API ────────────────────────────────────────────────────────────

    public void showLoading() {
        innerCards.show(innerRoot, CARD_LOADING);
    }

    public void setResults(int score, String feedback, int level) {
        scoreLabel.setText(String.valueOf(Math.max(0, Math.min(100, score))));
        String grade = letterGrade(score);
        gradeLabel.setText(grade);
        gradeLabel.setForeground(gradeColor(score));
        feedbackArea.setText(feedback);
        feedbackArea.setCaretPosition(0);
        levelLabel.setText("LEVEL  " + level + "  ·  DISCUSSION COMPLETE");
        innerCards.show(innerRoot, CARD_RESULTS);
        repaint();
    }

    // ── card builders ─────────────────────────────────────────────────────────

    private JPanel buildLoadingCard() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel lbl = centeredLabel("Scoring your discussion...", "Monospaced", Font.PLAIN, 14, TEXT_DIM);
        p.add(lbl);
        return p;
    }

    private JPanel buildResultsCard(Runnable onNextLevel, Runnable onPlayAgain) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // ── header ────────────────────────────────────────────────────────────
        JLabel title = centeredLabel("HARKNESS", "Monospaced", Font.BOLD, 28, ACCENT);
        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(levelLabel);
        p.add(Box.createVerticalStrut(28));

        // ── score row ─────────────────────────────────────────────────────────
        JPanel scoreRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        scoreRow.setOpaque(false);
        scoreRow.add(scoreLabel);
        scoreRow.add(outOfLabel);
        p.add(scoreRow);
        p.add(Box.createVerticalStrut(4));
        p.add(gradeLabel);
        p.add(Box.createVerticalStrut(28));

        // ── divider ───────────────────────────────────────────────────────────
        p.add(buildDivider());
        p.add(Box.createVerticalStrut(16));

        // ── feedback ──────────────────────────────────────────────────────────
        JScrollPane scroll = new JScrollPane(feedbackArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBackground(BG);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COL, 1));
        scroll.setOpaque(false);
        scroll.setPreferredSize(new Dimension(440, 72));
        scroll.setMaximumSize(new Dimension(440, 72));
        scroll.setAlignmentX(CENTER_ALIGNMENT);
        p.add(scroll);
        p.add(Box.createVerticalStrut(32));

        // ── buttons ───────────────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(CENTER_ALIGNMENT);
        btnRow.add(makeButton("NEXT LEVEL  →", ACCENT,    onNextLevel));
        btnRow.add(makeButton("PLAY AGAIN",    TEXT_DIM,  onPlayAgain));
        p.add(btnRow);

        return p;
    }

    // ── painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth(), h = getHeight();

        g2.setColor(BG);
        g2.fillRect(0, 0, w, h);

        // outer border
        g2.setColor(BORDER_COL);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(28, 28, w - 56, h - 56);
        g2.setColor(new Color(62, 38, 12));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(32, 32, w - 64, h - 64);

        // corner decorations
        int[][] corners = {{34,34},{w-42,34},{34,h-42},{w-42,h-42}};
        for (int[] c : corners) {
            g2.setColor(ACCENT);      g2.fillRect(c[0], c[1], 6, 6);
            g2.setColor(BORDER_COL);  g2.drawRect(c[0], c[1], 6, 6);
        }

        // CRT scanlines
        g2.setColor(SCANLINE);
        for (int y = 0; y < h; y += 3) g2.drawLine(0, y, w, y);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static JLabel centeredLabel(String text, String family, int style, int size, Color fg) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font(family, style, size));
        lbl.setForeground(fg);
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        return lbl;
    }

    private static JTextArea buildFeedbackArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Monospaced", Font.PLAIN, 11));
        area.setBackground(new Color(32, 26, 18));
        area.setForeground(TEXT_BRIGHT);
        area.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return area;
    }

    private static JPanel buildDivider() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_COL);
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        d.setOpaque(false);
        d.setPreferredSize(new Dimension(440, 6));
        d.setMaximumSize(new Dimension(440, 6));
        d.setAlignmentX(CENTER_ALIGNMENT);
        return d;
    }

    private static JButton makeButton(String text, Color fg, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setBackground(new Color(40, 30, 18));
        btn.setOpaque(true);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL, 2),
            BorderFactory.createEmptyBorder(9, 20, 9, 20)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(65, 48, 22)); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(new Color(40, 30, 18)); }
        });
        return btn;
    }

    private static String letterGrade(int score) {
        if (score >= 93) return "A";
        if (score >= 90) return "A−";
        if (score >= 87) return "B+";
        if (score >= 83) return "B";
        if (score >= 80) return "B−";
        if (score >= 77) return "C+";
        if (score >= 73) return "C";
        if (score >= 70) return "C−";
        if (score >= 60) return "D";
        return "F";
    }

    private static Color gradeColor(int score) {
        if (score >= 80) return new Color(120, 200, 110);
        if (score >= 70) return new Color(215, 195, 80);
        if (score >= 60) return new Color(225, 145, 55);
        return new Color(205, 75, 65);
    }
}
