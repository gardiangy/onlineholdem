package hu.onlineholdem.restclient.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.enums.HandStrength;

public class HandEvaluator {

    public static EvaluatedHand evaluateHand(List<Card> board, Card playerCardOne, Card playerCardTwo){

        List<Card> cardList = new ArrayList<>();
        cardList.addAll(board);
        cardList.add(playerCardOne);
        cardList.add(playerCardTwo);

        List<Card> highCards = new ArrayList<>();
        EvaluatedHand evaluatedHand = new EvaluatedHand();

        if(null != hasRoyalFlush(cardList)){
            evaluatedHand.setHandStrength(HandStrength.ROYAL_FLUSH);
            return evaluatedHand;
        }
        List<Card> straightFlushValues = hasStraightFlush(cardList);
        if(null != straightFlushValues){
            Collections.sort(straightFlushValues, new CardComperator());
            highCards.add(straightFlushValues.get(4));
            evaluatedHand.setHandStrength(HandStrength.STRAIGHT_FLUSH);
            evaluatedHand.setHighCard(highCards);
            return evaluatedHand;
        }
        Card fourOfAKindValue = hasFourOfAKind(cardList);
        if(null != fourOfAKindValue){
            Collections.sort(cardList, new CardComperator());
            Collections.reverse(cardList);
            for (Card card : cardList){
                if(!card.equals(fourOfAKindValue)){
                    highCards.add(card);
                    break;
                }
            }
            evaluatedHand.setHandStrength(HandStrength.FOUR_OF_A_KIND);
            evaluatedHand.setHighCard(highCards);
            return evaluatedHand;
        }
        if(null != hasFullHouse(cardList)){
            evaluatedHand.setHandStrength(HandStrength.FULL_HOUSE);
            return evaluatedHand;
        }
        List<Card> flushValues = hasFlush(cardList);
        if(null != flushValues){
            Collections.sort(flushValues,new CardComperator());
            highCards.add(flushValues.get(4));

            evaluatedHand.setHandStrength(HandStrength.FLUSH);
            evaluatedHand.setHighCard(highCards);
            return evaluatedHand;

        }

        List<Card> straightValues = hasStraight(cardList);
        if(null != straightValues){
            Collections.sort(straightValues,new CardComperator());
            highCards.add(straightValues.get(4));

            evaluatedHand.setHandStrength(HandStrength.STRAIGHT);
            evaluatedHand.setHighCard(highCards);
            return evaluatedHand;
        }

        Card threeOfAKindValue = hasThreeOfAKind(cardList);
        if(null != threeOfAKindValue){
            Collections.sort(cardList,new CardComperator());
            Collections.reverse(cardList);
            for(Card card : cardList){
                if(!card.equals(threeOfAKindValue) && highCards.size() < 2){
                    highCards.add(card);
                }
            }

            evaluatedHand.setHandStrength(HandStrength.THREE_OF_A_KIND);
            evaluatedHand.setHighCard(highCards);
            return evaluatedHand;
        }

        List<Card> twoPairValues = hasTwoPair(cardList);
        if(null != twoPairValues){
            Collections.sort(cardList,new CardComperator());
            Collections.reverse(cardList);
            for(Card card : cardList){
                if(!twoPairValues.contains(card) && highCards.size() < 1){
                    highCards.add(card);
                }
            }
            evaluatedHand.setHandStrength(HandStrength.TWO_PAIR);
            evaluatedHand.setHighCard(highCards);
            return evaluatedHand;
        }

        Card onePairValue = hasOnePair(cardList);
        if(null != onePairValue){
            Collections.sort(cardList,new CardComperator());
            Collections.reverse(cardList);
            for(Card card : cardList){
                if(!card.equals(onePairValue) && highCards.size() < 3){
                    highCards.add(card);
                }
            }

            evaluatedHand.setHandStrength(HandStrength.ONE_PAIR);
            evaluatedHand.setHighCard(highCards);
            return evaluatedHand;
        }

        Collections.sort(cardList, new CardComperator());

        evaluatedHand.setHandStrength(HandStrength.HIGH_CARD);
        evaluatedHand.setHighCard(cardList);
        return evaluatedHand;
}

