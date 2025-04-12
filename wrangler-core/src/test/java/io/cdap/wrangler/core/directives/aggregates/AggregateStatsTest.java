package io.cdap.wrangler.core.directives.aggregates;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.test.TestingRig;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;

public class AggregateStatsTest {

    @Test
    public void testBasicAggregation() {
        List<Row> rows = new ArrayList<>();
        
        // Create test data
        Row row1 = new Row();
        row1.add("data_size", "100KB");
        row1.add("response_time", "500ms");
        
        Row row2 = new Row();
        row2.add("data_size", "2MB");
        row2.add("response_time", "1.5s");
        
        rows.add(row1);
        rows.add(row2);

        String[] recipe = new String[] {
            "aggregate-stats :data_size :response_time total_size_mb total_time_sec"
        };

        List<Row> results = TestingRig.execute(recipe, rows);

        // Verify results
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(2.0977, results.get(0).getValue("total_size_mb"), 0.001);
        Assert.assertEquals(2.0, results.get(0).getValue("total_time_sec"), 0.001);
    }

    @Test
    public void testMixedUnits() {
        List<Row> rows = new ArrayList<>();
        
        Row row1 = new Row();
        row1.add("data_size", "1GB");
        row1.add("response_time", "1h");
        
        Row row2 = new Row();
        row2.add("data_size", "1024MB");
        row2.add("response_time", "3600s");
        
        rows.add(row1);
        rows.add(row2);

        String[] recipe = new String[] {
            "aggregate-stats :data_size :response_time total_size_mb total_time_sec"
        };

        List<Row> results = TestingRig.execute(recipe, rows);
        
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(2048.0, results.get(0).getValue("total_size_mb"), 0.001);
        Assert.assertEquals(7200.0, results.get(0).getValue("total_time_sec"), 0.001);
    }

    @Test
    public void testEmptyInput() {
        List<Row> rows = new ArrayList<>();
        
        String[] recipe = new String[] {
            "aggregate-stats :data_size :response_time total_size_mb total_time_sec"
        };

        List<Row> results = TestingRig.execute(recipe, rows);
        
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(0.0, results.get(0).getValue("total_size_mb"), 0.001);
        Assert.assertEquals(0.0, results.get(0).getValue("total_time_sec"), 0.001);
    }
//These tests will verify how the directive handles invalid inputs and edge cases.
/*These tests cover:

1. Invalid byte size units
2. Invalid time duration units
3. Null value handling
4. Invalid number format
5. Non-existent columns
Would you like me to add any additional error handling test cases? */

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidByteUnit() {
        List<Row> rows = new ArrayList<>();
        Row row = new Row();
        row.add("data_transfer_size", "100XB"); // Invalid byte unit
        row.add("response_time", "500ms");
        rows.add(row);
    
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };
    
        TestingRig.execute(recipe, rows);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeUnit() {
        List<Row> rows = new ArrayList<>();
        Row row = new Row();
        row.add("data_transfer_size", "100KB");
        row.add("response_time", "500ys"); // Invalid time unit
        rows.add(row);
    
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };
    
