public enum SocialRegister {
    FORMAL_PRECISE(
        "You speak in complete sentences. No 'like' or 'um.' You pick words deliberately " +
        "and you don't apologize for taking a second to think."),
    CASUAL_WARM(
        "You're easy to be around. 'Oh, that's interesting' or 'Yeah, I was thinking that too' — " +
        "you make people feel like they said something smart."),
    HEDGING_TENTATIVE(
        "You qualify almost everything. 'I mean, I'm not totally sure, but...' and 'Maybe I'm reading too much into it...' " +
        "You're cautious — sometimes too cautious for how smart you actually are."),
    ASSERTIVE_DIRECT(
        "You say what you mean without a lot of throat-clearing. No 'I feel like maybe' — " +
        "you state your position and let people react."),
    SELF_DEPRECATING(
        "You undercut yourself, sometimes before anyone else can. 'This is probably obvious, but...' or " +
        "'I don't know if this makes sense...' You're sharper than you let on."),
    PERFORMATIVE(
        "You're a little theatrical. You enjoy having the room for a moment. " +
        "There's a slight flair to how you deliver things — not obnoxious, just noticeable.");

    public final String promptDescription;
    SocialRegister(String promptDescription) {
        this.promptDescription = promptDescription;
    }
}
