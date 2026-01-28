<h1 align="center">🏥 Birzeit University Insurance System – Web</h1>

<p align="center">
  <strong>Health Insurance Management Web Platform</strong>
</p>

<hr>

<img width="1918" height="1028" alt="Web System Screenshot"
     src="https://github.com/user-attachments/assets/REPLACE_ME" />

<h2>📌 Project Overview</h2>
<p>
  <strong>Birzeit University Insurance System – Web</strong> is a
  <strong>web-based Management Information System (MIS)</strong> designed to
  digitize and streamline health insurance services for the Birzeit University community.
</p>

<p>
  The system provides a centralized platform that enables users to manage insurance
  policies, submit and track claims, access medical records, and handle emergency
  requests through a secure and role-based web interface.
</p>

<p>
  This project was developed as a <strong>Graduation Project</strong> for the
  <strong>Department of Computer Science</strong> at Birzeit University and follows
  academic and professional software engineering standards.
</p>

<hr>

<h2>🎯 Project Objectives</h2>
<ul>
  <li>Provide a unified web portal for university health insurance services</li>
  <li>Automate insurance claims and approval workflows</li>
  <li>Improve transparency between customers, medical staff, and insurance managers</li>
  <li>Ensure secure access to sensitive medical and insurance data</li>
  <li>Reduce administrative workload through digital processes</li>
</ul>

<hr>

<h2>👥 System Stakeholders</h2>
<p>
  The system supports multiple stakeholders, each with clearly defined roles and responsibilities.
</p>

<h3>🗂️ Coordination Admin</h3>
<ul>
  <li>Oversees overall system coordination</li>
  <li>Monitors interactions between medical and insurance units</li>
  <li>Ensures smooth workflow execution across the platform</li>
</ul>

<h3>🏥 Medical Admin</h3>
<ul>
  <li>Manages medical operations within the system</li>
  <li>Validates medical records and reports</li>
  <li>Supervises doctors, pharmacies, and radiology units</li>
</ul>

<h3>💼 Insurance Manager</h3>
<ul>
  <li>Creates and manages insurance policies</li>
  <li>Reviews and approves insurance claims</li>
  <li>Handles emergency approvals</li>
  <li>Generates financial and operational reports</li>
</ul>

<h3>👤 Customer</h3>
<ul>
  <li>View insurance coverage and policy details</li>
  <li>Submit and track insurance claims</li>
  <li>Access medical history and prescriptions</li>
  <li>Request emergency approvals</li>
</ul>

<h3>👨‍⚕️ Doctor</h3>
<ul>
  <li>Access patient insurance and medical records</li>
  <li>Create medical reports and diagnoses</li>
  <li>Submit prescriptions and test requests</li>
</ul>

<h3>🩺 General Practitioner</h3>
<ul>
  <li>Handle general medical cases</li>
  <li>Initiate referrals to specialized doctors</li>
  <li>Update patient medical records</li>
</ul>

<h3>👨‍⚕️ Doctors (Specialists)</h3>
<ul>
  <li>Each doctor operates within a specific medical specialty</li>
  <li>Provide specialized diagnoses and treatments</li>
  <li>Submit detailed medical reports</li>
</ul>

<h3>💊 Pharmacy</h3>
<ul>
  <li>View and validate prescriptions</li>
  <li>Verify insurance coverage before dispensing medication</li>
  <li>Update prescription status</li>
</ul>

<h3>🧪 Radiology</h3>
<ul>
  <li>Manage imaging requests and results</li>
  <li>Upload radiology reports and files</li>
  <li>Link results to patient medical records</li>
</ul>

<hr>

<h2>🔄 Core System Workflows</h2>

<h3>🧾 Insurance Claims Workflow</h3>
<pre>
draft → submitted → reviewed → approved / rejected → closed
</pre>

<h3>🚨 Emergency Requests Workflow</h3>
<pre>
created → evaluated → escalated → approved → closed
</pre>

<p>
  All workflows are strictly enforced by the backend to ensure data integrity,
  traceability, and auditability.
</p>

<hr>

<h2>🏗️ System Architecture</h2>
<p>
  The web system follows a <strong>decoupled architecture</strong>, where the frontend
  communicates with the backend exclusively through RESTful APIs.
</p>

<ul>
  <li><strong>Frontend:</strong> React (Vite) – UI, routing, dashboards</li>
  <li><strong>Backend:</strong> Spring Boot – business logic and security</li>
  <li><strong>Database:</strong> PostgreSQL – relational data storage</li>
</ul>

<hr>

<h2>🧰 Technologies Used</h2>

<h3>Frontend</h3>
<ul>
  <li>React</li>
  <li>Vite</li>
  <li>Axios</li>
  <li>React Router</li>
  <li>Material UI</li>
</ul>

<hr>

<h2>🗂️ Project Structure</h2>
<pre>
insurance-system-web/
│
├── src/
│   ├── pages/
│   ├── components/
│   ├── services/
│   ├── context/
│   └── App.jsx
│
├── public/
├── index.html
└── README.md
</pre>

<hr>

<h2>🚀 Running the Web Application</h2>
<pre>
npm install
npm run dev
</pre>

<p>
  The application runs at:
  <strong>http://localhost:5173</strong>
</p>

<hr>

<h2>🔮 Future Enhancements</h2>
<ul>
  <li>Advanced dashboard analytics</li>
  <li>Real-time notifications</li>
  <li>Accessibility enhancements</li>
  <li>Integration with additional medical systems</li>
</ul>

<hr>

<h2>📚 Academic Information</h2>
<ul>
  <li><strong>Project Type:</strong> Graduation Project</li>
  <li><strong>Institution:</strong> Birzeit University</li>
  <li><strong>Department:</strong> Computer Science</li>
</ul>

<hr>

<p align="center">
  🏥 <strong>Birzeit University Insurance System – Web</strong><br>
  A secure, scalable, and professional health insurance web platform.
</p>
