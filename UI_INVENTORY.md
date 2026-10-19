# UI Inventory: Personal Finance Tracker
**Application Version**: Angular 18 (Standalone Components) & Spring Boot 3.3.2  
**Analysis Date**: October 2026  
**Analyst**: Senior UI/UX Designer & Frontend Systems Architect  
**Scope**: 100% verified screens, routes, dialogs, drawers, and shared components inspected from source code.

---

## 1. Executive Summary & Application Architecture

The application is a comprehensive personal finance tracking and budgeting platform with automated recurring payment tracking, dynamic salary planning, Gemini AI receipt scanning, and integrated group bill splitting (Splitwise).

### Global Navigation & Shell Structure
- **Desktop Shell (`>= 768px`)**: Top fixed/sticky application header (`.app-header.desktop-navbar`) containing:
  - Brand identity (`Finance Tracker` with SVG icon).
  - 9 navigation links (`Home`, `Transactions`, `Budgets`, `Goals`, `Recurring`, `Splitwise`, `Salary Plan`, `Categories`, `AI Scanner`).
  - Action utilities: Theme toggle (Light/Dark OLED), Financial Alerts Bell with unread badge counter, User profile name badge, and `Sign Out` button.
- **Mobile Shell (`< 768px`)**: Two-tier navigation system:
  - **Top Status Bar (`.mobile-top-bar`)**: Brand icon, Theme toggle, Notification Bell, Quick AI Scan icon, and User Avatar circle linking to `/more`.
  - **Bottom Navigation Bar (`.mobile-bottom-nav`)**: 4 primary tabs (`Home`, `Transactions`, `Budgets`, `More`).
  - **Floating Action Button (FAB)**: 56px circular floating action button (`+`) pinned at bottom-right for instant 1-tap transaction creation (`/transactions/add`).
- **Global Inset & Safe-Area Padding**: `.main-content` enforces `min-height: calc(100vh - 60px)` on desktop and `padding-bottom: calc(84px + env(safe-area-inset-bottom, 16px))` on mobile to prevent bottom navigation occlusion.
- **Global Overlays (App Root)**:
  - `<app-notification-center>`: Global urgent banner + slide-over drawer.
  - `<app-confirm-dialog>`: Centralized promise-based confirmation dialog.

---

## 2. Complete Screen-by-Screen Inventory

---

### Screen 1: Login
- **Route**: `/login` (Unauthenticated)
- **Component**: `LoginComponent` (`login.component.html`, `login.component.ts`, `login.component.css`)
- **Primary Purpose**: Authenticate existing users via either email or 10-digit mobile phone number with secure JWT session issuance.
- **Content Hierarchy**:
  1. Card container (`.auth-wrapper > .auth-card`).
  2. Brand mark & title: "Welcome Back" / "Sign in to manage your financial ledger and budgets".
  3. Error alert banner (dismissible).
  4. Form inputs:
     - `Phone Number or Email` (text, required, placeholder: `e.g. 9876543210 or name@example.com`).
     - `Password` (password, required, autocomplete `current-password`).
  5. Action button: `Sign In` (full-width, disabled on loading with spinner).
  6. Footer navigation: "Don't have an account? Sign Up" link to `/register`.
- **Forms & Validation**:
  - Reactive form (`loginForm`).
  - Required validators on both fields with `.is-invalid` borders and inline error messages.
- **Responsive Behavior**:
  - Desktop: Centered card (`max-width: 440px`), generous canvas margins.
  - Mobile (393px): Full-width card with 16px viewport gutters.
- **Visual & Usability Findings**:
  - Simple, functional authentication card.
  - Password field lacks a "show/hide password" visibility toggle button.
  - No "Forgot Password" recovery flow.

---

