/**
 * Placement Drive Management System - Client Application
 */

const API_BASE = '/api';

// State Management
const appState = {
  currentView: 'dashboard',
  selectedDriveIdForEligibility: null,
  activeReportType: 'students',
  drivesCache: [],
  studentsCache: [],
};

// Utilities
function showSpinner(show = true) {
  const spinner = document.getElementById('loading-spinner');
  if (spinner) {
    if (show) spinner.classList.add('active');
    else spinner.classList.remove('active');
  }
}

function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `custom-toast toast-${type}`;
  
  let icon = 'bi-check-circle-fill text-success';
  if (type === 'error') icon = 'bi-exclamation-triangle-fill text-danger';
  if (type === 'warning') icon = 'bi-exclamation-circle-fill text-warning';

  toast.innerHTML = `
    <i class="bi ${icon} fs-5"></i>
    <div class="flex-grow-1">
      <div class="small fw-bold text-dark">${type.toUpperCase()}</div>
      <div class="small text-secondary">${escapeHtml(message)}</div>
    </div>
    <button type="button" class="btn-close btn-close-sm" aria-label="Close"></button>
  `;

  toast.querySelector('.btn-close').addEventListener('click', () => toast.remove());
  container.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 4500);
}

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

async function apiFetch(endpoint, options = {}) {
  try {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {})
      },
      ...options
    });

    const data = await res.json().catch(() => null);

    if (!res.ok) {
      const errMsg = data?.message || (data?.details && data.details.join(', ')) || `Server error (${res.status})`;
      throw new Error(errMsg);
    }

    return data;
  } catch (err) {
    console.error(`API Error on ${endpoint}:`, err);
    throw err;
  }
}

// Navigation Handler
function navigateTo(viewId) {
  appState.currentView = viewId;

  document.querySelectorAll('.nav-link').forEach(link => {
    link.classList.toggle('active', link.getAttribute('data-view') === viewId);
  });

  document.querySelectorAll('.page-view').forEach(view => {
    view.classList.toggle('active', view.id === `view-${viewId}`);
  });

  const titles = {
    dashboard: 'Placement Analytics Dashboard',
    students: 'Student Directory & Management',
    drives: 'Placement Drives & Opportunities',
    eligibility: 'Automatic Eligibility Engine',
    registrations: 'Candidate Registrations',
    'status-tracking': 'Placement Status & Stage Tracking',
    reports: 'Placement Reports & Analytics',
    settings: 'System Health, Backup & Settings'
  };
  const titleElem = document.getElementById('current-page-title');
  if (titleElem) {
    titleElem.textContent = titles[viewId] || 'Placement Management';
  }

  // Close sidebar on mobile upon navigation
  const sidebar = document.getElementById('sidebar');
  const overlay = document.getElementById('sidebar-overlay');
  if (sidebar && sidebar.classList.contains('show')) {
    sidebar.classList.remove('show');
    if (overlay) overlay.classList.remove('show');
  }

  // Load view-specific data
  switch (viewId) {
    case 'dashboard':
      loadDashboard();
      break;
    case 'students':
      loadStudents();
      break;
    case 'drives':
      loadDrives();
      break;
    case 'eligibility':
      loadEligibilityView();
      break;
    case 'registrations':
      loadRegistrations();
      break;
    case 'status-tracking':
      loadStatusTracking();
      break;
    case 'reports':
      loadReportsView();
      break;
    case 'settings':
      loadSettings();
      break;
  }
}

