package org.jvnet.ixent.algorithms.graphics.engine.npr.watercolor;

import java.util.HashMap;
import java.util.Map;

import org.jvnet.ixent.math.MathConstants;

/**
 * A class that allows efficient manipulation of a number of pigments with
 * different concentrations.
 *
 * @author Kirill Grouchnikov
 */
public class PigmentList {
    private Map<Pigment, Double> concentrations;

    /**
     * Default constructor. Creates an empty list of pigments.
     */
    public PigmentList() {
        this.concentrations = null;
    }

    /**
     * Set concentration of specified pigment to a specified value. The previous
     * (if any) concentration of this pigment is discarded as a result of this
     * action.
     *
     * @param pigment       specified pigment
     * @param concentration its new concentration
     */
    public void setPigment(Pigment pigment, double concentration) {
        if (this.concentrations == null) {
            this.concentrations = new HashMap<Pigment, Double>();
        }
        if (concentration < MathConstants.EPS_BIG) {
            this.concentrations.remove(pigment);
        }
        else {
            this.concentrations.put(pigment, concentration);
        }
    }

    /**
     * Change the concentration of the specified pigment by the specified value.
     * If this pigment has no previous concentration, the specified value will
     * be taken as the current concentration of this pigment.
     *
     * @param pigment            the specified pigment
     * @param concentrationDelta concentration change of this pigment
     */
    public void addPigment(Pigment pigment, double concentrationDelta) {
        if (concentrationDelta < MathConstants.EPS_BIG) {
            return;
        }
        double oldConcentration = this.getPigmentConcentration(pigment);
        this.setPigment(pigment, oldConcentration + concentrationDelta);
    }

    /**
     * Returns the concentration of specified pigment. If no concentration was
     * ever defined for this pigment, 0.0 is returned.
     *
     * @param pigment the specified pigment
     * @return it concentration
     */
    public double getPigmentConcentration(Pigment pigment) {
        if (this.concentrations == null) {
            return 0.0;
        }
        if (this.concentrations.containsKey(pigment)) {
            return this.concentrations.get(pigment);
        }
        return 0.0;
    }

    /**
     * Add all pigments from the specified list to this list. The pigments
     * already present change their concentrations accordingly, the pigments
     * that exist only in the second list are added to this list
     *
     * @param pigmentList the second pigment list
     */
    public void combine(PigmentList pigmentList) {
        if (pigmentList == null) {
            return;
        }
        if (pigmentList.concentrations == null) {
            return;
        }
        for (Pigment currPigment : pigmentList.concentrations.keySet()) {
            double currConcentration = pigmentList.concentrations.get(
                    currPigment);
            this.addPigment(currPigment, currConcentration);
        }
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("pigment list (");
        int count = (this.concentrations == null) ?
                0 : this.concentrations.size();
        sb.append(count);
        sb.append(" pigments)");
        if (count != 0) {
            sb.append(":\n");
            for (Pigment currPigment : this.concentrations.keySet()) {
                double currConcentration = this.concentrations.get(currPigment);
                sb.append(" " + currPigment.name() + " - ");
                sb.append(currConcentration + "\n");
            }
        }

        return sb.toString();
    }


}
