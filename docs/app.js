'use strict';

const MAX_NOTES_LENGTH = 500;
const API_BASE = 'api';   // relative, so it works wherever the page and API are served together
const API_TIMEOUT_MS = 2000;
const SUBMIT_TIMEOUT_MS = 10000;
const BUSINESS_TIME_ZONE = 'America/Chicago';   // same zone the server uses for "today"
const SAMPLE_DATA_URL = 'sample_requests.json';
const STORAGE_KEY = 'ifsi-demo-requests';
const STUDENT_NOT_FOUND = (studentId) => `No student found with ID ${studentId}.`;

// Same wording as the server, so users see one message whichever side catches the problem.
const ERROR_MESSAGES = {
  studentIdRequired: 'Student ID is required.',
  studentIdNotNumber: 'Student ID must be a whole number.',
  courseCodeRequired: 'Course is required.',
  requestDateRequired: 'Exam date is required.',
  requestDateInvalid: 'Enter a real date as YYYY-MM-DD.',
  requestDatePast: "Exam date can't be in the past.",
  notesTooLong: `Notes must be ${MAX_NOTES_LENGTH} characters or fewer.`,
};

const MONTH_NAMES = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

const requestForm = document.getElementById('requestForm');
const studentIdInput = document.getElementById('studentId');
const courseCodeSelect = document.getElementById('courseCode');
const requestDateInput = document.getElementById('requestDate');
const needsAccommodationCheckbox = document.getElementById('needsAccommodation');
const notesTextarea = document.getElementById('notes');
const notesCounter = document.getElementById('notesCount');
const formStatusMessage = document.getElementById('formStatus');
const submitButton = requestForm.querySelector('button[type="submit"]');
const filterInput = document.getElementById('filter');
const requestsTableBody = document.querySelector('#requestsTable tbody');
const resultCountMessage = document.getElementById('resultCount');
const noMatchesMessage = document.getElementById('noMatches');
const demoModeBadge = document.getElementById('demoModeBadge');

// Demo mode is decided by where the page is hosted, never by a failed request:
// GitHub Pages has no server; ?demo forces it for local testing.
const isDemoMode = location.hostname.endsWith('github.io')
  || new URLSearchParams(location.search).has('demo');

let allRequests = [];     // every loaded request; the table is always drawn from this
let sampleRequests = [];  // demo mode: the requests from sample_requests.json

// API field name -> form control. Keys match the server's error keys, so one
// showFieldErrors() handles both page checks and server responses.
const FIELD_INPUTS = {
  student_id: studentIdInput,
  course_code: courseCodeSelect,
  request_date: requestDateInput,
  notes: notesTextarea,
};

// Today's date as YYYY-MM-DD in Central Time ('en-CA' formats dates as YYYY-MM-DD).
function getTodayBusinessDate() {
  return new Intl.DateTimeFormat('en-CA', { timeZone: BUSINESS_TIME_ZONE }).format(new Date());
}

function updateNotesCounter() {
  notesCounter.textContent = `${notesTextarea.value.length}/${MAX_NOTES_LENGTH}`;
}

// --- Validation ---------------------------------------------------------

// Returns {field: message} for every problem; an empty object means the form is valid.
function validateForm() {
  const errors = {};

  const studentId = studentIdInput.value.trim();
  if (studentId === '') {
    errors.student_id = ERROR_MESSAGES.studentIdRequired;
  } else if (!/^\d+$/.test(studentId)) {
    errors.student_id = ERROR_MESSAGES.studentIdNotNumber;
  }

  if (courseCodeSelect.value === '') {
    errors.course_code = ERROR_MESSAGES.courseCodeRequired;
  }

  // badInput: the user typed something the date field couldn't read as a full date.
  if (requestDateInput.validity.badInput) {
    errors.request_date = ERROR_MESSAGES.requestDateInvalid;
  } else if (requestDateInput.value === '') {
    errors.request_date = ERROR_MESSAGES.requestDateRequired;
  } else if (requestDateInput.value < getTodayBusinessDate()) {   // YYYY-MM-DD strings sort like dates
    errors.request_date = ERROR_MESSAGES.requestDatePast;
  }

  if (notesTextarea.value.trim().length > MAX_NOTES_LENGTH) {
    errors.notes = ERROR_MESSAGES.notesTooLong;
  }

  return errors;
}

