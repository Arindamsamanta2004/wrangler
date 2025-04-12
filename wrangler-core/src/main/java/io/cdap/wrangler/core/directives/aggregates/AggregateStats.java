package io.cdap.wrangler.core.directives.aggregates;

import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.annotations.Description;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.UsageDefinition;

@Categories(categories = { "aggregate"})
@Description("Aggregates byte sizes and time durations")
public class AggregateStats implements Directive {
    private final String sizeColumn;
    private final String timeColumn;
    private final String totalSizeColumn;
    private final String totalTimeColumn;
    private long totalBytes = 0;
    private long totalNanos = 0;
    private int rowCount = 0;
    
    public AggregateStats(String sizeColumn, String timeColumn, 
                         String totalSizeColumn, String totalTimeColumn) {
        this.sizeColumn = sizeColumn;
        this.timeColumn = timeColumn;
        this.totalSizeColumn = totalSizeColumn;
        this.totalTimeColumn = totalTimeColumn;
    }
    
    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
        builder.define("size_column", TokenType.COLUMN_NAME);
        builder.define("time_column", TokenType.COLUMN_NAME);
        builder.define("total_size_column", TokenType.COLUMN_NAME);
        builder.define("total_time_column", TokenType.COLUMN_NAME);
        return builder.build();
    }
    
    @Override
    public void execute(Row row, ExecutorContext context) {
        Object sizeObj = row.getValue(sizeColumn);
        Object timeObj = row.getValue(timeColumn);
        
        if (sizeObj instanceof ByteSize) {
            totalBytes += ((ByteSize) sizeObj).getBytes();
        }
        
        if (timeObj instanceof TimeDuration) {
            totalNanos += ((TimeDuration) timeObj).getNanos();
        }
        
        rowCount++;
        
        if (context.isLast()) {
            Row resultRow = new Row();
            resultRow.add(totalSizeColumn, totalBytes / (1024.0 * 1024.0)); // Convert to MB
            resultRow.add(totalTimeColumn, totalNanos / 1_000_000_000.0); // Convert to seconds
            context.getStore().add(resultRow);
        }
    }
}