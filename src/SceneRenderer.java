import java.awt.*;
import java.awt.geom.*;

/**
 * Draws the first-person classroom scene.
 *
 * Camera angle: seated at the near end of the table, looking across.
 *
 * Seat layout (first-person view):
 *
 *          [S1]  [Teacher]  [S6]         ← far arc (y ≈ 280)
 *       [S2]                   [S5]      ← mid (y ≈ 410)
 *     [S3]                       [S4]    ← near sides (y ≈ 545)
 *               [PLAYER]                 ← bottom-center (off-screen)
 *
 * Seat indices: 1-6 = students, 7 = teacher.
 */
public class SceneRenderer {

    // ── seat positions [centerX, baselineY] ────────────────────────────────
    private static final int[][] SEATS = {
        {},                     // 0 = player (unused)
        {220, 285},             // 1  far-left student
        {145, 415},             // 2  mid-left student
        {105, 548},             // 3  near-left student
        {695, 548},             // 4  near-right student
        {655, 415},             // 5  mid-right student
        {580, 285},             // 6  far-right student
        {400, 260},             // 7  teacher (top-center)
    };

    // Table trapezoid corners (perspective: wider near player)
    static final int FAR_LEFT_X  = 185;
    static final int FAR_RIGHT_X = 615;
    static final int FAR_Y       = 318;
    static final int NEAR_LEFT_X = 28;
    static final int NEAR_RIGHT_X= 772;
    static final int NEAR_Y      = 648;

    // ── public draw API ─────────────────────────────────────────────────────

    public static void drawBackground(Graphics2D g2, int w, int h) {
        // ── ceiling / upper wall ──────────────────────────────────────────
        g2.setColor(new Color(60, 55, 50));
        g2.fillRect(0, 0, w, 80);                          // ceiling strip

        // Fluorescent lights (pixel art ceiling fixtures)
        g2.setColor(new Color(245, 245, 210));
        g2.fillRect(120, 12, 140, 12);
        g2.fillRect(540, 12, 140, 12);
        g2.setColor(new Color(255, 255, 230, 80));
        g2.fillRect(120, 8, 140, 20);
        g2.fillRect(540, 8, 140, 20);

        // ── back wall ─────────────────────────────────────────────────────
        g2.setColor(new Color(185, 170, 148));
        g2.fillRect(0, 80, w, 240);

        // Chalkboard
        g2.setColor(new Color(42, 72, 48));
        g2.fillRect(160, 90, 480, 170);
        g2.setColor(new Color(35, 60, 40));              // border
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(160, 90, 480, 170);
        // Chalk writing — simple pixel lines
        g2.setColor(new Color(210, 210, 195, 180));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(215, 130, 380, 130);
        g2.drawLine(215, 155, 420, 155);
        g2.drawLine(215, 180, 350, 180);
        g2.drawLine(215, 205, 390, 205);
        g2.drawLine(430, 130, 590, 130);
        // Chalk tray
        g2.setColor(new Color(90, 75, 60));
        g2.fillRect(160, 258, 480, 8);
        g2.setColor(new Color(230, 225, 210, 200));
        g2.fillRect(178, 261, 18, 3);
        g2.fillRect(202, 261, 18, 3);

        // ── side walls ───────────────────────────────────────────────────
        // Left wall (visible sliver)
        g2.setColor(new Color(170, 155, 132));
        g2.fillRect(0, 80, 60, h);
        // Right wall
        g2.fillRect(w - 60, 80, 60, h);

        // Windows on side walls
        drawWindow(g2, 8,  130, 45, 110);
        drawWindow(g2, 8,  265, 45, 110);
        drawWindow(g2, w - 53, 130, 45, 110);
        drawWindow(g2, w - 53, 265, 45, 110);

        // ── floor ────────────────────────────────────────────────────────
        g2.setColor(new Color(148, 120, 88));
        g2.fillRect(0, 320, w, h - 320);

        // Floor planks (horizontal, receding perspective)
        g2.setStroke(new BasicStroke(1f));
        int[] plankY = {360, 400, 445, 495, 550, 610, 680};
        for (int py : plankY) {
            g2.setColor(new Color(130, 104, 72));
            g2.drawLine(0, py, w, py);
        }
        // Vertical plank joints (narrower near top, wider near bottom)
        for (int row = 0; row < plankY.length; row++) {
            int y0 = (row == 0 ? 320 : plankY[row - 1]);
            int y1 = plankY[row];
            int joints = 3 + row;
            for (int j = 1; j <= joints; j++) {
                int jx = (int)(w * j / (double)(joints + 1));
                g2.setColor(new Color(120, 96, 65));
                g2.drawLine(jx, y0, jx, y1);
            }
        }

        // ── ambient vignette at bottom ───────────────────────────────────
        GradientPaint vignette = new GradientPaint(
            0, h - 120f, new Color(0, 0, 0, 0),
            0, h,        new Color(0, 0, 0, 110)
        );
        g2.setPaint(vignette);
        g2.fillRect(0, h - 120, w, 120);
        g2.setStroke(new BasicStroke(1f));
    }

