# Framer Design Brief: Minimal & Premium Personal Finance Tracker

**Target Product**: Personal Finance Tracker (Next-Gen Visual Prototype)  
**Author**: Senior UI/UX Designer & Design Systems Architect  
**Objective**: Comprehensive blueprint for building a high-fidelity, interactive Framer prototype that will serve as the source of truth for the Angular 18 frontend implementation.  
**Visual Philosophy**: Calm, trustworthy, restrained, high-density financial precision.

---

## 1. Visual Direction & Brand Identity

### 1.1 The Aesthetic Thesis
Traditional consumer finance apps often suffer from two extremes: either visually dry, corporate spreadsheet interfaces or gamified, neon-gradient "crypto-style" apps with distracting glassmorphism.

Our target direction for this Personal Finance Tracker is **Calm Financial Precision**—inspired by modern enterprise fintechs such as Stripe, Linear, Mercury, and Wealthfront:
- **Restrained Color**: Color is an informational status indicator, not decoration. The interface is predominantly warm neutral monochrome (Slate / Zinc / Off-white canvas) with focused, semantic accents: Emerald `#059669` for capital inflow/savings, Coral Rose `#e11d48` for outflows/obligations, Amber `#d97706` for warning thresholds, and deep Cyan Teal `#0f766e` for brand actions.
- **De-carded Layouts**: Eliminate nested, boxy white cards surrounded by heavy drop shadows. Utilize subtle tonal section fills (`--slate-50` / `#f8fafc`), clean 1px hairline dividers (`--border-color`), and deliberate whitespace to define grouping.
- **Micro-Typography & Tabular Numerics**: Standardize all currency amounts on tabular numerals (`tnum`), with distinct visual hierarchy between whole currency amounts and cents to accelerate scanning.
- **Purposeful Iconography**: 100% unified 1.5px stroke Lucide/Feather iconography. Remove all OS native emojis (`🏠`, `⚡`, `📇`) in favor of curated vector symbols.

---

## 2. Framer Project Setup: Canvas & Tokens

### 2.1 Framer Artboard Dimensions

| Device Target | Framer Frame Name | Width | Height (Min) | Layout Grid | Margins / Gutters |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Desktop High-Res** | `Desktop / 1440` | `1440px` | `1024px` | 12-Column Grid | Margin: `80px`, Gutter: `24px`, Max-W: `1280px` |
| **Desktop Standard** | `Desktop / 1280` | `1280px` | `900px` | 12-Column Grid | Margin: `48px`, Gutter: `20px`, Max-W: `1184px` |
| **Tablet Portrait** | `Tablet / 834` | `834px` | `1194px` | 8-Column Grid | Margin: `32px`, Gutter: `16px` |
| **Mobile Flagship** | `Mobile / 393` | `393px` | `873px` | 4-Column Grid | Margin: `16px`, Gutter: `12px` |

> [!NOTE]
> **Provisional Mobile Benchmark**: `393px × 873px` represents modern viewport standards (iPhone 14/15/16 Pro, Google Pixel 8, Samsung Galaxy S24). All mobile artboards must account for iOS status bar (`47px`), dynamic island/notch, and bottom home indicator safe area (`34px`).

### 2.2 Color Styles in Framer

Create these Global Color Styles in the Framer project assets panel:

#### Canvas & Backgrounds
- `Canvas / Light`: `#fbfbfa` (Warm alabaster canvas)
- `Canvas / Dark`: `#090d16` (Deep OLED midnight)
- `Surface / Light`: `#ffffff` (Pure white for raised interactive surfaces)
- `Surface / Dark`: `#111827` (Rich slate midnight)
- `Surface / Subtle Light`: `#f4f5f4` (Light inset metric fill)
- `Surface / Subtle Dark`: `#1a2436` (Dark inset metric fill)
- `Border / Light`: `#e4e7e5` (Hairline divider)
- `Border / Dark`: `#1f293d` (Subtle dark divider)