### Screen 2: Registration & Phone OTP Verification
- **Route**: `/register` (Unauthenticated)
- **Component**: `RegisterComponent` (`register.component.html`, `register.component.ts`, `register.component.css`)
- **Primary Purpose**: Two-step account creation validating full name, 10-digit national phone number, optional email, and 6-digit SMS OTP verification.
- **Content Hierarchy**:
  - **Step 1 (Details)**:
    1. Header: "Create Account" / "Sign up using your phone number to get started".
    2. Input fields:
       - `Full Name *` (min 2 chars, letters/spaces only).
       - `Mobile Number (10 Digits) *` with fixed `+91` input group addon.
       - `Email Address (Optional)` with email format validation.
       - `Password *` (min 6 characters).
       - `Confirm Password *` with client-side equality validator.
    3. Action: `Send OTP Verification` button.
  - **Step 2 (OTP Verification)**:
    1. Header: "Verify Phone Number".
    2. Verification summary box showing phone number + `Edit Number` button.
    3. Simulated demo OTP alert banner (green/info banner showing code with 10-minute expiry).
    4. Centered 6-digit monospace input box (`letter-spacing: 0.3em`, `font-size: 1.5rem`).
    5. Action row: "Didn't receive code? Resend Code" link + `Verify & Create Account` button.
- **Forms & Validation**:
  - Strict regex patterns (`^[0-9]{10}$` for phone, `^[0-9]{6}$` for OTP).
  - Clean state transitions between Step 1 and Step 2.
- **Responsive Behavior**:
  - Desktop: Centered modal card (`max-width: 480px`).
  - Mobile: Adapts cleanly; keypad triggers `inputmode="numeric"` on phone and OTP fields.
- **Visual & Usability Findings**:
  - High usability with numeric keypads on mobile.
  - Demo OTP alert banner is prominently styled for testing; in production, this should transition to real SMS gateway with fallback timer.

---

### Screen 3: Financial Overview (Dashboard)
- **Route**: `/dashboard` (Authenticated, default landing)
- **Component**: `DashboardComponent` (`dashboard.component.html`, `dashboard.component.ts`, `dashboard.component.css`)
- **Primary Purpose**: Executive monthly cash-flow command center, combining net liquid balance, salary budget variance, emergency fund reserves, interactive Chart.js visualizations, upcoming bill obligations, and recent transactions.
- **Content Hierarchy**:
  1. **Header**:
     - Desktop: Page title ("Financial Overview"), subtitle with active month/year, interactive Month Navigator (`< Month Year >` dropdown selectors), `+ Add Transaction` button, and `Scan Receipt` button.
     - Mobile: Compact sticky month bar with arrow buttons and month/year selects.
  2. **Alert Area**: Dismissible success and error notifications.
  3. **Cash Flow Summary**:
     - Desktop: 3-column KPI strip:
       - `Period Inflow` (+₹, text-income, link to ledger).
       - `Period Outflow` (-₹, text-expense, link to records).
       - `Net Cash Flow` (Net surplus/deficit pill badge).
     - Mobile: Unified hero card displaying large centered Net Cash Flow amount, status pill (Surplus/Deficit), and horizontal split bar with Inflow vs Outflow values.
  4. **Dual Planning Grid (2 columns on desktop, stacked on mobile)**:
     - **Card 1: Salary Plan (`salaryPlan`)**:
       - Take-home pay summary.
       - Progress bar: Actual expenses vs planned allowance.
       - Emergency fund allocation status.
       - Calculated Safety Buffer indicator (`Take-Home - (Expenses + Savings)`).
     - **Card 2: Savings Goals & Safety Net**:
       - Emergency fund milestone gauge with percentage completion.
       - Cumulative savings across all goals with target amount and progress bar.
       - Quick link to `/savings-goals`.
  5. **Financial Visualizations Grid (2 columns)**:
     - **Chart 1: Cash Flow Dynamics**: Chart.js Bar chart comparing monthly Inflow vs Outflow.
     - **Chart 2: Expense Breakdown by Category**: Chart.js Doughnut chart + itemized category progress list with percentages and amounts.
  6. **Upcoming Repeating Commitments & Bills**:
     - Header with "Manage All (N)" button.
     - Desktop: Data table with Commitment name, Category, Frequency badge, Status (Overdue / Due Today / Due in Nd), Amount, and `Record Payment` action button.
     - Mobile: Touch cards with status badges and full-width `Record Payment Now` button.
  7. **Recent Activity Feed**:
     - Desktop: Data table of the last 5 transactions (Description, Category, Date, Type badge, Amount).
     - Mobile: `.mobile-tx-list` with circular avatar icons (Income arrow, Expense arrow, Refund icon), title, category, date, and colored tabular amount.
