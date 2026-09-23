import React, { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Layers,
  Car,
  FileText,
  Receipt,
  PieChart,
  ShieldCheck,
  TrendingUp,
  ArrowRight,
  CheckCircle2,
  Lock,
  Users,
  Building2,
  MapPin,
  Clock,
  Menu,
  X,
} from 'lucide-react';

export const LandingPage = () => {
  const { user } = useAuth();
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  // If already authenticated, redirect to /dashboard
  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <div style={{ backgroundColor: '#ffffff', minHeight: '100vh', color: 'var(--text-main)', fontFamily: 'Inter, sans-serif' }}>
      {/* ── 1. LANDING NAVBAR ──────────────────────────────────────────────── */}
      <nav
        style={{
          borderBottom: '1px solid #e2e8f0',
          position: 'sticky',
          top: 0,
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(8px)',
          zIndex: 50,
        }}
      >
        <div
          style={{
            maxWidth: 1280,
            margin: '0 auto',
            padding: '1rem 1.5rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          {/* Brand */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div
              style={{
                width: 38,
                height: 38,
                borderRadius: '8px',
                background: 'linear-gradient(135deg, #2563eb, #1d4ed8)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#ffffff',
              }}
            >
              <Layers size={22} />
            </div>
            <div>
              <span style={{ fontSize: '1.25rem', fontWeight: 700, color: '#0f172a', letterSpacing: '-0.02em' }}>
                FleetFlow
              </span>
              <span style={{ display: 'block', fontSize: '0.7rem', color: '#64748b', fontWeight: 500, marginTop: '-2px' }}>
                Fleet Billing & Operations
              </span>
            </div>
          </div>

          {/* Nav Links Desktop */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }} className="hidden lg:flex">
            <a href="#features" style={{ fontSize: '0.9rem', color: '#475569', fontWeight: 500 }}>Features</a>
            <a href="#billing" style={{ fontSize: '0.9rem', color: '#475569', fontWeight: 500 }}>Billing Engine</a>
            <a href="#how-it-works" style={{ fontSize: '0.9rem', color: '#475569', fontWeight: 500 }}>How It Works</a>
            <a href="#security" style={{ fontSize: '0.9rem', color: '#475569', fontWeight: 500 }}>Security</a>
          </div>

          {/* CTA Buttons */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }} className="hidden sm:flex">
            <Link to="/login" className="btn btn-secondary">
              Sign In
            </Link>
            <Link to="/register" className="btn btn-primary">
              Get Started <ArrowRight size={16} />
            </Link>
          </div>

          {/* Mobile Menu Toggle */}
          <button
            onClick={() => setMobileNavOpen(!mobileNavOpen)}
            style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#0f172a' }}
            className="sm:hidden"
          >
            {mobileNavOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {/* Mobile Nav Drawer */}
        {mobileNavOpen && (
          <div style={{ padding: '1rem 1.5rem 1.5rem 1.5rem', borderTop: '1px solid #e2e8f0', background: '#ffffff' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.25rem' }}>
              <a href="#features" onClick={() => setMobileNavOpen(false)} style={{ color: '#0f172a', fontWeight: 500 }}>Features</a>
              <a href="#billing" onClick={() => setMobileNavOpen(false)} style={{ color: '#0f172a', fontWeight: 500 }}>Billing Engine</a>
              <a href="#how-it-works" onClick={() => setMobileNavOpen(false)} style={{ color: '#0f172a', fontWeight: 500 }}>How It Works</a>
              <a href="#security" onClick={() => setMobileNavOpen(false)} style={{ color: '#0f172a', fontWeight: 500 }}>Security</a>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <Link to="/login" className="btn btn-secondary" style={{ width: '100%' }}>Sign In</Link>
              <Link to="/register" className="btn btn-primary" style={{ width: '100%' }}>Get Started</Link>
            </div>
          </div>
        )}
      </nav>

      {/* ── 2. HERO SECTION ────────────────────────────────────────────────── */}
      <section style={{ padding: '5rem 1.5rem 6rem 1.5rem', background: 'linear-gradient(180deg, #f8fafc 0%, #ffffff 100%)' }}>
        <div style={{ maxWidth: 1280, margin: '0 auto', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '3.5rem', alignItems: 'center' }}>
          <div>
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', backgroundColor: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: '9999px', padding: '0.35rem 0.85rem', fontSize: '0.8rem', fontWeight: 600, color: '#2563eb', marginBottom: '1.25rem' }}>
              <ShieldCheck size={16} /> Enterprise Fleet Billing & Fair Cost Allocation
            </div>
            <h1 style={{ fontSize: '3rem', fontWeight: 800, color: '#0f172a', lineHeight: 1.15, letterSpacing: '-0.03em', marginBottom: '1.25rem' }}>
              Smarter Fleet Operations. <br />
              <span style={{ color: '#2563eb' }}>Fairer Billing.</span>
            </h1>
            <p style={{ fontSize: '1.125rem', color: '#475569', lineHeight: 1.6, marginBottom: '2rem', maxWidth: 540 }}>
              Manage vehicles, contracts, trips, and automated billing in one intelligent platform — with transparent cost allocation and built-in fraud detection.
            </p>
            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
              <Link to="/register" className="btn btn-primary btn-lg">
                Get Started Free <ArrowRight size={18} />
              </Link>
              <Link to="/login" className="btn btn-secondary btn-lg">
                Sign In to Platform
              </Link>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', marginTop: '2.5rem', paddingTop: '1.5rem', borderTop: '1px solid #e2e8f0', fontSize: '0.85rem', color: '#64748b' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}><CheckCircle2 size={16} color="#16a34a" /> Role-Based Access</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}><CheckCircle2 size={16} color="#16a34a" /> Audit-Friendly</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}><CheckCircle2 size={16} color="#16a34a" /> 100% Transparent</div>
            </div>
          </div>

          {/* Interactive Hero Card Preview */}
          <div style={{ background: '#0f172a', borderRadius: '16px', padding: '1.75rem', color: '#ffffff', boxShadow: '0 25px 50px -12px rgba(15, 23, 42, 0.25)', border: '1px solid #1e293b' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', paddingBottom: '1rem', borderBottom: '1px solid #1e293b' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                <div style={{ width: 10, height: 10, borderRadius: '50%', backgroundColor: '#22c55e' }} />
                <span style={{ fontSize: '0.9rem', fontWeight: 600 }}>Fleet Billing Calculation Engine</span>
              </div>
              <span style={{ fontSize: '0.75rem', backgroundColor: '#1e293b', padding: '0.25rem 0.65rem', borderRadius: '6px', color: '#94a3b8' }}>Live Engine</span>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.25rem' }}>
              <div style={{ background: '#1e293b', padding: '1rem', borderRadius: '8px' }}>
                <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Total Eligible Trips</div>
                <div style={{ fontSize: '1.5rem', fontWeight: 700, marginTop: '0.2rem' }}>1,284</div>
              </div>
              <div style={{ background: '#1e293b', padding: '1rem', borderRadius: '8px' }}>
                <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Fixed Monthly Pool</div>
                <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#38bdf8', marginTop: '0.2rem' }}>₹4,50,000</div>
              </div>
            </div>

            <div style={{ background: '#1e293b', padding: '1rem', borderRadius: '8px', marginBottom: '1.25rem' }}>
              <div style={{ fontSize: '0.8rem', fontWeight: 600, color: '#e2e8f0', marginBottom: '0.5rem' }}>Trip Cost Split Formula</div>
              <div style={{ fontFamily: 'monospace', fontSize: '0.8rem', color: '#38bdf8', background: '#0f172a', padding: '0.65rem', borderRadius: '4px' }}>
                Trip Cost = (Dist × Rate) + Waiting + Night + Toll
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.85rem', background: 'rgba(34, 197, 94, 0.1)', border: '1px solid rgba(34, 197, 94, 0.3)', padding: '0.75rem 1rem', borderRadius: '8px', color: '#4ade80' }}>
              <span>✔ Largest Remainder Split Reconciled</span>
              <strong>100.00%</strong>
            </div>
          </div>
        </div>
      </section>

      {/* ── 3. FEATURES SECTION ────────────────────────────────────────────── */}
      <section id="features" style={{ padding: '5rem 1.5rem', backgroundColor: '#ffffff' }}>
        <div style={{ maxWidth: 1280, margin: '0 auto' }}>
          <div style={{ textAlign: 'center', maxWidth: 640, margin: '0 auto 3.5rem auto' }}>
            <h2 style={{ fontSize: '2.25rem', fontWeight: 700, color: '#0f172a', marginBottom: '0.75rem' }}>
              Everything you need to run fleet operations
            </h2>
            <p style={{ fontSize: '1rem', color: '#64748b' }}>
              Purpose-built tools for enterprise rental fleets, contract management, and fair monthly cost splits.
            </p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.75rem' }}>
            {[
              { icon: Building2, title: 'Fleet Management', desc: 'Manage vendors, vehicles, and operational records from one centralized platform.' },
              { icon: FileText, title: 'Smart Contracts', desc: 'Maintain contract versions and pricing slabs without losing historical pricing information.' },
              { icon: Receipt, title: 'Automated Billing', desc: 'Generate accurate monthly billing using contract-based pricing and trip-level calculations.' },
              { icon: PieChart, title: 'Fair Cost Allocation', desc: 'Distribute fixed monthly costs transparently using distance-based allocation.' },
              { icon: ShieldCheck, title: 'Fraud Detection', desc: 'Detect duplicate and suspicious trips before they distort your financial billing.' },
              { icon: TrendingUp, title: 'Real-Time Visibility', desc: 'Track trips, invoices, billing runs, and operational alerts from a unified dashboard.' },
            ].map((f, i) => (
              <div
                key={i}
                style={{
                  background: '#f8fafc',
                  border: '1px solid #e2e8f0',
                  borderRadius: '12px',
                  padding: '1.75rem',
                  transition: 'transform 0.15s ease, border-color 0.15s ease',
                }}
              >
                <div style={{ width: 44, height: 44, borderRadius: '10px', backgroundColor: '#eff6ff', color: '#2563eb', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1.25rem' }}>
                  <f.icon size={22} />
                </div>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 600, color: '#0f172a', marginBottom: '0.5rem' }}>{f.title}</h3>
                <p style={{ fontSize: '0.875rem', color: '#64748b', lineHeight: 1.6 }}>{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── 4. BILLING ENGINE TRANSPARENCY SECTION ────────────────────────── */}
      <section id="billing" style={{ padding: '5rem 1.5rem', backgroundColor: '#0f172a', color: '#ffffff' }}>
        <div style={{ maxWidth: 1280, margin: '0 auto' }}>
          <div style={{ textAlign: 'center', maxWidth: 640, margin: '0 auto 3rem auto' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: '#38bdf8', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Billing Engine</span>
            <h2 style={{ fontSize: '2.25rem', fontWeight: 700, color: '#ffffff', marginTop: '0.35rem', marginBottom: '0.75rem' }}>
              Billing you can actually explain.
            </h2>
            <p style={{ fontSize: '1rem', color: '#94a3b8' }}>
              No hidden calculations or opaque black boxes. Every invoice line item breaks down base charge, surcharges, and cost splits.
            </p>
          </div>

          <div style={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '16px', padding: '2rem', maxWidth: 900, margin: '0 auto' }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', textAlign: 'center' }}>
              <div style={{ background: '#0f172a', padding: '1rem', borderRadius: '8px' }}>
                <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Distance × Rate</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 600, color: '#ffffff', marginTop: '0.25rem' }}>Base Charge</div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.25rem', color: '#38bdf8', fontWeight: 700 }}>+</div>
              <div style={{ background: '#0f172a', padding: '1rem', borderRadius: '8px' }}>
                <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Waiting & Night</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 600, color: '#ffffff', marginTop: '0.25rem' }}>Surcharges</div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.25rem', color: '#38bdf8', fontWeight: 700 }}>+</div>
              <div style={{ background: '#0f172a', padding: '1rem', borderRadius: '8px' }}>
                <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Tolls</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 600, color: '#ffffff', marginTop: '0.25rem' }}>Pass-Through</div>
              </div>
            </div>

            <div style={{ margin: '1.75rem 0', borderTop: '1px solid #334155' }} />

            <div style={{ textAlign: 'center' }}>
              <p style={{ fontSize: '0.95rem', color: '#cbd5e1', marginBottom: '0.5rem' }}>
                <strong>Fixed Monthly Fee Allocation:</strong> Lump sum contract fees are distributed across eligible shift trips using the <em>Largest Remainder Method</em>.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* ── 5. HOW IT WORKS SECTION ────────────────────────────────────────── */}
      <section id="how-it-works" style={{ padding: '5rem 1.5rem', backgroundColor: '#f8fafc' }}>
        <div style={{ maxWidth: 1280, margin: '0 auto' }}>
          <div style={{ textAlign: 'center', maxWidth: 640, margin: '0 auto 3.5rem auto' }}>
            <h2 style={{ fontSize: '2.25rem', fontWeight: 700, color: '#0f172a', marginBottom: '0.75rem' }}>
              How FleetFlow Works
            </h2>
            <p style={{ fontSize: '1rem', color: '#64748b' }}>Four simple steps from contract setup to audit-ready invoice generation.</p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.5rem' }}>
            {[
              { step: '01', title: 'Configure', desc: 'Set up vendors, vehicles, contracts, and tiered pricing slabs.' },
              { step: '02', title: 'Track', desc: 'Log completed trips, distance, duty hours, and toll receipts.' },
              { step: '03', title: 'Calculate', desc: 'Run automated monthly billing engine against applicable contract versions.' },
              { step: '04', title: 'Verify', desc: 'Review tax invoices, fair cost splits, and fraud security alerts.' },
            ].map((s, i) => (
              <div key={i} style={{ background: '#ffffff', border: '1px solid #e2e8f0', borderRadius: '12px', padding: '1.5rem' }}>
                <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#2563eb', marginBottom: '0.5rem' }}>{s.step}</div>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 600, color: '#0f172a', marginBottom: '0.5rem' }}>{s.title}</h3>
                <p style={{ fontSize: '0.875rem', color: '#64748b' }}>{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── 6. SECURITY & ROLES SECTION ───────────────────────────────────── */}
      <section id="security" style={{ padding: '5rem 1.5rem', backgroundColor: '#ffffff' }}>
        <div style={{ maxWidth: 1280, margin: '0 auto' }}>
          <div style={{ textAlign: 'center', maxWidth: 640, margin: '0 auto 3.5rem auto' }}>
            <h2 style={{ fontSize: '2.25rem', fontWeight: 700, color: '#0f172a', marginBottom: '0.75rem' }}>
              Built for Controlled Enterprise Access
            </h2>
            <p style={{ fontSize: '1rem', color: '#64748b' }}>
              Role-based authorization ensures every user sees only the operations relevant to their responsibilities.
            </p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
            {[
              { role: 'ADMIN', badge: 'Full Access', desc: 'Manage vendors, vehicles, contracts, pricing slabs, billing runs, and fraud alerts.' },
              { role: 'HR', badge: 'Billing & Operations', desc: 'Review operational trips, execute billing runs, allocate fees, and inspect invoices.' },
              { role: 'EMPLOYEE', badge: 'Operational Visibility', desc: 'View assigned fleet vehicles, log trips, and track personal operational history.' },
            ].map((r, i) => (
              <div key={i} style={{ border: '1px solid #e2e8f0', borderRadius: '12px', padding: '1.5rem', background: '#f8fafc' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                  <span style={{ fontSize: '1.1rem', fontWeight: 700, color: '#0f172a' }}>{r.role}</span>
                  <span style={{ fontSize: '0.75rem', fontWeight: 600, backgroundColor: '#eff6ff', color: '#2563eb', padding: '0.25rem 0.65rem', borderRadius: '9999px' }}>
                    {r.badge}
                  </span>
                </div>
                <p style={{ fontSize: '0.875rem', color: '#64748b', lineHeight: 1.5 }}>{r.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── 7. FINAL CTA & FOOTER ─────────────────────────────────────────── */}
      <section style={{ padding: '5rem 1.5rem', backgroundColor: '#2563eb', color: '#ffffff', textAlign: 'center' }}>
        <div style={{ maxWidth: 640, margin: '0 auto' }}>
          <h2 style={{ fontSize: '2.25rem', fontWeight: 800, marginBottom: '1rem' }}>
            Bring your fleet operations into one place.
          </h2>
          <p style={{ fontSize: '1.1rem', opacity: 0.9, marginBottom: '2rem' }}>
            Automate monthly billing runs, eliminate pricing disputes, and detect fraud before it affects your bottom line.
          </p>
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
            <Link to="/register" className="btn btn-secondary btn-lg" style={{ color: '#2563eb', fontWeight: 700 }}>
              Start Managing Your Fleet
            </Link>
            <Link to="/login" className="btn btn-primary btn-lg" style={{ background: '#1d4ed8', borderColor: '#1d4ed8' }}>
              Sign In
            </Link>
          </div>
        </div>
      </section>

      <footer style={{ borderTop: '1px solid #e2e8f0', padding: '2.5rem 1.5rem', backgroundColor: '#0f172a', color: '#94a3b8' }}>
        <div style={{ maxWidth: 1280, margin: '0 auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff' }}>FleetFlow</div>
            <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Fleet Billing & Operations</div>
          </div>
          <div style={{ fontSize: '0.85rem', color: '#64748b' }}>
            © 2026 FleetFlow. All rights reserved. Enterprise Fleet Platform.
          </div>
        </div>
      </footer>
    </div>
  );
};
