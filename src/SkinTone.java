import java.awt.Color;

public enum SkinTone {
    LIGHT       (new Color(255, 224, 189), new Color(230, 190, 150), new Color(255, 240, 210)),
    MEDIUM_LIGHT(new Color(240, 195, 145), new Color(210, 165, 115), new Color(255, 215, 170)),
    MEDIUM      (new Color(198, 148, 100), new Color(168, 118,  70), new Color(220, 170, 120)),
    MEDIUM_DARK (new Color(155, 105,  65), new Color(125,  80,  45), new Color(180, 130,  90)),
    DARK        (new Color(101,  67,  33), new Color( 75,  48,  20), new Color(125,  90,  55));

    private final Color base;
    private final Color shadow;
    private final Color highlight;

    SkinTone(Color base, Color shadow, Color highlight) {
        this.base = base;
        this.shadow = shadow;
        this.highlight = highlight;
    }

    public Color baseColor()      { return base; }
    public Color shadowColor()    { return shadow; }
    public Color highlightColor() { return highlight; }
}
