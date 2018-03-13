package com.android.flashbackmusic;

import java.util.ArrayList;

/**
 * Created by K on 3/12/2018.
 */

public class AnonymousNameGenerator {

    //Make sure these two arrays have different length, otherwise it would be same combination everytime
    private final static String[] prefix = {"Retarded", "Small", "Smelly"};
    private final static String[] postfix = {"Redditor", "Nugget", "Cat"};

    public static String GenerateAnonymousName(String email) {
        int prefixSize = prefix.length;
        int postfixSize = postfix.length;

        String name = "Anonymous ";
        name += prefix[Math.abs(email.hashCode()) % prefixSize];
        name += " ";
        name += postfix[Math.abs(email.hashCode()) % postfixSize];
        return name;
    }
}
