---
name: Multi-Role Marketplace Catalog
colors:
  surface: '#f1fcf4'
  surface-dim: '#d1ddd5'
  surface-bright: '#f1fcf4'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#ebf6ef'
  surface-container: '#e5f1e9'
  surface-container-high: '#dfebe3'
  surface-container-highest: '#dae5de'
  on-surface: '#141e19'
  on-surface-variant: '#3f4946'
  inverse-surface: '#28332e'
  inverse-on-surface: '#e8f4ec'
  outline: '#6f7976'
  outline-variant: '#bec9c5'
  surface-tint: '#1b6a5d'
  primary: '#004e44'
  on-primary: '#ffffff'
  primary-container: '#17675b'
  on-primary-container: '#9ae2d3'
  inverse-primary: '#8cd4c5'
  secondary: '#57615c'
  on-secondary: '#ffffff'
  secondary-container: '#dbe5df'
  on-secondary-container: '#5d6762'
  tertiary: '#6c3424'
  on-tertiary: '#ffffff'
  tertiary-container: '#894b39'
  on-tertiary-container: '#ffc8b9'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#a7f1e1'
  primary-fixed-dim: '#8cd4c5'
  on-primary-fixed: '#00201b'
  on-primary-fixed-variant: '#005046'
  secondary-fixed: '#dbe5df'
  secondary-fixed-dim: '#bfc9c3'
  on-secondary-fixed: '#151d1a'
  on-secondary-fixed-variant: '#3f4945'
  tertiary-fixed: '#ffdbd1'
  tertiary-fixed-dim: '#ffb5a0'
  on-tertiary-fixed: '#380d02'
  on-tertiary-fixed-variant: '#6f3726'
  background: '#f1fcf4'
  on-background: '#141e19'
  surface-variant: '#dae5de'
typography:
  headline-xl:
    fontFamily: Inter
    fontSize: 2rem
    fontWeight: '600'
    lineHeight: 2.5rem
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 1.5rem
    fontWeight: '600'
    lineHeight: 2rem
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Inter
    fontSize: 1.25rem
    fontWeight: '600'
    lineHeight: 1.75rem
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 1rem
    fontWeight: '600'
    lineHeight: 1.5rem
    letterSpacing: -0.005em
  title-price:
    fontFamily: Inter
    fontSize: 1.125rem
    fontWeight: '600'
    lineHeight: 1.5rem
    letterSpacing: -0.01em
  body-md:
    fontFamily: Inter
    fontSize: 0.875rem
    fontWeight: '400'
    lineHeight: 1.25rem
    letterSpacing: 0em
  body-md-medium:
    fontFamily: Inter
    fontSize: 0.875rem
    fontWeight: '500'
    lineHeight: 1.25rem
    letterSpacing: 0em
  body-sm:
    fontFamily: Inter
    fontSize: 0.75rem
    fontWeight: '400'
    lineHeight: 1rem
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 0.875rem
    fontWeight: '600'
    lineHeight: 1.25rem
    letterSpacing: 0.005em
  label-sm:
    fontFamily: Inter
    fontSize: 0.75rem
    fontWeight: '600'
    lineHeight: 1rem
    letterSpacing: 0.02em
  label-xs:
    fontFamily: Inter
    fontSize: 0.6875rem
    fontWeight: '600'
    lineHeight: 0.875rem
    letterSpacing: 0.03em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-mobile: 0.75rem
  margin: 1.5rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system delivers a focused, high-density, professional marketplace environment tailored for high-frequency operations, cross-role workflows (buyers, merchants, inventory coordinators, and marketplace administrators), and fast-scanning SKU evaluation.

The visual direction combines **Functional Minimalism** with **Pragmatic Modernism**:
- High visual density without optical friction, optimizing the number of visible listings, inventory states, and pricing breakdowns per viewport.
- Institutional trustworthiness grounded in deep mineral tones and balanced off-white backdrops, shifting the focus completely to catalog data, search precision, and catalog fulfillment statuses.
- Tactical micro-interactions: quick actions, direct quantity steppers, batch selections, and rapid status verification designed for repetitive commercial use.

