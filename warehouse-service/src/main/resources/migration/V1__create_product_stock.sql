CREATE TABLE product_stock (
                               id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               product_id          UUID NOT NULL,
                               available_quantity  INTEGER NOT NULL DEFAULT 0,
                               reserved_quantity   INTEGER NOT NULL DEFAULT 0,
                               version             BIGINT NOT NULL DEFAULT 0,

                               CONSTRAINT uq_product_stock_product_id UNIQUE (product_id),
                               CONSTRAINT chk_available_non_negative CHECK (available_quantity >= 0),
                               CONSTRAINT chk_reserved_non_negative CHECK (reserved_quantity >= 0)
);