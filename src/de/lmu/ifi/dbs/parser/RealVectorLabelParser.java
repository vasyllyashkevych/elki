package de.lmu.ifi.dbs.parser;

import de.lmu.ifi.dbs.data.ClassLabel;
import de.lmu.ifi.dbs.data.DoubleVector;
import de.lmu.ifi.dbs.data.FloatVector;
import de.lmu.ifi.dbs.data.RealVector;
import de.lmu.ifi.dbs.data.SimpleClassLabel;
import de.lmu.ifi.dbs.database.connection.AbstractDatabaseConnection;
import de.lmu.ifi.dbs.utilities.Util;
import de.lmu.ifi.dbs.utilities.optionhandling.ClassParameter;
import de.lmu.ifi.dbs.utilities.optionhandling.Flag;
import de.lmu.ifi.dbs.utilities.optionhandling.IntParameter;
import de.lmu.ifi.dbs.utilities.optionhandling.ParameterException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a parser for parsing one point per line, attributes separated by
 * whitespace. The parser provides a parameter for parsing the real values as
 * doubles (default) (resulting in a {@link ParsingResult} of
 * {@link DoubleVector}s) or float (resulting in a {@link ParsingResult} of
 * {@link FloatVector}s).<p/>
 * 
 * Several labels may be given per point. A label must not be parseable as
 * double (or float). Lines starting with &quot;#&quot; will be ignored.
 * 
 * @author Arthur Zimek
 */
public class RealVectorLabelParser<V extends RealVector<V, ?>> extends AbstractParser<V>
{

    /**
     * Option string for parameter float.
     */
    public static final String FLOAT_F = "float";

    /**
     * Description for parameter float.
     */
    public static final String FLOAT_D = "flag to specify parsing the real values as floats (default is double)";

    /**
     * If true, the real values are parsed as floats.
     */
    protected boolean parseFloat;

    public static final String CLASS_LABEL_INDEX_P = "numericalClassLabelIndex";

    public static final String CLASS_LABEL_INDEX_D = "(optional) index of a class label (may be numeric), counting whitespace separated entries in a line starting with 0 - the corresponding entry will be treated as a label. To actually set this label as class label, use also the parametrization of "+AbstractDatabaseConnection.class.getCanonicalName()+" -"+AbstractDatabaseConnection.CLASS_LABEL_INDEX_P+" -"+AbstractDatabaseConnection.CLASS_LABEL_CLASS_P;

    public static final IntParameter CLASS_LABEL_INDEX_PARAM = new IntParameter(CLASS_LABEL_INDEX_P, CLASS_LABEL_INDEX_D);
    static
    {
        CLASS_LABEL_INDEX_PARAM.setDefaultValue(-1);
        CLASS_LABEL_INDEX_PARAM.setOptional(true);
    }

    private int classLabelIndex;
    
    
    /**
     * Provides a parser for parsing one point per line, attributes separated by
     * whitespace. <p/> Several labels may be given per point. A label must not
     * be parseable as double (or float). Lines starting with &quot;#&quot; will
     * be ignored.
     */
    public RealVectorLabelParser()
    {
        super();
        debug = true;
        optionHandler.put(FLOAT_F, new Flag(FLOAT_F, FLOAT_D));
        optionHandler.put(CLASS_LABEL_INDEX_PARAM);
    }

    /**
     * @see Parser#parse(java.io.InputStream)
     */
    public ParsingResult<V> parse(InputStream in)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        int lineNumber = 1;
        int dimensionality = -1;
        List<ObjectAndLabels<V>> objectAndLabelsList = new ArrayList<ObjectAndLabels<V>>();
        try
        {
            for(String line; (line = reader.readLine()) != null; lineNumber++)
            {
                if(!line.startsWith(COMMENT) && line.length() > 0)
                {
                    String[] entries = WHITESPACE_PATTERN.split(line);
                    List<Double> attributes = new ArrayList<Double>();
                    List<String> labels = new ArrayList<String>();
                    for(int i = 0; i < entries.length; i++)
                    {
                        if(i != classLabelIndex)
                        {
                            try
                            {
                                Double attribute = Double.valueOf(entries[i]);
                                attributes.add(attribute);
                            }
                            catch(NumberFormatException e)
                            {
                                labels.add(entries[i]);
                            }
                        }
                        else
                        {
                            labels.add(entries[i]);
                        }
                    }

                    if(dimensionality < 0)
                    {
                        dimensionality = attributes.size();
                    }
                    else if(dimensionality != attributes.size())
                    {
                        throw new IllegalArgumentException("Differing dimensionality in line " + lineNumber + ":" + attributes.size() + " != " + dimensionality);
                    }

                    V featureVector;
                    if(parseFloat)
                    {
                        featureVector = (V) new FloatVector(Util.convertToFloat(attributes));
                    }
                    else
                    {
                        featureVector = (V) new DoubleVector(attributes);
                    }

                    ObjectAndLabels<V> objectAndLabel = new ObjectAndLabels<V>(featureVector, labels);
                    objectAndLabelsList.add(objectAndLabel);
                }
            }
        }
        catch(IOException e)
        {
            throw new IllegalArgumentException("Error while parsing line " + lineNumber + ".");
        }

        return new ParsingResult<V>(objectAndLabelsList);
    }

    /**
     * @see de.lmu.ifi.dbs.utilities.optionhandling.Parameterizable#description()
     */
    @Override
    public String description()
    {
        StringBuffer description = new StringBuffer();
        description.append(RealVectorLabelParser.class.getName());
        description.append(" expects following format of parsed lines:\n");
        description.append("A single line provides a single point. Attributes are separated by whitespace (");
        description.append(WHITESPACE_PATTERN.pattern() + "). ");
        description.append("If parameter " + FLOAT_F + " is set, the real values will be parsed as floats (resulting in a set of FloatVectors), " + "otherwise the real values will be parsed as as doubles (resulting in a set of DoubleVectors -- default).");
        description.append("Any substring not containing whitespace is tried to be read as double (or float). " + "If this fails, it will be appended to a label. (Thus, any label must not be parseable " + "as double nor as float.) Empty lines and lines beginning with \"");
        description.append(COMMENT);
        description.append("\" will be ignored. If any point differs in its dimensionality from other points, " + "the parse method will fail with an Exception.\n");

        return usage(description.toString());
    }

    /**
     * @see de.lmu.ifi.dbs.utilities.optionhandling.Parameterizable#setParameters(String[])
     */
    @Override
    public String[] setParameters(String[] args) throws ParameterException
    {
        String[] remainingParams = super.setParameters(args);
        parseFloat = optionHandler.isSet(FLOAT_F);
        if(optionHandler.isSet(CLASS_LABEL_INDEX_PARAM))
        {
            classLabelIndex = optionHandler.getParameterValue(CLASS_LABEL_INDEX_PARAM);
        }
        else
        {
            classLabelIndex = CLASS_LABEL_INDEX_PARAM.getDefaultValue();
        }
        setParameters(args, remainingParams);
        return remainingParams;
    }
}
