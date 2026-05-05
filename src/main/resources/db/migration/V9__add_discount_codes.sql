-- V9: Discount codes table

CREATE TABLE IF NOT EXISTS discount_codes (
    code VARCHAR(20) PRIMARY KEY,
    discount_type VARCHAR(20) NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Insert default discount codes
INSERT INTO discount_codes (code, discount_type, value, active) VALUES
    ('DESCUENTO10', 'PERCENTAGE', 10, true),
    ('ENVIOGRATIS', 'FREE_SHIPPING', 100, true),
    ('AHORRA20', 'PERCENTAGE', 20, true)
ON CONFLICT (code) DO NOTHING;