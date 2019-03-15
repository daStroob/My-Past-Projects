package arcade;

public class ArduinoInput {
    
    public static final int TYPE_P1_LEFT = 8;
    public static final int TYPE_P1_DOWN = 7;
    public static final int TYPE_P1_RIGHT = 6;
    public static final int TYPE_P1_UP = 5;
    public static final int TYPE_P1_BUTTON1 = 14;
    public static final int TYPE_P1_BUTTON2 = 13;
    public static final int TYPE_P2_LEFT = 1;
    public static final int TYPE_P2_DOWN = 2;
    public static final int TYPE_P2_RIGHT = 3;
    public static final int TYPE_P2_UP = 4;
    public static final int TYPE_P2_BUTTON1 = 16;
    public static final int TYPE_P2_BUTTON2 = 15;
    public static final int TYPE_CLOSE = 9;
    public static final int TYPE_MINIMIZE = 10;
    public static final int TYPE_PAUSE = 11;
    
    public boolean pressed, released, typed;
    public int TYPE;
    
    public ArduinoInput() {
        pressed = false;
        released = false;
        TYPE = 0;
    }
    
    public ArduinoInput(int type) {
        pressed = false;
        released = false;
        TYPE = type;
    }
    
}
