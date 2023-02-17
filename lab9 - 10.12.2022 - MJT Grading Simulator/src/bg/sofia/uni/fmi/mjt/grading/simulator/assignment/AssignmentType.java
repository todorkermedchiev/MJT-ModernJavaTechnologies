package bg.sofia.uni.fmi.mjt.grading.simulator.assignment;

import java.util.Random;

public enum AssignmentType {
    LAB(20), PLAYGROUND(40), HOMEWORK(80), PROJECT(120);

    private final int gradingTime;
    private static final Random RAND = new Random();

    AssignmentType(int gradingTime) {
        this.gradingTime = gradingTime;
    }

    public int getGradingTime() {
        return gradingTime;
    }

    public static AssignmentType randomType()  {
        AssignmentType[] types = values();
        return types[RAND.nextInt(types.length)];
    }
}