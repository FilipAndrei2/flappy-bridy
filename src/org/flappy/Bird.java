package org.flappy;

public class Bird {
    public int x;
    public int y;
    public int w;
    public int h;
    public int type;
    public Bird(int x, int y, int w, int h, int type) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.type = type;
    }

    public static final int YELLOW_BIRD = 0;
    public static final int BLUE_BIRD = 1;
    public static final int RED_BIRD = 2;
}
