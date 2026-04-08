// import Link from "next/link"

// export const metadata = {
//   title: "SmartBuoy - Home",
// }

// export default function HomePage() {
//   return (
//     <main className="page">
//       <h1 className="h1 mb-6">Welcome to SmartBuoy</h1>

//       <p className="card container-wide text-lg leading-relaxed text-gray-700 mb-10">
//         SmartBuoy is an intelligent buoy system that monitors marine conditions in real-time,
//         helping researchers, fishermen, and environmental agencies protect ocean ecosystems.
//       </p>

//       <div className="flex gap-4 mt-8">
//         <a
//           href="https://github.com/laurenmonahan16/smartbuoy-webapp"
//           target="_blank"
//           rel="noopener noreferrer"
//           className="btn btn-primary"
//         >
//           View on GitHub
//         </a>

//         <Link href="/vision" className="btn btn-primary">
//           Our Vision
//         </Link>

//         <Link href="/contact" className="btn btn-primary">
//           Contact Us
//         </Link>

//         <Link href="/dashboard" className="btn btn-primary">
//           Dashboard
//         </Link>
//       </div>

//       <footer>SmartBuoy Team: Empowering Smarter Oceans</footer>
//     </main>
//   )
// }

import Link from "next/link"

export const metadata = {
  title: "SmartBuoy - Home",
}

function Feature({ title, desc }: { title: string; desc: string }) {
  return (
    <div className="card p-6">
      <div className="text-base font-semibold text-slate-900 dark:text-white">{title}</div>
      <div className="mt-2 text-sm leading-relaxed text-slate-600 dark:text-slate-300">{desc}</div>
    </div>
  )
}

function Step({ num, title, desc }: { num: string; title: string; desc: string }) {
  return (
    <div className="rounded-2xl border border-blue-100 bg-white/60 p-6 shadow-sm dark:border-slate-700 dark:bg-slate-950/30">
      <div className="flex items-center gap-3">
        <div className="inline-flex h-8 w-8 items-center justify-center rounded-xl bg-blue-600 text-sm font-semibold text-white">
          {num}
        </div>
        <div className="text-sm font-semibold text-slate-900 dark:text-white">{title}</div>
      </div>
      <div className="mt-3 text-sm leading-relaxed text-slate-600 dark:text-slate-300">{desc}</div>
    </div>
  )
}

