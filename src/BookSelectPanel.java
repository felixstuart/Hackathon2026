import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * Pixel-art book selection screen shown before entering the classroom.
 *
 * Consistent with the classroom scene palette (warm browns, off-whites, dark greys).
 * Navigation: UP/DOWN arrows or mouse click to highlight, ENTER or double-click to confirm.
 * Calls onBookSelected with the chosen book title when the player presses ENTER.
 */
public class BookSelectPanel extends JPanel {

    // ── books ────────────────────────────────────────────────────────────────
    private static final String[] BOOKS = {
        "Twelfth Night",
        "Hamlet",
        "The Great Gatsby",
        "Lord of the Flies",
        "1984",
        "The Scarlet Letter",
        "Brave New World",
        "The Odyssey",
    };

    // ── palette (matches classroom scene) ───────────────────────────────────
    private static final Color BG          = new Color(22,  18,  14);
    private static final Color BORDER      = new Color(95,  58,  20);
    private static final Color WOOD_DARK   = new Color(62,  38,  12);
    private static final Color TEXT_DIM    = new Color(140, 120, 90);
    private static final Color TEXT_BRIGHT = new Color(235, 220, 185);
    private static final Color SEL_BG      = new Color(80,  52,  18);
    private static final Color SEL_BORDER  = new Color(175, 128, 68);
    private static final Color CURSOR_COL  = new Color(210, 165, 75);
    private static final Color SCANLINE    = new Color(0,   0,   0,  35);
    private static final Color ACCENT      = new Color(185, 140, 55);

    // ── state ────────────────────────────────────────────────────────────────
    private int  selectedIndex = 0;
    private boolean blinkOn    = true;

    private final Consumer<String> onBookSelected;
    private final Timer            blinkTimer;

