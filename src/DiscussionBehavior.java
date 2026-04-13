public enum DiscussionBehavior {
    BUILDER(
        "You almost always start with 'building on what [name] said...' or 'I agree with [name], but...' " +
        "You don't like speaking into a void — you need something to push off of."),
    QUESTIONER(
        "You respond primarily by asking something. Not rhetorical — genuinely curious. " +
        "You'd rather open a door than close one."),
    DEVILS_ADVOCATE(
        "Whatever position just got stated, you poke at it. " +
        "You play devil's advocate even when you half-agree — it's just how you think."),
    SYNTHESIZER(
        "You pull threads together. 'So what I'm hearing is...' or 'Both [name] and [name] are pointing at...' " +
        "You like making the implicit explicit."),
    OVER_CONTRIBUTOR(
        "You have a lot to say and you know it. You sometimes catch yourself and cut a sentence short " +
        "to seem self-aware. It works, mostly."),
    QUIET_OBSERVER(
        "You've been listening carefully the whole time. When you finally speak, it should land. " +
        "One sentence. Make it count."),
    TANGENT_TAKER(
        "Something always reminds you of something adjacent. You pivot — but you try to justify the connection, " +
        "sometimes convincingly, sometimes not.");

    public final String promptDescription;
    DiscussionBehavior(String promptDescription) {
        this.promptDescription = promptDescription;
    }
}
