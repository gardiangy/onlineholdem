package hu.onlineholdem.util;

import hu.onlineholdem.enums.Suit;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class SuitConverter implements AttributeConverter<Suit,String>{

    @Override
    public String convertToDatabaseColumn(Suit suit) {
        switch (suit) {
            case DIAMONDS:
                return "d";
            case SPADES:
                return "s";
            case HEARTS:
                return "h";
            case CLUBS:
                return "c";
            default:
                throw new IllegalArgumentException("Unknown" + suit);
        }
    }

    @Override
    public Suit convertToEntityAttribute(String s) {
        switch (s) {
            case "d":
                return Suit.DIAMONDS;
            case "h":
                return Suit.HEARTS;
            case "c":
                return Suit.CLUBS;
            case "s":
                return Suit.SPADES;
            default:
                throw new IllegalArgumentException("Unknown" + s);
        }
    }
}
