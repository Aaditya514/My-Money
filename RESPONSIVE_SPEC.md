# Responsive Layout Specification: Personal Finance Tracker

**Target Viewports**: Mobile (393 × 873px Benchmark), Tablet (768px – 1024px), Desktop (1280px – 1440px+)  
**Methodology**: Mobile-First Fluid Grid Architecture, Touch-Target Ergonomics, Safe-Area Awareness, and Viewport-Adaptive Component Transformations.

---

## 1. Breakpoint System & Viewport Classification

The personal finance tracker utilizes a 5-tier responsive scale calibrated for modern mobile devices, high-density tablets, and widescreen desktop monitors.

```
Responsive Breakpoint Scale
  |-------------- Mobile Small (< 360px)
  |---------------------------- Mobile Standard / Flagship (360px - 639px) [Benchmark: 393px]
  |------------------------------------------ Tablet Portrait (640px - 767px)
  |-------------------------------------------------------- Tablet Landscape / Laptop (768px - 1023px)
  |---------------------------------------------------------------------- Desktop Standard (1024px - 1279px)
  |------------------------------------------------------------------------------------ Desktop Widescreen (1280px+)
```

### Breakpoint Tokens & Media Query Rules

| Breakpoint Name | Token | Viewport Range | Target Devices | Primary Navigation Mode | Container Max-Width |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Mobile Small** | `--bp-xs` | `< 360px` | iPhone SE (1st gen), Galaxy Fold outer screen | Bottom Dock + Quick Sheet | `100%` (fluid, 12px margin) |
| **Mobile Standard** | `--bp-sm` | `360px – 639px` | **iPhone 14/15/16 (393px)**, Pixel 8, Galaxy S24 | Bottom Dock + Quick Sheet | `100%` (fluid, 16px margin) |
| **Tablet Portrait** | `--bp-md` | `640px – 767px` | iPad Mini, Galaxy Tab portrait | Bottom Dock + Quick Sheet | `100%` (fluid, 24px margin) |
| **Tablet Landscape**| `--bp-lg` | `768px – 1023px` | iPad Pro 11", Surface Pro, small laptops | Collapsed Top Bar + Sidebar | `720px` – `960px` |
| **Desktop Standard**| `--bp-xl` | `1024px – 1279px` | Standard 1080p displays, MacBook Air 13" | Sticky Top Navigation Shell | `1024px` (32px margin) |
| **Desktop Widescreen**| `--bp-2xl`| `1280px+` | 1440p / 4K monitors, MacBook Pro 16" | Full Sticky Top Navigation Shell | `1280px` (centered, 48px margin)|

---

## 2. Benchmark Mobile Viewport: 393 × 873 CSS Pixels

The standard design benchmark for all mobile screens in this handoff is **393px × 873px** (the exact CSS pixel viewport of iPhone 14/15/16 Pro and modern flagship Android devices).

```
+-------------------------------------------------------+ 0px (Top of Viewport)
| [StatusBar: Time, Signal, Battery]   (Height: 47px)   | <-- env(safe-area-inset-top)
+-------------------------------------------------------+ 47px
| [Brand Mark]       [October 2026 v]     [Theme]  [Bell]| <-- Mobile Header (56px)
+-------------------------------------------------------+ 103px
|                                                       |
|                     SCROLLABLE BODY                   |
|                   CONTENT AREA (672px)                |
|                                                       |
|  - Metric Cards / Hero Balance                        |
|  - Active Salary Allocation / Pacing Bar              |
|  - Categorized Transaction Feed                       |
|  - Quick Filter Pills Carousel                        |
|                                                       |
+-------------------------------------------------------+ 775px
| [Home]   [Activity]    (+)    [Budgets]    [Hub/More] | <-- Floating Bottom Dock (64px)
+-------------------------------------------------------+ 839px
| [Home Indicator Bar]                 (Height: 34px)   | <-- env(safe-area-inset-bottom)
+-------------------------------------------------------+ 873px (Bottom of Viewport)
```

### Critical Mobile Requirements
1. **Thumb Zone Ergonomics**:
   - The primary action trigger (Center `+` button) and the 4 navigation tabs sit directly in the bottom "Natural Thumb Reach Zone" (between 650px and 839px from the top).
   - Destructive actions (e.g., delete confirmation) require two-handed intent or an explicit confirmation swipe.
2. **Safe Area Insets**:
   - Mobile Header: `padding-top: calc(12px + env(safe-area-inset-top));`.
   - Bottom Navigation Dock: `padding-bottom: calc(8px + env(safe-area-inset-bottom)); margin-bottom: 0;`.
   - Floating Modals & Sheets: `padding-bottom: max(16px, env(safe-area-inset-bottom));`.

---

## 3. Structural Transformations Across Viewports