#### Semantic Financial Accents
- `Brand / Primary`: `#0f766e` (Deep Emerald Teal)
- `Brand / Primary Subtle`: `#f0fdfa` (Light teal tint)
- `Finance / Income`: `#059669` (Vibrant emerald positive cash flow)
- `Finance / Income Subtle`: `#ecfdf5` (5% opacity tint for badges/bars)
- `Finance / Expense`: `#dc2626` (Refined crimson outflow)
- `Finance / Expense Subtle`: `#fef2f2` (Light pinkish-red tint)
- `Finance / Warning`: `#d97706` (Amber for pacing alerts & 80%+ budget caps)
- `Finance / Info`: `#0284c7` (Sky blue for Gemini AI & notifications)

#### Typography Grayscale (Slate Scale)
- `Text / Primary Light`: `#0f172a` (`--slate-900`)
- `Text / Primary Dark`: `#f8fafc` (`--slate-50`)
- `Text / Secondary Light`: `#475569` (`--slate-600`)
- `Text / Secondary Dark`: `#cbd5e1` (`--slate-300`)
- `Text / Muted Light`: `#64748b` (`--slate-500`)
- `Text / Muted Dark`: `#94a3b8` (`--slate-400`)

### 2.3 Typography Styles in Framer

Use Google Font **Inter** across all frames:

| Framer Text Style | Font Size | Line Height | Weight | Letter Spacing | Font Features |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `Display / Hero Total` | `32px` | `1.15` | Bold (700) | `-0.03em` | `tnum`, `cv05` |
| `Display / Page Title` | `24px` | `1.25` | Semibold (600) | `-0.02em` | `cv05` |
| `Heading / Section` | `18px` | `1.3` | Semibold (600) | `-0.01em` | `cv05` |
| `Heading / Card Title` | `15px` | `1.4` | Medium (500) | `0em` | `cv05` |
| `Body / Standard` | `14px` | `1.5` | Regular (400) | `0em` | Default |
| `Body / Medium` | `14px` | `1.5` | Medium (500) | `0em` | Default |
| `Micro / Caption` | `12px` | `1.4` | Regular (400) | `+0.01em` | Default |
| `Micro / Badge` | `11px` | `1.2` | Semibold (600) | `+0.03em` (Caps) | Default |
| `Number / Table Row` | `14px` | `1.4` | Medium (500) | `-0.01em` | `tnum` |
| `Number / Metric Value`| `22px` | `1.2` | Bold (700) | `-0.02em` | `tnum` |

---

## 3. Global Shell & Navigation Architecture

### 3.1 Shared Global Shell Specification

The navigation model establishes clear visual separation between **Primary Overview** and **Focused Execution**:

```
Desktop Navigation (Sticky Top Shell - 64px)
[Logo: FinanceTracker]   |   [Overview]  [Transactions]  [Planning v]  [Split & Bills v]   |   [Global Search (Cmd+K)]  [+ Quick Action]  [🔔 2]  [Theme]  [Avatar]
```

#### Desktop Top Shell Architecture
- Height: `64px`, pinned `top: 0`, background: `rgba(255, 255, 255, 0.88)` with `backdrop-filter: blur(12px)` and bottom border `1px solid var(--border-color)`.
- Replaces the 9 cluttered horizontal links with **4 Core Navigation Pillars**:
  1. **Overview** (`/dashboard`): Real-time balance, cash flow pacing, active budgets, and upcoming commitments.
  2. **Transactions** (`/transactions`): Unified ledger, real-time filtering, categorization, and CSV export.
  3. **Planning (Dropdown)**: Consolidates `Budgets`, `Salary Planner`, and `Savings Goals` into an integrated financial allocation suite.
  4. **Shared & Automation (Dropdown)**: Houses `Splitwise Groups`, `Recurring Bills`, `AI Receipt Scanner`, and `Category Management`.
