package com.davidaq.logio.util;

public interface Queue<InType, OutType> {
    public void push(InType val);

    public OutType shift();
}
