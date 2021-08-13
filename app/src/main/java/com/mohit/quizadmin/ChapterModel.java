package com.mohit.quizadmin;

public class ChapterModel {

    private String quesID;
    private String question;
    private String noOfSets;
    private String setCounter;

    public String getNoOfSets() {
        return noOfSets;
    }

    public void setNoOfSets(String noOfSets) {
        this.noOfSets = noOfSets;
    }

    public String getSetCounter() {
        return setCounter;
    }

    public void setSetCounter(String setCounter) {
        this.setCounter = setCounter;
    }

    public ChapterModel(String quesID, String question) {
        this.quesID = quesID;
        this.question = question;
    }

    public String getQuesID() {
        return quesID;
    }

    public void setQuesID(String quesID) {
        this.quesID = quesID;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
