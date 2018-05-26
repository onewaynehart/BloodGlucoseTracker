package com.yourharts.www.bloodglucosetracker;


import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

public class GeneralEntry <K, V> extends Entry {
    private final K key;
    private V value;


    public GeneralEntry(K key, V value){
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }


    public V getValue() {
        return value;
    }


    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }
}
