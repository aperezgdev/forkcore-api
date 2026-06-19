CREATE TABLE orders (
    id UUID PRIMARY KEY,
    status VARCHAR(32) NOT NULL,
    total NUMERIC(10, 2) NOT NULL CHECK (total >= 0),
    table_id UUID,
    notes TEXT
);

CREATE TABLE order_lines (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 1),
    unit_price NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0)
);

CREATE INDEX idx_order_lines_order_id ON order_lines (order_id);
CREATE INDEX idx_orders_table_id ON orders (table_id);
