-- ==============================================================================
-- Avalon Avatars - Initial Database Schema (V1)
-- ==============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

--
-- PostgreSQL database dump
--



SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: update_updated_at_column(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ai_character_alternate_greetings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ai_character_alternate_greetings (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    character_id uuid NOT NULL,
    greeting_text text NOT NULL,
    title character varying(150),
    order_index integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: ai_character_lorebook_bindings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ai_character_lorebook_bindings (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    character_id uuid NOT NULL,
    lorebook_id uuid NOT NULL,
    is_enabled boolean DEFAULT true NOT NULL,
    is_primary boolean DEFAULT false NOT NULL,
    priority integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: ai_character_lorebook_entries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ai_character_lorebook_entries (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    lorebook_id uuid NOT NULL,
    keys text NOT NULL,
    secondary_keys text,
    content text NOT NULL,
    comment character varying(255),
    enabled boolean DEFAULT true NOT NULL,
    constant boolean DEFAULT false NOT NULL,
    selective boolean DEFAULT false NOT NULL,
    insertion_order integer DEFAULT 0 NOT NULL,
    "position" character varying(20) DEFAULT 'BEFORE_CHAR'::character varying NOT NULL,
    use_regex boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone,
    priority integer DEFAULT 10 NOT NULL,
    probability integer DEFAULT 100 NOT NULL,
    scan_depth_override integer,
    sticky_turns integer DEFAULT 0 NOT NULL,
    cooldown_turns integer DEFAULT 0 NOT NULL,
    delay_messages integer DEFAULT 0 NOT NULL,
    secondary_logic character varying(20) DEFAULT 'AND_ANY'::character varying NOT NULL,
    exclude_recursion boolean DEFAULT false NOT NULL,
    prevent_recursion boolean DEFAULT false NOT NULL,
    delay_until_recursion boolean DEFAULT false NOT NULL,
    depth integer DEFAULT 4 NOT NULL
);


--
-- Name: ai_character_lorebooks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ai_character_lorebooks (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    character_id uuid,
    name character varying(150) DEFAULT 'Default Lorebook'::character varying NOT NULL,
    description text,
    scan_depth integer DEFAULT 5 NOT NULL,
    token_budget integer,
    recursive_scanning boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone,
    user_id uuid,
    is_public boolean DEFAULT false NOT NULL,
    avatar_url text
);


--
-- Name: ai_character_prompt_details; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ai_character_prompt_details (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    character_id uuid NOT NULL,
    scenario text,
    mes_example text,
    custom_system_prompt text,
    post_history_instructions text,
    creator character varying(120),
    character_version character varying(50),
    creator_notes text,
    raw_extensions text,
    is_imported boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: ai_character_stats; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ai_character_stats (
    character_id uuid NOT NULL,
    message_count bigint DEFAULT 0 NOT NULL,
    total_generated_tokens bigint DEFAULT 0 NOT NULL,
    last_interaction_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    popularity_score double precision DEFAULT 0.0 NOT NULL
);


--
-- Name: ai_character_tags; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ai_character_tags (
    character_id uuid NOT NULL,
    tag character varying(60) NOT NULL
);


--
-- Name: ai_characters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ai_characters (
    id uuid NOT NULL,
    owner uuid NOT NULL,
    gemini_model character varying(100) NOT NULL,
    character_name character varying(100) NOT NULL,
    full_description text,
    short_description character varying(255) NOT NULL,
    temperature real NOT NULL,
    harassment integer NOT NULL,
    hate integer NOT NULL,
    sexually_explicit integer NOT NULL,
    dangerous_content integer NOT NULL,
    top_k integer,
    top_p real NOT NULL,
    max_output_tokens integer NOT NULL,
    max_context_tokens integer NOT NULL,
    is_public_full_description boolean NOT NULL,
    category character varying(100) NOT NULL,
    first_message text,
    is_banned boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    visibility character varying(20) DEFAULT 'PRIVATE'::character varying NOT NULL,
    interaction_format character varying(50) DEFAULT 'LITERARY_RPG'::character varying NOT NULL,
    unclaimed_energy double precision DEFAULT 0.0 NOT NULL,
    total_earned_energy double precision DEFAULT 0.0 NOT NULL,
    is_outreach_enabled boolean DEFAULT false NOT NULL,
    outreach_sent_count integer DEFAULT 0 NOT NULL,
    outreach_replied_count integer DEFAULT 0 NOT NULL,
    consecutive_failures_count integer DEFAULT 0 NOT NULL,
    is_outreach_blocked boolean DEFAULT false NOT NULL,
    voice character varying(50) DEFAULT 'ZEPHYR'::character varying NOT NULL
);


--
-- Name: ai_providers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ai_providers (
    id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone,
    provider_id character varying(32) NOT NULL,
    display_name character varying(64) NOT NULL,
    base_url character varying(500) NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    is_default boolean DEFAULT false NOT NULL,
    order_index integer DEFAULT 0 NOT NULL
);


--
-- Name: api_keys; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.api_keys (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    key character varying(255) NOT NULL,
    request_count integer DEFAULT 0 NOT NULL,
    last_used bigint,
    is_banned boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    deleted_at timestamp with time zone,
    cooldown_until timestamp with time zone,
    last_selected timestamp with time zone DEFAULT now(),
    failure_count integer DEFAULT 0
);


--
-- Name: app_users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_users (
    id uuid NOT NULL,
    google_sub character varying(255),
    password_hash character varying(255),
    name character varying(50),
    email character varying(255),
    image_path character varying(255),
    current_daily_balance integer NOT NULL,
    last_quota_reset bigint DEFAULT '1761590985850'::bigint NOT NULL,
    total_generated_messages integer DEFAULT 0 NOT NULL,
    default_persona uuid,
    is_banned boolean DEFAULT false NOT NULL,
    is_shadow_banned boolean DEFAULT false NOT NULL,
    total_generated_tokens integer DEFAULT 0 NOT NULL,
    rarity_profile_name character varying(50) DEFAULT 'CLASSIC_RPG'::character varying NOT NULL,
    nick_name character varying(20) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    premium_status character varying(20) DEFAULT 'FREE'::character varying NOT NULL,
    reserve_balance integer DEFAULT 0 NOT NULL,
    last_seen_at timestamp without time zone,
    timezone_offset integer DEFAULT 0 NOT NULL,
    is_creator_outreach_blocked boolean DEFAULT false NOT NULL,
    is_outreach_enabled boolean DEFAULT true NOT NULL,
    daily_outreach_limit integer DEFAULT 3 NOT NULL,
    is_flow_mode_enabled boolean DEFAULT false NOT NULL,
    is_early_supporter boolean DEFAULT false NOT NULL
);


--
-- Name: auth_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.auth_sessions (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    refresh_token character varying(255) NOT NULL,
    revoked boolean DEFAULT false NOT NULL,
    device_id character varying(128),
    device_name character varying(128),
    user_agent character varying(255),
    ip character varying(64),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    expires_at timestamp with time zone,
    last_used_at timestamp with time zone,
    previous_refresh_token character varying(255),
    last_rotated_at timestamp with time zone,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone,
    fcm_token character varying(512)
);


--
-- Name: COLUMN auth_sessions.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.auth_sessions.created_at IS 'Время создания записи (устанавливается БД при INSERT)';


--
-- Name: COLUMN auth_sessions.updated_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.auth_sessions.updated_at IS 'Время последнего обновления (устанавливается БД при INSERT и UPDATE)';


--
-- Name: COLUMN auth_sessions.deleted_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.auth_sessions.deleted_at IS 'Время мягкого удаления (NULL = запись активна)';


--
-- Name: categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.categories (
    id integer NOT NULL,
    category character varying(255) NOT NULL,
    localized_names text NOT NULL,
    sort_order integer DEFAULT 0 NOT NULL,
    is_special boolean DEFAULT false NOT NULL
);


--
-- Name: categories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.categories_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.categories_id_seq OWNED BY public.categories.id;


--
-- Name: character_avatars; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.character_avatars (
    id uuid NOT NULL,
    character_public_id uuid NOT NULL,
    image_path character varying(255) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    thumb_hash character varying(255)
);


--
-- Name: character_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.character_events (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    required_level integer NOT NULL,
    event_trigger_scenario text NOT NULL,
    event_permanent_trait text NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone,
    event_name character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- Name: character_images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.character_images (
    id uuid NOT NULL,
    character_public_id uuid NOT NULL,
    image_path character varying(255) NOT NULL,
    order_index integer,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    thumb_hash character varying(255),
    is_private boolean DEFAULT false NOT NULL,
    energy_cost integer DEFAULT 0 NOT NULL,
    required_level integer
);


--
-- Name: character_welcome_card; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.character_welcome_card (
    character_id uuid NOT NULL,
    welcome_card_description text,
    card_color_start text DEFAULT 'DEFAULT_COLOR'::text NOT NULL,
    card_color_end text DEFAULT 'DEFAULT_COLOR'::text NOT NULL,
    is_enable_light boolean DEFAULT false NOT NULL,
    card_color_light text DEFAULT 'DEFAULT_COLOR'::text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    id uuid NOT NULL
);


--
-- Name: chat_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_sessions (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    character_id uuid NOT NULL,
    is_archived boolean DEFAULT false NOT NULL,
    active_variant_id uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    last_message_id uuid,
    last_message_preview character varying(255),
    last_message_type character varying(20),
    last_message_at timestamp with time zone,
    last_message_role character varying(20)
);


--
-- Name: chat_summaries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_summaries (
    id uuid NOT NULL,
    chat_variant_id uuid NOT NULL,
    content text NOT NULL,
    last_included_slot_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: chat_variant_images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_variant_images (
    id uuid NOT NULL,
    chat_variants_id uuid NOT NULL,
    image_path character varying(255) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    thumb_hash character varying(255)
);


--
-- Name: chat_variants; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_variants (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    chat_session_id uuid NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    unread_count integer DEFAULT 0 NOT NULL
);


--
-- Name: creator_affinity; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.creator_affinity (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    creator_id uuid NOT NULL,
    message_count integer DEFAULT 0 NOT NULL,
    last_interacted_at timestamp without time zone DEFAULT now() NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    deleted_at timestamp without time zone
);


--
-- Name: energy_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.energy_history (
    id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    user_id uuid NOT NULL,
    operation_type character varying(30) NOT NULL,
    amount integer NOT NULL,
    daily_balance_after integer NOT NULL,
    reserve_balance_after integer NOT NULL,
    meta_data text,
    related_message_id uuid,
    total_tokens bigint,
    prompt_tokens bigint,
    output_tokens bigint,
    cached_tokens bigint,
    thoughts_tokens bigint,
    real_cost_usd numeric(10,6) DEFAULT 0 NOT NULL
);


--
-- Name: COLUMN energy_history.real_cost_usd; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.energy_history.real_cost_usd IS 'Реальная стоимость запроса в долларах США (USD).';


--
-- Name: gift_packs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gift_packs (
    id uuid NOT NULL,
    author_id uuid,
    name character varying(255) NOT NULL,
    description text,
    cover_url character varying(512) NOT NULL,
    is_public boolean DEFAULT false,
    price integer DEFAULT 0,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    deleted_at timestamp with time zone
);


--
-- Name: gifts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gifts (
    id uuid NOT NULL,
    pack_id uuid,
    file_url character varying(512) NOT NULL,
    price integer DEFAULT 0,
    action_type character varying(50) NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    deleted_at timestamp with time zone,
    gift_name character varying(255) NOT NULL,
    gift_description character varying(512) NOT NULL,
    effect_type character varying(50) DEFAULT 'NONE'::character varying
);


--
-- Name: global_settings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.global_settings (
    id integer NOT NULL,
    max_quota_messages_per_hour integer DEFAULT 40 NOT NULL,
    main_character_rules text DEFAULT ''::text NOT NULL,
    default_gemini_model text DEFAULT ''::text NOT NULL,
    max_output_tokens integer DEFAULT 2000 NOT NULL,
    is_enable_payment_service boolean DEFAULT false NOT NULL,
    vertex_location character varying(64) DEFAULT 'europe-west4'::character varying NOT NULL,
    gemini_3_vertex_location character varying(64) DEFAULT 'us-central1'::character varying NOT NULL,
    audio_vertex_location character varying(64) DEFAULT 'europe-west4'::character varying
);


--
-- Name: global_settings_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.global_settings_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: global_settings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.global_settings_id_seq OWNED BY public.global_settings.id;


--
-- Name: live_call_turn_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.live_call_turn_logs (
    id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    user_id uuid NOT NULL,
    chat_variant_id uuid NOT NULL,
    live_model character varying(100),
    turn_complete_reason character varying(50),
    had_model_turn boolean DEFAULT false NOT NULL,
    interrupted boolean DEFAULT false NOT NULL,
    waiting_for_input boolean DEFAULT false NOT NULL,
    total_tokens bigint,
    prompt_tokens bigint,
    response_tokens bigint,
    cached_tokens bigint,
    thoughts_tokens bigint,
    billing_disposition character varying(30) NOT NULL,
    energy_cost integer,
    energy_history_record_id uuid,
    meta_data text
);


--
-- Name: llm_models; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.llm_models (
    id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone,
    code character varying(50) NOT NULL,
    name character varying(100) NOT NULL,
    real_model_id character varying(100) NOT NULL,
    provider character varying(50) NOT NULL,
    energy_cost integer DEFAULT 1 NOT NULL,
    min_status character varying(20) DEFAULT 'FREE'::character varying NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    sort_order integer DEFAULT 0 NOT NULL,
    price_per_million_input numeric(10,6) DEFAULT 0 NOT NULL,
    price_per_million_output numeric(10,6) DEFAULT 0 NOT NULL
);


--
-- Name: COLUMN llm_models.price_per_million_input; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.llm_models.price_per_million_input IS 'Цена за 1 миллион входных токенов (USD). Для Imagen - цена за 1 изображение.';


--
-- Name: COLUMN llm_models.price_per_million_output; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.llm_models.price_per_million_output IS 'Цена за 1 миллион выходных токенов (USD). Для Imagen = 0.';


--
-- Name: message_container_lore_states; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.message_container_lore_states (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    container_id uuid NOT NULL,
    entry_id uuid NOT NULL,
    sticky_remaining integer DEFAULT 0 NOT NULL,
    cooldown_remaining integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: message_containers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.message_containers (
    id uuid NOT NULL,
    slot_id uuid NOT NULL,
    role character varying(10) NOT NULL,
    message_tokens integer,
    rarity character varying(20) DEFAULT 'COMMON'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    user_reaction character varying(16),
    character_reaction character varying(16)
);


--
-- Name: message_contents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.message_contents (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    container_id uuid NOT NULL,
    type character varying(50) NOT NULL,
    content text NOT NULL,
    order_index integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: message_read_receipts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.message_read_receipts (
    id uuid NOT NULL,
    message_container_id uuid NOT NULL,
    user_id uuid NOT NULL,
    read_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: message_slots; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.message_slots (
    id uuid NOT NULL,
    chat_variant_id uuid NOT NULL,
    active_message_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    is_archived boolean DEFAULT false NOT NULL
);


--
-- Name: moderators; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.moderators (
    user_id uuid NOT NULL,
    assigned_at bigint DEFAULT '1761590985949'::bigint NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: COLUMN moderators.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.moderators.created_at IS 'Время создания записи (устанавливается БД при INSERT)';


--
-- Name: COLUMN moderators.updated_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.moderators.updated_at IS 'Время последнего обновления (устанавливается БД при INSERT и UPDATE)';


--
-- Name: COLUMN moderators.deleted_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.moderators.deleted_at IS 'Время мягкого удаления (NULL = запись активна)';


--
-- Name: outreach_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.outreach_history (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    user_id uuid NOT NULL,
    sent_at timestamp without time zone DEFAULT now() NOT NULL,
    feedback_status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    deleted_at timestamp without time zone
);


--
-- Name: server_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.server_categories (
    id integer NOT NULL,
    category_code character varying(255) NOT NULL,
    localized_names text NOT NULL,
    sort_order integer DEFAULT 0 NOT NULL,
    is_special boolean DEFAULT true NOT NULL
);


--
-- Name: server_categories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.server_categories_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: server_categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.server_categories_id_seq OWNED BY public.server_categories.id;


--
-- Name: servercategories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.servercategories (
    id integer NOT NULL,
    category_code character varying(255) NOT NULL,
    localized_names text NOT NULL,
    sort_order integer DEFAULT 0 NOT NULL
);


--
-- Name: servercategories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.servercategories_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: servercategories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.servercategories_id_seq OWNED BY public.servercategories.id;


--
-- Name: subscription_tiers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subscription_tiers (
    id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone,
    code character varying(20) NOT NULL,
    daily_energy integer DEFAULT 0 NOT NULL,
    max_capacity integer DEFAULT 0 NOT NULL
);


--
-- Name: subscriptions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subscriptions (
    id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone,
    user_id uuid NOT NULL,
    status character varying(20) NOT NULL,
    plan character varying(20) NOT NULL,
    provider character varying(50) NOT NULL,
    purchase_token text NOT NULL,
    last_order_id character varying(100),
    start_date timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    is_auto_renewing boolean DEFAULT true NOT NULL,
    product_id character varying(100) NOT NULL,
    last_validation_date timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    country character varying(30),
    is_test_purchase boolean DEFAULT false NOT NULL
);


--
-- Name: transactions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.transactions (
    id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone,
    user_id uuid NOT NULL,
    type character varying(30) NOT NULL,
    provider character varying(20) NOT NULL,
    product_id character varying(100) NOT NULL,
    transaction_id character varying(255) NOT NULL,
    related_subscription_token text,
    amount numeric(19,4),
    currency character varying(3),
    country character varying(30),
    is_test_purchase boolean DEFAULT false NOT NULL
);


--
-- Name: unlocked_character_images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.unlocked_character_images (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    character_image_id uuid NOT NULL,
    unlocked_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: user_ai_settings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_ai_settings (
    id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone,
    user_id uuid NOT NULL,
    base_url character varying(500) DEFAULT 'https://generativelanguage.googleapis.com'::character varying NOT NULL,
    api_key text,
    text_model character varying(150) DEFAULT 'gemini-2.5-flash'::character varying NOT NULL,
    tts_model character varying(150) DEFAULT 'gemini-2.5-flash-preview-tts'::character varying NOT NULL,
    image_model character varying(150) DEFAULT 'gemini-2.5-flash-image'::character varying NOT NULL,
    summary_model character varying(150) DEFAULT 'gemini-2.5-flash-lite'::character varying NOT NULL,
    is_custom_provider_enabled boolean DEFAULT false NOT NULL,
    image_prompt_model character varying(150) DEFAULT 'gemini-2.5-flash'::character varying,
    provider_id character varying(32) DEFAULT 'GOOGLE'::character varying NOT NULL
);


--
-- Name: user_character_lorebooks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_character_lorebooks (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    character_id uuid NOT NULL,
    lorebook_id uuid NOT NULL,
    is_enabled boolean DEFAULT true NOT NULL,
    priority integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: user_character_relationships; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_character_relationships (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    character_id uuid NOT NULL,
    relationship_level integer DEFAULT 1 NOT NULL,
    experience_points bigint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    pending_event_level integer,
    is_outreach_enabled boolean DEFAULT true NOT NULL,
    outreach_morning_enabled boolean DEFAULT true NOT NULL,
    outreach_morning_prompt text,
    outreach_afternoon_enabled boolean DEFAULT true NOT NULL,
    outreach_afternoon_prompt text,
    outreach_evening_enabled boolean DEFAULT true NOT NULL,
    outreach_evening_prompt text,
    last_outreach_sent_at timestamp with time zone,
    last_outreach_slot character varying(20)
);


--
-- Name: user_personas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_personas (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    persona_name character varying(255) NOT NULL,
    persona_description text NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: user_pity_counters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_pity_counters (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    profile_name character varying(50) NOT NULL,
    pity_count integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: user_saved_gift_packs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_saved_gift_packs (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    pack_id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: user_saved_lorebooks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_saved_lorebooks (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    lorebook_id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: welcome_card_images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.welcome_card_images (
    id uuid NOT NULL,
    character_public_id uuid NOT NULL,
    image_path character varying(255) NOT NULL,
    order_index integer,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    thumb_hash character varying(255)
);


--
-- Name: categories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories ALTER COLUMN id SET DEFAULT nextval('public.categories_id_seq'::regclass);


--
-- Name: global_settings id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.global_settings ALTER COLUMN id SET DEFAULT nextval('public.global_settings_id_seq'::regclass);


--
-- Name: server_categories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.server_categories ALTER COLUMN id SET DEFAULT nextval('public.server_categories_id_seq'::regclass);


--
-- Name: servercategories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servercategories ALTER COLUMN id SET DEFAULT nextval('public.servercategories_id_seq'::regclass);


--
-- Name: ai_characters Characters_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_characters
    ADD CONSTRAINT "Characters_pkey" PRIMARY KEY (id);


--
-- Name: ai_character_alternate_greetings ai_character_alternate_greetings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_alternate_greetings
    ADD CONSTRAINT ai_character_alternate_greetings_pkey PRIMARY KEY (id);


--
-- Name: ai_character_lorebook_bindings ai_character_lorebook_bindings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_lorebook_bindings
    ADD CONSTRAINT ai_character_lorebook_bindings_pkey PRIMARY KEY (id);


--
-- Name: ai_character_lorebook_entries ai_character_lorebook_entries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_lorebook_entries
    ADD CONSTRAINT ai_character_lorebook_entries_pkey PRIMARY KEY (id);


--
-- Name: ai_character_lorebooks ai_character_lorebooks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_lorebooks
    ADD CONSTRAINT ai_character_lorebooks_pkey PRIMARY KEY (id);


--
-- Name: ai_character_prompt_details ai_character_prompt_details_character_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_prompt_details
    ADD CONSTRAINT ai_character_prompt_details_character_id_key UNIQUE (character_id);


--
-- Name: ai_character_prompt_details ai_character_prompt_details_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_prompt_details
    ADD CONSTRAINT ai_character_prompt_details_pkey PRIMARY KEY (id);


--
-- Name: ai_character_tags ai_character_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_tags
    ADD CONSTRAINT ai_character_tags_pkey PRIMARY KEY (character_id, tag);


--
-- Name: ai_providers ai_providers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_providers
    ADD CONSTRAINT ai_providers_pkey PRIMARY KEY (id);


--
-- Name: api_keys api_keys_key_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.api_keys
    ADD CONSTRAINT api_keys_key_unique UNIQUE (key);


--
-- Name: api_keys apikeys_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.api_keys
    ADD CONSTRAINT apikeys_pkey PRIMARY KEY (id);


--
-- Name: app_users app_users_google_sub_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_users
    ADD CONSTRAINT app_users_google_sub_unique UNIQUE (google_sub);


--
-- Name: auth_sessions auth_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auth_sessions
    ADD CONSTRAINT auth_sessions_pkey PRIMARY KEY (id);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);


--
-- Name: character_events character_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_events
    ADD CONSTRAINT character_events_pkey PRIMARY KEY (id);


--
-- Name: character_welcome_card character_welcome_card_character_id_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_welcome_card
    ADD CONSTRAINT character_welcome_card_character_id_unique UNIQUE (character_id);


--
-- Name: character_welcome_card character_welcome_card_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_welcome_card
    ADD CONSTRAINT character_welcome_card_pkey PRIMARY KEY (id);


--
-- Name: character_avatars characteravatars_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_avatars
    ADD CONSTRAINT characteravatars_pkey PRIMARY KEY (id);


--
-- Name: character_images characterimages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_images
    ADD CONSTRAINT characterimages_pkey PRIMARY KEY (id);


--
-- Name: chat_summaries chat_summaries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_summaries
    ADD CONSTRAINT chat_summaries_pkey PRIMARY KEY (id);


--
-- Name: chat_variant_images chat_variant_images_chat_variants_id_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_variant_images
    ADD CONSTRAINT chat_variant_images_chat_variants_id_unique UNIQUE (chat_variants_id);


--
-- Name: chat_variants chatvariants_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_variants
    ADD CONSTRAINT chatvariants_pkey PRIMARY KEY (id);


--
-- Name: creator_affinity creator_affinity_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creator_affinity
    ADD CONSTRAINT creator_affinity_pkey PRIMARY KEY (id);


--
-- Name: energy_history energy_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.energy_history
    ADD CONSTRAINT energy_history_pkey PRIMARY KEY (id);


--
-- Name: gift_packs gift_packs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gift_packs
    ADD CONSTRAINT gift_packs_pkey PRIMARY KEY (id);


--
-- Name: gifts gifts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gifts
    ADD CONSTRAINT gifts_pkey PRIMARY KEY (id);


--
-- Name: global_settings global_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.global_settings
    ADD CONSTRAINT global_settings_pkey PRIMARY KEY (id);


--
-- Name: live_call_turn_logs live_call_turn_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.live_call_turn_logs
    ADD CONSTRAINT live_call_turn_logs_pkey PRIMARY KEY (id);


--
-- Name: llm_models llm_models_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.llm_models
    ADD CONSTRAINT llm_models_pkey PRIMARY KEY (id);


--
-- Name: message_contents message_attachments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_contents
    ADD CONSTRAINT message_attachments_pkey PRIMARY KEY (id);


--
-- Name: message_container_lore_states message_container_lore_states_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_container_lore_states
    ADD CONSTRAINT message_container_lore_states_pkey PRIMARY KEY (id);


--
-- Name: message_read_receipts message_read_receipts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_read_receipts
    ADD CONSTRAINT message_read_receipts_pkey PRIMARY KEY (id);


--
-- Name: message_slots message_slots_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_slots
    ADD CONSTRAINT message_slots_pkey PRIMARY KEY (id);


--
-- Name: message_containers messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_containers
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);


--
-- Name: moderators moderators_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moderators
    ADD CONSTRAINT moderators_pkey PRIMARY KEY (id);


--
-- Name: outreach_history outreach_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.outreach_history
    ADD CONSTRAINT outreach_history_pkey PRIMARY KEY (id);


--
-- Name: ai_character_stats pk_ai_character_stats; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_stats
    ADD CONSTRAINT pk_ai_character_stats PRIMARY KEY (character_id);


--
-- Name: server_categories server_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.server_categories
    ADD CONSTRAINT server_categories_pkey PRIMARY KEY (id);


--
-- Name: servercategories servercategories_category_code_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servercategories
    ADD CONSTRAINT servercategories_category_code_unique UNIQUE (category_code);


--
-- Name: servercategories servercategories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servercategories
    ADD CONSTRAINT servercategories_pkey PRIMARY KEY (id);


--
-- Name: chat_variant_images storyimages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_variant_images
    ADD CONSTRAINT storyimages_pkey PRIMARY KEY (id);


--
-- Name: subscription_tiers subscription_tiers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscription_tiers
    ADD CONSTRAINT subscription_tiers_pkey PRIMARY KEY (id);


--
-- Name: subscriptions subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT subscriptions_pkey PRIMARY KEY (id);


--
-- Name: transactions transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_pkey PRIMARY KEY (id);


--
-- Name: user_character_relationships unique_user_character_relationship_idx; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_character_relationships
    ADD CONSTRAINT unique_user_character_relationship_idx UNIQUE (user_id, character_id);


--
-- Name: user_pity_counters unique_user_profile_idx; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_pity_counters
    ADD CONSTRAINT unique_user_profile_idx UNIQUE (user_id, profile_name);


--
-- Name: unlocked_character_images unlocked_character_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unlocked_character_images
    ADD CONSTRAINT unlocked_character_images_pkey PRIMARY KEY (id);


--
-- Name: ai_character_lorebook_bindings uq_char_lorebook; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_lorebook_bindings
    ADD CONSTRAINT uq_char_lorebook UNIQUE (character_id, lorebook_id);


--
-- Name: message_container_lore_states uq_container_entry_timed; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_container_lore_states
    ADD CONSTRAINT uq_container_entry_timed UNIQUE (container_id, entry_id);


--
-- Name: user_character_lorebooks uq_user_character_lorebook; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_character_lorebooks
    ADD CONSTRAINT uq_user_character_lorebook UNIQUE (user_id, character_id, lorebook_id);


--
-- Name: user_saved_gift_packs uq_user_saved_gift_packs_user_pack; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_gift_packs
    ADD CONSTRAINT uq_user_saved_gift_packs_user_pack UNIQUE (user_id, pack_id);


--
-- Name: user_saved_lorebooks uq_user_saved_lorebook; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_lorebooks
    ADD CONSTRAINT uq_user_saved_lorebook UNIQUE (user_id, lorebook_id);


--
-- Name: user_ai_settings user_ai_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_ai_settings
    ADD CONSTRAINT user_ai_settings_pkey PRIMARY KEY (id);


--
-- Name: user_character_lorebooks user_character_lorebooks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_character_lorebooks
    ADD CONSTRAINT user_character_lorebooks_pkey PRIMARY KEY (id);


--
-- Name: user_character_relationships user_character_relationships_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_character_relationships
    ADD CONSTRAINT user_character_relationships_pkey PRIMARY KEY (id);


--
-- Name: user_pity_counters user_pity_counters_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_pity_counters
    ADD CONSTRAINT user_pity_counters_pkey PRIMARY KEY (id);


--
-- Name: user_saved_gift_packs user_saved_gift_packs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_gift_packs
    ADD CONSTRAINT user_saved_gift_packs_pkey PRIMARY KEY (id);


--
-- Name: user_saved_lorebooks user_saved_lorebooks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_lorebooks
    ADD CONSTRAINT user_saved_lorebooks_pkey PRIMARY KEY (id);


--
-- Name: chat_sessions usercharacterchats_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_sessions
    ADD CONSTRAINT usercharacterchats_pkey PRIMARY KEY (id);


--
-- Name: user_personas userpersonas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_personas
    ADD CONSTRAINT userpersonas_pkey PRIMARY KEY (id);


--
-- Name: app_users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: welcome_card_images welcomecardimages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.welcome_card_images
    ADD CONSTRAINT welcomecardimages_pkey PRIMARY KEY (id);


--
-- Name: auth_sessions_expires_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX auth_sessions_expires_at ON public.auth_sessions USING btree (expires_at);


--
-- Name: auth_sessions_refresh_token_unique; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX auth_sessions_refresh_token_unique ON public.auth_sessions USING btree (refresh_token);


--
-- Name: auth_sessions_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX auth_sessions_user_id ON public.auth_sessions USING btree (user_id);


--
-- Name: idx_active_keys; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_active_keys ON public.api_keys USING btree (is_banned, cooldown_until, deleted_at);


--
-- Name: idx_ai_character_stats_popular; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ai_character_stats_popular ON public.ai_character_stats USING btree (message_count DESC);


--
-- Name: idx_ai_characters_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ai_characters_category ON public.ai_characters USING btree (visibility, category);


--
-- Name: idx_ai_characters_live_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ai_characters_live_id ON public.ai_characters USING btree (id) WHERE (deleted_at IS NULL);


--
-- Name: idx_ai_characters_new; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ai_characters_new ON public.ai_characters USING btree (visibility, created_at DESC);


--
-- Name: idx_ai_characters_owner; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ai_characters_owner ON public.ai_characters USING btree (owner, updated_at);


--
-- Name: idx_ai_characters_visibility_live; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ai_characters_visibility_live ON public.ai_characters USING btree (visibility) WHERE (deleted_at IS NULL);


--
-- Name: idx_ai_providers_provider_id; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_ai_providers_provider_id ON public.ai_providers USING btree (provider_id);


--
-- Name: idx_ai_stats_popularity_score; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ai_stats_popularity_score ON public.ai_character_stats USING btree (popularity_score DESC);


--
-- Name: idx_app_users_created_at_live; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_users_created_at_live ON public.app_users USING btree (created_at, id) WHERE (deleted_at IS NULL);


--
-- Name: idx_attachments_lookup_ordered; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attachments_lookup_ordered ON public.message_contents USING btree (container_id, order_index);


--
-- Name: idx_auth_sessions_deleted_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_auth_sessions_deleted_at ON public.auth_sessions USING btree (deleted_at);


--
-- Name: idx_auth_sessions_previous_refresh_token; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_auth_sessions_previous_refresh_token ON public.auth_sessions USING btree (previous_refresh_token);


--
-- Name: idx_avatars_lookup; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_avatars_lookup ON public.character_avatars USING btree (character_public_id);


--
-- Name: idx_char_greetings_char_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_char_greetings_char_id ON public.ai_character_alternate_greetings USING btree (character_id);


--
-- Name: idx_char_lb_bindings_book_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_char_lb_bindings_book_id ON public.ai_character_lorebook_bindings USING btree (lorebook_id);


--
-- Name: idx_char_lb_bindings_char_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_char_lb_bindings_char_id ON public.ai_character_lorebook_bindings USING btree (character_id);


--
-- Name: idx_char_tags_tag; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_char_tags_tag ON public.ai_character_tags USING btree (tag);


--
-- Name: idx_character_images_ordered; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_character_images_ordered ON public.character_images USING btree (character_public_id, order_index);


--
-- Name: idx_chat_sessions_character; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_sessions_character ON public.chat_sessions USING btree (user_id, character_id);


--
-- Name: idx_chat_sessions_last_message_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_sessions_last_message_id ON public.chat_sessions USING btree (last_message_id);


--
-- Name: idx_chat_sessions_updated_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_sessions_updated_at ON public.chat_sessions USING btree (updated_at);


--
-- Name: idx_chat_sessions_user_character_live; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_sessions_user_character_live ON public.chat_sessions USING btree (user_id, character_id, id) WHERE (deleted_at IS NULL);


--
-- Name: idx_chat_sessions_user_sync; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_sessions_user_sync ON public.chat_sessions USING btree (user_id, updated_at DESC);


--
-- Name: idx_chat_summaries_last; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_summaries_last ON public.chat_summaries USING btree (chat_variant_id, created_at);


--
-- Name: idx_chat_variants_session_live; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_variants_session_live ON public.chat_variants USING btree (chat_session_id, id) WHERE (deleted_at IS NULL);


--
-- Name: idx_container_lore_states_cid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_container_lore_states_cid ON public.message_container_lore_states USING btree (container_id);


--
-- Name: idx_container_lore_states_eid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_container_lore_states_eid ON public.message_container_lore_states USING btree (entry_id);


--
-- Name: idx_creator_affinity_lookup; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_creator_affinity_lookup ON public.creator_affinity USING btree (user_id, creator_id);


--
-- Name: idx_energy_history_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_energy_history_created_at ON public.energy_history USING btree (created_at);


--
-- Name: idx_energy_history_real_cost; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_energy_history_real_cost ON public.energy_history USING btree (created_at) WHERE (real_cost_usd > (0)::numeric);


--
-- Name: idx_energy_history_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_energy_history_user ON public.energy_history USING btree (user_id);


--
-- Name: idx_gift_packs_author; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gift_packs_author ON public.gift_packs USING btree (author_id);


--
-- Name: idx_gifts_pack; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gifts_pack ON public.gifts USING btree (pack_id);


--
-- Name: idx_live_turn_logs_chat_variant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_live_turn_logs_chat_variant ON public.live_call_turn_logs USING btree (chat_variant_id);


--
-- Name: idx_live_turn_logs_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_live_turn_logs_created ON public.live_call_turn_logs USING btree (created_at);


--
-- Name: idx_live_turn_logs_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_live_turn_logs_user ON public.live_call_turn_logs USING btree (user_id);


--
-- Name: idx_llm_models_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_llm_models_code ON public.llm_models USING btree (code);


--
-- Name: idx_lore_entries_book_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lore_entries_book_id ON public.ai_character_lorebook_entries USING btree (lorebook_id);


--
-- Name: idx_lorebooks_char_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lorebooks_char_id ON public.ai_character_lorebooks USING btree (character_id);


--
-- Name: idx_lorebooks_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lorebooks_user_id ON public.ai_character_lorebooks USING btree (user_id);


--
-- Name: idx_message_containers_created_at_live; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_message_containers_created_at_live ON public.message_containers USING btree (created_at, id) WHERE (deleted_at IS NULL);


--
-- Name: idx_message_contents_images_lookup; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_message_contents_images_lookup ON public.message_contents USING btree (container_id, type) WHERE (deleted_at IS NULL);


--
-- Name: idx_message_slots_active_message_live; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_message_slots_active_message_live ON public.message_slots USING btree (active_message_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_messages_slot_lookup; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_slot_lookup ON public.message_containers USING btree (slot_id);


--
-- Name: idx_moderators_deleted_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_moderators_deleted_at ON public.moderators USING btree (deleted_at);


--
-- Name: idx_moderators_user; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_moderators_user ON public.moderators USING btree (user_id);


--
-- Name: idx_outreach_history_lookup; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_outreach_history_lookup ON public.outreach_history USING btree (character_id, user_id);


--
-- Name: idx_prompt_details_char_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_prompt_details_char_id ON public.ai_character_prompt_details USING btree (character_id);


--
-- Name: idx_read_receipts_unique; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_read_receipts_unique ON public.message_read_receipts USING btree (user_id, message_container_id);


--
-- Name: idx_slots_active_context; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_slots_active_context ON public.message_slots USING btree (chat_variant_id, is_archived, created_at);


--
-- Name: idx_slots_active_msg; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_slots_active_msg ON public.message_slots USING btree (active_message_id);


--
-- Name: idx_slots_deleted_sync; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_slots_deleted_sync ON public.message_slots USING btree (deleted_at);


--
-- Name: idx_slots_history_loading; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_slots_history_loading ON public.message_slots USING btree (chat_variant_id, created_at DESC);


--
-- Name: idx_slots_sync; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_slots_sync ON public.message_slots USING btree (chat_variant_id, updated_at);


--
-- Name: idx_subs_expires; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subs_expires ON public.subscriptions USING btree (expires_at);


--
-- Name: idx_subs_purchase_token; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_subs_purchase_token ON public.subscriptions USING btree (purchase_token);


--
-- Name: idx_subs_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subs_status ON public.subscriptions USING btree (status);


--
-- Name: idx_subscription_tiers_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_subscription_tiers_code ON public.subscription_tiers USING btree (code);


--
-- Name: idx_subscriptions_status_expires_user_live; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subscriptions_status_expires_user_live ON public.subscriptions USING btree (status, expires_at, user_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_subscriptions_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subscriptions_user ON public.subscriptions USING btree (user_id);


--
-- Name: idx_subscriptions_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subscriptions_user_id ON public.subscriptions USING btree (user_id);


--
-- Name: idx_transactions_subscriptions_analytics; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_transactions_subscriptions_analytics ON public.transactions USING btree (created_at, user_id, product_id) WHERE ((deleted_at IS NULL) AND ((type)::text = ANY ((ARRAY['SUBSCRIPTION_PURCHASE'::character varying, 'SUBSCRIPTION_RENEWAL'::character varying])::text[])));


--
-- Name: idx_transactions_transaction_id; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_transactions_transaction_id ON public.transactions USING btree (transaction_id);


--
-- Name: idx_transactions_user_created_subscription_types; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_transactions_user_created_subscription_types ON public.transactions USING btree (user_id, created_at) WHERE ((deleted_at IS NULL) AND ((type)::text = ANY ((ARRAY['SUBSCRIPTION_PURCHASE'::character varying, 'SUBSCRIPTION_RENEWAL'::character varying])::text[])));


--
-- Name: idx_transactions_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_transactions_user_id ON public.transactions USING btree (user_id);


--
-- Name: idx_user_ai_settings_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_user_ai_settings_user_id ON public.user_ai_settings USING btree (user_id);


--
-- Name: idx_user_char_lb_book; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_char_lb_book ON public.user_character_lorebooks USING btree (lorebook_id);


--
-- Name: idx_user_char_lb_user_char; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_char_lb_user_char ON public.user_character_lorebooks USING btree (user_id, character_id);


--
-- Name: idx_user_personas_sync; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_personas_sync ON public.user_personas USING btree (user_id, updated_at);


--
-- Name: idx_user_saved_lb_book_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_saved_lb_book_id ON public.user_saved_lorebooks USING btree (lorebook_id);


--
-- Name: idx_user_saved_lb_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_saved_lb_user_id ON public.user_saved_lorebooks USING btree (user_id);


--
-- Name: idx_users_active_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_active_status ON public.app_users USING btree (deleted_at, is_banned);


--
-- Name: idx_users_nickname; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_nickname ON public.app_users USING btree (nick_name);


--
-- Name: idx_variants_session_lookup; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_variants_session_lookup ON public.chat_variants USING btree (chat_session_id, updated_at DESC);


--
-- Name: idx_welcome_images_ordered; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_welcome_images_ordered ON public.welcome_card_images USING btree (character_public_id, order_index);


--
-- Name: unique_active_user_character_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX unique_active_user_character_idx ON public.chat_sessions USING btree (user_id, character_id) WHERE (deleted_at IS NULL);


--
-- Name: unique_character_event_level_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX unique_character_event_level_idx ON public.character_events USING btree (character_id, required_level);


--
-- Name: unique_unlocked_character_image_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX unique_unlocked_character_image_idx ON public.unlocked_character_images USING btree (user_id, character_image_id);


--
-- Name: user_character_relationships_character_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX user_character_relationships_character_id_idx ON public.user_character_relationships USING btree (character_id);


--
-- Name: user_character_relationships_updated_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX user_character_relationships_updated_at_idx ON public.user_character_relationships USING btree (updated_at);


--
-- Name: user_character_relationships_user_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX user_character_relationships_user_id_idx ON public.user_character_relationships USING btree (user_id);


--
-- Name: user_pity_counters_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX user_pity_counters_created_at_idx ON public.user_pity_counters USING btree (created_at);


--
-- Name: user_pity_counters_updated_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX user_pity_counters_updated_at_idx ON public.user_pity_counters USING btree (updated_at);


--
-- Name: message_container_lore_states trg_message_container_lore_states_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_message_container_lore_states_updated_at BEFORE UPDATE ON public.message_container_lore_states FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: ai_characters trg_update_ai_characters_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_ai_characters_updated_at BEFORE UPDATE ON public.ai_characters FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: app_users trg_update_app_users_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_app_users_updated_at BEFORE UPDATE ON public.app_users FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: character_avatars trg_update_character_avatars_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_character_avatars_updated_at BEFORE UPDATE ON public.character_avatars FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: character_images trg_update_character_images_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_character_images_updated_at BEFORE UPDATE ON public.character_images FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: character_welcome_card trg_update_character_welcome_card_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_character_welcome_card_updated_at BEFORE UPDATE ON public.character_welcome_card FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: chat_sessions trg_update_chat_sessions_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_chat_sessions_updated_at BEFORE UPDATE ON public.chat_sessions FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: chat_variant_images trg_update_chat_variant_images_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_chat_variant_images_updated_at BEFORE UPDATE ON public.chat_variant_images FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: chat_variants trg_update_chat_variants_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_chat_variants_updated_at BEFORE UPDATE ON public.chat_variants FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: message_contents trg_update_message_attachments_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_message_attachments_updated_at BEFORE UPDATE ON public.message_contents FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: message_slots trg_update_message_slots_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_message_slots_updated_at BEFORE UPDATE ON public.message_slots FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: message_containers trg_update_messages_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_messages_updated_at BEFORE UPDATE ON public.message_containers FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: user_ai_settings trg_update_user_ai_settings_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_user_ai_settings_updated_at BEFORE UPDATE ON public.user_ai_settings FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: user_character_relationships trg_update_user_character_relationships_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_user_character_relationships_updated_at BEFORE UPDATE ON public.user_character_relationships FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: user_personas trg_update_user_personas_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_user_personas_updated_at BEFORE UPDATE ON public.user_personas FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: user_pity_counters trg_update_user_pity_counters_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_user_pity_counters_updated_at BEFORE UPDATE ON public.user_pity_counters FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: energy_history update_energy_history_modtime; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER update_energy_history_modtime BEFORE UPDATE ON public.energy_history FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: live_call_turn_logs update_live_turn_logs_modtime; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER update_live_turn_logs_modtime BEFORE UPDATE ON public.live_call_turn_logs FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: user_saved_gift_packs update_user_saved_gift_packs_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER update_user_saved_gift_packs_updated_at BEFORE UPDATE ON public.user_saved_gift_packs FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: welcome_card_images update_welcome_card_images_modtime; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER update_welcome_card_images_modtime BEFORE UPDATE ON public.welcome_card_images FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: ai_character_alternate_greetings ai_character_alternate_greetings_character_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_alternate_greetings
    ADD CONSTRAINT ai_character_alternate_greetings_character_id_fkey FOREIGN KEY (character_id) REFERENCES public.ai_characters(id) ON DELETE CASCADE;


--
-- Name: ai_character_lorebook_bindings ai_character_lorebook_bindings_character_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_lorebook_bindings
    ADD CONSTRAINT ai_character_lorebook_bindings_character_id_fkey FOREIGN KEY (character_id) REFERENCES public.ai_characters(id) ON DELETE CASCADE;


--
-- Name: ai_character_lorebook_bindings ai_character_lorebook_bindings_lorebook_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_lorebook_bindings
    ADD CONSTRAINT ai_character_lorebook_bindings_lorebook_id_fkey FOREIGN KEY (lorebook_id) REFERENCES public.ai_character_lorebooks(id) ON DELETE CASCADE;


--
-- Name: ai_character_lorebook_entries ai_character_lorebook_entries_lorebook_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_lorebook_entries
    ADD CONSTRAINT ai_character_lorebook_entries_lorebook_id_fkey FOREIGN KEY (lorebook_id) REFERENCES public.ai_character_lorebooks(id) ON DELETE CASCADE;


--
-- Name: ai_character_lorebooks ai_character_lorebooks_character_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_lorebooks
    ADD CONSTRAINT ai_character_lorebooks_character_id_fkey FOREIGN KEY (character_id) REFERENCES public.ai_characters(id) ON DELETE CASCADE;


--
-- Name: ai_character_lorebooks ai_character_lorebooks_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_lorebooks
    ADD CONSTRAINT ai_character_lorebooks_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: ai_character_prompt_details ai_character_prompt_details_character_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_prompt_details
    ADD CONSTRAINT ai_character_prompt_details_character_id_fkey FOREIGN KEY (character_id) REFERENCES public.ai_characters(id) ON DELETE CASCADE;


--
-- Name: ai_character_tags ai_character_tags_character_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_tags
    ADD CONSTRAINT ai_character_tags_character_id_fkey FOREIGN KEY (character_id) REFERENCES public.ai_characters(id) ON DELETE CASCADE;


--
-- Name: character_events character_events_character_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_events
    ADD CONSTRAINT character_events_character_id_fkey FOREIGN KEY (character_id) REFERENCES public.ai_characters(id) ON DELETE CASCADE;


--
-- Name: creator_affinity creator_affinity_creator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creator_affinity
    ADD CONSTRAINT creator_affinity_creator_id_fkey FOREIGN KEY (creator_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: creator_affinity creator_affinity_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creator_affinity
    ADD CONSTRAINT creator_affinity_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: ai_character_stats fk_ai_character_stats_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_character_stats
    ADD CONSTRAINT fk_ai_character_stats_character FOREIGN KEY (character_id) REFERENCES public.ai_characters(id) ON DELETE CASCADE;


--
-- Name: auth_sessions fk_auth_sessions_user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auth_sessions
    ADD CONSTRAINT fk_auth_sessions_user_id FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: character_welcome_card fk_character_welcome_card_ai_characters; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_welcome_card
    ADD CONSTRAINT fk_character_welcome_card_ai_characters FOREIGN KEY (character_id) REFERENCES public.ai_characters(id) ON DELETE CASCADE;


--
-- Name: character_avatars fk_characteravatars_character_public_id__server_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_avatars
    ADD CONSTRAINT fk_characteravatars_character_public_id__server_id FOREIGN KEY (character_public_id) REFERENCES public.ai_characters(id) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: character_images fk_characterimages_character_public_id__server_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_images
    ADD CONSTRAINT fk_characterimages_character_public_id__server_id FOREIGN KEY (character_public_id) REFERENCES public.ai_characters(id) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: ai_characters fk_characters_owner__id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_characters
    ADD CONSTRAINT fk_characters_owner__id FOREIGN KEY (owner) REFERENCES public.app_users(id) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: chat_sessions fk_chat_sessions_active_variant_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_sessions
    ADD CONSTRAINT fk_chat_sessions_active_variant_id FOREIGN KEY (active_variant_id) REFERENCES public.chat_variants(id) ON DELETE SET NULL;


--
-- Name: chat_sessions fk_chat_sessions_character_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_sessions
    ADD CONSTRAINT fk_chat_sessions_character_id FOREIGN KEY (character_id) REFERENCES public.ai_characters(id) ON DELETE CASCADE;


--
-- Name: chat_sessions fk_chat_sessions_last_message_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_sessions
    ADD CONSTRAINT fk_chat_sessions_last_message_id FOREIGN KEY (last_message_id) REFERENCES public.message_containers(id) ON DELETE SET NULL;


--
-- Name: chat_sessions fk_chat_sessions_user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_sessions
    ADD CONSTRAINT fk_chat_sessions_user_id FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: chat_summaries fk_chat_summaries_slot; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_summaries
    ADD CONSTRAINT fk_chat_summaries_slot FOREIGN KEY (last_included_slot_id) REFERENCES public.message_slots(id) ON DELETE SET NULL;


--
-- Name: chat_summaries fk_chat_summaries_variant; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_summaries
    ADD CONSTRAINT fk_chat_summaries_variant FOREIGN KEY (chat_variant_id) REFERENCES public.chat_variants(id) ON DELETE CASCADE;


--
-- Name: chat_variants fk_chat_variants_chat_session_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_variants
    ADD CONSTRAINT fk_chat_variants_chat_session_id FOREIGN KEY (chat_session_id) REFERENCES public.chat_sessions(id) ON DELETE CASCADE;


--
-- Name: energy_history fk_energy_history_app_users; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.energy_history
    ADD CONSTRAINT fk_energy_history_app_users FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: live_call_turn_logs fk_live_turn_logs_app_users; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.live_call_turn_logs
    ADD CONSTRAINT fk_live_turn_logs_app_users FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: message_slots fk_message_slots_active_message_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_slots
    ADD CONSTRAINT fk_message_slots_active_message_id FOREIGN KEY (active_message_id) REFERENCES public.message_containers(id) ON DELETE SET NULL;


--
-- Name: message_slots fk_message_slots_chat_variant_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_slots
    ADD CONSTRAINT fk_message_slots_chat_variant_id FOREIGN KEY (chat_variant_id) REFERENCES public.chat_variants(id) ON DELETE CASCADE;


--
-- Name: message_containers fk_messages_slot_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_containers
    ADD CONSTRAINT fk_messages_slot_id FOREIGN KEY (slot_id) REFERENCES public.message_slots(id) ON DELETE CASCADE;


--
-- Name: moderators fk_moderators_user_id__id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moderators
    ADD CONSTRAINT fk_moderators_user_id__id FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: chat_variant_images fk_storyimages_chat_variants_id__id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_variant_images
    ADD CONSTRAINT fk_storyimages_chat_variants_id__id FOREIGN KEY (chat_variants_id) REFERENCES public.chat_variants(id) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: subscriptions fk_subscriptions_user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT fk_subscriptions_user_id FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: transactions fk_transactions_user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT fk_transactions_user_id FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE RESTRICT;


--
-- Name: user_pity_counters fk_user_pity_counters_user_id__id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_pity_counters
    ADD CONSTRAINT fk_user_pity_counters_user_id__id FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: user_saved_gift_packs fk_user_saved_gift_packs_packs; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_gift_packs
    ADD CONSTRAINT fk_user_saved_gift_packs_packs FOREIGN KEY (pack_id) REFERENCES public.gift_packs(id) ON DELETE CASCADE;


--
-- Name: user_saved_gift_packs fk_user_saved_gift_packs_users; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_gift_packs
    ADD CONSTRAINT fk_user_saved_gift_packs_users FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: user_personas fk_userpersonas_user_id__id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_personas
    ADD CONSTRAINT fk_userpersonas_user_id__id FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: app_users fk_users_default_persona__id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_users
    ADD CONSTRAINT fk_users_default_persona__id FOREIGN KEY (default_persona) REFERENCES public.user_personas(id) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: welcome_card_images fk_welcomecardimages_character_public_id__server_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.welcome_card_images
    ADD CONSTRAINT fk_welcomecardimages_character_public_id__server_id FOREIGN KEY (character_public_id) REFERENCES public.ai_characters(id) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: gift_packs gift_packs_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gift_packs
    ADD CONSTRAINT gift_packs_author_id_fkey FOREIGN KEY (author_id) REFERENCES public.app_users(id) ON DELETE SET NULL;


--
-- Name: gifts gifts_pack_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gifts
    ADD CONSTRAINT gifts_pack_id_fkey FOREIGN KEY (pack_id) REFERENCES public.gift_packs(id) ON DELETE CASCADE;


--
-- Name: message_contents message_attachments_message_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_contents
    ADD CONSTRAINT message_attachments_message_id_fkey FOREIGN KEY (container_id) REFERENCES public.message_containers(id) ON DELETE CASCADE;


--
-- Name: message_container_lore_states message_container_lore_states_container_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_container_lore_states
    ADD CONSTRAINT message_container_lore_states_container_id_fkey FOREIGN KEY (container_id) REFERENCES public.message_containers(id) ON DELETE CASCADE;


--
-- Name: message_container_lore_states message_container_lore_states_entry_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_container_lore_states
    ADD CONSTRAINT message_container_lore_states_entry_id_fkey FOREIGN KEY (entry_id) REFERENCES public.ai_character_lorebook_entries(id) ON DELETE CASCADE;


--
-- Name: message_read_receipts message_read_receipts_message_container_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_read_receipts
    ADD CONSTRAINT message_read_receipts_message_container_id_fkey FOREIGN KEY (message_container_id) REFERENCES public.message_containers(id) ON DELETE CASCADE;


--
-- Name: message_read_receipts message_read_receipts_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_read_receipts
    ADD CONSTRAINT message_read_receipts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: outreach_history outreach_history_character_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.outreach_history
    ADD CONSTRAINT outreach_history_character_id_fkey FOREIGN KEY (character_id) REFERENCES public.ai_characters(id) ON DELETE CASCADE;


--
-- Name: outreach_history outreach_history_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.outreach_history
    ADD CONSTRAINT outreach_history_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: unlocked_character_images unlocked_character_images_character_image_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unlocked_character_images
    ADD CONSTRAINT unlocked_character_images_character_image_id_fkey FOREIGN KEY (character_image_id) REFERENCES public.character_images(id) ON DELETE CASCADE;


--
-- Name: unlocked_character_images unlocked_character_images_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unlocked_character_images
    ADD CONSTRAINT unlocked_character_images_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: user_ai_settings user_ai_settings_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_ai_settings
    ADD CONSTRAINT user_ai_settings_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: user_character_lorebooks user_character_lorebooks_character_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_character_lorebooks
    ADD CONSTRAINT user_character_lorebooks_character_id_fkey FOREIGN KEY (character_id) REFERENCES public.ai_characters(id) ON DELETE CASCADE;


--
-- Name: user_character_lorebooks user_character_lorebooks_lorebook_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_character_lorebooks
    ADD CONSTRAINT user_character_lorebooks_lorebook_id_fkey FOREIGN KEY (lorebook_id) REFERENCES public.ai_character_lorebooks(id) ON DELETE CASCADE;


--
-- Name: user_character_lorebooks user_character_lorebooks_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_character_lorebooks
    ADD CONSTRAINT user_character_lorebooks_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: user_character_relationships user_character_relationships_character_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_character_relationships
    ADD CONSTRAINT user_character_relationships_character_id_fkey FOREIGN KEY (character_id) REFERENCES public.ai_characters(id) ON DELETE CASCADE;


--
-- Name: user_character_relationships user_character_relationships_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_character_relationships
    ADD CONSTRAINT user_character_relationships_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- Name: user_saved_lorebooks user_saved_lorebooks_lorebook_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_lorebooks
    ADD CONSTRAINT user_saved_lorebooks_lorebook_id_fkey FOREIGN KEY (lorebook_id) REFERENCES public.ai_character_lorebooks(id) ON DELETE CASCADE;


--
-- Name: user_saved_lorebooks user_saved_lorebooks_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_lorebooks
    ADD CONSTRAINT user_saved_lorebooks_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--




-- Unique nickname index for active users
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_nickname_unique ON public.app_users(nick_name) WHERE deleted_at IS NULL;
