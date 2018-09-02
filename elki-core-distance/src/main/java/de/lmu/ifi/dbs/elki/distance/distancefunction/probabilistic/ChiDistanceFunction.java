/*
 * This file is part of ELKI:
 * Environment for Developing KDD-Applications Supported by Index-Structures
 *
 * Copyright (C) 2018
 * ELKI Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.lmu.ifi.dbs.elki.distance.distancefunction.probabilistic;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.spatial.SpatialComparable;
import de.lmu.ifi.dbs.elki.data.type.SimpleTypeInformation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.AbstractNumberVectorDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.NumberVectorDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.SpatialPrimitiveDistanceFunction;
import de.lmu.ifi.dbs.elki.utilities.Alias;
import de.lmu.ifi.dbs.elki.utilities.Priority;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import net.jafama.FastMath;

/**
 * χ distance function, symmetric version.
 * This is the square root of the {@link ChiSquaredDistanceFunction}, and can
 * serve as a fast approximation to
 * {@link SqrtJensenShannonDivergenceDistanceFunction}.
 * <p>
 * This implementation assumes \(\sum_i x_i=\sum_i y_i\), and is defined as:
 * \[ \chi(\vec{x},\vec{y}):= \sqrt{2 \sum\nolimits_i
 * \tfrac{(x_i-x_i)^2}{x_i+y_i}} \]
 * <p>
 * Reference:
 * <p>
 * J. Puzicha, J. M. Buhmann, Y. Rubner, C. Tomasi<br>
 * Empirical evaluation of dissimilarity measures for color and texture<br>
 * Proc. 7th IEEE International Conference on Computer Vision
 * <p>
 * D. M. Endres, J. E. Schindelin<br>
 * A new metric for probability distributions<br>
 * IEEE Transactions on Information Theory, 49(7)
 *
 * @author Erich Schubert
 */
@Alias("chi")
@Priority(Priority.IMPORTANT)
@Reference(authors = "J. Puzicha, J. M. Buhmann, Y. Rubner, C. Tomasi", //
    title = "Empirical evaluation of dissimilarity measures for color and texture", //
    booktitle = "Proc. 7th IEEE International Conference on Computer Vision", //
    url = "https://doi.org/10.1109/ICCV.1999.790412", //
    bibkey = "DBLP:conf/iccv/PuzichaRTB99")
@Reference(authors = "D. M. Endres, J. E. Schindelin", //
    title = "A new metric for probability distributions", //
    booktitle = "IEEE Transactions on Information Theory, 49(7)", //
    url = "https://doi.org/10.1109/TIT.2003.813506", //
    bibkey = "DBLP:journals/tit/EndresS03")
public class ChiDistanceFunction extends AbstractNumberVectorDistanceFunction implements SpatialPrimitiveDistanceFunction<NumberVector>, NumberVectorDistanceFunction<NumberVector> {
  /**
   * Static instance. Use this!
   */
  public static final ChiDistanceFunction STATIC = new ChiDistanceFunction();

  /**
   * Constructor for the Chi-Squared distance function.
   * 
   * @deprecated Use static instance!
   */
  @Deprecated
  public ChiDistanceFunction() {
    super();
  }

  @Override
  public double distance(NumberVector v1, NumberVector v2) {
    final int dim1 = v1.getDimensionality(), dim2 = v2.getDimensionality();
    final int mindim = (dim1 < dim2) ? dim1 : dim2;
    double agg = 0.;
    for(int d = 0; d < mindim; d++) {
      final double xd = v1.doubleValue(d), yd = v2.doubleValue(d);
      final double di = xd - yd;
      final double si = xd + yd;
      if(!(si > 0. || si < 0.) || !(di > 0. || di < 0.)) {
        continue;
      }
      agg += di * di / si;
    }
    for(int d = mindim; d < dim1; d++) {
      final double xd = v1.doubleValue(d);
      if(xd != xd) { /* avoid NaNs */
        continue;
      }
      agg += xd;
    }
    for(int d = mindim; d < dim2; d++) {
      final double xd = v2.doubleValue(d);
      if(xd != xd) { /* avoid NaNs */
        continue;
      }
      agg += xd;
    }
    return FastMath.sqrt(2. * agg);
  }

  @Override
  public double minDist(SpatialComparable mbr1, SpatialComparable mbr2) {
    final int dim1 = mbr1.getDimensionality(), dim2 = mbr2.getDimensionality();
    final int mindim = (dim1 < dim2) ? dim1 : dim2;
    double agg = 0.;
    for(int d = 0; d < mindim; d++) {
      final double min1 = mbr1.getMin(d), max1 = mbr1.getMax(d);
      final double min2 = mbr2.getMin(d), max2 = mbr2.getMax(d);
      final double diff; // Minimum difference
      if(max1 < min2) {
        diff = min2 - max1;
      }
      else if(max2 < min1) {
        diff = max2 - min1;
      }
      else {
        continue; // 0.
      }
      final double si = max1 + max2; // Maximum sum
      if(!(si > 0. || si < 0.) || !(diff > 0. || diff < 0.)) {
        continue;
      }
      agg += diff * diff / si;
    }
    for(int d = mindim; d < dim1; d++) {
      final double min1 = mbr1.getMin(d);
      if(min1 > 0.) {
        agg += min1;
      }
      else {
        final double max1 = mbr1.getMax(d);
        if(max1 < 0.) { // Should never happen.
          agg += max1;
        }
      }
    }
    for(int d = mindim; d < dim2; d++) {
      final double min2 = mbr2.getMin(d);
      if(min2 > 0.) {
        agg += min2;
      }
      else {
        final double max2 = mbr2.getMax(d);
        if(max2 < 0.) { // Should never happen.
          agg += max2;
        }
      }
    }
    return FastMath.sqrt(2. * agg);
  }

  @Override
  public boolean isMetric() {
    return true;
  }

  @Override
  public String toString() {
    return "ChiDistance";
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this || (obj != null && this.getClass().equals(obj.getClass()));
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public SimpleTypeInformation<? super NumberVector> getInputTypeRestriction() {
    return NumberVector.VARIABLE_LENGTH;
  }

  /**
   * Parameterization class, using the static instance.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.exclude
   */
  public static class Parameterizer extends AbstractParameterizer {
    @Override
    protected ChiDistanceFunction makeInstance() {
      return STATIC;
    }
  }
}