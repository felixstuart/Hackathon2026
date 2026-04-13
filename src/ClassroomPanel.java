import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

/**
 * Main game panel. Renders the first-person classroom scene at 20 fps.
 *
 * Seat indices: 1-6 = students, 7 = teacher.
 *
 * Public API for game logic:
 *   setSpeaking(seatIndex, boolean)
 *   setGesture(seatIndex, GestureState)
 *   getCharacterAt(seatIndex)
 */
public class ClassroomPanel extends JPanel {

    private static final int TICK_MS   = 50;   // 20 fps
    private static final int NUM_SEATS = 7;    // 6 students + teacher

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

        // Crisp pixel art — no smoothing anywhere
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        SceneRenderer.drawBackground(g2, getWidth(), getHeight());
        SceneRenderer.drawTableShadow(g2);
        SceneRenderer.drawTable(g2);
        SceneRenderer.drawTableGrain(g2);

        // Draw characters back → front
        for (int seat : depthOrder()) {
            VisualCharacter vc    = getCharacterAt(seat);
            int[]           pos   = SceneRenderer.getSeatPosition(seat);
            int             scale = SceneRenderer.getSeatScale(seat);
            CharacterRenderer.drawCharacter(g2, vc, pos[0], pos[1], scale);
        }

        // Near table edge drawn last — foreground, covers lower bodies
        SceneRenderer.drawTableNearEdge(g2);

        g2.dispose();
    }

    /** Seats 1-7 sorted by y ascending (back-to-front / far-to-near). */
    private int[] depthOrder() {
        Integer[] seats = new Integer[NUM_SEATS];
        for (int i = 0; i < NUM_SEATS; i++) seats[i] = i + 1;
        Arrays.sort(seats, (a, b) ->
            Integer.compare(SceneRenderer.getSeatPosition(a)[1],
                            SceneRenderer.getSeatPosition(b)[1]));
        int[] result = new int[NUM_SEATS];
        for (int i = 0; i < NUM_SEATS; i++) result[i] = seats[i];
        return result;
    }
}
