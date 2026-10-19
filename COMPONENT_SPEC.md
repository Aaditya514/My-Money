# Component Specification: Personal Finance Tracker

**Target Architecture**: Angular 18 Component Library / Design System  
**Design Exploration**: Framer Component Catalog  
**Standards**: WCAG 2.1 AA Compliance, 44px Touch Targets, Tabular Numerics (`tnum`), Keyboard Navigation Support

---

## 1. Overview & Component Catalog Architecture

This document defines the complete reusable component system for the personal finance tracker. Every component is specified with its props/inputs, events/outputs, structural hierarchy, responsive behavior, visual states, and accessibility requirements.

```
Design System Component Hierarchy
├── 01. Shell & Navigation
│   ├── NavigationShell (Desktop Header & Mobile Bottom Dock)
│   ├── QuickActionDrawer (Mobile Bottom Sheet Modal)
│   └── UserProfileDropdown
├── 02. Data Display & Metrics
│   ├── MetricCard (KPI cards with trend badges)
│   ├── CurrencyAmount (Tabular numeric display with decimal styling)
│   ├── BudgetProgressBar (With pacing hairline indicator)
│   ├── GoalCard (Savings goal card with milestone marker)
│   └── CategoryBadge (Color-coded category indicators)
├── 03. Lists & Records
│   ├── TransactionRow (Standard, grouped, and swipeable mobile card)
│   ├── DateGroupHeader (Daily summary with net cash flow)
│   └── EmptyState (Contextual illustrations and action buttons)
├── 04. Form Controls & Inputs
│   ├── FormField (Wrapper with label, hint, error message)
│   ├── CurrencyInput (Auto-formatted currency with symbol prefix)
│   ├── SegmentedControl (Tabbed pill switcher with smooth slider)
│   ├── FilterBar (Search input, quick chips, drawer trigger)
│   └── DropdownSelect (Searchable select with keyboard traps)
└── 05. Feedback & Overlays
    ├── ModalDialog (Desktop centered dialog)
    ├── BottomSheet (Mobile touch-friendly drawer)
    ├── SkeletonLoader (Shimmer placeholder components)
    ├── ConfirmDialog (Destructive and non-destructive alerts)
    └── ToastNotification (Auto-dismissing status alerts)
```

---

## 2. Component Specifications

### 2.1 `NavigationShell`
The global layout wrapper providing responsive desktop top navigation and mobile bottom navigation.

#### Inputs / Props
- `activeRoute`: `string` — current router URL path.
- `unreadNotificationsCount`: `number` — badge count for the alert bell.
- `isDarkMode`: `boolean` — current theme status.
- `userProfile`: `{ name: string, email: string, avatarUrl?: string }`.

#### Outputs / Events
- `toggleTheme`: `EventEmitter<void>` — switches light/dark mode.
- `openQuickAction`: `EventEmitter<void>` — opens the mobile quick action bottom sheet or desktop modal.
- `searchQuery`: `EventEmitter<string>` — opens the global `Cmd+K` command palette.

#### Visual States
1. **Default**:
   - Desktop: White/midnight backdrop `rgba(255,255,255,0.85)` with `backdrop-filter: blur(12px)`, height `64px`, border-bottom `1px solid var(--border-color)`.
   - Mobile: Top header `56px`, bottom dock `64px` pinned to viewport bottom with `env(safe-area-inset-bottom)`.
2. **Scrolled**: Header acquires elevated shadow `0 2px 8px rgba(0,0,0,0.06)` when page scroll offset > 10px.
3. **Item Hover**: Background transitions to `--slate-100` (`#f1f5f9`), text becomes `--primary` (`#0f766e`).
4. **Item Active**: Font weight `600`, text `--primary`, with a bottom active indicator bar `2px` tall on desktop.

#### Accessibility (A11y)
- Landmark: `<nav aria-label="Main Navigation">`.
- Active Link: `aria-current="page"`.
- Mobile Dock: Center button `aria-label="Create new transaction or bill"`.

