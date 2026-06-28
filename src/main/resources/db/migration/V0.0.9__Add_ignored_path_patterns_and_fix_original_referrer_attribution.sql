ALTER TABLE tb_domains ADD COLUMN ignored_path_patterns TEXT;

-- Recompute original_referrer with tab_id and domain_id scope (per session entry attribution)

UPDATE tb_views SET original_referrer = NULL;

UPDATE tb_views AS target
SET original_referrer = target.referrer
FROM tb_pages p
JOIN tb_domains d ON p.domain_id = d.id
WHERE target.page_id = p.id
  AND (
      target.referrer IS NULL
      OR d.hostname IS NULL
      OR target.referrer NOT LIKE '%' || d.hostname || '%'
  );

UPDATE tb_views AS target
SET original_referrer = (
    SELECT pv.referrer
    FROM tb_views pv
    JOIN tb_pages pp ON pv.page_id = pp.id
    JOIN tb_domains pd ON pp.domain_id = pd.id
    WHERE pv.user_id = target.user_id
      AND pv.tab_id = target.tab_id
      AND pp.domain_id = cp.domain_id
      AND pv.access_timestamp < target.access_timestamp
      AND (
          pv.referrer IS NULL
          OR pd.hostname IS NULL
          OR pv.referrer NOT LIKE '%' || pd.hostname || '%'
      )
    ORDER BY pv.access_timestamp DESC
    LIMIT 1
)
FROM tb_pages cp
JOIN tb_domains cd ON cp.domain_id = cd.id
WHERE target.page_id = cp.id
  AND target.referrer IS NOT NULL
  AND cd.hostname IS NOT NULL
  AND target.referrer LIKE '%' || cd.hostname || '%'
  AND target.original_referrer IS NULL;

CREATE OR REPLACE FUNCTION set_original_referrer()
RETURNS TRIGGER AS $$
DECLARE
    domain_hostname TEXT;
    current_domain_id BIGINT;
    is_self_referral BOOLEAN;
    prev_external_referrer TEXT;
BEGIN
    SELECT d.hostname, d.id INTO domain_hostname, current_domain_id
    FROM tb_pages p
    JOIN tb_domains d ON p.domain_id = d.id
    WHERE p.id = NEW.page_id;

    is_self_referral := (
        NEW.referrer IS NOT NULL
        AND domain_hostname IS NOT NULL
        AND NEW.referrer LIKE '%' || domain_hostname || '%'
    );

    IF is_self_referral THEN
        SELECT v.referrer INTO prev_external_referrer
        FROM tb_views v
        JOIN tb_pages p ON v.page_id = p.id
        JOIN tb_domains d ON p.domain_id = d.id
        WHERE v.user_id = NEW.user_id
          AND v.tab_id = NEW.tab_id
          AND p.domain_id = current_domain_id
          AND v.access_timestamp < NEW.access_timestamp
          AND (
              v.referrer IS NULL
              OR d.hostname IS NULL
              OR v.referrer NOT LIKE '%' || d.hostname || '%'
          )
        ORDER BY v.access_timestamp DESC
        LIMIT 1;
        NEW.original_referrer := prev_external_referrer;
    ELSE
        NEW.original_referrer := NEW.referrer;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_set_original_referrer ON tb_views;
CREATE TRIGGER trg_set_original_referrer
    BEFORE INSERT ON tb_views
    FOR EACH ROW
    EXECUTE FUNCTION set_original_referrer();
