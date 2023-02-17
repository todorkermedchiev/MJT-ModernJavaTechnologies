package bg.sofia.uni.fmi.mjt.grading.simulator.grader;

import bg.sofia.uni.fmi.mjt.grading.simulator.Assistant;
import bg.sofia.uni.fmi.mjt.grading.simulator.assignment.Assignment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class CodePostGrader implements AdminGradingAPI {
    private final Queue<Assignment> assignments;
    private final List<Assistant> assistants;
//    private final AtomicInteger submissionsCount;
    private int submissionsCount;
    private final int assistantsCount;
    private boolean isFinalized;

    public CodePostGrader(int assistantsCount) {
        if (assistantsCount < 0) {
            throw new IllegalArgumentException("The number of assistants cannot be less than 0");
        }

        this.assignments = new LinkedList<>();
        this.assistants = new ArrayList<>();

        this.assistantsCount = assistantsCount;
//        this.submissionsCount = new AtomicInteger(0);
        this.submissionsCount = 0;
        this.isFinalized = false;

        initializeAssistants();
    }

    private void initializeAssistants() {
        for (int i = 0; i < assistantsCount; ++i) {
            assistants.add(new Assistant("Assistant " + i, this));
            assistants.get(i).start();
        }
    }

    @Override
    public synchronized Assignment getAssignment() {
        while (assignments.isEmpty() && !isFinalized) {
            try {
                this.wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (assignments.isEmpty()) {
            return null;
        }
        return assignments.poll();
    }

    @Override
    public synchronized int getSubmittedAssignmentsCount() {
        return submissionsCount; // .get();
    }

    @Override
    public synchronized void finalizeGrading() {
        isFinalized = true;
        this.notifyAll();
    }

    @Override
    public synchronized List<Assistant> getAssistants() {
        return List.copyOf(assistants);
    }

    @Override
    public void submitAssignment(Assignment assignment) {
        synchronized (this) {
            if (isFinalized) {
                return;
            }
        }

        if (assignment == null) {
            throw new IllegalArgumentException("Submitted assignment cannot be null");
        }

//        submissionsCount.incrementAndGet();

        synchronized (this) {
            ++submissionsCount;
            assignments.add(assignment);
            this.notifyAll();
        }
    }
}
