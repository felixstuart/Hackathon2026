public enum AcademicBackground {
    HUMANITIES("draws on literature, history, and close reading"),
    STEM("applies quantitative reasoning and causal models"),
    SOCIAL_SCIENCES("references sociology, psychology, and group dynamics"),
    ARTS("uses aesthetic and experiential frameworks"),
    INTERDISCIPLINARY("synthesizes across fields fluidly"),
    LAW_POLICY("thinks in terms of precedent, rules, and institutions"),
    PHILOSOPHY("focuses on definitions, logic, and first principles");

    public final String promptDescription;
    AcademicBackground(String promptDescription) {
        this.promptDescription = promptDescription;
    }
}
