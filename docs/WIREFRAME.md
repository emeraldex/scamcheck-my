# ScamCheck MY — Wireframe (v1)

Low-fidelity layout for the single page ([ADR-006](adr/006-static-web-ui.md)). Drawn at phone width (360 px), which is the primary target (SRS US-13). On desktop the same column is centred, max 640 px wide.

Text shown in English; every string has a BM version (US-09).

## 1. Input state

```
┌────────────────────────────────────┐
│ ScamCheck MY              [BM | EN]│  ← language toggle (US-09)
├────────────────────────────────────┤
│ Is this a scam?                    │
│ Paste a message, link, phone       │
│ number or account number.          │
│                                    │
│ ┌────────────────────────────────┐ │
│ │                                │ │  ← one text box (US-01)
│ │  Paste here...                 │ │     max 5,000 chars,
│ │                                │ │     counter shown near limit
│ │                                │ │
│ └────────────────────────────────┘ │
│                        0 / 5000    │
│ ┌────────────────────────────────┐ │
│ │            CHECK               │ │  ← ≥ 44 px tall (US-13)
│ └────────────────────────────────┘ │
│        Try an example              │  ← Could (US-14)
│                                    │
│ 🔒 We don't store what you paste.  │  ← US-10 AC3
├────────────────────────────────────┤
│ Not affiliated with PDRM, NSRC or  │  ← every page (US-12)
│ any bank. This tool gives advice   │
│ only.                              │
└────────────────────────────────────┘
```

Empty input → inline message under the box: "Please paste something to check." (US-01 AC3).

## 2. Result state (High)

The result appears below the Check button; the page scrolls to it.

```
┌────────────────────────────────────┐
│ ┌────────────────────────────────┐ │
│ │ ▲ HIGH RISK                    │ │  ← text + colour + icon,
│ │ Several strong scam warning    │ │     never colour alone (US-13)
│ │ signs were found.              │ │
│ └────────────────────────────────┘ │
│                                    │
│ What to do now                     │
│ Do not transfer money or click the │  ← tip by level (US-08 AC3)
│ link. If you have already paid,    │
│ call 997 immediately.              │
│                                    │
│ ┌────────────────────────────────┐ │
│ │      📞  Call 997 (NSRC)       │ │  ← tel:997 (US-08 AC2)
│ └────────────────────────────────┘ │
│ ┌────────────────────────────────┐ │
│ │  Check on Semak Mule  ↗        │ │  ← new tab (US-08 AC4)
│ └────────────────────────────────┘ │
│                                    │
│ Warning signs found (4)            │  ← strongest first (US-07)
│ ┌────────────────────────────────┐ │
│ │ ●●● Promise of guaranteed      │ │
│ │     returns                    │ │
│ │ "dijamin untung"               │ │  ← matched text
│ │ Real investments never         │ │  ← plain explanation
│ │ guarantee profit...            │ │
│ └────────────────────────────────┘ │
│ ┌────────────────────────────────┐ │
│ │ ●●● Link pretends to be a bank │ │
│ │ "maybank2u-secure.xyz"         │ │
│ │ This link uses a bank's name   │ │
│ │ but is not the bank's...       │ │
│ └────────────────────────────────┘ │
│ ┌────────────────────────────────┐ │
│ │ ●●○ Pressure to act quickly    │ │
│ │ "segera"                       │ │
│ │ ...                            │ │
│ └────────────────────────────────┘ │
│   ... more findings                │
│                                    │
│        Check another message       │  ← clears box, scrolls up
└────────────────────────────────────┘
```

Strength dots: `●●●` strong · `●●○` moderate · `●○○` weak · `○○○` info.

## 3. Result state (Low)

```
┌────────────────────────────────────┐
│ ┌────────────────────────────────┐ │
│ │ ● LOW RISK                     │ │
│ │ No common warning signs found. │ │  ← never "safe" (US-02 AC2, R1)
│ │ This does not mean it is safe. │ │
│ └────────────────────────────────┘ │
│                                    │
│ What to do now                     │
│ If you are still unsure, don't     │
│ share personal details and check   │
│ with the sender through an         │
│ official channel.                  │
│                                    │
│ [ 📞 Call 997 (NSRC) ]             │  ← still shown (US-08 AC1)
│ [ Check on Semak Mule ↗ ]          │
└────────────────────────────────────┘
```

## 4. Visual notes

| Element | Rule |
|---|---|
| Base font | ≥ 16 px, system font stack |
| Risk colours | High red, Medium amber, Low grey-green; each paired with text and an icon; WCAG AA contrast |
| Branding | Neutral; no government or bank logos (US-12 AC2, R6) |
| Errors | Inline under the box, plain language, never echo the input |
| Loading | Button shows "Checking…" and is disabled until the response arrives |