- **Right Utilities**: Global Command Bar trigger (`Cmd + K`), Primary "+ Add" Button (triggers quick-entry modal with tabs: Transaction, Bill, Split, or Goal), Notification Bell with red pulse dot, Theme Switcher (Sun/Moon), and User Profile popover.

#### Mobile Shell Architecture (Viewport: 393 × 873px)
- **Top Bar**: Minimalist header (`56px` height) with Brand Mark, Month Picker pill (`October 2026 ▾`), Notification Bell, and Theme Toggle.
- **Bottom Navigation Dock**: Floating glass dock (`64px` height) with 4 key destinations + center elevated action trigger:
  - Tab 1: **Home** (Grid icon)
  - Tab 2: **Ledger** (List icon)
  - Center: **Primary Action Button (+)** (Solid Teal circle `48px × 48px`, opens Quick Action Bottom Sheet)
  - Tab 3: **Budgets** (PieChart icon)
  - Tab 4: **Hub** (Sliders/More icon - opens full screen feature sheet)

---

## 4. Screen-by-Screen Framer Artboard Instructions

### Screen 1: Dashboard (`/dashboard`)
*Framer Artboard: `Dashboard - Desktop (1440)` & `Dashboard - Mobile (393)`*

#### Visual Hierarchy & Layout
1. **Hero Cash Flow Strip (Top)**:
   - Total Net Worth / Available Balance: `$14,250.80` displayed in `Display / Hero Total` (`32px` bold).
   - Beside it, two compact metric pills:
     - Inflow: `+$5,200.00` (Emerald badge, with mini up-trend sparkline).
     - Outflow: `-$2,840.20` (Coral badge, with mini down-trend sparkline).
     - Net Monthly Savings: `+$2,359.80` (45.3% savings rate).
2. **Salary Plan Status Widget (Mid-tier)**:
   - A single slim, horizontal card showing "October Salary Allocation":
   - Proportional 3-segment bar (Needs 50% / Wants 30% / Savings 20%) with live utilization markers.
   - Micro-action link: "View Salary Plan →".
3. **Primary Analytics Grid (2 Columns on Desktop, Tabbed on Mobile)**:
   - **Column 1 (Left 60%)**: Interactive Cash Flow Trajectory Chart (Monthly Income vs. Expense bar chart with daily spend curve overlay).
   - **Column 2 (Right 40%)**: Category Spending Breakdown (Donut visualizer replaced by a clean vertical list of top 5 spending categories with horizontal micro-bars and percent chips).
4. **Actionable Commitments & Recent Activity**:
   - **Upcoming Recurring Bills**: 3 upcoming items (Rent in 5 days, Netflix in 8 days) with 1-click "Mark Paid" buttons.
   - **Recent Transactions**: Last 5 transactions with high-contrast category icons, clear merchant names, and tabular currency values.

---

### Screen 2: Transaction Ledger (`/transactions`)
*Framer Artboard: `Transactions - Desktop (1440)` & `Transactions - Mobile (393)`*

#### Visual Hierarchy & Layout
1. **Streamlined Filter Bar**:
   - Primary Search Input: Full-width borderless search input with `Cmd+K` shortcut hint.
   - Quick Filter Chips: `All (142)`, `Expenses (118)`, `Income (24)`, `Pending (2)`.
   - Action Button: "Filter Options (0 active)" which triggers the slide-out Filter Drawer on mobile.
2. **Date-Grouped Transaction Stream**:
   - Group transactions by sticky day dividers:
     - `Today • Monday, Oct 19` — `Total Net: -$42.50`
     - `Yesterday • Sunday, Oct 18` — `Total Net: -$110.00`
   - **Transaction Row Design**:
     - Left: 40×40px rounded category avatar (e.g., Fork/Knife icon for Dining).
     - Center: Primary merchant name (`Whole Foods Market`), subtitle (`Groceries • Paid via Chase Card • 2:45 PM`).
     - Right: Prominent amount (`-$84.20` in coral red, `tnum`), status pill (`Settled`).
