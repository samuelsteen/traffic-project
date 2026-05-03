# Traffic Simulation (Java)

This project is a traffic simulation built in Java using Swing. It simulates cars moving through intersections with traffic lights, using threads to handle movement, timing, and updates.

---

## Features

- Multiple intersections
- Traffic lights that cycle (green → yellow → red)
- Cars that follow traffic rules
- Cars stop at red lights and avoid collisions
- Add cars and intersections dynamically
- Real-time clock
- Pause, resume, and stop controls
- Threaded system (cars, lights, and clock all run independently)

---

## How to Run

### Option 1 (Terminal)

1. Navigate to the project folder (You will need the JVM installed on your device)
2. Compile:  
   `javac *.java`  
3. Run:  
   `java Main`  

---

### Option 2 (VS Code)

1. Open the folder in VS Code  
2. Open `Main.java`  
3. Click Run  

---

## Controls

- Start – starts the simulation  
- Pause – pauses everything (cars, lights, clock)  
- Resume – continues the simulation  
- Stop – stops the simulation  
- Add Car – adds a new car  
- Add Intersection – adds another intersection  

---

## Notes

- The simulation uses miles per hour for speed and converts it into movement based on time  
- Distance between intersections is treated as 1000 meters  
- The system uses threads so cars, lights, and the clock all run at the same time  
- There is basic spacing logic to prevent cars from overlapping  

---

## Limitations

- No real collision physics  
- Cars move in straight lines only  
- No turning or lane changes  
- Visual overlap can happen in some cases  

---

## Author

Samuel Steen