- **Data Displayed**:
  - `totalIncome`, `totalExpenses`, `balance`
  - `salaryPlan`: `takeHomeSalary`, `actualExpenses`, `plannedExpenses`, `calculatedBuffer`
  - `emergencyFundGoal`: `currentAmount`, `targetAmount`, `progressPercentage`
  - `categoryExpenses`: category name, amount, percentage
  - `upcomingBills`: list of `RecurringPayment`
  - `recentTransactions`: list of `Transaction`
- **Responsive Behavior**:
  - Desktop uses CSS Grid (`grid-template-columns: repeat(3, 1fr)` and `repeat(2, 1fr)`).
  - Mobile collapses all cards to single-column stack, replacing tables with high-touch card items.
- **Visual & Usability Findings**:
  - Very rich data set; mobile screen requires extensive vertical scrolling (approx. 4–5 full viewports).
  - Month navigation controls are duplicated between desktop and mobile headers using `.desktop-only` / `.mobile-only` CSS classes.
  - The Salary Plan card on the dashboard only displays high-level progress; details require navigating to `/salary-plan`.

---

### Screen 4: Transactions Ledger
- **Route**: `/transactions` (Authenticated)
- **Component**: `TransactionListComponent` (`transaction-list.component.html`, `transaction-list.component.ts`, `transaction-list.component.css`)
- **Primary Purpose**: Filterable, searchable, exportable, and printable financial audit ledger.
- **Content Hierarchy**:
  1. **Header**:
     - Desktop: Title, subtitle, `Export CSV` button, `Print Statement` button, `Scan Receipt` button, and `+ Add Transaction` button.
     - Mobile: Title, transaction count, quick `CSV` export, and `+ Add` button.
  2. **Filtered Totals Strip**:
     - Desktop: 3 summary stat cards (`Filtered Inflow`, `Filtered Outflow`, `Net Filtered Cash Flow`).
     - Mobile: Compact 3-column metric bar.
  3. **Filter & Search Toolbar (Card)**:
     - Date Presets row: `This Month`, `Last Month`, `Last 30 Days`, `Last 90 Days`, `This Year`, `All Time`.
     - Custom Date Range: Start Date & End Date pickers.
     - Category Filter dropdown (All Categories + dynamic category list).
     - Type Filter dropdown (`All Types`, `Expenses`, `Income`, `Refunds`).
     - Search input: Real-time search by title or merchant name.
     - Reset Filters link.
  4. **Active Filter Chips**: Removable badge pills showing applied filters.
  5. **Transaction Ledger Content**:
     - Desktop: Table with column headers: `Date`, `Description / Merchant`, `Category`, `Type`, `Amount`, `Actions` (Edit pencil, Delete trash).
     - Mobile: `.mobile-tx-list` containing individual transaction cards with category badge, date, amount, and quick swipe/action buttons.
  6. **Pagination Controls**: `Previous`, Page Numbers (1, 2, ...), `Next`, and item counter (`Showing X - Y of Z entries`).
  7. **Empty State**: Centered illustration, prompt, and `Record First Transaction` button.
- **Forms & Validation**:
  - Date inputs validate `startDate <= endDate`.
  - Search query triggers client-side debounced filter.
- **Responsive Behavior**:
  - Table hidden below 768px (`.desktop-only`); replaced by touch-friendly list (`.mobile-only`).
  - Filter bar stacks vertically on narrow screens.
- **Visual & Usability Findings**:
  - Excellent export features (CSV download + CSS print media stylesheet for statement generation).
  - Filter bar takes up significant vertical space on mobile before reaching the first transaction row.

---

