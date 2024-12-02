# coffee1-events

## Deploy cloud functions

1. Set up a firebase project:
https://firebase.google.com/docs/functions/get-started?gen=2nd

2. Run:
```bash
firebase login
cd functions/
npm install
sudo apt install node-typescript
cd ..
firebase deploy --only functions
```

