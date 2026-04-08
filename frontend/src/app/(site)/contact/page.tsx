import Link from "next/link"

export const metadata = {
  title: "SmartBuoy - Contact",
}

export default function Contact() {
  return (
    <div className="page">
      <h1 className="h1 mb-10">Contact Us</h1>

      <div className="card container-narrow text-lg leading-relaxed text-gray-700 space-y-3">
        <p>Lauren Monahan: monahan2@bu.edu</p>
        <p>Nandana Alwarappan: nmalwa26@bu.edu</p>
        <p>Daigen Burton: daigenb@bu.edu</p>
        <p>Sterling Wodzro: sywodrzo@bu.edu</p>
        <p>Benjamin Dekan: bdekan@bu.edu</p>
      </div>

      <Link href="/home" className="btn btn-primary mt-10">
        Back to Home
      </Link>
    </div>
  )
}
