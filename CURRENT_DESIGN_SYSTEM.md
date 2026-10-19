# Current Design System Specification
**Application**: Personal Finance Tracker (Angular 18 + Spring Boot 3.3.2)  
**Document**: Technical Design System Audit & Token Catalog  
**Status**: Exact values extracted directly from `frontend/src/styles.css` and component stylesheets.

---

## 1. Color Palette & Design Tokens

The application implements a dual-theme architecture (Light Mode default and an OLED/AMOLED-tuned Dark Mode triggered by `data-theme="dark"` or `.dark-theme`).

### 1.1 Neutral Palette (Slate Scale)

| Token Name | Light Mode Value (Actual) | Dark Mode Value (Actual) | Primary Usage |
| :--- | :--- | :--- | :--- |
| `--slate-50` | `#f8fafc` | `#1e293b` | Surface zebra rows, card footers, subtle backgrounds |
| `--slate-100` | `#f1f5f9` | `#1a2436` | Pill hover states, neutral badges, secondary buttons |
| `--slate-200` | `#e2e8f0` | `#26334a` | Card borders, table dividers, input borders |
| `--slate-300` | `#cbd5e1` | `#3b4d66` | Stronger borders, focus outlines, button outlines |
| `--slate-400` | `#94a3b8` | `#64748b` | Placeholder text, close icons, inactive icons |
| `--slate-500` | `#64748b` | `#94a3b8` | Subtitles, helper text, empty state descriptions |
| `--slate-600` | `#475569` | `#cbd5e1` | Secondary text, badge text |
| `--slate-700` | `#334155` | `#e2e8f0` | Form labels, table cell text |
| `--slate-800` | `#1e293b` | `#f1f5f9` | Subheadings, card titles |
| `--slate-900` | `#0f172a` | `#f8fafc` | Primary titles, bold amounts, strong text |

### 1.2 Brand & Semantic Financial Status Colors

| Token Name | Light Mode Value (Actual) | Dark Mode Value (Actual) | Semantic Financial Meaning |
| :--- | :--- | :--- | :--- |
| `--primary` | `#0f766e` (Deep Emerald Teal) | `#14b8a6` (Radiant Cyan Teal) | Brand actions, primary CTA, active navigation |
| `--primary-hover` | `#115e59` | `#2dd4bf` | Button hover and interactive focus |
| `--primary-active` | `#134e4a` | `#0d9488` | Active tab, pressed buttons |
| `--primary-subtle` | `#f0fdfa` | `rgba(20, 184, 166, 0.16)` | Selected state background, category badges |
| `--primary-border` | `#99f6e4` | `rgba(20, 184, 166, 0.35)` | Outline button border, badge borders |
| `--income` | `#059669` (Emerald Green) | `#34d399` (Mint Green) | Positive cash flow, deposit transactions, savings |
| `--income-hover` | `#047857` | `#10b981` | Positive action hover |
| `--income-subtle` | `#ecfdf5` | `rgba(16, 185, 129, 0.16)` | Income badge background, positive progress bar |
| `--income-border` | `#a7f3d0` | `rgba(16, 185, 129, 0.35)` | Income card & badge border |
| `--expense` | `#dc2626` (Crimson Red) | `#f87171` (Coral Red) | Outflows, debt obligations, overdue bills, deficits |
| `--expense-hover` | `#b91c1c` | `#ef4444` | Destructive action hover |
| `--expense-subtle` | `#fef2f2` | `rgba(239, 68, 68, 0.16)` | Expense badge background, alert banner fill |
| `--expense-border` | `#fecaca` | `rgba(239, 68, 68, 0.35)` | Expense badge & input invalid border |
| `--warning` | `#d97706` (Amber Orange) | `#fbbf24` (Golden Amber) | Due-soon alerts, 80%+ budget utilization |
| `--warning-subtle` | `#fffbeb` | `rgba(245, 158, 11, 0.16)` | Warning alert background |
| `--warning-border` | `#fde68a` | `rgba(245, 158, 11, 0.35)` | Warning border |
| `--info` | `#0284c7` (Sky Blue) | `#38bdf8` (Bright Azure) | Refunds, Gemini AI badges, informational alerts |
| `--info-subtle` | `#f0f9ff` | `rgba(14, 165, 233, 0.16)` | Info badge & toast fill |
| `--info-border` | `#bae6fd` | `rgba(14, 165, 233, 0.35)` | Info border |

