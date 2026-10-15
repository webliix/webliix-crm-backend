-- V63: Fix menu_items.key reserved-keyword issue by safely renaming column to item_key
-- Key is an ANSI SQL / PostgreSQL reserved keyword which causes syntax conflicts in various SQL tools/dialects

DO $$
BEGIN
    -- Check if column "key" exists in menu_items and "item_key" does not exist yet
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'menu_items' AND column_name = 'key'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'menu_items' AND column_name = 'item_key'
    ) THEN
        ALTER TABLE menu_items RENAME COLUMN "key" TO item_key;
    END IF;
END $$;

-- Ensure unique index / constraint exists on item_key
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_menu_items_item_key'
    ) THEN
        BEGIN
            ALTER TABLE menu_items ADD CONSTRAINT uq_menu_items_item_key UNIQUE (item_key);
        EXCEPTION
            WHEN duplicate_table THEN NULL;
            WHEN duplicate_object THEN NULL;
        END;
    END IF;
END $$;