### Screen 5: Transaction Entry & Edit Form
- **Route**: `/transactions/add` & `/transactions/edit/:id` (Authenticated)
- **Component**: `TransactionFormComponent` (`transaction-form.component.html`, `transaction-form.component.ts`, `transaction-form.component.css`)
- **Primary Purpose**: Create or edit individual financial records with instant categorization, date selection, and optional AI scanner referral.
- **Content Hierarchy**:
  1. Card Header: Title ("Record Transaction" / "Edit Transaction"), subtitle, and `← Back to Ledger` link.
  2. AI Helper Banner (Add mode only): Prompt suggesting receipt scanning with Gemini AI with direct button.
  3. Form Fields:
     - `Transaction Type`: 3-way Segmented Control (`Expense`, `Income`, `Refund`).
     - `Title / Merchant *`: Text input with character limits (2–100 chars).
     - `Amount (₹) *`: Number input with tabular numbers and ₹ prefix addon.
     - `Category *`: Dropdown select filtered dynamically based on selected transaction type (Income vs Expense).
     - `Date *`: Date input defaulting to current date (`YYYY-MM-DD`).
     - `Notes / Description (Optional)`: Multi-line textarea.
  4. Action Buttons:
     - `Cancel` button (navigates back to ledger).
     - `Save Transaction` / `Update Transaction` submit button (with loading spinner).
- **Forms & Validation**:
  - Reactive form (`transactionForm`).
  - Validation: Title required (min 2, max 100), Amount required (min 0.01, max 100,000,000), Category required, Date required.
  - Category list automatically repopulates when user switches between Income, Expense, or Refund types.
- **Responsive Behavior**:
  - Centered card (`max-width: 680px`) on desktop.
  - Spans full width on mobile viewports; segmented control remains accessible.
- **Visual & Usability Findings**:
  - Clean, high-focus form layout.
  - Keyboard focus order is logical and validation errors appear directly below each field.

---

### Screen 6: Category Budgets
- **Route**: `/budgets` (Authenticated)
- **Component**: `BudgetListComponent` (`budget-list.component.html`, `budget-list.component.ts`, `budget-list.component.css`)
- **Primary Purpose**: Define monthly spending caps for individual expense categories and monitor real-time utilization progress bars.
- **Content Hierarchy**:
  1. Header: Title, subtitle, and link to `/categories`.
  2. Two-Column Split Layout on Desktop:
     - **Left Column: Budget Form Card**:
       - Title: "Set Category Budget" / "Edit Category Budget".
       - Fields: Expense Category select, Planned Allowance (₹) input, Month select, Year input.
       - Submit button (`Save Budget` / `Update Budget`) + `Cancel` in edit mode.
     - **Right Column: Budgets List & Utilization**:
       - Month & Year filter dropdowns for filtering budgets.
       - Budget Cards Grid: Each card shows Category Name, Month/Year badge, Progress Bar with color coding (`bg-primary` < 75%, `bg-warning` 75-90%, `bg-danger` > 90%), Spent vs Allowance amounts, Remaining buffer, and Edit / Delete buttons.
       - Empty state when no budgets exist for the selected month.
- **Forms & Validation**:
  - Validates positive amount (> 0) and unique category budget per month/year.
- **Responsive Behavior**:
  - Desktop: 40% / 60% split layout.
  - Mobile: Form card stacks above the budget list.
- **Visual & Usability Findings**:
  - Clear progress bar feedback for financial discipline.
  - Stacking the form above the list on mobile causes the list to be pushed down below the fold.

---

