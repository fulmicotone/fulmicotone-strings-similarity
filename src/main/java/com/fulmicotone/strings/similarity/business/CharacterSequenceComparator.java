package com.fulmicotone.strings.similarity.business;

import com.fulmicotone.strings.similarity.models.CharacterSequence;
import com.fulmicotone.strings.similarity.models.CharacterSequenceComparison;
import com.fulmicotone.strings.similarity.models.Phrase;
import com.fulmicotone.strings.similarity.utils.Combination;
import com.fulmicotone.strings.similarity.utils.StringDistanceAlgorithms;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.experimental.Sift4;
import info.debatty.java.stringsimilarity.interfaces.StringDistance;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CharacterSequenceComparator {







       public static <E extends CharacterSequence>
      List<CharacterSequenceComparison> compare(CharacterSequence a, CharacterSequence b){


         List<CharacterSequenceComparison> compareResult=new ArrayList<>();

         Class clazz1 = ((Class) ((ParameterizedType) a.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);

         Class clazz2 = ((Class) ((ParameterizedType)b.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);

        if(clazz1.getName().equals(clazz2.getName())==false){ new IllegalArgumentException("can't compare two different clazz");}

        //main comparison
         compareResult.add( distance(a,b));


            //group by sort id in the context with factor discriminant
            Map<String, List<CharacterSequence>> child1 = ((Stream<CharacterSequence>) a
                    .asUnitStream())
                    .collect(Collectors.groupingBy(x -> x.getSortIndex() + "a"));

            Map<String, List<CharacterSequence>> child2 = ((Stream<CharacterSequence>) b
                    .asUnitStream())
                    .collect(Collectors.groupingBy(x -> x.getSortIndex() + "b"));


            if(child1.size()!=0&&child2.size()!=0) {

                //create a key combination
                ArrayList<String> keys = new ArrayList<>(child1.keySet());
                keys.addAll(new ArrayList<>(child2.keySet()));
                //remove the combination in the same factor



                    List<CharacterSequenceComparison> childComparison = Combination
                            .get(keys.toArray(new String[]{}), keys.size(), 2)
                            .stream()
                            .filter(tuple ->
                                    tuple.get(0).substring(tuple.get(0).length() - 1).equals(tuple.get(1)
                                                    .substring(tuple.get(1).length() - 1)) == false)
                            .flatMap(tuple -> compare(child1.get(tuple.get(0)).get(0),
                                    child2.get(tuple.get(1)).get(0)).stream())
                            .collect(Collectors.toList());

                    compareResult.addAll(childComparison);

            }

         return  compareResult;




     }


    //*Driver function to check for above function*//*
    public static void main (String[] args) {


        Phrase a = PhraseFactory.newOne()
                .withSplitterDelimiter("-")
                .withMinWordLength(1)
                .build().apply("soccer-movie and gadget");

        Phrase b = PhraseFactory.newOne()
                .withSplitterDelimiter("-")
                .build().apply("ab");


       // List<CharacterSequenceComparison> r = compareImplementation(a, b);


    }

    public static <E extends StringDistance>CharacterSequenceComparison distance (CharacterSequence a , CharacterSequence b){

        Map<Class<?>,Double> r=new HashMap<>();

        for(StringDistanceAlgorithms sda:StringDistanceAlgorithms.values()){
            r.put(sda.getAlgorithmClass(),sda.getInstance().distance(a.toString(),b.toString()));
        }

        return new CharacterSequenceComparison(a,b,r);

    }
}
