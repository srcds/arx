package org.deidentifier.arx;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PopulationUniqueness;
import org.deidentifier.arx.io.CSVDataInput;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness.PopulationUniquenessModel;

import cern.colt.Arrays;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXPopulationModel.Region;

/**
 * Setup class for ShadowModel MIA benchmark
 * 
 * @author Fabian Prasser
 * @author Thierry Meurers
 *
 */
public class ShadowModelSetup {
    
    /**
     * Interface for anonymization methods
     * 
     * @author Fabian Prasser
     */
    public interface AnonymizationMethod {
        
        public DataHandle anonymize(Data handle);
        public DataHandle anonymize(Data handle, double suppressionLimit);
    }
    
    public static AnonymizationMethod IDENTITY_ANONYMIZATION = new AnonymizationMethod() {
        
        @Override
        public DataHandle anonymize(Data data) {
            return anonymize(data, 0d);
        }
        
        @Override
        public DataHandle anonymize(Data data, double suppressionLimit) {
            
            // Prepare
            ARXConfiguration config = ARXConfiguration.create();
            config.addPrivacyModel(new KAnonymity(1));
            config.setSuppressionLimit(suppressionLimit);
            config.setAlgorithm(AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP);
            config.setHeuristicSearchStepLimit(1);
            
            // Anonymize
            ARXAnonymizer anonymizer = new ARXAnonymizer();
            try {
                ARXResult result = anonymizer.anonymize(data, config);
                // TODO remove
                //printTransformation(result);
                
                return result.getOutput();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        
        @Override
        public String toString() {
            return "Identity";
        }
    };

    public static AnonymizationMethod K2_ANONYMIZATION = new AnonymizationMethod() {
        
        @Override
        public DataHandle anonymize(Data data) {
            return anonymize(data, 0d);
        }
        
        @Override
        public DataHandle anonymize(Data data, double suppressionLimit) {

            // Prepare
            ARXConfiguration config = ARXConfiguration.create();
            config.addPrivacyModel(new KAnonymity(2));
            config.setSuppressionLimit(suppressionLimit);
            config.setAlgorithm(AnonymizationAlgorithm.BEST_EFFORT_TOP_DOWN);
            config.setHeuristicSearchStepLimit(1000);
            
            // Anonymize
            ARXAnonymizer anonymizer = new ARXAnonymizer();
            try {
                ARXResult result = anonymizer.anonymize(data, config);
                // TODO remove
                //printTransformation(result);
                
                return result.getOutput();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        
        @Override
        public String toString() {
            return "2-Anonymity";
        }
    };
    
    public static AnonymizationMethod K5_ANONYMIZATION = new AnonymizationMethod() {
        
        @Override
        public DataHandle anonymize(Data data) {
            return anonymize(data, 0d);
        }
        
        @Override
        public DataHandle anonymize(Data data, double suppressionLimit) {

            // Prepare
            ARXConfiguration config = ARXConfiguration.create();
            config.addPrivacyModel(new KAnonymity(5));
            config.setSuppressionLimit(suppressionLimit);
            
            // Anonymize
            ARXAnonymizer anonymizer = new ARXAnonymizer();
            try {
                return anonymizer.anonymize(data, config).getOutput();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        
        @Override
        public String toString() {
            return "5-Anonymity";
        }
    };
    
    public static AnonymizationMethod K10_ANONYMIZATION = new AnonymizationMethod() {
        
        @Override
        public DataHandle anonymize(Data data) {
            return anonymize(data, 0d);
        }
        
        @Override
        public DataHandle anonymize(Data data, double suppressionLimit) {

            // Prepare
            ARXConfiguration config = ARXConfiguration.create();
            config.addPrivacyModel(new KAnonymity(10));
            config.setSuppressionLimit(suppressionLimit);
            
            // Anonymize
            ARXAnonymizer anonymizer = new ARXAnonymizer();
            try {
                return anonymizer.anonymize(data, config).getOutput();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        
        @Override
        public String toString() {
            return "10-Anonymity";
        }
    };
    

    
    public static AnonymizationMethod PITMAN_ANONYMIZATION = new AnonymizationMethod() {
        
        @Override
        public DataHandle anonymize(Data data) {
            return anonymize(data, 0d);
        }
        
        @Override
        public DataHandle anonymize(Data data, double suppressionLimit) {

            // Prepare
            ARXConfiguration config = ARXConfiguration.create();
            config.addPrivacyModel(new PopulationUniqueness(0.01, PopulationUniquenessModel.PITMAN, ARXPopulationModel.create(Region.USA)));
            config.setSuppressionLimit(suppressionLimit);
            config.setAlgorithm(AnonymizationAlgorithm.BEST_EFFORT_TOP_DOWN);
            config.setHeuristicSearchStepLimit(1000);
            
            // Anonymize
            ARXAnonymizer anonymizer = new ARXAnonymizer();
            try {
                ARXResult result = anonymizer.anonymize(data, config);
                // TODO remove
                //printTransformation(result);
                
                return result.getOutput();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        
        @Override
        public String toString() {
            return "Pitman01";
        }
    };
    
    
    private static void printTransformation(ARXResult result) {
        ARXNode node = result.getGlobalOptimum();
        int[] transformation = node.getTransformation();
        System.out.println(Arrays.toString(transformation));
    }

    
    /**
     * Datasets
     */
    public static enum BenchmarkDataset {
        TEXAS_10, TEXAS, TEXAS_CRAFTED, ADULT, ADULT_FULL, ADULT_FULL_CRAFTED
    }
    
    /**
     * Configures and returns the dataset.
     * 
     * @param dataset
     * @param tm
     * @param qis
     * @return
     * @throws IOException
     */
    public static Data getData(BenchmarkDataset dataset) throws IOException {

        Iterator<String[]> cfgIter = loadDataConfig(dataset).iterator(false);

        List<String> attributeNames = new ArrayList<String>();
        List<String> attributeTypes = new ArrayList<String>();
        List<String> attributeInclude = new ArrayList<String>();
        List<String> attributeIsQI = new ArrayList<>();

        while (cfgIter.hasNext()) {
            String[] line = cfgIter.next();
            attributeNames.add(line[0]);
            attributeTypes.add(line[1]);
            attributeInclude.add(line[2]);
            attributeIsQI.add(line[3]);
        }

        Data data = loadData(dataset);

        for (int i = 0; i < attributeNames.size(); i++) {
            if (attributeInclude.get(i).equals("TRUE")) {
                String attributeName = attributeNames.get(i);
                switch (attributeTypes.get(i)) {

                case "categorical":
                    data.getDefinition().setDataType(attributeName, DataType.STRING);
                    if (attributeIsQI.get(i).equals("TRUE")) {
                        // Set hierarchy
                        data.getDefinition().setAttributeType(attributeName, loadHierarchy(dataset, attributeName));
                    } else {
                        data.getDefinition().setAttributeType(attributeName, AttributeType.INSENSITIVE_ATTRIBUTE);
                    }
                    break;
                case "continuous":
                    data.getDefinition().setDataType(attributeNames.get(i),DataType.createDecimal("#.#", Locale.US));
                    if (attributeIsQI.get(i).equals("TRUE")) {
                        // Set aggregation function
                        data.getDefinition().setAttributeType(attributeName, loadHierarchy(dataset, attributeName));
                        data.getDefinition().setMicroAggregationFunction(attributeName, MicroAggregationFunction.createArithmeticMean(), true);
                    } else {
                        data.getDefinition().setAttributeType(attributeName, AttributeType.INSENSITIVE_ATTRIBUTE);
                    }
                    break;
                case "ordinal":
                    // TODO
                default:
                    throw new RuntimeException("Invalid datatype");
                }

            }
        }
        return data;
    }
    
    /**
     * Returns a dataset
     * @param dataset
     * @return
     * @throws IOException
     */
    public static Data loadData(BenchmarkDataset dataset) throws IOException {
        String filename = null;
        switch (dataset) {

        case ADULT:
            filename = "data/adult.csv";
            break;
        case ADULT_FULL:
            filename = "data_new/adult_full.csv";
            break;
        case ADULT_FULL_CRAFTED:
            filename = "data_new/adult_full_crafted.csv";
            break;
        case TEXAS_10:
            filename = "data/texas_10.csv";
            break;
        case TEXAS:
            filename = "data_new/texas.csv";
            break;
        case TEXAS_CRAFTED:
            filename = "data_new/texas_crafted.csv";
            break;
        default:
            throw new RuntimeException("Invalid dataset");
        }
        return Data.create(filename, Charset.defaultCharset(), ';');
    }
    
    /**
     * Returns handle for config file
     * 
     * @param dataset
     * @return
     * @throws IOException
     */
    public static CSVDataInput loadDataConfig(BenchmarkDataset dataset) throws IOException {
        String filename = null;
        switch (dataset) {
        case ADULT:
            filename = "data/adult.cfg";
            break;
        case ADULT_FULL:
        case ADULT_FULL_CRAFTED:
            filename = "data_new/adult_full.cfg";
            break;
        case TEXAS_10:
            filename = "data/texas_10.cfg";
            break;
        case TEXAS:
        case TEXAS_CRAFTED:
            filename = "data_new/texas_NHS.cfg";
            break;
        default:
            throw new RuntimeException("Invalid dataset");
        }
        return new CSVDataInput(filename, Charset.defaultCharset(), ';');
    }
    
    /**
     * Returns the generalization hierarchy for the dataset and attribute
     * @param dataset
     * @param attribute
     * @return
     * @throws IOException
    */
    public static Hierarchy loadHierarchy(BenchmarkDataset dataset, String attribute) throws IOException {
        switch (dataset) {
        case ADULT:
            return Hierarchy.create("data/adult_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case ADULT_FULL:
        case ADULT_FULL_CRAFTED:
            return Hierarchy.create("data_new/adult_full_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ',');
        case TEXAS_10:
            return Hierarchy.create("data/texas_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case TEXAS:
        case TEXAS_CRAFTED:
            return Hierarchy.create("data_new/texas_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        default:
            throw new IllegalArgumentException("Unknown dataset");
        }
    }
      
}
