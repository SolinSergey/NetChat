package Task2and3;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import Task2and3.CutMassiv;
import Task2and3.CheckMassiv;

public class TestCutMassiv {

    @ParameterizedTest
    @MethodSource ("dataForTest1Throws")
    public void testCutMassiveTrows(int[] inData, int[] result){
        Assertions.assertThrows(RuntimeException.class,()->CutMassiv.getRightSideMassiv(inData));
    }

    @ParameterizedTest
    @MethodSource ("dataForTest1")
    public void testCutMassive(int[] inData, int[] result) {
       Assertions.assertArrayEquals(result, CutMassiv.getRightSideMassiv(inData));
    }

    @ParameterizedTest
    @MethodSource("dataForTest3True")
    public void testChekMassiveTrue(int[] inData){
        Assertions.assertTrue(CheckMassiv.checkMassiv1or4Elements(inData));
    }


    public static Stream<Arguments> dataForTest3True(){
        List<Arguments> out = new ArrayList<>();
        out.add(Arguments.arguments(new int[]{1,1,4,1}));
        out.add(Arguments.arguments(new int[]{4,1,4,1}));
        return out.stream();
    }

    @ParameterizedTest
    @MethodSource("dataForTest3False")
    public void testChekMassiveFalse(int[] inData){
        Assertions.assertFalse(CheckMassiv.checkMassiv1or4Elements(inData));
    }


    public static Stream<Arguments> dataForTest3False(){
        List<Arguments> out = new ArrayList<>();
        out.add(Arguments.arguments(new int[]{2,2,2,4}));
        out.add(Arguments.arguments(new int[]{1,1,2,1}));
        return out.stream();
    }

    public static Stream<Arguments> dataForTest1(){
        List<Arguments> out = new ArrayList<>();
        out.add(Arguments.arguments(new int[]{1, 1, 2, 4, 8, 1, 7, 8}, new int[]{8, 1, 7, 8}));
        out.add(Arguments.arguments(new int[]{2,3,5,4,5,4,6,4,1,1,3}, new int[]{1,1,3}));
        out.add(Arguments.arguments(new int[]{7,5,4,1,1,4,1}, new int[]{1}));
        out.add(Arguments.arguments(new int[]{4,1,4,5,6,7,4,3,2,2}, new int[]{3,2,2}));
        out.add(Arguments.arguments(new int[]{5,6,7,3,2,2,4}, new int[]{}));
        return out.stream();
    }

    public static Stream<Arguments> dataForTest1Throws(){
        List<Arguments> out = new ArrayList<>();
        out.add(Arguments.arguments(new int[]{1, 1, 2, 8, 1, 7, 8}, new int[]{}));
        out.add(Arguments.arguments(new int[]{2,3,5,5,6,1,1,3}, new int[]{}));
        out.add(Arguments.arguments(new int[]{7,5,1,1,1}, new int[]{}));
        out.add(Arguments.arguments(new int[]{1,5,6,7,3,2,2}, new int[]{}));
        out.add(Arguments.arguments(new int[]{5,6,7,3,2,2}, new int[]{}));
        return out.stream();
    }



}
