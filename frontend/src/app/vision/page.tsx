import Link from "next/link"

export const metadata = {
  title: "SmartBuoy - Our Vision",
}

export default function Vision() {
  return (
    <div className="page">
      <h1 className="h1 mb-10">Smarter Fishing, Safer Oceans</h1>

      <div className="card container-wide text-lg leading-relaxed text-gray-700">
        <p className="mb-5">
          At SmartBuoy, we’re redefining the future of lobster fishing—making it smarter, safer, and
          more sustainable. For decades, fishermen have faced two major challenges: the risk of
          entanglement with marine wildlife and the inability to monitor traps in real time.
        </p>

        <p className="mb-5">
          Our vision is to bring technology and tradition together to protect the ocean and empower
          those who depend on it. The SmartBuoy is a rugged, buoy-mounted system that combines
          advanced sensors, wireless connectivity, and cloud-based data services to give fishermen
          immediate insight into trap conditions.
        </p>

        <p className="mb-5">
          Each buoy tracks GPS location, water temperature, and tidal movement while detecting signs
          of tampering or entanglement. Real-time data is transmitted via LTE to our web platform,
          where users can monitor multiple buoys, receive instant alerts, and locate traps with
          homing features. Built to endure harsh marine environments, the SmartBuoy operates for up
          to seven days without recharge and integrates seamlessly with existing gear.
        </p>

        <p>
          By enhancing fishing efficiency and promoting marine conservation, SmartBuoy is solving a
          problem that costs both livelihoods and marine life.
        </p>
      </div>

      <Link href="/home" className="btn btn-primary mt-10">
        Back to Home
      </Link>
    </div>
  )
}
