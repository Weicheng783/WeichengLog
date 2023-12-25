# Weicheng Log (Journal App Series)

## Overview
"Weicheng Log" is a series of journaling apps developed for Android (planned for IOS and the others) using the latest Jetpack Compose framework. The apps are designed to help me manage daily activities, stay up-to-date with my life events, and promote a healthy and meaningful lifestyle.

## Downloads & Releases
Weicheng Log 231225 Beta (APK file): <a href="https://weicheng.app/cms/weicheng_log/downloads/231225.beta.apk">https://weicheng.app/cms/weicheng_log/downloads/231225.beta.apk</a>

## Screenshots
![Screenshots showing the main screen](android/screenshots_main.png "Screenshots showing the main screen (Android API 34)")

## Features

### 1. Intuitive User Interface
The app boasts a clean and intuitive user interface built with Jetpack Compose, providing a seamless and enjoyable user experience.

### 2. Daily Journaling
I can log my daily activities, thoughts, and experiences. The journaling feature encourages self-reflection and helps in maintaining a record of personal growth.

### 3. **Planned: Multimedia Support**
Enhance your journal entries by attaching images, videos, or voice recordings to capture the moment more vividly.

### 4. **Planned: Calendar View**
The calendar view, planned for future implementation, will allow me to navigate through my entries chronologically, making it easy to revisit and reflect on specific days.

### 5. **Planned: Mood Tracking**
In the future, I will be able to express my mood for the day using a simple and visually appealing mood tracker. Analyze patterns over time to better understand your emotional well-being.

### 6. Praise the Little Good Things in Life Column
Highlight and celebrate the small victories and positive moments in your life with the "Praise the Little Good Things in Life" column.

### 7. Selective Photo Browsing Card
Explore and relive memories with a selective photo browsing card, allowing you to showcase and revisit your favorite photos.

### 8. Jump Into Life
Discover the beautiful things you've overlooked by actively engaging with life and appreciating the moments that matter.

### 9. **Planned: Privacy and Security**
We take user privacy seriously. Your journal entries are securely stored, and the app ensures that your personal data remains confidential.

### 10. Multi-Language Support
The app offers multi-language support, allowing users to experience "Weicheng Log/伟程手记" in either English or Simplified Chinese.

# UI
The app was designed using [Material 3 guidelines](https://m3.material.io/).

The Screens and UI elements are built entirely using [Jetpack Compose](https://developer.android.com/jetpack/compose).

The app has two themes:

- Dynamic color - uses colors based on the [user's current color theme](https://material.io/blog/announcing-material-you) (if supported)
- Default theme - uses predefined colors when dynamic color is not supported

Each theme also supports dark mode.

The app uses adaptive layouts to
[support different screen sizes](https://developer.android.com/guide/topics/large-screens/support-different-screen-sizes).

## Getting Started

### Prerequisites
- Please check build.gradle.kts
- Jetpack Compose related skill set

### Installation
1. Clone the repository: `git clone https://github.com/Weicheng783/WeichengLog.git` or `git clone git@github.com:Weicheng783/WeichengLog.git`
2. Open the project in Android Studio.
3. Build and run the app on an emulator or physical device.

## Contributing
If you would like to contribute to the development of Weicheng Log, please follow our [contribution guidelines](CONTRIBUTING.md).

## License
This project is licensed under the [MIT License](LICENSE) - see the [LICENSE.md](LICENSE) file for details.

## Acknowledgments
We would like to express our gratitude to the open-source community and the contributors who have helped make Weicheng Log a reality.

Happy journaling! 📔✨