/** Format paisa to Indian Rupee display: 150000 → ₹1,500.00 */
export function formatCurrency(paisa) {
  if (paisa == null || paisa === undefined) return '—';
  const rupees = paisa / 100;
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
  }).format(rupees);
}

/** Convert rupee string input to integer paisa: "15.50" → 1550 */
export function rupeesToPaisa(rupeesStr) {
  if (!rupeesStr && rupeesStr !== 0) return 0;
  const rupees = parseFloat(String(rupeesStr).replace(/,/g, ''));
  if (isNaN(rupees)) return 0;
  return Math.round(rupees * 100);
}

/** Convert paisa to rupee string for input display: 1550 → "15.50" */
export function paisaToRupees(paisa) {
  if (paisa == null) return '';
  return (paisa / 100).toFixed(2);
}
