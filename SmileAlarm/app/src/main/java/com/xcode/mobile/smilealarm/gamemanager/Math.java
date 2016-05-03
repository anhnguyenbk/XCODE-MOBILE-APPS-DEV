package com.xcode.mobile.smilealarm.gamemanager;

public class Math {
    private final int _factor_1;
    private final int _factor_2;
    private final boolean _isAddOperation;
    private final int _fakeResult;

    public Math(int _factor_1, int _factor_2, boolean _isAddOperation,
                int _fakeResult) {
        this._factor_1 = _factor_1;
        this._factor_2 = _factor_2;
        this._isAddOperation = _isAddOperation;
        this._fakeResult = _fakeResult;
    }

    public String get_factor_1() {
        return String.valueOf(_factor_1);
    }

    public String get_factor_2() {
        return String.valueOf(_factor_2);
    }

    public boolean isAddOperation() {
        return _isAddOperation;
    }

    public String get_fakeResult() {
        return String.valueOf(_fakeResult);
    }

    public boolean getMathResult() {
        if (_isAddOperation)
            return (_factor_1 + _factor_2 == _fakeResult);
        else
            return (_factor_1 - _factor_2 == _fakeResult);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(get_factor_1());
        sb.append(_isAddOperation ? "\t+\t" : "\t-\t");
        sb.append(get_factor_2());
        sb.append("\t=\t");
        sb.append(get_fakeResult());
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Math))
            return false;

        Math cast = (Math) obj;
        return get_factor_1().equals(cast.get_factor_1()) && get_factor_2().equals(cast.get_factor_2())
                && isAddOperation() == cast.isAddOperation() && get_fakeResult().equals(cast.get_fakeResult());
    }

    @Override
    public int hashCode() {
        return _factor_1 * _factor_2 * _fakeResult;
    }
}
