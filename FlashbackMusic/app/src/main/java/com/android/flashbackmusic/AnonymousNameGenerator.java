package com.android.flashbackmusic;

import java.util.ArrayList;

/**
 * Created by K on 3/12/2018.
 */

public class AnonymousNameGenerator {

    //Make sure these two arrays have different length, otherwise it would be same combination everytime
    private final static String[] prefix = {"Agreeable","Buffalo","Befuddled","Clumsy","Delicious","Exuberant","Fluffy","Good",
            "Handsome","Immense","Jittery","Kind","Large","Magnificent","Slow", "Small", "Smelly"};
    private final static String[] postfix = {"Aardvark","Buffalo","Cat","Dog","Elephant","Flamingo","Giant Clam","Hamster" +
            "Gerenuk", "Snake","Penguin","Lion","Dolphin","Tardigrade","Caterpillar"};

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
