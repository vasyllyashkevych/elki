package de.lmu.ifi.dbs.elki.distance.distancefunction;

import de.lmu.ifi.dbs.elki.data.FeatureVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.preprocessing.KnnQueryBasedHiCOPreprocessor;
import de.lmu.ifi.dbs.elki.preprocessing.Preprocessor;
import de.lmu.ifi.dbs.elki.preprocessing.PreprocessorClient;
import de.lmu.ifi.dbs.elki.preprocessing.PreprocessorHandler;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;

/**
 * Abstract super class for locally weighted distance functions using a
 * preprocessor to compute the local weight matrix.
 * 
 * @author Elke Achtert
 * @param <O> the type of DatabaseObject to compute the distances in between
 * @param <P> preprocessor type
 */
public abstract class AbstractLocallyWeightedDistanceFunction<O extends FeatureVector<O, ?>, P extends Preprocessor<O>> extends AbstractDoubleDistanceFunction<O> implements PreprocessorClient<P, O> {
  /**
   * The handler class for the preprocessor.
   */
  private PreprocessorHandler<O, P> preprocessorHandler;

  /**
   * Provides an abstract locally weighted distance function.
   */
  protected AbstractLocallyWeightedDistanceFunction(Parameterization config) {
    super();
    preprocessorHandler = new PreprocessorHandler<O, P>(config, this);
  }

  @Override
  public void setDatabase(Database<O> database, boolean verbose, boolean time) {
    super.setDatabase(database, verbose, time);
    preprocessorHandler.runPreprocessor(database, verbose, time);
  }

  @Override
  public String shortDescription() {
    return "Locally weighted distance function. Pattern for defining a range: \"" + requiredInputPattern() + "\".\n";
  }

  /**
   * @return the name of the default preprocessor, which is
   *         {@link de.lmu.ifi.dbs.elki.preprocessing.KnnQueryBasedHiCOPreprocessor}
   */
  @Override
  public Class<?> getDefaultPreprocessorClass() {
    return KnnQueryBasedHiCOPreprocessor.class;
  }

  public String getPreprocessorDescription() {
    return "Preprocessor class to determine the correlation dimension of each object.";
  }

  /**
   * @return the super class for the preprocessor, which is
   *         {@link de.lmu.ifi.dbs.elki.preprocessing.Preprocessor}
   */
  public Class<P> getPreprocessorSuperClass() {
    return ClassGenericsUtil.uglyCastIntoSubclass(Preprocessor.class);
  }
}