### Screen 7: Monthly Salary & Budget Plan
- **Route**: `/salary-plan` (Authenticated)
- **Component**: `SalaryPlanComponent` (`salary-plan.component.html`, `salary-plan.component.ts`, `salary-plan.component.css`)
- **Primary Purpose**: Complete zero-based or envelope budgeting system allowing users to define multiple income sources, allocate funds across categories/savings, calculate safety buffers, and track actual variances.
- **Content Hierarchy**:
  1. **Header & Navigation**:
     - Title, subtitle, Month navigator with arrows and dropdowns, `Manage Categories` link.
     - Action buttons: `Export CSV`, `Print Report`, `Edit Plan`, `Copy from Month`, `Delete Plan`, `+ Create Plan`.
  2. **Unconfigured Empty State**: Large banner showing "No Plan Configured", explanation, action buttons (`+ Create Custom Plan`, `Copy from Prior Month`), and actual transaction totals tracked so far.
  3. **Configured Plan Command Dashboard**:
     - **KPI Summary Grid (4 Cards)**:
       - `Planned Take-Home`: Total of all income streams.
       - `Planned Expenses`: Sum of expense category allocations.
       - `Planned Savings`: Capital earmarked for emergency fund / goals.
       - `Calculated Buffer`: Remaining cushion (`Income - Allocations`). Highlighted red if over-allocated (`Deficit`).
     - **Real-Time Plan vs Actual Variance Comparison Table**:
       - Columns: Category / Stream, Planned Amount, Actual Tracked, Variance (+/- difference), Utilization % bar.
     - **Income Sources Card**: List of itemized streams (e.g. Primary Salary, Freelance, Dividend) with individual amounts.
     - **Expense Allocations Card**: List of category envelopes with percentage of take-home and actual spending comparison.
     - **Savings & Emergency Allocations**: Progress toward target reserves.
  4. **Modals**:
     - `Create / Edit Plan Modal`: Dynamic form arrays for adding multiple income sources and multiple category allocations.
     - `Copy Plan Modal`: Source month/year picker to duplicate existing budgets.
     - `Delete Confirmation Modal`.
- **Responsive Behavior**:
  - Complex desktop dashboard with extensive data tables.
  - Responsive cards on mobile with scrollable tables.
- **Visual & Usability Findings**:
  - Very powerful financial planning tool; one of the deepest features in the app.
  - The plan creation modal has dynamic row additions (`+ Add Income Stream`, `+ Add Allocation`) which can feel cluttered on small screens.

---

### Screen 8: Repeating Commitments & Bills
- **Route**: `/recurring` (Authenticated)
- **Component**: `RecurringPaymentsComponent` (`recurring-payments.component.html`, `recurring-payments.component.ts`, `recurring-payments.component.css`)
- **Primary Purpose**: Lifecycle management of fixed commitments (rent, utilities, SaaS subscriptions, loan EMIs) with auto-calculation of next due dates and 1-tap recording into the transaction ledger.
- **Content Hierarchy**:
  1. Header: Title, subtitle, `Add Repeating Bill` button.
  2. KPI Strip (3 Cards):
     - `Monthly Commitments`: Total normalized monthly baseline expense.
     - `Active Schedules`: Active count vs total count.
     - `Due in Next 7 Days`: Count and sum of impending bills (highlighted with warning border if > 0).
  3. Toolbar: Filter pills (`All`, `Active`, `Due Soon`, `Paused`) and Search input.
  4. Commitments Grid / Table:
     - Desktop Table: Commitment title & notes, Category, Frequency (`Monthly`, `Weekly`, `Yearly`), Due Date badge (Overdue in red, Due Today in amber), Normalized Amount, and Actions (`Record Payment`, `Pause/Resume`, `Edit`, `Delete`).
     - Mobile Cards: Individual bill cards with status chips and primary action button.
  5. Modal: `Create / Edit Recurring Bill Modal`:
     - Fields: Title, Amount, Transaction Type (Expense/Income), Category select, Frequency dropdown, Start Date, Next Due Date override, Notes.
- **Responsive Behavior**:
  - Responsive tables on desktop; card feed on mobile.
- **Visual & Usability Findings**:
  - Excellent workflow: clicking `Record Payment` automatically generates a ledger transaction and updates `nextDueDate` by the frequency period.

---

