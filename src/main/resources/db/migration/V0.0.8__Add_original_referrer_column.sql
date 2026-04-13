-- =============================================================================
-- Fast denormalisation of original_referrer
-- Uses a single window function to backfill all rows in one efficient pass.
-- =============================================================================

-- 1. Add the column (nullable initially)
ALTER TABLE tb_views ADD COLUMN original_referrer VARCHAR(255);

-- 2. Backfill using a window function (much faster than correlated subquery)
--    We find, for each row, the most recent previous view of the same user
--    that is NOT a self-referral (i.e., referrer does not contain the page's domain).
WITH previous_views AS (
    SELECT 
        v.id,
        v.user_id,
        v.access_timestamp,
        v.referrer,
        p.domain_id,
        LAG(v.referrer) OVER (
            PARTITION BY v.user_id 
            ORDER BY v.access_timestamp
        ) AS prev_referrer,
        LAG(p.domain_id) OVER (
            PARTITION BY v.user_id 
            ORDER BY v.access_timestamp
        ) AS prev_domain_id
    FROM tb_views v
    LEFT JOIN tb_pages p ON v.page_id = p.id
)
UPDATE tb_views AS target
SET original_referrer = pv.prev_referrer
FROM previous_views pv
WHERE target.id = pv.id
  AND target.original_referrer IS NULL
  AND (
      pv.prev_domain_id IS NULL 
      OR pv.prev_referrer NOT LIKE '%' || (
          SELECT hostname FROM tb_domains WHERE id = pv.prev_domain_id
      ) || '%'
  );

-- 3. For any row still NULL (first visit of a user or no external referrer), leave NULL

-- 4. Create indexes (standard CREATE INDEX – fast, but locks table briefly)
CREATE INDEX idx_views_user_access ON tb_views(user_id, access_timestamp DESC);
CREATE INDEX idx_views_original_referrer ON tb_views(original_referrer);

-- 5. Create trigger for future inserts (same as before, but now using fast lookups)
CREATE OR REPLACE FUNCTION set_original_referrer()
RETURNS TRIGGER AS $$
BEGIN
    SELECT referrer INTO NEW.original_referrer
    FROM tb_views
    WHERE user_id = NEW.user_id
      AND access_timestamp < NEW.access_timestamp
      AND (
          page_id IS NULL 
          OR referrer NOT LIKE '%' || (
              SELECT hostname FROM tb_domains 
              WHERE id = (SELECT domain_id FROM tb_pages WHERE id = NEW.page_id)
          ) || '%'
      )
    ORDER BY access_timestamp DESC
    LIMIT 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_set_original_referrer ON tb_views;
CREATE TRIGGER trg_set_original_referrer
    BEFORE INSERT ON tb_views
    FOR EACH ROW
    EXECUTE FUNCTION set_original_referrer();

-- 6. Drop old views (no longer needed)
DROP VIEW IF EXISTS tb_views_original_referer;
DROP VIEW IF EXISTS tb_views_original_referrer;