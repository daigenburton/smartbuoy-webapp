"use client";

import { useState } from "react";

type Props = {
  buoyId: string;
};

export default function DeployBuoy({ buoyId }: Props) {
  const [isOpen, setIsOpen] = useState(false)
  const [radius, setRadius] = useState(30);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function deployBuoy() {
    setLoading(true);
    setStatus(null);
    const numericBuoyId = Number(buoyId.split("-")[1])

    try {
      const res = await fetch("http://localhost:8000/deploy", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          buoyId: numericBuoyId,
          allowedRadiusMeters: radius,
        }),
      });

      if (!res.ok) {
        const errorJson = await res.json();
        throw new Error(errorJson.error || "Deployment failed");
      }

      setStatus("Buoy deployed successfully");
      setIsOpen(false);
      setTimeout(() => setStatus(null),8000);

    } catch (err: any) {
        console.error(err);
        setStatus(err.message || "Error deploying buoy");
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <button
            onClick={() => setIsOpen(true)}
            className="px-6 py-2 rounded-md bg-blue-600 hover:bg-blue-700 text-white font-semibold border-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
            Deploy Buoy
        </button>

    {isOpen && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center">
            <div className="bg-slate-900 p-6 rounded-lg w-96">
            <h2 className="text-lg font-semibold mb-2">Deploy Buoy</h2>

            <label className="text-sm text-gray-300">
                How far can the buoy move from its deployment point? (meters)
            </label>

            <input
                type="number"
                value={radius}
                onChange={(e) => setRadius(Number(e.target.value))}
                className="mt-2 w-full px-3 py-2 rounded bg-slate-800 text-white"
            />

            <div className="flex justify-end gap-2 mt-4">
                <button onClick={() => setIsOpen(false)}>Cancel</button>
                <button
                onClick={deployBuoy}
                disabled={loading}
                className="bg-blue-600 px-4 py-2 rounded text-white"
                >
                    {loading ? "Deploying..." : "Confirm & Deploy"}
                </button>
            </div>
            </div>
        </div>
        )}

      {status && <p className="mt-2 text-sm">{status}</p>}
    </>
  );  
}