### 1.3 Canvas & Surfaces

| Token Name | Light Mode Value (Actual) | Dark Mode Value (Actual) | Description |
| :--- | :--- | :--- | :--- |
| `--bg-canvas` | `#fbfbfa` (Warm Natural Bone) | `#090d16` (OLED Deep Midnight) | Full page background |
| `--bg-surface` | `#ffffff` (Pure White) | `#111827` (Midnight Slate) | Cards, modals, drawers, form controls |
| `--bg-subtle` | `#f4f5f4` | `#1f293d` | Inset metric cards, zebra fills |
| `--bg-muted` | `#e4e7e5` | `#2b3952` | Disabled controls, divider lines |
| `--border-color` | `#e4e7e5` | `#1f293d` | Universal border token |
| `--text-main` | `#18181b` (Near Black) | `#f8fafc` (Pure Slate White) | Primary text color |
| `--text-muted` | `#71717a` (Neutral Gray) | `#94a3b8` (Muted Slate) | Secondary and placeholder text |

---

## 2. Typography & Scale

The system utilizes Google Font **Inter** as its primary sans-serif typeface, paired with system monospace fonts for numeric alignment and codes.

### 2.1 Font Families
- **Primary Body & Display**: `'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif` (`--font-sans`).
- **Monospace & Code**: `ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace` (`--font-mono`).

### 2.2 Type Scale (Actual Tokens in `styles.css`)

| Token | CSS Rem | Pixel Equivalent | Line Height | Usage |
| :--- | :--- | :--- | :--- | :--- |
| `--fs-xs` | `0.75rem` | 12px | 1.35 | Badges, timestamps, form error text, micro-labels |
| `--fs-sm` | `0.8125rem` | 13px | 1.4 | Table cells, subtitles, form labels, small buttons |
| `--fs-base`| `0.875rem` | 14px | 1.5 | Standard body text, inputs, transaction titles |
| `--fs-md` | `0.9375rem` | 15px | 1.4 | Card titles, modal headers, mobile transaction amounts |
| `--fs-lg` | `1.125rem` | 18px | 1.35 | Sub-section headers, metric highlights |
| `--fs-xl` | `1.25rem` | 20px | 1.3 | KPI card values, modal titles, secondary page titles |
| `--fs-2xl` | `1.5rem` | 24px | 1.25 | Primary page titles (`.page-title`, `h1`), hero amounts |
| `--fs-3xl` | `1.875rem` | 30px | 1.2 | Prominent financial totals, annual net worth displays |

### 2.3 Font Weights
- `400` / Regular (`.fw-normal`): Body copy, table secondary cells.
- `500` / Medium (`.fw-medium`): Form labels, button text, table primary cells.
- `600` / Semibold (`.fw-semibold`, `.font-weight-600`): Section headers, card titles, badges, merchant names.
- `700` / Bold (`.fw-bold`, `.font-weight-bold`): Page titles, financial sums, KPI amounts.

### 2.4 Financial Number Alignment (Tabular Figures)
- `.tabular-nums`, `.amount`, `.currency`, `table td.text-end`, `.stat-value`:
  - `font-variant-numeric: tabular-nums;`
  - `font-feature-settings: "tnum" 1, "cv05" 1;`
  - Ensures monospaced width across all numeric characters (0-9) so stacked currency numbers align vertically at the decimal point without jitter.

---

## 3. Spacing System (4px / 8px Grid)