---

### 2.2 `MetricCard`
Standardized financial KPI card displaying total balances, cash flow, and monthly budget aggregates.

#### Structure & Layout
```
+-----------------------------------------------------------+
| [Icon] Title Label                      [Trend Badge ^8%] |
| $14,250.80                                               |
| +$450.00 vs last month               [Mini Sparkline Slot]|
+-----------------------------------------------------------+
```

#### Inputs / Props
- `label`: `string` — e.g., "Total Balance", "Monthly Income", "Net Savings".
- `amount`: `number` — numeric monetary value.
- `currency`: `string` (default: `USD`).
- `trendPercentage`: `number` (optional) — e.g., `+12.4` or `-4.2`.
- `trendPeriod`: `string` (optional) — e.g., "vs last month".
- `variant`: `'neutral' | 'income' | 'expense' | 'highlight'`.
- `isLoading`: `boolean` (default: `false`).

#### Visual States
1. **Default (Neutral)**: Background `--bg-surface`, border `1px solid var(--border-color)`, border-radius `16px`, padding `20px`.
2. **Income Highlight**: Left accent border `3px solid var(--income)`, trend badge background `--income-subtle`, text `--income`.
3. **Expense Highlight**: Left accent border `3px solid var(--expense)`, trend badge background `--expense-subtle`, text `--expense`.
4. **Hover (Interactive)**: Subtle lift `transform: translateY(-2px)`, shadow `var(--shadow-md)`.
5. **Loading**: Replaced with `SkeletonLoader` displaying shimmering gray rectangles matching label, amount, and badge geometry.

---

### 2.3 `CurrencyAmount`
Tabular, high-legibility currency display separating whole units from fractional cents.

#### Inputs / Props
- `value`: `number` — raw numeric value (e.g., `12450.75`).
- `showSign`: `boolean` (default: `false`) — forces `+` or `-` prefix.
- `currencyCode`: `string` (default: `USD`).
- `size`: `'sm' | 'base' | 'lg' | 'xl' | 'hero'`.
- `semanticColor`: `boolean` (default: `true`) — green for >0, red for <0.

#### Styling & Behavior
- CSS: `font-variant-numeric: tabular-nums; font-feature-settings: "tnum" 1;`.
- Split rendering: `<span class="currency-symbol">$</span><span class="currency-integer">12,450</span><span class="currency-decimal">.75</span>`.
- The decimal `.75` is rendered at `75%` relative font size and `80%` opacity to streamline rapid numeric scanning.

---

### 2.4 `TransactionRow`
Individual transaction ledger entry supporting desktop table format and responsive mobile swipe card format.

#### Structure & Layout (Mobile Viewport: 393px)
```
+-----------------------------------------------------------+
| [Icon 40x40]  Merchant Title               -$84.20        |
|               Category • Oct 19, 2:45 PM   [Settled]      |
+-----------------------------------------------------------+
```

#### Inputs / Props
- `transaction`: `Transaction` — data model (id, title, amount, type, category, date, paymentMethod, status).
- `showActions`: `boolean` — displays inline edit/delete triggers.

#### Outputs / Events
- `edit`: `EventEmitter<number>` — emits transaction ID.
- `delete`: `EventEmitter<number>` — emits transaction ID.
- `click`: `EventEmitter<number>` — emits transaction ID to open detail drawer.

#### Visual States & Touch Interactions
1. **Default**: Height `64px`, border-bottom `1px solid var(--border-color)`, padding `12px 16px`.
2. **Hover (Desktop)**: Background `--slate-50` (`#f8fafc`), action buttons fade in at `opacity: 1`.
3. **Focus-Visible**: Keyboard tab focus highlights row with `outline: 2px solid var(--primary); outline-offset: -2px`.
4. **Mobile Swipe Left (< 768px)**: Row transforms `translateX(-72px)` revealing a bright red delete button (`44px` wide) with trash icon.
5. **Mobile Swipe Right (< 768px)**: Row transforms `translateX(72px)` revealing an emerald edit button with pencil icon.

