import java.awt.*;
import javax.swing.*;

/**
 * Top-level game window.
 *
 * Flow:
 *   1. Opens on the BookSelectPanel (pixel-art book chooser).
 *   2. When the player confirms a book, swaps to ClassroomPanel via CardLayout.
 *
 * After entering the classroom, call getClassroomPanel() to wire game logic.
 */
public class ClassroomFrame extends JFrame {

    private static final String CARD_SELECT   = "select";
    private static final String CARD_CLASSROOM = "classroom";

    private final CardLayout     cards          = new CardLayout();
    private final JPanel         root           = new JPanel(cards);
    private final ClassroomPanel classroomPanel = new ClassroomPanel();
    private       String         selectedBook   = "";

    public ClassroomFrame() {
        setTitle("Harkness Discussion");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        BookSelectPanel bookSelect = new BookSelectPanel(this::enterClassroom);

        root.add(bookSelect,     CARD_SELECT);
        root.add(classroomPanel, CARD_CLASSROOM);

        add(root);
        pack();
        setLocationRelativeTo(null);

        cards.show(root, CARD_SELECT);
        // Give focus to the book-select panel so keyboard works immediately
        SwingUtilities.invokeLater(bookSelect::requestFocusInWindow);
        setVisible(true);
    }

    /** Called by BookSelectPanel when the player picks a book. */
    private void enterClassroom(String book) {
        this.selectedBook = book;
        classroomPanel.startAnimation();
        cards.show(root, CARD_CLASSROOM);
        classroomPanel.requestFocusInWindow();
    }

    /** The selected book title (empty string before selection). */
    public String getSelectedBook()       { return selectedBook; }

    /** Direct access for wiring AI / discussion logic. */
    public ClassroomPanel getClassroomPanel() { return classroomPanel; }
}
