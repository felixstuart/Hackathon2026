import java.awt.Color;

public enum ClothingColor {
    NAVY  (new Color( 30,  50, 100)),
    RED   (new Color(180,  45,  45)),
    GREEN (new Color( 45, 120,  65)),
    GREY  (new Color(120, 120, 125)),
    BEIGE (new Color(210, 190, 155)),
    BLACK (new Color( 35,  35,  40)),
    WHITE (new Color(240, 240, 240)),
    PURPLE(new Color(110,  55, 150));

    private final Color color;

    ClothingColor(Color color) { this.color = color; }

    public Color toColor() { return color; }

    public Color darkerShade() {
        int r = (int)(color.getRed()   * 0.72);
        int g = (int)(color.getGreen() * 0.72);
        int b = (int)(color.getBlue()  * 0.72);
        return new Color(r, g, b);
    }
}
