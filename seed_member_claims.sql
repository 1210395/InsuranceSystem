-- Insert test data into claims table (member/client claims)
DO $$
DECLARE
    v_client_id UUID;
    v_policy_id UUID;
BEGIN
    SELECT id INTO v_client_id FROM clients WHERE username = 'client';
    SELECT id INTO v_policy_id FROM policies LIMIT 1;

    -- PENDING claims
    INSERT INTO claims (id, member_id, policy_id, amount, description, service_date, status, submitted_at, diagnosis, doctor_name, provider_name, is_covered, insurance_covered_amount, member_name)
    VALUES
        (gen_random_uuid(), v_client_id, v_policy_id, 250.00, 'Medical consultation for chronic condition', CURRENT_DATE - 1, 'PENDING', NOW() - INTERVAL '1 day', 'Chronic Fatigue', 'Dr. Ahmad Hassan', 'University Health Center', true, 175.00, 'Khaled Abu Omar'),
        (gen_random_uuid(), v_client_id, v_policy_id, 180.00, 'Blood tests and laboratory work', CURRENT_DATE - 2, 'PENDING', NOW() - INTERVAL '2 days', 'Routine Testing', 'Dr. Sara Khalil', 'Central Lab', true, 126.00, 'Khaled Abu Omar'),
        (gen_random_uuid(), v_client_id, v_policy_id, 320.00, 'X-Ray examination', CURRENT_DATE - 3, 'PENDING', NOW() - INTERVAL '3 days', 'Chest Pain Investigation', 'Dr. Mohammad Ali', 'Radiology Center', true, 224.00, 'Khaled Abu Omar'),
        (gen_random_uuid(), v_client_id, v_policy_id, 150.00, 'Prescription medications', CURRENT_DATE - 4, 'PENDING', NOW() - INTERVAL '4 days', 'Hypertension Treatment', 'Dr. Fatima Nasser', 'University Pharmacy', true, 105.00, 'Khaled Abu Omar');

    -- APPROVED claims
    INSERT INTO claims (id, member_id, policy_id, amount, description, service_date, status, submitted_at, approved_at, diagnosis, doctor_name, provider_name, is_covered, insurance_covered_amount, member_name)
    VALUES
        (gen_random_uuid(), v_client_id, v_policy_id, 500.00, 'Minor surgical procedure', CURRENT_DATE - 15, 'APPROVED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '10 days', 'Appendicitis', 'Dr. Ahmad Hassan', 'University Hospital', true, 350.00, 'Khaled Abu Omar'),
        (gen_random_uuid(), v_client_id, v_policy_id, 200.00, 'Specialist consultation', CURRENT_DATE - 20, 'APPROVED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '15 days', 'Cardiac Review', 'Dr. Specialist Cardiologist', 'Heart Center', true, 140.00, 'Khaled Abu Omar'),
        (gen_random_uuid(), v_client_id, v_policy_id, 120.00, 'Physical therapy session', CURRENT_DATE - 25, 'APPROVED', NOW() - INTERVAL '25 days', NOW() - INTERVAL '20 days', 'Back Pain Rehab', 'PT Ahmad', 'Rehab Center', true, 84.00, 'Khaled Abu Omar'),
        (gen_random_uuid(), v_client_id, v_policy_id, 350.00, 'MRI scan', CURRENT_DATE - 30, 'APPROVED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '25 days', 'Knee Injury', 'Dr. Orthopedic', 'Imaging Center', true, 245.00, 'Khaled Abu Omar');

    -- REJECTED claims
    INSERT INTO claims (id, member_id, policy_id, amount, description, service_date, status, submitted_at, rejected_at, rejection_reason, diagnosis, doctor_name, provider_name, is_covered, member_name)
    VALUES
        (gen_random_uuid(), v_client_id, v_policy_id, 800.00, 'Cosmetic dental procedure', CURRENT_DATE - 35, 'REJECTED', NOW() - INTERVAL '35 days', NOW() - INTERVAL '30 days', 'Cosmetic procedures are not covered under the policy', 'Teeth Whitening', 'Dr. Dentist', 'Dental Clinic', false, 'Khaled Abu Omar'),
        (gen_random_uuid(), v_client_id, v_policy_id, 1200.00, 'Elective surgery', CURRENT_DATE - 40, 'REJECTED', NOW() - INTERVAL '40 days', NOW() - INTERVAL '35 days', 'Elective procedures require prior authorization which was not obtained', 'Elective', 'Dr. Surgeon', 'Private Hospital', false, 'Khaled Abu Omar');

    -- APPROVED_BY_MEDICAL claims (awaiting admin)
    INSERT INTO claims (id, member_id, policy_id, amount, description, service_date, status, submitted_at, medical_reviewed_at, diagnosis, doctor_name, provider_name, is_covered, insurance_covered_amount, member_name)
    VALUES
        (gen_random_uuid(), v_client_id, v_policy_id, 400.00, 'Emergency room visit', CURRENT_DATE - 5, 'APPROVED_BY_MEDICAL', NOW() - INTERVAL '5 days', NOW() - INTERVAL '3 days', 'Severe Allergic Reaction', 'Dr. Emergency', 'ER Department', true, 280.00, 'Khaled Abu Omar'),
        (gen_random_uuid(), v_client_id, v_policy_id, 280.00, 'Ultrasound examination', CURRENT_DATE - 7, 'APPROVED_BY_MEDICAL', NOW() - INTERVAL '7 days', NOW() - INTERVAL '5 days', 'Abdominal Pain', 'Dr. Radiologist', 'Ultrasound Center', true, 196.00, 'Khaled Abu Omar');

    -- AWAITING_ADMIN_REVIEW claims
    INSERT INTO claims (id, member_id, policy_id, amount, description, service_date, status, submitted_at, medical_reviewed_at, diagnosis, doctor_name, provider_name, is_covered, insurance_covered_amount, member_name)
    VALUES
        (gen_random_uuid(), v_client_id, v_policy_id, 600.00, 'Dental treatment', CURRENT_DATE - 8, 'AWAITING_ADMIN_REVIEW', NOW() - INTERVAL '8 days', NOW() - INTERVAL '6 days', 'Root Canal', 'Dr. Dental Surgeon', 'Dental Hospital', true, 420.00, 'Khaled Abu Omar'),
        (gen_random_uuid(), v_client_id, v_policy_id, 450.00, 'Eye examination and glasses', CURRENT_DATE - 10, 'AWAITING_ADMIN_REVIEW', NOW() - INTERVAL '10 days', NOW() - INTERVAL '8 days', 'Vision Correction', 'Dr. Eye Specialist', 'Vision Center', true, 315.00, 'Khaled Abu Omar');

    RAISE NOTICE 'Successfully inserted member claims!';
END $$;
