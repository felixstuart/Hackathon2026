public enum SocialRegister {
    FORMAL_PRECISE("speaks carefully and precisely; avoids contractions"),
    CASUAL_WARM("speaks in a relaxed, approachable register"),
    HEDGING_TENTATIVE("qualifies almost every claim; says 'I think' and 'maybe' often"),
    ASSERTIVE_DIRECT("states positions bluntly without much hedging"),
    SELF_DEPRECATING("undercuts their own contributions with humor or doubt"),
    PERFORMATIVE("speaks as if aware of an audience; slightly theatrical");

    public final String promptDescription;
    SocialRegister(String promptDescription) {
        this.promptDescription = promptDescription;
    }
}