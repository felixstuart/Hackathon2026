import java.util.Random;

public class VisualCharacter {

    // Static appearance — generated once at construction
    public final Gender gender;
    public final SkinTone skinTone;
    public final HairStyle hairStyle;
    public final HairColor hairColor;
    public final ClothingStyle clothingStyle;
    public final ClothingColor clothingColor;
    public final boolean isTeacher;
    public final String displayName;

    // Live animation state
    private MouthState mouthState   = MouthState.CLOSED;
    private GestureState gestureState = GestureState.IDLE;
    private int animationTick = 0;

    public VisualCharacter(boolean isTeacher, String displayName) {
        this(isTeacher, displayName, new Random().nextLong());
    }

    public VisualCharacter(boolean isTeacher, String displayName, long seed) {
        this.isTeacher   = isTeacher;
        this.displayName = displayName;
        Random rng = new Random(seed);
        gender        = randomEnum(rng, Gender.class);
        skinTone      = randomEnum(rng, SkinTone.class);
        hairStyle     = randomEnum(rng, HairStyle.class);
        hairColor     = randomEnum(rng, HairColor.class);
        clothingStyle = randomEnum(rng, ClothingStyle.class);
        clothingColor = randomEnum(rng, ClothingColor.class);
    }

    // Called by the game loop every tick (50 ms)
    public void tick() {
        animationTick++;
        if (gestureState != GestureState.IDLE && animationTick % 80 == 0) {
            gestureState = GestureState.IDLE;
        }
    }

    // Getters
    public MouthState    getMouthState()    { return mouthState; }
    public GestureState  getGestureState()  { return gestureState; }
    public int           getAnimationTick() { return animationTick; }

    // Setters — called by game logic to trigger animations
    public void setMouthState(MouthState state)     { this.mouthState   = state; }
    public void setGestureState(GestureState state) { this.gestureState = state; }

    private static <T extends Enum<T>> T randomEnum(Random rng, Class<T> cls) {
        T[] values = cls.getEnumConstants();
        return values[rng.nextInt(values.length)];
    }
}
