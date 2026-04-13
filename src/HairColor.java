import java.awt.Color;

public enum HairColor {
    BLACK      (new Color( 20,  15,  10)),
    DARK_BROWN (new Color( 75,  45,  20)),
    LIGHT_BROWN(new Color(135,  90,  45)),
    BLONDE     (new Color(230, 195, 100)),
    RED        (new Color(185,  65,  30)),
    GREY       (new Color(160, 155, 150)),
    WHITE      (new Color(235, 235, 230));

    private final Color color;

    HairColor(Color color) { this.color = color; }

    public Color toColor() { return color; }
}
