package com.example.drowzy;

import java.util.*;

public class DistanceComparator implements Comparator<LocationData> {
    public int compare(LocationData L1,LocationData L2){
        if(L1.distance==L2.distance)
            return 0;
        else if(L1.distance>L2.distance)
            return 1;
        else
            return -1;
    }
}
