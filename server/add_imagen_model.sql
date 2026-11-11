-- ============================================================================
-- Добавление модели IMAGEN-4.0-GENERATE-001 в llm_models
-- Для генерации изображений через Google Vertex AI
-- ============================================================================

-- 1. Проверка: есть ли уже такая модель
SELECT * FROM llm_models 
WHERE code = 'IMAGEN_4' 
   OR name = 'imagen-4.0-generate-001'
   OR real_model_id = 'imagen-4.0-generate-001';

-- 2. Вставка новой модели (если не существует)
INSERT INTO llm_models (
    id,
    code,
    name,
    real_model_id,
    provider,
    energy_cost,
    min_status,
    is_active,
    sort_order,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'IMAGEN_4',                    -- Код модели для приложения
    'imagen-4.0-generate-001',     -- Полное название
    'imagen-4.0-generate-001',     -- Реальный ID для Google Vertex API
    'GOOGLE_VERTEX',               -- Провайдер (Google Vertex AI)
    50,                            -- Стоимость генерации картинки (50 энергии)
    'FREE',                        -- Доступна всем пользователям
    true,                          -- Активна
    0,                             -- Порядок сортировки
    NOW(),
    NOW()
)
ON CONFLICT DO NOTHING;

-- 3. Проверка после вставки
SELECT 
    id,
    code,
    name,
    real_model_id,
    provider,
    energy_cost,
    min_status,
    is_active
FROM llm_models 
WHERE code = 'IMAGEN_4';

-- ============================================================================
-- Запрос для статистики по энергиям (с учётом imagen)
-- ============================================================================

SELECT
    DATE(eh.created_at) AS "Дата",
    COALESCE(lm.name, TRIM(SUBSTRING(eh.meta_data FROM 'model:\s*([^;]+)'))) AS "Модель",
    COALESCE(lm.code, 'N/A') AS "Код",
    COALESCE(lm.provider, 'IMAGE_GEN') AS "Провайдер",
    COUNT(*) AS "Количество запросов",
    SUM(ABS(eh.amount)) AS "Потрачено энергии",
    SUM(eh.total_tokens) AS "Всего токенов",
    SUM(eh.output_tokens) AS "Всего output токенов",
    AVG(eh.total_tokens) AS "Среднее токенов за запрос",
    AVG(eh.prompt_tokens) AS "Среднее prompt",
    AVG(eh.output_tokens) AS "Среднее output",
    SUM(eh.cached_tokens) AS "Всего кэша"
FROM energy_history eh
LEFT JOIN llm_models lm ON 
    TRIM(SUBSTRING(eh.meta_data FROM 'model:\s*([^;]+)')) = lm.name
    OR TRIM(SUBSTRING(eh.meta_data FROM 'model:\s*([^;]+)')) = lm.code
WHERE eh.amount < 0
    AND eh.operation_type IN ('SPEND_CHAT_REPLY', 'SPEND_CHAT_FORCE', 'SPEND_CHAT_REGEN', 'SPEND_GIFT')
    AND eh.created_at >= NOW() - INTERVAL '30 days'
GROUP BY DATE(eh.created_at), eh.meta_data, lm.name, lm.code, lm.provider
ORDER BY "Дата" ASC;