    public static Card hasOnePair(List<Card> cards){
        Set<Integer> valuesWithOutDuplicates = new HashSet<>();
        Card pair = null;
        for(Card card : cards){
            if(!valuesWithOutDuplicates.add(card.getValue())){
                pair = card;
            }
        }
        if(null != pair && valuesWithOutDuplicates.size() == cards.size() - 1){
            return pair;
        }

        return null;
    }

    public static List<Card> hasTwoPair(List<Card> cards){
        List<Card> pairs = new ArrayList<>();
        for(Card card : cards){
            int sameCardValue = 0;
            for(Card otherCard : cards){
                if(card.equals(otherCard)){
                    continue;
                }
                if(card.getValue().equals(otherCard.getValue())){
                    sameCardValue++;
                }
            }
            if(sameCardValue > 0 ){
                pairs.add(card);
            }
        }

        return pairs.size() > 1 ? pairs : null;
    }

    public static Card hasThreeOfAKind(List<Card> cards){
        for(Card card : cards){
            int sameCardValue = 0;
            for(Card otherCard : cards){
                if(card.equals(otherCard)){
                    continue;
                }
                if(card.getValue().equals(otherCard.getValue())){
                    sameCardValue++;
                }
            }
            if(sameCardValue == 3 ){
                return card;
            }
        }

        return null;
    }

    public static List<Card> hasStraight(List<Card> cards){

        List<Card> straight = new ArrayList<>();
        Collections.sort(cards,new CardComperator());

        Integer previousValue = cards.get(0).getValue();
        for(Card card : cards){
            if(card.getValue() == previousValue + 1 || (previousValue == 13 && card.getValue() == 1)){
                straight.add(card);
            }
        }
        if(straight.size() == 5){
            return straight;
        }

        return null;
    }

    public static List<Card> hasFlush(List<Card> cards){

        List<Card> flush = new ArrayList<>();
        for(Card card : cards){
            for(Card otherCard : cards){
                if(card.equals(otherCard)){
                    continue;
                }
                if(card.getSuit().equals(otherCard.getSuit())){
                    flush.add(card);
                }
            }
            if(flush.size() == 5 ){
                return flush;
            }
        }

        return null;
    }

    public static Card hasFourOfAKind(List<Card> cards){
        for(Card card : cards){
            int sameCardValue = 0;
            for(Card otherCard : cards){
                if(card.equals(otherCard)){
                    continue;
                }
                if(card.getValue().equals(otherCard.getValue())){
                    sameCardValue++;
                }
            }
            if(sameCardValue == 4 ){
                return card;
            }
        }

        return null;
    }

    public static List<Card> hasFullHouse(List<Card> cards){
        List<Card> fullHouse = new ArrayList<>();
        Card onePair = hasOnePair(cards);
        Card threeOfAKind = hasThreeOfAKind(cards);

        if(null != onePair && null != threeOfAKind){
            fullHouse.add(onePair);
            fullHouse.add(threeOfAKind);
            return fullHouse;
        }


        return null;
    }

    public static List<Card> hasStraightFlush(List<Card> cards){
        List<Card> straight = hasStraight(cards);
        List<Card> flush = hasFlush(cards);

        if(null != straight && null != flush){
            return straight;
        }


        return null;
    }

    public static List<Card> hasRoyalFlush(List<Card> cards){
        List<Card> straightFlush = hasStraightFlush(cards);

        if(null != straightFlush){
            Collections.sort(straightFlush, new CardComperator());

            if(straightFlush.get(0).getValue() == 1 && straightFlush.get(4).getValue() == 13){
                return straightFlush;
            }
        }




        return null;
    }
}