function setFieldError(field, message) {
  const input = FIELD_INPUTS[field];
  if (!input) return;
  document.getElementById(`${input.id}-error`).textContent = message;
  if (message) {
    input.setAttribute('aria-invalid', 'true');
  } else {
    input.removeAttribute('aria-invalid');
  }
}

function clearFieldErrors() {
  Object.keys(FIELD_INPUTS).forEach((field) => setFieldError(field, ''));
}

// Shows each message under its field and focuses the first invalid field, in form order.
function showFieldErrors(errors) {
  clearFieldErrors();
  Object.entries(errors).forEach(([field, message]) => setFieldError(field, message));
  const firstInvalidField = Object.keys(FIELD_INPUTS).find((field) => errors[field]);
  if (firstInvalidField) {
    FIELD_INPUTS[firstInvalidField].focus();
  }
}

function showFormStatus(message, isError = false) {
  formStatusMessage.textContent = message;
  formStatusMessage.classList.toggle('is-error', isError);
}

// The JSON body the server expects (same keys as sample_requests.json).
function buildRequestPayload() {
  return {
    student_id: studentIdInput.value.trim(),
    course_code: courseCodeSelect.value,
    request_date: requestDateInput.value,
    needs_accommodation: needsAccommodationCheckbox.checked,
    notes: notesTextarea.value.trim() || null,
  };
}

// Shared by both modes once a request is saved.
function showSavedRequest(savedRequest, statusSuffix = '') {
  allRequests.push(savedRequest);
  filterInput.value = '';                      // make sure the new row isn't filtered out
  renderRequestsTable(savedRequest.request_id);
  requestForm.reset();
  clearFieldErrors();
  showFormStatus(`Request #${savedRequest.request_id} submitted.${statusSuffix}`);
}

async function submitRequest(payload) {
  if (isDemoMode) {
    const result = saveRequestInBrowser(payload);
    if (result.errors) {
      showServerErrors(result.errors);
    } else {
      showSavedRequest(result.savedRequest, result.isStored
        ? ' (Demo: saved in this browser only, not sent to IFSI.)'
        : ' (Demo: saved for this visit only; browser storage is unavailable.)');
    }
    return;
  }

  submitButton.disabled = true;
  showFormStatus('Submitting…');
  try {
    const response = await fetch(`${API_BASE}/requests`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
      signal: AbortSignal.timeout(SUBMIT_TIMEOUT_MS),
    });

    if (response.status === 201) {
      showSavedRequest(await response.json());
      return;
    }
    if (response.status === 400) {
      const body = await response.json();
      showServerErrors(body.errors || {});
      return;
    }

    throw new Error(`HTTP ${response.status}`);
  } catch (error) {
    console.error('Could not save request:', error);
    // After a timeout the browser can't know whether the server saved it, so say so.
    showFormStatus(error.name === 'TimeoutError'
      ? "The server didn't respond. Check the table before submitting again; it may have been saved."
      : "Couldn't save your request. Please try again.", true);
  } finally {
    submitButton.disabled = false;
  }
}

// Field errors go under their fields; anything without a matching field goes in the status line.
function showServerErrors(errors) {
  showFieldErrors(errors);
  const otherMessages = Object.entries(errors)
    .filter(([field]) => !FIELD_INPUTS[field])
    .map(([, message]) => message);
  showFormStatus(otherMessages.length > 0
    ? otherMessages.join(' ')
    : 'Please fix the highlighted fields.', true);
}

