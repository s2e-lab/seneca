package edu.rit.se.design.callgraph.analysis;

/**
 * Enumeration that selects which propagation algorithm to use.
 *
 * @author Joanna C. S. Santos
 * @author Reese A. Jones
 */
public class PointerAnalysisPolicy {
    public PolicyType policyType;
    /**
     * a value for the n or x in nCFA, ZeroXCFA, ZeroXContainerCFA
     */
    public int policyNumber;
    /**
     * @param policyNumber the bound n in the algorithms: n-CFA, 0-n-CFA, 0-n-Container-CFA.
     * @param policyType   the pointer analysis method (n-CFA, 0-n-CFA, 0-n-Container-CFA)
     */
    public PointerAnalysisPolicy(PolicyType policyType, int policyNumber) {
        this.policyType = policyType;
        this.policyNumber = policyNumber;
    }

    @Override
    public String toString() {
        switch (policyType) {
            case nCFA:
                return String.format("%d-CFA", policyNumber);
            case ZeroXCFA:
                return String.format("0-%d-CFA", policyNumber);
            case ZeroXContainerCFA:
                return String.format("0-%d-Container-CFA", policyNumber);
        }
        throw new IllegalArgumentException("Unknown value " + this);
    }


    public enum PolicyType {
        nCFA,
        ZeroXCFA,
        ZeroXContainerCFA;
    }
}

