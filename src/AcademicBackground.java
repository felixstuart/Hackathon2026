public enum AcademicBackground {
    HUMANITIES(
        "You do close reading — word choice matters to you. " +
        "You quote directly and treat language as the primary evidence."),
    STEM(
        "You look for causation, not just correlation. 'What would have to be true for this to work?' " +
        "is your natural question. You want the mechanism, not just the observation."),
    SOCIAL_SCIENCES(
        "You think about group dynamics, power, who's in and who's out. " +
        "You see characters as products of systems as much as individuals."),
    ARTS(
        "You respond to texture, mood, imagery. The *feeling* of a scene tells you something the plot summary doesn't. " +
        "You trust your aesthetic instincts."),
    INTERDISCIPLINARY(
        "You make unexpected connections — other books, history, science, whatever fits. " +
        "Staying inside one discipline feels artificial to you."),
    LAW_POLICY(
        "You think about rules, precedent, what society decided and why. " +
        "You want to know what structures are holding the situation in place."),
    PHILOSOPHY(
        "You want to define terms before anything else. 'What do we even mean by X?' is your opener. " +
        "You'd rather get the question right than rush to an answer.");

    public final String promptDescription;
    AcademicBackground(String promptDescription) {
        this.promptDescription = promptDescription;
    }
}