### 3.1 Navigation Shell Transformation

```
Desktop (>= 1024px)
+---------------------------------------------------------------------------------------------------+
| [Logo]  |  Overview   Transactions   Planning v   Shared v  |  [Search Cmd+K]  [+ Add]  [Bell]  [User] |
+---------------------------------------------------------------------------------------------------+

Mobile (< 768px)
Top Header:    [Logo]                     [Oct 2026 v]                 [Bell]  [Theme]
Bottom Dock:   [ Home ]     [ Activity ]     [  (+)  ]     [ Budgets ]     [ Hub ]
```

- **>= 1024px (Desktop)**:
  - Sticky top header `64px` height with backdrop blur.
  - 4 major navigation categories with dropdown menus for secondary tools.
  - Right-aligned global action button `+ Add New` opens universal creation modal.
- **768px – 1023px (Tablet)**:
  - Sticky top header collapses secondary text links into a compact hamburger or segmented menu button.
- **< 768px (Mobile)**:
  - Top header drops down to `56px` height, displaying only brand, current month selector pill, and notifications.
  - Bottom navigation dock renders 4 primary tabs + floating center `+` action button.
  - Tapping `+` opens the **Quick-Action Bottom Sheet** directly above the thumb zone.

---

### 3.2 Dashboard Grid Transformations

#### Desktop Layout (12-Column Grid, Width: 1280px)
```
+---------------------------------------------------------------------------------------------------+
| Top KPI Row: 4 Equal Columns (3 Cols Each)                                                        |
| [ Net Worth: $14,250 ]  [ Inflow: +$5,200 ]  [ Outflow: -$2,840 ]  [ Net Savings: +$2,360 ]      |
+---------------------------------------------------------------------------------------------------+
| Mid Tier: Salary Plan Strip (12 Cols)                                                             |
| [ Active Plan: 50% Needs | 30% Wants | 20% Savings                           (Remaining: $650) ] |
+-----------------------------------------------------------------+---------------------------------+
| Left Column (7 Cols - 58% Width)                                | Right Column (5 Cols - 42% Width)|
| - Cash Flow Trajectory Chart (Monthly Bars + Line)             | - Category Spending Breakdown   |
| - Recent Transactions Feed (Date Grouped Ledger)               | - Upcoming Recurring Bills      |
|                                                                 | - Savings Goals Progress        |
+-----------------------------------------------------------------+---------------------------------+
```

#### Tablet Layout (8-Column Grid, Width: 768px – 1023px)
- **Top KPI Row**: 2 × 2 Grid (2 cards per row, 4 columns each).
- **Salary Plan**: Full width (8 columns).
- **Charts & Feeds**: Stacked vertically into single-column modules (Cash flow chart -> Category Breakdown -> Bills -> Recent Transactions).

#### Mobile Layout (4-Column Grid, Benchmark: 393px)
- **Top KPI Row**: Transformed into a **Horizontal Swipe Carousel** or **Compact Hero Stack**:
  - Hero Card: Net Available Balance (`$14,250.80`) with inline micro-sparkline.
  - Direct Sub-Pills: Inflow (`+$5,200.00`) and Outflow (`-$2,840.20`) side-by-side in a 2-column flex row (`16px` gap).
- **Salary Plan**: Collapsed into an interactive 1-line progress bar with an expandable details drawer.
- **Charts vs. Activity**: Instead of infinite vertical stacking, mobile uses a **Segmented Tab Switcher**:
  - `[ Overview ]` (Shows Cash Flow Chart & Category Bars)
  - `[ Activity & Bills ]` (Shows Date-Grouped Transaction Feed & Upcoming Bills)
  - *Outcome*: Reduces mobile page height from 4,200px down to 1,450px, eliminating scroll fatigue.

---

### 3.3 Transaction Ledger: Table to Card Transformation

#### Desktop (>= 768px): Data Table Format
- Rendered using semantic HTML `<table>` with fixed column widths:
  - `Date`: `120px` (Left-aligned)
  - `Category`: `160px` (Icon avatar + category badge)
  - `Merchant & Notes`: Auto flex width (Merchant bold, payment note gray)
  - `Type`: `100px` (Pill badge)
  - `Amount`: `140px` (Right-aligned, tabular figures `tnum`, bold with decimal styling)
  - `Actions`: `90px` (Hover-revealed Edit/Delete action buttons)

#### Mobile (< 768px): Swipeable List Card Format
- Table headers and column boundaries are completely removed.
- Each transaction renders as an interactive card item (`height: 64px`):
  - Left: `40 × 40px` rounded category avatar with SVG vector icon.
  - Middle: Multi-line flex container:
    - Line 1: Merchant title (`Whole Foods`) in bold (`14px`).
    - Line 2: Category and time (`Groceries • 2:45 PM`) in muted slate (`12px`).
  - Right:
    - Line 1: Formatted amount (`-$84.20`) in tabular bold typography (`15px`).
    - Line 2: Status pill (`Settled`) or payment method icon.
