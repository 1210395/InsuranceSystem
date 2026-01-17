-- Get provider IDs and insert test claims
DO $$
DECLARE
    v_doctor_id UUID;
    v_pharmacist_id UUID;
    v_labtech_id UUID;
    v_radiologist_id UUID;
    v_client_id UUID;
    v_policy_id UUID;
BEGIN
    SELECT id INTO v_doctor_id FROM clients WHERE username = 'doctor';
    SELECT id INTO v_pharmacist_id FROM clients WHERE username = 'pharmacist';
    SELECT id INTO v_labtech_id FROM clients WHERE username = 'labtech';
    SELECT id INTO v_radiologist_id FROM clients WHERE username = 'radiologist';
    SELECT id INTO v_client_id FROM clients WHERE username = 'client';
    SELECT id INTO v_policy_id FROM policies LIMIT 1;

    -- Doctor claims - PENDING (for Medical Review)
    INSERT INTO healthcare_provider_claims (id, provider_id, client_id, client_name, description, amount, service_date, status, submitted_at, diagnosis, treatment_details, provider_role, provider_name, provider_employee_id, is_follow_up, client_age, client_gender, client_employee_id)
    VALUES
        (gen_random_uuid(), v_doctor_id, v_client_id, 'Khaled Abu Omar', 'Consultation - Hypertension', 150.00, CURRENT_DATE - 1, 'PENDING', NOW() - INTERVAL '1 day', 'Hypertension Stage 1', 'Prescribed ACE inhibitors, lifestyle modifications', 'DOCTOR', 'Dr. Ahmad Hassan', 'DOC001', false, 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_doctor_id, v_client_id, 'Khaled Abu Omar', 'Follow-up - Diabetes Type 2', 0.00, CURRENT_DATE - 2, 'PENDING', NOW() - INTERVAL '2 days', 'Diabetes Type 2', 'Medication adjustment, blood sugar monitoring', 'DOCTOR', 'Dr. Ahmad Hassan', 'DOC001', true, 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_doctor_id, v_client_id, 'Khaled Abu Omar', 'Emergency Visit - Chest Pain', 300.00, CURRENT_DATE - 3, 'PENDING', NOW() - INTERVAL '3 days', 'Chest Pain - Cardiac', 'ECG performed, cardiac enzymes ordered', 'DOCTOR', 'Dr. Ahmad Hassan', 'DOC001', false, 45, 'MALE', 'EMP12345');

    -- Doctor claims - APPROVED
    INSERT INTO healthcare_provider_claims (id, provider_id, client_id, client_name, description, amount, service_date, status, submitted_at, approved_at, diagnosis, treatment_details, provider_role, provider_name, provider_employee_id, is_follow_up, client_age, client_gender, client_employee_id)
    VALUES
        (gen_random_uuid(), v_doctor_id, v_client_id, 'Khaled Abu Omar', 'Routine Check-up', 80.00, CURRENT_DATE - 10, 'APPROVED', NOW() - INTERVAL '10 days', NOW() - INTERVAL '8 days', 'General Check-up', 'No issues found, annual wellness exam', 'DOCTOR', 'Dr. Ahmad Hassan', 'DOC001', false, 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_doctor_id, v_client_id, 'Khaled Abu Omar', 'Specialist Referral - Cardiology', 200.00, CURRENT_DATE - 15, 'APPROVED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '12 days', 'Cardiac Assessment', 'Referred to cardiology for further evaluation', 'DOCTOR', 'Dr. Ahmad Hassan', 'DOC001', false, 45, 'MALE', 'EMP12345');

    -- Doctor claims - REJECTED
    INSERT INTO healthcare_provider_claims (id, provider_id, client_id, client_name, description, amount, service_date, status, submitted_at, rejected_at, rejection_reason, diagnosis, treatment_details, provider_role, provider_name, provider_employee_id, is_follow_up, client_age, client_gender, client_employee_id)
    VALUES
        (gen_random_uuid(), v_doctor_id, v_client_id, 'Khaled Abu Omar', 'Cosmetic Procedure', 500.00, CURRENT_DATE - 20, 'REJECTED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '18 days', 'Cosmetic procedures not covered', 'Cosmetic', 'Botox injection requested', 'DOCTOR', 'Dr. Ahmad Hassan', 'DOC001', false, 45, 'MALE', 'EMP12345');

    -- Pharmacist claims - PENDING
    INSERT INTO healthcare_provider_claims (id, provider_id, client_id, client_name, description, amount, service_date, status, submitted_at, provider_role, provider_name, provider_employee_id, provider_pharmacy_code, role_specific_data, client_age, client_gender, client_employee_id)
    VALUES
        (gen_random_uuid(), v_pharmacist_id, v_client_id, 'Khaled Abu Omar', 'Diabetes Medication', 45.00, CURRENT_DATE - 1, 'PENDING', NOW() - INTERVAL '1 day', 'PHARMACIST', 'Sara Pharmacy', 'PH001', 'PHR-001', '{"items":[{"name":"Metformin 500mg","form":"Tablets","dosage":2,"timesPerDay":2,"duration":30,"calculatedQuantity":120}],"isChronic":false}', 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_pharmacist_id, v_client_id, 'Khaled Abu Omar', 'Blood Pressure Medication', 35.00, CURRENT_DATE - 2, 'PENDING', NOW() - INTERVAL '2 days', 'PHARMACIST', 'Sara Pharmacy', 'PH001', 'PHR-001', '{"items":[{"name":"Lisinopril 10mg","form":"Tablets","dosage":1,"timesPerDay":1,"duration":30,"calculatedQuantity":30}],"isChronic":true}', 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_pharmacist_id, v_client_id, 'Khaled Abu Omar', 'Antibiotic Course', 25.00, CURRENT_DATE - 5, 'APPROVED', NOW() - INTERVAL '5 days', 'PHARMACIST', 'Sara Pharmacy', 'PH001', 'PHR-001', '{"items":[{"name":"Amoxicillin 500mg","form":"Capsules","dosage":1,"timesPerDay":3,"duration":7,"calculatedQuantity":21}],"isChronic":false}', 45, 'MALE', 'EMP12345');

    -- Lab Tech claims - PENDING
    INSERT INTO healthcare_provider_claims (id, provider_id, client_id, client_name, description, amount, service_date, status, submitted_at, provider_role, provider_name, provider_employee_id, provider_lab_code, role_specific_data, client_age, client_gender, client_employee_id)
    VALUES
        (gen_random_uuid(), v_labtech_id, v_client_id, 'Khaled Abu Omar', 'Complete Blood Count (CBC)', 50.00, CURRENT_DATE - 1, 'PENDING', NOW() - INTERVAL '1 day', 'LAB_TECH', 'Central Lab', 'LT001', 'LAB-001', '{"testName":"Complete Blood Count (CBC)"}', 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_labtech_id, v_client_id, 'Khaled Abu Omar', 'Lipid Panel', 75.00, CURRENT_DATE - 3, 'PENDING', NOW() - INTERVAL '3 days', 'LAB_TECH', 'Central Lab', 'LT001', 'LAB-001', '{"testName":"Lipid Panel - Cholesterol, Triglycerides, HDL, LDL"}', 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_labtech_id, v_client_id, 'Khaled Abu Omar', 'HbA1c Test', 60.00, CURRENT_DATE - 7, 'APPROVED', NOW() - INTERVAL '7 days', 'LAB_TECH', 'Central Lab', 'LT001', 'LAB-001', '{"testName":"HbA1c - Glycated Hemoglobin"}', 45, 'MALE', 'EMP12345');

    -- Radiologist claims - PENDING
    INSERT INTO healthcare_provider_claims (id, provider_id, client_id, client_name, description, amount, service_date, status, submitted_at, provider_role, provider_name, provider_employee_id, provider_radiology_code, role_specific_data, client_age, client_gender, client_employee_id)
    VALUES
        (gen_random_uuid(), v_radiologist_id, v_client_id, 'Khaled Abu Omar', 'Chest X-Ray', 120.00, CURRENT_DATE - 1, 'PENDING', NOW() - INTERVAL '1 day', 'RADIOLOGIST', 'Imaging Center', 'RAD001', 'RAD-001', '{"testName":"Chest X-Ray PA and Lateral"}', 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_radiologist_id, v_client_id, 'Khaled Abu Omar', 'MRI Brain', 450.00, CURRENT_DATE - 5, 'PENDING', NOW() - INTERVAL '5 days', 'RADIOLOGIST', 'Imaging Center', 'RAD001', 'RAD-001', '{"testName":"MRI Brain with Contrast"}', 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_radiologist_id, v_client_id, 'Khaled Abu Omar', 'CT Abdomen', 350.00, CURRENT_DATE - 10, 'APPROVED', NOW() - INTERVAL '10 days', 'RADIOLOGIST', 'Imaging Center', 'RAD001', 'RAD-001', '{"testName":"CT Abdomen and Pelvis"}', 45, 'MALE', 'EMP12345');

    -- APPROVED_MEDICAL claims (for Coordination Admin review)
    INSERT INTO healthcare_provider_claims (id, provider_id, client_id, client_name, description, amount, service_date, status, submitted_at, medical_reviewed_at, diagnosis, treatment_details, provider_role, provider_name, provider_employee_id, is_follow_up, client_age, client_gender, client_employee_id)
    VALUES
        (gen_random_uuid(), v_doctor_id, v_client_id, 'Khaled Abu Omar', 'Awaiting Coordination - Heart Check', 200.00, CURRENT_DATE - 4, 'APPROVED_MEDICAL', NOW() - INTERVAL '4 days', NOW() - INTERVAL '2 days', 'Cardiac Review', 'Stress test performed, results normal', 'DOCTOR', 'Dr. Ahmad Hassan', 'DOC001', false, 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_doctor_id, v_client_id, 'Khaled Abu Omar', 'Awaiting Coordination - Diabetes Mgmt', 180.00, CURRENT_DATE - 5, 'APPROVED_MEDICAL', NOW() - INTERVAL '5 days', NOW() - INTERVAL '3 days', 'Diabetes Management', 'Insulin dose adjusted', 'DOCTOR', 'Dr. Ahmad Hassan', 'DOC001', false, 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_pharmacist_id, v_client_id, 'Khaled Abu Omar', 'Awaiting Coordination - Insulin', 150.00, CURRENT_DATE - 3, 'APPROVED_MEDICAL', NOW() - INTERVAL '3 days', NOW() - INTERVAL '1 day', 'Insulin Supply', 'Monthly insulin supply', 'PHARMACIST', 'Sara Pharmacy', 'PH001', false, 45, 'MALE', 'EMP12345');

    -- RETURNED_FOR_REVIEW claims (returned from coordinator to medical)
    INSERT INTO healthcare_provider_claims (id, provider_id, client_id, client_name, description, amount, service_date, status, submitted_at, rejection_reason, diagnosis, treatment_details, provider_role, provider_name, provider_employee_id, is_follow_up, client_age, client_gender, client_employee_id)
    VALUES
        (gen_random_uuid(), v_doctor_id, v_client_id, 'Khaled Abu Omar', 'Needs Re-evaluation - Surgery Cost', 1500.00, CURRENT_DATE - 6, 'RETURNED_FOR_REVIEW', NOW() - INTERVAL '6 days', 'Please verify the surgical procedure code and cost breakdown', 'Minor Surgery', 'Arthroscopy knee surgery', 'DOCTOR', 'Dr. Ahmad Hassan', 'DOC001', false, 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_labtech_id, v_client_id, 'Khaled Abu Omar', 'Needs Re-evaluation - Test Panel', 200.00, CURRENT_DATE - 4, 'RETURNED_FOR_REVIEW', NOW() - INTERVAL '4 days', 'Please clarify medical necessity for comprehensive panel', 'Comprehensive Testing', 'Full metabolic panel requested', 'LAB_TECH', 'Central Lab', 'LT001', false, 45, 'MALE', 'EMP12345');

    -- APPROVED_FINAL and REJECTED_FINAL claims (for Final Decisions)
    INSERT INTO healthcare_provider_claims (id, provider_id, client_id, client_name, description, amount, service_date, status, submitted_at, approved_at, diagnosis, treatment_details, provider_role, provider_name, provider_employee_id, is_follow_up, client_age, client_gender, client_employee_id)
    VALUES
        (gen_random_uuid(), v_doctor_id, v_client_id, 'Khaled Abu Omar', 'Final Approved - Surgery', 2000.00, CURRENT_DATE - 30, 'APPROVED_FINAL', NOW() - INTERVAL '30 days', NOW() - INTERVAL '25 days', 'Appendectomy', 'Emergency appendix removal surgery', 'DOCTOR', 'Dr. Ahmad Hassan', 'DOC001', false, 45, 'MALE', 'EMP12345'),
        (gen_random_uuid(), v_doctor_id, v_client_id, 'Khaled Abu Omar', 'Final Approved - Treatment', 800.00, CURRENT_DATE - 25, 'APPROVED_FINAL', NOW() - INTERVAL '25 days', NOW() - INTERVAL '20 days', 'Chronic Treatment', 'Long-term medication therapy', 'DOCTOR', 'Dr. Ahmad Hassan', 'DOC001', false, 45, 'MALE', 'EMP12345');

    INSERT INTO healthcare_provider_claims (id, provider_id, client_id, client_name, description, amount, service_date, status, submitted_at, rejected_at, rejection_reason, diagnosis, treatment_details, provider_role, provider_name, provider_employee_id, is_follow_up, client_age, client_gender, client_employee_id)
    VALUES
        (gen_random_uuid(), v_doctor_id, v_client_id, 'Khaled Abu Omar', 'Final Rejected - Not Covered', 600.00, CURRENT_DATE - 35, 'REJECTED_FINAL', NOW() - INTERVAL '35 days', NOW() - INTERVAL '30 days', 'Service not covered under policy', 'Elective Procedure', 'LASIK eye surgery - elective', 'DOCTOR', 'Dr. Ahmad Hassan', 'DOC001', false, 45, 'MALE', 'EMP12345');

    RAISE NOTICE 'Successfully inserted test healthcare provider claims!';
END $$;
