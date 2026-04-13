import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Discussion {
    public static final int MAX_TURNS = 12;   // 2 full rounds of 6 students

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
    private static final OpenAIClient aiClient = OpenAIOkHttpClient.fromEnv();

    // Pre-generation state — next student's response is fetched while current audio plays
    private static final ExecutorService prefetchExecutor = Executors.newSingleThreadExecutor();
    private volatile Future<String> prefetchFuture       = null;
    private volatile Student        prefetchForStudent   = null;
    private volatile int            prefetchMessageCount = -1;

    private static final String[] NAME_POOL = {
        "Aisha", "Ben", "Chloe", "Dev", "Elena", "Felix",
        "Grace", "Hassan", "Ingrid", "Jasper", "Keiko", "Liam",
        "Maya", "Noah", "Olivia", "Priya", "Quinn", "Ravi",
        "Sofia", "Theo", "Uma", "Victor", "Wren", "Zoe"
    };

    public Discussion(String book, int level) {
        this.book = book;
        this.level = level;

        // Shuffle name pool and deal 6 unique names
        String[] names = NAME_POOL.clone();
        for (int i = names.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String tmp = names[i]; names[i] = names[j]; names[j] = tmp;
        }

        students = new Student[6];
        for (int i = 0; i < students.length; i++) {
            students[i] = new Student(gaussianAbility(level), names[i]);
        }

        for (int i = 0; i < order.length; i++) order[i] = i;
        for (int i = order.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = order[i]; order[i] = order[j]; order[j] = tmp;
        }
        next = students[order[0]];

        prompt = String.format(
                "You are a teacher running a Harkness discussion about \"%s\" with a class at level %d/10.\n\n" +

                        "Harkness is student-led. Your job is to start the conversation and then get out of the way. " +
                        "Students talk to each other — not to you. You are one voice at the table, not the authority at the front.\n\n" +

                        "Your only moves are:\n" +
                        "1. Open with a specific, genuinely debatable question grounded in a real moment or passage from the text. " +
                        "Do not summarize the book. Do not explain the question. Just ask it.\n" +
                        "2. When the discussion stalls (silence, students repeating each other, the thread dying out), " +
                        "ask a short follow-up question — one that sharpens or redirects, not one that validates.\n" +
                        "3. If a student says something vague or unsupported, push back quietly: " +
                        "\"Where in the text are you getting that?\" or \"What do you mean by that exactly?\"\n\n" +

                        "What you must NOT do:\n" +
                        "- Do not summarize what students said\n" +
                        "- Do not praise contributions ('great point', 'exactly', 'I love that')\n" +
                        "- Do not answer your own questions\n" +
                        "- Do not lecture\n" +
                        "- Do not explain themes or meanings to the class\n\n" +

                        "Adjust by level:\n" +
                        "- Level 1–3: The discussion needs more structure. Ask simpler, more text-anchored questions. " +
                        "Intervene sooner when it stalls.\n" +
                        "- Level 4–6: Trust the students more. Let threads develop before stepping in.\n" +
                        "- Level 7–10: Almost never speak. Only enter if the discussion genuinely breaks down or goes badly off-track.\n\n" +

                        "Tone: calm, curious, direct. Sound like a person, not a facilitator. One or two sentences at most.",

                book,
                level
        );

        teacher = new Student(level, "Teacher", "onyx");
    }

    public boolean isOver() {
        return orderIndex >= MAX_TURNS;
    }

    public String takeTurn() {
        Student s = students[order[orderIndex % order.length]];
        orderIndex++;

        // Use pre-generated text if it was made for this student with the same message context
        String msg = null;
        Future<String> pf = prefetchFuture;
        if (pf != null && prefetchForStudent == s && prefetchMessageCount == messages.size()) {
            prefetchFuture = null;
            try { msg = pf.get(); } catch (Exception ignored) {}
        }

        if (msg == null) {
            msg = s.getMessage(prompt, messages);  // generate + enqueue TTS
        } else {
            s.enqueueSpeech(msg);  // text was prefetched; just enqueue TTS
        }

        messages.add(s.name + ": " + msg);
        startPrefetch();  // pre-generate the next student's response in the background
        return msg;
    }

    /**
     * Begins pre-generating the next student's response so it is ready when their turn fires.
     * Safe to call any time; silently does nothing if the discussion is over.
     */
    public void startPrefetch() {
        if (isOver()) return;
        Student next = students[order[orderIndex % order.length]];
        ArrayList<String> snapshot = new ArrayList<>(messages);
        int snapshotSize = messages.size();
        prefetchForStudent   = next;
        prefetchMessageCount = snapshotSize;
        prefetchFuture = prefetchExecutor.submit(() -> next.fetchText(prompt, snapshot));
    }

    /**
     * Cancels any in-flight prefetch. Call when player input changes the message list,
     * so a stale pre-generated response is not used.
     */
    public void invalidatePrefetch() {
        Future<String> pf = prefetchFuture;
        if (pf != null) pf.cancel(true);
        prefetchFuture       = null;
        prefetchForStudent   = null;
        prefetchMessageCount = -1;
    }

    /**
     * Scores the player's contributions (0–100) and returns a two-element array:
     * [0] = score as a string, [1] = short written feedback.
     */
    public String[] evaluatePlayer() {
        OpenAIClient client = aiClient;

        StringBuilder transcript = new StringBuilder();
        for (String msg : messages) transcript.append(msg).append("\n");

        String systemPrompt =
                "You are grading a student's performance in a Harkness discussion about \"" + book + "\".\n\n" +
                "The player's contributions are marked with '> You:' in the transcript.\n\n" +
                "Score them 0–100 on:\n" +
                "- Quality of textual analysis (are they citing and analyzing, not just summarizing?)\n" +
                "- Engagement with other speakers (are they building on, questioning, or responding to others?)\n" +
                "- Clarity and relevance (are their points on-topic and well-expressed?)\n\n" +
                "If the player said nothing, score them 0.\n\n" +
                "Respond in EXACTLY this format — two lines, nothing else:\n" +
                "SCORE: <number 0-100>\n" +
                "FEEDBACK: <2-3 sentences of specific, honest feedback addressing the player directly>";

        ResponseCreateParams params = ResponseCreateParams.builder()
                .instructions(systemPrompt)
                .input("Transcript:\n" + transcript)
                .model("gpt-4o")
                .build();

        Response response = client.responses().create(params);
        String raw = response.output().get(0).asMessage().content().get(0).asOutputText().text().trim();

        int score = 50;
        String feedback = "Discussion complete.";
        for (String line : raw.split("\n")) {
            if (line.startsWith("SCORE:")) {
                try { score = Integer.parseInt(line.substring(6).trim()); } catch (NumberFormatException ignored) {}
            } else if (line.startsWith("FEEDBACK:")) {
                feedback = line.substring(9).trim();
            }
        }
        return new String[]{ String.valueOf(score), feedback };
    }

    public String getTeacherMessage() {
        OpenAIClient client = aiClient;
        StringBuilder context = new StringBuilder();
        if (messages.isEmpty()) {
            context.append("Begin the discussion as described in your instructions.");
        } else {
            context.append("Recent contributions:\n");
            for (String msg : messages) context.append("- ").append(msg).append("\n");
            context.append("\nContinue facilitating the discussion.");
        }
        ResponseCreateParams params = ResponseCreateParams.builder()
                .instructions(prompt)
                .input(context.toString())
                .model("gpt-4o")
                .build();
        Response response = client.responses().create(params);
        String message = response.output().get(0).asMessage().content().get(0).asOutputText().text();
        messages.add("Teacher: " + message);
        teacher.enqueueSpeech(message);
        return message;
    }

    /**
     * Checks whether the player's comment warrants teacher intervention.
     * Returns a redirect message if problematic, or null if a student should respond normally.
     */
    public String checkAndRedirect(String playerMessage) {
        OpenAIClient client = aiClient;
        StringBuilder recentContext = new StringBuilder();
        int start = Math.max(0, messages.size() - 4);
        for (int i = start; i < messages.size(); i++)
            recentContext.append("- ").append(messages.get(i)).append("\n");

        String systemPrompt =
                "You are a Harkness discussion teacher for the book: " + book + ".\n" +
                "A student just contributed to the discussion. Decide if their comment is problematic.\n\n" +
                "Problematic means: off-topic, rude, inappropriate, completely unrelated to the text, or disruptive.\n\n" +
                "If it is acceptable (on-topic, genuine engagement, even if brief or imperfect), " +
                "respond with exactly the word: OK\n\n" +
                "If it is problematic, respond with a brief, calm redirect (1–2 sentences) that " +
                "steers the discussion back to the text. Sound like a real teacher, not an AI.";

        String input = "Recent discussion:\n" + recentContext +
                "\nStudent just said: \"" + playerMessage + "\"";

        ResponseCreateParams params = ResponseCreateParams.builder()
                .instructions(systemPrompt)
                .input(input)
                .model("gpt-4o")
                .build();
        Response response = client.responses().create(params);
        String result = response.output().get(0).asMessage().content().get(0).asOutputText().text().trim();

        if (result.equalsIgnoreCase("OK") || result.toUpperCase().startsWith("OK")) return null;
        messages.add("Teacher: " + result);
        teacher.enqueueSpeech(result);
        return result;
    }

    private int gaussianAbility(int center) {
        double stdDev = 2.0;
        int raw = (int) Math.round(center + random.nextGaussian() * stdDev);
        return Math.max(1, Math.min(5, raw));
    }

    public int getLastSpeakerSeat() {
        return order[(orderIndex - 1) % order.length] + 1;
    }

    /** Returns the display name for a seat (1-6 = students, 7 = teacher). */
    public String getSpeakerName(int seat) {
        if (seat >= 1 && seat <= 6) return students[seat - 1].name;
        return "Teacher";
    }
}
