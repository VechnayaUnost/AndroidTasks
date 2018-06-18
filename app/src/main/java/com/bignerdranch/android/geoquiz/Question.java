package com.bignerdranch.android.geoquiz;

public class Question {

    private int mTextResId;
    private boolean mAnswerTrue;
    private boolean mCheatTrue;     //TODO: "add field"

    public Question(int textResId, boolean answerTrue, boolean cheatTrue) {
        mTextResId = textResId;
        mAnswerTrue = answerTrue;
        mCheatTrue = cheatTrue;
    }

    public int getTextResId() {
        return mTextResId;
    }

    public void setTextResId(int textResId) {
        mTextResId = textResId;
    }

    public boolean isAnswerTrue() {
        return mAnswerTrue;
    }

    public void setAnswerTrue(boolean answerTrue) {
        mAnswerTrue = answerTrue;
    }

    public boolean isCheatTrue() {
        return mCheatTrue;
    }

    public void setCheatTrue(boolean cheatTrue) {
        mCheatTrue = cheatTrue;
    }
}
