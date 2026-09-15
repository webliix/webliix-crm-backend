CREATE TABLE stored_files (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255),
    content_type VARCHAR(100),
    file_size BIGINT NOT NULL,
    storage_provider VARCHAR(100) NOT NULL,
    storage_path TEXT NOT NULL,
    uploaded_by BIGINT,
    module VARCHAR(100),
    reference_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