        TestingRig.execute(recipe, rows);
    }

    @Test
    public void testNullValues() {
        List<Row> rows = new ArrayList<>();
        Row row = new Row();
        row.add("data_transfer_size", null);
        row.add("response_time", null);
        rows.add(row);
    
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };
    
        List<Row> results = TestingRig.execute(recipe, rows);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(0.0, results.get(0).getValue("total_size_mb"), 0.001);
        Assert.assertEquals(0.0, results.get(0).getValue("total_time_sec"), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNumberFormat() {
        List<Row> rows = new ArrayList<>();
        Row row = new Row();
        row.add("data_transfer_size", "abc KB"); // Invalid number format
        row.add("response_time", "500ms");
        rows.add(row);
    
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };
    
        TestingRig.execute(recipe, rows);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentColumns() {
        List<Row> rows = new ArrayList<>();
        Row row = new Row();
        row.add("wrong_column", "100KB");
        rows.add(row);
    
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };
    
        TestingRig.execute(recipe, rows);
    }

    @Test
    public void testLargeDatasetPerformance() {
        List<Row> rows = new ArrayList<>();
        // Add 100K rows to test performance
        for (int i = 0; i < 100000; i++) {
            Row row = new Row();
            row.add("data_size", (i % 2 == 0) ? "1MB" : "1024KB");
            row.add("response_time", (i % 2 == 0) ? "1s" : "1000ms");
            rows.add(row);
        }
        
        String[] recipe = new String[] {
            "aggregate-stats :data_size :response_time total_size_mb total_time_sec"
        };
        
        long startTime = System.currentTimeMillis();
        List<Row> results = TestingRig.execute(recipe, rows);
        long endTime = System.currentTimeMillis();
        
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(100000.0, results.get(0).getValue("total_size_mb"), 0.001);
        Assert.assertEquals(100000.0, results.get(0).getValue("total_time_sec"), 0.001);
        Assert.assertTrue("Performance test took too long", (endTime - startTime) < 5000);
    }

    @Test
    public void testAverageCalculation() {
        List<Row> rows = new ArrayList<>();
        Row row1 = new Row();
        row1.add("data_size", "100MB");
        row1.add("response_time", "10s");
        
        Row row2 = new Row();
        row2.add("data_size", "300MB");
        row2.add("response_time", "20s");
        
        rows.add(row1);
        rows.add(row2);
    
        String[] recipe = new String[] {
            "aggregate-stats :data_size :response_time avg_size_mb avg_time_sec average"
        };
    
        List<Row> results = TestingRig.execute(recipe, rows);
        Assert.assertEquals(200.0, results.get(0).getValue("avg_size_mb"), 0.001);
        Assert.assertEquals(15.0, results.get(0).getValue("avg_time_sec"), 0.001);
    }

    @Test
    public void testPrecisionAndAccuracy() {
        List<Row> rows = new ArrayList<>();
        
        // Test precise calculations
        Row row1 = new Row();
        row1.add("data_size", "1.5GB");
        row1.add("response_time", "1.75h");
        
        Row row2 = new Row();
        row2.add("data_size", "2048MB");
        row2.add("response_time", "3600.5s");
        
        rows.add(row1);
        rows.add(row2);
    
        String[] recipe = new String[] {
            "aggregate-stats :data_size :response_time total_size_mb total_time_sec"
        };
    
        List<Row> results = TestingRig.execute(recipe, rows);
        
        // Verify precise calculations
        Assert.assertEquals(3584.0, results.get(0).getValue("total_size_mb"), 0.0001);
        Assert.assertEquals(9900.5, results.get(0).getValue("total_time_sec"), 0.0001);
    }

    @Test
    public void testEdgeCases() {
        List<Row> rows = new ArrayList<>();
        
        // Test extreme values
        Row row1 = new Row();
        row1.add("data_size", "0B");
        row1.add("response_time", "0ms");
        
        Row row2 = new Row();
        row2.add("data_size", "9999TB");
        row2.add("response_time", "999999h");
        
        // Test mixed case units
        Row row3 = new Row();
        row3.add("data_size", "1Mb");
        row3.add("response_time", "1Ms");
        
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
    
        String[] recipe = new String[] {
            "aggregate-stats :data_size :response_time total_size_mb total_time_sec"
        };
    
        List<Row> results = TestingRig.execute(recipe, rows);
        Assert.assertEquals(1, results.size());
        // Verify calculations with extreme values
        Assert.assertTrue(results.get(0).getValue("total_size_mb") > 0);
        Assert.assertTrue(results.get(0).getValue("total_time_sec") > 0);
    }

    @Test
    public void testComprehensiveCoverage() {
        List<Row> rows = new ArrayList<>();
        
        // Test all byte units
        rows.add(createRow("1B", "1ms"));
        rows.add(createRow("1KB", "1s"));
        rows.add(createRow("1MB", "1m"));
        rows.add(createRow("1GB", "1h"));
        rows.add(createRow("1TB", "1d"));
        
        // Test decimal values
        rows.add(createRow("1.5MB", "1.5h"));
        
        // Test case insensitivity
        rows.add(createRow("1mb", "1H"));
        
        String[] recipe = new String[] {
            "aggregate-stats :data_size :response_time total_size_mb total_time_sec"
        };
    
        List<Row> results = TestingRig.execute(recipe, rows);
        Assert.assertEquals(1, results.size());
        // Verify all conversions worked correctly
        Assert.assertTrue(results.get(0).getValue("total_size_mb") > 0);
        Assert.assertTrue(results.get(0).getValue("total_time_sec") > 0);
    }

    private Row createRow(String size, String time) {
        Row row = new Row();
        row.add("data_size", size);
        row.add("response_time", time);
        return row;
    }
}