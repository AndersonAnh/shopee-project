ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP;

COMMENT ON COLUMN orders.cancelled_at IS 'Момент отмены заказа';