### Screen 9: Splitwise & Group Bills
- **Route**: `/splitwise` & `/splitwise/:id` (Authenticated)
- **Component**: `SplitwiseComponent` (`splitwise.component.html`, `splitwise.component.ts`, `splitwise.component.css`)
- **Primary Purpose**: Group expense sharing, AI bill parsing, debt settlement calculation, personal contact book, and 1-tap ledger syncing.
- **Content Hierarchy**:
  1. Page Header: Title, subtitle, `Contacts` button (`📇`), and `+ New Group` button.
  2. Group Switcher Carousel / Tabs: Horizontal scrolling tab bar of all user groups with group type icon (`✈️ Trip`, `🏠 Home`, `🏢 Office`, `👥 Other`), group title, and net balance badge (+₹ / -₹).
  3. Empty State (No Groups): Friendly graphic and `Create First Group` button.
  4. Active Group View:
     - Group Header Card: Title, description, group type pill, member count, and member avatar pills.
     - Group Actions Row: `+ Add Expense`, `Scan & Split (AI)`, `Settle Up`, `+ Add Member`.
     - Balance & Settlement Summary Card:
       - 4-metric strip: Total Group Spending, You Paid, Your Share, Your Net Balance (+/-).
       - Debt Simplification Transfers: Clear narrative cards ("You owe Rahul ₹450" or "Sneha owes you ₹1,200") with quick `Settle` button.
     - Group Expenses Feed:
       - Itemized expense cards showing Date badge, Category, Split Method (`EQUAL`, `EXACT`, `PERCENT`), Title, Paid By info, Notes, and Delete button.
       - Individual member shares expansion (`Who owes what`).
       - `1-Tap Sync to Ledger` button: Pulls the user's personal share into their main transaction ledger with automatic linking.
  5. Modals & Drawers:
     - **Create Split Group Modal**: Group Name, Type selector pills, Initial Members input (with embedded `📇` phonebook icon and contacts drawer launcher), Description.
     - **Add Member Modal**: Member Name, Phone/Email, and `📇 Contacts` trigger.
     - **Add Expense Modal**: Title, Amount, Paid By dropdown, Split Method tabs, Category selector, Notes, and itemized share rows.
     - **AI Group Split Modal**: Receipt image upload / camera capture, text prompt for custom orders ("Rahul: Burger, Sneha: Pizza"), Gemini AI processing, and structured split preview.
     - **Settle Up Modal**: Payer, Recipient, Amount, Date, and `Record in Personal Ledger` toggle.
     - **Contacts Book Drawer**: Search bar (local cache + PostgreSQL DB query), contact cards with real name, custom nickname badge, phone number, email, group count, inline nickname editor (`✏️`), and sticky top `[ ✓ Add Selected (N) ]` button.
     - **Custom Nickname Editor Modal**: User-specific local contact renaming.
- **Responsive Behavior**:
  - Horizontal tab scrolling for group selector.
  - Modals adapt to mobile viewports with sticky footers and scrollable drawer bodies.
- **Visual & Usability Findings**:
  - Highly sophisticated feature set.
  - The embedded `📇` icon inside the input field and top contacts drawer provide excellent contact reuse without redundant database calls.

---

### Screen 10: Savings Goals & Capital Reserves
- **Route**: `/savings-goals` (Authenticated)
- **Component**: `SavingsGoalsComponent` (`savings-goals.component.html`, `savings-goals.component.ts`, `savings-goals.component.css`)
- **Primary Purpose**: Track emergency funds, milestone purchases, and capital allocations with progress tracking and contribution history.
- **Content Hierarchy**:
  1. Header: Title, subtitle, `Salary Plan` link, and `+ Add Goal` button.
  2. Summary Dashboard Cards (4 Cards):
     - `Total Target`: Cumulative financial target.
     - `Total Saved`: Cumulative capital currently accumulated.
     - `Overall Progress`: Combined percentage gauge with progress bar.
     - `Completed Goals`: Completed count vs active count.
  3. Goals Grid: Cards displaying Goal Name, Completion badge, Target Amount, Current Amount, Target Date, Days Remaining, Progress Bar, and Action buttons:
     - `+ Contribute`: Modal to log a new savings deposit.
     - `History`: Modal to view past deposit logs.
     - `Edit`: Update target amount or target date.
     - `Delete`: Remove goal.
  4. Modals: Create/Edit Goal Modal, Contribution Modal, Contribution History Modal.
- **Responsive Behavior**:
  - Grid switches from 3 columns on desktop to 1 column on mobile.
- **Visual & Usability Findings**:
  - Clear, motivating visual progress bars.
  - Contribution flow is clean and keeps deposit history organized.

---

