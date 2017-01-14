package org.jvnet.ixent.math.filters;

import java.util.LinkedList;
import java.util.List;

public class FilterBank {
    public static Filter[] getFilters(double sigmaStart, double sigmaDelta,
                                      int countRegular, int countDifference, int countOrientations,
                                      int countScales) {

        List<Filter> filterList = new LinkedList<Filter>();

        // create regular gaussians
        for (int i = 0; i < countRegular; i++) {
            filterList.add(FilterFactory.getGaussianRegular(sigmaStart + i
                    * sigmaDelta));
        }

        // difference of gaussians
        for (int i = 0; i < countDifference; i++) {
            filterList.add(FilterFactory.getDOOGFilter(sigmaStart + i
                    * sigmaDelta, 2.0));
        }

        // elongated G2-G1
        double angleDelta = 180.0 / countOrientations;
        for (int or = 0; or < countOrientations; or++) {
            for (int sc = 0; sc < countScales; sc++) {
                double theta = or * angleDelta;
                double sigma = sigmaStart + sc * sigmaDelta;
                filterList.add(FilterFactory.getG1Filter(sigma, 3.0, theta));
                filterList.add(FilterFactory.getG2Filter(sigma, 3.0, theta));
            }
        }

        Filter[] result = new Filter[filterList.size()];
        int count = 0;
        for (Filter filter : filterList) {
            result[count++] = filter;
        }
        return result;
    }
}