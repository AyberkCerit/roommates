package com.roommates.study;

public enum StudySessionStatus {
    PLANNED,
    RUNNING,
    FINISHED;

    public boolean canTransitionTo(StudySessionStatus sonraki) {
        if (this == sonraki) {
            return true;
        }
        if (this == PLANNED && sonraki == RUNNING) {
            return true;
        }
        if (this == RUNNING && sonraki == FINISHED) {
            return true;
        }
        return false;
    }
}
