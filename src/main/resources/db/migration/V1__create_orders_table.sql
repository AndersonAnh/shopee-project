CREATE TABLE IF NOT EXISTS orders (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      customer_id UUID NOT NULL,
                                      order_status VARCHAR(20) NOT NULL,
                                      total_amount NUMERIC(19, 4),
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      estimated_delivery DATE,

                                      city VARCHAR(100),
                                      street VARCHAR(255),
                                      house VARCHAR(50),
                                      apartment VARCHAR(50)
);

COMMENT ON TABLE orders IS 'Заказы';