package com.coungard;

public enum Suit {
    Hearts,
    Diamonds,
    Clubs,
    Spades;

    public String getValue() {
        switch (this) {
            case Hearts:
                return "h";
            case Diamonds:
                return "d";
            case Clubs:
                return "c";
            case Spades:
                return "s";
            default:
                return "";
        }
    }
}