## Colors

The palette establishes an ergonomic, daylight-neutral backdrop engineered for sustained operational use across extensive catalog sessions.

### Application Rules
- **Canvas (`#F7F8F5`)**: Global canvas background providing high contrast against elevated surfaces without the glare of pure white.
- **Surface (`#FFFFFF`)**: Primary cards, modals, table rows, sliding panels, and nested containers.
- **Structural Outlines (`#E1E6DF`)**: Fine boundary definition for tables, cards, filters, and field borders.
- **Primary Brand (`#17675B`)**: Primary buttons, critical CTAs, active segmented controllers, link interactions, and brand markers.
- **Active / Accent Tint (`#E8F2EC`)**: Subtle structural highlights, selected row states, badge backdrops, and active navigation items, paired strictly with `#17675B` text/iconography.
- **Text & Hierarchy**:
  - Primary Content: `#202C28` for titles, SKUs, high-impact attributes, and monetary values.
  - Secondary / Supporting: `#69746E` for meta-labels, timestamps, category breadcrumbs, and disabled states.

### Status & Inventory Color Tokens
- **In Stock (Available)**: Text `#2E7D32` over Fill `#E8F5E9` with matching subtle border `#C8E6C9`.
- **Low Stock (Last Units)**: Text `#ED6C02` over Fill `#FFF4E5` with matching subtle border `#FFE0B2`.
- **Out of Stock**: Text `#D32F2F` over Fill `#FFEBEE` with matching subtle border `#FFCDD2`.

## Typography

The type system is standardized around **Inter**, operating on a compact 14px (`0.875rem`) base size to support data-dense catalog grids, inventory tables, and order specifications.

- **Weight Allocation**:
  - `600 (Semi-bold)`: Used exclusively for card headings, SKU codes, prices, status tags, and action buttons to create an immediate hierarchy.
  - `500 (Medium)`: Used for table header columns, interactive tab items, and filter counts.
  - `400 (Regular)`: Used for catalog descriptions, metadata, tooltips, and secondary specifications.
- **Tabular Figures**: Numeric pricing, SKU identifiers, order quantities, and dimensions utilize `font-feature-settings: "tnum" 1` to ensure vertical column alignment across data tables and pricing grids.

## Layout & Spacing

A compact 12-column grid layout built for seamless switching between grid and tabular marketplace views.

### Structure & Grids
- **Desktop (1280px+)**: 12 columns, 16px gutter (`gutter`), 24px canvas margin (`margin`). Supports persistent faceted filter sidebars (280px) and 4-to-5 column card matrices.
- **Tablet (768px - 1279px)**: 8 columns, 16px gutter, 20px canvas margin. Sidebars convert into sliding sheets; cards collapse to 3 columns.
- **Mobile (320px - 767px)**: 4 columns, 12px gutter (`gutter-mobile`), 16px canvas margin (`margin-mobile`). Dual-column product layout with sticky operational bottom action bars.

### Density Rules
- Component paddings default to 12px (`space-md`) on cards and 8px-12px vertical padding on tabular rows.
- Compact metadata blocks use 4px (`space-xs`) vertical gaps to anchor label-value pairs without breaking scanning flow.

## Elevation & Depth

This design system avoids high-contrast drop shadows, relying on **tactile border separation** and subtle atmospheric diffusion to maintain clarity across dense layouts.