function handleFormSubmit(event) {
  event.preventDefault();
  showFormStatus('');

  const errors = validateForm();
  if (Object.keys(errors).length > 0) {
    showFieldErrors(errors);
    return;
  }
  clearFieldErrors();

  // Confirm only after validation passes, so nobody confirms a form that then fails.
  if (needsAccommodationCheckbox.checked) {
    const isConfirmed = window.confirm(
      'You requested a testing accommodation. Submit this request with an accommodation?');
    if (!isConfirmed) {
      needsAccommodationCheckbox.checked = false;
      showFormStatus('Not submitted. Accommodation was unchecked. Review the form and submit again.', true);
      needsAccommodationCheckbox.focus();
      return;
    }
  }

  submitRequest(buildRequestPayload());
}

// --- Requests table ------------------------------------------------------

// '2026-09-14' -> 'Sep 14, 2026'. Splits the string instead of using new Date(),
// which reads the date as UTC midnight and shows the previous day in US time zones.
function formatDisplayDate(isoDate) {
  const [year, month, day] = isoDate.split('-');
  return `${MONTH_NAMES[Number(month) - 1]} ${Number(day)}, ${year}`;
}

function formatAccommodation(needsAccommodation) {
  return needsAccommodation ? 'Yes' : 'No';
}

// Everything visible in a row, lowercased, so the filter matches what users see.
function getSearchText(request) {
  return [
    request.request_id,
    request.first_name,
    request.last_name,
    `${request.first_name} ${request.last_name}`,
    request.fire_department,
    request.course_code,
    request.course_name,
    formatDisplayDate(request.request_date),
    request.status,
    formatAccommodation(request.needs_accommodation),
    request.notes,
  ].join(' ').toLowerCase();
}

function createCell(label, text, className) {
  const cell = document.createElement('td');
  cell.dataset.label = label;   // shown as the field name in the phone card layout
  cell.textContent = text;      // textContent, not innerHTML: user text is never run as HTML
  if (className) cell.className = className;
  return cell;
}

function createStatusCell(status) {
  const cell = document.createElement('td');
  cell.dataset.label = 'Status';
  const badge = document.createElement('span');
  badge.className = `badge badge-${status.toLowerCase()}`;
  badge.textContent = status;
  cell.append(badge);
  return cell;
}

function createRequestRow(request, highlightedRequestId) {
  const row = document.createElement('tr');
  if (request.request_id === highlightedRequestId) {
    row.classList.add('is-new');
  }
  row.append(
    createCell('ID', request.request_id, 'cell-nowrap'),
    createCell('Student', `${request.first_name} ${request.last_name}`),
    createCell('Department', request.fire_department || '—'),
    createCell('Course', `${request.course_code} · ${request.course_name}`),
    createCell('Exam date', formatDisplayDate(request.request_date), 'cell-nowrap'),
    createStatusCell(request.status),
    createCell('Accommodation', formatAccommodation(request.needs_accommodation)),
    createCell('Notes', request.notes || '—', 'cell-notes'),
  );
  return row;
}

// Redraws the table from allRequests, applying the current filter text.
// highlightedRequestId (optional) marks a just-submitted row and scrolls it into view.
function renderRequestsTable(highlightedRequestId) {
  const query = filterInput.value.trim().toLowerCase();
  const matchingRequests = allRequests
    .filter((request) => getSearchText(request).includes(query))
    .sort((a, b) => a.request_id - b.request_id);   // oldest first: requests are handled in the order received

  requestsTableBody.replaceChildren(
    ...matchingRequests.map((request) => createRequestRow(request, highlightedRequestId)));
  resultCountMessage.textContent = `Showing ${matchingRequests.length} of ${allRequests.length} requests`;
  noMatchesMessage.hidden = matchingRequests.length > 0 || allRequests.length === 0;

  const highlightedRow = requestsTableBody.querySelector('.is-new');
  if (highlightedRow) {
    highlightedRow.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, { signal: AbortSignal.timeout(API_TIMEOUT_MS), ...options });
  if (!response.ok) throw new Error(`HTTP ${response.status} for ${url}`);
  return response.json();
}

// --- Browser storage (demo mode) -----------------------------------------

// Stored data can be stale or edited by hand, so only well-formed rows are used.
function isValidStoredRequest(request) {
  return request !== null && typeof request === 'object'
    && Number.isInteger(request.request_id)
    && typeof request.first_name === 'string'
    && typeof request.status === 'string'
    && /^\d{4}-\d{2}-\d{2}$/.test(request.request_date);
}

