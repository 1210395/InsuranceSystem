--
-- PostgreSQL database dump
--

\restrict viGpKokbPryjDzT2lWiqCsdT1Q39GL3BPfWQtsFZfGkaA69gce1d2ya9BHOjPXX

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: annual_usage; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.annual_usage (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    client_id uuid NOT NULL,
    year integer NOT NULL,
    service_type character varying(50) NOT NULL,
    total_amount numeric(12,2) DEFAULT 0,
    total_count integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.annual_usage OWNER TO postgres;

--
-- Name: chronic_patient_schedules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.chronic_patient_schedules (
    id uuid NOT NULL,
    amount double precision,
    created_at timestamp(6) with time zone NOT NULL,
    description text,
    interval_months integer NOT NULL,
    is_active boolean NOT NULL,
    lab_test_name character varying(200),
    last_sent_at timestamp(6) with time zone,
    medication_name character varying(200),
    medication_quantity integer,
    next_send_date date,
    notes text,
    radiology_test_name character varying(200),
    schedule_type character varying(20) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    patient_id uuid NOT NULL
);


ALTER TABLE public.chronic_patient_schedules OWNER TO postgres;

--
-- Name: claim_invoice_images; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.claim_invoice_images (
    claim_id uuid NOT NULL,
    image_path character varying(255)
);


ALTER TABLE public.claim_invoice_images OWNER TO postgres;

--
-- Name: claims; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.claims (
    id uuid NOT NULL,
    approved_at timestamp(6) with time zone,
    document_path character varying(255),
    rejected_at timestamp(6) with time zone,
    rejection_reason text,
    status character varying(30) NOT NULL,
    submitted_at timestamp(6) with time zone NOT NULL,
    member_id uuid NOT NULL,
    policy_id uuid NOT NULL,
    amount double precision NOT NULL,
    description character varying(255) NOT NULL,
    diagnosis character varying(255),
    doctor_name character varying(255),
    invoice_image_path character varying(255),
    provider_name character varying(255),
    service_date date NOT NULL,
    treatment_details character varying(255),
    admin_reviewed_at timestamp(6) with time zone,
    medical_reviewed_at timestamp(6) with time zone,
    admin_reviewer_id uuid,
    medical_reviewer_id uuid,
    client_pay_amount numeric(12,2),
    coverage_message text,
    coverage_percent_used numeric(5,2),
    insurance_covered_amount numeric(12,2),
    max_coverage_used numeric(12,2),
    is_covered boolean,
    emergency boolean DEFAULT false,
    member_name character varying(255),
    CONSTRAINT claims_status_check CHECK (((status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('APPROVED_BY_MEDICAL'::character varying)::text, ('REJECTED_BY_MEDICAL'::character varying)::text, ('AWAITING_ADMIN_REVIEW'::character varying)::text, ('APPROVED'::character varying)::text, ('REJECTED'::character varying)::text])))
);


ALTER TABLE public.claims OWNER TO postgres;

--
-- Name: client_chronic_diseases; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_chronic_diseases (
    client_id uuid NOT NULL,
    disease character varying(255),
    CONSTRAINT client_chronic_diseases_disease_check CHECK (((disease)::text = ANY (ARRAY[('DIABETES'::character varying)::text, ('HYPERTENSION'::character varying)::text, ('ASTHMA'::character varying)::text, ('HEART_DISEASE'::character varying)::text, ('KIDNEY_DISEASE'::character varying)::text, ('THYROID'::character varying)::text, ('EPILEPSY'::character varying)::text])))
);


ALTER TABLE public.client_chronic_diseases OWNER TO postgres;

--
-- Name: client_chronic_documents; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_chronic_documents (
    client_id uuid NOT NULL,
    document_path character varying(255)
);


ALTER TABLE public.client_chronic_documents OWNER TO postgres;

--
-- Name: client_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_roles (
    client_id uuid NOT NULL,
    role_id uuid NOT NULL
);


ALTER TABLE public.client_roles OWNER TO postgres;

--
-- Name: client_university_cards; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_university_cards (
    client_id uuid NOT NULL,
    image_path character varying(255)
);


ALTER TABLE public.client_university_cards OWNER TO postgres;

--
-- Name: clients; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.clients (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    email character varying(150),
    full_name character varying(150) NOT NULL,
    password_hash character varying(255) NOT NULL,
    phone character varying(40),
    requested_role character varying(40),
    role_request_status character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    university_card_image character varying(255),
    policy_id uuid,
    clinic_location character varying(200),
    department character varying(150),
    employee_id character varying(50),
    faculty character varying(150),
    lab_code character varying(50),
    lab_location character varying(200),
    lab_name character varying(150),
    pharmacy_code character varying(50),
    pharmacy_location character varying(200),
    pharmacy_name character varying(150),
    specialization character varying(150),
    radiology_code character varying(50),
    radiology_location character varying(200),
    radiology_name character varying(150),
    date_of_birth date,
    email_verification_code character varying(10),
    email_verification_expiry timestamp(6) with time zone,
    email_verified boolean DEFAULT false NOT NULL,
    gender character varying(10),
    national_id character varying(20),
    CONSTRAINT clients_requested_role_check CHECK (((requested_role)::text = ANY (ARRAY[('INSURANCE_CLIENT'::character varying)::text, ('DOCTOR'::character varying)::text, ('PHARMACIST'::character varying)::text, ('LAB_TECH'::character varying)::text, ('EMERGENCY_MANAGER'::character varying)::text, ('INSURANCE_MANAGER'::character varying)::text, ('RADIOLOGIST'::character varying)::text, ('MEDICAL_ADMIN'::character varying)::text]))),
    CONSTRAINT clients_role_request_status_check CHECK (((role_request_status)::text = ANY (ARRAY[('NONE'::character varying)::text, ('PENDING'::character varying)::text, ('APPROVED'::character varying)::text, ('REJECTED'::character varying)::text]))),
    CONSTRAINT clients_status_check CHECK (((status)::text = ANY (ARRAY[('ACTIVE'::character varying)::text, ('INACTIVE'::character varying)::text, ('DEACTIVATED'::character varying)::text])))
);


ALTER TABLE public.clients OWNER TO postgres;

--
-- Name: conversations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.conversations (
    id uuid NOT NULL,
    last_updated timestamp(6) with time zone NOT NULL,
    user1_id uuid NOT NULL,
    user2_id uuid NOT NULL,
    conversation_type character varying(50)
);


ALTER TABLE public.conversations OWNER TO postgres;

--
-- Name: coverage_usage; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.coverage_usage (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    client_id uuid NOT NULL,
    provider_specialization character varying(100),
    service_type character varying(50),
    usage_date date NOT NULL,
    year integer NOT NULL,
    visit_count integer DEFAULT 1,
    amount_used numeric(12,2) DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.coverage_usage OWNER TO postgres;

--
-- Name: coverages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.coverages (
    id uuid NOT NULL,
    amount numeric(12,2) NOT NULL,
    coverage_percent numeric(5,2) NOT NULL,
    coverage_type character varying(255) NOT NULL,
    is_covered boolean NOT NULL,
    description text,
    emergency_eligible boolean NOT NULL,
    max_limit numeric(12,2),
    minimum_deductible numeric(12,2),
    requires_referral boolean NOT NULL,
    service_name character varying(160) NOT NULL,
    policy_id uuid NOT NULL,
    allowed_gender character varying(255) DEFAULT 'ALL'::character varying,
    min_age integer,
    max_age integer,
    frequency_limit integer,
    frequency_period character varying(255),
    CONSTRAINT coverages_coverage_type_check CHECK (((coverage_type)::text = ANY (ARRAY[('OUTPATIENT'::character varying)::text, ('INPATIENT'::character varying)::text, ('EMERGENCY'::character varying)::text, ('LAB'::character varying)::text, ('XRAY'::character varying)::text])))
);


ALTER TABLE public.coverages OWNER TO postgres;

--
-- Name: doctor_medicine_assignments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.doctor_medicine_assignments (
    id uuid NOT NULL,
    active boolean NOT NULL,
    assigned_at timestamp(6) with time zone NOT NULL,
    max_daily_prescriptions integer,
    max_quantity_per_prescription integer,
    notes text,
    specialization character varying(150),
    updated_at timestamp(6) with time zone,
    assigned_by uuid,
    doctor_id uuid NOT NULL,
    medicine_id uuid NOT NULL
);


ALTER TABLE public.doctor_medicine_assignments OWNER TO postgres;

--
-- Name: doctor_procedures; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.doctor_procedures (
    id uuid NOT NULL,
    active boolean NOT NULL,
    category character varying(255) NOT NULL,
    coverage_status character varying(255) NOT NULL,
    created_at timestamp(6) with time zone,
    price numeric(12,2) NOT NULL,
    procedure_name character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone,
    max_price numeric(12,2),
    coverage_percentage integer,
    CONSTRAINT doctor_procedures_coverage_status_check CHECK (((coverage_status)::text = ANY (ARRAY[('COVERED'::character varying)::text, ('REQUIRES_APPROVAL'::character varying)::text, ('NOT_COVERED'::character varying)::text])))
);


ALTER TABLE public.doctor_procedures OWNER TO postgres;

--
-- Name: doctor_specialization; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.doctor_specialization (
    id bigint NOT NULL,
    consultation_price double precision NOT NULL,
    diagnoses text[],
    display_name character varying(255) NOT NULL,
    gender_restriction character varying(255),
    max_age integer,
    min_age integer,
    treatment_plans text[],
    diagnosis_treatment_mappings text
);


ALTER TABLE public.doctor_specialization OWNER TO postgres;

--
-- Name: doctor_specialization_allowed_genders; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.doctor_specialization_allowed_genders (
    specialization_id bigint CONSTRAINT doctor_specialization_allowed_gender_specialization_id_not_null NOT NULL,
    gender character varying(255)
);


ALTER TABLE public.doctor_specialization_allowed_genders OWNER TO postgres;

--
-- Name: doctor_specialization_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.doctor_specialization ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.doctor_specialization_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: doctor_test_assignments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.doctor_test_assignments (
    id uuid NOT NULL,
    active boolean NOT NULL,
    assigned_at timestamp(6) with time zone NOT NULL,
    max_daily_requests integer,
    notes text,
    specialization character varying(150),
    test_type character varying(20) NOT NULL,
    updated_at timestamp(6) with time zone,
    assigned_by uuid,
    doctor_id uuid NOT NULL,
    test_id uuid NOT NULL
);


ALTER TABLE public.doctor_test_assignments OWNER TO postgres;

--
-- Name: emergency_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.emergency_requests (
    id uuid NOT NULL,
    approved_at timestamp(6) with time zone,
    contact_phone character varying(255) NOT NULL,
    description character varying(255) NOT NULL,
    incident_date date NOT NULL,
    location character varying(255) NOT NULL,
    notes character varying(255),
    rejected_at timestamp(6) with time zone,
    rejection_reason character varying(255),
    status character varying(255),
    submitted_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone DEFAULT now() NOT NULL,
    doctor_id uuid,
    member_id uuid,
    family_member_id uuid,
    CONSTRAINT emergency_requests_status_check CHECK (((status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('APPROVED'::character varying)::text, ('REJECTED'::character varying)::text])))
);


ALTER TABLE public.emergency_requests OWNER TO postgres;

--
-- Name: family_member_documents; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.family_member_documents (
    family_member_id uuid NOT NULL,
    document_path character varying(255)
);


ALTER TABLE public.family_member_documents OWNER TO postgres;

--
-- Name: family_members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.family_members (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    date_of_birth date NOT NULL,
    full_name character varying(150) NOT NULL,
    gender character varying(10) NOT NULL,
    insurance_number character varying(30) NOT NULL,
    national_id character varying(20) NOT NULL,
    relation character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    client_id uuid NOT NULL,
    CONSTRAINT family_members_gender_check CHECK (((gender)::text = ANY (ARRAY[('MALE'::character varying)::text, ('FEMALE'::character varying)::text]))),
    CONSTRAINT family_members_relation_check CHECK (((relation)::text = ANY (ARRAY[('WIFE'::character varying)::text, ('SON'::character varying)::text, ('DAUGHTER'::character varying)::text, ('FATHER'::character varying)::text, ('MOTHER'::character varying)::text]))),
    CONSTRAINT family_members_status_check CHECK (((status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('APPROVED'::character varying)::text, ('REJECTED'::character varying)::text])))
);


ALTER TABLE public.family_members OWNER TO postgres;

--
-- Name: healthcare_provider_claims; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.healthcare_provider_claims (
    id uuid NOT NULL,
    amount double precision NOT NULL,
    approved_at timestamp(6) with time zone,
    client_id uuid,
    client_name character varying(255),
    description text NOT NULL,
    invoice_image_path character varying(255),
    rejected_at timestamp(6) with time zone,
    rejection_reason text,
    role_specific_data text,
    service_date date NOT NULL,
    status character varying(30) NOT NULL,
    submitted_at timestamp(6) with time zone,
    provider_id uuid NOT NULL,
    client_pay_amount numeric(12,2),
    coverage_message text,
    coverage_percent_used numeric(5,2),
    diagnosis text,
    doctor_name character varying(255),
    emergency boolean,
    insurance_covered_amount numeric(12,2),
    is_covered boolean,
    max_coverage_used numeric(12,2),
    medical_reviewed_at timestamp(6) with time zone,
    medical_reviewer_id uuid,
    medical_reviewer_name character varying(255),
    original_consultation_fee numeric(12,2),
    treatment_details text,
    policy_id uuid,
    is_chronic boolean DEFAULT false,
    paid_at timestamp without time zone,
    paid_by uuid,
    is_follow_up boolean DEFAULT false,
    CONSTRAINT healthcare_provider_claims_status_check CHECK (((status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('APPROVED'::character varying)::text, ('REJECTED'::character varying)::text, ('APPROVED_BY_MEDICAL'::character varying)::text, ('PENDING_MEDICAL'::character varying)::text, ('AWAITING_COORDINATION_REVIEW'::character varying)::text, ('APPROVED_FINAL'::character varying)::text, ('REJECTED_FINAL'::character varying)::text, ('RETURNED_FOR_REVIEW'::character varying)::text, ('RETURNED_TO_PROVIDER'::character varying)::text, ('PAYMENT_PENDING'::character varying)::text, ('PAID'::character varying)::text])))
);


ALTER TABLE public.healthcare_provider_claims OWNER TO postgres;

--
-- Name: lab_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.lab_requests (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    notes character varying(255),
    result_url character varying(255),
    status character varying(255),
    test_name character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone,
    doctor_id uuid NOT NULL,
    member_id uuid NOT NULL,
    lab_tech_id uuid,
    approved_price double precision,
    entered_price double precision,
    test_id uuid,
    price_id uuid,
    diagnosis text,
    treatment text,
    CONSTRAINT lab_requests_status_check CHECK (((status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('COMPLETED'::character varying)::text])))
);


ALTER TABLE public.lab_requests OWNER TO postgres;

--
-- Name: medical_diagnoses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.medical_diagnoses (
    id uuid NOT NULL,
    active boolean NOT NULL,
    arabic_name character varying(255) NOT NULL,
    created_at timestamp(6) with time zone,
    description text,
    english_name character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone
);


ALTER TABLE public.medical_diagnoses OWNER TO postgres;

--
-- Name: medical_records; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.medical_records (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    diagnosis character varying(255) NOT NULL,
    notes character varying(255),
    treatment character varying(255),
    updated_at timestamp(6) with time zone,
    doctor_id uuid NOT NULL,
    member_id uuid NOT NULL
);


ALTER TABLE public.medical_records OWNER TO postgres;

--
-- Name: medical_tests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.medical_tests (
    id uuid NOT NULL,
    active boolean NOT NULL,
    category character varying(255) NOT NULL,
    coverage_status character varying(255) NOT NULL,
    created_at timestamp(6) with time zone,
    price numeric(12,2),
    test_name character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone,
    coverage_percentage integer,
    CONSTRAINT medical_tests_coverage_status_check CHECK (((coverage_status)::text = ANY (ARRAY[('COVERED'::character varying)::text, ('REQUIRES_APPROVAL'::character varying)::text, ('NOT_COVERED'::character varying)::text])))
);


ALTER TABLE public.medical_tests OWNER TO postgres;

--
-- Name: medicine_prices; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.medicine_prices (
    id uuid NOT NULL,
    active boolean NOT NULL,
    composition text,
    coverage_status character varying(255) NOT NULL,
    created_at timestamp(6) with time zone,
    drug_name character varying(255) NOT NULL,
    generic_name text,
    price numeric(12,2) NOT NULL,
    type character varying(255),
    unit character varying(255),
    updated_at timestamp(6) with time zone,
    coverage_percentage integer,
    CONSTRAINT medicine_prices_coverage_status_check CHECK (((coverage_status)::text = ANY (ARRAY[('COVERED'::character varying)::text, ('REQUIRES_APPROVAL'::character varying)::text, ('NOT_COVERED'::character varying)::text])))
);


ALTER TABLE public.medicine_prices OWNER TO postgres;

--
-- Name: messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.messages (
    id uuid NOT NULL,
    content character varying(2000) NOT NULL,
    is_read boolean NOT NULL,
    sent_at timestamp(6) with time zone NOT NULL,
    conversation_id uuid NOT NULL,
    receiver_id uuid NOT NULL,
    sender_id uuid NOT NULL
);


ALTER TABLE public.messages OWNER TO postgres;

--
-- Name: notifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notifications (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    is_read boolean NOT NULL,
    message character varying(500) NOT NULL,
    recipient_id uuid NOT NULL,
    type character varying(255),
    sender_id uuid,
    replied boolean DEFAULT false NOT NULL,
    CONSTRAINT notifications_type_check CHECK (((type)::text = ANY (ARRAY[('MANUAL_MESSAGE'::character varying)::text, ('CLAIM'::character varying)::text, ('EMERGENCY'::character varying)::text, ('SYSTEM'::character varying)::text])))
);


ALTER TABLE public.notifications OWNER TO postgres;

--
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.password_reset_tokens (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    token character varying(255) NOT NULL,
    used boolean NOT NULL,
    username character varying(255) NOT NULL
);


ALTER TABLE public.password_reset_tokens OWNER TO postgres;

--
-- Name: policies; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.policies (
    id uuid NOT NULL,
    coverage_limit numeric(12,2) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    deductible numeric(12,2) NOT NULL,
    description text,
    emergency_rules text,
    end_date date NOT NULL,
    name character varying(120) NOT NULL,
    policy_no character varying(50) NOT NULL,
    start_date date NOT NULL,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT policies_status_check CHECK (((status)::text = ANY (ARRAY[('ACTIVE'::character varying)::text, ('INACTIVE'::character varying)::text, ('EXPIRED'::character varying)::text])))
);


ALTER TABLE public.policies OWNER TO postgres;

--
-- Name: prescription_items; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.prescription_items (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    dosage integer,
    expiry_date timestamp(6) with time zone,
    final_price double precision,
    pharmacist_price double precision,
    times_per_day integer,
    updated_at timestamp(6) with time zone,
    prescription_id uuid NOT NULL,
    price_list_id uuid NOT NULL,
    calculated_quantity integer,
    covered_quantity integer,
    dispensed_quantity integer,
    drug_form character varying(255),
    duration integer,
    pharmacist_price_per_unit double precision,
    union_price_per_unit double precision
);


ALTER TABLE public.prescription_items OWNER TO postgres;

--
-- Name: prescriptions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.prescriptions (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    dosage character varying(255),
    instructions character varying(255),
    status character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone,
    doctor_id uuid NOT NULL,
    member_id uuid NOT NULL,
    pharmacist_id uuid,
    notes character varying(255),
    total_price double precision,
    diagnosis text,
    is_chronic boolean,
    treatment text,
    CONSTRAINT prescriptions_status_check CHECK (((status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('VERIFIED'::character varying)::text, ('REJECTED'::character varying)::text])))
);


ALTER TABLE public.prescriptions OWNER TO postgres;

--
-- Name: price_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.price_list (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    notes character varying(500),
    price double precision NOT NULL,
    provider_type character varying(255) NOT NULL,
    service_code character varying(255),
    service_name character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone,
    service_details text,
    max_age integer,
    min_age integer,
    quantity integer,
    coverage_percentage integer,
    CONSTRAINT price_list_provider_type_check CHECK (((provider_type)::text = ANY (ARRAY[('PHARMACY'::character varying)::text, ('LAB'::character varying)::text, ('RADIOLOGY'::character varying)::text, ('DOCTOR'::character varying)::text])))
);


ALTER TABLE public.price_list OWNER TO postgres;

--
-- Name: price_list_allowed_genders; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.price_list_allowed_genders (
    price_list_id uuid NOT NULL,
    gender character varying(255)
);


ALTER TABLE public.price_list_allowed_genders OWNER TO postgres;

--
-- Name: price_list_allowed_specializations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.price_list_allowed_specializations (
    price_list_id uuid NOT NULL,
    specialization_id bigint NOT NULL
);


ALTER TABLE public.price_list_allowed_specializations OWNER TO postgres;

--
-- Name: provider_policies; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.provider_policies (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    provider_id uuid NOT NULL,
    service_name character varying(160) NOT NULL,
    negotiated_price numeric(12,2),
    coverage_percent numeric(5,2),
    effective_from date,
    effective_to date,
    active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.provider_policies OWNER TO postgres;

--
-- Name: radiology_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.radiology_requests (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    notes character varying(255),
    result_url character varying(255),
    status character varying(255),
    test_name character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone,
    doctor_id uuid NOT NULL,
    member_id uuid NOT NULL,
    radiologist_id uuid,
    entered_price double precision,
    approved_price double precision,
    union_price double precision,
    price_id uuid,
    test_id uuid,
    diagnosis text,
    treatment text,
    CONSTRAINT radiology_requests_status_check CHECK (((status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('COMPLETED'::character varying)::text])))
);


ALTER TABLE public.radiology_requests OWNER TO postgres;

--
-- Name: revoked_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.revoked_tokens (
    id uuid NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    revoked_at timestamp(6) with time zone NOT NULL,
    token character varying(500) NOT NULL
);


