package demos.nehe.lesson32;

public class TargetType {
    public static final TargetType FACE = new TargetType("Face");
    public static final TargetType BUCKET = new TargetType("Bucket");
    public static final TargetType TARGET = new TargetType("Target");
    public static final TargetType COKE = new TargetType("Coke");
    public static final TargetType VASE = new TargetType("Vase");

    private final String myName; // for debug only

    private TargetType(String name) {
        myName = name;
    }

    public String toString() {
        return myName;
    }
}
