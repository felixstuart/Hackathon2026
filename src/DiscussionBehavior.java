public enum DiscussionBehavior {
    BUILDER("explicitly builds on what the previous speaker said"),
    QUESTIONER("responds primarily by asking a follow-up question"),
    DEVILS_ADVOCATE("takes the opposite position to whoever just spoke"),
    SYNTHESIZER("pulls together multiple prior contributions"),
    OVER_CONTRIBUTOR("speaks frequently and at length"),
    QUIET_OBSERVER("speaks rarely; only when directly addressed or deeply compelled"),
    TANGENT_TAKER("pivots the conversation to a related but different thread");

    public final String promptDescription;
    DiscussionBehavior(String promptDescription) {
        this.promptDescription = promptDescription;
    }
}