- **Level 0 (Flat Surface)**: `#FFFFFF` against canvas `#F7F8F5`, isolated by a solid 1px border of `#E1E6DF`. Used for static catalog cards, data tables, and inline filters.
- **Level 1 (Card Hover / Focused Control)**: Border `#17675B` (or darker neutral `#C5CEC4`) accompanied by an ambient soft shadow: `0 4px 12px -2px rgba(32, 44, 40, 0.06), 0 2px 4px -1px rgba(32, 44, 40, 0.04)`.
- **Level 2 (Popovers, Dropdowns, Flyout Menus)**: Surface `#FFFFFF` enclosed by 1px `#E1E6DF` with shadow `0 10px 24px -4px rgba(32, 44, 40, 0.08), 0 4px 8px -2px rgba(32, 44, 40, 0.04)`.
- **Level 3 (Modal Overlays, Role-Switching Dialogs)**: Surface `#FFFFFF` with shadow `0 20px 32px -8px rgba(32, 44, 40, 0.12)`. Modal backdrops apply `#202C28` with 40% opacity.

## Shapes

The geometry balances structured architectural framing with user-friendly ergonomics:

- **Buttons, Form Inputs, Badges, & Interactive Pills**: Explicitly set to **8px** (`0.5rem`). Provides a refined, click-friendly touch target while preserving structured alignment in dense toolbars.
- **Product Cards, Containers, & Panels**: Explicitly set to **12px** (`0.75rem`). Ensures product cards and nested groups remain distinct modules without clipping nested content.
- **Status Tags / Micro-Chips**: 6px to 8px radius to keep edge geometry parallel with adjacent input components.

## Components

### Buttons
- **Primary**: Background `#17675B`, text `#FFFFFF`, border-radius 8px. Hover state deepens to `#114E45`. Active state scales subtly (98%). Height: 36px (dense) or 40px (standard). Weight: 600.
- **Secondary / Highlight**: Background `#E8F2EC`, text `#17675B`, border 1px solid transparent, border-radius 8px. Hover: background `#DCECE2`.
- **Outline / Neutral**: Background `#FFFFFF`, text `#202C28`, border 1px solid `#E1E6DF`, border-radius 8px. Hover: border `#69746E`.

### Cards (Product Catalog & Listing)
- Built on surface `#FFFFFF` with 1px border `#E1E6DF` and border-radius 12px.
- Internal padding: 12px (`space-md`) to 16px (`space-lg`).
- Image container nested with an internal 8px radius, enclosed by a 1px inner outline (`rgba(0,0,0,0.04)`).
- Clear functional zones: Media & Quick-view button -> SKU & Secondary Attributes (`#69746E`, 12px) -> Product Title (`#202C28`, 14px, 600) -> Inventory Pill Tag -> Price (`#202C28`, 18px, 600) + Action Trigger.

### Stock Status Chips
- Height 24px, radius 6px to 8px, font-size 12px, weight 600, padding 2px 8px.
- **Available**: Background `#E8F5E9`, text `#2E7D32`. Optional 6px solid dot indicator in `#2E7D32`.
- **Last Units**: Background `#FFF4E5`, text `#ED6C02`.
- **Out of Stock**: Background `#FFEBEE`, text `#D32F2F`.

### Inputs & Filters
- **Text & Numeric Inputs**: Background `#FFFFFF`, border 1px solid `#E1E6DF`, border-radius 8px, text `#202C28`, placeholder `#69746E`. Focused state uses border `#17675B` and a subtle 2px glow `rgba(23, 103, 91, 0.15)`.
- **Role-Switcher Selector**: Segmented pill component with surface container `#F7F8F5` and 1px `#E1E6DF` border. Active item elevates via `#FFFFFF` pill, `#17675B` text, and subtle elevation Level 1.

### Checkboxes & Radios
- Size: 16px x 16px, border 1.5px solid `#69746E`, border-radius 4px (checkbox) or 50% (radio).
- Checked: Background `#17675B`, border-color `#17675B`, icon color `#FFFFFF`.

### Multi-Role Context Banner
- Compact 32px height utility strip mounted atop catalog views: displays current acting role (Merchant, Purchasing Agent, Catalog Admin) using background `#E8F2EC`, text `#17675B`, and a fast-action switch modal toggle.