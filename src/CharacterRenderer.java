import java.awt.*;

/**
 * Draws pixel-art character sprites via Java2D fillRect calls.
 *
 * Sprite grid: 16 px wide × 32 px tall.
 * Every "pixel" is rendered as scale×scale screen pixels via fillPixel().
 *
 * Teacher is visually distinct: always wears a blazer and has glasses.
 */
public class CharacterRenderer {

    // ── public API ───────────────────────────────────────────────────────────

    public static void drawCharacter(Graphics2D g2, VisualCharacter vc,
                                     int centerX, int baselineY, int scale) {
        int ox = centerX - 8 * scale;
        int oy = baselineY - 32 * scale;

        int bodyDy = (vc.getGestureState() == GestureState.LEANING_FORWARD)
                ? (int)(1 + Math.sin(vc.getAnimationTick() * 0.08) * 0.5)
                : 0;

        drawLegs (g2, vc, ox, oy, scale, bodyDy);
        drawTorso(g2, vc, ox, oy, scale, bodyDy);
        drawArms (g2, vc, ox, oy, scale, bodyDy);
        drawHead (g2, vc, ox, oy, scale, bodyDy);
        drawHair (g2, vc, ox, oy, scale, bodyDy);
        drawMouth(g2, vc, ox, oy, scale, bodyDy);
        if (vc.isTeacher) drawGlasses(g2, vc, ox, oy, scale, bodyDy);
    }

    // ── sprite parts ─────────────────────────────────────────────────────────

    private static void drawHair(Graphics2D g2, VisualCharacter vc,
                                  int ox, int oy, int scale, int bodyDy) {
        Color hc = vc.hairColor.toColor();
        switch (vc.hairStyle) {
            case SHORT:
                fillBlock(g2, 0, 2, 5, 10, ox, oy, scale, bodyDy, hc);
                break;
            case MEDIUM:
                fillBlock(g2, 0, 2, 5, 10, ox, oy, scale, bodyDy, hc);
                fillBlock(g2, 2, 5, 4,  5, ox, oy, scale, bodyDy, hc);
                fillBlock(g2, 2, 5, 11, 12, ox, oy, scale, bodyDy, hc);
                break;
            case LONG:
                fillBlock(g2, 0, 2, 5, 10, ox, oy, scale, bodyDy, hc);
                fillBlock(g2, 2, 8, 3,  5, ox, oy, scale, bodyDy, hc);
                fillBlock(g2, 2, 8, 11, 13, ox, oy, scale, bodyDy, hc);
                break;
            case CURLY:
                fillBlock(g2, 0, 3, 3, 12, ox, oy, scale, bodyDy, hc);
                break;
            case BRAIDED:
                fillBlock(g2, 0, 2, 5, 10, ox, oy, scale, bodyDy, hc);
                for (int row = 2; row < 10; row += 2)
                    fillPixel(g2, 8, row, ox, oy, scale, bodyDy, hc);
                break;
            case BALD:
                break;
        }
    }

    private static void drawHead(Graphics2D g2, VisualCharacter vc,
                                  int ox, int oy, int scale, int bodyDy) {
        Color skin   = vc.skinTone.baseColor();
        Color skinHi = vc.skinTone.highlightColor();
        Color eyeCol = new Color(35, 25, 15);

        // Head block rows 2-7, cols 4-11
        fillBlock(g2, 2, 8, 4, 11, ox, oy, scale, bodyDy, skin);
        // Forehead highlight
        fillPixel(g2, 7, 2, ox, oy, scale, bodyDy, skinHi);
        fillPixel(g2, 8, 2, ox, oy, scale, bodyDy, skinHi);
        // Eyes row 3
        fillPixel(g2, 5,  3, ox, oy, scale, bodyDy, eyeCol);
        fillPixel(g2, 6,  3, ox, oy, scale, bodyDy, eyeCol);
        fillPixel(g2, 9,  3, ox, oy, scale, bodyDy, eyeCol);
        fillPixel(g2, 10, 3, ox, oy, scale, bodyDy, eyeCol);
        // Neck rows 7-8
        fillBlock(g2, 7, 9, 6, 9, ox, oy, scale, bodyDy, skin);
    }

