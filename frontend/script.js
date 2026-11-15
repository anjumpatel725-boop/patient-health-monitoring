// =====================================================
// HEALTHCARE MONITORING SYSTEM - FULL UPDATED SCRIPT
// =====================================================

const API_BASE_URL = "https://patient-health-monitoring.onrender.com/api";
const REFRESH_INTERVAL = 10000;

// =====================================================
// COMMON API FUNCTIONS
// =====================================================


async function apiRequest(url, options = {}) {
    const currentUser = getCurrentUser();

    const headers = {
        ...(options.headers || {})
    };

    if (currentUser?.token) {
        headers.Authorization = `Bearer ${currentUser.token}`;
    }

    const response = await fetch(url, {
        cache: "no-store",
        ...options,
        headers
    });

    const responseText = await response.text();
    let data = null;

    if (responseText.trim()) {
        try {
            data = JSON.parse(responseText);
        } catch {
            data = responseText;
        }
    }

    if (!response.ok) {
        const message =
            typeof data === "object" && data !== null
                ? data.message || data.error || `HTTP ${response.status}`
                : data || `HTTP ${response.status}`;

        throw new Error(message);
    }

    return data;
}


function getErrorMessage(error) {
    return error?.message || "An unexpected error occurred.";
}

function getCurrentUser() {
    const savedUser = sessionStorage.getItem("currentUser");

    if (!savedUser) return null;

    try {
        return JSON.parse(savedUser);
    } catch (error) {
        console.error("Invalid currentUser:", error);
        sessionStorage.removeItem("currentUser");
        return null;
    }
}

function saveCurrentUser(user) {
    sessionStorage.setItem("currentUser", JSON.stringify(user));
}

function clearCurrentUser() {
    sessionStorage.removeItem("currentUser");
}

function getUserId(user) {
    return user?.userId ?? user?.id ?? null;
}

function getUserName(user) {
    return user?.fullName || user?.name || "Unnamed user";
}

function setText(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent = value ?? "";
    }
}

function showFormError(element, message) {
    if (!element) return;

    element.textContent = message || "";

    if (message) {
        element.classList.add("show");
    } else {
        element.classList.remove("show");
    }
}

function formatDateTime(value) {
    if (!value) return "-";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return String(value);
    }

    return date.toLocaleString();
}

function getStatusClass(status) {
    if (status === "Critical") return "critical";
    if (status === "Attention") return "warning";
    if (status === "No Reading") return "warning";

    return "normal";
}

function redirectByRole(role) {
    const normalizedRole = String(role || "").toLowerCase();

    if (normalizedRole === "patient") {
        window.location.replace("patient-dashboard.html");
    } else if (normalizedRole === "doctor") {
        window.location.replace("doctor-dashboard.html");
    } else if (normalizedRole === "admin") {
        window.location.replace("admin-dashboard.html");
    } else {
        window.location.replace("login.html");
    }
}

function logout() {
    clearCurrentUser();
    sessionStorage.removeItem("registeredEmail");
    window.location.replace("login.html");
}

// =====================================================
// REGISTER
// =====================================================

const registerForm = document.getElementById("registerForm");

if (registerForm) {
    registerForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const fullName =
            document.getElementById("fullName")?.value.trim() || "";

        const email =
            document.getElementById("email")?.value.trim().toLowerCase() || "";

        const password =
            document.getElementById("password")?.value || "";

        const confirmPassword =
            document.getElementById("confirmPassword")?.value || "";

        const role =
            document.getElementById("role")?.value || "";

        const errorElement = document.getElementById("registerError");

        showFormError(errorElement, "");

        if (!fullName || !email || !password || !confirmPassword || !role) {
            showFormError(errorElement, "Please fill all fields.");
            return;
        }

        if (password.length < 6) {
            showFormError(
                errorElement,
                "Password must contain at least 6 characters."
            );
            return;
        }

        if (password !== confirmPassword) {
            showFormError(errorElement, "Passwords do not match.");
            return;
        }

        try {
            await apiRequest(`${API_BASE_URL}/auth/register`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    fullName,
                    email,
                    password,
                    role
                })
            });

            sessionStorage.setItem("registeredEmail", email);

            alert("Registration successful! Please login.");
            window.location.href = "login.html";

        } catch (error) {
            console.error("Registration error:", error);

            showFormError(
                errorElement,
                getErrorMessage(error)
            );
        }
    });
}

