package edu.rit.se.design.callgraph.analysis;

import com.ibm.wala.fixedpoint.impl.AbstractFixedPointSolver;
import com.ibm.wala.fixedpoint.impl.Worklist;
import com.ibm.wala.ipa.callgraph.propagation.AbstractPointsToSolver;
import com.ibm.wala.ipa.callgraph.propagation.IPointsToSolver;
import com.ibm.wala.ipa.callgraph.propagation.PropagationSystem;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.MonitorUtil;

import java.lang.reflect.Field;


/**
 * A fixed-point iterative solver for performing pointer analysis on programs that uses serialization features.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class SerializationPointsToSolver extends AbstractPointsToSolver {
    private IPointsToSolver delegate;
    private AbstractSerializationHandler serializationHandler;
    private int i = 1; // to mark the iterations for this subtask related to serialization support
    private int delta = 0; // to mark how many extra elements were added by Salsa or Seneca to the worklist

    public SerializationPointsToSolver(PropagationSystem system, SSAPropagationCallGraphBuilder builder, AbstractSerializationHandler serializationHandler, IPointsToSolver delegate) {
        super(system, builder);
        this.delegate = delegate;
        this.serializationHandler = serializationHandler;
    }

    @Override
    public void solve(MonitorUtil.IProgressMonitor monitor) throws IllegalArgumentException, CancelException {
        do {
            if (monitor != null) monitor.beginTask("Points-to analysis", i);
            // ensures that all is computed normally
            this.delegate.solve(monitor);
            int before = getWorkListSize();
            // adding constraints from newly (synthetic) nodes that replaced calls to ObjectInputStream
            if (monitor != null) monitor.subTask("Serialization-related Features");
            serializationHandler.handleSerializationRelatedFeatures(monitor);
            if (monitor != null) monitor.worked(i++);
            delta += (getWorkListSize() - before);
        } while (!getSystem().emptyWorkList());

        if (monitor != null) {
            monitor.subTask("SerializationPointsToSolver::extra iterations= " + i);
            monitor.subTask("SerializationPointsToSolver::delta= " + delta);
        }
    }

    /**
     * Check the size of WALA's worklist.
     *
     * @return the size of the worklist.
     */
    private int getWorkListSize() {
        try {
            Field privateField = AbstractFixedPointSolver.class.getDeclaredField("workList");
            privateField.setAccessible(true);
            Worklist list = (Worklist) privateField.get(getSystem());
            return list.size();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