| Token | Dimension (Actual) | Implementation Context |
| :--- | :--- | :--- |
| `--space-1` | `4px` | Micro-gap inside badges, icon-to-text spacing, input borders |
| `--space-2` | `8px` | Gap between action buttons, grid gutter half, list item gap |
| `--space-3` | `12px` | Card header padding, modal action gap, alert padding |
| `--space-4` | `16px` | Standard card body padding on mobile, form-group bottom margin |
| `--space-5` | `20px` | Card bottom margin, desktop modal body padding |
| `--space-6` | `24px` | Page container padding on desktop, section separation |
| `--space-8` | `32px` | Major dashboard grid separation |

---

## 4. Border Radii & Elevation

### 4.1 Border Radii
- `--radius-xs`: `4px` (Micro badges, code tags)
- `--radius-sm`: `6px` (Small buttons `.btn-sm`, date selector arrows, close buttons)
- `--radius-md`: `8px` (Inputs, standard buttons `.btn`, alerts, table containers)
- `--radius-lg`: `12px` (Cards `.card`, mobile transaction items, drop zones)
- `--radius-xl`: `16px` (Modal cards `.modal-card`, slide-over drawers)
- `--radius-pill`: `9999px` (Progress bar tracks, rounded avatars, circular FAB)

### 4.2 Elevation Shadows
- `--shadow-xs`: `0 1px 2px 0 rgba(0, 0, 0, 0.04)` (Cards, standard buttons)
- `--shadow-sm`: `0 1px 3px 0 rgba(0, 0, 0, 0.05), 0 1px 2px -1px rgba(0, 0, 0, 0.03)` (Card hover, active tabs)
- `--shadow-md`: `0 4px 6px -1px rgba(0, 0, 0, 0.06), 0 2px 4px -2px rgba(0, 0, 0, 0.03)` (Dropdown menus, popovers)
- `--shadow-lg`: `0 10px 15px -3px rgba(0, 0, 0, 0.08), 0 4px 6px -4px rgba(0, 0, 0, 0.03)` (Modal cards, toast banners)

*(In Dark Mode, shadow opacities scale from `rgba(0,0,0, 0.35)` to `0.6` to remain visible against deep slate surfaces).*

---

## 5. Components & UI Elements

### 5.1 Buttons
- **Base Button (`.btn`)**: `padding: 8px 14px`, `font-size: 13px`, `font-weight: 500`, `border-radius: 8px`, `transition: all 0.15s ease`.
- **Button Variants**:
  - `.btn-primary`: Solid fill (`var(--primary)`), white text, subtle primary glow shadow.
  - `.btn-secondary`: Light surface fill (`var(--bg-surface)`), border `var(--slate-300)`, dark text.
  - `.btn-outline-secondary`: Transparent background, subtle border, gray text.
  - `.btn-danger`: Solid red (`var(--expense)`), white text.
  - `.btn-success`: Solid green (`var(--income)`), white text.
  - `.btn-sm`: Compact `padding: 5px 10px`, `font-size: 12px`, `border-radius: 6px`.
  - `.btn-close`: Transparent icon button with `✕` CSS pseudo-element (`.btn-close:empty::before`).

### 5.2 Form Inputs & Controls
- **Inputs & Selects (`.form-control`, `.form-select`)**:
  - `padding: 8px 12px`, `font-size: 14px`, `border: 1px solid var(--slate-300)`, `border-radius: 8px`, `background-color: var(--bg-surface)`.
  - **Focus State**: `border-color: var(--primary)`, `box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12)`.
  - **Invalid State (`.is-invalid`)**: `border-color: var(--expense) !important`, `box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.12)`.
- **Prefix / Suffix Wrappers (`.input-prefix-wrapper`, `.input-group`)**:
  - Currency symbol `₹` or national code `+91` anchored at left with tabular padding.
- **Segmented Control (`.segmented-control`)**:
  - 3-way toggle pill (`Expense` / `Income` / `Refund`) with smooth active pill slide.