    private static void drawWindow(Graphics2D g2, int x, int y, int w, int h) {
        // Sky / outside
        g2.setColor(new Color(160, 200, 230));
        g2.fillRect(x, y, w, h);
        // Frame
        g2.setColor(new Color(110, 90, 70));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(x, y, w, h);
        // Panes
        g2.setColor(new Color(110, 90, 70));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x + w / 2, y, x + w / 2, y + h);
        g2.drawLine(x, y + h / 2, x + w, y + h / 2);
        g2.setStroke(new BasicStroke(1f));
    }

    public static void drawTableShadow(Graphics2D g2) {
        int[] xs = {NEAR_LEFT_X - 12, NEAR_RIGHT_X + 12, FAR_RIGHT_X + 8, FAR_LEFT_X - 8};
        int[] ys = {NEAR_Y + 18, NEAR_Y + 18, FAR_Y + 18, FAR_Y + 18};
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillPolygon(xs, ys, 4);
    }

    public static void drawTable(Graphics2D g2) {
        int[] xs = {FAR_LEFT_X, FAR_RIGHT_X, NEAR_RIGHT_X, NEAR_LEFT_X};
        int[] ys = {FAR_Y, FAR_Y, NEAR_Y, NEAR_Y};

        // Surface fill
        g2.setColor(new Color(135, 85, 38));
        g2.fillPolygon(xs, ys, 4);

        // Far edge highlight (the oval-ish far end)
        g2.setColor(new Color(170, 115, 58));
        g2.setStroke(new BasicStroke(5f));
        g2.drawArc(FAR_LEFT_X - 20, FAR_Y - 22, (FAR_RIGHT_X - FAR_LEFT_X) + 40, 44, 0, 180);

        // Side edges
        g2.setColor(new Color(95, 55, 18));
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(FAR_LEFT_X, FAR_Y, NEAR_LEFT_X, NEAR_Y);
        g2.drawLine(FAR_RIGHT_X, FAR_Y, NEAR_RIGHT_X, NEAR_Y);
    }

    public static void drawTableGrain(Graphics2D g2) {
        // Clip to table shape
        Shape prev = g2.getClip();
        int[] xs = {FAR_LEFT_X + 3, FAR_RIGHT_X - 3, NEAR_RIGHT_X - 3, NEAR_LEFT_X + 3};
        int[] ys = {FAR_Y + 2, FAR_Y + 2, NEAR_Y - 2, NEAR_Y - 2};
        g2.setClip(new Polygon(xs, ys, 4));

        Composite prev2 = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
        g2.setStroke(new BasicStroke(1.4f));

        int[] grainColors = {
            0x7A4515, 0x8B5218, 0x955F22, 0xA0692A, 0x7C4C1C,
            0x886025, 0x9A6128, 0xA8702E, 0x7B4A18, 0x8D5620
        };
        for (int i = 0; i < grainColors.length; i++) {
            g2.setColor(new Color(grainColors[i]));
            // Converging lines from near edge to far edge (following perspective)
            float t = i / (float)(grainColors.length - 1);
            int nearX = (int)(NEAR_LEFT_X + t * (NEAR_RIGHT_X - NEAR_LEFT_X));
            int farX  = (int)(FAR_LEFT_X  + t * (FAR_RIGHT_X  - FAR_LEFT_X));
            g2.drawLine(nearX, NEAR_Y, farX, FAR_Y);
        }
        // A few cross-grain arcs for realism
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
        for (int arc = 0; arc < 5; arc++) {
            int ay = FAR_Y + 40 + arc * 58;
            int halfW = (int)(60 + arc * 45);
            g2.setColor(new Color(0x8A5520));
            g2.drawArc(400 - halfW, ay - 10, halfW * 2, 20, 0, 180);
        }

        g2.setClip(prev);
        g2.setComposite(prev2);
        g2.setStroke(new BasicStroke(1f));
    }

    /** Thick near-edge "lip" of the table — drawn as a foreground element. */
    public static void drawTableNearEdge(Graphics2D g2) {
        // Top face of the near slab
        g2.setColor(new Color(118, 72, 28));
        g2.fillRect(NEAR_LEFT_X, NEAR_Y, NEAR_RIGHT_X - NEAR_LEFT_X, 22);
        // Front face (beveled)
        g2.setColor(new Color(88, 52, 18));
        g2.fillRect(NEAR_LEFT_X, NEAR_Y + 22, NEAR_RIGHT_X - NEAR_LEFT_X, 28);
        // Highlight line on top edge
        g2.setColor(new Color(175, 128, 68));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(NEAR_LEFT_X, NEAR_Y, NEAR_RIGHT_X, NEAR_Y);
        g2.setStroke(new BasicStroke(1f));
    }

    // ── seat geometry ───────────────────────────────────────────────────────

    /** Returns [centerX, baselineY] for seat index 1-7. */
    public static int[] getSeatPosition(int seatIndex) {
        return SEATS[seatIndex];
    }

    /**
     * Scale: near characters are larger.
     * Seat 3,4 (near sides) → scale 3; everything else → scale 2.
     */
    public static int getSeatScale(int seatIndex) {
        return (seatIndex == 3 || seatIndex == 4) ? 3 : 2;
    }
}
