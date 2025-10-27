import Link from 'next/link';

export const metadata = {
  title: 'SmartBuoy - Home'
};

export default function HomePage() {
  return (
    <main className="flex flex-col items-center justify-center min-h-screen bg-gradient-to-b from-blue-50 to-blue-100 text-center text-gray-800 font-sans p-8">
      <h1 className="text-5xl font-bold text-blue-700 mb-6">
        Welcome to SmartBuoy
      </h1>

      <p className="text-lg max-w-2xl mb-10">
        SmartBuoy is an intelligent buoy system that monitors marine conditions in real-time,
        helping researchers, fishermen, and environmental agencies protect ocean ecosystems.
      </p>


      <div className="flex gap-4 mt-8">
        <a href="https://github.com/laurenmonahan16/smartbuoy-webapp"
          target="_blank"
          rel="noopener noreferrer"
          className="px-6 py-3 bg-blue-600 text-white rounded-full font-medium hover:bg-blue-700 transition-colors"
        >
          View on GitHub
        </a>

        <Link href="/vision">
          <button className="px-6 py-3 bg-blue-600 text-white rounded-full font-medium shadow hover:bg-blue-700 transition-all duration-300">
            Our Vision
          </button>
        </Link>

        <Link href="/contact">
          <button className="px-6 py-3 bg-blue-600 text-white rounded-full font-medium shadow hover:bg-blue-700 transition-all duration-300">
            Contact Us
          </button>
        </Link>
      </div>

      <footer className="mt-16 text-sm text-gray-500">
        SmartBuoy Team: Empowering Smarter Oceans
      </footer>
    </main>
  );
        // copyright footer if we get copyrights
        // © {new Date().getFullYear()} SmartBuoy Team: Empowering Smarter Oceans
}