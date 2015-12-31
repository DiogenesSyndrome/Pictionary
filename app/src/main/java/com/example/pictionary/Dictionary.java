package com.example.pictionary;

import android.util.Log;

import java.util.HashSet;

/**
 * Created by Lucian on 12/31/2015.
 */
public class Dictionary {
     /*
        HashSet<T> is designed to give O(1) performance while searching for the object,
        considerably faster than Array / List,
        contains only unique entries (duplicates will be overwritten), and does not care about order
        ====> perfect for a BIG Dictionary which needs fast access
        */

    public static HashSet<String> hset = new HashSet<String>();

    public static int addDictionary(){

        //TODO: do in a separate ressource / java file
        String[] dictionary = {"house", "car","dollar"};

        for (String s : dictionary){
            hset.add(s);
        }

        return hset.size();
    }

    public static boolean checkDictionary(String word){
        if (hset.contains(word))
            return true;
        else
            return false;
    }
}