// =====================================================
// AUTO-FILL REGISTERED EMAIL
// =====================================================

function fillRegisteredEmail() {
    const loginEmail = document.getElementById("loginEmail");

    if (!loginEmail) return;

    const registeredEmail = sessionStorage.getItem("registeredEmail");

    if (registeredEmail) {
        loginEmail.value = registeredEmail;
        sessionStorage.removeItem("registeredEmail");
    }
}

// =====================================================
// LOGIN
// =====================================================

const loginForm = document.getElementById("loginForm");

if (loginForm) {
    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email =
            document.getElementById("loginEmail")?.value.trim().toLowerCase() || "";

        const password =
            document.getElementById("loginPassword")?.value || "";

        const role =
            document.getElementById("loginRole")?.value || "";

        const errorElement = document.getElementById("loginError");

        showFormError(errorElement, "");

        if (!email || !password || !role) {
            showFormError(errorElement, "Please fill all login fields.");
            return;
        }

        try {
            const data = await apiRequest(`${API_BASE_URL}/auth/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email,
                    password,
                    role
                })
            });

          const currentUser = {
    userId: data.userId ?? data.id,
    fullName: data.name ?? data.fullName,
    email: data.email,
    role: data.role,
    token: data.token
};
if (!currentUser.token) {
    throw new Error("JWT token missing from login response.");
}

            if (currentUser.userId == null || !currentUser.role) {
                throw new Error(
                    "Login response is missing user ID or role. Check AuthController."
                );
            }

            if (
                String(currentUser.role).toLowerCase() !==
                String(role).toLowerCase()
            ) {
                throw new Error("Selected role does not match the account role.");
            }

            saveCurrentUser(currentUser);
            redirectByRole(currentUser.role);

        } catch (error) {
            console.error("Login error:", error);

            showFormError(
                errorElement,
                getErrorMessage(error)
            );
        }
    });
}

// =====================================================
// DASHBOARD LOGIN CHECK
// =====================================================

function checkLogin(requiredRole) {
    const user = getCurrentUser();

    if (!user) {
        window.location.replace("login.html");
        return null;
    }

    if (!user.role) {
        clearCurrentUser();
        window.location.replace("login.html");
        return null;
    }

    if (
        requiredRole &&
        String(user.role).toLowerCase() !== requiredRole.toLowerCase()
    ) {
        redirectByRole(user.role);
        return null;
    }

    return user;
}

// =====================================================
// HEALTH STATUS
// Temperature is in Celsius.
// These are demo-project thresholds, not a diagnosis.
// =====================================================

function calculateHealthStatus(reading) {
    if (!reading) return "No Reading";

    const heartRate = Number(reading.heartRate);
    const oxygen = Number(reading.oxygenLevel);
    const systolic = Number(reading.systolic);
    const diastolic = Number(reading.diastolic);
    const temperature = Number(reading.temperature);

    if (
        heartRate > 120 ||
        heartRate < 50 ||
        oxygen < 90 ||
        systolic >= 180 ||
        diastolic >= 120 ||
        temperature >= 40 ||
        temperature < 35
    ) {
        return "Critical";
    }

    if (
        heartRate > 100 ||
        heartRate < 60 ||
        oxygen < 95 ||
        systolic >= 140 ||
        diastolic >= 90 ||
        temperature >= 38 ||
        temperature < 36
    ) {
        return "Attention";
    }

    return "Stable";
}
// =====================================================
// PATIENT DASHBOARD - LOAD ASSIGNED DOCTOR
// API: GET /api/assignments/patient/me
// =====================================================

async function loadPatientAssignedDoctor() {

    const content = document.getElementById(
        "assignedDoctorContent"
    );

    if (!content) return;

    try {

        const doctor = await apiRequest(
            `${API_BASE_URL}/assignments/patient/me?t=${Date.now()}`
        );

        content.innerHTML = "";

        if (!doctor.assigned) {

            const message = document.createElement("p");

            message.className = "empty-state";
            message.textContent =
                doctor.message || "No doctor assigned yet.";

            content.appendChild(message);

            return;
        }

        const card = document.createElement("div");
        card.className = "assigned-doctor-card";

        const icon = document.createElement("div");
        icon.className = "assigned-doctor-icon";
        icon.textContent = "ðŸ‘¨â€âš•ï¸";

        const details = document.createElement("div");
        details.className = "assigned-doctor-details";

        const name = document.createElement("h3");
        name.textContent = doctor.doctorName || "Doctor";

        const email = document.createElement("p");
        email.textContent = doctor.email || "-";

        const status = document.createElement("span");
        status.className = "role-badge";
        status.textContent = "Assigned Doctor";

        details.append(name, email, status);
        card.append(icon, details);
        content.appendChild(card);

    } catch (error) {

        console.error(
            "Assigned doctor API error:",
            error
        );

        content.textContent =
            `Unable to load assigned doctor: ${getErrorMessage(error)}`;
    }
}
// =====================================================
// PATIENT DASHBOARD
// =====================================================

async function loadPatientHealth() {
    const user = getCurrentUser();
    const userId = getUserId(user);

    if (userId == null) {
        console.error("Patient ID is missing.");
        return;
    }

    try {
        const data = await apiRequest(
            `${API_BASE_URL}/health/${encodeURIComponent(userId)}?t=${Date.now()}`
        );

        if (!Array.isArray(data)) {
            throw new Error("Health API did not return an array.");
        }

        if (data.length === 0) {
            setText("heartRate", "-- BPM");
            setText("bloodPressure", "--/--");
            setText("temperature", "-- Â°C");
            setText("oxygenLevel", "--%");

            const alertElement = document.getElementById("healthAlert");

            if (alertElement) {
                alertElement.textContent = "No health readings available yet.";
                alertElement.className = "alert-normal";
            }

            const table = document.getElementById("healthTable");

            if (table) {
                table.innerHTML = `
                    <tr>
                        <td colspan="6" class="empty-state">
                            No health readings available yet.
                        </td>
                    </tr>
                `;
            }

            return;
        }

        // Backend returns readings newest first.
        const latest = data[0];

        setText("heartRate", `${latest.heartRate ?? "--"} BPM`);
        setText(
            "bloodPressure",
            `${latest.systolic ?? "--"}/${latest.diastolic ?? "--"}`
        );
        setText("temperature", `${latest.temperature ?? "--"} Â°C`);
        setText("oxygenLevel", `${latest.oxygenLevel ?? "--"}%`);

        const status = calculateHealthStatus(latest);
        const alertElement = document.getElementById("healthAlert");

        if (alertElement) {
            if (status === "Critical") {
                alertElement.textContent =
                    "ðŸš¨ Critical reading detected. Seek appropriate medical help.";
                alertElement.className = "alert-critical";
            } else if (status === "Attention") {
                alertElement.textContent =
                    "âš ï¸ Your readings need attention. Please monitor them.";
                alertElement.className = "alert-warning";
            } else {
                alertElement.textContent =
                    "âœ“ Latest reading is within the configured demo range.";
                alertElement.className = "alert-normal";
            }
        }

        const table = document.getElementById("healthTable");

        if (table) {
            table.innerHTML = "";

            data.slice(0, 10).forEach(reading => {
                const row = document.createElement("tr");

                const values = [
                    formatDateTime(reading.recordedAt),
                    `${reading.heartRate ?? "--"} BPM`,
                    `${reading.systolic ?? "--"}/${reading.diastolic ?? "--"}`,
                    `${reading.temperature ?? "--"} Â°C`,
                    `${reading.oxygenLevel ?? "--"}%`
                ];

                values.forEach(value => {
                    const cell = document.createElement("td");
                    cell.textContent = value;
                    row.appendChild(cell);
                });

                const statusCell = document.createElement("td");
                const badge = document.createElement("span");

                badge.className = getStatusClass(
                    calculateHealthStatus(reading)
                );
                badge.textContent = calculateHealthStatus(reading);

                statusCell.appendChild(badge);
                row.appendChild(statusCell);
                table.appendChild(row);
            });
        }

    } catch (error) {
        console.error("Patient health API error:", error);

        const alertElement = document.getElementById("healthAlert");

        if (alertElement) {
            alertElement.textContent =
                `Unable to load health readings: ${getErrorMessage(error)}`;
            alertElement.className = "alert-warning";
        }
    }
}

// =====================================================
// DOCTOR DASHBOARD - PATIENT FILTERING
// =====================================================

let allDoctorPatients = [];
let selectedPatientFilter = "All";

function filterPatients(status) {
    const allowedStatuses = [
        "All",
        "Stable",
        "Attention",
        "Critical"
    ];

    selectedPatientFilter = allowedStatuses.includes(status)
        ? status
        : "All";

    const headings = {
        All: "All Assigned Patients",
        Stable: "Stable Patients",
        Attention: "Patients Needing Attention",
        Critical: "Critical Patients"
    };

    const descriptions = {
        All: "Showing all patients assigned to you.",
        Stable: "Showing patients with stable health status.",
        Attention: "Showing patients needing attention.",
        Critical: "Showing patients with critical health status."
    };

    setText("patientListHeading", headings[selectedPatientFilter]);
    setText("patientListDescription", descriptions[selectedPatientFilter]);

    renderFilteredPatients();

    document.getElementById("patientListSection")?.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

function renderFilteredPatients() {
    const table = document.getElementById("patientTable");

    if (!table) return;

    const filteredPatients = allDoctorPatients.filter(patient => {
        if (selectedPatientFilter === "All") return true;

        if (!patient.latestReading) return false;

        return (
            calculateHealthStatus(patient.latestReading) ===
            selectedPatientFilter
        );
    });

    table.innerHTML = "";

    if (filteredPatients.length === 0) {
        const message = allDoctorPatients.length === 0
            ? "No patients assigned to you yet."
            : "No patients found in this category.";

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 7;
        cell.className = "empty-state";
        cell.textContent = message;

        row.appendChild(cell);
        table.appendChild(row);
        return;
    }

    filteredPatients.forEach(patient => {
        const reading = patient.latestReading;

        const status = reading
            ? calculateHealthStatus(reading)
            : "No Reading";

        const row = document.createElement("tr");

        const values = [
            patient.patientName || patient.fullName || patient.name || "-",
            patient.email || "-",
            reading ? `${reading.heartRate ?? "--"} BPM` : "--",
            reading
                ? `${reading.systolic ?? "--"}/${reading.diastolic ?? "--"}`
                : "--/--",
            reading ? `${reading.oxygenLevel ?? "--"}%` : "--",
            reading ? `${reading.temperature ?? "--"} Â°C` : "--"
        ];

        values.forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });

        const statusCell = document.createElement("td");
        const statusBadge = document.createElement("span");

        statusBadge.className = getStatusClass(status);
        statusBadge.textContent = status;

        statusCell.appendChild(statusBadge);
        row.appendChild(statusCell);
        table.appendChild(row);
    });
}

// =====================================================
// DOCTOR DASHBOARD - LOAD ASSIGNED PATIENTS
// API: GET /api/assignments/doctor/{doctorId}
// =====================================================

async function loadDoctorPatients() {
    const user = getCurrentUser();
    const doctorId = getUserId(user);

    if (doctorId == null) {
        console.error("Doctor ID is missing. Please login again.");
        return;
    }

    try {
        const assignedPatients = await apiRequest(
            `${API_BASE_URL}/assignments/doctor/${encodeURIComponent(doctorId)}?t=${Date.now()}`
        );

        if (!Array.isArray(assignedPatients)) {
            throw new Error("Assignment API did not return an array.");
        }

        const patients = await Promise.all(
            assignedPatients.map(async assignment => {
                const patientId =
                    assignment.patientId ??
                    assignment.patient?.id ??
                    assignment.patient?.userId;

                const patient = {
                    ...assignment,
                    patientId,
                    patientName:
                        assignment.patientName ||
                        assignment.patient?.fullName ||
                        assignment.patient?.name ||
                        "Unnamed patient",
                    email: assignment.email || assignment.patient?.email || "-",
                    latestReading: null
                };

                if (patientId == null) {
                    console.error("Assignment is missing patientId:", assignment);
                    return patient;
                }

                // A single health API failure should not hide the patient.
                try {
                    const readings = await apiRequest(
                        `${API_BASE_URL}/health/${encodeURIComponent(patientId)}?t=${Date.now()}`
                    );

                    if (!Array.isArray(readings)) {
                        throw new Error("Health API did not return an array.");
                    }

                    if (readings.length > 0) {
                        patient.latestReading = readings[0];
                    }

                } catch (error) {
                    console.error(
                        `Health API failed for patient ${patientId}:`,
                        getErrorMessage(error)
                    );
                }

                return patient;
            })
        );

        allDoctorPatients = patients;

        let stable = 0;
        let attention = 0;
        let critical = 0;

        patients.forEach(patient => {
            if (!patient.latestReading) return;

            const status = calculateHealthStatus(patient.latestReading);

            if (status === "Stable") stable++;
            if (status === "Attention") attention++;
            if (status === "Critical") critical++;
        });

        setText("totalPatients", patients.length);
        setText("stablePatients", stable);
        setText("attentionPatients", attention);
        setText("criticalPatients", critical);

        renderFilteredPatients();

        const criticalNames = patients
            .filter(patient =>
                patient.latestReading &&
                calculateHealthStatus(patient.latestReading) === "Critical"
            )
            .map(patient => patient.patientName);

        const attentionNames = patients
            .filter(patient =>
                patient.latestReading &&
                calculateHealthStatus(patient.latestReading) === "Attention"
            )
            .map(patient => patient.patientName);

        const alertElement = document.getElementById("criticalAlert");

        if (alertElement) {
            if (criticalNames.length > 0) {
                alertElement.textContent =
                    `ðŸš¨ Critical Alert: ${criticalNames.join(", ")}`;
                alertElement.className = "alert-critical";

            } else if (attentionNames.length > 0) {
                alertElement.textContent =
                    `âš ï¸ Needs Attention: ${attentionNames.join(", ")}`;
                alertElement.className = "alert-warning";

            } else if (patients.some(patient => !patient.latestReading)) {
                alertElement.textContent =
                    "Some patients do not have health readings yet.";
                alertElement.className = "alert-warning";

            } else {
                alertElement.textContent =
                    "âœ“ No critical or attention alerts in the latest readings.";
                alertElement.className = "alert-normal";
            }
        }

        console.log("Doctor's assigned patients:", patients);

    } catch (error) {
        console.error("Doctor dashboard error:", error);

        const table = document.getElementById("patientTable");

        if (table) {
            table.innerHTML = "";

            const row = document.createElement("tr");
            const cell = document.createElement("td");

            cell.colSpan = 7;
            cell.className = "empty-state";
            cell.textContent =
                `Could not load assigned patients: ${getErrorMessage(error)}`;

            row.appendChild(cell);
            table.appendChild(row);
        }

        const alertElement = document.getElementById("criticalAlert");

        if (alertElement) {
            alertElement.textContent =
                "Unable to load patient alerts. Check the backend API.";
            alertElement.className = "alert-warning";
        }
    }
}

// =====================================================
// ADMIN DASHBOARD - CREATE DOCTOR
// API: POST /api/users/doctors
// =====================================================

const addDoctorForm = document.getElementById("addDoctorForm");

if (addDoctorForm) {

    addDoctorForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        const fullName = document
            .getElementById("doctorFullName")
            .value.trim();

        const email = document
            .getElementById("doctorEmail")
            .value.trim()
            .toLowerCase();

        const password = document
            .getElementById("doctorPassword")
            .value;

        const messageElement =
            document.getElementById("doctorFormMessage");

        const submitButton =
            document.getElementById("createDoctorButton");

        messageElement.textContent = "";
        messageElement.className = "doctor-form-message";

        if (fullName.length < 2 || fullName.length > 80) {
            messageElement.textContent =
                "Name must be between 2 and 80 characters.";
            messageElement.classList.add("error");
            return;
        }

        if (password.length < 8 || password.length > 72) {
            messageElement.textContent =
                "Password must be between 8 and 72 characters.";
            messageElement.classList.add("error");
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = "Creating Doctor...";

        try {

            const doctor = await apiRequest(
                `${API_BASE_URL}/users/doctors`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        fullName,
                        email,
                        password
                    })
                }
            );

            messageElement.textContent =
                `Doctor account created successfully for ${doctor.fullName}.`;

            messageElement.classList.add("success");

            addDoctorForm.reset();

            // Refresh doctor count and registered users table.
            await loadAdminUsers();

        } catch (error) {

            messageElement.textContent =
                getErrorMessage(error);

            messageElement.classList.add("error");

        } finally {

            submitButton.disabled = false;
            submitButton.textContent = "Create Doctor Account";
        }
    });
}

// =====================================================
// ADMIN DASHBOARD - USER FILTERING
// =====================================================

let allAdminUsers = [];
let selectedAdminFilter = "all";

function filterAdminUsers(role) {
    const allowedRoles = ["all", "patient", "doctor"];

    selectedAdminFilter = allowedRoles.includes(
        String(role).toLowerCase()
    )
        ? String(role).toLowerCase()
        : "all";

    const heading = document.getElementById("userTableHeading");

    if (heading) {
        if (selectedAdminFilter === "patient") {
            heading.textContent = "Registered Patients";
        } else if (selectedAdminFilter === "doctor") {
            heading.textContent = "Registered Doctors";
        } else {
            heading.textContent = "Registered Users";
        }
    }

    renderAdminUsers();

    document.getElementById("registeredUsersSection")?.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

function renderAdminUsers() {
    const table = document.getElementById("userTable");

    if (!table) return;

    const filteredUsers = allAdminUsers.filter(user => {
        const role = String(user.role || "").toLowerCase();

        if (selectedAdminFilter === "all") return true;

        return role === selectedAdminFilter;
    });

    table.innerHTML = "";

    if (filteredUsers.length === 0) {
        const message =
            selectedAdminFilter === "patient"
                ? "No patients found."
                : selectedAdminFilter === "doctor"
                    ? "No doctors found."
                    : "No registered users found.";

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 4;
        cell.className = "empty-state";
        cell.textContent = message;

        row.appendChild(cell);
        table.appendChild(row);
        return;
    }

    filteredUsers.forEach(user => {
        const row = document.createElement("tr");

        const idCell = document.createElement("td");
        idCell.textContent = user.id ?? user.userId ?? "-";

        const nameCell = document.createElement("td");
        nameCell.textContent = getUserName(user);

        const emailCell = document.createElement("td");
        emailCell.textContent = user.email || "-";

        const roleCell = document.createElement("td");
        const roleBadge = document.createElement("span");

        roleBadge.className = "role-badge";
        roleBadge.textContent = user.role || "-";

        roleCell.appendChild(roleBadge);
        row.append(idCell, nameCell, emailCell, roleCell);
        table.appendChild(row);
    });
}

// =====================================================
// ADMIN DASHBOARD - LOAD USERS
// API: GET /api/users
// =====================================================

async function loadAdminUsers() {
    try {
        const data = await apiRequest(
            `${API_BASE_URL}/users?t=${Date.now()}`
        );

        if (!Array.isArray(data)) {
            throw new Error("Users API did not return an array.");
        }

        allAdminUsers = data;

        const patients = data.filter(
            user => String(user.role || "").toLowerCase() === "patient"
        );

        const doctors = data.filter(
            user => String(user.role || "").toLowerCase() === "doctor"
        );

        setText("totalUsers", data.length);
        setText("totalPatients", patients.length);
        setText("totalDoctors", doctors.length);

        renderAdminUsers();

    } catch (error) {
        console.error("Admin dashboard error:", error);

        const table = document.getElementById("userTable");

        if (table) {
            table.innerHTML = "";

            const row = document.createElement("tr");
            const cell = document.createElement("td");

            cell.colSpan = 4;
            cell.className = "empty-state";
            cell.textContent =
                `Could not load users: ${getErrorMessage(error)}`;

            row.appendChild(cell);
            table.appendChild(row);
        }
    }
}

// =====================================================
// ADMIN DASHBOARD - VITAL ALERTS
// API: GET /api/health/all
// =====================================================

async function loadAdminVitalAlerts() {
    const alertContainer = document.getElementById("adminVitalAlerts");

    if (!alertContainer) return;

    try {
        const readings = await apiRequest(
            `${API_BASE_URL}/health/all?t=${Date.now()}`
        );

        if (!Array.isArray(readings)) {
            throw new Error("The health API did not return an array.");
        }

        // Keep only the latest reading for each patient.
        const latestByPatient = new Map();

        readings.forEach(reading => {
            const patientId =
                reading.patient?.id ??
                reading.patient?.userId ??
                reading.patientId;

            if (patientId == null) return;

            const key = String(patientId);
            const existing = latestByPatient.get(key);

            if (!existing) {
                latestByPatient.set(key, reading);
                return;
            }

            const currentTime = new Date(
                reading.recordedAt || 0
            ).getTime();

            const existingTime = new Date(
                existing.recordedAt || 0
            ).getTime();

            if (currentTime > existingTime) {
                latestByPatient.set(key, reading);
            }
        });

        const alerts = [];

        latestByPatient.forEach(reading => {
            const status = calculateHealthStatus(reading);

            if (status === "Stable" || status === "No Reading") {
                return;
            }

            const patientName =
                reading.patient?.fullName ||
                reading.patient?.name ||
                `Patient ${reading.patient?.id ?? reading.patientId ?? ""}`;

            alerts.push({
                patientName,
                status,
                reading
            });
        });

        alertContainer.innerHTML = "";

        if (alerts.length === 0) {
            alertContainer.textContent =
                "âœ“ No abnormal readings detected in the latest available data.";
            alertContainer.className = "alert-normal";
            return;
        }

        // Show critical alerts first.
        alerts.sort((a, b) => {
            if (a.status === "Critical" && b.status !== "Critical") return -1;
            if (b.status === "Critical" && a.status !== "Critical") return 1;
            return 0;
        });

        alertContainer.className = "";

        alerts.forEach(alert => {
            const item = document.createElement("div");

            item.className = alert.status === "Critical"
                ? "alert-critical"
                : "alert-warning";

            item.textContent =
                `${alert.status === "Critical" ? "ðŸš¨" : "âš ï¸"} ` +
                `${alert.patientName}: ${alert.status} â€” ` +
                `HR ${alert.reading.heartRate ?? "--"} BPM, ` +
                `Oâ‚‚ ${alert.reading.oxygenLevel ?? "--"}%, ` +
                `BP ${alert.reading.systolic ?? "--"}/` +
                `${alert.reading.diastolic ?? "--"}, ` +
                `Temp ${alert.reading.temperature ?? "--"} Â°C`;

            alertContainer.appendChild(item);
        });

    } catch (error) {
        console.error("Admin vital alerts error:", error);

        alertContainer.textContent =
            `Unable to load vital alerts: ${getErrorMessage(error)}`;
        alertContainer.className = "alert-warning";
    }
}

// =====================================================
// DASHBOARD SECURITY
// =====================================================

function setupDashboardSecurity() {
    const page = document.body.dataset.page;

    const isDashboard = [
        "patient",
        "doctor",
        "admin"
    ].includes(page);

    if (!isDashboard) return;

    window.addEventListener("pageshow", function (event) {
        if (event.persisted && !getCurrentUser()) {
            window.location.replace("login.html");
        }
    });

    window.addEventListener("popstate", function () {
        if (!getCurrentUser()) {
            window.location.replace("login.html");
        }
    });
}

// =====================================================
// PAGE INITIALIZATION
// =====================================================

document.addEventListener("DOMContentLoaded", function () {
    if (document.getElementById("loginForm")) {
        fillRegisteredEmail();
    }

    setupDashboardSecurity();

    const page = document.body.dataset.page;

    // PATIENT DASHBOARD
if (page === "patient") {
    const user = checkLogin("patient");

    if (!user) return;

    setText("userName", user.fullName || "Patient");

    loadPatientHealth();
    loadPatientAssignedDoctor();

    setInterval(loadPatientHealth, REFRESH_INTERVAL);
    setInterval(loadPatientAssignedDoctor, REFRESH_INTERVAL);
}

    // DOCTOR DASHBOARD
    if (page === "doctor") {
        const user = checkLogin("doctor");

        if (!user) return;

        setText("userName", user.fullName || "Doctor");

        loadDoctorPatients();
        setInterval(loadDoctorPatients, REFRESH_INTERVAL);
    }

    // ADMIN DASHBOARD
    if (page === "admin") {
        const user = checkLogin("admin");

        if (!user) return;

        setText("userName", user.fullName || "Admin");

        loadAdminUsers();
        loadAdminVitalAlerts();

        setInterval(loadAdminUsers, REFRESH_INTERVAL);
        setInterval(loadAdminVitalAlerts, REFRESH_INTERVAL);
    }
});
