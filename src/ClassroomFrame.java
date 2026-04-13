import java.awt.*;
import javax.swing.*;

public class ClassroomFrame extends JFrame {

    private static final String CARD_SELECT    = "select";
    private static final String CARD_CLASSROOM = "classroom";
    private static final String CARD_GAMEOVER  = "gameover";

    private final CardLayout     cards          = new CardLayout();
    private final JPanel         root           = new JPanel(cards);
    private final ClassroomPanel classroomPanel = new ClassroomPanel();
    private final ChatPanel      chatPanel      = new ChatPanel();
    private final GameOverPanel  gameOverPanel;

    private String     selectedBook = "";
    public  Discussion discussion;
    private int        currentLevel = 1;

    // Live state shared with enterClassroom lambdas
    private Timer    autoAdvanceTimer;
    private boolean  aiInProgress = false;

    public ClassroomFrame() {
        setTitle("Harkness Discussion");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        gameOverPanel = new GameOverPanel(this::nextLevel, this::playAgain);

        BookSelectPanel bookSelect = new BookSelectPanel(this::enterClassroom);

        JPanel classroomWrapper = new JPanel(new BorderLayout());
        classroomWrapper.add(classroomPanel, BorderLayout.CENTER);
        classroomWrapper.add(chatPanel,      BorderLayout.EAST);

        root.add(bookSelect,       CARD_SELECT);
        root.add(classroomWrapper, CARD_CLASSROOM);
        root.add(gameOverPanel,    CARD_GAMEOVER);

        add(root);
        pack();
        setLocationRelativeTo(null);

        cards.show(root, CARD_SELECT);
        SwingUtilities.invokeLater(bookSelect::requestFocusInWindow);
        setVisible(true);
    }

    // ── navigation ────────────────────────────────────────────────────────────

    private void enterClassroom(String book) {
        this.selectedBook = book;
        discussion = new Discussion(book, currentLevel);
        aiInProgress = false;

        // Auto-advance timer
        if (autoAdvanceTimer != null) autoAdvanceTimer.stop();
        autoAdvanceTimer = new Timer(8000, null);
        autoAdvanceTimer.addActionListener(e -> triggerStudentTurn());

        // Teacher opens the discussion, then auto-advance starts
        SwingWorker<String, Void> teacherOpener = new SwingWorker<>() {
            protected String doInBackground() { return discussion.getTeacherMessage(); }
            protected void done() {
                try {
                    String msg = get();
                    classroomPanel.setSpeaking(7, true);
                    classroomPanel.setGesture(7, GestureState.HAND_RAISE);
                    Student.runAfterSpeech(() -> {
                        chatPanel.addMessage("Teacher", msg);
                        classroomPanel.setSpeaking(7, false);
                    });
                    discussion.startPrefetch();  // pre-generate first student turn while teacher speaks
                } catch (Exception e) { e.printStackTrace(); }
                autoAdvanceTimer.start();
            }
        };
        teacherOpener.execute();

        chatPanel.setOnSubmit(this::handlePlayerInput);
        chatPanel.setOnTyping(() -> autoAdvanceTimer.restart());

        classroomPanel.startAnimation();
        cards.show(root, CARD_CLASSROOM);
        SwingUtilities.invokeLater(chatPanel::requestFocusOnInput);
    }

    private void triggerStudentTurn() {
        if (aiInProgress) return;
        if (discussion.isOver()) { endGame(); return; }
        aiInProgress = true;
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            protected String doInBackground() { return discussion.takeTurn(); }
            protected void done() {
                try {
                    int seat = discussion.getLastSpeakerSeat();
                    String msg = get();
                    String name = discussion.getSpeakerName(seat);
                    classroomPanel.setSpeaking(seat, true);
                    classroomPanel.setGesture(seat, GestureState.HAND_RAISE);
                    Student.runAfterSpeech(() -> {
                        chatPanel.addMessage(name, msg);
                        classroomPanel.setSpeaking(seat, false);
                    });
                } catch (Exception ex) { ex.printStackTrace(); }
                finally {
                    aiInProgress = false;
                    if (discussion.isOver()) endGame();
                }
            }
        };
        worker.execute();
    }

    private void handlePlayerInput(String playerText) {
        if (aiInProgress) return;
        aiInProgress = true;
        Student.stopSpeech();
        discussion.messages.add("> You: " + playerText);
        autoAdvanceTimer.restart();
        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            protected String[] doInBackground() {
                discussion.invalidatePrefetch();  // player message changed context; discard stale prefetch
                String redirect = discussion.checkAndRedirect(playerText);
                if (redirect != null) return new String[]{"Teacher", redirect};
                String msg = discussion.takeTurn();
                int seat = discussion.getLastSpeakerSeat();
                return new String[]{discussion.getSpeakerName(seat), msg};
            }
            protected void done() {
                try {
                    String[] result = get();
                    String speaker = result[0];
                    String msg = result[1];
                    if (speaker.equals("Teacher")) {
                        classroomPanel.setSpeaking(7, true);
                        classroomPanel.setGesture(7, GestureState.HAND_RAISE);
                        Student.runAfterSpeech(() -> {
                            chatPanel.addMessage(speaker, msg);
                            classroomPanel.setSpeaking(7, false);
                        });
                    } else {
                        int seat = discussion.getLastSpeakerSeat();
                        classroomPanel.setSpeaking(seat, true);
                        classroomPanel.setGesture(seat, GestureState.HAND_RAISE);
                        Student.runAfterSpeech(() -> {
                            chatPanel.addMessage(speaker, msg);
                            classroomPanel.setSpeaking(seat, false);
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
                finally {
                    aiInProgress = false;
                    if (discussion.isOver()) endGame();
                }
            }
        };
        worker.execute();
    }

    private void endGame() {
        if (autoAdvanceTimer != null) autoAdvanceTimer.stop();
        classroomPanel.stopAnimation();

        gameOverPanel.showLoading();
        cards.show(root, CARD_GAMEOVER);

        SwingWorker<String[], Void> scorer = new SwingWorker<>() {
            protected String[] doInBackground() { return discussion.evaluatePlayer(); }
            protected void done() {
                try {
                    String[] result = get();
                    int score = Integer.parseInt(result[0]);
                    gameOverPanel.setResults(score, result[1], currentLevel);
                } catch (Exception e) {
                    gameOverPanel.setResults(0, "Could not score discussion.", currentLevel);
                }
            }
        };
        scorer.execute();
    }

    private void nextLevel() {
        currentLevel++;
        chatPanel.clear();
        enterClassroom(selectedBook);
    }

    private void playAgain() {
        currentLevel = 1;
        chatPanel.clear();
        enterClassroom(selectedBook);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static void newTimer(int delayMs, Runnable action) {
        Timer t = new Timer(delayMs, e -> action.run());
        t.setRepeats(false);
        t.start();
    }

    public String getSelectedBook()         { return selectedBook; }
    public ClassroomPanel getClassroomPanel() { return classroomPanel; }
    public ChatPanel getChatPanel()         { return chatPanel; }
}
