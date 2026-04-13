import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Participant {

        public final String name;
        public int ability;

        IntellectualStyle intellectualStyle;
        SocialRegister socialRegister;
        DiscussionBehavior behavior;
        AcademicBackground academicBackground;

        private static final Random random = new Random();

        // Shared across all students — created once, reused forever
        private static final OpenAIClient aiClient  = OpenAIOkHttpClient.fromEnv();
        private static final HttpClient httpClient = HttpClient.newHttpClient();

        // Single-threaded queue — speech plays in submission order, never overlapping
        private static final ExecutorService speechQueue = Executors.newSingleThreadExecutor();

        // Incremented on stopSpeech() — queued tasks with a stale generation are no-ops
        private static volatile int     speechGeneration   = 0;
        private static volatile Process currentSpeechProcess = null;

        private static final String[] VOICES = { "alloy", "verse", "coral", "sage", "ember" };
        private final String voice;

        private static <T extends Enum<T>> T randomEnum(Class<T> enumClass) {
            T[] values = enumClass.getEnumConstants();
            return values[random.nextInt(values.length)];
        }

        public Participant(int ability, String name) {
            this(ability, name, VOICES[random.nextInt(VOICES.length)]);
        }

        public Participant(int ability, String name, String voice) {
            this.ability = ability;
            this.name    = name;
            this.intellectualStyle  = randomEnum(IntellectualStyle.class);
            this.socialRegister     = randomEnum(SocialRegister.class);
            this.behavior           = randomEnum(DiscussionBehavior.class);
            this.academicBackground = randomEnum(AcademicBackground.class);
            this.voice = voice;
        }

        public String getMessage(String discussionPrompt, ArrayList<String> lastMessages) {
            String message = fetchText(discussionPrompt, lastMessages);
            enqueueSpeech(message);
            return message;
        }

        /** Generates only the text response — no TTS. Used by Discussion for pre-generation. */
        String fetchText(String discussionPrompt, ArrayList<String> lastMessages) {
            StringBuilder context = new StringBuilder();
            context.append("Discussion topic: ").append(discussionPrompt).append("\n\n");
            context.append("Recent contributions:\n");
            for (String msg : lastMessages) context.append("- ").append(msg).append("\n");
            context.append("\nNow respond.");

            ResponseCreateParams params = ResponseCreateParams.builder()
                    .instructions(buildSystemPrompt())
                    .input(context.toString())
                    .model("gpt-4o-mini")
                    .build();

            Response response = aiClient.responses().create(params);
            return response.output().get(0).asMessage().content().get(0).asOutputText().text();
        }

        /** Submits pre-generated text to the TTS speech queue. */
        void enqueueSpeech(String message) {
            final int gen = speechGeneration;
            speechQueue.submit(() -> speak(message, gen));
        }

        /** Stops the currently playing audio and discards any queued speech. */
        public static void stopSpeech() {
            speechGeneration++;
            Process p = currentSpeechProcess;
            if (p != null) p.destroy();
        }

        /**
         * Runs {@code r} on the EDT after all currently queued speech has finished.
         * Use this to delay a UI update (e.g. showing a chat bubble) until the speaker is done.
         */
        public static void runAfterSpeech(Runnable r) {
            speechQueue.submit(() -> SwingUtilities.invokeLater(r));
        }

        /** Runs {@code r} on the EDT as the next item in the speech queue. Use to set speaking=true right before audio starts. */
        public static void enqueueTask(Runnable r) {
            speechQueue.submit(() -> SwingUtilities.invokeLater(r));
        }

        private void speak(String text, int gen) {
            try {
                if (gen != speechGeneration) return;

                String apiKey = System.getenv("OPENAI_API_KEY");

                String json = "{\"model\":\"gpt-4o-mini-tts\","
                        + "\"input\":\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\","
                        + "\"voice\":\"" + voice + "\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.openai.com/v1/audio/speech"))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

                if (gen != speechGeneration) return;  // stopped while fetching audio

                File tempFile = File.createTempFile("speech_", ".mp3");
                tempFile.deleteOnExit();
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(response.body());
                }

                Process p = new ProcessBuilder("afplay", tempFile.getAbsolutePath()).start();
                currentSpeechProcess = p;
                p.waitFor();
                currentSpeechProcess = null;

            } catch (Exception e) {
                if (gen == speechGeneration) e.printStackTrace();  // only log if not intentionally stopped
            }
        }

    protected abstract String buildSystemPrompt();

    private static String abilityDescription(int level) {
            if (level <= 2)  return "You're a weak student. Your points are simple, sometimes confused. You rarely cite the text precisely.";
            if (level <= 4)  return "You're average. You can make a point but lack depth — you paraphrase the text more than you analyze it.";
            if (level <= 6)  return "You're solid. You back up your points with specific references and think through implications.";
            if (level <= 8)  return "You're strong. Your analysis is nuanced, you connect ideas, and you push the conversation forward.";
            return "You're the sharpest in the room. You challenge surface readings, draw on subtle evidence, and elevate everyone else.";
        }

        private static String lengthInstruction(DiscussionBehavior b) {
            switch (b) {
                case QUIET_OBSERVER:   return "One sentence only. You speak rarely.";
                case OVER_CONTRIBUTOR: return "Two sentences max. You want to say more — resist it.";
                default:               return "One sentence, two at most. Stop there.";
            }
        }
    }
