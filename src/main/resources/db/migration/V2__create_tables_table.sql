CREATE TABLE tables (
    id UUID PRIMARY KEY,
    code VARCHAR(16) NOT NULL UNIQUE,
    capacity INTEGER NOT NULL CHECK (capacity >= 1),
    location TEXT,
    status VARCHAR(32) NOT NULL
);
