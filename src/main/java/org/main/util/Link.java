package org.main.util;

public class Link {
    public char src, dst;
    public int cost;

    public Link(char _src, char _dst, int _cost){
        this.src = _src;
        this.dst = _dst;
        this.cost = _cost;
    }
}
