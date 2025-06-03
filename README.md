## Project group members:
Elia Cekici
Kristi Xhumbi

## Project Description:
## MyUNYT

  MyUNYT is an Android app for students at the University of New York in Tirana (UNYT). 
  It allows users to log in and access various student-oriented features, such as transportation schedules, 
  the academic calendar, professor voting, Instagram posts, and more.
---

## Features

- **Login/Authentication**  
  Users sign in with their email, password and username. Credentials are currently verified against a real database.

- **Home Page**  
  Once logged in, the user can access the homepage. The user can interact with: Students Portal, My Schedule, Transportation, Academic Calendar, School's Library, Navigation Menu, Motivational quotes and Semester Progress.
- **Students Portal**: Opens the KION webpage..
- **My Schedule**: Allows users to create their own semester schedule.
- **Transportation**: Displays bus routes and schedules.
- **Academic Calendar**: Shows semester dates and holidays.
- **School's Library**: Opens the UNYT library website in a browser (demonstrates network connectivity).
- **Motivational Quotes**: Contains 15 saved quotes. Each day, a random quote is displayed.
- **Semester Progress**: Based on the academic year’s start and end dates, a progress bar shows the percentage of the school year completed.


- **Navigation Menu**
  Swipe from the left to open the menu that links to:
- **Vote Professors**:  Lets students rate or vote for professors (only available after schedule confirmation). 
    Once the user confirms their schedule, only the selected professors will appear on the voting page. 
    Students can vote once per semester.
- **Information**: Opens the UNYT website in a browser or WebView (demonstrates network communication).
- **Location**: Opens UNYT’s location on a map. Users can choose between the Main or East campus (also demonstrates network communication).
- **Instagram Posts**:  Fetches and displays recent images from the official UNYT Instagram account.
    

- **WebView / External Browser**  
  The “Information” button either launches an implicit ACTION_VIEW intent or loads a WebView to show
  https://www.unyt.edu.al. Because it actually fetches a remote webpage, this satisfies the project
  requirement to “present data from an external server.”


---

## Getting Started

### Prerequisites

- **Android Studio 4.x or higher**
- **Android SDK (API 21+)**
- **Internet access** (needed for loading the UNYT website.)

### Installation

1. **Clone this repository**
   ```bash
   git clone https://github.com/eliacekici/AndroidApp---MyUNYT.git
