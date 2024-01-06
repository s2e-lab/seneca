package edu.rit.se.design.dodo.utils.wala;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to load method signatures from a CSV file (entrypoints, sinks, sources).
 *
 * @author Joanna C. S. Santos <jds5109@rit.edu>
 */
public class MethodDescriptorsLoader {

    /**
     * Indicates whether a given method belongs to a class (abstract or not) or interface
     */
    public enum ClassType {CLASS, INTERFACE}


    // character used to separate lists within cells in the CSV file
    private static final String SEPARATOR = ";";

    private static final Pattern PATTERN_DANGEROUS_DATA = Pattern.compile("([0-9]+)\\.?([_$a-z][\\w$]*)?");

    // applicable to all CSV files
    private static final int IDX_MEMBER_OF = 0;
    private static final int IDX_METHOD_NAME = 1;
    private static final int IDX_PARAMS_LIST = 2;
    private static final int IDX_RETURN_TYPE = 3;

    // entrypoints only
    private static final int IDX_CLASS_TYPE = 4;

    // sinks only
    private static final int IDX_DANGEROUS_RANGE = 4;

    /**
     * It loads a CSV file containing method signatures and then create a list of method descriptors.
     *
     * @param csv       path to the CSV file containing method signatures.
     * @param hasHeader true if the CSV file has a header row, false otherwise
     * @return a list of {@link Descriptor} objects.
     * @throws FileNotFoundException in case the CSV file was not found.
     */
    public static List<EntrypointDescriptor> loadEntrypointsFromCsv(File csv, boolean hasHeader) throws IOException {
        List<EntrypointDescriptor> descriptors = new ArrayList<>();
        CSVReader reader = new CSVReader(new FileReader(csv));

        if (hasHeader) {
            try {
                reader.readNext(); // ignores file header
            } catch (CsvValidationException e) {
                throw new IOException(e);
            }
        }
        // reads each row
        reader.forEach(row -> {
            String memberOf = row[IDX_MEMBER_OF];
            String methodName = row[IDX_METHOD_NAME];
            Descriptor desc = Descriptor.findOrCreateUTF8(String.format("(%s)%s", row[IDX_PARAMS_LIST], row[IDX_RETURN_TYPE]));
            ClassType classType = ClassType.valueOf(row[IDX_CLASS_TYPE]);
            descriptors.add(new EntrypointDescriptor(memberOf, methodName, desc, classType));
        });

        return descriptors;
    }

    /**
     * It loads a CSV file containing method signatures for sinks and then create a list of sink descriptors.
     *
     * @param csv       path to the CSV file containing method signatures.
     * @param hasHeader true if the CSV file has a header row, false otherwise
     * @return a list of {@link Descriptor} objects.
     * @throws FileNotFoundException in case the CSV file was not found.
     */
    public static List<SinkDescriptor> loadSinksFromCsv(File csv, boolean hasHeader) throws IOException {
        List<SinkDescriptor> descriptors = new ArrayList<>();

        CSVReader reader = new CSVReader(new FileReader(csv));

        if (hasHeader) {
            try {
                reader.readNext(); // ignores file header
            } catch (CsvValidationException e) {
                throw new IOException(e);
            }
        }
        // reads
        reader.forEach((String[] row) -> {
            String memberOf = row[IDX_MEMBER_OF];
            String methodName = row[IDX_METHOD_NAME];
            String descriptor = String.format("(%s)%s", row[IDX_PARAMS_LIST], row[IDX_RETURN_TYPE]);
            String[] byteRangeStr = row[IDX_DANGEROUS_RANGE].trim().split(SEPARATOR);
            int[] byteRange = new int[byteRangeStr.length];
            String[] fieldsNames = new String[byteRangeStr.length];
            for (int i = 0; i < byteRangeStr.length; i++) {
                Matcher matcher = PATTERN_DANGEROUS_DATA.matcher(byteRangeStr[i]);
                if (matcher.find()) {
                    byteRange[i] = Integer.valueOf(matcher.group(1));
                    fieldsNames[i] = matcher.groupCount() > 1 ? matcher.group(2) : null;
                }
            }
            TypeReference typeRef = TypeReference.findOrCreate(ClassLoaderReference.Application, memberOf);
            MethodReference methodRef = MethodReference.findOrCreate(typeRef, methodName, descriptor);
            SinkDescriptor methodSignature = new SinkDescriptor(methodRef, byteRange, fieldsNames);
            descriptors.add(methodSignature);
        });

        return descriptors;
    }
}
