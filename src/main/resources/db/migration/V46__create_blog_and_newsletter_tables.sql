-- Create blog_posts table
CREATE TABLE IF NOT EXISTS blog_posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    summary VARCHAR(1000),
    content TEXT,
    cover_image_url VARCHAR(500),
    author_name VARCHAR(255),
    author_id BIGINT,
    category VARCHAR(100),
    tags VARCHAR(500),
    status VARCHAR(50) DEFAULT 'DRAFT',
    views_count BIGINT DEFAULT 0,
    likes_count BIGINT DEFAULT 0,
    reading_time_minutes INT DEFAULT 3,
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create blog_comments table
CREATE TABLE IF NOT EXISTS blog_comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_name VARCHAR(255) NOT NULL,
    author_email VARCHAR(255),
    content TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'APPROVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create newsletter_subscribers table
CREATE TABLE IF NOT EXISTS newsletter_subscribers (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    unsubscribe_token VARCHAR(255) UNIQUE,
    subscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unsubscribed_at TIMESTAMP
);

-- Create indices for high-traffic query paths
CREATE INDEX IF NOT EXISTS idx_blog_posts_status ON blog_posts(status);
CREATE INDEX IF NOT EXISTS idx_blog_posts_slug ON blog_posts(slug);
CREATE INDEX IF NOT EXISTS idx_blog_comments_post_id ON blog_comments(post_id);
CREATE INDEX IF NOT EXISTS idx_newsletter_subscribers_email ON newsletter_subscribers(email);
