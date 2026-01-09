# Leanback UI Refactor Plan

This document outlines the plan to completely refactor the existing TV UI into a modern, immersive experience.

## 1. Design Philosophy & Architecture

The new UI will move away from the traditional simple list view to a **Content-First** approach, common in modern streaming applications (e.g., Netflix, YouTube TV).

### Core Layout Structure
- **Root Layout**: A horizontal container dividing the screen.
- **Left Rail (Navigation)**: A persistent, collapsible sidebar for primary navigation (Search, Home, Movies, TV, Settings).
  - *Collapsed State*: Shows icons only (saving space).
  - *Expanded State*: Shows icons + labels (when focused).
- **Main Content Area**: The remaining space on the right, hosting the active Fragment.

### Visual Style (Dark Theme)
- **Background**: Deep dark background (`#0F0F0F` or similar) to make content pop.
- **Focus Indicators**: 
  - High-contrast borders (`#FFFFFF` or Brand Color).
  - Scale/Zoom effect on focused cards (1.1x).
  - Elevation/Shadows for depth.
- **Typography**:
  - **Headlines**: Bold, sans-serif for content titles.
  - **Metadata**: Lighter, smaller text for year, rating, duration.

## 2. Component Design

### A. Navigation Rail (`NavRailFragment` / `CustomNavigationView`)
- **Structure**: Vertical list of icons/labels.
- **Interaction**:
  - Default focus on "Home".
  - Pressing RIGHT moves focus to Content Area.
  - Pressing LEFT from Content Area moves focus back to Rail.
- **Items**: Search (Icon), Home, Vod (Movies/TV), Live, History, Settings.

### B. Home Screen (`HomeFragment`)
- **Hero Banner (Top)**:
  - Large background image (fanart) of the featured/highlighted item.
  - Gradient overlay for text readability.
  - Title, Synopsis, and Action Buttons ("Play", "Details").
- **Swimlanes (Rows)**:
  - Horizontal lists (`HorizontalGridView`) for categories (e.g., "Trending Now", "History", "New Releases").
  - Each item is a **Content Card**.

### C. Content Card (`VodPresenter`)
- **Layout**: Image view with aspect ratio (Poster 2:3 or Backdrop 16:9).
- **Focus State**: 
  - Scale up.
  - Show thick white border.
  - Display Title/Subtitle below or overlay on focus.

## 3. Implementation Steps

### Phase 1: Foundation & Resources
1.  **Clean Up**: Remove unused legacy layouts if completely replacing.
2.  **Styles & Colors**: Define the new Color Palette and Styles in `res/values`.
    - Define `Theme.Leanback.Modern`.
    - Add colors for Background, Surface, Primary, Focus.
3.  **Drawables**: Create backgrounds, selectors (focus states), and icons.

### Phase 2: Navigation Architecture
1.  **Layout Refactor (`activity_home.xml`)**:
    - Change root to `ConstraintLayout` or `LinearLayout`.
    - Add `FrameLayout` for Side Nav.
    - Add `FrameLayout` for Main Content.
2.  **Side Navigation Component**:
    - Create `SideMenuFragment` or a custom `VerticalGridView` for the menu.
    - Implement focus handling logic (expand/collapse).

### Phase 3: Home UI Implementation
1.  **Hero Banner**:
    - Create `view_home_hero.xml`.
    - Implement logic to update banner based on the focused item in the row below (optional) or static featured content.
2.  **Content Rows**:
    - Refactor `HomeFragment` to use `VerticalGridView` containing multiple `HorizontalGridView` rows.
    - Update `VodPresenter` to render the new **Modern Card** layout.

### Phase 4: Browsing & Details
1.  **VodFragment**: Update to share the same card styles and grid layout.
2.  **Detail View**: (If scope permits) Create a modal or new Activity for content details that matches the new design language.

## 4. Todo List
- [ ] **Styles**: Define `colors.xml` and `styles.xml` for the new Dark Theme.
- [ ] **Layout**: Create `activity_home_modern.xml` with Side Nav structure.
- [ ] **Nav**: Implement `SideMenuAdapter` and view.
- [ ] **Cards**: Create `item_vod_card_modern.xml` and update `VodPresenter`.
- [ ] **Hero**: Implement `HeroPresenter` or static header in `HomeFragment`.
- [ ] **Logic**: Connect `HomeActivity` to switch fragments based on Side Nav selection.
- [ ] **Focus**: Test and refine D-pad navigation between Nav and Content.