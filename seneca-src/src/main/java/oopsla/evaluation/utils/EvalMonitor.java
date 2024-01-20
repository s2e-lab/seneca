package oopsla.evaluation.utils;

import com.ibm.wala.util.MonitorUtil;

public  class EvalMonitor implements MonitorUtil.IProgressMonitor {
    private boolean isCanceled = false;
    private int extraIterations = 0;
    private int delta = 0;

    @Override
    public void beginTask(String s, int i) {
//            System.out.println("begin task " + s + " / i = " + i);
    }

    @Override
    public void subTask(String s) {
//            System.out.println("sub task " + s);
        if (s.contains("extra iterations")) {
            String extra = s.replace("SerializationPointsToSolver::extra iterations= ", "");
            extraIterations = Integer.parseInt(extra);
        }
        if (s.contains("delta")) {
            String extra = s.replace("SerializationPointsToSolver::delta= ", "");
            delta = Integer.parseInt(extra);
        }
    }

    @Override
    public void cancel() {
//            System.out.println("cancel");
        isCanceled = true;
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }

    @Override
    public void done() {
//            System.out.println("done");
    }

    @Override
    public void worked(int i) {
//            System.out.println("worked i = " + i);
    }

    @Override
    public String getCancelMessage() {
        return "¯\\_(ツ)_/¯";
    }

    public int getExtraIterations() {
        return extraIterations;
    }

    public int getDelta() {
        return delta;
    }
}