// ==========================================
// 1. DASHBOARD
// ==========================================
async function loadDashboard() {
  try {
    showSpinner(true);
    const res = await apiFetch('/dashboard/stats');
    const s = res.data;

    document.getElementById('stat-total-students').textContent = s.totalStudents;
    document.getElementById('stat-total-drives').textContent = s.totalDrives;
    document.getElementById('stat-active-drives').textContent = s.activeDrives;
    document.getElementById('stat-total-registrations').textContent = s.totalRegistrations;
    document.getElementById('stat-shortlisted').textContent = s.shortlistedCount;
    document.getElementById('stat-interview').textContent = s.interviewCount;
    document.getElementById('stat-selected').textContent = s.selectedCount;

    // Recent Activity
    const timelineElem = document.getElementById('dashboard-timeline');
    if (!s.recentActivity || s.recentActivity.length === 0) {
      timelineElem.innerHTML = `<div class="text-muted small p-3">No activity recorded yet. Register students or update stages to see timeline.</div>`;
    } else {
      timelineElem.innerHTML = s.recentActivity.map(item => `
        <div class="timeline-item">
          <div class="timeline-dot bg-primary"></div>
          <div class="timeline-content">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <strong class="text-dark small">${escapeHtml(item.studentName)} (${escapeHtml(item.studentRollNumber)})</strong>
              <span class="timeline-time">${formatDate(item.changedAt)}</span>
            </div>
            <div class="small text-secondary mb-1">
              Drive: <span class="fw-semibold text-dark">${escapeHtml(item.companyName)}</span>
              &bull; Status: <span class="badge badge-status badge-${(item.toStatus || 'registered').toLowerCase()}">${escapeHtml(item.toStatus)}</span>
            </div>
            <div class="small text-muted italic">"${escapeHtml(item.remarks || 'Status update')}"</div>
          </div>
        </div>
      `).join('');
    }
  } catch (err) {
    showToast('Failed to load dashboard statistics: ' + err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

// ==========================================
// 2. STUDENTS MANAGEMENT
// ==========================================
async function loadStudents() {
  try {
    showSpinner(true);
    const search = document.getElementById('student-search')?.value || '';
    const branch = document.getElementById('student-branch-filter')?.value || '';
    const minCgpa = document.getElementById('student-min-cgpa')?.value || '';
    const skill = document.getElementById('student-skill-filter')?.value || '';
    const sortBy = document.getElementById('student-sort-by')?.value || 'name';
    const sortOrder = document.getElementById('student-sort-order')?.value || 'asc';

    let query = `?sortBy=${sortBy}&sortOrder=${sortOrder}`;
    if (search) query += `&search=${encodeURIComponent(search)}`;
    if (branch && branch !== 'ALL') query += `&branch=${encodeURIComponent(branch)}`;
    if (minCgpa) query += `&minCgpa=${encodeURIComponent(minCgpa)}`;
    if (skill) query += `&skill=${encodeURIComponent(skill)}`;

    const res = await apiFetch(`/students${query}`);
    appState.studentsCache = res.data;

    const tbody = document.getElementById('students-table-body');
    const countBadge = document.getElementById('students-count-badge');
    if (countBadge) countBadge.textContent = `${res.data.length} Students`;

    if (res.data.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7" class="text-center py-4 text-muted">
            <i class="bi bi-people display-6 d-block mb-2"></i>
            No students found matching current filters.
          </td>
        </tr>`;
      return;
    }

    tbody.innerHTML = res.data.map(s => `
      <tr>
        <td><strong class="text-primary">${escapeHtml(s.rollNumber)}</strong></td>
        <td>
          <div class="fw-semibold text-dark">${escapeHtml(s.fullName)}</div>
          <div class="small text-muted">${escapeHtml(s.email || '-')}</div>
        </td>
        <td><span class="badge bg-light text-dark border">${escapeHtml(s.branch)}</span></td>
        <td>
          <span class="fw-bold ${s.cgpa >= 8.0 ? 'text-success' : s.cgpa >= 6.5 ? 'text-primary' : 'text-danger'}">
            ${s.cgpa != null ? s.cgpa.toFixed(2) : '-'}
          </span>
        </td>
        <td>
          <span class="badge ${s.backlogs > 0 ? 'bg-danger' : 'bg-success'} text-white">
            ${s.backlogs} Backlog${s.backlogs === 1 ? '' : 's'}
          </span>
        </td>
        <td>
          <div style="max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
            ${(s.skills || []).map(sk => `<span class="skill-tag">${escapeHtml(sk)}</span>`).join('')}
          </div>
        </td>
        <td class="text-end">
          <button class="btn btn-sm btn-outline-info me-1" onclick="viewStudentDetails('${s.id}')" title="View Details">
            <i class="bi bi-eye"></i>
          </button>
          <button class="btn btn-sm btn-outline-primary me-1" onclick="openEditStudentModal('${s.id}')" title="Edit Student">
            <i class="bi bi-pencil"></i>
          </button>
          <button class="btn btn-sm btn-outline-danger" onclick="confirmDeleteStudent('${s.id}', '${escapeHtml(s.fullName)}')" title="Delete Student">
            <i class="bi bi-trash"></i>
          </button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    showToast('Failed to load students: ' + err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

function openAddStudentModal() {
  document.getElementById('student-form').reset();
  document.getElementById('student-id-field').value = '';
  document.getElementById('student-modal-title').textContent = 'Add New Student';
  const modal = new bootstrap.Modal(document.getElementById('studentModal'));
  modal.show();
}

function openEditStudentModal(id) {
  const student = appState.studentsCache.find(s => s.id === id);
  if (!student) return;

  document.getElementById('student-id-field').value = student.id;
  document.getElementById('student-roll').value = student.rollNumber || '';
  document.getElementById('student-name').value = student.fullName || '';
  document.getElementById('student-branch').value = student.branch || 'CSE';
  document.getElementById('student-cgpa').value = student.cgpa != null ? student.cgpa : '';
  document.getElementById('student-skills').value = (student.skills || []).join(', ');
  document.getElementById('student-backlogs').value = student.backlogs != null ? student.backlogs : 0;
  document.getElementById('student-email').value = student.email || '';
  document.getElementById('student-phone').value = student.phone || '';

  document.getElementById('student-modal-title').textContent = 'Edit Student Details';
  const modal = new bootstrap.Modal(document.getElementById('studentModal'));
  modal.show();
}

async function saveStudent(e) {
  e.preventDefault();
  const id = document.getElementById('student-id-field').value;
  const rollNumber = document.getElementById('student-roll').value.trim();
  const fullName = document.getElementById('student-name').value.trim();
  const branch = document.getElementById('student-branch').value.trim();
  const cgpa = parseFloat(document.getElementById('student-cgpa').value);
  const rawSkills = document.getElementById('student-skills').value;
  const backlogs = parseInt(document.getElementById('student-backlogs').value || '0', 10);
  const email = document.getElementById('student-email').value.trim() || null;
  const phone = document.getElementById('student-phone').value.trim() || null;

  const skills = rawSkills.split(',')
    .map(s => s.trim())
    .filter(s => s.length > 0);

  if (!rollNumber || !fullName || !branch || isNaN(cgpa)) {
    showToast('Please fill all mandatory fields correctly.', 'warning');
    return;
  }

  if (cgpa < 0 || cgpa > 10) {
    showToast('CGPA must be between 0.0 and 10.0', 'warning');
    return;
  }

  const payload = { rollNumber, fullName, branch, cgpa, skills, backlogs, email, phone };

  try {
    showSpinner(true);
    if (id) {
      await apiFetch(`/students/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      showToast('Student profile updated successfully!');
    } else {
      await apiFetch('/students', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Student created successfully!');
    }
    bootstrap.Modal.getInstance(document.getElementById('studentModal'))?.hide();
    loadStudents();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

function viewStudentDetails(id) {
  const student = appState.studentsCache.find(s => s.id === id);
  if (!student) return;

  const body = document.getElementById('view-student-body');
  body.innerHTML = `
    <div class="row g-3">
      <div class="col-6"><strong>Roll Number:</strong> ${escapeHtml(student.rollNumber)}</div>
      <div class="col-6"><strong>Full Name:</strong> ${escapeHtml(student.fullName)}</div>
      <div class="col-6"><strong>Branch:</strong> <span class="badge bg-light text-dark border">${escapeHtml(student.branch)}</span></div>
      <div class="col-6"><strong>CGPA:</strong> <span class="fw-bold text-primary">${student.cgpa?.toFixed(2) || '-'}</span></div>
      <div class="col-6"><strong>Backlogs:</strong> ${student.backlogs}</div>
      <div class="col-6"><strong>Email:</strong> ${escapeHtml(student.email || 'None')}</div>
      <div class="col-6"><strong>Phone:</strong> ${escapeHtml(student.phone || 'None')}</div>
      <div class="col-6"><strong>Registered Since:</strong> ${formatDate(student.createdAt)}</div>
      <div class="col-12">
        <strong>Skills:</strong>
        <div class="mt-1">
          ${(student.skills || []).map(s => `<span class="skill-tag">${escapeHtml(s)}</span>`).join('')}
        </div>
      </div>
    </div>
  `;
  new bootstrap.Modal(document.getElementById('viewStudentModal')).show();
}

function confirmDeleteStudent(id, name) {
  openConfirmModal(
    'Delete Student Record',
    `Are you sure you want to permanently delete <strong>${escapeHtml(name)}</strong>? Associated placement registrations will also be removed.`,
    async () => {
      try {
        showSpinner(true);
        await apiFetch(`/students/${id}`, { method: 'DELETE' });
        showToast('Student deleted successfully.');
        loadStudents();
      } catch (err) {
        showToast(err.message, 'error');
      } finally {
        showSpinner(false);
      }
    }
  );
}

// ==========================================
// 3. COMPANIES & PLACEMENT DRIVES
// ==========================================
async function loadDrives() {
  try {
    showSpinner(true);
    const search = document.getElementById('drive-search')?.value || '';
    const status = document.getElementById('drive-status-filter')?.value || '';
    const minPackage = document.getElementById('drive-min-pkg')?.value || '';
    const sortBy = document.getElementById('drive-sort-by')?.value || 'company';
    const sortOrder = document.getElementById('drive-sort-order')?.value || 'asc';

    let query = `?sortBy=${sortBy}&sortOrder=${sortOrder}`;
    if (search) query += `&search=${encodeURIComponent(search)}`;
    if (status && status !== 'ALL') query += `&status=${encodeURIComponent(status)}`;
    if (minPackage) query += `&minPackage=${encodeURIComponent(minPackage)}`;

    const res = await apiFetch(`/drives${query}`);
    appState.drivesCache = res.data;

    const tbody = document.getElementById('drives-table-body');
    const countBadge = document.getElementById('drives-count-badge');
    if (countBadge) countBadge.textContent = `${res.data.length} Drives`;

    if (res.data.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7" class="text-center py-4 text-muted">
            <i class="bi bi-building display-6 d-block mb-2"></i>
            No placement drives found matching criteria.
          </td>
        </tr>`;
      return;
    }

    tbody.innerHTML = res.data.map(d => `
      <tr>
        <td>
          <div class="fw-bold text-dark">${escapeHtml(d.companyName)}</div>
          <div class="small text-muted">${escapeHtml(d.jobRole)}</div>
        </td>
        <td><span class="fw-bold text-success fs-6">${d.packageLpa != null ? d.packageLpa.toFixed(2) : '-'} LPA</span></td>
        <td>Min CGPA: <strong>${d.minCgpa?.toFixed(2) || '0.00'}</strong><br><small class="text-muted">Max Backlogs: ${d.maxBacklogs || 0}</small></td>
        <td>
          <div style="max-width: 200px; overflow: hidden; text-overflow: ellipsis;">
            ${(d.eligibleBranches || []).map(b => `<span class="badge bg-light text-dark border me-1">${escapeHtml(b)}</span>`).join('')}
          </div>
        </td>
        <td>${escapeHtml(d.applicationDeadline || '-')}</td>
        <td>
          <span class="badge badge-status badge-${d.status.toLowerCase()}">${d.status}</span>
        </td>
        <td class="text-end">
          <button class="btn btn-sm btn-outline-info me-1" onclick="checkEligibilityForDrive('${d.id}')" title="Check Eligible Students">
            <i class="bi bi-person-check"></i> Eligibility
          </button>
          <button class="btn btn-sm btn-outline-primary me-1" onclick="openEditDriveModal('${d.id}')" title="Edit Drive">
            <i class="bi bi-pencil"></i>
          </button>
          <button class="btn btn-sm btn-outline-danger" onclick="confirmDeleteDrive('${d.id}', '${escapeHtml(d.companyName)}')" title="Delete Drive">
            <i class="bi bi-trash"></i>
          </button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    showToast('Failed to load drives: ' + err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

function openAddDriveModal() {
  document.getElementById('drive-form').reset();
  document.getElementById('drive-id-field').value = '';
  document.getElementById('drive-modal-title').textContent = 'Create Placement Drive';
  new bootstrap.Modal(document.getElementById('driveModal')).show();
}

function openEditDriveModal(id) {
  const drive = appState.drivesCache.find(d => d.id === id);
  if (!drive) return;

  document.getElementById('drive-id-field').value = drive.id;
  document.getElementById('drive-company').value = drive.companyName || '';
  document.getElementById('drive-role').value = drive.jobRole || '';
  document.getElementById('drive-package').value = drive.packageLpa != null ? drive.packageLpa : '';
  document.getElementById('drive-min-cgpa-input').value = drive.minCgpa != null ? drive.minCgpa : '';
  document.getElementById('drive-branches').value = (drive.eligibleBranches || []).join(', ');
  document.getElementById('drive-skills').value = (drive.requiredSkills || []).join(', ');
  document.getElementById('drive-max-backlogs').value = drive.maxBacklogs != null ? drive.maxBacklogs : 0;
  document.getElementById('drive-deadline').value = drive.applicationDeadline || '';
  document.getElementById('drive-status-select').value = drive.status || 'ACTIVE';
  document.getElementById('drive-description').value = drive.jobDescription || '';

  document.getElementById('drive-modal-title').textContent = 'Edit Placement Drive';
  new bootstrap.Modal(document.getElementById('driveModal')).show();
}

async function saveDrive(e) {
  e.preventDefault();
  const id = document.getElementById('drive-id-field').value;
  const companyName = document.getElementById('drive-company').value.trim();
  const jobRole = document.getElementById('drive-role').value.trim();
  const packageLpa = parseFloat(document.getElementById('drive-package').value);
  const minCgpa = parseFloat(document.getElementById('drive-min-cgpa-input').value);
  const rawBranches = document.getElementById('drive-branches').value;
  const rawSkills = document.getElementById('drive-skills').value;
  const maxBacklogs = parseInt(document.getElementById('drive-max-backlogs').value || '0', 10);
  const applicationDeadline = document.getElementById('drive-deadline').value.trim();
  const status = document.getElementById('drive-status-select').value;
  const jobDescription = document.getElementById('drive-description').value.trim();

  const eligibleBranches = rawBranches.split(',').map(b => b.trim().toUpperCase()).filter(b => b.length > 0);
  const requiredSkills = rawSkills.split(',').map(s => s.trim()).filter(s => s.length > 0);

  if (!companyName || !jobRole || isNaN(packageLpa) || isNaN(minCgpa) || eligibleBranches.length === 0 || !applicationDeadline) {
    showToast('Please fill all mandatory fields correctly.', 'warning');
    return;
  }

  const payload = {
    companyName,
    jobRole,
    packageLpa,
    minCgpa,
    eligibleBranches,
    requiredSkills,
    maxBacklogs,
    applicationDeadline,
    status,
    jobDescription
  };

  try {
    showSpinner(true);
    if (id) {
      await apiFetch(`/drives/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      showToast('Placement drive updated successfully!');
    } else {
      await apiFetch('/drives', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Placement drive published successfully!');
    }
    bootstrap.Modal.getInstance(document.getElementById('driveModal'))?.hide();
    loadDrives();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

function confirmDeleteDrive(id, company) {
  openConfirmModal(
    'Delete Placement Drive',
    `Are you sure you want to delete drive for <strong>${escapeHtml(company)}</strong>? All candidate registrations for this drive will be deleted.`,
    async () => {
      try {
        showSpinner(true);
        await apiFetch(`/drives/${id}`, { method: 'DELETE' });
        showToast('Placement drive deleted successfully.');
        loadDrives();
      } catch (err) {
        showToast(err.message, 'error');
      } finally {
        showSpinner(false);
      }
    }
  );
}

function checkEligibilityForDrive(driveId) {
  appState.selectedDriveIdForEligibility = driveId;
  navigateTo('eligibility');
}

// ==========================================
// 4. AUTOMATIC ELIGIBILITY ENGINE
// ==========================================
async function loadEligibilityView() {
  try {
    showSpinner(true);
    const res = await apiFetch('/drives');
    appState.drivesCache = res.data;

    const select = document.getElementById('eligibility-drive-select');
    if (!select) return;

    select.innerHTML = `<option value="">-- Choose a Placement Drive --</option>` +
      res.data.map(d => `<option value="${d.id}" ${d.id === appState.selectedDriveIdForEligibility ? 'selected' : ''}>
        ${escapeHtml(d.companyName)} - ${escapeHtml(d.jobRole)} (${d.packageLpa} LPA) [${d.status}]
      </option>`).join('');

    if (appState.selectedDriveIdForEligibility) {
      evaluateSelectedDrive(appState.selectedDriveIdForEligibility);
    } else if (res.data.length > 0) {
      select.value = res.data[0].id;
      evaluateSelectedDrive(res.data[0].id);
    } else {
      document.getElementById('eligibility-results-card').style.display = 'none';
    }
  } catch (err) {
    showToast('Failed to load placement drives: ' + err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

async function onEligibilityDriveChange() {
  const driveId = document.getElementById('eligibility-drive-select').value;
  if (!driveId) {
    document.getElementById('eligibility-results-card').style.display = 'none';
    return;
  }
  appState.selectedDriveIdForEligibility = driveId;
  evaluateSelectedDrive(driveId);
}

async function evaluateSelectedDrive(driveId) {
  try {
    showSpinner(true);
    const res = await apiFetch(`/drives/${driveId}/eligible-students`);
    const { drive, eligible, ineligible, totalEligible, totalIneligible } = res.data;

    document.getElementById('eligibility-results-card').style.display = 'block';

    // Update Criteria Card
    document.getElementById('elig-drive-title').textContent = `${drive.companyName} - ${drive.jobRole}`;
    document.getElementById('elig-drive-badge').textContent = drive.status;
    document.getElementById('elig-drive-badge').className = `badge badge-status badge-${drive.status.toLowerCase()}`;
    document.getElementById('elig-crit-pkg').textContent = `${drive.packageLpa} LPA`;
    document.getElementById('elig-crit-cgpa').textContent = `>= ${drive.minCgpa.toFixed(2)}`;
    document.getElementById('elig-crit-backlog').textContent = `<= ${drive.maxBacklogs} backlogs`;
    document.getElementById('elig-crit-branches').innerHTML = (drive.eligibleBranches || [])
      .map(b => `<span class="badge bg-light text-dark border me-1">${escapeHtml(b)}</span>`).join('');
    document.getElementById('elig-crit-skills').innerHTML = (drive.requiredSkills || []).length > 0
      ? (drive.requiredSkills || []).map(s => `<span class="skill-tag">${escapeHtml(s)}</span>`).join('')
      : '<span class="text-muted small">None specified</span>';

    // Update Tab Badges
    document.getElementById('eligible-count-tab').textContent = totalEligible;
    document.getElementById('ineligible-count-tab').textContent = totalIneligible;

    // Render Eligible Table
    const elTbody = document.getElementById('eligible-candidates-tbody');
    if (eligible.length === 0) {
      elTbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">No students currently meet all criteria for this drive.</td></tr>`;
    } else {
      elTbody.innerHTML = eligible.map(item => {
        const s = item.student;
        const isRegistered = item.alreadyRegistered;
        const regStatus = item.currentRegistrationStatus;

        return `
          <tr>
            <td><strong class="text-primary">${escapeHtml(s.rollNumber)}</strong></td>
            <td>
              <div class="fw-semibold text-dark">${escapeHtml(s.fullName)}</div>
              <div class="small text-muted">${escapeHtml(s.email || '-')}</div>
            </td>
            <td><span class="badge bg-light text-dark border">${escapeHtml(s.branch)}</span></td>
            <td><strong class="text-success">${s.cgpa?.toFixed(2)}</strong></td>
            <td>
              <div>${(s.skills || []).map(sk => `<span class="skill-tag">${escapeHtml(sk)}</span>`).join('')}</div>
            </td>
            <td class="text-end">
              ${isRegistered
                ? `<span class="badge badge-status badge-${(regStatus || 'registered').toLowerCase()}">
                    <i class="bi bi-check-circle me-1"></i>${escapeHtml(regStatus)}
                   </span>`
                : drive.status === 'ACTIVE'
                  ? `<button class="btn btn-sm btn-primary-custom" onclick="quickRegisterStudent('${s.id}', '${drive.id}', '${escapeHtml(s.fullName)}')">
                      <i class="bi bi-plus-circle me-1"></i> Register Now
                     </button>`
                  : `<span class="badge bg-secondary">Drive Inactive</span>`
              }
            </td>
          </tr>
        `;
      }).join('');
    }

    // Render Ineligible Table
    const inelTbody = document.getElementById('ineligible-candidates-tbody');
    if (ineligible.length === 0) {
      inelTbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-muted">All students meet eligibility criteria!</td></tr>`;
    } else {
      inelTbody.innerHTML = ineligible.map(item => {
        const s = item.student;
        return `
          <tr>
            <td><strong>${escapeHtml(s.rollNumber)}</strong></td>
            <td>
              <div class="fw-semibold text-dark">${escapeHtml(s.fullName)}</div>
              <div class="small text-muted">Branch: ${escapeHtml(s.branch)} | CGPA: ${s.cgpa?.toFixed(2) || 'N/A'}</div>
            </td>
            <td>
              <div class="d-flex flex-column gap-1">
                ${(item.reasons || []).map(r => `<span class="badge bg-danger-subtle text-danger border border-danger-subtle text-start p-1 small"><i class="bi bi-x-circle me-1"></i>${escapeHtml(r)}</span>`).join('')}
              </div>
            </td>
            <td>
              <div>${(s.skills || []).map(sk => `<span class="skill-tag">${escapeHtml(sk)}</span>`).join('')}</div>
            </td>
            <td class="text-end">
              <span class="badge bg-light text-muted border">Ineligible</span>
            </td>
          </tr>
        `;
      }).join('');
    }

  } catch (err) {
    showToast('Eligibility calculation failed: ' + err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

async function quickRegisterStudent(studentId, driveId, studentName) {
  try {
    showSpinner(true);
    await apiFetch('/registrations', {
      method: 'POST',
      body: JSON.stringify({ studentId, driveId })
    });
    showToast(`Successfully registered ${studentName}!`);
    evaluateSelectedDrive(driveId);
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

// ==========================================
// 5. REGISTRATIONS & APPLICATIONS
// ==========================================
async function loadRegistrations() {
  try {
    showSpinner(true);
    // Populate drive filter if empty
    const driveFilter = document.getElementById('reg-drive-filter');
    if (driveFilter && driveFilter.options.length <= 1) {
      const drivesRes = await apiFetch('/drives');
      driveFilter.innerHTML = '<option value="">All Placement Drives</option>' +
        drivesRes.data.map(d => `<option value="${d.id}">${escapeHtml(d.companyName)} - ${escapeHtml(d.jobRole)}</option>`).join('');
    }

    const driveId = document.getElementById('reg-drive-filter')?.value || '';
    const status = document.getElementById('reg-status-filter')?.value || '';
    const search = document.getElementById('reg-search')?.value || '';

    let query = '?';
    if (driveId) query += `&driveId=${encodeURIComponent(driveId)}`;
    if (status && status !== 'ALL') query += `&status=${encodeURIComponent(status)}`;
    if (search) query += `&search=${encodeURIComponent(search)}`;

    const res = await apiFetch(`/registrations${query}`);

    const tbody = document.getElementById('registrations-table-body');
    const badge = document.getElementById('registrations-count-badge');
    if (badge) badge.textContent = `${res.data.length} Applications`;

    if (res.data.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">No student applications found.</td></tr>`;
      return;
    }

    tbody.innerHTML = res.data.map(r => `
      <tr>
        <td><strong class="text-primary">${escapeHtml(r.studentRollNumber)}</strong></td>
        <td><div class="fw-semibold text-dark">${escapeHtml(r.studentName)}</div></td>
        <td>
          <div class="fw-semibold">${escapeHtml(r.companyName)}</div>
          <div class="small text-muted">${escapeHtml(r.jobRole)}</div>
        </td>
        <td><span class="fw-bold text-success">${r.packageLpa != null ? r.packageLpa.toFixed(2) : '-'} LPA</span></td>
        <td><span class="badge badge-status badge-${r.status.toLowerCase()}">${r.status}</span></td>
        <td>${formatDate(r.registeredAt)}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-outline-primary me-1" onclick="openStatusUpdateModal('${r.id}', '${r.status}', '${escapeHtml(r.studentName)}', '${escapeHtml(r.companyName)}')">
            <i class="bi bi-arrow-repeat me-1"></i> Update
          </button>
          <button class="btn btn-sm btn-outline-secondary" onclick="viewRegistrationHistory('${r.id}')" title="Audit Trail">
            <i class="bi bi-clock-history"></i>
          </button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    showToast('Failed to load registrations: ' + err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

async function openNewRegistrationModal() {
  try {
    showSpinner(true);
    const [studentsRes, drivesRes] = await Promise.all([
      apiFetch('/students'),
      apiFetch('/drives?status=ACTIVE')
    ]);

    const studentSelect = document.getElementById('new-reg-student-select');
    studentSelect.innerHTML = '<option value="">-- Choose Student --</option>' +
      studentsRes.data.map(s => `<option value="${s.id}">${escapeHtml(s.rollNumber)} - ${escapeHtml(s.fullName)} (${s.branch}, CGPA ${s.cgpa})</option>`).join('');

    const driveSelect = document.getElementById('new-reg-drive-select');
    driveSelect.innerHTML = '<option value="">-- Choose Active Drive --</option>' +
      drivesRes.data.map(d => `<option value="${d.id}">${escapeHtml(d.companyName)} - ${escapeHtml(d.jobRole)} (${d.packageLpa} LPA)</option>`).join('');

    new bootstrap.Modal(document.getElementById('newRegistrationModal')).show();
  } catch (err) {
    showToast('Failed to initialize registration dialog: ' + err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

async function submitNewRegistration(e) {
  e.preventDefault();
  const studentId = document.getElementById('new-reg-student-select').value;
  const driveId = document.getElementById('new-reg-drive-select').value;

  if (!studentId || !driveId) {
    showToast('Please select both a student and a placement drive.', 'warning');
    return;
  }

  try {
    showSpinner(true);
    await apiFetch('/registrations', {
      method: 'POST',
      body: JSON.stringify({ studentId, driveId })
    });
    showToast('Candidate successfully registered!');
    bootstrap.Modal.getInstance(document.getElementById('newRegistrationModal'))?.hide();
    loadRegistrations();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

// ==========================================
// 6. PLACEMENT STATUS & STAGE TRACKING
// ==========================================
async function loadStatusTracking() {
  try {
    showSpinner(true);
    const res = await apiFetch('/registrations');
    const regs = res.data;

    // Update Status Counters
    const counts = {
      REGISTERED: 0,
      SHORTLISTED: 0,
      INTERVIEW: 0,
      SELECTED: 0,
      REJECTED: 0,
      WITHDRAWN: 0
    };

    regs.forEach(r => {
      if (counts[r.status] !== undefined) {
        counts[r.status]++;
      }
    });

    document.getElementById('stage-registered-count').textContent = counts.REGISTERED;
    document.getElementById('stage-shortlisted-count').textContent = counts.SHORTLISTED;
    document.getElementById('stage-interview-count').textContent = counts.INTERVIEW;
    document.getElementById('stage-selected-count').textContent = counts.SELECTED;
    document.getElementById('stage-rejected-count').textContent = counts.REJECTED;

    const tbody = document.getElementById('status-tracking-tbody');
    if (regs.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">No candidate stages to track.</td></tr>`;
      return;
    }

    tbody.innerHTML = regs.map(r => `
      <tr>
        <td><strong class="text-primary">${escapeHtml(r.studentRollNumber)}</strong></td>
        <td><div class="fw-semibold text-dark">${escapeHtml(r.studentName)}</div></td>
        <td>${escapeHtml(r.companyName)} <small class="text-muted">(${escapeHtml(r.jobRole)})</small></td>
        <td><span class="badge badge-status badge-${r.status.toLowerCase()}">${r.status}</span></td>
        <td><small class="text-muted">${escapeHtml(r.remarks || '-')}</small></td>
        <td>${formatDate(r.updatedAt || r.registeredAt)}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-outline-primary" onclick="openStatusUpdateModal('${r.id}', '${r.status}', '${escapeHtml(r.studentName)}', '${escapeHtml(r.companyName)}')">
            <i class="bi bi-arrow-right-circle me-1"></i> Advance Stage
          </button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    showToast('Failed to load status tracker: ' + err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

// Transition state matrix for UI dropdown
const ALLOWED_TRANSITIONS = {
  REGISTERED: ['SHORTLISTED', 'REJECTED', 'WITHDRAWN'],
  SHORTLISTED: ['INTERVIEW', 'REJECTED', 'WITHDRAWN'],
  INTERVIEW: ['SELECTED', 'REJECTED', 'WITHDRAWN'],
  SELECTED: ['WITHDRAWN'],
  REJECTED: [],
  WITHDRAWN: []
};

function openStatusUpdateModal(regId, currentStatus, studentName, companyName) {
  document.getElementById('status-reg-id').value = regId;
  document.getElementById('status-modal-candidate').textContent = `${studentName} - ${companyName}`;
  document.getElementById('status-current-badge').textContent = currentStatus;
  document.getElementById('status-current-badge').className = `badge badge-status badge-${currentStatus.toLowerCase()}`;
  document.getElementById('status-remarks').value = '';

  const select = document.getElementById('status-next-select');
  const allowed = ALLOWED_TRANSITIONS[currentStatus] || [];

  if (allowed.length === 0) {
    select.innerHTML = `<option value="">No further transitions allowed (${currentStatus} is final)</option>`;
    select.disabled = true;
    document.getElementById('btn-save-status').disabled = true;
  } else {
    select.disabled = false;
    document.getElementById('btn-save-status').disabled = false;
    select.innerHTML = '<option value="">-- Select New Status --</option>' +
      allowed.map(st => `<option value="${st}">${st}</option>`).join('');
  }

  new bootstrap.Modal(document.getElementById('statusUpdateModal')).show();
}

async function submitStatusUpdate(e) {
  e.preventDefault();
  const regId = document.getElementById('status-reg-id').value;
  const status = document.getElementById('status-next-select').value;
  const remarks = document.getElementById('status-remarks').value.trim();

  if (!status) {
    showToast('Please select a valid target status.', 'warning');
    return;
  }

  try {
    showSpinner(true);
    await apiFetch(`/registrations/${regId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status, remarks })
    });
    showToast('Candidate stage updated successfully!');
    bootstrap.Modal.getInstance(document.getElementById('statusUpdateModal'))?.hide();
    if (appState.currentView === 'registrations') {
      loadRegistrations();
    } else {
      loadStatusTracking();
    }
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

async function viewRegistrationHistory(regId) {
  try {
    showSpinner(true);
    const res = await apiFetch(`/registrations/${regId}/history`);
    const history = res.data;

    const listElem = document.getElementById('history-timeline-list');
    if (!history || history.length === 0) {
      listElem.innerHTML = '<div class="text-muted p-3">No history available for this record.</div>';
    } else {
      listElem.innerHTML = history.map(h => `
        <div class="timeline-item">
          <div class="timeline-dot bg-info"></div>
          <div class="timeline-content">
            <div class="d-flex justify-content-between mb-1">
              <span class="badge badge-status badge-${h.toStatus.toLowerCase()}">${h.toStatus}</span>
              <span class="timeline-time">${formatDate(h.changedAt)}</span>
            </div>
            <div class="small text-secondary">${escapeHtml(h.remarks || 'No notes')}</div>
          </div>
        </div>
      `).join('');
    }

    new bootstrap.Modal(document.getElementById('historyModal')).show();
  } catch (err) {
    showToast('Failed to fetch history: ' + err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

// ==========================================
// 7. REPORTS & EXPORTS
// ==========================================
function loadReportsView() {
  switchReportTab('students');
}

async function switchReportTab(type) {
  appState.activeReportType = type;

  document.querySelectorAll('#reports-nav-tabs .nav-link').forEach(tab => {
    tab.classList.toggle('active', tab.getAttribute('data-report-type') === type);
  });

  const previewContainer = document.getElementById('report-preview-container');
  showSpinner(true);

  try {
    switch (type) {
      case 'students':
        await renderStudentDirectoryReport(previewContainer);
        break;
      case 'shortlisted':
        await renderRegistrationsReport(previewContainer, '/reports/shortlisted', 'Shortlisted Candidates');
        break;
      case 'interview':
        await renderRegistrationsReport(previewContainer, '/reports/interview', 'Interview Stage Candidates');
        break;
      case 'selected':
        await renderRegistrationsReport(previewContainer, '/reports/selected', 'Selected / Placed Candidates');
        break;
      case 'company-wise':
        await renderCompanyWiseReport(previewContainer);
        break;
      case 'branch-wise':
        await renderBranchWiseReport(previewContainer);
        break;
    }
  } catch (err) {
    previewContainer.innerHTML = `<div class="alert alert-danger m-3">Error loading report: ${escapeHtml(err.message)}</div>`;
  } finally {
    showSpinner(false);
  }
}

async function renderStudentDirectoryReport(container) {
  const res = await apiFetch('/reports/students');
  const data = res.data;

  container.innerHTML = `
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h6 class="fw-bold mb-0">Student Directory (${data.length} Total Registered Students)</h6>
      <button class="btn btn-sm btn-outline-success" onclick="downloadCsv('students')">
        <i class="bi bi-file-earmark-spreadsheet me-1"></i> Download CSV (Excel)
      </button>
    </div>
    <div class="table-responsive">
      <table class="table custom-table">
        <thead>
          <tr>
            <th>Roll Number</th>
            <th>Name</th>
            <th>Branch</th>
            <th>CGPA</th>
            <th>Backlogs</th>
            <th>Skills</th>
            <th>Email</th>
          </tr>
        </thead>
        <tbody>
          ${data.map(s => `
            <tr>
              <td><strong>${escapeHtml(s.rollNumber)}</strong></td>
              <td>${escapeHtml(s.fullName)}</td>
              <td><span class="badge bg-light text-dark border">${escapeHtml(s.branch)}</span></td>
              <td><strong>${s.cgpa?.toFixed(2) || '-'}</strong></td>
              <td>${s.backlogs}</td>
              <td>${(s.skills || []).map(sk => `<span class="skill-tag">${escapeHtml(sk)}</span>`).join('')}</td>
              <td>${escapeHtml(s.email || '-')}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

async function renderRegistrationsReport(container, endpoint, title) {
  const res = await apiFetch(endpoint);
  const data = res.data;

  container.innerHTML = `
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h6 class="fw-bold mb-0">${title} (${data.length} Candidates)</h6>
      <button class="btn btn-sm btn-outline-success" onclick="downloadCsv('${appState.activeReportType}')">
        <i class="bi bi-file-earmark-spreadsheet me-1"></i> Download CSV (Excel)
      </button>
    </div>
    <div class="table-responsive">
      <table class="table custom-table">
        <thead>
          <tr>
            <th>Roll Number</th>
            <th>Student Name</th>
            <th>Company Name</th>
            <th>Job Role</th>
            <th>Package</th>
            <th>Status</th>
            <th>Updated Date</th>
          </tr>
        </thead>
        <tbody>
          ${data.length === 0 ? `<tr><td colspan="7" class="text-center py-3 text-muted">No records found.</td></tr>` : ''}
          ${data.map(r => `
            <tr>
              <td><strong>${escapeHtml(r.studentRollNumber)}</strong></td>
              <td>${escapeHtml(r.studentName)}</td>
              <td><strong>${escapeHtml(r.companyName)}</strong></td>
              <td>${escapeHtml(r.jobRole)}</td>
              <td><span class="text-success fw-bold">${r.packageLpa?.toFixed(2) || '-'} LPA</span></td>
              <td><span class="badge badge-status badge-${r.status.toLowerCase()}">${r.status}</span></td>
              <td>${formatDate(r.updatedAt || r.registeredAt)}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

async function renderCompanyWiseReport(container) {
  const res = await apiFetch('/reports/company-wise');
  const data = res.data;

  container.innerHTML = `
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h6 class="fw-bold mb-0">Company-Wise Placement Statistics (${data.length} Drives)</h6>
      <button class="btn btn-sm btn-outline-success" onclick="downloadCsv('company-wise')">
        <i class="bi bi-file-earmark-spreadsheet me-1"></i> Download CSV (Excel)
      </button>
    </div>
    <div class="table-responsive">
      <table class="table custom-table">
        <thead>
          <tr>
            <th>Company</th>
            <th>Role</th>
            <th>Package</th>
            <th>Status</th>
            <th>Applications</th>
            <th>Shortlisted</th>
            <th>Interview</th>
            <th>Selected</th>
            <th>Rejected</th>
          </tr>
        </thead>
        <tbody>
          ${data.map(c => `
            <tr>
              <td><strong>${escapeHtml(c.companyName)}</strong></td>
              <td>${escapeHtml(c.jobRole)}</td>
              <td><span class="text-success fw-bold">${c.packageLpa?.toFixed(2)} LPA</span></td>
              <td><span class="badge badge-status badge-${c.status.toLowerCase()}">${c.status}</span></td>
              <td><span class="badge bg-light text-dark border">${c.totalRegistrations}</span></td>
              <td><span class="badge bg-warning-subtle text-warning border">${c.shortlistedCount}</span></td>
              <td><span class="badge bg-purple-subtle text-primary border">${c.interviewCount}</span></td>
              <td><span class="badge bg-success-subtle text-success border fw-bold">${c.selectedCount}</span></td>
              <td><span class="badge bg-danger-subtle text-danger border">${c.rejectedCount}</span></td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

async function renderBranchWiseReport(container) {
  const res = await apiFetch('/reports/branch-wise');
  const data = res.data;

  container.innerHTML = `
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h6 class="fw-bold mb-0">Branch-Wise Placement Performance</h6>
      <button class="btn btn-sm btn-outline-success" onclick="downloadCsv('branch-wise')">
        <i class="bi bi-file-earmark-spreadsheet me-1"></i> Download CSV (Excel)
      </button>
    </div>
    <div class="table-responsive">
      <table class="table custom-table">
        <thead>
          <tr>
            <th>Branch</th>
            <th>Total Students</th>
            <th>Registered</th>
            <th>Placed Students</th>
            <th>Placement Rate</th>
            <th>Average Package</th>
            <th>Highest Package</th>
          </tr>
        </thead>
        <tbody>
          ${data.map(b => `
            <tr>
              <td><strong>${escapeHtml(b.branch)}</strong></td>
              <td>${b.totalStudents}</td>
              <td>${b.registeredStudents}</td>
              <td><span class="fw-bold text-success">${b.selectedStudents}</span></td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="progress flex-grow-1" style="height: 6px;">
                    <div class="progress-bar bg-success" style="width: ${b.placementPercentage}%"></div>
                  </div>
                  <small class="fw-bold">${b.placementPercentage}%</small>
                </div>
              </td>
              <td>${b.averagePackage > 0 ? b.averagePackage.toFixed(2) + ' LPA' : '-'}</td>
              <td><span class="fw-bold text-primary">${b.highestPackage > 0 ? b.highestPackage.toFixed(2) + ' LPA' : '-'}</span></td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

function downloadCsv(reportType) {
  const url = `${API_BASE}/reports/export-csv?type=${reportType}`;
  window.open(url, '_blank');
}

// ==========================================
// 8. SETTINGS & BACKUP
// ==========================================
async function loadSettings() {
  try {
    showSpinner(true);
    const [healthRes, backupRes] = await Promise.all([
      apiFetch('/health'),
      apiFetch('/backup/list')
    ]);

    const h = healthRes.data;
    document.getElementById('settings-status').textContent = h.status;
    document.getElementById('settings-datadir').textContent = h.dataDirectory;
    document.getElementById('settings-students-count').textContent = h.totalStudents;
    document.getElementById('settings-drives-count').textContent = h.totalDrives;

    const backupList = document.getElementById('settings-backups-list');
    const backups = backupRes.data;

    if (!backups || backups.length === 0) {
      backupList.innerHTML = `<div class="text-muted p-3">No database backups created yet. Click "Create Backup Now" to make a snapshot.</div>`;
    } else {
      backupList.innerHTML = backups.map(b => `
        <div class="d-flex justify-content-between align-items-center p-2 border-bottom">
          <div>
            <i class="bi bi-archive text-primary me-2"></i>
            <span class="fw-semibold">${escapeHtml(b)}</span>
          </div>
          <div>
            <button class="btn btn-sm btn-outline-primary" onclick="confirmRestoreBackup('${escapeHtml(b)}')">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Restore
            </button>
          </div>
        </div>
      `).join('');
    }
  } catch (err) {
    showToast('Failed to load settings: ' + err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

async function triggerCreateBackup() {
  try {
    showSpinner(true);
    const res = await apiFetch('/backup/create', { method: 'POST' });
    showToast(`Backup ${res.data.backupName} created successfully!`);
    loadSettings();
  } catch (err) {
    showToast('Backup creation failed: ' + err.message, 'error');
  } finally {
    showSpinner(false);
  }
}

function confirmRestoreBackup(backupName) {
  openConfirmModal(
    'Restore Database from Backup',
    `Are you sure you want to restore database from <strong>${backupName}</strong>? Current data will be replaced by the backup snapshot.`,
    async () => {
      try {
        showSpinner(true);
        await apiFetch(`/backup/restore/${encodeURIComponent(backupName)}`, { method: 'POST' });
        showToast('Database successfully restored from ' + backupName);
        loadSettings();
      } catch (err) {
        showToast(err.message, 'error');
      } finally {
        showSpinner(false);
      }
    }
  );
}

function confirmLoadDemoData() {
  openConfirmModal(
    'Load College Demo Dataset',
    'Load 12 fictional students, 4 placement drives, and candidate application lifecycles? (Existing records will be preserved unless you choose to reset).',
    async () => {
      try {
        showSpinner(true);
        await apiFetch('/demo/load?forceReset=true', { method: 'POST' });
        showToast('Demo dataset loaded successfully!');
        loadSettings();
      } catch (err) {
        showToast(err.message, 'error');
      } finally {
        showSpinner(false);
      }
    }
  );
}

function confirmResetAllData() {
  openConfirmModal(
    'Reset All Placement Data',
    '<strong class="text-danger">WARNING:</strong> This action will delete all students, drives, registrations, and status history files. Are you sure you want to proceed?',
    async () => {
      try {
        showSpinner(true);
        await apiFetch('/data/reset', { method: 'DELETE' });
        showToast('All placement data cleared. Database is empty.');
        loadSettings();
      } catch (err) {
        showToast(err.message, 'error');
      } finally {
        showSpinner(false);
      }
    }
  );
}

// Confirmation Modal Helper
let onConfirmAction = null;
function openConfirmModal(title, message, callback) {
  document.getElementById('confirm-modal-title').textContent = title;
  document.getElementById('confirm-modal-body').innerHTML = message;
  onConfirmAction = callback;
  new bootstrap.Modal(document.getElementById('confirmModal')).show();
}

function executeConfirmAction() {
  if (typeof onConfirmAction === 'function') {
    const action = onConfirmAction;
    onConfirmAction = null;
    bootstrap.Modal.getInstance(document.getElementById('confirmModal'))?.hide();
    action();
  }
}

function formatDate(isoStr) {
  if (!isoStr) return '-';
  try {
    const d = new Date(isoStr);
    return d.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return isoStr;
  }
}

// Initialize Application on Page Load
document.addEventListener('DOMContentLoaded', () => {
  // Navigation Links
  document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      const viewId = link.getAttribute('data-view');
      if (viewId) navigateTo(viewId);
    });
  });

  // Mobile menu toggle
  const toggleBtn = document.getElementById('menu-toggle');
  const sidebar = document.getElementById('sidebar');
  const overlay = document.getElementById('sidebar-overlay');
  if (toggleBtn && sidebar && overlay) {
    toggleBtn.addEventListener('click', () => {
      sidebar.classList.toggle('show');
      overlay.classList.toggle('show');
    });
    overlay.addEventListener('click', () => {
      sidebar.classList.remove('show');
      overlay.classList.remove('show');
    });
  }

  // Forms
  document.getElementById('student-form')?.addEventListener('submit', saveStudent);
  document.getElementById('drive-form')?.addEventListener('submit', saveDrive);
  document.getElementById('status-form')?.addEventListener('submit', submitStatusUpdate);
  document.getElementById('new-reg-form')?.addEventListener('submit', submitNewRegistration);
  document.getElementById('btn-confirm-action')?.addEventListener('click', executeConfirmAction);

  // Initial Route
  navigateTo('dashboard');
});