function readStoredRequests() {
  try {
    const stored = JSON.parse(localStorage.getItem(STORAGE_KEY));
    return Array.isArray(stored) ? stored.filter(isValidStoredRequest) : [];
  } catch (error) {
    console.warn('Browser storage unavailable:', error);
    return [];
  }
}

// Returns false if the browser refused (private mode, storage full, blocked).
function writeStoredRequests(requests) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(requests));
    return true;
  } catch (error) {
    console.warn('Could not save to browser storage:', error);
    return false;
  }
}

// --- Demo mode data ------------------------------------------------------

// Unique courses from the sample data, sorted by name like GET /api/courses.
function getCoursesFromSampleData() {
  const coursesByCode = new Map();
  sampleRequests.forEach((request) => coursesByCode.set(request.course_code, request.course_name));
  return [...coursesByCode]
    .map(([courseCode, courseName]) => ({ course_code: courseCode, course_name: courseName }))
    .sort((a, b) => a.course_name.localeCompare(b.course_name));
}

// Same result as POST /api/requests: a saved row, or {errors} if the student is unknown.
function saveRequestInBrowser(payload) {
  const student = sampleRequests.find((request) => String(request.student_id) === payload.student_id);
  if (!student) {
    return { errors: { student_id: STUDENT_NOT_FOUND(payload.student_id) } };
  }
  const course = getCoursesFromSampleData().find((c) => c.course_code === payload.course_code);

  const savedRequest = {
    request_id: Math.max(...allRequests.map((request) => request.request_id)) + 1,
    student_id: student.student_id,
    first_name: student.first_name,
    last_name: student.last_name,
    fire_department: student.fire_department,
    course_code: course.course_code,
    course_name: course.course_name,          // from the catalog, never from the form
    request_date: payload.request_date,
    status: 'Pending',
    needs_accommodation: payload.needs_accommodation,
    notes: payload.notes,
  };
  const storedRequests = [...readStoredRequests(), savedRequest];
  return { savedRequest, isStored: writeStoredRequests(storedRequests) };
}

// --- Loading -------------------------------------------------------------

function renderCourseOptions(courses) {
  courseCodeSelect.append(...courses.map((course) => {
    const option = document.createElement('option');
    option.value = course.course_code;
    option.textContent = `${course.course_name} (${course.course_code})`;
    return option;
  }));
}

async function loadCourses() {
  if (isDemoMode) {
    demoModeBadge.hidden = false;
    sampleRequests = await fetchJson(SAMPLE_DATA_URL);
    renderCourseOptions(getCoursesFromSampleData());
  } else {
    renderCourseOptions(await fetchJson(`${API_BASE}/courses`));
  }
}

async function loadRequests() {
  allRequests = isDemoMode
    ? [...sampleRequests, ...readStoredRequests()]
    : await fetchJson(`${API_BASE}/requests`);
  renderRequestsTable();
}

async function startApp() {
  try {
    await loadCourses();
    await loadRequests();
  } catch (error) {
    console.error('Could not load data:', error);
    resultCountMessage.textContent = "Couldn't load requests.";
    showFormStatus('The request service is unavailable right now. Please refresh the page to try again.', true);
    submitButton.disabled = true;     // never "save" while the server is down
  }
}

// --- Wiring --------------------------------------------------------------

requestDateInput.min = getTodayBusinessDate();
notesTextarea.addEventListener('input', updateNotesCounter);
requestForm.addEventListener('reset', () => setTimeout(updateNotesCounter));
requestForm.addEventListener('submit', handleFormSubmit);

// Clear a field's error as soon as the user changes it.
Object.entries(FIELD_INPUTS).forEach(([field, input]) => {
  const eventName = input.tagName === 'SELECT' ? 'change' : 'input';
  input.addEventListener(eventName, () => setFieldError(field, ''));
});

filterInput.addEventListener('input', () => renderRequestsTable());

updateNotesCounter();
startApp();
