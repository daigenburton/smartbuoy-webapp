## SmartBuoy Web App
The SmartBuoy project aims to modernize the centuries-old practice of lobster fishing by integrating IoT sensors, AI analytics, and cloud-connected web tools to provide real-time insight into trap conditions, environmental data, and potential marine wildlife entanglement events. Traditional lobster buoys and traps have remained largely unchanged for generations, leaving fishermen blind to their traps’ status once deployed. These traps not only risk being lost to rough seas or tampering but also contribute to marine entanglement that threatens protected species such as whales and turtles.

SmartBuoy addresses these issues through a modular, waterproof, and affordable buoy-mounted system that continuously monitors environmental and positional data, transmitting it via LTE to a secure cloud server. From there, fishermen and researchers can access data through a responsive web app interface, allowing them to view trap locations, environmental conditions, and alerts in real time.

### Core Functionalities 
- Entanglement & Tampering Detection
- Environmental Sensing
- Location Tracking & Homing
- Real-Time Cloud Sync
- Multi-Buoy Mangement 

### Tech Stack
- Next.js (App Router, TypeScript, Tailwind)
- API: SmartBuoy backend
- Docker + Docker Compose for local dev & prod build
- ESLint for code quality

### Run for the First Time 
From the project root, run these commands:
```bash
#build container image
docker-compose build

#create container instance
docker-compose create

#start the application
docker-compose start 
```
After a few moments, open:
http://localhost:3000/

You will see the SmartBuoy web dashboard running locally in a containerized environment. 