    // ── constructor ──────────────────────────────────────────────────────────
    public BookSelectPanel(Consumer<String> onBookSelected) {
        this.onBookSelected = onBookSelected;
        setPreferredSize(new Dimension(800, 700));
        setBackground(BG);
        setFocusable(true);

        blinkTimer = new Timer(500, e -> { blinkOn = !blinkOn; repaint(); });
        blinkTimer.start();

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        selectedIndex = (selectedIndex - 1 + BOOKS.length) % BOOKS.length;
                        repaint(); break;
                    case KeyEvent.VK_DOWN:
                        selectedIndex = (selectedIndex + 1) % BOOKS.length;
                        repaint(); break;
                    case KeyEvent.VK_ENTER:
                        confirm(); break;
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int hit = hitTest(e.getY());
                if (hit >= 0) {
                    if (hit == selectedIndex && e.getClickCount() == 2) {
                        confirm();
                    } else {
                        selectedIndex = hit;
                        repaint();
                    }
                }
                requestFocusInWindow();
            }
        });
    }

    public void stopBlink() { blinkTimer.stop(); }

    // ── painting ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int w = getWidth(), h = getHeight();

        // ── background ───────────────────────────────────────────────────
        g2.setColor(BG);
        g2.fillRect(0, 0, w, h);

        // Pixel-art bookshelf background (top band)
        drawBookshelf(g2, w);

        // ── outer panel border ───────────────────────────────────────────
        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(28, 28, w - 56, h - 56);
        g2.setColor(WOOD_DARK);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(32, 32, w - 64, h - 64);

        // ── title ─────────────────────────────────────────────────────────
        drawPixelTitle(g2, w);

        // ── subtitle ─────────────────────────────────────────────────────
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(TEXT_DIM);
        String sub = "SELECT A TEXT FOR DISCUSSION";
        g2.drawString(sub, (w - g2.getFontMetrics().stringWidth(sub)) / 2, 168);

        // ── divider line ─────────────────────────────────────────────────
        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(80, 180, w - 80, 180);
        g2.setStroke(new BasicStroke(1f));

        // ── book list ─────────────────────────────────────────────────────
        drawBookList(g2, w);

        // ── bottom prompt ─────────────────────────────────────────────────
        if (blinkOn) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.setColor(CURSOR_COL);
            String prompt = "[ ENTER ] BEGIN DISCUSSION";
            g2.drawString(prompt, (w - g2.getFontMetrics().stringWidth(prompt)) / 2, h - 55);
        }

        // Arrow hints
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(TEXT_DIM);
        String arrows = "^  UP         DOWN  v";
        g2.drawString(arrows, (w - g2.getFontMetrics().stringWidth(arrows)) / 2, h - 38);

        // ── CRT scanline overlay ──────────────────────────────────────────
        g2.setColor(SCANLINE);
        for (int y = 0; y < h; y += 3) g2.drawLine(0, y, w, y);

        // ── corner decorations ────────────────────────────────────────────
        drawCornerDeco(g2, 34, 34);
        drawCornerDeco(g2, w - 42, 34);
        drawCornerDeco(g2, 34, h - 42);
        drawCornerDeco(g2, w - 42, h - 42);

        g2.dispose();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void drawBookshelf(Graphics2D g2, int w) {
        // Dark shelf background
        g2.setColor(new Color(32, 24, 16));
        g2.fillRect(0, 0, w, 82);
        // Shelf plank
        g2.setColor(new Color(88, 58, 22));
        g2.fillRect(0, 78, w, 8);
        g2.setColor(new Color(110, 75, 30));
        g2.fillRect(0, 78, w, 2);
        // Book spines (decorative pixel art)
        int[] bookWidths = {12, 9, 14, 8, 11, 13, 10, 9, 15, 8, 12, 11, 14, 9, 10, 13};
        int[] bookColors = {
            0x8B2020, 0x205A8B, 0x206B3A, 0x7A5C10, 0x5A1A8B,
            0x8B4820, 0x1A5C6B, 0x7A2060, 0x3A6B20, 0x6B3A10,
            0x20488B, 0x8B6B10, 0x601A3A, 0x20603A, 0x8B3010, 0x104858
        };
        int bx = 45;
        for (int i = 0; i < bookWidths.length && bx < w - 50; i++) {
            int bw = bookWidths[i % bookWidths.length];
            int bc = bookColors[i % bookColors.length];
            g2.setColor(new Color(bc));
            g2.fillRect(bx, 12 + (i % 3) * 4, bw, 62 - (i % 3) * 4);
            g2.setColor(new Color(bc).brighter());
            g2.fillRect(bx + 1, 14 + (i % 3) * 4, 2, 10);
            g2.setColor(new Color(bc).darker());
            g2.drawRect(bx, 12 + (i % 3) * 4, bw, 62 - (i % 3) * 4);
            bx += bw + 2;
        }
    }

    private void drawPixelTitle(Graphics2D g2, int w) {
        // "HARKNESS" drawn as large pixel-art letters using 5x7 pixel grid scaled 3x
        g2.setFont(new Font("Monospaced", Font.BOLD, 36));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        FontMetrics fm = g2.getFontMetrics();
        String title = "HARKNESS";
        int tx = (w - fm.stringWidth(title)) / 2;
        // Shadow
        g2.setColor(new Color(60, 38, 10));
        g2.drawString(title, tx + 3, 132);
        // Main text
        g2.setColor(ACCENT);
        g2.drawString(title, tx, 129);
        // Highlight pass (1px offset)
        g2.setColor(new Color(255, 220, 130));
        g2.drawString(title, tx - 1, 128);
    }

    private static final int LIST_TOP    = 198;
    private static final int ITEM_HEIGHT = 44;
    private static final int LIST_LEFT   = 110;
    private static final int LIST_RIGHT  = 690;

    private void drawBookList(Graphics2D g2, int w) {
        for (int i = 0; i < BOOKS.length; i++) {
            int itemY = LIST_TOP + i * ITEM_HEIGHT;
            boolean sel = (i == selectedIndex);

            // Selection background
            if (sel) {
                g2.setColor(SEL_BG);
                g2.fillRect(LIST_LEFT - 4, itemY, LIST_RIGHT - LIST_LEFT + 8, ITEM_HEIGHT - 4);
                g2.setColor(SEL_BORDER);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(LIST_LEFT - 4, itemY, LIST_RIGHT - LIST_LEFT + 8, ITEM_HEIGHT - 4);
                g2.setStroke(new BasicStroke(1f));
            }

            // Cursor arrow
            if (sel) {
                g2.setFont(new Font("Monospaced", Font.BOLD, 14));
                g2.setColor(CURSOR_COL);
                g2.drawString(">", LIST_LEFT - 22, itemY + 26);
            }

            // Book number
            g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g2.setColor(sel ? TEXT_DIM : new Color(80, 62, 38));
            g2.drawString(String.format("%02d", i + 1), LIST_LEFT + 4, itemY + 26);

            // Book title
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));
            g2.setColor(sel ? TEXT_BRIGHT : TEXT_DIM);
            g2.drawString(BOOKS[i], LIST_LEFT + 36, itemY + 26);

            // Separator line
            if (!sel && i < BOOKS.length - 1) {
                g2.setColor(new Color(55, 40, 22));
                g2.drawLine(LIST_LEFT, itemY + ITEM_HEIGHT - 2, LIST_RIGHT, itemY + ITEM_HEIGHT - 2);
            }
        }
    }

    private void drawCornerDeco(Graphics2D g2, int x, int y) {
        g2.setColor(ACCENT);
        g2.fillRect(x, y, 6, 6);
        g2.setColor(BORDER);
        g2.drawRect(x, y, 6, 6);
    }

    /** Returns book index at screen y, or -1 if no hit. */
    private int hitTest(int mouseY) {
        for (int i = 0; i < BOOKS.length; i++) {
            int y0 = LIST_TOP + i * ITEM_HEIGHT;
            if (mouseY >= y0 && mouseY < y0 + ITEM_HEIGHT - 4) return i;
        }
        return -1;
    }

    private void confirm() {
        stopBlink();
        onBookSelected.accept(BOOKS[selectedIndex]);
    }
}
