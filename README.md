# ChessPad

## Overview

ChessPad is a mobile app that makes it easy to save/archive and add notes to games of chess
from Chess.com, useful for self-study and record keeping. It helps players of all levels: Beginners, 
Club Members, Hustlers, Titled Players, and Arbiters for recordkeeping in their games. Equipped with the
ability to store these games, it is a portable and resourceful tool that improves the ability to take 
notes and track progress/highlights over time.

## Features

*   **ChessPad Features**
    * Working search bar with calendar picker to find any user's previous games on Chess.com
    * Option to save these games in persistent storage, along with notes for each individual game
    * Ability to edit and delete added notes with a single button
    * Analytics feature to show recent/all-time win rates and other stats
    * Minimalistic Navbar for user to access both services seamlessly
    * Option to be redirected to the physical game/user on Chess.com, if more research is needed
    
## Tech Stack

### Database

Room: Room will be integrated into ChessPad due to its lightweight features, ability to store data
on one's device, and ease of use offline, or wherever the user is. For the purposes of this project,
it offers all the functionality that is needed.

### API

Chess.com API: Chess.com API is used to fetch data about players, as well as played games, for the 
user to add to their own collection for future review/use. The API allows users to retrieve information
about a player based on their user, and is non-invasive, since the information is public.

### Sensors

Voice: Voice recorded memos is open for note taking on each game, and can be used instead of adding
notes manually.

### Target Devices

*   Phones
*   Tablets

(Portability is a concern!)
