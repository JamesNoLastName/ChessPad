# ChessPad

## Overview

ChessPad is a mobile app that makes it easy to archive and take notes on games of chess 
for self-study and record keeping. It helps players of all levels: Beginners, Club Members, 
Hustlers, Titled Players, and Arbiters recordkeep for their games. Equipped with all the tools
a standard notepad has, it is a portable and resourceful tool that improves the ability to take 
notes for chess.

## Features

*   **Notepad Features** Keyboard with numbers 1-8 and letters a-h for recording chess move
    *   notation, main keyboard for note taking, bulleted lists for reminders, camera option,
    *   upload images option, marker option, AI analysis option.
*   **Folder Selection:** Create folders for each game to organize notes with each game, storing context and data.
*   **Sharing:** Creatable links to share chess studies with friends, with ability to make private and public studies
    
## Tech Stack

### Database

Firebase: Firebase will be integrated into ChessPad to provide real-time data synchronization, 
secure user authentication, and seamless cloud storage for game archives and notes. It enhances 
the app's performance by enabling fast, reliable access to stored games across multiple devices.

### API

OpenCV API: OpenCV SDK provides computer vision support for photo to board camera capture, enabling
ease of use for setting up board to specific positions.

OpenAI API: Provides AI generated description/analysis for the current position on board, and offers
suggestions/organizes taken notes on the game.

### Sensors

Camera: Used for taking photos of the chessboard to convert to the chess position.

Light Sensor: Used for adjusting brightness for taking photos of the chessboard.

GPS: Used for recording location of played games, to add context/record keeping
for game.

### Target Devices

*   Phones
*   Tablets
