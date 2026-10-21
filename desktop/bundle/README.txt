TERRAN VANGUARD @version@
=====================

A vertical shoot'em up set in 2185. This release holds Acts 1 to 3 of the
campaign's seven acts: twenty-one levels on easy, medium or hard. The later
acts follow in later releases.

Everything the game needs is in this folder: it brings its own Java runtime,
nothing has to be installed.


STARTING THE GAME
-----------------

Windows   Run terran-vanguard.exe.
          The game is not signed, so Windows SmartScreen may say "Windows
          protected your PC". Choose "More info", then "Run anyway". This is
          needed only on the first start.

macOS     Move "Terran Vanguard.app" to Applications (or anywhere you like)
          and open it.
          The game is not signed, so macOS refuses to open it the first time
          ("cannot be opened because the developer cannot be verified", or
          "is damaged"). Either right-click (Control-click) the app, choose
          Open and confirm, or open System Settings > Privacy & Security and
          choose "Open Anyway". If macOS still says the app is damaged, run
          this once in Terminal (adjust the path to where the app is):
              xattr -dr com.apple.quarantine "/Applications/Terran Vanguard.app"

Linux     Run ./terran-vanguard from this folder (64-bit x86, X11 or
          XWayland, OpenGL 3.2 or newer). If it does not start, make the
          file executable first: chmod +x terran-vanguard

The game starts in borderless full screen on the first start. Alt+Enter or
F11 switch between full screen and a window at any time.


CONTROLS
--------

Action                 Keyboard        Alternative   Gamepad
Move                   Arrow keys      W A S D       Left stick / D-pad
Fire (all weapons)     Space           Z             A / Cross, right trigger
Special                X               Left Ctrl     B / Circle
Precision (hold)       Left Shift      C             Right bumper
Pause                  Esc             P             Start
Full screen            Alt+Enter       F11           -
Screenshot             F12             -             -

Menus: arrow keys or D-pad to choose, Enter or A to confirm, Esc or B to go
back. The mouse works in every menu and in the hangar: point to choose, left
click to confirm, right click to go back, the wheel to scroll. In flight the
pointer is hidden and the ship flies by keyboard or gamepad only. Every key except Esc, Enter, Alt+Enter, F11 and F12 can be changed in
Options > Controls; auto-fire can be switched on there too.


SETTINGS, SAVES AND SCREENSHOTS
-------------------------------

The game keeps its settings file (settings.properties), the save slots
(saves/), the high scores (scores.yaml) and the F12 screenshots
(screenshots/) in one folder:

Windows   %APPDATA%\Terran Vanguard
macOS     ~/Library/Application Support/Terran Vanguard
Linux     ~/.config/terran-vanguard  (or $XDG_CONFIG_HOME/terran-vanguard)

Delete that folder to start over with the default settings and no saves.


LICENCE AND CREDITS
-------------------

LICENSE.txt holds the licence of the game (Apache License 2.0). CREDITS.md
lists every third-party file the game uses (sound effects, fonts) with its
author, source and licence; the ones that ask for credit are named on the
in-game credits screen too.