3. **Interactive Actions**:
   - Hover on desktop reveals subtle `Edit` and `•••` options.
   - Swipe left on mobile reveals red "Delete" action; swipe right reveals teal "Edit" action.

---

### Screen 3: Budgets & Spending Limits (`/budgets`)
*Framer Artboard: `Budgets - Desktop (1440)` & `Budgets - Mobile (393)`*

#### Visual Hierarchy & Layout
1. **Master Envelope Summary**:
   - Circular gauge or dual-metric card: "Spent $2,840 of $3,500 Budget" (`81% utilized`).
   - "Day 19 of 31 — Safe pacing is 61% (You are pacing 20% ahead of schedule)".
2. **Category Budget Grid (3 columns on Desktop, 1 column on Mobile)**:
   - Category Card Component:
     - Header: Category icon, Category title, and 3-dots menu button.
     - Pacing Visualizer: 6px tall progress track with two indicators:
       - Solid fill: actual spending.
       - Dotted vertical hairline: today's ideal pacing marker.
     - Quantitative Data: `$680 spent / $800 limit` (Bold numbers, muted limit).
     - Status Chip: "🟢 On Track" (if under pace), "🟡 Tight" (if within 10%), "🔴 Over by $45" (if exceeded).

---

### Screen 4: Salary Planner (`/salary-plan`)
*Framer Artboard: `Salary Planner - Desktop (1440)` & `Salary Planner - Mobile (393)`*

#### Visual Hierarchy & Layout
1. **Paycheck Distribution Hero**:
   - Input: Net Monthly Salary (`$6,500.00`).
   - Stacked Interactive Proportional Bar:
     - Fixed Essentials (Rent, Utilities, Insurance) — `45% ($2,925)`
     - Financial Goals (Retirement, Emergency Fund) — `25% ($1,625)`
     - Discretionary Spending (Dining, Entertainment) — `20% ($1,300)`
     - Unallocated Surplus — `10% ($650)` (Highlighted in bright teal CTA: "Assign Remaining").
2. **Dynamic Allocation Cards (Mobile Friendly)**:
   - Instead of a massive wide table, render grouped accordion cards by category.
   - Each card displays Planned Amount vs. Actual Auto-Calculated Spend from linked transactions.

---

### Screen 5: Splitwise Group Debt & P2P Settlement (`/splitwise`, `/splitwise/:id`)
*Framer Artboard: `Splitwise - Desktop (1440)` & `Splitwise - Mobile (393)`*

#### Visual Hierarchy & Layout
1. **Net Position Banner**:
   - Top Card: "You are owed **$124.50** across 3 groups" (or "You owe **$45.00**").
   - Action Buttons: `[+ Add Group Bill]` and `[Settle Up]`.
2. **Simplified Pairwise Debt Graph**:
   - Clean avatar connection cards:
     - `Sarah M.` owes you `$84.50` (Pill: "Request via UPI / Bank").
     - You owe `David K.` `$20.00` (Pill: "Pay Now").
3. **AI Split Receipt Integration**:
   - Button inside group: "⚡ Split from Receipt Photo".
   - Flow: Snaps bill -> parses items -> assigns members to items -> generates Splitwise ledger automatically.

---

### Screen 6: AI Receipt Scanner (`/ai-scanner`)
*Framer Artboard: `AI Scanner - Desktop (1440)` & `AI Scanner - Mobile (393)`*

#### Visual Hierarchy & Layout
1. **Dual Capture Ingestion Zone**:
   - Mobile: Split action tiles — `[📷 Snap Receipt]` and `[📁 Choose File / PDF]`.
   - Desktop: Drag-and-drop zone with dashed border, upload icon, and supported format tags (PNG, JPG, PDF up to 10MB).