---

### 2.5 `BudgetProgressBar`
Visual progress indicator for monthly envelope spending with integrated pacing threshold.

#### Structure & Layout
```
+-----------------------------------------------------------+
| Groceries                      $480.00 spent of $600.00   |
| [===================|====----------] 80%                  |
|                     ^ Today (Day 19: Safe pace 61%)       |
+-----------------------------------------------------------+
```

#### Inputs / Props
- `spent`: `number` — current spending sum.
- `limit`: `number` — allocated budget maximum.
- `daysElapsed`: `number` — day of current month (e.g., `19`).
- `daysInMonth`: `number` — total days in month (e.g., `31`).

#### Visual States
1. **Track**: Background `--slate-100` (`#f1f5f9`), height `8px`, border-radius `999px`, relative positioning.
2. **Fill**:
   - `spent / limit < 0.8`: Background `--income` (`#059669`).
   - `0.8 <= spent / limit <= 1.0`: Background `--warning` (`#d97706`).
   - `spent / limit > 1.0`: Background `--expense` (`#dc2626`).
3. **Pacing Marker**: Vertical hairline `width: 2px; height: 14px; top: -3px; background: var(--slate-900);` positioned at `(daysElapsed / daysInMonth) * 100%`.
4. **Micro-copy Status**:
   - Under pace: "🟢 Safe pacing ($114 remaining before budget limit)".
   - Over pace: "🟡 Pacing 19% ahead of timeline".
   - Exceeded: "🔴 Over budget by $42.50".

---

### 2.6 `FilterBar` & `SegmentedControl`
Responsive search and multi-parameter filtering component.

#### Structure & Layout
- Desktop: Horizontal flex layout containing Search Input (`280px`), Category dropdown, Type dropdown, Date Range picker, and "Clear Filters" button.
- Mobile (< 768px): Full-width Search Input (`44px` height) with an attached filter trigger button displaying active filter count badge (`Filter (2)`). Tapping button opens the `FilterBottomSheet`.
- Horizontal scrollable quick-filter chips directly below: `[All] [Expenses] [Income] [Transfers]`.

#### Inputs / Props
- `searchQuery`: `string`.
- `selectedType`: `'ALL' | 'EXPENSE' | 'INCOME'`.
- `selectedCategory`: `string | null`.
- `activeFilterCount`: `number`.

#### Outputs / Events
- `filterChange`: `EventEmitter<FilterCriteria>`.
- `clearFilters`: `EventEmitter<void>`.

---

### 2.7 `CurrencyInput` / `FormField`
Form control tailored for monetary input with touch-first accessibility.

#### Structure & Layout
```
+-----------------------------------------------------------+
| Amount *                                                  |
| [ $ ] [  1,250.00                                       ] |
| Helper text / Validation error message                     |
+-----------------------------------------------------------+
```

#### Inputs / Props
- `label`: `string` — e.g., "Transaction Amount".
- `required`: `boolean`.
- `placeholder`: `string` (default: `0.00`).
- `currencySymbol`: `string` (default: `$`).
- `errorMessage`: `string | null`.
- `hint`: `string | null`.

#### Visual States & Touch Ergonomics
1. **Default**: Border `1px solid var(--border-color)`, border-radius `10px`, background `--bg-surface`, minimum height `44px` (touch-target compliance).
2. **Focus**: Border `1px solid var(--primary)`, box-shadow `0 0 0 3px rgba(15, 118, 110, 0.15)`.
3. **Invalid / Error**: Border `1px solid var(--expense)`, box-shadow `0 0 0 3px rgba(220, 38, 38, 0.15)`, error message appears below in `--expense` text (`--fs-xs`).
4. **Disabled**: Background `--slate-100`, text `--slate-400`, cursor `not-allowed`.

---

### 2.8 `BottomSheet` (Mobile Overlay)
Touch-friendly drawer that slides from the bottom of the screen on mobile devices, replacing rigid centered modal dialogs.

