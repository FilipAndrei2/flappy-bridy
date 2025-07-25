package org.flappy;

public class Pipe {
    public int x;
    public int y;
    public int w;
    public int h;
    public boolean isTop;

    public Pipe(int x, int y, int w, int h, boolean isTop) {
        this.y = y;
        this.w = w;
        this.h = h;
        this.isTop = isTop;
        this.x = x;
    }
}