    private static void drawGlasses(Graphics2D g2, VisualCharacter vc,
                                     int ox, int oy, int scale, int bodyDy) {
        Color frame = new Color(50, 38, 25);
        // Left lens outline (rows 3-4, cols 4-7)
        fillPixel(g2, 4, 2, ox, oy, scale, bodyDy, frame);
        fillPixel(g2, 5, 2, ox, oy, scale, bodyDy, frame);
        fillPixel(g2, 6, 2, ox, oy, scale, bodyDy, frame);
        fillPixel(g2, 7, 2, ox, oy, scale, bodyDy, frame);
        fillPixel(g2, 4, 4, ox, oy, scale, bodyDy, frame);
        fillPixel(g2, 7, 4, ox, oy, scale, bodyDy, frame);
        // Right lens outline (rows 3-4, cols 8-11)
        fillPixel(g2, 8,  2, ox, oy, scale, bodyDy, frame);
        fillPixel(g2, 9,  2, ox, oy, scale, bodyDy, frame);
        fillPixel(g2, 10, 2, ox, oy, scale, bodyDy, frame);
        fillPixel(g2, 11, 2, ox, oy, scale, bodyDy, frame);
        fillPixel(g2, 8,  4, ox, oy, scale, bodyDy, frame);
        fillPixel(g2, 11, 4, ox, oy, scale, bodyDy, frame);
        // Bridge connecting lenses
        fillPixel(g2, 7, 3, ox, oy, scale, bodyDy, frame);
        fillPixel(g2, 8, 3, ox, oy, scale, bodyDy, frame);
        // Lens tint (semi-transparent blue — just a lighter color overlay)
        Color tint = new Color(160, 195, 215, 100);
        fillPixel(g2, 5, 3, ox, oy, scale, bodyDy, tint);
        fillPixel(g2, 6, 3, ox, oy, scale, bodyDy, tint);
        fillPixel(g2, 9,  3, ox, oy, scale, bodyDy, tint);
        fillPixel(g2, 10, 3, ox, oy, scale, bodyDy, tint);
    }

    private static void drawMouth(Graphics2D g2, VisualCharacter vc,
                                   int ox, int oy, int scale, int bodyDy) {
        if (vc.getMouthState() == MouthState.CLOSED) {
            fillBlock(g2, 5, 7, 6, 9, ox, oy, scale, bodyDy, vc.skinTone.shadowColor());
        } else {
            int mouthH = 2 + (vc.getAnimationTick() / 4) % 2;
            for (int row = 5; row < 5 + mouthH; row++) {
                Color c = (row == 5 + mouthH - 1) ? new Color(185, 65, 65)
                                                   : new Color(20, 10, 10);
                fillBlock(g2, row, row + 1, 6, 9, ox, oy, scale, bodyDy, c);
            }
        }
    }

    private static void drawTorso(Graphics2D g2, VisualCharacter vc,
                                   int ox, int oy, int scale, int bodyDy) {
        // Teachers always show as blazer regardless of random style
        ClothingStyle style = vc.isTeacher ? ClothingStyle.BLAZER : vc.clothingStyle;
        Color main = vc.clothingColor.toColor();
        Color dark = vc.clothingColor.darkerShade();

        fillBlock(g2, 8, 19, 3, 12, ox, oy, scale, bodyDy, main);

        switch (style) {
            case COLLARED_SHIRT:
                fillBlock(g2, 8, 10, 6, 9, ox, oy, scale, bodyDy, new Color(240, 240, 240));
                break;
            case HOODIE:
                fillBlock(g2, 14, 17, 5, 10, ox, oy, scale, bodyDy, dark);
                break;
            case BLAZER:
                // Lapels
                for (int row = 8; row < 13; row++) {
                    fillPixel(g2, 5,  row, ox, oy, scale, bodyDy, new Color(28, 28, 28));
                    fillPixel(g2, 6,  row, ox, oy, scale, bodyDy, new Color(28, 28, 28));
                    fillPixel(g2, 9,  row, ox, oy, scale, bodyDy, new Color(28, 28, 28));
                    fillPixel(g2, 10, row, ox, oy, scale, bodyDy, new Color(28, 28, 28));
                }
                // White shirt beneath
                fillBlock(g2, 8, 13, 7, 8, ox, oy, scale, bodyDy, new Color(235, 235, 235));
                break;
            case SWEATER:
                fillBlock(g2, 17, 19, 3, 12, ox, oy, scale, bodyDy, dark);
                break;
            case T_SHIRT:
                break;
        }
    }