ALTER TABLE public.revoked_tokens OWNER TO postgres;

--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    name character varying(40) NOT NULL,
    CONSTRAINT roles_name_check CHECK (((name)::text = ANY (ARRAY[('INSURANCE_CLIENT'::character varying)::text, ('DOCTOR'::character varying)::text, ('PHARMACIST'::character varying)::text, ('LAB_TECH'::character varying)::text, ('EMERGENCY_MANAGER'::character varying)::text, ('INSURANCE_MANAGER'::character varying)::text, ('RADIOLOGIST'::character varying)::text, ('MEDICAL_ADMIN'::character varying)::text])))
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: search_profiles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.search_profiles (
    id uuid NOT NULL,
    address character varying(255),
    contact_info character varying(100),
    description character varying(500),
    location_lat double precision,
    location_lng double precision,
    name character varying(150) NOT NULL,
    type character varying(20) NOT NULL,
    owner_id uuid NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    rejection_reason character varying(300),
    clinic_registration character varying(300),
    id_or_passport_copy character varying(300),
    medical_license character varying(300),
    university_degree character varying(300),
    CONSTRAINT search_profiles_type_check CHECK (((type)::text = ANY (ARRAY[('CLINIC'::character varying)::text, ('PHARMACY'::character varying)::text, ('LAB'::character varying)::text, ('DOCTOR'::character varying)::text])))
);


ALTER TABLE public.search_profiles OWNER TO postgres;

--
-- Name: tests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tests (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    test_name character varying(255) NOT NULL,
    union_price double precision NOT NULL,
    updated_at timestamp(6) with time zone
);


ALTER TABLE public.tests OWNER TO postgres;

--
-- Name: v_role_id; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.v_role_id (
    id uuid
);


ALTER TABLE public.v_role_id OWNER TO postgres;

--
-- Name: visits; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.visits (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    doctor_specialization character varying(150) NOT NULL,
    notes text,
    updated_at timestamp(6) with time zone NOT NULL,
    visit_date date NOT NULL,
    visit_type character varying(20) NOT NULL,
    visit_year integer NOT NULL,
    doctor_id uuid NOT NULL,
    family_member_id uuid,
    patient_id uuid,
    previous_visit_id uuid,
    CONSTRAINT visits_visit_type_check CHECK (((visit_type)::text = ANY (ARRAY[('NORMAL'::character varying)::text, ('FOLLOW_UP'::character varying)::text])))
);


ALTER TABLE public.visits OWNER TO postgres;

