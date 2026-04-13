import java.awt.*;
import java.awt.geom.*;

/**
 * Draws the first-person classroom scene.
 *
 * Table is a perspective trapezoid (wider near viewer, narrower far away).
 * Characters across the table are drawn BEFORE the table surface so the
 * table edge naturally hides their lower bodies.  Side characters are
 * drawn AFTER the table surface so they sit outside/around it.
 *
 * Seat layout (7 seats):
 *
 *      [1]  [Teacher/7]  [2]       ← far side (behind table)
 *   [3]                     [4]    ← upper sides (outside table)
 *  [5]                        [6]  ← lower sides (outside table)
 *             [PLAYER]             ← near center (off-screen)
 *
 * FAR seats  (drawn before table): 1, 2, 7
 * SIDE seats (drawn after  table): 3, 4, 5, 6
 */
public class SceneRenderer {

    // ── table trapezoid corners ──────────────────────────────────────────────
    static final int FAR_LEFT_X  = 242;
    static final int FAR_RIGHT_X = 558;
    static final int FAR_Y       = 328;

    static final int NEAR_LEFT_X  = 128;
    static final int NEAR_RIGHT_X = 672;
    static final int NEAR_Y       = 680;   // mostly off-screen for immersion

    // ── seat positions [centerX, baselineY, scale] ───────────────────────────
    // Far seats: baseline set below FAR_Y so the table covers their legs.
    // Side seats: baseline at natural seated height, positioned outside the table.
    private static final int[][] SEATS = {
        {},                        // 0 = player (unused)
        {265,  358, 2},            // 1  far-left student
        {535,  358, 2},            // 2  far-right student
        {172,  425, 2},            // 3  upper-left side
        {628,  425, 2},            // 4  upper-right side
        {118,  552, 3},            // 5  lower-left side  (nearest to player)
        {682,  552, 3},            // 6  lower-right side
        {400,  350, 2},            // 7  teacher (far center)
    };

    /** Returns [centerX, baselineY] */
    public static int[] getSeatPosition(int i) {
        return new int[]{SEATS[i][0], SEATS[i][1]};
    }

    /** Returns sprite scale for the seat. */
    public static int getSeatScale(int i) {
        return SEATS[i][2];
    }

    /** Seats that must be rendered BEFORE the table surface (appear behind it). */
    public static final int[] FAR_SEATS  = {7, 1, 2};
    /** Seats rendered AFTER the table surface (appear around the sides). */
    public static final int[] SIDE_SEATS = {3, 4, 5, 6};

    // ── public draw API ──────────────────────────────────────────────────────

    public static void drawBackground(Graphics2D g2, int w, int h) {
        // Ceiling
        g2.setColor(new Color(55, 50, 45));
        g2.fillRect(0, 0, w, 82);

        // Ceiling lights
        g2.setColor(new Color(245, 245, 210));
        g2.fillRect(115, 14, 145, 10);
        g2.fillRect(540, 14, 145, 10);
        g2.setColor(new Color(255, 255, 230, 75));
        g2.fillRect(115, 10, 145, 18);
        g2.fillRect(540, 10, 145, 18);

        // Back wall
        g2.setColor(new Color(182, 167, 144));
        g2.fillRect(0, 82, w, 252);

        // Chalkboard
        g2.setColor(new Color(40, 68, 45));
        g2.fillRect(158, 92, 484, 168);
        g2.setColor(new Color(32, 58, 38));
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(158, 92, 484, 168);

        // Chalk marks
        g2.setColor(new Color(205, 205, 190, 165));
        g2.setStroke(new BasicStroke(2f));
        int[][] lines = {{212,128,374,128},{212,152,418,152},{212,176,348,176},{212,200,392,200},
                         {428,128,588,128},{428,155,572,155}};
        for (int[] ln : lines) g2.drawLine(ln[0], ln[1], ln[2], ln[3]);

        // Chalk tray
        g2.setColor(new Color(88, 72, 55));
        g2.setStroke(new BasicStroke(1f));
        g2.fillRect(158, 258, 484, 6);
        g2.setColor(new Color(228, 222, 206, 190));
        g2.fillRect(175, 260, 18, 2);
        g2.fillRect(200, 260, 18, 2);
        g2.fillRect(228, 260, 10, 2);

        // Side walls
        g2.setColor(new Color(165, 150, 128));
        g2.fillRect(0, 82, 58, h);
        g2.fillRect(w - 58, 82, 58, h);

        // Windows
        drawWindow(g2, 6,  130, 46, 108);
        drawWindow(g2, 6,  262, 46, 108);
        drawWindow(g2, w - 52, 130, 46, 108);
        drawWindow(g2, w - 52, 262, 46, 108);

        // Floor
        g2.setColor(new Color(145, 118, 85));
        g2.fillRect(0, 334, w, h - 334);

        // Receding floor planks
        g2.setStroke(new BasicStroke(1f));
        int[] plankY = {368, 408, 453, 503, 560, 622, 692};
        for (int py : plankY) {
            g2.setColor(new Color(125, 100, 68));
            g2.drawLine(0, py, w, py);
        }
        for (int row = 0; row < plankY.length; row++) {
            int y0 = row == 0 ? 334 : plankY[row - 1];
            int y1 = plankY[row];
            int joints = 3 + row;
            for (int j = 1; j <= joints; j++) {
                int jx = w * j / (joints + 1);
                g2.setColor(new Color(115, 92, 60));
                g2.drawLine(jx, y0, jx, y1);
            }
        }

        // Vignette
        GradientPaint vig = new GradientPaint(0, h - 130f, new Color(0,0,0,0), 0, h, new Color(0,0,0,100));
        g2.setPaint(vig);
        g2.fillRect(0, h - 130, w, 130);
        g2.setStroke(new BasicStroke(1f));
    }

