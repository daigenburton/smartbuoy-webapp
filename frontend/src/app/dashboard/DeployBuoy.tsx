"use client";

import { useState } from "react";

type Props = {
  buoyId: string;
  buttonLabel?: string;
};

export default function DeployBuoy({
  buoyId,
  buttonLabel = "Set Drift Range",
}: Props) {
  const [isOpen, setIsOpen] = useState(false);
  const [radius, setRadius] = useState(30);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function setDriftRange() {
    setLoading(true);
    setStatus(null);
    const numericBuoyId = Number(buoyId.split("-")[1]);

    try {
      const res = await fetch("/api/deploy", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          buoyId: numericBuoyId,
          allowedRadiusMeters: radius,
        }),
      });

      if (!res.ok) {
        const errorJson = await res.json();
        throw new Error(errorJson.error || "Failed to set drift range");
      }

      setStatus("Drift range updated successfully");
      setIsOpen(false);
      setTimeout(() => setStatus(null), 8000);
    } catch (err: any) {
      console.error(err);
      setStatus(err.message || "Error setting drift range");
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <button
        onClick={() => setIsOpen(true)}
        className="btn btn-primary h-[42px]"
        type="button"
      >
        {buttonLabel}
      </button>

      {isOpen && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-slate-900 p-6 rounded-lg w-96">
            <h2 className="text-lg font-semibold mb-2 text-white">Set Drift Range</h2>

            <label className="text-sm text-gray-300">
              How far can the buoy move from its deployment point? (meters)
            </label>

            <input
              type="number"
              min={1}
              value={radius}
              onChange={(e) => setRadius(Number(e.target.value))}
              className="mt-2 w-full px-3 py-2 rounded bg-slate-800 text-white"
            />

            <div className="flex justify-end gap-2 mt-4">
              <button
                onClick={() => setIsOpen(false)}
                className="px-3 py-2 text-sm text-gray-300"
                type="button"
              >
                Cancel
              </button>

              <button
                onClick={setDriftRange}
                disabled={loading}
                className="bg-blue-600 px-4 py-2 rounded text-white"
                type="button"
              >
                {loading ? "Saving..." : "Save Range"}
              </button>
            </div>
          </div>
        </div>
      )}

      {status && <p className="mt-2 text-sm">{status}</p>}
    </>
  );
}