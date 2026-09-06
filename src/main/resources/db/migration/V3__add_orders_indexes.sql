-- Поиск / фильтрация заказов по клиенту
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);

-- Фильтрация по статусу (CONFIRMED, CANCELLED...)
CREATE INDEX IF NOT EXISTS idx_orders_order_status ON orders(order_status);

-- Сортировка по дате создания (свежие сверху)
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at DESC);

-- Композитный: частый запрос "заказы клиента со статусом X"
CREATE INDEX IF NOT EXISTS idx_orders_customer_status ON orders(customer_id, order_status);