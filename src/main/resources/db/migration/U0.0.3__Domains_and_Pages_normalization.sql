-- Start transaction
START TRANSACTION;

-- Step 1: Remove the foreign key constraint
ALTER TABLE tb_views 
    DROP CONSTRAINT IF EXISTS tb_views_page_fk;

-- Step 2: Remove the page_id column
ALTER TABLE tb_views 
    DROP COLUMN IF EXISTS page_id;

-- Step 3: Drop the normalized tables
DROP TABLE IF EXISTS tb_pages;
DROP TABLE IF EXISTS tb_domains;

-- Commit transaction
COMMIT;