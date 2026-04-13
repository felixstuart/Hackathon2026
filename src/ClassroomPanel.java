import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

/**
 * Main game panel — first-person classroom scene at 20 fps.
 *
 * Rendering pipeline (critical for "sitting around" illusion):
 *   1. Background
 *   2. FAR characters (teacher + 2 across) drawn BEFORE table
 *      → table surface paints over their legs so they appear seated behind it
 *   3. Table surface + grain
 *   4. SIDE characters (4 around the edges) drawn AFTER table
 *      → they appear outside/around the table
 *   5. Near-edge slab (foreground)
 *
 * Seat indices: 1-6 = students, 7 = teacher.
 */
public class ClassroomPanel extends JPanel {

    private static final int TICK_MS = 50;  // 20 fps

    private final VisualCharacter[] students = new VisualCharacter[6];
    private final VisualCharacter   teacher;
    private final Timer             gameTimer;

    public ClassroomPanel() {
        setPreferredSize(new Dimension(800, 700));
        setBackground(Color.BLACK);

        for (int i = 0; i < 6; i++) {
            students[i] = new VisualCharacter(false, "Student " + (i + 1), i);
        }
        teacher = new VisualCharacter(true, "Teacher", 99L);
        gameTimer = new Timer(TICK_MS, e -> onTick());
    }

    // ── public API ──────────────────────────────────────────────────────────

    public void startAnimation() { gameTimer.start(); }
    public void stopAnimation()  { gameTimer.stop();  }

    /** seatIndex 1-6 = students, 7 = teacher */
    public void setSpeaking(int seatIndex, boolean speaking) {
        VisualCharacter vc = getCharacterAt(seatIndex);
        if (vc != null) vc.setMouthState(speaking ? MouthState.OPEN : MouthState.CLOSED);
    }

    public void setGesture(int seatIndex, GestureState state) {
        VisualCharacter vc = getCharacterAt(seatIndex);
        if (vc != null) vc.setGestureState(state);
    }

    public VisualCharacter getCharacterAt(int seatIndex) {
        if (seatIndex >= 1 && seatIndex <= 6) return students[seatIndex - 1];
        if (seatIndex == 7)                   return teacher;
        return null;
    }

    // ── rendering ───────────────────────────────────────────────────────────

    private void onTick() {
        for (VisualCharacter vc : students) vc.tick();
        teacher.tick();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Crisp pixel art — no smoothing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // ── 1. Background ────────────────────────────────────────────────
        SceneRenderer.drawBackground(g2, getWidth(), getHeight());

        // ── 2. FAR characters — drawn BEFORE table ───────────────────────
        //    Table surface will cover their legs, making them look seated.
        drawGroup(g2, SceneRenderer.FAR_SEATS);

        // ── 3. Table ─────────────────────────────────────────────────────
        SceneRenderer.drawTableShadow(g2);
        SceneRenderer.drawTable(g2);
        SceneRenderer.drawTableGrain(g2);

        // ── 4. SIDE characters — drawn AFTER table, outside its edges ────
        drawGroup(g2, SceneRenderer.SIDE_SEATS);

        // ── 5. Near-edge foreground slab ─────────────────────────────────
        SceneRenderer.drawTableNearEdge(g2);

        g2.dispose();
    }

    /** Draw a group of seats, sorted by ascending y (far → near within the group). */
    private void drawGroup(Graphics2D g2, int[] seats) {
        Integer[] sorted = new Integer[seats.length];
        for (int i = 0; i < seats.length; i++) sorted[i] = seats[i];
        Arrays.sort(sorted, (a, b) ->
            Integer.compare(SceneRenderer.getSeatPosition(a)[1],
                            SceneRenderer.getSeatPosition(b)[1]));
        for (int seat : sorted) {
            VisualCharacter vc    = getCharacterAt(seat);
            int[]           pos   = SceneRenderer.getSeatPosition(seat);
            int             scale = SceneRenderer.getSeatScale(seat);
            CharacterRenderer.drawCharacter(g2, vc, pos[0], pos[1], scale);
        }
    }
}
