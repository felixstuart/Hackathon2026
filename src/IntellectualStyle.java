public enum IntellectualStyle {
    EMPIRICIST("grounds claims in evidence and data"),
    MORALIST("frames issues through ethics and values"),
    CONTRARIAN("challenges prevailing assumptions"),
    SYSTEMS_THINKER("looks for structural and systemic causes"),
    ANECDOTE_DRIVEN("reasons from personal experience"),
    IDEALIST("argues toward what ought to be, not what is");

    public final String promptDescription;
    IntellectualStyle(String promptDescription) {
        this.promptDescription = promptDescription;
    }
}
