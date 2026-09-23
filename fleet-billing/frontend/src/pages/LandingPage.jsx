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
  Sparkles,
  Calculator,
  Zap,
  ChevronDown,
  ChevronUp,
  Check,
  HelpCircle,
  Activity,
  Sliders,
} from 'lucide-react';

export const LandingPage = () => {
  const { user } = useAuth();
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  // Interactive Live Billing Simulator State for Hero Widget
  const [simVehicleType, setSimVehicleType] = useState('CAB');
  const [simDistance, setSimDistance] = useState(35);
  const [simNight, setSimNight] = useState(true);
  const [simWaiting, setSimWaiting] = useState(1.5);
  const [simToll, setSimToll] = useState(120);

  // Interactive Feature Explorer Tab State
  const [activeTab, setActiveTab] = useState('billing');

  // FAQ Accordion State
  const [openFaq, setOpenFaq] = useState(null);

  // If user is already authenticated, redirect to dashboard
  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  // Calculate live simulator values
  const ratePerKm = simVehicleType === 'SUV' ? 22 : simVehicleType === 'BUS' ? 45 : 16;
  const baseCharge = simDistance * ratePerKm;
  const nightCharge = simNight ? 250 : 0;
  const waitingCharge = Math.round(simWaiting * 150);
  const totalCost = baseCharge + nightCharge + waitingCharge + simToll;

  const toggleFaq = (index) => {
    setOpenFaq(openFaq === index ? null : index);
  };

  return (
    <div
      style={{
        backgroundColor: '#090d16',
        minHeight: '100vh',
        color: '#f8fafc',
        fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, sans-serif",
        overflowX: 'hidden',
      }}
    >
      {/* Background Radial Glow Effects */}
      <div
        style={{
          position: 'absolute',
          top: 0,
          left: '50%',
          transform: 'translateX(-50%)',
          width: '1000px',
          height: '550px',
          background: 'radial-gradient(circle, rgba(37, 99, 235, 0.18) 0%, rgba(99, 102, 241, 0.08) 50%, rgba(9, 13, 22, 0) 80%)',
          pointerEvents: 'none',
          zIndex: 0,
        }}
      />

      {/* ── 1. TRANSLUCENT GLASS NAVBAR ──────────────────────────────────── */}
      <nav
        style={{
          position: 'sticky',
          top: 0,
          backgroundColor: 'rgba(9, 13, 22, 0.85)',
          backdropFilter: 'blur(16px)',
          WebkitBackdropFilter: 'blur(16px)',
          borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
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
          {/* Logo Brand */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div
              style={{
                width: 40,
                height: 40,
                borderRadius: '10px',
                background: 'linear-gradient(135deg, #38bdf8 0%, #2563eb 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#ffffff',
                boxShadow: '0 0 20px rgba(56, 189, 248, 0.35)',
              }}
            >
              <Layers size={22} />
            </div>
            <div>
              <span style={{ fontSize: '1.3rem', fontWeight: 800, color: '#ffffff', letterSpacing: '-0.02em' }}>
                FleetFlow
              </span>
              <span style={{ display: 'block', fontSize: '0.675rem', color: '#38bdf8', fontWeight: 600, letterSpacing: '0.05em' }}>
                FLEET BILLING & OPS
              </span>
            </div>
          </div>

          {/* Desktop Links */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '2.25rem' }} className="hidden lg:flex">
            <a href="#calculator" style={{ fontSize: '0.875rem', color: '#94a3b8', fontWeight: 500, transition: 'color 0.15s' }}>
              Simulator
            </a>
            <a href="#features" style={{ fontSize: '0.875rem', color: '#94a3b8', fontWeight: 500, transition: 'color 0.15s' }}>
              Capabilities
            </a>
            <a href="#cost-split" style={{ fontSize: '0.875rem', color: '#94a3b8', fontWeight: 500, transition: 'color 0.15s' }}>
              Fair Cost Split
            </a>
            <a href="#security" style={{ fontSize: '0.875rem', color: '#94a3b8', fontWeight: 500, transition: 'color 0.15s' }}>
              Security
            </a>
            <a href="#faq" style={{ fontSize: '0.875rem', color: '#94a3b8', fontWeight: 500, transition: 'color 0.15s' }}>
              FAQ
            </a>
          </div>

          {/* Desktop Action CTAs */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }} className="hidden sm:flex">
            <Link
              to="/login"
              style={{
                color: '#f8fafc',
                border: '1px solid rgba(255, 255, 255, 0.15)',
                borderRadius: '8px',
                padding: '0.5rem 1.15rem',
                fontSize: '0.875rem',
                fontWeight: 600,
                textDecoration: 'none',
                backgroundColor: 'rgba(255, 255, 255, 0.03)',
              }}
            >
              Sign In
            </Link>
            <Link
              to="/register"
              style={{
                background: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)',
                color: '#ffffff',
                borderRadius: '8px',
                padding: '0.5rem 1.25rem',
                fontSize: '0.875rem',
                fontWeight: 600,
                textDecoration: 'none',
                boxShadow: '0 0 20px rgba(37, 99, 235, 0.4)',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.4rem',
              }}
            >
              Get Started <ArrowRight size={16} />
            </Link>
          </div>

          {/* Mobile Menu Toggle Button */}
          <button
            onClick={() => setMobileNavOpen(!mobileNavOpen)}
            style={{ background: 'none', border: 'none', color: '#ffffff', cursor: 'pointer' }}
            className="lg:hidden"
          >
            {mobileNavOpen ? <X size={26} /> : <Menu size={26} />}
          </button>
        </div>

        {/* Mobile Dropdown Menu */}
        {mobileNavOpen && (
          <div style={{ backgroundColor: '#0f172a', borderBottom: '1px solid #1e293b', padding: '1.25rem 1.5rem' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.25rem' }}>
              <a href="#calculator" onClick={() => setMobileNavOpen(false)} style={{ color: '#cbd5e1', textDecoration: 'none' }}>Live Simulator</a>
              <a href="#features" onClick={() => setMobileNavOpen(false)} style={{ color: '#cbd5e1', textDecoration: 'none' }}>Capabilities</a>
              <a href="#cost-split" onClick={() => setMobileNavOpen(false)} style={{ color: '#cbd5e1', textDecoration: 'none' }}>Fair Cost Split</a>
              <a href="#security" onClick={() => setMobileNavOpen(false)} style={{ color: '#cbd5e1', textDecoration: 'none' }}>Security</a>
              <a href="#faq" onClick={() => setMobileNavOpen(false)} style={{ color: '#cbd5e1', textDecoration: 'none' }}>FAQ</a>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <Link to="/login" className="btn btn-secondary" style={{ width: '100%', justifyContent: 'center' }}>Sign In</Link>
              <Link to="/register" className="btn btn-primary" style={{ width: '100%', justifyContent: 'center' }}>Get Started</Link>
            </div>
          </div>
        )}
      </nav>

      {/* ── 2. HERO SECTION WITH LIVE BILLING SIMULATOR ────────────────── */}
      <section style={{ padding: '4.5rem 1.5rem 5.5rem 1.5rem', position: 'relative', zIndex: 1 }}>
        <div
          style={{
            maxWidth: 1280,
            margin: '0 auto',
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))',
            gap: '3.5rem',
            alignItems: 'center',
          }}
        >
          {/* Hero Left Content */}
          <div>
            <div
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.5rem',
                background: 'rgba(56, 189, 248, 0.1)',
                border: '1px solid rgba(56, 189, 248, 0.3)',
                borderRadius: '9999px',
                padding: '0.35rem 0.9rem',
                fontSize: '0.8rem',
                fontWeight: 600,
                color: '#38bdf8',
                marginBottom: '1.5rem',
              }}
            >
              <Sparkles size={16} /> Enterprise Fleet Billing & Cost Split Platform
            </div>

            <h1
              style={{
                fontSize: '3.25rem',
                fontWeight: 800,
                lineHeight: 1.12,
                letterSpacing: '-0.035em',
                marginBottom: '1.25rem',
                color: '#ffffff',
              }}
            >
              Smarter Fleet Operations.{' '}
              <span
                style={{
                  background: 'linear-gradient(135deg, #38bdf8 0%, #818cf8 100%)',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                }}
              >
                Fairer Billing.
              </span>
            </h1>

            <p style={{ fontSize: '1.125rem', color: '#94a3b8', lineHeight: 1.65, marginBottom: '2.25rem', maxWidth: 540 }}>
              Manage vehicles, contracts, trips, and automated billing in one unified platform — with zero-drift cost allocation and real-time fraud security alerts.
            </p>

            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
              <Link
                to="/register"
                style={{
                  background: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)',
                  color: '#ffffff',
                  padding: '0.85rem 1.75rem',
                  borderRadius: '10px',
                  fontWeight: 700,
                  fontSize: '1rem',
                  textDecoration: 'none',
                  boxShadow: '0 0 25px rgba(37, 99, 235, 0.45)',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                }}
              >
                Start Managing Your Fleet <ArrowRight size={18} />
              </Link>
              <Link
                to="/login"
                style={{
                  backgroundColor: 'rgba(255, 255, 255, 0.05)',
                  border: '1px solid rgba(255, 255, 255, 0.15)',
                  color: '#ffffff',
                  padding: '0.85rem 1.75rem',
                  borderRadius: '10px',
                  fontWeight: 600,
                  fontSize: '1rem',
                  textDecoration: 'none',
                }}
              >
                Sign In
              </Link>
            </div>

            {/* Quick Metrics Bar */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(3, 1fr)',
                gap: '1rem',
                marginTop: '3rem',
                paddingTop: '2rem',
                borderTop: '1px solid rgba(255, 255, 255, 0.08)',
              }}
            >
              <div>
                <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#ffffff' }}>100%</div>
                <div style={{ fontSize: '0.775rem', color: '#94a3b8', marginTop: '0.1rem' }}>Reconciled Cost Splits</div>
              </div>
              <div>
                <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#38bdf8' }}>&lt; 2s</div>
                <div style={{ fontSize: '0.775rem', color: '#94a3b8', marginTop: '0.1rem' }}>Billing Calculation</div>
              </div>
              <div>
                <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#818cf8' }}>24/7</div>
                <div style={{ fontSize: '0.775rem', color: '#94a3b8', marginTop: '0.1rem' }}>Fraud Detection</div>
              </div>
            </div>
          </div>

          {/* Hero Right: Live Interactive Billing Simulator */}
          <div id="calculator">
            <div
              style={{
                background: 'rgba(15, 23, 42, 0.75)',
                backdropFilter: 'blur(20px)',
                WebkitBackdropFilter: 'blur(20px)',
                border: '1px solid rgba(56, 189, 248, 0.25)',
                borderRadius: '20px',
                padding: '1.75rem',
                boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5), 0 0 30px rgba(56, 189, 248, 0.15)',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem', paddingBottom: '0.85rem', borderBottom: '1px solid rgba(255, 255, 255, 0.08)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                  <Calculator size={20} color="#38bdf8" />
                  <span style={{ fontSize: '0.95rem', fontWeight: 700, color: '#ffffff' }}>Live Billing Engine Simulator</span>
                </div>
                <span style={{ fontSize: '0.7rem', fontWeight: 600, background: 'rgba(56, 189, 248, 0.15)', color: '#38bdf8', padding: '0.2rem 0.6rem', borderRadius: '9999px', border: '1px solid rgba(56, 189, 248, 0.3)' }}>
                  Interactive Demo
                </span>
              </div>

              {/* Vehicle Type Selector Pills */}
              <div style={{ marginBottom: '1.25rem' }}>
                <label style={{ display: 'block', fontSize: '0.75rem', color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.5rem' }}>
                  Vehicle Class Rate Contract
                </label>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.5rem' }}>
                  {[
                    { id: 'CAB', label: 'Cab Sedan', rate: '₹16/km' },
                    { id: 'SUV', label: 'Exec SUV', rate: '₹22/km' },
                    { id: 'BUS', label: 'Traveller', rate: '₹45/km' },
                  ].map((v) => (
                    <button
                      key={v.id}
                      onClick={() => setSimVehicleType(v.id)}
                      style={{
                        padding: '0.5rem 0.25rem',
                        borderRadius: '8px',
                        fontSize: '0.8rem',
                        fontWeight: 600,
                        border: simVehicleType === v.id ? '1px solid #38bdf8' : '1px solid rgba(255, 255, 255, 0.1)',
                        backgroundColor: simVehicleType === v.id ? 'rgba(56, 189, 248, 0.15)' : 'rgba(255, 255, 255, 0.02)',
                        color: simVehicleType === v.id ? '#ffffff' : '#94a3b8',
                        cursor: 'pointer',
                        textAlign: 'center',
                        transition: 'all 0.15s ease',
                      }}
                    >
                      <div>{v.label}</div>
                      <div style={{ fontSize: '0.7rem', opacity: 0.75, marginTop: '2px' }}>{v.rate}</div>
                    </button>
                  ))}
                </div>
              </div>

              {/* Distance Slider */}
              <div style={{ marginBottom: '1.25rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: '#cbd5e1', marginBottom: '0.35rem' }}>
                  <span>Trip Distance</span>
                  <strong style={{ color: '#38bdf8' }}>{simDistance} km</strong>
                </div>
                <input
                  type="range"
                  min="5"
                  max="120"
                  value={simDistance}
                  onChange={(e) => setSimDistance(Number(e.target.value))}
                  style={{ width: '100%', accentColor: '#38bdf8', cursor: 'pointer' }}
                />
              </div>

              {/* Toggles: Night Shift, Waiting, Toll */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1.5rem' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8rem', color: '#cbd5e1', cursor: 'pointer', background: 'rgba(255, 255, 255, 0.03)', padding: '0.5rem 0.75rem', borderRadius: '6px', border: '1px solid rgba(255, 255, 255, 0.06)' }}>
                  <input
                    type="checkbox"
                    checked={simNight}
                    onChange={(e) => setSimNight(e.target.checked)}
                    style={{ accentColor: '#38bdf8' }}
                  />
                  Night Shift Surcharge
                </label>
                <div style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '0.5rem 0.75rem', borderRadius: '6px', border: '1px solid rgba(255, 255, 255, 0.06)' }}>
                  <div style={{ fontSize: '0.7rem', color: '#94a3b8' }}>Toll Pass-Through</div>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#ffffff' }}>₹{simToll}</div>
                </div>
              </div>

              {/* Real-time Calculation Result Panel */}
              <div style={{ background: '#090d16', border: '1px solid rgba(56, 189, 248, 0.3)', borderRadius: '12px', padding: '1.15rem' }}>
                <div style={{ fontSize: '0.75rem', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.5rem' }}>
                  Calculated Trip Invoice Total
                </div>
                <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                  <span style={{ fontSize: '2rem', fontWeight: 800, color: '#38bdf8', letterSpacing: '-0.02em' }}>
                    ₹{totalCost.toLocaleString('en-IN')}
                  </span>
                  <span style={{ fontSize: '0.75rem', color: '#4ade80', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                    <CheckCircle2 size={14} /> Audit Validated
                  </span>
                </div>

                <div style={{ fontSize: '0.75rem', color: '#94a3b8', borderTop: '1px solid rgba(255, 255, 255, 0.08)', paddingTop: '0.65rem', display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>Base Distance ({simDistance} km × ₹{ratePerKm}):</span>
                    <strong style={{ color: '#e2e8f0' }}>₹{baseCharge}</strong>
                  </div>
                  {simNight && (
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>Night Shift Flat Surcharge:</span>
                      <strong style={{ color: '#e2e8f0' }}>₹{nightCharge}</strong>
                    </div>
                  )}
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>Pass-Through Toll Receipt:</span>
                    <strong style={{ color: '#e2e8f0' }}>₹{simToll}</strong>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ── 3. INTERACTIVE FEATURE EXPLORER ────────────────────────────────── */}
      <section id="features" style={{ padding: '5rem 1.5rem', backgroundColor: '#0f172a', borderTop: '1px solid rgba(255, 255, 255, 0.05)' }}>
        <div style={{ maxWidth: 1280, margin: '0 auto' }}>
          <div style={{ textAlign: 'center', maxWidth: 640, margin: '0 auto 3rem auto' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: '#38bdf8', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Comprehensive Capabilities</span>
            <h2 style={{ fontSize: '2.25rem', fontWeight: 800, color: '#ffffff', marginTop: '0.35rem', marginBottom: '0.75rem' }}>
              Everything you need to run fleet operations
            </h2>
            <p style={{ fontSize: '1rem', color: '#94a3b8' }}>
              Explore the core components designed specifically for corporate transport fleet managers and finance teams.
            </p>
          </div>

          {/* Feature Selector Tabs */}
          <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '2.5rem' }}>
            {[
              { id: 'billing', label: 'Automated Billing Engine', icon: Receipt },
              { id: 'contracts', label: 'Rate Contracts & Slabs', icon: FileText },
              { id: 'split', label: 'Fair Cost Split', icon: PieChart },
              { id: 'fraud', label: 'Fraud Detection', icon: ShieldCheck },
              { id: 'fleet', label: 'Fleet Management', icon: Car },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  padding: '0.65rem 1.15rem',
                  borderRadius: '9999px',
                  fontSize: '0.875rem',
                  fontWeight: 600,
                  border: activeTab === tab.id ? '1px solid #38bdf8' : '1px solid rgba(255, 255, 255, 0.1)',
                  backgroundColor: activeTab === tab.id ? 'rgba(56, 189, 248, 0.15)' : 'rgba(255, 255, 255, 0.02)',
                  color: activeTab === tab.id ? '#ffffff' : '#94a3b8',
                  cursor: 'pointer',
                  transition: 'all 0.15s ease',
                }}
              >
                <tab.icon size={16} /> {tab.label}
              </button>
            ))}
          </div>

          {/* Active Tab Card Content */}
          <div
            style={{
              background: 'rgba(15, 23, 42, 0.8)',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              borderRadius: '16px',
              padding: '2.5rem',
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
              gap: '2.5rem',
              alignItems: 'center',
            }}
          >
            {activeTab === 'billing' && (
              <>
                <div>
                  <div style={{ color: '#38bdf8', fontWeight: 700, fontSize: '0.85rem', marginBottom: '0.5rem' }}>01 / AUTOMATED BILLING</div>
                  <h3 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#ffffff', marginBottom: '1rem' }}>
                    Contract-based automated billing runs
                  </h3>
                  <p style={{ fontSize: '0.95rem', color: '#94a3b8', lineHeight: 1.6, marginBottom: '1.5rem' }}>
                    Execute monthly billing runs with one click. FleetFlow loads eligible trips for the billing month, resolves active contract versions, calculates tiered slab rates, and outputs itemized tax invoices automatically.
                  </p>
                  <ul style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem', listStyle: 'none' }}>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Idempotent execution prevents duplicate billing</li>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Real-time calculation previews without database writes</li>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Automatic tax invoice generation with printable breakdown</li>
                  </ul>
                </div>
                <div style={{ background: '#090d16', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(255, 255, 255, 0.1)' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.85rem' }}>Monthly Billing Run Execution</div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
                    <div style={{ background: '#1e293b', padding: '0.75rem 1rem', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                      <span>Run #1028 (Vehicle KA-01-AB-1234)</span>
                      <span style={{ color: '#4ade80', fontWeight: 700 }}>COMPLETED</span>
                    </div>
                    <div style={{ background: '#1e293b', padding: '0.75rem 1rem', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                      <span>Total Eligible Trips Processed</span>
                      <strong style={{ color: '#ffffff' }}>42 Trips</strong>
                    </div>
                    <div style={{ background: '#1e293b', padding: '0.75rem 1rem', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                      <span>Reconciled Subtotal</span>
                      <strong style={{ color: '#38bdf8' }}>₹1,42,850.00</strong>
                    </div>
                  </div>
                </div>
              </>
            )}

            {activeTab === 'contracts' && (
              <>
                <div>
                  <div style={{ color: '#38bdf8', fontWeight: 700, fontSize: '0.85rem', marginBottom: '0.5rem' }}>02 / SMART CONTRACTS</div>
                  <h3 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#ffffff', marginBottom: '1rem' }}>
                    Version-controlled rate agreements
                  </h3>
                  <p style={{ fontSize: '0.95rem', color: '#94a3b8', lineHeight: 1.6, marginBottom: '1.5rem' }}>
                    Maintain historical contract versions with precise effective dates. Rate changes mid-month are applied accurately to trip dates without overriding past financial billing.
                  </p>
                  <ul style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem', listStyle: 'none' }}>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Support for Fixed Monthly, Per-KM, and Per-Trip contracts</li>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Tiered pricing slabs (e.g. 0-50 km @ ₹18, 51-100 km @ ₹15)</li>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Fleet-wide vendor contracts or vehicle-specific agreements</li>
                  </ul>
                </div>
                <div style={{ background: '#090d16', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(255, 255, 255, 0.1)' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.85rem' }}>Contract Version History</div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
                    <div style={{ background: '#1e293b', padding: '0.75rem 1rem', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                      <span>Version v2 (Effective Sep 1, 2026)</span>
                      <strong style={{ color: '#38bdf8' }}>Active</strong>
                    </div>
                    <div style={{ background: '#1e293b', padding: '0.75rem 1rem', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                      <span>Version v1 (Historical Jan-Aug)</span>
                      <span style={{ color: '#94a3b8' }}>Archived</span>
                    </div>
                  </div>
                </div>
              </>
            )}

            {activeTab === 'split' && (
              <>
                <div>
                  <div style={{ color: '#38bdf8', fontWeight: 700, fontSize: '0.85rem', marginBottom: '0.5rem' }}>03 / COST ALLOCATION</div>
                  <h3 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#ffffff', marginBottom: '1rem' }}>
                    Largest Remainder fair cost split
                  </h3>
                  <p style={{ fontSize: '0.95rem', color: '#94a3b8', lineHeight: 1.6, marginBottom: '1.5rem' }}>
                    Distribute lump-sum fixed monthly vehicle rental fees fairly across shift trips based on distance ratios, eliminating rounding discrepancies down to the paisa.
                  </p>
                  <ul style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem', listStyle: 'none' }}>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Pro-rata distance-weighted mathematical model</li>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Zero rounding drift guarantee (`SUM(items) == Total`)</li>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Shift cost center audit transparency</li>
                  </ul>
                </div>
                <div style={{ background: '#090d16', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(255, 255, 255, 0.1)' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.85rem' }}>Fixed Fee Shift Allocation</div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
                    <div style={{ background: '#1e293b', padding: '0.75rem 1rem', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                      <span>Shift A (Morning Transport - 45% dist)</span>
                      <strong style={{ color: '#ffffff' }}>₹20,250.00</strong>
                    </div>
                    <div style={{ background: '#1e293b', padding: '0.75rem 1rem', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                      <span>Shift B (Evening Transport - 55% dist)</span>
                      <strong style={{ color: '#ffffff' }}>₹24,750.00</strong>
                    </div>
                  </div>
                </div>
              </>
            )}

            {activeTab === 'fraud' && (
              <>
                <div>
                  <div style={{ color: '#38bdf8', fontWeight: 700, fontSize: '0.85rem', marginBottom: '0.5rem' }}>04 / FRAUD SECURITY</div>
                  <h3 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#ffffff', marginBottom: '1rem' }}>
                    Built-in security anomaly detection
                  </h3>
                  <p style={{ fontSize: '0.95rem', color: '#94a3b8', lineHeight: 1.6, marginBottom: '1.5rem' }}>
                    Detect duplicate trip submissions, impossible speed/distance anomalies, and overlapping driver logs before invoices are generated and paid.
                  </p>
                  <ul style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem', listStyle: 'none' }}>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Duplicate trip ID & timestamp checks</li>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Impossible speed/distance anomaly flag</li>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> One-click manager resolution and dismissal workflow</li>
                  </ul>
                </div>
                <div style={{ background: '#090d16', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(255, 255, 255, 0.1)' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.85rem' }}>Security Alerts Register</div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
                    <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.3)', padding: '0.75rem 1rem', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: '#f87171' }}>
                      <span>IMPOSSIBLE_DISTANCE (Trip #TR-102)</span>
                      <strong style={{ textTransform: 'uppercase' }}>High</strong>
                    </div>
                  </div>
                </div>
              </>
            )}

            {activeTab === 'fleet' && (
              <>
                <div>
                  <div style={{ color: '#38bdf8', fontWeight: 700, fontSize: '0.85rem', marginBottom: '0.5rem' }}>05 / FLEET MANAGEMENT</div>
                  <h3 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#ffffff', marginBottom: '1rem' }}>
                    Centralized vendor & vehicle registry
                  </h3>
                  <p style={{ fontSize: '0.95rem', color: '#94a3b8', lineHeight: 1.6, marginBottom: '1.5rem' }}>
                    Organize your entire corporate fleet database. Link vehicles to transport vendors, track operational status, and manage registration records easily.
                  </p>
                  <ul style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem', listStyle: 'none' }}>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Vendor contact and code registry</li>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Vehicle registration number tracking</li>
                    <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#e2e8f0', fontSize: '0.875rem' }}><CheckCircle2 size={16} color="#38bdf8" /> Vehicle classification (Cab Sedan, SUV, Bus)</li>
                  </ul>
                </div>
                <div style={{ background: '#090d16', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(255, 255, 255, 0.1)' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.85rem' }}>Fleet Registry Status</div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
                    <div style={{ background: '#1e293b', padding: '0.75rem 1rem', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                      <span>Registered Transport Vendors</span>
                      <strong style={{ color: '#ffffff' }}>12 Vendors</strong>
                    </div>
                    <div style={{ background: '#1e293b', padding: '0.75rem 1rem', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                      <span>Active Fleet Vehicles</span>
                      <strong style={{ color: '#38bdf8' }}>128 Vehicles</strong>
                    </div>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </section>

      {/* ── 4. FAIR COST SPLIT TRANSPARENCY SECTION ─────────────────────── */}
      <section id="cost-split" style={{ padding: '5rem 1.5rem', backgroundColor: '#090d16' }}>
        <div style={{ maxWidth: 1280, margin: '0 auto' }}>
          <div style={{ textAlign: 'center', maxWidth: 640, margin: '0 auto 3rem auto' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: '#38bdf8', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Financial Accuracy</span>
            <h2 style={{ fontSize: '2.25rem', fontWeight: 800, color: '#ffffff', marginTop: '0.35rem', marginBottom: '0.75rem' }}>
              Fair Cost Allocation Visualized
            </h2>
            <p style={{ fontSize: '1rem', color: '#94a3b8' }}>
              How fixed monthly vehicle rental costs are distributed transparently across shift cost centers.
            </p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1.5rem' }}>
            <div style={{ background: 'rgba(15, 23, 42, 0.6)', border: '1px solid rgba(255, 255, 255, 0.08)', padding: '1.75rem', borderRadius: '12px' }}>
              <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#38bdf8', marginBottom: '0.5rem' }}>Step 1</div>
              <h4 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.5rem' }}>Lump Sum Monthly Pool</h4>
              <p style={{ fontSize: '0.875rem', color: '#94a3b8', lineHeight: 1.5 }}>
                Contract version specifies a fixed monthly fee (e.g. ₹45,000) for dedicated vehicle availability.
              </p>
            </div>

            <div style={{ background: 'rgba(15, 23, 42, 0.6)', border: '1px solid rgba(255, 255, 255, 0.08)', padding: '1.75rem', borderRadius: '12px' }}>
              <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#38bdf8', marginBottom: '0.5rem' }}>Step 2</div>
              <h4 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.5rem' }}>Distance Ratio Weighting</h4>
              <p style={{ fontSize: '0.875rem', color: '#94a3b8', lineHeight: 1.5 }}>
                Eligible shift trips are weighted based on their individual distance against the total monthly mileage.
              </p>
            </div>

            <div style={{ background: 'rgba(15, 23, 42, 0.6)', border: '1px solid rgba(255, 255, 255, 0.08)', padding: '1.75rem', borderRadius: '12px' }}>
              <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#38bdf8', marginBottom: '0.5rem' }}>Step 3</div>
              <h4 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.5rem' }}>Largest Remainder Split</h4>
              <p style={{ fontSize: '0.875rem', color: '#94a3b8', lineHeight: 1.5 }}>
                Fractional paisa values are allocated by largest remainder ranking, guaranteeing 100% total sum match.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* ── 5. SECURITY & ROLE-BASED ACCESS SECTION ──────────────────────── */}
      <section id="security" style={{ padding: '5rem 1.5rem', backgroundColor: '#0f172a', borderTop: '1px solid rgba(255, 255, 255, 0.05)' }}>
        <div style={{ maxWidth: 1280, margin: '0 auto' }}>
          <div style={{ textAlign: 'center', maxWidth: 640, margin: '0 auto 3.5rem auto' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: '#38bdf8', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Enterprise Security</span>
            <h2 style={{ fontSize: '2.25rem', fontWeight: 800, color: '#ffffff', marginTop: '0.35rem', marginBottom: '0.75rem' }}>
              Built for Controlled Enterprise Access
            </h2>
            <p style={{ fontSize: '1rem', color: '#94a3b8' }}>
              Role-based authorization ensures every user sees only the operations relevant to their responsibilities.
            </p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
            <div style={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', padding: '1.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                <span style={{ fontSize: '1.2rem', fontWeight: 800, color: '#ffffff' }}>ADMIN</span>
                <span style={{ fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'rgba(56, 189, 248, 0.15)', color: '#38bdf8', padding: '0.25rem 0.65rem', borderRadius: '9999px', border: '1px solid rgba(56, 189, 248, 0.3)' }}>
                  Full Admin Access
                </span>
              </div>
              <p style={{ fontSize: '0.875rem', color: '#cbd5e1', lineHeight: 1.6 }}>
                Full system access: register vendors, add vehicles, manage rate contracts, version effective dates, execute billing runs, and resolve fraud alerts.
              </p>
            </div>

            <div style={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', padding: '1.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                <span style={{ fontSize: '1.2rem', fontWeight: 800, color: '#ffffff' }}>HR</span>
                <span style={{ fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'rgba(250, 204, 21, 0.15)', color: '#facc15', padding: '0.25rem 0.65rem', borderRadius: '9999px', border: '1px solid rgba(250, 204, 21, 0.3)' }}>
                  Ops & Billing
                </span>
              </div>
              <p style={{ fontSize: '0.875rem', color: '#cbd5e1', lineHeight: 1.6 }}>
                Operational management: review trip activity, execute monthly billing runs, allocate fixed monthly rental fees, and inspect tax invoices.
              </p>
            </div>

            <div style={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', padding: '1.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                <span style={{ fontSize: '1.2rem', fontWeight: 800, color: '#ffffff' }}>EMPLOYEE</span>
                <span style={{ fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'rgba(74, 222, 128, 0.15)', color: '#4ade80', padding: '0.25rem 0.65rem', borderRadius: '9999px', border: '1px solid rgba(74, 222, 128, 0.3)' }}>
                  Operational Access
                </span>
              </div>
              <p style={{ fontSize: '0.875rem', color: '#cbd5e1', lineHeight: 1.6 }}>
                Operational view: view assigned fleet vehicles, log completed trip journeys (distance, duty hours, tolls), and track personal trip history.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* ── 6. FAQ ACCORDION SECTION ────────────────────────────────────── */}
      <section id="faq" style={{ padding: '5rem 1.5rem', backgroundColor: '#090d16' }}>
        <div style={{ maxWidth: 840, margin: '0 auto' }}>
          <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: '#38bdf8', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Got Questions?</span>
            <h2 style={{ fontSize: '2.25rem', fontWeight: 800, color: '#ffffff', marginTop: '0.35rem' }}>
              Frequently Asked Questions
            </h2>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {[
              {
                q: 'How does contract versioning handle mid-month rate changes?',
                a: 'FleetFlow resolves the exact active contract version based on each individual trip date. If a rate contract changes mid-month, trips prior to the date use version v1, while subsequent trips use version v2, preserving complete audit accuracy.'
              },
              {
                q: 'How are fixed monthly vehicle fees split across trips?',
                a: 'Fixed monthly fees are allocated using the Largest Remainder Method. Each trip is weighted by its distance ratio relative to total monthly mileage, ensuring zero rounding loss and exact subtotal matching.'
              },
              {
                q: 'What happens if duplicate trips or impossible distances occur?',
                a: 'Built-in security checks immediately flag duplicate trip IDs, overlapping driver logs, or impossible distance/duty ratios as OPEN fraud alerts for manager review before invoices are finalized.'
              },
              {
                q: 'Can I test the platform with different user roles?',
                a: 'Yes! You can register a new account or log in with your credentials to test ADMIN, HR, or EMPLOYEE permissions.'
              }
            ].map((faq, idx) => (
              <div
                key={idx}
                style={{
                  background: 'rgba(15, 23, 42, 0.7)',
                  border: '1px solid rgba(255, 255, 255, 0.08)',
                  borderRadius: '12px',
                  overflow: 'hidden',
                }}
              >
                <button
                  onClick={() => toggleFaq(idx)}
                  style={{
                    width: '100%',
                    padding: '1.25rem 1.5rem',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    background: 'none',
                    border: 'none',
                    color: '#ffffff',
                    fontSize: '1rem',
                    fontWeight: 600,
                    textAlign: 'left',
                    cursor: 'pointer',
                  }}
                >
                  <span>{faq.q}</span>
                  {openFaq === idx ? <ChevronUp size={20} color="#38bdf8" /> : <ChevronDown size={20} color="#94a3b8" />}
                </button>
                {openFaq === idx && (
                  <div style={{ padding: '0 1.5rem 1.25rem 1.5rem', color: '#94a3b8', fontSize: '0.9rem', lineHeight: 1.6, borderTop: '1px solid rgba(255, 255, 255, 0.05)', paddingTop: '1rem' }}>
                    {faq.a}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── 7. HIGH-IMPACT FINAL CTA & FOOTER ────────────────────────────── */}
      <section style={{ padding: '6rem 1.5rem', backgroundColor: '#0f172a', borderTop: '1px solid rgba(255, 255, 255, 0.05)', textAlign: 'center' }}>
        <div style={{ maxWidth: 720, margin: '0 auto' }}>
          <div
            style={{
              background: 'linear-gradient(135deg, rgba(37, 99, 235, 0.2) 0%, rgba(56, 189, 248, 0.1) 100%)',
              border: '1px solid rgba(56, 189, 248, 0.3)',
              borderRadius: '24px',
              padding: '3.5rem 2rem',
              boxShadow: '0 0 50px rgba(37, 99, 235, 0.25)',
            }}
          >
            <h2 style={{ fontSize: '2.5rem', fontWeight: 800, color: '#ffffff', marginBottom: '1rem', letterSpacing: '-0.02em' }}>
              Bring your fleet operations into one place.
            </h2>
            <p style={{ fontSize: '1.1rem', color: '#cbd5e1', marginBottom: '2.25rem', lineHeight: 1.6 }}>
              Automate monthly billing runs, eliminate contract pricing disputes, and audit cost splits effortlessly.
            </p>
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
              <Link
                to="/register"
                style={{
                  background: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)',
                  color: '#ffffff',
                  padding: '0.9rem 2rem',
                  borderRadius: '10px',
                  fontWeight: 700,
                  fontSize: '1.05rem',
                  textDecoration: 'none',
                  boxShadow: '0 0 25px rgba(37, 99, 235, 0.5)',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                }}
              >
                Start Managing Your Fleet <ArrowRight size={18} />
              </Link>
              <Link
                to="/login"
                style={{
                  backgroundColor: 'rgba(255, 255, 255, 0.08)',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  color: '#ffffff',
                  padding: '0.9rem 2rem',
                  borderRadius: '10px',
                  fontWeight: 600,
                  fontSize: '1.05rem',
                  textDecoration: 'none',
                }}
              >
                Sign In
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer style={{ borderTop: '1px solid rgba(255, 255, 255, 0.08)', padding: '2.5rem 1.5rem', backgroundColor: '#090d16', color: '#64748b' }}>
        <div style={{ maxWidth: 1280, margin: '0 auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ width: 28, height: 28, borderRadius: '6px', background: 'linear-gradient(135deg, #38bdf8, #2563eb)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#ffffff' }}>
              <Layers size={16} />
            </div>
            <div>
              <span style={{ fontSize: '1.05rem', fontWeight: 700, color: '#ffffff' }}>FleetFlow</span>
              <span style={{ fontSize: '0.75rem', color: '#94a3b8', display: 'block' }}>Fleet Billing & Operations</span>
            </div>
          </div>
          <div style={{ fontSize: '0.85rem', color: '#64748b' }}>
            © 2026 FleetFlow. All rights reserved. Built for Enterprise Fleet Management.
          </div>
        </div>
      </footer>
    </div>
  );
};