--
-- Data for Name: annual_usage; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.annual_usage (id, client_id, year, service_type, total_amount, total_count, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: chronic_patient_schedules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.chronic_patient_schedules (id, amount, created_at, description, interval_months, is_active, lab_test_name, last_sent_at, medication_name, medication_quantity, next_send_date, notes, radiology_test_name, schedule_type, updated_at, patient_id) FROM stdin;
66666666-aaaa-aaaa-aaaa-666666666661	300	2025-12-20 21:18:53.395801+02	إعادة تعبئة شهرية للميتفورمين لإدارة السكري	1	t	\N	2026-01-16 21:18:53.395801+02	ميتفورمين 500 ملغ	60	2026-02-15	المريض يتناول قرصين يومياً مع الوجبات	\N	MEDICATION	2026-01-19 21:18:53.395801+02	88888888-8888-8888-8888-888888888884
66666666-aaaa-aaaa-aaaa-666666666662	70	2025-10-21 21:18:53.395801+03	مراقبة الهيموغلوبين السكري كل ثلاثة أشهر	3	t	الهيموغلوبين السكري (HbA1c)	2026-01-14 21:18:53.395801+02	\N	\N	2026-04-14	مراقبة التحكم في السكري كل 3 أشهر	\N	LAB_TEST	2026-01-19 21:18:53.395801+02	88888888-8888-8888-8888-888888888884
66666666-aaaa-aaaa-aaaa-666666666663	150	2025-07-23 21:18:53.395801+03	سونار البطن نصف سنوي لفحص الكبد/الكلى	6	t	\N	2026-01-12 21:18:53.395801+02	\N	\N	2026-07-11	فحص للكبد الدهني ومضاعفات الكلى	سونار البطن	RADIOLOGY_TEST	2026-01-19 21:18:53.395801+02	88888888-8888-8888-8888-888888888884
\.


--
-- Data for Name: claim_invoice_images; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.claim_invoice_images (claim_id, image_path) FROM stdin;
\.


--
-- Data for Name: claims; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.claims (id, approved_at, document_path, rejected_at, rejection_reason, status, submitted_at, member_id, policy_id, amount, description, diagnosis, doctor_name, invoice_image_path, provider_name, service_date, treatment_details, admin_reviewed_at, medical_reviewed_at, admin_reviewer_id, medical_reviewer_id, client_pay_amount, coverage_message, coverage_percent_used, insurance_covered_amount, max_coverage_used, is_covered, emergency, member_name) FROM stdin;
33333333-aaaa-aaaa-aaaa-333333333331	\N	\N	\N	\N	PENDING	2026-01-17 21:18:53.395801+02	88888888-8888-8888-8888-888888888881	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	150	استشارة عامة وفحوصات دم	تحقيق في سبب الإرهاق	د. سارة عبدالله	\N	عيادة بيرزيت	2026-01-17	وصف مكملات فيتامينات	\N	\N	\N	\N	30.00	تم تطبيق التغطية القياسية	80.00	120.00	2000.00	t	f	أحمد محمود
33333333-aaaa-aaaa-aaaa-333333333332	\N	\N	\N	\N	PENDING	2026-01-18 21:18:53.395801+02	88888888-8888-8888-8888-888888888882	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	80	استشارة جلدية	التهاب الجلد التماسي	د. خالد عمر	\N	عيادة بيرزيت للجلدية	2026-01-18	علاج موضعي	\N	\N	\N	\N	16.00	تم تطبيق التغطية القياسية	80.00	64.00	2000.00	t	f	لينا حسن
33333333-aaaa-aaaa-aaaa-333333333333	\N	\N	\N	\N	APPROVED_BY_MEDICAL	2026-01-14 21:18:53.395801+02	88888888-8888-8888-8888-888888888883	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	250	استشارة قلب وتخطيط قلب	خفقان القلب	د. محمد ناصر	\N	مركز القلب	2026-01-14	تم إجراء تخطيط القلب، نصح بتغيير نمط الحياة	\N	2026-01-16 21:18:53.395801+02	\N	22222222-2222-2222-2222-222222222222	50.00	استشارة أخصائي مغطاة بنسبة 80%	80.00	200.00	3000.00	t	f	كريم نصار
33333333-aaaa-aaaa-aaaa-333333333334	\N	\N	\N	\N	AWAITING_ADMIN_REVIEW	2026-01-15 21:18:53.395801+02	88888888-8888-8888-8888-888888888884	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	370	إدارة السكري - استشارة وفحص الهيموغلوبين السكري	داء السكري من النوع 2	د. طارق الخطيب	\N	عيادة الباطنية	2026-01-15	استمرار ميتفورمين، إرشاد غذائي	\N	2026-01-17 21:18:53.395801+02	\N	22222222-2222-2222-2222-222222222222	74.00	تغطية الحالة المزمنة	80.00	296.00	5000.00	t	f	سمير علي
33333333-aaaa-aaaa-aaaa-333333333335	2026-01-09 21:18:53.395801+02	\N	\N	\N	APPROVED	2026-01-04 21:18:53.395801+02	88888888-8888-8888-8888-888888888881	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	200	استشارة سابقة وعمل مخبري	فحص سنوي	د. سارة عبدالله	\N	عيادة بيرزيت	2026-01-04	جميع النتائج طبيعية	2026-01-09 21:18:53.395801+02	2026-01-07 21:18:53.395801+02	11111111-1111-1111-1111-111111111111	22222222-2222-2222-2222-222222222222	40.00	تغطية الرعاية الوقائية	80.00	160.00	2000.00	t	f	أحمد محمود
33333333-aaaa-aaaa-aaaa-333333333336	2026-01-11 21:18:53.395801+02	\N	\N	\N	APPROVED	2026-01-07 21:18:53.395801+02	88888888-8888-8888-8888-888888888883	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	350	استشارة أطفال للطفل	التهاب الجهاز التنفسي العلوي	د. فاطمة حسن	\N	عيادة الأطفال	2026-01-07	وصف مضادات حيوية، توقع الشفاء	2026-01-11 21:18:53.395801+02	2026-01-09 21:18:53.395801+02	11111111-1111-1111-1111-111111111111	22222222-2222-2222-2222-222222222222	70.00	تغطية أفراد العائلة	80.00	280.00	2000.00	t	f	يزن كريم
33333333-aaaa-aaaa-aaaa-333333333337	\N	\N	2026-01-14 21:18:53.395801+02	الإجراءات التجميلية غير مغطاة بموجب البوليصة	REJECTED	2026-01-09 21:18:53.395801+02	88888888-8888-8888-8888-888888888882	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	500	إجراء جلدي تجميلي	تحسين تجميلي	مزود خارجي	\N	عيادة خاصة	2026-01-09	علاج بوتوكس	\N	2026-01-12 21:18:53.395801+02	\N	22222222-2222-2222-2222-222222222222	500.00	غير مغطى - إجراء تجميلي	0.00	0.00	0.00	f	f	لينا حسن
33333333-aaaa-aaaa-aaaa-333333333338	\N	\N	2026-01-16 21:18:53.395801+02	تم تقديم الخدمة خارج الشبكة بدون إذن مسبق	REJECTED	2026-01-11 21:18:53.395801+02	88888888-8888-8888-8888-888888888881	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	800	استشارة أخصائي خارج الشبكة	رأي ثاني	طبيب خارجي	\N	مستشفى خاص	2026-01-11	لم يتم الحصول على إذن مسبق	\N	2026-01-14 21:18:53.395801+02	\N	22222222-2222-2222-2222-222222222222	800.00	مرفوض - خارج الشبكة	0.00	0.00	0.00	f	f	أحمد محمود
33333333-aaaa-aaaa-aaaa-333333333339	2026-01-17 21:18:53.395801+02	\N	\N	\N	APPROVED	2026-01-13 21:18:53.395801+02	88888888-8888-8888-8888-888888888883	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	1200	زيارة غرفة الطوارئ - رد فعل تحسسي شديد	صدمة تحسسية	فريق الطوارئ	\N	طوارئ المستشفى	2026-01-13	تم إعطاء الإبينفرين، مراقبة لمدة 6 ساعات	2026-01-17 21:18:53.395801+02	2026-01-15 21:18:53.395801+02	11111111-1111-1111-1111-111111111111	22222222-2222-2222-2222-222222222222	120.00	تغطية الطوارئ بنسبة 90%	90.00	1080.00	10000.00	t	t	كريم نصار
\.


--
-- Data for Name: client_chronic_diseases; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_chronic_diseases (client_id, disease) FROM stdin;
88888888-8888-8888-8888-888888888884	DIABETES
88888888-8888-8888-8888-888888888884	HYPERTENSION
\.


--
-- Data for Name: client_chronic_documents; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_chronic_documents (client_id, document_path) FROM stdin;
\.


--
-- Data for Name: client_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_roles (client_id, role_id) FROM stdin;
708a20c4-dc93-4913-9fda-fde4f2a74d33	5342bc8b-7238-47d2-871e-800c7a0fd9b2
b8d24b9e-da85-4a53-aea5-b65abf94daf2	09893f46-d674-4c7b-9333-ad4821c50151
254765f1-2e18-46c9-a77e-d9cafaff38f8	09893f46-d674-4c7b-9333-ad4821c50151
3948392b-2d34-4f39-b342-3ceb7fb941c7	09893f46-d674-4c7b-9333-ad4821c50151
07ddd467-36ca-4879-b77f-1c1fb2686153	09893f46-d674-4c7b-9333-ad4821c50151
85a72505-648e-40ee-9e68-b68441a4f122	09893f46-d674-4c7b-9333-ad4821c50151
551be117-7c0b-4f5a-80f6-d0966b22a087	1c9ebceb-b151-4d57-91cc-a6c69de28c56
11111111-1111-1111-1111-111111111111	5342bc8b-7238-47d2-871e-800c7a0fd9b2
22222222-2222-2222-2222-222222222222	9489a6f6-e315-495c-8215-43e7fae1e49e
44444444-4444-4444-4444-444444444444	09893f46-d674-4c7b-9333-ad4821c50151
44444444-4444-4444-4444-444444444445	09893f46-d674-4c7b-9333-ad4821c50151
44444444-4444-4444-4444-444444444446	09893f46-d674-4c7b-9333-ad4821c50151
44444444-4444-4444-4444-444444444447	09893f46-d674-4c7b-9333-ad4821c50151
44444444-4444-4444-4444-444444444448	09893f46-d674-4c7b-9333-ad4821c50151
44444444-4444-4444-4444-444444444449	09893f46-d674-4c7b-9333-ad4821c50151
44444444-4444-4444-4444-44444444444a	09893f46-d674-4c7b-9333-ad4821c50151
44444444-4444-4444-4444-44444444444b	09893f46-d674-4c7b-9333-ad4821c50151
44444444-4444-4444-4444-44444444444c	09893f46-d674-4c7b-9333-ad4821c50151
44444444-4444-4444-4444-44444444444d	09893f46-d674-4c7b-9333-ad4821c50151
44444444-4444-4444-4444-44444444444e	09893f46-d674-4c7b-9333-ad4821c50151
44444444-4444-4444-4444-44444444444f	09893f46-d674-4c7b-9333-ad4821c50151
55555555-5555-5555-5555-555555555555	6f632aff-382a-48a4-8fc5-cbe50fae8222
66666666-6666-6666-6666-666666666666	3a68cb10-6026-4b0d-b154-8a3cd81a18a7
77777777-7777-7777-7777-777777777777	3c4c7431-0c52-4e5a-b8d3-6d3ee591f0ed
88888888-8888-8888-8888-888888888881	1c9ebceb-b151-4d57-91cc-a6c69de28c56
88888888-8888-8888-8888-888888888882	1c9ebceb-b151-4d57-91cc-a6c69de28c56
88888888-8888-8888-8888-888888888883	1c9ebceb-b151-4d57-91cc-a6c69de28c56
88888888-8888-8888-8888-888888888884	1c9ebceb-b151-4d57-91cc-a6c69de28c56
\.


--
-- Data for Name: client_university_cards; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_university_cards (client_id, image_path) FROM stdin;
\.


--
-- Data for Name: clients; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.clients (id, created_at, email, full_name, password_hash, phone, requested_role, role_request_status, status, updated_at, university_card_image, policy_id, clinic_location, department, employee_id, faculty, lab_code, lab_location, lab_name, pharmacy_code, pharmacy_location, pharmacy_name, specialization, radiology_code, radiology_location, radiology_name, date_of_birth, email_verification_code, email_verification_expiry, email_verified, gender, national_id) FROM stdin;
708a20c4-dc93-4913-9fda-fde4f2a74d33	2026-01-19 22:00:52.664507+02	manager@insurance.com	System Manager	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0512345678	\N	APPROVED	ACTIVE	2026-01-19 22:00:52.664507+02	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1990-01-01	\N	\N	t	M	123456789
b8d24b9e-da85-4a53-aea5-b65abf94daf2	2026-01-19 22:00:52.798901+02	dr.ahmad.cardio@hospital.com	Dr. Ahmad Nasser	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591111111	DOCTOR	APPROVED	ACTIVE	2026-01-19 22:00:52.798901+02	\N	\N	\N	Cardiology Department	EMP111	Medicine	\N	\N	\N	\N	\N	\N	Cardiology	\N	\N	\N	1980-03-15	\N	\N	t	M	DOC111222
254765f1-2e18-46c9-a77e-d9cafaff38f8	2026-01-19 22:00:52.895803+02	dr.sara.general@hospital.com	Dr. Sara Mahmoud	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0592222222	DOCTOR	APPROVED	ACTIVE	2026-01-19 22:00:52.895803+02	\N	\N	\N	General Medicine	EMP222	Medicine	\N	\N	\N	\N	\N	\N	General Practice	\N	\N	\N	1985-07-22	\N	\N	t	F	DOC222333
3948392b-2d34-4f39-b342-3ceb7fb941c7	2026-01-19 22:00:52.984796+02	dr.khaled.internal@hospital.com	Dr. Khaled Abu-Salem	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0593333333	DOCTOR	APPROVED	ACTIVE	2026-01-19 22:00:52.984796+02	\N	\N	\N	Internal Medicine Department	EMP333	Medicine	\N	\N	\N	\N	\N	\N	Internal Medicine	\N	\N	\N	1978-11-08	\N	\N	t	M	DOC333444
44444444-4444-4444-4444-44444444444a	2026-01-19 21:18:53.395801+02	dr.ent@birzeit.edu	د. ليلى قاسم	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000010	DOCTOR	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	عيادة الأنف والأذن والحنجرة، الطابق 3	أنف وأذن وحنجرة	DOC007	الطب	\N	\N	\N	\N	\N	\N	ENT (Ear, Nose, Throat)	\N	\N	\N	1983-12-05	\N	\N	t	FEMALE	900000010
44444444-4444-4444-4444-44444444444b	2026-01-19 21:18:53.395801+02	dr.neuro@birzeit.edu	د. هناء محمود	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000011	DOCTOR	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	قسم الأمراض العصبية	الأمراض العصبية	DOC008	الطب	\N	\N	\N	\N	\N	\N	Neurology	\N	\N	\N	1977-05-18	\N	\N	t	FEMALE	900000011
44444444-4444-4444-4444-44444444444c	2026-01-19 21:18:53.395801+02	dr.gyn@birzeit.edu	د. أمل إبراهيم	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000012	DOCTOR	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	مركز صحة المرأة	أمراض النساء	DOC009	الطب	\N	\N	\N	\N	\N	\N	Gynecology	\N	\N	\N	1981-03-27	\N	\N	t	FEMALE	900000012
44444444-4444-4444-4444-44444444444d	2026-01-19 21:18:53.395801+02	dr.internal@birzeit.edu	د. طارق الخطيب	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000013	DOCTOR	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	جناح الباطنية	الباطنية	DOC010	الطب	\N	\N	\N	\N	\N	\N	Internal Medicine	\N	\N	\N	1974-10-11	\N	\N	t	MALE	900000013
44444444-4444-4444-4444-44444444444e	2026-01-19 21:18:53.395801+02	dr.psych@birzeit.edu	د. ريم عوض	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000014	DOCTOR	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	مركز الصحة النفسية	الطب النفسي	DOC011	الطب	\N	\N	\N	\N	\N	\N	Psychiatry	\N	\N	\N	1979-01-23	\N	\N	t	FEMALE	900000014
44444444-4444-4444-4444-44444444444f	2026-01-19 21:18:53.395801+02	dr.uro@birzeit.edu	د. وليد صافي	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000015	DOCTOR	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	عيادة المسالك البولية	المسالك البولية	DOC012	الطب	\N	\N	\N	\N	\N	\N	Urology	\N	\N	\N	1975-06-30	\N	\N	t	MALE	900000015
55555555-5555-5555-5555-555555555555	2026-01-19 21:18:53.395801+02	pharmacist@birzeit.edu	نور الدين	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000016	PHARMACIST	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	\N	\N	\N	\N	\N	\N	\N	PH001	حرم الجامعة، مبنى د	صيدلية بيرزيت	\N	\N	\N	\N	1990-08-15	\N	\N	t	MALE	900000016
66666666-6666-6666-6666-666666666666	2026-01-19 21:18:53.395801+02	labtech@birzeit.edu	مريم خليل	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000017	LAB_TECH	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	\N	\N	\N	\N	LAB001	المركز الطبي، الطابق السفلي	مختبر بيرزيت الطبي	\N	\N	\N	\N	\N	\N	\N	1988-04-20	\N	\N	t	FEMALE	900000017
77777777-7777-7777-7777-777777777777	2026-01-19 21:18:53.395801+02	radiologist@birzeit.edu	عمر صالح	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000018	RADIOLOGIST	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	RAD001	المركز الطبي، الطابق 1	مركز بيرزيت للتصوير	1983-12-10	\N	\N	t	MALE	900000018
88888888-8888-8888-8888-888888888881	2026-01-19 21:18:53.395801+02	student1@birzeit.edu	أحمد محمود	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000019	INSURANCE_CLIENT	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	\N	علوم الحاسوب	STU001	الهندسة	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2000-05-15	\N	\N	t	MALE	900000019
88888888-8888-8888-8888-888888888883	2026-01-19 21:18:53.395801+02	employee@birzeit.edu	كريم نصار	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000021	INSURANCE_CLIENT	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	\N	قسم تكنولوجيا المعلومات	EMP100	الإدارة	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1985-11-08	\N	\N	t	MALE	900000021
88888888-8888-8888-8888-888888888884	2026-01-19 21:18:53.395801+02	chronic@birzeit.edu	سمير علي	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000022	INSURANCE_CLIENT	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	\N	المالية	EMP101	إدارة الأعمال	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1970-07-14	\N	\N	t	MALE	900000022
07ddd467-36ca-4879-b77f-1c1fb2686153	2026-01-19 22:00:53.073449+02	dr.hana.neuro@hospital.com	Dr. Hana Qassem	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0594444444	DOCTOR	APPROVED	ACTIVE	2026-01-19 22:00:53.073449+02	\N	\N	\N	Neurology Department	EMP444	Medicine	\N	\N	\N	\N	\N	\N	Neurology	\N	\N	\N	1982-05-30	\N	\N	t	F	DOC444555
85a72505-648e-40ee-9e68-b68441a4f122	2026-01-19 22:00:53.168382+02	dr.youssef.endo@hospital.com	Dr. Youssef Barakat	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0595555555	DOCTOR	APPROVED	ACTIVE	2026-01-19 22:00:53.168382+02	\N	\N	\N	Endocrinology & Diabetes	EMP555	Medicine	\N	\N	\N	\N	\N	\N	Endocrinology	\N	\N	\N	1975-09-12	\N	\N	t	M	DOC555666
551be117-7c0b-4f5a-80f6-d0966b22a087	2026-01-19 22:00:53.259551+02	john.smith@example.com	John Smith	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0501234567	INSURANCE_CLIENT	APPROVED	ACTIVE	2026-01-19 22:00:53.259551+02	\N	\N	\N	\N	EMP001	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1985-05-15	\N	\N	t	M	987654321
37d3331f-6375-4f91-b1c2-b98a8095fbee	2026-01-19 22:00:53.346541+02	test.doctor@example.com	Dr. Ahmed Hassan	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0521111111	DOCTOR	PENDING	INACTIVE	2026-01-19 22:00:53.346541+02	\N	\N	\N	Cardiology Department	\N	Medicine	\N	\N	\N	\N	\N	\N	Cardiology	\N	\N	\N	1988-03-20	\N	\N	t	M	111222333
04844f92-a2b5-435b-9847-316fbb27cabe	2026-01-19 22:00:53.435038+02	test.pharmacist@example.com	Sarah Johnson	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0522222222	PHARMACIST	PENDING	INACTIVE	2026-01-19 22:00:53.435038+02	\N	\N	\N	\N	\N	\N	\N	\N	\N	PH001	Ramallah, Main Street	Care Plus Pharmacy	\N	\N	\N	\N	1992-07-10	\N	\N	t	F	222333444
570b6639-7de3-42cd-ba78-d9e037010b61	2026-01-19 22:00:53.519931+02	test.labtech@example.com	Mohammad Ali	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0523333333	LAB_TECH	PENDING	INACTIVE	2026-01-19 22:00:53.519931+02	\N	\N	\N	\N	\N	\N	LAB001	Nablus, Medical District	City Lab Center	\N	\N	\N	\N	\N	\N	\N	1995-11-05	\N	\N	t	M	333444555
7078ea16-baeb-417c-9099-332ec5d34c08	2026-01-19 22:00:53.605713+02	test.radiologist@example.com	Dr. Fatima Khalil	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0524444444	RADIOLOGIST	PENDING	INACTIVE	2026-01-19 22:00:53.605713+02	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	RAD001	Bethlehem, Hospital Road	Advanced Imaging Center	1990-01-25	\N	\N	t	F	444555666
e113c547-3d59-4ea3-b290-19797452b678	2026-01-19 22:00:53.695489+02	test.client@example.com	Omar Ibrahim	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0525555555	INSURANCE_CLIENT	PENDING	INACTIVE	2026-01-19 22:00:53.695489+02	\N	\N	\N	\N	EMP555	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1998-09-12	\N	\N	t	M	555666777
e94fcbea-05b1-4c25-afee-ef606d9d68e1	2026-01-19 22:00:53.780042+02	test.doctor2@example.com	Dr. Layla Mansour	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0526666666	DOCTOR	PENDING	INACTIVE	2026-01-19 22:00:53.780042+02	\N	\N	\N	Pediatrics Department	\N	Medicine	\N	\N	\N	\N	\N	\N	Pediatrics	\N	\N	\N	1987-04-08	\N	\N	t	F	666777888
99999999-9999-9999-9999-999999999999	2026-01-19 21:18:53.395801+02	pending@birzeit.edu	رانيا قاسم	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000023	INSURANCE_CLIENT	PENDING	INACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	\N	القانون	STU003	الحقوق	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2001-02-28	\N	\N	t	FEMALE	900000023
33333333-3333-3333-3333-333333333333	2026-01-19 21:18:53.395801+02	emergency@birzeit.edu	سامي خليل	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000003	\N	NONE	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	\N	خدمات الطوارئ	EMG001	الطوارئ	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1980-01-10	\N	\N	t	MALE	900000003
11111111-1111-1111-1111-111111111111	2026-01-19 21:18:53.395801+02	manager@birzeit.edu	أحمد المصري	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000001	INSURANCE_MANAGER	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	\N	قسم التأمين	MGR001	الإدارة	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1975-03-15	\N	\N	t	MALE	900000001
22222222-2222-2222-2222-222222222222	2026-01-19 21:18:53.395801+02	medicaladmin@birzeit.edu	د. رامي يوسف	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000002	MEDICAL_ADMIN	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	\N	الشؤون الطبية	MED001	الطب	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1970-06-20	\N	\N	t	MALE	900000002
44444444-4444-4444-4444-444444444444	2026-01-19 21:18:53.395801+02	dr.general@birzeit.edu	د. سارة عبدالله	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000004	DOCTOR	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	عيادة الجامعة، مبنى أ	الطب العام	DOC001	الطب	\N	\N	\N	\N	\N	\N	General Practice	\N	\N	\N	1985-04-12	\N	\N	t	FEMALE	900000004
44444444-4444-4444-4444-444444444445	2026-01-19 21:18:53.395801+02	dr.cardio@birzeit.edu	د. محمد ناصر	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000005	DOCTOR	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	مركز القلب، الطابق 2	أمراض القلب	DOC002	الطب	\N	\N	\N	\N	\N	\N	Cardiology	\N	\N	\N	1978-08-25	\N	\N	t	MALE	900000005
44444444-4444-4444-4444-444444444446	2026-01-19 21:18:53.395801+02	dr.pediatric@birzeit.edu	د. فاطمة حسن	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000006	DOCTOR	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	جناح الأطفال، غرفة 105	طب الأطفال	DOC003	الطب	\N	\N	\N	\N	\N	\N	Pediatrics	\N	\N	\N	1982-11-30	\N	\N	t	FEMALE	900000006
44444444-4444-4444-4444-444444444447	2026-01-19 21:18:53.395801+02	dr.derma@birzeit.edu	د. خالد عمر	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000007	DOCTOR	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	عيادة الجلدية، مبنى ب	الأمراض الجلدية	DOC004	الطب	\N	\N	\N	\N	\N	\N	Dermatology	\N	\N	\N	1980-02-14	\N	\N	t	MALE	900000007
44444444-4444-4444-4444-444444444448	2026-01-19 21:18:53.395801+02	dr.ortho@birzeit.edu	د. نادية سالم	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000008	DOCTOR	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	مركز العظام، مبنى ج	جراحة العظام	DOC005	الطب	\N	\N	\N	\N	\N	\N	Orthopedics	\N	\N	\N	1976-07-08	\N	\N	t	FEMALE	900000008
44444444-4444-4444-4444-444444444449	2026-01-19 21:18:53.395801+02	dr.eye@birzeit.edu	د. يوسف بركات	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000009	DOCTOR	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	مركز العناية بالعيون	طب العيون	DOC006	الطب	\N	\N	\N	\N	\N	\N	Ophthalmology	\N	\N	\N	1979-09-22	\N	\N	t	MALE	900000009
88888888-8888-8888-8888-888888888882	2026-01-19 21:18:53.395801+02	student2@birzeit.edu	لينا حسن	$2b$10$cCMfdO9Hc3zybuEYIfI.guUWFsvjSAMO05vWG9qsJw.GDOlxOTD8O	0591000020	INSURANCE_CLIENT	APPROVED	ACTIVE	2026-01-19 21:18:53.395801+02	\N	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	\N	الأحياء	STU002	العلوم	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1999-03-22	\N	\N	t	FEMALE	900000020
\.


--
-- Data for Name: conversations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.conversations (id, last_updated, user1_id, user2_id, conversation_type) FROM stdin;
\.


--
-- Data for Name: coverage_usage; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.coverage_usage (id, client_id, provider_specialization, service_type, usage_date, year, visit_count, amount_used, created_at) FROM stdin;
\.


--
-- Data for Name: coverages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.coverages (id, amount, coverage_percent, coverage_type, is_covered, description, emergency_eligible, max_limit, minimum_deductible, requires_referral, service_name, policy_id, allowed_gender, min_age, max_age, frequency_limit, frequency_period) FROM stdin;
cccccccc-cccc-cccc-cccc-cccccccccc01	100.00	80.00	OUTPATIENT	t	استشارة طبيب عام	f	2000.00	10.00	f	استشارة الطب العام	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	ALL	0	120	12	YEAR
cccccccc-cccc-cccc-cccc-cccccccccc02	150.00	70.00	OUTPATIENT	t	استشارة طبيب أخصائي	f	3000.00	20.00	t	استشارة الأخصائي	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	ALL	0	120	8	YEAR
cccccccc-cccc-cccc-cccc-cccccccccc03	500.00	90.00	EMERGENCY	t	خدمات غرفة الطوارئ	t	10000.00	50.00	f	خدمات الطوارئ	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	ALL	0	120	\N	\N
cccccccc-cccc-cccc-cccc-cccccccccc04	200.00	80.00	LAB	t	الفحوصات المخبرية والتشخيصية	f	5000.00	15.00	f	خدمات المختبر	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	ALL	0	120	\N	\N
cccccccc-cccc-cccc-cccc-cccccccccc05	300.00	75.00	XRAY	t	خدمات الأشعة والتصوير	f	8000.00	25.00	t	خدمات الأشعة	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	ALL	0	120	\N	\N
cccccccc-cccc-cccc-cccc-cccccccccc06	1000.00	85.00	INPATIENT	t	الإقامة في المستشفى	t	25000.00	100.00	t	الإقامة في المستشفى	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	ALL	0	120	\N	\N
\.


--
-- Data for Name: doctor_medicine_assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.doctor_medicine_assignments (id, active, assigned_at, max_daily_prescriptions, max_quantity_per_prescription, notes, specialization, updated_at, assigned_by, doctor_id, medicine_id) FROM stdin;
9518341c-e171-4856-b814-182f0659c5f0	t	2026-01-19 22:09:58.986485+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.986485+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	254765f1-2e18-46c9-a77e-d9cafaff38f8	e7ece66f-cf2a-4913-9a15-303cf17256e1
6700b928-08d6-44f5-b5ac-3dbdfe552d47	t	2026-01-19 22:09:58.98723+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.98723+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	254765f1-2e18-46c9-a77e-d9cafaff38f8	8f7021f3-da71-420f-80ab-6d0d097411cf
f652026e-8016-416f-8fc7-9794d5b43076	t	2026-01-19 22:09:58.98723+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.98723+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	254765f1-2e18-46c9-a77e-d9cafaff38f8	a94d612f-7c1d-4012-823b-7cf240389c1e
61029800-766e-41a2-b7f1-ca141ea64ca5	t	2026-01-19 22:09:58.98723+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.98723+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	254765f1-2e18-46c9-a77e-d9cafaff38f8	ac48f27f-da1c-442d-9727-1ac559abe5d4
e68428df-e880-4b3e-876f-d71d3f4e9832	t	2026-01-19 22:09:58.98723+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.98723+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	254765f1-2e18-46c9-a77e-d9cafaff38f8	26ee62a2-9099-4ff8-b649-9f6574a5ce94
83b93169-e6d5-4a68-9699-232b9911da89	t	2026-01-19 22:09:58.98723+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.98723+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	254765f1-2e18-46c9-a77e-d9cafaff38f8	971f4fdd-5602-457b-a26d-69a2c5d90ba9
a396c58c-101c-410f-9018-efa0e7d5e6bd	t	2026-01-19 22:09:58.988254+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.988254+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	254765f1-2e18-46c9-a77e-d9cafaff38f8	d573b094-06dc-4472-8769-7f1e4db16033
ac235ed3-c4a5-4c43-851a-2d020d46c8e3	t	2026-01-19 22:09:58.988254+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.988254+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	254765f1-2e18-46c9-a77e-d9cafaff38f8	e533f6c5-2a75-49d0-81fb-20720d01b8ed
55747040-e673-46a6-a9fa-9fb4f49db63d	t	2026-01-19 22:09:58.988254+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.988254+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	254765f1-2e18-46c9-a77e-d9cafaff38f8	0532b15f-6ff6-4e8b-b2f6-303970f3c60d
2a6d168f-1c58-4845-8bc6-c935121728c4	t	2026-01-19 22:09:58.988254+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.988254+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	254765f1-2e18-46c9-a77e-d9cafaff38f8	6be9e52f-08ad-48dc-bfcb-ce473687ce79
c2ec12d1-bad3-41e2-adf4-ff0de204df53	t	2026-01-19 22:09:58.988254+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.988254+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	3948392b-2d34-4f39-b342-3ceb7fb941c7	e7ece66f-cf2a-4913-9a15-303cf17256e1
dc4903ba-5cb5-4cc4-8b57-8426b44f5b6e	t	2026-01-19 22:09:58.989237+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.989237+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	3948392b-2d34-4f39-b342-3ceb7fb941c7	8f7021f3-da71-420f-80ab-6d0d097411cf
d6110618-8b39-49fe-810e-423de011bd52	t	2026-01-19 22:09:58.989237+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.989237+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	3948392b-2d34-4f39-b342-3ceb7fb941c7	a94d612f-7c1d-4012-823b-7cf240389c1e
9a0b48d1-ac0c-442a-8c98-4bb2d53b3859	t	2026-01-19 22:09:58.989237+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.989237+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	3948392b-2d34-4f39-b342-3ceb7fb941c7	ac48f27f-da1c-442d-9727-1ac559abe5d4
45161aad-3a83-4ed3-aac2-0d7ae20ad6d9	t	2026-01-19 22:09:58.989237+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.989237+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	3948392b-2d34-4f39-b342-3ceb7fb941c7	26ee62a2-9099-4ff8-b649-9f6574a5ce94
4cd855fb-38d1-4765-a5ab-1b92f70098ec	t	2026-01-19 22:09:58.990224+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.990224+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	3948392b-2d34-4f39-b342-3ceb7fb941c7	971f4fdd-5602-457b-a26d-69a2c5d90ba9
414aea29-6a1a-4077-a9e1-867e06b79737	t	2026-01-19 22:09:58.990224+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.990224+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	3948392b-2d34-4f39-b342-3ceb7fb941c7	d573b094-06dc-4472-8769-7f1e4db16033
b8c6a7a9-a275-473d-ba51-5aa52d8017b0	t	2026-01-19 22:09:58.990224+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.990224+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	3948392b-2d34-4f39-b342-3ceb7fb941c7	e533f6c5-2a75-49d0-81fb-20720d01b8ed
7c53a640-d01d-4b42-9221-0e32c3fdf632	t	2026-01-19 22:09:58.990224+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.990224+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	3948392b-2d34-4f39-b342-3ceb7fb941c7	0532b15f-6ff6-4e8b-b2f6-303970f3c60d
92e3f97a-25e0-4923-a106-06ede31985bd	t	2026-01-19 22:09:58.990224+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.990224+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	3948392b-2d34-4f39-b342-3ceb7fb941c7	6be9e52f-08ad-48dc-bfcb-ce473687ce79
455ff00c-0f41-437b-9bb9-9358788e1ac6	t	2026-01-19 22:09:58.992302+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.992302+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-444444444444	e7ece66f-cf2a-4913-9a15-303cf17256e1
3a9bca7f-e12b-45f7-a56e-bfcd22fb8ae2	t	2026-01-19 22:09:58.992523+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.992523+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-444444444444	8f7021f3-da71-420f-80ab-6d0d097411cf
1c6e0769-6528-418a-aaef-d8d16a5a0f87	t	2026-01-19 22:09:58.992523+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.992523+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-444444444444	a94d612f-7c1d-4012-823b-7cf240389c1e
e764c883-26a5-469b-955d-f40123fecc5b	t	2026-01-19 22:09:58.992523+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.992523+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-444444444444	ac48f27f-da1c-442d-9727-1ac559abe5d4
c9062aed-da51-4ab4-a3f8-614a723d7bf7	t	2026-01-19 22:09:58.992523+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.992523+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-444444444444	26ee62a2-9099-4ff8-b649-9f6574a5ce94
6a362f44-0d47-4407-aaa3-3cffe214776f	t	2026-01-19 22:09:58.992523+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.992523+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-444444444444	971f4fdd-5602-457b-a26d-69a2c5d90ba9
8bc135ff-2f0b-4e00-97f9-551067905e44	t	2026-01-19 22:09:58.992523+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.992523+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-444444444444	d573b094-06dc-4472-8769-7f1e4db16033
aef2b5ff-c2c3-4122-acac-735b9bb83609	t	2026-01-19 22:09:58.992523+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.992523+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-444444444444	e533f6c5-2a75-49d0-81fb-20720d01b8ed
cc328382-8238-4bee-b2d1-aa5963cf041e	t	2026-01-19 22:09:58.992523+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.992523+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-444444444444	0532b15f-6ff6-4e8b-b2f6-303970f3c60d
229d8800-bdbd-4a1b-8f2f-0aab427b62b6	t	2026-01-19 22:09:58.993521+02	20	30	Auto-assigned during system initialization	General Practice	2026-01-19 22:09:58.993521+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-444444444444	6be9e52f-08ad-48dc-bfcb-ce473687ce79
b72cb272-8831-4152-8104-3e250f2d2710	t	2026-01-19 22:09:58.994519+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.994519+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-44444444444d	e7ece66f-cf2a-4913-9a15-303cf17256e1
b0bf3afc-8fc5-4c30-8618-6bb376fa87fc	t	2026-01-19 22:09:58.994519+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.994519+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-44444444444d	8f7021f3-da71-420f-80ab-6d0d097411cf
9dcc1817-a6e2-449b-9c8c-1ca13d062d8c	t	2026-01-19 22:09:58.994519+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.994519+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-44444444444d	a94d612f-7c1d-4012-823b-7cf240389c1e
75d828d0-c244-468f-9f7b-459f6f00c8ae	t	2026-01-19 22:09:58.994519+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.994519+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-44444444444d	ac48f27f-da1c-442d-9727-1ac559abe5d4
79506a48-6a08-46e8-83d0-e06807f15cfa	t	2026-01-19 22:09:58.994519+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.994519+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-44444444444d	26ee62a2-9099-4ff8-b649-9f6574a5ce94
4a0d1230-2c41-46a8-aabc-bc375bd25b23	t	2026-01-19 22:09:58.994519+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.994519+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-44444444444d	971f4fdd-5602-457b-a26d-69a2c5d90ba9
39f5f3ae-dd70-4755-8830-2092910afc76	t	2026-01-19 22:09:58.994519+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.994519+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-44444444444d	d573b094-06dc-4472-8769-7f1e4db16033
38f6ad73-0c58-49f5-accb-650066989a4b	t	2026-01-19 22:09:58.994519+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.994519+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-44444444444d	e533f6c5-2a75-49d0-81fb-20720d01b8ed
270fb886-5663-4134-b04b-c1aab51d449e	t	2026-01-19 22:09:58.995515+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.995515+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-44444444444d	0532b15f-6ff6-4e8b-b2f6-303970f3c60d
116a53c5-bd9e-47bd-8f87-93324766b10a	t	2026-01-19 22:09:58.995515+02	20	30	Auto-assigned during system initialization	Internal Medicine	2026-01-19 22:09:58.995515+02	708a20c4-dc93-4913-9fda-fde4f2a74d33	44444444-4444-4444-4444-44444444444d	6be9e52f-08ad-48dc-bfcb-ce473687ce79
\.


--
-- Data for Name: doctor_procedures; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.doctor_procedures (id, active, category, coverage_status, created_at, price, procedure_name, updated_at, max_price, coverage_percentage) FROM stdin;
7c9dd525-4882-43bc-ad01-99d3c932d7a3	t	GENERAL	COVERED	2026-01-19 22:09:58.871292+02	30.00	ECG Normal	2026-01-19 22:09:58.871292+02	\N	100
3692853e-abcb-419f-848d-ac3ec42b1af3	t	GENERAL	COVERED	2026-01-19 22:09:58.875283+02	70.00	ECG with Report	2026-01-19 22:09:58.875283+02	\N	100
7bf632e8-dceb-49c3-a921-e84e21b8c108	t	GENERAL	COVERED	2026-01-19 22:09:58.875283+02	10.00	Injection (I.M)	2026-01-19 22:09:58.875283+02	\N	100
b9f7bdee-7a04-412e-a9bc-7af4cbf2ecb8	t	GENERAL	COVERED	2026-01-19 22:09:58.87628+02	20.00	I.V Line Set + Canula	2026-01-19 22:09:58.87628+02	\N	100
342fc183-f3e9-425f-bd78-453362107f98	t	GENERAL	COVERED	2026-01-19 22:09:58.87628+02	30.00	Dressing (Small)	2026-01-19 22:09:58.87628+02	45.00	100
04bc3d6f-f267-44dd-9cde-9528f1150ac0	t	GENERAL	COVERED	2026-01-19 22:09:58.87628+02	50.00	Dressing (Large)	2026-01-19 22:09:58.87628+02	70.00	100
c9c6aced-0dc8-4939-a142-5b16e690150a	t	CARDIOLOGY	COVERED	2026-01-19 22:09:58.87628+02	200.00	Echo Cardiogram	2026-01-19 22:09:58.87628+02	\N	100
eb834269-321d-42fa-8021-39cb79994a5b	t	CARDIOLOGY	COVERED	2026-01-19 22:09:58.87628+02	175.00	Stress Test	2026-01-19 22:09:58.87628+02	\N	100
4b0710b4-a4ce-4f61-8d31-daa407103e91	t	CARDIOLOGY	COVERED	2026-01-19 22:09:58.87628+02	175.00	Holter Monitor 24hr	2026-01-19 22:09:58.87628+02	\N	100
23ac4b24-0b69-408a-9ca6-ead064cfb05f	t	CARDIOLOGY	REQUIRES_APPROVAL	2026-01-19 22:09:58.87628+02	300.00	ABPM	2026-01-19 22:09:58.87628+02	\N	100
26b91536-0300-4b92-80c5-303ddc22cbee	t	SURGERY	REQUIRES_APPROVAL	2026-01-19 22:09:58.87628+02	150.00	Minor Surgery	2026-01-19 22:09:58.87628+02	500.00	100
a81511b5-d8d8-46ad-92b3-09adb333f997	t	SURGERY	REQUIRES_APPROVAL	2026-01-19 22:09:58.87628+02	500.00	Skin Biopsy	2026-01-19 22:09:58.87628+02	\N	100
63f3d038-bcbc-4c9a-9528-dfca9fdc173b	t	SURGERY	REQUIRES_APPROVAL	2026-01-19 22:09:58.87628+02	575.00	Lipoma Excision	2026-01-19 22:09:58.87628+02	\N	100
a28dff17-e908-40c1-a775-1e682fd07ada	t	SURGERY	COVERED	2026-01-19 22:09:58.877292+02	600.00	Ingrown Toenail Removal	2026-01-19 22:09:58.877292+02	\N	100
057e6c3e-7d7f-4478-b570-8dfedb199ed8	t	SURGERY	REQUIRES_APPROVAL	2026-01-19 22:09:58.877292+02	575.00	Sebaceous Cyst Excision	2026-01-19 22:09:58.877292+02	\N	100
cce5f41a-1a7c-408b-826f-c4b7fc65bf31	t	SURGERY	COVERED	2026-01-19 22:09:58.878137+02	300.00	Circumcision	2026-01-19 22:09:58.878137+02	\N	100
94819848-38fc-4093-8874-552073555943	t	ENT	COVERED	2026-01-19 22:09:58.878137+02	150.00	Nasal Cautery	2026-01-19 22:09:58.878137+02	\N	100
ab603455-aab6-4fe3-88f5-fcc7bf434290	t	ENT	COVERED	2026-01-19 22:09:58.878137+02	80.00	Ear Irrigation	2026-01-19 22:09:58.878137+02	\N	100
9bc25499-ee53-4510-82e7-2f340137cbc4	t	ENT	COVERED	2026-01-19 22:09:58.878137+02	150.00	Audiogram	2026-01-19 22:09:58.878137+02	\N	100
0f5dbca5-b092-4981-885a-9396c3750166	t	ENT	REQUIRES_APPROVAL	2026-01-19 22:09:58.878137+02	200.00	Laryngoscopy	2026-01-19 22:09:58.878137+02	\N	100
994e0099-2341-4e9f-9903-1d5758b2a1a9	t	ENT	COVERED	2026-01-19 22:09:58.878137+02	250.00	Foreign Body Removal	2026-01-19 22:09:58.878137+02	\N	100
ac95378c-8381-4cc9-8202-f10cb409441a	t	ORTHOPEDIC	COVERED	2026-01-19 22:09:58.878137+02	235.00	Cast (Below Elbow)	2026-01-19 22:09:58.878137+02	\N	100
212aa889-11e8-43e4-bd07-f00e67bb4aac	t	ORTHOPEDIC	COVERED	2026-01-19 22:09:58.878137+02	250.00	Cast (Above Elbow)	2026-01-19 22:09:58.878137+02	\N	100
f7b54f42-725c-497a-b63c-d3e3c0d13b7a	t	ORTHOPEDIC	COVERED	2026-01-19 22:09:58.879137+02	235.00	Cast (Below Knee)	2026-01-19 22:09:58.879137+02	\N	100
434efdbe-b021-4ab6-b723-b45325dd6a87	t	ORTHOPEDIC	COVERED	2026-01-19 22:09:58.879137+02	300.00	Cast (Above Knee)	2026-01-19 22:09:58.879137+02	\N	100
8f5c5dce-3013-4cb8-8549-34156f9bea8e	t	ORTHOPEDIC	COVERED	2026-01-19 22:09:58.879137+02	100.00	Joint Injection	2026-01-19 22:09:58.879137+02	\N	100
ddff7b98-caef-4021-a51a-cd38da87cdee	t	ORTHOPEDIC	REQUIRES_APPROVAL	2026-01-19 22:09:58.879137+02	130.00	Arthrocentesis	2026-01-19 22:09:58.879137+02	\N	100
138654d2-5c16-40a3-b99b-e7e4083b048f	t	OBGYN	COVERED	2026-01-19 22:09:58.879137+02	100.00	Pap Smear	2026-01-19 22:09:58.879137+02	\N	100
2087f537-3f57-4352-a726-61cd61f607a5	t	OBGYN	COVERED	2026-01-19 22:09:58.879137+02	300.00	IUD Insertion	2026-01-19 22:09:58.879137+02	\N	100
84880703-a032-45d4-b6be-715b87898603	t	OBGYN	COVERED	2026-01-19 22:09:58.879137+02	100.00	IUD Removal	2026-01-19 22:09:58.879137+02	\N	100
6a861ec4-bb88-41f5-be9c-aac6eca3a74e	t	OBGYN	REQUIRES_APPROVAL	2026-01-19 22:09:58.879137+02	250.00	Cervical Cautery	2026-01-19 22:09:58.879137+02	\N	100
23312a05-6154-4c2d-8854-76a151413521	t	OBGYN	COVERED	2026-01-19 22:09:58.880133+02	50.00	NST (Non-Stress Test)	2026-01-19 22:09:58.880133+02	\N	100
\.


--
-- Data for Name: doctor_specialization; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.doctor_specialization (id, consultation_price, diagnoses, display_name, gender_restriction, max_age, min_age, treatment_plans, diagnosis_treatment_mappings) FROM stdin;
1	50	{"نزلة برد",انفلونزا,صداع,حمى,إرهاق,حساسية,"التهابات بسيطة","آلام الظهر"}	طب عام	\N	120	0	{"راحة وسوائل","مسكنات ألم","مضادات حيوية","مضادات هيستامين","مكملات فيتامينات"}	\N
2	100	{"ارتفاع ضغط الدم","قصور القلب","اضطراب نظم القلب","مرض الشريان التاجي","ذبحة صدرية","احتشاء عضلة القلب"}	أمراض القلب	\N	120	18	{"حاصرات بيتا","مثبطات ACE",ستاتينات,"مميعات الدم","تعديل نمط الحياة"}	\N
3	60	{"حمى الأطفال","التهاب الأذن","التهاب اللوزتين","جدري الماء","مشاكل النمو","ربو الأطفال"}	طب الأطفال	\N	18	0	{"مضادات حيوية للأطفال","خافضات حرارة",لقاحات,"إرشادات غذائية",بخاخات}	\N
4	80	{"حب الشباب",أكزيما,صدفية,"طفح جلدي","عدوى فطرية","التهاب الجلد"}	الأمراض الجلدية	\N	120	0	{"كريمات موضعية","أدوية مضادة للفطريات","كريمات ستيرويد","مضادات حيوية","علاج ضوئي"}	\N
5	90	{كسور,"التهاب المفاصل","هشاشة العظام","إصابة الأربطة","آلام الظهر","آلام المفاصل"}	جراحة العظام	\N	120	0	{"علاج طبيعي","إدارة الألم",جراحة,تجبير,"مكملات كالسيوم"}	\N
6	70	{"إعتام عدسة العين",الجلوكوما,"التهاب الملتحمة","قصر النظر","طول النظر","جفاف العين"}	طب العيون	\N	120	0	{"قطرات عين","عدسات تصحيحية","جراحة ليزر","جراحة العين",أدوية}	\N
7	75	{"التهاب الجيوب الأنفية","التهاب اللوزتين","فقدان السمع",دوار,"لحمية أنفية","التهاب الحلق"}	أنف وأذن وحنجرة	\N	120	0	{"مضادات حيوية","بخاخ أنف",جراحة,"سماعات أذن","مزيلات احتقان"}	\N
8	120	{"صداع نصفي",صرع,"سكتة دماغية","مرض باركنسون","تصلب متعدد","اعتلال عصبي"}	الأمراض العصبية	\N	120	18	{"مضادات صرع","إدارة الألم","علاج طبيعي",أدوية,"إعادة تأهيل"}	\N
9	85	{"اضطرابات الدورة الشهرية","تكيس المبايض","بطانة الرحم المهاجرة","رعاية الحمل","سن اليأس",التهابات}	أمراض النساء	FEMALE	120	18	{"علاج هرموني","موانع حمل",جراحة,"رعاية ما قبل الولادة","مضادات حيوية"}	\N
10	80	{السكري,"ارتفاع ضغط الدم","اضطرابات الغدة الدرقية","فقر الدم","أمراض الكبد","أمراض الكلى"}	الباطنية	\N	120	18	{"علاج الأنسولين","أدوية ضغط الدم","أدوية الغدة الدرقية","مكملات الحديد","غسيل الكلى"}	\N
11	100	{اكتئاب,قلق,"اضطراب ثنائي القطب",فصام,"اضطراب ما بعد الصدمة","وسواس قهري"}	الطب النفسي	\N	120	18	{"مضادات اكتئاب","علاج نفسي","مثبتات مزاج","مضادات ذهان",استشارات}	\N
12	90	{"التهاب المسالك البولية","حصى الكلى","مشاكل البروستاتا","سلس البول","التهاب المثانة","ضعف الانتصاب"}	المسالك البولية	\N	120	18	{"مضادات حيوية",جراحة,أدوية,"تفتيت الحصى","تغيير نمط الحياة"}	\N
\.


--
-- Data for Name: doctor_specialization_allowed_genders; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.doctor_specialization_allowed_genders (specialization_id, gender) FROM stdin;
\.


--
-- Data for Name: doctor_test_assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.doctor_test_assignments (id, active, assigned_at, max_daily_requests, notes, specialization, test_type, updated_at, assigned_by, doctor_id, test_id) FROM stdin;
\.


--
-- Data for Name: emergency_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.emergency_requests (id, approved_at, contact_phone, description, incident_date, location, notes, rejected_at, rejection_reason, status, submitted_at, updated_at, created_at, doctor_id, member_id, family_member_id) FROM stdin;
44444444-aaaa-aaaa-aaaa-444444444441	\N	0591000019	ألم شديد في البطن، اشتباه بالتهاب الزائدة	2026-01-19	حرم الجامعة مبنى أ	المريض يعاني من ألم حاد منذ ساعتين	\N	\N	PENDING	2026-01-19 20:18:53.395801+02	2026-01-19 20:18:53.395801+02	2026-01-19 20:18:53.395801+02	\N	88888888-8888-8888-8888-888888888881	\N
44444444-aaaa-aaaa-aaaa-444444444442	2026-01-14 21:18:53.395801+02	0591000021	رد فعل تحسسي شديد للطعام	2026-01-13	مطعم قريب من الحرم الجامعي	تم إعطاء المريض الإبينفرين ونقله للطوارئ	\N	\N	APPROVED	2026-01-13 21:18:53.395801+02	2026-01-14 21:18:53.395801+02	2026-01-13 21:18:53.395801+02	44444444-4444-4444-4444-444444444444	88888888-8888-8888-8888-888888888883	\N
44444444-aaaa-aaaa-aaaa-444444444443	\N	0591000022	نوبة ألم مزمن في الظهر	2026-01-09	المنزل	المريض طلب تغطية طوارئ لحالة مستمرة	2026-01-11 21:18:53.395801+02	ليست حالة طوارئ حقيقية - الحالة المزمنة يجب إدارتها من خلال مواعيد منتظمة	REJECTED	2026-01-09 21:18:53.395801+02	2026-01-11 21:18:53.395801+02	2026-01-09 21:18:53.395801+02	\N	88888888-8888-8888-8888-888888888884	\N
\.


--
-- Data for Name: family_member_documents; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.family_member_documents (family_member_id, document_path) FROM stdin;
\.


--
-- Data for Name: family_members; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.family_members (id, created_at, date_of_birth, full_name, gender, insurance_number, national_id, relation, status, client_id) FROM stdin;
aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa	2026-01-19 21:18:53.395801+02	1988-06-20	منى كريم	FEMALE	INS-FAM-001	800000001	WIFE	APPROVED	88888888-8888-8888-8888-888888888883
aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab	2026-01-19 21:18:53.395801+02	2015-09-10	يزن كريم	MALE	INS-FAM-002	800000002	SON	APPROVED	88888888-8888-8888-8888-888888888883
aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaac	2026-01-19 21:18:53.395801+02	2018-03-05	ليان كريم	FEMALE	INS-FAM-003	800000003	DAUGHTER	APPROVED	88888888-8888-8888-8888-888888888883
aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaad	2026-01-19 21:18:53.395801+02	2020-12-15	آدم كريم	MALE	INS-FAM-004	800000004	SON	PENDING	88888888-8888-8888-8888-888888888883
f10d0261-091b-4071-a47e-21f5cdf12ab6	2026-01-19 22:00:53.787076+02	1988-08-20	Mary Smith	FEMALE	EMP001.01	FM001001	WIFE	PENDING	551be117-7c0b-4f5a-80f6-d0966b22a087
70582db4-2fc6-40b3-8ea5-47f2492bc3a4	2026-01-19 22:00:53.791745+02	2015-03-10	James Smith	MALE	EMP001.02	FM001002	SON	PENDING	551be117-7c0b-4f5a-80f6-d0966b22a087
8606dc15-51a9-4784-9c6b-719a1718ba15	2026-01-19 22:00:53.795734+02	2018-06-25	Emma Smith	FEMALE	EMP001.03	FM001003	DAUGHTER	PENDING	551be117-7c0b-4f5a-80f6-d0966b22a087
a519a35b-1eb3-4d5a-bb72-8cef628c1f08	2026-01-19 22:00:53.799427+02	1955-12-01	Robert Smith Sr.	MALE	EMP001.04	FM001004	FATHER	PENDING	551be117-7c0b-4f5a-80f6-d0966b22a087
\.


--
-- Data for Name: healthcare_provider_claims; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.healthcare_provider_claims (id, amount, approved_at, client_id, client_name, description, invoice_image_path, rejected_at, rejection_reason, role_specific_data, service_date, status, submitted_at, provider_id, client_pay_amount, coverage_message, coverage_percent_used, diagnosis, doctor_name, emergency, insurance_covered_amount, is_covered, max_coverage_used, medical_reviewed_at, medical_reviewer_id, medical_reviewer_name, original_consultation_fee, treatment_details, policy_id, is_chronic, paid_at, paid_by, is_follow_up) FROM stdin;
74c34e60-c04e-4bf3-a117-2c7a958f7f4c	150	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Pending medical review 1	\N	\N	\N	\N	2026-01-19	PENDING_MEDICAL	2026-01-19 22:42:00.175649+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	\N	\N	\N	General consultation - 1	Dr. Ahmad Nasser	\N	\N	\N	\N	\N	\N	\N	\N	Treatment plan 1	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
d8582356-df6f-4873-bde0-62b03eaad22c	200	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Pending medical review 2	\N	\N	\N	\N	2026-01-18	PENDING_MEDICAL	2026-01-19 22:42:00.180635+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	\N	\N	\N	General consultation - 2	Dr. Ahmad Nasser	\N	\N	\N	\N	\N	\N	\N	\N	Treatment plan 2	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
572030aa-10ad-40df-aa22-2445b4bcaa72	250	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Pending medical review 3	\N	\N	\N	\N	2026-01-17	PENDING_MEDICAL	2026-01-19 22:42:00.180635+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	\N	\N	\N	General consultation - 3	Dr. Ahmad Nasser	\N	\N	\N	\N	\N	\N	\N	\N	Treatment plan 3	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
84995c4a-ce10-4629-bee4-5b1c20e7e0ca	200	2026-01-14 22:42:00.171635+02	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Approved 1	\N	\N	\N	\N	2026-01-09	APPROVED_FINAL	2026-01-19 22:42:00.180635+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	20.00	\N	\N	Approved diagnosis 1	Dr. Ahmad Nasser	\N	180.00	t	\N	\N	\N	\N	\N	Approved treatment 1	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
517e8948-e83c-43d2-bc41-56dbedd4cd98	230	2026-01-13 22:42:00.171635+02	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Approved 2	\N	\N	\N	\N	2026-01-08	APPROVED_FINAL	2026-01-19 22:42:00.180635+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	25.00	\N	\N	Approved diagnosis 2	Dr. Ahmad Nasser	\N	205.00	t	\N	\N	\N	\N	\N	Approved treatment 2	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
27949c98-4594-4610-b0b5-55909ef96a42	350	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Awaiting coordination review	\N	\N	\N	\N	2026-01-14	AWAITING_COORDINATION_REVIEW	2026-01-19 22:42:00.180635+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	50.00	\N	\N	Awaiting coordination - cardiology	Dr. Ahmad Nasser	\N	300.00	t	\N	2026-01-16 22:42:00.171635+02	\N	\N	\N	Cardiology treatment plan	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
76413a50-de79-4c00-ab99-ae8472f6d795	1500	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Rejected (not covered)	\N	2026-01-04 22:42:00.171635+02	Cosmetic procedures are not covered under the policy	\N	2025-12-30	REJECTED_FINAL	2026-01-19 22:42:00.180635+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	\N	\N	\N	Cosmetic procedure	Dr. Ahmad Nasser	\N	\N	f	\N	\N	\N	\N	\N	Cosmetic surgery	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
fb2c9179-f840-4eff-9fcc-f7af229eac6d	85	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Pharmacy claim - Pending 1	\N	\N	\N	{"prescriptionNumber": "RX-2024-1"}	2026-01-19	PENDING_MEDICAL	2026-01-19 22:42:00.181633+02	55555555-5555-5555-5555-555555555555	\N	\N	\N	Prescription fill - 1	Dr. Ahmad	\N	\N	\N	\N	\N	\N	\N	\N	Medication dispensing	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
75d59197-6aa6-41b4-a77f-f4060cd58250	105	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Pharmacy claim - Pending 2	\N	\N	\N	{"prescriptionNumber": "RX-2024-2"}	2026-01-18	PENDING_MEDICAL	2026-01-19 22:42:00.181633+02	55555555-5555-5555-5555-555555555555	\N	\N	\N	Prescription fill - 2	Dr. Ahmad	\N	\N	\N	\N	\N	\N	\N	\N	Medication dispensing	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
bcb430a4-6d41-4dff-a0e1-200a76486e46	120	2026-01-14 22:42:00.171635+02	88888888-8888-8888-8888-888888888881	أحمد محمود	Pharmacy claim - Approved chronic meds	\N	\N	\N	\N	2026-01-12	APPROVED_FINAL	2026-01-19 22:42:00.181633+02	55555555-5555-5555-5555-555555555555	12.00	\N	\N	Chronic medication refill	Dr. Khaled	\N	108.00	t	\N	\N	\N	\N	\N	Monthly chronic medication	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	t	\N	\N	f
ceca629f-69dd-4697-a28b-1143e6a6ac21	450	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Pharmacy claim - Awaiting coordination	\N	\N	\N	\N	2026-01-16	AWAITING_COORDINATION_REVIEW	2026-01-19 22:42:00.181633+02	55555555-5555-5555-5555-555555555555	50.00	\N	\N	Specialty medication	Dr. Sara	\N	400.00	t	\N	2026-01-17 22:42:00.171635+02	\N	\N	\N	High-cost specialty drug	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
3f1d1451-dafe-4645-a235-722497fd023e	110	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Lab claim - Pending 1	\N	\N	\N	{"testType": "Blood Work", "tests": ["CBC", "Lipid Panel"]}	2026-01-19	PENDING_MEDICAL	2026-01-19 22:42:00.182606+02	66666666-6666-6666-6666-666666666666	\N	\N	\N	Blood work - CBC 1	Dr. Khaled	\N	\N	\N	\N	\N	\N	\N	\N	Complete blood count analysis	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
71c36d8b-c4b5-48e1-b59f-72c8d067d25e	140	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Lab claim - Pending 2	\N	\N	\N	{"testType": "Blood Work", "tests": ["CBC", "Lipid Panel"]}	2026-01-18	PENDING_MEDICAL	2026-01-19 22:42:00.182606+02	66666666-6666-6666-6666-666666666666	\N	\N	\N	Blood work - CBC 2	Dr. Khaled	\N	\N	\N	\N	\N	\N	\N	\N	Complete blood count analysis	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
22eaedc0-5ccf-4c84-b4e7-2931e66d1c74	150	2026-01-11 22:42:00.171635+02	88888888-8888-8888-8888-888888888881	أحمد محمود	Lab claim - Approved thyroid test	\N	\N	\N	\N	2026-01-09	APPROVED_FINAL	2026-01-19 22:42:00.182606+02	66666666-6666-6666-6666-666666666666	15.00	\N	\N	Thyroid panel	Dr. Hana	\N	135.00	t	\N	\N	\N	\N	\N	TSH, Free T4 analysis	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
3ecb77b5-a435-4242-a4c6-93c4b99d2288	200	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Lab claim - Awaiting coordination	\N	\N	\N	\N	2026-01-15	AWAITING_COORDINATION_REVIEW	2026-01-19 22:42:00.182606+02	66666666-6666-6666-6666-666666666666	20.00	\N	\N	Comprehensive metabolic panel	Dr. Ahmad	\N	180.00	t	\N	2026-01-17 22:42:00.171635+02	\N	\N	\N	Full metabolic analysis	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
5a39b2d5-3936-46a7-9c01-e593a48962a1	180	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Radiology claim - Pending 1	\N	\N	\N	{"imagingType": "X-Ray", "bodyPart": "Chest"}	2026-01-19	PENDING_MEDICAL	2026-01-19 22:42:00.183721+02	77777777-7777-7777-7777-777777777777	\N	\N	\N	X-Ray - Chest 1	Dr. Sara	\N	\N	\N	\N	\N	\N	\N	\N	Chest radiograph PA and Lateral	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
d3a66560-803e-4249-918d-e6f608f22941	230	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Radiology claim - Pending 2	\N	\N	\N	{"imagingType": "X-Ray", "bodyPart": "Chest"}	2026-01-18	PENDING_MEDICAL	2026-01-19 22:42:00.183721+02	77777777-7777-7777-7777-777777777777	\N	\N	\N	X-Ray - Chest 2	Dr. Sara	\N	\N	\N	\N	\N	\N	\N	\N	Chest radiograph PA and Lateral	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
b7460b84-22fa-406f-98e8-4256a0ae11d4	600	2026-01-07 22:42:00.171635+02	88888888-8888-8888-8888-888888888881	أحمد محمود	Radiology claim - Approved MRI	\N	\N	\N	\N	2026-01-04	APPROVED_FINAL	2026-01-19 22:42:00.183721+02	77777777-7777-7777-7777-777777777777	60.00	\N	\N	MRI - Knee	Dr. Nadia	\N	540.00	t	\N	\N	\N	\N	\N	MRI scan of right knee	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
b1b27af5-b33d-435d-8a05-23281c86b99a	800	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Radiology claim - Awaiting coordination	\N	\N	\N	\N	2026-01-13	AWAITING_COORDINATION_REVIEW	2026-01-19 22:42:00.183721+02	77777777-7777-7777-7777-777777777777	80.00	\N	\N	CT Scan - Abdomen	Dr. Ahmad	\N	720.00	t	\N	2026-01-15 22:42:00.171635+02	\N	\N	\N	Abdominal CT with contrast	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
76b3a48e-b36d-43af-9124-b36ccbfe8338	2500	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Radiology claim - Rejected (preventive not covered)	\N	2025-12-30 22:42:00.171635+02	Preventive full body scans are not covered	\N	2025-12-25	REJECTED_FINAL	2026-01-19 22:42:00.183721+02	77777777-7777-7777-7777-777777777777	\N	\N	\N	Full body MRI - preventive	Dr. Youssef	\N	\N	f	\N	\N	\N	\N	\N	Preventive full body scan	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
611b956c-7997-409b-bf11-caa36634ca6a	150	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Pending medical review 1	\N	\N	\N	\N	2026-01-19	PENDING_MEDICAL	2026-01-19 23:00:50.683924+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	\N	\N	\N	General consultation - 1	Dr. Ahmad Nasser	\N	\N	\N	\N	\N	\N	\N	\N	Treatment plan 1	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
63ef3013-ded1-4463-b28a-787e2951e793	200	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Pending medical review 2	\N	\N	\N	\N	2026-01-18	PENDING_MEDICAL	2026-01-19 23:00:50.693129+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	\N	\N	\N	General consultation - 2	Dr. Ahmad Nasser	\N	\N	\N	\N	\N	\N	\N	\N	Treatment plan 2	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
8c986bf4-9d35-433e-af83-b7fa04f844ad	250	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Pending medical review 3	\N	\N	\N	\N	2026-01-17	PENDING_MEDICAL	2026-01-19 23:00:50.694693+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	\N	\N	\N	General consultation - 3	Dr. Ahmad Nasser	\N	\N	\N	\N	\N	\N	\N	\N	Treatment plan 3	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
740ed84a-f0a6-43ac-b208-338bdc4278d9	200	2026-01-14 23:00:50.683924+02	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Approved 1	\N	\N	\N	\N	2026-01-09	APPROVED_FINAL	2026-01-19 23:00:50.694693+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	20.00	\N	\N	Approved diagnosis 1	Dr. Ahmad Nasser	\N	180.00	t	\N	\N	\N	\N	\N	Approved treatment 1	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
e338482d-56ad-4eb7-97c7-81769111373f	230	2026-01-13 23:00:50.683924+02	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Approved 2	\N	\N	\N	\N	2026-01-08	APPROVED_FINAL	2026-01-19 23:00:50.694693+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	25.00	\N	\N	Approved diagnosis 2	Dr. Ahmad Nasser	\N	205.00	t	\N	\N	\N	\N	\N	Approved treatment 2	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
5ac28ff0-74ab-420d-9168-8c0e816be659	350	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Awaiting coordination review	\N	\N	\N	\N	2026-01-14	AWAITING_COORDINATION_REVIEW	2026-01-19 23:00:50.694693+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	50.00	\N	\N	Awaiting coordination - cardiology	Dr. Ahmad Nasser	\N	300.00	t	\N	2026-01-16 23:00:50.683924+02	\N	\N	\N	Cardiology treatment plan	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
575ae124-5b5f-42ea-9ddb-e7da86223bc0	1500	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Rejected (not covered)	\N	2026-01-04 23:00:50.683924+02	Cosmetic procedures are not covered under the policy	\N	2025-12-30	REJECTED_FINAL	2026-01-19 23:00:50.694693+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	\N	\N	\N	Cosmetic procedure	Dr. Ahmad Nasser	\N	\N	f	\N	\N	\N	\N	\N	Cosmetic surgery	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
2a54712a-14b6-475c-a5d0-7ec0681877f9	85	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Pharmacy claim - Pending 1	\N	\N	\N	{"prescriptionNumber": "RX-2024-1"}	2026-01-19	PENDING_MEDICAL	2026-01-19 23:00:50.694693+02	55555555-5555-5555-5555-555555555555	\N	\N	\N	Prescription fill - 1	Dr. Ahmad	\N	\N	\N	\N	\N	\N	\N	\N	Medication dispensing	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
6b5046e5-e1fd-4b81-8927-4befce7e0c82	105	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Pharmacy claim - Pending 2	\N	\N	\N	{"prescriptionNumber": "RX-2024-2"}	2026-01-18	PENDING_MEDICAL	2026-01-19 23:00:50.694693+02	55555555-5555-5555-5555-555555555555	\N	\N	\N	Prescription fill - 2	Dr. Ahmad	\N	\N	\N	\N	\N	\N	\N	\N	Medication dispensing	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
5e94e177-9690-4c17-8439-f0e4e67c2a15	120	2026-01-14 23:00:50.683924+02	88888888-8888-8888-8888-888888888881	أحمد محمود	Pharmacy claim - Approved chronic meds	\N	\N	\N	\N	2026-01-12	APPROVED_FINAL	2026-01-19 23:00:50.694693+02	55555555-5555-5555-5555-555555555555	12.00	\N	\N	Chronic medication refill	Dr. Khaled	\N	108.00	t	\N	\N	\N	\N	\N	Monthly chronic medication	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	t	\N	\N	f
6b639fb1-12c5-46cc-bd8d-89bac409b881	450	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Pharmacy claim - Awaiting coordination	\N	\N	\N	\N	2026-01-16	AWAITING_COORDINATION_REVIEW	2026-01-19 23:00:50.694693+02	55555555-5555-5555-5555-555555555555	50.00	\N	\N	Specialty medication	Dr. Sara	\N	400.00	t	\N	2026-01-17 23:00:50.683924+02	\N	\N	\N	High-cost specialty drug	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
1085051c-5899-4920-8ddb-f63def433ab6	110	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Lab claim - Pending 1	\N	\N	\N	{"testType": "Blood Work", "tests": ["CBC", "Lipid Panel"]}	2026-01-19	PENDING_MEDICAL	2026-01-19 23:00:50.694693+02	66666666-6666-6666-6666-666666666666	\N	\N	\N	Blood work - CBC 1	Dr. Khaled	\N	\N	\N	\N	\N	\N	\N	\N	Complete blood count analysis	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
a594574e-f998-44da-8c88-8c3307a90755	140	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Lab claim - Pending 2	\N	\N	\N	{"testType": "Blood Work", "tests": ["CBC", "Lipid Panel"]}	2026-01-18	PENDING_MEDICAL	2026-01-19 23:00:50.694693+02	66666666-6666-6666-6666-666666666666	\N	\N	\N	Blood work - CBC 2	Dr. Khaled	\N	\N	\N	\N	\N	\N	\N	\N	Complete blood count analysis	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
804e8e17-bcb8-42de-b1f4-6139d1408be4	150	2026-01-11 23:00:50.683924+02	88888888-8888-8888-8888-888888888881	أحمد محمود	Lab claim - Approved thyroid test	\N	\N	\N	\N	2026-01-09	APPROVED_FINAL	2026-01-19 23:00:50.694693+02	66666666-6666-6666-6666-666666666666	15.00	\N	\N	Thyroid panel	Dr. Hana	\N	135.00	t	\N	\N	\N	\N	\N	TSH, Free T4 analysis	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
09aa3bda-174d-4055-a98f-b70e46f9ae63	200	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Lab claim - Awaiting coordination	\N	\N	\N	\N	2026-01-15	AWAITING_COORDINATION_REVIEW	2026-01-19 23:00:50.694693+02	66666666-6666-6666-6666-666666666666	20.00	\N	\N	Comprehensive metabolic panel	Dr. Ahmad	\N	180.00	t	\N	2026-01-17 23:00:50.683924+02	\N	\N	\N	Full metabolic analysis	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
a03b7d76-25d5-4886-b367-24b80783c6a9	180	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Radiology claim - Pending 1	\N	\N	\N	{"imagingType": "X-Ray", "bodyPart": "Chest"}	2026-01-19	PENDING_MEDICAL	2026-01-19 23:00:50.694693+02	77777777-7777-7777-7777-777777777777	\N	\N	\N	X-Ray - Chest 1	Dr. Sara	\N	\N	\N	\N	\N	\N	\N	\N	Chest radiograph PA and Lateral	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
25632cce-2c76-4b8e-950a-19ceee351971	230	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Radiology claim - Pending 2	\N	\N	\N	{"imagingType": "X-Ray", "bodyPart": "Chest"}	2026-01-18	PENDING_MEDICAL	2026-01-19 23:00:50.694693+02	77777777-7777-7777-7777-777777777777	\N	\N	\N	X-Ray - Chest 2	Dr. Sara	\N	\N	\N	\N	\N	\N	\N	\N	Chest radiograph PA and Lateral	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
ba90adff-8a73-4f41-89f0-524440732434	600	2026-01-07 23:00:50.683924+02	88888888-8888-8888-8888-888888888881	أحمد محمود	Radiology claim - Approved MRI	\N	\N	\N	\N	2026-01-04	APPROVED_FINAL	2026-01-19 23:00:50.694693+02	77777777-7777-7777-7777-777777777777	60.00	\N	\N	MRI - Knee	Dr. Nadia	\N	540.00	t	\N	\N	\N	\N	\N	MRI scan of right knee	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
1ea5244c-912b-43dd-9fd9-c256e6428b5a	800	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Radiology claim - Awaiting coordination	\N	\N	\N	\N	2026-01-13	AWAITING_COORDINATION_REVIEW	2026-01-19 23:00:50.694693+02	77777777-7777-7777-7777-777777777777	80.00	\N	\N	CT Scan - Abdomen	Dr. Ahmad	\N	720.00	t	\N	2026-01-15 23:00:50.683924+02	\N	\N	\N	Abdominal CT with contrast	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
b4a9ac48-c72e-4d8b-974e-2b955ebaaa98	2500	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Radiology claim - Rejected (preventive not covered)	\N	2025-12-30 23:00:50.683924+02	Preventive full body scans are not covered	\N	2025-12-25	REJECTED_FINAL	2026-01-19 23:00:50.694693+02	77777777-7777-7777-7777-777777777777	\N	\N	\N	Full body MRI - preventive	Dr. Youssef	\N	\N	f	\N	\N	\N	\N	\N	Preventive full body scan	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
55555555-aaaa-aaaa-aaaa-555555555551	100	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	استشارة عامة - تحقيق في سبب الإرهاق	\N	\N	\N	{"specialization": "طب عام", "visitType": "NORMAL"}	2026-01-09	PENDING	2026-01-09 21:18:53.395801+02	44444444-4444-4444-4444-444444444444	20.00	رسوم الاستشارة القياسية	80.00	إرهاق	د. سارة عبدالله	f	80.00	t	2000.00	\N	\N	\N	100.00	تقييم أولي	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
55555555-aaaa-aaaa-aaaa-555555555552	105	2026-01-16 21:18:53.395801+02	88888888-8888-8888-8888-888888888883	يزن كريم	صرف وصفة - أموكسيسيلين	\N	\N	\N	{"prescriptionId": "ffffffff-ffff-ffff-ffff-fffffffffff1"}	2026-01-14	APPROVED	2026-01-14 21:18:53.395801+02	55555555-5555-5555-5555-555555555555	21.00	الأدوية مغطاة بنسبة 80%	80.00	التهاب الجهاز التنفسي العلوي	د. فاطمة حسن	f	84.00	t	5000.00	2026-01-15 21:18:53.395801+02	22222222-2222-2222-2222-222222222222	د. رامي يوسف	\N	دورة مضاد حيوي	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
55555555-aaaa-aaaa-aaaa-555555555553	70	2026-01-17 21:18:53.395801+02	88888888-8888-8888-8888-888888888884	سمير علي	فحص الهيموغلوبين السكري لمراقبة السكري	\N	\N	\N	{"testId": "59a26c52-a8d8-4893-a2ee-add683744e76"}	2026-01-14	PAID	2026-01-14 21:18:53.395801+02	66666666-6666-6666-6666-666666666666	14.00	فحص المختبر مغطى بنسبة 80%	80.00	داء السكري من النوع 2	د. طارق الخطيب	f	56.00	t	5000.00	2026-01-16 21:18:53.395801+02	22222222-2222-2222-2222-222222222222	د. رامي يوسف	\N	مراقبة ربع سنوية	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	t	2026-01-18 21:18:53.395801	11111111-1111-1111-1111-111111111111	f
55555555-aaaa-aaaa-aaaa-555555555554	50	2026-01-13 21:18:53.395801+02	88888888-8888-8888-8888-888888888883	كريم نصار	تخطيط قلب للخفقان	\N	\N	\N	{"testId": "efecd794-321b-4751-b2c1-5dceeec41af9"}	2026-01-09	PAID	2026-01-09 21:18:53.395801+02	77777777-7777-7777-7777-777777777777	10.00	الأشعة مغطاة بنسبة 80%	80.00	خفقان القلب	د. محمد ناصر	f	40.00	t	8000.00	2026-01-11 21:18:53.395801+02	22222222-2222-2222-2222-222222222222	د. رامي يوسف	\N	تخطيط قلب تشخيصي	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	2026-01-14 21:18:53.395801	11111111-1111-1111-1111-111111111111	f
be5f5167-9930-47ce-8e30-47212bf4f400	150	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Pending medical review 1	\N	\N	\N	\N	2026-01-19	PENDING_MEDICAL	2026-01-19 23:18:32.289598+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	\N	\N	\N	General consultation - 1	Dr. Ahmad Nasser	\N	\N	\N	\N	\N	\N	\N	\N	Treatment plan 1	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
a820e3a5-22e2-4d8a-80ae-6c876cf1d4fe	200	\N	88888888-8888-8888-8888-888888888883	كريم نصار	Doctor consultation - Pending medical review 2	\N	\N	\N	\N	2026-01-18	PENDING_MEDICAL	2026-01-19 23:18:32.293425+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	\N	\N	\N	General consultation - 2	Dr. Ahmad Nasser	\N	\N	\N	\N	\N	\N	\N	\N	Treatment plan 2	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
a6228ae9-37ed-487e-9c95-a7feac0c6836	250	\N	88888888-8888-8888-8888-888888888884	سمير علي	Doctor consultation - Pending medical review 3	\N	\N	\N	\N	2026-01-17	PENDING_MEDICAL	2026-01-19 23:18:32.294065+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	\N	\N	\N	General consultation - 3	Dr. Ahmad Nasser	\N	\N	\N	\N	\N	\N	\N	\N	Treatment plan 3	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
20c3e066-c3bd-45ba-b198-a883fda49694	200	2026-01-14 23:18:32.283927+02	551be117-7c0b-4f5a-80f6-d0966b22a087	John Smith	Doctor consultation - Approved 1	\N	\N	\N	\N	2026-01-09	APPROVED_FINAL	2026-01-19 23:18:32.294065+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	20.00	\N	\N	Approved diagnosis 1	Dr. Ahmad Nasser	\N	180.00	t	\N	\N	\N	\N	\N	Approved treatment 1	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
8b4148bb-ce59-406c-a538-a3cc60516d90	230	2026-01-13 23:18:32.283927+02	88888888-8888-8888-8888-888888888882	لينا حسن	Doctor consultation - Approved 2	\N	\N	\N	\N	2026-01-08	APPROVED_FINAL	2026-01-19 23:18:32.294587+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	25.00	\N	\N	Approved diagnosis 2	Dr. Ahmad Nasser	\N	205.00	t	\N	\N	\N	\N	\N	Approved treatment 2	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
45aac0b6-2056-4f6c-843e-abffafa53aeb	350	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Doctor consultation - Awaiting coordination review	\N	\N	\N	\N	2026-01-14	AWAITING_COORDINATION_REVIEW	2026-01-19 23:18:32.294587+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	50.00	\N	\N	Awaiting coordination - cardiology	Dr. Ahmad Nasser	\N	300.00	t	\N	2026-01-16 23:18:32.283927+02	\N	\N	\N	Cardiology treatment plan	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
5c883431-fba1-4bde-a087-46db6d95b03e	1500	\N	88888888-8888-8888-8888-888888888883	كريم نصار	Doctor consultation - Rejected (not covered)	\N	2026-01-04 23:18:32.283927+02	Cosmetic procedures are not covered under the policy	\N	2025-12-30	REJECTED_FINAL	2026-01-19 23:18:32.294587+02	b8d24b9e-da85-4a53-aea5-b65abf94daf2	\N	\N	\N	Cosmetic procedure	Dr. Ahmad Nasser	\N	\N	f	\N	\N	\N	\N	\N	Cosmetic surgery	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
ee20150d-37d5-4ada-8988-aa40d06c8e22	85	\N	88888888-8888-8888-8888-888888888884	سمير علي	Pharmacy claim - Pending 1	\N	\N	\N	{"prescriptionNumber": "RX-2024-1"}	2026-01-19	PENDING_MEDICAL	2026-01-19 23:18:32.295109+02	55555555-5555-5555-5555-555555555555	\N	\N	\N	Prescription fill - 1	Dr. Ahmad	\N	\N	\N	\N	\N	\N	\N	\N	Medication dispensing	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
7025e35f-75aa-45b9-bfb5-349818415c31	105	\N	551be117-7c0b-4f5a-80f6-d0966b22a087	John Smith	Pharmacy claim - Pending 2	\N	\N	\N	{"prescriptionNumber": "RX-2024-2"}	2026-01-18	PENDING_MEDICAL	2026-01-19 23:18:32.295638+02	55555555-5555-5555-5555-555555555555	\N	\N	\N	Prescription fill - 2	Dr. Ahmad	\N	\N	\N	\N	\N	\N	\N	\N	Medication dispensing	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
5ee48835-b271-4cb2-9ce3-4a5c09258509	120	2026-01-14 23:18:32.284925+02	88888888-8888-8888-8888-888888888882	لينا حسن	Pharmacy claim - Approved chronic meds	\N	\N	\N	\N	2026-01-12	APPROVED_FINAL	2026-01-19 23:18:32.295638+02	55555555-5555-5555-5555-555555555555	12.00	\N	\N	Chronic medication refill	Dr. Khaled	\N	108.00	t	\N	\N	\N	\N	\N	Monthly chronic medication	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	t	\N	\N	f
f5f1574e-a7f4-4553-bb88-cd7569b3468f	450	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Pharmacy claim - Awaiting coordination	\N	\N	\N	\N	2026-01-16	AWAITING_COORDINATION_REVIEW	2026-01-19 23:18:32.295638+02	55555555-5555-5555-5555-555555555555	50.00	\N	\N	Specialty medication	Dr. Sara	\N	400.00	t	\N	2026-01-17 23:18:32.284925+02	\N	\N	\N	High-cost specialty drug	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
ee506529-4bb3-4c85-9109-ef7928e42d0c	110	\N	88888888-8888-8888-8888-888888888883	كريم نصار	Lab claim - Pending 1	\N	\N	\N	{"testType": "Blood Work", "tests": ["CBC", "Lipid Panel"]}	2026-01-19	PENDING_MEDICAL	2026-01-19 23:18:32.296157+02	66666666-6666-6666-6666-666666666666	\N	\N	\N	Blood work - CBC 1	Dr. Khaled	\N	\N	\N	\N	\N	\N	\N	\N	Complete blood count analysis	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
9f5c6fa6-494d-4416-b272-c1811df047be	140	\N	88888888-8888-8888-8888-888888888884	سمير علي	Lab claim - Pending 2	\N	\N	\N	{"testType": "Blood Work", "tests": ["CBC", "Lipid Panel"]}	2026-01-18	PENDING_MEDICAL	2026-01-19 23:18:32.296157+02	66666666-6666-6666-6666-666666666666	\N	\N	\N	Blood work - CBC 2	Dr. Khaled	\N	\N	\N	\N	\N	\N	\N	\N	Complete blood count analysis	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
af7c90af-466b-4627-bfe4-0563678e6657	150	2026-01-11 23:18:32.284925+02	551be117-7c0b-4f5a-80f6-d0966b22a087	John Smith	Lab claim - Approved thyroid test	\N	\N	\N	\N	2026-01-09	APPROVED_FINAL	2026-01-19 23:18:32.296157+02	66666666-6666-6666-6666-666666666666	15.00	\N	\N	Thyroid panel	Dr. Hana	\N	135.00	t	\N	\N	\N	\N	\N	TSH, Free T4 analysis	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
7d940935-18e2-40aa-bb01-f94cfa0aed8e	200	\N	88888888-8888-8888-8888-888888888882	لينا حسن	Lab claim - Awaiting coordination	\N	\N	\N	\N	2026-01-15	AWAITING_COORDINATION_REVIEW	2026-01-19 23:18:32.296157+02	66666666-6666-6666-6666-666666666666	20.00	\N	\N	Comprehensive metabolic panel	Dr. Ahmad	\N	180.00	t	\N	2026-01-17 23:18:32.284925+02	\N	\N	\N	Full metabolic analysis	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
b567593c-ab3c-4b0f-97c5-045a78983dfd	180	\N	88888888-8888-8888-8888-888888888881	أحمد محمود	Radiology claim - Pending 1	\N	\N	\N	{"imagingType": "X-Ray", "bodyPart": "Chest"}	2026-01-19	PENDING_MEDICAL	2026-01-19 23:18:32.297301+02	77777777-7777-7777-7777-777777777777	\N	\N	\N	X-Ray - Chest 1	Dr. Sara	\N	\N	\N	\N	\N	\N	\N	\N	Chest radiograph PA and Lateral	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
daccf21d-b1ac-4e20-8a63-e5fc5a9232a3	230	\N	88888888-8888-8888-8888-888888888883	كريم نصار	Radiology claim - Pending 2	\N	\N	\N	{"imagingType": "X-Ray", "bodyPart": "Chest"}	2026-01-18	PENDING_MEDICAL	2026-01-19 23:18:32.297807+02	77777777-7777-7777-7777-777777777777	\N	\N	\N	X-Ray - Chest 2	Dr. Sara	\N	\N	\N	\N	\N	\N	\N	\N	Chest radiograph PA and Lateral	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
8bfbf984-e605-4f53-a7ec-be4062f66476	600	2026-01-07 23:18:32.284925+02	88888888-8888-8888-8888-888888888884	سمير علي	Radiology claim - Approved MRI	\N	\N	\N	\N	2026-01-04	APPROVED_FINAL	2026-01-19 23:18:32.297807+02	77777777-7777-7777-7777-777777777777	60.00	\N	\N	MRI - Knee	Dr. Nadia	\N	540.00	t	\N	\N	\N	\N	\N	MRI scan of right knee	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
e2d2a022-f766-488a-9ffd-7a5948d4efa7	800	\N	551be117-7c0b-4f5a-80f6-d0966b22a087	John Smith	Radiology claim - Awaiting coordination	\N	\N	\N	\N	2026-01-13	AWAITING_COORDINATION_REVIEW	2026-01-19 23:18:32.297807+02	77777777-7777-7777-7777-777777777777	80.00	\N	\N	CT Scan - Abdomen	Dr. Ahmad	\N	720.00	t	\N	2026-01-15 23:18:32.284925+02	\N	\N	\N	Abdominal CT with contrast	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
b374a398-88e9-40af-957f-18508877879e	2500	\N	88888888-8888-8888-8888-888888888882	لينا حسن	Radiology claim - Rejected (preventive not covered)	\N	2025-12-30 23:18:32.284925+02	Preventive full body scans are not covered	\N	2025-12-25	REJECTED_FINAL	2026-01-19 23:18:32.298338+02	77777777-7777-7777-7777-777777777777	\N	\N	\N	Full body MRI - preventive	Dr. Youssef	\N	\N	f	\N	\N	\N	\N	\N	Preventive full body scan	23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	f	\N	\N	f
\.


--
-- Data for Name: lab_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.lab_requests (id, created_at, notes, result_url, status, test_name, updated_at, doctor_id, member_id, lab_tech_id, approved_price, entered_price, test_id, price_id, diagnosis, treatment) FROM stdin;
11111111-aaaa-aaaa-aaaa-111111111111	2026-01-17 21:18:53.395801+02	فحص مستويات فيتامين ب12 والحديد	\N	PENDING	تعداد الدم الكامل (CBC)	2026-01-17 21:18:53.395801+02	44444444-4444-4444-4444-444444444444	88888888-8888-8888-8888-888888888881	\N	50	50	\N	11111111-bbbb-bbbb-bbbb-111111111111	تحقيق في سبب الإرهاق	تشخيصي
11111111-aaaa-aaaa-aaaa-111111111112	2026-01-14 21:18:53.395801+02	مراقبة روتينية للسكري	/results/lab/hba1c_result.pdf	COMPLETED	الهيموغلوبين السكري (HbA1c)	2026-01-16 21:18:53.395801+02	44444444-4444-4444-4444-44444444444d	88888888-8888-8888-8888-888888888884	66666666-6666-6666-6666-666666666666	70	70	\N	22222222-bbbb-bbbb-bbbb-222222222222	مراقبة السكري	فحص ربع سنوي
11111111-aaaa-aaaa-aaaa-111111111113	2026-01-11 21:18:53.395801+02	تحليل دهون لمريض القلب	/results/lab/lipid_result.pdf	COMPLETED	تحليل الدهون	2026-01-13 21:18:53.395801+02	44444444-4444-4444-4444-444444444445	88888888-8888-8888-8888-888888888883	66666666-6666-6666-6666-666666666666	80	80	\N	33333333-bbbb-bbbb-bbbb-333333333333	تقييم مخاطر أمراض القلب	وقائي
\.


--
-- Data for Name: medical_diagnoses; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.medical_diagnoses (id, active, arabic_name, created_at, description, english_name, updated_at) FROM stdin;
f2528667-3a55-41d7-bfdc-0c9b28d4f318	t	ارتفاع ضغط الدم	2026-01-19 22:09:58.885823+02	\N	Hypertension	2026-01-19 22:09:58.885823+02
d5859b4a-3600-45d3-b444-20fa34b0d6b2	t	السكري النوع الثاني	2026-01-19 22:09:58.886847+02	\N	Diabetes Mellitus Type 2	2026-01-19 22:09:58.886847+02
da7beda2-4d1f-4abc-929a-71bea1b549e4	t	السكري النوع الأول	2026-01-19 22:09:58.886847+02	\N	Diabetes Mellitus Type 1	2026-01-19 22:09:58.886847+02
29c5e1bc-db8a-44c2-9936-106756fd2ed8	t	الربو	2026-01-19 22:09:58.886847+02	\N	Asthma	2026-01-19 22:09:58.886847+02
6bc869d6-c0d1-4d83-8d32-c6514b1de2f4	t	مرض الانسداد الرئوي المزمن	2026-01-19 22:09:58.886847+02	\N	Chronic Obstructive Pulmonary Disease	2026-01-19 22:09:58.886847+02
52eb9b01-4937-41b4-b2b8-15116924f19c	t	مرض الشريان التاجي	2026-01-19 22:09:58.886847+02	\N	Coronary Artery Disease	2026-01-19 22:09:58.886847+02
ca6afc34-5a00-403c-9ae2-96f8dc46d700	t	فشل القلب	2026-01-19 22:09:58.886847+02	\N	Heart Failure	2026-01-19 22:09:58.886847+02
f7aa3544-4c51-4128-bdf8-4979340ed901	t	الرجفان الأذيني	2026-01-19 22:09:58.886847+02	\N	Atrial Fibrillation	2026-01-19 22:09:58.886847+02
149eb532-6269-497c-805b-df360e39a5c3	t	مرض الكلى المزمن	2026-01-19 22:09:58.886847+02	\N	Chronic Kidney Disease	2026-01-19 22:09:58.886847+02
633c8bbf-25cb-4f30-be18-9a26582aee5f	t	التهاب المعدة	2026-01-19 22:09:58.886847+02	\N	Gastritis	2026-01-19 22:09:58.886847+02
236dfc17-1eb1-4c44-8a32-da3feb52ea3e	t	مرض الارتجاع المعدي المريئي	2026-01-19 22:09:58.886847+02	\N	Gastroesophageal Reflux Disease	2026-01-19 22:09:58.886847+02
854f5fd5-8294-46e1-8011-924b77d1e0e8	t	مرض القرحة الهضمية	2026-01-19 22:09:58.886847+02	\N	Peptic Ulcer Disease	2026-01-19 22:09:58.886847+02
8792ef93-84b8-4f95-8fa3-44eba14365ce	t	الصداع النصفي	2026-01-19 22:09:58.886847+02	\N	Migraine	2026-01-19 22:09:58.886847+02
017df202-0129-4ce7-86f5-62f838380931	t	صداع التوتر	2026-01-19 22:09:58.886847+02	\N	Tension Headache	2026-01-19 22:09:58.886847+02
69817ced-eb10-4cab-b484-692548931b75	t	الاكتئاب	2026-01-19 22:09:58.887828+02	\N	Depression	2026-01-19 22:09:58.887828+02
4165893c-99a5-47e2-994e-bbdd6148809d	t	اضطراب القلق	2026-01-19 22:09:58.887828+02	\N	Anxiety Disorder	2026-01-19 22:09:58.887828+02
c260d936-357d-45d6-9eec-d8114cf78095	t	قصور الغدة الدرقية	2026-01-19 22:09:58.887828+02	\N	Hypothyroidism	2026-01-19 22:09:58.887828+02
bc68d664-8f05-4cc6-bf85-7cc777d4de39	t	فرط نشاط الغدة الدرقية	2026-01-19 22:09:58.887828+02	\N	Hyperthyroidism	2026-01-19 22:09:58.887828+02
9f46701a-f8ba-4560-b237-bd07b97f8d6e	t	هشاشة العظام	2026-01-19 22:09:58.887828+02	\N	Osteoporosis	2026-01-19 22:09:58.887828+02
d0a1cecf-d213-4edf-a994-00c2bc36b8cb	t	التهاب المفاصل التنكسي	2026-01-19 22:09:58.887828+02	\N	Osteoarthritis	2026-01-19 22:09:58.887828+02
56ee423e-15b0-45ce-9802-e574f8021b83	t	التهاب المفاصل الروماتويدي	2026-01-19 22:09:58.887828+02	\N	Rheumatoid Arthritis	2026-01-19 22:09:58.887828+02
4d724bdd-de6e-4d64-b89a-8fd681fbf767	t	فقر الدم	2026-01-19 22:09:58.887828+02	\N	Anemia	2026-01-19 22:09:58.887828+02
1b8c2ea9-8063-405c-9bf6-efc8a59deb98	t	فقر الدم بعوز الحديد	2026-01-19 22:09:58.888817+02	\N	Iron Deficiency Anemia	2026-01-19 22:09:58.888817+02
17680b47-1fe6-4feb-b680-010085afcf94	t	نقص فيتامين د	2026-01-19 22:09:58.888817+02	\N	Vitamin D Deficiency	2026-01-19 22:09:58.888817+02
cf5da331-8073-430a-9cb0-1e1c8ab9ff76	t	عدوى الجهاز التنفسي العلوي	2026-01-19 22:09:58.888817+02	\N	Upper Respiratory Tract Infection	2026-01-19 22:09:58.888817+02
3085a31a-3ec7-4569-bfd5-6a5267eef8b1	t	عدوى المسالك البولية	2026-01-19 22:09:58.888817+02	\N	Urinary Tract Infection	2026-01-19 22:09:58.888817+02
9e4de7f3-6ef4-44a6-a37e-472be4572ad7	t	الالتهاب الرئوي	2026-01-19 22:09:58.888817+02	\N	Pneumonia	2026-01-19 22:09:58.888817+02
2e1ca09f-b802-4933-bca1-f083fb4497a1	t	التهاب الشعب الهوائية	2026-01-19 22:09:58.888817+02	\N	Bronchitis	2026-01-19 22:09:58.888817+02
211c5e49-cfd9-4863-8164-12d94460637e	t	التهاب الجيوب الأنفية	2026-01-19 22:09:58.888817+02	\N	Sinusitis	2026-01-19 22:09:58.888817+02
001d3d93-8b7a-4b90-a599-d71e299e5d58	t	التهاب الأنف التحسسي	2026-01-19 22:09:58.888817+02	\N	Allergic Rhinitis	2026-01-19 22:09:58.888817+02
7f7dcd10-f5ab-4fd7-9dad-8b2164681145	t	الإكزيما	2026-01-19 22:09:58.888817+02	\N	Eczema	2026-01-19 22:09:58.888817+02
a8504870-8ec3-4e81-8b48-57dc071d317d	t	الصدفية	2026-01-19 22:09:58.888817+02	\N	Psoriasis	2026-01-19 22:09:58.888817+02
cb63bc1e-e29c-4f00-a8eb-a1d1d40859b0	t	حب الشباب	2026-01-19 22:09:58.888817+02	\N	Acne Vulgaris	2026-01-19 22:09:58.888817+02
50406d54-c01f-440d-8cdb-682eee18663c	t	آلام أسفل الظهر	2026-01-19 22:09:58.889814+02	\N	Lower Back Pain	2026-01-19 22:09:58.889814+02
77de615c-b07c-4b09-9dfa-ed0dc6a0cfd3	t	داء الفقار الرقبي	2026-01-19 22:09:58.889814+02	\N	Cervical Spondylosis	2026-01-19 22:09:58.889814+02
6f5e9588-2e14-4724-9af5-8037e8b39d37	t	عرق النسا	2026-01-19 22:09:58.889814+02	\N	Sciatica	2026-01-19 22:09:58.889814+02
\.


--
-- Data for Name: medical_records; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.medical_records (id, created_at, diagnosis, notes, treatment, updated_at, doctor_id, member_id) FROM stdin;
eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01	2026-01-09 21:18:53.395801+02	إرهاق خفيف بسبب نقص الفيتامينات	نصح المريض بتحسين النظام الغذائي وجدول النوم	مكملات فيتامين ب12، متابعة بعد أسبوعين	2026-01-09 21:18:53.395801+02	44444444-4444-4444-4444-444444444444	88888888-8888-8888-8888-888888888881
eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02	2026-01-11 21:18:53.395801+02	عدم انتظام ضربات القلب - حميد	تخطيط القلب يظهر PVCs عرضية، لا يوجد قلق فوري	تقليل الكافيين، إدارة التوتر	2026-01-11 21:18:53.395801+02	44444444-4444-4444-4444-444444444445	88888888-8888-8888-8888-888888888883
eeeeeeee-eeee-eeee-eeee-eeeeeeeeee03	2026-01-14 21:18:53.395801+02	التهاب الجهاز التنفسي العلوي	الطفل يعاني من حرارة 38.5 درجة وسعال منتج	أموكسيسيلين 250 ملغ لمدة 7 أيام، خافضات حرارة عند الحاجة	2026-01-14 21:18:53.395801+02	44444444-4444-4444-4444-444444444446	88888888-8888-8888-8888-888888888883
eeeeeeee-eeee-eeee-eeee-eeeeeeeeee04	2026-01-16 21:18:53.395801+02	داء السكري من النوع 2 - مسيطر عليه	الهيموغلوبين السكري 7.2%، تحسن طفيف عن الزيارة السابقة	استمرار ميتفورمين 500 ملغ مرتين يومياً، تعديل النظام الغذائي	2026-01-16 21:18:53.395801+02	44444444-4444-4444-4444-44444444444d	88888888-8888-8888-8888-888888888884
eeeeeeee-eeee-eeee-eeee-eeeeeeeeee05	2026-01-18 21:18:53.395801+02	تعافي من الإرهاق	تحسنت مستويات الفيتامينات، المريض يشعر بتحسن	استمرار المكملات لمدة شهر إضافي	2026-01-18 21:18:53.395801+02	44444444-4444-4444-4444-444444444444	88888888-8888-8888-8888-888888888881
eeeeeeee-eeee-eeee-eeee-eeeeeeeeee06	2026-01-19 21:18:53.395801+02	التهاب الجلد التماسي	طفح جلدي موضعي على الساعدين، ربما رد فعل تحسسي	كريم كورتيكوستيرويد موضعي، تجنب المهيج	2026-01-19 21:18:53.395801+02	44444444-4444-4444-4444-444444444447	88888888-8888-8888-8888-888888888882
\.


--
-- Data for Name: medical_tests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.medical_tests (id, active, category, coverage_status, created_at, price, test_name, updated_at, coverage_percentage) FROM stdin;
0a950556-06e7-4993-a73f-8277964a4435	t	LAB	COVERED	2026-01-19 21:18:53.395801+02	50.00	تعداد الدم الكامل (CBC)	2026-01-19 21:18:53.395801+02	100
9e88481c-fe76-481f-839f-9ff21454a793	t	LAB	COVERED	2026-01-19 21:18:53.395801+02	30.00	سكر الدم الصائم	2026-01-19 21:18:53.395801+02	100
2ff86611-d609-477b-a9b0-8f620ca589ae	t	LAB	COVERED	2026-01-19 21:18:53.395801+02	80.00	تحليل الدهون	2026-01-19 21:18:53.395801+02	100
77c5a3ed-5c0e-483d-820e-d5b661c09e34	t	LAB	COVERED	2026-01-19 21:18:53.395801+02	90.00	فحص وظائف الكبد (LFT)	2026-01-19 21:18:53.395801+02	100
453270a2-f9d4-48fd-9342-598ead50c744	t	LAB	COVERED	2026-01-19 21:18:53.395801+02	85.00	فحص وظائف الكلى (KFT)	2026-01-19 21:18:53.395801+02	100
18cb5701-5b6e-4e57-9cec-6c33abd98168	t	LAB	COVERED	2026-01-19 21:18:53.395801+02	120.00	تحليل الغدة الدرقية (TSH/T3/T4)	2026-01-19 21:18:53.395801+02	100
f90dea40-2ca1-4b60-ba5b-3396883c1299	t	LAB	COVERED	2026-01-19 21:18:53.395801+02	25.00	تحليل البول	2026-01-19 21:18:53.395801+02	100
59a26c52-a8d8-4893-a2ee-add683744e76	t	LAB	COVERED	2026-01-19 21:18:53.395801+02	70.00	الهيموغلوبين السكري (HbA1c)	2026-01-19 21:18:53.395801+02	100
5ff2be41-b675-4416-9b35-5d69b207ef5b	t	LAB	COVERED	2026-01-19 21:18:53.395801+02	60.00	تحليل الأملاح والمعادن	2026-01-19 21:18:53.395801+02	100
1b1e36c3-0f87-47d0-a34f-b787bf522a97	t	LAB	COVERED	2026-01-19 21:18:53.395801+02	20.00	سرعة ترسب الدم (ESR)	2026-01-19 21:18:53.395801+02	100
f202f72e-b459-453b-b746-aceabb29bbdc	t	LAB	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	120.00	فيتامين د	2026-01-19 21:18:53.395801+02	80
a54b05a8-66b9-44cc-bae5-bee71ddf86c5	t	LAB	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	100.00	فيتامين ب12	2026-01-19 21:18:53.395801+02	80
4f86330f-ba77-4891-95ef-d459d7929471	t	LAB	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	110.00	دراسة الحديد	2026-01-19 21:18:53.395801+02	80
a8b9b4d4-4ef9-42d6-96c8-2661eb74046d	t	LAB	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	200.00	تحليل الهرمونات	2026-01-19 21:18:53.395801+02	70
231055ed-f84e-467c-beab-f58128477b3d	t	LAB	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	300.00	دلالات الأورام	2026-01-19 21:18:53.395801+02	60
e4109d61-6eed-4bb3-a3ae-aa7d9fda6e17	t	LAB	NOT_COVERED	2026-01-19 21:18:53.395801+02	500.00	الفحص الجيني	2026-01-19 21:18:53.395801+02	0
349f4c91-69d1-4f4d-9c92-696c3e66a94c	t	LAB	NOT_COVERED	2026-01-19 21:18:53.395801+02	400.00	فحص الحساسية الشامل	2026-01-19 21:18:53.395801+02	0
350ed018-68f8-4c12-b2b6-bef2715702dd	t	RADIOLOGY	COVERED	2026-01-19 21:18:53.395801+02	80.00	أشعة الصدر	2026-01-19 21:18:53.395801+02	100
0a7847b1-80c2-4b59-a0c7-853a280251f2	t	RADIOLOGY	COVERED	2026-01-19 21:18:53.395801+02	100.00	أشعة العمود الفقري (الرقبة)	2026-01-19 21:18:53.395801+02	100
9aca7519-3b90-4ed9-bab6-53f0b16d522f	t	RADIOLOGY	COVERED	2026-01-19 21:18:53.395801+02	100.00	أشعة العمود الفقري (أسفل الظهر)	2026-01-19 21:18:53.395801+02	100
f1b1681e-7e74-48f7-9ef0-b9c1588f40d6	t	RADIOLOGY	COVERED	2026-01-19 21:18:53.395801+02	70.00	أشعة الأطراف	2026-01-19 21:18:53.395801+02	100
1fac3db8-0d64-4427-a3a2-bf67ffb127a7	t	RADIOLOGY	COVERED	2026-01-19 21:18:53.395801+02	150.00	سونار البطن	2026-01-19 21:18:53.395801+02	100
c84ed8c2-44d9-47f0-92ba-e345ed01c2b7	t	RADIOLOGY	COVERED	2026-01-19 21:18:53.395801+02	150.00	سونار الحوض	2026-01-19 21:18:53.395801+02	100
967aee61-4592-47a0-a2bb-e4d534fb7af8	t	RADIOLOGY	COVERED	2026-01-19 21:18:53.395801+02	200.00	إيكو القلب	2026-01-19 21:18:53.395801+02	100
efecd794-321b-4751-b2c1-5dceeec41af9	t	RADIOLOGY	COVERED	2026-01-19 21:18:53.395801+02	50.00	تخطيط القلب (ECG)	2026-01-19 21:18:53.395801+02	100
841a80a0-9d75-4fe2-b986-d37e26e09c8d	t	RADIOLOGY	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	400.00	أشعة مقطعية للرأس	2026-01-19 21:18:53.395801+02	80
581f443a-88f5-444d-bdeb-f768cafc356f	t	RADIOLOGY	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	450.00	أشعة مقطعية للصدر	2026-01-19 21:18:53.395801+02	80
059bec5a-1cf1-4d59-97f9-1b65ce90f50d	t	RADIOLOGY	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	500.00	أشعة مقطعية للبطن	2026-01-19 21:18:53.395801+02	80
1fde6339-c61e-44e4-9eb2-489aeccdada5	t	RADIOLOGY	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	800.00	رنين مغناطيسي للدماغ	2026-01-19 21:18:53.395801+02	70
1fe80ceb-ae32-4802-919b-612f8551be76	t	RADIOLOGY	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	850.00	رنين مغناطيسي للعمود الفقري	2026-01-19 21:18:53.395801+02	70
bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb	t	RADIOLOGY	NOT_COVERED	2026-01-19 21:18:53.395801+02	1500.00	مسح PET	2026-01-19 21:18:53.395801+02	0
\.


--
-- Data for Name: medicine_prices; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.medicine_prices (id, active, composition, coverage_status, created_at, drug_name, generic_name, price, type, unit, updated_at, coverage_percentage) FROM stdin;
e7ece66f-cf2a-4913-9a15-303cf17256e1	t	باراسيتامول 500 ملغ	COVERED	2026-01-19 21:18:53.395801+02	باراسيتامول 500 ملغ	باراسيتامول	5.00	أقراص	500 ملغ	2026-01-19 21:18:53.395801+02	100
8f7021f3-da71-420f-80ab-6d0d097411cf	t	أموكسيسيلين ثلاثي الماء 500 ملغ	COVERED	2026-01-19 21:18:53.395801+02	أموكسيسيلين 500 ملغ	أموكسيسيلين ثلاثي الماء	15.00	كبسولات	500 ملغ	2026-01-19 21:18:53.395801+02	100
a94d612f-7c1d-4012-823b-7cf240389c1e	t	إيبوبروفين 400 ملغ	COVERED	2026-01-19 21:18:53.395801+02	إيبوبروفين 400 ملغ	إيبوبروفين	8.00	أقراص	400 ملغ	2026-01-19 21:18:53.395801+02	100
ac48f27f-da1c-442d-9727-1ac559abe5d4	t	أوميبرازول 20 ملغ	COVERED	2026-01-19 21:18:53.395801+02	أوميبرازول 20 ملغ	أوميبرازول	12.00	كبسولات	20 ملغ	2026-01-19 21:18:53.395801+02	100
26ee62a2-9099-4ff8-b649-9f6574a5ce94	t	ميتفورمين هيدروكلوريد 500 ملغ	COVERED	2026-01-19 21:18:53.395801+02	ميتفورمين 500 ملغ	ميتفورمين هيدروكلوريد	10.00	أقراص	500 ملغ	2026-01-19 21:18:53.395801+02	100
971f4fdd-5602-457b-a26d-69a2c5d90ba9	t	أملوديبين بيسيلات 5 ملغ	COVERED	2026-01-19 21:18:53.395801+02	أملوديبين 5 ملغ	أملوديبين بيسيلات	15.00	أقراص	5 ملغ	2026-01-19 21:18:53.395801+02	100
d573b094-06dc-4472-8769-7f1e4db16033	t	أزيثرومايسين 250 ملغ	COVERED	2026-01-19 21:18:53.395801+02	أزيثرومايسين 250 ملغ	أزيثرومايسين	25.00	أقراص	250 ملغ	2026-01-19 21:18:53.395801+02	100
e533f6c5-2a75-49d0-81fb-20720d01b8ed	t	سيتيريزين هيدروكلوريد 10 ملغ	COVERED	2026-01-19 21:18:53.395801+02	سيتيريزين 10 ملغ	سيتيريزين هيدروكلوريد	6.00	أقراص	10 ملغ	2026-01-19 21:18:53.395801+02	100
0532b15f-6ff6-4e8b-b2f6-303970f3c60d	t	رانيتيدين هيدروكلوريد 150 ملغ	COVERED	2026-01-19 21:18:53.395801+02	رانيتيدين 150 ملغ	رانيتيدين هيدروكلوريد	8.00	أقراص	150 ملغ	2026-01-19 21:18:53.395801+02	100
6be9e52f-08ad-48dc-bfcb-ce473687ce79	t	ميتوبرولول طرطرات 50 ملغ	COVERED	2026-01-19 21:18:53.395801+02	ميتوبرولول 50 ملغ	ميتوبرولول طرطرات	12.00	أقراص	50 ملغ	2026-01-19 21:18:53.395801+02	100
f54f36d7-2e75-476e-838b-171fadb1da24	t	أتورفاستاتين كالسيوم 20 ملغ	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	أتورفاستاتين 20 ملغ	أتورفاستاتين كالسيوم	35.00	أقراص	20 ملغ	2026-01-19 21:18:53.395801+02	80
d6da4f36-bc4a-4f7a-8fa2-72590b4ce254	t	كلوبيدوغريل بيسلفات 75 ملغ	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	كلوبيدوغريل 75 ملغ	كلوبيدوغريل بيسلفات	45.00	أقراص	75 ملغ	2026-01-19 21:18:53.395801+02	80
0e6386b2-6501-47b1-afe3-2801ba07405a	t	دولوكستين هيدروكلوريد 30 ملغ	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	دولوكستين 30 ملغ	دولوكستين هيدروكلوريد	65.00	كبسولات	30 ملغ	2026-01-19 21:18:53.395801+02	70
a1e0a873-87b1-4cc5-bbb8-680656760758	t	روزوفاستاتين كالسيوم 10 ملغ	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	روزوفاستاتين 10 ملغ	روزوفاستاتين كالسيوم	40.00	أقراص	10 ملغ	2026-01-19 21:18:53.395801+02	80
e48e60b8-dce1-4558-b386-c82c88c2f95b	t	بريجابالين 75 ملغ	REQUIRES_APPROVAL	2026-01-19 21:18:53.395801+02	بريجابالين 75 ملغ	بريجابالين	55.00	كبسولات	75 ملغ	2026-01-19 21:18:53.395801+02	70
cf9e14c2-5cc1-4d4d-9211-8d4560d6384b	t	سيلدينافيل سيترات 50 ملغ	NOT_COVERED	2026-01-19 21:18:53.395801+02	سيلدينافيل 50 ملغ	سيلدينافيل سيترات	80.00	أقراص	50 ملغ	2026-01-19 21:18:53.395801+02	0
de4b14b9-738b-4f03-9052-a7f34eaa209f	t	مينوكسيديل 5%	NOT_COVERED	2026-01-19 21:18:53.395801+02	مينوكسيديل 5%	مينوكسيديل	120.00	محلول	60 مل	2026-01-19 21:18:53.395801+02	0
39fa8bc9-637b-41f7-af0a-db4b39504ddb	t	فيناسترايد 1 ملغ	NOT_COVERED	2026-01-19 21:18:53.395801+02	فيناسترايد 1 ملغ	فيناسترايد	150.00	أقراص	1 ملغ	2026-01-19 21:18:53.395801+02	0
\.


--
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.messages (id, content, is_read, sent_at, conversation_id, receiver_id, sender_id) FROM stdin;
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notifications (id, created_at, is_read, message, recipient_id, type, sender_id, replied) FROM stdin;
77777777-aaaa-aaaa-aaaa-777777777771	2026-01-17 21:18:53.395801+02	f	تم تقديم مطالبتك للاستشارة بتاريخ 2026-01-17 بنجاح.	88888888-8888-8888-8888-888888888881	CLAIM	\N	f
77777777-aaaa-aaaa-aaaa-777777777772	2026-01-14 21:18:53.395801+02	t	تمت الموافقة على مطالبتك. غطى التأمين 80% من المبلغ.	88888888-8888-8888-8888-888888888883	CLAIM	\N	f
77777777-aaaa-aaaa-aaaa-777777777773	2026-01-19 20:18:53.395801+02	f	عاجل: طلب طوارئ بانتظار الموافقة للمريض أحمد محمود.	33333333-3333-3333-3333-333333333333	EMERGENCY	88888888-8888-8888-8888-888888888881	f
77777777-aaaa-aaaa-aaaa-777777777774	2026-01-09 21:18:53.395801+02	t	تم التحقق من وصفتك الطبية وهي جاهزة للاستلام من صيدلية بيرزيت.	88888888-8888-8888-8888-888888888883	SYSTEM	55555555-5555-5555-5555-555555555555	f
77777777-aaaa-aaaa-aaaa-777777777775	2026-01-16 21:18:53.395801+02	f	تذكير: موعد فحص الهيموغلوبين السكري الأسبوع القادم. يرجى جدولة زيارتك للمختبر.	88888888-8888-8888-8888-888888888884	SYSTEM	\N	f
\.


--
-- Data for Name: password_reset_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.password_reset_tokens (id, created_at, expires_at, token, used, username) FROM stdin;
\.


--
-- Data for Name: policies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.policies (id, coverage_limit, created_at, deductible, description, emergency_rules, end_date, name, policy_no, start_date, status, updated_at) FROM stdin;
23b258e9-0b56-4c2f-b0e7-97ad82c6a83c	50000.00	2026-01-19 21:18:53.395801+02	100.00	بوليصة تأمين صحي شاملة لموظفي وطلاب جامعة بيرزيت. تشمل خدمات العيادات الخارجية والداخلية والطوارئ والمختبرات والأشعة.	تغطية الطوارئ حتى 10000 شيكل لكل حادثة. لا يتطلب موافقة مسبقة للحالات الطارئة الحقيقية.	2027-12-31	خطة جامعة بيرزيت الصحية	BZU-2026-001	2026-01-01	ACTIVE	2026-01-19 21:18:53.395801+02
\.


--
-- Data for Name: prescription_items; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.prescription_items (id, created_at, dosage, expiry_date, final_price, pharmacist_price, times_per_day, updated_at, prescription_id, price_list_id, calculated_quantity, covered_quantity, dispensed_quantity, drug_form, duration, pharmacist_price_per_unit, union_price_per_unit) FROM stdin;
\.


--
-- Data for Name: prescriptions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.prescriptions (id, created_at, dosage, instructions, status, updated_at, doctor_id, member_id, pharmacist_id, notes, total_price, diagnosis, is_chronic, treatment) FROM stdin;
ffffffff-ffff-ffff-ffff-ffffffffffff	2026-01-09 21:18:53.395801+02	قرص واحد يومياً	يؤخذ مع الطعام في الصباح	PENDING	2026-01-09 21:18:53.395801+02	44444444-4444-4444-4444-444444444444	88888888-8888-8888-8888-888888888881	\N	فيتامين ب12 للإرهاق	30	نقص الفيتامينات	f	مكملات فيتامين ب12
ffffffff-ffff-ffff-ffff-fffffffffff1	2026-01-14 21:18:53.395801+02	250 ملغ ثلاث مرات يومياً	إكمال دورة المضاد الحيوي كاملة	VERIFIED	2026-01-15 21:18:53.395801+02	44444444-4444-4444-4444-444444444446	88888888-8888-8888-8888-888888888883	55555555-5555-5555-5555-555555555555	مضاد حيوي للأطفال لالتهاب الجهاز التنفسي	105	التهاب الجهاز التنفسي العلوي	f	أموكسيسيلين 250 ملغ
ffffffff-ffff-ffff-ffff-fffffffffff2	2026-01-16 21:18:53.395801+02	500 ملغ مرتين يومياً	يؤخذ مع الوجبات، مراقبة مستوى السكر	VERIFIED	2026-01-17 21:18:53.395801+02	44444444-4444-4444-4444-44444444444d	88888888-8888-8888-8888-888888888884	55555555-5555-5555-5555-555555555555	إعادة تعبئة شهرية لإدارة السكري	300	داء السكري من النوع 2	t	ميتفورمين 500 ملغ
ffffffff-ffff-ffff-ffff-fffffffffff3	2026-01-17 21:18:53.395801+02	50 ملغ مرة واحدة يومياً	عند الحاجة لضعف الانتصاب	REJECTED	2026-01-18 21:18:53.395801+02	44444444-4444-4444-4444-44444444444f	88888888-8888-8888-8888-888888888883	55555555-5555-5555-5555-555555555555	مرفوض - غير مغطى بالتأمين	80	ضعف الانتصاب	f	سيلدينافيل 50 ملغ
\.


--
-- Data for Name: price_list; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.price_list (id, active, created_at, notes, price, provider_type, service_code, service_name, updated_at, service_details, max_age, min_age, quantity, coverage_percentage) FROM stdin;
11111111-bbbb-bbbb-bbbb-111111111111	t	2026-01-19 21:18:53.395801+02	فحص مختبري	50	LAB	LAB-CBC	تعداد الدم الكامل (CBC)	2026-01-19 21:18:53.395801+02	فحص شامل للدم	120	0	1	\N
22222222-bbbb-bbbb-bbbb-222222222222	t	2026-01-19 21:18:53.395801+02	فحص مختبري	70	LAB	LAB-HBA1C	الهيموغلوبين السكري (HbA1c)	2026-01-19 21:18:53.395801+02	فحص السكر التراكمي	120	0	1	\N
33333333-bbbb-bbbb-bbbb-333333333333	t	2026-01-19 21:18:53.395801+02	فحص مختبري	80	LAB	LAB-LIPID	تحليل الدهون	2026-01-19 21:18:53.395801+02	فحص الدهون والكوليسترول	120	0	1	\N
44444444-bbbb-bbbb-bbbb-444444444444	t	2026-01-19 21:18:53.395801+02	فحص أشعة	80	RADIOLOGY	RAD-CHEST	أشعة الصدر	2026-01-19 21:18:53.395801+02	أشعة سينية للصدر	120	0	1	\N
55555555-bbbb-bbbb-bbbb-555555555555	t	2026-01-19 21:18:53.395801+02	فحص أشعة	50	RADIOLOGY	RAD-ECG	تخطيط القلب (ECG)	2026-01-19 21:18:53.395801+02	تخطيط كهربائي للقلب	120	0	1	\N
66666666-bbbb-bbbb-bbbb-666666666666	t	2026-01-19 21:18:53.395801+02	فحص أشعة	150	RADIOLOGY	RAD-USABD	سونار البطن	2026-01-19 21:18:53.395801+02	فحص بالموجات فوق الصوتية للبطن	120	0	1	\N
\.


--
-- Data for Name: price_list_allowed_genders; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.price_list_allowed_genders (price_list_id, gender) FROM stdin;
\.


--
-- Data for Name: price_list_allowed_specializations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.price_list_allowed_specializations (price_list_id, specialization_id) FROM stdin;
\.


--
-- Data for Name: provider_policies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.provider_policies (id, provider_id, service_name, negotiated_price, coverage_percent, effective_from, effective_to, active, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: radiology_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.radiology_requests (id, created_at, notes, result_url, status, test_name, updated_at, doctor_id, member_id, radiologist_id, entered_price, approved_price, union_price, price_id, test_id, diagnosis, treatment) FROM stdin;
22222222-aaaa-aaaa-aaaa-222222222221	2026-01-18 21:18:53.395801+02	فحص للكشف عن التهاب رئوي	\N	PENDING	أشعة الصدر	2026-01-18 21:18:53.395801+02	44444444-4444-4444-4444-444444444446	88888888-8888-8888-8888-888888888883	\N	80	80	80	\N	44444444-bbbb-bbbb-bbbb-444444444444	التهاب الجهاز التنفسي مع اشتباه بالتهاب رئوي	تشخيصي
22222222-aaaa-aaaa-aaaa-222222222222	2026-01-09 21:18:53.395801+02	تخطيط قلب للخفقان	/results/radiology/ecg_result.pdf	COMPLETED	تخطيط القلب (ECG)	2026-01-11 21:18:53.395801+02	44444444-4444-4444-4444-444444444445	88888888-8888-8888-8888-888888888883	77777777-7777-7777-7777-777777777777	50	50	50	\N	55555555-bbbb-bbbb-bbbb-555555555555	خفقان القلب	تشخيصي
22222222-aaaa-aaaa-aaaa-222222222223	2026-01-12 21:18:53.395801+02	سونار البطن لمريض مزمن	/results/radiology/us_abdomen.pdf	COMPLETED	سونار البطن	2026-01-14 21:18:53.395801+02	44444444-4444-4444-4444-44444444444d	88888888-8888-8888-8888-888888888884	77777777-7777-7777-7777-777777777777	150	150	150	\N	66666666-bbbb-bbbb-bbbb-666666666666	فحص السكري - الكبد/الكلى	فحص سنوي
\.


--
-- Data for Name: revoked_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.revoked_tokens (id, expires_at, revoked_at, token) FROM stdin;
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (id, name) FROM stdin;
1c9ebceb-b151-4d57-91cc-a6c69de28c56	INSURANCE_CLIENT
09893f46-d674-4c7b-9333-ad4821c50151	DOCTOR
6f632aff-382a-48a4-8fc5-cbe50fae8222	PHARMACIST
3a68cb10-6026-4b0d-b154-8a3cd81a18a7	LAB_TECH
5342bc8b-7238-47d2-871e-800c7a0fd9b2	INSURANCE_MANAGER
9489a6f6-e315-495c-8215-43e7fae1e49e	MEDICAL_ADMIN
3c4c7431-0c52-4e5a-b8d3-6d3ee591f0ed	RADIOLOGIST
\.


--
-- Data for Name: search_profiles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.search_profiles (id, address, contact_info, description, location_lat, location_lng, name, type, owner_id, status, rejection_reason, clinic_registration, id_or_passport_copy, medical_license, university_degree) FROM stdin;
88888888-aaaa-aaaa-aaaa-888888888881	حرم الجامعة، مبنى أ	0591000004	عيادة طب عام تخدم مجتمع الجامعة	31.9539	35.1674	عيادة بيرزيت العامة	CLINIC	44444444-4444-4444-4444-444444444444	APPROVED	\N	reg_001.pdf	id_001.pdf	license_001.pdf	degree_001.pdf
88888888-aaaa-aaaa-aaaa-888888888882	حرم الجامعة، مبنى د	0591000016	صيدلية متكاملة مع مخزون واسع من الأدوية	31.9535	35.168	صيدلية بيرزيت	PHARMACY	55555555-5555-5555-5555-555555555555	APPROVED	\N	reg_002.pdf	id_002.pdf	license_002.pdf	degree_002.pdf
88888888-aaaa-aaaa-aaaa-888888888883	المركز الطبي، الطابق السفلي	0591000017	خدمات مخبرية شاملة	31.954	35.1678	مختبر بيرزيت الطبي	LAB	66666666-6666-6666-6666-666666666666	APPROVED	\N	reg_003.pdf	id_003.pdf	license_003.pdf	degree_003.pdf
\.


--
-- Data for Name: tests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tests (id, created_at, test_name, union_price, updated_at) FROM stdin;
\.


--
-- Data for Name: v_role_id; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.v_role_id (id) FROM stdin;
5342bc8b-7238-47d2-871e-800c7a0fd9b2
\.


--
-- Data for Name: visits; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.visits (id, created_at, doctor_specialization, notes, updated_at, visit_date, visit_type, visit_year, doctor_id, family_member_id, patient_id, previous_visit_id) FROM stdin;
dddddddd-dddd-dddd-dddd-dddddddddd01	2026-01-09 21:18:53.395801+02	General Practice	فحص روتيني، المريض يشكو من إرهاق خفيف	2026-01-09 21:18:53.395801+02	2026-01-09	NORMAL	2026	44444444-4444-4444-4444-444444444444	\N	88888888-8888-8888-8888-888888888881	\N
dddddddd-dddd-dddd-dddd-dddddddddd02	2026-01-11 21:18:53.395801+02	Cardiology	خفقان في القلب، تم طلب تخطيط قلب	2026-01-11 21:18:53.395801+02	2026-01-11	NORMAL	2026	44444444-4444-4444-4444-444444444445	\N	88888888-8888-8888-8888-888888888883	\N
dddddddd-dddd-dddd-dddd-dddddddddd03	2026-01-14 21:18:53.395801+02	Pediatrics	طفل يعاني من حمى وسعال	2026-01-14 21:18:53.395801+02	2026-01-14	NORMAL	2026	44444444-4444-4444-4444-444444444446	aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab	88888888-8888-8888-8888-888888888883	\N
dddddddd-dddd-dddd-dddd-dddddddddd04	2026-01-16 21:18:53.395801+02	Internal Medicine	متابعة السكري وإدارة مستوى السكر في الدم	2026-01-16 21:18:53.395801+02	2026-01-16	NORMAL	2026	44444444-4444-4444-4444-44444444444d	\N	88888888-8888-8888-8888-888888888884	\N
dddddddd-dddd-dddd-dddd-dddddddddd05	2026-01-18 21:18:53.395801+02	General Practice	متابعة لشكوى الإرهاق السابقة	2026-01-18 21:18:53.395801+02	2026-01-18	FOLLOW_UP	2026	44444444-4444-4444-4444-444444444444	\N	88888888-8888-8888-8888-888888888881	dddddddd-dddd-dddd-dddd-dddddddddd01
dddddddd-dddd-dddd-dddd-dddddddddd06	2026-01-19 21:18:53.395801+02	Dermatology	فحص طفح جلدي	2026-01-19 21:18:53.395801+02	2026-01-19	NORMAL	2026	44444444-4444-4444-4444-444444444447	\N	88888888-8888-8888-8888-888888888882	\N
\.


--
-- Name: doctor_specialization_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.doctor_specialization_id_seq', 12, true);


--
-- Name: annual_usage annual_usage_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.annual_usage
    ADD CONSTRAINT annual_usage_pkey PRIMARY KEY (id);


--
-- Name: chronic_patient_schedules chronic_patient_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chronic_patient_schedules
    ADD CONSTRAINT chronic_patient_schedules_pkey PRIMARY KEY (id);


--
-- Name: claims claims_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.claims
    ADD CONSTRAINT claims_pkey PRIMARY KEY (id);


--
-- Name: client_roles client_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_roles
    ADD CONSTRAINT client_roles_pkey PRIMARY KEY (client_id, role_id);


--
-- Name: clients clients_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT clients_pkey PRIMARY KEY (id);


--
-- Name: conversations conversations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT conversations_pkey PRIMARY KEY (id);


--
-- Name: coverage_usage coverage_usage_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverage_usage
    ADD CONSTRAINT coverage_usage_pkey PRIMARY KEY (id);


--
-- Name: coverages coverages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverages
    ADD CONSTRAINT coverages_pkey PRIMARY KEY (id);


--
-- Name: doctor_medicine_assignments doctor_medicine_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_medicine_assignments
    ADD CONSTRAINT doctor_medicine_assignments_pkey PRIMARY KEY (id);


--
-- Name: doctor_procedures doctor_procedures_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_procedures
    ADD CONSTRAINT doctor_procedures_pkey PRIMARY KEY (id);


--
-- Name: doctor_specialization doctor_specialization_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_specialization
    ADD CONSTRAINT doctor_specialization_pkey PRIMARY KEY (id);


--
-- Name: doctor_test_assignments doctor_test_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_test_assignments
    ADD CONSTRAINT doctor_test_assignments_pkey PRIMARY KEY (id);


--
-- Name: emergency_requests emergency_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emergency_requests
    ADD CONSTRAINT emergency_requests_pkey PRIMARY KEY (id);


--
-- Name: family_members family_members_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.family_members
    ADD CONSTRAINT family_members_pkey PRIMARY KEY (id);


--
-- Name: healthcare_provider_claims healthcare_provider_claims_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.healthcare_provider_claims
    ADD CONSTRAINT healthcare_provider_claims_pkey PRIMARY KEY (id);


--
-- Name: lab_requests lab_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lab_requests
    ADD CONSTRAINT lab_requests_pkey PRIMARY KEY (id);


--
-- Name: medical_diagnoses medical_diagnoses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medical_diagnoses
    ADD CONSTRAINT medical_diagnoses_pkey PRIMARY KEY (id);


--
-- Name: medical_records medical_records_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medical_records
    ADD CONSTRAINT medical_records_pkey PRIMARY KEY (id);


--
-- Name: medical_tests medical_tests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medical_tests
    ADD CONSTRAINT medical_tests_pkey PRIMARY KEY (id);


--
-- Name: medicine_prices medicine_prices_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medicine_prices
    ADD CONSTRAINT medicine_prices_pkey PRIMARY KEY (id);


--
-- Name: messages messages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);


--
-- Name: policies policies_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.policies
    ADD CONSTRAINT policies_pkey PRIMARY KEY (id);


--
-- Name: prescription_items prescription_items_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescription_items
    ADD CONSTRAINT prescription_items_pkey PRIMARY KEY (id);


--
-- Name: prescriptions prescriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescriptions
    ADD CONSTRAINT prescriptions_pkey PRIMARY KEY (id);


--
-- Name: price_list price_list_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.price_list
    ADD CONSTRAINT price_list_pkey PRIMARY KEY (id);


--
-- Name: provider_policies provider_policies_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.provider_policies
    ADD CONSTRAINT provider_policies_pkey PRIMARY KEY (id);


--
-- Name: radiology_requests radiology_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.radiology_requests
    ADD CONSTRAINT radiology_requests_pkey PRIMARY KEY (id);


--
-- Name: revoked_tokens revoked_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.revoked_tokens
    ADD CONSTRAINT revoked_tokens_pkey PRIMARY KEY (id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: search_profiles search_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_profiles
    ADD CONSTRAINT search_profiles_pkey PRIMARY KEY (id);


--
-- Name: tests tests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tests
    ADD CONSTRAINT tests_pkey PRIMARY KEY (id);


--
-- Name: doctor_medicine_assignments uk6ktkigqqe3gtlw6cckeoeybwi; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_medicine_assignments
    ADD CONSTRAINT uk6ktkigqqe3gtlw6cckeoeybwi UNIQUE (doctor_id, medicine_id);


--
-- Name: family_members uk6uusbnsgol75t193gji8ecpsk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.family_members
    ADD CONSTRAINT uk6uusbnsgol75t193gji8ecpsk UNIQUE (national_id);


--
-- Name: password_reset_tokens uk71lqwbwtklmljk3qlsugr1mig; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT uk71lqwbwtklmljk3qlsugr1mig UNIQUE (token);


--
-- Name: coverage_usage uk8kslwiw8nr9qq05qyi5xbxdh6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverage_usage
    ADD CONSTRAINT uk8kslwiw8nr9qq05qyi5xbxdh6 UNIQUE (client_id, provider_specialization, usage_date);


--
-- Name: coverages uk8qvyc0n60sqc4r7n3u83j7u89; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverages
    ADD CONSTRAINT uk8qvyc0n60sqc4r7n3u83j7u89 UNIQUE (policy_id, service_name);


--
-- Name: provider_policies uk8uu3s87y6qyg0l9cdhxyx1lfj; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.provider_policies
    ADD CONSTRAINT uk8uu3s87y6qyg0l9cdhxyx1lfj UNIQUE (provider_id, service_name);


--
-- Name: clients uk9tpl6kc2cx19t73txqcej15db; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT uk9tpl6kc2cx19t73txqcej15db UNIQUE (national_id);


--
-- Name: family_members ukctu694c5o6odtbo5fgcemyron; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.family_members
    ADD CONSTRAINT ukctu694c5o6odtbo5fgcemyron UNIQUE (insurance_number);


--
-- Name: policies ukd27t4fo4735lcedivrfo0ie5i; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.policies
    ADD CONSTRAINT ukd27t4fo4735lcedivrfo0ie5i UNIQUE (policy_no);


--
-- Name: tests ukeun95fhgw0odg4ggweopwrn1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tests
    ADD CONSTRAINT ukeun95fhgw0odg4ggweopwrn1 UNIQUE (test_name);


--
-- Name: annual_usage ukhpe9uem1wp7cio9mlwid3pwkc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.annual_usage
    ADD CONSTRAINT ukhpe9uem1wp7cio9mlwid3pwkc UNIQUE (client_id, year, service_type);


--
-- Name: revoked_tokens uko7nu95tpd50oqhacdvs5qk1bf; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.revoked_tokens
    ADD CONSTRAINT uko7nu95tpd50oqhacdvs5qk1bf UNIQUE (token);


--
-- Name: roles ukofx66keruapi6vyqpv6f2or37; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT ukofx66keruapi6vyqpv6f2or37 UNIQUE (name);


--
-- Name: clients uksrv16ica2c1csub334bxjjb59; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT uksrv16ica2c1csub334bxjjb59 UNIQUE (email);


--
-- Name: doctor_test_assignments uktbrc29m3s0donph2ttv393qmb; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_test_assignments
    ADD CONSTRAINT uktbrc29m3s0donph2ttv393qmb UNIQUE (doctor_id, test_id);


--
-- Name: annual_usage uq_annual_usage_client_year_service; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.annual_usage
    ADD CONSTRAINT uq_annual_usage_client_year_service UNIQUE (client_id, year, service_type);


--
-- Name: coverage_usage uq_coverage_usage_client_spec_date; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverage_usage
    ADD CONSTRAINT uq_coverage_usage_client_spec_date UNIQUE (client_id, provider_specialization, usage_date);


--
-- Name: provider_policies uq_provider_policies_provider_service; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.provider_policies
    ADD CONSTRAINT uq_provider_policies_provider_service UNIQUE (provider_id, service_name);


--
-- Name: visits visits_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits
    ADD CONSTRAINT visits_pkey PRIMARY KEY (id);


--
-- Name: idx_annual_usage_client_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_annual_usage_client_id ON public.annual_usage USING btree (client_id);


--
-- Name: idx_annual_usage_year; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_annual_usage_year ON public.annual_usage USING btree (year);


--
-- Name: idx_clients_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_clients_email ON public.clients USING btree (email);


--
-- Name: idx_coverage_usage_client_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coverage_usage_client_id ON public.coverage_usage USING btree (client_id);


--
-- Name: idx_coverage_usage_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coverage_usage_date ON public.coverage_usage USING btree (usage_date);


--
-- Name: idx_coverage_usage_year; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coverage_usage_year ON public.coverage_usage USING btree (year);


--
-- Name: idx_dma_doctor; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dma_doctor ON public.doctor_medicine_assignments USING btree (doctor_id);


--
-- Name: idx_dma_medicine; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dma_medicine ON public.doctor_medicine_assignments USING btree (medicine_id);


--
-- Name: idx_dma_specialization; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dma_specialization ON public.doctor_medicine_assignments USING btree (specialization);


--
-- Name: idx_dta_doctor; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dta_doctor ON public.doctor_test_assignments USING btree (doctor_id);


--
-- Name: idx_dta_specialization; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dta_specialization ON public.doctor_test_assignments USING btree (specialization);


--
-- Name: idx_dta_test; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dta_test ON public.doctor_test_assignments USING btree (test_id);


--
-- Name: idx_dta_test_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dta_test_type ON public.doctor_test_assignments USING btree (test_type);


--
-- Name: idx_expiration; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_expiration ON public.revoked_tokens USING btree (expires_at);


--
-- Name: idx_provider_policies_active; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_provider_policies_active ON public.provider_policies USING btree (active);


--
-- Name: idx_provider_policies_provider_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_provider_policies_provider_id ON public.provider_policies USING btree (provider_id);


--
-- Name: idx_token; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_token ON public.revoked_tokens USING btree (token);


--
-- Name: idx_visit_doctor_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_visit_doctor_date ON public.visits USING btree (doctor_id, visit_date);


--
-- Name: idx_visit_family_member_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_visit_family_member_date ON public.visits USING btree (family_member_id, visit_date);


--
-- Name: idx_visit_patient_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_visit_patient_date ON public.visits USING btree (patient_id, visit_date);


--
-- Name: idx_visit_patient_year; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_visit_patient_year ON public.visits USING btree (patient_id, visit_year);


--
-- Name: coverages fk1eeuvnw4pvkj6l4cf33iq8tov; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverages
    ADD CONSTRAINT fk1eeuvnw4pvkj6l4cf33iq8tov FOREIGN KEY (policy_id) REFERENCES public.policies(id);


--
-- Name: lab_requests fk1rffqggb6fa8csprlcuayi5rr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lab_requests
    ADD CONSTRAINT fk1rffqggb6fa8csprlcuayi5rr FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: client_roles fk20mdyn9gv0h2sauw6qkesfxom; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_roles
    ADD CONSTRAINT fk20mdyn9gv0h2sauw6qkesfxom FOREIGN KEY (client_id) REFERENCES public.clients(id) ON DELETE CASCADE;


--
-- Name: visits fk3kxgrlgagyfp6upkeufrthxfg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits
    ADD CONSTRAINT fk3kxgrlgagyfp6upkeufrthxfg FOREIGN KEY (previous_visit_id) REFERENCES public.visits(id);


--
-- Name: claim_invoice_images fk3uroi3nuflenc4n6lm485stcs; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.claim_invoice_images
    ADD CONSTRAINT fk3uroi3nuflenc4n6lm485stcs FOREIGN KEY (claim_id) REFERENCES public.claims(id);


--
-- Name: client_roles fk4o8ntxejbpn5quw4au89kmbwv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_roles
    ADD CONSTRAINT fk4o8ntxejbpn5quw4au89kmbwv FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: radiology_requests fk50fbtrjyhxeqiygs26ie4nejx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.radiology_requests
    ADD CONSTRAINT fk50fbtrjyhxeqiygs26ie4nejx FOREIGN KEY (member_id) REFERENCES public.clients(id);


--
-- Name: family_member_documents fk52646ajui2rptvi6d5qsbqgvo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.family_member_documents
    ADD CONSTRAINT fk52646ajui2rptvi6d5qsbqgvo FOREIGN KEY (family_member_id) REFERENCES public.family_members(id);


--
-- Name: doctor_test_assignments fk562hq0uxpdk98yptew8ig4lk3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_test_assignments
    ADD CONSTRAINT fk562hq0uxpdk98yptew8ig4lk3 FOREIGN KEY (test_id) REFERENCES public.medical_tests(id);


--
-- Name: doctor_test_assignments fk5yri2wb5jl8wvwam7vacrom9j; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_test_assignments
    ADD CONSTRAINT fk5yri2wb5jl8wvwam7vacrom9j FOREIGN KEY (assigned_by) REFERENCES public.clients(id);


--
-- Name: prescriptions fk6371p076t2y5lic6mdt8q01pp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescriptions
    ADD CONSTRAINT fk6371p076t2y5lic6mdt8q01pp FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: doctor_medicine_assignments fk656dosx2c7kd8ghavd6668c8g; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_medicine_assignments
    ADD CONSTRAINT fk656dosx2c7kd8ghavd6668c8g FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: prescription_items fk6uh7tdy2lv6sx34u1365acqsf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescription_items
    ADD CONSTRAINT fk6uh7tdy2lv6sx34u1365acqsf FOREIGN KEY (prescription_id) REFERENCES public.prescriptions(id);


--
-- Name: radiology_requests fk78excs6dv3ykf0a26x65ugwy2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.radiology_requests
    ADD CONSTRAINT fk78excs6dv3ykf0a26x65ugwy2 FOREIGN KEY (radiologist_id) REFERENCES public.clients(id);


--
-- Name: radiology_requests fk7pq145t1rkyfmi1yq30t36x59; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.radiology_requests
    ADD CONSTRAINT fk7pq145t1rkyfmi1yq30t36x59 FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: doctor_test_assignments fk8s10tn0lvymlb86xufs0jclmq; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_test_assignments
    ADD CONSTRAINT fk8s10tn0lvymlb86xufs0jclmq FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: claims fk939bdcedi2vql56c3b5xa4vis; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.claims
    ADD CONSTRAINT fk939bdcedi2vql56c3b5xa4vis FOREIGN KEY (member_id) REFERENCES public.clients(id);


--
-- Name: conversations fk9hb7wet212ewsyrhkm0d31nhx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT fk9hb7wet212ewsyrhkm0d31nhx FOREIGN KEY (user2_id) REFERENCES public.clients(id);


--
-- Name: visits fk9k5sig9v4lyeql9j4w7jgvbe8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits
    ADD CONSTRAINT fk9k5sig9v4lyeql9j4w7jgvbe8 FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: annual_usage fk_annual_usage_client; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.annual_usage
    ADD CONSTRAINT fk_annual_usage_client FOREIGN KEY (client_id) REFERENCES public.clients(id) ON DELETE CASCADE;


--
-- Name: coverage_usage fk_coverage_usage_client; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverage_usage
    ADD CONSTRAINT fk_coverage_usage_client FOREIGN KEY (client_id) REFERENCES public.clients(id) ON DELETE CASCADE;


--
-- Name: emergency_requests fk_doctor_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emergency_requests
    ADD CONSTRAINT fk_doctor_id FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: emergency_requests fk_member_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emergency_requests
    ADD CONSTRAINT fk_member_id FOREIGN KEY (member_id) REFERENCES public.clients(id);


--
-- Name: notifications fk_notifications_sender; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fk_notifications_sender FOREIGN KEY (sender_id) REFERENCES public.clients(id);


--
-- Name: provider_policies fk_provider_policies_provider; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.provider_policies
    ADD CONSTRAINT fk_provider_policies_provider FOREIGN KEY (provider_id) REFERENCES public.clients(id) ON DELETE CASCADE;


--
-- Name: messages fka8yuuiu8ih0w6oggnc7jbtv5q; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fka8yuuiu8ih0w6oggnc7jbtv5q FOREIGN KEY (receiver_id) REFERENCES public.clients(id);


--
-- Name: price_list_allowed_specializations fkamy68liyh3hr9v0dlr5qooxgo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.price_list_allowed_specializations
    ADD CONSTRAINT fkamy68liyh3hr9v0dlr5qooxgo FOREIGN KEY (specialization_id) REFERENCES public.doctor_specialization(id);


--
-- Name: doctor_medicine_assignments fkbma90pbxeiisirgr5dja9lwvl; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_medicine_assignments
    ADD CONSTRAINT fkbma90pbxeiisirgr5dja9lwvl FOREIGN KEY (assigned_by) REFERENCES public.clients(id);


--
-- Name: visits fkbtgdrpb7t9o3crxcaqi7ehvdx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits
    ADD CONSTRAINT fkbtgdrpb7t9o3crxcaqi7ehvdx FOREIGN KEY (patient_id) REFERENCES public.clients(id);


--
-- Name: claims fkc2fxh3pjpbidb9eefwx7kgyhl; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.claims
    ADD CONSTRAINT fkc2fxh3pjpbidb9eefwx7kgyhl FOREIGN KEY (admin_reviewer_id) REFERENCES public.clients(id);


--
-- Name: search_profiles fkcu7um5fc6no2vbqllqj09581t; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_profiles
    ADD CONSTRAINT fkcu7um5fc6no2vbqllqj09581t FOREIGN KEY (owner_id) REFERENCES public.clients(id);


--
-- Name: lab_requests fkcun9cdxcato9jqx6fj9b5nuk5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lab_requests
    ADD CONSTRAINT fkcun9cdxcato9jqx6fj9b5nuk5 FOREIGN KEY (member_id) REFERENCES public.clients(id);


--
-- Name: price_list_allowed_genders fkcve5cfs3v0ka07fu7o8jykhcr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.price_list_allowed_genders
    ADD CONSTRAINT fkcve5cfs3v0ka07fu7o8jykhcr FOREIGN KEY (price_list_id) REFERENCES public.price_list(id);


--
-- Name: conversations fkddlpoqm3rdfbkv7jd9r1pg2wk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT fkddlpoqm3rdfbkv7jd9r1pg2wk FOREIGN KEY (user1_id) REFERENCES public.clients(id);


--
-- Name: doctor_specialization_allowed_genders fkfi0sf5yk23lt0jdtmmk8pd0y6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_specialization_allowed_genders
    ADD CONSTRAINT fkfi0sf5yk23lt0jdtmmk8pd0y6 FOREIGN KEY (specialization_id) REFERENCES public.doctor_specialization(id);


--
-- Name: healthcare_provider_claims fkfweswl5utabmgee2haxrf48dg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.healthcare_provider_claims
    ADD CONSTRAINT fkfweswl5utabmgee2haxrf48dg FOREIGN KEY (provider_id) REFERENCES public.clients(id);


--
-- Name: messages fkhldlthynlnp1d04ftgn4xvddp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fkhldlthynlnp1d04ftgn4xvddp FOREIGN KEY (sender_id) REFERENCES public.clients(id);


--
-- Name: family_members fkihjs36r9w66us9vwyaar9kqux; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.family_members
    ADD CONSTRAINT fkihjs36r9w66us9vwyaar9kqux FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: price_list_allowed_specializations fkj4ffj7t0puwpglt5oet4ui5pk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.price_list_allowed_specializations
    ADD CONSTRAINT fkj4ffj7t0puwpglt5oet4ui5pk FOREIGN KEY (price_list_id) REFERENCES public.price_list(id);


--
-- Name: chronic_patient_schedules fkjfeibcx5qb5xwlagp9opt4lvn; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chronic_patient_schedules
    ADD CONSTRAINT fkjfeibcx5qb5xwlagp9opt4lvn FOREIGN KEY (patient_id) REFERENCES public.clients(id);


--
-- Name: notifications fkjm7q7l8p5npt0c3cusfi898pj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fkjm7q7l8p5npt0c3cusfi898pj FOREIGN KEY (recipient_id) REFERENCES public.clients(id);


--
-- Name: healthcare_provider_claims fkjnhpiqg4ytyaavvud6wml5y5l; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.healthcare_provider_claims
    ADD CONSTRAINT fkjnhpiqg4ytyaavvud6wml5y5l FOREIGN KEY (policy_id) REFERENCES public.policies(id);


--
-- Name: medical_records fklg3kpf7sme9ko5f4h2t5ofoy5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medical_records
    ADD CONSTRAINT fklg3kpf7sme9ko5f4h2t5ofoy5 FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: claims fklia9d35fuo6rs1adsvumdyfbi; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.claims
    ADD CONSTRAINT fklia9d35fuo6rs1adsvumdyfbi FOREIGN KEY (medical_reviewer_id) REFERENCES public.clients(id);


--
-- Name: claims fkm0w2xffwe13pmkusoxnxuim7j; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.claims
    ADD CONSTRAINT fkm0w2xffwe13pmkusoxnxuim7j FOREIGN KEY (policy_id) REFERENCES public.policies(id);


--
-- Name: lab_requests fkmjcvdaupmu9k57ea6pg57932x; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lab_requests
    ADD CONSTRAINT fkmjcvdaupmu9k57ea6pg57932x FOREIGN KEY (price_id) REFERENCES public.price_list(id);


--
-- Name: clients fkmvac0bkdq2xnk1cblpvw5aiyh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT fkmvac0bkdq2xnk1cblpvw5aiyh FOREIGN KEY (policy_id) REFERENCES public.policies(id);


--
-- Name: lab_requests fknjc98od4ejkcgcya6gtibo4xk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lab_requests
    ADD CONSTRAINT fknjc98od4ejkcgcya6gtibo4xk FOREIGN KEY (lab_tech_id) REFERENCES public.clients(id);


--
-- Name: prescription_items fkooqpbcfdxsl8kpmytgyu7indp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescription_items
    ADD CONSTRAINT fkooqpbcfdxsl8kpmytgyu7indp FOREIGN KEY (price_list_id) REFERENCES public.price_list(id);


--
-- Name: radiology_requests fkpvtehayep4wh962tt82dqva2u; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.radiology_requests
    ADD CONSTRAINT fkpvtehayep4wh962tt82dqva2u FOREIGN KEY (test_id) REFERENCES public.price_list(id);


--
-- Name: prescriptions fkpwyw9am7es9nqydvqh8e15578; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescriptions
    ADD CONSTRAINT fkpwyw9am7es9nqydvqh8e15578 FOREIGN KEY (member_id) REFERENCES public.clients(id);


--
-- Name: medical_records fkqsw9jdc6wey67mfjo9ldx6mkg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medical_records
    ADD CONSTRAINT fkqsw9jdc6wey67mfjo9ldx6mkg FOREIGN KEY (member_id) REFERENCES public.clients(id);


--
-- Name: radiology_requests fkrn7vjlmof2cpfbt0bnd58dw0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.radiology_requests
    ADD CONSTRAINT fkrn7vjlmof2cpfbt0bnd58dw0 FOREIGN KEY (price_id) REFERENCES public.price_list(id);


--
-- Name: client_university_cards fksppsg3e5lyg5sr6w7unssrny1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_university_cards
    ADD CONSTRAINT fksppsg3e5lyg5sr6w7unssrny1 FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: messages fkt492th6wsovh1nush5yl5jj8e; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fkt492th6wsovh1nush5yl5jj8e FOREIGN KEY (conversation_id) REFERENCES public.conversations(id);


--
-- Name: prescriptions fkt5puwvjok2nfm3400c5bf01ql; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescriptions
    ADD CONSTRAINT fkt5puwvjok2nfm3400c5bf01ql FOREIGN KEY (pharmacist_id) REFERENCES public.clients(id);


--
-- Name: visits fkt9ut7lemcm7jgab8d304bjrp6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits
    ADD CONSTRAINT fkt9ut7lemcm7jgab8d304bjrp6 FOREIGN KEY (family_member_id) REFERENCES public.family_members(id);


--
-- Name: client_chronic_documents fktawyg5x5kik281uou72bgpke5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_chronic_documents
    ADD CONSTRAINT fktawyg5x5kik281uou72bgpke5 FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: client_chronic_diseases fkthwhu1qkayrthummgrk6hqg83; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_chronic_diseases
    ADD CONSTRAINT fkthwhu1qkayrthummgrk6hqg83 FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: doctor_medicine_assignments fktk4kg5ctu4lce8552xhgpk5wd; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_medicine_assignments
    ADD CONSTRAINT fktk4kg5ctu4lce8552xhgpk5wd FOREIGN KEY (medicine_id) REFERENCES public.medicine_prices(id);


--
-- PostgreSQL database dump complete
--

\unrestrict viGpKokbPryjDzT2lWiqCsdT1Q39GL3BPfWQtsFZfGkaA69gce1d2ya9BHOjPXX

