package io.cdap.wrangler.api.parser;

public class TimeDuration extends Token {
    private final long nanos;
    
    public TimeDuration(String value) {
        super(TokenType.TIME_DURATION, value);
        this.nanos = parseNanos(value);
    }
    
    public long getNanos() {
        return nanos;
    }
    
    public double getSeconds() {
        return nanos / 1_000_000_000.0;
    }
    
    private long parseNanos(String value) {
        String number = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").toLowerCase();
        double amount = Double.parseDouble(number);
        
        return switch (unit) {
            case "ms" -> (long) (amount * 1_000_000);
            case "s" -> (long) (amount * 1_000_000_000);
            case "m" -> (long) (amount * 60 * 1_000_000_000);
            case "h" -> (long) (amount * 60 * 60 * 1_000_000_000);
            case "d" -> (long) (amount * 24 * 60 * 60 * 1_000_000_000);
            default -> throw new IllegalArgumentException("Invalid time unit: " + unit);
        };
    }
}