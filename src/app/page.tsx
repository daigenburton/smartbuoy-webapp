"use client";
import Image from "next/image";
import { useState } from "react";


export default function Home() {
  
  const [showContact, setShowContact] = useState(false);
  const [showVision, setShowVision] = useState(false);

  if (showContact) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-100 text-center px-8 py-16">
        <h1 className="italic text-5xl font-semibold text-blue-700 mb-10 drop-shadow-sm">
          Contact Us
        </h1>
  
        <div className="max-w-2xl text-lg text-gray-700 leading-relaxed bg-white shadow-md rounded-2xl p-10 border border-blue-100">
          <p className="mb-3">Lauren Monahan: monahan2@bu.edu</p>
          <p className="mb-3">Nandana Alwarappan: nmalwa26@bu.edu</p>
          <p className="mb-3">Daigen Burton: daigenb@bu.edu</p>
          <p className="mb-3">Sterling Wodzro: sywodrzo@bu.edu</p>
          <p>Benjamin Dekan: bdekan@bu.edu</p>
        </div>
  
        <button
          className="mt-10 px-6 py-3 bg-blue-600 text-white rounded-full font-medium shadow hover:bg-blue-700 transition-all duration-300"
          onClick={() => setShowContact(false)}
        >
          Back to Home
        </button>
      </div>
    );
  }
  

  if (showVision) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-100 text-center px-8 py-16">
        <h1 className="italic text-5xl font-semibold text-blue-700 mb-10 drop-shadow-sm">
          Smarter Fishing, Safer Oceans
        </h1>
  
        <div className="max-w-4xl text-lg text-gray-700 leading-relaxed text-left bg-white shadow-md rounded-2xl p-10 border border-blue-100">
          <p className="mb-5">
            At SmartBuoy, we’re redefining the future of lobster fishing—making it
            smarter, safer, and more sustainable. For decades, fishermen have faced
            two major challenges: the risk of entanglement with marine wildlife and
            the inability to monitor traps in real time.
          </p>
  
          <p className="mb-5">
            Our vision is to bring technology and tradition together to protect
            the ocean and empower those who depend on it. The SmartBuoy is a rugged,
            buoy-mounted system that combines advanced sensors, wireless connectivity,
            and cloud-based data services to give fishermen immediate insight into
            trap conditions.
          </p>
  
          <p className="mb-5">
            Each buoy tracks GPS location, water temperature, and tidal movement while
            detecting signs of tampering or entanglement. Real-time data is transmitted
            via LTE to our web platform, where users can monitor multiple buoys, receive
            instant alerts, and locate traps with homing features. Built to endure harsh
            marine environments, the SmartBuoy operates for up to seven days without
            recharge and integrates seamlessly with existing gear.
          </p>
  
          <p>
            By enhancing fishing efficiency and promoting marine conservation, SmartBuoy
            is solving a problem that costs both livelihoods and marine life.
          </p>
        </div>
  
        <button
          className="mt-10 px-6 py-3 bg-blue-600 text-white rounded-full font-medium shadow hover:bg-blue-700 transition-all duration-300"
          onClick={() => setShowVision(false)}
        >
          Back to Home
        </button>
      </div>
    );
  }  
  

  return (

    <main className="flex flex-col items-center justify-center min-h-screen bg-gradient-to-b from-blue-50 to-blue-100 text-center text-gray-800 font-sans p-8">
      <h1 className="text-5xl font-bold text-blue-700 mb-6">
        Welcome to SmartBuoy
      </h1>

      <p className="text-lg max-w-2xl mb-10">
        SmartBuoy is an intelligent buoy system that monitors marine conditions in real-time,
        helping researchers, fishermen, and environmental agencies protect ocean ecosystems.
      </p>
      
    
    <div className="flex flex-col gap-4 text-base font-medium sm:flex-row">
    <a
          href="https://github.com/laurenmonahan16/smartbuoy-webapp"
          target="_blank"
          rel="noopener noreferrer"
          className="px-6 py-3 bg-blue-600 text-white rounded-full font-medium hover:bg-blue-700 transition-colors"
        >
          View on GitHub
        </a>

    <button
      className="px-6 py-3 bg-blue-600 text-white rounded-full font-medium hover:bg-blue-700 transition-colors"
      onClick={() => setShowContact(true)}
    >
      Contact Us
    </button>

    <button
          className="w-[158px] px-6 py-3 bg-blue-600 text-white rounded-full font-medium hover:bg-blue-700 transition-colors"
          onClick={() => setShowVision(true)}
        >
          Our Vision
        </button>
      
    </div>

    <footer className="mt-16 text-sm text-gray-500">
        © {new Date().getFullYear()} SmartBuoy Team: Empowering Smarter Oceans
      </footer>
    </main>
  );

}