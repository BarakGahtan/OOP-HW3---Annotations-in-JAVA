package Solution;

import Provided.StoryTestException;
import java.util.*;

public class StoryTestExceptionImpl extends StoryTestException {
    private int numberOfFailures;
    private List<String> expectedParams;
    private List<String> foundParams;
    private String firstSentence;

    public StoryTestExceptionImpl(){
        numberOfFailures = 0;
        expectedParams = new LinkedList<>();
        foundParams = new LinkedList<>();
    }

    public void setFirstSentence(String givenSentence){
        firstSentence = givenSentence;
    }

    public void incNumFailures(){
        numberOfFailures++;
    }

    public void addExpectedParam(String param){
        expectedParams.add(param);
    }

    public void addFoundParam(String param){
        foundParams.add(param);
    }

    public String getSentence(){
        return firstSentence;
    }

    public List<String> getStoryExpected(){
        return expectedParams;
    }

    public List<String> getTestResult(){
        return foundParams;
    }

    public int getNumFail(){
        return numberOfFailures;
    }
}