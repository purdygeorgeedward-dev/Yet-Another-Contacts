# Fossify Contacts
<img alt="Logo" src="graphics/icon.webp" width="120" />

<a href='https://play.google.com/store/apps/details?id=org.fossify.contacts'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' height=80/></a> <a href="https://f-droid.org/packages/org.fossify.contacts/"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on-en.svg" alt="Get it on F-Droid" height=80/></a> <a href="https://apt.izzysoft.de/fdroid/index/apk/org.fossify.contacts"><img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyOnDroid" height=80/></a>

Introducing Fossify Contacts - the next evolution in contact management. Poised to redefine how you manage your contacts, our app combines simplicity with advanced features, tailored for both personal and professional use.  

🔍 **SMART SEARCH & FIELD CUSTOMIZATION:**  
Locate contacts quickly with our intelligent search feature. Customize visible fields, enjoy a user-friendly interface, and find contacts effortlessly, saving time and enhancing productivity.

✉️ **GROUP MANAGEMENT & COMMUNICATION:**  
Effortlessly manage contact groups for streamlined communication. Our app facilitates easy grouping for batch emails or SMS, with features to create favorite lists and rename groups, enhancing your organizational capabilities.

🔄 **RELIABLE BACKUP & EXPORT OPTIONS:**  
Ensure your contacts are always safe with our reliable backup system. Seamlessly export or import contacts in vCard format, making data migration and backup a breeze.

🌐 **OPEN-SOURCE TRANSPARENCY:**  
Built on an open-source platform, Fossify Contacts champions transparency and user trust. Access our code on GitHub and become part of a community that values privacy, openness, and collaborative improvement.

🖼️ **PERSONALIZED USER EXPERIENCE:**  
Customize your contact management with ease. Our app offers flexible settings and design options, allowing you to tailor the interface to your liking. Sort contacts, choose themes, and personalize your experience for maximum convenience.

🔋 **EFFICIENT & LIGHTWEIGHT:**  
Optimized for performance, Fossify Contacts is designed to be light on your device's resources. It not only organizes your contacts efficiently but also contributes to longer battery life, ensuring smooth operation.

🚀 **ADVANCED SYNCHRONIZATION:**  
Whether you choose to store your contacts locally or prefer syncing them across devices using different means, our app ensures a smooth, efficient, and secure management experience.

🔐 **PRIVACY-FIRST APPROACH:**  
Your contact information remains confidential with Fossify Contacts. We prioritize your privacy, ensuring your data is never shared with third-party apps.

🌙 **MODERN DESIGN & USER-FRIENDLY INTERFACE:**  
Enjoy a clean, modern design with a user-friendly interface. The app features material design themes and a supports dynamic theming, providing a visually appealing and comfortable user experience.

Download the app now and elevate your contact management to new heights. Your journey to efficient, secure, and intuitive contact organization begins here.

➡️ Explore more Fossify apps: https://www.fossify.org<br>
➡️ Open-Source Code: https://www.github.com/FossifyOrg<br>
➡️ Join the community on Reddit: https://www.reddit.com/r/Fossify<br>
➡️ Connect on Telegram: https://t.me/Fossify

<div align="center">
<img alt="App image" src="fastlane/metadata/android/en-US/images/phoneScreenshots/1_en-US.png" width="30%">
<img alt="App image" src="fastlane/metadata/android/en-US/images/phoneScreenshots/2_en-US.png" width="30%">
<img alt="App image" src="fastlane/metadata/android/en-US/images/phoneScreenshots/3_en-US.png" width="30%">
</div>
---

## Fork changes (Yet-Another-Contacts)

- **Debounced contact/group search.** `onSearchQueryChanged()` was wired
  directly to the search box's text-changed callback with no debouncing
  anywhere in the chain - confirmed against Fossify Commons' actual
  `MySearchMenu` source, which invokes its callback immediately on every
  change with no delay. The filter itself scans every field (name,
  nickname, phone numbers, emails, addresses, IMs, notes, organization,
  websites) of every contact, then sorts the result - synchronously, on
  whichever thread calls it. For a large contact list, that's real,
  repeated, avoidable work on every single keystroke while typing,
  something that can visibly stall input. Added a 300ms debounce (only the
  delay before the existing logic runs - the logic itself is unchanged),
  plus cleanup on `onDetachedFromWindow()` so a pending debounced search
  can't fire ~300ms later against a view that's already been recycled by
  the ViewPager (harmless if it did - it'd just update an invisible view's
  own bindings, not a shared object - but pointless work worth skipping).

  Deliberately did **not** move the actual filtering/sorting work to a
  background thread in the same pass, even though that's the more complete
  fix - `contactsIgnoringSearch` is a plain `var` reassigned elsewhere on
  the main thread, and doing that safely means auditing every mutation
  site for a data race, which is real, separate work beyond a debounce.

  **Not verified on a real device** - reasoned from the actual call chain
  and confirmed against Commons' real source, not measured against a
  live large contact list, since this environment has neither a device
  nor test contact data at that scale.

- **Glossy "gel bubble" contact avatars** - contact list thumbnails that
  have no photo now get a glossy gradient circle with a specular highlight
  instead of a flat-color letter icon, matching the visual language of
  Yet-Another-Messages-App's gel bubble theme. `extensions/GelAvatar.kt`
  draws this on a `Canvas` (gradient body, darker rim, radial-gradient
  highlight, letter on top) rather than as a `Drawable`, since it needs to
  composite text the same way the function it replaces already does.
  Picked a vivid, Aqua-era 8-color palette (sky blue, emerald, hot pink,
  amber, purple, teal, coral, gold) - same hash-based per-contact color
  selection as Commons' own `getContactLetterIcon()` (same name always
  gets the same color), just a more vivid palette and gel rendering
  instead of a flat fill.

  Doesn't touch or wrap Commons' `getContactLetterIcon()` itself - that's
  compiled library code this app can't modify (confirmed it's a remote
  Gradle dependency, not a locally-vendored module, before deciding how to
  approach this).

  **Now covers every rendering call site, not just the main list.**
  Searched the codebase for every place a flat colored circle gets built
  and found three more: `SelectContactsAdapter` (the multi-select "pick
  contacts" screen) and `AutoCompleteTextViewAdapter` (the recipient
  autocomplete dropdown) reuse `createGelContactAvatar()` directly.
  `GroupsAdapter` needed its own `createGelGroupIcon()` - groups use a
  different Commons function (`getColoredGroupIcon()`, compositing
  `ic_group_circle_bg` + `ic_people_vector` rather than a letter) -
  fetched both from the real Commons source before replicating them,
  reusing the exact same people-icon glyph at the same inset the flat
  version used, so it reads as one consistent gel system rather than two
  different treatments. `GelAvatar.kt` was refactored to pull the shared
  gradient/rim/highlight drawing into one `drawGelCircleBase()` function
  both avatar and group-icon builders call, instead of duplicating it.

  Deliberately left **one** call site untouched: `getContactLetterIcon()`
  used for the launcher shortcut icon (pinning a contact to the home
  screen, in `ContactsAdapter.getShortcutImage()`) still uses the
  original flat icon - that one is subject to Android's own
  adaptive-icon masking on whatever launcher the user has, which is
  outside this app's control and riskier to introduce new
  gradient/highlight artwork into without a device to verify it on.

  **Not verified on a real device** - reasoned from the actual `Canvas`/
  `Paint`/`Shader` APIs (all pure platform, no dependency-availability
  question the way `androidx.core.graphics.ColorUtils` was for the
  Messages version), not confirmed against a live render.