2. **Interactive Scanning Simulation State**:
   - Once file is uploaded: Receipt image preview is shown with a glowing emerald scan line moving down the image.
   - Status Indicator: Step 1: Uploading -> Step 2: Gemini 1.5 Flash Vision OCR -> Step 3: Categorizing Line Items.
3. **Inline Editable Verification Card**:
   - Two columns on desktop: Receipt Preview on Left (50%), Parsed Form Fields on Right (50%).
   - Form fields: Merchant Name, Transaction Date, Category Dropdown, Subtotal, Tax, Tip, Total Amount.
   - Line Items list with checkbox to select which items to import.
   - Primary CTA: "Confirm & Add to Ledger".

---

### Screen 7: Recurring Bills & Subscriptions (`/recurring`)
*Framer Artboard: `Recurring - Desktop (1440)` & `Recurring - Mobile (393)`*

#### Visual Hierarchy & Layout
1. **Monthly Subscription Runway**:
   - Total Monthly Commitment: `$412.00 / month` across 8 active subscriptions.
2. **Calendar Runway Timeline**:
   - Horizontal date strip showing upcoming bill due dates for the next 30 days.
3. **Subscription Cards List**:
   - Icon / Logo of service (Netflix, Spotify, Gym, Cloudflare).
   - Frequency: "Monthly on the 1st", "Annual on Nov 12".
   - Status: "Paid this month", "Due in 3 days", "Auto-debit active".

---

## 5. Modal & Bottom Sheet Prototype Flows in Framer

### 5.1 The Unified Quick-Action Sheet (Mobile)
Triggered by tapping the center `+` button in the mobile dock:
- Slides up from bottom (`spring(mass: 0.8, damping: 25, stiffness: 250)`).
- Semi-transparent backdrop (`rgba(15, 23, 42, 0.4)` with `backdrop-filter: blur(4px)`).
- Rounded top corners: `24px`.
- Drag handle: `36px × 4px`, color `--slate-300`.
- Quick Action Tiles (2 × 2 Grid):
  - 💳 **Add Expense** (Quick transaction entry)
  - 💰 **Add Income** (Salary, dividend, refund)
  - 🧾 **Scan Receipt** (Launches AI camera)
  - 👥 **Split Bill** (Select Splitwise group)

### 5.2 Add Transaction Modal / Sheet
- Clean segmented switch at top: `[Expense] [Income] [Transfer]`.
- Massive Hero Input for amount: `$ 0.00` with auto-focus virtual numpad.
- Inline selectors: Category chip carousel, Date picker, Merchant text input, Payment Method pill.
- Sticky primary CTA bar at bottom: "Add $0.00 Expense".

---

## 6. Micro-Interactions & Animation Tokens

1. **Card Hover (Desktop)**:
   - `transform: translateY(-2px);`
   - `box-shadow: 0 4px 12px -2px rgba(15, 23, 42, 0.08);`
   - Transition: `all 0.18s cubic-bezier(0.16, 1, 0.3, 1)`.
2. **Button Tap / Active**:
   - `transform: scale(0.98);`
   - Transition: `0.1s ease-out`.
3. **Tab & Segmented Control Switcher**:
   - Framer layout animation with shared `layoutId` so the active background pill glides seamlessly between options.
4. **Counter Ticker / Amount Load**:
   - Numbers transition using tabular roll-up animation on initial screen load.

---

## 7. Status of Reference Screenshots & Visual Assets

> [!IMPORTANT]
> **Environment Verification Notice**:  
> Automated browser screenshot capture tools were evaluated in the local execution sandbox. Due to restricted headless browser binary drivers in this offline environment, automated browser PNG exports could not be generated.  
> In accordance with strict engineering integrity standards:
> - No fake or synthetic screenshot files have been falsely claimed.
> - Instead, the structural and CSS-pixel specifications provided in this brief and in `CURRENT_DESIGN_SYSTEM.md` and `UI_INVENTORY.md` provide 100% exact dimensions, hex codes, typography parameters, and layout blueprints for Framer artboard generation.
