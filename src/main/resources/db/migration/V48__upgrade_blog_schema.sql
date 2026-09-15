-- Create blog_categories table
CREATE TABLE IF NOT EXISTS blog_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create blog_tags table
CREATE TABLE IF NOT EXISTS blog_tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create blog_media table (tracking Cloudinary images & videos)
CREATE TABLE IF NOT EXISTS blog_media (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(255) NOT NULL,
    secure_url VARCHAR(1000) NOT NULL,
    resource_type VARCHAR(50) NOT NULL DEFAULT 'IMAGE', -- 'IMAGE' or 'VIDEO'
    format VARCHAR(50),
    width INT,
    height INT,
    file_size BIGINT,
    alt_text VARCHAR(255),
    caption VARCHAR(500),
    folder VARCHAR(100),
    uploaded_by BIGINT,
    post_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add advanced SEO, scheduling, and media fields to blog_posts
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS seo_title VARCHAR(255);
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS seo_description VARCHAR(500);
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS canonical_url VARCHAR(500);
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS og_image_url VARCHAR(1000);
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS is_featured BOOLEAN DEFAULT FALSE;
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS scheduled_publish_at TIMESTAMP;
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS cover_image_alt VARCHAR(255);
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS cover_image_caption VARCHAR(500);
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS comments_count BIGINT DEFAULT 0;

-- Add nested replies & moderation tracking to blog_comments
ALTER TABLE blog_comments ADD COLUMN IF NOT EXISTS parent_id BIGINT;
ALTER TABLE blog_comments ADD COLUMN IF NOT EXISTS author_website VARCHAR(255);
ALTER TABLE blog_comments ADD COLUMN IF NOT EXISTS ip_address VARCHAR(100);
ALTER TABLE blog_comments ADD COLUMN IF NOT EXISTS user_agent VARCHAR(500);
ALTER TABLE blog_comments ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Create indexes for fast querying and SEO lookup
CREATE INDEX IF NOT EXISTS idx_blog_posts_published_at ON blog_posts(published_at);
CREATE INDEX IF NOT EXISTS idx_blog_posts_scheduled_publish_at ON blog_posts(scheduled_publish_at);
CREATE INDEX IF NOT EXISTS idx_blog_posts_category ON blog_posts(category);
CREATE INDEX IF NOT EXISTS idx_blog_posts_is_featured ON blog_posts(is_featured);
CREATE INDEX IF NOT EXISTS idx_blog_comments_parent_id ON blog_comments(parent_id);
CREATE INDEX IF NOT EXISTS idx_blog_comments_status ON blog_comments(status);
CREATE INDEX IF NOT EXISTS idx_blog_media_post_id ON blog_media(post_id);
CREATE INDEX IF NOT EXISTS idx_blog_categories_slug ON blog_categories(slug);
CREATE INDEX IF NOT EXISTS idx_blog_tags_slug ON blog_tags(slug);

-- Insert default categories if they don't already exist
INSERT INTO blog_categories (name, slug, description)
VALUES 
    ('Engineering & Technology', 'engineering-technology', 'Deep dives into modern architecture, backend, frontend, and cloud systems.'),
    ('Product Updates', 'product-updates', 'New feature announcements and platform improvements.'),
    ('Tutorials & Guides', 'tutorials-guides', 'Step-by-step developer guides and implementation tutorials.'),
    ('Design & UX', 'design-ux', 'UI/UX best practices, responsive layout, and design system patterns.'),
    ('Case Studies', 'case-studies', 'Real-world customer success stories and business impact.'),
    ('Security & Cloud', 'security-cloud', 'Best practices for cyber defense, authentication, and DevOps.')
ON CONFLICT (slug) DO NOTHING;