#### Structure & Layout
```
+-----------------------------------------------------------+
|                           [===]                           |  <-- Drag Handle (36x4px)
| Modal Header Title                                    [X] |
+-----------------------------------------------------------+
|                                                           |
| Scrollable Content Area                                   |
| (Form fields, member split lists, filter controls)        |
|                                                           |
+-----------------------------------------------------------+
| [ Cancel ]                            [ Confirm / Save ]  |  <-- Sticky Bottom Action Bar
+-----------------------------------------------------------+
```

#### Behavior & Touch Gestures
- **Mount**: Animates upwards from `translateY(100%)` to `translateY(0)` in `240ms cubic-bezier(0.16, 1, 0.3, 1)`.
- **Dismiss**: Dragging down past 120px velocity dismisses the sheet; tapping the dimmed backdrop (`rgba(15, 23, 42, 0.4)`) dismisses.
- **Keyboard Handling**: Automatically adjusts `bottom` padding when virtual keyboard opens to keep the active form input and action buttons visible.

---

### 2.9 `SkeletonLoader`
Shimmer placeholder component providing immediate visual feedback during HTTP data fetching.

#### Visual Tokens
- Light Mode Fill: `#e2e8f0` (`--slate-200`).
- Light Mode Shimmer Highlight: `linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.6) 50%, rgba(255,255,255,0) 100%)`.
- Dark Mode Fill: `#1e293b` (`--slate-800`).
- Dark Mode Shimmer Highlight: `linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.08) 50%, rgba(255,255,255,0) 100%)`.
- Animation Duration: `1.5s infinite linear`.

---

## 3. Component Interaction & State Matrix

| Component | Default | Hover | Focus-Visible | Active / Pressed | Disabled | Loading State | Error State |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Primary Button** | Teal fill (`#0f766e`) | Darker Teal (`#115e59`) | 3px Teal halo | `scale(0.98)` | Gray fill, `opacity 0.5` | Inline spinner + label | Red border/halo |
| **Secondary Button**| White fill, gray border| `--slate-50` fill | 3px Slate halo| `scale(0.98)` | `opacity 0.4` | Inline spinner | N/A |
| **Destructive Button**| Rose fill (`#dc2626`) | Darker Rose (`#b91c1c`)| 3px Rose halo | `scale(0.98)` | `opacity 0.5` | Inline spinner | N/A |
| **Form Input** | Slate border (`#e2e8f0`)| Slate border (`#cbd5e1`)| Teal border + ring| Inset shadow | Gray background | Skeleton box | Rose border + ring |
| **Transaction Card**| Flat white, hairline | Subtle gray fill | 2px Teal outline| Swipe reveal | `opacity 0.6` | Row skeleton | Rose flash on fail |
| **Category Badge** | Subtle tint fill | Slightly deeper tint | Outline ring | N/A | Gray monochrome| Skeleton pill | N/A |
| **Tab / Pill** | Transparent text | `--slate-100` fill | Focus ring | Teal text + bar | Grayed out | N/A | N/A |

---

## 4. Accessibility (A11y) Verification Checklist

All components adhere to the following strict criteria:
1. **Interactive Target Sizing**: Every clickable button, link, checkbox, and input trigger has a bounding box of at least **44 × 44 CSS pixels** on viewports `< 768px`.
2. **Keyboard Navigation**:
   - `Tab` moves focus sequentially through interactive elements.
   - `Escape` closes open modals, dropdown menus, and bottom sheets.
   - `Enter` / `Space` activates buttons and toggles checkboxes.
   - `Arrow Keys` navigate segmented controls and dropdown option lists.
3. **Screen Reader Semantics**:
   - Modals utilize `role="dialog"`, `aria-modal="true"`, and `aria-labelledby="dialog-title"`.
   - Progress bars utilize `role="progressbar"`, `aria-valuenow="80"`, `aria-valuemin="0"`, `aria-valuemax="100"`.
   - Dynamic alerts (toast, scanner status) utilize `aria-live="polite"`.