- **Gesture Support**:
  - Swipe left reveals full-height red "Delete" action button (`44px` width).
  - Swipe right reveals full-height teal "Edit" action button (`44px` width).

---

### 3.4 Modal Dialogs vs. Mobile Bottom Sheets

| Attribute | Desktop Viewport (>= 768px) | Mobile Viewport (< 768px) |
| :--- | :--- | :--- |
| **Component Used** | `ModalDialogComponent` | `BottomSheetComponent` |
| **Positioning** | Centered: `top: 50%; left: 50%; transform: translate(-50%, -50%);` | Docked: `bottom: 0; left: 0; width: 100%;` |
| **Width** | Fixed: `520px` (Standard) / `720px` (Large) | Fluid: `100vw` (no side gutters) |
| **Max Height** | `85vh` with internal scrollbar | `90vh` with swipe-down dismissal handle |
| **Border Radius** | `16px` on all 4 corners | `24px 24px 0 0` (top corners only) |
| **Entry Transition** | Fade + Scale: `scale(0.95)` to `scale(1)` in `180ms` | Slide Up: `translateY(100%)` to `translateY(0)` in `240ms` |
| **Dismissal Gestures**| Click backdrop, click "X", press `Escape` | Drag handle downward > 120px, swipe down backdrop, click "X" |
| **Keyboard Behavior**| Centers above background | Translates up to anchor directly above virtual keyboard |

---

### 3.5 Filter Controls: Horizontal Bar to Slide-Out Drawer

```
Desktop Filter Bar (>= 768px)
[ Search transactions... (Cmd+K) ]  [ Type: All v ]  [ Category: All v ]  [ Date Range v ]  [ Clear ]

Mobile Filter System (< 768px)
Step 1 (On Page): [ Search transactions...                ] [ Filter (2) ]
                  [ All ] [ Expenses ] [ Income ] [ Transfers ] (Horizontal scroll)

Step 2 (Drawer):  Tapping [ Filter (2) ] opens the Filter Bottom Sheet with full-height
                  segmented selectors, date picker wheels, and sticky "Apply Filters" button.
```

---

## 4. Touch Target & Sizing Standards

To guarantee effortless mobile interaction without mis-clicks:

1. **44 × 44px Minimum Interactive Bounding Box**:
   - All buttons, icon triggers, form checkboxes, date pickers, and tab items have a minimum interactive tap target of `44px × 44px`.
   - Small visual icons (e.g., `20 × 20px` Feather icon) are wrapped in padding:
     ```css
     .btn-icon {
       width: 44px;
       height: 44px;
       display: inline-flex;
       align-items: center;
       justify-content: center;
       border-radius: var(--radius-sm);
     }
     ```
2. **Form Input Ergonomics**:
   - Mobile form inputs have a minimum height of `48px` with `16px` (`1rem`) base font size to prevent iOS Safari auto-zooming on focus.
   - Numeric inputs (Amount, Phone Number, OTP) explicitly specify `inputmode="decimal"` or `inputmode="numeric"` to summon the numeric keypad directly.

---

## 5. Responsive Implementation CSS Patterns

```css
/* Responsive Layout Tokens */
:root {
  --content-max-width: 1280px;
  --header-height-desktop: 64px;
  --header-height-mobile: 56px;
  --bottom-dock-height: 64px;
}

/* Page Wrapper */
.app-container {
  width: 100%;
  max-width: var(--content-max-width);
  margin-left: auto;
  margin-right: auto;
  padding-left: var(--space-4);
  padding-right: var(--space-4);
}

/* Desktop Only Display */
@media (max-width: 767px) {
  .desktop-only {
    display: none !important;
  }
  
  .app-container {
    padding-left: var(--space-3);
    padding-right: var(--space-3);
    padding-bottom: calc(var(--bottom-dock-height) + env(safe-area-inset-bottom) + 16px);
  }
}

/* Mobile Only Display */
@media (min-width: 768px) {
  .mobile-only {
    display: none !important;
  }
}

/* Tabular Number Alignment */
.amount-cell, .metric-value, .stat-number {
  font-variant-numeric: tabular-nums;
  font-feature-settings: "tnum" 1, "cv05" 1;
}

/* Safe Area Inset Support */
.mobile-bottom-dock {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: calc(var(--bottom-dock-height) + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
  border-top: 1px solid var(--border-color);
  z-index: 1000;
}
```

This responsive specification ensures that every screen in the personal finance tracker delivers an uncompromising, native-feeling experience whether accessed on a 393px mobile screen or a 1440px desktop workstation.
