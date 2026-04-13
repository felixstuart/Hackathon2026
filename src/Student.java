import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.ArrayList;
import java.util.Random;

public class Student {
    public int ability;

    IntellectualStyle intellectualStyle;
    SocialRegister socialRegister;
    DiscussionBehavior behavior;
    AcademicBackground academicBackground;

    private static final Random random = new Random();

    private static <T extends Enum<T>> T randomEnum(Class<T> enumClass) {
        T[] values = enumClass.getEnumConstants();
        return values[random.nextInt(values.length)];
    }

    public Student(int ability) {
        this.ability = ability;

        this.intellectualStyle = randomEnum(IntellectualStyle.class);
        this.socialRegister    = randomEnum(SocialRegister.class);
        this.behavior          = randomEnum(DiscussionBehavior.class);
        this.academicBackground = randomEnum(AcademicBackground.class);
    }

    private String buildSystemPrompt() {
        return String.format(
                "You are a student in a Harkness discussion. Here is your profile:\n\n" +
                        "Intellectual style: %s\n" +
                        "Discussion behavior: %s\n" +
                        "Academic background: %s\n" +
                        "Social register: %s\n" +
                        "Ability level: %d/5\n\n" +
                        "Stay in character. Speak naturally as a student — not an AI. " +
                        "Keep your response to 1-3 sentences unless the moment calls for more. " +
                        "Reference prior speakers by name when building on or disagreeing with them."+
                        "Feel free to ask open-ended questions when the moment calls for it."+
                        "Make sure to refer to the text.",
                intellectualStyle.promptDescription,
                behavior.promptDescription,
                academicBackground.promptDescription,
                socialRegister.promptDescription,
                ability
        );
    }

    public String getMessage(String discussionPrompt,  ArrayList<String> lastMessages) {
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        // Build context from recent messages
        StringBuilder context = new StringBuilder();
        context.append("Discussion topic: ").append(discussionPrompt).append("\n\n");
        context.append("Recent contributions:\n");
        for (String msg : lastMessages) {
            context.append("- ").append(msg).append("\n");
        }
        context.append("\nNow contribute your response.");

        ResponseCreateParams params = ResponseCreateParams.builder()
                .instructions(buildSystemPrompt())
                .input(context.toString())
                .model("gpt-5.4")
                .build();

        Response response = client.responses().create(params);
        String message = response.output().get(0).asMessage().content().get(0).asOutputText().text();

        return message;
    }
}