    private static void drawWindow(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(155, 196, 228));
        g2.fillRect(x, y, w, h);
        g2.setColor(new Color(105, 85, 65));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(x, y, w, h);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x + w / 2, y, x + w / 2, y + h);
        g2.drawLine(x, y + h / 2, x + w, y + h / 2);
        g2.setStroke(new BasicStroke(1f));
    }

    public static void drawTableShadow(Graphics2D g2) {
        int[] xs = {FAR_LEFT_X - 10, FAR_RIGHT_X + 10, NEAR_RIGHT_X + 10, NEAR_LEFT_X - 10};
        int[] ys = {FAR_Y + 16, FAR_Y + 16, NEAR_Y + 16, NEAR_Y + 16};
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillPolygon(xs, ys, 4);
    }

    public static void drawTable(Graphics2D g2) {
        // Surface fill
        int[] xs = {FAR_LEFT_X, FAR_RIGHT_X, NEAR_RIGHT_X, NEAR_LEFT_X};
        int[] ys = {FAR_Y, FAR_Y, NEAR_Y, NEAR_Y};
        g2.setColor(new Color(132, 82, 35));
        g2.fillPolygon(xs, ys, 4);


        // Side edges
        g2.setColor(new Color(90, 50, 14));
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(FAR_LEFT_X,  FAR_Y,  NEAR_LEFT_X,  NEAR_Y);
        g2.drawLine(FAR_RIGHT_X, FAR_Y,  NEAR_RIGHT_X, NEAR_Y);

        g2.setStroke(new BasicStroke(1f));
    }

    public static void drawTableGrain(Graphics2D g2) {
        Shape prevClip = g2.getClip();
        int[] xs = {FAR_LEFT_X + 4, FAR_RIGHT_X - 4, NEAR_RIGHT_X - 4, NEAR_LEFT_X + 4};
        int[] ys = {FAR_Y + 3, FAR_Y + 3, NEAR_Y - 3, NEAR_Y - 3};
        g2.setClip(new Polygon(xs, ys, 4));

        Composite prevComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.28f));
        g2.setStroke(new BasicStroke(1.3f));

        int[] grains = {0x7A4515,0x8B5218,0x945F22,0x9F682A,0x7C4B1C,
                        0x876024,0x996028,0xA7702E,0x7B4918,0x8C5520};
        for (int i = 0; i < grains.length; i++) {
            float t = i / (float)(grains.length - 1);
            int nearX = (int)(NEAR_LEFT_X + t * (NEAR_RIGHT_X - NEAR_LEFT_X));
            int farX  = (int)(FAR_LEFT_X  + t * (FAR_RIGHT_X  - FAR_LEFT_X));
            g2.setColor(new Color(grains[i]));
            g2.drawLine(nearX, NEAR_Y, farX, FAR_Y);
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.13f));
        for (int arc = 0; arc < 5; arc++) {
            int ay = FAR_Y + 38 + arc * 62;
            int hw = 55 + arc * 42;
            g2.setColor(new Color(0x8A5520));
            g2.drawArc(400 - hw, ay - 9, hw * 2, 18, 0, 180);
        }

        g2.setClip(prevClip);
        g2.setComposite(prevComp);
        g2.setStroke(new BasicStroke(1f));
    }

    /**
     * The near edge of the table — a solid slab visible at the very bottom.
     * Drawn last so it sits in the foreground.
     */
    public static void drawTableNearEdge(Graphics2D g2) {
        int edgeY = NEAR_Y - 10;  // a sliver above the off-screen near edge
        g2.setColor(new Color(115, 70, 25));
        g2.fillRect(NEAR_LEFT_X, edgeY, NEAR_RIGHT_X - NEAR_LEFT_X, 22);
        g2.setColor(new Color(82, 48, 14));
        g2.fillRect(NEAR_LEFT_X, edgeY + 22, NEAR_RIGHT_X - NEAR_LEFT_X, 20);
        g2.setColor(new Color(172, 124, 62));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(NEAR_LEFT_X, edgeY, NEAR_RIGHT_X, edgeY);
        g2.setStroke(new BasicStroke(1f));
    }
}