### Screen 11: AI Receipt & Statement Scanner
- **Route**: `/ai-scanner` (Authenticated)
- **Component**: `AiScannerComponent` (`ai-scanner.component.html`, `ai-scanner.component.ts`, `ai-scanner.component.css`)
- **Primary Purpose**: Multimodal AI receipt parser powered by Google Gemini AI, converting physical receipts or digital invoices into structured transactions.
- **Content Hierarchy**:
  1. Header: Gemini Engine badge, Active Key status pill, Title, Subtitle, and `Gemini Key Settings` button.
  2. Quota / Key Limit Warning Banner (conditional): Appears when free quota is exceeded with direct link to update API key.
  3. Upload & Input Card (Step 1):
     - Tabs: `Receipt Photos` vs `Paste Statement Text`.
     - Image Tab: Drag-and-drop zone with camera capture trigger, file browser, and multi-file thumbnail strip with individual delete buttons.
     - Text Tab: Large textarea for pasting raw bank SMS messages or invoice text.
     - Action: `Scan & Parse with Gemini AI` button with pulse animation during execution.
  4. Extracted Transaction Review Card (Step 2):
     - Appears upon successful AI analysis.
     - Form pre-filled with AI extracted values:
       - Merchant / Title (editable).
       - Amount (editable).
       - Category (auto-matched against existing user categories).
       - Transaction Date.
       - Type (Expense / Income / Refund).
       - Notes / AI Raw Summary.
     - Action buttons: `Save to Ledger`, `Scan Another`, `Discard`.
  5. Gemini API Key Configuration Modal:
     - Allows users to enter their own Google Gemini API key (`AIza...`) stored securely in localStorage, with validation and test ping.
- **Responsive Behavior**:
  - Camera shutter works directly on mobile devices via `<input type="file" accept="image/*" capture="environment">`.
- **Visual & Usability Findings**:
  - Outstanding AI capability with fast extraction.
  - Custom API key fallback ensures users are never blocked by global quota limits.

---

### Screen 12: Categories & Classification
- **Route**: `/categories` (Authenticated)
- **Component**: `CategoryListComponent` (`category-list.component.html`, `category-list.component.ts`, `category-list.component.css`)
- **Primary Purpose**: Manage classification taxonomy (Income, Expense, Savings) for accurate budgeting and transaction categorization.
- **Content Hierarchy**:
  1. Header: Title, subtitle, and `Show Archived` toggle.
  2. Split Layout:
     - **Left: Category Form Card**: Category Name input, Classification radio options (`Expense`, `Income`, `Savings`), and Save button.
     - **Right: Categories Table / List**: Active categories table with Name, Type badge, Transaction count, and Actions (`Edit`, `Archive`, `Delete`).
- **Responsive Behavior**:
  - Two columns on desktop; stacked on mobile.
- **Visual & Usability Findings**:
  - Clean master-detail management.
  - System categories are protected against accidental deletion.

---

### Screen 13: More Hub / User Settings & Account
- **Route**: `/more` (Authenticated, Mobile Navigation Hub)
- **Component**: `MoreComponent` (`more.component.html`, `more.component.ts`, `more.component.css`)
- **Primary Purpose**: Centralized hub on mobile viewports for account settings, profile information, Gemini key configuration, PWA installation, and secondary navigation links.
- **Content Hierarchy**:
  1. Profile Header Card: User avatar circle, Full Name, Email, and Three-Dots Action Menu (Gemini Key, Alerts, Install App).
  2. PWA Install Prompt Banner: One-tap installation banner for progressive web app home screen pinning.
  3. Section 1: Planning & Commitments (Links to Alerts, Salary Plan, Recurring Payments, Savings Goals, Splitwise).
  4. Section 2: Financial Management (Links to Categories, AI Scanner, Transactions, Budgets).
  5. Section 3: Data & Preferences (Export All Data as JSON/CSV, Dark Mode toggle).
  6. Section 4: Account Actions (`Sign Out` button with confirmation dialog).
- **Responsive Behavior**:
  - Primarily targeted for mobile screens (`< 768px`), but functions as a clean settings page on desktop.
