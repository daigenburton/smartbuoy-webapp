import Link from "next/link"

export const metadata = {
  title: "SmartBuoy - Home",
}

export default function HomePage() {
  return (
    <main className="page">
      <h1 className="h1 mb-6">Welcome to SmartBuoy</h1>

      <p className="card container-wide text-lg leading-relaxed text-gray-700 mb-10">
        SmartBuoy is an intelligent buoy system that monitors marine conditions in real-time,
        helping researchers, fishermen, and environmental agencies protect ocean ecosystems.
      </p>

      <div className="flex gap-4 mt-8">
        <a
          href="https://github.com/laurenmonahan16/smartbuoy-webapp"
          target="_blank"
          rel="noopener noreferrer"
          className="btn btn-primary"
        >
          View on GitHub
        </a>

        <Link href="/vision" className="btn btn-primary">
          Our Vision
        </Link>

        <Link href="/contact" className="btn btn-primary">
          Contact Us
        </Link>
      </div>

      <footer>SmartBuoy Team: Empowering Smarter Oceans</footer>
    </main>
  )
}
