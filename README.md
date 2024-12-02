# coffee1-events


## Overview

- [About](#About)
- [Setup](#Setup)
- [Screenshots](#Screenshots)

## About

This is team **coffee1**'s community event app made for CMPUT301 Fall 2024.

The Wiki page contains detailed information about this project, including CRC Cards, a UML diagram, a User Interface Mockup, and a timeline 
containing details about when each User Story was completed.

## Setup

1. [Set up a firebase project](https://firebase.google.com/docs/functions/get-started?gen=2nd)

2. Download the *google-services.json* file and place it in the **/app** directory of the project

3. Deploy cloud functions
```bash
firebase login
cd functions/
npm install
sudo apt install node-typescript
cd ..
firebase deploy --only functions
```

## Screenshots
