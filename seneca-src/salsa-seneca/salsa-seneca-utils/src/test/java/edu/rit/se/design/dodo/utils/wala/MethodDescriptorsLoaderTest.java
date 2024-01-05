package edu.rit.se.design.dodo.utils.wala;

import edu.rit.se.design.dodo.utils.ResourceLoader;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class MethodDescriptorsLoaderTest {

    /**
     * Test of loadEntrypointsFromCSV method, of class MethodDescriptorsLoader.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testLoadEntrypointsFromCSV() throws Exception {
        File csv = ResourceLoader.getResourceFile("/config/entrypoints.csv");
        System.out.println("testLoadEntrypointsFromCSV() " + csv.getName());
        List<EntrypointDescriptor> descriptorsMap = MethodDescriptorsLoader.loadEntrypointsFromCsv(csv, true);
        descriptorsMap.forEach((EntrypointDescriptor entry) -> {
            System.out.println("\t" + entry);
        });
        assertTrue(descriptorsMap.size() > 0);
    }

    /**
     * Test of loadEntrypointsFromCSV method, of class MethodDescriptorsLoader.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testLoadSinksFromCSV() throws Exception {
        File csv = ResourceLoader.getResourceFile("/config/sinks.csv");
        System.out.println("testLoadSinksFromCSV() " + csv.getName());
        List<SinkDescriptor> descriptorsMap = MethodDescriptorsLoader.loadSinksFromCsv(csv, true);
        descriptorsMap.forEach((SinkDescriptor entry) -> {
            System.out.println("\t" + entry);
        });
        assertTrue(descriptorsMap.size() > 0);
    }

}
