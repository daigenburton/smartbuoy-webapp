import Link from "next/link"

export const metadata = {
  title: "SmartBuoy - Contact",
}

export default function Contact() {
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

      <Link href="/home">
        <button className="mt-10 px-6 py-3 bg-blue-600 text-white rounded-full font-medium shadow hover:bg-blue-700 transition-all duration-300">
          Back to Home
        </button>
      </Link>
    </div>
  )
}
