# Classic Tic Tac Toe

A free, open-source Android implementation of the classic noughts and crosses game. No advertisements. No telemetry. No internet required. Available on F-Droid.

[![F-Droid](https://img.shields.io/f-droid/v/com.itsfrz.tictactoe?label=F-Droid)](https://f-droid.org/packages/com.itsfrz.tictactoe/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-6.0%2B-green)](https://f-droid.org/packages/com.itsfrz.tictactoe/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-orange)](https://kotlinlang.org/)
![Visitors](https://visitor-badge.laobi.icu/badge?page_id=itsfrz.tictactoe&style=flat-square)

---

## Overview

Classic Tic Tac Toe, Offline-first  game built with a single principle in mind: respect the user.

FOSS, primary distribution through F-Droid for more trasparent releases.

---

## Features

- Fully offline — no network permission required
- Customisable emoji pieces via an in-app coin store
- Persistent scoring history across sessions
- Custom theming, across game style
- Reproducible builds verified by F-Droid
- Minimal permissions footprint
- Actively maintained with regular releases

---

## Download

The recommended installation method is through the F-Droid client, which handles signature verification and update notifications automatically.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="75">](https://f-droid.org/packages/com.itsfrz.tictactoe/)
[<img src="https://github.com/user-attachments/assets/da9ac9a0-b388-4b81-8b10-ca66690e7d91" alt="Get it on GitHub" height="60">](https://github.com/ItsFRZ/TicTacToe/releases)


Direct APK downloads with PGP signatures are also available on the [F-Droid package page](https://f-droid.org/packages/com.itsfrz.tictactoe/) and [GitHub Releases]
(https://github.com/ItsFRZ/TicTacToe/releases). Manual installs will not receive automatic update notifications.

**Requires:** Android 6.0 (API 23) or higher

---

## Building from Source

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 11 or higher
- Android SDK platform API 23+

### Steps

```sh
git clone https://github.com/ItsFRZ/TicTacToe.git
cd TicTacToe
git checkout fdroid
./gradlew assembleDebug
```

The resulting APK will be located at `app/build/outputs/apk/debug/`.

To verify that your build matches the F-Droid distribution, refer to the [reproducibility status page](https://verification.f-droid.org/packages/com.itsfrz.tictactoe/).

---

## Project Structure

The repository is organised as a multi-module Gradle project:

| Module | Purpose |
|---|---|
| `app` | Main Android application — UI, navigation, and entry point |
| `minimax` | Self-contained game logic implementing the Minimax algorithm |
| `goonline` | Multiplayer / online play module |
| `fastlane` | F-Droid and store metadata, screenshots, and changelogs |

---

## Contributing

Contributions are welcome and encouraged. This project follows the same norms as most open-source Android projects: open an issue before beginning significant work, keep pull requests focused, and write clear commit messages.

Ways to contribute:

- **Bug reports** — open an issue on [GitHub](https://github.com/ItsFRZ/TicTacToe/issues) with steps to reproduce, device model, and Android version
- **Feature requests** — open an issue describing the use case and proposed solution
- **Code contributions** — fork the repository, work on a feature branch, and submit a pull request against `fdroid`
- **Translations** — add or improve locale strings under `app/src/main/res/values-*/`
- **Documentation** — improvements to this README or inline code documentation are always appreciated

Please be respectful. This is a project maintained in personal time.

---

## Changelog

See [GitHub Releases](https://github.com/ItsFRZ/TicTacToe/releases) for a full history.

**v1.0.1** — Bug fixes, performance improvements, scoring history, coin-based emoji store  
**v1.0** — Initial stable release

---

## Privacy

This application requests no network access and collects no user data. There are no third-party SDKs, no analytics libraries, and no crash reporting services. All data (scores, preferences) is stored locally on the device and never transmitted.

The only permission declared is `com.itsfrz.tictactoe.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, which is an internal broadcast protection required by Android's notification API.

---

## License

```
MIT License

Copyright (c) 2026 Faraz Sheikh

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## Contact

**Faraz Sheikh** — [faraz.ar.sheikh@gmail.com](mailto:faraz.ar.sheikh@gmail.com)  
Project page: [github.com/ItsFRZ/TicTacToe](https://github.com/ItsFRZ/TicTacToe)  
F-Droid listing: [f-droid.org/packages/com.itsfrz.tictactoe](https://f-droid.org/packages/com.itsfrz.tictactoe/)
