# Steam-Machine

Steam-Machine is a software application developed in Java 22 and JavaFX, designed for Windows. This software emulates the main functionalities of Steam, allowing you to view information about specific software, download them, uninstall them, and much more, with a user-friendly interface and various additional features. It is compatible with any Windows executable and can directly launch ROMs on RetroArch.

Since it was not created for distribution, the code is highly confusing and poorly optimized.

[UI.webm](https://github.com/user-attachments/assets/fb31f373-8214-4fda-8b5b-ed7cab62d1fa)

## Main Features

* Complete Software Management: Allows you to view details about software, download them, and uninstall them conveniently.
* Compatibility: Supports any Windows executable and can launch ROMs on RetroArch.
* Integration with Steam: Retrieves software details directly from Steam, including descriptions, images, videos, and banners.
* Free External Database: Uses a Google Sheets page as a database for details on custom software not available on Steam and for download links.
* Direct Link Support: Works only with direct links that support header information, but a webcrawler search system can be added.
* Automatic Update System: Includes an automatic update system, via an external Java application not included in this repository.
* Download Management:
  - Pause and Resume: Ability to pause and resume downloads.
  - Sortable Queue: Organizes downloads in a sortable queue.
  - Download Recovery: Recovers downloads from where they were interrupted, even after restarting the software.
  - Security: Various systems to ensure download completion and management of partial files.
* File Management:
  - .rar File Support: Currently supports only .rar files with a maximum size of 5 GB. Larger files must be partitioned into 5 GB archives.
  - Partition Management: Fully extracted partitions are deleted to optimize disk space.

And a lot more, the project status is: complete, all that could be seen and doesn't need to be is completed and functional.

## Requirements

* Java 22: Ensure you have Java 22 installed on your system.
* Windows: The software is designed to run on Windows systems.
* Google Sheets Access: The software requires access to the configured Google Sheets page for the database.

## Limitations

Modification and Distribution: The software is intended for demonstration purposes only. It should not be modified, distributed, or used beyond educational purposes. For security reasons, the launcher class has been removed.
For further details, please refer to the LICENSE file.

## Contributions

As the software is intended solely for exercise purposes and should not be modified, external contributions are not currently accepted.

For more information, support, or questions, you can contact us at @mm_winther on Discord.
