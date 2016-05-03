package com.xcode.mobile.smilealarm.gamemanager;

import java.util.Random;

public class GameHandler {
    private static GameHandler _instance = new GameHandler();
    private int _count;
    private Math _currentMath;
    private Random _rand = new Random();

    private GameHandler() {
        reset();
    }

    public static GameHandler getInstance() {
        if (_instance.get_count() == 0)
            _instance.reset();
        return _instance;
    }

    private void reset() {
        _count = GameConstant.NUMBER_OF_MATCHES;
        _currentMath = randomMath();
    }

    public int get_count() {
        return _count;
    }

    public Math get_currentMath() {
        return _currentMath;
    }

    private int zenInt(int lower, int upper) {
        return _rand.nextInt(upper - lower + 1) + lower;
    }

    private Math randomMath() {
        boolean isAdd = _rand.nextBoolean();
        int factor1 = zenInt(GameConstant.MIN_NUMBER, GameConstant.MAX_NUMBER);
        int factor2 = zenInt(GameConstant.MIN_NUMBER, GameConstant.MAX_NUMBER);
        int fakeAns;
        if (isAdd) {
            int rightAns = factor1 + factor2;
            if (_rand.nextInt(100) < GameConstant.ZEN_RIGHT_ANS_RATE) {//40%
                fakeAns = rightAns;
            } else {
                fakeAns = zenInt(rightAns - GameConstant.FAKE_ANS_RANGE, rightAns + GameConstant.FAKE_ANS_RANGE);
            }
        } else {
            if (factor2 > factor1) {
                int tmp = factor1;
                factor1 = factor2;
                factor2 = tmp;
            }
            int rightAns = factor1 - factor2;
            if (_rand.nextInt(100) < GameConstant.ZEN_RIGHT_ANS_RATE) {
                fakeAns = rightAns;
            } else {
                fakeAns = zenInt(rightAns < GameConstant.FAKE_ANS_RANGE ? 0 : rightAns - GameConstant.FAKE_ANS_RANGE,
                        rightAns + GameConstant.FAKE_ANS_RANGE);
            }
        }

        Math math = new Math(factor1, factor2, isAdd, fakeAns);
        return math;
    }

    public int processInput(boolean ans) {
        int returnCode = GameConstant.WRONG_CODE;

        if (_currentMath.getMathResult() == ans) {
            _count--;
            returnCode = GameConstant.CORRECT_CODE;
        }
        if (_count == 0)
            return GameConstant.WIN_GAME_CODE;

        _currentMath = randomMath();
        return returnCode;
    }
}
