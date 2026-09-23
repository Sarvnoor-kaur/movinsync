/** Format ISO datetime string to human-readable: "22 Sep 2026, 10:30 AM" */
export function formatDateTime(isoStr) {
  if (!isoStr) return '—';
  try {
    return new Intl.DateTimeFormat('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit', hour12: true,
    }).format(new Date(isoStr));
  } catch {
    return isoStr;
  }
}

/** Format date only: "22 Sep 2026" */
export function formatDate(dateStr) {
  if (!dateStr) return '—';
  try {
    return new Intl.DateTimeFormat('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric',
    }).format(new Date(dateStr));
  } catch {
    return dateStr;
  }
}

/** Format LocalTime "HH:mm:ss" to "10:30 AM" */
export function formatTime(timeStr) {
  if (!timeStr) return '—';
  try {
    const [h, m] = timeStr.split(':');
    const d = new Date();
    d.setHours(Number(h), Number(m));
    return d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: true });
  } catch {
    return timeStr;
  }
}

/** Convert "YYYY-MM-DD" to display "Sep 2026" (for billing month) */
export function formatBillingMonth(ym) {
  if (!ym) return '—';
  try {
    const [y, m] = ym.split('-');
    return new Intl.DateTimeFormat('en-IN', { month: 'short', year: 'numeric' })
      .format(new Date(Number(y), Number(m) - 1, 1));
  } catch {
    return ym;
  }
}