### 5.3 Badges & Status Pills
- **Base Badge (`.badge`)**: `padding: 2px 7px`, `font-size: 12px`, `font-weight: 600`, `border-radius: 6px`, `line-height: 1.35`.
- **Semantic Badges**:
  - `.badge-income`: Light green background with dark green text and border.
  - `.badge-expense`: Light red background with dark red text and border.
  - `.badge-warning`: Light amber background with dark amber text and border.
  - `.badge-info`: Light sky-blue background with dark sky-blue text and border.
  - `.badge-neutral`: Slate gray background.

### 5.4 Data Tables & Data Grids
- **Desktop Table (`.table`)**: Full-width, collapsed borders, uppercase headers (`font-size: 12px`, `letter-spacing: 0.04em`), subtle row border dividers, and zebra hover highlight (`var(--slate-50)`).
- **Mobile Transaction Row (`.mobile-tx-item`)**: High-touch card replacing table rows below 768px:
  - 40px circular avatar icon indicating Income, Expense, or Refund.
  - Two-line metadata: Merchant / Title + Category badge and Date.
  - Right-aligned bold tabular amount with explicit sign (`+₹` or `-₹`).

### 5.5 Charts
- Implemented via HTML5 `<canvas>` managed by Chart.js:
  - **Cash Flow Bar Chart**: Vertical bars comparing monthly inflow vs outflow with rounded corners.
  - **Category Doughnut Chart**: Multi-color slice breakdown displaying percentages and top expense allocations.

---

## 6. Iconography

The application uses an inline **SVG Feather/Lucide-style icon system**:
- **ViewBox**: Standardized `0 0 24 24`.
- **Stroke Parameters**: `fill="none"`, `stroke="currentColor"`, `stroke-width="2"` or `2.2`, `stroke-linecap="round"`, `stroke-linejoin="round"`.
- **Standard Sizes**:
  - `14 × 14 px`: Inside compact buttons (`.btn-sm`).
  - `16–18 × 18 px`: Inside navigation bars, toolbar icons, and badges.
  - `20–24 × 24 px`: Inside empty states, modal icon wrappers, and mobile bottom tab icons.
- **Emoji Utility Exceptions**:
  - `📇` (Contacts Book & Addon)
  - `✈️` / `🏠` / `🏢` / `👥` (Splitwise group type categorization)
  - `⏰` / `⚠️` / `💸` / `🔔` (Notification center alert category classification)

---

## 7. Responsive Breakpoints

| Breakpoint Variable / Media Query | Screen Width | Targeted Devices & Behavior |
| :--- | :--- | :--- |
| `max-width: 359px` | `< 360px` | Ultra-compact mobile (SE, small Androids); container padding reduces to 10px |
| `max-width: 640px` | `< 640px` | Standard mobile viewports; container padding reduces to 16px |
| `max-width: 767px` | `< 768px` | Mobile breakpoint: hides desktop header and tables; activates bottom nav, FAB, and mobile card lists |
| `min-width: 768px` | `768px - 991px`| Tablet / Small Desktop: activates desktop header, data tables, and 2-column grids |
| `min-width: 992px` | `992px - 1199px`| Standard Desktop: 3-column grids, dual planning split |
| `max-width: 1200px` | `1200px+` | Max page container width (`margin: 0 auto; max-width: 1200px`) |

---

## 8. Verification Distinctions

- **Directly Verified from Code**:
  - All color hex codes, slate scales, and dark mode overrides (`styles.css` lines 5–156).
  - All font sizes, rem values, and line heights (`styles.css` lines 59–67).
  - All breakpoint values (`359px`, `640px`, `767px`, `768px`, `992px`).
  - Exact HTML button and form control class definitions across all 15 components.
- **Inferred / Estimated**:
  - The color palette is based on Tailwind/Slate design token naming conventions, but is implemented entirely in custom vanilla CSS without the Tailwind utility build engine.
  - Touch target heights on mobile average 40–48px for primary buttons, with some secondary action icons measuring 32px (which will be addressed in the UX audit).
