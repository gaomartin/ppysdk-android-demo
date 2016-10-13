package com.pplive.testppysdk;

import java.util.ArrayList;

public interface ArrayListResultCallack<T>
{
    void result(int errcode, ArrayList<T> result);
}