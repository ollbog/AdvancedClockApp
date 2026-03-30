# Advanced Clock

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-purple.svg)](https://kotlinlang.org)

A highly customizable clock and date widget for your Android home screen, built with Jetpack Compose Glance.

<!-- Add a screenshot here once available -->
<!-- ![Screenshot](docs/screenshot.png) -->

## Features

- **Two widget styles** — Normal (compact) and Advanced (multi-line, fully custom layout)
- **Custom layout templates** — Define your own layout with macros: `${TIME}`, `${DATE}`, `${DAY}`, `${WEEK}`, `${ALARM}`, and more
- **Per-element time zones** — Show multiple time zones in a single widget
- **Full style control** — Font size, color (including Material You dynamic color), text alignment
- **Week number support** — ISO, US, or locale-based week numbering
- **Preset system** — Save and restore your favorite configurations
- **No data collection** — Fully offline, zero permissions beyond alarm scheduling

## Requirements

- Android 8.0 (API 26) or higher

## Install

<!-- Replace with real Play Store link when published -->
<!-- [![Get it on Google Play](https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png)](https://play.google.com/store/apps/details?id=com.advancedclock.app) -->

*Coming soon on Google Play.*

## Privacy

Advanced Clock collects no personal data. See the full [Privacy Policy](https://ollbog.github.io/AdvancedClockApp/privacy-policy.html).

## Building

```bash
# Debug build
./gradlew assembleDebug

# Signed release AAB (requires env vars — see CI/CD setup)
./gradlew bundleRelease
```

## CI/CD

Releases are automated via GitHub Actions + Gradle Play Publisher. Pushing a `v*` tag builds a signed AAB and uploads it to the Play Store internal track automatically.

## Contributing

Bug reports and feature requests are welcome via [GitHub Issues](https://github.com/ollbog/AdvancedClockApp/issues).

## License

[MIT](LICENSE) © 2026 Olle Bogatir
