package com.enjin.sponge.stats;

public class StatValue {

    Object  value;
    boolean cumulative = true;

    public StatValue(int stat, boolean isRelative) {
        value = new Integer(stat);
        this.cumulative = isRelative;
    }

    public StatValue(float stat, boolean isRelative) {
        value = new Float(stat);
        this.cumulative = isRelative;
    }

    public StatValue(double stat, boolean isRelative) {
        value = new Double(stat);
        this.cumulative = isRelative;
    }

    public void setStat(int stat) {
        value = new Integer(stat);
    }

    public void setStat(float stat) {
        value = new Float(stat);
    }

    public void setStat(double stat) {
        value = new Double(stat);
    }

    public void addStat(int stat) {
        if (value instanceof Integer) {
            int ivalue = (Integer) value;
            ivalue += stat;
            value = new Integer(ivalue);
        } else if (value instanceof Float) {
            float ivalue = (Float) value;
            ivalue += stat;
            value = new Float(ivalue);
        } else if (value instanceof Double) {
            double ivalue = (Double) value;
            ivalue += stat;
            value = new Double(ivalue);
        }
    }

    public void addStat(float stat) {
        if (value instanceof Integer) {
            float ivalue = (Integer) value;
            ivalue += stat;
            value = new Float(ivalue);
        } else if (value instanceof Float) {
            float ivalue = (Float) value;
            ivalue += stat;
            value = new Float(ivalue);
        } else if (value instanceof Double) {
            double ivalue = (Double) value;
            ivalue += stat;
            value = new Double(ivalue);
        }
    }

    public void addStat(double stat) {
        if (value instanceof Integer) {
            double ivalue = (Integer) value;
            ivalue += stat;
            value = new Double(ivalue);
        } else if (value instanceof Float) {
            double ivalue = (Float) value;
            ivalue += stat;
            value = new Double(ivalue);
        } else if (value instanceof Double) {
            double ivalue = (Double) value;
            ivalue += stat;
            value = new Double(ivalue);
        }
    }

    public Object getStat() {
        return value;
    }

    public boolean isRelative() {
        return cumulative;
    }
}
