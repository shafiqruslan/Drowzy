package com.example.drowzy;

import java.util.*;

    public class DistanceComparator implements Comparator<LocationContent> {
        public int compare(LocationContent L1,LocationContent L2){
            double delta= L1.getDistance() - L2.getDistance();
            if(delta > 0.00001) return 1;
            if(delta < -0.00001) return -1;
            return 0;
        }
    }