export default function HomePage() {
  return (
    <div className="relative">
      {/* Background accents */}
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute left-1/2 top-[-120px] h-[380px] w-[680px] -translate-x-1/2 rounded-full bg-blue-200/40 blur-3xl dark:bg-blue-500/10" />
        <div className="absolute left-[-120px] top-[240px] h-[320px] w-[320px] rounded-full bg-cyan-200/40 blur-3xl dark:bg-cyan-500/10" />
        <div className="absolute right-[-120px] top-[520px] h-[320px] w-[320px] rounded-full bg-indigo-200/40 blur-3xl dark:bg-indigo-500/10" />
      </div>

      <section className="mx-auto max-w-6xl px-6 py-14 md:py-20">
        {/* Hero */}
        <div className="grid items-center gap-10 md:grid-cols-2">
          <div>
            <div className="inline-flex items-center gap-2 rounded-full border border-blue-200 bg-white/70 px-3 py-1 text-xs font-medium text-slate-700 shadow-sm backdrop-blur dark:border-slate-700 dark:bg-slate-950/40 dark:text-slate-200">
              Built for fishermen
              <span className="mx-1 text-slate-400">•</span>
              Track your buoys in real time
            </div>

            {/* NEW: brand wordmark */}
            <div className="brand-script mt-6 text-3xl font-semibold text-blue-700 dark:text-blue-300 md:text-4xl">
              SmartBuoy
            </div>

            <h1 className="mt-3 text-4xl font-semibold tracking-tight text-slate-900 dark:text-white md:text-5xl">
              Know where your buoys are —<span className="text-blue-700 dark:text-blue-300"> anytime</span>.
            </h1>

            <p className="mt-4 max-w-xl text-base leading-relaxed text-slate-600 dark:text-slate-300">
              SmartBuoy is a monitoring device you attach to your lobster trap buoy. It helps you
              track location and conditions so you can recover gear faster, avoid losses, and fish smarter.
            </p>

            {/* Removed: hero buttons */}

            <div className="mt-8 flex flex-wrap gap-6 text-sm text-slate-600 dark:text-slate-300">
              <div>
                <div className="font-semibold text-slate-900 dark:text-white">GPS tracking</div>
                <div>See buoy position</div>
              </div>
              <div>
                <div className="font-semibold text-slate-900 dark:text-white">Temp + pressure</div>
                <div>Monitor conditions</div>
              </div>
              <div>
                <div className="font-semibold text-slate-900 dark:text-white">Entanglement alerts</div>
                <div>Detect unusual movement</div>
              </div>
            </div>
          </div>

          {/* Product “card” */}
          <div className="card p-6 md:p-8">
            <div className="text-sm font-semibold text-slate-900 dark:text-white">What you’ll see</div>

            <div className="mt-4 grid gap-3">
              <div className="flex items-start gap-3 rounded-xl border border-blue-100 bg-white/60 p-4 dark:border-slate-700 dark:bg-slate-950/30">
                <div className="mt-0.5 h-2.5 w-2.5 rounded-full bg-blue-600" />
                <div>
                  <div className="text-sm font-semibold text-slate-900 dark:text-white">
                    Live location on a map
                  </div>
                  <div className="text-sm text-slate-600 dark:text-slate-300">
                    Track your buoy position and view movement over time.
                  </div>
                </div>
              </div>

              <div className="flex items-start gap-3 rounded-xl border border-blue-100 bg-white/60 p-4 dark:border-slate-700 dark:bg-slate-950/30">
                <div className="mt-0.5 h-2.5 w-2.5 rounded-full bg-blue-600" />
                <div>
                  <div className="text-sm font-semibold text-slate-900 dark:text-white">
                    Conditions at a glance
                  </div>
                  <div className="text-sm text-slate-600 dark:text-slate-300">
                    Temperature and pressure readings help you understand conditions offshore.
                  </div>
                </div>
              </div>

              {/* NEW: Entanglement feature */}
              <div className="flex items-start gap-3 rounded-xl border border-blue-100 bg-white/60 p-4 dark:border-slate-700 dark:bg-slate-950/30">
                <div className="mt-0.5 h-2.5 w-2.5 rounded-full bg-blue-600" />
                <div>
                  <div className="text-sm font-semibold text-slate-900 dark:text-white">
                    Wildlife entanglement detection
                  </div>
                  <div className="text-sm text-slate-600 dark:text-slate-300">
                    SmartBuoy can flag unusual motion patterns that may indicate an entanglement event,
                    helping support safer marine operations.
                  </div>
                </div>
              </div>
            </div>

            {/* Removed: note at bottom */}
          </div>
        </div>

        {/* Features */}
        <div className="mt-14">
          <div className="text-sm font-semibold text-slate-900 dark:text-white">Why it helps</div>
          <div className="mt-4 grid gap-4 md:grid-cols-3">
            <Feature
              title="Find your gear faster"
              desc="If a buoy drifts, you can quickly see where it went instead of searching blindly."
            />
            <Feature
              title="Track multiple buoys"
              desc="Manage several buoys from one place and switch between them in seconds."
            />
            <Feature
              title="Support safer marine operations"
              desc="Entanglement detection can help surface abnormal movement patterns for faster awareness and response."
            />
          </div>
        </div>

        {/* How it works (fisherman version) */}
        <div className="mt-14">
          <div className="card p-6 md:p-8">
            <div className="text-sm font-semibold text-slate-900 dark:text-white">How it works</div>
            <div className="mt-5 grid gap-4 md:grid-cols-4">
              <Step num="1" title="Attach" desc="Mount the SmartBuoy device on your buoy before you head out." />
              <Step num="2" title="Deploy" desc="Set your traps as usual. SmartBuoy starts recording and updating." />
              <Step
                num="3"
                title="Monitor"
                desc="Log in to view your buoys and see location + conditions from the dashboard."
              />
              <Step
                num="4"
                title="Recover"
                desc="Use the map to find buoys faster and trace movement if conditions shift."
              />
            </div>

            {/* Removed: buttons at bottom */}
          </div>
        </div>

        {/* Small CTA (optional, subtle) */}
        <div className="mt-10 text-center">
          <p className="text-sm text-slate-600 dark:text-slate-300">
            Want to see the current prototype UI?{" "}
            <Link href="/dashboard" className="font-medium text-blue-700 hover:text-blue-800 dark:text-blue-300" target="_blank" rel="noopener noreferrer">
              View the demo dashboard →
            </Link>
          </p>
        </div>
      </section>
    </div>
  )
}