package io.cdap.wrangler.api.parser;

/**
 * Represents a byte size value with unit conversion capabilities.
 * Supports units: B, KB, MB, GB, TB
 * Uses 1024-based conversion (1 KB = 1024 B)
 */
public class ByteSize extends Token {
    private static final long BYTES_PER_KB = 1024L;
    private static final long BYTES_PER_MB = BYTES_PER_KB * 1024L;
    private static final long BYTES_PER_GB = BYTES_PER_MB * 1024L;
    private static final long BYTES_PER_TB = BYTES_PER_GB * 1024L;
    
    private final long bytes;
    
    public ByteSize(String value) {
        super(TokenType.BYTE_SIZE, value);
        this.bytes = parseBytes(value);
    }
    
    public long getBytes() {
        return bytes;
    }
    
    public double getMegabytes() {
        return bytes / (1024.0 * 1024.0);
    }
    
    private long parseBytes(String value) {
        String number = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").toUpperCase();
        double amount = Double.parseDouble(number);
        
        return switch (unit) {
            case "B" -> (long) amount;
            case "KB" -> (long) (amount * 1024);
            case "MB" -> (long) (amount * 1024 * 1024);
            case "GB" -> (long) (amount * 1024 * 1024 * 1024);
            case "TB" -> (long) (amount * 1024L * 1024L * 1024L * 1024L);
            default -> throw new IllegalArgumentException("Invalid byte unit: " + unit);
        };
    }
}