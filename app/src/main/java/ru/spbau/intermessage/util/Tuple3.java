package ru.spbau.intermessage.util;

public class Tuple3 <A, B, C> {
    public Tuple3() {}
    public Tuple3(A f, B s, C t) {first = f; second = s; third = t;}
    
    public A first;
    public B second;
    public C third;
}