- **Visual & Usability Findings**:
  - Very well organized; ensures no feature is lost despite the 4-item bottom navigation bar on mobile.

---

### Screen 14: Notification Center (Global Slide-Over Drawer)
- **Selector**: `<app-notification-center>` (Global Overlay)
- **Component**: `NotificationCenterComponent` (`notification-center.component.html`, `notification-center.component.ts`, `notification-center.component.css`)
- **Primary Purpose**: Slide-over drawer and heads-up alert banner displaying actionable alerts for overdue recurring bills, budget thresholds exceeded, and Splitwise debt reminders.
- **Content Hierarchy**:
  1. Heads-Up Toast Banner: Floating card at top of viewport for urgent alerts with `View →` and `✕` dismiss button.
  2. Drawer Header: "Financial Alerts", unread count pill, `⚡ Send Test` button, and close button.
  3. Lock-Screen Notification Banner: Native Android/Web notification permission toggle.
  4. Filter Tabs: `All`, `⏰ Bills & Rent`, `⚠️ Budgets`, `💸 Splitwise Debts`, `🔔 System`.
  5. Notification Cards Feed: Icon, Title, Body, Timestamp, Action Link button (deep links to relevant screen), and Dismiss button.
  6. Drawer Footer: `Mark All as Read` and `Clear All` buttons.
- **Visual & Usability Findings**:
  - Highly engaging micro-interaction; provides proactive financial intelligence.

---

### Screen 15: Global Confirmation Dialog
- **Selector**: `<app-confirm-dialog>` (Global Overlay)
- **Component**: `ConfirmDialogComponent` (`confirm-dialog.component.html`, `confirm-dialog.component.ts`, `confirm-dialog.component.css`)
- **Primary Purpose**: Accessible, type-safe confirmation modal for high-impact actions (deleting transactions, deleting budgets, canceling plans, logging out).
- **Content Hierarchy**:
  - Backdrop with blur effect.
  - Dialog card with mobile bottom-sheet pill.
  - Semantically colored icon container (Trash for delete, Alert for warning, Logout for sign out).
  - Dialog Title and Message.
  - Action buttons: `Cancel` and `Confirm` (danger or primary styling).

---

## 3. Verified Route Map

| Path | Component | Auth Required | Purpose |
| :--- | :--- | :--- | :--- |
| `/login` | `LoginComponent` | No | Email or 10-digit phone login |
| `/register` | `RegisterComponent` | No | Phone OTP registration flow |
| `/dashboard` | `DashboardComponent` | Yes (Guard) | Executive cash-flow dashboard & overview |
| `/transactions` | `TransactionListComponent` | Yes (Guard) | Filterable, exportable transactions ledger |
| `/transactions/add` | `TransactionFormComponent` | Yes (Guard) | Create new transaction |
| `/transactions/edit/:id` | `TransactionFormComponent` | Yes (Guard) | Edit existing transaction |
| `/budgets` | `BudgetListComponent` | Yes (Guard) | Monthly category budget ceilings |
| `/salary-plan` | `SalaryPlanComponent` | Yes (Guard) | Zero-based salary & envelope allocation planner |
| `/recurring` | `RecurringPaymentsComponent` | Yes (Guard) | Repeating subscriptions, rent & bill schedules |
| `/splitwise` | `SplitwiseComponent` | Yes (Guard) | Group bills, AI receipt split, contacts & debt settlements |
| `/splitwise/:id` | `SplitwiseComponent` | Yes (Guard) | Direct deep-link to specific split group |
| `/savings-goals` | `SavingsGoalsComponent` | Yes (Guard) | Emergency fund & capital milestone goals |
| `/ai-scanner` | `AiScannerComponent` | Yes (Guard) | Gemini AI multimodal receipt parser |
| `/categories` | `CategoryListComponent` | Yes (Guard) | Income/Expense category classification |
| `/more` | `MoreComponent` | Yes (Guard) | Mobile navigation hub, profile & settings |
| `''` | Redirect to `/dashboard` | — | Default route |
| `'**'` | Redirect to `/dashboard` | — | Catch-all wildcard |