    private static void drawArms(Graphics2D g2, VisualCharacter vc,
                                  int ox, int oy, int scale, int bodyDy) {
        Color skin   = vc.skinTone.baseColor();
        Color sleeve = vc.clothingColor.toColor();
        GestureState gs   = vc.getGestureState();
        int          tick = vc.getAnimationTick();

        // Upper arms (sleeve-colored) rows 8-13
        fillBlock(g2, 8, 14, 0,  3,  ox, oy, scale, bodyDy, sleeve);
        fillBlock(g2, 8, 14, 13, 16, ox, oy, scale, bodyDy, sleeve);

        // Forearms
        int[] lOff = leftArmOffset(gs, tick);
        int[] rOff = rightArmOffset(gs, tick);
        for (int row = 14; row < 19; row++) {
            fillBlock(g2, row + lOff[1], row + lOff[1] + 1, lOff[0],       lOff[0] + 3,
                      ox, oy, scale, bodyDy, skin);
            fillBlock(g2, row + rOff[1], row + rOff[1] + 1, 13 + rOff[0],  16 + rOff[0],
                      ox, oy, scale, bodyDy, skin);
        }
    }

    private static void drawLegs(Graphics2D g2, VisualCharacter vc,
                                  int ox, int oy, int scale, int bodyDy) {
        Color pants = vc.clothingColor.darkerShade();
        Color foot  = new Color(48, 38, 28);

        fillBlock(g2, 19, 30, 4, 11, ox, oy, scale, 0, pants);
        fillBlock(g2, 30, 32, 3, 6,  ox, oy, scale, 0, foot);
        fillBlock(g2, 30, 32, 10, 13, ox, oy, scale, 0, foot);
    }

    // ── arm offsets ──────────────────────────────────────────────────────────

    private static int[] leftArmOffset(GestureState gs, int tick) {
        int osc = (int)(Math.sin(tick * 0.15) * 1);
        switch (gs) {
            case HAND_RAISE:      return new int[]{0,  -6 + osc};
            case WAVE:            return new int[]{1,  -4 + osc};
            case POINT:           return new int[]{2,  -2};
            case LEANING_FORWARD: return new int[]{0,   4};
            default:              return new int[]{0,   0};
        }
    }

    private static int[] rightArmOffset(GestureState gs, int tick) {
        int osc = (int)(Math.sin(tick * 0.15) * 1);
        switch (gs) {
            case HAND_RAISE:      return new int[]{0,  -6 + osc};
            case WAVE:            return new int[]{-1, -4 + osc};
            case POINT:           return new int[]{-2, -2};
            case LEANING_FORWARD: return new int[]{0,   4};
            default:              return new int[]{0,   0};
        }
    }

    // ── drawing primitives ───────────────────────────────────────────────────

    /** Fill a rectangular block of sprite pixels (rows and cols are exclusive-end). */
    private static void fillBlock(Graphics2D g2,
                                   int rowStart, int rowEnd,
                                   int colStart, int colEnd,
                                   int ox, int oy, int scale, int bodyDy, Color color) {
        g2.setColor(color);
        g2.fillRect(
            ox + colStart * scale,
            oy + (rowStart + bodyDy) * scale,
            (colEnd - colStart) * scale,
            (rowEnd - rowStart) * scale
        );
    }

    /** Fill a single sprite pixel (1×1 in sprite space = scale×scale on screen). */
    private static void fillPixel(Graphics2D g2,
                                   int sx, int sy,
                                   int ox, int oy, int scale, int bodyDy, Color color) {
        g2.setColor(color);
        g2.fillRect(ox + sx * scale, oy + (sy + bodyDy) * scale, scale, scale);
    }
}
