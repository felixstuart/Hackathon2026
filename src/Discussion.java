import java.util.ArrayList;
import java.util.Random;

public class Discussion {
    public Student[] students;
    public String book;
    public Student teacher;
    public int level;

    public String prompt;

    public Student next;
    public int[] order = new int[6];
    public int orderIndex = 0;

    public ArrayList<String> messages = new ArrayList<String>();

    private static final Random random = new Random();

    public Discussion(String book, int level) {
        this.book = book;
        this.level = level;

        students = new Student[6];

        for (int i = 0; i < students.length; i++) {
            int ability = gaussianAbility(level);
            students[i] = new Student(ability);
        }

        for (int i = 0; i < order.length; i++) order[i] = i;
        for (int i = order.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = order[i]; order[i] = order[j]; order[j] = tmp;
        }
        next = students[order[0]];

        prompt = String.format(
                "You are a teacher facilitating a Harkness discussion.\n\n" +
                        "Context:\n" +
                        "Book: %s\n" +
                        "Discussion level: %d/5\n\n" +

                        "Your role is to guide—not dominate—the conversation. " +
                        "You should help students deepen their thinking, make connections, and stay grounded in the text.\n\n" +

                        "Start by selecting a specific, meaningful passage, moment, or theme from the text. " +
                        "Briefly introduce it or reference it, then pose an open-ended question to begin discussion.\n\n" +

                        "During the discussion:\n" +
                        "- Ask probing, open-ended questions rather than giving answers\n" +
                        "- Encourage quieter students to participate\n" +
                        "- Redirect if the conversation goes off track\n" +
                        "- Push for textual evidence and deeper analysis\n" +
                        "- Occasionally synthesize or highlight strong ideas\n\n" +

                        "Adjust your facilitation style based on discussion level:\n" +
                        "- Lower levels: provide more structure, clearer guidance, and more frequent intervention\n" +
                        "- Higher levels: step back more, let students lead, and only guide when necessary\n\n" +

                        "Keep your responses concise (1–3 sentences usually), unless guiding or refocusing the discussion requires more.\n" +
                        "Do NOT sound like an AI or lecturer—sound like a thoughtful, engaged teacher.\n\n" +

                        "Your first response should:\n" +
                        "1. Introduce a specific moment, passage, or idea from the book\n" +
                        "2. Ask an open-ended question that invites multiple interpretations\n" +
                        "3. Encourage students to reference the text in their responses",

                book,
                level
        );
    }

    public void turn() {
        messages.add(students[order[orderIndex]].getMessage(prompt, messages));
    }

    private int gaussianAbility(int center) {
        double stdDev = 2.0;
        int raw = (int) Math.round(center + random.nextGaussian() * stdDev);
        return Math.max(1, Math.min(5, raw));
    }
}
